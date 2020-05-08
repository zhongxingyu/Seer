 package com.globant.gaetraining.addsincgae.controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import com.globant.gaetraining.addsincgae.model.User;
 import com.globant.gaetraining.addsincgae.services.UserService;
 import com.google.appengine.api.datastore.KeyFactory;
 
 @Controller
 @RequestMapping("/people")
 public class UserController {
 
 	@Autowired
 	private UserService userService;
 	
 	@ModelAttribute("users")
 	public List<User> getUsers(){
 		return userService.getUsers();
 	}
 
 	@RequestMapping(value = "", method = RequestMethod.GET)
 	public String getUsers(Map<String, Object> model) {
 		return "UserList";
 	}
 
	@RequestMapping(value = "/{userKey}", method = RequestMethod.DELETE)
 	public String delUser(@PathVariable String userKey) {
 		userService.deleteUser(KeyFactory.stringToKey(userKey));
 		return "UserList";
 	}
 
 	@RequestMapping(value = "/{userKey}", method = RequestMethod.POST)
 	public String updateUserSubmit(@ModelAttribute("user") User user,
 			@PathVariable String userKey, ModelMap model) {
 
 		userService.updateUser(KeyFactory.stringToKey(userKey), user);
 
 		return "UserList";
 	}
 	
 	@RequestMapping(value = "/{userKey}", method = RequestMethod.GET, produces = "text/html")
 	public String editUser(@PathVariable String userKey, Model model) {
 
 		User user = userService.getUser(KeyFactory.stringToKey(userKey));
 
 		model.addAttribute("user", user);
 		
 		return "EditUser";
 	}
 
 	@RequestMapping(value = "", method = RequestMethod.POST)
 	public String adddUserSubmit(@ModelAttribute("user") User user,
 			ModelMap model) {
 
 		userService.addUser(user);
 
 		return "UserList";
 	}
 
 
 	@RequestMapping(value = "/add", method = RequestMethod.GET)
 	public String addUser(Model model) {
 		;
 		model.addAttribute("user", new User());
 		return "AdUser";
 	}
 	
 	@ModelAttribute("roles")
 	private List<String> getRoles(){
 		List<String> roles = new ArrayList<>();
 		roles.add("admin");
 		roles.add("representative");
 		roles.add("customer");
 		return roles;
 	}
 
 }
