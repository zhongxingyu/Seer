 package it.geosolutions.nrl.mvc;
 
 import it.geosolutions.nrl.utils.ControllerUtils;
 
 import java.security.Principal;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 public class WelcomePage {
 
 	/**
 	 * Home page
 	 * @param model
 	 * @param principal
 	 * @return
 	 */
 	@RequestMapping(value="/home", method = RequestMethod.GET)
 	public String printWelcome(ModelMap model, Principal principal ) {
 		
 		if(principal != null) {
 			String name = principal.getName();
 			model.addAttribute("userName", name);
 		}
 		model.addAttribute("context", "context/home");
 		ControllerUtils.setCommonModel(model);
 		return "template";
  
 	}
 
 	/**
 	 * Default controller
 	 * just a proxy to the real Home
 	 * @param model
 	 * @param principal
 	 * @return
 	 */
 	@RequestMapping(value="/", method = RequestMethod.GET)
 	public String landingPage(ModelMap model, Principal principal ) {
 		
		return "redirect:login";
  
 	}
 	
 }
