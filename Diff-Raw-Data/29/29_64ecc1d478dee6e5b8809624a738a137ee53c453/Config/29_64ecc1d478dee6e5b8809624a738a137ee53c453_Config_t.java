 package pl.jacbar.runner;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class Config {
	public static final String API_URL = "http://10.1.1.194:49542/Api.svc/";
 	public static final String RUNNER_PREFERENCES = "runnerPreferences";
 	
 	static String getType(int id){
 		switch(id){
 		case 0 : return "running";
 		case 1: return "cycling";
 		case 2 : return "walking";
 		case 3 : return "skating";
 		default : return "";
 		}
 	}
 }
