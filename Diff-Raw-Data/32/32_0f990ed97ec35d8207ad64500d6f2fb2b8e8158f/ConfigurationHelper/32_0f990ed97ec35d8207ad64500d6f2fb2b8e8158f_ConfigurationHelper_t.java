 /* Copyright 2009-2013 Tracy Flynn
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.verymuchme.appconfig;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Utility methods used for building configurations
  *
  * @author Tracy Flynn
  * @version 3.0
  * @since 3.0
  */
 public class ConfigurationHelper {
 
   /*
    * Logger instance
    */
   private static Logger logger = null;
   
   /**
    * Get a setting. Look first in options, then system properties , then environment
    * 
    * @param propertyName Name of setting
    * @param options Hash of runtime options
   * @return Value as an object
    */
   public Object getSettingFromOptionsEnvSystem(String propertyName, Options options) {
     options = options == null ? new Options() : options;
     Object setting = null;
     setting = options.get(propertyName);
     if (setting == null) {
       setting = System.getProperty(propertyName);
     }
     if (setting == null) {
       setting = System.getenv(propertyName);
     }
     return setting;
   }
   
   
   /**
    * Load internal properties files
    * 
    * Supports automatic reload if the name of the internal properties file is overridden in the proerties file
    * Default in order to options, command=line, environment, defaults
    * 
    * @param options Runtime options
    */
   public ExtendedProperties loadInternalProperties(Options options) {
 
     Properties internalDefaults = null;
     InputStream internalDefaultsInputStream = null;
     ExtendedProperties internalProperties = null;
     
     // Load internal defaults
     try {
       internalDefaults = new Properties();
       internalDefaultsInputStream = getClass().getResourceAsStream(InternalConfigurationConstants.INTERNAL_DEFAULTS_PROPERTY_FILE_NAME);
       internalDefaults.load(internalDefaultsInputStream);
     }
     catch (Exception e) {
       String errorMessage = "AppConfig.ConfigurationDefinitionBuilder.setInternalProperties failed to load the default internal properties for AppConfig";
       logger.error(errorMessage,e);
       throw new AppConfigException(errorMessage,e);
     }
     finally {
       try {
         internalDefaultsInputStream.close();
       }
       catch (Exception e) {
         // Ignore
       }
     }
 
     try {
       boolean overriddenAlready = false; 
       
       while (true) {
   
         internalProperties = new ExtendedProperties();
         // Use putAll rather than the constructor so as not to get multiple copies of the same property
         internalProperties.loadStringProperties(internalDefaults);
         
         // Do this in reverse order so the value defaulting order works
         @SuppressWarnings("unchecked")
         Iterator<String> propertyNamesItr = internalProperties.propertyNames();
         while (propertyNamesItr.hasNext()) {
           String propertyName = propertyNamesItr.next();
           Object overrideValue = getSettingFromOptionsEnvSystem(propertyName,options);
           if (overrideValue != null) {
             internalProperties.put(propertyName, overrideValue);
           }
         }
         
         // Check for an override to the internal properties file name. If present, reload everything
         String overriddenInternalPropertiesFileName = internalProperties.getProperty(InternalConfigurationConstants.INTERNAL_DEFAULTS_PROPERTY_FILE_NAME_PROPERTY_NAME);
         if (overriddenInternalPropertiesFileName == null) {
           // No override, so load is complete
           break;
         }
         else {
           if (overriddenAlready) {
             // Only override once
             break;
           }
           else {
             // Go around again to reload everything against the new defaults
             logger.trace(String.format("AppConfig.ConfigurationHelper.loadInternalProperties Reloading because found internal property file override %s",overriddenInternalPropertiesFileName));
             internalDefaults = new Properties();
             internalDefaultsInputStream = getClass().getResourceAsStream(overriddenInternalPropertiesFileName);
             internalDefaults.load(internalDefaultsInputStream);
             internalDefaultsInputStream.close();        
             overriddenAlready = true;
           }
         }
       }
     }
     catch (Exception e) {
       String errorMessage = "AppConfig.ConfigurationHelper.setInternalProperties failed to load internal AppConfig properties or overrides";
       logger.error(errorMessage,e);
       throw new AppConfigException(errorMessage,e);
     }
     
     return internalProperties;
   }
   
   /** 
    * Check the run-time environment. Defaults to 'development' if any errors during setting
    * 
    * @param internalProperties Internal Properties
    */
   public void checkRunTimeEnvironment(ExtendedProperties internalProperties) {
     boolean matched = false;
     String runTimeEnv = internalProperties.getProperty(InternalConfigurationConstants.RUN_TIME_ENVIRONMENT_PROPERTY_NAME);
     for (String env : internalProperties.getListProperty(InternalConfigurationConstants.PERMITTED_RUN_TIME_ENVIRONMENTS_PROPERTY_NAME)) {
       if (env.equalsIgnoreCase(runTimeEnv)) {
         runTimeEnv = env;
         matched = true;
         break;
       }
     }
     if (!matched) {
       runTimeEnv = internalProperties.getProperty(InternalConfigurationConstants.DEFAULT_RUN_TIME_ENVIRONMENT_PROPERTY_NAME);
       internalProperties.setProperty(InternalConfigurationConstants.RUN_TIME_ENVIRONMENT_PROPERTY_NAME,runTimeEnv);
       logger.trace(String.format("AppConfig.ConfigurationHelper.setRunTimeEnvironment invalid runtime environment set, defaulting to '%s'",runTimeEnv));
     }
     else {
       logger.trace(String.format("AppConfig.ConfigurationHelper.setRunTimeEnvironment setting runtime environment to '%s'",runTimeEnv));
     }
   }
 
   /**
    * Generate logging configuration file names - ordered so first one on the list that exists should be loaded
    * 
    * @param internalProperties Internal Properties
 
    * @return List of log4j configuration file names
    */
   public List<String> generateLoggingConfigurationNames(ExtendedProperties internalProperties) {
     List<String> configNames = new ArrayList<String>();
     String packageDir = internalProperties.getProperty(InternalConfigurationConstants.APPLICATION_PROPERTIES_PACKAGE_DIR_PROPERTY_NAME);
     if (packageDir != null) {
       String suffix = internalProperties.getProperty(InternalConfigurationConstants.CONFIGURATION_NAME_SUFFIX_PROPERTY_NAME);
       String log4jPrefix = internalProperties.getProperty(InternalConfigurationConstants.LOGGING_CONFIGURATION_NAME_PREFIX_PROPERTY_NAME);
       String rtEnv = internalProperties.getProperty(InternalConfigurationConstants.RUN_TIME_ENVIRONMENT_PROPERTY_NAME);
       String defaultPropName = internalProperties.getProperty(InternalConfigurationConstants.DEFAULT_CONFIGURATION_NAME_PROPERTY_NAME);
       String externalConfigurationDirectory = internalProperties.getProperty(InternalConfigurationConstants.EXTERNAL_CONFIGURATION_DIRECTORY_PROPERTY_NAME);
       if (externalConfigurationDirectory != null ) {
         String confDir = externalConfigurationDirectory;
         String extLog4jDef = String.format("%s/%s-%s.%s",confDir,log4jPrefix,rtEnv,suffix);
         configNames.add(extLog4jDef);
       }
       String log4jDef = String.format("%s/%s-%s.%s",packageDir,log4jPrefix,rtEnv,suffix);
       configNames.add(log4jDef);
   
       boolean includeLog4jDefault = internalProperties.getBooleanProperty(InternalConfigurationConstants.DEFAULT_LOG4J_CONFIGURATION_ENABLED_PROPERTY_NAME);
       if (includeLog4jDefault) {
         String log4jDefault = String.format("%s/%s-%s.%s",packageDir,log4jPrefix,defaultPropName,suffix);
         configNames.add(log4jDefault);
       }
       
       
     }
     if (logger.isTraceEnabled()) {
       if (configNames.isEmpty()) {
         logger.trace(String.format("AppConfig.ConfigurationHelper.generateLoggingConfigurationNames No application log file names generated"));
       }
       else {
         logger.trace(String.format("AppConfig.ConfigurationHelper.generateLoggingConfigurationNames Application log file names"));
         for (String confName : configNames) {
           logger.trace(String.format("AppConfig.ConfigurationHelper.generateLoggingConfigurationNames logger configuration name %s",confName));
         }
       }
     }
     return configNames;
   }
 
   
   /**
    * Set the active logger for this class
    */
   public static void setActiveLogger() {
     setActiveLogger(null);
   }
   
   /**
    * Set the active logger for this class
    * 
    * @param activeLogger
    */
   public static void setActiveLogger(Logger activeLogger) {
     logger = activeLogger == null ? LoggerFactory.getLogger(ConfigurationHelper.class) : activeLogger;
   }
 
   
 }
