 package be.artesis.timelog.model;
 
 import java.io.IOException;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class ExistingUsernames {
 
 	public static boolean check(String mode, String gebruikersnaam) {
 		JSONObject jObject = null;
 		try {
 			jObject = Connection.getObject("gebruiker/checkExists/" + mode + "/" + gebruikersnaam);
 		} catch (IOException | JSONException e) {
 			e.printStackTrace();
 		}
		
 		return (jObject != null) ;
 		
 	}
 }
