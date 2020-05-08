 /*
  * Copyright 2012 Bright Interactive, All Rights Reserved.
  */
 
 package com.brightinteractive.common.exception;
 
 import org.junit.Test;
 
 import static org.junit.Assert.assertSame;
 
 /**
  * @author Bright Interactive
  */
 public class BrightRuntimeExceptionTest
 {
     @Test(expected=BrightRuntimeException.class)
     public void testCanBeThrownWithoutThrowsDeclaration()
     {
         throw new BrightRuntimeException();
     }
 
     @Test
     public void testNoArgConstructor()
     {
        @SuppressWarnings("ThrowableInstanceNeverThrown") Exception e = new BrightRuntimeException();
     }
 
     @Test
     public void testMessageConstructor()
     {
         String message = "Something went wrong";
 
         BrightRuntimeException e = new BrightRuntimeException(message);
 
         assertSame(message, e.getMessage());
     }
 
     @Test
     public void testCauseConstructor()
     {
         Throwable cause = new Throwable();
 
         BrightRuntimeException e = new BrightRuntimeException(cause);
 
         assertSame(cause, e.getCause());
     }
 
     @Test
     public void testMessageAndCauseConstructor()
     {
         String message = "Something threw a Throwable";
         Throwable cause = new Throwable();
 
         BrightRuntimeException e = new BrightRuntimeException(message, cause);
 
         assertSame(message, e.getMessage());
         assertSame(cause, e.getCause());
     }
 }
