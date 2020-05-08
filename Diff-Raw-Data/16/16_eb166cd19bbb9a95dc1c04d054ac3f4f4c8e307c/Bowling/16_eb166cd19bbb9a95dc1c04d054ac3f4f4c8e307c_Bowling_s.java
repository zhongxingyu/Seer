 package uk.co.geekcaroline.bowling;
 import java.io.*;
 
 /**
  * Creates the console and gets the player count then initialises the game.
  * */
 public class Bowling {
     //private static Logger logger;
     //private static Console console;
     private static BufferedReader inputStreamReader;
     private static InputDevice inputDevice;
 
 
     //TODO:
     // - finish implementing logger
     // - unit tests
     // - end to end testing
     // - alter methods to being package protected to enable further unit testing
     // - implement proper javadoc comments
     // - provide menu of different options, e.g. exit, delete last score etc.
 
     public static void main(String[] args) {
 
         inputDevice = InputDevice.getDefaultTextDevice();
 
         //logger = getLogger();
         CommandLineController commandLineController = new CommandLineController(inputDevice);
 
         int playerCount = commandLineController.getPlayerCount();
         assert playerCount>0 : "Player count is below permitted";
         assert playerCount<5 : "Player count is above permitted";
 
         String[] playerNames = commandLineController.getPlayerNames(playerCount);
         assert playerNames.length == playerCount+1 : "player names array not long enough";
 
         Competition competition = new Competition(playerNames, commandLineController);
         competition.play();
     }
 
 //    private static Logger getLogger(){
 //        Properties prop = null;
 //        Logger logger = Logger.getLogger("uk.co.geek-caroline.bowling");
 //        FileHandler fh;
 //
 //        try {
 //            fh = new FileHandler("logs/log.txt", true);
 //            logger.addHandler(fh);
 //            logger.setLevel(Level.ALL);
 //            SimpleFormatter formatter = new SimpleFormatter();
 //            fh.setFormatter(formatter);
 //        } catch (SecurityException e) {
 //            e.printStackTrace();
 //        } catch (IOException e) {
 //            e.printStackTrace();
 //        }
 //        return logger;
 //    }
 }
