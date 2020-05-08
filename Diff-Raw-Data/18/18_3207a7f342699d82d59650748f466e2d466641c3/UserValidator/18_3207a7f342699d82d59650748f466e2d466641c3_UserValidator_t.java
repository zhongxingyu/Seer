 package org.aphillips.hw.domain;
 
 import java.text.MessageFormat;
 import java.util.Calendar;
 
 import org.apache.commons.lang.StringUtils;
 import org.aphillips.hw.impl.ValidationError;
 
 public class UserValidator {
 
   private User user;
 
   public UserValidator(User user) {
     this.user = user;
   }
 
   /**
    * At least one of the following fields must exist: First Name, Last Name, Email Address, or Phone Number
    * First and Last Name must be alpha-characters only
    * Email Address must be real email address
    * Phone Number can accept any of the following characters: ()-.0-9
    * Birthday must be an actual date, and in the past. "Actual date" means no Feb 31, for example.
   */
   public void validate() {
     if (StringUtils.isEmpty(user.getFirstName()) && StringUtils.isEmpty(user.getLastName())
         || StringUtils.isEmpty(user.getPhone())) {
       throw new ValidationError("Insufficent data to create user");
     }
 
     if (user.getFirstName() != null) {
       if (!StringUtils.isAlpha(user.getFirstName())) {
         throw new ValidationError(MessageFormat.format("First name \"{0}\" must contain apha characters only",
             user.getFirstName()));
       }
     }
 
     if (user.getLastName() != null) {
       if (!StringUtils.isAlpha(user.getLastName())) {
         throw new ValidationError(MessageFormat.format("Last name \"{0}\" must contain apha characters only",
             user.getLastName()));
       }
     }
 
     if (user.getEmail() != null) {
       String emailCaps = user.getEmail().toUpperCase();
       if (!emailCaps.matches("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}$")) {
         throw new ValidationError(MessageFormat.format("Invalid email \"{0}\"", user.getEmail()));
       }
     }
 
     if (user.getPhone() != null) {
       if (!user.getPhone().matches("^\\(?([0-9]{3})\\)?[-. ]?([0-9]{3})[-. ]?([0-9]{4})$")) {
         throw new ValidationError(MessageFormat.format("Invalid phone \"{0}\"", user.getPhone()));
       }
     }
 
     if (user.getDob() != null) {
       Calendar mostRecentLegalBirthDate = Calendar.getInstance();
       mostRecentLegalBirthDate.add(Calendar.YEAR, -18);
 
       if (user.getDob().after(mostRecentLegalBirthDate.getTime())) {
         throw new ValidationError(MessageFormat.format("Birth date \"{0}\" if after legal limit", user.getDob()));
       }
     }
   }
 
 }
