 package org.hackystat.projectbrowser;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Map;
 import java.util.Properties;
 
 /**
  * Provides access to the values stored in the sensorbase.properties file. 
  * @author Philip Johnson
  */
 public class ProjectBrowserProperties {
   
   /** The sensorbase host.  */
   public static final String SENSORBASE_HOST_KEY =  "projectbrowser.sensorbase.host";
   /** The dpd host.  */
   public static final String DAILYPROJECTDATA_HOST_KEY =  "projectbrowser.dailyprojectdata.host";
   /** The telemetry host.  */
   public static final String TELEMETRY_HOST_KEY =  "projectbrowser.telemetry.host";
   /** The logging level.  */
   public static final String LOGGING_LEVEL_KEY =  "projectbrowser.logging.level";
   /** The admin email key. */
   public static final String ADMIN_EMAIL_KEY =  "projectbrowser.admin.email";
   /** How to control wicket development vs. deployment mode. */
   public static final String WICKET_CONFIGURATION_KEY =  "projectbrowser.wicket.configuration";
   
   // Not sure yet if we need the following.
   /** The projectbrowser hostname key. */
   //public static final String HOSTNAME_KEY = "projectbrowser.hostname";
   /** The projectbrowser context root. */
   //public static final String CONTEXT_ROOT_KEY = "projectbrowser.context.root";
   /** The projectbrowser port key. */
   //public static final String PORT_KEY = "projectbrowser.port";
   /** The SMTP host key. */
   //public static final String SMTP_HOST_KEY =  "projectbrowser.smtp.host";
   /** The test install host key. */
   //public static final String TEST_INSTALL_KEY =  "projectbrowser.test.install";
   
   
   /** Where we store the properties. */
   private Properties properties; 
   
   /**
    * Creates a new ProjectBrowserProperties instance. 
    * Prints an error to the console if problems occur on loading.
    * @param properties A properties instance containing values that override those in the 
    * properties file. Can be null if no properties are to be overridden.  
    */
   public ProjectBrowserProperties(Properties properties) {
     try {
       initializeProperties(properties);
     }
     catch (Exception e) {
       System.out.println("Error initializing projectbrowser properties.");
     }
   }
  
 
   /**
    * Reads in the properties in ~/.hackystat/projectbrowser/projectbrowser.properties
    * if this file exists, 
    * and provides default values for all properties not mentioned in this file.
    * @param testProperties A properties instance containing values that override those in the 
    * @throws Exception if errors occur.
    */
   private void initializeProperties (Properties testProperties) throws Exception {
     String userHome = System.getProperty("user.home");
     String propFile = userHome + "/.hackystat/projectbrowser/projectbrowser.properties";
     this.properties = new Properties();
     // Set defaults.
     properties.setProperty(SENSORBASE_HOST_KEY, "http://localhost:9876/sensorbase");
     properties.setProperty(DAILYPROJECTDATA_HOST_KEY, "http://localhost:9877/dailyprojectdata");
     properties.setProperty(TELEMETRY_HOST_KEY, "http://localhost:9878/telemetry");
     properties.setProperty(LOGGING_LEVEL_KEY, "INFO");
     properties.setProperty(ADMIN_EMAIL_KEY, "johnson@hackystat.org");
 
     // Now read in the properties file, and override the defaults if supplied. 
     FileInputStream stream = null;
     try {
       stream = new FileInputStream(propFile);
       properties.load(stream);
       System.out.println("Loading ProjectBrowser properties from: " + propFile);
     }
     catch (IOException e) {
       System.out.println(propFile + " not found. Using default properties.");
     }
     finally {
       if (stream != null) {
         stream.close();
       }
     }
     // If we have supplied some properties as an argument, use these to override whatever we've 
     // done so far. 
     if (testProperties != null) {
       this.properties.putAll(testProperties);
     }
     trimProperties(properties);
     String wicketConfig = properties.getProperty(WICKET_CONFIGURATION_KEY);
    if (wicketConfig == null) {
       System.out.println("Setting wicket.configuration to: " + wicketConfig);
       System.setProperty("wicket.configuration", wicketConfig);
     }
     
     // I don't think we need to do the following for ProjectBrowser, since we aren't using Mailer. 
     // Now add to System properties. Since the Mailer class expects to find this stuff on the 
     // System Properties, we will add everything to it.  In general, however, clients should not
     // use the System Properties to get at these values, since that precludes running several
     // SensorBases in a single JVM.   And just is generally bogus. 
     //Properties systemProperties = System.getProperties();
     //systemProperties.putAll(properties);
     //System.setProperties(systemProperties);
   }
   
   /**
    * Returns a string containing the current properties.
    * @return A string with the properties.  
    */
   public String echoProperties() {
     String cr = System.getProperty("line.separator"); 
     String eq = " = ";
     String pad = "                ";
     return "ProjectBrowser Properties:" + cr +
       pad + SENSORBASE_HOST_KEY      + eq + get(SENSORBASE_HOST_KEY) + cr +
       pad + DAILYPROJECTDATA_HOST_KEY  + eq + get(DAILYPROJECTDATA_HOST_KEY) + cr +
       pad + TELEMETRY_HOST_KEY          + eq + get(TELEMETRY_HOST_KEY) + cr +
       pad + LOGGING_LEVEL_KEY   + eq + get(LOGGING_LEVEL_KEY) + cr +
       pad + WICKET_CONFIGURATION_KEY   + eq + get(WICKET_CONFIGURATION_KEY) + cr +
       pad + ADMIN_EMAIL_KEY   + eq + get(ADMIN_EMAIL_KEY);
   }
   
   /**
    * Returns the value of the ProjectBrowser Property specified by the key.
    * @param key Should be one of the public static final strings in this class.
    * @return The value of the key, or null if not found.
    */
   public String get(String key) {
     return this.properties.getProperty(key);
   }
   
   /**
    * Ensures that the there is no leading or trailing whitespace in the property values.
    * The fact that we need to do this indicates a bug in Java's Properties implementation to me. 
    * @param properties The properties. 
    */
   private void trimProperties(Properties properties) {
     // Have to do this iteration in a Java 5 compatible manner. no stringPropertyNames().
     for (Map.Entry<Object, Object> entry : properties.entrySet()) {
       String propName = (String)entry.getKey();
       properties.setProperty(propName, properties.getProperty(propName).trim());
     }
   }
 }
