 package org.greatage.ioc.annotations;
 
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

 /**
  * @author Ivan Khalopik
  * @since 1.1
  */
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
 @MarkerAnnotation
 public @interface Named {
 
 	String value();
 }
