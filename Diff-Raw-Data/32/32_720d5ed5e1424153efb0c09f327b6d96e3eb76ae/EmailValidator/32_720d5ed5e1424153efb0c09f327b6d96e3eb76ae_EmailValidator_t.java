 package de.aidger.model.validators;
 
 import java.util.regex.Pattern;
import java.util.regex.Matcher;
 
 import de.aidger.model.AbstractModel;
 
 /**
  *
  * @author rmbl
  */
 public class EmailValidator extends Validator {
 
     /**
      * Initializes the EmailValidator class.
      *
      * @param model
      *            The model to validate
      * @param members
      *            The member variables of the model
      */
     public EmailValidator(AbstractModel model, String[] members) {
         this.model = model;
         this.members = members;
     }
 
     /**
      * Validate an email address.
      *
      * @param o
      *            Variable to validate
      * @return True if the variable validates
      */
     @Override
     public boolean validateVar(Object o) {
         if (o == null) {
             return false;
         } else if (!o.getClass().getName().equals(String.class.getName())) {
             return false;
         }
 
        String expression =
                "^[\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = (String) o;
        Pattern pattern = Pattern.compile(expression,Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        return matcher.matches();        
     }
 
 }
