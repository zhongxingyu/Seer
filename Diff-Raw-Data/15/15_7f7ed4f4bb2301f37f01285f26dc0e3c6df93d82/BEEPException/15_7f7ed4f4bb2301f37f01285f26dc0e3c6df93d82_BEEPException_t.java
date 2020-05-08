 /*
 * BEEPException.java  $Revision: 1.4 $ $Date: 2002/05/07 04:57:51 $
  *
  * Copyright (c) 2001 Huston Franklin.  All rights reserved.
  *
  * The contents of this file are subject to the Blocks Public License (the
  * "License"); You may not use this file except in compliance with the License.
  *
  * You may obtain a copy of the License at http://www.beepcore.org/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied.  See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  */
 package org.beepcore.beep.core;
 
 
 /**
  * An exception for representing BEEP related errors.
  * <code>BEEPException</code> adds support for exception chaining
  * similar to what is available in JDK 1.4.
  *
  * @author Huston Franklin
 * @version $Revision: 1.4 $, $Date: 2002/05/07 04:57:51 $
  */
 public class BEEPException extends Exception {
     private final Throwable cause;
 
 
     /**
      * Constructs a new <code>BEEPException</code> with the specified
      * detail message.
      *
      * @param message the detailed message which is saved for later
      * retrieval by the <code>getMessage()</code> method.
      */
     public BEEPException(String message) {
         super(message);
         cause = null;
     }
 
     /**
      * Constructs a new <code>BEEPException</code> with the specified
      * cause and a detailed message of
      * <code>(cause == null ? null : cause.toString())</code>.
      *
      * @param cause the cause which is saved for later retrieval by
      * the <code>getCause()</code> method.
      */
     public BEEPException(Throwable cause) {
         super(cause == null ? null : cause.toString());
         this.cause = cause;
     }
 
     /**
      * Returns the cause of this <code>BEEPException</code>.
      */
     public Throwable getCause() {
         return cause;
     }
 
     public String getLocalizedMessage() {
         if (cause == null) {
             return super.getLocalizedMessage();
         }
         return cause.getLocalizedMessage();
     }
 
     /**
      * Prints this <code>BEEPException</code> and its backtrace to the
      * standard error stream. If this <code>BEEPException</code> was
      * initialized with a <code>Throwable</code> the backtrace for it
      * will be printed as well.
      */
     public void printStackTrace() {
         printStackTrace(System.err);
     }
 
     /**
      * Prints this <code>BEEPException</code> and its backtrace to the
      * specified print stream.
      */
     public void printStackTrace(java.io.PrintStream s) {
         synchronized (s) {
             super.printStackTrace(s);
             if (cause != null) {
                 s.print("Caused by: ");
                 cause.printStackTrace(s);
             }
         }
     }
 
     /**
      * Prints this <code>BEEPException</code> and its backtrace to the
      * specified print writer.
      */
     public void printStackTrace(java.io.PrintWriter s) {
         synchronized (s) {
             super.printStackTrace(s);
             if (cause != null) {
                 s.print("Caused by: ");
                 cause.printStackTrace(s);
             }
         }
     }
 
     public String toString() {
         if (cause == null) {
             return super.toString();
         }
         return super.toString() + cause.toString();
     }
 }
