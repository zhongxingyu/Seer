 package epfl.sweng.authentication;
 
 /**
  * 
  */
 public interface ISessionCreationCallback {
 	/**
 	 * 
 	 */
	void onAuthSuccess();
 	/**
 	 * 
 	 */
	void onAuthError();
 }
