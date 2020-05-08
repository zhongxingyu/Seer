 package cz.muni.fi.pv243.model.validation;
 
 import java.lang.annotation.Documented;
 import java.lang.annotation.Retention;
 import java.lang.annotation.Target;
 
 import static java.lang.annotation.ElementType.*;
 import static java.lang.annotation.RetentionPolicy.RUNTIME;
 
 import javax.validation.Constraint;
 import javax.validation.Payload;
 
 @Target( { METHOD, FIELD, ANNOTATION_TYPE })
 @Retention(RUNTIME)
 @Constraint(validatedBy = EmailValidator.class)
 @Documented
 public @interface ValidEmail {
 	
	String message() default "not a valid email address";
 
     Class<?>[] groups() default {};
 
     Class<? extends Payload>[] payload() default {};
 }
