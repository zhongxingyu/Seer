 /*
  * $Id$
  *
  * Copyright 2003-2007 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.http;
 
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.collections.PropertyReaderUtils;
 import org.xins.common.service.CallRequest;
 
 /**
  * A request towards an HTTP service.
  *
  * <p>Since XINS 1.1.0, an HTTP method is not a mandatory property anymore. If
  * the HTTP method is not specified in a request, then it will from the
  * applicable {@link HTTPCallConfig}.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
  *
  * @since XINS 1.0.0
  *
  * @see HTTPServiceCaller
  */
 public final class HTTPCallRequest extends CallRequest {
 
    /**
     * The number of instances of this class. Initially zero.
     */
    private static int INSTANCE_COUNT;
 
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
 
    /**
     * Constructs a new <code>HTTPCallRequest</code> with the specified
     * parameters and status code verifier. Fail-over is not unconditionally
     * allowed.
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
 
       // Store information
       _parameters         = (parameters != null)
                           ? parameters
                           : PropertyReaderUtils.EMPTY_PROPERTY_READER;
       _statusCodeVerifier = statusCodeVerifier;
 
       // NOTE: _asString is lazily initialized
    }
 
    /**
     * Constructs a new <code>HTTPCallRequest</code> with the specified
     * parameters.
     * Unconditional fail-over is disabled.
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
     * Unconditional fail-over is disabled.
     *
     * @since XINS 1.1.0
     */
    public HTTPCallRequest() {
       this((PropertyReader) null, (HTTPStatusCodeVerifier) null);
    }
 
    /**
     * Constructs a new <code>HTTPCallRequest</code> with the specified HTTP
     * method. No arguments are passed to the URL.
     * Unconditional fail-over is disabled.
     *
     * @param method
     *    the HTTP method to use, or <code>null</code> if the method should be
     *    determined when the call is made.
     */
    public HTTPCallRequest(HTTPMethod method) {
       this(method, null, false, null);
    }
 
    /**
     * Constructs a new <code>HTTPCallRequest</code> with the specified HTTP
     * method and parameters.
     * Unconditional fail-over is disabled.
     *
     * @param method
     *    the HTTP method to use, or <code>null</code> if the method should be
     *    determined when the call is made.
     *
     * @param parameters
     *    the parameters for the HTTP call, can be <code>null</code>.
     */
    public HTTPCallRequest(HTTPMethod     method,
                           PropertyReader parameters) {
       this(method, parameters, false, null);
    }
 
    /**
     * Constructs a new <code>HTTPCallRequest</code> with the specified HTTP
     * method, parameters and status code verifier, optionally allowing
     * unconditional fail-over.
     *
     * @param method
     *    the HTTP method to use, or <code>null</code> if the method should be
     *    determined when the call is made.
     *
     * @param parameters
     *    the parameters for the HTTP call, can be <code>null</code>.
     *
     * @param failOverAllowed
     *    flag that indicates whether fail-over is unconditionally allowed.
     *
     * @param statusCodeVerifier
     *    the HTTP status code verifier, or <code>null</code> if all HTTP
     *    status codes are allowed.
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
 
    /**
     * Describes this request.
     *
     * @return
     *    the description of this request, never <code>null</code>.
     */
    public String describe() {
 
       // Lazily initialize the description of this call request object
       if (_asString == null) {
          StringBuffer description = new StringBuffer(193);
          description.append("HTTP");
          if (getHTTPCallConfig() != null) {
              description.append(" " + getMethod().toString());
          } else {
              description.append("(no method)");
          }
          description.append(" request ");
 
          // Parameters
          if (_parameters == null || _parameters.size() < 1) {
             description.append("; parameters=(null)");
          } else {
             description.append("; parameters=\"");
             description.append(PropertyReaderUtils.toString(_parameters, "(null)"));
             description.append('"');
          }
          _asString = description.toString();
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
     * Returns the HTTP method associated with this call request. This is
     * determined by getting the HTTP method on the associated call config, see
     * {@link #getHTTPCallConfig()}. If the associated call config is
     * <code>null</code>, then <code>null</code> is returned.
     *
     * @return
     *    the HTTP method, or <code>null</code> if none is set for the call
     *    configuration associated with this request.
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
