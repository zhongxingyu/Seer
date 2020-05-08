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
 import org.xins.util.io.FastStringWriter;
 import org.xins.util.text.FastStringBuffer;
 
 /**
  * Base class for function implementation classes.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  */
 public abstract class Function
 extends Object
 implements DefaultResultCodes {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
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
 
       // Check argument
       MandatoryArgumentChecker.check("api", api, "name", name, "version", version);
 
       _log          = Logger.getLogger(getClass().getName());
       _api          = api;
       _name         = name;
       _version      = version;
       _sessionBased = sessionBased;
 
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
 
    /**
     * Lock object for a successful call.
     */
    private final Object _successfulCallLock = new Object();
 
    /**
     * Lock object for an unsuccessful call.
     */
    private final Object _unsuccessfulCallLock = new Object();
 
    /**
     * The number of successful calls executed up until now.
     */
    private int _successfulCalls;
 
    /**
     * The number of unsuccessful calls executed up until now.
     */
    private int _unsuccessfulCalls;
 
    /**
     * The start time of the most recent successful call.
     */
    private long _lastSuccessfulStart;
 
    /**
     * The start time of the most recent unsuccessful call.
     */
    private long _lastUnsuccessfulStart;
 
    /**
     * The duration of the most recent successful call.
     */
    private long _lastSuccessfulDuration;
 
    /**
     * The duration of the most recent unsuccessful call.
     */
    private long _lastUnsuccessfulDuration;
 
    /**
     * The total duration of all successful calls up until now.
     */
    private long _successfulDuration;
 
    /**
     * The total duration of all unsuccessful calls up until now.
     */
    private long _unsuccessfulDuration;
 
    /**
     * The minimum time a successful call took.
     */
    private long _successfulMin = Long.MAX_VALUE;
 
    /**
     * The minimum time an unsuccessful call took.
     */
    private long _unsuccessfulMin = Long.MAX_VALUE;
 
    /**
     * The start time of the successful call that took the shortest.
     */
    private long _successfulMinStart;
 
    /**
     * The start time of the unsuccessful call that took the shortest.
     */
    private long _unsuccessfulMinStart;
 
    /**
     * The duration of the successful call that took the longest.
     */
    private long _successfulMax;
 
    /**
     * The duration of the unsuccessful call that took the longest.
     */
    private long _unsuccessfulMax;
 
    /**
     * The start time of the successful call that took the longest.
     */
    private long _successfulMaxStart;
 
    /**
     * The start time of the unsuccessful call that took the longest.
     */
    private long _unsuccessfulMaxStart;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Returns the logger associated with this function.
     *
     * @return
     *    the associated logger, constant, and cannot be <code>null</code>.
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
    final API getAPI() {
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
                if (_log.isDebugEnabled()) {
                   FastStringBuffer buffer = new FastStringBuffer(120);
                   buffer.append(CallContext.getLogPrefix(_name, callID));
                   buffer.append("Invalid value for session ID type: \"");
                   buffer.append(sessionID);
                   buffer.append("\".");
                   _log.debug(buffer.toString());
                }
                // TODO: Cache CallResult and use ResultCode
                performedCall(start, callID, null, false, "InvalidSessionID");
                return new BasicCallResult(false, "InvalidSessionID", null, null);
             }
             if (session == null) {
                if (_log.isDebugEnabled()) {
                   FastStringBuffer buffer = new FastStringBuffer(120);
                   buffer.append(CallContext.getLogPrefix(_name, callID));
                   buffer.append("Unknown session ID: \"");
                   buffer.append(sessionID);
                   buffer.append("\".");
                   _log.debug(buffer.toString());
                }
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
 
          // TODO: Allow customization of what exceptions are logged?
          _log.error("Caught exception while calling API.", exception);
 
          // Create a set of parameters for the result
          BasicPropertyReader parameters = new BasicPropertyReader();
 
          // Add the exception class
          parameters.set("_exception.class", exception.getClass().getName());
 
          // Add the exception message, if any
          String message = exception.getMessage();
          if (message != null && message.length() > 0) {
             parameters.set("_exception.message", message);
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
 
       long duration = System.currentTimeMillis() - start;
       boolean debugEnabled = _log.isDebugEnabled();
       String message = null;
       if (success) {
          if (debugEnabled) {
             FastStringBuffer buffer = new FastStringBuffer(250);
             if (session != null) {
                buffer.append("Call ");
                buffer.append(_name);
                buffer.append(':');
                buffer.append(callID);
                buffer.append(" succeeded for session ");
                buffer.append(session.toString());
                buffer.append(". Duration: ");
             } else {
                buffer.append("Call ");
                buffer.append(_name);
                buffer.append(':');
                buffer.append(callID);
                buffer.append(" succeeded. Duration: ");
             }
             buffer.append(String.valueOf(duration));
             buffer.append(" ms.");
             if (code != null) {
                buffer.append(" Code: \"");
                buffer.append(code);
                buffer.append("\".");
             }
             message = buffer.toString();
          }
 
          synchronized (_successfulCallLock) {
             _lastSuccessfulStart    = start;
             _lastSuccessfulDuration = duration;
             _successfulCalls++;
             _successfulDuration += duration;
             _successfulMin      = _successfulMin > duration ? duration : _successfulMin;
             _successfulMax      = _successfulMax < duration ? duration : _successfulMax;
             _successfulMinStart = (_successfulMin == duration) ? start : _successfulMinStart;
             _successfulMaxStart = (_successfulMax == duration) ? start : _successfulMaxStart;
          }
       } else {
          if (debugEnabled) {
             FastStringBuffer buffer = new FastStringBuffer(250);
             buffer.clear();
             buffer.append("Call ");
             buffer.append(_name);
             buffer.append(':');
             buffer.append(callID);
             buffer.append(" failed. Duration: ");
             buffer.append(String.valueOf(duration));
             buffer.append(" ms.");
             if (code != null) {
                buffer.append(" Code: \"");
                buffer.append(code);
                buffer.append("\".");
             }
             message = buffer.toString();
          }
 
          synchronized (_unsuccessfulCallLock) {
             _lastUnsuccessfulStart    = start;
             _lastUnsuccessfulDuration = duration;
             _unsuccessfulCalls++;
             _unsuccessfulDuration += duration;
             _unsuccessfulMin = _unsuccessfulMin > duration ? duration : _unsuccessfulMin;
             _unsuccessfulMax = _unsuccessfulMax < duration ? duration : _unsuccessfulMax;
             _unsuccessfulMinStart = (_unsuccessfulMin == duration) ? start : _unsuccessfulMinStart;
             _unsuccessfulMaxStart = (_unsuccessfulMax == duration) ? start : _unsuccessfulMaxStart;
          }
       }
 
       if (debugEnabled) {
          _log.debug(message);
       }
    }
 
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * Call statistics pertaining to a certain function.
     *
     * @version $Revision$ $Date$
     * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
     */
    final class Statistics extends Object {
 
       //----------------------------------------------------------------------
       // Constructors
       //----------------------------------------------------------------------
 
       /**
        * Constructs a new <code>Statistics</code> object.
        */
       private Statistics() {
          // empty
       }
 
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
 
       /**
        * Returns the number of successful calls executed up until now.
        *
        * @return
        *    the number of successful calls executed up until now.
        */
       public int getSuccessfulCalls() {
          return _successfulCalls;
       }
 
       /**
        * Returns the number of unsuccessful calls executed up until now.
        *
        * @return
        *    the number of unsuccessful calls executed up until now.
        */
       public int getUnsuccessfulCalls() {
          return _unsuccessfulCalls;
       }
 
       /**
        * Returns the start time of the most recent successful call.
        *
        * @return
        *    the start time of the most recent successful call.
        */
       public long getLastSuccessfulStart() {
          return _lastSuccessfulStart;
       }
 
       /**
        * Returns the start time of the most recent unsuccessful call.
        *
        * @return
        *    the start time of the most recent unsuccessful call.
        */
       public long getLastUnsuccessfulStart() {
          return _lastUnsuccessfulStart;
       }
 
       /**
        * Returns the duration of the most recent successful call.
        *
        * @return
        *    the duration of the most recent successful call.
        */
       public long getLastSuccessfulDuration() {
          return _lastSuccessfulDuration;
       }
 
       /**
        * Returns the duration of the most recent unsuccessful call.
        *
        * @return
        *    the duration of the most recent unsuccessful call.
        */
       public long getLastUnsuccessfulDuration() {
          return _lastUnsuccessfulDuration;
       }
 
       /**
        * Returns the total duration of all successful calls up until now.
        *
        * @return
        *    the total duration of all successful calls up until now.
        */
       public long getSuccessfulDuration() {
          return _successfulDuration;
       }
 
       /**
        * Returns the total duration of all unsuccessful calls up until now.
        *
        * @return
        *    the total duration of all unsuccessful calls up until now.
        */
       public long getUnsuccessfulDuration() {
          return _unsuccessfulDuration;
       }
 
       /**
        * Returns the minimum time a successful call took.
        *
        * @return
        *    the minimum time a successful call took.
        */
       public long getSuccessfulMin() {
          return _successfulMin;
       }
 
       /**
        * Returns the start time of the successful call that took the shortest.
        *
        * @return
        *    the start time of the successful call that took the shortest.
        */
       public long getSuccessfulMinStart() {
          return _successfulMinStart;
       }
 
       /**
        * Returns the minimum time an unsuccessful call took.
        *
        * @return
        *    the minimum time an unsuccessful call took.
        */
       public long getUnsuccessfulMin() {
          return _unsuccessfulMin;
       }
 
       /**
        * Returns the start time of the unsuccessful call that took the shortest.
        *
        * @return
        *    the start time of the unsuccessful call that took the shortest,
        *    always &gt;= 0.
        */
       public long getUnsuccessfulMinStart() {
          return _unsuccessfulMinStart;
       }
 
       // TODO: Have a similar description for all these getters
 
       /**
        * Returns the duration of the successful call that took the longest.
        *
        * @return
        *    the duration of the successful call that took the longest, always
        *    &gt;= 0.
        */
       public long getSuccessfulMax() {
          return _successfulMax;
       }
 
       /**
        * Returns the start time of the most recent successful call that took
        * the longest.
        *
        * @return
        *    the start time of the most recent successful call that took the
        *    longest, always &gt;= 0.
        */
       public long getSuccessfulMaxStart() {
          return _successfulMaxStart;
       }
 
       /**
        * Returns the duration of the unsuccessful call that took the longest.
        *
        * @return
        *    the duration of the unsuccessful call that took the longest,
        *    always &gt;= 0.
        */
       public long getUnsuccessfulMax() {
          return _unsuccessfulMax;
       }
 
       /**
        * Returns the start time of the most recent unsuccessful call that took
        * the longest.
        *
        * @return
        *    the start time of the most recent unsuccessful call that took the
        *    longest, always &gt;= 0.
        */
       public long getUnsuccessfulMaxStart() {
          return _unsuccessfulMaxStart;
       }
    }
 }
