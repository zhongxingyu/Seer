 /*
  * $Id$
  */
 package org.xins.common.service.http;
 
 import org.xins.common.service.TargetDescriptor;
 
 /**
  * Exception that indicates that an HTTP call failed because the returned HTTP
  * status code was considered invalid.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 0.207
  */
 public final class StatusCodeHTTPCallException
 extends HTTPCallException {
 
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
     * Constructs a new <code>StatusCodeHTTPCallException</code> based on the
     * original request, target called, call duration and HTTP status code.
     *
     * @param request
     *    the original request, cannot be <code>null</code>.
     *
     * @param target
     *    descriptor for the target that was attempted to be called, cannot be
     *    <code>null</code>.
     *
     * @param duration
     *    the call duration in milliseconds, must be &gt;= 0.
     *
     * @param code
     *    the HTTP status code.
     *
     * @throws IllegalArgumentException
     *    if <code>request == null
     *          || target == null
     *          || duration &lt; 0</code>.
     */
   StatusCodeHTTPCallException(String           shortReason,
                               HTTPCallRequest  request,
                                TargetDescriptor target,
                                long             duration,
                                int              code)
    throws IllegalArgumentException {
       super("Unsupported HTTP status code " + code, request, target, duration, null, null);
 
       _code = code;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The returned HTTP status code.
     */
    private final int _code;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Returns the HTTP status code.
     *
     * @return
     *    the HTTP status code that is considered unacceptable.
     */
    public int getCode() {
       return _code;
    }
 }
