 package controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import play.Logger.ALogger;
 import play.mvc.Controller;
 import flexjson.JSONDeserializer;
 
 public class AppSession extends Controller{
 
 	static ALogger logger = play.Logger.of(AppSession.class);
 	
 	public static Boolean has(String code){
 		
 		String value = session("roles");
		JSONDeserializer<?> d = new JSONDeserializer<Object>();
		@SuppressWarnings("unchecked")
 		List<String> roles = (ArrayList<String>) d.deserialize(value);
 		
 		return roles.contains(code);
 	}
 }
