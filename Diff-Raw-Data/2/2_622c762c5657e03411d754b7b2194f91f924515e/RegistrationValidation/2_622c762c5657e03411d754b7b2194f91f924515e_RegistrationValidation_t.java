 package org.homebudget.services;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.annotation.Resource;
 
 import org.homebudget.model.UserDetails;
 import org.springframework.stereotype.Service;
 import org.springframework.validation.Errors;
 import org.springframework.validation.ValidationUtils;
 
 @Service
 public class RegistrationValidation {
 
    @Resource
    UserManagementService service;
 
    public boolean supports(Class<?> klass) {
 
       return UserDetails.class.isAssignableFrom(klass);
    }
 
    public void validate(Object target, Errors errors) {
 
       UserDetails aUserDetails = (UserDetails) target;
 
       validateEmptyFields(errors);
 
       String userUsername = aUserDetails.getUserUsername();
       validateUserUsername(userUsername, errors);
 
       String password = aUserDetails.getPassword();
       String confPassword = aUserDetails.getConfpassword();
       if (password == null || confPassword == null || password.compareTo(confPassword) != 0) {
          errors.rejectValue("password", "registration.password.dont_match");
       }
 
       String email = aUserDetails.getEmail();
       validateEmail(email, errors);
 
    }
 
    private void validateEmail(String email, Errors errors) {
 
       Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
       Matcher m = p.matcher(email);
 
       // check whether any match is found
       boolean matchFound = m.matches();
 
       if (!matchFound) {
          errors.rejectValue("email", "registration.email.invalid");
       }
 
       final UserDetails result = service.getUserByEmail(email);
       if (result != null) {
          errors.rejectValue("email", "registration.email.notunique");
       }
    }
 
    private void validateEmptyFields(Errors errors) {
 
       ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userUsername",
             "registration.userUsername.empty");
 
       ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "registration.email.empty");
 
       ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "registration.password.empty");
       ValidationUtils.rejectIfEmptyOrWhitespace(errors, "confpassword",
             "registration.password.empty");
 
    }
 
    private void validateUserUsername(String userUsername, Errors errors) {
 
       if (userUsername == null || userUsername.isEmpty() || (userUsername.length()) > 50) {
          errors.rejectValue("userUsername", "registration.user_username.size");
       }
       else {
          UserDetails result = service.getUserDetailsByUsername(userUsername);
         if (result != null && result.getUserUsername().equals(userUsername)) {
             errors.rejectValue("userUsername", "registration.user_username.notunique");
          }
       }
 
    }
 }
