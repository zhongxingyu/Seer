 package domain;
 
 import validation.core.Field;
 import validation.core.Validator;
 
 import static validation.library.ValidationMatchers.*;
import static org.hamcrest.Matchers.lessThan;
 
 public class AMandatoryInteger {
     private final Field field;
 
     public AMandatoryInteger(Field field) {
         this.field = field;
     }
 
     public void describeTo(Validator validator) {
         validator.validateThat(field, isMandatory());
         validator.validateThat(field, isAWholeNumber());
        validator.validateThat(field, hasLength(lessThan(9)));
     }
 }
