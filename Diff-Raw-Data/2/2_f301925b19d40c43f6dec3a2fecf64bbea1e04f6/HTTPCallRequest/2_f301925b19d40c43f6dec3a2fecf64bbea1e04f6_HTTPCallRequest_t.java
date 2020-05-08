 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.http;
 
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.collections.PropertyReaderUtils;
 
 import org.xins.common.service.CallRequest;
 
 import org.xins.common.text.FastStringBuffer;
 import org.xins.common.text.TextUtils;
 
 /**
  * A request towards an HTTP service.
  *
  * <p>Since XINS 1.1.0, an HTTP method is not a mandatory property anymore. If
  * the HTTP method is not specified in a request, then it will from the
  * applicable {@link HTTPCallConfig}.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 1.0.0
  *
  * @see HTTPServiceCaller
  */
 public final class HTTPCallRequest extends CallRequest {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The number of instances of this class. Initially zero.
     */
    private static int INSTANCE_COUNT;
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>HTTPCallRequest</code> with the specified
     * parameters and status code verifier.
     *
     * @param parameters
     *    the parameters for the HTTP call, can be <code>null</code> if there
     *    are none to pass down.
     *
     * @param statusCodeVerifier
     *    the HTTP status code verifier, or <code>null</code> if all HTTP
     *    status codes are allowed.
     *
     * @since XINS 1.1.0
     */
    public HTTPCallRequest(PropertyReader         parameters,
                           HTTPStatusCodeVerifier statusCodeVerifier) {
 
       // Determine instance number first
       _instanceNumber = ++INSTANCE_COUNT;
 
       // Store information
       _parameters         = (parameters != null)
                           ? parameters
                           : PropertyReaderUtils.EMPTY_PROPERTY_READER;
       _statusCodeVerifier = statusCodeVerifier;
 
       // Note that _asString is lazily initialized.
    }
 
    /**
     * Constructs a new <code>HTTPCallRequest</code> with the specified
     * parameters.
     *
     * @param parameters
     *    the parameters for the HTTP call, can be <code>null</code> if there
     *    are none to pass down.
     *
     * @since XINS 1.1.0
     */
    public HTTPCallRequest(PropertyReader parameters) {
       this(parameters, (HTTPStatusCodeVerifier) null);
    }
 
    /**
     * Constructs a new <code>HTTPCallRequest</code> with no parameters.
     *
     * @since XINS 1.1.0
     */
    public HTTPCallRequest() {
       this((PropertyReader) null, (HTTPStatusCodeVerifier) null);
    }
 
    /**
     * Constructs a new <code>HTTPCallRequest</code> with the specified HTTP
     * method. No arguments are be passed to the URL. Fail-over is disallowed,
     * unless the request was definitely not processed by the other end.
     *
     * @param method
     *    the HTTP method to use, or <code>null</code> if the method should be
     *    determined when the call is made
     *    (<em>since XINS 1.1.0 this argument can be null</em>).
     *
     * @deprecated
     *    Deprecated since XINS 1.1.0.
     *    Use {@link #HTTPCallRequest(PropertyReader)}
     *    instead, in combination with
     *    {@link #setHTTPCallConfig(HTTPCallConfig)}.
     *    This constructor is guaranteed not to be removed before XINS 2.0.0.
     */
    public HTTPCallRequest(HTTPMethod method) {
       this(method, null, false, null);
    }
 
    /**
     * Constructs a new <code>HTTPCallRequest</code> with the specified HTTP
     * method and parameters. Fail-over is disallowed, unless the request was
     * definitely not processed by the other end.
     *
     * @param method
     *    the HTTP method to use, or <code>null</code> if the method should be
     *    determined when the call is made
     *    (<em>since XINS 1.1.0 this argument can be null</em>).
     *
     * @param parameters
     *    the parameters for the HTTP call, can be <code>null</code>.
     *
     * @deprecated
     *    Deprecated since XINS 1.1.0.
     *    Use {@link #HTTPCallRequest(PropertyReader)}
     *    instead, in combination with
     *    {@link #setHTTPCallConfig(HTTPCallConfig)}.
     *    This constructor is guaranteed not to be removed before XINS 2.0.0.
     */
    public HTTPCallRequest(HTTPMethod     method,
                           PropertyReader parameters) {
       this(method, parameters, false, null);
    }
 
    /**
     * Constructs a new <code>HTTPCallRequest</code> with the specified HTTP
     * method, parameters and status code verifier, optionally allowing
     * fail-over in all cases.
     *
     * @param method
     *    the HTTP method to use, or <code>null</code> if the method should be
     *    determined when the call is made
     *    (<em>since XINS 1.1.0 this argument can be null</em>).
     *
     * @param parameters
     *    the parameters for the HTTP call, can be <code>null</code>.
     *
     * @param failOverAllowed
     *    flag that indicates whether fail-over is in principle allowed, even
     *    if the request was already sent to the other end.
     *
     * @param statusCodeVerifier
     *    the HTTP status code verifier, or <code>null</code> if all HTTP
     *    status codes are allowed.
     *
     * @deprecated
     *    Deprecated since XINS 1.1.0.
     *    Use {@link #HTTPCallRequest(PropertyReader,HTTPStatusCodeVerifier)}
     *    instead, in combination with
     *    {@link #setHTTPCallConfig(HTTPCallConfig)}.
     *    This constructor is guaranteed not to be removed before XINS 2.0.0.
     */
    public HTTPCallRequest(HTTPMethod             method,
                           PropertyReader         parameters,
                           boolean                failOverAllowed,
                           HTTPStatusCodeVerifier statusCodeVerifier) {
 
       this(parameters, statusCodeVerifier);
 
       // Create an HTTPCallConfig object
       HTTPCallConfig callConfig = new HTTPCallConfig();
       callConfig.setFailOverAllowed(failOverAllowed);
       if (method != null) {
          callConfig.setMethod(method);
       }
       setCallConfig(callConfig);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The 1-based sequence number of this instance. Since this number is
     * 1-based, the first instance of this class will have instance number 1
     * assigned to it.
     */
    private final int _instanceNumber;
 
    /**
     * Description of this HTTP call request. This field cannot be
     * <code>null</code>, it is initialized during construction.
     */
    private String _asString;
 
    /**
     * The parameters for the HTTP call. This field cannot be
     * <code>null</code>, it is initialized during construction.
     */
    private final PropertyReader _parameters;
 
    /**
     * The HTTP status code verifier, or <code>null</code> if all HTTP status
     * codes are allowed.
     */
    private final HTTPStatusCodeVerifier _statusCodeVerifier;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Describes this request.
     *
     * @return
     *    the description of this request, never <code>null</code>.
     */
    public String describe() {
 
       // Lazily initialize the description of this call request object
       if (_asString == null) {
          FastStringBuffer buffer =
             new FastStringBuffer(193, "HTTP request #");
 
          // Request number
          buffer.append(_instanceNumber);
 
         // HTTP config
          buffer.append(" [config=");
          buffer.append(TextUtils.quote(getCallConfig()));
 
          // Parameters
          if (_parameters == null || _parameters.size() < 1) {
             buffer.append("; parameters=(null)");
          } else {
             buffer.append("; parameters=\"");
             PropertyReaderUtils.serialize(_parameters, buffer, "(null)");
             buffer.append('"');
          }
 
          _asString = buffer.toString();
       }
 
       return _asString;
    }
 
 
    /**
     * Returns the HTTP call configuration.
     *
     * @return
     *    the HTTP call configuration object, or <code>null</code>.
     *
     * @since XINS 1.1.0
     */
    public HTTPCallConfig getHTTPCallConfig() {
       return (HTTPCallConfig) getCallConfig();
    }
 
    /**
     * Sets the associated HTTP call configuration.
     *
     * @param callConfig
     *    the HTTP call configuration object to associate with this request, or
     *    <code>null</code>.
     *
     * @since XINS 1.1.0
     */
    public void setHTTPCallConfig(HTTPCallConfig callConfig) {
       setCallConfig(callConfig);
    }
 
    /**
     * Returns the HTTP method associated with this call request.
     *
     * <p><em>Since XINS 1.1.0, this method may return <code>null</code>.</em>
     *
     * @return
     *    the HTTP method, or <code>null</code>.
     *
     * @deprecated
     *    Deprecated since XINS 1.1.0.
     *    Use {@link #getHTTPCallConfig()} instead.
     *    This method is guaranteed not to be removed before XINS 2.0.0.
     */
    public HTTPMethod getMethod() {
       HTTPCallConfig callConfig = getHTTPCallConfig();
       if (callConfig == null) {
          return null;
       } else {
          return callConfig.getMethod();
       }
    }
 
    /**
     * Returns the parameters associated with this call request.
     *
     * <p>Since XINS 1.1.0, this method will never return <code>null</code>.
     *
     * @return
     *    the parameters, never <code>null</code>.
     */
    public PropertyReader getParameters() {
       return _parameters;
    }
 
    /**
     * Determines whether fail-over is in principle allowed, even if the
     * request was already sent to the other end.
     *
     * @return
     *    <code>true</code> if fail-over is in principle allowed, even if the
     *    request was already sent to the other end, <code>false</code>
     *    otherwise.
     */
    public boolean isFailOverAllowed() {
       return getHTTPCallConfig().isFailOverAllowed();
    }
 
    /**
     * Returns the HTTP status code verifier. If all HTTP status codes are
     * allowed, then <code>null</code> is returned.
     *
     * @return
     *    the HTTP status code verifier, or <code>null</code>.
     */
    public HTTPStatusCodeVerifier getStatusCodeVerifier() {
       return _statusCodeVerifier;
    }
 }
