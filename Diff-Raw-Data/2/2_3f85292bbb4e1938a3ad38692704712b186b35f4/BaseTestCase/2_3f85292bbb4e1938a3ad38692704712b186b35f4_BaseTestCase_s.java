 package com.khmelyuk.core;
 
 import junit.framework.TestCase;
 
 /**
  * The base test case.
  *
  * @author Ruslan Khmelyuk
  * @since 2008-8-23 2:50
  */
 public abstract class BaseTestCase extends TestCase {
 
     /**
      * Unexpected exception is thrown.
      * @param message the fail message.
      */
     protected void unexpectedException(String message) {
         fail("Unexpected exception: " + message);
     }
 
     /**
     * Unexpected situation happend.
      * @param message the fail message.
      */
     protected void unexpected(String message) {
         fail("Unexpected: " + message);
     }
 
     /**
      * Unexpected exception is thrown.
      * @param exceptionClass the unexpected exception type.
      */
     protected void unexpectedException(Class<? extends Throwable> exceptionClass) {
         fail("Unexpected exception: " + exceptionClass.getName());
     }
 }
