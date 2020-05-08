 /*
  * $Id$
  */
 package org.xins.server;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.TimeZone;
 import javax.servlet.ServletRequest;
 import org.apache.log4j.Logger;
 import org.xins.types.Type;
 import org.xins.types.TypeValueException;
 import org.xins.types.standard.Text;
 import org.xins.util.MandatoryArgumentChecker;
 import org.xins.util.collections.BasicPropertyReader;
 import org.xins.util.collections.InvalidPropertyValueException;
 import org.xins.util.collections.MissingRequiredPropertyException;
 import org.xins.util.collections.PropertyReader;
 import org.xins.util.collections.PropertyReaderUtils;
 import org.xins.util.collections.PropertiesPropertyReader;
 import org.xins.util.collections.expiry.ExpiryFolder;
 import org.xins.util.collections.expiry.ExpiryStrategy;
 import org.xins.util.io.FastStringWriter;
 import org.xins.util.manageable.BootstrapException;
 import org.xins.util.manageable.DeinitializationException;
 import org.xins.util.manageable.InitializationException;
 import org.xins.util.manageable.Manageable;
 import org.xins.util.text.DateConverter;
 import org.xins.util.text.FastStringBuffer;
 import org.xins.util.text.ParseException;
 import org.znerd.xmlenc.XMLOutputter;
 
 /**
  * Base class for API implementation classes.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  */
 public abstract class API
 extends Manageable
 implements DefaultResultCodes {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * String returned by the function <code>_GetStatistics</code> when certain
     * information is not available.
     */
    private static final String NOT_AVAILABLE = "N/A";
 
    /**
     * Successful empty call result.
     */
    private static final CallResult SUCCESSFUL_RESULT = new BasicCallResult(true, null, null, null);
 
    /**
     * The runtime (init) property that contains the ACL descriptor.
     */
    private static final String ACL_PROPERTY = "org.xins.server.acl";
 
    /**
     * The default access rule list.
     */
    private static final String DEFAULT_ACCESS_RULE_LIST = "allow 0.0.0.0/0 *";
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>API</code> object.
     *
     * @param name
     *    the name of the API, cannot be <code>null</code> nor can it be an
     *    empty string.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null
     *          || name.{@link String#length() length()} &lt; 1</code>.
     */
    protected API(String name)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("name", name);
       if (name.length() < 1) {
          throw new IllegalArgumentException("name.length() (" + name.length() + " < 1");
       }
 
       // Initialize fields
       _name              = name;
       _startupTimestamp  = System.currentTimeMillis();
       _manageableObjects = new ArrayList();
       _functionsByName   = new HashMap();
       _functionList      = new ArrayList();
       _resultCodesByName = new HashMap();
       _resultCodeList    = new ArrayList();
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The name of this API. Cannot be <code>null</code> and cannot be an empty
     * string.
     */
    private final String _name;
 
    /**
     * Flag that indicates if this API is session-based.
     */
    private boolean _sessionBased;
 
    /**
     * List of registered manageable objects. See {@link #add(Manageable)}.
     *
     * <p />This field is initialized to a non-<code>null</code> value by the
     * constructor.
     */
    private final List _manageableObjects;
 
    /**
     * Expiry strategy for <code>_sessionsByID</code>.
     *
     * <p />For session-based APIs, this field is initialized to a
     * non-<code>null</code> value by the initialization method
     * {@link #init(PropertyReader)}.
     */
    private ExpiryStrategy _sessionExpiryStrategy;
 
    /**
     * Collection that maps session identifiers to <code>Session</code>
     * instances. Contains all sessions associated with this API.
     *
     * <p />For session-based APIs, this field is initialized to a
     * non-<code>null</code> value by the initialization method
     * {@link #init(PropertyReader)}.
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
     * The build-time settings. This field is initialized exactly once by
     * {@link #bootstrap(PropertyReader)}. It can be <code>null</code> before
     * that.
     */
    private PropertyReader _buildSettings;
 
    /**
     * The runtime-time settings. This field is initialized by
     * {@link #init(PropertyReader)}. It can be <code>null</code> before that.
     */
    private PropertyReader _runtimeSettings;
 
    /**
     * The type that applies for session identifiers. For session-based APIs
     * this will be set in {@link #init(PropertyReader)}.
     */
    private SessionIDType _sessionIDType;
 
    /**
     * The session ID generator. For session-based APIs this will be set in
     * {@link #init(PropertyReader)}.
     */
    private SessionIDType.Generator _sessionIDGenerator;
 
    /**
     * Flag that indicates if the shutdown sequence has been initiated.
     */
    private boolean _shutDown;
    // TODO: Use a state for this
 
    /**
     * Timestamp indicating when this API instance was created.
     */
    private final long _startupTimestamp;
 
    /**
     * Host name for the machine that was used for this build.
     */
    private String _buildHost;
 
    /**
     * Time stamp that indicates when this build was done.
     */
    private String _buildTime;
 
    /**
     * XINS version used to build the web application package.
     */
    private String _buildVersion;
 
    /**
     * The time zone used when generating dates for output.
     */
    private TimeZone _timeZone;
 
    /**
     * The access rule list.
     */
    private AccessRuleList _accessRuleList;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Gets the name of this API.
     *
     * @return
     *    the name of this API, never <code>null</code> and never an empty
     *    string.
     */
    public final String getName() {
       return _name;
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
     * Returns the applicable time zone.
     *
     * @return
     *    the time zone, not <code>null</code>.
     *
     * @since XINS 0.95
     */
    public final TimeZone getTimeZone() {
       return _timeZone;
    }
 
    /**
     * Returns the current number of sessions.
     *
     * @return
     *    the current number of sessions, always &gt;= 0.
     *
     * @throws IllegalStateException
     *    if this API is not session-based.
     *
     * @since XINS 0.95
     */
    public final int getCurrentSessions()
    throws IllegalStateException {
 
       // Check preconditions
       if (! _sessionBased) {
          throw new IllegalStateException("This API is not session-based.");
       }
 
       return _sessionsByID.size();
    }
 
    /**
     * Checks if response validation is enabled.
     *
     * @return
     *    <code>true</code> if response validation is enabled,
     *    <code>false</code> otherwise.
     *
     * @since XINS 0.98
     *
     * @deprecated
     *    Deprecated since XINS 0.157, with no replacement. This method always
     *    returns <code>false</code>.
     */
    public final boolean isResponseValidationEnabled() {
       return false;
    }
 
    /**
     * Bootstraps this API (wrapper method). This method calls
     * {@link #bootstrapImpl2(PropertyReader)}.
     *
     * @param buildSettings
     *    the build-time configuration properties, not <code>null</code>.
     *
     * @throws MissingRequiredPropertyException
     *    if a required property is not given.
     *
     * @throws InvalidPropertyValueException
     *    if a property has an invalid value.
     *
     * @throws BootstrapException
     *    if the bootstrap fails.
     */
    protected final void bootstrapImpl(PropertyReader buildSettings)
    throws MissingRequiredPropertyException,
           InvalidPropertyValueException,
           BootstrapException {
 
       // Log the time zone
       // TODO: Why log the time zone?
       _timeZone = TimeZone.getDefault();
       String tzShortName = _timeZone.getDisplayName(false, TimeZone.SHORT);
       String tzLongName  = _timeZone.getDisplayName(false, TimeZone.LONG);
       Log.log_4004(tzShortName, tzLongName);
 
       // Store the build-time settings
       _buildSettings = buildSettings;
 
       // Check if this API is session-based
       _sessionBased = PropertyReaderUtils.getBooleanProperty(buildSettings, "org.xins.api.sessionBased", false);
       if (_sessionBased) {
          Log.log_2013();
       } else {
          Log.log_2014();
       }
 
       // XXX: Allow configuration of session ID type ?
 
       // Initialize session-based API
       if (_sessionBased) {
          Log.log_2015();
 
          // Initialize session ID type
          _sessionIDType      = new BasicSessionIDType(this);
          _sessionIDGenerator = _sessionIDType.getGenerator();
 
          // Determine session time-out duration and precision (in seconds)
          int timeOut   = PropertyReaderUtils.getIntProperty(buildSettings, "org.xins.api.sessionTimeOut");
          int precision = PropertyReaderUtils.getIntProperty(buildSettings, "org.xins.api.sessionTimeOutPrecision");
 
          Log.log_2017(String.valueOf(timeOut), String.valueOf(precision));
 
          // Create expiry strategy and folder
          final long MINUTE_IN_MS = 60000L;
          _sessionExpiryStrategy = new ExpiryStrategy(timeOut * MINUTE_IN_MS, precision * MINUTE_IN_MS);
          _sessionsByID          = new ExpiryFolder("sessionsByID",         // name of folder (for logging)
                                                    _sessionExpiryStrategy, // expiry strategy
                                                    false,                  // strict thread sync checking? (TODO)
                                                    5000L);                 // max queue wait time in ms    (TODO)
          Log.log_2016();
       }
 
       // Get build-time properties
       _buildHost    = _buildSettings.get("org.xins.api.build.host");
       _buildTime    = _buildSettings.get("org.xins.api.build.time");
       _buildVersion = _buildSettings.get("org.xins.api.build.version");
 
       Log.log_2018(_buildHost, _buildTime, _buildVersion);
 
       // Let the subclass perform initialization
       bootstrapImpl2(buildSettings);
 
       // Bootstrap all instances
       int count = _manageableObjects.size();
       for (int i = 0; i < count; i++) {
          Manageable m = (Manageable) _manageableObjects.get(i);
          String className = m.getClass().getName();
          Log.log_2019(className, _name);
          try {
             m.bootstrap(_buildSettings);
             Log.log_2020(_name, className);
          } catch (MissingRequiredPropertyException exception) {
             Log.log_2021(_name, className, exception.getPropertyName());
             throw exception;
          } catch (InvalidPropertyValueException exception) {
             Log.log_2022(_name, className, exception.getPropertyName(), exception.getPropertyValue());
             throw exception;
          } catch (BootstrapException exception) {
             Log.log_2023(_name, className, exception.getMessage());
             throw exception;
          } catch (Throwable exception) {
             Log.log_2024(_name, className, exception.getClass().getName(), exception.getMessage());
             throw new BootstrapException(exception);
          }
       }
 
       // Bootstrap all functions
       count = _functionList.size();
       for (int i = 0; i < count; i++) {
          Function f = (Function) _functionList.get(i);
          String functionName = f.getName();
         Log.log_2027(_name, functionName);
          try {
             f.bootstrap(_buildSettings);
             Log.log_2028(_name, functionName);
          } catch (MissingRequiredPropertyException exception) {
             Log.log_2029(_name, functionName, exception.getPropertyName());
             throw exception;
          } catch (InvalidPropertyValueException exception) {
             Log.log_2030(_name, functionName, exception.getPropertyName(), exception.getPropertyValue());
             throw exception;
          } catch (BootstrapException exception) {
             Log.log_2031(_name, functionName, exception.getMessage());
             throw exception;
          } catch (Throwable exception) {
             Log.log_2032(_name, functionName, exception.getClass().getName(), exception.getMessage());
             throw new BootstrapException(exception);
          }
       }
    }
 
    /**
     * Bootstraps this API (implementation method).
     *
     * <p />The implementation of this method in class {@link API} is empty.
     * Custom subclasses can perform any necessary bootstrapping in this
     * class.
     *
     * <p />Note that bootstrapping and initialization are different. Bootstrap
     * includes only the one-time configuration of the API based on the
     * build-time settings, while the initialization
     *
     * <p />The {@link #add(Manageable)} may be called from this method,
     * and from this method <em>only</em>.
     *
     * @param buildSettings
     *    the build-time properties, guaranteed not to be <code>null</code>.
     *
     * @throws MissingRequiredPropertyException
     *    if a required property is not given.
     *
     * @throws InvalidPropertyValueException
     *    if a property has an invalid value.
     *
     * @throws BootstrapException
     *    if the bootstrap fails.
     */
    protected void bootstrapImpl2(PropertyReader buildSettings)
    throws MissingRequiredPropertyException,
           InvalidPropertyValueException,
           BootstrapException {
       // empty
    }
 
    /**
     * Initializes this API.
     *
     * @param runtimeSettings
     *    the runtime configuration settings, cannot be <code>null</code>.
     *
     * @throws MissingRequiredPropertyException
     *    if a required property is missing.
     *
     * @throws InvalidPropertyValueException
     *    if a property has an invalid value.
     *
     * @throws InitializationException
     *    if the initialization failed for some other reason.
     */
    protected final void initImpl(PropertyReader runtimeSettings)
    throws MissingRequiredPropertyException,
           InvalidPropertyValueException,
           InitializationException {
 
       // TODO: Check state
 
       // TODO: Perform rollback if initialization fails at some point
 
       Log.log_4005(_name);
 
       // Store runtime settings
       _runtimeSettings = runtimeSettings;
 
       // Initialize ACL subsystem
       Logger log = Library.INIT_ACL_LOG;
       String acl = runtimeSettings.get(ACL_PROPERTY);
       if (acl == null || acl.trim().length() < 1) {
          try {
             log.info("Property \"" + ACL_PROPERTY + "\" not set. Falling back to default: \"" + DEFAULT_ACCESS_RULE_LIST + "\".");
             log.warn("Property \"" + ACL_PROPERTY + "\" not set. Allowing all requests. In the future, this behaviour will change so that if the property is not set, then all requests will be denied instead of allowed.");
             _accessRuleList = AccessRuleList.parseAccessRuleList(DEFAULT_ACCESS_RULE_LIST);
          } catch (ParseException exception) {
             throw new InitializationException("Unable to apply the default access rule list \"" + DEFAULT_ACCESS_RULE_LIST + "\".");
          }
       } else {
          try {
             _accessRuleList = AccessRuleList.parseAccessRuleList(acl);
             int ruleCount = _accessRuleList.getRuleCount();
             if (ruleCount == 1) {
                log.info("Access rule list loaded, with 1 rule.");
             } else {
                log.info("Access rule list loaded, with " + ruleCount + " rules.");
             }
          } catch (ParseException exception) {
             throw new InvalidPropertyValueException(ACL_PROPERTY, acl, exception.getMessage());
          }
       }
 
       // Initialize all instances
       log = Library.INIT_LOG;
       int count = _manageableObjects.size();
       for (int i = 0; i < count; i++) {
          Manageable m = (Manageable) _manageableObjects.get(i);
          String className = m.getClass().getName();
          log.debug("Initializing manageable object of class " + className + " for " + _name + " API.");
          try {
             m.init(runtimeSettings);
             log.debug("Initialized manageable object of class " + className + " for " + _name + " API.");
          } catch (MissingRequiredPropertyException exception) {
             throw exception;
          } catch (InvalidPropertyValueException exception) {
             throw exception;
          } catch (InitializationException exception) {
             throw exception;
          } catch (Throwable exception) {
             FastStringBuffer buffer = new FastStringBuffer(100, "Failed to initialize manageable object of class ");
             buffer.append(className);
             buffer.append(" for ");
             buffer.append(_name);
             buffer.append(" API ");
             buffer.append(APIServlet.dueToUnexpected(exception));
             String message = buffer.toString();
             log.error(message, exception);
             throw new InitializationException(message);
          }
       }
 
       // Initialize all functions
       count = _functionList.size();
       for (int i = 0; i < count; i++) {
          Function f = (Function) _functionList.get(i);
          String functionName = f.getName();
          log.debug("Initializing function " + functionName + " for " + _name + " API.");
          try {
             f.init(runtimeSettings);
             log.debug("Initialized function " + functionName + " for " + _name + " API.");
          } catch (MissingRequiredPropertyException exception) {
             throw exception;
          } catch (InvalidPropertyValueException exception) {
             throw exception;
          } catch (InitializationException exception) {
             throw exception;
          } catch (Throwable exception) {
             FastStringBuffer buffer = new FastStringBuffer(100, "Failed to initialize function ");
             buffer.append(functionName);
             buffer.append(" for ");
             buffer.append(_name);
             buffer.append(" API ");
             buffer.append(APIServlet.dueToUnexpected(exception));
             String message = buffer.toString();
             log.error(message, exception);
             throw new InitializationException(message);
          }
       }
 
       // TODO: Call initImpl2(PropertyReader) ?
 
       Log.log_4006(_name);
    }
 
    /**
     * Adds the specified manageable object. It will not immediately be
     * bootstrapped and initialized.
     *
     * @param m
     *    the manageable object to add, not <code>null</code>.
     *
     * @throws IllegalStateException
     *    if this API is currently not bootstrapping.
     *
     * @throws IllegalArgumentException
     *    if <code>instance == null</code>.
     *
     * @since XINS 0.147
     */
    protected final void add(Manageable m)
    throws IllegalStateException,
           IllegalArgumentException {
 
       // Check state
       Manageable.State state = getState();
       if (getState() != BOOTSTRAPPING) {
          // TODO: Log
          throw new IllegalStateException("State is " + state + " instead of " + BOOTSTRAPPING + '.');
       }
 
       // Check preconditions
       MandatoryArgumentChecker.check("m", m);
       String className = m.getClass().getName();
 
       Log.log_2025(_name, className);
 
       // Store the manageable object in the list
       _manageableObjects.add(m);
 
       Log.log_2026(_name, className);
    }
 
    /**
     * Performs shutdown of this XINS API. This method will never throw any
     * exception.
     */
    protected final void deinitImpl() {
 
       _shutDown = true;
 
       // Stop expiry strategy
       _sessionExpiryStrategy.stop();
 
       // Destroy all sessions
       int openSessionCount = _sessionsByID.size();
       if (openSessionCount == 1) {
          Library.SHUTDOWN_LOG.info("Closing 1 open session.");
       } else {
          Library.SHUTDOWN_LOG.info("Closing " + openSessionCount + " open sessions.");
       }
       _sessionsByID = null;
 
       // Deinitialize instances
       int count = _manageableObjects.size();
       for (int i = 0; i < count; i++) {
          Manageable m = (Manageable) _manageableObjects.get(i);
 
          String className = m.getClass().getName();
 
          try {
             m.deinit();
             Library.SHUTDOWN_LOG.info("Deinitialized manageable object of class " + className + " for " + _name + " API.");
          } catch (DeinitializationException exception) {
             Library.SHUTDOWN_LOG.error("Failed to deinitialize manageable object of class " + className + " for " + _name + " API.", exception);
          }
       }
 
       // Deinitialize functions
       count = _functionList.size();
       for (int i = 0; i < count; i++) {
          Function f = (Function) _functionList.get(i);
 
          String functionName = f.getName();
 
          try {
             f.deinit();
             Library.SHUTDOWN_LOG.info("Deinitialized function " + functionName + " for " + _name + " API.");
          } catch (DeinitializationException exception) {
             Library.SHUTDOWN_LOG.error("Failed to deinitialize function " + functionName + " for " + _name + " API.", exception);
          }
       }
    }
 
    /**
     * Returns the name of the default function, if any.
     *
     * @return
     *    the name of the default function, or <code>null</code> if there is
     *    none.
     *
     * @deprecated
     *    This method is deprecated since XINS 0.157, with no replacement. This
     *    method will always return <code>null</code>.
     */
    public String getDefaultFunctionName() {
       return null;
    }
 
    /**
     * Returns if this API is session-based.
     *
     * @return
     *    <code>true</code> if this API is session-based, or <code>false</code>
     *    if it is not.
     *
     * @throws IllegalStateException
     *    if this API is currently not 'usable'.
     */
    public boolean isSessionBased()
    throws IllegalStateException {
 
       assertUsable();
 
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
     *    if this API is currently not 'usable' or if it is not session-based.
     */
    public final SessionIDType getSessionIDType()
    throws IllegalStateException {
 
       // Check preconditions
       assertUsable();
       if (! _sessionBased) {
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
     *    if this API is currently not 'usable' or if it is not session-based.
     */
    final Session createSession()
    throws IllegalStateException {
 
       // Check preconditions
       assertUsable();
       if (! _sessionBased) {
          throw new IllegalStateException("This API is not session-based.");
       }
 
       // Generate a session ID that does not yet exist
       Object sessionID;
       do {
          sessionID = _sessionIDGenerator.generateSessionID();
       } while (_sessionsByID.get(sessionID) != null);
 
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
     *    if this API is currently not 'usable' or if it is not session-based.
     */
    final Session getSession(Object id)
    throws IllegalStateException {
 
       // Check preconditions
       assertUsable();
       if (! _sessionBased) {
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
     *    if this API is currently not 'usable' or if it is not session-based.
     *
     * @throws TypeValueException
     *    if the specified string is not a valid representation for a value for
     *    the specified type.
     */
    final Session getSessionByString(String idString)
    throws IllegalStateException, TypeValueException {
 
       // Check preconditions
       assertUsable();
       if (! _sessionBased) {
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
     * {@link Function#handleCall(long,ServletRequest)}.
     *
     * @param start
     *    the start time of the request, in milliseconds since midnight January
     *    1, 1970.
     *
     * @param request
     *    the original servlet request, not <code>null</code>.
     *
     * @return
     *    the result of the call, never <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>request == null</code>.
     *
     * @throws NoSuchFunctionException
     *    if there is no matching function for the specified request.
     *
     * @throws AccessDeniedException
     *    if access is denied for the specified combination of IP address and
     *    function name.
     */
    final CallResult handleCall(long start, ServletRequest request)
    throws NullPointerException,
           NoSuchFunctionException,
           AccessDeniedException {
 
       // Determine the function name
       String functionName = request.getParameter("_function");
       if (functionName == null || functionName.length() == 0) {
          functionName = request.getParameter("function");
       }
       if (functionName == null || functionName.length() == 0) {
          functionName = getDefaultFunctionName();
       }
 
       // The function name is required
       if (functionName == null || functionName.length() == 0) {
          throw new NoSuchFunctionException(null);
       }
 
       // Check the access rule list
       String ip = request.getRemoteAddr();
       boolean allow;
       try {
          allow = _accessRuleList.allow(ip, functionName);
       } catch (ParseException exception) {
          throw new Error("Malformed IP address: " + ip + '.');
       }
       if (allow == false) {
          throw new AccessDeniedException(ip, functionName);
       }
 
       // Detect special functions
       if (functionName.charAt(0) == '_') {
          if ("_NoOp".equals(functionName)) {
             return SUCCESSFUL_RESULT;
          } else if ("_PerformGC".equals(functionName)) {
             return doPerformGC();
          } else if ("_GetFunctionList".equals(functionName)) {
             return doGetFunctionList();
          } else if ("_GetStatistics".equals(functionName)) {
             return doGetStatistics();
          } else if ("_GetVersion".equals(functionName)) {
             return doGetVersion();
          } else if ("_GetSettings".equals(functionName)) {
             return doGetSettings();
          } else if ("_DisableFunction".equals(functionName)) {
             return doDisableFunction(request);
          } else if ("_EnableFunction".equals(functionName)) {
             return doEnableFunction(request);
          } else {
             throw new NoSuchFunctionException(functionName);
          }
       }
 
       // Short-circuit if we are shutting down
       if (_shutDown) {
          // TODO: Add message
          return new BasicCallResult(false, "InternalError", null, null);
       }
 
       // Get the function object
       Function function = getFunction(functionName);
       if (function == null)  {
          throw new NoSuchFunctionException(functionName);
       }
 
       // Forward the call to the function
       return function.handleCall(start, request);
    }
 
    /**
     * Performs garbage collection.
     *
     * @return
     *    the call result, never <code>null</code>.
     */
    private final CallResult doPerformGC() {
       System.gc();
       return SUCCESSFUL_RESULT;
    }
 
    /**
     * Returns a list of all functions in this API. Per function the name and
     * the version are returned.
     *
     * @return
     *    the call result, never <code>null</code>.
     */
    private final CallResult doGetFunctionList() {
 
       // Initialize a builder
       CallResultBuilder builder = new CallResultBuilder();
 
       int count = _functionList.size();
       for (int i = 0; i < count; i++) {
          Function function = (Function) _functionList.get(i);
          builder.startTag("function");
          builder.attribute("name",    function.getName());
          builder.attribute("version", function.getVersion());
          builder.attribute("enabled", function.isEnabled() ? "true" : "false");
          builder.endTag();
       }
 
       return builder;
    }
 
    /**
     * Returns the call statistics for all functions in this API.
     *
     * @return
     *    the call result, never <code>null</code>.
     */
    private final CallResult doGetStatistics() {
 
       // Initialize a builder
       CallResultBuilder builder = new CallResultBuilder();
 
       builder.param("startup", DateConverter.toDateString(_timeZone, _startupTimestamp));
       builder.param("now",     DateConverter.toDateString(_timeZone, System.currentTimeMillis()));
 
       // Currently available processors
       Runtime rt = Runtime.getRuntime();
       try {
          builder.param("availableProcessors", String.valueOf(rt.availableProcessors()));
       } catch (NoSuchMethodError error) {
          // ignore: Runtime.availableProcessors() is not available in Java 1.3
       }
 
       // Heap memory statistics
       builder.startTag("heap");
       long free  = rt.freeMemory();
       long total = rt.totalMemory();
       builder.attribute("used",  String.valueOf(total - free));
       builder.attribute("free",  String.valueOf(free));
       builder.attribute("total", String.valueOf(total));
       try {
          builder.attribute("max", String.valueOf(rt.maxMemory()));
       } catch (NoSuchMethodError error) {
          // ignore: Runtime.maxMemory() is not available in Java 1.3
       }
       builder.endTag(); // heap
 
       // Function-specific statistics
       int count = _functionList.size();
       for (int i = 0; i < count; i++) {
          Function function = (Function) _functionList.get(i);
          Function.Statistics stats = function.getStatistics();
 
          long successfulCalls      = stats.getSuccessfulCalls();
          long unsuccessfulCalls    = stats.getUnsuccessfulCalls();
          long successfulDuration   = stats.getSuccessfulDuration();
          long unsuccessfulDuration = stats.getUnsuccessfulDuration();
 
          String successfulAverage;
          String successfulMin;
          String successfulMinStart;
          String successfulMax;
          String successfulMaxStart;
          String lastSuccessfulStart;
          String lastSuccessfulDuration;
          if (successfulCalls == 0) {
             successfulAverage      = NOT_AVAILABLE;
             successfulMin          = NOT_AVAILABLE;
             successfulMinStart     = NOT_AVAILABLE;
             successfulMax          = NOT_AVAILABLE;
             successfulMaxStart     = NOT_AVAILABLE;
             lastSuccessfulStart    = NOT_AVAILABLE;
             lastSuccessfulDuration = NOT_AVAILABLE;
          } else if (successfulDuration == 0) {
             successfulAverage      = "0";
             successfulMin          = String.valueOf(stats.getSuccessfulMin());
             successfulMinStart     = DateConverter.toDateString(_timeZone, stats.getSuccessfulMinStart());
             successfulMax          = String.valueOf(stats.getSuccessfulMax());
             successfulMaxStart     = DateConverter.toDateString(_timeZone, stats.getSuccessfulMaxStart());
             lastSuccessfulStart    = DateConverter.toDateString(_timeZone, stats.getLastSuccessfulStart());
             lastSuccessfulDuration = String.valueOf(stats.getLastSuccessfulDuration());
          } else {
             successfulAverage      = String.valueOf(successfulDuration / successfulCalls);
             successfulMin          = String.valueOf(stats.getSuccessfulMin());
             successfulMinStart     = DateConverter.toDateString(_timeZone, stats.getSuccessfulMinStart());
             successfulMax          = String.valueOf(stats.getSuccessfulMax());
             successfulMaxStart     = DateConverter.toDateString(_timeZone, stats.getSuccessfulMaxStart());
             lastSuccessfulStart    = DateConverter.toDateString(_timeZone, stats.getLastSuccessfulStart());
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
             unsuccessfulAverage      = NOT_AVAILABLE;
             unsuccessfulMin          = NOT_AVAILABLE;
             unsuccessfulMinStart     = NOT_AVAILABLE;
             unsuccessfulMax          = NOT_AVAILABLE;
             unsuccessfulMaxStart     = NOT_AVAILABLE;
             lastUnsuccessfulStart    = NOT_AVAILABLE;
             lastUnsuccessfulDuration = NOT_AVAILABLE;
          } else if (unsuccessfulDuration == 0) {
             unsuccessfulAverage      = "0";
             unsuccessfulMin          = String.valueOf(stats.getUnsuccessfulMin());
             unsuccessfulMinStart     = DateConverter.toDateString(_timeZone, stats.getUnsuccessfulMinStart());
             unsuccessfulMax          = String.valueOf(stats.getUnsuccessfulMax());
             unsuccessfulMaxStart     = DateConverter.toDateString(_timeZone, stats.getUnsuccessfulMaxStart());
             lastUnsuccessfulStart    = DateConverter.toDateString(_timeZone, stats.getLastUnsuccessfulStart());
             lastUnsuccessfulDuration = String.valueOf(stats.getLastUnsuccessfulDuration());
          } else {
             unsuccessfulAverage      = String.valueOf(unsuccessfulDuration / unsuccessfulCalls);
             unsuccessfulMin          = String.valueOf(stats.getUnsuccessfulMin());
             unsuccessfulMinStart     = DateConverter.toDateString(_timeZone, stats.getUnsuccessfulMinStart());
             unsuccessfulMax          = String.valueOf(stats.getUnsuccessfulMax());
             unsuccessfulMaxStart     = DateConverter.toDateString(_timeZone, stats.getUnsuccessfulMaxStart());
             lastUnsuccessfulStart    = DateConverter.toDateString(_timeZone, stats.getLastUnsuccessfulStart());
             lastUnsuccessfulDuration = String.valueOf(stats.getLastUnsuccessfulDuration());
          }
 
          builder.startTag("function");
          builder.attribute("name", function.getName());
 
          // Successful
          builder.startTag("successful");
          builder.attribute("count",    String.valueOf(successfulCalls));
          builder.attribute("average",  successfulAverage);
          builder.startTag("min");
          builder.attribute("start",    successfulMinStart);
          builder.attribute("duration", successfulMin);
          builder.endTag(); // min
          builder.startTag("max");
          builder.attribute("start",    successfulMaxStart);
          builder.attribute("duration", successfulMax);
          builder.endTag(); // max
          builder.startTag("last");
          builder.attribute("start",    lastSuccessfulStart);
          builder.attribute("duration", lastSuccessfulDuration);
          builder.endTag(); // last
          builder.endTag(); // successful
 
          // Unsuccessful
          builder.startTag("unsuccessful");
          builder.attribute("count",    String.valueOf(unsuccessfulCalls));
          builder.attribute("average",  unsuccessfulAverage);
          builder.startTag("min");
          builder.attribute("start",    unsuccessfulMinStart);
          builder.attribute("duration", unsuccessfulMin);
          builder.endTag(); // min
          builder.startTag("max");
          builder.attribute("start",    unsuccessfulMaxStart);
          builder.attribute("duration", unsuccessfulMax);
          builder.endTag(); // max
          builder.startTag("last");
          builder.attribute("start",    lastUnsuccessfulStart);
          builder.attribute("duration", lastUnsuccessfulDuration);
          builder.endTag(); // last
          builder.endTag(); // unsuccessful
 
          builder.endTag(); // function
       }
 
       return builder;
    }
 
    /**
     * Returns the XINS version.
     *
     * @return
     *    the call result, never <code>null</code>.
     */
    private final CallResult doGetVersion() {
 
       CallResultBuilder builder = new CallResultBuilder();
 
       builder.param("java.version",   System.getProperty("java.version"));
       builder.param("xmlenc.version", org.znerd.xmlenc.Library.getVersion());
       builder.param("xins.version",   Library.getVersion());
 
       return builder;
    }
 
    /**
     * Returns the settings.
     *
     * @return
     *    the call result, never <code>null</code>.
     */
    private final CallResult doGetSettings() {
 
       CallResultBuilder builder = new CallResultBuilder();
 
       // Build settings
       Iterator names = _buildSettings.getNames();
       builder.startTag("build");
       while (names.hasNext()) {
          String key   = (String) names.next();
          String value = _buildSettings.get(key);
 
          builder.startTag("property");
          builder.attribute("name", key);
          builder.pcdata(value);
          builder.endTag();
       }
       builder.endTag();
 
       // Runtime settings
       names = _runtimeSettings.getNames();
       builder.startTag("runtime");
       while (names.hasNext()) {
          String key   = (String) names.next();
          String value = _runtimeSettings.get(key);
 
          builder.startTag("property");
          builder.attribute("name", key);
          builder.pcdata(value);
          builder.endTag();
       }
       builder.endTag();
 
       // System properties
       Enumeration e = System.getProperties().propertyNames();
       builder.startTag("system");
       while (e.hasMoreElements()) {
          String key   = (String) e.nextElement();
          String value = System.getProperty(key);
 
          if (key != null && value != null && key.length() > 0 && value.length() > 0) {
             builder.startTag("property");
             builder.attribute("name", key);
             builder.pcdata(value);
             builder.endTag();
          }
       }
       builder.endTag();
 
       return builder;
    }
 
    /**
     * Enables a function.
     *
     * @param request
     *    the servlet request, cannot be <code>null</code>.
     *
     * @return
     *    the call result, never <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>request == null</code>.
     */
    private final CallResult doEnableFunction(ServletRequest request)
    throws NullPointerException {
 
       // Get the name of the function to enable
       String functionName = request.getParameter("functionName");
       if (functionName == null || functionName.length() < 1) {
          return new BasicCallResult(false, "MissingParameters", null, null);
       }
 
       // Get the Function object
       Function function = getFunction(functionName);
       if (function == null) {
          return new BasicCallResult(false, "InvalidParameters", null, null);
       }
 
       // Enable or disable the function
       function.setEnabled(true);
 
       return SUCCESSFUL_RESULT;
    }
 
    /**
     * Disables a function.
     *
     * @param request
     *    the servlet request, cannot be <code>null</code>.
     *
     * @return
     *    the call result, never <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>request == null</code>.
     */
    private final CallResult doDisableFunction(ServletRequest request)
    throws NullPointerException {
 
       // Get the name of the function to disable
       String functionName = request.getParameter("functionName");
       if (functionName == null || functionName.length() < 1) {
          return new BasicCallResult(false, "MissingParameters", null, null);
       }
 
       // Get the Function object
       Function function = getFunction(functionName);
       if (function == null) {
          return new BasicCallResult(false, "InvalidParameters", null, null);
       }
 
       // Enable or disable the function
       function.setEnabled(false);
 
       return SUCCESSFUL_RESULT;
    }
 }
