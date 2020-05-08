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
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * Dynamically build a configuration definition compatible with Apache Commons Configuration
  *
  * Settings available from the internal options configuration,  Java/JVM command-line and the environment. The values are taken starting with internal options,  command-line and then the environment value.
  * 
  * <ul>
  * <li>'com.verymuchme.appconfig.systemPropertiesOverride' - enable/disable the ability for system properties to override others</li>
  * <li>'com.verymuchme.appconfig.runTimeEnvironment' - If specified, one of 'development','production','test'. Defaults to 'development'</li>
  * <li>'com.verymuchme.appconfig.externalConfigurationDirectory' - If specified, defines the external directory location for configuration files specific to particular run-time environments</li>
  * <li>'com.verymuchme.appconfig.applicationConfigurationPrefix' - If specified, defines the prefix for application configuration property files. Defaults to 'application'</li>
  * <li>'com.verymuchme.appconfig.databaseConfigurationPrefix' - If specified, defines the prefix for database configuration property files. Defaults to 'database'</li>
  * <li>'com.verymuchme.appconfig.log4jConfigurationPrefix' - If specified, defines the prefix for log4j configuration property files. Defaults to 'log4j'</li>
  * <li>'com.verymuchme.appconfig.configurationNameSuffix' - If specified, defines the suffix for configuration property files. Defaults to 'properties'</li>
  * <li>'com.verymuchme.appconfig.defaultConfigurationName' - If specified, defines the prefix for default configuration property files. Defaults to 'defaults'</li>
  * <li>'com.verymuchme.appconfig.database.defaultConfigurationEnabled' - If set to true, a default database configuration file ('database-defaults.properties') must be present. Defaults to 'false'</li>
  * <li>'com.verymuchme.appconfig.log4j.defaultConfigurationEnabled' - If set to true, a default log4j configuration file ('log4j-defaults.properties') must be present. Defaults to 'true'</li>
  * </ul> 
  *
  * @author Tracy Flynn
  * @version 1.0-SNAPSHOT
  * @since 1.0-SNAPSHOT
  */
 public class ConfigurationDefinitionBuilder {
 
   private static final String DEFINITION_PREAMBLE  = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
   
   private static final String DEFINITION_ELEMENT_OPEN = "<configuration>";
   private static final String DEFINITION_ELEMENT_CLOSE = "</configuration>";
   
   private static final String DEFINITION_SYSTEM_PROPERTIES = "<system/>";
   
   private static final String SYSTEM_PROPERTIES_OVERRIDE_PROPERTY_NAME = "com.verymuchme.appconfig.systemPropertiesOverride";
   private static final String RUN_TIME_ENVIRONMENT_PROPERTY_NAME = "com.verymuchme.appconfig.runTimeEnvironment";
   private static final String EXTERNAL_CONFIGURATION_DIRECTORY_PROPERTY_NAME = "com.verymuchme.appconfig.externalConfigurationDirectory";
   
   private static final String DEFAULT_RUN_TIME_ENVIRONMENT = "development";
   private static final List<String> PERMITTED_RUN_TIME_ENVIRONMENTS = Arrays.asList("development", "production", "test");
   
   private static final String DEFAULT_APPLICATION_CONFIGURATION_NAME_PREFIX = "application";
   private static final String APPLICATION_CONFIGURATION_NAME_PREFIX_PROPERTY_NAME = "com.verymuchme.appconfig.applicationConfigurationPrefix";
   
   private static final String DEFAULT_DATABASE_CONFIGURATION_NAME_PREFIX = "database";
   private static final String DATABASE_CONFIGURATION_NAME_PREFIX_PROPERTY_NAME = "com.verymuchme.appconfig.database.ConfigurationPrefix";
   private static final String DEFAULT_DATABASE_CONFIGURATION_ENABLED = "false";
   private static final String DEFAULT_DATABASE_CONFIGURATION_ENABLED_PROPERTY_NAME = "com.verymuchme.appconfig.database.defaultConfigurationEnabled";
   
   
   private static final String DEFAULT_LOG4J_CONFIGURATION_NAME_PREFIX = "log4j";
   private static final String LOG4J_CONFIGURATION_NAME_PREFIX_PROPERTY_NAME = "com.verymuchme.appconfig.log4j.ConfigurationPrefix";
   private static final String DEFAULT_LOG4J_CONFIGURATION_ENABLED = "true";
   private static final String DEFAULT_LOG4J_CONFIGURATION_ENABLED_PROPERTY_NAME = "com.verymuchme.appconfig.log4j.defaultConfigurationEnabled";
   
   private static final String DEFAULT_CONFIGURATION_NAME_SUFFIX = "properties";
   private static final String CONFIGURATION_NAME_SUFFIX_PROPERTY_NAME = "com.verymuchme.appconfig.configurationNameSuffix";
   
   private static final String DEFAULT_CONFIGURATION_NAME = "defaults";
   private static final String DEFAULT_CONFIGURATION_NAME_PROPERTY_NAME = "com.verymuchme.appconfig.defaultConfigurationName";
   
   
   /*
    * Bootstrap logger instance 
    */
   private BootstrapLogger bootstrapLogger = null;
 
   /*
    * Specify whether System properties should override all settings. Default true
    */
   private Boolean systemPropertiesOverride = Boolean.TRUE;
   
   /*
    * Runtime environment
    */
   private String runTimeEnvironment = null;
   
   /*
    * External configuration directory
    */
   private String externalConfigurationDirectory = null;
 
   
   /*
    * Configuration options
    */
   private HashMap<String,String> configurationOptions = null;
   
 
   /**
    * Create a new instance
    */
   public ConfigurationDefinitionBuilder(HashMap<String,String> configOptions) {
     this.configurationOptions = configOptions;
   }
 
   /**
    * Build the Apache Commons Configuration-compliant configuration definition
    * 
    * @return XML String with the configuration definition
    */
   public String build() {
     this.setDefaults();
     return this.toString();
   }
   
   /**
    * Get the bootstrap logger
    *  
    * @return BootstrapLogger instance or null
    */
   public BootstrapLogger getBootstrapLogger() {
     return bootstrapLogger;
   }
 
   /**
    * Set the bootstrap logger instance
    * 
    * @param bootstrapLogger instance
    */
   public void setBootstrapLogger(BootstrapLogger bootstrapLogger) {
     this.bootstrapLogger = bootstrapLogger;
   }
 
   /**
    * Get the state of the SystemPropertiesOverride option
    * 
    * @return Boolean.TRUE if set, Boolean.FALSE otherwise
    */
   public Boolean getSystemPropertiesOverride() {
     if (systemPropertiesOverride != null) {
       return systemPropertiesOverride;
     }
     else {
       return Boolean.FALSE;
     }
   }
 
   /**
    * Set the state of the SystemPropertiesOverride option
    * 
    * @param systemPropertiesOverride
    */
   public void setSystemPropertiesOverride(Boolean systemPropertiesOverride) {
     this.systemPropertiesOverride = systemPropertiesOverride;
   }
 
   /**
    * Get run-time environment. Defaults to 'development' if not overridden.
    * 
    * @return One of the strings 'development','production' or 'test'.
    */
   public String getRunTimeEnvironment() {
     return this.runTimeEnvironment;
   }
   
   /** 
    * Set the run-time environment. Defaults to 'development' if any errors during setting
    * 
    * @param runTimeEnv One of the strings 'development','production' or 'test'.
    */
   public void setRunTimeEnvironment(String runTimeEnv) {
     if (runTimeEnv == null) {
       runTimeEnv = this.getConfigValue(RUN_TIME_ENVIRONMENT_PROPERTY_NAME,null);
       if (runTimeEnv == null) {
         runTimeEnv = DEFAULT_RUN_TIME_ENVIRONMENT;
         this.bootstrapLogger.debug(String.format("AppConfig.setRunTimeEnvironment no runtime environment set, defaulting to '%s'",DEFAULT_RUN_TIME_ENVIRONMENT));
       }
     }
     boolean matched = false;
     for (String env : PERMITTED_RUN_TIME_ENVIRONMENTS) {
       if (env.equalsIgnoreCase(runTimeEnv)) {
         runTimeEnv = env;
         matched = true;
       }
     }
     if (! matched) {
       runTimeEnv = DEFAULT_RUN_TIME_ENVIRONMENT;
       this.bootstrapLogger.debug(String.format("AppConfig.setRunTimeEnvironment invalid runtime environment set, defaulting to '%s'",DEFAULT_RUN_TIME_ENVIRONMENT));
     }
     this.runTimeEnvironment = runTimeEnv;
     this.bootstrapLogger.debug(String.format("AppConfig.setRunTimeEnvironment setting runtime environment to '%s'",runTimeEnv));
   }
   
   /**
    * Get external configuration directory
    * 
    * @return Fully-qualified external configuration directory, or null if none specified
    */
   public String getExternalConfigurationDirectory() {
     if (this.externalConfigurationDirectory == null) {
       this.externalConfigurationDirectory = this.getConfigValue(EXTERNAL_CONFIGURATION_DIRECTORY_PROPERTY_NAME,null);
     }
     return this.externalConfigurationDirectory;
   }
   
   /**
    * Set the external configuration directory
    * 
   * @param External configuration directory or null if not set
    */
   public void setExternalConfigurationDirectory(String externalConfDir) {
     this.externalConfigurationDirectory = externalConfDir;
   }
   
   /**
    * Generate configuration lines for variable configuration files 
    */
   public List<String> generateVariableDefinitions() {
     List<String> definitions = new ArrayList<String>();
     String suffix = this.getConfigValue(CONFIGURATION_NAME_SUFFIX_PROPERTY_NAME, DEFAULT_CONFIGURATION_NAME_SUFFIX);
     String applicationPrefix = this.getConfigValue(APPLICATION_CONFIGURATION_NAME_PREFIX_PROPERTY_NAME, DEFAULT_APPLICATION_CONFIGURATION_NAME_PREFIX);
     String databasePrefix = this.getConfigValue(DATABASE_CONFIGURATION_NAME_PREFIX_PROPERTY_NAME, DEFAULT_DATABASE_CONFIGURATION_NAME_PREFIX);
     String log4jPrefix = this.getConfigValue(LOG4J_CONFIGURATION_NAME_PREFIX_PROPERTY_NAME, DEFAULT_LOG4J_CONFIGURATION_NAME_PREFIX);
     String rtEnv = this.getRunTimeEnvironment();
     String defaultPropName = this.getConfigValue(DEFAULT_CONFIGURATION_NAME_PROPERTY_NAME,DEFAULT_CONFIGURATION_NAME);
     if (this.externalConfigurationDirectory != null ) {
       String confDir = this.externalConfigurationDirectory;
       String extApplicationDef = String.format("<properties fileName=\"%s/%s-%s.%s\" config-optional=\"true\"/>",confDir,applicationPrefix,rtEnv,suffix);
       String extDatabaseDef = String.format("<properties fileName=\"%s/%s-%s.%s\" config-optional=\"true\"/>",confDir,databasePrefix,rtEnv,suffix);
       String extLog4jDef = String.format("<properties fileName=\"%s/%s-%s.%s\" config-optional=\"true\"/>",confDir,log4jPrefix,rtEnv,suffix);
       definitions.add(extApplicationDef);
       definitions.add(extDatabaseDef);
       definitions.add(extLog4jDef);
     }
     String applicationDef = String.format("<properties fileName=\"%s-%s.%s\" config-optional=\"true\"/>",applicationPrefix,rtEnv,suffix);
     String databaseDef = String.format("<properties fileName=\"%s-%s.%s\" config-optional=\"true\"/>", databasePrefix,rtEnv,suffix);
     String log4jDef = String.format("<properties fileName=\"%s-%s.%s\" config-optional=\"true\"/>",log4jPrefix,rtEnv,suffix);
     definitions.add(applicationDef);
     definitions.add(databaseDef);
     definitions.add(log4jDef);
     
     String applicationDefault = String.format("<properties fileName=\"%s-%s.%s\"/>",applicationPrefix,defaultPropName,suffix);
     String databaseDefault = String.format("<properties fileName=\"%s-%s.%s\"/>",databasePrefix,defaultPropName,suffix);
     String log4jDefault = String.format("<properties fileName=\"%s-%s.%s\"/>",log4jPrefix,defaultPropName,suffix);
     definitions.add(applicationDefault);
     
     String includeDatabaseDefault = this.getConfigValue(DEFAULT_DATABASE_CONFIGURATION_ENABLED_PROPERTY_NAME, DEFAULT_DATABASE_CONFIGURATION_ENABLED);
     if (includeDatabaseDefault != null && includeDatabaseDefault.equalsIgnoreCase("true")) {
       definitions.add(databaseDefault);
     }
 
     String includeLog4jDefault = this.getConfigValue(DEFAULT_LOG4J_CONFIGURATION_ENABLED_PROPERTY_NAME, DEFAULT_LOG4J_CONFIGURATION_ENABLED);
     if (includeLog4jDefault != null && includeLog4jDefault.equalsIgnoreCase("true")) {
       definitions.add(log4jDefault);
     }
     
     this.bootstrapLogger.debug(String.format("\n\nAppConfig.configure configurationDefinitions\n"));
     for (String definition : definitions) {
       this.bootstrapLogger.debug(String.format("%s",definition));
     }
     this.bootstrapLogger.debug(String.format("\n\n"));
     return definitions;
   }
   
   /**
    * Set configuration options
    * 
    * @param configOpts
    */
   public void setOptions(HashMap<String,String> configOpts) {
     this.configurationOptions = configOpts;
   }
   
   /**
    * Get configuration options
    * 
    * @return Configuration options
    */
    public HashMap<String,String> getOptions() {
      return this.configurationOptions;
    }
   
    /**
     * Get value of the specified setting - in order look at option setting, system, environment then null
     * 
     * @return Named setting or null
     */
    public String getConfigValue(String valueName, String defaultValue) {
      String retVal = valueName;
      if (valueName != null) {
        if (this.configurationOptions == null) {
          retVal = null;
        }
        else {
          if (this.configurationOptions.containsKey(valueName)) {
            retVal = this.configurationOptions.get(valueName);
          }
        }
        if (retVal == null) {
            retVal = AppConfig.sSystemOrEnvironmentValue(valueName);
        }
      }
      if (retVal == null) {
        retVal = defaultValue;
      }
      return retVal;
    }
    
   /**
    * Get the configuration definition as an (XML) string
    */
   public String toString() {
     String nl = System.getProperty("line.separator");
     StringBuffer outputString = new StringBuffer();
     outputString.append(DEFINITION_PREAMBLE).append(nl);
     outputString.append(DEFINITION_ELEMENT_OPEN).append(nl);
     if (getSystemPropertiesOverride() == Boolean.TRUE) {
       outputString.append(DEFINITION_SYSTEM_PROPERTIES).append(nl);
     }
     List<String> variableFileEntries = this.generateVariableDefinitions();
     for (String variableFileEntry : variableFileEntries) {
       outputString.append(variableFileEntry).append(nl);
     }
     outputString.append(DEFINITION_ELEMENT_CLOSE).append(nl);
     return outputString.toString();
   }
   
   /*
    * Set the defaults for processing a build
    */
   private void setDefaults() {
     String sOverrideValue = this.getConfigValue(SYSTEM_PROPERTIES_OVERRIDE_PROPERTY_NAME,null);
     if (sOverrideValue != null && sOverrideValue.equalsIgnoreCase("true")) {
       this.setSystemPropertiesOverride(Boolean.TRUE);
     }
     else {
       this.setSystemPropertiesOverride(Boolean.FALSE);
     }
     this.setRunTimeEnvironment(null);
     this.getExternalConfigurationDirectory();
   }
 
   
 }
