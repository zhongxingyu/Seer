 package se.chalmers.krogkollen.backend;
 
 /**
  * An exception indicating that a object which was supposed to be in the backend cannot be found there
  * 
  * @author Oskar Karrman
  *
  */
 public class NotFoundInBackendException extends Exception {
 	
 	/**
	 * Generated serial version UID
	 */
	private static final long serialVersionUID = 303498786882068841L;

	/**
 	 * Create a new NotFoundInBackendException wthout additional information
 	 */
 	public NotFoundInBackendException() {}
 	
 	/**
 	 * Create a now NotFoundInBackendException with a specified message
 	 * 
 	 * @param message
 	 */
 	public NotFoundInBackendException(String message) {
 		super(message);
 	}
 }
