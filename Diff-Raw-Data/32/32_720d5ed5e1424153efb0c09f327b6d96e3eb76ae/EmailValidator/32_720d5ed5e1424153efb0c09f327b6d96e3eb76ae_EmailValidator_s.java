 package de.aidger.model.validators;
 
 import java.util.regex.Pattern;
 
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
 
        return Pattern.matches("/^[0-9a-z_\\.-]+@(([0-9]{1,3}\\.){3}[0-9]{1,3}|"
                + "([0-9a-z][0-9a-z-]*[0-9a-z]\\.)+[a-z]{2,3})$/i", (String) o);
     }
 
 }
