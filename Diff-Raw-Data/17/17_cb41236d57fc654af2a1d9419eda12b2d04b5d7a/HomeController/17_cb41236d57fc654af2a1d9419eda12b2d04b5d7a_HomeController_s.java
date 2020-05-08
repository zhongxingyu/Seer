 package com.boredatlunch.whatsforlunch.Controller;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import javax.servlet.http.HttpSession;
 import javax.validation.Valid;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.boredatlunch.whatsforlunch.Model.LocationSearchForm;
 import com.boredatlunch.whatsforlunch.Model.Yelp.YelpResponse;
 import com.boredatlunch.whatsforlunch.Service.EmailNotificationServiceImpl;
 import com.boredatlunch.whatsforlunch.Service.SearchRestaurantServiceImpl;
 import com.boredatlunch.whatsforlunch.Util.ResponseMapperUtil;
 
 /**
  * Handles requests for the application home page.
  */
 @Controller
 public class HomeController {
 	@Autowired
 	SearchRestaurantServiceImpl searchRestaurantService;
 	
 	@Autowired
 	ResponseMapperUtil responseMapperUtil;
 	
 	@Autowired
 	EmailNotificationServiceImpl emailNotificationService;
 	
 	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
 	private List<String> selectedPollList = new ArrayList<String>();
 	
 	@RequestMapping(method = RequestMethod.GET)
 	public String home(Locale locale, final Model model, HttpSession session) {
 		LocationSearchForm locationSearchForm = new LocationSearchForm();
 		model.addAttribute("locationSearchForm", locationSearchForm);
 		session.setAttribute("locationSearchForm", locationSearchForm);
 		session.setAttribute("yelpResponse", new YelpResponse());
 		return ".home";
 	}
 	
 	@RequestMapping(method = RequestMethod.POST, params={"search"})
 	public String search(Locale locale, final Model model, @ModelAttribute("locationSearchForm")@Valid LocationSearchForm locationSearchForm, BindingResult bindingResult) {
 		YelpResponse response = null;
 		try {
 			if(bindingResult.hasErrors()) {
 				return ".home";
 			}
 			//Get JSON from service
 			String yelpResponse = searchRestaurantService.searchYelpByLocation(locationSearchForm.getSearchTerm(), locationSearchForm.getZipCode());
 			
 			//Get mapped object from mapper
 			response = responseMapperUtil.mapYelpResponse(yelpResponse);
 			
 			//Testing emails
 			//emailNotificationService.sendPollCreatedNotification("shravan.alur@gmail.com");
 		} catch (Exception e) {
 			logger.error("Something went wrong when trying to retrieve search results :" + e.getMessage());
 		}
 		model.addAttribute("yelpResponse", response);
 		return ".results";
 	}
 	
 	@RequestMapping(value="/addToPoll", method=RequestMethod.POST)
 	public @ResponseBody String addToPoll(Locale locale, final Model model, @RequestParam("id") String id) {
		String returnText = null;
 		int size = selectedPollList.size();
		System.out.println("List size before add is " + size);
 		selectedPollList.add(id);
 		int newSize = selectedPollList.size();
 		System.out.println("Adding " + id + " to the poll");
		System.out.println("List size after add is " + newSize);
 		if(newSize == size + 1) {
			returnText = "added";
 		}
		else {
			returnText = "not added";
		}
		return returnText;
 	}
 	
 	public SearchRestaurantServiceImpl getSearchRestaurantService() {
 		return searchRestaurantService;
 	}
 
 	public void setSearchRestaurantService(
 			SearchRestaurantServiceImpl searchRestaurantService) {
 		this.searchRestaurantService = searchRestaurantService;
 	}
 }
