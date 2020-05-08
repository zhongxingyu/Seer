 package it.sevenbits.conferences.web.validator;
 
 import it.sevenbits.conferences.domain.User;
 import it.sevenbits.conferences.service.UserService;
 import it.sevenbits.conferences.web.form.ChangePasswordForm;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
 import org.springframework.security.crypto.password.PasswordEncoder;
 import org.springframework.stereotype.Component;
 import org.springframework.validation.Errors;
 import org.springframework.validation.ValidationUtils;
 import org.springframework.validation.Validator;
 
 /**
  *  Validator for changing password form.
  */
 @Component
 public class ChangePasswordValidator implements Validator {
 
     @Autowired
     private UserService userService;
 
     @Override
     public boolean supports(Class<?> clazz){
         return ChangePasswordForm.class.equals(clazz);
     }
 
     @Override
     public void validate(Object target, Errors errors) {
         ChangePasswordForm changePasswordForm = (ChangePasswordForm) target;
         validateEmail(changePasswordForm, errors);
         validateOldPassword(changePasswordForm, errors);
         validateNewPassword(errors);
     }
 
     private void validateEmail(ChangePasswordForm form, Errors errors) {
         ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "email.empty", "Email не должен быть пустым");
         if (!isUserExists(form.getEmail())) {
             errors.rejectValue("email","email.notExists","Такого пользователя не существует.");
         }
     }
 
     private void validateOldPassword(ChangePasswordForm form, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "oldPassword", "oldPassword.empty", "Пароль не должен быть пустым");
         PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
         User user = userService.getUser(form.getEmail());
        if ( user != null && !passwordEncoder.matches(form.getOldPassword(),user.getPassword())) {
             errors.rejectValue("oldPassword","oldPassword.notExists","Неверный пароль");
         }
     }
 
     private void validateNewPassword(Errors errors) {
         ValidationUtils.rejectIfEmptyOrWhitespace(errors, "newPassword", "newPassword.empty", "Пароль не должен быть пустым");
     }
 
     public boolean isUserExists(String email) {
         boolean isExists = false;
         if (userService.getUserByEmail(email) != null) {
             isExists = true;
         }
         return  isExists;
     }
 
 }
