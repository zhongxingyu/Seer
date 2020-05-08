 package server;
 import essentials.core.GameManager;
 import essentials.core.ScrabbleLogger;
 import essentials.enums.LogLevel;
 
 
 public class mtScrabbleServer {
 
 	public static String version = "0.1";
 	public static String author = "h.eilers";
 
 	/**
 	 * Main function
 	 * @param args Programm arguments
 	 */
 	public static void main(String[] args) {
 		
 		// Set logger settings
		ScrabbleLogger.logLevel = LogLevel.INFO;
 		ScrabbleLogger.logToFile = false;
 		ScrabbleLogger.logFile = "server.log";
 		
 		ScrabbleLogger.log( "Starting scrabble game server\n"
 							+ "version: " + version + "\t"
 							+ "author: " + author, LogLevel.DEFAULT );
 		
 		GameManager gm = new GameManager();
 		gm.startServer();
 		gm.startGame();		
 		gm.stopServer();
 		
 	}
 	
 }
