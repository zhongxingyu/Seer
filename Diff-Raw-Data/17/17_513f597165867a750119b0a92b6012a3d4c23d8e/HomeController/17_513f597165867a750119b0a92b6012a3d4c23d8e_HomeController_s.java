 package edu.hust.k54.controller;
 
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import edu.hust.k54.persistence.*;
 
 /**
  * Handles requests for the application home page.
  */
 @Controller
 public class HomeController {
 	
 	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
 	
 	/**
 	 * Simply selects the home view to render by returning its name.
 	 */
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String home(Locale locale, Model model) {
 		logger.info("Welcome home! the client locale is "+ locale.toString());
 		
 		CapuyHome cui = new CapuyHome ();
 		cui.delete(new Capuy());
 		
 		Date date = new Date();
 		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
 		
 		String formattedDate = dateFormat.format(date);
 		
 		model.addAttribute("serverTime", formattedDate );
 		
 		return "home";
 	}
	
	public static void main(String[] args) {
		CapuyHome cui = new CapuyHome ();
		cui.delete(new Capuy());
	}
 }
