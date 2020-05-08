 package net.sf.ircappender.app;
 
 import java.io.IOException;
 
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 /**
  * a simple test application
  *
  * @author hendrik
  *
  */
 public class TestApplication {
 	/**
 	 * initializes log4j with a custom properties file.
 	 *
 	 * @throws IOException 
 	 */
 	public static void init() throws IOException {
 		PropertyConfigurator.configure(TestApplication.class.getResource("log4j.properties"));
 	}
 
 	/**
 	 * Starts this simple test application
 	 *
 	 * @param args ignored
 	 * @throws IOException in case of an input/output error 
 	 * @throws InterruptedException in case the thread is interrupted
 	 */
 	public static void main(String[] args) throws IOException, InterruptedException {
 
 		// init the log system
 		init();
 		Logger logger = Logger.getLogger(TestApplication.class);
 
 		// log errors and warnings
 		logger.fatal("fatal");
 		logger.error("error");
 		logger.warn("warn");
 
 		// log info messages
 		logger.info("info");
 
 		// Note: min level is set to "info" in log4j.properties, 
 		//       so there should not be any debug output in the channel
 		logger.debug("debug");
 
 		// wait a little and shutdown the log system
 		Thread.sleep(5000);
 		LogManager.shutdown();
 	}
 
 }
