 /**
  * Worker
  *
  * A simple implementation for an async Worker.
  *
  * @author Ariel Gerardo Rios <mailto:ariel.gerardo.rios@gmail.com>
  *
  */
 
 
 package org.gabrielle.AsyncProcessingExample.worker;
 
 import java.util.Properties;
 import java.util.UUID;
 
 import org.apache.commons.lang.exception.ExceptionUtils;
 
 import org.apache.log4j.Logger;
 
 /** 
  * Common implementation for worker classes
  *
  * @author Ariel Gerardo Ríos <mailto:ariel.gerardo.rios@gmail.com>
 */
 public class Worker implements Runnable {
 
     public static final String WORKER_NAME_TEMPLATE = "Worker#%d";
 
     public static final String THREAD_SLEEP_KEY = "threads.sleep";
     
     private static final Logger exceptionLogger = Logger.getLogger(
             "exception");
 
     private static final Logger logger = Logger.getLogger("worker");
 
     private Properties config;
 
     private String name;
 
     private boolean executing = false;
 
     /**
      * Sets the config for this instance.
      *
      * @param config The config.
      */
     public void setConfig(Properties config) {
         this.config = config;
     }
 
 
     /** 
      * Returns the worker name.
      *
      * @return The name of the worker, as String.
     */
     public String getName() { 
         return new String(this.name);
     }
 
 
     /** 
      * Returns the value for 'executing' field.
      *
      * @return The executing value, as boolean.
     */
     public boolean isExecuting() {
         return this.executing;
     }
 
 
     /** 
      * Stops the execution loop.
      *
     */
     public void stopExecution() {
         this.executing = false;
     }
 
 
     /** 
      * Constructor.
      *
      * @param id The id number for this worker instance.
     */
     public Worker(final int id) {
         this.name = String.format(WORKER_NAME_TEMPLATE, id);
     }
                                                                        
                                                                        
     /** 
      * Constructor.
      *
      * @param id The id number for this worker instance.
      * @param config The Properties configuration object.
     */
     public Worker(final int id, final Properties config) {
         this.name = String.format(WORKER_NAME_TEMPLATE, id);
         this.config = config;
     }
 
 
     /** 
      * Executes the thread logic.
      *
     */
     public void run() {
         UUID uuid = UUID.randomUUID();
         logger.info(String.format("UUID=%s - %s initialized.", uuid,
                     this.name));
         this.executing = true;
                                                                                      
         try {
             int secondsSleep = Integer.parseInt(this.config.getProperty(
                         THREAD_SLEEP_KEY));
                                                                                      
             while (this.executing) {
                 logger.info(String.format("UUID=%s - Sleeping for %d seconds " +
                             "...", uuid, secondsSleep));
                 // TODO This should process a taks instead of sleep.
                Thread.sleep(secondsSleep);
                 logger.info(String.format("UUID=%s - Sleeping for %d seconds: " +
                             "DONE.", uuid, secondsSleep));
             }
         }
         catch (Exception e) {
             logger.error(String.format("UUID=%s - There was an exception " +
                         "processing a task. See exception logger for more " +
                         "details", uuid));
             exceptionLogger.error(String.format("UUID=%s - There was an " +
                         "exception processing a task: %s", uuid,
                         ExceptionUtils.getStackTrace(e)));
         }
         finally {
             this.executing = false;
         }
                                                                                      
         logger.info(String.format("UUID=%s - %s finished.", uuid, this.name));
     }
 }
 
 // vim:ft=java:
