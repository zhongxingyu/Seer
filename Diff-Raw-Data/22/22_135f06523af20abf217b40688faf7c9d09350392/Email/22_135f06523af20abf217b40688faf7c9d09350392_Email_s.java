 package org.endeios.penguineye.domain.validation;
 
 import javax.validation.Constraint;
 
 @Constraint(validatedBy=EmailValidator.class)
 public @interface Email {

 }
