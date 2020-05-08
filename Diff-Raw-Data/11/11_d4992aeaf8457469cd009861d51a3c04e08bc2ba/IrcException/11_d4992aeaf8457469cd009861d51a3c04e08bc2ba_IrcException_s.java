 package org.jibble.pircbot;
 
 /**
  * An IrcException class.
  *
  * @author PircBot-PPF project
  * @version 1.0.0
  */
 public class IrcException extends Exception {
     
     /**
      * Constructs a new IrcException.
      *
      * @param e The error message to report.
      */
     public IrcException(String e) {
         super(e);
     }
     
 }
