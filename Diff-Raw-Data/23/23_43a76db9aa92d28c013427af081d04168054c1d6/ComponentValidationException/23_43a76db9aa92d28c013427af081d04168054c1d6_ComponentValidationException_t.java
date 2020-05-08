 /**-----------------------------------------------------------------------------
  | File Name: ComponentValidationException.java
  | Create Date: Jun 4, 2008
  | @author: Yudha Herwono (UHERWYU)                                                             
  |                                                                              
 | COPYRIGHT © 2008 Pearson Inc.
  | -----------------------------------------------------------------------------
  |                                                                              
  | History:                                                                     
  |==============================================================================
  | Flags  Date      AUTHOR    Comments                                          
  | -----------------------------------------------------------------------------
  | YH01                                                                  
  \*-----------------------------------------------------------------------------*/
 package com.pearson.ed.lplc.exception;
 
 import java.util.List;
 
 /**
  * Thrown to indicated that a component has failed validation. A list of
  * Strings indicating the errors that occurred may be specified in the
  * constructor.
  * 
  * @author UHERWYU
  */
 public class ComponentValidationException extends LPLCBaseException {
 	private static final long serialVersionUID = 1L;
 	
 	private List<String> validationErrors;
 
 	/**
 	 * Constructs a ComponentValidationException with the offending component
 	 * and a list of validation errors. The constructor invokes <code>
 	 * LPLCBaseException(String message)</code>, passing a generated message.
 	 * 
 	 * @param obj
 	 * 			the component that failed validation.
 	 * @param validationErrors
 	 * 			a list of validation errors.
 	 */
 	public ComponentValidationException(Object obj,List<String> validationErrors) {
 		super("Business object " + obj + " failed validation. errors:" + validationErrors);
 		this.validationErrors = validationErrors;
 	}
 	
 	/**
 	 * Fetches the list of validation errors for the exception.
 	 * 
 	 * @return the list of validation errors.
 	 */
 	public List<String> getValidationErrors() {
 		return validationErrors;
 	}
 	
 	/**
 	 * The default constructor for ComponentValidationException. The message is
 	 * null and the cause unspecified.
 	 */
 	public ComponentValidationException() {
 		super();
 	}
 
 	/**
 	 * Constructs a new ComponentValidationException with the passed message
 	 * and cause.
 	 * 
 	 * @param message
 	 * 			the message for the exception.
 	 * @param cause
 	 * 			the cause of the exception.
 	 */
 	public ComponentValidationException(String message, Throwable cause) {
 		super(message, cause);
 	}
 
 	/**
 	 * Constructs a new ComponentValidationException with the passed message.
 	 * 
 	 * @param message
 	 * 			the message for the exception.
 	 */
 	public ComponentValidationException(String message) {
 		super(message);
 	}
 
 	/**
 	 * Constructs a new ComponentValidationException with the passed caused.
 	 * The message is automatically generated from the cause.
 	 * 
 	 * @param cause
 	 * 			the cause of the exception.
 	 */
 	public ComponentValidationException(Throwable cause) {
 		super(cause);
 	}
 }
