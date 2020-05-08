 package mud.network.server.input.interpreter;
 
 import java.net.InetAddress;
 import java.util.HashMap;
 import mud.GameMaster;
 import mud.Player;
 import mud.network.server.Connection;
 
 /**
  * An interpreter that handles commands and input related to logging in.
  *
  * @author Japhez
  */
 public class LoginInterpreter implements Interpretable {
 
     private HashMap<InetAddress, Connection> clientMap;
    GameMaster master;
     private LoginStage currentStage;
     private String suggestedName;
 
     public LoginInterpreter(HashMap<InetAddress, Connection> clientMap, GameMaster master) {
         this.clientMap = clientMap;
         this.master = master;
         currentStage = LoginStage.NAME_SELECTION;
     }
 
     /**
      * An enumeration to store the current state of the login stage.
      */
     public enum LoginStage {
 
         CONNECTION, NAME_SELECTION, NAME_CREATION, PASSWORD_INPUT
     }
 
     @Override
     public boolean interpret(Connection sender, ParsedInput input) {
         //Unused at the moment
         if (currentStage.equals(LoginStage.CONNECTION)) {
             return true;
         }
         //Name selection stage
         if (currentStage.equals(LoginStage.NAME_SELECTION)) {
             //Attempt to reconcile player name
             //Multiple words
             if (input.getWordCount() != 1) {
                 sender.sendMessage("Names can only be one word, please try again.");
                 return true;
                 //One word
             } else {
                 suggestedName = input.getFirstWord();
                 //Verify name length
                 if (suggestedName.length() < 3) {
                     sender.sendMessage("That's an awfully short name, don't you think? Try something with at least 3 characters.");
                     return true;
                 }
                 if (suggestedName.length() > 8) {
                    sender.sendMessage("That's quite the mouthfull.  Try something with less than 9 characters?");
                     return true;
                 }
                 Player existingPlayer = master.getPlayer(suggestedName);
                 //Player name isn't yet taken
                 if (existingPlayer == null) {
                    sender.sendMessage("I don't know that name, would you like to be " + suggestedName + "? (yes/no)");
                     this.setCurrentStage(LoginStage.NAME_CREATION);
                     return true;
                     //Player name is taken
                 } else {
                     sender.sendMessage("What's your password, " + suggestedName + "?");
                     this.setCurrentStage(LoginStage.PASSWORD_INPUT);
                     return true;
                 }
             }
         }
         if (currentStage.equals(LoginStage.NAME_CREATION)) {
             String firstWord = input.getFirstWord();
             if (firstWord.equalsIgnoreCase("yes")) {
                 sender.sendMessage("Excellent. You will be known as " + suggestedName);
                 Player player = sender.getPlayer();
                 //Add the player to the visible client map
                 clientMap.put(sender.getClientAddress(), sender);
                 //Set the player's name to whatever they wanted
                 player.setName(suggestedName);
                 //Spawn the player somewhere
                 master.respawnPlayer(player);
                 //Update the player's interpreter
                 sender.setInterpreter(new MasterInterpreter(clientMap, master));
                 return true;
             } else if (firstWord.equalsIgnoreCase("no")) {
                 sender.sendMessage("Okay, who are you, then?");
                 setCurrentStage(LoginStage.NAME_SELECTION);
                 return true;
             } else {
                sender.sendMessage("A simple yes or no will suffice, thank you");
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Sets the current stage of the login process.
      *
      * @param stage
      */
     public void setCurrentStage(LoginStage stage) {
         this.currentStage = stage;
     }
 }
