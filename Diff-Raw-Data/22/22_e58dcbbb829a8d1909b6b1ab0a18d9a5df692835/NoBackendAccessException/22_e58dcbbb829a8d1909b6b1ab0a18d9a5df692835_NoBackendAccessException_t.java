 package se.chalmers.krogkollen.backend;
 
 /**
  * An exception indicating that an action to the backend failed.
  * Causes could be, but not limited to, not logged in, backend is offline, no permission to edit selected Pub
  * 
  * @author Oskar Karrman
  *
  */
 public class NoBackendAccessException extends Exception {
 
 	/**
	 * Generated serial version UID
	 */
	private static final long serialVersionUID = -7468080825750362150L;

	/**
 	 * Create a new NoBackendAccessException without additional information
 	 */
 	public NoBackendAccessException() {}
 	
 	/**
 	 * Create a new NoBackendAccessException with a message
 	 * 
 	 * @param message the message saved in the exception
 	 */
 	public NoBackendAccessException(String message) {
 		super(message);
 	}
 	
 	/**
 	 * Create a new NoBackendAccessException with a message and an error code.
 	 * Output for the message becomes: "Errorcode: [errorcode], message: [message]
 	 * 
 	 * @param message
 	 * @param errorcode
 	 */
 	public NoBackendAccessException(String message, int errorcode) {
 		super("Errorcode: " + errorcode + ", message: " + message);
 	}
 }
