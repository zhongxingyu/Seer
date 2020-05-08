 package com.example.handsonandroid;
 
 public class User {
 
 	private String id;
 	private String userName;
 	private String firstName;
 	private String lastName;
 	private int age;
 	private String address1;
 	private String address2;
 	private String phone;
 	private String email;
 	
	private static User user;
	
 	public User(){
 		
 	}
 	
	public User(String userName, String password, String firstName){
		this.userName = userName;
		this.firstName = firstName;
		//Login function below
		//WebMethods.login();
	}
	
 }
