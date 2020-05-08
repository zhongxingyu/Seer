 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server;
 
 import org.xins.common.io.FastStringWriter;
 import java.io.PrintWriter;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.TimeZone;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.Utils;
 import org.xins.common.collections.BasicPropertyReader;
 import org.xins.common.collections.InvalidPropertyValueException;
 import org.xins.common.collections.MissingRequiredPropertyException;
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.manageable.BootstrapException;
 import org.xins.common.manageable.DeinitializationException;
 import org.xins.common.manageable.InitializationException;
 import org.xins.common.manageable.Manageable;
 import org.xins.common.net.IPAddressUtils;
 import org.xins.common.spec.InvalidSpecificationException;
 import org.xins.common.text.DateConverter;
 import org.xins.common.text.ParseException;
 import org.xins.common.xml.Element;
 import org.xins.common.xml.ElementBuilder;
 import org.xins.logdoc.LogdocSerializable;
 
 /**
  * Base class for API implementation classes.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  * @author Tauseef Rehman (<a href="mailto:tauseef.rehman@nl.wanadoo.com">tauseef.rehman@nl.wanadoo.com</a>)
  *
  * @since XINS 1.0.0
  */
 public abstract class API
 extends Manageable
 implements DefaultResultCodes {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * Fully-qualified name of this class.
     */
    private static final String CLASSNAME = API.class.getName();
 
    /**
     * Successful empty call result.
     */
    private static final FunctionResult SUCCESSFUL_RESULT = new FunctionResult();
 
    /**
     * The runtime (initialization) property that defines the ACL (access
     * control list) rules.
     */
    private static final String ACL_PROPERTY = "org.xins.server.acl";
 
    /**
     * The name of the build property that specifies the version of the API.
     */
    private static final String API_VERSION_PROPERTY = "org.xins.api.version";
 
    /**
     * The name of the build property that specifies the hostname of the
     * machine the package was built on.
     */
    private static final String BUILD_HOST_PROPERTY =
       "org.xins.api.build.host";
 
    /**
     * The name of the build property that specifies the time the package was
     * built.
     */
    private static final String BUILD_TIME_PROPERTY =
       "org.xins.api.build.time";
 
    /**
     * The name of the build property that specifies which version of XINS was
     * used to build the package.
     */
    private static final String BUILD_XINS_VERSION_PROPERTY =
       "org.xins.api.build.version";
 
 
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
       _name                = name;
       _startupTimestamp    = System.currentTimeMillis();
       _lastStatisticsReset = _startupTimestamp;
       _manageableObjects   = new ArrayList();
       _functionsByName     = new HashMap();
       _functionList        = new ArrayList();
       _resultCodesByName   = new HashMap();
       _resultCodeList      = new ArrayList();
       _emptyProperties     = new RuntimeProperties();
       _localIPAddress      = IPAddressUtils.getLocalHostIPAddress();
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The engine that owns this <code>API</code> object.
     */
    private Engine _engine;
 
    /**
     * The name of this API. Cannot be <code>null</code> and cannot be an empty
     * string.
     */
    private final String _name;
 
    /**
     * List of registered manageable objects. See {@link #add(Manageable)}.
     *
     * <p />This field is initialized to a non-<code>null</code> value by the
     * constructor.
     */
    private final List _manageableObjects;
 
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
     * The {@link RuntimeProperties} containing the method to verify and access
     * the defined runtime properties.
     */
    private RuntimeProperties _emptyProperties;
 
    /**
     * The runtime-time settings. This field is initialized by
     * {@link #init(PropertyReader)}. It can be <code>null</code> before that.
     */
    private PropertyReader _runtimeSettings;
 
    /**
     * Timestamp indicating when this API instance was created.
     */
    private final long _startupTimestamp;
 
    /**
     * Last time the statistics were reset. Initially the startup timestamp.
     */
    private long _lastStatisticsReset;
 
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
     * Version of the API.
     */
    private String _apiVersion;
 
    /**
     * The access rule list.
     */
    private AccessRuleList _accessRuleList;
 
    /**
     * Indicates whether the API should wait for the statistic to be unlocked
     * before continuing. This field is initially set to <code>false</code>.
     */
    private boolean _statisticsLocked;
 
    /**
     * The API specification.
     */
    private org.xins.common.spec.API _apiSpecification;
 
    /**
     * The local IP address.
     */
    private String _localIPAddress;
 
 
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
     * Gets the properties specified in the implementation.
     *
     * @return
     *    the runtime properties for the API, cannot be <code>null</code>.
     */
    public RuntimeProperties getProperties() {
       return _emptyProperties;
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
     */
    public final TimeZone getTimeZone() {
       return _timeZone;
    }
 
    /**
     * Bootstraps this API (wrapper method). This method calls
     * {@link #bootstrapImpl2(PropertyReader)}.
     *
     * @param buildSettings
     *    the build-time configuration properties, not <code>null</code>.
     *
     * @throws IllegalStateException
     *    if this API is currently not bootstraping.
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
    throws IllegalStateException,
           MissingRequiredPropertyException,
           InvalidPropertyValueException,
           BootstrapException {
 
       // Check state
       Manageable.State state = getState();
       if (state != BOOTSTRAPPING) {
          Log.log_3430(state.getName());
          throw new IllegalStateException("State is " + state + " instead of " + BOOTSTRAPPING + '.');
       }
 
       // Log the time zone
       _timeZone = TimeZone.getDefault();
       String tzShortName = _timeZone.getDisplayName(false, TimeZone.SHORT);
       String tzLongName  = _timeZone.getDisplayName(false, TimeZone.LONG);
       Log.log_3404(tzShortName, tzLongName);
 
       // Store the build-time settings
       _buildSettings = buildSettings;
 
       // Get build-time properties
       _apiVersion   = _buildSettings.get(API_VERSION_PROPERTY       );
       _buildHost    = _buildSettings.get(BUILD_HOST_PROPERTY        );
       _buildTime    = _buildSettings.get(BUILD_TIME_PROPERTY        );
       _buildVersion = _buildSettings.get(BUILD_XINS_VERSION_PROPERTY);
 
       Log.log_3212(_buildHost, _buildTime, _buildVersion, _name, _apiVersion);
 
       // Check if build version identifies a production release of XINS
       if (_buildVersion == null || ! Library.isProductionRelease(_buildVersion)) {
          Log.log_3228(_buildVersion);
       }
 
       // Let the subclass perform initialization
       // TODO: What if bootstrapImpl2 throws an unexpected exception?
       bootstrapImpl2(buildSettings);
 
       // Bootstrap all instances
       int count = _manageableObjects.size();
       for (int i = 0; i < count; i++) {
          Manageable m = (Manageable) _manageableObjects.get(i);
          String className = m.getClass().getName();
          Log.log_3213(_name, className);
          try {
             m.bootstrap(_buildSettings);
             Log.log_3214(_name, className);
 
          // Missing property
          } catch (MissingRequiredPropertyException exception) {
             Log.log_3215(_name, className, exception.getPropertyName());
             throw exception;
 
          // Invalid property
          } catch (InvalidPropertyValueException exception) {
             Log.log_3216(_name, className, exception.getPropertyName(), exception.getPropertyValue(), exception.getReason());
             throw exception;
 
          // Catch BootstrapException and any other exceptions not caught
          // by previous catch statements
          } catch (Throwable exception) {
 
             // Log event
             Log.log_3217(exception, _name, className, exception.getMessage());
 
             // Throw a BootstrapException. If necessary, wrap around the
             // caught exception
             if (exception instanceof BootstrapException) {
                throw (BootstrapException) exception;
             } else {
                throw new BootstrapException(exception);
             }
          }
       }
 
       // Bootstrap all functions
       count = _functionList.size();
       for (int i = 0; i < count; i++) {
          Function f = (Function) _functionList.get(i);
          String functionName = f.getName();
          Log.log_3220(_name, functionName);
          try {
             f.bootstrap(_buildSettings);
             Log.log_3221(_name, functionName);
 
          // Missing required property
          } catch (MissingRequiredPropertyException exception) {
             Log.log_3222(_name, functionName, exception.getPropertyName());
             throw exception;
 
          // Invalid property value
          } catch (InvalidPropertyValueException exception) {
             Log.log_3223(_name, functionName, exception.getPropertyName(), exception.getPropertyValue(), exception.getReason());
             throw exception;
 
          // Catch BootstrapException and any other exceptions not caught
          // by previous catch statements
          } catch (Throwable exception) {
 
             // Log this event
             Log.log_3224(exception, _name, functionName, exception.getMessage());
 
             // Throw a BootstrapException. If necessary, wrap around the
             // caught exception
             if (exception instanceof BootstrapException) {
                throw (BootstrapException) exception;
             } else {
                throw new BootstrapException(exception);
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
     * Stores a reference to the <code>Engine</code> that owns this
     * <code>API</code> object.
     *
     * @param engine
     *    the {@link Engine} instance, should not be <code>null</code>.
     */
    void setEngine(Engine engine) {
       _engine = engine;
    }
 
    /**
     * Triggers re-initialization of this API. This method is meant to be
     * called by API function implementations when it is anticipated that the
     * API should be re-initialized.
     */
    protected final void reinitializeImpl() {
       _engine.initAPI();
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
     *
     * @throws IllegalStateException
     *    if this API is currently not initializing.
     */
    protected final void initImpl(PropertyReader runtimeSettings)
    throws MissingRequiredPropertyException,
           InvalidPropertyValueException,
           InitializationException,
           IllegalStateException {
 
       // Check state
       Manageable.State state = getState();
       if (state != INITIALIZING) {
          Log.log_3430(state.getName());
          throw new IllegalStateException("State is " + state + " instead of " + INITIALIZING + '.');
       }
 
       Log.log_3405(_name);
 
       // Store runtime settings
       _runtimeSettings = runtimeSettings;
 
       // Initialize ACL subsystem
       String acl = runtimeSettings.get(ACL_PROPERTY);
       String aclInterval = runtimeSettings.get(APIServlet.CONFIG_RELOAD_INTERVAL_PROPERTY);
       int interval = APIServlet.DEFAULT_CONFIG_RELOAD_INTERVAL;
       if (aclInterval != null && aclInterval.trim().length() > 0) {
          interval = Integer.parseInt(aclInterval);
       }
 
       // Close the previous ACL
       if (_accessRuleList != null) {
          _accessRuleList.dispose();
       }
       if (acl == null || acl.trim().length() < 1) {
          _accessRuleList = AccessRuleList.EMPTY;
          Log.log_3426(ACL_PROPERTY);
       } else {
          try {
             _accessRuleList = AccessRuleList.parseAccessRuleList(acl, interval);
             int ruleCount = _accessRuleList.getRuleCount();
             Log.log_3427(ruleCount);
          } catch (ParseException exception) {
             Log.log_3428(ACL_PROPERTY, acl, exception.getMessage());
             throw new InvalidPropertyValueException(ACL_PROPERTY, acl, exception.getMessage());
          }
       }
 
       // Initialize the RuntimeProperties object.
       getProperties().init(runtimeSettings);
 
       // Initialize all instances
       int count = _manageableObjects.size();
       for (int i = 0; i < count; i++) {
          Manageable m = (Manageable) _manageableObjects.get(i);
          String className = m.getClass().getName();
          Log.log_3416(_name, className);
          try {
             m.init(runtimeSettings);
             Log.log_3417(_name, className);
 
          // Missing required property
          } catch (MissingRequiredPropertyException exception) {
             Log.log_3418(_name, className, exception.getPropertyName());
             throw exception;
 
          // Invalid property value
          } catch (InvalidPropertyValueException exception) {
             Log.log_3419(_name, className, exception.getPropertyName(), exception.getPropertyValue(), exception.getReason());
             throw exception;
 
          // Catch InitializationException and any other exceptions not caught
          // by previous catch statements
          } catch (Throwable exception) {
 
             // Log this event
             Log.log_3420(exception, _name, className, exception.getMessage());
             if (exception instanceof InitializationException) {
                throw (InitializationException) exception;
             } else {
                throw new InitializationException(exception);
             }
          }
       }
 
       // Initialize all functions
       count = _functionList.size();
       for (int i = 0; i < count; i++) {
          Function f = (Function) _functionList.get(i);
          String functionName = f.getName();
          Log.log_3421(_name, functionName);
          try {
             f.init(runtimeSettings);
             Log.log_3422(_name, functionName);
 
          // Missing required property
          } catch (MissingRequiredPropertyException exception) {
             Log.log_3423(_name, functionName, exception.getPropertyName());
             throw exception;
 
          // Invalid property value
          } catch (InvalidPropertyValueException exception) {
             Log.log_3424(_name, functionName, exception.getPropertyName(), exception.getPropertyValue(), exception.getReason());
             throw exception;
 
          // Catch InitializationException and any other exceptions not caught
          // by previous catch statements
          } catch (Throwable exception) {
 
             // Log this event
             Log.log_3425(exception, _name, functionName);
 
             // Throw an InitializationException. If necessary, wrap around the
             // caught exception
             if (exception instanceof InitializationException) {
                throw (InitializationException) exception;
             } else {
                throw new InitializationException(exception);
             }
          }
       }
 
       // TODO: Call initImpl2(PropertyReader) ?
 
       Log.log_3406(_name);
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
     */
    protected final void add(Manageable m)
    throws IllegalStateException,
           IllegalArgumentException {
 
       // Check state
       Manageable.State state = getState();
       if (state != BOOTSTRAPPING) {
          Log.log_3430(state.getName());
          throw new IllegalStateException("State is " + state + " instead of " + BOOTSTRAPPING + '.');
       }
 
       // Check preconditions
       MandatoryArgumentChecker.check("m", m);
       String className = m.getClass().getName();
 
       Log.log_3218(_name, className);
 
       // Store the manageable object in the list
       _manageableObjects.add(m);
 
       Log.log_3219(_name, className);
    }
 
    /**
     * Performs shutdown of this XINS API. This method will never throw any
     * exception.
     */
    protected final void deinitImpl() {
 
       // Deinitialize instances
       int count = _manageableObjects.size();
       for (int i = 0; i < count; i++) {
          Manageable m = (Manageable) _manageableObjects.get(i);
 
          String className = m.getClass().getName();
 
          Log.log_3603(_name, className);
          try {
             m.deinit();
             Log.log_3604(_name, className);
          } catch (DeinitializationException exception) {
             Log.log_3605(_name, className, exception.getMessage());
          } catch (Throwable exception) {
             Log.log_3606(exception, _name, className);
          }
       }
       _manageableObjects.clear();
 
       // Deinitialize functions
       count = _functionList.size();
       for (int i = 0; i < count; i++) {
          Function f = (Function) _functionList.get(i);
 
          String functionName = f.getName();
 
          Log.log_3607(_name, functionName);
          try {
             f.deinit();
             Log.log_3608(_name, functionName);
          } catch (DeinitializationException exception) {
             Log.log_3609(_name, functionName, exception.getMessage());
          } catch (Throwable exception) {
             Log.log_3610(exception, _name, functionName);
          }
       }
    }
 
    /**
     * Callback method invoked when a function is constructed.
     *
     * @param function
     *    the function that is added, not <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>function == null</code>.
     *
     * @throws IllegalStateException
     *    if this API state is incorrect.
     *
     */
    final void functionAdded(Function function)
    throws NullPointerException, IllegalStateException {
 
       // Check state
       Manageable.State state = getState();
       if (state != UNUSABLE) {
          Log.log_3430(state.getName());
          throw new IllegalStateException("State is " + state + " instead of " + UNUSABLE + '.');
       }
 
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
     * Get the specification of the API.
     *
     * @return
     *    the {@link org.xins.common.spec.API} specification object.
     *
     * @throws InvalidSpecificationException
     *    if the specification cannot be found or is invalid.
     *
     * @see org.xins.common.spec.API
     *
     * @since XINS 1.3.0
     */
    public final org.xins.common.spec.API getAPISpecification()
    throws InvalidSpecificationException {
 
       if (_apiSpecification == null) {
          String baseURL = null;
          try {
             baseURL = _engine.getServletConfig().getServletContext().getResource("specs/").toExternalForm();
          } catch (MalformedURLException muex) {
 
             // Leave the variable as null
          }
          _apiSpecification = new org.xins.common.spec.API(getClass(), baseURL);
       }
       return _apiSpecification;
    }
 
 
    /**
     * Forwards a call to a function. The call will actually be handled by
     * {@link Function#handleCall(long,FunctionRequest,String)}.
     *
     * @param start
     *    the start time of the request, in milliseconds since midnight January
     *    1, 1970.
     *
     * @param functionRequest
     *    the function request, never <code>null</code>.
     *
     * @param ip
     *    the IP address of the requester, never <code>null</code>.
     *
     * @return
     *    the result of the call, never <code>null</code>.
     *
     * @throws IllegalStateException
     *    if this object is currently not initialized.
     *
     * @throws NullPointerException
     *    if <code>functionRequest == null</code>.
     *
     * @throws NoSuchFunctionException
     *    if there is no matching function for the specified request.
     *
     * @throws AccessDeniedException
     *    if access is denied for the specified combination of IP address and
     *    function name.
     */
    final FunctionResult handleCall(long            start,
                                    FunctionRequest functionRequest,
                                    String          ip)
    throws IllegalStateException,
           NullPointerException,
           NoSuchFunctionException,
           AccessDeniedException {
 
       final String THIS_METHOD = "handleCall(long,"
                                + FunctionRequest.class.getName()
                                + ",java.lang.String)";
 
       // Check state first
       assertUsable();
 
       // Determine the function name
       String functionName = functionRequest.getFunctionName();
 
       // Check the access rule list
       boolean allow;
 
       // If no property is defined only localhost is allowed
       if (_accessRuleList == AccessRuleList.EMPTY &&
           (ip.equals("127.0.0.1") || ip.equals(_localIPAddress))) {
          allow = true;
       } else {
          try {
             allow = _accessRuleList.allow(ip, functionName);
 
          // If the IP address cannot be parsed there is a programming error
          // somewhere
          } catch (ParseException exception) {
             final String SUBJECT_CLASS  = _accessRuleList.getClass().getName();
             final String SUBJECT_METHOD = "allow(java.lang.String,java.lang.String)";
             final String DETAIL         = "Malformed IP address: \"" + ip + "\".";
             throw Utils.logProgrammingError(CLASSNAME,
                                             THIS_METHOD,
                                             SUBJECT_CLASS,
                                             SUBJECT_METHOD,
                                             DETAIL,
                                             exception);
          }
       }
       if (!allow) {
          throw new AccessDeniedException(ip, functionName);
       }
 
       // Wait until the statistics are returned. This is indicated by
       // interrupt()-ing this thread.
       while (_statisticsLocked) {
          synchronized (this) {
             try {
                wait();
             } catch (InterruptedException iex) {
                // as expected
             }
          }
       }
 
       // Short-circuit if we are shutting down
       if (getState().equals(DEINITIALIZING)) {
          Log.log_3611(_name, functionName);
          return new FunctionResult("_InternalError");
       }
 
       // Handle meta-functions
       if (functionName.charAt(0) == '_') {
 			try {
             return callMetaFunction(start, functionName, functionRequest, ip);
 			} catch (Throwable exception) {
 				final int callID = 0; // TODO
 				return handleFunctionException(start, functionRequest, ip, callID,
                                            exception);
 			}
       }
 
       // Handle normal functions
       Function function = getFunction(functionName);
       if (function == null)  {
          throw new NoSuchFunctionException(functionName);
       }
       return function.handleCall(start, functionRequest, ip);
    }
 
    /**
     * Handles a call to a meta-function.
     *
     * @param start
     *    the start time of the request, in milliseconds since midnight January
     *    1, 1970.
     *
     * @param functionName
     *    the name of the meta-function, cannot be <code>null</code> and must
     *    start with the underscore character <code>'_'</code>.
     *
     * @param functionRequest
     *    the function request, never <code>null</code>.
     *
     * @param ip
     *    the IP address of the requester, never <code>null</code>.
     *
     * @return
     *    the result of the function call, never <code>null</code>.
     *
     * @throws NoSuchFunctionException
     *    if there is no meta-function by the specified name.
     */
    private FunctionResult callMetaFunction(long            start,
                                            String          functionName,
                                            FunctionRequest functionRequest,
                                            String          ip)
    throws NoSuchFunctionException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("functionName", functionName);
       if (functionName.length() < 1) {
          throw new IllegalArgumentException("functionName.length() < 1");
       } else if (functionName.charAt(0) != '_') {
          throw new IllegalArgumentException("Function name \"" +
                                             functionName +
                                             "\" is not a meta-function.");
       }
 
       FunctionResult result;
 
       // No Operation
       if ("_NoOp".equals(functionName)) {
          result = SUCCESSFUL_RESULT;
 
       // Retrieve function list
       } else if ("_GetFunctionList".equals(functionName)) {
          result = doGetFunctionList();
 
       // Get function call quantity and performance statistics
       } else if ("_GetStatistics".equals(functionName)) {
          String detailedArgument = functionRequest.getParameters().get("detailed");
          boolean detailed = detailedArgument != null && detailedArgument.equals("true");
          String resetArgument = functionRequest.getParameters().get("reset");
          if (resetArgument != null && resetArgument.equals("true")) {
             _statisticsLocked = true;
             result = doGetStatistics(detailed);
             doResetStatistics();
             _statisticsLocked = false;
             synchronized (this) {
                notifyAll();
             }
          } else {
             result = doGetStatistics(detailed);
          }
 
       // Get version information
       } else if ("_GetVersion".equals(functionName)) {
          result = doGetVersion();
 
       // Check links to underlying systems
       } else if ("_CheckLinks".equals(functionName)) {
          result = doCheckLinks();
 
       // Retrieve configuration settings
       } else if ("_GetSettings".equals(functionName)) {
          result = doGetSettings();
 
       // Disable a function
       } else if ("_DisableFunction".equals(functionName)) {
          result = doDisableFunction(functionRequest.getParameters().get("functionName"));
 
       // Enable a function
       } else if ("_EnableFunction".equals(functionName)) {
          result = doEnableFunction(functionRequest.getParameters().get("functionName"));
 
       // Reset the statistics
       } else if ("_ResetStatistics".equals(functionName)) {
          result = doResetStatistics();
 
       // Reload the runtime properties
       } else if ("_ReloadProperties".equals(functionName)) {
          _engine.getConfigManager().reloadPropertiesIfChanged();
          result = SUCCESSFUL_RESULT;
 
       // Meta-function does not exist
       } else {
          throw new NoSuchFunctionException(functionName);
       }
 
       // Determine duration
       long duration = System.currentTimeMillis() - start;
 
       // Determine error code, fallback is a zero character
       String code = result.getErrorCode();
       if (code == null || code.length() < 1) {
          code = "0";
       }
 
       // Prepare for transaction logging
       LogdocSerializable serStart  = new FormattedDate(start);
       LogdocSerializable inParams  = new FormattedParameters(functionRequest.getParameters());
       LogdocSerializable outParams = new FormattedParameters(result.getParameters());
 
       // Log transaction before returning the result
       Log.log_3540(serStart, ip, functionName, duration, code, inParams,
                    outParams);
       Log.log_3541(serStart, ip, functionName, duration, code);
 
       return result;
    }
 
    /**
     * Handles an exception caught while a function was executed.
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
     * @param callID
     *    the call identifier, never <code>null</code>.
     *
     * @param exception
     *    the exception caught, never <code>null</code>.
     *
     * @return
     *    the call result, never <code>null</code>.
     *
     * @throws IllegalStateException
     *    if this object is currently not initialized.
     */
    FunctionResult handleFunctionException(long            start,
                                           FunctionRequest functionRequest,
                                           String          ip,
 														int             callID,
                                           Throwable       exception) {
 
       Log.log_3500(exception, _name, callID);
 
       // Create a set of parameters for the result
       BasicPropertyReader resultParameters = new BasicPropertyReader();
 
       // Add the exception class
       resultParameters.set("_exception.class", exception.getClass().getName());
 
       // Add the exception message, if any
       String exceptionMessage = exception.getMessage();
       if (exceptionMessage != null && exceptionMessage.length() > 0) {
          resultParameters.set("_exception.message", exceptionMessage);
       }
 
       // Add the stack trace, if any
       FastStringWriter stWriter = new FastStringWriter();
       PrintWriter printWriter = new PrintWriter(stWriter);
       exception.printStackTrace(printWriter);
       String stackTrace = stWriter.toString();
       if (stackTrace != null && stackTrace.length() > 0) {
          resultParameters.set("_exception.stacktrace", stackTrace);
       }
 
       return new FunctionResult("_InternalError", resultParameters);
    }
 
    /**
     * Returns a list of all functions in this API. Per function the name and
     * the version are returned.
     *
     * @return
     *    the call result, never <code>null</code>.
     */
    private final FunctionResult doGetFunctionList() {
 
       // Initialize a builder
       FunctionResult builder = new FunctionResult();
 
       // Loop over all functions
       int count = _functionList.size();
       for (int i = 0; i < count; i++) {
 
          // Get some details about the function
          Function function = (Function) _functionList.get(i);
          String name    = function.getName();
          String version = function.getVersion();
          String enabled = function.isEnabled()
                         ? "true"
                         : "false";
 
          // Add an element describing the function
          ElementBuilder functionElem = new ElementBuilder("function");
          functionElem.setAttribute("name",    name   );
          functionElem.setAttribute("version", version);
          functionElem.setAttribute("enabled", enabled);
          builder.add(functionElem.createElement());
       }
 
       return builder;
    }
 
    /**
     * Converts the specified timestamp to a date string.
     *
     * @param millis
     *    the timestamp, as a number of milliseconds since the Epoch.
     *
     * @return
     *    the date string, never <code>null</code>.
     */
    private final String toDateString(long millis) {
       return DateConverter.toDateString(_timeZone, millis);
    }
 
    /**
     * Returns the call statistics for all functions in this API.
     *
     * @param detailed
     *    If <code>true</code>, the unsuccessful result will be returned sorted
     *    per error code. Otherwise the unsuccessful result won't be displayed
     *    by error code.
     *
     * @return
     *    the call result, never <code>null</code>.
     */
    private final FunctionResult doGetStatistics(boolean detailed) {
 
       // Initialize a builder
       FunctionResult builder = new FunctionResult();
 
       builder.param("startup",   toDateString(_startupTimestamp));
       builder.param("lastReset", toDateString(_lastStatisticsReset));
       builder.param("now",       toDateString(System.currentTimeMillis()));
 
       // Currently available processors
       Runtime rt = Runtime.getRuntime();
       try {
          builder.param("availableProcessors",
                        String.valueOf(rt.availableProcessors()));
       } catch (NoSuchMethodError error) {
          // NOTE: Runtime.availableProcessors() is not available in Java 1.3
       }
 
       // Heap memory statistics
       ElementBuilder heap = new ElementBuilder("heap");
       long free  = rt.freeMemory();
       long total = rt.totalMemory();
       heap.setAttribute("used",  String.valueOf(total - free));
       heap.setAttribute("free",  String.valueOf(free));
       heap.setAttribute("total", String.valueOf(total));
       try {
          heap.setAttribute("max", String.valueOf(rt.maxMemory()));
       } catch (NoSuchMethodError error) {
          // NOTE: Runtime.maxMemory() is not available in Java 1.3
       }
       builder.add(heap.createElement());
 
       // Function-specific statistics
       int count = _functionList.size();
       for (int i = 0; i < count; i++) {
          Function function = (Function) _functionList.get(i);
          FunctionStatistics stats = function.getStatistics();
 
          ElementBuilder functionElem = new ElementBuilder("function");
          functionElem.setAttribute("name", function.getName());
 
          // Successful
          Element successful = stats.getSuccessfulElement();
          functionElem.addChild(successful);
 
          // Unsuccessful
          Element[] unsuccessful = stats.getUnsuccessfulElement(detailed);
          for(int j = 0; j < unsuccessful.length; j++) {
             functionElem.addChild(unsuccessful[j]);
          }
 
          builder.add(functionElem.createElement());
       }
 
       return builder;
    }
 
    /**
     * Returns the XINS version.
     *
     * @return
     *    the call result, never <code>null</code>.
     */
    private final FunctionResult doGetVersion() {
 
       FunctionResult builder = new FunctionResult();
 
       builder.param("java.version",   System.getProperty("java.version"));
       builder.param("xmlenc.version", org.znerd.xmlenc.Library.getVersion());
       builder.param("xins.version",   Library.getVersion());
       builder.param("api.version",    _apiVersion);
 
       return builder;
    }
 
    /**
     * Returns the links in linked system components. It uses the 
     * {@link CheckLinks} to connect to each link and builds a 
     * {@link FunctionResult} which will have the total link count and total 
     * link failures.
     *
     * @return
     *    the call result, never <code>null</code>.
     */
    private final FunctionResult doCheckLinks() {
       return CheckLinks.checkLinks(getProperties().descriptors());
    }
 
    /**
     * Returns the settings.
     *
     * @return
     *    the call result, never <code>null</code>.
     */
    private final FunctionResult doGetSettings() {
 
       final String THIS_METHOD  = "doGetSettings()";
 
       FunctionResult builder = new FunctionResult();
 
       // Build settings
       Iterator names = _buildSettings.getNames();
       ElementBuilder build = new ElementBuilder("build");
       while (names.hasNext()) {
          String key   = (String) names.next();
          String value = _buildSettings.get(key);
 
          ElementBuilder property = new ElementBuilder("property");
          property.setAttribute("name", key);
          property.setText(value);
          build.addChild(property.createElement());
       }
       builder.add(build.createElement());
 
       // Runtime settings
       names = _runtimeSettings.getNames();
       ElementBuilder runtime = new ElementBuilder("runtime");
       while (names.hasNext()) {
          String key   = (String) names.next();
          String value = _runtimeSettings.get(key);
 
          ElementBuilder property = new ElementBuilder("property");
          property.setAttribute("name", key);
          property.setText(value);
          runtime.addChild(property.createElement());
       }
       builder.add(runtime.createElement());
 
       // System properties
       Properties sysProps;
       try {
          sysProps = System.getProperties();
       } catch (SecurityException ex) {
          final String SUBJECT_CLASS  = "java.lang.System";
          final String SUBJECT_METHOD = "getProperties()";
          Utils.logProgrammingError(CLASSNAME,     THIS_METHOD,
                                    SUBJECT_CLASS, SUBJECT_METHOD,
                                    null,          ex);
          sysProps = new Properties();
       }
 
       Enumeration e = sysProps.propertyNames();
       ElementBuilder system = new ElementBuilder("system");
       while (e.hasMoreElements()) {
          String key   = (String) e.nextElement();
          String value = sysProps.getProperty(key);
 
          if (  key != null &&   key.trim().length() > 0
           && value != null && value.trim().length() > 0) {
             ElementBuilder property = new ElementBuilder("property");
             property.setAttribute("name", key);
             property.setText(value);
             system.addChild(property.createElement());
          }
       }
       builder.add(system.createElement());
 
       return builder;
    }
 
    /**
     * Enables a function.
     *
     * @param functionName
     *    the name of the function to disable, can be <code>null</code>.
     *
     * @return
     *    the call result, never <code>null</code>.
     */
    private final FunctionResult doEnableFunction(String functionName) {
 
       // Get the name of the function to enable
       if (functionName == null || functionName.length() < 1) {
          InvalidRequestResult invalidRequest = new InvalidRequestResult();
          invalidRequest.addMissingParameter("functionName");
          return invalidRequest;
       }
 
       // Get the Function object
       Function function = getFunction(functionName);
       if (function == null) {
          return new InvalidRequestResult();
       }
 
       // Enable or disable the function
       function.setEnabled(true);
 
       return SUCCESSFUL_RESULT;
    }
 
    /**
     * Disables a function.
     *
     * @param functionName
     *    the name of the function to disable, can be <code>null</code>.
     *
     * @return
     *    the call result, never <code>null</code>.
     */
    private final FunctionResult doDisableFunction(String functionName) {
 
       // Get the name of the function to disable
       if (functionName == null || functionName.length() < 1) {
          InvalidRequestResult invalidRequest = new InvalidRequestResult();
          invalidRequest.addMissingParameter("functionName");
          return invalidRequest;
       }
 
       // Get the Function object
       Function function = getFunction(functionName);
       if (function == null) {
          return new InvalidRequestResult();
       }
 
       // Enable or disable the function
       function.setEnabled(false);
 
       return SUCCESSFUL_RESULT;
    }
 
    /**
     * Resets the statistics.
     *
     * @return
     *    the call result, never <code>null</code>.
     */
    private final FunctionResult doResetStatistics() {
 
       // Remember when we last reset the statistics
       _lastStatisticsReset = System.currentTimeMillis();
 
       // Function-specific statistics
       int count = _functionList.size();
       for (int i = 0; i < count; i++) {
          Function function = (Function) _functionList.get(i);
          function.getStatistics().resetStatistics();
       }
       return SUCCESSFUL_RESULT;
    }
 
 }
