 #set( $symbol_pound = '#' )
 #set( $symbol_dollar = '$' )
 #set( $symbol_escape = '\' )
 package ${package}.domain;
 
 import org.springframework.validation.Errors;
 import org.springframework.validation.ValidationUtils;
 import org.springframework.validation.Validator;
 
 
 public class ItemValidator implements Validator {
 
     public boolean supports(Class<?> clazz) {
 
         return Item.class.equals(clazz);
     }
 
 
     public void validate(Object target, Errors errors) {
 
         ValidationUtils.rejectIfEmptyOrWhitespace(errors, "description", "errors.empty");
 
         Item item = (Item) target;
         String description = item.getDescription();
 
         if (description.contains("XXX")) {
             errors.rejectValue("description", "errors.contains");
         }
 
         if (description.length() > 140) {
             errors.rejectValue("description", "errors.toolong");
         }
     }
 }
