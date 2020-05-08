 package controllers;
 
 import java.util.Map;
 
 import models.user.AuthenticationManager;
 
 import play.mvc.Content;
 import play.mvc.Result;
 import play.mvc.Results.Redirect;
 
 import scala.collection.mutable.HashMap;
 import views.html.index;
 import views.html.landingPages.AdminLandingPage;
 import views.html.landingPages.IndependentPupilLandingPage;
 import views.html.landingPages.OrganizerLandingPage;
 import views.html.landingPages.PupilLandingPage;
 import controllers.user.Type;
 
 /**
  * This class receives all GET requests and based on there session identifier (cookie)
  * and current role in the system they will be served a different view.
  * @author Sander Demeester
  */
 public class UserController extends EController{
 	
 	/**
 	 * This hashmap embodies the mapping from a Type to a view. 
 	 * Each view is responsible for getting all information from the DataModel and make a 
 	 * beautiful view for the user :)
 	 */
 	private HashMap<Type, Class<?>> landingPageHashmap = new HashMap<Type, Class<?>>();
 	private AuthenticationManager authenticatieManger = new AuthenticationManager();
 	
 	
 	public UserController(){
 		
 		landingPageHashmap.put(Type.ADMINISTRATOR, AdminLandingPage.class);
 		landingPageHashmap.put(Type.INDEPENDENT, IndependentPupilLandingPage.class);
 		landingPageHashmap.put(Type.ORGANIZER, OrganizerLandingPage.class);
 		landingPageHashmap.put(Type.PUPIL,PupilLandingPage.class);
 		
 		
 	}
 	
 	public static Result register(){
 		//TODO: Delegate to authenticationManager
 		return null;
 	}
 	
 	public static Result login(){
 		//TODO: Delage to authenticationManager
 		return null;
 	}
 	
 	public static Result logout(){
 		//TODO: Tell authenticationManager to log a user out.
 		
	    setCommonHeaders();
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
