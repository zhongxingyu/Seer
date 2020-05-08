 /*
  * $Id$
  *
  * Copyright 2003-2008 Online Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.http;
 
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.io.InterruptedIOException;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.ConnectException;
 import java.net.NoRouteToHostException;
 import java.net.UnknownHostException;
 import java.util.Iterator;
 
 import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpMethodBase;
 import org.apache.commons.httpclient.HttpRecoverableException;
 import org.apache.commons.httpclient.NameValuePair;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.RequestEntity;
 import org.apache.commons.httpclient.methods.StringRequestEntity;
 import org.apache.commons.httpclient.params.HttpMethodParams;
 
 import org.apache.log4j.NDC;
 import org.xins.common.FormattedParameters;
 
 import org.xins.common.Log;
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.TimeOutException;
 import org.xins.common.Utils;
 import org.xins.common.collections.PropertyReader;
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
 import org.xins.common.service.NoRouteToHostCallException;
 import org.xins.common.service.ServiceCaller;
 import org.xins.common.service.SocketTimeOutCallException;
 import org.xins.common.service.TargetDescriptor;
 import org.xins.common.service.TotalTimeOutCallException;
 import org.xins.common.service.UnexpectedExceptionCallException;
 import org.xins.common.service.UnknownHostCallException;
 import org.xins.common.service.UnsupportedProtocolException;
 import org.xins.common.text.TextUtils;
 import org.xins.common.text.URLEncoding;
 
 /**
  * HTTP service caller. This class can be used to perform a call to an HTTP
  * server and fail-over to other HTTP servers if the first one fails.
  *
  * 
  * <h2>Supported protocols</h2>
  *
  * <p>This service caller supports both HTTP and HTTPS (which is HTTP
  * tunneled over a secure SSL connection). If a {@link TargetDescriptor} is
  * passed to the constructor with a protocol other than <code>"http"</code>
  * or <code>"https"</code>, then an {@link UnsupportedProtocolException} is
  * thrown.
  *
  * <h2>Load-balancing and fail-over</h2>
  *
  * <p>To perform an HTTP call using a
  * <code>HTTPServiceCaller</code> use {@link #call(HTTPCallRequest)}.
  *
  * <p>How load-balancing is done depends on the {@link Descriptor} passed to
  * the {@link #HTTPServiceCaller(Descriptor)} constructor:
  *
  * <ul>
  *    <li>if it is a {@link TargetDescriptor}, then only this single target
  *        service is called and no load-balancing is performed;
  *    <li>if it is a {@link org.xins.common.service.GroupDescriptor}, then the
  *        configuration of the <code>GroupDescriptor</code> determines how the
  *        load-balancing is done.
  * </ul>
  *
  * <p>A <code>GroupDescriptor</code> is a recursive data structure, which allows
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
  * <h2>Thread-safety</h2>
  *
  * <p>Instances of this class can safely be used from multiple threads at the 
  * same time.
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
  * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
  * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
  *
  * @since XINS 1.0.0
  */
 public class HTTPServiceCaller extends ServiceCaller {
 
    /**
     * Fully-qualified name of this class.
     */
    private static final String CLASSNAME = HTTPServiceCaller.class.getName();
 
    /**
     * Fully-qualified name of the inner class <code>CallExecutor</code>.
     */
    private static final String EXECUTOR_CLASSNAME =
       HTTPServiceCaller.CallExecutor.class.getName();
 
    /**
     * The number of constructed call executors.
     */
    private static int CALL_EXECUTOR_COUNT;
 
    /**
     * Lock object for <code>CALL_EXECUTOR_COUNT</code>. Never
     * <code>null</code>.
     */
    private static final Object CALL_EXECUTOR_COUNT_LOCK = new Object();
 
    /**
     * HTTP retry handler that does not allow any retries.
     */
    private static DefaultHttpMethodRetryHandler NO_RETRIES = new DefaultHttpMethodRetryHandler(0, false);
 
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
 
       // Call superclass constructor
       super(descriptor, callConfig);
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
 
    /**
     * Returns the {@link HttpClient} to use to contact the given target.
     *
     * @param target
     *    the target of the service.
     *
     * @return
     *    the HttpClient shared instance.
     */
    private static HttpClient getHttpClient(TargetDescriptor target) {
 
       HttpClient httpClient= new HttpClient();
 
       // Add support for proxies
       int proxyPort = 80;
       if ("true".equals(System.getProperty("proxySet")) && System.getProperty("proxyHost") != null) {
          String proxyHost = System.getProperty("proxyHost");
          if (System.getProperty("proxyPort") != null) {
             proxyPort = Integer.parseInt(System.getProperty("proxyPort"));
          }
          httpClient.getHostConfiguration().setProxy(proxyHost, proxyPort);
       } else if (System.getProperty("http.proxyHost") != null) {
          String proxyHost = System.getProperty("http.proxyHost");
          if (System.getProperty("http.proxyPort") != null) {
             proxyPort = Integer.parseInt(System.getProperty("http.proxyPort"));
          }
          httpClient.getHostConfiguration().setProxy(proxyHost, proxyPort);
       }
 
       int connectionTimeOut = target.getConnectionTimeOut();
       int socketTimeOut     = target.getSocketTimeOut();
 
       // Configure connection time-out and socket time-out
       // For compatibility with HTTPClient 2.0, the deprecated methods are still used.
       httpClient.setHttpConnectionFactoryTimeout(connectionTimeOut);
       httpClient.setTimeout(socketTimeOut);
 
       return httpClient;
    }
 
    /**
     * Creates an appropriate <code>HttpMethodBase</code> object for the
     * specified URL.
     *
     * @param url
     *    the URL for which to create an {@link HttpMethodBase} object, should
     *    not be <code>null</code>.
     *
     * @param request
     *    the HTTP call request, not <code>null</code>.
     *
     * @param callConfig
     *    the HTTP call configuration object, not <code>null</code>.
     *
     * @return
     *    the constructed {@link HttpMethodBase} object, not <code>null</code>.
     */
    private static HttpMethodBase createMethod(String          url,
                                               HTTPCallRequest request,
                                               HTTPCallConfig  callConfig) {
 
       // Get the HTTP method (like GET and POST) and parameters
       HTTPMethod     method     = callConfig.getMethod();
       PropertyReader parameters = request.getParameters();
 
       // HTTP POST request
       if (method == HTTPMethod.POST) {
          PostMethod postMethod = new UnicodePostMethod(url);
 
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
          postMethod.setFollowRedirects(callConfig.getFollowRedirect());
 
          return postMethod;
 
       // HTTP GET request
       } else if (method == HTTPMethod.GET) {
          GetMethod getMethod = new GetMethod(url);
          getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, NO_RETRIES);
 
          // Loop through the parameters
          StringBuffer query = new StringBuffer(255);
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
 
                if (query.length() > 0) {
                   query.append("&");
                }
                query.append(URLEncoding.encode(key));
                query.append("=");
                query.append(URLEncoding.encode(value));
             }
          }
          if (query.length() > 0) {
             getMethod.setQueryString(query.toString());
          }
          getMethod.setFollowRedirects(callConfig.getFollowRedirect());
 
          return getMethod;
 
       // Unrecognized HTTP method (only GET and POST are supported)
       } else {
          String detail = "Unrecognized HTTP method \"" + method + "\".";
          throw Utils.logProgrammingError(detail);
       }
    }
 
    /**
     * Checks if the specified protocol is supported (implementation method).
     * The protocol is the part in a URL before the string <code>"://"</code>).
     *
     * <p>This method should only ever be called from the
     * {@link #isProtocolSupported(String)} method.
     *
     * <p>The implementation of this method in class
     * <code>HTTPServiceCaller</code> throws an
     * {@link UnsupportedOperationException} unless the protocol equals either
     * <code>"http"</code> or <code>"https</code> (ignoring case)..
     *
     * @param protocol
     *    the protocol, guaranteed not to be <code>null</code>.
     *
     * @return
     *    <code>true</code> if the specified protocol is supported, or
     *    <code>false</code> if it is not.
     *
     * @since XINS 1.2.0
     */
    protected boolean isProtocolSupportedImpl(String protocol) {
       return "http".equalsIgnoreCase(protocol) || "https".equalsIgnoreCase(protocol);
    }
 
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
     * Sets the <code>HTTPCallConfig</code> associated with this HTTP service
     * caller.
     *
     * @param config
     *    the fall-back {@link HTTPCallConfig} object for this service caller,
     *    cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>config == null</code>.
     *
     * @since XINS 1.2.0
     */
    protected void setHTTPCallConfig(HTTPCallConfig config)
    throws IllegalArgumentException {
       super.setCallConfig(config);
    }
 
    /**
     * Returns the <code>HTTPCallConfig</code> associated with this service
     * caller.
     *
     * <p>This method is the type-safe equivalent of {@link #getCallConfig()}.
     *
     * @return
     *    the fall-back {@link HTTPCallConfig} object for this HTTP service
     *    caller, never <code>null</code>.
     *
     * @since XINS 1.2.0
     */
    public HTTPCallConfig getHTTPCallConfig() {
       return (HTTPCallConfig) getCallConfig();
    }
 
    /**
     * Executes a request towards the specified target. If the call succeeds,
     * then a {@link HTTPCallResult} object is returned, otherwise a
     * {@link CallException} is thrown.
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
    public Object doCallImpl(CallRequest      request,
                                CallConfig       callConfig,
                                TargetDescriptor target)
    throws ClassCastException, IllegalArgumentException, CallException {
 
       // Delegate to method with more specialized interface
       Object ret = call((HTTPCallRequest) request,
                         (HTTPCallConfig)  callConfig,
                         target);
 
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
     *
     * @since XINS 1.1.0
     */
    public HTTPCallResult call(HTTPCallRequest request,
                               HTTPCallConfig  callConfig)
    throws IllegalArgumentException, GenericCallException, HTTPCallException {
 
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
     */
    private HTTPCallResult call(HTTPCallRequest  request,
                               HTTPCallConfig   callConfig,
                               TargetDescriptor target)
    throws IllegalArgumentException,
           GenericCallException,
           HTTPCallException {
 
       // Get the parameters for logging
       PropertyReader p = request.getParameters();
       FormattedParameters params = new FormattedParameters(p, null, "", "?", 160);
 
       // Prepare a thread for execution of the call
       // NOTE: Preconditions are checked by the CallExecutor constructor
       CallExecutor executor = new CallExecutor(request, callConfig, target, NDC.peek());
 
       // Get URL and time-out values
       String url               = target.getURL();
       int    totalTimeOut      = target.getTotalTimeOut();
       int    connectionTimeOut = target.getConnectionTimeOut();
       int    socketTimeOut     = target.getSocketTimeOut();
 
       // About to make an HTTP call
       Log.log_1100(url, params);
 
       // Perform the HTTP call
       long start = System.currentTimeMillis();
       long duration;
       try {
          controlTimeOut(executor, target);
          // 2.1 code: TimeoutController.execute(executor, totalTimeOut);
 
       // Total time-out exceeded
       } catch (TimeOutException exception) {
          duration = System.currentTimeMillis() - start;
          Log.log_1106(url, params, duration, totalTimeOut);
          executor.dispose();
          throw new TotalTimeOutCallException(request, target, duration);
 
       }
 
       // Determine the call duration
       duration = System.currentTimeMillis() - start;
 
       // Log that the HTTP call is done
       Log.log_1101(url, params, duration);
 
       // Check for exceptions
       Throwable exception = executor.getException();
       if (exception != null) {
 
          String exceptionName = exception.getClass().getName();
          // Unknown host
          if (exception instanceof UnknownHostException) {
             Log.log_1102(url, params, duration);
             executor.dispose();
             throw new UnknownHostCallException(request, target, duration);
 
          // No route to host
          } else if (exception instanceof NoRouteToHostException) {
             Log.log_1110(url, params, duration);
             executor.dispose();
             throw new NoRouteToHostCallException(request, target, duration);
 
          // Connection refusal
          } else if (exception instanceof ConnectException) {
             Log.log_1103(url, params, duration);
             executor.dispose();
             throw new ConnectionRefusedCallException(request, target, duration);
 
          // Connection time-out
          //
          // XXX: We do not use instanceof because class ConnectTimeoutException
          //      is not available in HTTPClient 2.0
          } else if (exceptionName.equals("org.apache.commons.httpclient.ConnectTimeoutException")
                || exceptionName.equals("org.apache.commons.httpclient.HttpConnection.ConnectionTimeoutException")) {
             Log.log_1104(url, params, duration, connectionTimeOut);
             executor.dispose();
             throw new ConnectionTimeOutCallException(request, target, duration);
 
          // Socket time-out (Java 1.4+)
          //
          // XXX: We do not use instanceof because class SocketTimeoutException
          //      is not available in Java 1.3
          } else if (exceptionName.equals("java.net.SocketTimeoutException")
                || (exception instanceof HttpRecoverableException
                   && ((HttpRecoverableException) exception).getReason().indexOf("Read timed out") != -1)) {
             Log.log_1105(url, params, duration, socketTimeOut);
             executor.dispose();
             throw new SocketTimeOutCallException(request, target, duration);
 
          // Socket time-out (Java 1.3)
          } else if (exception instanceof InterruptedIOException) {
             String exMessage = exception.getMessage();
 
             // XXX: Only tested on Sun JVM
             // TODO: Test on non-Sun JVM
             if (exMessage != null && exMessage.startsWith("Read timed out")) {
                Log.log_1105(url, params, duration, socketTimeOut);
                executor.dispose();
                throw new SocketTimeOutCallException(request, target, duration);
 
             // Unspecific I/O error
             } else {
                Log.log_1109(exception, url, params, duration);
                executor.dispose();
                throw new IOCallException(request, target, duration,
                                          (IOException) exception);
             }
 
          // Unspecific I/O error
          } else if (exception instanceof IOException) {
             Log.log_1109(exception, url, params, duration);
             executor.dispose();
             throw new IOCallException(request, target, duration,
                                       (IOException) exception);
 
          // Unrecognized kind of exception caught
          } else {
             String thisMethod = "call(HTTPCallREquest, HTTPCallConfig, TargetDescriptor)";
             String subjectClass  = executor.getThrowingClass();
             String subjectMethod = executor.getThrowingMethod();
             Log.log_1052(exception, CLASSNAME, thisMethod, subjectClass, subjectMethod, null);
             executor.dispose();
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
 
          executor.dispose();
          throw new StatusCodeHTTPCallException(request, target, duration, code);
       }
 
       executor.dispose();
       return new HTTPCallResult(request, target, duration, null, data);
    }
 
    /**
     * Constructs an appropriate <code>CallResult</code> object for a
     * successful call attempt. This method is called from
     * {@link #doCall(CallRequest,CallConfig)}.
     *
     * <p>The implementation of this method in class
     * {@link HTTPServiceCaller} expects an {@link HTTPCallRequest} and
     * returns an {@link HTTPCallResult}.
     *
     * @param request
     *    the {@link CallRequest} that was to be executed, never
     *    <code>null</code> when called from {@link #doCall(CallRequest,CallConfig)};
     *    should be an instance of class {@link HTTPCallRequest}.
     *
     * @param succeededTarget
     *    the {@link TargetDescriptor} for the service that was successfully
     *    called, never <code>null</code> when called from
     *    {@link #doCall(CallRequest,CallConfig)}.
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
     *    {@link #doCallImpl(CallRequest,CallConfig,TargetDescriptor)},
     *    always an instance of class {@link HTTPCallResult}, never <code>null</code>; .
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
     *    the request for the call, as passed to {@link #doCall(CallRequest,CallConfig)},
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
          return code < 200 || code > 299;
 
       // Otherwise do not fail over
       } else {
          return false;
       }
    }
 
    /**
     * Executor of calls to an API.
     *
     * @version $Revision$ $Date$
     * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
     */
    private static final class CallExecutor extends Thread {
 
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
        */
       private CallExecutor(HTTPCallRequest  request,
                            HTTPCallConfig   callConfig,
                            TargetDescriptor target,
                            String           context) {
 
          // Store data for later use in the run() method
          _request    = request;
          _callConfig = callConfig;
          _target     = target;
          _context    = context;
 
          // Determine the unique ID of this instance
          int instanceID;
          synchronized (CALL_EXECUTOR_COUNT_LOCK) {
             instanceID = CALL_EXECUTOR_COUNT++;
          }
 
          // Set the name of this thread
          String name = EXECUTOR_CLASSNAME + " #" + instanceID;
          setName(name);
       }
 
       /**
        * The call request to execute. Never <code>null</code>.
        */
       private HTTPCallRequest _request;
 
       /**
        * The call configuration. Never <code>null</code>.
        */
       private HTTPCallConfig _callConfig;
 
       /**
        * The service target on which to execute the request. Never
        * <code>null</code>.
        */
       private TargetDescriptor _target;
 
       /**
        * The <em>Nested Diagnostic Context identifier</em> (NDC). Is set to
        * <code>null</code> if it should be left unchanged.
        */
       private String _context;
 
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
 
       /**
        * Runs this thread (wrapper method). It will call the HTTP service. If that call was
        * successful, then the result is stored in this object. Otherwise
        * there is an exception, in which case that exception is stored in this
        * object instead.
        */
       public void run() {
 
          // XXX: Note that performance could be improved by using local
          //      variables for _target and _request
 
          // Activate the diagnostic context ID
          if (_context != null) {
             NDC.push(_context);
          }
 
          // Get the HttpClient object
          HttpClient client = getHttpClient(_target);
 
          // Determine URL and time-outs
          String url = _target.getURL();
 
          // Construct the method object
          HttpMethodBase method = createMethod(url, _request, _callConfig);
 
          // Set the user agent, if specified.
          String userAgent = _callConfig.getUserAgent();
          if (! TextUtils.isEmpty(userAgent)) {
             method.setRequestHeader("User-Agent", userAgent);
          }
 
          // Perform the HTTP call
          try {
             // Execute call
             _throwingClass  = client.getClass().getName();
             _throwingMethod = "executeMethod(" + method.getClass().getName() + ')';
             int statusCode  = client.executeMethod(method);
 
             // Get response body
             _throwingClass  = method.getClass().getName();
             _throwingMethod = "getResponseBodyAsStream()";
             InputStream in  = method.getResponseBodyAsStream();
 
             byte[] body = null;
             if (in != null) {
                _throwingMethod    = "getResponseContentLength()";
                int contentLength = 4096;
 
                // This method fails with HTTPClient 2.0.
                try {
                   contentLength = (int) method.getResponseContentLength();
                } catch (NoSuchMethodError nsme) {
                   // Ignore
                }
 
                // Create byte array output stream
                int size = contentLength > 0 ? contentLength : 4096;
                ByteArrayOutputStream out = new ByteArrayOutputStream(size);
                byte[] buffer = new byte[4096];
 
                // Copy from the input stream to the byte array
                String inClass  = in.getClass().getName();
                String outClass = "java.io.ByteArrayOutputStream";
                _throwingClass  = inClass;
                _throwingMethod = "read(byte[])";
                for (int len = in.read(buffer); len > 0; ) {
 
                   _throwingClass  = outClass;
                   _throwingMethod = "write(byte[],int,int)";
                   out.write(buffer, 0, len);
 
                   _throwingClass  = inClass;
                   _throwingMethod = "read(byte[])";
                   len             = in.read(buffer);
                }
 
                _throwingClass  = outClass;
                _throwingMethod = "toByteArray()";
                body            = out.toByteArray();
             }
 
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
 
          // Release the HTTP connection immediately
          } finally {
             try {
                method.releaseConnection();
             } catch (Throwable exception) {
                String thisMethod = "run()";
                String subjectClass = method.getClass().getName();
                String subjectMethod = "releaseConnection()";
                Log.log_1052(exception, EXECUTOR_CLASSNAME, thisMethod, subjectClass, subjectMethod, null);
             }
          }
 
          // Remove the diagnostic context ID
          if (_context != null) {
             NDC.pop();
          }
          NDC.remove();
 
          // Set objects to null for garbage collection
          _callConfig = null;
          _context = null;
          _request = null;
          _target = null;
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
 
       /**
        * Disposes the result variables, so that the variables could be
        * garbage collected.
        */
       private void dispose() {
          _exception = null;
          _throwingClass = null;
          _throwingMethod = null;
          _result = null;
       }
    }
 
    /**
     * Container of the data part of an HTTP call result.
     *
     * @version $Revision$ $Date$
     * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
     *
     * @since XINS 1.0.0
     */
    private static final class HTTPCallResultDataHandler
    implements HTTPCallResultData {
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
 
       /**
        * The HTTP status code.
        */
       private final int _code;
 
       /**
        * The data returned.
        */
       private final byte[] _data;
 
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
 
    /**
     * Post method that encode the Unicode characters above 255 as %uxxxx
     * where xxxx is the hexadecimal value of the character.
     *
     * @version $Revision$ $Date$
     * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
     *
     * @since XINS 1.4.0
     */
    private static class UnicodePostMethod extends PostMethod {
 
       public UnicodePostMethod(String url) {
          super(url);
 
          // Disable retries
          getParams().setParameter(HttpMethodParams.RETRY_HANDLER, NO_RETRIES);
       }
 
       protected RequestEntity generateRequestEntity() {
          NameValuePair[] params = getParameters();
          int paramsCount = params.length;
          if (paramsCount == 0) {
             return super.generateRequestEntity();
          } else {
             StringBuffer queryString = new StringBuffer();
             for (int i = 0; i < paramsCount; i++) {
                if (i > 0) {
                   queryString.append('&');
                }
                queryString.append(URLEncoding.encode(params[i].getName()));
                queryString.append('=');
                queryString.append(URLEncoding.encode(params[i].getValue()));
             }
             try {
                return new StringRequestEntity(queryString.toString(),
                      "application/x-www-form-urlencoded", "UTF-8");
             } catch (UnsupportedEncodingException ueex) {
                // Should never happen
                throw Utils.logProgrammingError(ueex);
             }
          }
       }
    }
 }
