 package com.acme.fitness.webmvc.controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import com.acme.fitness.domain.exceptions.FitnessDaoException;
 import com.acme.fitness.domain.exceptions.StoreQuantityException;
 import com.acme.fitness.domain.orders.Basket;
 import com.acme.fitness.domain.products.Product;
 import com.acme.fitness.domain.users.User;
 import com.acme.fitness.orders.GeneralOrdersService;
 import com.acme.fitness.products.GeneralProductsService;
 import com.acme.fitness.users.GeneralUsersService;
 import com.acme.fitness.webmvc.web.ProductsManager;
 
 @Controller
 @RequestMapping("/aruhaz")
 public class WebShopController {
 
 	private static final Logger logger = LoggerFactory.getLogger(WebShopController.class);
 
 	@Autowired
 	private GeneralProductsService gps;
 
 	@Autowired
 	private GeneralOrdersService gos;
 
 	@Autowired
 	private GeneralUsersService gus;
 	
 	@Autowired
 	private ProductsManager productsManager;
 
 	@RequestMapping(value = "", method = RequestMethod.GET)
 	public String aruhaz() {
 		return "redirect:/aruhaz/1";
 	}
 
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String aruhazWithSlash() {
 		return "redirect:/aruhaz/1";
 
 	}
 
 	@RequestMapping(value = "/{page}", method = RequestMethod.GET)
 	public String setPage(Model model, @PathVariable String page, HttpServletResponse response, HttpServletRequest request) {
 
 		productsManager.addBasketToSessionIfExists(request, response, new ObjectMapper());
 		setPageNumberAndProducts(model, page);
 		return "aruhaz";
 	}
 
 
 	@RequestMapping(value = "/{page}/addToCart", method = RequestMethod.POST)
 	public String addProductToCart(@ModelAttribute("productId") long id, @ModelAttribute("quantity") int quantity, @PathVariable String page, HttpServletResponse response,
 			HttpServletRequest request) {
 		productsManager.addNewOrderItem(id, quantity, response, request, new ObjectMapper());
 		return "redirect:/aruhaz/" + page;
 	}
 
 	@RequestMapping(value = "/{page}/deleteBasket", method = RequestMethod.GET)
 	public String deleteBasket(@PathVariable String page, HttpServletRequest request, HttpServletResponse response, Model model) {
 		productsManager.deleteBasket(request, response);
 		setPageNumberAndProducts(model, page);
 		return "aruhaz";
 	}
 
 	@RequestMapping(value = "/{page}/confirmBasket", method = RequestMethod.GET)
 	public String confirmOrder(@PathVariable String page, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes, Model model) {
 
		if (getUserName().equals("anonymousUser")) {
			return failToCheckOut(page, redirectAttributes);
		} else {
			checkOutBasket(redirectAttributes, getBasketFromSession(request));
			return deleteBasket(page, request, response, model);
 		}
 	}
 
 	private void setPageNumberAndProducts(Model model, String page) {
 		int pageNumber = validatePageNumber(parsePageNumber(page), gps.getAllProduct().size());
 		model.addAttribute("products", getProductsOnPage(pageNumber));
 		model.addAttribute("pageNumber", pageNumber);
 	}
 
 	private Basket getBasketFromSession(HttpServletRequest request) {
 		return (Basket) request.getSession().getAttribute("productsInBasket");
 	}
 
 	private String failToCheckOut(String page, RedirectAttributes redirectAttributes) {
 		redirectAttributes.addFlashAttribute("message", "Termék rendeléséhez be kell jelentkezni!");
 		return "redirect:/aruhaz/" + page;
 	}
 
 	private void checkOutBasket(RedirectAttributes redirectAttributes, Basket basket) {
 		setBasketToUser(basket);
 		try {
 			gos.checkOutBasket(basket);
 			logger.info("Basket with id: " + basket.getId() + " has confirmed!");
 		} catch (StoreQuantityException e) {
 			addMissingProductsMessages(redirectAttributes, e.getProduct());
 		}
 	}
 
 	private void addMissingProductsMessages(RedirectAttributes redirectAttributes, List<Product> list) {
 		redirectAttributes.addFlashAttribute("missingProduct", list);
 		redirectAttributes.addFlashAttribute("message", "Egyes termékekből nincsen elegendő mennyiség. További információk a hiányzó termékek linken!");
 	}
 
 	private void setBasketToUser(Basket basket) {
 		try {
 			User user = gus.getUserByUsername(getUserName());
 			basket.setUser(user);
 		} catch (FitnessDaoException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private String getUserName() {
 		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 		return auth.getName();
 	}
 
 	private List<Product> getProductsOnPage(int pageNumber) {
 		int size = gps.getAllProduct().size();
 		List<Product> productsOnPage = new ArrayList<Product>();
 		if (size > 0)
 			productsOnPage = gps.getAllProduct().subList((pageNumber - 1) * 9, Math.min(pageNumber * 9, size));
 		return productsOnPage;
 	}
 
 	private int validatePageNumber(int pageNumber, int productSize) {
 		if (pageNumber < 1) {
 			pageNumber = 1;
 		} else if (pageNumber > (Math.ceil(productSize / 9.0))) {
 			pageNumber = (int) Math.ceil(productSize / 9.0);
 		}
 		return pageNumber;
 	}
 
 	private int parsePageNumber(String page) {
 		int pageNumber;
 		try {
 			pageNumber = Integer.parseInt(page);
 		} catch (NumberFormatException e) {
 			pageNumber = 1;
 		}
 		return pageNumber;
 	}
 }
