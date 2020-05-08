 /**
  * 
  */
 package org.cotrix.web.shared;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class MainServiceException extends Exception {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 7184987134482636583L;
	
	public MainServiceException(){}
 
 	/**
 	 * @param message
 	 */
 	public MainServiceException(String message) {
 		super(message);
 	}
 
 }
