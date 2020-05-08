 package no.sag.ativitymanager.controller;
 
 import java.util.List;
 
 import javax.servlet.http.HttpSession;
 
 import no.sag.activitymanager.entity.User;
 import no.sag.activitymanager.repo.UserRepository;
 import no.sag.activitymanager.service.Role;
 import no.sag.activitymanager.service.Secure;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 public class HelloWorldController {
 
 	private static final Logger log = Logger.getLogger(HelloWorldController.class);
 	
 	@Autowired
 	UserRepository userRepository;
 	
 	public HelloWorldController() {
 		log.info("\n\n\n\nHelloWorldController created.\n\n\n\n");
 	}
 	
 	@Secure(role=Role.USER)
 	@ModelAttribute(value = "name")
 	@RequestMapping(value = "/hello", method = RequestMethod.GET)
	public ModelAndView getUsers(HttpSession session) {
 		
 		
 		String sessionId = session.getId();
 		log.info("*****************************************'");
 		log.info("SESSIONID: " + sessionId);
 		
 		log.info("getting users...");
 		
 		List<User> users = userRepository.getUsers();
 		for (User user : users) {
 			log.info(user.toString());
 		}
 		
		ModelAndView mav = new ModelAndView();
		mav.addObject("name", "Odelia");
 	
 		
		return mav;
 	}
 	
 }
