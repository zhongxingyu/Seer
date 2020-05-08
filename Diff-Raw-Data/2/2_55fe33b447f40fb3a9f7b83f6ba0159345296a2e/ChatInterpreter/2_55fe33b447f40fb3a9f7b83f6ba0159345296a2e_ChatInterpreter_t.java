 package mud.network.server.input.interpreter;
 
 import java.net.InetAddress;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Set;
 import mud.GameMaster;
 import mud.Player;
 import mud.geography.Room;
 import mud.network.server.Connection;
 
 /**
  * This ChatInterpreter contains methods used to facilitate communication
  * between players within the game. It will require information provided from
  * the GameServer class, and in turn will provide abstraction to make that class
  * more manageable.
  *
  * @author Japhez
  */
 public class ChatInterpreter implements Interpretable {
 
     private HashMap<InetAddress, Connection> clientMap;
     private GameMaster master;
 
     public ChatInterpreter(HashMap<InetAddress, Connection> clientMap, GameMaster master) {
         this.clientMap = clientMap;
         this.master = master;
     }
 
     /**
      * Interprets the packet passed in, and takes action if this packet relates
      * to communication, then returns true. Otherwise this method returns false.
      *
      * @param player the player that sent the command
      * @param packet the Packet sent by the Client
      * @param args the arguments passed
      * @return true if the command is communication related, false otherwise
      */
     @Override
     public boolean interpret(Connection sender, ParsedInput input) {
         String firstWord = input.getFirstWord();
         //Check to see if the message is a tell
         if (firstWord.equalsIgnoreCase("tell")) {
             String target = input.getWordAtIndex(1);
             //Check to see if there is no target argument
             if (target == null) {
                 sender.sendMessage("Tell who what?");
                 return true;
                 //Target argument is there
             } else {
                 //Verify that target is valid
                 Player receiver = master.getPlayer(target);
                 //Target is invalid
                 if (receiver == null || !receiver.getConnection().isOnline()) {
                     sender.sendMessage("I can't seem to find that person.");
                     return true;
                     //Target is valid
                 } else {
                     //Make sure that there's a message to send
                     String message = input.getWordsStartingAtIndex(2);
                     String targetName = receiver.getName();
                     //No message to send
                     if (message == null) {
                         sender.sendMessage("What would you like to tell " + targetName + "?");
                         return true;
                         //Message to send
                     } else {
                        receiver.getConnection().sendMessage(sender.getPlayer().getName() + " tells you, \"" + message + "\"");
                         sender.sendMessage("You tell " + targetName + ", " + "\"" + message + "\"");
                         return true;
                     }
                 }
             }
         }
         //Check to see if the player is trying to say something to the room
         if (firstWord.equalsIgnoreCase("say")) {
             String senderName = sender.getPlayer().getName();
             //Check to make sure there's something to say
             String message = input.getWordsStartingAtIndex(1);
             //There is nothing to say
             if (message == null) {
                 sender.sendMessage("Say what?");
                 return true;
                 //There is something to say
             } else {
                 Room currentRoom = sender.getPlayer().getCurrentRoom();
                 ArrayList<Player> players = currentRoom.getPlayers();
                 for (Player p : players) {
                     //Don't send message to sender
                     if (p != sender.getPlayer()) {
                         p.sendMessage(senderName + " says, \"" + message + "\"");
                     }
                 }
                 //Send message to sender
                 sender.sendMessage("You say, \"" + message + "\"");
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Sends the given string to all connected clients except the given client.
      *
      * @param message the message to send
      */
     private void sendToAllConnectedUsers(String message, mud.network.server.Connection exception) {
         Set<InetAddress> keySet = clientMap.keySet();
         for (InetAddress inetAddress : keySet) {
             mud.network.server.Connection nextClient = clientMap.get(inetAddress);
             if (nextClient != exception) {
                 nextClient.sendMessage(message);
             }
         }
     }
 }
