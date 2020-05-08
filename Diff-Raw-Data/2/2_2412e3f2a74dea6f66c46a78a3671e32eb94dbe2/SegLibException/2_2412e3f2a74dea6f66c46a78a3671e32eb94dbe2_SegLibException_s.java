 package com.innovatrics.iseglib;
 
 /**
  * Exception thrown by the {@link SegLib} wrapper methods.
  * @author Martin Vysny
  */
 public class SegLibException extends RuntimeException {
 
     private static final long serialVersionUID = 1L;
     public final int errorCode;
 
     public SegLibException(final String msg, final int errorCode) {
        super(msg);
         this.errorCode = errorCode;
     }
 }
