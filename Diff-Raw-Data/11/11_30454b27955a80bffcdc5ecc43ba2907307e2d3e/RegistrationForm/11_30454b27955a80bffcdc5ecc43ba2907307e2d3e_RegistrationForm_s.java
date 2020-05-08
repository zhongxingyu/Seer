 package com.in6k.mypal.form;
 
 import com.in6k.mypal.util.SecurityUtil;
 import org.hibernate.validator.constraints.Email;
 import org.hibernate.validator.constraints.NotBlank;
 import org.hibernate.validator.constraints.NotEmpty;
 
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 public class RegistrationForm {
 
     @NotEmpty(message = "*First name should not be empty")
     private String firstName;
 
     @NotEmpty(message = "*Last name should not be empty")
     private String lastName;
 
     @Email(message = "*It is not valid email")
     @NotEmpty(message = "*Email should not be empty")
     private String email;
 
     @NotEmpty(message = "*Password should not be empty")
     private String password;
 

     private String confirm;
 
     public boolean isPasswordsValid() {
         if (null == password) {
             return false;
         }
 
         return password.equals(confirm);
     }
 
     public String getFirstName() {
         return firstName;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     public String getLastName() {
         return lastName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
        this.password = SecurityUtil.passwordEncoder(password);
     }
 
     public String getConfirm() {
         return confirm;
     }
 
     public void setConfirm(String confirm) {
         this.confirm = confirm;
     }
 }
