 package org.accesointeligente.server.robots;
 
 import org.accesointeligente.model.Request;
 import org.accesointeligente.shared.RequestStatus;
 
 public abstract class Robot {
	protected String username;
	protected String password;
 
	public abstract void login() throws RobotException;
 
	public abstract RequestStatus makeRequest(Request request) throws RobotException;
 
	public abstract RequestStatus checkRequestStatus(Request request) throws RobotException;
 
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
