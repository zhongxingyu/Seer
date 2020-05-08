 /*
  * $Id$
  */
 package org.xins.server;
 
 import java.io.PrintWriter;
 import javax.servlet.ServletRequest;
 import org.apache.log4j.Logger;
 import org.xins.types.TypeValueException;
 import org.xins.util.MandatoryArgumentChecker;
 import org.xins.util.collections.BasicPropertyReader;
 import org.xins.util.collections.PropertyReader;
 import org.xins.util.manageable.Manageable;
 import org.xins.util.io.FastStringWriter;
 import org.xins.util.text.FastStringBuffer;
 
 /**
  * Base class for function implementation classes.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
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
    private static final CallResult DISABLED_FUNCTION_RESULT = new BasicCallResult(false, "DisabledFunction", null, null);
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new session-less <code>Function</code>.
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
       this(api, name, version, false);
    }
 
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
     * @param sessionBased
     *    flag that indicates if this function is session-based
     *    (if <code>true</code>) or session-less (if <code>false</code>).
     *
     * @throws IllegalArgumentException
     *    if <code>api == null || name == null || version == null</code>.
     */
    protected Function(API api, String name, String version, boolean sessionBased)
    throws IllegalArgumentException {
 
       // Check arguments
       MandatoryArgumentChecker.check("api", api, "name", name, "version", version);
 
       _log          = Logger.getLogger("org.xins.server.apis." + api.getName() + '.' + name);
       _api          = api;
       _name         = name;
       _version      = version;
       _sessionBased = sessionBased;
       _enabled      = true;
 
       _api.functionAdded(this);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The logger used by this function. This field is initialized by the
     * constructor and set to a non-<code>null</code> value.
     */
    private final Logger _log;
 
    /**
     * The API implementation this function is part of.
     */
    protected final API _api;
 
    /**
     * The name of this function.
     */
    private final String _name;
 
    /**
     * The version of the specification this function implements.
     */
    private final String _version;
 
    /**
     * Flag that indicates if this function is session-based.
     */
    private final boolean _sessionBased;
 
    /**
     * Flag that indicates if this function is currently accessible.
     */
    private boolean _enabled;
 
    /**
     * Lock object for <code>_callCount</code>.
     */
    private final Object _callCountLock = new Object();
 
    /**
     * The total number of calls executed up until now.
     */
    private int _callCount;
 
    /**
     * Statistics object linked to this function.
     */
   private final Statistics _statistics = new Statistics();
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Returns the logger associated with this function.
     *
     * @return
     *    the associated logger, constant, and cannot be <code>null</code>.
     *
     * @deprecated
     *    Deprecated since XINS 0.158 with no replacement. Instead of using a
     *    <code>Logger</code>, use the <em>logdoc</em> approach. Although this
     *    method is deprecated, it still works as specified.
     */
    final Logger getLogger() {
       return _log;
    }
 
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
     * Checks if this function is session-based or not.
     *
     * @return
     *    <code>true</code> if this function is session-based,
     *    <code>false</code> if this function is session-less.
     *
     * @since XINS 0.52
     */
    final boolean isSessionBased() {
       return _sessionBased;
    }
 
    /**
     * Checks if this function is currently accessible.
     *
     * @return
     *    <code>true</code> if this function is currently accessible,
     *    <code>false</code> otherwise.
     *
     * @since XINS 0.139
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
     *
     * @since XINS 0.139
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
   final Statistics getStatistics() {
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
       synchronized (_callCountLock) {
          return _callCount++;
       }
    }
 
    /**
     * Handles a call to this function (wrapper method). This method will call
     * {@link #handleCall(CallContext context)}.
     *
     * @param start
     *    the start time of the call, as milliseconds since midnight January 1,
     *    1970.
     *
     * @param request
     *    the original servlet request for this call, never <code>null</code>.
     *
     * @return
     *    the call result, never <code>null</code>.
     */
    CallResult handleCall(long start, ServletRequest request) {
 
       // Assign a call ID
       int callID = assignCallID();
 
       // Check if this function is enabled
       if (_enabled == false) {
          performedCall(start, callID, null, false, "DisabledFunction");
          return DISABLED_FUNCTION_RESULT;
       }
 
       // Determine the session identifier
       Session session;
       if (!isSessionBased()) {
          session = null;
       } else {
          String sessionID = request.getParameter("_session");
          if (sessionID == null || sessionID.length() == 0) {
             // TODO: Cache CallResult and use ResultCode
             performedCall(start, callID, null, false, "MissingSessionID");
             return new BasicCallResult(false, "MissingSessionID", null, null);
          } else {
             try {
                session = _api.getSessionByString(sessionID);
             } catch (TypeValueException exception) {
                Log.log_5011(_name, callID, sessionID);
                // TODO: Cache CallResult and use ResultCode
                performedCall(start, callID, null, false, "InvalidSessionID");
                return new BasicCallResult(false, "InvalidSessionID", null, null);
             }
             if (session == null) {
                Log.log_5012(_name, callID, sessionID);
                // TODO: Cache CallResult and use ResultCode
                performedCall(start, callID, null, false, "UnknownSessionID");
                return new BasicCallResult(false, "UnknownSessionID", null, null);
             }
          }
       }
 
       // Construct a CallContext object
       CallContext context = new CallContext(request, start, this, callID, session);
 
       CallResult result;
       try {
 
          handleCall(context);
          result = context.getCallResult();
 
       } catch (Throwable exception) {
 
          String exceptionClass = exception.getClass().getName();
          String exceptionMessage = exception.getMessage();
 
          // TODO: Allow customization of what exceptions are logged?
          // TODO: Log stack trace (5013)
          Log.log_5013(_name, callID, exceptionClass, exceptionMessage);
 
          // Create a set of parameters for the result
          BasicPropertyReader parameters = new BasicPropertyReader();
 
          // Add the exception class
          parameters.set("_exception.class", exceptionClass);
 
          // Add the exception message, if any
          if (exceptionMessage != null && exceptionMessage.length() > 0) {
             parameters.set("_exception.message", exceptionMessage);
          }
 
          // Add the stack trace, if any
          FastStringWriter stWriter = new FastStringWriter();
          PrintWriter printWriter = new PrintWriter(stWriter);
          exception.printStackTrace(printWriter);
          String stackTrace = stWriter.toString();
          if (stackTrace != null && stackTrace.length() > 0) {
             parameters.set("_exception.stacktrace", stackTrace);
          }
 
          result = new BasicCallResult(false, "InternalError", parameters, null);
       }
 
       // Update function statistics
       performedCall(start, callID, session, result.isSuccess(), result.getCode());
 
       return result;
    }
 
    /**
     * Handles a call to this function.
     *
     * @param context
     *    the context for this call, never <code>null</code>.
     *
     * @throws Throwable
     *    if anything goes wrong.
     */
    protected abstract void handleCall(CallContext context)
    throws Throwable;
 
    /**
     * Callback method that may be called after a call to this function. This
     * method will store statistics-related information.
     *
     * <p />This method does not <em>have</em> to be called. If statistics
     * gathering is disabled, then this method should not be called.
     *
     * @param start
     *    the start time, in milliseconds since January 1, 1970, not
     *    <code>null</code>.
     *
     * @param callID
     *    the assigned call ID.
     *
     * @param session
     *    the session, if and only if this function is session-based, otherwise
     *    <code>null</code>.
     *
     * @param success
     *    indication if the call was successful.
     *
     * @param code
     *    the function result code, or <code>null</code>.
     */
    private final void performedCall(long start, int callID, Session session, boolean success, String code) {
 
       // TODO: Accept ResultCode
 
       String message = null;
 
       // TODO: If the Logging is moved somewhere else the  
       // The method invoking this method (performedCall) can directly
       // invoke recordCall and this method can be removed.
       _statistics.recordCall(start, success);
       
       long duration = _statistics.getLastDuration();
       
       // Call succeeded
       if (success) {
          if (session == null) {
             Log.log_5014(_name, callID, duration);
          } else {
             Log.log_5015(_name, callID, duration, session);
          }
       // Call failed
       } else {
          if (session == null) {
             Log.log_5016(_name, callID, duration, code);
          } else {
             Log.log_5017(_name, callID, duration, code, session);
          }
       }
    }
 }
