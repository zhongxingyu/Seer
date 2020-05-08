 package com.spaceraccoons.core.validation.constraints;
 
 import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
 import static java.lang.annotation.ElementType.CONSTRUCTOR;
 import static java.lang.annotation.ElementType.FIELD;
 import static java.lang.annotation.ElementType.METHOD;
 import static java.lang.annotation.ElementType.PARAMETER;
 import static java.lang.annotation.RetentionPolicy.RUNTIME;
 
 import java.lang.annotation.Documented;
 import java.lang.annotation.Retention;
 import java.lang.annotation.Target;
 
 import javax.validation.Constraint;
 import javax.validation.Payload;
 
 /**
  * The annotated element must represent a valid user name.
  * <p/>
  * Accepts {@code CharSequence}. {@code null} elements are considered valid.
  * 
  * @author Benjamin P. Jung
  */
 @Target({ METHOD, FIELD, PARAMETER })
 @Retention(RUNTIME)
 @Documented
 @Constraint(validatedBy = { })
 public @interface Username {
 
     String message() default "{com.spaceraccoons.core.validation.constraints.Username.message}";
 
     Class<?>[] groups() default { };
 
     Class<? extends Payload>[] payload() default { };
 
     /**
      * Defines several {@link Username} annotations on the same element.
     * @see com.spaceraccoons.core.validation.constraints
      */
     @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
     @Retention(RUNTIME)
     @Documented
     @interface List {
         Username[] value();
     }
 }
