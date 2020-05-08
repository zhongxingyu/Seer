 /**-----------------------------------------------------------------------------
  | File Name: LPLCFieldsNotValidException.java
  | Create Date: Jun 5, 2008
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
 
 /**
  * Thrown to indicated that the LPLC Fields are not valid.
  * 
  * @author UHERWYU
  */
 public class LPLCFieldsNotValidException extends LPLCBaseException {
 	private static final long serialVersionUID = 1L;
 	
 	/**
 	 * The default constructor for LPLCFieldsNotValidException.
 	 */
 	public LPLCFieldsNotValidException() {
 		super();
 	}
 
 	/**
 	 * Constructs a new LPLCFieldsNotValidException with the passed message and
 	 * cause.
 	 * 
 	 * @param message
 	 * 			the message for the exception.
 	 * @param cause
 	 * 			the cause of the exception.
 	 */
 	public LPLCFieldsNotValidException(String message, Throwable cause) {
 		super(message, cause);
 	}
 
 	/**
 	 * Constructs a new LPLCFieldsNotValidException with the passed message.
 	 * 
 	 * @param message
 	 * 			the message for the exception.
 	 */
 	public LPLCFieldsNotValidException(String message) {
 		super(message);
 	}
 
 	/**
 	 * Constructs a new LPLCFieldsNotValidException with the passed cause. The
 	 * message is automatically generated from the cause.
 	 * 
 	 * @param cause
 	 * 			the cause of the exception.
 	 */
 	public LPLCFieldsNotValidException(Throwable cause) {
 		super(cause);
 	}
 }
