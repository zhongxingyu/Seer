 package com.piotrnowicki.btrace;
 
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.piotrnowicki.btrace.DataAccessor;
 
 public class Executor {
 
     private Logger log = Logger.getLogger(Executor.class.getName());
 
    public static void main(String[] args) {
         new Executor().execute();
     }
 
     void execute() {
         DataAccessor dataAccessor = new DataAccessor();
 
         for (int i = 0; i < 1000; i++) {
             dataAccessor.getData(generateKey());
             sleep();
         }
     }
 
     String generateKey() {
         return new Random().nextInt(100) + "";
     }
 
     long getSleepTime() {
         return new Double(Math.random() * 1000).longValue();
     }
 
     void sleep() {
         long sleepTime = getSleepTime();
 
         try {
             log.log(Level.INFO, "Going to sleep for {0} milliseconds",
                     sleepTime);
             Thread.sleep(sleepTime);
         } catch (InterruptedException e) {
             // no-op for exemplary purpose
         }
     }
 }
