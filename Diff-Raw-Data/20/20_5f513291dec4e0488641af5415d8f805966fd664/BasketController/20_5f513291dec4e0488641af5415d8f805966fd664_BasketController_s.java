 package com.acme.fitness.webmvc.controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import com.acme.fitness.domain.exceptions.BasketCheckOutException;
 import com.acme.fitness.domain.exceptions.FitnessDaoException;
 import com.acme.fitness.domain.exceptions.StoreQuantityException;
 import com.acme.fitness.domain.orders.Store;
 import com.acme.fitness.domain.products.Product;
 import com.acme.fitness.domain.products.Training;
 import com.acme.fitness.orders.GeneralOrdersService;
 import com.acme.fitness.orders.simple.TrainingDateReservedException;
 import com.acme.fitness.webmvc.basket.BasketManager;
 
 @Controller
 @RequestMapping("kosar")
 public class BasketController {
 
 	@Autowired
 	private BasketManager basketManager;
 	
 	@Autowired
 	private GeneralOrdersService gos;
 
 	@RequestMapping("/torol")
 	public String deleteBasket(HttpServletRequest request, HttpServletResponse response) {
 		basketManager.deleteBasket(request, response);
 		return "redirect:"+redirectedFrom(request);
 	}
 	
 	@RequestMapping(value = "/rendel", method = RequestMethod.GET)
 	public String confirmOrder(HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes, Model model) {
 		try {
 			basketManager.checkOutBasket(response, request);
 		} catch (StoreQuantityException e) {
 			addMissingProductsMessages(redirectAttributes, e.getProduct());
 			return "redirect:/aruhaz/1";
 		} catch (BasketCheckOutException e) {
 			return failToCheckOut(redirectAttributes);
 		} catch (TrainingDateReservedException e ) {
 			addReservedTrainingsMessage(redirectAttributes, e.getReservedTrainings());
 			return "redirect:/edzesek";
 		}
 		redirectAttributes.addFlashAttribute("successCheckOut", "yeahh");
 		return "redirect:" + redirectedFrom(request);
 	}
 	
 	private String failToCheckOut(RedirectAttributes redirectAttributes) {
 		redirectAttributes.addFlashAttribute("message", "Termék rendeléséhez be kell jelentkezni!");
 		return "redirect:/aruhaz/1";
 	}
 	
 	private void addReservedTrainingsMessage(RedirectAttributes redirectAttributes, List<Training> reservedTrainings) {
 		redirectAttributes.addFlashAttribute("reservedTraining", reservedTrainings);
		redirectAttributes.addFlashAttribute("reservedMessage", "Egyes edzések időpontja már foglalt. További információk az alábbi linek");
 	}
 	
 	private void addMissingProductsMessages(RedirectAttributes redirectAttributes, List<Product> list) {
 		List<Store> stores = new ArrayList<Store>();
 		for(Product p : list) {
 			try {
 				stores.add(gos.getStoreByProduct(p));
 			} catch (FitnessDaoException e) {
 				e.printStackTrace();
 			}
 		}
 		redirectAttributes.addFlashAttribute("missingProduct", stores);
 		redirectAttributes.addFlashAttribute("message", "Egyes termékekből nincsen elegendő mennyiség a raktáron. További információk az alábbi linken!");
 	}
 	
 	private String redirectedFrom(HttpServletRequest request) {
 		String split[] = request.getHeader("referer").split(request.getContextPath());
 		return split[1];
 	}
 }
