 package com.vreco.boomerang;
 
import com.vreco.util.shutdownhooks.SimpleShutdown;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import javax.jms.JMSException;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 /**
  *
  * @author baldrich
  */
 public final class Main {
 
   private Logger logger;
 
   public Main() {
   }
 
   protected void start() throws JMSException {
     SimpleShutdown shutdown = SimpleShutdown.getInstance();
 
     Properties conf = null;
 
     try {
       Runtime.getRuntime().addShutdownHook(shutdown);
       conf = getConf("/usr/local/boomerang/conf/boomerang.conf");
       logger = getLog4J(conf);
 
       logger.info("starting Threads.");
       //start threads here
     } catch (Throwable e) {
       shutdown.setShutdown(true);
       System.out.print(e);
       System.out.println("Caught unexpected exception, exiting");
       if (logger != null) {
         logger.fatal("Caught unexpected exception, exiting.", e);
       } else {
         System.out.println("Caught unexpected exception, exiting.");
         System.out.print(e.getStackTrace().toString());
       }
     }
     while (!shutdown.isShutdown()) {
       try {
         Thread.sleep(200);
       } catch (InterruptedException e) {
         logger.warn("Interrupted sleep", e);
       }
     }
 
    gracefulShutdown(shutdown, conf);
   }
 
   /**
    * Gracefully shutdown our threads.
    *
    * @param shutdown
    */
   protected void gracefulShutdown(final SimpleShutdown shutdown, Properties conf) {
     shutdown.setShutdown(true);
     if (conf == null) {
       conf = new Properties();
       conf.setProperty("loader.join.wait", "60000");
     }
     logger.info("starting graceful shutdown...");
     long joinTime = Long.parseLong(conf.getProperty("loader.join.wait", "30000"));
     try {
       //join threads here
     } catch (Exception e) {
       logger.error("failed to join thread", e);
     }
     shutdown.setFinished(true);
   }
 
   /**
    * Start threads.
    *
    * @param threads
    */
   public static void startThreads(HashMap<Integer, Thread> threads) {
     for (Map.Entry<Integer, Thread> map : threads.entrySet()) {
       map.getValue().start();
     }
   }
 
   /**
    * Join threads.
    *
    * @param threads
    */
   public static void joinThreads(HashMap<Integer, Thread> threads, long millis) throws InterruptedException {
     for (Map.Entry<Integer, Thread> map : threads.entrySet()) {
       map.getValue().join(millis);
     }
   }
 
   /**
    * Get our properties file.
    *
    * @param filePath
    * @return
    * @throws IOException
    */
   protected Properties getConf(final String filePath) throws IOException {
     Properties config = new Properties();
     InputStream stream = null;
     try {
       stream = new FileInputStream(
               filePath);
 
       config.load(stream);
     } catch (Exception e) {
       throw new IOException("Failed to read config file", e);
     } finally {
       if (stream != null) {
         stream.close();
       }
     }
     return config;
 
   }
 
   /**
    * Load log4j config.
    *
    * @throws ComplianceException
    */
   protected Logger getLog4J(final Properties conf) throws IOException {
     File log4jFile = new File(conf.getProperty("log4j.conf", "conf/log4j.conf"));
     if (!log4jFile.exists()) {
       throw new IOException("No log4j conf file found: " + log4jFile.toString());
     }
     PropertyConfigurator.configureAndWatch(log4jFile.getAbsolutePath());
     return logger = Logger.getLogger(Main.class);
   }
 
   public static void main(final String args[]) throws JMSException {
     new Main().start();
   }
 }
