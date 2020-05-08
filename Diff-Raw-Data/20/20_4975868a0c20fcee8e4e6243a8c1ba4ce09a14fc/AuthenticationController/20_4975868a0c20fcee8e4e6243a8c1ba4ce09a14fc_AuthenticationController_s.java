 package in.ac.vit.productrating.security;
 
 import org.apache.log4j.Logger;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 public class AuthenticationController {
 
	private static final Logger log = Logger
 			.getLogger(AuthenticationController.class);
 
 	@RequestMapping(value = "/login", method = RequestMethod.GET)
 	public String login() {
 		return "login";
 	}
 
 	public String logout() {
		return "login";
 	}
 
 	@RequestMapping(value = "/loginerror")
 	public String loginError() {
		log.error("Authentication Failed");
 		return "login";
 	}
 }
