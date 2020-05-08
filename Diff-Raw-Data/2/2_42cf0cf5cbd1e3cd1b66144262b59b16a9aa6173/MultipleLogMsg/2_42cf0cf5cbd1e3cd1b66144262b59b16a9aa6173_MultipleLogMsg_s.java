 package com.github.manasg.logging.test;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 public class MultipleLogMsg {
 
     public static long multipleLogs(int number) {
         Logger logger = Logger.getLogger("multi");
         PropertyConfigurator.configure("conf/log4j.properties");
         
         /*
          * Just doing this via Code. Log4.properties has the same effect!
          
        EasilydoAppender a4 = new EasilydoAppender();
         a4.setEnvironment("load-test");
         a4.setDestination("192.168.100.8:2464");
         logger.addAppender(a4);
         logger.setAdditivity(false);
         */
         long start = System.currentTimeMillis();
         
         for(int i=0;i<number;i++) {
             logger.fatal("Oh boy!");
          }
         
         long end = System.currentTimeMillis();
         
         return end-start;
     }
     
     public static void main(String[] args) {
         int n = 30;
         long time = multipleLogs(n);
         System.out.println("It took "+ time/1000.0 +" seconds - for "+n+" messages");
      
     }
 
 }
