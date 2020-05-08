 
 /*
  * Copyright (c) 1998, 1999 Semiotek Inc. All Rights Reserved.
  *
  * This software is the confidential intellectual property of
  * of Semiotek Inc.; it is copyrighted and licensed, not sold.
  * You may use it under the terms of the GNU General Public License,
  * version 2, as published by the Free Software Foundation. If you 
  * do not want to use the GPL, you may still use the software after
  * purchasing a proprietary developers license from Semiotek Inc.
  *
  * This software is provided "as is", with NO WARRANTY, not even the 
  * implied warranties of fitness to purpose, or merchantability. You
  * assume all risks and liabilities associated with its use.
  *
  * See the attached License.html file for details, or contact us
  * by e-mail at info@semiotek.com to get a copy.
  */
 
 
 package org.webmacro.broker;
 
 import java.lang.*;
 import java.io.*;
 import java.util.*;
 import org.webmacro.*;
 import org.webmacro.util.*;
 
 /**
   * Config represnts the data stored in a WebMacro.properites, and 
   * a few things that can be derived from it; as well as other information
   * known about the environment that is relevant to the proper functioning
   * of the WebMacro system.
   * <p>
   * You can put your own variables into the configuration file, and use 
   * Config to access them. The configuration file is composed of key 
   * value pairs separated by an "=". It can contain comments, which are
   * lines beginning with "#".
   * <p>
   * There are a bunch of standard values in a configuration file. A few
   * of them MUST be set when you first install WebMacro. Most of the
   * settings have relatively sensible defaults.
   * <p>
   * Config is used by thenservlet package's WebContext, Reactor, and Handler to construct the
   * appropriate environment. It also controls the overal logging of 
   * WebMacro information.
   * <p>
   * When using a Config object, you should refer to standard values in the 
   * config file by way of the string constants defined below.
   */
 
 public class Config implements ResourceProvider
 {
 
    /**
      * Constant that contains the Log and ResourceProvider Type 
      * served by this class.
      */
    final public static String TYPE = Broker.CONFIG_TYPE;
 
    // RESOURCE PROVIDER API
 
    final private String[] _types = { TYPE };
 
    /**
      * No concurrency--all in memory operation.
      */
    final public int resourceThreads() { return 0; }
 
    /**
      * We serve up "config" type resources
      */
    final public String[] getTypes() { return _types; }
 
    /**
      * Never cache. Not worth it.
      */
    final public int resourceExpireTime() { return NEVER_CACHE; }
 
    /**
      * Request a configuration value.
      */
    final public void resourceRequest(RequestResourceEvent evt) {
       try {
          evt.set(get(evt.getName()));      
       } catch (Exception e) {
          // revoked or we didn't have it (and tried to set a null)
          // do nothing
       }
    }
 
    
    /**
      * Unimplemented / does nothing
      */
    final public void destroy() { }
 
    /**
      * Unimplemented / does nothing
      */
    final public void init(ResourceBroker r) { }
 
 
    /**
      * Unimplemented / does nothing
      */
    final public void resourceCreate(CreateResourceEvent evt) { }
 
    /**
      * Unimplemented / does nothing
      */
    final public boolean resourceDelete(ResourceEvent evt) { return false; }
 
    /**
      * Unimplemented / does nothing
      */
    final public boolean resourceSave(ResourceEvent evt) { return false; }
 
 
    // STATIC DATA 
 
    /**
      * Used to log events in the same package as Config
      */
    final static Log log = 
       new Log("config","resource broker configuration");
 
    /**
      * A unique id to this VM
      */
    final private static String vmId = Base64.encode(new java.rmi.dgc.VMID().toString()); // Base64 avoids NT filename characters
 
    /**
      * How many instances of config so far
      */
    private static int configCounter = 0;
 
    /**
      * My configuration number, used for constructing unique strings
      */
    final private int myId = nextId();
 
    private static synchronized int nextId() {
       return configCounter++;
    }
 
    /**
      * Unique number counter
      */
    private static long uniqueNumber = 0;
 
    /**
      * Turn on and off debugging in the package local to Config:
      * reactor, servlet, etc.
      */
    final static boolean debug = org.webmacro.util.Log.debug && false;
 
    /**
      * What is the name of our config file.. we need to bootstrap 
      * ourselves using this in the default constructor.
      */
    final private static String[] CONFIG_FILE = 
       { "/WebMacro.properties", "/webmacro.properties",
         "WebMacro.properties", "webmacro.properties", 
         "/webmacro/WebMacro.properties", "/webmacro/webmacro.properties", 
         "/WebMacro/WebMacro.properties", "/WebMacro/webmacro.properties" 
       };
 
 
    // INSTANCE VARIABLES
 
    /**
      * Internally configuration information is stored as a Properties
      * object, here it is.
      */
    /*final*/ private Properties myProps;
 
    // CONSTANTS
 
    /**
      * Platform dependent line separator required to construct 
      * path names for File
      */
    static final public String lineSeparator;
    static {
      String tempSep = System.getProperty("line.separator");
      if (tempSep == null) {
          tempSep = "\n";
      }
       lineSeparator = tempSep;
    }
 
    /**
      * This variable must correspond to a whitespace separated list
      * of resource providers, to be loaded into WebMacro on startup.
      */
    final public static String PROVIDERS = "Providers";
 
    /**
      * Default directory from which to load templates is defined 
      * in the config file under the name TemplateDirectory. The
      * default is to look for a directory called "template" under
      * the current working directory.
      */
    final public static String TEMPLATE_DIR = "TemplatePath";
 
    /**
      * Duration of time to cache templates. Setting this to 
      * zero turns off caching, which is useful during debugging
      * and coding sessions. Set it to a number of milliseconds 
      * for production use (eg: 60000, being 10 * 60 * 1000, is 10 minutes).
      */
    final public static String TEMPLATE_CACHE = "TemplateExpireTime";
 
    /**
      * The file into which the user database is saved. This is defined
      * in the config file under the name UserDB. The default is "user.db"
      * in the current working directory.
      */
    final public static String USER_DB_FILE = "UserFile";
 
    /**
      * Name of the string that gets set in WebContext on an error
      * message is stored in the ConfigFile under the name ,
      * ErrorVariable. By default it is "error" ($error in templates)
      */
    final public static String ERROR_VARIABLE = "ErrorVariable";
 
    /**
      * Name of the log file appears in the config file under 
      * the name LogFile. If it isn't defined, then stderr is used.
      */
    final public static String LOG_FILE = "LogFile";
 
    /**
      * Whether exceptions are traced in the log or not is set in 
      * the log file as the variable LogTraceExceptions and can 
      * have the value (ignoring case) of "true" or "false". Any
      * other value is considered to be false. The default is false.
      */
    final public static String LOG_TRACE_EXCEPTIONS = "LogTraceExceptions";
 
    /**
      * The desired log level appears in the config file under 
      * the name LogLevel. If it isn't defined, then the default 
      * is "WARNING". 
      *
      * Possible values: ALL DEBUG EXCEPTION ERROR WARNING INFO NONE
      */
    final public static String LOG_LEVEL = "LogLevel";
 
 
    /**
      * Construct a new config file, reading from the default location.
      */
    public Config()
       throws BrokerInitException
    {
       this(CONFIG_FILE);
    }
 
    /**
     * Construct a new configuration instance using the specified config
     * file, rather than attempting to use the default.
     */
    public Config(String configFile) 
       throws BrokerInitException
    {
       this( arrayWrap(configFile) );
    }
 
    private static String[] arrayWrap(String name) {
       String[] ret = new String[1];
       ret[0] = name;
       return ret;
    }
 
    /**
      * Construct a configuration instance from the first resolvable
      * name in the supplied list
      */
    public Config(String[] configFiles) 
       throws BrokerInitException
    {
 
       for (int i = 0; i < configFiles.length; i++) {
          try {
             ctor(getStream(configFiles[i]));
             return;
          } catch (Exception e) {
             // do nothing
          }
       }
 
       StringBuffer error = new StringBuffer();
       error.append("Could not locate the WebMacro.properties file in\n"
             + "your classpath.\nThe following variations were tried,\n "
             + "all relative to the directories in your classpath:\n");
       for (int i = 0; i < configFiles.length; i++) {
          error.append("  ");
          error.append(configFiles[i]);
          error.append("\n");
       }
       error.append("Note that you may have several classpaths defined,\n");
       error.append("Your WebMacro.properties file should be located on the\n");
       error.append("classpath as the core WebMacro classes load from.");
       throw new BrokerInitException(error.toString());
    }
 
    /**
     * Read config from the supplied input stream
     */
    public Config(InputStream configStream)
       throws BrokerInitException
    {
       ctor(configStream);
    }
 
    /**
      * Perform the actual construction of the Config
      */
    private void ctor(InputStream configStream) 
       throws BrokerInitException
    {
       // set default values
 
       Properties defaults = new Properties();
 
       defaults.put(TEMPLATE_DIR,"template");
       defaults.put(ERROR_VARIABLE,"error");
       defaults.put(LOG_LEVEL,"WARNING");
       defaults.put(USER_DB_FILE,"user.db");
       // no default for LOG_FILE, let it be null
 
       // load the properties
 
       Properties p = defaults;
       try {
          p = new Properties(defaults);
          p.load(configStream);
          configStream.close();
       } catch (Exception e) {
          String error = "Config: UNABLE TO LOAD CONFIGURATION:\n"
                + "The WebMacro.properties file should be located somewhere "
                + "on the same classpath as was used to load the core WebMacro "
                + "classes. It is usually called \"WebMacro.properties\", "
                + "though your application may have specified another name.";
          throw new BrokerInitException(error);
       } finally {
          myProps = p;
       }
 
       // initialize logging
       try {
          Log.setTarget(get(LOG_FILE));
          Log.setLevel(Log.getConstant(get(LOG_LEVEL)));
          boolean trace = 
             Boolean.valueOf(get(LOG_TRACE_EXCEPTIONS)).booleanValue();
          Log.traceExceptions(trace);
       } catch (Exception e) {
          log.exception(e);
          e.printStackTrace(); // XXX
          log.error("Config: could not correctly initialize logging!");
       }
 
    }
 
    private InputStream getStream(String file)
       throws BrokerInitException
    {
       try {
          Class c = Config.class;
          InputStream in = c.getResourceAsStream(file);
          if (null == in) {
             c = Class.class;
             in = c.getResourceAsStream(file);
          }
          if (null == in) { 
             in = new FileInputStream(file); 
          }
          return in;
       } catch (Exception e) {
          throw new BrokerInitException("Could not read stream: " + e);
       }
    }
 
 
    /**
      * Look up the specified configuration property in the config file 
      * and return the value assigned to it. If it does not exist, instead
      * return the default setting. If there is no default setting and it
      * does not exist, return null.
      * @param configProperty the variable keyword (left hand side)
      * @return the value corresponding to the keyword (right hand side)
      */
    final public String get(String configProperty)
    {
       String ret = myProps.getProperty(configProperty);
       if (ret != null) { 
          ret = ret.trim();
       }
       return ret;
    }
 
    final public static boolean isTrue(String propValue)
    {
       return ((propValue != null) && 
             ((propValue.equalsIgnoreCase("yes")) || 
               propValue.equalsIgnoreCase("true")));
    }
 
 
    /**
      * Return the configuration properties object
      */
    final public Properties getProperties()
    {
       return myProps;
    }
 
    /**
      * Get an ID that uniquely identifies this instance of the server. The
      * intent of this method is to provide a unique string that can be 
      * used to avoid conflicts between multiple simultaneous instances
      * of a servlet. This number is unique across all VM's, and unique
      * across all config objects in this VM. Since each Reactor gets its
      * own copy of Config, this should work...
      * <p>
      * You can use this string to construct temporary files and create
      * other unique strings.
      */
    final public String getServerId()
    {
       return vmId + "-" + myId;
    }
 
    /**
      * Get a string that is unique across all currently running VM's
      */
    final public static synchronized String getUniqueString()
    {
       return vmId + "-" + ++uniqueNumber; 
    }
 
 }
 
