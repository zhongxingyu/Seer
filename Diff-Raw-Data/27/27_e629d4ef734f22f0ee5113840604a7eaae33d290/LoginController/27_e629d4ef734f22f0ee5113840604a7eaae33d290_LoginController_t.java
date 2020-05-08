 package com.youthministry.controller;
 
 import javax.validation.Valid;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.social.connect.Connection;
 import org.springframework.social.connect.web.ProviderSignInUtils;
 import org.springframework.stereotype.Component;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.context.request.WebRequest;
 import com.youthministry.controller.SignInUtils;
 import com.youthministry.controller.SignupForm;
 import com.youthministry.domain.Role;
 import com.youthministry.domain.User;
 import com.youthministry.domain.UserProfile;
 
 import com.youthministry.service.UserService;
 
@Controller
 public class LoginController {
 
 	@Autowired
 	private UserService UserService;
 	
 	@RequestMapping(value={"/", "/signin"})
 	public String signin() {
 		return "signin";
 	}
 	
 	@RequestMapping(value="/signup", method=RequestMethod.GET)
 	public SignupForm signupForm(WebRequest request) {
 		Connection<?> connection = ProviderSignInUtils.getConnection(request);
 		if (connection != null) {
 			return SignupForm.fromProviderUser(connection.fetchUserProfile());
 		} else {
 			return new SignupForm();
 		}
 	}
 
 	@RequestMapping(value="/signup", method=RequestMethod.POST)
 	public String signup(@Valid SignupForm form, BindingResult formBinding, WebRequest request) {
 		if (formBinding.hasErrors()) {
 			return null;
 		}
 		User user = createUser(form, formBinding);
 		if (user != null) {
 			SignInUtils.signin(user.getUsername());
 			ProviderSignInUtils.handlePostSignUp(user.getUsername(), request);
 			return "redirect:/";
 		}
 		return null;
 	}
 	
 	// Internal helpers
 	private User createUser(SignupForm form, BindingResult formBinding) {
 		User user = new User();
 		user.setUsername(form.getUsername());
 		user.setPassword(form.getPassword());
 
 		UserProfile userProfile = new UserProfile();
 		userProfile.setFirstName(form.getFirstName());
 		userProfile.setLastName(form.getLastName());
 
 		user.setUserProfile(userProfile);
 
 		Role role = new Role();
 		role.setName("ROLE_USER");
 		
 		user.getRoles().add(role);
 		
 		UserService.addUser(user);
 
 		return user;
 	}
 }
