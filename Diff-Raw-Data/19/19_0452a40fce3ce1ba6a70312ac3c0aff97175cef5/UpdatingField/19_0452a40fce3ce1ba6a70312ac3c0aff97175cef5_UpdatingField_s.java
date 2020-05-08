 package org.skycastle.client;
 
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
 
 /**
  * Indicates that a field can be updated with data from the server.
  */
 @Retention(RetentionPolicy.RUNTIME)
 @Target({ElementType.METHOD, ElementType.FIELD})
 public @interface UpdatingField {
 }
