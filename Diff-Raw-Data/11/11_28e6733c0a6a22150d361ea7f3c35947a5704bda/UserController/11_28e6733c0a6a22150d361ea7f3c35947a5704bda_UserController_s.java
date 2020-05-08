 package com.navigrad.m2m.webclient.controller;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 
 import com.navigrad.m2m.server.gps.entity.User;
 
@ManagedBean(name = "user")
 @SessionScoped
 public class UserController {
 
 	private User user;
 
 	public UserController() {
 	}
 
 	public UserController(User user) {
 		this.user = user;
 	}
 
 	public User getUser() {
 		return user;
 	}
 
 	public void setUser(User user) {
 		this.user = user;
 	}
 
 }
