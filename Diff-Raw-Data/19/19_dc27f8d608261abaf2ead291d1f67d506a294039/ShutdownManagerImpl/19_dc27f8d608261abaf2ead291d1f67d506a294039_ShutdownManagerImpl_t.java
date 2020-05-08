 package polly.core;
 
import java.io.File;
 import java.io.IOException;
 import java.lang.management.ManagementFactory;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import polly.Polly;
 
 
 import de.skuzzle.polly.sdk.CompositeDisposable;
 import de.skuzzle.polly.sdk.Disposable;
 import de.skuzzle.polly.sdk.ShutdownManager;
 import de.skuzzle.polly.sdk.exceptions.DisposingException;
 
 
 
 /**
  * 
  * @author Simon
  * @version 27.07.2011 ae73250
  * @since Beta 0.5
  */
 public class ShutdownManagerImpl implements ShutdownManager {
 
     private static Logger logger = Logger.getLogger(ShutdownManagerImpl.class.getName());
 
     
     /** 
      * Sun property pointing the main class and its arguments. 
      * Might not be defined on non Hotspot VM implementations.
      */
     public static final String SUN_JAVA_COMMAND = "sun.java.command";
     
     
     
     private static ShutdownManagerImpl instance;
     
     public static synchronized ShutdownManagerImpl get() {
         if (instance == null) {
             instance = new ShutdownManagerImpl();
         }
         return instance;
     }
     
     
 
     private CompositeDisposable shutdownList;
     
     private ShutdownManagerImpl() {
         this.shutdownList = new CompositeDisposable();
     }
     
     
     
     public CompositeDisposable getShutdownList() {
         return this.shutdownList;
     }
     
     
     
     public synchronized void addDisposable(Disposable d) {
         this.shutdownList.addLast(d);
     }
     
     
     
     @Override
     public void restart() {
         this.restart(Polly.getCommandLine());
     }
     
     
     /*
      * Implementation is adapted from 
      * http://java.dzone.com/articles/programmatically-restart-java
      */
     
     @Override
     public synchronized void restart(String commandLine) {
         // properly shutdown all loaded components
         this.shutdown(false);
         logger.info("Preparing to restart...");
         logger.debug("Commandline arguments: " + commandLine);
         
         try {
             String s = System.getProperty("file.separator");
             // java binary
            final String java = System.getProperty("java.home") + s +"bin" + s + "java";
             logger.trace("Java Home resolved to: " + java);
             // vm arguments
             List<String> vmArguments = 
                 ManagementFactory.getRuntimeMXBean().getInputArguments();
             StringBuffer vmArgsOneLine = new StringBuffer();
             for (String arg : vmArguments) {
                 // if it's the agent argument : we ignore it otherwise the
                 // address of the old application and the new one will be in conflict
                 if (!arg.contains("-agentlib")) {
                     vmArgsOneLine.append(arg);
                     vmArgsOneLine.append(" ");
                 }
             }
             // init the command to execute, add the vm args
            final StringBuffer cmd = new StringBuffer(java + " " + vmArgsOneLine);
 
            cmd.append(" -jar polly.jar");
 
            if (commandLine != null && !commandLine.equals("")) {
                cmd.append(" ");
                cmd.append(commandLine);
            }
            
             logger.trace("Java command: '" + cmd.toString() + "'");
             // execute the command in a shutdown hook, to be sure that all the
             // resources have been disposed before restarting the application
             Runtime.getRuntime().addShutdownHook(new Thread() {
                 @Override
                 public void run() {
                     try {
                         logger.info("Starting new polly instance: " + cmd);
                         Runtime.getRuntime().exec(cmd.toString());
                         logger.info("New polly instance initialized.");
                     } catch (IOException e) {
                         logger.error("Could not start new polly instance.", e);
                     }
                 }
             });
             
         } catch (Exception e) {
             logger.error("Could not start new polly instance", e);
         } finally {
             // All resources have been unloaded, so perform shutdown
             logger.warn("Exiting current polly process.");
             System.exit(0);
         }
     }
     
     
     
     public synchronized void shutdown(boolean exit) {
         logger.info("Shutting down all components.");
         try {
             this.shutdownList.dispose();
         } catch (DisposingException e) {
             logger.error("One ore more components failed to properly unload. " +
             		"Last exception trace:", e);
         }
         
         logger.trace("Remaining active threads: " + Thread.activeCount());
         logger.trace("Remaining stacktraces:");
         this.printRemainingThreads();
         
         if (exit) {
             logger.info("All connections closed. Now exiting the whole program. ByeBye");
             System.exit(0);
         }
     }
     
     
     
     public void shutdown() {
         this.shutdown(true);
     }
     
     
     
     private void printRemainingThreads() {
         Thread[] active = new Thread[Thread.activeCount()];
         Thread.enumerate(active);
         for (Thread t : active) {
             logger.trace("Thread: " + t.toString());
             for (StackTraceElement e : t.getStackTrace()) {
                 logger.trace("    " + e.toString());
             }
         }
     }
 }
