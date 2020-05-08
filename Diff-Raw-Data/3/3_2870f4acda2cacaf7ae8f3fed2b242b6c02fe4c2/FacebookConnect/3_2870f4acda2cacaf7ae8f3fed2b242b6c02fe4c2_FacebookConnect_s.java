 package org.apache.cordova.plugin;
 
 import android.content.Intent;
 import android.util.Log;
 import com.facebook.*;
 import com.facebook.model.GraphUser;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.cordova.CordovaPlugin;
 import org.apache.cordova.CallbackContext;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * This class echoes a string called from JavaScript.
  */
 public class FacebookConnect extends CordovaPlugin {
 	
 	public final static String TAG = "FacebookConnect";
 	public boolean force_login = true;
 	
     @Override
     public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    	
     	boolean result = false;
     	
     	try {
     		if(action.equals("login")) result = this.login(args, callbackContext);
     		else if(action.equals("logout")) result = this.logout(args, callbackContext);
     		else if(action.equals("me")) result = this.me(args, callbackContext);
     		else if(action.equals("status")) result = this.status(args, callbackContext);
     		else {callbackContext.error("Error: Unsupported action"); result = false;}
     	} catch (MalformedURLException e) {
     		e.printStackTrace();
     		callbackContext.error("Error: Malformed exception " + e.getMessage());
     		result = false;
     	} catch (IOException e) {
     		e.printStackTrace();
     		callbackContext.error("Error: IO Excepion " + e.getMessage());
     		result = false;
     	} catch (JSONException e) {
     		e.printStackTrace();
     		callbackContext.error("Error: JSON Exception" + e.getMessage());
     		result = false;
     	}    	
     	return result;
     }
 
 	/**
 	* Cordova interface to perform a login
 	*
 	* @param args
 	* @param callbackContext
 	* @return result
 	* @throws JSONException
 	* @throws MalformedURLException
 	* @throws IOException
 	*/
     private boolean login(final JSONArray args, final CallbackContext callbackContext) throws JSONException, MalformedURLException, IOException {
     	
     	Log.d(TAG, "Login Triggered: " + args.toString());
     	Session session = Session.getActiveSession();
     	
     	if(!args.isNull(0)){
     		JSONObject params = args.getJSONObject(0);
     		force_login = params.getBoolean("1");
     	}
     	
         if (session == null || !session.isOpened()) {
         	cordova.setActivityResultCallback(this); 
     		cordova.getThreadPool().execute(new Runnable() { 
                 public void run() {
 					Session.openActiveSession(cordova.getActivity(), force_login, new Session.StatusCallback() {
                 			// callback when session changes state
                 		@Override
                 		public void call(Session session, SessionState state, Exception exception) {
                 			Log.d(TAG, "[StatusCallback]: " + state.toString() + " [Token]: " + session.getAccessToken());   
                 			if (state.isOpened()) {
                 				callbackContext.success(session.getState().toString());
                 			}
                 		}
                     });
                 }    		
     		});
         } else if(session.isOpened()){
         	Log.d(TAG, "[StatusCallback]:" + session.getState().toString() + " [Token]: " + session.getAccessToken());
         	callbackContext.success(session.getState().toString());
         }
         
     	return true;
     }
     
     /**
     * Cordova interface to perform a logout
     *
     * @param args
     * @param callbackContext
     * @return result
     * @throws JSONException
     * @throws MalformedURLException
     * @throws IOException
     */
     private boolean logout(JSONArray args, final CallbackContext callbackContext) throws JSONException, MalformedURLException, IOException {
     	
     	Log.d(TAG, "Logout Triggered");    	
 
     	if (Session.getActiveSession() != null && Session.getActiveSession().isOpened()) {
     		cordova.setActivityResultCallback(this);
     		cordova.getThreadPool().execute(new Runnable() { 
                 public void run() {
                 	Session.getActiveSession().closeAndClearTokenInformation();
                 	callbackContext.success("Session Closed");
                 }
     		});
     	} else{
     		Log.d(TAG, "No active Session found");
     	}    	
     	return true;
     }
     
     /**
     * Cordova interface to get user data
     *
     * @param args
     * @param callbackContext
     * @return result
     * @throws JSONException
     * @throws MalformedURLException
     * @throws IOException
     */
     private boolean me(JSONArray args, final CallbackContext callbackContext) throws JSONException, MalformedURLException, IOException {
     	
     	Log.d(TAG, "Me Triggered");    	
 
     	if (Session.getActiveSession() == null || Session.getActiveSession().isClosed()) {
     		callbackContext.error("No opened session");
     	} else {
     		Request.newMeRequest(Session.getActiveSession(), new Request.GraphUserCallback() {
     			// callback after Graph API response with user object
     			@Override
     			public void onCompleted(final GraphUser user, final Response response) {
     				Log.d(TAG, "[me onCompleted] user: " + user + " response: " + response);
     				if (user != null) {
     					callbackContext.success(userToJSON(user));
     				}
     			}
     		}).executeAsync(); 
     	}
     	return true;
     }
     
     /**
     * Cordova interface to get status
     *
     * @param args
     * @param callbackContext
     * @return result
     * @throws JSONException
     * @throws MalformedURLException
     * @throws IOException
     */
     private boolean status(JSONArray args, CallbackContext callbackContext) throws JSONException, MalformedURLException, IOException {
     	Log.d(TAG, "Status Triggered");
     	String message;
     	if(Session.getActiveSession() == null){
     		message = "NULL";
     	} else{
     		message = Session.getActiveSession().getState().toString();
     	}
     	callbackContext.success(message);
     	return true;    	
     }
     
     public JSONObject userToJSON(GraphUser user) {
     	Map<String, Object> response = new HashMap<String, Object>();
     	response.put("id", user.getId());
     	response.put("username", user.getUsername());
     	response.put("name", user.getName());
     	response.put("firstName", user.getFirstName());
     	response.put("middleName", user.getMiddleName());
     	response.put("lastName", user.getLastName());
     	response.put("url", user.getLink());
     	response.put("birthday", user.getBirthday());
     	return new JSONObject(response);
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         Session.getActiveSession().onActivityResult(cordova.getActivity(), requestCode, resultCode, data);
     }    
 }
