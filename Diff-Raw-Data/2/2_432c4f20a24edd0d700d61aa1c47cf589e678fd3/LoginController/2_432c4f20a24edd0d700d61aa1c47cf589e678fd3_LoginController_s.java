 /**
  * Copyright U-wiss
  */
 package com.pingpong.portal.controller;
 
 import com.pingpong.portal.command.LoginCommand;
 import com.pingpong.shared.AppService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * @author Artur Zhurat
  * @version 1.0
  * @since 01/02/2012
  */
 @Controller
 public class LoginController {
 	private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);
 	@Autowired
 	private AppService appService;
 
 	@RequestMapping("/login")
		public ModelAndView list() {
 			ModelAndView model = new ModelAndView("login/index");
 			model.addObject("login", new LoginCommand());
 
 			return model;
 		}
 }
