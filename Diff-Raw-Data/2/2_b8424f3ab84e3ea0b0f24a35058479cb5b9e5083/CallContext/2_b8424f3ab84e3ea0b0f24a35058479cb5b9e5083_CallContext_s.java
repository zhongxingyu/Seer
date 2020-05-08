 /*
  * $Id$
  */
 package org.xins.server;
 
 import javax.servlet.ServletRequest;
 import org.xins.types.TypeValueException;
 import org.xins.util.MandatoryArgumentChecker;
 import org.xins.util.io.FastStringWriter;
 import org.xins.util.text.FastStringBuffer;
 import org.znerd.xmlenc.XMLOutputter;
 import org.apache.commons.logging.Log;
 import org.apache.log4j.Logger;
 import org.apache.log4j.Priority;
 
 /**
  * Context for a function call. Objects of this kind are passed with a
  * function call.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  */
 public final class CallContext
 extends Object
 implements Responder, Log {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The fully-qualified name of this class.
     */
    private static final String FQCN = CallContext.class.getName();
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Returns the text to prefix to all log messages for the specified
     * function and the specified call identifier.
     *
     * @param functionName
     *    the name of the function, should not be <code>null</code>.
     *
     * @param callID
     *    the call identifier.
     *
     * @return
     *    the prefix for log messages, never <code>null</code>.
     *
     * @deprecated
     *    Deprecated since XINS 0.158. Should no longer be useful if logdoc is
     *    used for logging.
     */
    static final String getLogPrefix(String functionName, int callID) {
       FastStringBuffer buffer = new FastStringBuffer(50);
       buffer.append("Call ");
       buffer.append(functionName);
       buffer.append(':');
       buffer.append(callID);
       buffer.append(": ");
       return buffer.toString();
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>CallContext</code> and configures it for the
     * specified servlet request. The state is set to {@link #BEFORE_START}.
     *
     * @param request
     *    the servlet request, should not be <code>null</code>.
     *
     * @param start
     *    the start time of the call, as milliseconds since midnight January 1,
     *    1970.
     *
     * @param function
     *    the concerning function, cannot be <code>null</code>.
     *
     * @param callID
     *    the assigned call ID.
     *
     * @param session
     *    the session, if any, or <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>request == null || function == null</code>.
     */
    CallContext(ServletRequest request, long start, Function function, int callID, Session session)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("request",  request, "function", function);
 
       // Initialize fields
       _request      = request;
       _start        = start;
       _api          = function.getAPI();
       _function     = function;
       _functionName = function.getName();
       _callID       = callID;
       _logger       = function.getLogger();
      _logPrefix    = getLogPrefix(functionName, callID);
       _session      = session;
       _state        = BEFORE_START;
       _builder      = new CallResultBuilder();
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The API for which this CallContext is used. This field is initialized by
     * the constructor and can never be <code>null</code>.
     */
    private final API _api;
 
    /**
     * The original servlet request.
     */
    private final ServletRequest _request;
 
    /**
     * The call result builder. Cannot be <code>null</code>.
     */
    private final CallResultBuilder _builder;
 
    /**
     * The start time of the call, as a number of milliseconds since midnight
     * January 1, 1970 UTC.
     */
    private final long _start;
 
    /**
     * The current state.
     */
    private ResponderState _state;
 
    /**
     * The number of element tags currently open within the data section.
     */
    private int _elementDepth;
 
    /**
     * The name of the function currently being called. Cannot be
     * <code>null</code>.
     */
    private final String _functionName;
 
    /**
     * The function currently being called. Cannot be <code>null</code>.
     */
    private final Function _function;
 
    /**
     * The logger associated with the function. Cannot be <code>null</code>.
     */
    private final Logger _logger;
 
    /**
     * The log prefix for log messages.
     */
    private final String _logPrefix;
 
    /**
     * The session for this call.
     */
    private Session _session;
 
    /**
     * Flag that indicates if the session ID should be added as a parameter to
     * the response.
     */
    private boolean _returnSessionID;
 
    /**
     * The call ID, unique in the context of the pertaining function.
     */
    private final int _callID;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    // TODO: Document
    // TODO: Probably take a different approach
    CallResult getCallResult() {
       if (_builder.isSuccess() && _returnSessionID) {
          _builder.param("_session", _session.getIDString());
          _returnSessionID = false;
       }
       return _builder;
    }
 
    /**
     * Returns the start time of the call.
     *
     * @return
     *    the timestamp indicating when the call was started, as a number of
     *    milliseconds since midnight January 1, 1970 UTC.
     */
    public long getStart() {
       return _start;
    }
 
    /**
     * Returns the stored success indication. The default is <code>true</code>
     * and it will <em>only</em> be set to <code>false</code> if and only if
     * {@link #startResponse(boolean,String)} is called with the first
     * parameter (<em>success</em>) set to <code>false</code>.
     *
     * @return
     *    the success indication.
     *
     * @since XINS 0.128
     */
    public final boolean isSuccess() {
       return _builder.isSuccess();
    }
 
    /**
     * Returns the stored return code. The default is <code>null</code>
     * and it will <em>only</em> be set to something else if and only if
     * {@link #startResponse(boolean,String)} is called with the second
     * parameter (<em>code</em>) set to a non-<code>null</code>, non-empty
     * value.
     *
     * @return
     *    the return code, can be <code>null</code>.
     */
    final String getCode() {
       return _builder.getCode();
    }
 
    /**
     * Returns the session for this call, if any.
     *
     * @return
     *    the session for this call, not <code>null</code>.
     *
     * @throws IllegalStateException
     *    if the current function is not session-based.
     */
    public Session getSession() throws IllegalStateException {
 
       // Check preconditions
       if (_function.isSessionBased() == false) {
          throw new IllegalStateException("The function " + _functionName + " is not session-based.");
       }
 
       // Get the session
       return _session;
    }
 
    /**
     * Creates a session, stores it and remembers that it will have to be
     * returned in the result.
     *
     * @return
     *    the constructed session, cannot be <code>null</code>.
     */
    public Session createSession() {
 
       // Create the session
       Session session = _api.createSession();
 
       // Store the session and remember that we have to send it down
       _session         = session;
       _returnSessionID = true;
 
       return session;
    }
 
    /**
     * Returns the value of a parameter with the specificied name. Note that
     * reserved parameters, i.e. those starting with an underscore
     * (<code>'_'</code>) cannot be retrieved.
     *
     * @param name
     *    the name of the parameter, not <code>null</code>.
     *
     * @return
     *    the value of the parameter, or <code>null</code> if the parameter is
     *    not set, never an empty string (<code>""</code>) because it will be
     *    returned as being <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null</code>.
     */
    public String getParameter(String name)
    throws IllegalArgumentException {
 
       // Check arguments
       if (name == null) {
          throw new IllegalArgumentException("name == null");
       }
 
       // XXX: In a later version, support a parameter named 'function'
 
       if (_request != null && name.length() > 0 && !"function".equals(name) && name.charAt(0) != '_') {
          String value = _request.getParameter(name);
          return "".equals(value) ? null : value;
       }
       return null;
    }
 
    /**
     * Returns the assigned call ID. This ID is unique within the context of
     * the pertaining function. If no call ID is assigned, then <code>-1</code>
     * is returned.
     *
     * @return
     *    the assigned call ID for the function, or <code>-1</code> if none is
     *    assigned.
     */
    public int getCallID() {
       return _callID;
    }
 
    public final void startResponse(ResultCode resultCode)
    throws IllegalStateException, InvalidResponseException {
       if (resultCode == null) {
          startResponse(true, null);
       } else {
          startResponse(false, resultCode.getValue());
       }
    }
 
    public final void startResponse(boolean success)
    throws IllegalStateException, InvalidResponseException {
       startResponse(success, null);
    }
 
    public final void startResponse(boolean success, String returnCode)
    throws IllegalStateException, InvalidResponseException {
 
       // Check state
       if (_state != BEFORE_START) {
          throw new IllegalStateException("The state is " + _state + '.');
       }
 
       // Temporarily enter the ERROR state
       _state = ERROR;
 
       _builder.startResponse(success, returnCode);
 
       // Add the session ID, if any
       if (success && _returnSessionID) {
          _builder.param("_session", _session.getIDString());
          _returnSessionID = false;
       }
 
       // Reset the state
       _state = WITHIN_PARAMS;
    }
 
    public final void param(String name, String value)
    throws IllegalStateException, IllegalArgumentException, InvalidResponseException {
 
       // Check state
       if (_state != BEFORE_START && _state != WITHIN_PARAMS) {
          throw new IllegalStateException("The state is " + _state + '.');
       }
 
       // Check arguments
       MandatoryArgumentChecker.check("name", name);
       if (name.length() < 1) {
          throw new IllegalArgumentException("name.length() == " + name.length());
       }
 
       // TODO: Disallow parameters that start with an underscore?
 
       // Start the response if necesary
       if (_state == BEFORE_START) {
          startResponse(true, null);
       }
 
       // Short-circuit if the value is null or empty
       if (value != null && value.length() > 0) {
 
          // Temporarily enter the ERROR state
          _state = ERROR;
 
          // Set the parameter
          _builder.param(name, value);
       }
 
       // Reset the state
       _state = WITHIN_PARAMS;
    }
 
    public final void startTag(String type)
    throws IllegalStateException, IllegalArgumentException, InvalidResponseException {
 
       // Check state
       if (_state == AFTER_END) {
          throw new IllegalStateException("The state is " + _state + '.');
       }
 
       // Check argument
       if (type == null) {
          throw new IllegalArgumentException("type == null");
       } else if (type.length() == 0) {
          throw new IllegalArgumentException("type.equals(\"\")");
       }
 
       // Start the response if necesary
       if (_state == BEFORE_START) {
          startResponse(true, null);
       }
 
       // Temporarily enter the ERROR state
       _state = ERROR;
 
       // Write the start tag
       _builder.startTag(type);
       _elementDepth++;
 
       // Reset the state
       _state = START_TAG_OPEN;
    }
 
    public final void attribute(String name, String value)
    throws IllegalStateException, IllegalArgumentException, InvalidResponseException {
 
       // Check state
       if (_state != START_TAG_OPEN) {
          throw new IllegalStateException("The state is " + _state + '.');
       }
 
       // Temporarily enter the ERROR state
       _state = ERROR;
 
       // Write the attribute
       _builder.attribute(name, value);
 
       // Reset the state
       _state = START_TAG_OPEN;
    }
 
    public final void pcdata(String text)
    throws IllegalStateException, IllegalArgumentException, InvalidResponseException {
 
       // Check state
       if (_state != START_TAG_OPEN && _state != WITHIN_ELEMENT) {
          throw new IllegalStateException("The state is " + _state + '.');
       }
 
       // Temporarily enter the ERROR state
       _state = ERROR;
 
       // Write the PCDATA
       _builder.pcdata(text);
 
       // Reset the state
       _state = WITHIN_ELEMENT;
    }
 
    public final void endTag()
    throws IllegalStateException, InvalidResponseException {
 
       // Check state
       if (_state != START_TAG_OPEN && _state != WITHIN_ELEMENT) {
          throw new IllegalStateException("The state is " + _state + '.');
       }
       if (_elementDepth == 0) {
          throw new IllegalStateException("There are no more elements in the data section to close.");
       }
 
       // Temporarily enter the ERROR state
       _state = ERROR;
 
       // End the tag
       _builder.endTag();
       _elementDepth--;
 
       // Reset the state
       _state = WITHIN_ELEMENT;
    }
 
    public void fail(ResultCode resultCode)
    throws IllegalArgumentException, IllegalStateException, InvalidResponseException {
       fail(resultCode, null);
    }
 
    public void fail(ResultCode resultCode, String message)
    throws IllegalArgumentException, IllegalStateException, InvalidResponseException {
 
       // Check state
       if (_state != BEFORE_START) {
          throw new IllegalStateException("The state is " + _state + '.');
       }
 
       // Start response
       if (resultCode == null) {
          startResponse(false);
       } else {
          startResponse(resultCode);
       }
 
       // Include the message, if any
       if (message != null) {
          param("_message", message);
       }
 
       // End response
       endResponse();
    }
 
    public final void endResponse() throws InvalidResponseException {
 
       // Short-circuit if the response is already ended
       if (_state == AFTER_END) {
          return;
       }
 
       // Start the response if necesary
       if (_state == BEFORE_START) {
          startResponse(true, null);
       }
 
       // Temporarily enter the ERROR state
       _state = ERROR;
 
       // Close all open elements
       _builder.endResponse();
 
       // Set the state
       _state = AFTER_END;
    }
 
    /**
     * @deprecated
     *    Deprecated since XINS 0.157 with no replacement. Use <em>logdoc</em>
     *    instead.
     */
    public void trace(Object message) {
       _logger.log(FQCN, Priority.DEBUG, _logPrefix + message, null);
    }
 
    /**
     * @deprecated
     *    Deprecated since XINS 0.157 with no replacement. Use <em>logdoc</em>
     *    instead.
     */
    public void trace(Object message, Throwable t) {
       _logger.log(FQCN, Priority.DEBUG, _logPrefix + message, t);
    }
 
    /**
     * @deprecated
     *    Deprecated since XINS 0.157 with no replacement. Use <em>logdoc</em>
     *    instead.
     */
    public void debug(Object message) {
       _logger.log(FQCN, Priority.DEBUG, _logPrefix + message, null);
    }
 
    /**
     * @deprecated
     *    Deprecated since XINS 0.157 with no replacement. Use <em>logdoc</em>
     *    instead.
     */
    public void debug(Object message, Throwable t) {
       _logger.log(FQCN, Priority.DEBUG, _logPrefix + message, t);
    }
 
    /**
     * @deprecated
     *    Deprecated since XINS 0.157 with no replacement. Use <em>logdoc</em>
     *    instead.
     */
    public void info(Object message) {
       _logger.log(FQCN, Priority.INFO, _logPrefix + message, null);
    }
 
    /**
     * @deprecated
     *    Deprecated since XINS 0.157 with no replacement. Use <em>logdoc</em>
     *    instead.
     */
    public void info(Object message, Throwable t) {
       _logger.log(FQCN, Priority.INFO, _logPrefix + message, t);
    }
 
    /**
     * @deprecated
     *    Deprecated since XINS 0.157 with no replacement. Use <em>logdoc</em>
     *    instead.
     */
    public void warn(Object message) {
       _logger.log(FQCN, Priority.WARN, _logPrefix + message, null);
    }
 
    /**
     * @deprecated
     *    Deprecated since XINS 0.157 with no replacement. Use <em>logdoc</em>
     *    instead.
     */
    public void warn(Object message, Throwable t) {
       _logger.log(FQCN, Priority.WARN, _logPrefix + message, t);
    }
 
    /**
     * @deprecated
     *    Deprecated since XINS 0.157 with no replacement. Use <em>logdoc</em>
     *    instead.
     */
    public void error(Object message) {
       _logger.log(FQCN, Priority.ERROR, _logPrefix + message, null);
    }
 
    /**
     * @deprecated
     *    Deprecated since XINS 0.157 with no replacement. Use <em>logdoc</em>
     *    instead.
     */
    public void error(Object message, Throwable t) {
       _logger.log(FQCN, Priority.ERROR, _logPrefix + message, t);
    }
 
    /**
     * @deprecated
     *    Deprecated since XINS 0.157 with no replacement. Use <em>logdoc</em>
     *    instead.
     */
    public void fatal(Object message) {
       _logger.log(FQCN, Priority.FATAL, _logPrefix + message, null);
    }
 
    /**
     * @deprecated
     *    Deprecated since XINS 0.157 with no replacement. Use <em>logdoc</em>
     *    instead.
     */
    public void fatal(Object message, Throwable t) {
       _logger.log(FQCN, Priority.FATAL, _logPrefix + message, t);
    }
 
    /**
     * @deprecated
     *    Deprecated since XINS 0.157 with no replacement. Use <em>logdoc</em>
     *    instead.
     */
    public boolean isDebugEnabled() {
       return _logger.isDebugEnabled();
    }
 
    /**
     * @deprecated
     *    Deprecated since XINS 0.157 with no replacement. Use <em>logdoc</em>
     *    instead.
     */
    public boolean isErrorEnabled() {
       return _logger.isEnabledFor(Priority.ERROR);
    }
 
    /**
     * @deprecated
     *    Deprecated since XINS 0.157 with no replacement. Use <em>logdoc</em>
     *    instead.
     */
    public boolean isFatalEnabled() {
       return _logger.isEnabledFor(Priority.FATAL);
    }
 
    /**
     * @deprecated
     *    Deprecated since XINS 0.157 with no replacement. Use <em>logdoc</em>
     *    instead.
     */
    public boolean isInfoEnabled() {
       return _logger.isInfoEnabled();
    }
 
    /**
     * @deprecated
     *    Deprecated since XINS 0.157 with no replacement. Use <em>logdoc</em>
     *    instead.
     */
    public boolean isTraceEnabled() {
       return _logger.isDebugEnabled();
    }
 
    /**
     * @deprecated
     *    Deprecated since XINS 0.157 with no replacement. Use <em>logdoc</em>
     *    instead.
     */
    public boolean isWarnEnabled() {
       return _logger.isEnabledFor(Priority.WARN);
    }
 }
