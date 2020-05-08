 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.github.etsai.kfsxtrackingserver;
 
 import com.github.etsai.utils.logging.TeeLogger;
 import com.github.etsai.utils.sql.ConnectionPool;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.sql.SQLException;
 import java.util.concurrent.Executors;
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
                 } catch (SQLException ex) {
                     Common.logger.log(Level.SEVERE, "Error shutting down connections", ex);
                 }
                 Common.logger.info("Shutting down server");
             }
         });
         
         Common.pool.submit(new UDPListener(props.getUdpPort()));
         Common.pool.submit(new HTTPListener(props.getHttpPort()));
         Common.pool.submit(new SteamPoller(Common.connPool.getConnection(), props.getSteamPollingThreads()));
     }
     
     public static void initModules(ServerProperties props) throws ClassNotFoundException, SQLException {
         Common.logger.log(Level.INFO,"Loading stats from database: {0}", props.getDbName());
         
         Class.forName("org.sqlite.JDBC");
        Common.connPool= new ConnectionPool(props.getNumDbConn());
         Common.connPool.setJdbcUrl(String.format("jdbc:sqlite:%s", props.getDbName()));
         
         Accumulator.writer= new DataWriter(Common.connPool.getConnection());
         Accumulator.statMsgTTL= props.getStatsMsgTTL();
         Accumulator.packetParser= new PacketParser(props.getPassword());
         HTTPListener.httpRootDir= props.getHttpRootDir();
         
         if (props.getNumThreads() < 0) {
             Common.pool= Executors.newCachedThreadPool();
         } else {
             Common.pool= Executors.newFixedThreadPool(props.getNumThreads());
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
         } catch (IOException ex) {
             Common.logger.log(Level.WARNING, "Output will not be saved to file...", ex);
         }
 
         
     }
 }
