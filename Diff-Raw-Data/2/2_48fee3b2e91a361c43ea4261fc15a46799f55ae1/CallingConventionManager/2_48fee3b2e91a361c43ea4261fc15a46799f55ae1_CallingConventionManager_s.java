 /*
  * $Id$
  *
  * Copyright 2003-2007 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server;
 
 import java.lang.reflect.Constructor;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.xins.common.Utils;
 import org.xins.common.collections.InvalidPropertyValueException;
 import org.xins.common.collections.MissingRequiredPropertyException;
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.manageable.BootstrapException;
 import org.xins.common.manageable.InitializationException;
 import org.xins.common.manageable.Manageable;
 import org.xins.common.text.TextUtils;
 
 /**
  * Manages the <code>CallingConvention</code> instances for the API.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:mees.witteman@orange-ftgroup.com">Mees Witteman</a>
  * @author <a href="mailto:anthony.goubard@orange-ftgroup.com">Anthony Goubard</a>
  * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
  *
  * @see CallingConvention
  */
 class CallingConventionManager extends Manageable {
 
    /**
     * List of the names of the calling conventions currently included in
     * XINS.
     */
    private final static List CONVENTIONS = Arrays.asList(new String[] {
       APIServlet.STANDARD_CALLING_CONVENTION,
       APIServlet.XML_CALLING_CONVENTION,
       APIServlet.XSLT_CALLING_CONVENTION,
       APIServlet.SOAP_CALLING_CONVENTION,
       APIServlet.XML_RPC_CALLING_CONVENTION,
       APIServlet.JSON_RPC_CALLING_CONVENTION,
       APIServlet.JSON_CALLING_CONVENTION
    });
 
    /**
     * Array of type <code>Class</code> that is used when constructing a
     * <code>CallingConvention</code> instance via RMI.
     */
    private final static Class[] CONSTRUCTOR_ARG_CLASSES = { API.class };
 
    /**
     * Placeholder object used to indicate that the construction of a calling
     * convention object failed. Never <code>null</code>.
     */
    private final static Object CREATION_FAILED = new Object();
 
    /**
     * The API. Never <code>null</code>.
     */
    private final API _api;
 
    /**
     * The name of the default calling convention. There is always a default
     * calling convention (at least after bootstrapping).
     *
     * <p>This field is initialized during bootstrapping.
     */
    private String _defaultConventionName;
 
    /**
     * The names of the possible calling conventions.
     */
    private List _conventionNames;
 
    /**
     * Map containing all calling conventions. The key is the name of the
     * calling convention, the value is the calling convention object, or
     * {@link #CREATION_FAILED} if the calling convention object could not be
     * constructed.
     */
    private final HashMap _conventions;
 
    /**
     * Creates a <code>CallingConventionManager</code> for the specified API.
     *
     * @param api
     *    the API, cannot be <code>null</code>.
     */
    CallingConventionManager(API api) {
 
       // Store the reference to the API
       _api = api;
 
       // Fill the list of the convention names with the pre defined conventions
       _conventionNames = new ArrayList();
       _conventionNames.addAll(CONVENTIONS);
 
       // Create a map to store the conventions in
       _conventions = new HashMap(12);
 
    }
 
    /**
     * Performs the bootstrap procedure (actual implementation).
     *
     * @param properties
     *    the bootstrap properties, not <code>null</code>.
     *
     * @throws MissingRequiredPropertyException
     *    if a required property is not given.
     *
     * @throws InvalidPropertyValueException
     *    if the value of a certain property is invalid.
     *
     * @throws BootstrapException
     *    if the bootstrapping failed for any other reason.
     */
    protected void bootstrapImpl(PropertyReader properties)
    throws MissingRequiredPropertyException,
           InvalidPropertyValueException,
           BootstrapException {
 
       // Determine the name and class of the custom calling convention
       _defaultConventionName = determineDefaultConvention(properties);
 
       // Append the defined calling conventions
       Iterator itCustomCC = properties.getNames();
       while (itCustomCC.hasNext()) {
          String nextProperty = (String) itCustomCC.next();
          if (nextProperty.startsWith(APIServlet.API_CALLING_CONVENTION_PROPERTY + '.') &&
              !nextProperty.equals(APIServlet.API_CALLING_CONVENTION_CLASS_PROPERTY)) {
             String conventionName = nextProperty.substring(32, nextProperty.length() - 6);
             _conventionNames.add(conventionName);
          }
       }
 
       // Construct and bootstrap the default calling convention
       CallingConvention cc = create(properties, _defaultConventionName);
 
       // If created, store the object and attempt bootstrapping
       if (cc != null) {
          _conventions.put(_defaultConventionName, cc);
          bootstrap(_defaultConventionName, cc, properties);
 
          if (cc.getState() != Manageable.BOOTSTRAPPED) {
             throw new BootstrapException("Failed to bootstrap the default calling convention.");
          }
 
       // Otherwise, if it's the default calling convention, fails
       } else {
          throw new BootstrapException("Failed to create the default calling convention.");
       }
    }
 
    /**
     * Determines the default calling convention.
     *
     * @param properties
     *    the bootstrap properties, cannot be <code>null</code>.
     *
     * @return
     *    the name of the default calling convention, never <code>null</code>.
     *
     * @throws MissingRequiredPropertyException
     *    if a required property is not given.
     *
     * @throws InvalidPropertyValueException
     *    if the value of a certain property is invalid.
     */
    private String determineDefaultConvention(PropertyReader properties)
    throws MissingRequiredPropertyException,
           InvalidPropertyValueException {
 
       // Name of the default calling convention (if any)
       String name = TextUtils.trim(properties.get(APIServlet.API_CALLING_CONVENTION_PROPERTY), null);
 
       // No calling convention defined
       if (name == null) {
 
          // Log: No custom calling convention specified
          Log.log_3246();
 
          // Fallback to the XINS-specified default calling convention
          name = APIServlet.STANDARD_CALLING_CONVENTION;
       }
 
       // Log: Determined default calling convention
       Log.log_3245(name);
 
       // Return the name of the default calling convention
       return name;
    }
 
    /**
     * Constructs the calling convention with the specified name, using the
     * specified bootstrap properties. This method is called for both
     * <em>regular</em> and <em>custom</em> calling conventions.
     *
     * <p>If the name does not identify a recognized calling convention, then
     * <code>null</code> is returned.
     *
     * @param properties
     *    the bootstrap properties, cannot be <code>null</code>.
     *
     * @param name
     *    the name of the calling convention to construct, cannot be
     *    <code>null</code>.
     *
     * @return
     *    a non-bootstrapped {@link CallingConvention} instance that matches
     *    the specified name, or <code>null</code> if no match is found.
     */
    private CallingConvention create(PropertyReader properties, String name) {
 
       // Determine the name of the CallingConvention class
       String className = null;
       if (name.charAt(0) == '_') {
          className = classNameForRegular(name);
       } else {
          className = properties.get(APIServlet.API_CALLING_CONVENTION_PROPERTY + '.' + name + ".class");
       }
 
       // If the class could not be determined, then return null
       if (className == null) {
          Log.log_3239(null, name, null);
          return null;
       }
 
       Log.log_3237(name, className);
 
       // Construct a CallingConvention instance
       CallingConvention cc = construct(name, className);
 
       // NOTE: Logging of construction failures is done in construct(...)
 
       // Constructed successfully
       if (cc != null) {
          Log.log_3238(name, className);
          cc.setAPI(_api);
       }
 
       return cc;
    }
 
    /**
     * Determines the name of the class that represents the regular calling
     * convention with the specified name. A <em>regular</em> calling
     * convention is one that comes with the XINS framework.
     *
     * @param name
     *    the name of the calling convention, should not be <code>null</code>
     *    and should normally starts with an underscore character
     *    (<code>'_'</code>).
     *
     * @return
     *    the name of the {@link CallingConvention} class that matches the
     *    specified calling convention name, or <code>null</code> if unknown.
     */
    private String classNameForRegular(String name) {
 
       // XINS standard
       if (name.equals(APIServlet.STANDARD_CALLING_CONVENTION)) {
          return "org.xins.server.StandardCallingConvention";
 
       // XINS XML
       } else if (name.equals(APIServlet.XML_CALLING_CONVENTION)) {
          return "org.xins.server.XMLCallingConvention";
 
       // XSLT
       } else if (name.equals(APIServlet.XSLT_CALLING_CONVENTION)) {
          return "org.xins.server.XSLTCallingConvention";
 
       // SOAP
       } else if (name.equals(APIServlet.SOAP_CALLING_CONVENTION)) {
          return "org.xins.server.SOAPCallingConvention";
 
       // XML-RPC
       } else if (name.equals(APIServlet.XML_RPC_CALLING_CONVENTION)) {
          return "org.xins.server.XMLRPCCallingConvention";
 
       // JSON-RPC
       } else if (name.equals(APIServlet.JSON_RPC_CALLING_CONVENTION)) {
          return "org.xins.server.JSONRPCCallingConvention";
 
       // JSON
       } else if (name.equals(APIServlet.JSON_CALLING_CONVENTION)) {
          return "org.xins.server.JSONCallingConvention";
 
       // Unrecognized
       } else {
          return null;
       }
    }
 
    /**
     * Constructs a new <code>CallingConvention</code> instance by class name.
     *
     * @param name
     *    the name of the calling convention, cannot be <code>null</code>.
     *
     * @param className
     *    the name of the class, cannot be <code>null</code>.
     *
     * @return
     *    the constructed {@link CallingConvention} instance, or
     *    <code>null</code> if the construction failed.
     */
    private CallingConvention construct(String name, String className) {
 
       // Try to load the class
       Class clazz;
       try {
          clazz = Class.forName(className);
       } catch (Throwable exception) {
          Log.log_3239(exception, name, className);
          return null;
       }
 
       // Get the constructor that accepts an API argument
       Constructor con = null;
       try {
          con = clazz.getConstructor(CONSTRUCTOR_ARG_CLASSES);
       } catch (NoSuchMethodException exception) {
          // fall through, do not even log
       }
 
       // If there is such a constructor, invoke it
       if (con != null) {
 
          // Invoke it
          Object[] args = { _api };
          try {
             return (CallingConvention) con.newInstance(args);
 
          // If the constructor exists but failed, then construction failed
          } catch (Throwable exception) {
             Utils.logIgnoredException(exception);
             return null;
          }
       }
 
       // Secondly try a constructor with no arguments
       try {
          return (CallingConvention) clazz.newInstance();
       } catch (Throwable exception) {
          Log.log_3239(exception, name, className);
          return null;
       }
    }
 
    /**
     * Bootstraps the specified calling convention.
     *
     * @param name
     *    the name of the calling convention, cannot be <code>null</code>.
     *
     * @param cc
     *    the {@link CallingConvention} object to bootstrap, cannot be
     *    <code>null</code>.
     *
     * @param properties
     *    the bootstrap properties, cannot be <code>null</code>.
     */
    private void bootstrap(String name, CallingConvention cc, PropertyReader properties) {
 
       // Bootstrapping calling convention
       Log.log_3240(name);
 
       try {
          cc.bootstrap(properties);
          Log.log_3241(name);
 
       // Missing property
       } catch (MissingRequiredPropertyException exception) {
          Log.log_3242(name, exception.getPropertyName(),
                       exception.getDetail());
 
       // Invalid property
       } catch (InvalidPropertyValueException exception) {
          Log.log_3243(name,
                       exception.getPropertyName(),
                       exception.getPropertyValue(),
                       exception.getReason());
 
       // Catch BootstrapException and any other exceptions not caught
       // by previous catch statements
       } catch (Throwable exception) {
          Log.log_3244(exception, name);
       }
    }
 
    /**
     * Performs the initialization procedure (actual implementation).
     *
     * @param properties
     *    the initialization properties, not null.
     *
     * @throws MissingRequiredPropertyException
     *    if a required property is not given.
     *
     * @throws InvalidPropertyValueException
     *    if the value of a certain property is invalid.
     *
     * @throws InitializationException
     *    if the initialization failed, for any other reason.
     */
    protected void initImpl(PropertyReader properties)
    throws MissingRequiredPropertyException,
           InvalidPropertyValueException,
           InitializationException {
 
       // Loop through all CallingConvention instances
       Iterator iterator = _conventions.entrySet().iterator();
       while (iterator.hasNext()) {
 
          // Determine the name and get the CallingConvention instance
          Map.Entry entry = (Map.Entry) iterator.next();
          String    name  = (String) entry.getKey();
          Object    cc    = entry.getValue();
 
          // Process this CallingConvention only if it was created OK
          if (cc != CREATION_FAILED) {
 
             // Initialize the CallingConvention
             CallingConvention conv = (CallingConvention) cc;
             init(name, conv, properties);
 
             // Fail if the *default* calling convention fails to initialize
             if (!conv.isUsable() && name.equals(_defaultConventionName)) {
                throw new InitializationException("Failed to initialize the default calling convention \"" + name + "\".");
             }
          }
       }
    }
 
    /**
     * Initializes the specified calling convention.
     *
     * <p>If the specified calling convention is not even bootstrapped, the
     * initialization is not even attempted.
     *
     * @param name
     *    the name of the calling convention, cannot be <code>null</code>.
     *
     * @param cc
     *    the {@link CallingConvention} object to initialize, cannot be
     *    <code>null</code>.
     *
     * @param properties
     *    the initialization properties, cannot be <code>null</code>.
     */
    private void init(String name, CallingConvention cc, PropertyReader properties) {
 
       // If the CallingConvention is not even bootstrapped, then do not even
       // attempt to initialize it
       if (! cc.isBootstrapped()) {
          return;
       }
 
       // Initialize calling convention
       Log.log_3435(name);
 
       try {
          cc.init(properties);
 
       // Missing property
       } catch (MissingRequiredPropertyException exception) {
          Log.log_3437(name, exception.getPropertyName(),
                       exception.getDetail());
 
       // Invalid property
       } catch (InvalidPropertyValueException exception) {
          Log.log_3438(name,
                       exception.getPropertyName(),
                       exception.getPropertyValue(),
                       exception.getReason());
 
       // Catch InitializationException and any other exceptions not caught
       // by previous catch statements
       } catch (Throwable exception) {
          Log.log_3439(exception, name);
       }
    }
 
    /**
     * Determines the calling convention to use for the specified request.
     *
     * @param request
     *    the incoming request, cannot be <code>null</code>.
     *
     * @return
     *    the calling convention to use, never <code>null</code>.
     *
     * @throws InvalidRequestException
     *    if the request is considered invalid, for example because the calling
     *    convention specified in the request is unknown.
     */
    CallingConvention getCallingConvention(HttpServletRequest request)
    throws InvalidRequestException {
 
       // Get the value of the input parameter that determines the convention
       String paramName = APIServlet.CALLING_CONVENTION_PARAMETER;
       String ccName    = request.getParameter(paramName);
 
       // If a calling convention is specified then use that one
       if (! TextUtils.isEmpty(ccName)) {
          CallingConvention cc = getCallingConvention(ccName);
          if (! Arrays.asList(cc.getSupportedMethods(request)).contains(request.getMethod()) && !"OPTIONS".equals(request.getMethod())) {
             String detail = "Calling convention \"" + ccName +
                   "\" does not support the \"" + request.getMethod() + "\" for this request.";
             Log.log_3507(ccName, detail);
             throw new InvalidRequestException(detail);
          }
          return cc;
 
       // Otherwise try to detect which one is appropriate
       } else {
          return detectCallingConvention(request);
       }
    }
 
    /**
     * Gets the calling convention for the given name.
     *
     * <p>The returned calling convention is bootstrapped and initialized.
     *
     * @param name
     *    the name of the calling convention to retrieve, should not be
     *    <code>null</code>.
     *
     * @return
     *    the calling convention initialized, never <code>null</code>.
     *
     * @throws InvalidRequestException
     *    if the calling convention name is unknown.
     */
    private CallingConvention getCallingConvention(String name)
    throws InvalidRequestException {
 
       // Get the CallingConvention object
       Object o = _conventions.get(name);
 
       // Not found
       if (o == null && !_conventionNames.contains(name)) {
             String detail = "Calling convention \"" + name + "\" is unknown.";
             Log.log_3507(name, detail);
             throw new InvalidRequestException(detail);
       } else if (o == null) {
 
          // Create the asked calling convention and initiaze it
          CallingConvention cc = create(_api.getBootstrapProperties(), name);
 
          // If created, store the object and attempt bootstrapping
          if (cc != null) {
             o = cc;
             _conventions.put(name, cc);
             bootstrap(name, cc, _api.getBootstrapProperties());
             init(name, cc, _api.getRuntimeProperties());
          } else {
             o = CREATION_FAILED;
             _conventions.put(name, o);
          }
       }
 
       // Creation failed
       if (o == CREATION_FAILED) {
          String detail = "Calling convention \""
                        + name
                        + "\" is known, but could not be created.";
          Log.log_3507(name, detail);
          throw new InvalidRequestException(detail);
 
       // Calling convention is recognized and was created OK
       } else {
 
          // Not usable (so not bootstrapped and initialized)
          CallingConvention cc = (CallingConvention) o;
          if (! cc.isUsable()) {
             String detail = "Calling convention \""
                           + name
                           + "\" is known, but is uninitialized.";
             Log.log_3507(name, detail);
             throw new InvalidRequestException(detail);
          }
 
          return cc;
       }
    }
 
    /**
     * Gets the calling convention for the given name, or <code>null</code> if
     * the calling convention is not found or not usable.
     *
     * <p>The returned calling convention is bootstrapped and initialized.
     *
     * @param name
     *    the name of the calling convention to retrieve, should not be
     *    <code>null</code>.
     *
     * @return
     *    the calling convention, or <code>null</code>.
     */
    CallingConvention getCallingConvention2(String name) {
 
       try {
          return getCallingConvention(name);
       } catch (InvalidRequestException ex) {
          return null;
       }
    }
 
    /**
     * Attempts to detect which calling convention is the most appropriate for
     * an incoming request. This method is called when the calling convention
     * is not explicitly specified in the request.
     *
     * <p>The {@link CallingConvention#matchesRequest(HttpServletRequest)}
     * method is used to determine which calling conventions match. Then
     * the following algorithm is used to chose one:
     *
     * <ul>
     *    <li>if the default calling convention matches, use that;
     *    <li>otherwise if the {@link XSLTCallingConvention} matches and at
     *        least one of the parameters specific for the this calling
     *        convention is set, then use it;
     *    <li>otherwise if the {@link StandardCallingConvention} matches, use
     *        that;
     *    <li>otherwise if there is exactly one other calling convention that
     *        matches, use that one;
     *    <li>if none of the calling conventions match, throw an
     *        {@link InvalidRequestException}, indicating that no match could
     *        be found;
     *    <li>if multiple calling conventions match, throw an
     *        {@link InvalidRequestException}, indicating that several matches
     *        were found;
     * </ul>
     *
     * @param request
     *    the incoming request, cannot be <code>null</code>.
     *
     * @return
     *    the calling convention to use, never <code>null</code>.
     *
     * @throws InvalidRequestException
     *    if the request is considered invalid, for example because the calling
     *    convention specified in the request is unknown.
     */
    CallingConvention detectCallingConvention(HttpServletRequest request)
    throws InvalidRequestException {
 
       // Log: Request does not specify any calling convention
       Log.log_3508();
 
       // See if the default calling convention matches
       CallingConvention defCC = getCallingConvention2(_defaultConventionName);
       if (defCC != null && defCC.matchesRequest(request)) {
          Log.log_3509(defCC.getClass().getName());
          return defCC;
       }
 
       // If not, see if XSLT-specific properties are set /and/ _xins-xslt matches
       CallingConvention xslCC = getCallingConvention2("_xins-xslt");
       if (xslCC != null && xslCC != defCC && xslCC.matchesRequest(request)) {
 
          // Determine if one of the two XSLT-specific parameters is set
          String p1 = request.getParameter(XSLTCallingConvention.TEMPLATE_PARAMETER);
          String p2 = request.getParameter(XSLTCallingConvention.CLEAR_TEMPLATE_CACHE_PARAMETER);
 
          // Use the XSLT calling convention if and only if at least one of the
          // parameters is actually set
          if (! (TextUtils.isEmpty(p1) && TextUtils.isEmpty(p2))) {
             Log.log_3509(XSLTCallingConvention.class.getName());
             return xslCC;
          }
       }
 
       // If not, see if _xins-std matches
       CallingConvention stdCC = getCallingConvention2("_xins-std");
       if (stdCC != null && stdCC != defCC && stdCC.matchesRequest(request)) {
          Log.log_3509(StandardCallingConvention.class.getName());
          return stdCC;
       }
 
       // Local variable to hold the first matching calling convention
       CallingConvention matching = null;
 
       // Determine which calling conventions match
       Iterator itConventionNames = _conventionNames.iterator();
       while (itConventionNames.hasNext()) {
          String name = (String) itConventionNames.next();
          Object value = getCallingConvention2(name);
 
          // if the value is null, that's maybe an initialization problem
          if (value == null) {
             value = _conventions.get(name);
          }
 
          // Skip all values that are not CallingConvention instances
          // Skip also the default and the standard calling conventions, we
          // already established that they cannot handle the request
          if (value == CREATION_FAILED || value == defCC || value == stdCC) {
             continue;
          }
 
          // Convert the value to a CallingConvention
          CallingConvention cc = (CallingConvention) value;
 
          // Determine whether this one can handle it
          if (cc.matchesRequest(request)) {
 
             // First match
             if (matching == null) {
                matching = cc;
 
             // Fail: Multiple matches
             } else {
                Log.log_3511();
                String multipleMatches = "Request does not specify a calling "
                      + "convention, it cannot be handled by the "
                      + "default calling convention and multiple "
                      + "calling conventions are able to handle it: \"";
                String message = multipleMatches + matching.getClass().getName()
                      + "\", \"" + cc.getClass().getName() + "\".";
                throw new InvalidRequestException(message);
             }
          }
       }
 
       // One match
       if (matching != null) {
          return matching;
 
       // Fail: No matches
       } else {
          Log.log_3510();
          String noMatches = "Request does not specify a calling convention, it "
               + "cannot be handled by the default calling convention and it was"
                + "not possible to find any calling convention that can handle it.";
          throw new InvalidRequestException(noMatches);
       }
    }
 
    /**
     * Returns the set of HTTP methods supported for function invocations. This
     * is the union of the methods supported by the individual calling
     * conventions for invoking functions, so excluding the <em>OPTIONS</em>
     * method. The latter cannot be used for function invocations, only to
     * determine which HTTP methods are available. See
     * {@link CallingConvention#getSupportedMethods()}.
     *
     * @return
     *    the {@link Set} of supported HTTP methods, never <code>null</code>.
     *
     * @throws IllegalStateException
     *    if this calling convention manager is not yet bootstrapped and
     *    initialized, see {@link #isUsable()}.
     */
    final Set getSupportedMethods() throws IllegalStateException {
 
       // Make sure this Manageable object is bootstrapped and initialized
       assertUsable();
 
       HashSet supportedMethods = new HashSet();
       Iterator itConventionNames = _conventionNames.iterator();
       while (itConventionNames.hasNext()) {
 
          String name = (String) itConventionNames.next();
          Object convention = getCallingConvention2(name);
 
          // if the value is null, that's maybe an initialization problem
          if (convention == null) {
             convention = _conventions.get(name);
          }
 
          // Add all methods supported by the calling convention
          if (convention instanceof CallingConvention) {
             CallingConvention cc = (CallingConvention) convention;
             supportedMethods.addAll(Arrays.asList(cc.getSupportedMethods()));
          }
       }
 
       return supportedMethods;
    }
 }
