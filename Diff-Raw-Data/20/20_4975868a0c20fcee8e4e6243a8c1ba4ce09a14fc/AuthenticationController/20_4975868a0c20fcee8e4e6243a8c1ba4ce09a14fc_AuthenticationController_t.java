 package in.ac.vit.productrating.security;
 
 import org.apache.log4j.Logger;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 public class AuthenticationController {
 
	private transient Logger log = Logger
 			.getLogger(AuthenticationController.class);
 
 	@RequestMapping(value = "/login", method = RequestMethod.GET)
 	public String login() {
 		return "login";
 	}
 
	@RequestMapping(value = "/logout")
 	public String logout() {
		this.log.debug("logging out");
		return "redirect:j_spring_security_logout";
 	}
 
 	@RequestMapping(value = "/loginerror")
 	public String loginError() {
		this.log.error("Authentication Failed");
 		return "login";
 	}
 }
