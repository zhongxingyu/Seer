 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.github.etsai.kfsxtrackingserver;
 
 import com.github.etsai.kfsxtrackingserver.impl.SQLiteWriter;
 import com.github.etsai.kfsxtrackingserver.web.WebHandler;
 import com.github.etsai.utils.logging.TeeLogger;
 import com.github.etsai.utils.sql.ConnectionPool;
 import fi.iki.elonen.NanoHTTPD;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.lang.reflect.InvocationTargetException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.logging.*;
 
 /**
  * Main entry point for the tracking server
  * @author etsai
  */
 public class Main {
     private static ConsoleHandler logConsoleHandler;
     private static FileWriter logWriter;
     private static NanoHTTPD webHandler;
     private static ExecutorService threadPool;
     
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws ClassNotFoundException, SQLException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
         CommandLine clom= new CommandLine(args);
         ServerProperties props;
         
         try {
             props= ServerProperties.load(clom.getPropertiesFilename());
         } catch (IOException ex) {
             Common.logger.warning(ex.getMessage());
             Common.logger.warning("Using default properties...");
             props= ServerProperties.getDefaults();
         }
         
         initLogging(props.getLogLevel());
         initModules(props);
         
         Runtime.getRuntime().addShutdownHook(new Thread() {
             @Override
             public void run() {
                 try {
                     Common.connPool.close();
                     if (webHandler != null) {
                         webHandler.stop();
                     }
                 } catch (SQLException ex) {
                     Common.logger.log(Level.SEVERE, "Error shutting down connections", ex);
                 }
                 Common.logger.info("Shutting down server");
             }
         });
         
         try {
             if (props.getHttpPort() > 0) {
                 webHandler= new WebHandler(props.getHttpPort(), props.getHttpRootDir());
                 webHandler.start();
             } else {
                 Common.logger.log(Level.CONFIG, "HTTP server disabled");
             }
         } catch (IOException ex) {
             Common.logger.log(Level.SEVERE, null, ex);
         }
        DataWriter writer= Common.dataWriterClass.getConstructor(new Class<?>[] {Connection.class}).newInstance(new Object[] {Common.connPool.getConnection()});
         threadPool= Executors.newCachedThreadPool();
         threadPool.submit(new UDPListener(props.getUdpPort(), 
                 new Accumulator(writer, props.getPassword(), props.getStatsMsgTTL())));
         threadPool.submit(new SteamPoller(Common.connPool.getConnection(), props.getSteamPollingThreads()));
     }
     
     public static void initModules(ServerProperties props) throws ClassNotFoundException, SQLException {
         Common.logger.log(Level.CONFIG,"Loading stats from database: {0}", props.getDbName());
         
         Class.forName("org.sqlite.JDBC");
         Common.connPool= new ConnectionPool(props.getNumDbConn());
         Common.connPool.setJdbcUrl(String.format("jdbc:sqlite:%s", props.getDbName()));
         try {
            URL[] urls= {new URL("jar:file:lib/DataConnection.jar!/")};
             URLClassLoader urlCl= new URLClassLoader(urls, Main.class.getClassLoader());
             
            Common.dataWriterClass= (Class<DataWriter>)Class.forName("SQLiteWriter", true, urlCl);
            Common.dataReaderClass= (Class<DataReader>)Class.forName("SQLiteReader", true, urlCl);
             
         } catch (MalformedURLException ex) {
             Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     public static void initLogging(Level logLevel) {
         try {
             logWriter= TeeLogger.getFileWriter("kfsxtracking", new File("log"));
             Common.oldStdOut= System.out;
             Common.oldStdErr= System.err;
             System.setOut(new PrintStream(new TeeLogger(logWriter, Common.oldStdOut), true));
             System.setErr(new PrintStream(new TeeLogger(logWriter, Common.oldStdErr), true));
             
             for(Handler handler: Common.logger.getHandlers()) {
                 Common.logger.removeHandler(handler);
             }
             logConsoleHandler= new ConsoleHandler();
             logConsoleHandler.setLevel(logLevel);
             Common.logger.setLevel(Level.ALL);
             Common.logger.addHandler(logConsoleHandler);
             Common.logger.setUseParentHandlers(false);
             
             NanoHTTPD.logger= Common.logger;
             NanoHTTPD.logLevel= logLevel;
         } catch (IOException ex) {
             Common.logger.log(Level.WARNING, "Output will not be saved to file...", ex);
         }
 
         
     }
 }
