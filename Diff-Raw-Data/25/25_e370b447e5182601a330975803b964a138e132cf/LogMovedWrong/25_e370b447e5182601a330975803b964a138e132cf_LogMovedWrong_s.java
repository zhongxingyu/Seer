 package com.sleaker.logmovedwrong;
 
 import java.io.File;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.logging.Handler;
 import java.util.logging.LogRecord;
 import java.util.logging.Logger;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class LogMovedWrong extends JavaPlugin {
 
 	private String plugName;
 	private static Logger log = Logger.getLogger("Minecraft");
 	private Queue<String> logQueue = new ConcurrentLinkedQueue<String>();
 	LogThread logThread = new LogThread(logQueue);
 	MoveWrongLogHandler logHandler;
 
 	@Override
 	public void onDisable() {	    
 		//Shutdown our logging thread
 		try {
 			logThread.writeQueue();
 			logThread.join();
 		} catch (InterruptedException e) {
 			//don't care if we can't stop
 		}
 
 		//Cleanup our handler
 		log.removeHandler(logHandler);
 		logHandler.close();
 
 		log.info(plugName + " disabled!");
 	}
 
 	@Override
 	public void onEnable() {
 		plugName = "[" + this.getDescription().getName() + "]";
 
		File baseDir = new File("log-archive/");
 		if (!baseDir.exists()) {
 			log.info(plugName + " - Logging directory not found, creating at <server_dir>/log-archive");
 			baseDir.mkdir();
 		}
 		//Register our log handler to intercept the moved wrongly messages.
 		logHandler = new MoveWrongLogHandler();
 		log.addHandler(logHandler);
 
 		logThread.start();
 		log.info(plugName + " by Sleaker v" + this.getDescription().getVersion() + " enabled!");
 	}	
 
 	private class MoveWrongLogHandler extends Handler {
 
 		@Override
 		public void publish(LogRecord record) {
 			String testString = record.getMessage();
 			if (testString.contains("moved wrongly") || testString.contains("Got position") || testString.contains("Expected"))
 				logQueue.add(getDateTime() + "  " + testString);
 
 		}
 
 		@Override
 		public void flush() {
 
 		}
 
 		@Override
 		public void close() throws SecurityException {
 
 		}
 		
 		private String getDateTime() {
			DateFormat dateFormat = new SimpleDateFormat("[dd-MM-yy | hh-mm-ss ]");
 			return dateFormat.format(new Date());
 		}
 	}
 }
