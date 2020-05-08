 package org.jrask.compiler;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 public class DefaultErrorLoggerTest {
 
     ErrorLogger logger = new DefaultErrorLogger();
 
     @Test
     public void testEmptyLogger() {
        assertTrue(logger.errors().isEmpty());
     }
 
     @Test
     public void testLogging() {
         String[] messages = { "error message", "another error message", "yet another one" };
         for (String msg : messages)
             logger.log(msg);
 
         assertArrayEquals(messages, logger.errors().toArray());
     }
 
 }
