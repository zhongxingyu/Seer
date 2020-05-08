 package com.sandeepmore.caching;
 
 /**
  * <br><b>Creation date:</b> April 4, 2011 10:28 AM<br>
  * @author Sandeep More</dl>
  * 
  * <UL>
  * <LI>
  * </UL><BR>
  *
  */
 public class CachingException extends Exception {
 
      /**
	 * 
	 */
	private static final long serialVersionUID = -7336342248916593590L;

	/**
 	 * Default constructor
 	 */
 	public CachingException() {
 		super();
 	}
 	
 	/**
 	 * 
 	 * @param arg0
 	 */
 	public CachingException(String arg0) {
 		super(arg0);
 	}
 	
 	/**
 	 * 
 	 * @param cause
 	 */
 	public CachingException(Throwable cause) {
 		super(cause);
 	}
 	
 	/**
 	 * 
 	 * @param msg
 	 * @param cause
 	 */
 	public CachingException(String msg, Throwable cause) {
 		super (msg, cause);
 	}
 }
