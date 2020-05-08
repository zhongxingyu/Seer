 package com.iq4j.utils.validation;
 
 import javax.validation.ConstraintValidator;
 import javax.validation.ConstraintValidatorContext;
 
 import com.iq4j.utils.model.Range;
 
 @SuppressWarnings("rawtypes")
 public class RangeCheckValidator implements	ConstraintValidator<RangeCheck, Range> {
 
 	private boolean nullable;
 	private boolean allowEquality;
 	
 	@Override
 	public void initialize(RangeCheck constraintAnnotation) {
 		nullable = constraintAnnotation.nullable();
 		allowEquality = constraintAnnotation.allowEquality();
 	}
 
 	@Override
 	public boolean isValid(Range value, ConstraintValidatorContext context) {
 		
 		context.disableDefaultConstraintViolation();
 		if(!nullable) {
 			if(value == null || value.isEmpty()) {
 				context.buildConstraintViolationWithTemplate("may not be null.").addConstraintViolation();
 				return false;				
 			}
 		}
 		else {
 			
			if(value != null) {
 				
 				if(!allowEquality && value.isMinEqualsMax())	
 				{
 					context.buildConstraintViolationWithTemplate("Minimum may not be equal maximum").addConstraintViolation();
 					return false;				
 				}	
 				
 				if(value.compareValues() > 0){				
 					context.buildConstraintViolationWithTemplate("Minimum may not be greater than maximum").addConstraintViolation();
 					return false;
 				}
 				
 			}
 		}
 				
 		return true;
 	}
 
 }
