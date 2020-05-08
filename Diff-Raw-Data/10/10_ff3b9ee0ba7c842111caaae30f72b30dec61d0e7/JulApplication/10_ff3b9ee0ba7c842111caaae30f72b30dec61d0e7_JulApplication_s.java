 package net.sf.ircappender.app;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 
 
 /**
  * a simple test application
  *
  * @author hendrik
  *
  */
 public class JulApplication {
 	/**
 	 * initializes jul
 	 *
	 * @throws IOException
 	 */
 	public static void init() throws IOException {
 		InputStream is = JulApplication.class.getResourceAsStream("jul.properties");
		LogManager.getLogManager().readConfiguration(is);
		is.close();
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
 		Logger logger = Logger.getLogger(JulApplication.class.getName());
 
 		// log errors and warnings
 		logger.severe("serve");
 		Thread.sleep(10000);
 
 		// log info messages
 		logger.info("info");
 
 		// Note: min level is set to "info" in jul.properties,
 		//       so there should not be any debug output in the channel
 		logger.fine("fine");
 		logger.finer("finer");
 		logger.finest("finest");
 		Thread.sleep(1000);
 	}
 }
