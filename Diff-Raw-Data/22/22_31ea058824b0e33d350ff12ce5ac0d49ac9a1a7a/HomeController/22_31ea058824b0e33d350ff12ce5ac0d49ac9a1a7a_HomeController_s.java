 package com.pq.trends;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.mobile.device.Device;
 import org.springframework.mobile.device.site.SitePreference;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.pq.trends.domain.User;
 import com.pq.trends.services.UserService;
 
 /**
  * Handles requests for the application home page.
  */
 @Controller
 public class HomeController {
 	
 	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
 	
 	/**
 	 * Simply selects the home view to render by returning its name.
 	 */
 	@Autowired
 	UserService userservice;
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String home(Model model, Device device, SitePreference sitePreference) {
 		
 		 logger.info("SitePreference : " + sitePreference);
 	        logger.info("Device : " + device);
 	        userservice.addUsers(this.userstoadd()); 
 	        model.addAttribute("message", "Hello World!");
 		model.addAttribute("controllerMessage", "deviceIsMobile= " + device.isMobile() + " prefersMobile="
 				+ (sitePreference == SitePreference.MOBILE) );
 		model.addAttribute("controllerMessage1" ,  "deviceIsTablet= " + device.isTablet() + " prefersTablet="
 						+ (sitePreference == SitePreference.TABLET) );
 		model.addAttribute("controllerMessage2", "deviceIsNormal= " + device.isNormal() + " prefersNormal="
 								+ (sitePreference == SitePreference.NORMAL));
	sitePreference.isNormal();
<<<<<<< HEAD
 		User user = userservice.findUser("1","password");
=======
		User user = userservice.findUser("rajeev1","password");
>>>>>>> origin/HEAD
 		logger.info(user.toString());
 		model.addAttribute("FirstName", user.getFirstName() );
 		model.addAttribute("email", user.getEmail() );
 		model.addAttribute("users" ,userservice.getAllUsers());
 		
 		return "home";
 	}
 	
 	@RequestMapping(value = "/m/", method = RequestMethod.GET)
 	public String mobile(Model model, Device device, SitePreference sitePreference) {
 	    logger.info("Welcome mobile!");
 	   return home(model, device, sitePreference);
 	    //return "mobile";
 	}
 	
 	@RequestMapping(value = "/users", method = RequestMethod.GET)
 	public @ResponseBody Map<String,List<User>> allUsers (@RequestParam(value = "page", required = false, defaultValue = "1") int page,
 	@RequestParam(value = "max", required = false, defaultValue = "20" ) int max) {
 		//List<User> users =userservice.getAllUsers();
 		Map mp = new HashMap();
 		final List<User> userList =  userservice.getAllUsers();
 		final int startIdx = (page - 1) * max;
 		final int endIdx = Math.min(startIdx + max, userList.size());
 		//ArrayList<User> newUserList = new ArrayList<User>();
 		mp.put("rows", userList);
 		mp.put("page", 1);
 		return mp;
 		
 
 	}
 	
 	@RequestMapping(value = "/updateuser", method = RequestMethod.GET)
 	public String updateUser() {
 		 logger.info("@updateUser");
 		return "userupdate";
 	}
 	
 
 	@RequestMapping(value = "/adduser", method = RequestMethod.GET)
 	public String addUser() {
 		 logger.info("@addUser");
 		return "useradd";
 	}
 	
 	@RequestMapping(value = "/deleteuser", method = RequestMethod.GET)
 	public String deleteUser() {
 		 logger.info("@deleteuser");
 		return "userdelete";
 	}
 	
 	public List<User> userstoadd(){
 		List<User> ltu = new ArrayList<User>();
 		
 		
 		for(int i=0 ;i<100;i++){
 			
 			User u = new User(i , "rajeev" +i , "kumar"+i, "r-chopra@hotmail.com","password");
 			ltu.add(u);
 		}
 		return ltu;
 	}
 	
 }
