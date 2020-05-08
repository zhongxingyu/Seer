 package controller;
 
import java.util.Map;

 import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
import service.UserService;

import entity.User;

 /**
  * Handles and retrieves the login or denied page depending on the URI template
  */
 @Controller
 @RequestMapping("/auth/*")
 public class LoginLogoutController {
         
 	protected static Logger logger = Logger.getLogger("controller");
 
	 @Autowired
	 private UserService userSvc;
	
 	/**
 	 * Handles and retrieves the login JSP page
 	 * 
 	 * @return the name of the JSP page
 	 */
 	@RequestMapping(value = "/login", method = RequestMethod.GET)
 	public String getLoginPage(@RequestParam(value="error", required=false) boolean error, 
 			ModelMap model) {
 		logger.debug("Received request to show login page");
 
 		// Add an error message to the model if login is unsuccessful
 		// The 'error' parameter is set to true based on the when the authentication has failed. 
 		// We declared this under the authentication-failure-url attribute inside the spring-security.xml
 		/* See below:
 		 <form-login 
 				login-page="/ihelp/auth/login" 
 				authentication-failure-url="/ihelp/auth/login?error=true" 
 				default-target-url="/ihelp/main/common"/>
 		 */
 		if (error == true) {
 			// Assign an error message
 			model.put("error", "You have entered an invalid username or password!");
 		} else {
 			model.put("error", "");
 		}
 		
 		// This will resolve to /WEB-INF/views/loginpage.jsp
 		return "loginpage";
 	}
 	
 	/**
 	 * Handles and retrieves the denied JSP page. This is shown whenever a regular user
 	 * tries to access an admin only page.
 	 * 
 	 * @return the name of the JSP page
 	 */
 	@RequestMapping(value = "/denied", method = RequestMethod.GET)
  	public String getDeniedPage() {
 		logger.debug("Received request to show denied page");
 		
 		// This will resolve to /WEB-INF/views/deniedpage.jsp
 		return "deniedpage";
 	}
	
	/**
	 * Handles and retrieves the register JSP page. 
	 * 
	 * @return the name of the JSP page
	 */
	@RequestMapping(value = "/register", method = RequestMethod.GET)
 	public String getRegisterPage(Map<String, Object> map) {
		logger.debug("Received request to show register page");
		 map.put("user", new User());
	        map.put("userList", userSvc.getUsers());
		// This will resolve to /WEB-INF/views/register.jsp
		return "register";
	}
	
 }
