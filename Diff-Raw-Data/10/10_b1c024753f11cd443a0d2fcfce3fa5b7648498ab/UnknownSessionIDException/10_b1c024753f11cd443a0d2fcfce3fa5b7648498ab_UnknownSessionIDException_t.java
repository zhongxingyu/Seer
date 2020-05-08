 /*
  * $Id$
  */
 package org.xins.server;
 
 /**
 * Exception that indicates that a specified session ID is unknown.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.52
  */
 public final class UnknownSessionIDException
 extends Exception {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The one and only <code>UnknownSessionIDException</code> instance.
     */
    static final UnknownSessionIDException SINGLETON = new UnknownSessionIDException();
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>UnknownSessionIDException</code>.
     */
    private UnknownSessionIDException() {
       // empty
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 }
