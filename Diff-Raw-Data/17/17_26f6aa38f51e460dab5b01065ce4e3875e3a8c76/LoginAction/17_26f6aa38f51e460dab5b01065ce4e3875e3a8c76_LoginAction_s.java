 package com.tp.action;
 
 import com.opensymphony.xwork2.ActionSupport;
 
 public class LoginAction extends ActionSupport {
 
 	private static final long serialVersionUID = 1L;
 
 	@Override
 	public String execute() throws Exception {
 		return login();
 	}
 
 	public String login() throws Exception {
		return "login";
 	}
 
 }
