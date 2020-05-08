 package com.webgearz.tb.controllers;
 
 import java.util.List;
 
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.RedirectView;
 
 
 
 
 
 import com.webgearz.tb.client.model.JSONView;
 import com.webgearz.tb.domain.models.User;
 import com.webgearz.tb.domain.models.UserDomain;
 import com.webgearz.tb.exceptions.WebgearzException;
 import com.webgearz.tb.forms.LoginForm;
 import com.webgearz.tb.services.UserDomainService;
 import com.webgearz.tb.services.UserService;
 
 
 @Controller
 @SessionAttributes("userid")
 public class UserController{
 
 	private static final Log log = LogFactory.getLog(UserController.class);
 	private UserService userService;
 	//private SecurityService securityService;
 	private UserDomainService userDomainService;
 	@RequestMapping(value="/getRegistePage",method=RequestMethod.GET)
 	public ModelAndView getRegistrationPage(){
 		ModelAndView mav = new ModelAndView("user-details");
 		return mav;
 	}
 
 	/**
 	 * FIXME : Removing the authentication till I fix the spring security issue
 	 * @param user
 	 * @param session
 	 * @return
 	 */
 	@RequestMapping(value="/createUser",method=RequestMethod.POST)
 	public ModelAndView createUser(User user,HttpSession session){
 		ModelAndView mav = new ModelAndView();
 		mav.setView(new JSONView());
 		log.debug(user);
 		User storeduser = userService.store(user);
 		mav.getModel().put("user", storeduser);
 		session.setAttribute("userid", storeduser.getId());
 		//securityService.addAuthenticatedUser(storeduser);
 		mav.getModel().put("msg", "User created");
		mav.getModel().put("userid",storeduser.getId());
 		return mav;
 	}
 	
 	@RequestMapping(value="/getlogin",method=RequestMethod.GET)
 	public ModelAndView getLoginPage(){
 		ModelAndView mav = new ModelAndView("login");
 		log.info("Returning login page");
 		return mav;
 		
 	}
 	
 	
	@RequestMapping(value="/cms/user_dashboard/{userid}",method=RequestMethod.GET)
	public ModelAndView getUserDashboard(@PathVariable("userid")String userid){
 		ModelAndView mav = new ModelAndView("dashboard");
 		User user = userService.findUser(userid);
 		if(user==null)
 			throw new WebgearzException("Not Authenticated User or Unknown user");
 		List<UserDomain> userDomains = userDomainService.findDomainsByUser(userid);
 		mav.getModel().put("user", user);
 		mav.getModel().put("userdomains", userDomains);
 		return mav;
 		
 	}
 	
 	@RequestMapping(value = "/dologin", method = RequestMethod.POST) 
 	public String login(@ModelAttribute("loginForm") LoginForm loginform,                 
 			  BindingResult result,HttpSession session){
 		User user = userService.authenticate(loginform.getEmail(), loginform.getPassword());
 		if(user!=null){
 		session.setAttribute("userid", user.getId());
 		
		return "redirect:/cms/user_dashboard/"+user.getId();
 		}
 		else{
 			System.out.println("Throwing new exception");
 			throw new WebgearzException("Problem authenticating user");
 		}
 	}
 	
 
 	@Autowired
 	public void setUserService(UserService userService) {
 		this.userService = userService;
 	}
 
 	public UserService getUserService() {
 		return userService;
 	}
 
 	@ExceptionHandler(WebgearzException.class)
 	  public ModelAndView handleIOException(WebgearzException ex) {
 
 	    System.out.println("handleIOException - Catching: " + ex.getClass().getSimpleName());
 	    return errorModelAndView(ex);
 	  }
 	
 	
 	/**
 	   * Get the users details for the 'personal' page
 	   */
 	  private ModelAndView errorModelAndView(Exception ex) {
 	    ModelAndView modelAndView = new ModelAndView();
 	    modelAndView.setViewName("GenericExceptionPage");
 	    modelAndView.addObject("exception",ex);
 	    
 
 	    return modelAndView;
 	  }
 
 	public UserDomainService getUserDomainService() {
 		return userDomainService;
 	}
 
 	@Autowired
 	public void setUserDomainService(UserDomainService userDomainService) {
 		this.userDomainService = userDomainService;
 	}
 	
 }
 
