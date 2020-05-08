 package com.thoughtworks.go;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.hamcrest.core.Is.is;
import static 
org.junit.Assert.assertThat;
 
 public class HelloWorldTest
 {
     private HelloWorld helloWorld;
 
     @Before
     public void setup()
     {
         helloWorld = new HelloWorld();
     }
 
     @Test
     public void shouldReturnItWorks() throws Exception
     {
         assertThat(helloWorld.doesItWork(), is("It works!"));
     }
 
     @Test
     public void shouldReturnFoo() throws Exception
     {
         assertThat(helloWorld.getFoo(), is("foo"));
     }
 
     @Test
     public void shouldReturnBar() throws Exception
     {
         assertThat(helloWorld.getBar(), is("bar"));
     }
 
 }

