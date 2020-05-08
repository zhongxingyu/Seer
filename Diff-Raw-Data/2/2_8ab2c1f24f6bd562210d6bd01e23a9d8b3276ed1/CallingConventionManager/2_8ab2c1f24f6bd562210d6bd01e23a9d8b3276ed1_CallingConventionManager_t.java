 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server;
 
 import java.lang.reflect.Constructor;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.collections.InvalidPropertyValueException;
 import org.xins.common.collections.MissingRequiredPropertyException;
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.manageable.BootstrapException;
 import org.xins.common.servlet.ServletConfigPropertyReader;
 import org.xins.common.text.TextUtils;
 import org.xins.logdoc.ExceptionUtils;
 
 /**
  * Manages the <code>CallingConvention</code> instances for the API.
  *
 * @version $Revision$ $Date$
  * @author Mees Witteman (<a href="mailto:mees.witteman@nl.wanadoo.com">mees.witteman@nl.wanadoo.com</a>)
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  */
 class CallingConventionManager {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The list of the calling convention included in XINS.
     */
    private final static String[] CONVENTIONS = {
       APIServlet.STANDARD_CALLING_CONVENTION, 
       APIServlet.OLD_STYLE_CALLING_CONVENTION,
       APIServlet.XML_CALLING_CONVENTION,
       APIServlet.XSLT_CALLING_CONVENTION,
       APIServlet.SOAP_CALLING_CONVENTION,
       APIServlet.XML_RPC_CALLING_CONVENTION,
    };
    
    
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Creates the <code>CallingConventionManager</code>.
     *
     * @param servletConfig
     *    the servlet configuration object.
     * @param api
     *    the API.
     *
     * @throws ServletException
     *    if the default calling convention cannot be created.
     */
    CallingConventionManager(ServletConfig servletConfig, API api) 
    throws ServletException {
       
       _servletConfig = servletConfig;
       _api = api;
       _otherConventions = new HashMap();
       
       // Initialize the default calling convention
       initCallingConvention();
    }
    
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The stored servlet configuration object. Never <code>null</code>.
     */
    private final ServletConfig _servletConfig;
 
    /**
     * The stored runtime properties. Can be <code>null</code>.
     */
    private PropertyReader _runtimeProperties;
 
    /**
     * The API. Never <code>null</code>.
     */
    private final API _api;
    
    /**
     * The name of the default calling convention for this engine. This field
     * can never be <code>null</code> and must always be in sync with
     * {@link #_defaultConvention}.
     *
     * <p>If no calling convention is specified in a request, then the default
     * calling convention is used.
     */
    private String _defaultConventionName;
 
    /**
     * The default calling convention for this engine. <p>This field can never
     * be <code>null</code> and must always be in sync with
     * {@link #_defaultConventionName}.
     *
     * <p>If no calling convention is specified in a request, then the default
     * calling convention is used.
     */
    private CallingConvention _defaultConvention;
 
    /**
     * The map containing the calling conventions other than the default one.
     * The key is the name of the calling convention, the value is the calling
     * convention object. This field is never <code>null</code>.
     */
    private final Map _otherConventions;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Determines the default calling convention name from the config object
     * and uses this to create a calling convention. If this does not work out,
     * a default XINS standard calling convention is constructed.
     *
     * @throws ServletException
     *    if the calling convention can not be created.
     */
    private void initCallingConvention() throws ServletException {
 
       try {
          // Determine the name of the default calling convention, as specified
          // in the build-time propertie
          String ccName = _servletConfig.getInitParameter(APIServlet.API_CALLING_CONVENTION_PROPERTY);
 
          // If the name is specified, attempt to construct an instance
          if (! TextUtils.isEmpty(ccName)) {
             _defaultConventionName = ccName;
             _defaultConvention = create(ccName);
 
             // If the factory method returned null, then the specified name
             // does not identify a known calling convention
             if (_defaultConvention == null) {
                Log.log_3210(APIServlet.API_CALLING_CONVENTION_PROPERTY,
                             ccName,
                             "No such calling convention.");
                throw new ServletException();
             }
 
             // TODO: Log that we use the specified calling convention
             // TODO: Log.log_3xxx(_defaultConventionName);
 
          // No calling convention is specified in the build-time properties,
          // so use the standard calling convention
          } else {
             _defaultConventionName = APIServlet.STANDARD_CALLING_CONVENTION;
             _defaultConvention = create(_defaultConventionName);
 
             // TODO: Log that we use the standard calling convention
             // TODO: Log.log_3xxx("_xins-std");
          }
 
       } catch (Throwable t) {
 
          // TODO: Consider catching the exception one level up so we do not
          //       have to generate a ServletException here
 
          // Throw a ServletException
          ServletException se;
          if (t instanceof ServletException) {
             se = (ServletException) t;
          } else {
             se = new ServletException("Calling convention construction failed.");
             ExceptionUtils.setCause(se, t);
          }
          throw se;
       }
       
       // Initialize the other calling conventions.
       for (int i = 0; i < CONVENTIONS.length; i++) {
          String nextConventionName = CONVENTIONS[i];
          if (!nextConventionName.equals(_defaultConventionName)) {
             try {
                CallingConvention nextConvention = create(nextConventionName);
                _otherConventions.put(nextConventionName, nextConvention);
             } catch (Exception ex) {
                
                // Just log a warning.
                Log.log_3560(ex, nextConventionName);
             }
          }
       }
    }
 
    /**
     * Initialized the properties for the default calling convention.
     * This will also clear the cache for the other calling convention.
     *
     * @param runtimeProperties
     *    the runtime properties, never <code>null</code>.
     *
     * @throws Exception
     *    if the default calling convention could not be initialized.
     */
    void init(PropertyReader runtimeProperties) throws Exception {
       _runtimeProperties = runtimeProperties;
       _defaultConvention.init(runtimeProperties);
       Iterator itConventions = _otherConventions.keySet().iterator();
       while (itConventions.hasNext()) {
          String nextConventionName = (String) itConventions.next();
          CallingConvention nextConvention = (CallingConvention) _otherConventions.get(nextConventionName);
          try {
             nextConvention.init(runtimeProperties);
          } catch (Exception ex) {
             Log.log_3561(ex, nextConventionName);
             _otherConventions.remove(nextConventionName);
          }
       }
    }
 
    /**
     * Gets the calling convention for the given name.
     * If the given name is <code>null</code> or empty, the default calling
     * convention is returned.
     * The returned calling convention is bootstraped and inittialized.
     *
     * @param name
     *    the name of the calling convention to retrieve, can be
     *    <code>null</code>.
     *
     * @return
     *    the calling convention, never <code>null</code>
     *
     * @throws InvalidRequestException
     *    if the calling convention name is unknown.
     */
    CallingConvention getCallingConvention(String name) 
    throws InvalidRequestException {
       
       if (TextUtils.isEmpty(name) || name.equals(_defaultConventionName)) {
          return _defaultConvention;
       }
 
       CallingConvention cc = (CallingConvention) _otherConventions.get(name);
       if (cc != null) {
          return cc;
       }
 
       for (int i = 0; i < CONVENTIONS.length; i++) {
          if (name.equals(CONVENTIONS[i])) {
             throw new InvalidRequestException("The calling convention \"" + 
                   name + "\" was not created or initialized correctly.");
          }
       }
       throw new InvalidRequestException("Unknown calling convention: \"" + name + "\".");
    }
    
    /**
     * Creates a calling convention based on a name. If the name does not
     * identify a recognized calling convention, then <code>null</code> is
     * returned.
     *
     * <p>Either an existing {@link CallingConvention} object is retrieved or a
     * new one is constructed.
     *
     * <p>Before returning the {@link CallingConvention} instance, it will be
     * bootstrapped with the properties of the servlet configuration and
     * initialized with the runtime properties (if available).
     *
     * @param name
     *    the name of the calling convention to retrieve, cannot be
     *    <code>null</code>.
     *
     * @return
     *    a bootstrapped {@link CallingConvention} that matches the specified
     *    calling convention name, or <code>null</code> if no match is found.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null</code>.
     *
     * @throws MissingRequiredPropertyException
     *    if the created calling convention requires a bootstrap property that
     *    is missing.
     *
     * @throws InvalidPropertyValueException
     *    if the created calling convention has a bootstrap property with an
     *    incorrect value.
     *
     * @throws BootstrapException
     *    if an error occured during the bootstraping of the calling
     *    convention.
     */
    private CallingConvention create(String name)
    throws IllegalArgumentException,
           MissingRequiredPropertyException,
           InvalidPropertyValueException,
           BootstrapException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("name", name);
 
       CallingConvention created = null;
 
       // Old-style XINS calling convention
       if (APIServlet.OLD_STYLE_CALLING_CONVENTION.equals(name)) {
          created = new OldStyleCallingConvention();
 
       // XINS standard calling convention
       } else if (APIServlet.STANDARD_CALLING_CONVENTION.equals(name)) {
          created = new StandardCallingConvention();
 
       // XINS XML calling convention
       } else if (APIServlet.XML_CALLING_CONVENTION.equals(name)) {
          created = new XMLCallingConvention();
 
       // XINS XSLT calling convention
       } else if (APIServlet.XSLT_CALLING_CONVENTION.equals(name)) {
          created = new XSLTCallingConvention();
 
       // XINS SOAP calling convention
       } else if (APIServlet.SOAP_CALLING_CONVENTION.equals(name)) {
          created = new SOAPCallingConvention(_api);
 
       // XINS XML-RPC calling convention
       } else if (APIServlet.XML_RPC_CALLING_CONVENTION.equals(name)) {
          created = new XMLRPCCallingConvention(_api);
 
       // Custom calling convention
       } else if (name.charAt(0) != '_') {
          if (! name.equals(_servletConfig.getInitParameter(
                 APIServlet.API_CALLING_CONVENTION_PROPERTY))) {
 
             // TODO: Log
             return null;
          }
          String conventionClass = _servletConfig.getInitParameter(
             APIServlet.API_CALLING_CONVENTION_CLASS_PROPERTY);
          try {
 
             // First try with a constructor with the API as parameter then
             // with the empty constructor
             try {
                Class[]  constructorClasses = { API.class };
                Object[] constructorArgs    = { _api      };
                Constructor customConstructor = Class.forName(conventionClass).getConstructor(constructorClasses);
                created = (CustomCallingConvention) customConstructor.newInstance(constructorArgs);
             } catch (NoSuchMethodException nsmex) {
                created = (CustomCallingConvention) Class.forName(conventionClass).newInstance();
             }
          } catch (Exception ex) {
             Log.log_3562(ex, name, conventionClass);
             return null;
          }
 
       // Otherwise return nothing
       } else {
          return null;
       }
 
       // Bootstrap the calling convention
       created.bootstrap(new ServletConfigPropertyReader(_servletConfig));
 
       return created;
    }
 }
