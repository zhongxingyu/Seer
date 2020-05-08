 package wed.site.app.controller;
 
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.core.userdetails.User;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
  
 @Controller
 public class LoginController {
  
 	@RequestMapping(value="/", method = RequestMethod.GET)
 	public String printWelcome(ModelMap model) {
  
 		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 		String name = user.getUsername();
 	
 		model.addAttribute("view", "1st");
 		model.addAttribute("username", name);
 		return "pages/couple";
  
 	}
  
 	@RequestMapping(value="/login", method = RequestMethod.GET)
	public String login(ModelMap model) {
		System.out.println("login");
		return "login";
	}
	
	@RequestMapping(value="/loginfailed", method = RequestMethod.GET)
	public String loginerror(ModelMap model) {
 
		model.addAttribute("error", "true");
 		return "login";
 
	}
	
	@RequestMapping(value="/logout", method = RequestMethod.GET)
	public String logout(ModelMap model) {
		System.out.println("logout");
		return "login";
 
 	}
 	
 }
