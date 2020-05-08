 /**
  *   Copyright 2013 Christian Hilmersson
  *
  *   This file is part of config-bootstrapper 
  *   http://www.github.com/chilmers/config-bootstrapper
  *
  *   config-bootstrapper is free software; you can redistribute it and/or modify
  *   it under the terms of version 2.1 of the GNU Lesser General Public
  *   License as published by the Free Software Foundation.
  *
  *   config-bootstrapper is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public
  *   License along with config-bootstrapper; if not, write to the
  *   Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  *   Boston, MA 02111-1307  USA
  */
 
 package com.chilmers.configbootstrapper;
 
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.MissingResourceException;
 import java.util.PropertyResourceBundle;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 import org.apache.log4j.xml.DOMConfigurator;
 
 /**
  * <p>
  * Helps initializing logging and application configuration for a system.<br/>
  * <br/>
  * It does two main things:<br/>
  * <ul>
  *  <li> Finds out which application configuration file to use, and sets it as a system property (by default "application.config.location") <br/>
  *      making it possible for the application to use separate configurations for separate environments. <br/>
  *  </li>
  *  <li>    
  *      Loads the logging configuration in a way that makes it possible to<br/> 
  *      use different log4j configurations in different environments.<br/>
  *  </li>
  * <ul>
  * </p>
  * <br/>
  * 
  * <b>Finding correct application configuration</b><br/>
  * <p>
  * This context listener will in the given order look in the system properties, environment variables<br/>
  * or the servlet context parameters for the location of a configuration file to use in the application.<br/>
  * By default it will look for an entry with the key "application.config.location". (This key name can be overridden, see Overriding defaults below)<br/>
 * If no such entry is found it will by default fall back to using "classpath:/application.properties" as configuration location. (The fallback location can also be overridden if necessary, see below)<br/>
  * <br/>
  * Whichever configuration location string is decided, will be set in the system properties using the same key as above, 
  * i.e by default it will be set in system property "application.config.location"<br/> 
  * This makes it possible to locate the configuration from within the application, for example by reading it into a 
  * PropertyResourceBundle or by using Spring's PropertyPlaceholderConfigurer like this:
  * <br/>
  *  <pre>  
  *  &lt;bean id="placeholderConfig" class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer"&gt;
  *      &lt;property name="location" value="${application.config.location}" /&gt;
  *  &lt;/bean&gt;
  *  </pre>
  *  Or, if you have the context namespace defined, simply:
  *  <pre>
  *  &lt;context:property-placeholder location="${application.config.location}"/&gt;
  *  </pre>
  * <br/>
  * <br/> 
  * <b>Logging configuration (i.e. Log4j)</b><br/>
  * This mechanism configures Log4j using a given file whose location is stated in the application configuration, 
  * or if no such file is available falls back to Log4j's default configuration behavior, <br/>
  * i.e. looks for log4j.xml or log4j.properties on the classpath.<br/>
  * <br/>
  * To use an external log4j file you will have to state the location of the file in the application configuration as 
  * a property with the key given by "application.log4j.config.location". (This key name can be overriden, see Overriding defaults below)<br/>
  * </p>
  * <br/>
  * <br/> 
  * <b>Usage</b><br/>
  * <p>
  * Add this to your web.xml and make sure it is located before any application specific listeners that need
  * to use the configuration location property or that needs to log. E.g. before Spring's ContextLoaderListener<br/>
  * <pre>
  *  &lt;listener&gt;
  *      &lt;listener-class&gt;com.chilmers.configbootstrapper.ConfigServletContextListener&lt;/listener-class&gt;
  *  &lt;/listener&gt;
  * </pre>
  * 
 * If you want to specify an external configuration file (instead of the default "classpath:/application.properties"), <br/>
  * add a context-param or more likely a system property or environment variable stating the location of your application configuration. <br/> 
  * For example:<br/>
  * <br/>
  *  As context param:<br/>
  *  <pre>
  *  &lt;context-param&gt;
  *      &lt;description&gt;The location of the application configuration. 
 *          If not set it defaults to classpath:/application.properties&lt;/description&gt;
  *      &lt;param-name&gt;application.config.location&lt;/param-name&gt;
  *      &lt;param-value&gt;/Users/myusername/my-app-config/app.properties&lt;/param-value&gt;
  *  &lt;/context-param&gt;
  *  </pre>
  * <br/>
  *  As environment variable in a bash shell:<br/>
  *  <pre>export application.config.location=/Users/myusername/my-app-config/app.properties</pre>
  *  <br/>
  *  As a system property upon starting your container:<br/>
  *  <pre>java [your application] -Dapplication.config.location=/Users/myusername/my-app-config/app.properties</pre><br/>
  * </p>
  * <br/>
  * <b>Overriding defaults</b><br/>
  * <p>
  * The following context-parameters can be set to configure the listener.<br/>
  * All of them have default values so they don't have to be set if not needed<br/>
  * <pre>
  *  &lt;context-param&gt;
  *      &lt;description&gt;Sets the key for the entry that holds the application configuration location. 
  *          If not set it defaults to application.config.location&lt;/description&gt;
  *      &lt;param-name&gt;configServletContextListener.configLocationPropertyKey&lt;/param-name&gt;
  *      &lt;param-value&gt;myown.config.location&lt;/param-value&gt;
  *  &lt;/context-param&gt;
  *  </pre>
  * <pre>
  *  &lt;context-param&gt;
  *      &lt;description&gt;Sets the key for where in the application configuration file to look for a log4j
  *          configuration file location.
  *          If not set it defaults to application.log4j.config.location&lt;/description&gt;
  *      &lt;param-name&gt;configServletContextListener.log4jConfigLocationPropertyKey&lt;/param-name&gt;
  *      &lt;param-value&gt;myown.log4j.config.location&lt;/param-value&gt;
  *  &lt;/context-param&gt;
  *  </pre>
  * <pre>
  *  &lt;context-param&gt;
  *      &lt;description&gt;Sets the location of the application configuration to fall back to if no other configuration
  *          file location was set. E.g. a bundled configuration on the classpath. 
 *          If not set it defaults to classpath:/application.properties&lt;/description&gt;
  *      &lt;param-name&gt;configServletContextListener.fallbackConfigLocation&lt;/param-name&gt;
 *      &lt;param-value&gt;classpath:/myown.properties&lt;/param-value&gt;
  *  &lt;/context-param&gt;
  *  </pre>
  * <pre>
  *  &lt;context-param&gt;
  *      &lt;description&gt;
  *      Application name that is printed when using System.out logging when no logging manager is available.
  *      Defaults to the display-name of the web.xml or if no display-name exists it will be ConfigServletContextListener
  *      &lt;/description&gt;
  *      &lt;param-name&gt;configServletContextListener.applicationName&lt;/param-name&gt;
  *      &lt;param-value&gt;My Application&lt;/param-value&gt;
  *  &lt;/context-param&gt;
  *  </pre>
  *  </p>
  *  
  * @author Christian Hilmersson (https://github.com/chilmers/)
  */
 public class ConfigServletContextListener implements ServletContextListener {
 
     private static final Logger log = Logger.getLogger(ConfigServletContextListener.class);
     
     /**
      * Default value for {@link ConfigServletContextListener#applicationName}
      */    
     private static final String DEFAULT_APPLICATION_NAME = "ConfigServletContextListener";
     
     /**
      * The name of the context param to use for overriding {@link ConfigServletContextListener#applicationName}
      */
     private static final String OVERRIDE_DEFAULT_APPLICATION_NAME_PARAM = "configServletContextListener.applicationName";
     
     /**
      * Default value for {@link ConfigServletContextListener#configLocationPropertyKey}
      */
     private static final String DEFAULT_CONFIG_LOCATION_PROPERTY_KEY = "application.config.location";
     
     /**
      * The name of the context param to use for overriding {@link ConfigServletContextListener#configLocationPropertyKey}
      */
     private static final String OVERRIDE_DEFAULT_CONFIG_LOCATION_PROPERTY_KEY_PARAM = "configServletContextListener.configLocationPropertyKey";
     
     /**
      * Default value for {@link ConfigServletContextListener#fallbackConfigLocation}
      */    
    private static final String DEFAULT_FALLBACK_CONFIG_LOCATION = "classpath:/application.properties";
 
     /**
      * The name of the context param to use for overriding {@link ConfigServletContextListener#fallbackConfigLocation}
      */
     private static final String OVERRIDE_DEFAULT_FALLBACK_CONFIG_LOCATION_PARAM = "configServletContextListener.fallbackConfigLocation";
     
     /**
      * Default value for {@link ConfigServletContextListener#log4jConfigLocationPropertyKey}
      */
     private static final String DEFAULT_LOG4J_CONFIG_LOCATION_PROPERTY_KEY = "application.log4j.config.location";
 
     /**
      * The name of the context param to use for overriding {@link ConfigServletContextListener#log4jConfigLocationPropertyKey}
      */
     private static final String OVERRIDE_DEFAULT_LOG4J_CONFIG_LOCATION_PROPERTY_KEY_PARAM = "configServletContextListener.log4jConfigLocationPropertyKey";    
     
     /**
      * Prefix for system properties found in the application configuration file
      */
     private static final String CONFIG_SYSTEM_PROPERTY_PREFIX = "system.property.";
     
     /**
      * The location of the application configuration, to fall back on if the application configuration location
      * was not set by other means. 
      * 
      * Defaults to the value of {@link ConfigServletContextListener#DEFAULT_FALLBACK_CONFIG_LOCATION}
      * Can be overriden in web.xml by stating the following context-param
      *  <pre>
      *  &lt;context-param&gt;
      *      &lt;description&gt;Sets the location of the application configuration to fall back to if no other configuration
      *          file location was set. E.g. a bundled configuration on the classpath. 
     *          If not set it defaults to classpath:/application.properties&lt;/description&gt;
      *      &lt;param-name&gt;configServletContextListener.fallbackConfigLocation&lt;/param-name&gt;
     *      &lt;param-value&gt;classpath:/myown.properties&lt;/param-value&gt;
      *  &lt;/context-param&gt;
      *  </pre>
      */
     private String fallbackConfigLocation;
     
     /**
      * The key used to find the application configuration location.
      * The system will in order use this key to look up the config location value in the system properties, 
      * the environment variables and in the servlet context params.
      * It will use the first one that is defined, or default to "application.config.location" 
      * will be used to locate the config location.
      */    
     private String configLocationPropertyKey;
     
     /**
      * This value is the key of a property in the application configuration which holds the log4j configuration file location.
      * It defaults to "application.log4j.config.location" and can be overriden in by stating a 
      * context-param in web.xml like this:
      * 
      * <pre>
      *  &lt;context-param&gt;
      *      &lt;description&gt;Sets the key for where in the configuration file to look for a log4j
      *          configuration file location.
      *          If not set it defaults to application.log4j.config.location&lt;/description&gt;
      *      &lt;param-name&gt;configServletContextListener.log4jConfigLocationPropertyKey&lt;/param-name&gt;
      *      &lt;param-value&gt;myown.log4j.config.location&lt;/param-value&gt;
      *  &lt;/context-param&gt;
      *  </pre>
      * 
      * The entry that this key points to shall hold a value that ends with .xml or .properties and the 
      * which is the location of the log4j configuration.
      * If no configuration entry with this key exists or if it is empty then the default Log4j configuration
      * mechanism will be utilized, e.g. Log4j will look for log4j.xml or log4j.properties on the classpath.
      */
     private String log4jConfigLocationPropertyKey;
     
     /**
      * Application name that is printed when using System.out logging when no 
      * logging manager is available.
      * Defaults to the display-name of the web.xml or if no display-name exists, the value of
      * {@link ConfigServletContextListener#DEFAULT_APPLICATION_NAME}
      */
     private String applicationName;
     
     /**
      * Configures the when the servlet context is initialized.
      * {@inheritDoc} 
      */
     public void contextInitialized(ServletContextEvent sce) {
         overrideDefaults(sce.getServletContext());
         String configLocation = getApplicationConfigurationLocation(sce.getServletContext());
         setSystemProperty(this.configLocationPropertyKey, configLocation);        
         
         PropertyResourceBundle config = getApplicationConfiguration(configLocation);
         if(config != null){
             loadApplicationConfigurationSystemProperties(config);
             loadLoggingConfiguration(config);    
         }
     }
     
     private void overrideDefaults(ServletContext ctx) {
         this.configLocationPropertyKey = ctx.getInitParameter(OVERRIDE_DEFAULT_CONFIG_LOCATION_PROPERTY_KEY_PARAM);
         if (StringUtils.isBlank(this.configLocationPropertyKey)) {
             this.configLocationPropertyKey = DEFAULT_CONFIG_LOCATION_PROPERTY_KEY;
         }
         this.log4jConfigLocationPropertyKey = ctx.getInitParameter(OVERRIDE_DEFAULT_LOG4J_CONFIG_LOCATION_PROPERTY_KEY_PARAM);
         if (StringUtils.isBlank(this.log4jConfigLocationPropertyKey)) {
             this.log4jConfigLocationPropertyKey = DEFAULT_LOG4J_CONFIG_LOCATION_PROPERTY_KEY;
         }
         this.fallbackConfigLocation = ctx.getInitParameter(OVERRIDE_DEFAULT_FALLBACK_CONFIG_LOCATION_PARAM);
         if (StringUtils.isBlank(this.fallbackConfigLocation)) {
             this.fallbackConfigLocation = DEFAULT_FALLBACK_CONFIG_LOCATION;
         }
         this.applicationName = ctx.getInitParameter(OVERRIDE_DEFAULT_APPLICATION_NAME_PARAM);
         if (StringUtils.isBlank(this.applicationName)) {
             this.applicationName = ctx.getServletContextName();
         }
         if (StringUtils.isBlank(this.applicationName)) {
             this.applicationName = DEFAULT_APPLICATION_NAME;
         }
     }
     
     /**
      * Helper method that logs to System.out.
      * Good to have before and after the logging framework is configured
      * and after it has been shut down. 
      * @param text the text to log to System.out
      */
     private void logToSystemOut(String text) {
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
         System.out.println(sdf.format(new Date()) + " [" + applicationName + "] " + text);
     }
     
     /**
      * Decides which application configuration file to use and sets the location in the system properties.
      * By default the location will be set in the property with key "application.config.location" but this 
      * can be overridden by configuration
      */
     private String getApplicationConfigurationLocation(ServletContext ctx) {
         logToSystemOut("Servlet context initialized, checking for configuration location parameters...");
         logToSystemOut("Checking for system property " + this.configLocationPropertyKey);
         String configLocation = System.getProperty(this.configLocationPropertyKey);
         if (configLocation == null) {
             logToSystemOut("Didn't find system property " + this.configLocationPropertyKey + " holding application configuration location, checking environment variable " + this.configLocationPropertyKey);
             configLocation = System.getenv(this.configLocationPropertyKey);
         }
         if (configLocation == null) {
             logToSystemOut("Didn't find environment variable " + this.configLocationPropertyKey + " holding application configuration location, checking servlet context-param " + this.configLocationPropertyKey);
             configLocation = ctx.getInitParameter(this.configLocationPropertyKey);
         }
         if (configLocation == null) {
             logToSystemOut("Didn't find servlet-context variable " + this.configLocationPropertyKey + " holding application configuration location, " +
                     "using fallback configuration location: " + this.fallbackConfigLocation);
             configLocation = this.fallbackConfigLocation;
         } 
         
         return configLocation;
     }
     
     protected void setSystemProperty(String key, String value){
         logToSystemOut("Setting system property " + key + " " +
                 "to the following value: " + value);
         System.setProperty(key, value);
     }
     
     /**
      * Reads the application configuration file  
      * 
      * @param applicationConfigLocation The location of the application configuration
      * @return application configuration properties
      */
     protected PropertyResourceBundle getApplicationConfiguration(String applicationConfigLocation) {
         InputStream is = null;
         try {
             if (applicationConfigLocation.startsWith("classpath:")) {
                 applicationConfigLocation = applicationConfigLocation.replaceFirst("classpath:", "");
                 is = Thread.currentThread().getContextClassLoader().getResourceAsStream(applicationConfigLocation); 
             } else {
                 is = new FileInputStream(applicationConfigLocation);    
             }
             return new PropertyResourceBundle(is);
             
         } catch (Exception e) {
             logToSystemOut("There was a problem reading the application configuration at location: " 
                     + applicationConfigLocation +"\n"
                     + "Exception:" + e.getClass().toString() + "\n"
                     + "Message:" + e.getMessage());
         } finally {
             try {
                 is.close();
             } catch (Exception e) {
                 logToSystemOut("WARNING! Exception while trying to close configuration file.\n"
                         + "Exception:" + e.getClass().toString() + "\n"
                         + "Message:" + e.getMessage());
             }
         }
         return null;
     }
 
     /**
      * Loads system properties from the application configuration
      *  
      * @param configBundle The application configuration
      */
     protected void loadApplicationConfigurationSystemProperties(PropertyResourceBundle configBundle){
         logToSystemOut("Checking for system properties in application configuration");
         Enumeration<String> keys = configBundle.getKeys();
          
         while(keys.hasMoreElements()){
             
             String key = keys.nextElement();
             
             if(key.startsWith(CONFIG_SYSTEM_PROPERTY_PREFIX)){
                 String systemPropertyKey = key.substring(CONFIG_SYSTEM_PROPERTY_PREFIX.length());
                 
                 if(systemPropertyKey.length() > 0){
                     setSystemProperty(systemPropertyKey, configBundle.getString(key));
                 }
             }
         }
     }
     
     /**
      * Loads the log4j configuration from a file whose location is given in the application configuration. 
      * 
      * The location of the log4j configuration file shall by default be specified in 
      * an entry in the application configuration file with key "application.log4j.config.location"
      * This key can be overridden by configuration if you need to. 
      * 
      * If no such property is found in the application configuration, log4j's default 
      * configuration mechanism will be used, e.g. it will look for log4j.xml 
      * or log4j.properties on the classpath.
      */
     private void loadLoggingConfiguration(PropertyResourceBundle configBundle) {
         logToSystemOut("Finding log4j configuration location in application configuration...");
         
         String log4jConfigLocation = null;
         
         try {
             log4jConfigLocation = configBundle.getString(this.log4jConfigLocationPropertyKey);
         } catch (MissingResourceException e) {
             logToSystemOut("No log4j configuration location was found for property " + 
                     this.log4jConfigLocationPropertyKey + " in the application configuration. ");
         }
     
         if (StringUtils.isNotBlank(log4jConfigLocation)) {
             LogManager.resetConfiguration();
             logToSystemOut("Found log4j configuration location in the application configuration. " +
             		"Configuring logger using file: " + log4jConfigLocation);
             if (log4jConfigLocation.endsWith(".xml")) {
                 DOMConfigurator.configureAndWatch(log4jConfigLocation);
             } else if (log4jConfigLocation.endsWith(".properties")) {
                 PropertyConfigurator.configureAndWatch(log4jConfigLocation);
             } else {
                 logToSystemOut("The log4j configuration file location must end with .xml or .properties. " +
                 		"\nFalling back to the default log4j configuration mechanism.");
             }
         } else {
             logToSystemOut("Didn't find log4j configuration location in application configuration. " +
             		"Falling back to the default log4j configuration mechanism.");
         }
         log.info("Log4j was configured, see System.out log for initialization information.");
     }
 
     /**
      * Destroys the servlet context and shutting down the Log Manager 
      * which in turn is stopping Log4j's watch dog thread.
      * {@inheritDoc}
      */
     public void contextDestroyed(ServletContextEvent sce) {
         log.debug("Servlet context destroyed");
         log.debug("Shutting down log manager...");
         logToSystemOut("Destroying servlet context...");
         logToSystemOut("Shutting down log manager...");
         LogManager.shutdown();
         logToSystemOut("The log manager has been shut down.");
         logToSystemOut("The servlet context has been destroyed.");
     }
 
 }
