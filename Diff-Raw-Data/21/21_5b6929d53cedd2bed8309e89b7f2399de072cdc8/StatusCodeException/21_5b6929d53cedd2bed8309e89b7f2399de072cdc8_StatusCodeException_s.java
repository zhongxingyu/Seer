 package taco;
 
 /**
  * Throw this exception within your execute() method of your {@link Controller}
  * to signal a specific status code to a client.
  * 
  * @author henper
  * 
  */
 @SuppressWarnings("serial")
 public class StatusCodeException extends RuntimeException {
 
 	/**
	 * The request cannot be fulfilled due to bad syntax
	 */
 	public static final int BAD_REQUEST = 400;
 	
 	/**
	 * Similar to 403 Forbidden, but specifically for use when authentication is possible but has failed or not yet been provided.
	 */
 	public static final int UNAUTHORIZED = 401;
 	
 	/**
	 * The request was a legal request, but the server is refusing to respond to it.
	 * Unlike a 401 Unauthorized response, authenticating will make no difference.
	 */
 	public static final int FORBIDDEN = 403;
 	 
 	/**
 	 * The requested resource could not be found but may be available again in the future.
 	 */
 	public static final int NOT_FOUND = 404;
 	
 	
 	private int code;
 
 	/**
 	 * Creates a {@link StatusCodeException} with the specified status code
 	 * 
 	 * @param code
 	 * @param message
 	 */
 	public StatusCodeException(int code, String message) {
 		this(code, message, null);
 	}
 
 	public StatusCodeException(int code, String message, Throwable t) {
 		super(message, t);
 		this.code = code;
 	}
 
 	/**
 	 * Gets the status code
 	 * 
 	 * @return
 	 */
 	public int getCode() {
 		return code;
 	}
 
 }
