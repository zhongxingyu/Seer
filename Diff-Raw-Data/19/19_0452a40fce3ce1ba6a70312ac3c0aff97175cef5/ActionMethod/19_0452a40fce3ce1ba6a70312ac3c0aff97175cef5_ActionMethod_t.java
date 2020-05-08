 package org.skycastle.client;
 
import java.lang.annotation.*;
 
 /**
  * Indicates that a method handles some types of messages from the server.
  */
 @Retention(RetentionPolicy.RUNTIME)
 @Target(ElementType.METHOD)
@Inherited
 public @interface ActionMethod {
 }
