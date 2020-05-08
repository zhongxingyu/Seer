 /**
  * ValidFileValidator.java.
  *
  * @copyright 2011 Monits
  * @license Copyright (C) 2011. All rights reserved
  * @version Release: 1.0.0
  * @link http://www.monits.com/
  * @since 1.0.0
  */
 package com.monits.commons.validation;
 
 import javax.validation.ConstraintValidator;
 import javax.validation.ConstraintValidatorContext;
 
 /**
  * ValidFileValidator.java.
  *
  * @author Gaston Muñiz <gmuniz@monits.com>
  * @copyright 2011 Monits
  * @license Copyright (C) 2011. All rights reserved
  * @version Release: 1.0.0
  * @link http://www.monits.com/
  * @since 1.0.0
  */
 public class UploadedFileValidator implements
 ConstraintValidator<UploadedFile, Object> {
 
 	@Override
 	public void initialize(UploadedFile constraintAnnotation) {
 	}
 
 	@Override
 	public boolean isValid(Object value, ConstraintValidatorContext context) {
 
 		final String methodIsEmpty = "isEmpty";
 		final String methodGetOriginalName = "getOriginalFilename";
 
 		// This class should be "CommonMultipartFile"
 		final Class<? extends Object> clazz = value.getClass();
 
 		// Minimum size for the file, at least the extension.
 		final long minLength = 3;
 
 		try {
 
 			if((Boolean)clazz.getMethod(methodIsEmpty).invoke(value)) {
 				return false;
 			}
 
 		} catch (Exception e) {
 			// Ignored
 		}
 
 		try {
 
 			if(((String)clazz.getMethod(methodGetOriginalName).invoke(value)).length() >= minLength) {
				return true;
 			}
 
 		} catch (Exception e) {
 			// Ignored
 		}
 
 		return false;
 	}
 
 }
