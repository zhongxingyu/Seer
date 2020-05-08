 package org.hackystat.dailyprojectdata.server;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 /**
  * Provides access to the values stored in the dailyprojectdata.properties file. 
  * @author Philip Johnson
  */
 public class ServerProperties {
   
   /** The sensorbase fully qualified host name, such as http://localhost:9876/sensorbase. */
   public static final String SENSORBASE_FULLHOST_KEY =  "dailyprojectdata.sensorbase.host";
   /** The dailyprojectdata hostname key. */
   public static final String HOSTNAME_KEY =        "dailyprojectdata.hostname";
   /** The dailyprojectdata context root. */
   public static final String CONTEXT_ROOT_KEY =     "dailyprojectdata.context.root";
   /** The logging level key. */
   public static final String LOGGING_LEVEL_KEY =   "dailyprojectdata.logging.level";
   /** The dailyprojectdata port key. */
   public static final String PORT_KEY =            "dailyprojectdata.port";
   /** The XML directory key. */
   public static final String XML_DIR_KEY =         "dailyprojectdata.xml.dir";
   /** The Restlet Logging key. */
   public static final String RESTLET_LOGGING_KEY = "dailyprojectdata.restlet.logging";
  /** The sensorbase port key during testing. */
   public static final String TEST_PORT_KEY =       "dailyprojectdata.test.port";
   /** The test installation key. */
   public static final String TEST_INSTALL_KEY =    "dailyprojectdata.test.install";
   /** The test installation key. */
   public static final String TEST_HOSTNAME_KEY =    "dailyprojectdata.test.hostname";
   /** The test installation key. */
   public static final String TEST_SENSORBASE_FULLHOST_KEY = "dailyprojectdata.test.sensorbase.host";
 
   /** Where we store the properties. */
   private Properties properties; 
  
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
    * Reads in the properties in ~/.hackystat/dailyprojectdata/dailyprojectdata.properties 
    * if this file exists, and provides default values for all properties.
    * @throws Exception if errors occur.
    */
   private void initializeProperties () throws Exception {
     String userHome = System.getProperty("user.home");
     String userDir = System.getProperty("user.dir");
     String propFile = userHome + "/.hackystat/dailyprojectdata/dailyprojectdata.properties";
     this.properties = new Properties();
     // Set defaults
     properties.setProperty(SENSORBASE_FULLHOST_KEY, "http://localhost:9876/sensorbase/");
     properties.setProperty(HOSTNAME_KEY, "localhost");
     properties.setProperty(PORT_KEY, "9877");
     properties.setProperty(CONTEXT_ROOT_KEY, "dailyprojectdata");
     properties.setProperty(LOGGING_LEVEL_KEY, "INFO");
     properties.setProperty(RESTLET_LOGGING_KEY, "false");
     properties.setProperty(XML_DIR_KEY, userDir + "/xml");
     properties.setProperty(TEST_PORT_KEY, "9977");
     properties.setProperty(TEST_HOSTNAME_KEY, "localhost");
     properties.setProperty(TEST_SENSORBASE_FULLHOST_KEY, "http://localhost:9976/sensorbase");
     properties.setProperty(TEST_INSTALL_KEY, "false");
     FileInputStream stream = null;
     try {
       stream = new FileInputStream(propFile);
       properties.load(stream);
       System.out.println("Loading DailyProjectData properties from: " + propFile);
     }
     catch (IOException e) {
       System.out.println(propFile + " not found. Using default dailyprojectdata properties.");
     }
     finally {
       if (stream != null) {
         stream.close();
       }
     }
     // make sure that SENSORBASE_HOST always has a final slash.
     String sensorBaseHost = (String) properties.get(SENSORBASE_FULLHOST_KEY);
     if (!sensorBaseHost.endsWith("/")) {
       properties.put(SENSORBASE_FULLHOST_KEY, sensorBaseHost + "/");
     }
   }
   
   /**
    * Sets the following properties' values to their "test" equivalent.
    * <ul>
    * <li> HOSTNAME_KEY
    * <li> PORT_KEY
    * <li> SENSORBASE_FULLHOST_KEY
    * </ul>
    * Also sets TEST_INSTALL_KEY's value to "true".
    */
   public void setTestProperties() {
     properties.setProperty(HOSTNAME_KEY, properties.getProperty(TEST_HOSTNAME_KEY));
     properties.setProperty(PORT_KEY, properties.getProperty(TEST_PORT_KEY));
     properties.setProperty(SENSORBASE_FULLHOST_KEY, 
         properties.getProperty(TEST_SENSORBASE_FULLHOST_KEY));
     properties.setProperty(TEST_INSTALL_KEY, "true");
   }
 
   /**
    * Returns a string indicating current property settings. 
    * @return The string with current property settings. 
    */
   public String echoProperties() {
     String cr = System.getProperty("line.separator"); 
     String eq = " = ";
     String pad = "                ";
     return "DailyProjectData Properties:" + cr +
       pad + SENSORBASE_FULLHOST_KEY   + eq + get(SENSORBASE_FULLHOST_KEY) + cr +
       pad + HOSTNAME_KEY      + eq + get(HOSTNAME_KEY) + cr +
       pad + CONTEXT_ROOT_KEY  + eq + get(CONTEXT_ROOT_KEY) + cr +
       pad + PORT_KEY          + eq + get(PORT_KEY) + cr +
       pad + LOGGING_LEVEL_KEY + eq + get(LOGGING_LEVEL_KEY) + cr +
       pad + TEST_INSTALL_KEY + eq + get(TEST_INSTALL_KEY);
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
    * Returns the fully qualified host name, such as "http://localhost:9877/dailyprojectdata/".
    * @return The fully qualified host name.
    */
   public String getFullHost() {
     return "http://" + get(HOSTNAME_KEY) + ":" + get(PORT_KEY) + "/" + get(CONTEXT_ROOT_KEY) + "/";
   }
 
 }
