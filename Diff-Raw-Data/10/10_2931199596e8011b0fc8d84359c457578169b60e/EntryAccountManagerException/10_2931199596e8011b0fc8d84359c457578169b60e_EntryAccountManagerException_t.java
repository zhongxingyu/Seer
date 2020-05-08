 package org.lazydog.entry.spi.account.manager;
 
 import java.io.Serializable;
 
 
 /**
  * Account manager exception.
  *
  * @author  Ron Rickard
  */
 public class EntryAccountManagerException extends RuntimeException implements Serializable {
 
     private static final long serialVersionUID = 1L;
 
     /**
      * Constructs a new exception with no message.
      *
      */
     public EntryAccountManagerException() {
         super();
     }
 
     /**
      * Constructs a new exception with the specified message.
      *
     * @param  message  the message.
      */
     public EntryAccountManagerException(String message) {
         super(message);
     }
 
     /**
      * Constructs a new exception with the specified message and cause.
      *
     * @param  message  the message.
     * @param  cause    the cause.
      */
     public EntryAccountManagerException(String message, Throwable cause) {
         super(message, cause);
     }
 
     /**
      * Constructs a new exception with the specified cause.
      *
     * @param  cause  the cause.
      */
     public EntryAccountManagerException(Throwable cause) {
         super(cause);
     }
 }
