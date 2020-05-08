 /**
  * 
  */
 package com.example.client.model;
 
 import com.google.gwt.user.client.rpc.IsSerializable;
 
 /**
  * The model class that represents a user of the system
  */
public class User implements IsSerializable {
 	
 	// serial uid
 	private static final long serialVersionUID = 1L;
 	
 	private String username;
 	
 	private String password;
 
 	/**
 	 * Must have default no-arg constructor for GWT-RPC
 	 */
 	public User() {}
 	
 	/**
 	 * Constructor
 	 * <p>
 	 * @param username
 	 * @param password
 	 */
 	public User(String username, String password) {
 		this.username = username;
 		this.password = password;
 	}
 	
 	/**
 	 * Indicates if the object is valid for submission
 	 * to the server.
 	 * @return <tt>true</tt> if valid; 
 	 */
 	public boolean isValid() {
 		return 
 			(username != null) && !(username.isEmpty()) &&
 			(password != null) && !(password.isEmpty());
 	}
 
 	public final String getUsername() {
 		return username;
 	}
 
 	public final void setUsername(String username) {
 		this.username = username;
 	}
 
 	public final String getPassword() {
 		return password;
 	}
 
 	public final void setPassword(String password) {
 		this.password = password;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((password == null) ? 0 : password.hashCode());
 		result = prime * result
 				+ ((username == null) ? 0 : username.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		User other = (User) obj;
 		if (password == null) {
 			if (other.password != null)
 				return false;
 		} else if (!password.equals(other.password))
 			return false;
 		if (username == null) {
 			if (other.username != null)
 				return false;
 		} else if (!username.equals(other.username))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "User [password=*******, username=" + username + "]";
 	}
 
 
 
 }
