 package harachu.mhr;
 
 //test by oosawak1975
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 
 import harachu.mhr.controller.GuiController;
 
 public class ManHourRecorder {
 
 	/**
 	 * @param args
 	 * @throws InterruptedException
 	 * @throws Exception
 	 */
 	public static void main(String args[]) {
 		Logger logger = null;
 		try {
 		LogManager.getLogManager().readConfiguration(
 				ManHourRecorder.class
 						.getResourceAsStream("logging.properties"));
 		
 		logger = Logger.getLogger(ManHourRecorder.class.getPackage().getName());
 		logger.info("start ManHourRecorder");
 
 			new GuiController().execute();
 		} catch (Exception e) {
 			e.printStackTrace();
 			logger.log(Level.SEVERE, "Exception", e);
 		}
 		logger.info("end ManHourRecorder");
 	}
 
 }
