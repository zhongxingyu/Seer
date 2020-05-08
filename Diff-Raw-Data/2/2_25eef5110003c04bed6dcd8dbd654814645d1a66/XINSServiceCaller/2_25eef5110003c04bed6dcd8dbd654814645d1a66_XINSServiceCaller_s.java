 /*
  * $Id$
  *
  * Copyright 2004 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.client;
 
 import org.xins.common.MandatoryArgumentChecker;
 
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.collections.PropertyReaderUtils;
 
 import org.xins.common.http.HTTPCallException;
 import org.xins.common.http.HTTPCallRequest;
 import org.xins.common.http.HTTPCallResult;
 import org.xins.common.http.HTTPServiceCaller;
 import org.xins.common.http.StatusCodeHTTPCallException;
 
 import org.xins.common.service.CallException;
 import org.xins.common.service.CallExceptionList;
 import org.xins.common.service.CallRequest;
 import org.xins.common.service.CallResult;
 import org.xins.common.service.ConnectionTimeOutCallException;
 import org.xins.common.service.ConnectionRefusedCallException;
 import org.xins.common.service.Descriptor;
 import org.xins.common.service.GenericCallException;
 import org.xins.common.service.ServiceCaller;
 import org.xins.common.service.SocketTimeOutCallException;
 import org.xins.common.service.TargetDescriptor;
 import org.xins.common.service.TotalTimeOutCallException;
 import org.xins.common.service.UnknownHostCallException;
 
 import org.xins.common.text.FastStringBuffer;
 import org.xins.common.text.ParseException;
 import org.xins.common.text.TextUtils;
 
 import org.xins.logdoc.LogdocSerializable;
 
 /**
  * XINS service caller. This class can be used to perform a call to a XINS
  * service, over HTTP, and fail-over to other XINS services if the first one
  * fails.
  *
  * <h2>Load-balancing and fail-over</h2>
  *
  * <p>To perform a XINS call, use {@link #call(XINSCallRequest)}. Fail-over
  * and load-balancing can be performed automatically.
  *
  * <p>How load-balancing is done depends on the {@link Descriptor} passed to
  * the {@link #XINSServiceCaller(Descriptor)} constructor. If it is a
  * {@link TargetDescriptor}, then only this single target service is called
  * and no load-balancing is performed. If it is a
  * {@link org.xins.common.service.GroupDescriptor}, then the configuration of
  * the <code>GroupDescriptor</code> determines how the load-balancing is done.
  * A <code>GroupDescriptor</code> is a recursive data structure, which allows
  * for fairly advanced load-balancing algorithms.
  *
  * <p>If a call attempt fails and there are more available target services,
  * then the <code>XINSServiceCaller</code> may or may not fail-over to a next
  * target. If the request was not accepted by the target service, then
  * fail-over is considered acceptable and will be performed. This includes
  * the following situations:
  *
  * <ul>
  *    <li>if the <em>failOverAllowed</em> property is set to <code>true</code>
  *        for the {@link XINSCallRequest};
  *    <li>on connection refusal;
  *    <li>if a connection attempt times out;
  *    <li>if an HTTP status code other than 200-299 is returned;
  *    <li>if the XINS error code <em>_InvalidRequest</em> is returned;
  *    <li>if the XINS error code <em>_DisabledFunction</em> is returned.
  * </ul>
  *
  * <p>If none of these conditions holds, then fail-over is not considered
  * acceptable and will not be performed.
  *
  * <h2>Example code</h2>
  *
  * <p>The following example code snippet constructs a
  * <code>XINSServiceCaller</code> instance:
  *
  * <blockquote><pre>// Initialize properties for the services. Normally these
 // properties would come from a configuration source, like a file.
 {@link org.xins.common.collections.BasicPropertyReader} properties = new {@link org.xins.common.collections.BasicPropertyReader#BasicPropertyReader() org.xins.common.collections.BasicPropertyReader}();
 properties.{@link org.xins.common.collections.BasicPropertyReader#set(String,String) set}("myapi",         "group, random, server1, server2");
 properties.{@link org.xins.common.collections.BasicPropertyReader#set(String,String) set}("myapi.server1", "service, http://server1/myapi, 10000");
 properties.{@link org.xins.common.collections.BasicPropertyReader#set(String,String) set}("myapi.server2", "service, http://server2/myapi, 12000");
 
 // Construct a descriptor and a XINSServiceCaller instance
 {@link Descriptor Descriptor} descriptor = {@link org.xins.common.service.DescriptorBuilder DescriptorBuilder}.{@link org.xins.common.service.DescriptorBuilder#build(PropertyReader,String) build}(properties, "myapi");
 XINSServiceCaller caller = new {@link #XINSServiceCaller(Descriptor) XINSServiceCaller}(descriptor);</pre></blockquote>
  *
  * <p>Then the following code snippet uses this <code>XINSServiceCaller</code>
  * to perform a call to a XINS function named <em>_GetStatistics</em>, using
  * HTTP POST:
  *
  * <blockquote><pre>// Prepare for the call
 {@link String}          function = "_GetStatistics";
 {@link org.xins.common.collections.PropertyReader}  params   = null;
 boolean         failOver = true;
 {@link org.xins.common.http.HTTPMethod}      method   = {@link org.xins.common.http.HTTPMethod}.{@link org.xins.common.http.HTTPMethod#POST POST};
 {@link XINSCallRequest} request  = new {@link XINSCallRequest#XINSCallRequest(String,PropertyReader,boolean,HTTPMethod) XINSCallRequest}(function, params, failOver, method);
 
 // Perform the call
 {@link XINSCallResult} result = caller.{@link #call(XINSCallRequest) call}(request);</pre></blockquote>
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 1.0.0
  */
 public final class XINSServiceCaller extends ServiceCaller {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * Fully-qualified name of this class.
     */
    private static final String CLASSNAME = XINSServiceCaller.class.getName();
 
 
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
       Log.log_2000(CLASSNAME, null);
 
       return descriptor;
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>XINSServiceCaller</code> with the specified
     * descriptor.
     *
     * @param descriptor
     *    the descriptor of the service, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>descriptor == null</code>.
     */
    public XINSServiceCaller(Descriptor descriptor)
    throws IllegalArgumentException {
 
       // Trace and then call constructor of superclass
       super(trace(descriptor));
 
       _parser     = new XINSCallResultParser();
       _httpCaller = new HTTPServiceCaller(descriptor);
 
       // TRACE: Leave constructor
       Log.log_2002(CLASSNAME, null);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The result parser. This field cannot be <code>null</code>.
     */
    private final XINSCallResultParser _parser;
 
    /**
     * An HTTP service caller instance. This is used to actually perform the
     * request towards a XINS API using HTTP. This field cannot be
     * <code>null</code>.
     */
    private final HTTPServiceCaller _httpCaller;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Executes the specified XINS call request towards one of the associated
     * targets. If the call succeeds with one of these targets, then a
     * {@link XINSCallResult} object is returned. Otherwise, if none of the
     * targets could successfully be called, a
     * {@link org.xins.common.service.CallException} is thrown.
     *
     * <p>If the call succeeds, but the result is unsuccessful, then an
     * {@link UnsuccessfulXINSCallException} is thrown, which contains the
     * result.
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
     * @throws XINSCallException
     *    if the first call attempt failed due to a XINS-related reason and
     *    all the other call attempts failed as well.
     */
    public XINSCallResult call(XINSCallRequest request)
    throws IllegalArgumentException,
           GenericCallException,
           HTTPCallException,
           XINSCallException {
 
       final String METHODNAME = "call(XINSCallException)";
 
       // TRACE: Enter method
       Log.log_2003(CLASSNAME, METHODNAME, null);
 
       XINSCallResult result;
       try {
          result = (XINSCallResult) doCall(request);
 
       // Allow GenericCallException, HTTPCallException, XINSCallException and
       // Error to proceed, but block other kinds of exceptions and throw an
       // Error instead.
       } catch (CallException exception) {
 
          // Get URL, function and parameters (for logging)
          TargetDescriptor   target    = exception.getTarget();
          String             url       = target.getURL();
          String             function  = request.getFunctionName();
          PropertyReader     p         = request.getParameters();
          LogdocSerializable params    = PropertyReaderUtils.serialize(p, "-");
 
          // Get the time-out values (for logging)
          int totalTimeOut      = target.getTotalTimeOut();
          int connectionTimeOut = target.getConnectionTimeOut();
          int socketTimeOut     = target.getSocketTimeOut();
 
          // Call performed
          long duration = exception.getDuration();
          Log.log_2101(url, function, params, duration);
 
          // Generic call exception
          if (exception instanceof GenericCallException) {
             if (exception instanceof UnknownHostCallException) {
                Log.log_2102(url, function, params, duration);
             } else if (exception instanceof ConnectionRefusedCallException) {
                Log.log_2103(url, function, params, duration);
             } else if (exception instanceof ConnectionTimeOutCallException) {
                Log.log_2104(url, function, params, duration, connectionTimeOut);
             } else if (exception instanceof SocketTimeOutCallException) {
                Log.log_2105(url, function, params, duration, socketTimeOut);
             } else if (exception instanceof TotalTimeOutCallException) {
                Log.log_2106(url, function, params, duration, totalTimeOut);
             } else {
                Log.log_2050(CLASSNAME, METHODNAME,
                             "Unrecognized GenericCallException subclass " +
                             exception.getClass().getName() + '.');
             }
 
             throw (GenericCallException) exception;
 
          // HTTP-specific call exception
          } else if (exception instanceof HTTPCallException) {
             if (exception instanceof StatusCodeHTTPCallException) {
                int code = ((StatusCodeHTTPCallException) exception).getStatusCode();
                Log.log_2108(url, function, params, duration, code);
             } else {
                Log.log_2050(CLASSNAME, METHODNAME,
                             "Unrecognized HTTPCallException subclass " +
                             exception.getClass().getName() + '.');
             }
             throw (HTTPCallException) exception;
 
          // XINS-specific call exception
          } else if (exception instanceof XINSCallException) {
             if (exception instanceof InvalidResultXINSCallException) {
                String detail = exception.getDetail();
                Log.log_2110(url, function, params, duration, detail);
             } else if (exception instanceof UnsuccessfulXINSCallException) {
               String errorCode = (UnsuccessfulXINSCallException) exception.getErrorCode();
                Log.log_2112(url, function, params, duration, errorCode);
             } else if (exception instanceof UnacceptableResultXINSCallException) {
                // This should never happen. Log and re-throw the exception.
                Log.log_2052(exception, CLASSNAME, "doCall(CallRequest)");
             } else {
                Log.log_2050(CLASSNAME, METHODNAME,
                             "Unrecognized XINSCallException subclass " +
                             exception.getClass().getName() + '.');
             }
             throw (XINSCallException) exception;
 
          // Unknown kind of CallException. This should never happen. Log and
          // re-throw the exception, wrapped in an Error.
          } else {
             Log.log_2052(exception, CLASSNAME, "doCall(CallRequest)");
 
             FastStringBuffer message = new FastStringBuffer(190);
             message.append("XINSServiceCaller.doCall(CallRequest) threw unexpected ");
             message.append(exception.getClass().getName());
             message.append(". Message: ");
             message.append(TextUtils.quote(exception.getMessage()));
             message.append('.');
 
             throw new Error(message.toString(), exception);
          }
 
       // Not a CallException but other kind of exception. This should never
       // happen. Log and re-throw the exception, wrapped in an Error.
       } catch (Throwable exception) {
          Log.log_2052(exception, CLASSNAME, "doCall(CallRequest)");
 
          FastStringBuffer message = new FastStringBuffer(190);
          message.append("XINSServiceCaller.doCall(CallRequest) threw unexpected ");
          message.append(exception.getClass().getName());
          message.append(". Message: ");
          message.append(TextUtils.quote(exception.getMessage()));
          message.append('.');
 
          throw new Error(message.toString(), exception);
       }
 
       // On failure, throw UnsuccessfulXINSCallException, otherwise return result
       if (result.getErrorCode() != null) {
          throw new UnsuccessfulXINSCallException(result);
       }
 
       // TRACE: Leave method
       Log.log_2005(CLASSNAME, METHODNAME, null);
 
       return result;
    }
 
    /**
     * Calls the specified target using the specified subject. If the call
     * succeeds, then a {@link XINSCallResult} object is returned, otherwise a
     * {@link org.xins.common.service.CallException} is thrown.
     *
     * @param target
     *    the target to call, cannot be <code>null</code>.
     *
     * @param request
     *    the call request to be executed, must be an instance of class
     *    {@link XINSCallRequest}, cannot be <code>null</code>.
     *
     * @return
     *    the result, if and only if the call succeeded, always an instance of
     *    class {@link XINSCallResult}, never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>request == null || target == null</code>.
     *
     * @throws ClassCastException
     *    if the specified <code>request</code> object is not <code>null</code>
     *    and not an instance of class {@link XINSCallRequest}.
     *
     * @throws GenericCallException
     *    if the call attempt failed due to a generic reason.
     *    other call attempts failed as well.
     *
     * @throws HTTPCallException
     *    if the call attempt failed due to an HTTP-related reason.
     *
     * @throws XINSCallException
     *    if the call attempt failed due to a XINS-related reason.
     */
    protected Object doCallImpl(CallRequest      request,
                                TargetDescriptor target)
    throws IllegalArgumentException,
           ClassCastException,
           GenericCallException,
           HTTPCallException,
           XINSCallException {
 
       final String METHODNAME = "doCallImpl(CallRequest,TargetDescriptor)";
 
       // TRACE: Enter method
       Log.log_2003(CLASSNAME, METHODNAME, null);
 
       // Check preconditions
       MandatoryArgumentChecker.check("request", request, "target", target);
 
       // Convert the request to the appropriate class
       XINSCallRequest xinsRequest = (XINSCallRequest) request;
 
       // Get URL, function and parameters (for logging)
       String             url       = target.getURL();
       String             function  = xinsRequest.getFunctionName();
       PropertyReader     p         = xinsRequest.getParameters();
       LogdocSerializable params    = PropertyReaderUtils.serialize(p, "-");
 
       // Get the time-out values (for logging)
       int totalTimeOut      = target.getTotalTimeOut();
       int connectionTimeOut = target.getConnectionTimeOut();
       int socketTimeOut     = target.getSocketTimeOut();
 
       Log.log_2100(url, function, params, totalTimeOut, connectionTimeOut, socketTimeOut);
 
       // Delegate the actual HTTP call to the HTTPServiceCaller.
       // The HTTPServiceCaller.call method may throw a CallException, which
       // is not caught in this method, but in call(XINSCallRequest).
       HTTPCallRequest httpRequest = xinsRequest.getHTTPCallRequest();
       HTTPCallResult  httpResult  = _httpCaller.call(httpRequest, target);
 
       // Determine call duration
       long duration = httpResult.getDuration();
 
       Log.log_2101(url, function, params, duration);
 
       // Make sure data was received
       byte[] httpData = httpResult.getData();
       if (httpData == null || httpData.length == 0) {
          throw new InvalidResultXINSCallException(
             xinsRequest, target, duration, "No data received.", null);
       }
 
       // Parse the result
       XINSCallResultData resultData;
       try {
          resultData = _parser.parse(httpData);
       } catch (ParseException e) {
 
          // Get the parse error description
          String detail = e.getDetail();
 
          // Create a message for the new exception
          FastStringBuffer message = new FastStringBuffer(69);
          message.append("Failed to parse result");
          if (detail != null) {
             message.append(": ");
             message.append(detail);
          } else {
             message.append('.');
          }
 
          throw new InvalidResultXINSCallException(
             xinsRequest, target, duration, message.toString(), e);
       }
 
       // If the result is unsuccessful, throw an exception
       if (resultData.getErrorCode() != null) {
          throw new UnsuccessfulXINSCallException(
             xinsRequest, target, duration, resultData);
       }
 
       // TRACE: Leave method
       Log.log_2005(CLASSNAME, METHODNAME, null);
 
       return resultData;
    }
 
    /**
     * Constructs an appropriate <code>CallResult</code> object for a
     * successful call attempt. This method is called from
     * {@link #doCall(CallRequest)}.
     *
     * <p>The implementation of this method in class
     * {@link XINSServiceCaller} expects an {@link XINSCallRequest} and
     * returns an {@link XINSCallResult}.
     *
     * @param request
     *    the {@link CallRequest} that was to be executed, never
     *    <code>null</code> when called from {@link #doCall(CallRequest)};
     *    should be an instance of class {@link XINSCallRequest}.
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
     *    the list of {@link org.xins.common.service.CallException} instances,
     *    or <code>null</code> if there were no call failures.
     *
     * @param result
     *    the result from the call, which is the object returned by
     *    {@link #doCallImpl(CallRequest,TargetDescriptor)}, always an instance
     *    of class {@link XINSCallResult}, never <code>null</code>; .
     *
     * @return
     *    a {@link XINSCallResult} instance, never <code>null</code>.
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
 
       final String METHODNAME = "createCallResult(CallRequest,TargetDescriptor,long,CallExceptionList,Object)";
 
       // TRACE: Enter method
       Log.log_2003(CLASSNAME, METHODNAME, null);
 
       XINSCallResult r = new XINSCallResult((XINSCallRequest) request,
                                             succeededTarget,
                                             duration,
                                             exceptions,
                                             (XINSCallResultData) result);
 
       // TRACE: Leave method
       Log.log_2005(CLASSNAME, METHODNAME, null);
 
       return r;
    }
 
    /**
     * Determines whether a call should fail-over to the next selected target.
     *
     * @param request
     *    the request for the call, as passed to {@link #doCall(CallRequest)},
     *    should not be <code>null</code>.
     *
     * @param exception
     *    the exception caught while calling the most recently called target,
     *    should not be <code>null</code>.
     *
     * @return
     *    <code>true</code> if the call should fail-over to the next target, or
     *    <code>false</code> if it should not.
     *
     * @throws ClassCastException
     *    if <code>request</code> is not an instance of class
     *    {@link XINSCallRequest}.
     */
    protected boolean shouldFailOver(CallRequest request,
                                     Throwable   exception)
    throws ClassCastException {
 
       final String METHODNAME = "shouldFailOver(CallRequest,Throwable)";
 
       // TRACE: Enter method
       Log.log_2003(CLASSNAME, METHODNAME, null);
 
       // The request must be a XINS call request
       XINSCallRequest xinsRequest = (XINSCallRequest) request;
 
       boolean should;
 
       // If fail-over is allowed even if request is already sent, then
       // short-circuit and allow fail-over.
       //
       // XXX: Note that fail-over will even be allowed if there was an
       //      internal error that may not have anything to do with the
       //      service being called, e.g. an OutOfMemoryError or an
       //      InterruptedException.
       if (xinsRequest.isFailOverAllowed()) {
          should = true;
 
       // Otherwise let the superclass look at the exception and determine
       // whether it allows fail-over in all instances, for example when a
       // connection is refused.
       } else if (super.shouldFailOver(request, exception)) {
          should = true;
 
       // Otherwise check if the request may fail-over from HTTP point-of-view
       //
       // XXX: Note that this duplicates code that is already in the
       //      HTTPServiceCaller. This may need to be refactored at some point.
       //      It has been decided to take this approach since the
       //      shouldFailOver method in class HTTPServiceCaller has protected
       //      access.
       //
       // A non-2xx HTTP status code indicates the request was not handled
       } else if (exception instanceof StatusCodeHTTPCallException) {
          int code = ((StatusCodeHTTPCallException) exception).getStatusCode();
          should = (code < 200 || code > 299);
 
       // Some XINS error codes indicate the request was not accepted
       } else if (exception instanceof UnsuccessfulXINSCallException) {
          String s = ((UnsuccessfulXINSCallException) exception).getErrorCode();
          should = ("_InvalidRequest".equals(s) || "_DisabledFunction".equals(s));
 
       // Otherwise do not fail over
       } else {
          should = false;
       }
 
       // TRACE: Leave method
       Log.log_2005(CLASSNAME, METHODNAME, should ? "true" : "false");
 
       return should;
    }
 }
