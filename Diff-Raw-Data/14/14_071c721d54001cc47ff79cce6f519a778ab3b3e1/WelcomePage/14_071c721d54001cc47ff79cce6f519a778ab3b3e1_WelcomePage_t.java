 package it.geosolutions.nrl.mvc;
 
import it.geosolutions.nrl.utils.ControllerUtils;

 import java.security.Principal;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 public class WelcomePage {
 
 	@RequestMapping(value="/welcome", method = RequestMethod.GET)
 	public String printWelcome(ModelMap model, Principal principal ) {
  
 		String name = principal.getName();
		ControllerUtils.setCommonModel(model);
 		model.addAttribute("context", "home");
 		return "template";
  
 	}
 	
 }
