 package eu.neq.mais.connector;
 
 /**
  * Connector interface to standardize the connector implemetations.
  * One connector builds one connection to one back-end HIS, like GNU Health or SAP Healthcare.
  * @author seba
  *
  */
public abstract class Connector {
 	
	/**
	 * return single instance from the class
	 * @return Connector instance
	 */
	public static Connector getInstance(){
		return null;
	}
 	
 	/**
 	 * Loging into the Backend
 	 * @param username
 	 * @param password
 	 * @return Session ID
 	 */
 	public abstract String login(String username, String password);
 	
 	/**
 	 * Logout
 	 */
 	public abstract void logout();
 	
 	/**
 	 * Dummy method...
 	 * @param o
 	 */
 	public abstract Object exec(Object o);
 	
 }
