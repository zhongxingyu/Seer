 package com.mymed.utils;
 
 import org.slf4j.LoggerFactory;
 
 import ch.qos.logback.classic.Logger;
 
 /**
  * Class to hold the logger facilities for mymed.
  * 
  * 
  * @author Milo Casagrande
  * 
  */
 public final class MLogger {
   private static final MLogger INSTANCE = new MLogger();
 
   // The name of the default mymed backend logger as defined in logback.xml
   private static final String DEFAULT_LOG_NAME = "mymed.backend.logger";
 
  private final Logger BACKEND_LOGGER;
 
   // Private constructor since all methods are static and we have a singleton
   private MLogger() {
    BACKEND_LOGGER = (Logger) LoggerFactory.getLogger(DEFAULT_LOG_NAME);
   }
 
   /**
    * Retrieve the MLogger instance to be used as a singleton
    * 
    * @return the MLogger instance
    */
   public static MLogger getInstance() {
     return INSTANCE;
   }
 
   /**
    * Get the default mymed backend logger
    * 
    * @return the default logger
    */
   @Deprecated
   public static Logger getLog() {
    return getInstance().BACKEND_LOGGER;
   }
 
   /**
    * Get the 'info' level logger of mymed
    * 
    * @return the info level logger
    */
   @Deprecated
   public static Logger getInfoLog() {
    return getInstance().BACKEND_LOGGER;
   }
 
   /**
    * Get the 'debug' level logger of mymed
    * 
    * @return the debug level logger
    */
   @Deprecated
   public static Logger getDebugLog() {
    return getInstance().BACKEND_LOGGER;
   }
 
   /**
    * Get the default mymed backend logger
    * 
    * @return the default logger
    */
   public static Logger getLogger() {
    return getInstance().BACKEND_LOGGER;
   }
 }
