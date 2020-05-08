 /*
  * $Id$
  */
 package org.xins.server;
 
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.TimeZone;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.collections.InvalidPropertyValueException;
 import org.xins.common.collections.MissingRequiredPropertyException;
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.manageable.BootstrapException;
 import org.xins.common.manageable.DeinitializationException;
 import org.xins.common.manageable.InitializationException;
 import org.xins.common.manageable.Manageable;
 import org.xins.common.text.DateConverter;
 import org.xins.common.text.ParseException;
 
 /**
  * Base class for API implementation classes.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
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
    private static final FunctionResult SUCCESSFUL_RESULT = new FunctionResult();
 
    /**
     * The runtime (init) property that contains the ACL descriptor.
     */
    private static final String ACL_PROPERTY = "org.xins.server.acl";
 
 
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
    *
    * @throws IllegalStateException
    *    if this API is currently not bootstraping.
     */
    protected final void bootstrapImpl(PropertyReader buildSettings)
   throws MissingRequiredPropertyException,
           InvalidPropertyValueException,
           BootstrapException {
 
       // Check state
       Manageable.State state = getState();
       if (state != BOOTSTRAPPING) {
          Log.log_1437(state.getName());
          throw new IllegalStateException("State is " + state + " instead of " + BOOTSTRAPPING + '.');
       }
 
       // Log the time zone
       _timeZone = TimeZone.getDefault();
       String tzShortName = _timeZone.getDisplayName(false, TimeZone.SHORT);
       String tzLongName  = _timeZone.getDisplayName(false, TimeZone.LONG);
       Log.log_1404(tzShortName, tzLongName);
 
       // Store the build-time settings
       _buildSettings = buildSettings;
 
       // Get build-time properties
       _buildHost    = _buildSettings.get("org.xins.api.build.host");
       _buildTime    = _buildSettings.get("org.xins.api.build.time");
       _buildVersion = _buildSettings.get("org.xins.api.build.version");
 
       Log.log_1218(_buildHost, _buildTime, _buildVersion);
 
       // Let the subclass perform initialization
       bootstrapImpl2(buildSettings);
 
       // Bootstrap all instances
       int count = _manageableObjects.size();
       for (int i = 0; i < count; i++) {
          Manageable m = (Manageable) _manageableObjects.get(i);
          String className = m.getClass().getName();
          Log.log_1219(className, _name);
          try {
             m.bootstrap(_buildSettings);
             Log.log_1220(_name, className);
          } catch (MissingRequiredPropertyException exception) {
             Log.log_1221(_name, className, exception.getPropertyName());
             throw exception;
          } catch (InvalidPropertyValueException exception) {
             Log.log_1222(_name, className, exception.getPropertyName(), exception.getPropertyValue());
             throw exception;
          } catch (BootstrapException exception) {
             Log.log_1224(exception, _name, className, exception.getMessage());
             throw exception;
          } catch (Throwable exception) {
             Log.log_1224(exception, _name, className, exception.getMessage());
             throw new BootstrapException(exception);
          }
       }
 
       // Bootstrap all functions
       count = _functionList.size();
       for (int i = 0; i < count; i++) {
          Function f = (Function) _functionList.get(i);
          String functionName = f.getName();
          Log.log_1227(_name, functionName);
          try {
             f.bootstrap(_buildSettings);
             Log.log_1228(_name, functionName);
          } catch (MissingRequiredPropertyException exception) {
             Log.log_1229(_name, functionName, exception.getPropertyName());
             throw exception;
          } catch (InvalidPropertyValueException exception) {
             Log.log_1230(_name, functionName, exception.getPropertyName(), exception.getPropertyValue());
             throw exception;
          } catch (BootstrapException exception) {
             Log.log_1232(exception, _name, functionName, exception.getMessage());
             throw exception;
          } catch (Throwable exception) {
             Log.log_1232(exception, _name, functionName, exception.getMessage());
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
     *
     * @since XINS 0.199
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
          Log.log_1437(state.getName());
          throw new IllegalStateException("State is " + state + " instead of " + INITIALIZING + '.');
       }
 
       Log.log_1405(_name);
 
       // Store runtime settings
       _runtimeSettings = runtimeSettings;
 
       // Initialize ACL subsystem
       String acl = runtimeSettings.get(ACL_PROPERTY);
       if (acl == null || acl.trim().length() < 1) {
          _accessRuleList = AccessRuleList.EMPTY;
          Log.log_1431(ACL_PROPERTY);
       } else {
          try {
             _accessRuleList = AccessRuleList.parseAccessRuleList(acl);
             int ruleCount = _accessRuleList.getRuleCount();
             Log.log_1434(ruleCount);
          } catch (ParseException exception) {
             Log.log_1435(ACL_PROPERTY, acl, exception.getMessage());
             throw new InvalidPropertyValueException(ACL_PROPERTY, acl, exception.getMessage());
          }
       }
 
       // Initialize all instances
       int count = _manageableObjects.size();
       for (int i = 0; i < count; i++) {
          Manageable m = (Manageable) _manageableObjects.get(i);
          String className = m.getClass().getName();
          Log.log_1419(_name, className);
          try {
             m.init(runtimeSettings);
             Log.log_1420(_name, className);
 
          // Missing required property
          } catch (MissingRequiredPropertyException exception) {
             Log.log_1421(_name, className, exception.getPropertyName());
             throw exception;
 
          // Invalid property value
          } catch (InvalidPropertyValueException exception) {
             Log.log_1422(_name, className, exception.getPropertyName(), exception.getPropertyValue());
             throw exception;
 
          // Catch InitializationException and any other exceptions not caught
          // by previous catch statements
          } catch (Throwable exception) {
 
             // Log this event
             Log.log_1424(exception, _name, className, exception.getMessage());
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
          Log.log_1425(_name, functionName);
          try {
             f.init(runtimeSettings);
             Log.log_1426(_name, functionName);
 
          // Missing required property
          } catch (MissingRequiredPropertyException exception) {
             Log.log_1427(_name, functionName, exception.getPropertyName());
             throw exception;
 
          // Invalid property value
          } catch (InvalidPropertyValueException exception) {
             Log.log_1428(_name, functionName, exception.getPropertyName(), exception.getPropertyValue());
             throw exception;
 
          // Catch InitializationException and any other exceptions not caught
          // by previous catch statements
          } catch (Throwable exception) {
 
             // Log this event
             Log.log_1430(exception, _name, functionName);
 
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
 
       Log.log_1406(_name);
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
       if (state != BOOTSTRAPPING) {
          Log.log_1437(state.getName());
          throw new IllegalStateException("State is " + state + " instead of " + BOOTSTRAPPING + '.');
       }
 
       // Check preconditions
       MandatoryArgumentChecker.check("m", m);
       String className = m.getClass().getName();
 
       Log.log_1225(_name, className);
 
       // Store the manageable object in the list
       _manageableObjects.add(m);
 
       Log.log_1226(_name, className);
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
 
          Log.log_1604(_name, className);
          try {
             m.deinit();
             Log.log_1605(_name, className);
          } catch (DeinitializationException exception) {
             Log.log_1606(_name, className, exception.getMessage());
          } catch (Throwable exception) {
             Log.log_1607(exception, _name, className);
          }
       }
       _manageableObjects.clear();
 
       // Deinitialize functions
       count = _functionList.size();
       for (int i = 0; i < count; i++) {
          Function f = (Function) _functionList.get(i);
 
          String functionName = f.getName();
 
          Log.log_1608(_name, functionName);
          try {
             f.deinit();
             Log.log_1609(_name, functionName);
          } catch (DeinitializationException exception) {
             Log.log_1610(_name, functionName, exception.getMessage());
          } catch (Throwable exception) {
             Log.log_1611(exception, _name, functionName);
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
          Log.log_1437(state.getName());
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
     * {@link Function#handleCall(long,PropertyReader,String)}.
     *
     * @param start
     *    the start time of the request, in milliseconds since midnight January
     *    1, 1970.
     *
     * @param parameters
     *    the parameters of the request, never <code>null</code>.
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
     *    if <code>parameters == null</code>.
     *
     * @throws NoSuchFunctionException
     *    if there is no matching function for the specified request.
     *
     * @throws AccessDeniedException
     *    if access is denied for the specified combination of IP address and
     *    function name.
     */
    final FunctionResult handleCall(long start, PropertyReader parameters, String ip)
    throws IllegalStateException,
           NullPointerException,
           NoSuchFunctionException,
           AccessDeniedException {
 
       // Check state first
       assertUsable();
 
       // Determine the function name
       String functionName = parameters.get("_function");
       if (functionName == null || functionName.length() == 0) {
          functionName = parameters.get("function");
       }
 
       // The function name is required
       if (functionName == null || functionName.length() == 0) {
          throw new NoSuchFunctionException(null);
       }
 
       // Check the access rule list
       boolean allow;
       try {
          allow = _accessRuleList.allow(ip, functionName);
       } catch (ParseException exception) {
          throw new Error("Malformed IP address: " + ip + '.');
       }
       if (!allow) {
          throw new AccessDeniedException(ip, functionName);
       }
 
       // Wait until the statistics are returned
       while (_statisticsLocked) {
          synchronized (this) {
             try {
                wait();
             } catch (InterruptedException iex) {
                // empty
             }
          }
       }
 
       // Detect special functions
       if (functionName.charAt(0) == '_') {
          if ("_NoOp".equals(functionName)) {
             return SUCCESSFUL_RESULT;
          } else if ("_GetFunctionList".equals(functionName)) {
             return doGetFunctionList();
          } else if ("_GetStatistics".equals(functionName)) {
             String resetArgument = parameters.get("reset");
             if (resetArgument != null && resetArgument.equals("true")) {
                _statisticsLocked = true;
                FunctionResult result = doGetStatistics();
                doResetStatistics();
                _statisticsLocked = false;
                synchronized (this) {
                   notifyAll();
                }
                return result;
             } else {
                return doGetStatistics();
             }
          } else if ("_GetVersion".equals(functionName)) {
             return doGetVersion();
          } else if ("_GetSettings".equals(functionName)) {
             return doGetSettings();
          } else if ("_DisableFunction".equals(functionName)) {
             return doDisableFunction(parameters);
          } else if ("_EnableFunction".equals(functionName)) {
             return doEnableFunction(parameters);
          } else if ("_ResetStatistics".equals(functionName)) {
             return doResetStatistics();
          } else {
             throw new NoSuchFunctionException(functionName);
          }
       }
 
       // Short-circuit if we are shutting down
       if (getState().equals(DEINITIALIZING)) {
          Log.log_1612(_name, functionName);
          return new FunctionResult("_InternalError");
       }
 
       // Get the function object
       Function function = getFunction(functionName);
       if (function == null)  {
          throw new NoSuchFunctionException(functionName);
       }
 
       // Forward the call to the function
       return function.handleCall(start, parameters, ip);
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
          Element functionElem = new Element("function");
          functionElem.addAttribute("name",    function.getName());
          functionElem.addAttribute("version", function.getVersion());
          functionElem.addAttribute("enabled", function.isEnabled() ? "true" : "false");
          builder.add(functionElem);
       }
 
       return builder;
    }
 
    /**
     * Returns the call statistics for all functions in this API.
     *
     * @return
     *    the call result, never <code>null</code>.
     */
    private final FunctionResult doGetStatistics() {
 
       // Initialize a builder
       FunctionResult builder = new FunctionResult();
 
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
       Element heap = new Element("heap");
       long free  = rt.freeMemory();
       long total = rt.totalMemory();
       heap.addAttribute("used",  String.valueOf(total - free));
       heap.addAttribute("free",  String.valueOf(free));
       heap.addAttribute("total", String.valueOf(total));
       try {
          heap.addAttribute("max", String.valueOf(rt.maxMemory()));
       } catch (NoSuchMethodError error) {
          // ignore: Runtime.maxMemory() is not available in Java 1.3
       }
       builder.add(heap);
 
       // Function-specific statistics
       int count = _functionList.size();
       for (int i = 0; i < count; i++) {
          Function function = (Function) _functionList.get(i);
          FunctionStatistics stats = function.getStatistics();
 
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
 
          Element functionElem = new Element("function");
          functionElem.addAttribute("name", function.getName());
 
          // Successful
          Element successful = new Element("successful");
          successful.addAttribute("count",    String.valueOf(successfulCalls));
          successful.addAttribute("average",  successfulAverage);
          Element successfulMinElem = new Element("min");
          successfulMinElem.addAttribute("start",    successfulMinStart);
          successfulMinElem.addAttribute("duration", successfulMin);
          successful.add(successfulMinElem);
          Element successfulMaxElem = new Element("max");
          successfulMaxElem.addAttribute("start",    successfulMaxStart);
          successfulMaxElem.addAttribute("duration", successfulMax);
          successful.add(successfulMaxElem);
          Element successfulLastElem = new Element("last");
          successfulLastElem.addAttribute("start",    lastSuccessfulStart);
          successfulLastElem.addAttribute("duration", lastSuccessfulDuration);
          successful.add(successfulLastElem);
          functionElem.add(successful);
 
          // Unsuccessful
          Element unsuccessful = new Element("unsuccessful");
          unsuccessful.addAttribute("count",    String.valueOf(unsuccessfulCalls));
          unsuccessful.addAttribute("average",  unsuccessfulAverage);
          Element unsuccessfulMinElem = new Element("min");
          unsuccessfulMinElem.addAttribute("start",    unsuccessfulMinStart);
          unsuccessfulMinElem.addAttribute("duration", unsuccessfulMin);
          unsuccessful.add(unsuccessfulMinElem);
          Element unsuccessfulMaxElem = new Element("max");
          unsuccessfulMaxElem.addAttribute("start",    unsuccessfulMaxStart);
          unsuccessfulMaxElem.addAttribute("duration", unsuccessfulMax);
          unsuccessful.add(unsuccessfulMaxElem);
          Element unsuccessfulLastElem = new Element("last");
          unsuccessfulLastElem.addAttribute("start",    lastUnsuccessfulStart);
          unsuccessfulLastElem.addAttribute("duration", lastUnsuccessfulDuration);
          unsuccessful.add(unsuccessfulLastElem);
          functionElem.add(unsuccessful);
 
          builder.add(functionElem);
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
 
       return builder;
    }
 
    /**
     * Returns the settings.
     *
     * @return
     *    the call result, never <code>null</code>.
     */
    private final FunctionResult doGetSettings() {
 
       FunctionResult builder = new FunctionResult();
 
       // Build settings
       Iterator names = _buildSettings.getNames();
       Element build = new Element("build");
       while (names.hasNext()) {
          String key   = (String) names.next();
          String value = _buildSettings.get(key);
 
          Element property = new Element("property");
          property.addAttribute("name", key);
          property.setText(value);
          build.add(property);
       }
       builder.add(build);
 
       // Runtime settings
       names = _runtimeSettings.getNames();
       Element runtime = new Element("runtime");
       while (names.hasNext()) {
          String key   = (String) names.next();
          String value = _runtimeSettings.get(key);
 
          Element property = new Element("property");
          property.addAttribute("name", key);
          property.setText(value);
          runtime.add(property);
       }
       builder.add(runtime);
 
       // System properties
       Enumeration e = System.getProperties().propertyNames();
       Element system = new Element("system");
       while (e.hasMoreElements()) {
          String key   = (String) e.nextElement();
          String value = System.getProperty(key);
 
          if (key != null && value != null && key.length() > 0 && value.length() > 0) {
             Element property = new Element("property");
             property.addAttribute("name", key);
             property.setText(value);
             system.add(property);
          }
       }
       builder.add(system);
 
       return builder;
    }
 
    /**
     * Enables a function.
     *
     * @param parameters
     *    the request parameters, cannot be <code>null</code>.
     *
     * @return
     *    the call result, never <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>parameters == null</code>.
     */
    private final FunctionResult doEnableFunction(PropertyReader parameters)
    throws NullPointerException {
 
       // Get the name of the function to enable
       String functionName = parameters.get("functionName");
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
     * @param parameters
     *    the request parameters, cannot be <code>null</code>.
     *
     * @return
     *    the call result, never <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>parameters == null</code>.
     */
    private final FunctionResult doDisableFunction(PropertyReader parameters)
    throws NullPointerException {
 
       // Get the name of the function to disable
       String functionName = parameters.get("functionName");
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
