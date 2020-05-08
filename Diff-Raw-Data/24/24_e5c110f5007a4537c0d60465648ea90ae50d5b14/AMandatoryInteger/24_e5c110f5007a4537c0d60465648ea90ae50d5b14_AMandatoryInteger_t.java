 package domain;
 
 import validation.core.Field;
 import validation.core.Validator;
 
import static org.hamcrest.Matchers.lessThanOrEqualTo;
 import static validation.library.ValidationMatchers.*;
 
 public class AMandatoryInteger {
     private final Field field;
 
     public AMandatoryInteger(Field field) {
         this.field = field;
     }
 
     public void describeTo(Validator validator) {
         validator.validateThat(field, isMandatory());
         validator.validateThat(field, isAWholeNumber());
        validator.validateThat(field, hasLength(lessThanOrEqualTo(4)));
     }
 }
