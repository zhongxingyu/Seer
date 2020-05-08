 package com.rightmove.es.controller;
 
import org.apache.log4j.Logger;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 @Controller
 public class HomeController {
	
	private Logger log = Logger.getLogger(HomeController.class);
 
 	@RequestMapping("/")
 	public String showHomepage(){
		log.debug("home"); 
 		return "home";
 	}
 
 }
