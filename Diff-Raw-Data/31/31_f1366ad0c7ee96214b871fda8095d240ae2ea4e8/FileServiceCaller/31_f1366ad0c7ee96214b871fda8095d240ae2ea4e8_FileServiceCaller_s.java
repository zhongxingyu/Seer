 /*
  * $Id$
  *
  * Copyright 2003-2006 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.file;
 
 
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import javax.servlet.ServletException;
 
 import org.xins.common.Log;
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.Utils;
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.collections.PropertyReaderUtils;
 import org.xins.common.http.HTTPCallConfig;
 import org.xins.common.http.HTTPCallException;
 import org.xins.common.http.HTTPCallRequest;
 import org.xins.common.http.HTTPCallResult;
 import org.xins.common.http.HTTPCallResultData;
 import org.xins.common.http.HTTPStatusCodeVerifier;
 import org.xins.common.http.StatusCodeHTTPCallException;
 import org.xins.common.service.CallConfig;
 import org.xins.common.service.CallException;
 import org.xins.common.service.CallExceptionList;
 import org.xins.common.service.CallRequest;
 import org.xins.common.service.CallResult;
 import org.xins.common.service.Descriptor;
 import org.xins.common.service.GenericCallException;
 import org.xins.common.service.IOCallException;
 import org.xins.common.service.ServiceCaller;
 import org.xins.common.service.TargetDescriptor;
 import org.xins.common.service.UnsupportedProtocolException;
 import org.xins.common.servlet.container.LocalServletHandler;
 import org.xins.common.servlet.container.XINSServletResponse;
 import org.xins.common.text.FastStringBuffer;
 import org.xins.common.text.URLEncoding;
 import org.xins.logdoc.LogdocSerializable;
 
 /**
  *
  *
  * @version $Revision$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  */
 public class FileServiceCaller extends ServiceCaller {
    
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
    
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The pool of the loaded XINS APIs. The key is the location of the WAR
    * file, as a {@TargetDescriptor}, the value is the {@link LocalServletHandler}.
     */
    private static HashMap SERVLETS = new HashMap();
 
 
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
    
    /**
     * Constructs a new <code>HTTPServiceCaller</code> object with the
     * specified descriptor and call configuration.
     *
     * @param descriptor
     *    the descriptor of the service, cannot be <code>null</code>.
     *
     * @param callConfig
     *    the call configuration, or <code>null</code> if a default one should
     *    be used.
     *
     * @throws IllegalArgumentException
     *    if <code>descriptor == null</code>.
     *
     * @throws UnsupportedProtocolException
     *    if <code>descriptor</code> is or contains a {@link TargetDescriptor}
     *    with an unsupported protocol.
     */
    public FileServiceCaller(Descriptor     descriptor,
                             HTTPCallConfig callConfig)
    throws IllegalArgumentException, UnsupportedProtocolException {
 
       // Call superclass constructor
       super(descriptor, callConfig);
    }
 
    /**
     * Constructs a new <code>FileServiceCaller</code> object with the
     * specified descriptor and call configuration.
     *
     * @param descriptor
     *    the descriptor of the service, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>descriptor == null</code>.
     *
     * @throws UnsupportedProtocolException
     *    if <code>descriptor</code> is or contains a {@link TargetDescriptor}
     *    with an unsupported protocol.
     */
    public FileServiceCaller(Descriptor descriptor) {
       this(descriptor, (HTTPCallConfig) null);
    }
    
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
    
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Returns a default <code>CallConfig</code> object. This method is called
     * by the <code>ServiceCaller</code> constructor if no
     * <code>CallConfig</code> object was given.
     *
    * <p>The implementation of this method in class {@link HTTPServiceCaller}
    * returns a standard {@link HTTPCallConfig} object which has unconditional
    * fail-over disabled and the HTTP method set to
    * {@link HTTPMethod#POST POST}.
     *
     * @return
     *    a new {@link HTTPCallConfig} instance, never <code>null</code>.
     */
    protected CallConfig getDefaultCallConfig() {
       return new HTTPCallConfig();
    }
 
    protected CallResult createCallResult(CallRequest request, TargetDescriptor succeededTarget, 
          long duration, CallExceptionList exceptions, Object result) throws ClassCastException {
 
       return new HTTPCallResult((HTTPCallRequest) request,
                                 succeededTarget,
                                 duration,
                                 exceptions,
                                 (HTTPCallResultData) result);
    }
 
    protected boolean isProtocolSupportedImpl(String protocol) {
       return "file".equalsIgnoreCase(protocol);
    }
    
    /**
     * Executes a request towards the specified target. If the call succeeds,
     * then a {@link HTTPCallResult} object is returned, otherwise a
     * {@link CallException} is thrown.
     *
     * <p>The implementation of this method in class
     * <code>HTTPServiceCaller</code> delegates to
     * {@link #call(HTTPCallRequest,TargetDescriptor)}.
     *
     * @param request
     *    the call request to be executed, must be an instance of class
     *    {@link HTTPCallRequest}, cannot be <code>null</code>.
     *
     * @param callConfig
     *    the call configuration, never <code>null</code> and should always be
     *    an instance of class {@link HTTPCallConfig}.
     *
     * @param target
     *    the target to call, cannot be <code>null</code>.
     *
     * @return
     *    the result, if and only if the call succeeded, always an instance of
     *    class {@link HTTPCallResult}, never <code>null</code>.
     *
     * @throws ClassCastException
     *    if the specified <code>request</code> object is not <code>null</code>
     *    and not an instance of class {@link HTTPCallRequest}.
     *
     * @throws IllegalArgumentException
     *    if <code>target == null || request == null</code>.
     *
     * @throws CallException
     *    if the call to the specified target failed.
     */
    public Object doCallImpl(CallRequest      request,
                                CallConfig       callConfig,
                                TargetDescriptor target)
    throws ClassCastException, IllegalArgumentException, CallException {
 
       long start = System.currentTimeMillis();
       long duration;
       LocalServletHandler servletHandler = (LocalServletHandler) SERVLETS.get(target);
       if (servletHandler == null) {
          String fileLocation = target.getURL();
          File warFile = new File(fileLocation.substring(7).replace('/', File.separatorChar));
          try {
             servletHandler = new LocalServletHandler(warFile);
             SERVLETS.put(target, servletHandler);
          } catch (ServletException sex) {
             // TODO Log
             sex.printStackTrace();
          }
       }
       
       PropertyReader parameters = ((HTTPCallRequest) request).getParameters();
       
       // Get the parameters for logging
       LogdocSerializable params = PropertyReaderUtils.serialize(parameters, "", "?", null);
 
       // Get URL value
       String url = target.getURL();
 
       // Loop through the parameters
       FastStringBuffer query = new FastStringBuffer(255);
       Iterator keys = parameters.getNames();
       while (keys.hasNext()) {
 
          // Get the parameter key
          String key = (String) keys.next();
 
          // Get the value
          String value = parameters.get(key);
          if (value == null) {
             value = "";
          }
 
          // Add this parameter key/value combination.
          if (key != null) {
 
             if (query.getLength() > 0) {
                query.append("&");
             }
             query.append(URLEncoding.encode(key));
             query.append("=");
             query.append(URLEncoding.encode(value));
          }
       }
       
       XINSServletResponse response = null;
       try {
          response = servletHandler.query(query.toString());
       } catch (IOException exception) {
          duration = System.currentTimeMillis() - start;
          Log.log_1109(exception, url, params, duration);
          throw new IOCallException(request, target, duration, exception);
          
       }
 
       // Retrieve the data returned from the call
       HTTPCallResultData data = null;
       try {
          String result = response.getResult();
          byte[] resultData = null;
          if (result != null) {
             resultData = result.getBytes(response.getCharacterEncoding());
          }
          data = new HTTPCallResultDataHandler(response.getStatus(), resultData);
       } catch (UnsupportedEncodingException ueex) {
          throw Utils.logProgrammingError(ueex);
       }
 
       // Determine the HTTP status code
       int code = data.getStatusCode();
 
       duration = System.currentTimeMillis() - start;
 
       HTTPStatusCodeVerifier verifier = ((HTTPCallRequest)request).getStatusCodeVerifier();
 
       // Status code is considered acceptable
       if (verifier == null || verifier.isAcceptable(code)) {
          Log.log_1107(url, params, duration, code);
 
       // Status code is considered unacceptable
       } else {
          Log.log_1108(url, params, duration, code);
 
          throw new StatusCodeHTTPCallException((HTTPCallRequest) request, target, duration, code);
       }
 
       return new HTTPCallResult((HTTPCallRequest) request, target, duration, null, data);
    }
 
    /**
     * Performs the specified request towards the HTTP service. If the call
     * succeeds with one of the targets, then a {@link HTTPCallResult} object
     * is returned, that combines the HTTP status code and the data returned.
     * Otherwise, if none of the targets could successfully be called, a
     * {@link CallException} is thrown.
     *
     * @param request
     *    the call request, not <code>null</code>.
     *
     * @param callConfig
     *    the call configuration to use, or <code>null</code>.
     *
     * @return
     *    the result of the call, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>request == null</code>.
     *
     * @throws GenericCallException
     *    if the first call attempt failed due to a generic reason and all the
     *    other call attempts failed as well.
     *
     * @throws HTTPCallException
     *    if the first call attempt failed due to an HTTP-related reason and
     *    all the other call attempts failed as well.
     */
    public HTTPCallResult call(HTTPCallRequest request,
                               HTTPCallConfig  callConfig)
    throws IllegalArgumentException,
           GenericCallException,
           HTTPCallException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("request", request);
 
       // Perform the call
       CallResult callResult;
       try {
          callResult = doCall(request, callConfig);
 
       // Allow GenericCallException, HTTPCallException and Error to proceed,
       // but block other kinds of exceptions and throw an Error instead.
       } catch (GenericCallException exception) {
          throw exception;
       } catch (HTTPCallException exception) {
          throw exception;
       } catch (Exception exception) {
          throw Utils.logProgrammingError(exception);
       }
 
       return (HTTPCallResult) callResult;
    }
 
    /**
     * Performs the specified request towards the HTTP service. If the call
     * succeeds with one of the targets, then a {@link HTTPCallResult} object
     * is returned, that combines the HTTP status code and the data returned.
     * Otherwise, if none of the targets could successfully be called, a
     * {@link CallException} is thrown.
     *
     * @param request
     *    the call request, not <code>null</code>.
     *
     * @return
     *    the result of the call, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>request == null</code>.
     *
     * @throws GenericCallException
     *    if the first call attempt failed due to a generic reason and all the
     *    other call attempts failed as well.
     *
     * @throws HTTPCallException
     *    if the first call attempt failed due to an HTTP-related reason and
     *    all the other call attempts failed as well.
     */
    public HTTPCallResult call(HTTPCallRequest request)
    throws IllegalArgumentException,
           GenericCallException,
           HTTPCallException {
       return call(request, (HTTPCallConfig) null);
    }
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * Container of the data part of an HTTP call result.
     *
     * @version $Revision$ $Date$
     * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
     *
     * @since XINS 1.5.0
     */
    private final static class HTTPCallResultDataHandler
    implements HTTPCallResultData {
 
       //-------------------------------------------------------------------------
       // Constructor
       //-------------------------------------------------------------------------
 
       /**
        * Constructs a new <code>HTTPCallResultDataHandler</code> object.
        *
        * @param code
        *    the HTTP status code.
        *
        * @param data
        *    the data returned from the call, as a set of bytes.
        */
       HTTPCallResultDataHandler(int code, byte[] data) {
          _code = code;
          _data = data;
       }
 
 
       //-------------------------------------------------------------------------
       // Fields
       //-------------------------------------------------------------------------
 
       /**
        * The HTTP status code.
        */
       private final int _code;
 
       /**
        * The data returned.
        */
       private final byte[] _data;
 
 
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
          return _code;
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
          return _data;
       }
    }
 }
