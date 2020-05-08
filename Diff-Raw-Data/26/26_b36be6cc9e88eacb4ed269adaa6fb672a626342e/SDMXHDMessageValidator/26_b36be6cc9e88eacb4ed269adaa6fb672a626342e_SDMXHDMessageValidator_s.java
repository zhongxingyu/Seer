 
 package org.openmrs.module.sdmxhdintegration;
 
 import org.springframework.validation.Errors;
 import org.springframework.validation.ValidationUtils;
 import org.springframework.validation.Validator;
 

 /**
 * @author Jembi
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
		ValidationUtils.rejectIfEmpty(e, "name", "field.required", "This field is required");
		ValidationUtils.rejectIfEmpty(e, "sdmxhdZipFileName", "field.required.sdmxhdMessage.name", "A SDMX-HD Message must be uploaded");
	}
	
 }
