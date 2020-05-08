 package wed.site.app.controller;
 
import java.util.Map;

 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.core.userdetails.User;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
  
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
	public String login(@RequestParam Map<String,String> params, ModelMap model) {
		if(params.get("error") != null){
			model.addAttribute("error", "true");
		}
		
		if(params.get("logout") != null){
			model.addAttribute("logout", "true");
		}
 		return "login";
 	}
 	
 }
