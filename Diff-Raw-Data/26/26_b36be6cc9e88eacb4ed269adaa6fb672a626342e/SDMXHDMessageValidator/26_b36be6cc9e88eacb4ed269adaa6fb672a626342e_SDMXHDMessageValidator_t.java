/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
 
 package org.openmrs.module.sdmxhdintegration;
 
 import org.springframework.validation.Errors;
 import org.springframework.validation.ValidationUtils;
 import org.springframework.validation.Validator;
 
 /**
 * Validator for message objects
  */
 public class SDMXHDMessageValidator implements Validator {
 	
 	/**
 	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
 	 */
 	@Override
 	public boolean supports(Class clazz) {
 		return SDMXHDMessage.class.equals(clazz);
 	}
 	
 	/**
 	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
 	 */
 	@Override
 	public void validate(Object obj, Errors e) {
		ValidationUtils.rejectIfEmptyOrWhitespace(e, "name", Constants.MODULE_ID + ".error.requiredField");
		ValidationUtils.rejectIfEmptyOrWhitespace(e, "sdmxhdZipFileName", Constants.MODULE_ID + ".error.requiredFile");
	}	
 }
