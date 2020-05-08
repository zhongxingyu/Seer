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
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.configuration.CombinedConfiguration;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * 
  * <h1>AppConfig V2.0 - Application configuration the easy way</h1>
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
  * <h2>Typical usage</h2>
  * 
  * <p>Suppose the settings for a component or a program that&#39;s been built with AppConfig support need to be changed.</p>
  * 
  * <h3>Create a configuration directory</h3>
  * 
  * <pre><code>  mkdir ~/local_appconfig
  * </code></pre>
  * 
  * <h3>Define environment-specific configurations</h3>
  * 
  * <p>Put environment-specific settings into the configuration directory in files with the naming patterns:</p>
  * 
  * <ul>
  * <li>application-[environemnt].properties</li>
  * <li>database-[environment].properties</li>
  * <li>log4j-[environment].properties</li>
  * </ul>
  * 
  * <p>The contents of the files follow Java Property file syntax. The properties clearly have to match those that configure the component that&#39;s being configured.</p>
  * 
  * <h3>Tell AppConfig where the directory is</h3>
  * 
  * <p>Either</p>
  * 
  * <pre><code>  export com.verymuchme.appconfig.externalConfigurationDirectory=~/local_appconfig
  * </code></pre>
  * 
  * <p>or</p>
  * 
  * <pre><code>  java â€¦ -Dcom.verymuchme.appconfig.externalConfigurationDirectory=~/local_appconfig ...
  * </code></pre>
  * 
  * <h3>(Production only) Tell AppConfig to load production configurations</h3>
  * 
  * <p>Either</p>
  * 
  * <pre><code>  export com.verymuchme.appconfig.runTimeEnvironment=production
  * </code></pre>
  * 
  * <p>or</p>
  * 
  * <pre><code>  java â€¦ -Dcom.verymuchme.appconfig.externalConfigurationDirectory=~/local_appconfig \
  *          -Dcom.verymuchme.appconfig.runTimeEnvironment=production ...
  * </code></pre>
  * 
  * <p>By default, AppConfig assumes you&#39;re running in &#39;development&#39; mode.</p>
  * 
  * <h2>That&#39;s it!!</h2>
  * 
  * <h2>Some details</h2>
  * 
  * <p>AppConfig is designed to provide painless configuration for multiple runtime environments based on a model similar to the Ruby-on-Rails &#39;development/production/test&#39; approach with all the appropriate defaulting and overriding of settings.</p>
  * 
  * <p>It is particularly targeted at the problem of configuring components and applications &#39;from the outside&#39; without needing to modify the packaged artifact.</p>
  * 
  * <p>It is based on Apache Commons Configuration http://commons.apache.org/configuration .
  * AppConfig is designed to provide painless multi-environment, external and internal configuration with default settings for components and programs.</p>
  * 
  * <h3>Using AppConfig in code</h3>
  * 
  * <h4>Initialize AppConfig</h4>
  * 
  * <pre><code>  AppConfig appConfig = new AppConfig();
  *   appConfig.setApplicationPropertiesPackageName(&quot;your base package name&quot;);
  *   appConfig.setExternalConfigurationDirectory(&quot;configuration directory name&quot;);
  *   appConfig.configure();
  * </code></pre>
  * 
 * <h5>Where to put configuration files</h5>
 * 
 * <p>Configuration files should be stored at the root of your package. </p>
 * 
 * <p><strong>Do NOT</strong> place them at the root your source tree. If you do, unpredictable things will happen when you try to load configuations in a project containing multiple AppConfig-enabled components. </p>
 * 
 * <p>Example:</p>
 * 
 * <p>AppConfig itself is a Maven-based package with a base package of &#39;com.verymuchme.appconfig&#39;. So, configuration files would be placed in:</p>
 * 
 * <pre><code>./src/main/resources/com/verymuchme/appconfig
 * </code></pre>
 * 
  * <h4>Configurations load sequence</h4>
  * 
 * <p>The following files will be automatically loaded in the order shown. </p>
  * 
  * <pre><code>  application-development.properties (optional)
  *   database-development.properties (optional)
  *   log4j-development.properties (optional)
  *   application-defaults.properties 
  *   database-defaults.properties
  *   log4j-defaults.properties (optional)
  * </code></pre>
  * 
  * <p>For each file, the search order is the following:</p>
  * 
  * <ul>
  * <li>the configuration directory (if defined)</li>
  * <li>the user directory</li>
  * <li>any jars in the classpath</li>
  * <li>any jars in the system classpath</li>
  * </ul>
  * 
  * <p>Following the Apache Commons Configuration convention, if the same key found in multiple files, the value from the first file found and loaded wins. So, in this case, &#39;development&#39; trumps &#39;defsults&#39; - just the desired behavior.</p>
  * 
  * <p>In general, if a file is shown, it must be present in one of the locations or an exception will be thrown. It may be empty. &#39;Optional&#39; indicates that the file can be absent. </p>
  * 
  * <h3>And configuration settings are available</h3>
  * 
  * <pre><code>CombinedConfiguration configuration = appConfig.getCombinedConfiguration();
  * 
  * // See Apache Commons Configuration API docs for more information 
  * // http://commons.apache.org/configuration/apidocs/index.html
  * String configurationValue = combinedConfiguration.getString(&quot;some property name&quot;); 
  * </code></pre>
  * 
  * <h2>Advanced information</h2>
  * 
  * <h3>Error handling</h3>
  * 
  * <p>Application or component configuration normally happens at point where the artifact starts. It&#39;s also usually the case that a problem with the configuration is serious if not fatal.</p>
  * 
  * <p>That assumption led to two simple decisions. </p>
  * 
  * <ul>
  * <li>If AppConfig encounters a problem when configuring the artifact, it will log the problem and throw an exception.</li>
  * <li>All exceptions are wrapped with AppConfigException - <strong>an unchecked, Runtime exception</strong>.</li>
  * </ul>
  * 
  * <p>So, a try/catch is needed around the AppConfig initialization (shown above) if the code wants to detect configuration errors and exit gracefully.</p>
  * 
  * <h3>Overriding default behaviors</h3>
  * 
  * <p>Most AppConfig internal behaviors can be overriden with appropriate feature switches. </p>
  * 
  * <p>The value of a particular feature switch is determined hierarchically, with the first value found for a particular setting being used. The searcn order in which the switch settings are examined is:</p>
  * 
  * <ul>
  * <li>Internal, programmatically defined options
  * 
  * <ul>
  * <li>Specified when initializing AppConfig using the &#39;appConfig.setOptions(..)&#39; syntax</li>
  * </ul></li>
  * <li>Command-line options
  * 
  * <ul>
  * <li>Specified using JVM &#39;-D[setting name]=[setting value]&#39; syntax</li>
  * </ul></li>
  * <li>Environment settings
  * 
  * <ul>
  * <li>Specified using &#39;export [setting name]=[setting value]&#39; syntax or equivalent</li>
  * </ul></li>
  * <li>Internal default value
  * 
  * <ul>
  * <li>Default internal settings are managed as property files so property File syntax applies</li>
  * </ul></li>
  * </ul>
  * 
  * <p><strong>The setting name is the same in all cases.</strong></p>
  * 
  * <p>The following internal settings are available:</p>
  * 
  * <pre><code># 
  * # Internal defaults for all settings that can be overridden
  * #
  * # Property values are specified with a slightly extended property syntax
  * # 
  * # &quot;true&quot; / &quot;false&quot; are recognized as boolean values
  * # &quot;null&quot; is recognized as the null value
  * # a comma delimited list of values is recognized as an array of string values
  * # 
  * 
  * # File name for internal default properties
  * com.verymuchme.appconfig.internalDefaultPropertiesFileName = null
  * 
  * # Include the Apache Commons Configuration definition file to include the &quot;&lt;system/&gt;&quot; element as the first definition
  * com.verymuchme.appconfig.systemPropertiesOverride = false
  * 
  * # Allowed run time environment names
  * com.verymuchme.appconfig.permittedRunTimeEnvironments = development,production,test
  * # Current run time environment name
  * com.verymuchme.appconfig.runTimeEnvironment = development
  * # Default run time environment name
  * com.verymuchme.appconfig.defaultRunTimeEnvironment = development
  * 
  * # External configuration directory
  * com.verymuchme.appconfig.externalConfigurationDirectory = null
  * 
  * # Default prefix for application configuration files
  * com.verymuchme.appconfig.applicationConfigurationPrefix = application
  * 
  * # Default prefix for database configuration files
  * com.verymuchme.appconfig.database.ConfigurationPrefix = database
  * # Require a default database configuration file
  * com.verymuchme.appconfig.database.defaultConfigurationEnabled = true
  * 
  * # Default prefix for a log4j configuration file
  * com.verymuchme.appconfig.log4j.ConfigurationPrefix = log4j
  * # Require a default log4j configuration file
  * com.verymuchme.appconfig.log4j.defaultConfigurationEnabled = true
  * 
  * # Default suffix for all configuration files
  * com.verymuchme.appconfig.configurationNameSuffix = properties
  * 
  * # Default prefix for default configuration files
  * com.verymuchme.appconfig.defaultConfigurationName = defaults
  * 
  * # Default name for Freemarker configuration template
  * com.verymuchme.appconfig.configurationTemplateName = configuration-xml.ftl
  * 
  * # Base class for Freemarker to use to find template
  * com.verymuchme.appconfig.freemarker.baseClassName = null
  * 
  * # Default AppConfig logging level at startup
  * com.verymuchme.appconfig.log4j.logLevel = ERROR
  * 
  * # Name of AppConfig internal log4j configuration file
  * com.verymuchme.appconfig.log4j.internalConfigurationFileName = log4j-internal-logger.properties
  * 
  * # Default value of application properties package name
  * application.propertiesPackageName = null
  * 
  * # Default value of application properties package directory
  * application.propertiesPackageDir = null
  * </code></pre>
  * 
  * <h2>Maven dependency information</h2>
  * 
  * <pre><code>&lt;repository&gt;
  *   &lt;id&gt;mvn-repo-releases&lt;/id&gt;
  *   &lt;url&gt;https://github.com/tflynn/mvn-repo-public/raw/master/releases&lt;/url&gt;
  * &lt;/repository&gt;
  * 
  * &lt;repository&gt;
  *   &lt;id&gt;mvn-repo-snapshots&lt;/id&gt;
  *   &lt;url&gt;https://github.com/tflynn/mvn-repo-public/raw/master/snapshots&lt;/url&gt;
  * &lt;/repository&gt;
  * 
  * &lt;dependency&gt;
  *   &lt;groupId&gt;com.verymuchme.appconfig&lt;/groupId&gt;
  *   &lt;artifactId&gt;app_config&lt;/artifactId&gt;
  *   &lt;version&gt;2.0&lt;/version&gt;
  * &lt;/dependency&gt;
  * </code></pre>
  * 
  * @author Tracy Flynn
  * @version 2.0
  * @since 1.0
  */
 public class AppConfig {
   
   /*
    * Logger instance
    */
   private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
   
   /*
    * Configuration options
    */
   private HashMap<String,String> configurationOptions = new HashMap<String,String>();
 
   /*
    * Private copy of Configuration object returned from DefaultConfigurationBuilder
    */
   private CombinedConfiguration combinedConfiguration = null;
   
   /*
    * Base application package name with which to locate application property files 
    */
   private String applicationPropertiesPackageName = null;
   
   /*
    * External directory for configuration
    */
   private String externalConfigurationDirectory = null;
   
 
   /**
    * Create a new AppConfig instance
    */
   public AppConfig() {
   }
 
   /**
    * Configure AppConfig
    */
   public void configure() {
     
     // Configure logging using internal defaults. Allow override for initial logging level from configuration options
     String logLevelOverride = this.configurationOptions.get(ConfigurationDefinitionBuilder.DEFAULT_LOGGING_LEVEL_PROPERTY_NAME);
     Log4jHelper.loadInitialInternalLogger(logLevelOverride);
     logger.trace(String.format("AppConfig.configure Added initial internal logger"));
     
     
     // Get the configuration definition
     // Make sure to pass the values from the convenience accessors 
     this.configurationOptions.put(ConfigurationDefinitionBuilder.APPLICATION_PROPERTIES_PACKAGE_NAME_PROPERTY_NAME, this.applicationPropertiesPackageName);
     this.configurationOptions.put(ConfigurationDefinitionBuilder.EXTERNAL_CONFIGURATION_DIRECTORY_PROPERTY_NAME,this.externalConfigurationDirectory);
     ConfigurationDefinitionBuilder configurationBuilder = new ConfigurationDefinitionBuilder(this.configurationOptions);
     if (logger.isTraceEnabled()) {
       logger.trace(String.format("AppConfig.configure runtime options passed to ConfigurationDefinitionBuilder"));
       AppConfigUtils.dumpMap(this.configurationOptions);
     }
     
     
     // Go and generate and default all property settings as appropriate
     configurationBuilder.setInternalProperties();
     
     // Now that all the properties are loaded and defaulted appropriately, load the real internal Log4j configuration
     Log4jHelper.loadInternalLogger(configurationBuilder.getInternalProperties());
 
     // Now generate the actual configuration
     String configurationDefinition = configurationBuilder.generateConfigurationDefinition();
 
     logger.trace(String.format("AppConfig.configure Generated configuration definitions"));
     
     // Configure application-level logging
     List<String> log4jConfigNames = configurationBuilder.generateLog4jConfigurationNames();
     Log4jHelper.loadLog4jConfiguration(log4jConfigNames, logger);
     logger.trace(String.format("AppConfig.configure loaded application logging"));
 
     // Load the configuration definition
     this.combinedConfiguration = configurationBuilder.loadConfigurationDefinition(configurationDefinition);
     
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
     * Get the current configuration object
     * 
     * @return Current configuration object
     */
    public CombinedConfiguration getCombinedConfiguration() {
      return combinedConfiguration;
    }
 
    /**
     * Set the current configuration object
     * 
     * @param combinedConfiguration Current configuration object
     */
    public void setCombinedConfiguration(CombinedConfiguration combinedConfiguration) {
      this.combinedConfiguration = combinedConfiguration;
    }
 
    /**
     * Get the base application package name with which to locate application property files
     * 
     * @return Package name
     */
    public String getApplicationPropertiesPackageName() {
      return this.applicationPropertiesPackageName;
    }
 
    /**
     * Set the base application package name with which to locate application property files
     * 
     * @param applicationPropertiesPackageName
     */
    public void setApplicationPropertiesPackageName(String applicationPropertiesPackageName) {
      this.applicationPropertiesPackageName = applicationPropertiesPackageName;
    }
 
    /**
     * Get the external configuration directory
     * 
     * @return External configuration directory
     */
    public String getExternalConfigurationDirectory() {
      return externalConfigurationDirectory;
    }
 
    /**
     * Set the external configuration directory
     * 
     * @param externalConfigurationDirectory
     */
    public void setExternalConfigurationDirectory(String externalConfigurationDirectory) {
      this.externalConfigurationDirectory = externalConfigurationDirectory;
    }
    
 }
