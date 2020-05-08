 /**
  * Copyright (C) 2011 - 101loops.com <dev@101loops.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.crashnote.core.config;
 
 import com.crashnote.core.log.LogLog;
 import com.crashnote.core.util.SystemUtil;
 
 import java.util.Enumeration;
 import java.util.Properties;
 
 import static com.crashnote.core.config.Config.*;
 
 /**
  * This class is responsible for instantiating a {@link Config} object and internally apply
  * the configuration settings (e.g. from system props, property file).
  * <p/>
  * Finally it validates the config ("fail fast") and returns it.
  */
 public class ConfigFactory<C extends Config> {
 
     protected final C config;
     private final LogLog logger;
     private final SystemUtil sysUtil;
 
     /**
      * specifies whether the config object was already validated
      */
     private boolean validated = false;
 
     /**
      * indicates whether the external configuration sources are already processed
      */
     private boolean externalConfigLoaded = false;
 
     /**
      * name of the configuration file to parse for settings
      */
     public static final String PROP_FILE = Config.LIB_NAME + ".properties";
 
     /**
      * name of the property to specify the path of the configuration file (e.g. via system properties)
      */
     public static final String PROP_FILE_CONF = Config.LIB_NAME + ".config";
 
 
     // SETUP ======================================================================================
 
     public ConfigFactory() {
         this((C) new Config());
     }
 
     protected ConfigFactory(final C config) {
         this.config = config;
         this.sysUtil = new SystemUtil();
         this.logger = config.getLogger(this.getClass());
     }
 
     // INTERFACE ==================================================================================
 
     public C get() {
         if (!validated) {
             loadExternalConfig(); // load external configuration (e.g. property file)
             validate(); // validate first to fail fast if necessary
             validated = true;
         }
         return config;
     }
 
     // SHARED =====================================================================================
 
     protected void applyProperties(final Properties props, final boolean strict) {
         config.setClientInfo(getProperty(props, PROP_CLIENT, strict));
 
         setEnabled(getProperty(props, PROP_ENABLED, strict));
         setDebug(getProperty(props, PROP_DEBUG, strict));
         setSync(getProperty(props, PROP_SYNC, strict));
 
         setBuild(getProperty(props, PROP_APP_BUILD, strict));
         setProfile(getProperty(props, PROP_APP_PROFILE, strict));
         setVersion(getProperty(props, PROP_APP_VERSION, strict));
 
         setKey(getProperty(props, PROP_API_KEY, strict));
         setHost(getProperty(props, PROP_API_HOST, strict));
         setSecure(getProperty(props, PROP_API_SECURE, strict));
         setPort(getProperty(props, PROP_API_PORT, strict));
         setSslPort(getProperty(props, PROP_API_PORT_SSL, strict));
         setConnectionTimeout(getProperty(props, PROP_API_TIMEOUT, strict));
     }
 
     protected String getProperty(final Properties props, final String name, final boolean strict) {
         if (props == null) return null;
         final Enumeration<?> propNames = props.propertyNames();
         while (propNames.hasMoreElements()) {
             final Object propName = propNames.nextElement();
             if (propName instanceof String) {
                 final String key = (String) propName;
                 if (strict) {
                     if (key.equalsIgnoreCase(getConfigKey(name))
                             || key.equalsIgnoreCase(getConfigKey(name, '_'))
                             || key.equalsIgnoreCase(getConfigKey(name, '-')))
                         return props.getProperty(key);
                 } else if (key.equalsIgnoreCase(name))
                     return props.getProperty(key);
             }
         }
         return null;
     }
 
     protected void loadExternalConfig() {
         if (!externalConfigLoaded) {
             loadFileProperties();       // #1: load file props
             loadEnvProperties();        // #2: load environment props
             loadSystemProperties();     // #3: load system props
 
             config.print(); // print final config to console (only in debug)
             externalConfigLoaded = true;
         }
     }
 
     // INTERNALS ==================================================================================
 
     private void loadSystemProperties() {
         logger.debug("applying properties from system");
         applyProperties(sysUtil.getProperties(), true);
     }
 
     private void loadEnvProperties() {
         logger.debug("applying properties from system environment");
         applyProperties(sysUtil.getEnvProperties(), true);
     }
 
     private void loadFileProperties() {
         final String fileName = // determine config file location: first try sys props, then env props and then default
                 sysUtil.getProperty(PROP_FILE_CONF, sysUtil.getEnv(PROP_FILE_CONF, PROP_FILE));
 
         // load properties from file
         final Properties props = sysUtil.loadProperties(fileName);
         if (props.isEmpty())
             logger.debug("no configuration properties found in '{}'", fileName);
         else
             logger.debug("applying properties from file '{}'", fileName);
 
         applyProperties(props, false);
     }
 
     private void validate() {
         if (isEnabled())
             validateKey();
         else
             logger.info("transfer to cloud service is DISABLED");
     }
 
     private boolean isEnabled() {
         if (config.isEnabled()) {
             logger.info("Status: ON");
             return true;
         } else {
             logger.info("Status: OFF");
             return false;
         }
     }
 
     private void validateKey() {
         final String key = config.getKey();
         if (key == null || key.length() == 0) {
             throw new IllegalArgumentException(
                     "The API Key is missing, please login to the web app under [" + LIB_URL_BOARD + "], " +
                             "browse to your app and consult the 'Docs'.");
 
         } else if (key.length() != 36)
             throw new IllegalArgumentException(
                     "The API Key appears to be invalid (it should be 32 characters long with 4 dashes), " +
                             "please login to the web app under [" + LIB_URL_BOARD + "], " +
                            "browse to your app and consult the 'Install' instructions.");
     }
 
     // SET ========================================================================================
 
     public void setEnabled(final String enabled) {
         config.setEnabled(enabled);
     }
 
     public void setDebug(final String debug) {
         config.setDebug(debug);
     }
 
     public void setSslPort(final String sslPort) {
         config.setSSLPort(sslPort);
     }
 
     public void setSecure(final String secure) {
         config.setSecure(secure);
     }
 
     public void setSync(final String sync) {
         config.setSync(sync);
     }
 
     public void setPort(final String port) {
         config.setPort(port);
     }
 
     public void setProfile(final String profile) {
         config.setProfile(profile);
     }
 
     public void setBuild(final String build) {
         config.setBuild(build);
     }
 
     public void setVersion(final String version) {
         config.setVersion(version);
     }
 
     public void setSecure(final Boolean secure) {
         config.setSecure(secure);
     }
 
     public void setConnectionTimeout(final String timeout) {
         config.setConnectionTimeout(timeout);
     }
 
     public void setKey(final String key) {
         config.setKey(key);
     }
 
     public void setEnabled(final boolean enabled) {
         config.setEnabled(enabled);
     }
 
     public void setPort(final int port) {
         config.setPort(port);
     }
 
     public void setSslPort(final int sslPort) {
         config.setSSLPort(sslPort);
     }
 
     public void setHost(final String host) {
         config.setHost(host);
     }
 
     public void setDebug(final Boolean debug) {
         config.setDebug(debug);
     }
 
     // GET ========================================================================================
 
     public LogLog getLogger() {
         return logger;
     }
 }
