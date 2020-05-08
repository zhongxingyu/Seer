 package com.sirra.server.rest.annotations;
 
 import java.lang.annotation.*;
 
 /**
  * We want to support REST, and yet we also want our rest methods calleable from java using method parameters.
  * Use this annotation to do the mapping between REST variable names and the method parameter (by order).
  * 
  * @author aris
  */
 @Retention(RetentionPolicy.RUNTIME)
 public @interface Parameters {
 	String[] value();
 }

