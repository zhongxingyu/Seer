 package net.tirasa.addressbook;
 
 import java.util.regex.Pattern;
 import org.apache.wicket.validation.IValidatable;
 import org.apache.wicket.validation.IValidator;
 import org.apache.wicket.validation.ValidationError;
 
 public class TelephoneValidator implements IValidator<String> {
 
     private static final long serialVersionUID = -2814784595929578608L;
 
     private final String INPUT_PATTERN = "^[0-9]{7,10}";
 
     private final Pattern pattern;
 
     TelephoneValidator() {
         pattern = Pattern.compile(INPUT_PATTERN);
     }
 
     @Override
     public void validate(IValidatable<String> validatable) {
         final String telephone = validatable.getValue();
         // validate telephone
        if (!pattern.matcher(telephone).matches()) {
             error(validatable, "bad-telephone-number");
         }
     }
 
     private void error(IValidatable<String> validatable, String errorKey) {
         ValidationError error = new ValidationError();
         error.addKey(getClass().getSimpleName() + "." + errorKey);
         validatable.error(error);
     }
 }
