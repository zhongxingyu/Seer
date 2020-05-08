 package com.payneteasy.mysql.scheduler.util;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.TimeUnit;
 
 public class ExecutorServiceUtils {
 
     private static final Logger LOG = LoggerFactory.getLogger(ExecutorServiceUtils.class);
 
     public static void shutdownAndAwaitTermination(ExecutorService aExecutor, String aExecutorName) throws InterruptedException {
         LOG.info("{}: Stopping executor ...", aExecutorName);
 
         aExecutor.shutdown();

        for(int i=0; i<10 && !aExecutor.isTerminated(); i++) {
            LOG.info("{}:    Waiting threads to be done ...", aExecutorName);
            aExecutor.awaitTermination(10, TimeUnit.SECONDS);
         }
 
         if(!aExecutor.isTerminated()) {
             LOG.error("{}: Cannot stop executor", aExecutorName);
         }
         LOG.info("{}: Executor stopped", aExecutorName);
     }
 
     public static TaskExecutor createTaskExecutor(int aThreads) {
         return new TaskExecutor(aThreads);
     }
 }
