 package epfl.sweng.authentication;
 
 /**
  * 
  */
 public interface ISessionCreationCallback {
 	/**
 	 * 
 	 */
	void onSessionCreateSuccess();
 	/**
 	 * 
 	 */
	void onSessionCreateError();
 }
