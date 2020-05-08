 /*
  * $Id$
  *
  * Copyright 2003-2006 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server;
 
import org.xins.logdoc.ExceptionUtils;

 /**
  * Exception that indicates that an incoming request is considered invalid.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  */
 public final class InvalidRequestException
 extends Exception {
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>InvalidRequestException</code> with the specified
     * detail message and cause exception.
     *
     * @param message
     *    the message, can be <code>null</code>.
     *
     * @param cause
     *    the cause exception, can be <code>null</code>.
     */
    public InvalidRequestException(String message, Throwable cause) {
      super(message);
      if (cause != null) {
         ExceptionUtils.setCause(this, cause);
      }
    }
 
    /**
     * Constructs a new <code>InvalidRequestException</code> with the specified
     * detail message.
     *
     * @param message
     *    the message, can be <code>null</code>.
     */
    public InvalidRequestException(String message) {
       this(message, null);
    }
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 }
