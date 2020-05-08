 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server;
 
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
 
 import org.xins.common.collections.InvalidPropertyValueException;
 import org.xins.common.collections.MissingRequiredPropertyException;
 import org.xins.common.collections.PropertyReader;
 
 import org.xins.common.manageable.BootstrapException;
 import org.xins.common.manageable.DeinitializationException;
 import org.xins.common.manageable.InitializationException;
 import org.xins.common.manageable.Manageable;
 
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
     * String returned by the function <code>_GetStatistics</code> when certain
     * information is not available.
     */
    private static final String NOT_AVAILABLE = "N/A";
 
    /**
     * Successful empty call result.
     */
    private static final FunctionResult SUCCESSFUL_RESULT = new FunctionResult();
 
    /**
     * The runtime (initialization) property that contains the ACL descriptor.
     */
    private static final String ACL_PROPERTY = "org.xins.server.acl";
 
    /**
     * The name of the build property that contains the version of the API.
     */
    private static final String API_VERSION_PROPERTY = "org.xins.api.version";
 
 
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
       _emptyProperties   = new RuntimeProperties();
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The API servlet that owns this <code>API</code> object.
     */
    private APIServlet _apiServlet;
 
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
    * The {@link RuntimeProperies} containing the method to verify and access
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
       _buildHost    = _buildSettings.get("org.xins.api.build.host");
       _buildTime    = _buildSettings.get("org.xins.api.build.time");
       _buildVersion = _buildSettings.get("org.xins.api.build.version");
       _apiVersion = _buildSettings.get(API_VERSION_PROPERTY);
 
       Log.log_3212(_buildHost, _buildTime, _buildVersion, _apiVersion);
 
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
     * Stores a reference to the <code>APIServlet</code> object that owns this
     * <code>API</code> object.
     *
     * @param apiServlet
     *    the {@link APIServlet} instance, should not be <code>null</code>.
     */
    void setAPIServlet(APIServlet apiServlet) {
       _apiServlet = apiServlet;
    }
 
    /**
     * Triggers re-initialization of this API. This method is meant to be
     * called by API function implementations when it is anticipated that the
     * API should be re-initialized.
     */
    protected final void reinitializeImpl() {
       _apiServlet.initAPI();
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
 
       // Handle meta-functions
       if (functionName.charAt(0) == '_') {
 
          FunctionResult result;
 
          if ("_NoOp".equals(functionName)) {
             result = SUCCESSFUL_RESULT;
          } else if ("_GetFunctionList".equals(functionName)) {
             result = doGetFunctionList();
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
          } else if ("_GetVersion".equals(functionName)) {
             result = doGetVersion();
          } else if ("_GetSettings".equals(functionName)) {
             result = doGetSettings();
          } else if ("_DisableFunction".equals(functionName)) {
             result = doDisableFunction(functionRequest.getParameters().get("functionName"));
          } else if ("_EnableFunction".equals(functionName)) {
             result = doEnableFunction(functionRequest.getParameters().get("functionName"));
          } else if ("_ResetStatistics".equals(functionName)) {
             result = doResetStatistics();
          } else if ("_ReloadProperties".equals(functionName)) {
             _apiServlet.reloadPropertiesIfChanged();
             result = SUCCESSFUL_RESULT;
          } else {
             throw new NoSuchFunctionException(functionName);
          }
 
          // Determine duration
          long duration = System.currentTimeMillis() - start;
 
          // Determine error code, fallback is a zero character
          String code = result.getErrorCode();
          if (code == null) {
             code = "0";
          }
 
          // Prepare for transaction logging
          LogdocSerializable serStart  = new FormattedDate(start);
          LogdocSerializable inParams  = new FormattedParameters(functionRequest.getParameters());
          LogdocSerializable outParams = new FormattedParameters(result.getParameters());
 
          // Log transaction before returning the result
          Log.log_3540(serStart, ip, _name, duration, code, inParams, outParams);
          Log.log_3541(serStart, ip, _name, duration, code);
 
          return result;
       }
 
       // Short-circuit if we are shutting down
       if (getState().equals(DEINITIALIZING)) {
          Log.log_3611(_name, functionName);
          return new FunctionResult("_InternalError");
       }
 
       // Get the function object
       Function function = getFunction(functionName);
       if (function == null)  {
          throw new NoSuchFunctionException(functionName);
       }
 
       // Forward the call to the function
       return function.handleCall(start, functionRequest, ip);
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
 
       int count = _functionList.size();
       for (int i = 0; i < count; i++) {
          Function function = (Function) _functionList.get(i);
          ElementBuilder functionElem = new ElementBuilder("function");
          functionElem.setAttribute("name",    function.getName());
          functionElem.setAttribute("version", function.getVersion());
          functionElem.setAttribute("enabled", function.isEnabled() ? "true" : "false");
          builder.add(functionElem.createElement());
       }
 
       return builder;
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
 
       builder.param("startup", DateConverter.toDateString(_timeZone, _startupTimestamp));
       builder.param("now",     DateConverter.toDateString(_timeZone, System.currentTimeMillis()));
 
       // Currently available processors
       Runtime rt = Runtime.getRuntime();
       try {
          builder.param("availableProcessors", String.valueOf(rt.availableProcessors()));
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
       // Function-specific statistics
       int count = _functionList.size();
       for (int i = 0; i < count; i++) {
          Function function = (Function) _functionList.get(i);
          function.getStatistics().resetStatistics();
       }
       return SUCCESSFUL_RESULT;
    }
 
 }
