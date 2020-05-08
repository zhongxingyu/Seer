 package com.spring.samples;
 
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
 
 /**
  * HelloWorld class
  * 
  * @author Malaiappan.K
  * @since 15-Sep-2013
  * @version 1.0
  */
 @Component
 public class HelloWorld {
 
     private final HelloSpring helloSpring;
     
     /**
      * Instantiates a new hello world.
      *
      * @param helloSpring the hello spring
      */
     @Autowired
     public HelloWorld(HelloSpring helloSpring) {
         this.helloSpring = helloSpring;
     }
 
     /**
      * Gets the hello spring.
      * 
      * @return the helloSpring
      */
     public HelloSpring getHelloSpring() {
         return helloSpring;
     }
 
 }
