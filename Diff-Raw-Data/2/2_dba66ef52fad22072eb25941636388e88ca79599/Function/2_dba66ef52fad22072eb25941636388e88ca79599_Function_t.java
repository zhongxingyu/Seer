 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.manageable.Manageable;
 
 import org.xins.logdoc.LogdocSerializable;
 
 /**
  * Base class for function implementation classes.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 1.0.0
  */
 public abstract class Function
 extends Manageable
 implements DefaultResultCodes {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * Call result to be returned when a function is currently disabled. See
     * {@link #isEnabled()}.
     */
    private static final FunctionResult DISABLED_FUNCTION_RESULT = new FunctionResult("_DisabledFunction");
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>Function</code>.
     *
     * @param api
     *    the API to which this function belongs, not <code>null</code>.
     *
     * @param name
     *    the name, not <code>null</code>.
     *
     * @param version
     *    the version of the specification this function implements, not
     *    <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>api == null || name == null || version == null</code>.
     */
    protected Function(API api, String name, String version)
    throws IllegalArgumentException {
 
       // Check arguments
       MandatoryArgumentChecker.check("api",     api,
                                      "name",    name,
                                      "version", version);
 
       // Initialize fields
       _statistics   = new FunctionStatistics();
       _api          = api;
       _name         = name;
       _version      = version;
       _enabled      = true;
 
       // Notify the API that a Function has been added
       _api.functionAdded(this);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The API implementation this function is part of. This field cannot be
     * <code>null</code>.
     */
    private final API _api;
 
    /**
     * The name of this function. This field cannot be <code>null</code>.
     */
    private final String _name;
 
    /**
     * The version of the specification this function implements. This field
     * cannot be <code>null</code>.
     */
    private final String _version;
 
    /**
     * Flag that indicates if this function is currently accessible.
     */
    private boolean _enabled;
 
    /**
     * Lock object for <code>_callCount</code>. This field cannot be
     * <code>null</code>.
     */
    private final Object _callCountLock = new Object();
 
    /**
     * The total number of calls executed up until now.
     */
    private int _callCount;
 
    /**
     * Statistics object linked to this function. This field cannot be
     * <code>null</code>.
     */
    private final FunctionStatistics _statistics;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Returns the API that contains this function.
     *
     * @return
     *    the {@link API}, not <code>null</code>.
     */
    public final API getAPI() {
       return _api;
    }
 
    /**
     * Returns the name of this function.
     *
     * @return
     *    the name, not <code>null</code>.
     */
    final String getName() {
       return _name;
    }
 
    /**
     * Returns the specification version for this function.
     *
     * @return
     *    the version, not <code>null</code>.
     */
    final String getVersion() {
       return _version;
    }
 
    /**
     * Checks if this function is currently accessible.
     *
     * @return
     *    <code>true</code> if this function is currently accessible,
     *    <code>false</code> otherwise.
     */
    public final boolean isEnabled() {
       return _enabled;
    }
 
    /**
     * Sets if this function is currently accessible.
     *
     * @param enabled
     *    <code>true</code> if this function should be accessible,
     *    <code>false</code> if not.
     */
    public final void setEnabled(boolean enabled) {
       _enabled = enabled;
    }
 
    /**
     * Returns the call statistics for this function.
     *
     * @return
     *    the statistics, never <code>null</code>.
     */
    final FunctionStatistics getStatistics() {
       return _statistics;
    }
 
    /**
     * Assigns a new call ID for the caller. Every call to this method will
     * return an increasing number.
     *
     * @return
     *    the assigned call ID, &gt;= 0.
     */
    final int assignCallID() {
       int callID;
       synchronized (_callCountLock) {
          callID = _callCount++;
       }
       return callID;
    }
 
    /**
     * Handles a call to this function (wrapper method). This method will call
     * {@link #handleCall(CallContext context)}.
     *
     * @param start
     *    the start time of the call, as milliseconds since midnight January 1,
     *    1970.
     *
     * @param functionRequest
     *    the request, never <code>null</code>.
     *
     * @param ip
     *    the IP address of the requester, never <code>null</code>.
     *
     * @return
     *    the call result, never <code>null</code>.
     *
     * @throws IllegalStateException
     *    if this object is currently not initialized.
     */
    FunctionResult handleCall(long            start,
                              FunctionRequest functionRequest,
                              String          ip)
    throws IllegalStateException {
 
       // Check state first
       assertUsable();
 
       // Assign a call ID
       int callID = assignCallID();
 
       // Check if this function is enabled
       if (! _enabled) {
          performedCall(functionRequest, ip, start, callID,
                        DISABLED_FUNCTION_RESULT);
          return DISABLED_FUNCTION_RESULT;
       }
 
       // Construct a CallContext object
       CallContext context = new CallContext(functionRequest, start, this,
                                             callID, ip);
 
       FunctionResult result;
       try {
 
          // Handle the call
          result = handleCall(context);
 
          // Make sure the result is valid
          InvalidResponseResult invalidResponse = result.checkOutputParameters();
          if (invalidResponse != null) {
             result = invalidResponse;
             Log.log_3501(functionRequest.getFunctionName(), callID);
          }
 
       } catch (Throwable exception) {
          result = _api.handleFunctionException(start, functionRequest, ip,
                                                callID, exception);
      }
 
       // Update function statistics
       // We assume that this method will never throw any exception
       performedCall(functionRequest, ip, start, callID, result);
 
       return result;
    }
 
    /**
     * Handles a call to this function.
     *
     * @param context
     *    the context for this call, never <code>null</code>.
     *
     * @return
     *    the result of the call, never <code>null</code>.
     *
     * @throws Throwable
     *    if anything goes wrong.
     */
    protected abstract FunctionResult handleCall(CallContext context)
    throws Throwable;
 
    /**
     * Callback method that should be called after a call to this function.
     * This method will update the statistics for this funciton and perform
     * transaction logging.
     *
     * <p />This method should <em>never</em> throw any
     * {@link RuntimeException}. If it does, then that should be considered a
     * serious bug.
     *
     * @param functionRequest
     *    the request, should not be <code>null</code>.
     *
     * @param ip
     *    the ip of the requester, should not be <code>null</code>.
     *
     * @param start
     *    the start time, as a number of milliseconds since January 1, 1970.
     *
     * @param callID
     *    the assigned call ID.
     *
     * @param result
     *    the call result, should not be <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>parameters == null || result == null</code>.
     */
    private final void performedCall(FunctionRequest functionRequest,
                                     String          ip,
                                     long            start,
                                     int             callID,
                                     FunctionResult  result)
    throws NullPointerException {
 
       // NOTE: Since XINS 1.0.0-beta11, callID is ignored.
 
       // Get the error code
       String code = result.getErrorCode();
 
       // Update statistics and determine the duration of the call
       boolean isSuccess = code == null;
       long duration = _statistics.recordCall(start, isSuccess, code);
 
       // Fallback is a zero character
       if (code == null) {
          code = "0";
       }
 
       // Serialize the date, input parameters and output parameters
       LogdocSerializable serStart  = new FormattedDate(start);
       LogdocSerializable inParams  = new FormattedParameters(functionRequest.getParameters());
       LogdocSerializable outParams = new FormattedParameters(result.getParameters());
 
       // Perform transaction logging, with and without parameters
       Log.log_3540(serStart, ip, _name, duration, code, inParams, outParams);
       Log.log_3541(serStart, ip, _name, duration, code);
    }
 }
