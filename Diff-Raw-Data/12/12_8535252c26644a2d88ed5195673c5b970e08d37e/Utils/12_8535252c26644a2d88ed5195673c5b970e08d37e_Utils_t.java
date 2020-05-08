 /*
  * $Id$
  *
  * Copyright 2004 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common;
 
 import org.xins.common.text.TextUtils;
 
 /**
  * General utility functions.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 1.1.0
  */
 public final class Utils extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Retrieves the name of the calling class. If it cannot be determined,
     * then a special string (e.g. <code>"&lt;unknown&gt;"</code>) is returned.
     *
     * @return
     *    the class name of the caller of the caller of this method, never an
     *    empty string and never <code>null</code>.
     */
    public static final String getCallingClass() {
       Throwable exception = new Throwable();
       StackTraceElement[] trace = exception.getStackTrace();
       if (trace != null && trace.length >= 3) {
          StackTraceElement caller = trace[2];
          if (caller != null) {
             String callingClass = caller.getClassName();
             if (! TextUtils.isEmpty(callingClass)) {
                return callingClass;
             }
          }
       }
 
       // Fallback
       return "<unknown>";
    }
 
    /**
     * Retrieves the name of the calling method. If it cannot be determined,
     * then a special string (e.g. <code>"&lt;unknown&gt;"</code>) is returned.
     *
     * @return
     *    the method name of the caller of the caller of this method, never an
     *    empty string and never <code>null</code>.
     */
    public static final String getCallingMethod() {
       Throwable exception = new Throwable();
       StackTraceElement[] trace = exception.getStackTrace();
       if (trace != null && trace.length >= 3) {
          StackTraceElement caller = trace[2];
          if (caller != null) {
             String callingMethod = caller.getMethodName();
             if (! TextUtils.isEmpty(callingMethod)) {
                return callingMethod;
             }
          }
       }
 
       // Fallback
       return "<unknown>";
    }
 
    /**
     * Logs a programming error with an optional cause exception, and returns a
     * <code>ProgrammingError</code> object for it.
     *
     * @param detectingClass
     *    the name of the class that detected the problem, or
     *    <code>null</code> if unknown.
     *
     * @param detectingMethod
     *    the name of the method within the <code>detectingClass</code> that
     *    detected the problem, or <code>null</code> if unknown.
     *
     * @param subjectClass
     *    the name of the class which exposes the programming error, or
     *    <code>null</code> if unknown.
     *
    * @param subjectMethod
     *    the name of the method (within the <code>subjectClass</code>) which
     *    exposes the programming error, or <code>null</code> if unknown.
     *
     * @param detail
     *    the detail message, can be <code>null</code>.
     *
     * @param cause
     *    the cause exception, can be <code>null</code>.
     */
    public static final ProgrammingError logProgrammingError(String    detectingClass,
                                                             String    detectingMethod,
                                                             String    subjectClass,
                                                             String    subjectMethod,
                                                             String    detail,
                                                             Throwable cause) {
 
       // Log programming error (not due to exception)
       if (cause == null) {
          Log.log_1050(detectingClass, detectingMethod,
                       subjectClass,   subjectMethod,
                       detail);
 
       // Log programming error (due to exception)
       } else {
          Log.log_1052(cause,
                       detectingClass, detectingMethod,
                       subjectClass,   subjectMethod,
                       detail);
       }
 
       // Construct and return ProgrammingError object
       return new ProgrammingError(detectingClass, detectingMethod,
                                   subjectClass,   subjectMethod,
                                   detail,         cause);
 
    }
 
    /**
     * Logs a programming error with no cause exception, and returns a
     * <code>ProgrammingError</code> object for it.
     *
     * @param detectingClass
     *    the name of the class that detected the problem, or
     *    <code>null</code> if unknown.
     *
     * @param detectingMethod
     *    the name of the method within the <code>detectingClass</code> that
     *    detected the problem, or <code>null</code> if unknown.
     *
     * @param subjectClass
     *    the name of the class which exposes the programming error, or
     *    <code>null</code> if unknown.
     *
    * @param subjectMethod
     *    the name of the method (within the <code>subjectClass</code>) which
     *    exposes the programming error, or <code>null</code> if unknown.
     *
     * @param detail
     *    the detail message, can be <code>null</code>.
     */
    public static final ProgrammingError logProgrammingError(String    detectingClass,
                                                             String    detectingMethod,
                                                             String    subjectClass,
                                                             String    subjectMethod,
                                                             String    detail) {
 
       return logProgrammingError(detectingClass, detectingMethod,
                                  subjectClass,   subjectMethod,
                                  detail,         null);
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>Utils</code> object.
     */
    private Utils() {
       // empty
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 }
