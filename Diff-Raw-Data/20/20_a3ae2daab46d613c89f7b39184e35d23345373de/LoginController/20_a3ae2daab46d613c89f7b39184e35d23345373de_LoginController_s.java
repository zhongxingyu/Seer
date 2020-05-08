 package org.knetwork.webapp.controller;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 public class LoginController {
 	@RequestMapping(value = "/login", method = RequestMethod.GET)
 	public String login(ModelMap model) {

 		return "login/login";

 	}
 	
	@RequestMapping(value = "/user-features", method = RequestMethod.GET)
	public String useSite(ModelMap model) {

		return "logged-in";

 	}
 }
