 /*
  * Copyright © 2012, Source Tree, All Rights Reserved
  * 
  * ValidationUtil.java
  * Modification History
  * *************************************************************
  * Date				Author						Comment
  * 04-Nov-2012		Venkaiah Chowdary Koneru	Created
  * *************************************************************
  */
 package org.sourcetree.interview.support.validation;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import javax.validation.ConstraintViolation;
 import javax.validation.Validator;
 
 import org.springframework.context.MessageSource;
 
 /**
  * @author Venkaiah Chowdary Koneru
  * 
  */
 public final class ValidationUtil
 {
 	/**
 	 * Private constructor to restrict instantiability.
 	 */
 	private ValidationUtil()
 	{
 	}
 
 	/**
 	 * Validates an object against number of groups. This helper method is
 	 * intended for grabbing all the messages across all the groups.
 	 * 
 	 * @param object
 	 *            the object that need to be validated
 	 * @param validator
 	 *            validator instance
 	 * @param messageSource
 	 *            message source instance to retrieve messages
 	 * @return errors map with codes as key and messages as value.
 	 */
 	public static Map<String, String> validate(final Object object,
 			final Validator validator, final MessageSource messageSource)
 	{
 		Map<String, String> errors = new HashMap<String, String>();
 		Set<ConstraintViolation<Object>> violations = validator
 				.validate(object);
 
 		for (ConstraintViolation<Object> violation : violations)
 		{
 			errors.put(violation.getPropertyPath().toString(), messageSource
 					.getMessage(violation.getMessage(), null, null));
 		}
 
 		violations = validator.validate(object, SecondGroup.class);
 		for (ConstraintViolation<Object> violation : violations)
 		{
			if (errors.containsKey(violation.getPropertyPath().toString()))
 			{
 				errors.put(violation.getPropertyPath().toString(),
 						messageSource.getMessage(violation.getMessage(), null,
 								null));
 			}
 		}
 
 		return errors;
 	}
 }
