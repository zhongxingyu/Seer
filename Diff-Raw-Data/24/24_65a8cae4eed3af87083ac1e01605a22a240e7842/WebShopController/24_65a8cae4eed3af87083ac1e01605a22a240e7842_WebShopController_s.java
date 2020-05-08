 package com.acme.fitness.webmvc.controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.imageio.plugins.bmp.BMPImageWriteParam;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import com.acme.fitness.domain.exceptions.BasketCheckOutException;
 import com.acme.fitness.domain.exceptions.StoreQuantityException;
 import com.acme.fitness.domain.orders.Basket;
 import com.acme.fitness.domain.orders.OrderItem;
 import com.acme.fitness.domain.products.Product;
 import com.acme.fitness.products.GeneralProductsService;
 import com.acme.fitness.webmvc.basket.BasketManager;
 
 @Controller
 @RequestMapping("/aruhaz")
 public class WebShopController {
 
 	@Autowired
 	private GeneralProductsService gps;
 
 	@Autowired
 	private BasketManager basketManager;
 
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
 		basketManager.addBasketToSessionIfExists(request, response, new ObjectMapper());
 		basketManager.isAnonymousBasketIfUserLoggedIn(request, response, new ObjectMapper());
 		setPageNumberAndProducts(model, page);
 		return "aruhaz";
 	}
 
 	@RequestMapping(value = "/{page}/addToCart", method = RequestMethod.POST)
 	public String addProductToCart(@ModelAttribute("productId") long id, @ModelAttribute("quantity") int quantity, @PathVariable String page, HttpServletResponse response,
 			HttpServletRequest request) {
 		basketManager.addNewOrderItem(id, quantity, response, request, new ObjectMapper());
 		return "redirect:/aruhaz/" + page;
 	}
 
 	@RequestMapping(value = "/deleteBasket", method = RequestMethod.GET)
 	public String deleteBasketDefault(HttpServletRequest request, HttpServletResponse response, Model model) {
 		return deleteBasket("1", request, response, model);
 	}
 
 	@RequestMapping(value = "/{page}/deleteBasket", method = RequestMethod.GET)
 	public String deleteBasket(@PathVariable String page, HttpServletRequest request, HttpServletResponse response, Model model) {
 		basketManager.deleteBasket(request, response);
 		setPageNumberAndProducts(model, page);
 		return "aruhaz";
 	}
 
 	@RequestMapping(value = "/confirmBasket", method = RequestMethod.GET)
 	public String confirmOrderDefault(HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes, Model model) {
 		return confirmOrder("1", request, response, redirectAttributes, model);
 	}
 
 	@RequestMapping(value = "/{page}/confirmBasket", method = RequestMethod.GET)
 	public String confirmOrder(@PathVariable String page, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes, Model model) {

 		try {
 			basketManager.checkOutBasket(response, request);
 		} catch (StoreQuantityException e) {
 			addMissingProductsMessages(redirectAttributes, e.getProduct());
 		} catch (BasketCheckOutException e) {
 			return failToCheckOut(page, redirectAttributes);
 		}
 		setPageNumberAndProducts(model, page);
 		return "aruhaz";
 	}
 
 	@RequestMapping(value = "/torles/{productId}")
 	public String removeProduct(@PathVariable long productId, HttpServletRequest request, HttpServletResponse response) {
 		basketManager.removeProductFromBasket(productId, request, response);
 		return "redirect:/aruhaz/";
 	}
 
 	@RequestMapping(value = "/torles/anonymous/{productId}")
 	public String removeAnonymousProduct(@PathVariable long productId, HttpServletRequest request, HttpServletResponse response) {
 		basketManager.removeAnonymousProduct(productId, request, response);
 		return "redirect:/aruhaz/";
 	}
 
 	@RequestMapping(value = "/anonymKosar/torles")
 	public String deleteAnonymousBasket(HttpServletRequest request, HttpServletResponse response) {
 		basketManager.addBasketToSessionIfExists(request, response, new ObjectMapper());
 		basketManager.deleteAnonymousBasket(request, response);
 		request.getSession().removeAttribute("anonymousBasket");
 		return "redirect:/aruhaz";
 	}
 
 	@RequestMapping(value = "/anonymKosar/hozzaad")
 	public String mergeAnonymousBasket(HttpServletRequest request, HttpServletResponse response) {
 		String redirectTo = "";
 		basketManager.addAnonymousProductsBasketLoggedInUser(response, request, new ObjectMapper());
 		if (basketManager.isAnonymousBasketContainsMemberships(request, response, new ObjectMapper())) {
 			redirectTo = "redirect:/berletek/anonymKosar/hozzaad";
 		} else if (basketManager.isAnonymousBasketContainsTrainings(request, response, new ObjectMapper())) {
 			redirectTo = "redirect:/edzesek/anonymKosar/hozzaad";
 		} else {
 			request.getSession().removeAttribute("anonymousBasket");
 			redirectTo = "redirect:/aruhaz";
 		}
 		return redirectTo;
 	}
 
 	private void setPageNumberAndProducts(Model model, String page) {
 		int pageNumber = validatePageNumber(parsePageNumber(page), gps.getAllProduct().size());
 		model.addAttribute("products", getProductsOnPage(pageNumber));
 		model.addAttribute("pageNumber", pageNumber);
 	}
 
 	private String failToCheckOut(String page, RedirectAttributes redirectAttributes) {
 		redirectAttributes.addFlashAttribute("message", "Termék rendeléséhez be kell jelentkezni!");
 		return "redirect:/aruhaz/" + page;
 	}
 
 	private void addMissingProductsMessages(RedirectAttributes redirectAttributes, List<Product> list) {
 		redirectAttributes.addFlashAttribute("missingProduct", list);
 		redirectAttributes.addFlashAttribute("message", "Egyes termékekből nincsen elegendő mennyiség. További információk a hiányzó termékek linken!");
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
