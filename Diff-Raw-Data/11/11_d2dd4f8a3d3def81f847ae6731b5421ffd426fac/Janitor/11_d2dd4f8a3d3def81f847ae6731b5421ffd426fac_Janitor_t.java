 package polly.core;
 
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.log4j.Logger;
 
 import polly.moduleloader.AbstractModule;
 import polly.moduleloader.ModuleLoader;
 import polly.moduleloader.SetupException;
 import polly.moduleloader.annotations.Module;
 import polly.util.concurrent.ThreadFactoryBuilder;
 
 @Module
 public class Janitor extends AbstractModule {
 
     private final static int RATE = 1000 * 60 * 10; // 10 minutes
     private final static Logger logger = Logger.getLogger(Janitor.class.getName());
     
     public Janitor(ModuleLoader loader) {
         super("JANITOR", loader, false);
     }
     
 
     @Override
     public void setup() throws SetupException {
         Executors.newScheduledThreadPool(1, 
            new ThreadFactoryBuilder("JANITOR").setPriority(Thread.MIN_PRIORITY))
                 .scheduleAtFixedRate(new Runnable() {
             
             @Override
             public void run() {
                 Runtime r = Runtime.getRuntime();
                 
                 logger.trace("Janitoring");
                 logger.trace("Total memory: " + r.totalMemory());
                 logger.trace("Free memory before: " + r.freeMemory());
                 System.gc();
                 logger.trace("Free memory after: " + r.freeMemory());
             }
         }, RATE, RATE, TimeUnit.MILLISECONDS);
     }
 }
