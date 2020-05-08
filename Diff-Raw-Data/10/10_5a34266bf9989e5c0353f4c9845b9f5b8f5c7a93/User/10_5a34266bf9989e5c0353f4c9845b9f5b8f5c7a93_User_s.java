 package com.cl9p.domain;
 
 import org.springframework.validation.annotation.Validated;
 
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
 
 @Validated
 public class User {
     @NotNull(message = "lastName cannot be NULL")
    @Min(value = 2, message = "lastName must be longer than 2")
     String lastName;
 
     @NotNull
    @Min(2)
     String firstName;
     String email;
 
     public User() {
        this.lastName = new String();
     }
 }
