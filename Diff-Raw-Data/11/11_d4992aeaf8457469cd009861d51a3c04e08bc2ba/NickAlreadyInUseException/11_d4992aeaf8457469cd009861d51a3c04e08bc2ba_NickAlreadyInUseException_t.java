 package org.jibble.pircbot;
 
 /**
  * A NickAlreadyInUseException class.  This exception is
  * thrown when the PircBot attempts to join an IRC server
  * with a user name that is already in use.
  *
  * @author PircBot-PPF project
  * @version 1.0.0
  */
 public class NickAlreadyInUseException extends IrcException {

    private static final long serialVersionUID = 1L;

     /**
      * Constructs a new IrcException.
      *
      * @param e The error message to report.
      */
     public NickAlreadyInUseException(String e) {
         super(e);
     }
     
 }
