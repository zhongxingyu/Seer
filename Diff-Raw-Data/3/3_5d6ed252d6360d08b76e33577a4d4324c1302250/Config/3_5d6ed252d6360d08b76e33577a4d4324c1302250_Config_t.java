 /*
  * server configuration is in properties file
  */
 
 package simpleWebServer;
  
 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 
 class WebRootNotFoundError extends Exception {
     private static final long serialVersionUID = 0L;
     public WebRootNotFoundError(String message) {
         super(message);
     }
 }
 
 class ConfigDefaults {
 
     public ConfigDefaults() {}
 
     public ConfigDefaults(Config c) {
         assignTo(c);
     }
 
     public void assignTo(Config c) {
         c.root = new File(System.getProperty("user.dir"));
         c.port = 8080;
         c.timeout = 5000;
         c.maxWorkersInPool = 5;
     }
 
     public void checkAndComplete(Config c) {
         if (c.root == null) {
             c.root = new File(System.getProperty("user.dir"));
         }
         if (c.port == 0) {
             c.port = 8080;
         }
         if (c.timeout <= 1000) {
             c.timeout = 5000;
         }
         if (c.maxWorkersInPool < 25) {
             c.maxWorkersInPool = 5;
         }
         if (c.logger == null) {
             c.logger = new SimpleLogger();
         }
     }
 }
 
 
 class Config {
     File root = null;
     int port = 0;
     int timeout = 0;
     int maxWorkersInPool = 0;
     Logger logger = null;
     ConfigDefaults defaults = null;
 
     public Config(ConfigDefaults defaults, Logger logger) {
         this.logger = logger;
         this.defaults = defaults;
     }
 
     public void load() throws IOException, WebRootNotFoundError {
         File storedProperties = new File(System.getProperty("java.home")
                     + File.separator
                     + "lib" + File.separator + "www-server.properties");
         if (!storedProperties.exists()) {
             defaults.assignTo(this);
             return;
         }
 
         Properties properties = new Properties();
         InputStream is = new BufferedInputStream(
                             new FileInputStream(storedProperties));
         properties.load(is);
         is.close();
 
         assign(properties);
         defaults.checkAndComplete(this);
     }
 
     protected void assign(Properties properties) throws WebRootNotFoundError {
         String rootDirName = properties.getProperty("root");
         if (rootDirName != null) {
             root = new File(rootDirName);
             if (!root.exists()) {
                 String message = rootDirName + " doesn't exist as server root";
                 throw new WebRootNotFoundError(message);
             }
         }
         String property = "";
         property = properties.getProperty("timeout");
         if (property != null) {
             timeout = Integer.parseInt(property);
         }
         property = properties.getProperty("workers");
         if (property != null) {
             maxWorkersInPool = Integer.parseInt(property);
         }
         property = properties.getProperty("log");
         if (property != null) {
             String logName = property;
             logger = new StreamLogger(logName);
         }
     }
 
     public void list() {
         logger.log("root=" + root);
         logger.log("timeout=" + timeout);
         logger.log("workers=" + maxWorkersInPool);
        logger.log("logger=" + logger.getClass());
     }
 }
 
