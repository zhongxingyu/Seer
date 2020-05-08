 package uk.co.geekcaroline.bowling;
 
 import java.text.MessageFormat;
 
 /**
  * Controls and validates user inputs
  */
 public class CommandLineController {
     final String PLAYER_COUNT_WRONG = "Sorry, that is an invalid player count, it must be a number between 1 and 4, e.g. 2";
     final String ENTER_PLAYER_COUNT = "Please enter number of players:";
    final String ENTER_NAME_TEMPLATE = "Please enter the name for player %s:";
     final String PLAYER_NAME_WRONG = "Sorry, the name must be less that 10 characters long";
     final String QUERY_PIN_COUNT = "We are in frame, {0}, how many pins has {1} knocked over in his/her {2} go?";
     final String INVALID_SCORE_MESSAGE = "Sorry, your score must be between 1 and 10";
     final String SCORE_SHEET_HEADER = "            Frame number  |   1   ||   2   ||   3   ||   4   ||   5   ||   6   ||   7   ||   8   ||   9   ||  10   |";
     
     InputDevice inputDevice;
 
     public CommandLineController(InputDevice inputDevice){
         this.inputDevice = inputDevice;
     }
 
     public int getPlayerCount() {
         int playerCount;
         do {
             String playerCountStr = inputDevice.writeCommandThenReadLine(ENTER_PLAYER_COUNT);
             playerCount = this.getValidPlayerCount(playerCountStr);
         } while (playerCount == -1);
         return playerCount;
     }
 
     private int getValidPlayerCount(String userInput) {
         if(userInput.length()>1 || !Character.isDigit(userInput.charAt(0))){
             System.out.println(PLAYER_COUNT_WRONG);
             return -1;
         }
         Integer playerCount = -1;
         try {
             playerCount = Integer.valueOf(userInput);
         } catch (NumberFormatException nfe) {
             System.out.println("An impossible exception has occured, sorry the program will now have to exit");
             System.exit(-1);
 //            logger.warning(
 //                    "An exception has occured that should not be possible due to checks, userInput was: " + userInput
 //                    + ", printing stack trace:" + nfe.getStackTrace());
         }
         if( playerCount>4 || playerCount<1 ) {
             System.out.println(PLAYER_COUNT_WRONG);
             return -1;
         }
         return playerCount;
     }
 
     public String[] getPlayerNames(int playerCount) {
         String[] playerNames = new String[playerCount];
 
         for (int i = 0; i < playerCount; i++) {
             String playerNameReq = String.format(ENTER_NAME_TEMPLATE, Integer.toString(i+1));
             String name = null;
             do {
                 name = inputDevice.writeCommandThenReadLine(playerNameReq);
             } while (!this.isValidName(name));
             assert name.length() > 0 : "name length is too short";
             assert name.length() < 11 : "name length is too long";
             playerNames[i] = name;
         }
 
         return playerNames;
     }
 
     private boolean isValidName(String name) {
         if(name.length() < 1 || name.length()>10){
             System.out.println(PLAYER_NAME_WRONG);
             return false;
         }
         return true;
     }
     
     public int getPlayerPins(int frameNumber, String name, int attemptNumber) {
         String ordinal = "first";
         if (attemptNumber == 1) {
             ordinal = "second";
         } else if (attemptNumber == 2) {
             ordinal = "extra";
         }
         MessageFormat mf = new MessageFormat(QUERY_PIN_COUNT);
         //todo: clean up this number / string hack
         Integer outputFrameNumber = 1+frameNumber;
         String[] tokens =  {outputFrameNumber.toString(), name, ordinal};
         String msg = mf.format(tokens);
         int pinCount;
         do {
             pinCount = this.isValidPinCount(inputDevice.writeCommandThenReadLine(msg));
         } while (pinCount == -1);
         assert pinCount >= 0 : "pin count is less than 1";
         assert pinCount <= 10 : "pin count is greater than 10";
         return pinCount;
     }
 
 
     private int isValidPinCount(String pinCountStr) {
         int pinCount;
         try {
             pinCount = Integer.valueOf(pinCountStr);
         } catch (NumberFormatException nfe) {
             System.out.println(INVALID_SCORE_MESSAGE);
             return -1;
         }
         if (pinCount<0 || pinCount>10) {
             System.out.println(INVALID_SCORE_MESSAGE);
             return -1;
         }
         return pinCount;
     }
 
     public void printScoreSheet(Player[] players) {
         System.out.println("Here are the scores so far:");
         System.out.println(SCORE_SHEET_HEADER);
         for(int i=0; i<players.length; i++) {
             players[i].printScoreSheet();
         }
 
     }
 }
