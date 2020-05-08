 /*
  * $Id$
  *
  * Copyright 2004 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.service;
 
 import java.util.Iterator;
 
 import org.xins.common.Log;
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.TimeOutController;
 import org.xins.common.TimeOutException;
 import org.xins.common.Utils;
 
 /**
  * Abstraction of a service caller for a TCP-based service. Service caller
  * implementations can be used to perform a call to a service, and potentially
  * fail-over to other back-ends if one is not available. Additionally,
  * load-balancing and different types of time-outs are supported.
  *
  * <p>Back-ends are
  * represented by {@link TargetDescriptor} instances. Groups of back-ends are
  * represented by {@link GroupDescriptor} instances.
  *
  * <a name="section-lbfo"></a>
  * <h2>Load-balancing and fail-over</h2>
  *
  * <p>TODO: Describe load-balancing and fail-over.
  *
  * <a name="section-timeouts"></a>
  * <h2>Time-outs</h2>
  *
  * <p>TODO: Describe time-outs.
  *
  * <a name="section-callconfig"></a>
  * <h2>Call configuration</h2>
  *
  * <p>Some aspects of a call can be configured using a {@link CallConfig}
  * object. For example, the <code>CallConfig</code> base class indicates
  * whether fail-over is unconditionally allowed. Like this, some aspects of
  * the behaviour of the caller can be tweaked.
  *
  * <p>There are different places where a <code>CallConfig</code> can be
  * applied:
  *
  * <ul>
  *    <li>stored in a <code>ServiceCaller</code>;
  *    <li>stored in a <code>CallRequest</code>;
  *    <li>passed with the call method.
  * </ul>
  *
  * <p>First of all, each <code>ServiceCaller</code> instance will have a
  * fall-back <code>CallConfig</code>.
  *
  * <p>Secondly, a {@link CallRequest} instance may have a
  * <code>CallConfig</code> associated with it as well. If it does, then this
  * overrides the one on the <code>ServiceCaller</code> instance.
  *
  * <p>Finally, a <code>CallConfig</code> can be passed as an argument to the
  * call method. If it is, then this overrides any other settings.
  *
  * <a name="section-implementations"></a>
  * <h2>Implementations</h2>
  *
  * <p>This class is abstract and is intended to be have service-specific
  * subclasses, e.g. for HTTP, FTP, JDBC, etc.
  *
  * <p>Normally, a subclass should be stick to the following rules:
  *
  * <ol>
  *    <li>There should be a constructor that accepts only a {@link Descriptor}
  *        object. This constructor should call
  *        <code>super(descriptor, null)</code>. If this descriptor contains
  *        any {@link TargetDescriptor} instances that have an unsupported
  *        protocol, then an {@link UnsupportedProtocolException} should be
  *        thrown.
  *    <li>There should be a constructor that accepts both a
  *        {@link Descriptor} and a service-specific call config object
  *        (derived from {@link CallConfig}).  This constructor should call
  *        <code>super(descriptor, callConfig)</code>. If this descriptor
  *        contains any {@link TargetDescriptor} instances that have an
  *        unsupported protocol, then an {@link UnsupportedProtocolException}
  *        should be thrown.
  *    <li>There should be a <code>call</code> method that accepts only a
  *        service-specific request object (derived from {@link CallRequest}).
  *        It should call
  *        {@link #doCall(CallRequest,CallConfig) doCall}<code>(request, null)</code>.
  *    <li>There should be a <code>call</code> method that accepts both a
  *        service-specific request object (derived from {@link CallRequest}).
  *        and a service-specific call config object (derived from
  *        {@link CallConfig}).  It should call
  *        {@link #doCall(CallRequest,CallConfig) doCall}<code>(request, callConfig)</code>.
  *    <li>The method
  *        {@link #doCallImpl(CallRequest,CallConfig,TargetDescriptor)} must
  *        be implemented as specified.
  *    <li>The {@link #createCallResult(CallRequest,TargetDescriptor,long,CallExceptionList,Object) createCallResult}
  *        method must be implemented as specified.
  *    <li>To control when fail-over is applied, the method
  *        {@link #shouldFailOver(CallRequest,CallConfig,CallExceptionList)}
  *        may also be implemented. The implementation can assume that
  *        the passed {@link CallRequest} object is an instance of the
  *        service-specific call request class and that the passed
  *        {@link CallConfig} object is an instance of the service-specific
  *        call config class.
  * </ol>
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 1.0.0
  */
 public abstract class ServiceCaller extends Object {
 
    // TODO: Describe typical implementation scenario, e.g. a
    //       SpecificCallResult call(SpecificCallRequest, SpecificCallConfig)
    //       method that calls doCall(CallRequest,CallConfig).
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The fully-qualified name of this class.
     */
    private static final String CLASSNAME = ServiceCaller.class.getName();
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>ServiceCaller</code> object.
     *
     * <p>A default {@link CallConfig} object will be used.
     *
     * @param descriptor
     *    the descriptor of the service, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>descriptor == null</code>.
     *
     * @deprecated
     *    Deprecated since XINS 1.1.0.
     *    Use {@link #ServiceCaller(Descriptor,CallConfig)} instead. Although
     *    marked as deprecated, this constructor still works the same as in
     *    XINS 1.0.x.
     *    This constructor is guaranteed not to be removed before XINS 2.0.0.
     */
    protected ServiceCaller(Descriptor descriptor)
    throws IllegalArgumentException {
 
       final String THIS_METHOD    = "<init>(" + Descriptor.class.getName() + ')';
 
       // TRACE: Enter constructor
       Log.log_1000(CLASSNAME, null);
 
       // Check preconditions
       MandatoryArgumentChecker.check("descriptor", descriptor);
 
       // Set fields
       _newStyle   = false;
       _descriptor = descriptor;
       _callConfig = null;
       _className  = getClass().getName();
 
       // Make sure the old-style (XINS 1.0) doCallImpl method is implemented
       try {
          doCallImpl((CallRequest) null, (TargetDescriptor) null);
          throw new Error();
       } catch (Throwable t) {
          if (t instanceof MethodNotImplementedError) {
             final String SUBJECT_METHOD = "doCallImpl(" + CallRequest.class.getName() + ',' + TargetDescriptor.class.getName() + ')';
             final String DETAIL         = "Method " + SUBJECT_METHOD + " should be implemented since class uses old-style (XINS 1.0) constructor.";
             throw Utils.logProgrammingError(CLASSNAME, THIS_METHOD, _className, SUBJECT_METHOD, DETAIL);
          }
       }
 
       // Make sure the old-style (XINS 1.0) shouldFailOver method is implemented
       try {
          shouldFailOver((CallRequest) null, (Throwable) null);
          throw new Error();
       } catch (Throwable t) {
          if (t instanceof MethodNotImplementedError) {
             final String SUBJECT_METHOD = "shouldFailOver(" + CallRequest.class.getName() + "java.lang.Throwable)";
             final String DETAIL         = "Method " + SUBJECT_METHOD + " should be implemented since class uses old-style (XINS 1.0) constructor.";
             throw Utils.logProgrammingError(CLASSNAME, THIS_METHOD, _className, SUBJECT_METHOD, DETAIL);
          }
       }
 
       // Make sure the new-style (XINS 1.1) doCallImpl method is not implemented
       try {
          doCallImpl((CallRequest) null, (CallConfig) null, (TargetDescriptor) null);
          throw new Error();
       } catch (Throwable t) {
          if (! (t instanceof MethodNotImplementedError)) {
             final String SUBJECT_METHOD = "doCallImpl(" + CallRequest.class.getName() + ',' + CallConfig.class.getName() + ',' + TargetDescriptor.class.getName() + ')';
             final String DETAIL         = "Method " + SUBJECT_METHOD + " should not be implemented since class uses old-style (XINS 1.0) constructor.";
             throw Utils.logProgrammingError(CLASSNAME, THIS_METHOD, _className, SUBJECT_METHOD, DETAIL);
          }
       }
 
       // Make sure the new-style (XINS 1.1) shouldFailOver method is not implemented
       try {
          shouldFailOver((CallRequest) null, (CallConfig) null, (CallExceptionList) null);
          throw new Error();
       } catch (Throwable t) {
          if (! (t instanceof MethodNotImplementedError)) {
             final String SUBJECT_METHOD = "shouldFailOver(" + CallRequest.class.getName() + ',' + CallConfig.class.getName() + ',' + CallExceptionList.class.getName() + ')';
             final String DETAIL         = "Method " + SUBJECT_METHOD + " should not be implemented since class uses old-style (XINS 1.0) constructor.";
             throw Utils.logProgrammingError(CLASSNAME, THIS_METHOD, _className, SUBJECT_METHOD, DETAIL);
          }
       }
 
       // TRACE: Leave constructor
       Log.log_1002(CLASSNAME, null);
    }
 
    /**
     * Constructs a new <code>ServiceCaller</code> with the specified
     * <code>CallConfig</code>.
     *
     * @param descriptor
     *    the descriptor of the service, cannot be <code>null</code>.
     *
     * @param callConfig
     *    the {@link CallConfig} object, or <code>null</code> if the default
     *    should be used.
     *
     * @throws IllegalArgumentException
     *    if <code>descriptor == null</code>.
     *
     * @since XINS 1.1.0
     */
    protected ServiceCaller(Descriptor descriptor, CallConfig callConfig)
    throws IllegalArgumentException {
 
       final String THIS_METHOD = "<init>(" + Descriptor.class.getName() + ',' + CallConfig.class.getName() + ')';
 
       // TRACE: Enter constructor
       Log.log_1000(CLASSNAME, null);
 
       // Check preconditions
       MandatoryArgumentChecker.check("descriptor", descriptor);
 
       // Initialize all fields except callConfig
       _className  = getClass().getName();
       _newStyle   = true;
       _descriptor = descriptor;
 
       // If no CallConfig is specified, then use a default one
       if (callConfig == null) {
 
          String actualClass = getClass().getName();
 
          String SUBJECT_METHOD = "getDefaultCallConfig()";
 
          // Call getDefaultCallConfig() to get the default config...
          try {
             callConfig = getDefaultCallConfig();
 
          // ...the method must be implemented...
          } catch (MethodNotImplementedError e) {
             final String DETAIL = "Method " + SUBJECT_METHOD + " should be implemented since class uses new-style (XINS 1.1) constructor.";
             throw Utils.logProgrammingError(CLASSNAME, THIS_METHOD, _className, SUBJECT_METHOD, DETAIL);
 
          // ...it should not throw any exception...
          } catch (Throwable t) {
             final String DETAIL = null;
             throw Utils.logProgrammingError(CLASSNAME, THIS_METHOD, _className, SUBJECT_METHOD, DETAIL, t);
          }
 
          // ...and it should never return null.
          if (callConfig == null) {
             final String DETAIL = "Method returned null, although that is disallowed by the ServiceCaller.getDefaultCallConfig() contract.";
             throw Utils.logProgrammingError(CLASSNAME, THIS_METHOD, _className, SUBJECT_METHOD, DETAIL);
          }
       }
 
       // Set call configuration
       _callConfig = callConfig;
 
       // Make sure the old-style (XINS 1.0) doCallImpl method is not implemented
       try {
          doCallImpl((CallRequest) null, (TargetDescriptor) null);
          throw new Error();
       } catch (Throwable t) {
          if (! (t instanceof MethodNotImplementedError)) {
             final String SUBJECT_METHOD = "doCallImpl(" + CallRequest.class.getName() + ',' + TargetDescriptor.class.getName() + ')';
             final String DETAIL         = "Method " + SUBJECT_METHOD + " should not be implemented since this class (" + _className + ") uses the new-style (XINS 1.1) constructor.";
             throw Utils.logProgrammingError(CLASSNAME, THIS_METHOD, _className, SUBJECT_METHOD, DETAIL);
          }
       }
 
       // Make sure the old-style (XINS 1.0) shouldFailOver method is not implemented
       try {
          shouldFailOver((CallRequest) null, (Throwable) null);
          throw new Error();
       } catch (Throwable t) {
          if (! (t instanceof MethodNotImplementedError)) {
             final String SUBJECT_METHOD = "shouldFailOver(" + CallRequest.class.getName() + ',' + Throwable.class.getName() + ')';
             final String DETAIL         = "Method " + SUBJECT_METHOD + " should not be implemented since this class (" + _className + ") uses the new-style (XINS 1.1) constructor.";
             throw Utils.logProgrammingError(CLASSNAME, THIS_METHOD, _className, SUBJECT_METHOD, DETAIL);
          }
       }
 
       // Make sure the new-style (XINS 1.1) doCallImpl method is implemented
       try {
          doCallImpl((CallRequest) null, (CallConfig) null, (TargetDescriptor) null);
          throw new Error();
       } catch (Throwable t) {
          if (t instanceof MethodNotImplementedError) {
             final String SUBJECT_METHOD = "doCallImpl(" + CallRequest.class.getName() + ',' + CallConfig.class.getName() + ',' + TargetDescriptor.class.getName() + ')';
             final String DETAIL         = "Method " + SUBJECT_METHOD + " should be implemented since this class (" + _className + ") uses the new-style (XINS 1.1) constructor.";
             throw Utils.logProgrammingError(CLASSNAME, THIS_METHOD, _className, SUBJECT_METHOD, DETAIL);
          }
       }
 
       // Make sure the new-style (XINS 1.1) shouldFailOver method is implemented
       try {
          shouldFailOver((CallRequest) null, (CallConfig) null, (CallExceptionList) null);
          throw new Error();
       } catch (Throwable t) {
          if (t instanceof MethodNotImplementedError) {
             final String SUBJECT_METHOD = "shouldFailOver(" + CallRequest.class.getName() + ',' + CallConfig.class.getName() + ',' + CallExceptionList.class.getName() + ')';
             final String DETAIL         = "Method " + SUBJECT_METHOD + " should be implemented since this class (" + _className + ") uses the new-style (XINS 1.1) constructor.";
             throw Utils.logProgrammingError(CLASSNAME, THIS_METHOD, _className, SUBJECT_METHOD, DETAIL);
          }
       }
 
 
       // TRACE: Leave constructor
       Log.log_1002(CLASSNAME, null);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The name of the current (concrete) class. Never <code>null</code>.
     */
    private final String _className;
 
    /**
     * Flag that indicates if the new-style (XINS 1.1) (since XINS 1.1.0) or the old-style (XINS 1.0)
     * (XINS 1.0.0) behavior is expected from the subclass.
     */
    private final boolean _newStyle;
 
    /**
     * The descriptor for this service. Cannot be <code>null</code>.
     */
    private final Descriptor _descriptor;
 
    /**
     * The fall-back call config object for this service caller. Cannot be
     * <code>null</code>.
     */
    private final CallConfig _callConfig;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Returns the descriptor.
     *
     * @return
     *    the descriptor for this service, never <code>null</code>.
     */
    public final Descriptor getDescriptor() {
       return _descriptor;
    }
 
    /**
     * Returns the <code>CallConfig</code> associated with this service caller.
     *
     * @return
     *    the fall-back {@link CallConfig} object for this service caller,
     *    never <code>null</code>.
     *
     * @since XINS 1.1.0
     */
    public final CallConfig getCallConfig() {
       return _callConfig;
    }
 
    /**
     * Returns a default <code>CallConfig</code> object. This method is called
     * by the <code>ServiceCaller</code> constructor if no
     * <code>CallConfig</code> object was given.
     *
     * <p>Subclasses that support the new service calling framework (introduced
     * in XINS 1.1.0) <em>must</em> override this method to return a more
     * suitable <code>CallConfig</code> instance.
     *
     * <p>This method should never be called by subclasses.
     *
     * @return
     *    a new, appropriate, {@link CallConfig} instance, never
     *    <code>null</code>.
     *
     * @since XINS 1.1.0
     */
    protected CallConfig getDefaultCallConfig() {
       throw new MethodNotImplementedError();
    }
 
    /**
     * Attempts to execute the specified call request on one of the target
     * services, with the specified call configuration. During the execution,
     * {@link TargetDescriptor Target descriptors} will be picked and passed to
     * {@link #doCallImpl(CallRequest,CallConfig,TargetDescriptor)} until there
     * is one that succeeds, as long as fail-over can be done (according to
     * {@link #shouldFailOver(CallRequest,CallConfig,CallExceptionList)}).
     *
     * <p>If one of the calls succeeds, then the result is returned. If
     * none succeeds or if fail-over should not be done, then a
     * {@link CallException} is thrown.
     *
     * <p>Subclasses that want to use this method <em>must</em> implement
     * {@link #doCallImpl(CallRequest,CallConfig,TargetDescriptor)}. That
     * method is called for each call attempt to a specific service target
     * (represented by a {@link TargetDescriptor}).
     *
     * @param request
     *    the call request, not <code>null</code>.
     *
     * @param callConfig
     *    the call configuration, or <code>null</code> if the one defined for
     *    the call request should be used if specified, or otherwise the
     *    fall-back call configuration associated with this
     *    <code>ServiceCaller</code> (see {@link #getCallConfig()}).
     *
     * @return
     *    a combination of the call result and a link to the
     *    {@link TargetDescriptor target} that returned this result, if and
     *    only if one of the calls succeeded, could be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>request == null</code>.
     *
     * @throws CallException
     *    if all call attempts failed.
     *
     * @since XINS 1.1.0
     */
    protected final CallResult doCall(CallRequest request,
                                      CallConfig  callConfig)
    throws IllegalArgumentException, CallException {
 
       final String THIS_METHOD = "doCall(" + CallRequest.class.getName() + ',' + CallConfig.class.getName() + ')';
 
       // TRACE: Enter method
       Log.log_1003(CLASSNAME, THIS_METHOD, null);
 
       // This method should only be called if the subclass uses the new style
       if (! _newStyle) {
          final String SUBJECT_CLASS  = Utils.getCallingClass();
          final String SUBJECT_METHOD = Utils.getCallingMethod();
          final String DETAIL = "Method " + THIS_METHOD + " called while class " + _className + " uses old-style (XINS 1.0) constructor.";
          throw Utils.logProgrammingError(CLASSNAME, THIS_METHOD, SUBJECT_CLASS, SUBJECT_METHOD, DETAIL);
       }
 
       // Check preconditions
       MandatoryArgumentChecker.check("request", request);
 
       // Determine what config to use. The argument has priority, then the one
       // associated with the request and the fall-back is the one associated
       // with this service caller.
       if (callConfig == null) {
          callConfig = request.getCallConfig();
          if (callConfig == null) {
             callConfig = _callConfig;
          }
       }
 
       // Keep a reference to the most recent CallException since
       // setNext(CallException) needs to be called on it to make it link to
       // the next one (if there is one)
       CallException lastException = null;
 
       // Maintain the list of CallExceptions
       //
       // This is needed if a successful result (a CallResult object) is
       // returned, since it will contain references to the exceptions as well;
       //
       // Note that this object is lazily initialized because this code is
       // performance- and memory-optimized for the successful case
       CallExceptionList exceptions = null;
 
       // Iterate over all targets
       Iterator iterator = _descriptor.iterateTargets();
 
       // TODO: Improve performance, do not use an iterator?
 
       // There should be at least one target
       if (! iterator.hasNext()) {
          final String SUBJECT_CLASS  = _descriptor.getClass().getName();
          final String SUBJECT_METHOD = "iterateTargets()";
          final String DETAIL         = "Descriptor returns no target descriptors.";
          throw Utils.logProgrammingError(CLASSNAME, THIS_METHOD, SUBJECT_CLASS, SUBJECT_METHOD, DETAIL);
       }
 
       // Loop over all TargetDescriptors
       boolean shouldContinue = true;
       while (shouldContinue) {
 
          // Get a reference to the next TargetDescriptor
          TargetDescriptor target = (TargetDescriptor) iterator.next();
 
          // Call using this target
          Log.log_1301(target.getURL());
          Object result = null;
          boolean succeeded = false;
          long start = System.currentTimeMillis();
          try {
 
             // Attempt the call
             result = doCallImpl(request, callConfig, target);
             succeeded = true;
 
          // If the call to the target fails, store the exception and try the next
          } catch (Throwable exception) {
 
             Log.log_1302(target.getURL());
 
             long duration = System.currentTimeMillis() - start;
 
             // If the caught exception is not a CallException, then
             // encapsulate it in one
             CallException currentException;
             if (exception instanceof CallException) {
                currentException = (CallException) exception;
             } else if (exception instanceof MethodNotImplementedError) {
                final String SUBJECT_METHOD = "doCallImpl(" + CallRequest.class.getName() + ',' + CallConfig.class.getName() + ',' + TargetDescriptor.class.getName() + ')';
                final String DETAIL         = "The method " + SUBJECT_METHOD + " is not implemented although it should be.";
                throw Utils.logProgrammingError(CLASSNAME, THIS_METHOD, _className, SUBJECT_METHOD, DETAIL);
             } else {
                currentException = new UnexpectedExceptionCallException(request, target, duration, null, exception);
             }
 
             // Link the previous exception (if there is one) to this one
             if (lastException != null) {
                lastException.setNext(currentException);
             }
 
             // Now set this exception as the most recent CallException
             lastException = currentException;
 
             // If this is the first exception being caught, then lazily
             // initialize the CallExceptionList and keep a reference to the
             // first exception
             if (exceptions == null) {
                exceptions = new CallExceptionList();
             }
 
             // Store the failure
             exceptions.add(currentException);
 
             // Determine whether fail-over is allowed and whether we have
             // another target to fail-over to
             boolean failOver = shouldFailOver(request, callConfig, exceptions);
             boolean haveNext = iterator.hasNext();
 
             // No more targets and no fail-over
             if (!haveNext && !failOver) {
                Log.log_1304();
                shouldContinue = false;
 
             // No more targets but fail-over would be allowed
             } else if (!haveNext) {
                Log.log_1305();
                shouldContinue = false;
 
             // More targets available but fail-over is not allowed
             } else if (!failOver) {
                Log.log_1306();
                shouldContinue = false;
 
             // More targets available and fail-over is allowed
             } else {
                Log.log_1307();
                shouldContinue = true;
             }
          }
 
          // The call succeeded
          if (succeeded) {
             long duration = System.currentTimeMillis() - start;
 
             // TRACE: Leave method
             Log.log_1005(CLASSNAME, THIS_METHOD, null);
 
             return createCallResult(request, target, duration, exceptions, result);
          }
       }
 
       // Loop ended, call failed completely
       Log.log_1303();
 
       // Get the first exception from the list, this one should be thrown
       CallException first = exceptions.get(0);
 
       // TRACE: Leave method with exception
       Log.log_1004(first, CLASSNAME, THIS_METHOD, null);
 
       throw first;
    }
 
    /**
     * Attempts to execute the specified call request on one of the target
     * services. During the execution,
     * {@link TargetDescriptor Target descriptors} will be picked and passed
     * to {@link #doCallImpl(CallRequest,TargetDescriptor)} until there is one
     * that succeeds, as long as fail-over can be done (according to
     * {@link #shouldFailOver(CallRequest,Throwable)}).
     *
     * <p>If one of the calls succeeds, then the result is returned. If
     * none succeeds or if fail-over should not be done, then a
     * {@link CallException} is thrown.
     *
     * <p>Each call attempt consists of a call to
     * {@link #doCallImpl(CallRequest,TargetDescriptor)}.
     *
     * @param request
     *    the call request, not <code>null</code>.
     *
     * @return
     *    a combination of the call result and a link to the
     *    {@link TargetDescriptor target} that returned this result, if and
     *    only if one of the calls succeeded, could be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>request == null</code>.
     *
     * @throws CallException
     *    if all call attempts failed.
     *
     * @deprecated
     *    Deprecated since XINS 1.1.0.
     *    Use {@link #doCall(CallRequest,CallConfig)} instead. Although marked
     *    as deprecated, this method still works the same as in XINS 1.0.x.
     *    This method is guaranteed not to be removed before XINS 2.0.0.
     */
    protected final CallResult doCall(CallRequest request)
    throws IllegalArgumentException, CallException {
 
       final String THIS_METHOD = "doCall(" + CallRequest.class.getName() + ')';
 
       // TRACE: Enter method
       Log.log_1003(CLASSNAME, THIS_METHOD, null);
 
       // This method should only be called if the subclass uses the old style
       if (_newStyle) {
          final String SUBJECT_CLASS  = Utils.getCallingClass();
          final String SUBJECT_METHOD = Utils.getCallingMethod();
          final String DETAIL         = "Method " + THIS_METHOD + " called while class " + _className + " uses new-style (XINS 1.1) constructor.";
          throw Utils.logProgrammingError(CLASSNAME, THIS_METHOD, SUBJECT_CLASS, SUBJECT_METHOD, DETAIL);
       }
 
       // Check preconditions
       MandatoryArgumentChecker.check("request", request);
 
       // Keep a reference to the most recent CallException since
       // setNext(CallException) needs to be called on it to make it link to
       // the next one (if there is one)
       CallException lastException = null;
 
       // Maintain the list of CallExceptions
       //
       // This is needed if a successful result (a CallResult object) is
       // returned, since it will contain references to the exceptions as well;
       //
       // Note that this object is lazily initialized because this code is
       // performance- and memory-optimized for the successful case
       CallExceptionList exceptions = null;
 
       // Iterate over all targets
       Iterator iterator = _descriptor.iterateTargets();
 
       // There should be at least one target
       if (! iterator.hasNext()) {
          final String SUBJECT_CLASS  = _descriptor.getClass().getName();
          final String SUBJECT_METHOD = "iterateTargets()";
          final String DETAIL         = "Descriptor returns no target descriptors.";
          throw Utils.logProgrammingError(CLASSNAME, THIS_METHOD, SUBJECT_CLASS, SUBJECT_METHOD, DETAIL);
       }
 
       // Loop over all TargetDescriptors
       boolean shouldContinue = true;
       while (shouldContinue) {
 
          // Get a reference to the next TargetDescriptor
          TargetDescriptor target = (TargetDescriptor) iterator.next();
 
          // Call using this target
          Log.log_1301(target.getURL());
          Object result = null;
          boolean succeeded = false;
          long start = System.currentTimeMillis();
          try {
 
             // Attempt the call
             result = doCallImpl(request, target);
             succeeded = true;
 
          // If the call to the target fails, store the exception and try the next
          } catch (Throwable exception) {
 
             Log.log_1302(target.getURL());
 
             long duration = System.currentTimeMillis() - start;
 
             // If the caught exception is not a CallException, then
             // encapsulate it in one
             CallException currentException;
             if (exception instanceof CallException) {
                currentException = (CallException) exception;
             } else if (exception instanceof MethodNotImplementedError) {
                // TODO: Detail message should be reviewed
                final String SUBJECT_METHOD = "doCallImpl(" + CallRequest.class.getName() + ',' + CallConfig.class.getName() + ',' + TargetDescriptor.class.getName() + ')';
                final String DETAIL         = "The method is not implemented although it should be.";
                throw Utils.logProgrammingError(CLASSNAME, THIS_METHOD, _className, SUBJECT_METHOD, DETAIL);
             } else {
                currentException = new UnexpectedExceptionCallException(request, target, duration, null, exception);
             }
 
             // Link the previous exception (if there is one) to this one
             if (lastException != null) {
                lastException.setNext(currentException);
             }
 
             // Now set this exception as the most recent CallException
             lastException = currentException;
 
             // If this is the first exception being caught, then lazily
             // initialize the CallExceptionList and keep a reference to the
             // first exception
             if (exceptions == null) {
                exceptions = new CallExceptionList();
             }
 
             // Store the failure
             exceptions.add(currentException);
 
             // Determine whether fail-over is allowed and whether we have
             // another target to fail-over to
             boolean failOver = shouldFailOver(request, exception);
             boolean haveNext = iterator.hasNext();
 
             // No more targets and no fail-over
             if (!haveNext && !failOver) {
                Log.log_1304();
                shouldContinue = false;
 
             // No more targets but fail-over would be allowed
             } else if (!haveNext) {
                Log.log_1305();
                shouldContinue = false;
 
             // More targets available but fail-over is not allowed
             } else if (!failOver) {
                Log.log_1306();
                shouldContinue = false;
 
             // More targets available and fail-over is allowed
             } else {
                Log.log_1307();
                shouldContinue = true;
             }
          }
 
          // The call succeeded
          if (succeeded) {
             long duration = System.currentTimeMillis() - start;
 
             // TRACE: Leave method
             Log.log_1005(CLASSNAME, THIS_METHOD, null);
 
             return createCallResult(request, target, duration, exceptions, result);
          }
       }
 
       // Loop ended, call failed completely
       Log.log_1303();
 
       // Get the first exception from the list, this one should be thrown
       CallException first = exceptions.get(0);
 
       // TRACE: Leave method with exception
       Log.log_1004(first, CLASSNAME, THIS_METHOD, null);
 
       throw first;
    }
 
    /**
     * Calls the specified target using the specified subject. This method must
     * be implemented by subclasses. It is called as soon as a target is
     * selected to be called. If the call fails, then a {@link CallException}
     * should be thrown. If the call succeeds, then the call result should be
     * returned from this method.
     *
     * <p>Subclasses that want to use {@link #doCall(CallRequest,CallConfig)}
     * <em>must</em> implement this method.
     *
     * @param request
     *    the call request to be executed, never <code>null</code>.
     *
     * @param callConfig
     *    the call config to be used, never <code>null</code>; this is
     *    determined by {@link #doCall(CallRequest,CallConfig)} and is
     *    guaranteed not to be <code>null</code>.
     *
     * @param target
     *    the target to call, cannot be <code>null</code>.
     *
     * @return
     *    the result, if and only if the call succeeded, could be
     *    <code>null</code>.
     *
     * @throws ClassCastException
     *    if the specified <code>request</code> object is not <code>null</code>
     *    and not an instance of an expected subclass of class
     *    {@link CallRequest}.
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
       throw new MethodNotImplementedError();
    }
 
    /**
     * Calls the specified target using the specified subject. This method must
     * be implemented by subclasses. It is called as soon as a target is
     * selected to be called. If the call fails, then a {@link CallException}
     * should be thrown. If the call succeeds, then the call result should be
     * returned from this method.
     *
     * @param request
     *    the call request to be executed, cannot be <code>null</code>.
     *
     * @param target
     *    the target to call, cannot be <code>null</code>.
     *
     * @return
     *    the result, if and only if the call succeeded, could be
     *    <code>null</code>.
     *
     * @throws ClassCastException
     *    if the specified <code>request</code> object is not <code>null</code>
     *    and not an instance of an expected subclass of class
     *    {@link CallRequest}.
     *
     * @throws IllegalArgumentException
     *    if <code>target == null || request == null</code>.
     *
     * @throws CallException
     *    if the call to the specified target failed.
     *
     * @deprecated
     *    Deprecated since XINS 1.1.0. Implement
     *    {@link #doCallImpl(CallRequest,CallConfig,TargetDescriptor)} instead
     *    of this method. Although deprecated, this method still works the same
     *    as in XINS 1.0.x.
     *    This method is guaranteed not to be removed before XINS 2.0.0.
     */
    protected Object doCallImpl(CallRequest      request,
                                TargetDescriptor target)
    throws ClassCastException, IllegalArgumentException, CallException {
       throw new MethodNotImplementedError();
    }
 
    /**
     * Constructs an appropriate <code>CallResult</code> object for a
     * successful call attempt. This method is called from
     * {@link #doCall(CallRequest)}.
     *
     * @param request
     *    the {@link CallRequest} that was to be executed, never
     *    <code>null</code> when called from {@link #doCall(CallRequest)}.
     *
     * @param succeededTarget
     *    the {@link TargetDescriptor} for the service that was successfully
     *    called, never <code>null</code> when called from
     *    {@link #doCall(CallRequest)}.
     *
     * @param duration
     *    the call duration in milliseconds, guaranteed to be a non-negative
     *    number when called from {@link #doCall(CallRequest)}.
     *
     * @param exceptions
     *    the list of {@link CallException} instances, or <code>null</code> if
     *    there were no call failures.
     *
     * @param result
     *    the result from the call, which is the object returned by
     *    {@link #doCallImpl(CallRequest,TargetDescriptor)}, can be
     *    <code>null</code>.
     *
     * @return
     *    a {@link CallResult} instance, never <code>null</code>.
     *
     * @throws ClassCastException
     *    if <code>request</code> and/or <code>result</code> are not of the
     *    correct class.
     */
    protected abstract CallResult createCallResult(CallRequest       request,
                                                   TargetDescriptor  succeededTarget,
                                                   long              duration,
                                                   CallExceptionList exceptions,
                                                   Object            result)
    throws ClassCastException;
 
    /**
     * Runs the specified task. If the task does not finish within the total
     * time-out period, then the thread executing it is interrupted using the
     * {@link Thread#interrupt()} method and a {@link TimeOutException} is
     * thrown.
     *
     * @param task
     *    the task to run, cannot be <code>null</code>.
     *
     * @param descriptor
     *    the descriptor for the target on which the task is executed, cannot
     *    be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>task == null || descriptor == null</code>.
     *
     * @throws IllegalThreadStateException
     *    if <code>descriptor.getTotalTimeOut() &gt; 0</code> and the task is a
     *    {@link Thread} which is already started.
     *
     * @throws SecurityException
     *    if the task did not finish within the total time-out period, but the
     *    interruption of the thread was disallowed (see
     *    {@link Thread#interrupt()}).
     *
     * @throws TimeOutException
     *    if the task did not finish within the total time-out period and was
     *    interrupted.
     */
    protected final void controlTimeOut(Runnable         task,
                                        TargetDescriptor descriptor)
    throws IllegalArgumentException,
           IllegalThreadStateException,
           SecurityException,
           TimeOutException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("task",       task,
                                      "descriptor", descriptor);
 
       // Determine the total time-out
       int totalTimeOut = descriptor.getTotalTimeOut();
 
       // If there is no total time-out, then execute the task on this thread
       if (totalTimeOut < 1) {
          task.run();
 
       // Otherwise a time-out controller will be used
       } else {
          TimeOutController.execute(task, totalTimeOut);
       }
    }
 
    /**
     * Determines whether a call should fail-over to the next selected target.
     * This method should only be called from {@link #doCall(CallRequest)}.
     *
     * <p>This method is typically overridden by subclasses. Usually, a
     * subclass first calls this method in the superclass, and if that returns
     * <code>false</code> it does some additional checks, otherwise
     * <code>true</code> is immediately returned.
     *
     * <p>The implementation of this method in class {@link ServiceCaller}
     * returns <code>true</code> if and only if <code>exception instanceof
     * {@link ConnectionCallException}</code>.
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
     * @deprecated
     *    Deprecated since XINS 1.1.0. Implement
     *    {@link #shouldFailOver(CallRequest,CallConfig,CallExceptionList)}
     *    instead of this method.
     *    This method is guaranteed not to be removed before XINS 2.0.0.
     */
    protected boolean shouldFailOver(CallRequest request,
                                     Throwable   exception) {
 
       if (_newStyle) {
          throw new MethodNotImplementedError();
       }
 
       final String THIS_METHOD = "shouldFailOver(CallRequest,Throwable)";
 
       // TRACE: Enter method
       Log.log_1003(CLASSNAME, THIS_METHOD, null);
 
       // Determine if fail-over is applicable
       boolean should = (exception instanceof ConnectionCallException);
 
       // TRACE: Leave method
       Log.log_1005(CLASSNAME, THIS_METHOD, null);
 
       return should;
    }
 
    /**
     * Determines whether a call should fail-over to the next selected target
     * based on a request, call configuration and exception list.
     * This method should only be called from
     * {@link #doCall(CallRequest,CallConfig)}.
     *
     * <p>This method is typically overridden by subclasses. Usually, a
     * subclass first calls this method in the superclass, and if that returns
     * <code>false</code> it does some additional checks, otherwise
     * <code>true</code> is immediately returned.
     *
     * <p>The implementation of this method in class {@link ServiceCaller}
     * returns <code>true</code> if and only if
     * <code>callConfig.{@link CallConfig#isFailOverAllowed() isFailOverAllowed()} || exception instanceof {@link ConnectionCallException}</code>.
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
     *
     * @since XINS 1.1.0
     */
    protected boolean shouldFailOver(CallRequest       request,
                                     CallConfig        callConfig,
                                     CallExceptionList exceptions) {
 
       final String THIS_METHOD = "shouldFailOver(CallRequest,CallConfig,CallExceptionList)";
 
       // TRACE: Enter method
       Log.log_1003(CLASSNAME, THIS_METHOD, null);
 
       // This method should only be called if the subclass uses the new style
       if (! _newStyle) {
          final String SUBJECT_CLASS  = Utils.getCallingClass();
          final String SUBJECT_METHOD = Utils.getCallingMethod();
          final String DETAIL = "Method " + THIS_METHOD + " called while class " + _className + " uses old-style (XINS 1.0) constructor.";
          throw Utils.logProgrammingError(CLASSNAME, THIS_METHOD, SUBJECT_CLASS, SUBJECT_METHOD, DETAIL);
       }
 
       // Determine if fail-over is applicable
       boolean should = callConfig.isFailOverAllowed()
           || (exceptions.last() instanceof ConnectionCallException);
 
       // TRACE: Leave method
       Log.log_1005(CLASSNAME, THIS_METHOD, null);
 
       return should;
    }
 
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * Error used to indicate a method should be implemented in a subclass, but
     * is not.
     *
     * @version $Revision$ $Date$
     * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
     */
    private static final class MethodNotImplementedError extends Error {
 
       //----------------------------------------------------------------------
       // Constructors
       //----------------------------------------------------------------------
 
       /**
        * Constructs a new <code>MethodNotImplementedError</code>.
        */
       private MethodNotImplementedError() {
          // empty
       }
 
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
 
    }
 }
