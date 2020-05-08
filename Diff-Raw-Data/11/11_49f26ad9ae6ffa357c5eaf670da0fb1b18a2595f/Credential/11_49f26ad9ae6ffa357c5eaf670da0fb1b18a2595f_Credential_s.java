 package com.example.routerCPR.domain;
 
public class Credential {
	
 	private final String user;
 	private final String password;
 	
 	public Credential(String user, String password) {
 		this.user = user;
 		this.password = password;
 	}
 	
 	public String getUser() {
 		return user;
 	}
 	
 	public String getPassword() {
 		return password;
 	}
 	
 }
