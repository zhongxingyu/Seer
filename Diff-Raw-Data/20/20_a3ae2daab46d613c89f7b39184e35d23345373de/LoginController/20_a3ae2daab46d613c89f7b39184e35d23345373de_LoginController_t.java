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
 	
	@RequestMapping(value = "/loginfailed", method = RequestMethod.GET)
	public String loginFailed(ModelMap model) {
		model.addAttribute("failureMessage", "<div class='alert alert-error'><h3>Incorrect user name or password.</h3></div>");
		return "login/login";
 	}
 }
