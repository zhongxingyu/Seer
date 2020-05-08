 package com.payneteasy.mysql.scheduler.util;
 
import com.payneteasy.mysql.scheduler.SchedulerConfig;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.TimeUnit;
 
 public class ExecutorServiceUtils {
 
     private static final Logger LOG = LoggerFactory.getLogger(ExecutorServiceUtils.class);
 
     public static void shutdownAndAwaitTermination(ExecutorService aExecutor, String aExecutorName) throws InterruptedException {
         LOG.info("{}: Stopping executor ...", aExecutorName);
 
         aExecutor.shutdown();
        final int WAIT_COUNT = SchedulerConfig.getIntConfig(SchedulerConfig.Config.WAIT_SHUTDOWN_SECONDS);
        final int INCREMENT = 10;
        for(int i=0; i<WAIT_COUNT && !aExecutor.isTerminated(); i+=INCREMENT) {
            LOG.info("{}:    Waiting threads to be done {}/{}...", new Object[] {aExecutorName, i, WAIT_COUNT});
            boolean terminated = aExecutor.awaitTermination(INCREMENT, TimeUnit.SECONDS);
            if(terminated) break;
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
