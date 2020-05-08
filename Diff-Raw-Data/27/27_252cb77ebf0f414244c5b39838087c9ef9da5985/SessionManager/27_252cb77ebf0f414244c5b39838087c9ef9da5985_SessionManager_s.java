 package epfl.sweng.authentication;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import epfl.sweng.globals.Globals;
 import epfl.sweng.tasks.AuthenticationTask;
 import epfl.sweng.tasks.IAuthenticationCallback;
 
 
 /**
  * Simple singleton data structure holding session ID and taking care of authentication
  */
 final public class SessionManager {
 	
 	private static SessionManager instance  = new SessionManager();
 	private SharedPreferences mSettings;
 	
 	private SessionManager() {
 		
 	}
 	
 	public static SessionManager getInstance() {
 		return instance;
 	}
 	
 	public void setSettingsFromActivity(Activity activity) {
 		mSettings = activity.getSharedPreferences(Globals.PREFS_NAME, 0);
 	}
 	
 	public void authenticate(final ISessionCreationCallback callback, String username, String password) {
 		
 		new AuthenticationTask(new IAuthenticationCallback() {
 			@Override
 			public void onSuccess(String sessionId) {
 				mSettings.edit().putString("SESSION_ID", sessionId).commit();
				callback.onAuthSuccess();
 			}
 
 			@Override
 			public void onError() {
				callback.onAuthError();
 			}
 		}).execute(username, password);
 	}
 	
 	public void destroySession() {
 		mSettings.edit().putString("SESSION_ID", "").commit();
 	}
 
 	public String getSessionId() {
 		System.out.println(mSettings.getString("SESSION_ID", ""));
 		return mSettings.getString("SESSION_ID", "");
 	}
 	
 	public boolean isAuthenticated() {
 		return !getSessionId().equals("");
 	}
 }
 	
