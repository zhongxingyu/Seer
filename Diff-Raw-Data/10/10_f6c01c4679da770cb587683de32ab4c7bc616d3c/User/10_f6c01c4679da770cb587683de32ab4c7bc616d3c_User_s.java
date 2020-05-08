 package com.jin.tpdb.entities;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 
 @Entity
 public class User {
 	@Id
	@GeneratedValue
 	int id;
 	String username;
 	String password;
 	String email;
 	String comment;
 	String avatar;
 	
 	public int getId() {
 		return id;
 	}
 	private void setId(int id) {
 		this.id = id;
 	}
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
 	public String getEmail() {
 		return email;
 	}	
 	public void setEmail(String email) {
 		this.email = email;
 	}
 	public String getComment() {
 		return comment;
 	}
 	public void setComment(String comment) {
 		this.comment = comment;
 	}
 	public String getAvatar() {
 		return avatar;
 	}
 	public void setAvatar(String avatar) {
 		this.avatar = avatar;
 	}
 }
