 package com.kcalculator.api.validation;
 
 import static java.lang.annotation.ElementType.FIELD;
 import static java.lang.annotation.RetentionPolicy.RUNTIME;
 
 import java.lang.annotation.Documented;
 import java.lang.annotation.Retention;
 import java.lang.annotation.Target;
 
 import javax.validation.Constraint;
 import javax.validation.Payload;
 
 @Target({ FIELD })
 @Retention(RUNTIME)
 @Constraint(validatedBy = IsoCodeValidator.class)
 @Documented
 public @interface CheckIsoCode {
 
     // TODO: replace by a message bundle key and add bundle file
     // ValidationMessage.properties
    String message() default "The provided ISO-code is not supported by the JVM in use";
 
     Class<?>[] groups() default {};
 
     Class<? extends Payload>[] payload() default {};
 
 }
