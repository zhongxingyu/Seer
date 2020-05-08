 // EmailAddressValidator.java
 package edu.umn.se.trap.rules.input;
 
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private final static String emailRegex = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private final Pattern pattern;

    private Matcher matcher;

    public EmailAddressValidator()
    {
        pattern = Pattern.compile(emailRegex);
    }
 
     @Override
     public void checkRule(ReimbursementApp app) throws InputValidationException,
             FormProcessorException
     {
         // Make sure we have an email address for the user
        CharSequence emailAddress = app.getUserInfo().getEmailAddress();
        pattern.matcher(emailAddress);
 
         // TODO Need to look up if this returns null or an empty string
         if (emailAddress == null)
         {
             throw new InputValidationException("Missing email address");
         }
 
         // TODO Make a better error message
         // Make sure the email address entered matches the valid email address format
        if (!matcher.matches())
         {
             throw new InputValidationException(
                     "Email address is not valid. example@example.com is a valid email format.");
         }
     }
 }
