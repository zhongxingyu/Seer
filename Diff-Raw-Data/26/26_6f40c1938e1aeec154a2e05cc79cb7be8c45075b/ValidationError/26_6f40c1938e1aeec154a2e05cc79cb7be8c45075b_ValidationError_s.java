 /**
  * 
  */
 package com.snyder.review.shared.validator;
 
 /**
  * 
 * @author greg
  */
 public interface ValidationError
 {
 
 	Validator getValidator();
 	
 	String getMessage();
 	
 }
