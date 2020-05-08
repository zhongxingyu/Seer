 package vooga.rts.networking.server;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import vooga.rts.networking.NetworkBundle;
 import vooga.rts.networking.communications.LobbyInfo;
 import vooga.rts.networking.communications.servermessages.LobbyListMessage;
import vooga.rts.networking.logger.NetworkLogger;
 
 
 /**
  * This is a class that contains all the information about the game on the server. In this case a
  * game refers to a generic game created through VOOGA such as "Age of Empires". It contains methods
  * for clients to interact with Rooms.
  * 
  * @author David Winegar
  * 
  */
 public class GameContainer extends AbstractThreadContainer {
 
     private Map<Integer, Room> myRooms = new HashMap<Integer, Room>();
     private Map<Integer, LobbyInfo> myLobbyInfos = new HashMap<Integer, LobbyInfo>();
     private int myRoomNumber = 0;
 
     /**
      * Removes the room from the game container.
      */
     protected void removeRoom (Room room) {
         myRooms.remove(room.getID());
         myLobbyInfos.remove(room.getID());
     }
 
     /**
      * Adds the room to the game container.
      */
     protected void addRoom (Room room) {
         myRooms.put(room.getID(), room);
     }
 
     /**
      * Increments lobby info size with the given id
      */
     protected void incrementLobbyInfoSize (int id) {
         myLobbyInfos.get(id).addPlayer();
     }
 
     /**
      * Decrements lobby info size with the given id
      */
     protected void decrementLobbyInfoSize (int id) {
         myLobbyInfos.get(id).removePlayer();
     }
 
     /**
      * Joins the lobby if the lobby exists.
      * 
      * @param thread that is joining
      * @param lobbyNumber number of lobby
      */
     @Override
     public void joinLobby (ConnectionThread thread, int lobbyNumber) {
         if (myRooms.containsKey(lobbyNumber) &&
             myRooms.get(lobbyNumber).getNumberOfConnections() < myRooms.get(lobbyNumber)
                     .getMaxConnections()) {
             removeConnection(thread);
             myRooms.get(lobbyNumber).addConnection(thread);
            NetworkLogger.logMessage(Level.INFO, NetworkBundle.getString("LobbyJoined"));
         }
     }
 
     @Override
     public void startLobby (ConnectionThread thread, LobbyInfo lobbyInfo) {
         LobbyInfo newLobby = new LobbyInfo(lobbyInfo, myRoomNumber);
         Room lobby = new Lobby(myRoomNumber, this, newLobby);
         myLobbyInfos.put(myRoomNumber, newLobby);
         myRoomNumber++;
         lobby.addConnection(thread);
         addRoom(lobby);
        NetworkLogger.logMessage(Level.INFO, NetworkBundle.getString("LobbyStarted"));
     }
 
     @Override
     public void requestLobbies (ConnectionThread thread) {
         LobbyInfo[] infoArray = myLobbyInfos.values().toArray(new LobbyInfo[myLobbyInfos.size()]);
         thread.sendMessage(new LobbyListMessage(infoArray));
     }
 
 }
