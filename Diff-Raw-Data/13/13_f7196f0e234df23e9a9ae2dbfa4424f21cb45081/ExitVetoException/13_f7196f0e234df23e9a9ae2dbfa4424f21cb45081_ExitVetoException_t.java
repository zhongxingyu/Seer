package org.wings.event;
 
 /**
  * ExitVetoException.java
  *
  *
  * Created: Fri Dec  6 11:16:35 2002
  *
  * @author <a href="mailto:armin.haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public class ExitVetoException extends Exception  {
     
     /**
      * 
      */
     public ExitVetoException(String message) {
         super(message);
     }
 
     /**
      * 
      */
     public ExitVetoException(String message, Throwable cause) {
         super(message, cause);
     }
     
 }// ExitVetoException
 
 /*
    $Log$
   Revision 1.2  2003/01/04 13:07:08  arminhaaf
   o fix packages

    Revision 1.1  2002/12/10 17:15:22  arminhaaf
    o support for exit listener
 
 */
