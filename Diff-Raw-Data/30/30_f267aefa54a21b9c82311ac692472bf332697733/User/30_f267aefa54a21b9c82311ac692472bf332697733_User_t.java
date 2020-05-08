 package com.WebQ.beans;
 
 public class User {
     private String userId;
     private String password;
 
     private String firstName;
     private String lastName;
     private String emailId;
     private String score;
    private String status;
 
     public User() {
 
     }
 
     public User(String userId, String password) {
 	super();
 	this.userId = userId;
 	this.password = password;
     }
 
     public User(String userId, String password, String firstName,
 	    String lastName, String emailId) {
 	super();
 	this.userId = userId;
 	this.password = password;
 	this.firstName = firstName;
 	this.lastName = lastName;
 	this.emailId = emailId;
     }
 
    public User(String userId, String password, String firstName,
	    String lastName, String emailId, String status) {
	super();
	this.userId = userId;
	this.password = password;
	this.firstName = firstName;
	this.lastName = lastName;
	this.emailId = emailId;
	this.status = status;
    }

     public String getUserId() {
 	return userId;
     }
 
     public void setUserId(String userId) {
 	this.userId = userId;
     }
 
     public String getPassword() {
 	return password;
     }
 
     public void setPassword(String password) {
 	this.password = password;
     }
 
     public String getFirstName() {
 	return firstName;
     }
 
     public void setFirstName(String firstName) {
 	this.firstName = firstName;
     }
 
     public String getLastName() {
 	return lastName;
     }
 
     public void setLastName(String lastName) {
 	this.lastName = lastName;
     }
 
     public String getEmailId() {
 	return emailId;
     }
 
     public void setEmailId(String emailId) {
 	this.emailId = emailId;
     }
 
     public String getScore() {
 	return score;
     }
 
     public void setScore(String score) {
 	this.score = score;
     }

    public String getStatus() {
	return status;
    }

    public void setStatus(String status) {
	this.status = status;
    }
 }
