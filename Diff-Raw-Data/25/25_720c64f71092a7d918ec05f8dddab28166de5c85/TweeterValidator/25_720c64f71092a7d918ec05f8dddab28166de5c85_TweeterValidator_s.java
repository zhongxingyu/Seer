 package com.um.canario.validators;
 
 import java.util.Date;
 import com.um.canario.models.Tweeter;
 import org.springframework.util.StringUtils;
 import org.springframework.validation.Errors;
 
 public class TweeterValidator {
 
     public void validate(Tweeter tweeter, Errors errors, String rePassword) {
         String username = tweeter.getUsername();
         String email = tweeter.getEmail();
         String name = tweeter.getName();
         String lname = tweeter.getLname();
         Date birth = tweeter.getBirthDate();
         String password = tweeter.getPassword();
         Tweeter tFromDB = Tweeter.findTweeterByUsername(username);
         boolean exists = tFromDB != null;
         
         if (!StringUtils.hasText(username)) {
             errors.rejectValue("username", "required", "El Nombre de Usuario no puede estar vacío");
         }
         else if(exists){
         	errors.rejectValue("username", "unique", "Ese Nombre de Usuario ya esta ocupado, elija otro");	
         }

         if (!StringUtils.hasText(email)) {
             errors.rejectValue("email", "required", "El E-Mail no puede estar vacío");
         }
         else if(exists){
             if(tFromDB.getEmail().equals(email)) {
                 errors.rejectValue("email", "unique", "Ese E-Mail ya esta ocupado, elija otro");  
             }
         }
 
         if (!StringUtils.hasText(name)) {
             errors.rejectValue("name", "required", "Nombre no puede estar vacío");
         }
         if (!StringUtils.hasText(lname)) {
             errors.rejectValue("lname", "required", "Apellido no puede estar vacío");
         }
         if (!StringUtils.hasText(password)) {
             errors.rejectValue("password", "required", "Contraseña no puede estar vacío");
         } 
         else if(!password.equals(rePassword)) {
         	errors.rejectValue("password", "no_equal", "Las Contraseñas deben ser iguales");	
         }
 
     }
 
 }
