 package it.sevenbits.space.web.validator;
 
 import it.sevenbits.space.dao.IUserDao;
import it.sevenbits.space.domain.User;
 import it.sevenbits.space.web.form.RegistrationForm;
 import org.apache.commons.validator.routines.EmailValidator;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.springframework.validation.Errors;
 import org.springframework.validation.ValidationUtils;
 import org.springframework.validation.Validator;
 
 @Component
 public class RegistrationValidator implements Validator {
 
     @Autowired
     private IUserDao userDao;
 
     @Override
     public boolean supports(Class<?> aClass) {
 
         return RegistrationForm.class.isAssignableFrom(aClass);
     }
 
     @Override
     public void validate(Object o, Errors errors) {
 
         RegistrationForm registrationForm = (RegistrationForm) o;
 
         ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", "username.empty", "Username must not be empty.");
         if ((registrationForm.getUsername().length()) > 16) {
             errors.rejectValue("username", "username.tooLong", "Username must be not more than 16 characters.");
         }
         if (!isUniqueUsername(registrationForm.getUsername())) {
             errors.rejectValue("username", "username.required", "User already exists.");
         }
 
         ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "password.empty", "Password must not be empty.");
         if (!(registrationForm.getPassword()).equals(registrationForm.getConfirmPassword())) {
             errors.rejectValue("confirmPassword", "confirmPassword.passwordDontMatch", "Passwords don't match.");
         }
         if (!EmailValidator.getInstance().isValid(registrationForm.getEmail())) {
             errors.rejectValue("email", "email.notValid", "Email address is not valid.");
         }
         if (!isUniqueEmail(registrationForm.getEmail())) {
             errors.rejectValue("email", "email.required", "Email already exists.");
         }
     }
 
     private boolean isUniqueUsername(final String username) {
 
         if (userDao.findUserByUsername(username) != null) {
             return false;
         }
         return true;
     }
 
     private boolean isUniqueEmail(final String email) {
 
         if (userDao.findUserByEmail(email) != null) {
             return false;
         }
         return true;
     }
 }
