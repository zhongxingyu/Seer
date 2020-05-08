 package org.mendix.runtime.validation;
 
 import org.springframework.util.Assert;
 import org.springframework.validation.Errors;
 import org.springframework.validation.Validator;
 
 /**
  * @author Eric Jan Malotaux
  * 
  */
 public class MinValueValidator implements Validator {
 
     private String field;
 
     private long min;
 
     private Class clazz;
 
     /**
      * @param field
      * @param min
      */
     public MinValueValidator(Class clazz, String field, long min) {
         this.clazz = clazz;
         this.field = field;
         this.min = min;
     }
 
     @SuppressWarnings("unchecked")
     public boolean supports(Class clazz) {
         return this.clazz.isAssignableFrom(clazz);
     }
 
     public void validate(Object target, Errors errors) {
 
         Long value = null;
 
         if (errors.getFieldValue(field) instanceof Integer) {
             value = ((Integer) errors.getFieldValue(field)).longValue();
         } else if (errors.getFieldValue(field) instanceof Long) {
             value = ((Long) errors.getFieldValue(field));
         }
 
         if (value != null) {
             if (value < min) {
                 errors.rejectValue(field, "field.value.min", new Long[] { new Long(min), new Long(value) }, field
                         + " should be at least " + min + ", but was " + value);
             }
         }
     }
 }
