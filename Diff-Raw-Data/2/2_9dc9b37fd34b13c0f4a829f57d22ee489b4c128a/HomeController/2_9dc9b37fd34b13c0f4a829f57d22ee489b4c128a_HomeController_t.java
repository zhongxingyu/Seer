 package com.slclassifieds.adsonline.web;
 
 import java.util.Locale;
 
 import org.hibernate.Query;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Bean;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import com.slclassifieds.adsonline.dao.UserDao;
 import com.slclassifieds.adsonline.dao.UserDaoImpl;
 import com.slclassifieds.adsonline.model.User;
 
 /**
  * Handles requests for the application home page.
  */
 @Controller
 public class HomeController {
 	
 	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
 	
 	
 	private UserDao userDao;
 	
 	@Autowired
 	public void setUserDao(UserDao userDaoImpl) {
 		this.userDao = userDaoImpl;
 	}
 	
 	
 	/**
 	 * Simply selects the home view to render by returning its name.
 	 */
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String home(Locale locale, Model model) {
 		
 		logger.info("Welcome home! The client locale is {}.", locale);
 		
		return "home";
 	}
 	
 	@RequestMapping(value = "/home", method = RequestMethod.GET)
 	public String homeMethod(Locale locale, Model model) {
 		
 		logger.info("Welcome home! The client locale is {}.", locale);
 		
 		return "home";
 	}
 	
 }
