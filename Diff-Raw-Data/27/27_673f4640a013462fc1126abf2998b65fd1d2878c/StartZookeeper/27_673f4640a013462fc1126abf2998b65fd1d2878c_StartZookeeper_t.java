 package com.reversemind.glia.zookeeper;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 /**
  *
  */
 public class StartZookeeper {
 
    private static final Logger LOG = LoggerFactory.getLogger(StartZookeeper.class);

     public static void start(){
         String port = "2181";
         String dataDirectory = System.getProperty("java.io.tmpdir");
 
         LocalTestZookeeper.start(new String[]{port, dataDirectory});
        LOG.debug(dataDirectory);
     }
 
     public static void stop(){
         LocalTestZookeeper.stop();
     }
 
     public static void main(String... args) throws InterruptedException {
 
         String port = "2181";
         String dataDirectory = System.getProperty("java.io.tmpdir");
 
         LocalTestZookeeper.start(new String[]{port, dataDirectory});
        LOG.debug(dataDirectory);
 
         Thread.sleep(60000);
         LocalTestZookeeper.stop();
 
     }
 
 }
