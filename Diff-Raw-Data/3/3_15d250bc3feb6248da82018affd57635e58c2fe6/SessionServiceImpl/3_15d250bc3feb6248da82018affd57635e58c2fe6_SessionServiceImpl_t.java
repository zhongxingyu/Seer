 package com.justcloud.osgifier.service.impl;
 
 import com.justcloud.osgifier.annotation.REST;
 import com.justcloud.osgifier.annotation.REST.RESTMethod;
 import com.justcloud.osgifier.annotation.RESTParam;
 import com.justcloud.osgifier.dto.User;
 import com.justcloud.osgifier.service.SessionService;
 import com.justcloud.osgifier.service.UserService;
 
 public class SessionServiceImpl implements SessionService {
 
 	private UserService userService = new UserServiceImpl();
 
 	@Override
 	public User login(String username, String password) {
 		return userService.findUser(username, password);
 	}
 
 	@Override
 	@REST(url = "/session/current", method = RESTMethod.POST)
 	public User getCurrentUser(
 			@RESTParam(value = "user", session = true) User current) {
		
		return userService.findUser(current.getUsername());
 	}
 
 	@Override
 	public String getName() {
 		return "Session";
 	}
 
 }
