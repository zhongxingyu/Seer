 package org.jrask.compiler;
 
 import static org.junit.Assert.*;
 
import java.util.List;

 import org.junit.Test;
 
 public class DefaultErrorLoggerTest {
 
     ErrorLogger logger = new DefaultErrorLogger();
 
    private void assertNoErrors() {
        assertTrue(logger.errors().isEmpty());
    }

     @Test
     public void testEmptyLogger() {
        assertNoErrors();
     }
 
     @Test
     public void testLogging() {
         String[] messages = { "error message", "another error message", "yet another one" };
         for (String msg : messages)
             logger.log(msg);
 
         assertArrayEquals(messages, logger.errors().toArray());
     }
 
    @Test
    public void testCopyErrorList() {
        List<String> list = logger.errors();
        list.add("xxx");
        assertNoErrors();
    }

 }
