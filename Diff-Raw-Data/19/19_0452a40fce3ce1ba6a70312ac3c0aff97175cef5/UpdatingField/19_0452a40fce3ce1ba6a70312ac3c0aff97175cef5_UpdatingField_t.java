 package org.skycastle.client;
 
import java.lang.annotation.*;
 
 /**
  * Indicates that a field can be updated with data from the server.
  */
 @Retention(RetentionPolicy.RUNTIME)
 @Target({ElementType.METHOD, ElementType.FIELD})
@Inherited
 public @interface UpdatingField {
 }
