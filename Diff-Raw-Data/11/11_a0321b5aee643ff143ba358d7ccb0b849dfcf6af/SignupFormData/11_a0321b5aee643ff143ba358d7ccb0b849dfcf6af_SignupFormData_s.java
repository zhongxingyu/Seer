 package views.formdata;
 
 import java.util.ArrayList;
 import java.util.List;
 import models.UserInfoDB;
 import play.data.validation.ValidationError;
 
 /**
  * Holds data for the Sign-up form.
  * @author eduardgamiao
  *
  */
 public class SignupFormData {
 
   /** Name. */
   public String signupName;
   
   /** Email. */
   public String signupEmail;
   
   /** Password. */
   public String signupPassword;
   
   /**
    * Default Constructor.
    */
   public SignupFormData() {
     
   }
   
   /**
    * Constructor.
    * @param name The User's name.
    * @param email The User's email.
    * @param password The User's password.
    */
   public SignupFormData(String name, String email, String password) {
     this.signupName = name;
     this.signupEmail = email;
     this.signupPassword = password;
   }
   
   /**
    * Provide form validation for the Signup form.
    * @return Null if no errors, else a list of ValidationErrors.
    */
   public List<ValidationError> validate() {
     List<ValidationError> errors = new ArrayList<>();
     
     if (signupName == null || signupName.length() == 0) {
      errors.add(new ValidationError("name", "Name is required."));
     }
     if (signupEmail == null || signupEmail.length() == 0) {
      errors.add(new ValidationError("email", "Email is required."));
     }
     if (UserInfoDB.getUser(signupEmail) != null) {
      errors.add(new ValidationError("email", "The email \"" + signupEmail + "\" is already used."));
     }    
     if (signupPassword == null || signupPassword.length() == 0) {
      errors.add(new ValidationError("password", "Password is required."));
     }
     return errors.isEmpty() ? null : errors;
   }
 }
