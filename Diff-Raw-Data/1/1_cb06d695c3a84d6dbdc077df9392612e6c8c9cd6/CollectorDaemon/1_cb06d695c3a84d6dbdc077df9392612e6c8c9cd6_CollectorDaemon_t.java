 package com.milo.collector;
 
 import com.milo.protocols.thrift.TMiloProtocol;
 import com.milo.thrift.MiloCollector;
 import com.milo.collector.handler.CollectorHandler;
 import com.milo.config.CollectorConfiguration;
 import com.milo.config.Config;
 import com.milo.config.ConfigurationException;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 import org.apache.thrift.TProcessorFactory;
 import org.apache.thrift.server.TThreadPoolServer;
 import org.apache.thrift.protocol.TProtocolFactory;
 import org.apache.thrift.transport.TFramedTransport;
 import org.apache.thrift.transport.TServerSocket;
 import org.apache.thrift.transport.TTransportException;
 import org.apache.thrift.transport.TTransportFactory;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.UnknownHostException;
 import java.util.Properties;
 import java.util.concurrent.TimeUnit;
 
 
 public class CollectorDaemon {
     // vm properties
     private static final String PROP_FOREGROUND = "fg";
 
     // "config" constants
     private static Logger logger = Logger.getLogger(CollectorDaemon.class);
     private TThreadPoolServer poolServer;
     private Config config = CollectorConfiguration.factory();
 
     public static void main(String[] args) throws Exception {
         if (args.length > 0) {
             help();
             System.exit(0);
         }
 
         Properties props = System.getProperties();
 
         // log4j
         String configPath = System.getProperty("config");
         if (configPath == null) {
             help();
             Exception cex = new Exception("No config directory specified.");
             logger.fatal(cex);
             throw cex;
         }
 
         String file = configPath + File.separator + "log4j.properties";
         PropertyConfigurator.configure(file);
        PropertyConfigurator.configure(configPath + File.separator + "collector.yaml");
 
         try {
             CollectorDaemon collector = new CollectorDaemon();
 
             if (System.getProperty(PROP_FOREGROUND) == null) {
                 logger.info("Background mode");
                 System.out.close();
                 System.err.close();
             }
 
             collector.start();
         } catch (Throwable e) {
             Exception cex = new Exception("Exception encountered during startup.", e);
             logger.fatal(cex);
             throw cex;
         }
     }
 
     private static void help() {
         System.out.println("Options:\n");
         System.out.println("\t-Dconfig    \tPath to directory that contains log4j.properties (required)");
         System.out.println("\t-Dfg        \tRun in the foreground (optional)");
     }
 
     private CollectorDaemon() throws Exception {
         try {
             setup();
         } catch (Exception e) {
             Exception cex = new Exception("Error during setup", e);
             logger.fatal(cex);
             throw cex;
         }
     }
 
     private void setup() throws IOException, TTransportException, ConfigurationException {
         int listenPort = config.collector_port;
         InetAddress listenAddr = InetAddress.getByName("0.0.0.0");
 
         Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
             public void uncaughtException(Thread t, Throwable e) {
                 if (e instanceof OutOfMemoryError) {
                     logger.fatal("Fatal exception in thread " + t + ". Shutting down.", e);
                     System.exit(100);
                 } else {
                     logger.error("Uncaught exception in thread. " + t, e);
                 }
             }
         });
 
         CollectorHandler collectorHandler = new CollectorHandler();
         MiloCollector.Processor processor = new MiloCollector.Processor(collectorHandler);
         TServerSocket tServerSocket = new TServerSocket(new InetSocketAddress(listenAddr, listenPort));
         logger.info(String.format("Binding thrift handler to %s:%s", listenAddr, listenPort));
 
         // Protocol factory
         TProtocolFactory tProtocolFactory = new TMiloProtocol.Factory();
 
         // Transport factory
         TTransportFactory inTransportFactory, outTransportFactory;
         inTransportFactory = new TFramedTransport.Factory();
         outTransportFactory = new TFramedTransport.Factory();
 
         // ThreadPool Server & related options
         TThreadPoolServer.Options options = new TThreadPoolServer.Options();
         options.minWorkerThreads = config.collector_min_workers;
         options.stopTimeoutVal = 1;
         options.stopTimeoutUnit = TimeUnit.SECONDS;
         poolServer = new TThreadPoolServer(new TProcessorFactory(processor),
                 tServerSocket,
                 inTransportFactory,
                 outTransportFactory,
                 tProtocolFactory,
                 tProtocolFactory,
                 options);
     }
 
     public void start() {
         logger.info("Starting Milo Collector");
         poolServer.serve();
     }
 
     public void stop() {
         logger.info("Stopping Milo Collector");
         poolServer.stop();
     }
 }
