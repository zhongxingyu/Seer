 package org.dedeler.template.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import org.dedeler.template.annotation.Logged;
 import org.dedeler.template.service.LoggingService.LogLevel;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 /**
  * Handles requests for the application home page.
  */
 @Logged(level = LogLevel.CONTROLLER)
 @Controller
 public class HomeController extends AbstractController {
 
 	@Value("${test.message}")
 	private String test;
 
 	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
 
 	/**
 	 * Simply selects the home view to render by returning its name.
 	 */
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	@ResponseBody
 	public List<String> home(Locale locale, Model model) {
 		logger.info("Welcome home! the client locale is " + locale.toString());
 		logger.info(test);
 
 		List<String> list = new ArrayList<String>();
 		list.add("hi");
 		list.add("slut");
 
 		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
 		list.add(authentication.getName());
 		for (GrantedAuthority auth: authentication.getAuthorities()) {
 			list.add(auth.getAuthority());
 		}
 		
 		return list;
 
 	}
 	
 	@RequestMapping(value = "/admin", method = RequestMethod.GET)
 	@ResponseBody
 	public List<String> admin(Locale locale, Model model) {
 		logger.info("Welcome home! the client locale is " + locale.toString());
 		logger.info(test);
 
 		List<String> list = new ArrayList<String>();
 		list.add("hi");
 		list.add("slut");
 
 		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
 		list.add(authentication.getName());
 		for (GrantedAuthority auth: authentication.getAuthorities()) {
 			list.add(auth.getAuthority());
 		}
 		
 		return list;
 
 	}
 
 
 }
