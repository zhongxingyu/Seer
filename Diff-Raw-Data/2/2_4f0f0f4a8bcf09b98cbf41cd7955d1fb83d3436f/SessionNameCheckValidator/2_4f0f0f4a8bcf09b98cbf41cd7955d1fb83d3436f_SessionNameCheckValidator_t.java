 /**
  * @author Brad Leege <leege@doit.wisc.edu>
  * Created on 5/1/13 at 2:28 PM
  */
 package org.jasig.portlet.blackboardvcportlet.validations.validators;
 
 import org.apache.commons.lang.StringUtils;
 import org.jasig.portlet.blackboardvcportlet.validations.annotations.SessionNameCheck;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.validation.ConstraintValidator;
 import javax.validation.ConstraintValidatorContext;
 
 public class SessionNameCheckValidator implements ConstraintValidator<SessionNameCheck, String>
 {
 	private static final Logger logger = LoggerFactory.getLogger(SessionNameCheckValidator.class);
 
 	@Override
 	public void initialize(SessionNameCheck constraintAnnotation)
 	{
 	}
 
 	@Override
 	public boolean isValid(String value, ConstraintValidatorContext context)
 	{
 		logger.debug("isValid() called with value = {}", value);
 		if (StringUtils.isEmpty(value))
 		{
 			logger.debug("Null, so returning false.");
 			return false;
 		}
 
 		// Test For Size
 		if (value.length() > 255)
 		{
 			logger.debug("The string is longer than 255 characters, returning false.");
 			return false;
 		}
 
 		// Test for illegal characters
		if (value.contains("<") || value.contains("&") || value.contains("#") || value.contains("%"))
 		{
 			logger.debug("Illegal character found, so returning false.");
 			return false;
 		}
 
 		logger.debug("Passed all the tests, returning true.");
 		return true;
 	}
 }
