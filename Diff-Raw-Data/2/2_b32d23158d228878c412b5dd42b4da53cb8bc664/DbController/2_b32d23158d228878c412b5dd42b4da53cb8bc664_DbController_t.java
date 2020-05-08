 package com.bigu.testing;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.*;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.Locale;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.ServletRequestUtils;
 import org.springframework.web.bind.ServletRequestBindingException;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.stereotype.Controller;
 
 @RequestMapping("/db")
 @Controller
 
 public class DbController {
 
 	private static final Logger logger = LoggerFactory.getLogger(DbController.class);
 	@RequestMapping(value = "", method = RequestMethod.GET)
 	public String db(Locale locale, Model model){
 		logger.info("db page");
 		return "db";
 		}
 	/*
 	@RequestMapping(value = "/test", method = RequestMethod.POST)
 		public String test(HttpServletRequest request,Locale locale,Model model){
 		String test = "";
 		try{
 			test = ServletRequestUtils.getStringParameter(request, "test");
 		}
 		catch(ServletRequestBindingException e){
 			e.printStackTrace();
 		}
 		model.addAttribute("test",test);
 		logger.info("test page {}", test);	
 		return "test";
 			}
 	*/
 	@RequestMapping(value = "/test", method = RequestMethod.GET)
 	public String testConnection(Locale locale, Model model) throws SQLException {
 
 	try {
 	  Class.forName("com.mysql.jdbc.Driver");
 	} catch (ClassNotFoundException e) {
 	  System.out.println("Where is your MySQL JDBC Driver?");
 	  e.printStackTrace();
 	}
 
 	Connection conn = null;
 	try {
 	  conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/contacts","root", "");
 	} catch (SQLException e) {
 	  e.printStackTrace();
 	}
 
 	if (conn != null) {
 	  System.out.println("You made it, take control your database now!");
 	} else {
 	  System.out.println("Failed to make connection!");
 	}
 
 	logger.debug("inside index method");
	return "test";
 	}
 
 
 	
 }
