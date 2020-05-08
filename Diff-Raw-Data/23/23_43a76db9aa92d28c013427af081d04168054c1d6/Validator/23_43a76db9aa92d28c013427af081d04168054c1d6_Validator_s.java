 /**-----------------------------------------------------------------------------
  | File Name: Validator.java
  | Create Date: Jun 4, 2008
  | @author: UHERWYU                                                             
  |                                                                              
 | COPYRIGHT  2008 Pearson Inc.   
  | -----------------------------------------------------------------------------
  |                                                                              
  | History:                                                                     
  |==============================================================================
  | Flags  Date      AUTHOR    Comments                                          
  | -----------------------------------------------------------------------------
  | YH01                                                                  
  \*-----------------------------------------------------------------------------*/
 package com.pearson.ed.lplc.model.validation;
 
 import java.util.List;
 
 /**
  * Validator is the interface that all model validator should implement.
  * 
  */
 
 public interface Validator {
 	/**
 	 * Performs validation on the domain objects. An object passes validation
 	 * if it obeys all of the business constraint rules.
 	 * 
 	 * @return true if the object passes validation.
 	 */
 	boolean validate(Object domainObject);
 	
 	/**
 	 * Creates a list of errors that occurred during the most recent call to
 	 * validate.
 	 * 
 	 * @return a list of errors that occurred during the last validation.
 	 */
 	List <String> getValidationErrors();
 	
 	/**
 	 * Creates an array of Classes which can be validated.
 	 * 
 	 * @return an array of Classes available for validation.
 	 */
 	Class[] getValidatedTypes();
 }
