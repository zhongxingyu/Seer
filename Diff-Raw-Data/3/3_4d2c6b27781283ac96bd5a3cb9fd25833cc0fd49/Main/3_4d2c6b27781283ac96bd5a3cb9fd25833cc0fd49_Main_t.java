 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.github.etsai.kfsxtrackingserver;
 
 import static com.github.etsai.kfsxtrackingserver.Common.*;
 import static com.github.etsai.kfsxtrackingserver.ServerProperties.*;
 import com.github.etsai.utils.logging.TeeLogger;
 import groovy.sql.Sql;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.sql.SQLException;
 import java.util.logging.*;
 
 /**
  * Main entry point for the tracking server
  * @author etsai
  */
 public class Main {
     private static ConsoleHandler logConsoleHandler;
     private static FileWriter logWriter;
     
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws ClassNotFoundException, SQLException {
         CommandLine clom= new CommandLine(args);
         
         try {
             properties= ServerProperties.load(clom.getPropertiesFilename());
         } catch (IOException ex) {
             logger.warning(ex.getMessage());
             logger.warning("Using default properties...");
             properties= ServerProperties.getDefaults();
         }
         
         initLogging();
         
         Thread udpTh= new Thread(new UDPListener(Integer.valueOf(properties.getProperty(propUdpPort))));
         Thread httpTh= new Thread(new HTTPListener(Integer.valueOf(properties.getProperty(propHttpPort))));
                 
         logger.log(Level.INFO,"Loading stats from databse: {0}", properties.getProperty(propDbName));
         Class.forName("org.sqlite.JDBC");
         Common.sql= Sql.newInstance(String.format("jdbc:sqlite:%s", properties.getProperty(propDbName)));
        Common.sql.execute("CREATE  TEMP  TABLE \"steaminfo\" (\"steamid64\" TEXT PRIMARY KEY  NOT NULL , \"name\" TEXT, \"avatar\" TEXT)");
         
         Runtime.getRuntime().addShutdownHook(new Thread() {
             @Override
             public void run() {
                Common.sql.close();
                 logger.info("Shutting down server");
             }
         });
         
         udpTh.start();
         httpTh.start();
     }
     
     public static void initLogging() {
         try {
             logWriter= TeeLogger.getFileWriter("kfsxtracking");
             oldStdOut= System.out;
             oldStdErr= System.err;
             System.setOut(new PrintStream(new TeeLogger(logWriter, oldStdOut), true));
             System.setErr(new PrintStream(new TeeLogger(logWriter, oldStdErr), true));
             
             Level logLevel= Level.parse(properties.getProperty(propLogLevel));
             for(Handler handler: logger.getHandlers()) {
                 logger.removeHandler(handler);
             }
             logConsoleHandler= new ConsoleHandler();
             logConsoleHandler.setLevel(logLevel);
             logger.setLevel(Level.ALL);
             logger.addHandler(logConsoleHandler);
             logger.setUseParentHandlers(false);   
         } catch (IOException ex) {
             logger.log(Level.WARNING, "Output will not be saved to file...", ex);
         }
 
         
     }
 }
