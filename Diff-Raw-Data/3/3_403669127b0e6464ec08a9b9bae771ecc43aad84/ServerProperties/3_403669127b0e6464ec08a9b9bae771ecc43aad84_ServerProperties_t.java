 package org.hackystat.sensorbase.server;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Map;
 import java.util.Properties;
 import java.util.TreeMap;
 
 /**
  * Provides access to the values stored in the sensorbase.properties file. 
  * @author Philip Johnson
  */
 public class ServerProperties {
   
   /** The admin email key. */
   public static final String ADMIN_EMAIL_KEY =     "sensorbase.admin.email";
   /** The admin password. */
   public static final String ADMIN_PASSWORD_KEY =   "sensorbase.admin.password";
   /** The context root key. */
   public static final String CONTEXT_ROOT_KEY =    "sensorbase.context.root";
   /** The database directory key. */
   public static final String DB_DIR_KEY =          "sensorbase.db.dir";
   /** The database implementation class. */
   public static final String DB_IMPL_KEY =          "sensorbase.db.impl";
   /** The hostname key. */
   public static final String HOSTNAME_KEY =        "sensorbase.hostname";
   /** The logging level key. */
   public static final String LOGGING_LEVEL_KEY =   "sensorbase.logging.level";
   /** The Restlet Logging key. */
   public static final String RESTLET_LOGGING_KEY = "sensorbase.restlet.logging";
   /** The SMTP host key. */
   public static final String SMTP_HOST_KEY =       "sensorbase.smtp.host";
   /** The sensorbase port key. */
   public static final String PORT_KEY =            "sensorbase.port";
   /** The XML directory key. */
   public static final String XML_DIR_KEY =         "sensorbase.xml.dir";
   /** The test installation key. */
   public static final String TEST_INSTALL_KEY =    "sensorbase.test.install";
   /** The test domain key. */
   public static final String TEST_DOMAIN_KEY =     "sensorbase.test.domain";
   /** The sensorbase port key during testing. */
   public static final String TEST_PORT_KEY =       "sensorbase.test.port";
   /** The sensorbase db dir during testing. */
   public static final String TEST_DB_DIR_KEY =       "sensorbase.test.db.dir";
   /** The test admin email key. */
   public static final String TEST_ADMIN_EMAIL_KEY =     "sensorbase.test.admin.email";
   /** The test admin password. */
   public static final String TEST_ADMIN_PASSWORD_KEY =   "sensorbase.test.admin.password";  
   /** The test hostname. */
   public static final String TEST_HOSTNAME_KEY =   "sensorbase.test.hostname";  
   /** Whether to compress on startup. */
   public static final String COMPRESS_ON_STARTUP_KEY =   "sensorbase.db.startup.compress";  
   /** Whether to re-index on startup. */
   public static final String REINDEX_ON_STARTUP_KEY =   "sensorbase.db.startup.reindex";  
   /** Where we store the properties. */
   private Properties properties; 
   
   private static String FALSE = "false";
   
   /**
    * Creates a new ServerProperties instance. 
    * Prints an error to the console if problems occur on loading. 
    */
   public ServerProperties() {
     try {
       initializeProperties();
     }
     catch (Exception e) {
       System.out.println("Error initializing server properties.");
     }
   }
   
   /**
    * Reads in the properties in ~/.hackystat/sensorbase.properties if this file exists,
    * and provides default values for all properties not mentioned in this file.
    * Will also add any pre-existing System properties that start with "sensorbase.".
    * @throws Exception if errors occur.
    */
   private void initializeProperties () throws Exception {
     String userHome = System.getProperty("user.home");
     String userDir = System.getProperty("user.dir");
     String hackyHome = userHome + "/.hackystat";
     String sensorBaseHome = hackyHome + "/sensorbase"; 
     String propFile = userHome + "/.hackystat/sensorbase/sensorbase.properties";
     String defaultAdmin = "admin@hackystat.org";
     this.properties = new Properties();
     // Set defaults for 'standard' operation. These will override any previously
     properties.setProperty(ADMIN_EMAIL_KEY, defaultAdmin);
     properties.setProperty(ADMIN_PASSWORD_KEY, defaultAdmin);
     properties.setProperty(CONTEXT_ROOT_KEY, "sensorbase");
     properties.setProperty(DB_DIR_KEY, sensorBaseHome + "/db");
     properties.setProperty(DB_IMPL_KEY, "org.hackystat.sensorbase.db.derby.DerbyImplementation");
     properties.setProperty(HOSTNAME_KEY, "localhost");
     properties.setProperty(LOGGING_LEVEL_KEY, "INFO");
     properties.setProperty(RESTLET_LOGGING_KEY, FALSE);
     properties.setProperty(SMTP_HOST_KEY, "mail.hawaii.edu");
     properties.setProperty(PORT_KEY, "9876");
     properties.setProperty(XML_DIR_KEY, userDir + "/xml");
     properties.setProperty(TEST_DOMAIN_KEY, "hackystat.org");
     properties.setProperty(TEST_INSTALL_KEY, FALSE);
     properties.setProperty(TEST_ADMIN_EMAIL_KEY, defaultAdmin);
     properties.setProperty(TEST_ADMIN_PASSWORD_KEY, defaultAdmin);
     properties.setProperty(TEST_DB_DIR_KEY, sensorBaseHome + "/testdb");
     properties.setProperty(TEST_PORT_KEY, "9976");
     properties.setProperty(TEST_HOSTNAME_KEY, "localhost");
     properties.setProperty(COMPRESS_ON_STARTUP_KEY, FALSE);
     properties.setProperty(REINDEX_ON_STARTUP_KEY, FALSE);
 
     FileInputStream stream = null;
     try {
       stream = new FileInputStream(propFile);
       properties.load(stream);
       System.out.println("Loading SensorBase properties from: " + propFile);
     }
     catch (IOException e) {
       System.out.println(propFile + " not found. Using default sensorbase properties.");
     }
     finally {
       if (stream != null) {
         stream.close();
       }
     }
    addSensorBaseSystemProperties(this.properties);
     trimProperties(properties);
     
     // Now add to System properties. Since the Mailer class expects to find this stuff on the 
     // System Properties, we will add everything to it.  In general, however, clients should not
     // use the System Properties to get at these values, since that precludes running several
     // SensorBases in a single JVM.   And just is generally bogus. 
     Properties systemProperties = System.getProperties();
     systemProperties.putAll(properties);
     System.setProperties(systemProperties);
   }
   
   /**
    * Finds any System properties whose key begins with "sensorbase.", and adds those
    * key-value pairs to the passed Properties object. 
    * @param properties The properties instance to be updated with the SensorBase system 
    * properties. 
    */
   private void addSensorBaseSystemProperties(Properties properties) {
     Properties systemProperties = System.getProperties();
     for (Map.Entry<Object, Object> entry : systemProperties.entrySet()) {
       String sysPropName = (String)entry.getKey();
       if (sysPropName.startsWith("sensorbase.")) {
         String sysPropValue = (String)entry.getValue();
         properties.setProperty(sysPropName, sysPropValue);
       }
     }
   }
   
   /**
    * Sets the following properties to their "test" equivalents.
    * <ul>
    * <li> ADMIN_EMAIL_KEY
    * <li> ADMIN_PASSWORD_KEY
    * <li> HOSTNAME_KEY
    * <li> DB_DIR_KEY
    * <li> PORT_KEY
    * <li> XML_DIR_KEY (if HACKYSTAT_SENSORBASE_HOME is in System properties).
    * <ul>
    * Also sets TEST_INSTALL_KEY to true.
    */
   public void setTestProperties() {
     properties.setProperty(ADMIN_EMAIL_KEY, properties.getProperty(TEST_ADMIN_EMAIL_KEY));
     properties.setProperty(ADMIN_PASSWORD_KEY, properties.getProperty(TEST_ADMIN_PASSWORD_KEY));
     properties.setProperty(HOSTNAME_KEY, properties.getProperty(TEST_HOSTNAME_KEY));
     properties.setProperty(DB_DIR_KEY, properties.getProperty(TEST_DB_DIR_KEY));
     properties.setProperty(PORT_KEY, properties.getProperty(TEST_PORT_KEY));
     properties.setProperty(TEST_INSTALL_KEY, "true");
     // Change the XML dir location if HACKYSTAT_SENSORBASE_HOME exists. 
     String sensorbaseHome = System.getProperty("HACKYSTAT_SENSORBASE_HOME");
     if (sensorbaseHome != null) {
       File file = new File(sensorbaseHome, "xml");
       if (file.exists()) {
         properties.setProperty(XML_DIR_KEY, file.getAbsolutePath());
       }
       else {
         System.out.println("Bad HACKYSTAT_SENSORBASE_HOME: " + sensorbaseHome);
       }
     }
     // Change the db implementation class if DB_IMPL_KEY is in system properties. 
     String dbImpl = System.getProperty(DB_IMPL_KEY);
     if (dbImpl != null) {
       properties.setProperty(DB_IMPL_KEY, dbImpl);
     }
     trimProperties(properties);
     // update the system properties object to reflect these new values. 
     Properties systemProperties = System.getProperties();
     systemProperties.putAll(properties);
     System.setProperties(systemProperties);    
   }
 
   /**
    * Returns a string containing all current properties in alphabetical order.
    * @return A string with the properties.  
    */
   public String echoProperties() {
     String cr = System.getProperty("line.separator"); 
     String eq = " = ";
     String pad = "                ";
     // Adding them to a treemap has the effect of alphabetizing them. 
     TreeMap<String, String> alphaProps = new TreeMap<String, String>();
     for (Map.Entry<Object, Object> entry : this.properties.entrySet()) {
       String propName = (String)entry.getKey();
       String propValue = (String)entry.getValue();
       alphaProps.put(propName, propValue);
     }
     StringBuffer buff = new StringBuffer(25);
     buff.append("SensorBase Properties:").append(cr);
     for (String key : alphaProps.keySet()) {
       buff.append(pad).append(key).append(eq).append(get(key)).append(cr);
     }
     return buff.toString();
   }
   
   /**
    * Returns the value of the Server Property specified by the key.
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
    
   /**
    * Returns the fully qualified host name, such as "http://localhost:9876/sensorbase/".
    * @return The fully qualified host name.
    */
   public String getFullHost() {
     return "http://" + get(HOSTNAME_KEY) + ":" + get(PORT_KEY) + "/" + get(CONTEXT_ROOT_KEY) + "/";
   }
   
   /**
    * True if the sensorbase.properties file indicates that the user wishes to compress the db
    * on startup. 
    * @return True if compress on startup.
    */
   public boolean compressOnStartup () {
     return this.properties.getProperty(COMPRESS_ON_STARTUP_KEY).equalsIgnoreCase("true");
   }
   
   /**
    * True if the sensorbase.properties file indicates the user wants to reindex on startup. 
    * @return True if reindex on startup. 
    */
   public boolean reindexOnStartup () {
     return this.properties.getProperty(REINDEX_ON_STARTUP_KEY).equalsIgnoreCase("true");
   }
 }
