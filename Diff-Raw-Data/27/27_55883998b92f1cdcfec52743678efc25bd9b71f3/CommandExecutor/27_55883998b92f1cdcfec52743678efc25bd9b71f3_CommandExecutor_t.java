 /**
  * CommandExecutor
  *
 * Implements the main method to instantiate workers, using configuration
 * parameters.
  *
  * @author Ariel Gerardo Rios <mailto:ariel.gerardo.rios@gmail.com>
  *
  */
 
 package org.gabrielle.AsyncProcessingExample.command;
 
 import java.io.InputStream;
 import java.io.IOException;
 
 import java.util.Properties;
 import java.util.UUID;
 
 import org.apache.commons.lang.exception.ExceptionUtils;
 
 import org.apache.log4j.Logger;
 
 import org.gabrielle.AsyncProcessingExample.enums.Status;
 
 import org.gabrielle.AsyncProcessingExample.util.PIDFile;
 
 /** 
 * Implements the main method to instantiate workers, using configuration
 * parameters. It also is responsible for create and remove the PID file and
 * register the signals to catch, if some.
  *
  * @author Ariel Gerardo Ríos <mailto:ariel.gerardo.rios@gmail.com>
 */
 public class CommandExecutor {
 
     public static final String NTHREADS_KEY = "threads.count";
 
     public static final String PIDFILE_PATH_KEY = "pidfile.path";
 
     // This path must be included in classpath
     public static final String PROPERTIES_FILE_PATH =
         "configuration.properties";
 
     private static final Logger exceptionLogger = Logger.getLogger("exception");
 
     private static final Logger logger = Logger.getLogger("command-executor");
 
     private Properties config = new Properties();
 
     private PIDFile pidfile = new PIDFile();
     
 
     /** 
      * Locks the PID file if avialable.
      *
      * @param 
      * @return 
      * @throws 
     */
     public boolean lockPIDFile(final UUID uuid, final String path) throws
         IOException {
 
         logger.info(String.format("UUID=%s - Locking PID file %s ...", uuid,
                     path));
         this.pidfile.setPath(path);
         boolean result = this.pidfile.lock();
         logger.info(String.format("UUID=%s - Locking PID file %s result: %s",
                     uuid, path, result));
         return result;
     }
 
 
     /** 
      * Removes the PID file.
      *
      * @param 
      * @return 
      * @throws 
     */
     public boolean unlockPIDFile(final UUID uuid) {
 
         boolean result;
         logger.info(String.format("UUID=%s - Unlocking PID file ...", uuid));
         try {
             result = this.pidfile.unlock();
         }
         catch(Exception e){
             logger.error(String.format("UUID=%s - There was an exception " +
                         "unlocking the PID file. See exception log for more " +
                         "details.", uuid));
             exceptionLogger.error(String.format("UUID=%s - There was an " +
                             "exception unlocking the PID file: %s", uuid,
                             ExceptionUtils.getStackTrace(e)));
             return false;
         }
 
         logger.info(String.format("UUID=%s - Unlocking PID file result: %s",
                     uuid, result));
         return result;
     }
 
 
     /** 
      * Loads the configuration stored in a properties file using a
      * Properties object.
      *
      * @param path The path as String of Properties file.
     */
     public void loadProperties(final UUID uuid, final String path) throws
         IOException {
 
         logger.debug(String.format("UUID=%s - Loading properties " +
                     "configuration from: '%s' ...", uuid, path));
 
         InputStream inputStream = this.getClass().getClassLoader().
             getResourceAsStream(path); 
         this.config.load(inputStream);
         
         logger.debug(String.format("UUID=%s - Loading properties " +
                     "configuration from: '%s': SUCCESS.", uuid, path));
     }
 
 
     /** 
      * Executes all tasks related to the queue consumption.
      *
     */
     public int process () {
 
         UUID uuid = UUID.randomUUID();
         AsyncWorkerCreator awk = new AsyncWorkerCreator();
         
         logger.info(String.format("UUID=%s - *** Async Processing Example " +
                     "started ***", uuid));
                                                                                   
         try {
 
             this.loadProperties(uuid, PROPERTIES_FILE_PATH);
                                                                                   
             String path = this.config.getProperty(PIDFILE_PATH_KEY);
                                                                                   
             if (!this.lockPIDFile(uuid, path)) {
                 logger.error(String.format("UUID=%s - PID file exists " +
                             "(process already running?). Exiting.", uuid,
                             path));
                 exceptionLogger.error(String.format("UUID=%s - PID file " +
                             "exists (process already running?). Exiting.",
                             uuid, path));
                 logger.error(String.format("UUID=%s - *** Async Processing " +
                             "Example finished: FAILED ***", uuid));
                 return Status.FAILED.getId();
             }
                                                                                   
             int nthreads = Integer.parseInt(this.config.getProperty(
                         NTHREADS_KEY));
             logger.info(String.format("UUID=%s - Will use %d workers.", uuid,
                         nthreads));
             awk.createWorkers(uuid, nthreads, this.config);
                                                                                   
             awk.startWorkers(uuid);
             
             logger.info(String.format("UUID=%s - Waiting for workers to " +
                         "finish ...", uuid));
             awk.joinWorkers(uuid);
             logger.info(String.format("UUID=%s - Done.", uuid));
                                                                                   
             logger.info(String.format("UUID=%s - *** Async Processing Example" +
                         " finished: SUCCESSFUL ***", uuid));
             this.unlockPIDFile(uuid);
             return Status.SUCCESSFUL.getId();
                                                                                   
         }
         catch(Exception e) {
             logger.error(String.format("UUID=%s - There was an " +
                         "exception processing the queue. See exception log " +
                         "for more details.", uuid));            
             logger.error(String.format("UUID=%s - *** Cancel Queue " +
                         "Processor finished: FAILED ***", uuid));
                                                                                   
             exceptionLogger.error(String.format("UUID=%s - There was an " +
                           "exception processing the queue: %s", uuid,
                           ExceptionUtils.getStackTrace(e)));
         }
         this.unlockPIDFile(uuid);
         return Status.FAILED.getId();
     }


    // TODO Register the signals to catch.
                                                                                   
                                                                                   
     /** 
      * Main method.
      *
      * @param args The argument list as String[]
      * @return An int value as result status.
     */
     public static void main (String [] args) {
                                                                                   
         CommandExecutor ce = new CommandExecutor();
         System.exit(ce.process());
     }
 }
 
 
 // vim:ft=java:
