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
 import org.xins.util.collections.PropertyReader;
 import org.xins.util.collections.PropertiesPropertyReader;
 import org.xins.util.collections.expiry.ExpiryFolder;
 import org.xins.util.collections.expiry.ExpiryStrategy;
 import org.xins.util.io.FastStringWriter;
 import org.xins.util.text.DateConverter;
 import org.xins.util.text.FastStringBuffer;
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
     * The <em>INITIAL</em> state.
     */
    private static final State INITIAL = new State("INITIAL");
 
    /**
     * The <em>BOOTSTRAPPING</em> state.
     */
    private static final State BOOTSTRAPPING = new State("BOOTSTRAPPING");
 
    /**
     * The <em>BOOTSTRAPPING_FAILED</em> state.
     */
    private static final State BOOTSTRAPPING_FAILED = new State("BOOTSTRAPPING_FAILED");
 
    /**
     * The <em>BOOTSTRAPPED</em> state.
     */
    private static final State BOOTSTRAPPED = new State("BOOTSTRAPPED");
 
    /**
     * The <em>INITIALIZING</em> state.
     */
    private static final State INITIALIZING = new State("INITIALIZING");
 
    /**
     * The <em>INITIALIZATION_FAILED</em> state.
     */
    private static final State INITIALIZATION_FAILED = new State("INITIALIZATION_FAILED");
 
    /**
     * The <em>INITIALIZED</em> state.
     */
    private static final State INITIALIZED = new State("INITIALIZED");
 
    /**
     * The <em>DISPOSING</em> state.
     */
    private static final State DISPOSING = new State("DISPOSING");
 
    /**
     * The <em>DISPOSED</em> state.
     */
    private static final State DISPOSED = new State("DISPOSED");
 
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
     * Call result to be returned when the function name is missing in the
     * request.
     */
    private static final CallResult MISSING_FUNCTION_NAME_RESULT = new BasicCallResult(false, "MissingFunctionName", null, null);
 
    /**
     * Call result to be returned when the function name does not denote an
     * existing function.
     */
    private static final CallResult NO_SUCH_FUNCTION_RESULT = new BasicCallResult(false, "NoSuchFunction", null, null);
 
 
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
       _stateLock         = new Object();
       _state             = INITIAL;
       _startupTimestamp  = System.currentTimeMillis();
       _lifespanManagers  = new ArrayList();
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
     * The current state.
     */
    private State _state;
 
    /**
     * Lock object for the current state.
     */
    private final Object _stateLock;
 
    /**
     * Flag that indicates if this API is session-based.
     */
    private boolean _sessionBased;
 
    /**
     * Flag that indicates if response validations should be enabled for the
     * functions in this API.
     */
    private boolean _responseValidationEnabled;
 
    /**
     * List of registered lifespan managers. See
     * {@link #add(LifespanManager)}.
     *
     * <p />This field is initialized to a non-<code>null</code> value by the
     * constructor.
     */
    private final List _lifespanManagers;
 
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
     * The name of the default function. Is <code>null</code> if there is no
     * default function.
     */
    private String _defaultFunction;
 
    /**
     * The type that applies for session identifiers. For session-based APIs
     * this will be set in {@link #init(PropertyReader)}.
     */
    private SessionID _sessionIDType;
 
    /**
     * The session ID generator. For session-based APIs this will be set in
     * {@link #init(PropertyReader)}.
     */
    private SessionID.Generator _sessionIDGenerator;
 
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
 
    /**
     * XINS version used to build the web application package.
     */
    private String _buildVersion;
 
    /**
     * The time zone used when generating dates for output.
     */
    private TimeZone _timeZone;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Gets the specified property and converts it to a <code>boolean</code>.
     * Unless the value of the property equals <code>"true"</code>
     * <code>false</code> is returned.
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
    private final boolean getBooleanProperty(PropertyReader properties, String propertyName)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("properties", properties, "propertyName", propertyName);
 
       String value = properties.get(propertyName);
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
     * @return
     *    the value of the property, as an <code>int</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>properties == null || propertyName == null</code>.
     *
     * @throws NumberFormatException
     *    if the conversion to an <code>int</code> failed.
     */
    private final int getIntProperty(PropertyReader properties, String propertyName)
    throws IllegalArgumentException, NumberFormatException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("properties", properties, "propertyName", propertyName);
 
       String value = properties.get(propertyName);
       return Integer.parseInt(value);
    }
 
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
     */
    public final boolean isResponseValidationEnabled() {
       return _responseValidationEnabled;
    }
 
    /**
     * Bootstraps this API (wrapper method). This method calls
     * {@link #bootstrapImpl(PropertyReader)}.
     *
     * @param buildSettings
     *    the build-time configuration properties, not <code>null</code>.
     *
     * @throws IllegalStateException
     *    TODO: determine
     *
     * @throws IllegalArgumentException
     *    if <code>buildSettings == null</code>.
     *
     * @throws InitializationException
     *    if the bootstrapping fails.
     */
    public final void bootstrap(PropertyReader buildSettings)
    throws IllegalStateException, Throwable {
 
       synchronized (_stateLock) {
 
          // Check state
          if (_state != INITIAL) {
             throw new IllegalStateException("The current state is " + _state + " instead of " + INITIAL + '.');
          }
 
          // Check argument
          MandatoryArgumentChecker.check("buildSettings", buildSettings);
 
          // Set the state
          _state = BOOTSTRAPPING;
 
          // Log the time zone
          // TODO: Why log the time zone?
          _timeZone = TimeZone.getDefault();
          String tzLongName  = _timeZone.getDisplayName(false, TimeZone.LONG);
          String tzShortName = _timeZone.getDisplayName(false, TimeZone.SHORT);
          if (tzLongName.equals(tzShortName)) {
             Library.BOOTSTRAP_LOG.info("Local time zone is " + tzLongName + '.');
          } else {
             Library.BOOTSTRAP_LOG.info("Local time zone is " + tzShortName + " (" + tzLongName + ").");
          }
 
          // Store the build-time settings
          _buildSettings = buildSettings;
 
          // Check if a default function is set
          _defaultFunction = _buildSettings.get("org.xins.api.defaultFunction");
          if (_defaultFunction != null) {
             Library.BOOTSTRAP_LOG.debug("Default function set to \"" + _defaultFunction + "\".");
          }
          // TODO: Check that default function exists. If not, set state to
          //       INITIALIZATION_FAILED
 
          // Check if this API is session-based
          _sessionBased = getBooleanProperty(buildSettings, "org.xins.api.sessionBased");
 
          // XXX: Allow configuration of session ID type ?
 
          // Initialize session-based API
          if (_sessionBased) {
             Library.BOOTSTRAP_LOG.debug("Performing session-related initialization.");
 
             // Initialize session ID type
             _sessionIDType      = new BasicSessionID(this);
             _sessionIDGenerator = _sessionIDType.getGenerator();
 
             // Determine session time-out duration and precision
             final long MINUTE_IN_MS = 60000L;
             long timeOut   = MINUTE_IN_MS * (long) getIntProperty(buildSettings, "org.xins.api.sessionTimeOut");
             long precision = MINUTE_IN_MS * (long) getIntProperty(buildSettings, "org.xins.api.sessionTimeOutPrecision");
 
             // Create expiry strategy and folder
             _sessionExpiryStrategy = new ExpiryStrategy(timeOut, precision);
             _sessionsByID          = new ExpiryFolder("sessionsByID",         // name of folder (for logging)
                                                       _sessionExpiryStrategy, // expiry strategy
                                                       false,                  // strict thread sync checking? (TODO)
                                                       5000L);                 // max queue wait time in ms    (TODO)
          }
 
          // Get build-time properties
          _deployment   = _buildSettings.get("org.xins.api.deployment");
          _buildHost    = _buildSettings.get("org.xins.api.build.host");
          _buildTime    = _buildSettings.get("org.xins.api.build.time");
          _buildVersion = _buildSettings.get("org.xins.api.build.version");
 
          // Log build-time properties
          FastStringBuffer buffer = new FastStringBuffer(160);
 
          // - build host name
          buffer.append("Built on ");
          if (_buildHost != null && !("".equals(_buildHost))) {
             buffer.append("host ");
             buffer.append(_buildHost);
          } else {
             Library.BOOTSTRAP_LOG.warn("Build host name is not set.");
             buffer.append("unknown host");
             _buildHost = null;
          }
 
          // - build time
          if (_buildTime != null && !("".equals(_buildTime))) {
             buffer.append(" (at ");
             buffer.append(_buildTime);
             buffer.append(")");
          } else {
             Library.BOOTSTRAP_LOG.warn("Build time stamp is not set.");
             _buildTime = null;
          }
 
          // - deployment
          if (_deployment != null && !("".equals(_deployment))) {
             buffer.append(", for deployment \"");
             buffer.append(_deployment);
             buffer.append('"');
          } else {
             _deployment = null;
          }
 
          // - XINS version
          if (_buildVersion != null && !("".equals(_buildVersion))) {
             buffer.append(", using XINS ");
             buffer.append(_buildVersion);
          } else {
             Library.BOOTSTRAP_LOG.warn("Build version is not set.");
             _buildVersion = null;
          }
 
          buffer.append('.');
          Library.BOOTSTRAP_LOG.info(buffer.toString());
 
          // Let the subclass perform initialization
          try {
             bootstrapImpl(buildSettings);
             _state = BOOTSTRAPPED;
 
          } finally {
             if (_state != BOOTSTRAPPED) {
                _state = BOOTSTRAPPING_FAILED;
             }
          }
 
          // Bootstrap all instances
          int count = _lifespanManagers.size();
          for (int i = 0; i < count; i++) {
             LifespanManager lsm = (LifespanManager) _lifespanManagers.get(i);
             String className = lsm.getClass().getName();
             Library.BOOTSTRAP_LOG.debug("Bootstrapping lifespan manager " + className + " for " + _name + " API.");
             try {
                lsm.bootstrap(_buildSettings);
                Library.BOOTSTRAP_LOG.info("Bootstrapped lifespan manager " + className + " for " + _name +  " API.");
             } catch (Throwable exception) {
                // XXX: The exception is not logged anywhere!
                String message = "Failed to initialize lifespan manager " + className + " for " + _name + " API due to unexpected " + exception.getClass().getName() + '.';
                Library.BOOTSTRAP_LOG.error(message);
                throw new InitializationException(message);
             }
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
     * <p />The {@link #add(LifespanManager)} may be called from this method,
     * and from this method <em>only</em>.
     *
     * @param buildSettings
     *    the build-time properties, guaranteed not to be <code>null</code>.
     *
     * @throws Throwable
     *    if the initialization fails.
     */
    protected void bootstrapImpl(PropertyReader buildSettings)
    throws Throwable {
       // empty
    }
 
    /**
     * Initializes this API.
     *
     * @param runtimeSettings
     *    the runtime configuration settings, cannot be <code>null</code>.
     *
     * @throws InitializationException
     *    if the initialization failed.
     */
    public void init(PropertyReader runtimeSettings)
    throws InitializationException {
 
       // TODO: Check state
 
       Logger log = Library.INIT_LOG;
       log.debug("Initializing " + _name + " API.");
 
       // Store runtime settings
       _runtimeSettings = runtimeSettings;
 
       // Check if response validation is enabled
       _responseValidationEnabled = getBooleanProperty(runtimeSettings, "org.xins.api.responseValidation");
       log.info("Response validation is " + (_responseValidationEnabled ? "enabled." : "disabled."));
 
       // Initialize all instances
       int count = _lifespanManagers.size();
       for (int i = 0; i < count; i++) {
          LifespanManager lsm = (LifespanManager) _lifespanManagers.get(i);
          String className = lsm.getClass().getName();
          log.debug("Initializing lifespan manager " + className + " for " + _name + " API.");
          try {
             lsm.init(runtimeSettings);
             log.info("Initialized lifespan manager " + className + " for " + _name + " API.");
          } catch (Throwable exception) {
             // XXX: The exception is not logged anywhere!
             String message = "Failed to initialize lifespan manager " + className + " for " + _name + " API due to unexpected " + exception.getClass().getName() + '.';
             log.error(message);
             throw new InitializationException(message);
          }
       }
 
       // TODO: Call initImpl(PropertyReader)
 
       log.debug("Initialized " + _name + " API.");
       _state = INITIALIZED;
    }
 
    /**
     * Adds the specified lifespan manager. It will immediately be initialized.
     * If the initialization fails, then an {@link InitializationException}
     * will be thrown.
     *
     * <p>The initialization will be performed by calling
     * {@link LifespanManager#bootstrap(PropertyReader)} and
     * {@link LifespanManager#init(PropertyReader)}.
     *
     * <p>At shutdown time {@link LifespanManager#destroy()} will be called.
     *
     * @param lsm
     *    the lifespan manager to initialize now, reinitialize when appropriate
     *    and deinitialize at shutdown time, not <code>null</code>.
     *
     * @throws IllegalStateException
     *    if this API is currently not bootstrapping.
     *
     * @throws IllegalArgumentException
     *    if <code>instance == null</code>.
     *
     * @throws InitializationException
     *    if the initialization of the instance failed.
     *
     * @since XINS 0.124
     */
    protected final void add(LifespanManager lsm)
    throws IllegalStateException,
           IllegalArgumentException,
           InitializationException {
 
       // Check state
       synchronized (_stateLock) {
          if (_state != BOOTSTRAPPING) {
             // TODO: Log and throw?
             throw new IllegalStateException("State is " + _state + " instead of " + BOOTSTRAPPING + '.');
          }
       }
 
       // Check preconditions
       MandatoryArgumentChecker.check("lsm", lsm);
 
       // Store the lifespan manager in the list
       _lifespanManagers.add(lsm);
 
       Library.BOOTSTRAP_LOG.debug("Added lifespan manager " + lsm.getClass().getName() + " for " + _name + " API.");
    }
 
    /**
     * Performs shutdown of this XINS API. This method will never throw any
     * exception.
     */
    final void destroy() {
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
       int count = _lifespanManagers.size();
       for (int i = 0; i < count; i++) {
          LifespanManager lsm = (LifespanManager) _lifespanManagers.get(i);
 
          String className = lsm.getClass().getName();
 
          try {
             lsm.destroy();
             Library.SHUTDOWN_LOG.info("Deinitialized lifespan manager " + className + " for " + _name + " API.");
          } catch (Throwable exception) {
             Library.SHUTDOWN_LOG.error("Failed to deinitialize lifespan manager " + className + " for " + _name + " API.", exception);
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
     */
    final CallResult handleCall(long start, ServletRequest request)
    throws NullPointerException {
 
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
          return MISSING_FUNCTION_NAME_RESULT;
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
          } else {
             return NO_SUCH_FUNCTION_RESULT;
          }
       }
 
       // Short-circuit if we are shutting down
       if (_shutDown) {
          return new BasicCallResult(false, "InternalError", null, null);
       }
 
       // Get the function object
       Function function  = getFunction(functionName);
       if (function == null)  {
          return NO_SUCH_FUNCTION_RESULT;
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
 
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * State of an <code>API</code>.
     *
     * @version $Revision$ $Date$
     * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
     *
     * @since XINS 0.125
     */
    private static final class State extends Object {
 
       //----------------------------------------------------------------------
       // Constructors
       //----------------------------------------------------------------------
 
       /**
        * Constructs a new <code>State</code> object.
        *
        * @param name
        *    the name of this state, cannot be <code>null</code>.
        *
        * @throws IllegalArgumentException
        *    if <code>name == null</code>.
        */
       private State(String name) throws IllegalArgumentException {
 
          // Check preconditions
          MandatoryArgumentChecker.check("name", name);
 
          _name = name;
       }
 
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       /**
        * The name of this state. Cannot be <code>null</code>.
        */
       private final String _name; 
 
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
 
       /**
        * Returns the name of this state.
        *
        * @return
        *    the name of this state, cannot be <code>null</code>.
        */
       String getName() {
          return _name;
       }
 
       /**
        * Returns a textual representation of this object.
        *
        * @return
        *    the name of this state, never <code>null</code>.
        */
       public String toString() {
          return _name;
       }
    }
 }
