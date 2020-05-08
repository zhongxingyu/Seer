 package org.freshwaterlife.fishlink;
 
 /**
  * Catchall Exception thrown by any FishLink method that needs to throw an Exception.
  * 
  * @author Christian 
  */
 public class FishLinkException extends Exception {
 
     /**
      * Constructs an instance of <code>FishLinkException</code> with the specified detail message.
      * @param  message the detail message (which is saved for later retrieval
      *         by the {@link #getMessage()} method).
      */
    public FishLinkException(String message) {
        super(message);
     }
 
    /**
      * Constructs a new exception with the specified detail message and
      * cause.  <p>Note that the detail message associated with
      * <code>cause</code> is <i>not</i> automatically incorporated in
      * this exception's detail message.
      *
      * @param  message the detail message (which is saved for later retrieval
      *         by the {@link #getMessage()} method).
      * @param  cause the cause (which is saved for later retrieval by the
      *         {@link #getCause()} method).  (A <tt>null</tt> value is
      *         permitted, and indicates that the cause is nonexistent or
      *         unknown.)
      */
    public FishLinkException(String message, Exception cause) {
        super(message, cause);
     }
 }
