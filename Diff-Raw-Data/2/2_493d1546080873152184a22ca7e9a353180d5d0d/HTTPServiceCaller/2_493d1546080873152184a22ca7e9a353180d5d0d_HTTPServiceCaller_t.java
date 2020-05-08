 /*
  * $Id$
  *
  * Copyright 2004 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.http;
 
 import java.io.IOException;
 
 import java.net.ConnectException;
 import java.net.UnknownHostException;
 
 import java.util.Iterator;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpConnection;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.HttpRecoverableException;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 
 import org.apache.log4j.NDC;
 
 import org.xins.common.Log;
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.ProgrammingError;
 import org.xins.common.TimeOutException;
 
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.collections.PropertyReaderUtils;
 
 import org.xins.common.text.URLEncoding;
 
 import org.xins.common.service.CallConfig;
 import org.xins.common.service.CallException;
 import org.xins.common.service.CallExceptionList;
 import org.xins.common.service.CallRequest;
 import org.xins.common.service.CallResult;
 import org.xins.common.service.ConnectionRefusedCallException;
 import org.xins.common.service.ConnectionTimeOutCallException;
 import org.xins.common.service.Descriptor;
 import org.xins.common.service.GenericCallException;
 import org.xins.common.service.IOCallException;
 import org.xins.common.service.ServiceCaller;
 import org.xins.common.service.SocketTimeOutCallException;
 import org.xins.common.service.TargetDescriptor;
 import org.xins.common.service.TotalTimeOutCallException;
 import org.xins.common.service.UnexpectedExceptionCallException;
 import org.xins.common.service.UnknownHostCallException;
 import org.xins.common.service.UnsupportedProtocolException;
 
 import org.xins.common.text.FastStringBuffer;
 import org.xins.common.text.TextUtils;
 import org.xins.logdoc.LogdocSerializable;
 
 /**
  * HTTP service caller. This class can be used to perform a call to an HTTP
  * server and fail-over to other HTTP servers if the first one fails.
  *
  * <h2>Supported protocols</h2>
  *
  * <p>This service caller currently only supports the HTTP protocol. If a
  * {@link TargetDescriptor} is passed to the constructor with a different
  * protocol, then an {@link UnsupportedProtocolException} is thrown. In the
  * future, HTTPS is expected to be supported as well.
  *
  * <h2>Load-balancing and fail-over</h2>
  *
  * <p>There are 2 ways to perform an HTTP call using a
  * <code>HTTPServiceCaller</code> instance:
  *
  * <ul>
  *    <li>to a single HTTP service, using
  *        {@link #call(HTTPCallRequest,TargetDescriptor)};
  *    <li>to a set of one or more HTTP services, using
  *        {@link #call(HTTPCallRequest)};
  * </ul>
  *
  * <p>With the second form of a HTTP call, fail-over and load-balancing can be
  * performed.
  *
  * <p>How load-balancing is done (in the second form) depends on the
  * {@link Descriptor} passed to the
  * {@link #HTTPServiceCaller(Descriptor)} constructor. If it is a
  * {@link TargetDescriptor}, then only this single target service is called
  * and no load-balancing is performed. If it is a
  * {@link org.xins.common.service.GroupDescriptor}, then the configuration of
  * the <code>GroupDescriptor</code> determines how the load-balancing is done.
  * A <code>GroupDescriptor</code> is a recursive data structure, which allows
  * for fairly advanced load-balancing algorithms.
  *
  * <p>If a call attempt fails and there are more available target services,
  * then the <code>HTTPServiceCaller</code> may or may not fail-over to a next
  * target. If the request was not accepted by the target service, then
  * fail-over is considered acceptable and will be performed. This includes
  * the following situations:
  *
  * <ul>
  *    <li>if the <em>failOverAllowed</em> property is set to <code>true</code>
  *        for the active {@link HTTPCallConfig};
  *    <li>if the connection cannot be established (e.g. due to connection
  *        refusal, a DNS error, connection time-out, etc.)
  *    <li>if an HTTP status code other than 200-299 is returned;
  * </ul>
  *
  * <p>If none of these conditions holds, then fail-over is not considered
  * acceptable and will not be performed.
  *
  *
  * <h2>Example code</h2>
  *
  * <p>The following example code snippet constructs an
  * <code>HTTPServiceCaller</code> instance:
  *
  * <blockquote><pre>// Initialize properties for the services. Normally these
 // properties would come from a configuration source, like a file.
 {@link org.xins.common.collections.BasicPropertyReader} properties = new {@link org.xins.common.collections.BasicPropertyReader#BasicPropertyReader() BasicPropertyReader}();
 properties.{@link org.xins.common.collections.BasicPropertyReader#set(String,String) set}("myapi",         "group, random, server1, server2");
 properties.{@link org.xins.common.collections.BasicPropertyReader#set(String,String) set}("myapi.server1", "service, http://server1/myapi, 10000");
 properties.{@link org.xins.common.collections.BasicPropertyReader#set(String,String) set}("myapi.server2", "service, http://server2/myapi, 12000");
 
 // Construct a descriptor and an HTTPServiceCaller instance
 {@link Descriptor Descriptor} descriptor = {@link org.xins.common.service.DescriptorBuilder DescriptorBuilder}.{@link org.xins.common.service.DescriptorBuilder#build(PropertyReader,String) build}(properties, "myapi");
 HTTPServiceCaller caller = new {@link #HTTPServiceCaller(Descriptor) HTTPServiceCaller}(descriptor);</pre></blockquote>
  *
  * <p>Then the following code snippet uses this <code>HTTPServiceCaller</code>
  * to perform an HTTP GET call:
  *
  * <blockquote><pre>{@link org.xins.common.collections.BasicPropertyReader} params = new {@link org.xins.common.collections.BasicPropertyReader BasicPropertyReader}();
 params.{@link org.xins.common.collections.BasicPropertyReader#set(String,String) set}("street",      "Broadband Avenue");
 params.{@link org.xins.common.collections.BasicPropertyReader#set(String,String) set}("houseNumber", "12");
 
 {@link HTTPCallRequest} request = new {@link HTTPCallRequest#HTTPCallRequest(HTTPMethod,PropertyReader) HTTPCallRequest}({@link HTTPMethod}.{@link HTTPMethod#GET GET}, params);
 {@link HTTPCallResult} result = caller.{@link #call(HTTPCallRequest) call}(request);</pre></blockquote>
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 1.0.0
  */
 public final class HTTPServiceCaller extends ServiceCaller {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * Fully-qualified name of this class.
     */
    private static final String CLASSNAME = HTTPServiceCaller.class.getName();
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Logs the fact that the constructor was entered. The descriptor passed
     * to the constructor is both the input and the output for this class
     * function.
     *
     * @param descriptor
     *    the descriptor, could be <code>null</code>.
     *
     * @return
     *    <code>descriptor</code>.
     */
    private static final Descriptor trace(Descriptor descriptor) {
 
       // TRACE: Enter constructor
       Log.log_1000(CLASSNAME, null);
 
       return descriptor;
    }
 
    /**
     * Creates an appropriate <code>HttpMethod</code> object for the specified
     * URL.
     *
     * @param url
     *    the URL for which to create an {@link HttpMethod} object, should not
     *    be <code>null</code>.
     *
     * @param request
     *    the HTTP call request, not <code>null</code>.
     *
     * @param request
     *    the HTTP call configuration object, not <code>null</code>.
     *
     * @return
     *    the constructed {@link HttpMethod} object, not <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>url == null || request == null</code>.
     */
    private static HttpMethod createMethod(String          url,
                                           HTTPCallRequest request,
                                           HTTPCallConfig  callConfig)
    throws IllegalArgumentException {
 
       final String THIS_METHOD = "createMethod(String,HTTPCallConfig,HTTPCallRequest)";
 
       // TRACE: Enter method
       Log.log_1003(CLASSNAME, THIS_METHOD, null);
 
       // Check preconditions
       MandatoryArgumentChecker.check("url", url, "request", request);
 
       // Get the HTTP method (like GET and POST) and parameters
       HTTPMethod     method     = callConfig.getMethod();
       PropertyReader parameters = request.getParameters();
 
       // HTTP POST request
       if (method == HTTPMethod.POST) {
          PostMethod postMethod = new PostMethod(url);
 
          // Loop through the parameters
          if (parameters != null) {
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
                   postMethod.addParameter(key, value);
                }
             }
          }
 
          // TRACE: Leave method
          Log.log_1005(CLASSNAME, THIS_METHOD, null);
 
          return postMethod;
 
       // HTTP GET request
       } else if (method == HTTPMethod.GET) {
          GetMethod getMethod = new GetMethod(url);
 
          // Loop through the parameters
          if (parameters != null) {
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
             if (query.getLength() > 0) {
                getMethod.setQueryString(query.toString());
             }
          }
 
          // TRACE: Leave method
          Log.log_1005(CLASSNAME, THIS_METHOD, null);
 
          return getMethod;
 
       // Unrecognized HTTP method (only GET and POST are supported)
       } else {
          String message = "Unrecognized HTTP method \"" + method + "\".";
          Log.log_1050(CLASSNAME, THIS_METHOD, message);
          throw new ProgrammingError(message);
       }
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
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
     *
     * @since XINS 1.1.0
     */
    public HTTPServiceCaller(Descriptor     descriptor,
                             HTTPCallConfig callConfig)
    throws IllegalArgumentException, UnsupportedProtocolException {
 
       // Trace first and then call superclass constructor
       super(trace(descriptor), callConfig);
 
       // Check that all targets have a supported protocol
       Iterator iterator = descriptor.iterateTargets();
       while (iterator.hasNext()) {
          TargetDescriptor target = (TargetDescriptor) iterator.next();
          String url = target.getURL().toLowerCase();
          if (! url.startsWith("http://")) {
             throw new UnsupportedProtocolException(target);
          }
       }
 
       // TRACE: Leave constructor
       Log.log_1002(CLASSNAME, null);
    }
 
 
    /**
     * Constructs a new <code>HTTPServiceCaller</code> object.
     *
     * @param descriptor
     *    the descriptor of the service, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>descriptor == null</code>.
     *
     * @throws UnsupportedProtocolException
     *    if <code>descriptor</code> is or contains a {@link TargetDescriptor}
    *    with an unsupported protocol (<em>since XINS 1.1.0</em>).
     */
    public HTTPServiceCaller(Descriptor descriptor)
    throws IllegalArgumentException, UnsupportedProtocolException {
       this(descriptor, null);
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
     *
     * @since XINS 1.1.0
     */
    protected Object doCallImpl(CallRequest      request,
                                CallConfig       callConfig,
                                TargetDescriptor target)
    throws ClassCastException, IllegalArgumentException, CallException {
 
       final String THIS_METHOD = "doCallImpl(CallRequest,CallConfig,TargetDescriptor)";
 
       // TRACE: Enter method
       Log.log_1003(CLASSNAME, THIS_METHOD, null);
 
       // Delegate to method with more specialized interface
       Object ret = call((HTTPCallRequest) request,
                         (HTTPCallConfig)  callConfig,
                         target);
 
       // TRACE: Leave method
       Log.log_1005(CLASSNAME, THIS_METHOD, null);
 
       return ret;
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
     *
     * @since XINS 1.1.0
     */
    public HTTPCallResult call(HTTPCallRequest request,
                               HTTPCallConfig  callConfig)
    throws IllegalArgumentException,
           GenericCallException,
           HTTPCallException {
 
       final String THIS_METHOD = "call(HTTPCallRequest,HTTPCallConfig)";
 
       // TRACE: Enter method
       Log.log_1003(CLASSNAME, THIS_METHOD, null);
 
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
          final String METHOD = "doCall(CallRequest,CallConfig)";
          FastStringBuffer message = new FastStringBuffer(190, CLASSNAME);
          message.append('.');
          message.append(METHOD);
          message.append(" threw ");
          message.append(exception.getClass().getName());
          message.append(". Message: ");
          message.append(TextUtils.quote(exception.getMessage()));
          message.append('.');
          Log.log_1052(exception, CLASSNAME, METHOD);
          throw new ProgrammingError(message.toString(), exception);
       }
 
       // TRACE: Leave method
       Log.log_1005(CLASSNAME, THIS_METHOD, null);
 
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
 
    /**
     * Executes the specified HTTP call request on the specified target with
     * the specified call configuration. If the call fails in any way, then a
     * {@link CallException} is thrown.
     *
     * @param request
     *    the call request to execute, cannot be <code>null</code>.
     *
     * @param callConfig
     *    the (optional) call configuration, or <code>null</code> if it should
     *    be determined at a lower level.
     *
     * @param target
     *    the service target on which to execute the request, cannot be
     *    <code>null</code>.
     *
     * @return
     *    the call result, never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>target == null || request == null</code>.
     *
     * @throws GenericCallException
     *    if the first call attempt failed due to a generic reason and all the
     *    other call attempts failed as well.
     *
     * @throws HTTPCallException
     *    if the first call attempt failed due to an HTTP-related reason and
     *    all the other call attempts failed as well.
     *
     * @since XINS 1.1.0
     *
     * @deprecated
     *    Deprecated since XINS 1.1.0, since this method is expected to be
     *    removed. Please do not use it directly.
     */
    public HTTPCallResult call(HTTPCallRequest  request,
                               HTTPCallConfig   callConfig,
                               TargetDescriptor target)
    throws IllegalArgumentException,
           GenericCallException,
           HTTPCallException {
 
       // TRACE: Enter method
       Log.log_1003(CLASSNAME, "call(HTTPCallRequest,TargetDescriptor)", null);
 
       // Get the parameters for logging
       PropertyReader     p      = request.getParameters();
       LogdocSerializable params = PropertyReaderUtils.serialize(p, "-");
 
       // Prepare a thread for execution of the call
       // NOTE: Preconditions are checked by the CallExecutor constructor
       CallExecutor executor = new CallExecutor(request, callConfig, target, NDC.peek());
 
       // Get URL and time-out values
       String url               = target.getURL();
       int    totalTimeOut      = target.getTotalTimeOut();
       int    connectionTimeOut = target.getConnectionTimeOut();
       int    socketTimeOut     = target.getSocketTimeOut();
 
       // About to make an HTTP call
       Log.log_1100(url, params, totalTimeOut, connectionTimeOut, socketTimeOut);
 
       // Perform the HTTP call
       long start = System.currentTimeMillis();
       long duration;
       try {
          controlTimeOut(executor, target);
 
       // Total time-out exceeded
       } catch (TimeOutException exception) {
          duration = System.currentTimeMillis() - start;
          Log.log_1106(url, params, duration, totalTimeOut);
          throw new TotalTimeOutCallException(request, target, duration);
 
       } finally {
 
          // Determine the call duration
          duration = System.currentTimeMillis() - start;
       }
 
       // Log the HTTP call done.
       Log.log_1101(url, params, duration);
 
       // Check for exceptions
       Throwable exception = executor.getException();
       if (exception != null) {
 
          // Unknown host
          if (exception instanceof UnknownHostException) {
             Log.log_1102(url, params, duration);
             throw new UnknownHostCallException(request, target, duration);
 
          // Connection refusal
          } else if (exception instanceof ConnectException) {
             Log.log_1103(url, params, duration);
             throw new ConnectionRefusedCallException(request, target, duration);
 
          // Connection time-out
          } else if (exception instanceof HttpConnection.ConnectionTimeoutException) {
             Log.log_1104(url, params, duration, connectionTimeOut);
             throw new ConnectionTimeOutCallException(request, target, duration);
 
          // Socket time-out
          } else if (exception instanceof HttpRecoverableException) {
 
             // XXX: This is an ugly way to detect a socket time-out, but there
             //      does not seem to be a better way in HttpClient 2.0. This
             //      will, however, be fixed in HttpClient 3.0. See:
             //      http://issues.apache.org/bugzilla/show_bug.cgi?id=19868
 
             String exMessage = exception.getMessage();
             if (exMessage != null && exMessage.startsWith("java.net.SocketTimeoutException")) {
                Log.log_1105(url, params, duration, socketTimeOut);
                throw new SocketTimeOutCallException(request, target, duration);
 
             // Unspecific I/O error
             } else {
                Log.log_1109(exception, url, params, duration);
                throw new IOCallException(request, target, duration, (IOException) exception);
             }
 
          // Unspecific I/O error
          } else if (exception instanceof IOException) {
             Log.log_1109(exception, url, params, duration);
             throw new IOCallException(request, target, duration, (IOException) exception);
 
          // Unrecognized kind of exception caught
          } else {
             Log.log_1052(exception, executor.getThrowingClass(), executor.getThrowingMethod());
             throw new UnexpectedExceptionCallException(request, target, duration, null, exception);
          }
       }
 
       // Retrieve the data returned from the HTTP call
       HTTPCallResultData data = executor.getData();
 
       // Determine the HTTP status code
       int code = data.getStatusCode();
 
       HTTPStatusCodeVerifier verifier = request.getStatusCodeVerifier();
 
       // Status code is considered acceptable
       if (verifier == null || verifier.isAcceptable(code)) {
          Log.log_1107(url, params, duration, code);
 
       // Status code is considered unacceptable
       } else {
          Log.log_1108(url, params, duration, code);
 
          // TODO: Pass down body as well. Perhaps just pass down complete
          //       HTTPCallResult object and add getter for the body to the
          //       StatusCodeHTTPCallException class.
          throw new StatusCodeHTTPCallException(request, target, duration, code);
       }
 
       // TRACE: Leave method
       Log.log_1005(CLASSNAME, "call(HTTPCallRequest,TargetDescriptor)", null);
 
       return new HTTPCallResult(request, target, duration, null, data);
    }
 
    /**
     * Executes the specified HTTP call request on the specified target. If the
     * call fails in any way, then a {@link CallException} is thrown.
     *
     * @param request
     *    the call request to execute, cannot be <code>null</code>.
     *
     * @param target
     *    the service target on which to execute the request, cannot be
     *    <code>null</code>.
     *
     * @return
     *    the call result, never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>target == null || request == null</code>.
     *
     * @throws GenericCallException
     *    if the first call attempt failed due to a generic reason and all the
     *    other call attempts failed as well.
     *
     * @throws HTTPCallException
     *    if the first call attempt failed due to an HTTP-related reason and
     *    all the other call attempts failed as well.
     *
     * @deprecated
     *    Deprecated since XINS 1.0.0, since this method is expected to be
     *    removed. Please do not use it directly.
     */
    public HTTPCallResult call(HTTPCallRequest  request,
                               TargetDescriptor target)
    throws IllegalArgumentException,
           GenericCallException,
           HTTPCallException {
       return call(request, (HTTPCallConfig) null, target);
    }
 
 
    /**
     * Constructs an appropriate <code>CallResult</code> object for a
     * successful call attempt. This method is called from
     * {@link #doCall(CallRequest)}.
     *
     * <p>The implementation of this method in class
     * {@link HTTPServiceCaller} expects an {@link HTTPCallRequest} and
     * returns an {@link HTTPCallResult}.
     *
     * @param request
     *    the {@link CallRequest} that was to be executed, never
     *    <code>null</code> when called from {@link #doCall(CallRequest)};
     *    should be an instance of class {@link HTTPCallRequest}.
     *
     * @param succeededTarget
     *    the {@link TargetDescriptor} for the service that was successfully
     *    called, never <code>null</code> when called from
     *    {@link #doCall(CallRequest)}.
     *
     * @param duration
     *    the call duration in milliseconds, must be a non-negative number.
     *
     * @param exceptions
     *    the list of {@link CallException} instances, or <code>null</code> if
     *    there were no call failures.
     *
     * @param result
     *    the result from the call, which is the object returned by
     *    {@link #doCallImpl(CallRequest,TargetDescriptor)}, always an instance
     *    of class {@link HTTPCallResult}, never <code>null</code>; .
     *
     * @return
     *    an {@link HTTPCallResult} instance, never <code>null</code>.
     *
     * @throws ClassCastException
     *    if either <code>request</code> or <code>result</code> is not of the
     *    correct class.
     */
    protected CallResult createCallResult(CallRequest       request,
                                          TargetDescriptor  succeededTarget,
                                          long              duration,
                                          CallExceptionList exceptions,
                                          Object            result)
    throws ClassCastException {
 
 
       return new HTTPCallResult((HTTPCallRequest) request,
                                 succeededTarget,
                                 duration,
                                 exceptions,
                                 (HTTPCallResultData) result);
    }
 
    /**
     * Determines whether a call should fail-over to the next selected target
     * based on a request, call configuration and exception list.
     * This method should only be called from
     * {@link #doCall(CallRequest,CallConfig)}.
     *
     * <p>The implementation of this method in class {@link HTTPServiceCaller}
     * returns <code>true</code> if and only if one of the following conditions
     * holds true:
     *
     * <ul>
     *    <li><code>super.shouldFailOver(request, callConfig, exceptions)</code>
     *    <li><code>exceptions.{@link CallExceptionList#last() last()}
     *        instanceof {@link StatusCodeHTTPCallException}</code> and for
     *        that exception
     *        {@link StatusCodeHTTPCallException#getStatusCode() getStatusCode()} is not in the
     *        range 200-299.
     * </ul>
     *
     * @param request
     *    the request for the call, as passed to {@link #doCall(CallRequest)},
     *    should not be <code>null</code>.
     *
     * @param callConfig
     *    the call config that is currently in use, never <code>null</code>.
     *
     * @param exceptions
     *    the current list of {@link CallException}s; never
     *    <code>null</code>; get the most recent one by calling
     *    <code>exceptions.</code>{@link CallExceptionList#last() last()}.
     *
     * @return
     *    <code>true</code> if the call should fail-over to the next target, or
     *    <code>false</code> if it should not.
     */
    protected boolean shouldFailOver(CallRequest       request,
                                     CallConfig        callConfig,
                                     CallExceptionList exceptions) {
 
       CallException last = exceptions.last();
 
       // Let the superclass do it's job
       if (super.shouldFailOver(request, callConfig, exceptions)) {
          return true;
 
       // A non-2xx HTTP status code indicates the request was not handled
       } else if (last instanceof StatusCodeHTTPCallException) {
          int code = ((StatusCodeHTTPCallException) last).getStatusCode();
          return (code < 200 || code > 299);
 
       // Otherwise do not fail over
       } else {
          return false;
       }
    }
 
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * Executor of calls to an API.
     *
     * @version $Revision$ $Date$
     * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
     *
     * @since XINS 1.0.0
     */
    private static final class CallExecutor extends Thread {
 
       //-------------------------------------------------------------------------
       // Class fields
       //-------------------------------------------------------------------------
 
       /**
        * The number of constructed call executors.
        */
       private static int CALL_EXECUTOR_COUNT;
 
 
       //----------------------------------------------------------------------
       // Constructors
       //----------------------------------------------------------------------
 
       /**
        * Constructs a new <code>CallExecutor</code> for the specified call to
        * an HTTP service.
        *
        * <p>A <em>Nested Diagnostic Context identifier</em> (NDC) may be
        * specified, which will be set for the new thread when it is executed.
        * If the NDC is <code>null</code>, then it will be left unchanged. See
        * the {@link NDC} class.
        *
        * @param request
        *    the call request to execute, cannot be <code>null</code>.
        *
        * @param callConfig
        *    the call configuration, never <code>null</code>.
        *
        * @param target
        *    the service target on which to execute the request, cannot be
        *    <code>null</code>.
        *
        * @param context
        *    the <em>Nested Diagnostic Context identifier</em> (NDC), or
        *    <code>null</code>.
        *
        * @throws IllegalArgumentException
        *    if <code>target     == null
        *          || callConfig == null
        *          || request    == null</code>.
        */
       private CallExecutor(HTTPCallRequest  request,
                            HTTPCallConfig   callConfig,
                            TargetDescriptor target,
                            String           context)
       throws IllegalArgumentException {
 
          // Create textual representation of this object
          _asString = "HTTP call executor #" + (++CALL_EXECUTOR_COUNT);
 
          // Check preconditions
          MandatoryArgumentChecker.check("request",    request,
                                         "callConfig", callConfig,
                                         "target",     target);
 
          // Store data for later use in the run() method
          _request    = request;
          _callConfig = callConfig;
          _target     = target;
          _context    = context;
       }
 
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       /**
        * Textual representation of this object. Never <code>null</code>.
        */
       private final String _asString;
 
       /**
        * The call request to execute. Never <code>null</code>.
        */
       private final HTTPCallRequest _request;
 
       /**
        * The call configuration. Never <code>null</code>.
        */
       private final HTTPCallConfig _callConfig;
 
       /**
        * The service target on which to execute the request. Never
        * <code>null</code>.
        */
       private final TargetDescriptor _target;
 
       /**
        * The <em>Nested Diagnostic Context identifier</em> (NDC). Is set to
        * <code>null</code> if it should be left unchanged.
        */
       private final String _context;
 
       /**
        * The exception caught while executing the call. If there was no
        * exception, then this field is <code>null</code>.
        */
       private Throwable _exception;
 
       /**
        * The name of the class that threw the exception. This field is
        * <code>null</code> if the call was performed successfully.
        */
       private String _throwingClass;
 
       /**
        * The name of the method that threw the exception. This field is
        * <code>null</code> if the call was performed successfully.
        */
       private String _throwingMethod;
 
       /**
        * The result from the call. The value of this field is
        * <code>null</code> if the call was unsuccessful or if it was not
        * executed yet.
        */
       private HTTPCallResultData _result;
 
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
 
       /**
        * Runs this thread. It will call the HTTP service. If that call was
        * successful, then the result is stored in this object. Otherwise
        * there is an exception, in which case that exception is stored in this
        * object instead.
        */
       public void run() {
 
          // TODO: Check if this request was already executed, since this is a
          //       stateful object. If not, mark it as executing within a
          //       synchronized section, so it may no 2 threads may execute
          //       this request at the same time.
 
          // XXX: Note that performance could be improved by using local
          //      variables for _target and _request
 
          // Activate the diagnostic context ID
          if (_context != null) {
             NDC.push(_context);
          }
 
          // Construct new HttpClient object
          HttpClient client = new HttpClient();
 
          // Determine URL and time-outs
          String url               = _target.getURL();
          int    totalTimeOut      = _target.getTotalTimeOut();
          int    connectionTimeOut = _target.getConnectionTimeOut();
          int    socketTimeOut     = _target.getSocketTimeOut();
 
          // Configure connection time-out and socket time-out
          client.setConnectionTimeout(connectionTimeOut);
          client.setTimeout          (socketTimeOut);
 
          // Construct the method object
          HttpMethod method = createMethod(url, _request, _callConfig);
 
          // Perform the HTTP call
          try {
             // Execute call
             _throwingClass  = client.getClass().getName();
             _throwingMethod = "executeMethod(" + method.getClass().getName() + ')';
             int statusCode  = client.executeMethod(method);
 
             // Get response body
             _throwingClass  = method.getClass().getName();
             _throwingMethod = "getResponseBody()";
             byte[] body     = method.getResponseBody();
 
             // Store the result
             _throwingClass  = HTTPCallResultDataHandler.class.getName();
             _throwingMethod = "<init>(int,byte[])";
             _result         = new HTTPCallResultDataHandler(statusCode, body);
 
             // No exception thrown, reset _throwingXXXX fields
             _throwingClass  = null;
             _throwingMethod = null;
 
          // If an exception is thrown, store it for processing at later stage
          } catch (Throwable exception) {
             _exception = exception;
          }
 
          // Release the HTTP connection immediately
          try {
             method.releaseConnection();
          } catch (Throwable exception) {
             Log.log_1052(exception, method.getClass().getName(), "releaseConnection()");
          }
 
          // Unset the diagnostic context ID
          if (_context != null) {
             NDC.pop();
          }
 
          // TODO: Mark this CallExecutor object as executed, so it may not be
          //       run again
       }
 
       /**
        * Gets the exception if any generated when calling the method.
        *
        * @return
        *    the invocation exception or <code>null</code> if the call was
        *    performed successfully.
        */
       private Throwable getException() {
          return _exception;
       }
 
       /**
        * Gets the name of the class that threw the exception.
        *
        * @return
        *    the name of the class that threw the exception or
        *    <code>null</code> if the call was performed successfully.
        */
       private String getThrowingClass() {
          return _throwingClass;
       }
 
       /**
        * Gets the name of the method that threw the exception.
        *
        * @return
        *    the name of the method that threw the exception or
        *    <code>null</code> if the call was performed successfully.
        */
       private String getThrowingMethod() {
          return _throwingMethod;
       }
 
       /**
        * Returns the result if the call was successful. If the call was
        * unsuccessful, then <code>null</code> is returned.
        *
        * @return
        *    the result from the call, or <code>null</code> if it was
        *    unsuccessful.
        */
       private HTTPCallResultData getData() {
          return _result;
       }
    }
 
    /**
     * Container of the data part of an HTTP call result.
     *
     * @version $Revision$ $Date$
     * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
     *
     * @since XINS 1.0.0
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
