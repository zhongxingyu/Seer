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
 package org.openmrs.calculation.validator;
 
 import org.apache.commons.lang.StringUtils;
 import org.openmrs.annotation.Handler;
 import org.openmrs.api.context.Context;
 import org.openmrs.calculation.TokenRegistration;
 import org.openmrs.calculation.api.TokenRegistrationService;
 import org.openmrs.calculation.util.CalculationUtil;
 import org.openmrs.util.OpenmrsUtil;
 import org.springframework.validation.Errors;
 import org.springframework.validation.ValidationUtils;
 import org.springframework.validation.Validator;
 
 /**
  * Validator for {@link TokenRegistration}
  */
 @Handler(supports = { TokenRegistration.class }, order = 50)
 public class TokenRegistrationValidator implements Validator {
 	
 	/**
 	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
 	 */
 	@SuppressWarnings("rawtypes")
     public boolean supports(Class c) {
 		return TokenRegistration.class.isAssignableFrom(c);
 	}
 	
 	/**
 	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
 	 * @should fail if token registration name is not unique amongst all tokens
 	 * @should fail if token registration name is empty or whitespace
 	 * @should fail if token registration provider class name is empty or whitespace
 	 * @should fail if token registration calculation name is empty or whitespace
 	 * @should fail if token registration does not represent a valid calculation
 	 * @should pass valid token registration
 	 * @should pass existing token registration
 	 */
 	public void validate(Object obj, Errors errors) {
 		TokenRegistration target = (TokenRegistration) obj;
 		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "error.null");
 		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "providerClassName", "error.null");
 		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "calculationName", "error.null");
 		
 		/* we need to check if this registration token has unique name amongst all registered tokens for each provider */
 		if (StringUtils.isNotBlank(target.getName())) {
 			TokenRegistrationService tokenService = Context.getService(TokenRegistrationService.class);
 			TokenRegistration possibleDuplicate = tokenService.getTokenRegistrationByName(target.getName());
 			if (possibleDuplicate != null && !OpenmrsUtil.nullSafeEquals(possibleDuplicate.getId(), target.getId())) {
 				errors.rejectValue("name", "calculation.TokenRegistration.error.nonUniqueTokenName");
 			}
 		}
 		try {
			CalculationUtil.getCalculationForTokenRegistration(target);
 		}
 		catch (Exception e) {
 			errors.reject(e.getMessage());
 		}
 	}
 	
 }
