 /*
  * 
  */
 
 package simpleWebServer;
  
 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 import simpleWebServer.Config;
 import simpleWebServer.Logger;
 import simpleWebServer.WorkerPool;
 
 
 class Runner {
     public static void main(String[] args) throws Exception {
         int port = 8080;
         if (args.length > 0) {
             port = Integer.parseInt(args[0]);
         }
 
         ConfigDefaults defaults = new ConfigDefaults();
         Logger logger = new SimpleLogger();
         Config config = new Config(defaults, logger);
         config.load();
         config.list();
 
         WorkerPool pool = new WorkerPool(config);
         pool.init();
 
         WebServer webServer = new WebServer(pool, config);
         webServer.start();
     }
 }
 
