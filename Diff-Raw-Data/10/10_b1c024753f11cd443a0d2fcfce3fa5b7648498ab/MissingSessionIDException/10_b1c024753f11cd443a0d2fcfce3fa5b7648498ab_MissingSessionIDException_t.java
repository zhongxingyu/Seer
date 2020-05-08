 /*
  * $Id$
  */
 package org.xins.server;
 
 /**
 * Exception that indicates that a session ID is required for the specified
 * function, but missing in the request.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.52
  */
 public final class MissingSessionIDException
 extends Exception {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The one and only <code>MissingSessionIDException</code> instance.
     */
    static final MissingSessionIDException SINGLETON = new MissingSessionIDException();
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>MissingSessionIDException</code>.
     */
    private MissingSessionIDException() {
       // empty
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 }
