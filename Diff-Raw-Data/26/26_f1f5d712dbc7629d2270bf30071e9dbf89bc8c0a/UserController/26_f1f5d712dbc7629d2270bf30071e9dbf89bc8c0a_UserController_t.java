 package com.crowdplatform.controller;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.validation.Valid;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.http.HttpStatus;
 import org.springframework.security.authentication.AuthenticationManager;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.web.authentication.WebAuthenticationDetails;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 import com.crowdplatform.model.PasswordResetData;
 import com.crowdplatform.model.PasswordResetRequest;
 import com.crowdplatform.model.PlatformUser;
 import com.crowdplatform.model.Registration;
 import com.crowdplatform.service.PasswordResetRequestService;
 import com.crowdplatform.service.UserService;
 import com.crowdplatform.util.MailSender;
 import com.crowdplatform.util.PasswordResetDataValidator;
 import com.crowdplatform.util.RegistrationValidator;
 
 @Controller
 public class UserController {
 
 	@Autowired
     private RegistrationValidator registrationValidation;
 	
 	@Autowired
 	private PasswordResetDataValidator passwordResetValidator;
 	
 	@Autowired @Qualifier("org.springframework.security.authenticationManager")
     protected AuthenticationManager authenticationManager;
 	
 	@Autowired
 	private UserService userService;
 	
 	@Autowired
 	private PasswordResetRequestService passwordService;
 
     public void setRegistrationValidation(
     		RegistrationValidator registrationValidation) {
             this.registrationValidation = registrationValidation;
     }
     
     private MailSender mailSender;
     
     public void setMailSender(MailSender mailSender) {
     	this.mailSender = mailSender;
     }
     
     public MailSender getMailSender() {
     	if (this.mailSender == null) {
     		ApplicationContext context = new ClassPathXmlApplicationContext("mail.xml");
     		this.mailSender = (MailSender) context.getBean("mailMail");
     	}
     	return this.mailSender;
     }
     
 	@RequestMapping("/login")
	public String loadLogin(Model model, @RequestParam(value="error", required=false) Boolean error) {
		if (error != null) {
			model.addAttribute("error", error);
		}
 		return "login";
 	}
 	
 	@RequestMapping(value={"/register"}, method=RequestMethod.GET)
 	public String showRegistration(Model model) {
 		model.addAttribute(new Registration());
 		return "register";
 	}
 	
 	@RequestMapping(value={"/register"}, method=RequestMethod.POST)
 	public String processRegistration(@Valid Registration registration, 
 			BindingResult bindingResult, HttpServletRequest request) {
 		registrationValidation.validate(registration, bindingResult);
 		if (bindingResult.hasErrors()) {
 			return "register";
 		}
 		userService.addUser(new PlatformUser(registration));
 		PlatformUser user = userService.getUser(registration.getUsername());
 		if (user != null) {
 			authenticateUserAndSetSession(user, request);
 			return "redirect:/projects?registered=true";
 		}
 		bindingResult.reject("registration.error");
 		return "register";
 	}
 	
 	@RequestMapping(value={"/forgot"}, method=RequestMethod.POST)
 	@ResponseStatus(HttpStatus.CREATED)
 	public void forgotPassword(String username) {
 		PlatformUser user = userService.getUserByUsernameOrEmail(username);
 		System.out.println(username);
 		if (user != null) {
 			PasswordResetRequest request = new PasswordResetRequest();
 			request.setUser(user);
 			
 			passwordService.addRequest(request);
 			
 			getMailSender().sendPasswordResetMail(user, request.getId());
 		}
 	}
 	
 	@RequestMapping("/forgot/{uid}")
 	public String loadPasswordReset(@PathVariable("uid") Long uid, Model model) {
 		PasswordResetData data = new PasswordResetData();
 		model.addAttribute(data);
 		model.addAttribute("uid", uid);
 		return "password-reset";
 	}
 	
 	@RequestMapping("/reset")
 	public String resetPassword(@Valid PasswordResetData data, BindingResult bindingResult) {
 		passwordResetValidator.validate(data, bindingResult);
 		
 		PasswordResetRequest request = passwordService.getRequest(data.getUid());
 		if (request == null) {
 			bindingResult.reject("password.change.error");
 		}
 		
 		if (!bindingResult.hasErrors()) {
 			PlatformUser user = request.getUser();
 			user.setPassword(data.getPassword());
 			userService.saveUser(user);
 			passwordService.removeRequest(request);
 			
 			// TODO: send email alerting the password was changed
 			
 			return "redirect:/login";
 		}
 		
 		return "password-reset";
 	}
 	
 	private void authenticateUserAndSetSession(PlatformUser user, HttpServletRequest request) {
 		UsernamePasswordAuthenticationToken token = 
 				new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());
 
 	    request.getSession();
 
 	    token.setDetails(new WebAuthenticationDetails(request));
 	    Authentication authenticatedUser = authenticationManager.authenticate(token);
 
 	    SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
 	}
 }
