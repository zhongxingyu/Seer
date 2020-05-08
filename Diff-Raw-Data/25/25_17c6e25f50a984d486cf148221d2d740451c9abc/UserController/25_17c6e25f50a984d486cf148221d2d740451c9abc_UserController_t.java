 package controllers;
 
 import java.util.Map;
 
 import play.mvc.Result;
 
 import scala.collection.mutable.HashMap;
import controllers.user.Type;
 
 /**
  * @author Sander Demeester
  */
 public class UserController {
 	
	private HashMap<Type, Result> landingPageHashmap = new HashMap<Type, Result>();
 	
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
		 * The result will come from the landingPageHashMap
 		 */
 		return null;
 	}
 
 }
