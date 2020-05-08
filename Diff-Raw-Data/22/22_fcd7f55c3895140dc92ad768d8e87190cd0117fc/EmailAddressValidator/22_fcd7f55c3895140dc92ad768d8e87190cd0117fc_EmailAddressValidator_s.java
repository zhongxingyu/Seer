 // EmailAddressValidator.java
 package edu.umn.se.trap.rules.input;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import edu.umn.se.trap.data.ReimbursementApp;
 import edu.umn.se.trap.exception.FormProcessorException;
 import edu.umn.se.trap.exception.InputValidationException;
 
 /**
  * @author Dylan
  * 
  */
 public class EmailAddressValidator extends InputValidationRule
 {
     /** Logger for the EmailAddressValidator class */
     private static Logger log = LoggerFactory.getLogger(EmailAddressValidator.class);
 
     /** TRAP format for an email address */
    private final static String emailRegex = "^[\\w-_\\.+]*[\\w-\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
 
     @Override
     public void checkRule(ReimbursementApp app) throws InputValidationException,
             FormProcessorException
     {
         // Make sure we have an email address for the user
        String emailAddress = app.getUserInfo().getEmailAddress();
 
         // TODO Need to look up if this returns null or an empty string
         if (emailAddress == null)
         {
             throw new InputValidationException("Missing email address");
         }
 
         // TODO Make a better error message
         // Make sure the email address entered matches the valid email address format
        if (!emailRegex.matches(emailAddress))
         {
             throw new InputValidationException(
                     "Email address is not valid. example@example.com is a valid email format.");
         }
     }
 }
