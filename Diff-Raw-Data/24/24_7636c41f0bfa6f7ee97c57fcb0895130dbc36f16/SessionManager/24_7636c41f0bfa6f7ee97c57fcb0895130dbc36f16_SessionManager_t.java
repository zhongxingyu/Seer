 /**
  * 
  */
 package me.ericmiles.mobiletrans.session;
 
 import android.content.Context;
 import android.text.TextUtils;
 
 /**
 * Simple singleton responsible for managing the session id received from the
 * server.
  * 
  * @author 94728
  * 
  */
 public class SessionManager {
 
 	/*
 	 * Singleton
 	 */
 	private static SessionManager _instance = null;
 
 	private String sessionId = null;
 	private Context context = null;
 
 	private SessionManager(Context context) {
 		this.context = context;
 	}
 
 	public String getSessionId() {
 		return sessionId;
 	}
 
 	public boolean isSessionIdSet() {
 		return !TextUtils.isEmpty(sessionId);
 	}
 
 	// using package visibility, don't want anyone else mucking with the token
	// with the exception of the broadcast receiver set to listen to
	// login/logout operation
 	// responses
 	void setSessionId(String sessionId) {
		// ensure we don't have a thread trying to read this while we're setting
		// it to a new value
		synchronized (this) {
 			this.sessionId = sessionId;
 		}
 	}
 
	// yeah, yeah, i know the double locking mechanism in the singleton might
	// not work all the time...
 	public static final SessionManager getInstance(Context applicationContext) {
 		if (_instance == null) {
			synchronized (SessionManager.class) {
 				if (_instance == null) {
 					_instance = new SessionManager(applicationContext);
 				}
 			}
 		}
 		return _instance;
 	}
 }
