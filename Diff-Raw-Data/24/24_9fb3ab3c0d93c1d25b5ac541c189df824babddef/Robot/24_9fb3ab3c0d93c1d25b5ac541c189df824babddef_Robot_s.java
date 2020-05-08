 package org.accesointeligente.server.robots;
 
 import org.accesointeligente.model.Request;
 import org.accesointeligente.shared.RequestStatus;
 
 public abstract class Robot {
	private String username;
	private String password;
 
	public abstract void login();
 
	public abstract RequestStatus makeRequest(Request request);
 
	public abstract RequestStatus checkRequestStatus(Request request);
 
 	public String getUsername() {
 		return username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 }
