 package com.centeractive.ws.client;
 
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 
 /**
  * @author Tom Bujok
  * @since 1.0.0
  */
 public class TransmissionExceptionTest {
 
     @Test
     public void messageFormatEmptyTest() {
         TransmissionException ex = new TransmissionException("", 0, new RuntimeException());
         assertEquals("", ex.getMessage());
     }
 
     @Test
     public void messageFormatCodeTest() {
         TransmissionException ex = new TransmissionException("", 404, new RuntimeException());
         assertEquals("HTTP code = [404]; ", ex.getMessage());
     }
 
     @Test
     public void messageFormatMessageTest() {
         TransmissionException ex = new TransmissionException("Message", 0, new RuntimeException());
         assertEquals("Message; ", ex.getMessage());
     }
 
     @Test
     public void messageFormatCauseTest() {
         TransmissionException ex = new TransmissionException("", 0, new RuntimeException("Cause"));
         assertEquals("Possible cause: Cause;", ex.getMessage());
     }
 
     @Test
     public void messageFormatFullFormatTest() {
         TransmissionException ex = new TransmissionException("SOAP communication failed", 500, new RuntimeException("Internal server error"));
         assertEquals("SOAP communication failed; HTTP code = [500]; Possible cause: Internal server error;", ex.getMessage());
     }
 
 }
