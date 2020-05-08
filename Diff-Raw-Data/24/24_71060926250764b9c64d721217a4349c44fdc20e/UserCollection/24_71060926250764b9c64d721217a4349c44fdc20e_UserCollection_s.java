 package edu.gatech.User;
 import java.util.*;
 
 
 public class UserCollection {
 	
	private User u;
 	private List<User> userArray = new ArrayList<User>();
 	
 	public UserCollection(){
 		User u = new User("fakeuser","fake","blah@gmail.com");	
 		userArray.add(u);
 	}
 	
 	public User addUser(User u){
 		return u;
 	}
 	
 	public User getUser(){
 		return userArray.get(0);	
 	}
 
 }
