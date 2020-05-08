 package com.odobo.web;
 
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.annotation.AnnotationConfigApplicationContext;
 
 
 /**
  * @author reiern70
  */
 public class ContextLoader implements org.springframework.test.context.ContextLoader {
 
 	public String[] processLocations(Class<?> clazz, String... locations) {
 		return new String[] {};
 	}
 
 	public ApplicationContext loadContext(String... locations) {
		return new AnnotationConfigApplicationContext(com.odobo.web.ApplicationContext.class);
 	}
 }
