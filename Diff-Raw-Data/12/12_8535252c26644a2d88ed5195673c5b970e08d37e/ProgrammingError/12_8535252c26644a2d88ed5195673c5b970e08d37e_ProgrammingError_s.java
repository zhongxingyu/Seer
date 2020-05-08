 /*
  * $Id$
  *
  * Copyright 2004 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common;
 
 /**
  * Indication of a programming error.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 1.0.1
  */
 public final class ProgrammingError extends Error {
 
    // TODO: Review this class
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>ProgrammingError</code> with the specified detail
     * message.
     *
     * @param detail
     *    the detail message, can be <code>null</code>.
     */
    public ProgrammingError(String detail) {
       this(null, null, null, null, detail, null);
    }
 
    /**
     * Constructs a new <code>ProgrammingError</code> with the specified detail
     * message and cause exception.
     *
     * @param detail
     *    the detail message, can be <code>null</code>.
     *
     * @param cause
     *    the cause exception, can be <code>null</code>.
     */
    public ProgrammingError(String detail, Throwable cause) {
       this(null, null, null, null, detail, cause);
    }
 
    /**
     * Constructs a new <code>ProgrammingError</code> for the specified class
     * and method, indicating which class and method detected the problem.
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
    * @param subjectClass
     *    the name of the method (within the <code>subjectClass</code>) which
     *    exposes the programming error, or <code>null</code> if unknown.
     *
     * @param detail
     *    the detail message, can be <code>null</code>.
     *
     * @param cause
     *    the cause exception, can be <code>null</code>.
     *
     * @since XINS 1.1.0
     */
    public ProgrammingError(String    detectingClass,
                            String    detectingMethod,
                            String    subjectClass,
                            String    subjectMethod,
                            String    detail,
                            Throwable cause) {
       super(detail, cause);
 
       // TODO: Include all arguments in the exception message
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 }
