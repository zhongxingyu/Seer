 /*
  * $Id$
  */
 package org.xins.server;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import javax.servlet.ServletRequest;
 import org.apache.log4j.Logger;
 import org.xins.types.Type;
 import org.xins.types.TypeValueException;
 import org.xins.types.standard.Text;
 import org.xins.util.MandatoryArgumentChecker;
 import org.xins.util.collections.PropertyReader;
 import org.xins.util.collections.PropertiesPropertyReader;
 import org.xins.util.collections.expiry.ExpiryFolder;
 import org.xins.util.collections.expiry.ExpiryStrategy;
 import org.xins.util.io.FastStringWriter;
 import org.znerd.xmlenc.XMLOutputter;
 
 /**
  * Base class for API implementation classes.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  */
 public abstract class API
 extends Object
 implements DefaultResultCodes {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The logging category used by this class. This class field is never
     * <code>null</code>.
     */
    private static final Logger LOG = Logger.getLogger(API.class.getName());
 
    /**
     * Constant indicating the <em>uninitialized</em> state. See
     * {@link #_state}.
     */
    private static final int UNINITIALIZED = 0;
 
    /**
     * Constant indicating the <em>initializing</em> state. See
     * {@link #_state}.
     */
    private static final int INITIALIZING = 1;
 
    /**
     * Constant indicating the <em>initialized</em> state. See
     * {@link #_state}.
     */
    private static final int INITIALIZED = 2;
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>API</code> object.
     */
    protected API() {
       _stateLock         = new Object();
       _startupTimestamp  = System.currentTimeMillis();
       _instances         = new ArrayList();
       _functionsByName   = new HashMap();
       _functionList      = new ArrayList();
       _resultCodesByName = new HashMap();
       _resultCodeList    = new ArrayList();
       _contextsByThread  = new HashMap();
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The current state. Either {@link #UNINITIALIZED}, {@link #INITIALIZING}
     * or {@link #INITIALIZED}.
     */
    private int _state;
 
    /**
     * Lock object for the current state.
     */
    private final Object _stateLock;
 
    /**
     * Flag that indicates if this API is session-based.
     */
    private boolean _sessionBased;
 
    /**
     * List of registered instances. See {@link #addInstance(Object)}.
     *
     * <p />This field is initialized to a non-<code>null</code> value by the
     * constructor.
     */
    private final List _instances;
 
    /**
     * Map that maps session identifiers to <code>Session</code> instances.
     * Contains all sessions associated with this API.
     *
     * <p />This field is initialized to a non-<code>null</code> value by the
     * initialization method {@link #init(Properties)}.
     */
    private ExpiryFolder _sessionsByID;
 
    /**
     * Map that maps function names to <code>Function</code> instances.
     * Contains all functions associated with this API.
     *
     * <p />This field is initialized to a non-<code>null</code> value by the
     * constructor.
     */
    private final Map _functionsByName;
 
    /**
     * List of all functions. This field cannot be <code>null</code>.
     */
    private final List _functionList;
 
    /**
     * Map that maps result code names to <code>ResultCode</code> instances.
     * Contains all result codes associated with this API.
     *
     * <p />This field is initialized to a non-<code>null</code> value by the
     * constructor.
     */
    private final Map _resultCodesByName;
 
    /**
     * List of all result codes. This field cannot be <code>null</code>.
     */
    private final List _resultCodeList;
 
    /**
     * Map that maps threads to <code>CallContext</code> instances.
     *
     * <p />This field is initialized to a non-<code>null</code> value by the
     * constructor.
     */
    private final Map _contextsByThread;
 
    /**
     * The initialization settings. This field is initialized by
     * {@link #init(Properties)}. It can be <code>null</code> before that.
     */
    private Properties _initSettings;
 
    /**
     * A reader for the initialization settings. This field is initialized by
     * {@link #init(Properties)}. It can be <code>null</code> before that.
     */
    private PropertyReader _initSettingsReader;
 
    /**
     * The name of the default function. Is <code>null</code> if there is no
     * default function.
     */
    private String _defaultFunction;
 
    /**
     * The type that applies for session identifiers. Will be set in
     * {@link #init(Properties)}.
     */
    private SessionID _sessionIDType;
 
    /**
     * The session ID generator. Will be set in {@link #init(Properties)}.
     */
    private SessionID.Generator _sessionIDGenerator;
 
    /**
     * Flag that indicates if the shutdown sequence has been initiated.
     */
    private boolean _shutDown;
 
    /**
     * Timestamp indicating when this API instance was created.
     */
    private final long _startupTimestamp;
 
    /**
     * Deployment identifier.
     */
    private String _deployment;
 
    /**
     * Host name for the machine that was used for this build.
     */
    private String _buildHost;
 
    /**
     * Time stamp that indicates when this build was done.
     */
    private String _buildTime;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Gets the specified property and converts it to a <code>boolean</code>.
     *
     * @param properties
     *    the set of properties to read from, cannot be <code>null</code>.
     *
     * @param propertyName
     *    the name of the property to read, cannot be <code>null</code>.
     *
     * @return
     *    the value of the property.
     *
     * @throws IllegalArgumentException
     *    if <code>properties == null || propertyName == null</code>.
     */
    private final boolean getBooleanProperty(Properties properties, String propertyName)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("properties", properties, "propertyName", propertyName);
 
       String value = properties.getProperty(propertyName);
       return "true".equals(value);
    }
 
    /**
     * Gets the specified property and converts it to an <code>int</code>.
     *
     * @param properties
     *    the set of properties to read from, cannot be <code>null</code>.
     *
     * @param propertyName
     *    the name of the property to read, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>properties == null || propertyName == null</code>.
     *
     * @throws NumberFormatException
     *    if the conversion to an <code>int</code> failed.
     */
    private final int getIntProperty(Properties properties, String propertyName)
    throws IllegalArgumentException, NumberFormatException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("properties", properties, "propertyName", propertyName);
 
       String value = properties.getProperty(propertyName);
       return Integer.parseInt(value);
    }
 
    /**
     * Gets the timestamp that indicates when this <code>API</code> instance
     * was created.
     *
     * @return
     *    the time this instance was constructed, as a number of milliseconds
     *    since midnight January 1, 1970.
     */
    public final long getStartupTimestamp() {
       return _startupTimestamp;
    }
 
    /**
     * Initializes this API. The properties are stored internally and then
     * {@link #initImpl(Properties)} is called.
     *
     * @param properties
     *    the properties, can be <code>null</code>.
     *
     * @throws IllegalStateException
     *    if this API is already initialized.
     *
     * @throws Throwable
     *    if the initialization fails (in {@link #initImpl(Properties)}).
     */
    public final void init(Properties properties)
    throws IllegalStateException, Throwable {
 
       // Check and set state
       synchronized (_stateLock) {
          if (_state != UNINITIALIZED) {
             throw new IllegalStateException("This API is not uninitialized anymore.");
          }
          _state = INITIALIZING;
       }
 
       // Store the settings
       if (properties == null) {
          _initSettings = new Properties();
       } else {
          _initSettings = (Properties) properties.clone();
       }
       _initSettingsReader = new PropertiesPropertyReader(_initSettings);
 
       // Check if a default function is set
       _defaultFunction = properties.getProperty("org.xins.api.defaultFunction");
       if (_defaultFunction != null) {
          LOG.debug("Default function set to \"" + _defaultFunction + "\".");
       }
 
       // Check if this API is session-based
       _sessionBased = getBooleanProperty(properties, "org.xins.api.sessionBased");
 
       // XXX: Allow configuration of session ID type ?
 
       // Initialize session-based API
       if (_sessionBased) {
          LOG.debug("Performing session-related initialization.");
 
          // Initialize session ID type
          _sessionIDType      = new BasicSessionID(this);
          _sessionIDGenerator = _sessionIDType.getGenerator();
 
          // Determine session time-out duration and precision
          final long MINUTE_IN_MS = 60000L;
          long timeOut   = MINUTE_IN_MS * (long) getIntProperty(properties, "org.xins.api.sessionTimeOut");
          long precision = MINUTE_IN_MS * (long) getIntProperty(properties, "org.xins.api.sessionTimeOutPrecision");
 
          // Create expiry strategy and folder, max queue wait time is set to
          // half of the time-out duration
          ExpiryStrategy expiryStrategy = new ExpiryStrategy(timeOut, precision);
          _sessionsByID = new ExpiryFolder(expiryStrategy, timeOut / 2L);
       }
 
       // Get build-time properties
       _deployment = properties.getProperty("org.xins.api.deployment");
       _buildHost  = properties.getProperty("org.xins.api.build.host");
       _buildTime  = properties.getProperty("org.xins.api.build.time");
       if (_buildHost == null) {
          throw new Exception("Build host name is not set.");
       } else if (_buildTime == null) {
          throw new Exception("Build time stamp is not set.");
       }
 
       // Log build-time properties
      if (_deployment == null) {
          LOG.info("Built on " + _buildHost + " (" + _buildTime + ").");
       } else {
          LOG.info("Built deployment \"" + _deployment + "\" on " + _buildHost + " (" + _buildTime + ").");
       }
 
       // Let the subclass perform initialization
       boolean succeeded = false;
       try {
          initImpl(properties);
          succeeded = true;
 
       // Set the state
       } finally {
          synchronized (_stateLock) {
             if (!succeeded) {
                _state = UNINITIALIZED;
             } else {
                _state = INITIALIZED;
             }
          }
       }
 
       // XXX: Initialize all instances here somewhere ?
    }
 
    /**
     * Actually initializes this API.
     *
     * <p />The implementation of this method in class {@link API} is empty.
     *
     * @param properties
     *    the properties, can be <code>null</code>.
     *
     * @throws Throwable
     *    if the initialization fails.
     */
    protected void initImpl(Properties properties)
    throws Throwable {
       // empty
    }
 
    /**
     * Adds the specified instance as an object to initialize at startup and
     * deinitialize at shutdown. The object will immediately be initialized. If
     * the initialization fails, then an {@link InitializationException} will
     * be thrown.
     *
     * <p>The initialization will be performed by calling
     * {@link Singleton#init(PropertyReader)}.
     *
     * <p>At shutdown time {@link Singleton#destroy()} will be called.
     *
     * @param instance
     *    the instance to initialize now and deinitialize at shutdown time, not
     *    <code>null</code>.
     *
     * @throws IllegalStateException
     *    if this API is currently not in the initializing state.
     *
     * @throws IllegalArgumentException
     *    if <code>instance == null</code>.
     *
     * @throws InitializationException
     *    if the initialization of the instance failed.
     *
     * @since XINS 0.55
     */
    protected final void addInstance(Singleton instance)
    throws IllegalStateException,
           IllegalArgumentException,
           InitializationException {
 
       // Check state
       if (_state != INITIALIZING) {
          throw new IllegalStateException("Currently not initializing.");
       }
 
       // Check preconditions
       MandatoryArgumentChecker.check("instance", instance);
 
       _instances.add(instance);
 
       boolean succeeded = false;
       String className = instance.getClass().getName();
       LOG.debug("Initializing instance of class \"" + className + "\".");
       try {
          instance.init(_initSettingsReader);
          succeeded = true;
       } finally {
          if (succeeded) {
             LOG.info("Initialized instance of class \"" + className + "\".");
          } else {
             String message = "Failed to initialize instance of \"" + className + "\".";
             LOG.error(message);
          }
       }
    }
 
    /**
     * Adds the specified instance as an object to initialize at startup and
     * deinitialize at shutdown. The object will immediately be initialized. If
     * the initialization fails, then an {@link InitializationException} will
     * be thrown.
     *
     * <p>The initialization will be performed by calling a method
     * <code>init(</code>{@link Properties}<code>)</code> in the specified
     * instance with the following characteristics:
     *
     * <ul>
     *    <li>Must be <em>public</em>
     *    <li>Cannot be <em>static</em>
     *    <li>Cannot be <em>abstract</em>
     * </ul>
     *
     * <p>At shutdown time, a method <code>destroy()</code> will be
     * called using the same approach. The conditions for the <code>init</code>
     * method also apply to this method.
     *
     * @param instance
     *    the instance to initialize now and deinitialize at shutdown time, not
     *    <code>null</code>.
     *
     * @throws IllegalStateException
     *    if this API is currently not in the initializing state.
     *
     * @throws IllegalArgumentException
     *    if <code>instance == null</code>.
     *
     * @throws InitializationException
     *    if the initialization of the instance failed.
     *
     * @deprecated
     *    Deprecated since XINS 0.55. Use {@link #addInstance(Singleton)}
     *    instead.
     */
    protected final void addInstance(Object instance)
    throws IllegalStateException,
           IllegalArgumentException,
           InitializationException {
 
       // Forward call to non-deprecated method, if possible
       if (instance instanceof Singleton) {
          addInstance((Singleton) instance);
       }
 
 
       // Check state
       if (_state != INITIALIZING) {
          throw new IllegalStateException("Currently not initializing.");
       }
 
       // Check preconditions
       MandatoryArgumentChecker.check("instance", instance);
 
       if ((instance instanceof Singleton) == false) {
          LOG.warn("Registering API singleton of class " + instance.getClass().getName() + ", which does not implement the interface " + Singleton.class.getName() + '.');
       }
 
       _instances.add(instance);
 
       boolean succeeded = callMethod(instance, "init", new Class[] { Properties.class }, new Object[] { _initSettings.clone() });
      
       String className = instance.getClass().getName();
       if (succeeded) {
          LOG.info("Initialized instance of " + className + '.');
       } else {
          String message = "Failed to initialize instance of " + className + '.';
          LOG.error(message);
          throw new InitializationException(message);
       }
    }
 
    /**
     * Calls the specified method with the specified arguments.
     *
     * @param instance
     *    the instance on which to call the method, should not be
     *    <code>null</code>.
     *
     * @param methodName
     *    the name of the method to call.
     *
     * @param parameterTypes
     *    the parameter types for the method.
     *
     * @param arguments
     *    the arguments to pass to the method.
     *
     * @return
     *    the value returned by the call, can be <code>null</code>.
     */
    private final boolean callMethod(Object   instance,
                                     String   methodName,
                                     Class[]  parameterTypes,
                                     Object[] arguments) {
 
       Class clazz      = instance.getClass();
       String className = clazz.getName();
 
       // Determine the signature
       StringBuffer sb = new StringBuffer(128);
       sb.append(className);
       sb.append('.');
       sb.append(methodName);
       sb.append('(');
       for (int i = 0; i < parameterTypes.length; i++) {
          if (i > 0) {
             sb.append(", ");
          }
          sb.append(parameterTypes[i].getClass().getName());
       }
       sb.append(')');
       String signature = sb.toString();
 
       // Get the method
       Method method;
       try {
          method = clazz.getDeclaredMethod(methodName, parameterTypes);
       } catch (NoSuchMethodException exception) {
          LOG.warn("Unable to find method " + signature + '.');
          return false;
       } catch (SecurityException exception) {
          LOG.warn("Access denied while attempting to lookup method " + signature + '.');
          return false;
       }
 
       // The method must be public, non-abstract and non-static
       int modifiers = method.getModifiers();
       if (Modifier.isAbstract(modifiers)) {
          LOG.warn("Unable to call abstract method " + signature + '.');
          return false;
       } else if (Modifier.isStatic(modifiers)) {
          LOG.warn("Unable to call abstract method " + signature + '.');
          return false;
       } else if (Modifier.isPublic(modifiers) == false) {
          LOG.warn("Unable to call non-public method " + signature + '.');
          return false;
       }
 
       // Attempt the call
       try {
          method.invoke(instance, arguments);
       } catch (Throwable exception) {
          LOG.error("Unable to call " + signature + " due to unexpected exception.", exception);
          return false;
       }
 
       return true;
    }
 
    /**
     * Performs shutdown of this XINS API.
     */
    final void destroy() {
       _shutDown = true;
 
       for (int i = 0; i < _instances.size(); i++) {
          Object instance = _instances.get(i);
 
          boolean succeeded = callMethod(instance, "destroy", new Class[] {}, null);
      
          String className = instance.getClass().getName();
          if (succeeded) {
             LOG.info("Deinitialized instance of " + className + '.');
          } else {
             LOG.error("Failed to deinitialize instance of " + className + '.');
          }
       }
    }
 
    /**
     * Returns the name of the default function, if any.
     *
     * @return
     *    the name of the default function, or <code>null</code> if there is
     *    none.
     */
    public String getDefaultFunctionName() {
       // TODO: Check state
       return _defaultFunction;
    }
 
    /**
     * Returns if this API is session-based.
     *
     * @return
     *    <code>true</code> if this API is session-based, or <code>false</code>
     *    if it is not.
     *
     * @throws IllegalStateException
     *    if this API is not in the <em>initialized</em> state.
     */
    public boolean isSessionBased()
    throws IllegalStateException {
 
       if (_state != INITIALIZED) {
          throw new IllegalStateException("This API is not in the 'initialized' state.");
       }
 
       return _sessionBased;
    }
 
    /**
     * Gets the session ID type.
     *
     * @return
     *    the type for session IDs in this API, unless otherwise defined this
     *    is {@link Text}.
     *
     * @throws IllegalStateException
     *    if this API is not in the <em>initialized</em> state or if this API is not session-based.
     */
    public final SessionID getSessionIDType()
    throws IllegalStateException {
 
       // Check preconditions
       if (_state != INITIALIZED) {
          throw new IllegalStateException("This API is not in the 'initialized' state.");
       } else if (! _sessionBased) {
          throw new IllegalStateException("This API is not session-based.");
       }
 
       return _sessionIDType;
    }
 
    /**
     * Creates a new session for this API.
     *
     * @return
     *    the newly constructed session, never <code>null</code>.
     *
     * @throws IllegalStateException
     *    if this API is not in the <em>initialized</em> state or if this API is not session-based.
     */
    final Session createSession() throws IllegalStateException {
 
       // Check preconditions
       if (_state != INITIALIZED) {
          throw new IllegalStateException("This API is not in the 'initialized' state.");
       } else if (! _sessionBased) {
          throw new IllegalStateException("This API is not session-based.");
       }
 
       // Generate a session ID
       Object sessionID = _sessionIDGenerator.generateSessionID();
 
       // Construct a Session object...
       Session session = new Session(this, sessionID);
 
       // ...store it...
       _sessionsByID.put(sessionID, session);
 
       // ...and then return it
       return session;
    }
 
    /**
     * Gets the session with the specified identifier.
     *
     * @param id
     *    the identifier for the session, can be <code>null</code>.
     *
     * @return
     *    the session with the specified identifier, or <code>null</code> if
     *    there is no match; if <code>id == null</code>, then <code>null</code>
     *    is returned.
     *
     * @throws IllegalStateException
     *    if this API is not in the <em>initialized</em> state or if this API is not session-based.
     */
    final Session getSession(Object id) throws IllegalStateException {
 
       // Check preconditions
       if (_state != INITIALIZED) {
          throw new IllegalStateException("This API is not in the 'initialized' state.");
       } else if (! _sessionBased) {
          throw new IllegalStateException("This API is not session-based.");
       }
 
       return (Session) _sessionsByID.get(id);
    }
 
    /**
     * Gets the session with the specified identifier as a string.
     *
     * @param idString
     *    the string representation of the identifier for the session, can be <code>null</code>.
     *
     * @return
     *    the session with the specified identifier, or <code>null</code> if
     *    there is no match; if <code>idString == null</code>, then
     *    <code>null</code> is returned.
     *
     * @throws IllegalStateException
     *    if this API is not in the <em>initialized</em> state or if this API is not session-based.
     *
     * @throws TypeValueException
     *    if the specified string is not a valid representation for a value for
     *    the specified type.
     */
    final Session getSessionByString(String idString)
    throws IllegalStateException, TypeValueException {
 
       // Check preconditions
       if (_state != INITIALIZED) {
          throw new IllegalStateException("This API is not in the 'initialized' state.");
       } else if (! _sessionBased) {
          throw new IllegalStateException("This API is not session-based.");
       }
 
       return getSession(_sessionIDType.fromString(idString));
    }
 
    /**
     * Callback method invoked when a function is constructed.
     *
     * @param function
     *    the function that is added, not <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>function == null</code>.
     */
    final void functionAdded(Function function)
    throws NullPointerException {
 
       // TODO: Check the state here?
 
       _functionsByName.put(function.getName(), function);
       _functionList.add(function);
 
       // TODO: After all functions are added, check that the default function
       //       is set.
    }
 
    /**
     * Callback method invoked when a result code is constructed.
     *
     * @param resultCode
     *    the result code that is added, not <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>resultCode == null</code>.
     */
    final void resultCodeAdded(ResultCode resultCode)
    throws NullPointerException {
       _resultCodesByName.put(resultCode.getName(), resultCode);
       _resultCodeList.add(resultCode);
    }
 
    /**
     * Returns the function with the specified name.
     *
     * @param name
     *    the name of the function, will not be checked if it is
     *    <code>null</code>.
     *
     * @return
     *    the function with the specified name, or <code>null</code> if there
     *    is no match.
     */
    final Function getFunction(String name) {
       return (Function) _functionsByName.get(name);
    }
 
    /**
     * Forwards a call to a function. The call will actually be handled by
     * {@link Function#handleCall(CallContext)}.
     *
     * @param request
     *    the original servlet request, not <code>null</code>.
     *
     * @param out
     *    the output stream to write to, not <code>null</code>.
     *
     * @throws IOException
     *    if an I/O error occurs.
     */
    final void handleCall(ServletRequest request, PrintWriter out)
    throws IOException {
 
       // Get call context
       Thread thread = Thread.currentThread();
       CallContext context = (CallContext) _contextsByThread.get(thread);
       if (context == null) {
          context = new CallContext(this);
          _contextsByThread.put(thread, context);
       }
 
       // Configure the call context
       try {
          context.reset(request);
       } catch (MissingSessionIDException exception) {
          XMLOutputter xmlOutputter = context.getXMLOutputter();
          xmlOutputter.reset(out, "UTF-8");
          xmlOutputter.startTag("result");
          xmlOutputter.attribute("success", "false");
          xmlOutputter.attribute("code", "MissingSessionID"); // TODO: Use special ResultCode
          xmlOutputter.endDocument();
          return;
       } catch (InvalidSessionIDException exception) {
          XMLOutputter xmlOutputter = context.getXMLOutputter();
          xmlOutputter.reset(out, "UTF-8");
          xmlOutputter.startTag("result");
          xmlOutputter.attribute("success", "false");
          xmlOutputter.attribute("code", "InvalidSessionID"); // TODO: Use special ResultCode
          xmlOutputter.endDocument();
          return;
       } catch (UnknownSessionIDException exception) {
          XMLOutputter xmlOutputter = context.getXMLOutputter();
          xmlOutputter.reset(out, "UTF-8");
          xmlOutputter.startTag("result");
          xmlOutputter.attribute("success", "false");
          xmlOutputter.attribute("code", "UnknownSessionID"); // TODO: Use special ResultCode
          xmlOutputter.endDocument();
          return;
       }
 
       FastStringWriter stringWriter = context.getStringWriter();
 
       // Determine the function name
       String functionName = context.getFunctionName();
       if (functionName == null || functionName.length() == 0) {
          context.startResponse(MISSING_FUNCTION_NAME);
          context.endResponse();
          out.print(stringWriter.toString());
          return;
       }
 
       // Detect special functions
       if (functionName.charAt(0) == '_') {
          if ("_NoOp".equals(functionName)) {
             // empty
          } else if ("_GetFunctionList".equals(functionName)) {
             doGetFunctionList(context);
          } else if ("_GetStatistics".equals(functionName)) {
             doGetStatistics(context);
          } else if ("_GetVersion".equals(functionName)) {
             doGetVersion(context);
          } else if ("_GetSettings".equals(functionName)) {
             doGetSettings(context);
          } else {
             context.startResponse(NO_SUCH_FUNCTION);
          }
          context.endResponse();
          out.print(stringWriter.toString());
          return;
       }
 
       // Short-circuit if we are shutting down
       if (_shutDown) {
          XMLOutputter xmlOutputter = context.getXMLOutputter();
          xmlOutputter.reset(out, "UTF-8");
          xmlOutputter.startTag("result");
          xmlOutputter.attribute("success", "false");
          xmlOutputter.attribute("code",    INTERNAL_ERROR.getValue());
          // TODO: Add _message parameter
          xmlOutputter.endDocument();
       }
 
       // Get the function object
       Function f = context.getFunction();
 
       // Detect case where function is not recognized
       if (f == null) {
          context.startResponse(NO_SUCH_FUNCTION);
          context.endResponse();
          out.print(stringWriter.toString());
          return;
       }
 
       // Forward the call
       boolean exceptionThrown = true;
       boolean success;
       // XXX: Use ResultCode here, instead of String ?
       String code;
       try {
          f.handleCall(context);
          context.endResponse();
          success = context.getSuccess();
          code    = context.getCode();
          exceptionThrown = false;
       } catch (Throwable exception) {
          LOG.error("Caught exception while calling API.", exception);
 
          success = false;
          code    = INTERNAL_ERROR.getValue();
 
          XMLOutputter xmlOutputter = context.getXMLOutputter();
          xmlOutputter.reset(out, "UTF-8");
          xmlOutputter.startTag("result");
          xmlOutputter.attribute("success", "false");
          xmlOutputter.attribute("code", code);
          xmlOutputter.startTag("param");
          xmlOutputter.attribute("name", "_exception.class");
          xmlOutputter.pcdata(exception.getClass().getName());
 
          String message = exception.getMessage();
          if (message != null && message.length() > 0) {
             xmlOutputter.endTag();
             xmlOutputter.startTag("param");
             xmlOutputter.attribute("name", "_exception.message");
             xmlOutputter.pcdata(message);
          }
 
          FastStringWriter stWriter = new FastStringWriter();
          PrintWriter printWriter = new PrintWriter(stWriter);
          exception.printStackTrace(printWriter);
          String stackTrace = stWriter.toString();
          if (stackTrace != null && stackTrace.length() > 0) {
             xmlOutputter.endTag();
             xmlOutputter.startTag("param");
             xmlOutputter.attribute("name", "_exception.stacktrace");
             xmlOutputter.pcdata(stackTrace);
          }
          xmlOutputter.close();
       }
 
       if (!exceptionThrown) {
          out.print(stringWriter.toString());
       }
       f.performedCall(context, success, code);
    }
 
    /**
     * Returns a list of all functions in this API. Per function the name and
     * the version are returned.
     *
     * @param context
     *    the context, guaranteed to be not <code>null</code>.
     *
     * @throws IOException
     *    if an I/O error occurs.
     */
    private final void doGetFunctionList(CallContext context)
    throws IOException {
       int count = _functionList.size();
       for (int i = 0; i < count; i++) {
          Function function = (Function) _functionList.get(i);
          context.startTag("function");
          context.attribute("name",    function.getName());
          context.attribute("version", function.getVersion());
          context.endTag();
       }
    }
 
    /**
     * Returns the call statistics for all functions in this API.
     *
     * @param context
     *    the context, guaranteed to be not <code>null</code>.
     *
     * @throws IOException
     *    if an I/O error occurs.
     */
    private final void doGetStatistics(CallContext context)
    throws IOException {
       context.param("now", String.valueOf(System.currentTimeMillis()));
       int count = _functionList.size();
       for (int i = 0; i < count; i++) {
          Function function = (Function) _functionList.get(i);
          Function.Statistics stats = function.getStatistics();
 
          long successfulCalls       = stats.getSuccessfulCalls();
          long unsuccessfulCalls     = stats.getUnsuccessfulCalls();
          long successfulDuration    = stats.getSuccessfulDuration();
          long unsuccessfulDuration  = stats.getUnsuccessfulDuration();
 
          String successfulAverage;
          String successfulMin;
          String successfulMinStart;
          String successfulMax;
          String successfulMaxStart;
          String lastSuccessfulStart;
          String lastSuccessfulDuration;
          if (successfulCalls == 0) {
             successfulAverage = "NA";
             successfulMin     = "NA";
             successfulMinStart = "NA";
             successfulMax     = "NA";
             successfulMaxStart = "NA";
             lastSuccessfulStart    = "NA";
             lastSuccessfulDuration = "NA";
          } else if (successfulDuration == 0) {
             successfulAverage = "0";
             successfulMin     = String.valueOf(stats.getSuccessfulMin());
             successfulMinStart     = String.valueOf(stats.getSuccessfulMinStart());
             successfulMax     = String.valueOf(stats.getSuccessfulMax());
             successfulMaxStart     = String.valueOf(stats.getSuccessfulMaxStart());
             lastSuccessfulStart    = String.valueOf(stats.getLastSuccessfulStart());
             lastSuccessfulDuration = String.valueOf(stats.getLastSuccessfulDuration());
          } else {
             successfulAverage = String.valueOf(successfulDuration / successfulCalls);
             successfulMin     = String.valueOf(stats.getSuccessfulMin());
             successfulMinStart     = String.valueOf(stats.getSuccessfulMinStart());
             successfulMax     = String.valueOf(stats.getSuccessfulMax());
             successfulMaxStart     = String.valueOf(stats.getSuccessfulMaxStart());
             lastSuccessfulStart    = String.valueOf(stats.getLastSuccessfulStart());
             lastSuccessfulDuration = String.valueOf(stats.getLastSuccessfulDuration());
          }
 
          String unsuccessfulAverage;
          String unsuccessfulMin;
          String unsuccessfulMinStart;
          String unsuccessfulMax;
          String unsuccessfulMaxStart;
          String lastUnsuccessfulStart;
          String lastUnsuccessfulDuration;
          if (unsuccessfulCalls == 0) {
             unsuccessfulAverage = "NA";
             unsuccessfulMin     = "NA";
             unsuccessfulMinStart = "NA";
             unsuccessfulMax     = "NA";
             unsuccessfulMaxStart = "NA";
             lastUnsuccessfulStart    = "NA";
             lastUnsuccessfulDuration = "NA";
          } else if (unsuccessfulDuration == 0) {
             unsuccessfulAverage = "0";
             unsuccessfulMin     = String.valueOf(stats.getUnsuccessfulMin());
             unsuccessfulMinStart     = String.valueOf(stats.getUnsuccessfulMinStart());
             unsuccessfulMax     = String.valueOf(stats.getUnsuccessfulMax());
             unsuccessfulMaxStart     = String.valueOf(stats.getUnsuccessfulMaxStart());
             lastUnsuccessfulStart    = String.valueOf(stats.getLastUnsuccessfulStart());
             lastUnsuccessfulDuration = String.valueOf(stats.getLastUnsuccessfulDuration());
          } else {
             unsuccessfulAverage = String.valueOf(unsuccessfulDuration / unsuccessfulCalls);
             unsuccessfulMin     = String.valueOf(stats.getUnsuccessfulMin());
             unsuccessfulMinStart     = String.valueOf(stats.getUnsuccessfulMinStart());
             unsuccessfulMax     = String.valueOf(stats.getUnsuccessfulMax());
             unsuccessfulMaxStart     = String.valueOf(stats.getUnsuccessfulMaxStart());
             lastUnsuccessfulStart    = String.valueOf(stats.getLastUnsuccessfulStart());
             lastUnsuccessfulDuration = String.valueOf(stats.getLastUnsuccessfulDuration());
          }
 
          context.startTag("function");
          context.attribute("name", function.getName());
          context.startTag("successful");
          context.attribute("count", String.valueOf(successfulCalls));
          context.attribute("average", successfulAverage);
          context.startTag("min");
          context.attribute("start",    successfulMinStart);
          context.attribute("duration", successfulMin);
          context.endTag(); // min
          context.startTag("max");
          context.attribute("start",    successfulMaxStart);
          context.attribute("duration", successfulMax);
          context.endTag(); // max
          context.startTag("last");
          context.attribute("start",    lastSuccessfulStart);
          context.attribute("duration", lastSuccessfulDuration);
          context.endTag(); // last
          context.endTag(); // successful
          context.startTag("unsuccessful");
          context.attribute("count",   String.valueOf(unsuccessfulCalls));
          context.attribute("average", unsuccessfulAverage);
          context.startTag("min");
          context.attribute("start",        unsuccessfulMinStart);
          context.attribute("duration",     unsuccessfulMin);
          context.endTag(); // min
          context.startTag("max");
          context.attribute("start",        unsuccessfulMaxStart);
          context.attribute("duration",     unsuccessfulMax);
          context.endTag(); // max
          context.startTag("last");
          context.attribute("start",    lastUnsuccessfulStart);
          context.attribute("duration", lastUnsuccessfulDuration);
          context.endTag(); // last
          context.endTag(); // unsuccessful
          context.endTag(); // function
       }
    }
 
    /**
     * Returns the XINS version.
     *
     * @param context
     *    the context, guaranteed to be not <code>null</code>.
     *
     * @throws IOException
     *    if an I/O error occurs.
     */
    private final void doGetVersion(CallContext context)
    throws IOException {
       context.param("java.version",   System.getProperty("java.version"));
       context.param("xmlenc.version", org.znerd.xmlenc.Library.getVersion());
       context.param("xins.version",   Library.getVersion());
    }
 
    /**
     * Returns the settings.
     *
     * @param context
     *    the context, guaranteed to be not <code>null</code>.
     *
     * @throws IOException
     *    if an I/O error occurs.
     */
    private final void doGetSettings(CallContext context)
    throws IOException {
 
       // Initialization settings
       Enumeration names = _initSettings.propertyNames();
       context.startTag("initialization");
       while (names.hasMoreElements()) {
          String key   = (String) names.nextElement();
          String value = _initSettings.getProperty(key);
 
          context.startTag("property");
          context.attribute("name", key);
          context.pcdata(value);
          context.endTag();
       }
       context.endTag();
 
       // System properties
       names = System.getProperties().propertyNames();
       context.startTag("runtime");
       while (names.hasMoreElements()) {
          String key   = (String) names.nextElement();
          String value = System.getProperty(key);
 
          if (key != null && value != null && key.length() > 0 && value.length() > 0) {
             context.startTag("property");
             context.attribute("name", key);
             context.pcdata(value);
             context.endTag();
          }
       }
       context.endTag();
    }
 }
