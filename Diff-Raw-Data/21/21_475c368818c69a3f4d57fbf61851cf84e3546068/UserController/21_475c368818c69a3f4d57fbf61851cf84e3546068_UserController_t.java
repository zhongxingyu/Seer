 package com.fauxwerd.web.controller;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.validation.Valid;
 
 import java.io.UnsupportedEncodingException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.security.authentication.ProviderManager;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.web.authentication.WebAuthenticationDetails;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.DefaultMessageCodesResolver;
 import org.springframework.validation.FieldError;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.ui.Model;
 
 import com.fauxwerd.model.Invite;
 import com.fauxwerd.model.InviteStatus;
 import com.fauxwerd.model.Role;
 import com.fauxwerd.model.User;
 import com.fauxwerd.service.UserService;
 
 @Controller
 public class UserController {
 
     final Logger log = LoggerFactory.getLogger(getClass());
     
     @Autowired
     private UserService userService;
     
 	//@Resource - need to understand best way to annotate dependencies when multiple options exist
 	@Autowired
 	@Qualifier("org.springframework.security.authenticationManager")
 	private ProviderManager authenticationManager;
         
     @RequestMapping(value = "/register")
     public ModelAndView register(@RequestParam String code) {
     	if (log.isDebugEnabled()) log.debug(String.format("code = %s", code));
     	if (code == null || code.isEmpty()) return new ModelAndView("redirect:/");
 
     	Invite invite = userService.getInvite(code);
     	if (invite == null || invite.getStatus() != InviteStatus.ACTIVE) return new ModelAndView("redirect:/");
     	
     	ModelAndView modelAndView = new ModelAndView("user/register");    	
     	User user = new User();
     	modelAndView.addObject(user);
     	modelAndView.addObject(invite);
     	
     	return modelAndView;
     }
     
 	@RequestMapping(value = "register", method = RequestMethod.POST)	
 	public ModelAndView create(@Valid User user, BindingResult result, HttpServletRequest req, HttpServletResponse res) {
 		if (result.hasErrors()) {
 			return new ModelAndView("user/register", "user", user);
 		}
 		
     	Invite invite = userService.getInvite(user.getInvite().getCode());
     	if (invite == null || invite.getStatus() != InviteStatus.ACTIVE) return new ModelAndView("redirect:/");
 
     	//this seems weird, but the version of invite that is currently set in the user object is transient
     	//so we need to replace it with the persistent one from the db, need to revisit
     	user.setInvite(invite);
     	
 		//check if user already exists
 		User existingUser = userService.getUser(user.getEmail());
 		
 		if (existingUser != null) {
 			if (log.isDebugEnabled()) {
 				log.debug(String.format("user already exists with email %s", user.getEmail()));
 			}
 			
 			//TODO this may be a bit of a hack, revisit when the time is right 
 			DefaultMessageCodesResolver messageCodesResolver = new DefaultMessageCodesResolver();			
 			String [] errorCodes = messageCodesResolver.resolveMessageCodes("Exists", "user", "email", String.class);
 			result.addError(new FieldError("user", "email", user.getEmail(), false, errorCodes, null, null));
 
 			return new ModelAndView("user/register", "user", user);
 		}
 		
 		String unhashedPassword = user.getPassword(); 
 		
 		List<Role> roles = new ArrayList<Role>();
 		roles.add(userService.getRole("ROLE_USER"));
 		user.setRoles(roles);
 		
 		userService.createUser(user);
 		
 		if (log.isDebugEnabled()) {
 			log.debug(String.format("Created user [%s]", user));
 		}
 				
 		autoLogin(req, res, user.getEmail(), unhashedPassword);
 						
 		return new ModelAndView("redirect:/home", "user", user);
 	}
 	
 	@RequestMapping(value = "/profile")
 	public ModelAndView profile() {
 		
 		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 		
 		//this is necessary b/c of hibernate lazy loading weirdness
 		user = userService.getUserById(user.getId());
 		
 		return new ModelAndView("user/profile", "user", user);
 	}
 	
 	@RequestMapping(value = "/users-all")
 	public ModelAndView listUsers() {
 		List<User> otherUsers = userService.listUsers();
 		return new ModelAndView("user/list", "otherUsers", otherUsers);		
 	}
 	
 	@RequestMapping(value = "/users")
 	public ModelAndView listUsersToFollow() {
 		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 		List<User> otherUsers = userService.listUsersExcept(user);
 		return new ModelAndView("user/list", "otherUsers", otherUsers);		
 	}
 		
 	@RequestMapping(value = "/user/{userIdString}")
 	public ModelAndView userPage(@PathVariable("userIdString") String userIdString) {
 		Long userId = null;
 
 		try {
 			userId = Long.valueOf(userIdString);
 		}
 		catch (NumberFormatException e) {
 			if(log.isErrorEnabled()) {
 				log.error("", e);
 			}
 		}
 		
 		if (log.isDebugEnabled()) log.debug(String.format("userId = %s", userId));
 		
 		User user = userService.getUserById(userId);
 		
 		return new ModelAndView("user/content", "user", user);
 	}
 	
 	@RequestMapping(value = "/follow/{userIdString}")
 	public ModelAndView follow(@PathVariable("userIdString") String userIdString) {
 		Long userId = null;
 
 		try {
 			userId = Long.valueOf(userIdString);
 		}
 		catch (NumberFormatException e) {
 			if(log.isErrorEnabled()) {
 				log.error("", e);
 			}
 		}	
 		
 		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 		User toFollow = userService.getUserById(userId);
 		
		if (log.isDebugEnabled()) log.debug(String.format("user = %s", user));
		if (log.isDebugEnabled()) log.debug(String.format("toFollow = %s", toFollow));
		
 		userService.followUser(user, toFollow);
 		
 		return new ModelAndView("redirect:/home", "user", user);
 	}
 	
 	@RequestMapping(value = "/unfollow/{userIdString}")
 	public ModelAndView unfollow(@PathVariable("userIdString") String userIdString) {
 		Long userId = null;
 
 		try {
 			userId = Long.valueOf(userIdString);
 		}
 		catch (NumberFormatException e) {
 			if(log.isErrorEnabled()) {
 				log.error("", e);
 			}
 		}	
 		
 		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 		User toUnfollow = userService.getUserById(userId);
 		
 		userService.unfollowUser(user, toUnfollow);
 		
 		return new ModelAndView("redirect:/home", "user", user);
 	}
 	
 	
 	@RequestMapping(value = "wrong-bookmark", method = RequestMethod.GET)
 	public ModelAndView wrongBookmark(HttpServletRequest req, HttpServletResponse res, Model model) {
 		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 		user = userService.getUserById(user.getId());
 		
 		String url = req.getParameter("url");
 		
 		try {
 			url = URLDecoder.decode(url, "utf-8");
 		} catch (UnsupportedEncodingException e) {
 			if (log.isErrorEnabled()) {
 				log.error("", e);
 			}
 		}
 		
 		model.addAttribute("url", url);
 		return new ModelAndView("user/wrong-bookmark","user", user);
 	}
     
 	//currently called after registration to automatically log user in
 	public void autoLogin(HttpServletRequest request, HttpServletResponse response, String username, String password) {
 		try {
 	      // Must be called from request filtered by Spring Security, otherwise SecurityContextHolder is not updated
 			UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
 			token.setDetails(new WebAuthenticationDetails(request));
 			Authentication authentication = authenticationManager.authenticate(token);
 			
 			if (log.isDebugEnabled()) {
 				log.debug(String.format("Logging in with %s", authentication.getPrincipal()));
 			}
 			SecurityContextHolder.getContext().setAuthentication(authentication);
 	    } 
 		catch (Exception e) {
 			SecurityContextHolder.getContext().setAuthentication(null);
 			if (log.isErrorEnabled()) {
 				log.error("Failure in autoLogin", e);
 			}
 	    }
 	}	
 	
 }
