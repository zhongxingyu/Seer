 package org.kalibro.core.model.abstracts;
 
 import static java.lang.annotation.ElementType.*;
 
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
@Target(TYPE)
 @Retention(RetentionPolicy.RUNTIME)
 public @interface SortingMethods {
 
 	String[] value();
 }
