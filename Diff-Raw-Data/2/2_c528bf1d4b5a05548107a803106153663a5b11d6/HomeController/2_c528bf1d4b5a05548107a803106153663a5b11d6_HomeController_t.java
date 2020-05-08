 package au.com.regimo.web;
 
 import java.security.Principal;
 
 import javax.inject.Inject;
 import javax.validation.Valid;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import au.com.regimo.core.domain.Dashboard;
 import au.com.regimo.core.domain.User;
 import au.com.regimo.core.repository.DashboardRepository;
 import au.com.regimo.core.service.UserService;
 import au.com.regimo.core.utils.SecurityUtils;
 import au.com.regimo.web.form.UserEntryForm;
 
 /**
  * Handles requests for the application home page.
  */
 @Controller
 public class HomeController {
 	
 	private UserService userService;
 
 	private DashboardRepository dashboardRepository;
 	
 	/**
 	 * Simply selects the home view to render by returning its name.
 	 */
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String home(ModelMap map, Principal user) {
 		if(user!=null && !SecurityUtils.isUserInRole("ADMIN")){
 			return "redirect:/profile";
 		}
 		return home(map);
 	}
 	
 	@RequestMapping(value = "/home", method = RequestMethod.GET)
 	public String home(ModelMap map) {
 		Dashboard content = dashboardRepository.findByViewName("HomeContent");
 		map.addAttribute("content", content);
 		return "home";
 	}
 	
 	@RequestMapping(value="/signin", method=RequestMethod.GET)
 	public void signin() {
 	}
 	
 	@RequestMapping(value="/profile", method=RequestMethod.GET)
 	public void viewProfile(ModelMap map) {
 		map.addAttribute("user",  SecurityUtils.getCurrentUser());
 	}
 	
 	@RequestMapping(value="/profile/edit", method=RequestMethod.GET)
 	public void editProfile(ModelMap map) {
 		map.addAttribute("user", SecurityUtils.getCurrentUser());
 	}
 
 	@RequestMapping(value = "/profile", method = RequestMethod.POST)
 	public String updateUser(@Valid UserEntryForm form,  ModelMap map) {
 		User user = userService.findOne(SecurityUtils.getCurrentUserId());
 		userService.save(form.getUpdatedUser(user));
 		SecurityUtils.updateCurrentUser(user);
		return "redirect:/profile";
 	}	
 
 	@Inject
 	public void setDashboardRepository(DashboardRepository dashboardRepository) {
 		this.dashboardRepository = dashboardRepository;
 	}
 	
 	@Inject
 	public void setUserService(UserService userService) {
 		this.userService = userService;
 	}
 	
 }
