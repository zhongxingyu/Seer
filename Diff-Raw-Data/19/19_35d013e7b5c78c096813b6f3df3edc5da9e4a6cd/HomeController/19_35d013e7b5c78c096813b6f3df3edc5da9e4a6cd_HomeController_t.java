 package com.jinengo.reporting.controller.web;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.jinengo.reporting.service.user.UserFiguresDao;
 
 /**
  * Handles requests for the application home page.
  */
 @Controller
 public class HomeController {
 	
 	@Autowired
 	private UserFiguresDao aggrUserFiguresDao;
 	
 	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
 	
 	/**
 	 * Mapping root url to home view an display welcome page
 	 * 
 	 * @return ModelAndView
 	 */
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public ModelAndView hello() {
 		ModelAndView modelAndView = new ModelAndView();
 		modelAndView.addObject("p", null);
 		modelAndView.setViewName("home");
 		
 		return modelAndView;
 	}
 	
 	/**
 	 * Mapping loggedout url to loggedout view
 	 * 
 	 * @return
 	 */
 	@RequestMapping(value = "/loggedout", method = RequestMethod.GET)
 	public String handleForm() {
 		return "helper/loggedout";
 	}
 	
 	/**
 	 * Mapping timeout url timeout view
 	 * 
 	 * @return
 	 */
 	@RequestMapping(value = "/timeout", method = RequestMethod.GET)
 	public String handleTimeout() {
		return "helper/timeout";
 	}	
 	
 	/**
 	 * Example Chart to show aggr user data from api
 	 * 
 	 * @return
 	 */
 	@RequestMapping(value = "/show-aggr-user", method = RequestMethod.GET)
 	public String showAggrUser() {
 		return "aggrUser";
 	}
 }
