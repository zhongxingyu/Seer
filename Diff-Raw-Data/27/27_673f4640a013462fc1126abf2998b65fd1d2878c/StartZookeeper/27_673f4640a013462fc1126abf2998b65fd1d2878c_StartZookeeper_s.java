 package com.reversemind.glia.zookeeper;
 
 /**
  *
  */
 public class StartZookeeper {
 
     public static void start(){
         String port = "2181";
         String dataDirectory = System.getProperty("java.io.tmpdir");
 
         LocalTestZookeeper.start(new String[]{port, dataDirectory});
        System.out.println(dataDirectory);
     }
 
     public static void stop(){
         LocalTestZookeeper.stop();
     }
 
     public static void main(String... args) throws InterruptedException {
 
         String port = "2181";
         String dataDirectory = System.getProperty("java.io.tmpdir");
 
         LocalTestZookeeper.start(new String[]{port, dataDirectory});
        System.out.println(dataDirectory);
 
         Thread.sleep(60000);
         LocalTestZookeeper.stop();
 
     }
 
 }
