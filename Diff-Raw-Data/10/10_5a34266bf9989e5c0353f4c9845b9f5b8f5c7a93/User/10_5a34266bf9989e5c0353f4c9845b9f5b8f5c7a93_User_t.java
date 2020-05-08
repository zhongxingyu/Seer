 package com.cl9p.domain;
 
 import org.springframework.validation.annotation.Validated;
 
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
 
 @Validated
 public class User {
     @NotNull(message = "lastName cannot be NULL")
    @Size(min = 2, message = "lastName must be longer than 2")
     String lastName;
 
     @NotNull
    @Size(min = 2, message = "firstName must be longer than 2")
     String firstName;
     String email;
 
     public User() {
        this.lastName = new String("faus");
     }
 }
