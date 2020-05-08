 /*
  * $Id$
  *
  * Copyright 2004 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.http;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 
 import org.xins.common.Log;
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.ProgrammingError;
 
 import org.xins.common.service.CallExceptionList;
 import org.xins.common.service.CallResult;
 import org.xins.common.service.TargetDescriptor;
 
 /**
  * Result returned from an HTTP request.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 1.0.0
  */
 public final class HTTPCallResult extends CallResult implements HTTPCallResultData {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The fully-qualified name of this class.
     */
    private static final String CLASSNAME = HTTPCallResult.class.getName();
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>HTTPCallResult</code> object.
     *
     * @param request
     *    the call request that resulted in this result, cannot be
     *    <code>null</code>.
     *
     * @param succeededTarget
     *    the target for which the call succeeded, cannot be <code>null</code>.
     *
     * @param duration
     *    the call duration in milliseconds, must be a non-negative number.
     *
     * @param exceptions
     *    the list of {@link CallExceptionList}s, or <code>null</code> if the first
     *    call attempt succeeded.
     *
     * @param data
     *    the {@link HTTPCallResultData} object returned from the call, cannot
     *    be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>request         ==   null
     *          || succeededTarget ==   null
     *          || data            ==   null
     *          || duration        &lt; 0L</code>.
     */
    HTTPCallResult(HTTPCallRequest    request,
                   TargetDescriptor   succeededTarget,
                   long               duration,
                   CallExceptionList  exceptions,
                   HTTPCallResultData data)
    throws IllegalArgumentException {
 
       super(request, succeededTarget, duration, exceptions);
 
       // Check preconditions
       MandatoryArgumentChecker.check("request",         request,
                                      "succeededTarget", succeededTarget,
                                      "data",            data);
       // TODO: Check all arguments at once, before calling superconstructor
 
       _data = data;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The <code>HTTPCallResultData</code> object that contains the information
     * returned from the call. This field cannot be <code>null</code>.
     */
    private final HTTPCallResultData _data;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Returns the HTTP status code.
     *
     * @return
     *    the HTTP status code.
     */
    public int getStatusCode() {
       return _data.getStatusCode();
    }
 
    /**
     * Returns the result data as a byte array. Note that this is not a copy or
     * clone of the internal data structure, but it is a link to the actual
     * data structure itself.
     *
     * @return
     *    a byte array of the result data, never <code>null</code>.
     */
    public byte[] getData() {
       return _data.getData();
    }
 
    /**
     * Returns the returned data as a <code>String</code>. The encoding
     * <code>US-ASCII</code> is assumed.
     *
     * @return
     *    the result data as a text string, not <code>null</code>.
     */
    public String getString() {
       final String ENCODING = "US-ASCII";
       try {
          return getString(ENCODING);
       } catch (UnsupportedEncodingException exception) {
          String message = "Default encoding \"" + ENCODING + "\" is unsupported.";
          Log.log_1050(CLASSNAME, "getString()", message);
         throw new ProgrammingError(message, uue);
       }
    }
 
    /**
     * Returns the returned data as a <code>String</code> in the specified
     * encoding.
     *
     * @param encoding
     *    the encoding to use in the conversion from bytes to a text string,
     *    not <code>null</code>.
     *
     * @return
     *    the result data as a text string, not <code>null</code>.
     *
     * @throws UnsupportedEncodingException
     *    if the specified encoding is not supported.
     */
    public String getString(String encoding)
    throws UnsupportedEncodingException {
       byte[] bytes = getData();
       return new String(bytes, encoding);
    }
 
    /**
     * Returns the returned data as an <code>InputStream</code>. The input
     * stream is based directly on the underlying byte array.
     *
     * @return
     *    an {@link InputStream} that returns the returned data, never
     *    <code>null</code>.
     */
    public InputStream getStream() {
       return new ByteArrayInputStream(getData());
    }
 }
