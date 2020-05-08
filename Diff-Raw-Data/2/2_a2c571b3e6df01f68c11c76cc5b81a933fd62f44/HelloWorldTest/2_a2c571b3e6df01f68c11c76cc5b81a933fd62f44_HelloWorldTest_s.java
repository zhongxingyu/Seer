 package com.example.helloworld;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 
 public class HelloWorldTest {
 
     @Test
     public void greetResultsInHello() {
         World world = new World();
        assertEquals("Hello world", world.greet());
     }
 
 }
