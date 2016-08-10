package com.bitwise.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.catalina.connector.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.bitwise.database.Products;
import com.bitwise.exceptions.OutOfStockException;
import com.bitwise.helpers.Utility;
import com.bitwise.models.Cart;
import com.bitwise.models.Product;

@Controller
@RequestMapping ("/cart")
public class CartController {

	@RequestMapping (value = "/display", method = RequestMethod.GET)
	public ModelAndView displayCart (ModelMap model, HttpSession session) {
		if ( ( (Cart)session.getAttribute("cart")).getCartItems().isEmpty()  ) {
			return new ModelAndView("redirect:/products/home");
		}
		model.addAttribute("title", "Cart");
		model.addAttribute("cartActive", "active");
		return new ModelAndView("display", model);
	}
	
	@RequestMapping (value = "/add", method = RequestMethod.GET)
	public @ResponseBody String addItem (ModelMap model, 
			HttpServletRequest req, HttpServletResponse res,
			@RequestParam Integer pid) {
		
		List<Product> products = ((Products)req.getSession(false).getAttribute("products")).getList();
		int cartSize = addItemToCart(req, pid, products);
		String response = ""+cartSize;
		return response;
	}
	
	@RequestMapping (value = "/remove", method = RequestMethod.GET)
	public @ResponseBody String removeItem (ModelMap model, 
			HttpServletRequest req, HttpServletResponse res,
			@RequestParam Integer pid) {
//		List<Product> products = ((Products)req.getSession(false).getAttribute("products")).getList();
		List<Product> products = ((Cart)req.getSession(false).getAttribute("cart")).getCartItems();
		int cartSize = removeItemFromCartByProductID(req, pid, products);
		String response = ""+cartSize;
		return response;
	}
	
	@RequestMapping (value = "/size", method = RequestMethod.GET)
	public @ResponseBody String cartSize(HttpServletRequest req, HttpServletResponse res) {
		Cart cart = (Cart)req.getSession(false).getAttribute("cart");
		String cartSize = cart == null ? ""+0 : "" + cart.getCartSize();
		return cartSize;
	}
	
	@RequestMapping (value ="/order", method = RequestMethod.GET)
	public String order(ModelMap model, 
			HttpSession session, HttpServletRequest req) {
		session = req.getSession(false);
		Cart cart = (Cart)session.getAttribute("cart");
		if (cart.getCartItems().isEmpty())
			return "redirect:/products/home";
		double total = cart.calculateTotalPrice();
		List<Product> cartItems = cart.getCartItems();
		model.addAttribute("title", "Order");
		model.addAttribute("total", total);
		model.addAttribute("cartItems", cartItems);
		
		return "order";
	}
 
	private int removeItemFromCartByProductID(HttpServletRequest req, Integer pid, List<Product> products) {
		if (req.getSession(false).getAttribute("cart") != null ) {
			Cart cart = (Cart) req.getSession(false).getAttribute("cart");
			Product product = Utility.getItemFromGivenListByProductID(pid, products);
			cart.removeItem(Utility.getItemFromGivenListByProductID(pid, products));
			req.getSession(false).setAttribute("cart", cart);
			return cart.getCartSize();
		}
		return 0;
	}
	
	
//	public ModelAndView order(ModelMap model) {
//		model.addAttribute("title", "Place Order");
//	}

	private int addItemToCart(HttpServletRequest req, Integer pid, List<Product> products) {
		if (req.getSession(false).getAttribute("cart") == null) {
			Cart cart = new Cart();
			sellStoreItem(req, pid);
			cart.addItem(Utility.getItemFromGivenListByProductID(pid, products));
			req.getSession(false).setAttribute("cart", cart);
			return 1;
		} else {
			Cart cart = (Cart) req.getSession(false).getAttribute("cart");
			sellStoreItem(req, pid);
			cart.addItem(Utility.getItemFromGivenListByProductID(pid, products));
			req.getSession(false).setAttribute("cart", cart);
			return cart.getCartSize();
		}
	}

	private void sellStoreItem(HttpServletRequest req, Integer pid) {
		System.out.println("IN");
		HttpSession session = req.getSession(false);
		Products products = (Products)session.getAttribute("products");
		Product prod = products.getProductByProductID(pid, products.getList());
		System.out.println(prod.getStock()); // 10 // 9
		if ( (prod.getStock() - 1) < 0) {
			throw new OutOfStockException("Product Is Out Of Stock");
		}
		prod.setStock(prod.getStock() - 1); // 9 // 8
		System.out.println(prod.getStock()); // 9 // 8
		req.getSession(false).setAttribute("products", products);
	}

	private void deductStock(HttpServletRequest req, Products products, Product prod) {
		
	}
}
