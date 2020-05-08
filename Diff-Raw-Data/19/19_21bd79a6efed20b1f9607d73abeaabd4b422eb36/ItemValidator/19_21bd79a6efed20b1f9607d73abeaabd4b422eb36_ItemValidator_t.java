 #set( $symbol_pound = '#' )
 #set( $symbol_dollar = '$' )
 #set( $symbol_escape = '\' )
 package ${package}.domain;
 
 import org.springframework.validation.Errors;
 import org.springframework.validation.ValidationUtils;
 import org.springframework.validation.Validator;
 
 
/*
 * This class is a simple demonstration how to implement bean validation for your entities. This validator will be
 * injected into the controller bean.
 */
 public class ItemValidator implements Validator {
 
     public boolean supports(Class<?> clazz) {
 
         return Item.class.equals(clazz);
     }
 
 
     public void validate(Object target, Errors errors) {
 
        /*
         * Validation takes place programmatically. The passed in Errors object allows to add messages for properties on
         * the validated object. The messages are taken from the configured Spring MessageSource and are looked up by
         * the following key <errorCode>.<lowercase object classname>.<property>. For example the following error is
         * looked up as errors.empty.item.description.
         */
         ValidationUtils.rejectIfEmptyOrWhitespace(errors, "description", "errors.empty");
 
         Item item = (Item) target;
         String description = item.getDescription();
 
         if (description.contains("XXX")) {
             errors.rejectValue("description", "errors.contains");
         }
 
         if (description.length() > 140) {
             errors.rejectValue("description", "errors.toolong");
         }

        /*
         * You can also add global errors, which are not specific to a particular property.
         */
        if (description.contains("DONE") && !Status.DONE.equals(item.getStatus())) {
            errors.reject("errors.misleading");
        }
     }
 }
