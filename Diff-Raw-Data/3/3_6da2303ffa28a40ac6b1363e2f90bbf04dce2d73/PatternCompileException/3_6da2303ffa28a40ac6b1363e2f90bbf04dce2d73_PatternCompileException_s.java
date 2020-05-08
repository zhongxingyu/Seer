 /*
  * $Id$
  */
 package org.xins.types;
 
 /**
 * Patterns type. An enumeration type only accepts values that match a certain
 * pattern.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  */
 public class PatternCompileException extends RuntimeException {
 
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
     * Creates a new <code>PatternCompileException</code>.
     *
     * @param message
     *    the detail message, or <code>null</code>.
     */
    protected PatternCompileException(String message) {
       super(message);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 }
