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
 
 import java.util.HashMap;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * 
 * <h1>AppConfig V3.0 - Application configuration the easy way</h1>
  * 
  * <h2>A thin wrapper for Apache Commons Configuration</h2>
  * 
  * <p>Copyright 2009-2013 Tracy Flynn. All rights reserved. Licensed under the Apache License, Version 2.0.</p>
  * 
  * <h2>Introduction</h2>
  * 
  * <p>AppConfig is designed to provide painless configuration for multiple runtime environments based on a model similar to the Ruby-on-Rails &#39;development/production/test&#39; approach with all the appropriate defaulting and overriding of settings.</p>
  * 
  * <p>It is particularly targeted at the problem of configuring components and applications &#39;from the outside&#39; without needing to modify the packaged artifact.</p>
  * 
  * <p>If certain conventions are observed, it can also independently manage configurations for multiple AppConfig-enabled components within a single application.</p>
  * 
  * <p>It is based on Apache Commons Configuration http://commons.apache.org/configuration .</p>
  * 
  * <p> See README.html or https://github.com/tflynn/app_config#readme for full documentation.
  * 
  * @author Tracy Flynn
  * @version 3.0
  * @since 1.0
  */
 public class AppConfig {
   
   /*
    * Logger instance
    */
   private static Logger logger = null;
   
   /*
    * Configuration options
    */
   private Options options = new Options();
   
   /*
    * Internal properties
    */
   private ExtendedProperties internalProperties = null;
 
   /*
    * Private copy of Configuration object returned
    */
   private Configuration configuration = null;
   
   /*
    * Base application package name with which to locate application property files 
    */
   private String applicationPropertiesPackageName = null;
   
   /*
    * External directory for configuration
    */
   private String externalConfigurationDirectory = null;
   
   /*
    * Configuration builder
    */
   private ConfigurationBuilder configurationBuilder = NullConfigurationBuilder.getInstance();
   
   /*
    * Logging helper instance
    */
   private LoggingHelper loggingHelper = NullLoggingHelper.getInstance();
 
   /*
    * Configuration Helper instance
    */
   private ConfigurationHelper configurationHelper = new ConfigurationHelper();
   
  /**
    * Create a new AppConfig instance
    */
   public AppConfig() {
     this(null);
   }
 
   /**
    * Create a new AppConfig instance
    * 
    * @param runtimeOptions Runtime options as a HashMap
    */
   public AppConfig(HashMap<String,Object> runtimeOptions) {
     this(runtimeOptions, null,null);
   }
 
   /**
    * Create a new AppConfig instance
    * 
    * @param options Runtime options as a HashMap
    * @param configurationBuilder Specify a custom configuration builder
    * @param loggingHelper Specify a custom loggingHelper
    */
   public AppConfig(HashMap<String,Object> options, ConfigurationBuilder configurationBuilder, LoggingHelper loggingHelper) {
     this.setOptions(options);
     this.setConfigurationBuilder(configurationBuilder);
     this.setLoggingHelper(loggingHelper);
   }
   
   
   /**
    * Configure AppConfig
    */
   public void configure() {
     
     // Configure logging using internal defaults. Allow override for initial logging level
     String logLevelOverride = (String) this.configurationHelper.getSettingFromOptionsEnvSystem(InternalConfigurationConstants.DEFAULT_BOOTSTRAP_LOGGING_LEVEL_PROPERTY_NAME, this.options);
     BootstrapLogger bootstrapLogger = new BootstrapLogger();
     bootstrapLogger.configureDynamically(logLevelOverride);
     
     // Use the bootstrap logger until the internal logger is fully configured
     AppConfig.setActiveLogger(bootstrapLogger);
     AppConfigUtils.setActiveLogger(bootstrapLogger);
     ConfigurationHelper.setActiveLogger(bootstrapLogger);
     ExtendedProperties.setActiveLogger(bootstrapLogger);
 
     logger.trace(String.format("AppConfig.configure Added bootstrap internal logging"));
 
     // Get internal properties - full defaulting rules applied - first found wins - options, system properties, environment variables, defaults
     // Make sure to pass the values from the convenience accessors
     this.options.put(InternalConfigurationConstants.APPLICATION_PROPERTIES_PACKAGE_NAME_PROPERTY_NAME, this.applicationPropertiesPackageName);
     this.options.put(InternalConfigurationConstants.EXTERNAL_CONFIGURATION_DIRECTORY_PROPERTY_NAME,this.externalConfigurationDirectory);
     if (this.applicationPropertiesPackageName != null) {
       String packageDir = this.applicationPropertiesPackageName.replaceAll("\\.", "/");
       this.options.put(InternalConfigurationConstants.APPLICATION_PROPERTIES_PACKAGE_DIR_PROPERTY_NAME, packageDir);
     }
     this.internalProperties = this.configurationHelper.loadInternalProperties(this.options);
 
     // Make sure the runtime environment is set
     this.configurationHelper.checkRunTimeEnvironment(this.internalProperties);
 
     if (logger.isTraceEnabled()) {
       logger.trace(String.format("AppConfig.configure runtime options and internal properties"));
       this.internalProperties.dumpProperties();
     }
     
     // Load the real internal logger configuration
     String loggingConfigurationFileName = internalProperties.getProperty(InternalConfigurationConstants.INTERNAL_LOGGING_CONFIGURATION_FILE_PROPERTY_NAME);
     String loggingLevel = internalProperties.getProperty(InternalConfigurationConstants.DEFAULT_LOGGING_LEVEL_PROPERTY_NAME);
     this.loggingHelper.configureLoggerFromConfigurationFile(loggingConfigurationFileName,loggingLevel,bootstrapLogger);
     this.loggingHelper.overrideLogLevel(this.getClass().getPackage().getName(), loggingLevel);
 
     // Reset the active logger to the default value in various places
     AppConfig.setActiveLogger();
     AppConfigUtils.setActiveLogger();
     ConfigurationHelper.setActiveLogger();
     ExtendedProperties.setActiveLogger();
 
     // Generate the configuration
     // Get a ConfigurationBuilder
     if (this.configurationBuilder == null) {
       this.configurationBuilder = ConfigurationBuilderFactory.instance(this.internalProperties);
     }
     this.configurationBuilder.setInternalProperties(this.internalProperties);
     // Now generate the actual configuration
     this.configuration = this.configurationBuilder.buildConfiguration();
 
     logger.trace(String.format("AppConfig.configure Generated configuration definitions"));
 
     // Configure application-level logging
     List<String> applictionLoggerConfigurationFileNames = this.configurationHelper.generateLoggingConfigurationNames(this.internalProperties);
     this.loggingHelper.configureLoggerFromConfigurationFiles(applictionLoggerConfigurationFileNames);
     logger.trace(String.format("AppConfig.configure loaded application logging"));
     
   }
   
   
   /**
    * Set configuration options
    * 
    * @param options
    * 
    * @return Current AppConfig instance
    */
   public AppConfig setOptions(HashMap<String,Object> options) {
     this.options = options == null ? new Options() : new Options(options); 
     return this;
   }
 
   /**
    * Set configuration options
    * 
    * @param options
    * 
    * @return Current AppConfig instance
    */
   public AppConfig setOptions(Options options) {
     this.options = options == null ? new Options() : options; 
     return this;
   }
   
   /**
    * Set the active ConfigurationBuilder instance
    * 
    * @param configurationBuilder ConfigurationBuilder instance
    * 
    * @return Current AppConfig instance
    */
   public AppConfig setConfigurationBuilder(ConfigurationBuilder configurationBuilder) {
     this.configurationBuilder = configurationBuilder;
     return this;
   }
   
   /**
    * Set the active LoggingHelper instance
    * 
    * @param loggingHelper LoggingHelper instance
    * 
    * @return Current AppConfig instance
    */
   public AppConfig setLoggingHelper(LoggingHelper loggingHelper) {
     this.loggingHelper = loggingHelper == null ? LoggingHelperFactory.instance(this.options) : loggingHelper;
     return this;
   }
 
   /**
    * Set the base application package name with which to locate application property files
    * 
    * @param applicationPropertiesPackageName
    * 
    * @return Current AppConfig instance
    */
   public AppConfig setApplicationPropertiesPackageName(String applicationPropertiesPackageName) {
     this.applicationPropertiesPackageName = applicationPropertiesPackageName;
     return this;
   }
 
   /**
    * Set the external configuration directory
    * 
    * @param externalConfigurationDirectory
    * 
    * @return Current AppConfig instance
   */
   public AppConfig setExternalConfigurationDirectory(String externalConfigurationDirectory) {
     this.externalConfigurationDirectory = externalConfigurationDirectory;
     return this;
   }
 
    /**
     * Get the current configuration object
     * 
     * @return Current configuration object
     */
    public Configuration getConfiguration() {
      return this.configuration;
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
      logger = activeLogger == null ? LoggerFactory.getLogger(AppConfig.class) : activeLogger;
    }
    
   
 }
