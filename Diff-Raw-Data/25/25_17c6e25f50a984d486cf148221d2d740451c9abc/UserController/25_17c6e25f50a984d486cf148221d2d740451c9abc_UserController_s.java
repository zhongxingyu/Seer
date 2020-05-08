 package controllers;
 
 import java.util.Map;
 
 import play.mvc.Result;
 
 import scala.collection.mutable.HashMap;
import controllers.user.Types;
 
 /**
  * @author Sander Demeester
  */
 public class UserController {
 	
	
 	
 	public UserController(){
 		
 		
 	}
 	
 	public static Result register(){
 		//TODO: Delegate to authenticationManager
 		return null;
 	}
 	
 	public static Result login(){
 		//TODO: Delage to authenticationManager
 		return null;
 	}
 	
 	public static Result getLandingPage(String token){ //or whatever the token will be
 		//TODO: Delegate to correct UserController object based on token
 		/*
		 * The result value will come from some UserController
 		 */
 		return null;
 	}
 
 }
