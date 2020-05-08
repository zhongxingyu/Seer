 package controllers;
 import play.*;
 import models.*;
 
 public class Security extends Secure.Security {
 
     static boolean authentify(String username, String password) {
         User user = User.find("byUserName", username).first();
         if (user !=null){
         	System.out.println("username: "+ user.getUserName());
         	if (password.equals(user.getPassword())){
         		System.out.println("password: "+ user.getPassword());
         		return true; 
         	}
         	else {
         		System.out.println("Wrong password: "+ password);
         		return false; 	
         	}
         }else{
         	System.out.println("username: "+ username + " not found" );
         	return false; 
         }
         
     }
 //    static void onDisconnected() {
 //    }
 ////    static void onAuthenticated() {
 ////        Application.index();
 ////    }
 
 }
