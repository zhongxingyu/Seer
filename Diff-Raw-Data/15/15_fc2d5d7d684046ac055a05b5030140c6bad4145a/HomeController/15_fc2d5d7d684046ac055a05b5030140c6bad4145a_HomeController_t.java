 package au.com.regimo.web;
 
 import java.security.Principal;
 
 import javax.inject.Inject;
 import javax.validation.Valid;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import au.com.regimo.core.domain.Dashboard;
 import au.com.regimo.core.domain.User;
 import au.com.regimo.core.repository.DashboardRepository;
 import au.com.regimo.core.service.UserService;
 import au.com.regimo.core.utils.SecurityUtils;
 import au.com.regimo.web.form.UserEditForm;
 
 /**
  * Handles requests for the application home page.
  */
 @Controller
 public class HomeController {
 	
 	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
 	
 	@Inject private UserService userService;
 	private DashboardRepository dashboardRepository;
 
 	
 	/**
 	 * Simply selects the home view to render by returning its name.
 	 */
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String home(ModelMap map, Principal user) {
 	
 		if(user!=null){
 			User currentUser =SecurityUtils.getCurrentUser();
 			if(currentUser.hasRole("admin")){
 				System.out.println("current user is admin");
 				return "redirect:/manage/main";
 			}
 			else{
 				return "redirect:/profile/view";
 			}
 		}
 		return "redirect:/home";
 	}
 	
 	@RequestMapping(value = "/home", method = RequestMethod.GET)
 	public String home1(ModelMap map) {
		Dashboard menu = dashboardRepository.findByViewName("HomeMenu");
		map.addAttribute("menu", menu);
		Dashboard content = dashboardRepository.findByViewName("HomeContent");
		map.addAttribute("content", content);
		return "home";
 	}
 	
 	@RequestMapping(value="/signin", method=RequestMethod.GET)
 	public void signin(ModelMap map) {
 		Dashboard menu = dashboardRepository.findByViewName("HomeMenu");
 		map.addAttribute("menu", menu);
 	}
 	
 	@RequestMapping(value="/manage/main", method=RequestMethod.GET)
 	public String manageMain(ModelMap map) {
 
 		Dashboard menu = dashboardRepository.findByViewName("AdminHomeMenu");
 		map.addAttribute("menu", menu);
 		Dashboard content = dashboardRepository.findByViewName("AdminHomeMenuItem");
 		map.addAttribute("content", content);
 		return "home";
 
 	}
 	
 	@RequestMapping(value="/profile/view", method=RequestMethod.GET)
 	public void profile(ModelMap map) {
 		map.addAttribute("user",  SecurityUtils.getCurrentUser());
 		Dashboard 	menu = dashboardRepository.findByViewName("HomeMenu");	
 		map.addAttribute("menu", menu);
 	}
 	
 	@RequestMapping(value="/profile/edit", method=RequestMethod.GET)
 	public void profile_edit(ModelMap map) {
 		Dashboard menu = dashboardRepository.findByViewName("HomeMenu");
 		map.addAttribute("menu", menu);
 		map.addAttribute("user", SecurityUtils.getCurrentUser());
 	}
 
 	@Inject
 	public void setDashboardRepository(DashboardRepository dashboardRepository) {
 		this.dashboardRepository = dashboardRepository;
 	}
 	
 	@RequestMapping(value = "/userUpdateCommit", method = RequestMethod.POST)
 	public String updateUser(@Valid @ModelAttribute UserEditForm form,  ModelMap map) {
 		User user = userService.findOne(SecurityUtils.getCurrentUserId());
 		user.setFirstName(form.getFirstName());
 		user.setLastName(form.getLastName());
 		userService.save(user);
 		SecurityUtils.updateCurrentUser(user);
 		return "redirect:/profile/view";
 	}	
 }
