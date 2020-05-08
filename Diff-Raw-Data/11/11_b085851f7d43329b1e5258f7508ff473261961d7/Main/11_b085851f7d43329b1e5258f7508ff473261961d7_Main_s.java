 package com.voracious.dragons.server;
 
 import java.io.IOException;
 
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.FileAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 
 import com.voracious.dragons.common.Turn;
 
 public class Main {
 	public static final String logfile = "server.log";
 	
 	private static Logger logger = Logger.getLogger(Main.class);
 	private static DBHandler database = new DBHandler();
 	
     public static void main(String[] args){
     	initLog4J();
         logger.setLevel(Level.INFO);
         if (args.length > 0) {
            if (args[0] == "--debug") {
                 logger.setLevel(Level.ALL);
             }
         }
     	
     	logger.info("Server version " + Turn.versionString + " started");
     	database.init();
     	
     	new Thread(new ServerConnectionManager()).start();
     }
     
     public static void initLog4J() {
         logger = Logger.getLogger(Main.class);
 
         Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%-4r [%t] %-5p %c %x - %m%n")));
         try {
             Logger.getRootLogger().addAppender(new FileAppender(new PatternLayout("%-4r [%t] %-5p %c %x - %m%n"), logfile));
         } catch (IOException e) {
             logger.error("Could not write log file", e);
         }
     }
 }
