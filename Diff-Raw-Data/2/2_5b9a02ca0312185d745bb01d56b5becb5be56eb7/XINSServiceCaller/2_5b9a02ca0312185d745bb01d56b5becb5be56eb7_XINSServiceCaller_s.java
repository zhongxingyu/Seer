 /*
  * $Id$
  */
 package org.xins.client;
 
 import java.io.IOException;
 import java.net.ConnectException;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpConnection;
 import org.apache.commons.httpclient.HttpRecoverableException;
 import org.apache.commons.httpclient.methods.PostMethod;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.TimeOutException;
 
 import org.xins.common.collections.BasicPropertyReader;
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.collections.PropertyReaderUtils;
 
 import org.xins.common.service.CallException;
 import org.xins.common.service.CallExceptionList;
 import org.xins.common.service.CallRequest;
 import org.xins.common.service.CallResult;
 import org.xins.common.service.Descriptor;
 import org.xins.common.service.GenericCallException;
 import org.xins.common.service.GroupDescriptor;
 import org.xins.common.service.ServiceCaller;
 import org.xins.common.service.TargetDescriptor;
 
 import org.xins.common.http.HTTPCallException;
 import org.xins.common.http.HTTPCallRequest;
 import org.xins.common.http.HTTPCallResult;
 import org.xins.common.http.HTTPMethod;
 import org.xins.common.http.HTTPServiceCaller;
 import org.xins.common.http.StatusCodeHTTPCallException;
 
 import org.xins.common.text.ParseException;
 
 import org.xins.logdoc.LogdocSerializable;
 
 /**
  * XINS service caller. This class can be used to perform a call to a XINS
  * service, over HTTP, and fail-over to other XINS services if the first one
  * fails.
  *
  * <h2>Load-balancing and fail-over</h2>
  *
  * <p>There are 2 ways to perform a XINS call using a
  * <code>XINSServiceCaller</code> instance:
  *
  * <ul>
  *    <li>to a single XINS service, using
  *        {@link #call(XINSCallRequest,TargetDescriptor)};
  *    <li>to a set of one or more XINS services, using
  *        {@link #call(XINSCallRequest)};
  * </ul>
  *
  * <p>With the second form of a XINS call, fail-over and load-balancing can be
  * performed.
  *
  * <p>How load-balancing is done (in the second form) depends on the
  * {@link Descriptor} passed to the
  * {@link #XINSServiceCaller(Descriptor)} constructor. If it is a
  * {@link TargetDescriptor}, then only this single target service is called
  * and no load-balancing is performed. If it is a {@link GroupDescriptor},
  * then the configuration of the <code>GroupDescriptor</code> determines how
  * the load-balancing is done. A <code>GroupDescriptor</code> is a recursive
  * data structure, which allows for fairly advanced load-balancing algorithms.
  *
  * <p>If a call attempt fails and there are more available target services,
  * then the <code>XINSServiceCaller</code> may or may not fail-over to a next
  * target. If the request was not accepted by the target service, then
  * fail-over is considered acceptable and will be performed. This includes
  * the following situations:
  *
  * <ul>
 *    <li>if the <code>failOver</code> property is set to <code>true</code>
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
 {@link BasicPropertyReader} properties = new {@link BasicPropertyReader#BasicPropertyReader() BasicPropertyReader}();
 properties.{@link BasicPropertyReader#set(String,String) set}("myapi",         "group, random, server1, server2");
 properties.{@link BasicPropertyReader#set(String,String) set}("myapi.server1", "service, http://server1/myapi, 10000");
 properties.{@link BasicPropertyReader#set(String,String) set}("myapi.server2", "service, http://server2/myapi, 12000");
 
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
 {@link PropertyReader}  params   = null;
 boolean         failOver = true;
 {@link HTTPMethod}      method   = {@link HTTPMethod}.{@link HTTPMethod#POST POST};
 {@link XINSCallRequest} request  = new {@link XINSCallRequest#XINSCallRequest(String,PropertyReader,boolean,HTTPMethod) XINSCallRequest}(function, params, failOver, method);
 
 // Perform the call
 {@link XINSCallResult} result = caller.{@link #call(XINSCallRequest) call}(request);</pre></blockquote>
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 0.146
  */
 public final class XINSServiceCaller extends ServiceCaller {
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
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
       super(descriptor);
 
       _parser     = new XINSCallResultParser();
       _httpCaller = new HTTPServiceCaller(descriptor);
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
     * Calls the specified target using the specified subject. If the call
     * succeeds, then a {@link XINSCallResult} object is returned, otherwise a
     * {@link CallException} is thrown.
     *
     * <p>The implementation of this method in class
     * <code>XINSServiceCaller</code> delegates to
     * {@link #call(XINSCallRequest,TargetDescriptor)}.
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
     * @throws ClassCastException
     *    if the specified <code>request</code> object is not <code>null</code>
     *    and not an instance of class {@link XINSCallRequest}.
     *
     * @throws IllegalArgumentException
     *    if <code>target == null || request == null</code>.
     *
     * @throws CallException
     *    if the call to the specified target failed.
     *
     * @since XINS 0.207
     */
    protected Object doCallImpl(CallRequest      request,
                                TargetDescriptor target)
    throws ClassCastException, IllegalArgumentException, CallException {
 
       // Delegate to method with more specialized interface
       return call((XINSCallRequest) request, target);
    }
 
    /**
     * Performs the specified request towards the XINS service. If the call
     * succeeds with one of the targets, then a {@link XINSCallResult} object
     * is returned. Otherwise, if none of the targets could successfully be
     * called, a {@link CallException} is thrown.
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
     *
     * @since XINS 0.207
     */
    public XINSCallResult call(XINSCallRequest request)
    throws IllegalArgumentException,
           GenericCallException,
           HTTPCallException,
           XINSCallException {
 
       CallResult result;
       try {
          result = doCall(request);
 
       // Allow GenericCallException, HTTPCallException, XINSCallException and
       // Error to proceed, but block other kinds of exceptions and throw an
       // Error instead.
       } catch (GenericCallException exception) {
          throw exception;
       } catch (HTTPCallException exception) {
          throw exception;
       } catch (XINSCallException exception) {
          throw exception;
       } catch (Exception exception) {
          throw new Error(getClass().getName() + ".doCall(" + request.getClass().getName() + ") threw " + exception.getClass().getName() + '.');
       }
 
       return (XINSCallResult) result;
    }
 
    /**
     * Executes the specified call request on the specified XINS API. If the
     * call fails in any way or if the result is unsuccessful, then a
     * {@link XINSCallException} is thrown.
     *
     * @param target
     *    the service target on which to execute the request, cannot be
     *    <code>null</code>.
     *
     * @param request
     *    the call request to execute, cannot be <code>null</code>.
     *
     * @return
     *    the call result, never <code>null</code> and always successful.
     *
     * @throws IllegalArgumentException
     *    if <code>target == null || request == null</code>.
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
     *
     * @since XINS 0.207
     */
    public XINSCallResult call(XINSCallRequest  request,
                               TargetDescriptor target)
    throws IllegalArgumentException,
           GenericCallException,
           HTTPCallException,
           XINSCallException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("request", request, "target", target);
 
       // Log that we are about to call the API
       // TODO: Either uncomment or remove the following line
       // Log.log_2011(url, functionName, serParams, totalTimeOut, connectionTimeOut, socketTimeOut);
 
       // Delegate the actual HTTP call to the HTTPServiceCaller. This may
       // cause a CallException
       HTTPCallRequest httpRequest = request.getHTTPCallRequest();
       HTTPCallResult  httpResult  = _httpCaller.call(httpRequest, target);
 
       long duration = httpResult.getDuration();
 
       // Parse the result
       XINSCallResultData data;
       try {
          data = _parser.parse(httpResult.getData());
       } catch (ParseException parseException) {
          throw new InvalidResultXINSCallException(request, target, duration, "Failed to parse result.", parseException);
       }
 
       XINSCallResult xinsResult = new XINSCallResult(request,
                                                      target,
                                                      duration,
                                                      null,
                                                      data);
 
       // On failure, throw UnsuccessfulXINSCallException, otherwise return result
       if (data.getErrorCode() != null) {
          throw new UnsuccessfulXINSCallException(xinsResult);
 
       // Otherwise just return the result
       } else {
          return xinsResult;
       }
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
     *    the list of {@link CallException} instances, or <code>null</code> if
     *    there were no call failures.
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
     *
     * @since XINS 0.207
     */
    protected CallResult createCallResult(CallRequest       request,
                                          TargetDescriptor  succeededTarget,
                                          long              duration,
                                          CallExceptionList exceptions,
                                          Object            result)
    throws ClassCastException {
 
 
       return new XINSCallResult((XINSCallRequest) request,
                                 succeededTarget,
                                 duration,
                                 exceptions,
                                 (XINSCallResultData) result);
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
     * @since XINS 0.207
     */
    protected boolean shouldFailOver(CallRequest request,
                                     Throwable   exception) {
 
       // First let the superclass do it's job
       if (super.shouldFailOver(request, exception)) {
          return true;
       }
 
       // The request must be a XINS call request
       XINSCallRequest xinsRequest = (XINSCallRequest) request;
 
       // If fail-over is allowed even if request is already sent, then
       // short-circuit and allow fail-over
       //
       // XXX: Note that fail-over will even be allowed if there was an
       //      internal error that does not have anything to do with the
       //      service being called, e.g. an OutOfMemoryError or an
       //      InterruptedException. This could be improved by checking the
       //      type of exception and only allowingt fail-over if the exception
       //      indicates an I/O error.
       if (xinsRequest.isFailOverAllowed()) {
          return true;
       }
 
       // Get the HTTP request underlying the XINS request
       HTTPCallRequest httpRequest = xinsRequest.getHTTPCallRequest();
 
       // Check if the request may fail-over from HTTP point-of-view
       //
       // XXX: Note that this duplicates code that is already in the
       //      HTTPServiceCaller. This may need to be refactored at some point.
       //      It has been decided to take this approach, since the
       //      shouldFailOver method in class HTTPServiceCaller has protected
       //      access.
       //
       // A non-2xx HTTP status code indicates the request was not handled
       if (exception instanceof StatusCodeHTTPCallException) {
          int code = ((StatusCodeHTTPCallException) exception).getStatusCode();
          return (code < 200 || code > 299);
 
       // Some XINS error codes indicate the request was not accepted
       } else if (exception instanceof UnsuccessfulXINSCallException) {
          String s = ((UnsuccessfulXINSCallException) exception).getErrorCode();
          return ("_InvalidRequest".equals(s) || "_DisabledFunction".equals(s));
 
       // Otherwise do not fail over
       } else {
          return false;
       }
    }
 }
