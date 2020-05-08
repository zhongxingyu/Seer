 package mud;
 
import mud.characters.Player;
 import file.FileManipulator;
 import java.util.HashMap;
 import mud.geography.Room;
 import mud.network.server.log.ConsoleLog;
 
 /**
  * The game manager that stores area, and player data. This is the top-level
  * class that provides functionality for the rest of the program.
  *
  * @author Japhez
  */
 public class GameMaster {
 
     public final static String MAIN_DATA_PATH = "data//";
     private AreaManager areaManager;
     private PlayerManager playerManager;
     private HashMap<String, Player> worldShapers;
 
     /**
      * Creates a new GameMaster, then attempts to load in an existing
      * AreaManger. If there is no saved AreaManager, it creates a new one (and a
      * sample area).
      */
     public GameMaster() {
         //Read in an existing area manager if possible, otherwise create a new one
         if (FileManipulator.fileExists(MAIN_DATA_PATH, "AreaManager.data")) {
             areaManager = (AreaManager) FileManipulator.readObject(MAIN_DATA_PATH, "AreaManager.data");
         } else {
             System.out.println(ConsoleLog.log() + "AreaManager not found, creating new one.");
             areaManager = new AreaManager();
             FileManipulator.writeObject(areaManager, MAIN_DATA_PATH, "AreaManager.data");
         }
         playerManager = new PlayerManager();
         worldShapers = new HashMap<>();
         //I exist in all worlds for some reason
         Player player = new Player("Japhez");
         worldShapers.put("Japhez", player);
     }
 
     public PlayerManager getPlayerManager() {
         return playerManager;
     }
 
     public void addWorldShaper(Player player) {
         worldShapers.put(player.getName(), player);
     }
 
     /**
      * @param player
      * @return true if the passed player is a World Shaper
      */
     public boolean playerIsWorldShaper(Player player) {
         return (worldShapers.get(player.getName()) != null);
     }
 
     /**
      * @return the area manager
      */
     public AreaManager getAreaManager() {
         return areaManager;
     }
 
     /**
      * Moves the passed player to their respawn (or the respawn room in
      * AreaManager if the player doesn't have one).
      *
      * @param player
      */
     public void respawnPlayer(Player player) {
         Room respawnRoom = player.getRespawnRoom();
         //See if the respawn point exists specifically for this player
         //If it doesn't exist, use the area respawn room
         if (respawnRoom == null) {
             player.setRespawnRoom(areaManager.getRespawnRoom());
             respawnRoom = player.getRespawnRoom();
         }
         if (respawnRoom == null) {
             System.out.println("crap, respawn room is null somehow");
         }
         player.setCurrentRoom(respawnRoom);
     }
 }
