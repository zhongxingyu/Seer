 package mybot;
 
 import java.util.logging.FileHandler;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.logging.SimpleFormatter;
 
 public class GameLogger {
 
 	private final static String LOGGER_NAME = "Ants";
 	private final static String LOG_DIR = "../log";
 	private Logger logger;
 
 	public GameLogger() {
 		logger = Logger.getLogger(LOGGER_NAME);
 		startLoggingToFile();
 		logger.setLevel(Level.ALL);
 	}
 
 	public String getRoundChanges() {
 		String log = getRoundHeader();
 		log += "Ants: " + Ant.getAnts().size() + " Food: " + Food.getFood().size();
 		return log;
 	}
 
 	public String getRoundHeader() {
 		return "========= Round #" + GameState.getRound() + " =========\n";
 	}
 
 	public void logRoundChanges() {
 		logger.info(getRoundChanges());
 		logAnts();
 		logFood();
 	}
 
 	public void logFood() {
 		String log = "";
 		for (Food food : Food.getFood()) {
 			log += food + "\n";
 		}
 		logger.info("Food status:\n" + log);
 	}
 
 	public void logAnts() {
 		String log = "";
 		for (Ant ant : Ant.getAnts()) {
 			log += ant + "\n";
 		}
 		logger.info("Ants status:\n" + log);
 	}
 
 	public void logMap() {
		//logger.info("Map:\n" + GameState.getMap().toString() + "\n");
 	}
 
 	public void startLoggingToFile() {
 		try {
 			Handler handler = new FileHandler(LOG_DIR + "/Ants.log", false);
 			handler.setFormatter(new SimpleFormatter());
 			logger.setUseParentHandlers(false);
 			logger.addHandler(handler);
 
 		} catch (Exception e) {
 			logger.severe("Logging to file not started: " + e.getMessage());
 		}
 	}
 
 	public void log(String message) {
 		logger.log(Level.INFO, message);
 	}
 
 }
