 package com.in6k.mypal.form;
 
 import org.hibernate.validator.constraints.Email;
 
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 public class RegistrationForm {
     @Size(min = 5, max = 25)
     private String firstName;
 
     @Size(min = 5, max = 25)
     private String lastName;
 
     @Email
     @NotNull
     private String email;
 
     @Size(min = 5, max = 25)
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
         this.password = password;
     }
 
     public String getConfirm() {
         return confirm;
     }
 
     public void setConfirm(String confirm) {
         this.confirm = confirm;
     }
 }
