 package com.baggers.bagboy;
 
 public class LoginManager {
 	
 	static String currUserEmail;
 	static String currUserPassword;
 	static DatabaseConnection db = new DatabaseConnection();
 	static ArrayList<String> registeredUsers;
 	//the error message to be returned
 	static String error = "";
 	
 	//variable to say that there is a current user logged into the app
 	private static boolean loggedIn = false;
 	
 	public LoginManager() {
 
 	}
 
 	public static boolean checkLogin(String username, String password) {
 		// admin log in
 		if(username.equals("admin") && password.equals("thebaggers")){
 			loggedIn = true;
 			return loggedIn;
 		}
 		
 		//tries to login with the database, 
 		//loggedIn = db.checkLogin(username, password);
 		
 		if (loggedIn) {
 			currUserEmail = username;
 			currUserPassword = password;
 		}
 		
 		error = "Invalid email or password";
 		return loggedIn;
 
 	}
 
 	public static boolean registerUser(String username, String password, String passwordConfirm) {
 		
 		//check if password and password confirm match
 		if (!(password.equals(passwordConfirm))) {
 			error = "Passwords do not match";
 			return false;
 		}
 		//check if it's a valid email address
 		String emailregex = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+.[a-zA-Z]{2,4}";
 		if (!(username.matches(emailregex))) {
 			error = "Invalid email address";
 			return false;
 		}
 		//check if the password length is greater than 5
 		if (password.length() < 5) {
 			error = "Invalid password length (use 6 or more characters)";
 			return false;
 		}
 		//if those are good, call database register user 
 		
 		//check to see if that user is already registered
 		//if (db.checkEmail(username)) {
 		//	error = "Email address already registered";
 		//	return false;
 		//}
 		if (registeredUsers.contains(username)){
 			error = "Email address already registered";
 			return false;
 		}
 		
 		//if everything is good, register the user, set the current user information
 		//loggedIn = db.registerUser(username, password);
 		registeredUsers.add("username");
 		if (loggedIn) {
 			currUserEmail = username;
 			currUserPassword = password;
 		}
 		else {
 			error = "Registration failed";
 		}
 		
 		return loggedIn;
 	
 		
 		//to make the compiler happy until we finish implementation
 	}
 	
 	public static String getError() {
 		return error;
 	}
 	
 
 }
