 package scrum.support.services;
 
 import scrum.support.model.User;
 
 import com.google.resting.component.impl.ServiceResponse;
 import com.google.resting.json.JSONException;
 import com.google.resting.json.JSONObject;
 
 /**
  * Singleton Service the manage the interactions between the 
  * REST requests / responses  and the UI Activities.
  *  
  * @author Dave W
  *
  */
public class ContentProvider {
 	
 	private final static int CONFLICT = 409;
 	private final static int BAD_REQUEST = 400;
 	private final static int UNAUTHORIZED = 401;
 	
	private static ContentProvider instance = null;
 	private RESTService rest;
 	
 	private User user;
 	
 	private enum RequestType {
 		User,
 		Project,
 		Story,
 		Task
 	}
 	
 	/**
 	 * Singleton Service the manage the interactions between the 
 	 * REST requests / responses and the UI Activities.
 	 */
	private ContentProvider() {
 		rest = new RESTService();
 	}
 	
 	/**
 	 *
 	 * @return the singleton instance of the service.
 	 */
	public static ContentProvider getInstance() {
 		if(instance == null) {
			instance = new ContentProvider();
 		}
 		return instance;
 	}
 	
 	/**
 	 * Validate the user - whether registering or logging in.
 	 * @param user the user to check for
 	 * @return true if the user was created or successfully authenticated.
 	 */
 	public boolean validateUser(User user) {
 		
 		ServiceResponse response;
 		JSONObject json = new JSONObject();
 		
 		response = (user.needToRegistered()) ? rest.registerUser(user) : rest.authenicateUser(user);
 		if(invalid(RequestType.User, response.getStatusCode())) return false;
 
 		this.user = user;
 		try {
 			json = new JSONObject(response.getResponseString());
 			user.setToken((String) json.get("auth_token"));
 			
 				// If the user has just registered, then they won't have any accounts, 
 				// just return true so the next activity can start.
 			if(user.needToRegistered()) return true;
 			
 			JSONObject link = (JSONObject) json.get("link");
 			return getProjects((String) link.get("accounts"));
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}		
 		return false;
 	}
 
 	/**
 	 * A method to get all of the projects for the user
 	 * @param accountLink
 	 * @return
 	 */
 	private boolean getProjects(String accountLink) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 	
 	private boolean invalid(RequestType type, int status) {
 		switch(type) {
 			case User : 
 				return status == CONFLICT ||
 						status == BAD_REQUEST ||
 						status == UNAUTHORIZED;			
 		}
 		return false;
 	}
 	
 }
