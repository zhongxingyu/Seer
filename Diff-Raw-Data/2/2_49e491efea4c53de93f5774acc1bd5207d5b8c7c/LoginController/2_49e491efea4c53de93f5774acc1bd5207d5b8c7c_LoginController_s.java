 package org.netvogue.server.webmvc.controllers;
 
 import org.netvogue.server.webmvc.security.NetvogueUserDetailsService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.netvogue.server.neo4japi.domain.User;
 import org.netvogue.server.neo4japi.service.BoutiqueService;
 
 @Controller
 @SessionAttributes("user")
 public class LoginController  {
 
 	@Autowired BoutiqueService 				boutiqueService;
 	@Autowired NetvogueUserDetailsService	userDetailsService;
 	
 	@RequestMapping(value= {"/Netvogue.html", "/"}, method = RequestMethod.GET)
 	public String showForm(@RequestParam(value = "login_error", required = false) boolean error, Model model) {
 		
 		User user = userDetailsService.getUserFromSession();
 		if(null != user) {
 			System.out.println("User identified");
 			
 			//This needs to be checked if there is a better way to implement
 			return "redirect:home.htm";
 		}
 		System.out.println("User has not yet logged in");
 		if (error) {
 			model.addAttribute("error", "You have entered an invalid username or password!");
 			return "login";
 	    }
            return "Netvogue";
         }
 	
	@RequestMapping(value= {"/forgotpassword.html", "/"}, method = RequestMethod.GET)
        public String forgotPassword() {
 	  
 	  return "resend_Password";
 	}
 }
