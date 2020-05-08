 package org.filterinterceptor.spi;
 
 import java.lang.annotation.Documented;
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 /**
  * Set this annotation on the method which you want to change the treatment
  * 
 * @See Filter
  */
 @Retention(RetentionPolicy.RUNTIME)
 @Target(ElementType.METHOD)
 @Documented
 public @interface FilteredMethod {
 }
