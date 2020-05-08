 package com.meadowhawk.homepi.util.service;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 /**
 * Fired before a method fires to enforce security and prevent anything from processing until the ApiKey has been validated. 
 * NOTE: Method signature parameter order is mandatory: (serialId, apiKey,...)
  * @author lee
  */
 @Retention(RetentionPolicy.RUNTIME)
 @Target(ElementType.METHOD)
 public @interface ApiKeyRequiredBeforeException {
 
 }
