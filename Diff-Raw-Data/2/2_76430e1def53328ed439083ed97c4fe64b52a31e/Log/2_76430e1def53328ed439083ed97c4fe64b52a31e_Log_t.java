 package edu.umro.util;
 
 /*
  * Copyright 2012 Regents of the University of Michigan
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Properties;
 import java.util.logging.FileHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 /**
  * Support logging services based on the <code>java.util.logging</code>
  * package.
  * <p>
  * In general, all logging is sent to files that are overwritten on a
 * rotating basis.
  * 
  * @author Jim Irrer  irrer@umich.edu 
  *
  */
 public class Log {
 
     /** Logger for this service. */
     private volatile static Logger serviceLogger = null;
 
     /** Puts log messages into files. */
     private volatile static FileHandler fileHandler = null;
 
     /** Formats log entries in a detailed way. */
     private volatile static LogFormatter serviceFormatter = null;
 
 
     /**
      * Get the service-wide logger.
      * 
      * @return Logger for this service.
      */
     public static Logger get() {
         initLogging();
         return serviceLogger;
     }
 
 
     /**
      * Set the logging level.
      * 
      * @param level New logging level.
      * @throws IOException 
      * @throws SecurityException 
      */
     public static void setLevel(Level level) throws SecurityException, IOException {
         initLogging();
         fileHandler.setLevel(level);
     }
 
 
     /**
      * Determine if the logging is viable, meaning that it is ready for use.  This
      * method is used in situations where logging may have not yet been initialized,
      * usually in the early startup phase of the program.
      * 
      * @return True if logging is safe to use.
      */
     public static boolean isViable() {
         return serviceLogger != null;
     }
 
 
     /**
      * Attempt to make any parent directories required by the file to
      * be used for logging.  On failure, quietly give up.
      */
     private static void makeLogDir() {
         try {
             String loggingPropertiesFile = System.getProperty("java.util.logging.config.file");
             File file = new File(loggingPropertiesFile);
             if (file.canRead()) {
                 Properties loggingProperties = new Properties();
                 loggingProperties.load(new FileInputStream(file));
                 File logFile = new File(loggingProperties.getProperty("edu.umro.util.LogFileHandler.pattern"));
                 File parent = (logFile.getParentFile() == null) ? new File(".") : logFile.getParentFile();
                 System.out.println("Using log directory: " + parent.getAbsolutePath());
                 parent.mkdirs();
             }
             else {
                 System.out.println("Unable to read logging properties file: " + file.getAbsolutePath() + "  proceeding without logging.");
             }
         }
         catch (Exception e) {
             ;
         }
     }
 
 
     /**
      * Initialize logging by setting up all loggers to use the same
      * handler. 
      * 
      */
     public static void initLogging() {
         if (serviceLogger == null) {
             try {
                 makeLogDir();
                 fileHandler = new LogFileHandler();
                 serviceLogger = Logger.getLogger(Log.class.getName());
                 serviceFormatter = new LogFormatter();
                 fileHandler.setFormatter(serviceFormatter);
             }
             catch (SecurityException ex) {
                 System.err.println("Security - Failed to set up logging to file: " + ex);
                 ex.printStackTrace();
             }
             catch (IOException ex) {
                 System.err.println("IO - Failed to set up logging to file: " + ex);
                 ex.printStackTrace();
             }
         }
     }
 
 }
