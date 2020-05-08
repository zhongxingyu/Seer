 package com.fabiale.vegetarianguide.web;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import com.dropbox.client2.exception.DropboxException;
 import com.fabiale.vegetarianguide.model.Image;
 import com.fabiale.vegetarianguide.model.Restaurant;
 import com.fabiale.vegetarianguide.service.ImageService;
 import com.fabiale.vegetarianguide.service.RestaurantService;
 
 @Controller
 public class HomeController {
 	
 	@Autowired RestaurantService restaurantService;
 	@Autowired ImageService imageService;
 
 	@RequestMapping(value = "/", method = {RequestMethod.GET, RequestMethod.HEAD})
 	public String home(ModelMap modelMap) throws DropboxException {
 		modelMap.addAttribute("current", "home");
 		List<Restaurant> results = restaurantService.getLastUptades(3);
 		for (Restaurant restaurant : results) {
 			List<Image> images = imageService.getByRestaurant(restaurant, 1);
 			if(images!= null && !images.isEmpty())
 				restaurant.setImageUrl(images.iterator().next().getFilePath());
 		}
 		modelMap.addAttribute("restaurants", results);
 		return "index";
 	}
 	
 	@RequestMapping(value = "/error", method = RequestMethod.GET)
	public String error(ModelMap modelMap) {
		modelMap.addAttribute("warn", "page.not.found");
 		System.out.println("ERROR CONTROLLER");
		return "error";
 	}
 }
