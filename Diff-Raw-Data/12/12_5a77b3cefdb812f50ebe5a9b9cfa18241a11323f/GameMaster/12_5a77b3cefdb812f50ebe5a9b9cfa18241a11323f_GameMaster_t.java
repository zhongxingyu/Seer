 package mud;
 
 import file.FileManipulator;
 import java.util.HashMap;
import java.util.Set;
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
     private HashMap<String, Player> players;
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
         }
         players = new HashMap<>();
         worldShapers = new HashMap<>();
         //I exist in all worlds for some reason
         Player player = new Player("Japhez");
         worldShapers.put("Japhez", player);
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
      * Adds the passed player to the player list.
      *
      * @param player the player to be added
      */
     public void addPlayer(Player player) {
         players.put(player.getName(), player);
     }
 
     /**
      * Removes the passed player from the player list.
      *
      * @param player the player to be removed
      */
     public void removePlayer(Player player) {
         players.remove(player.getName());
     }
 
     /**
      * @return the area manager
      */
     public AreaManager getAreaManager() {
         return areaManager;
     }
 
     /**
      * Returns the player with the passed name if they exist in the system.
      * Otherwise returns null.
      *
      * @param name
      * @return the player, or null
      */
     public Player getPlayer(String name) {
        Player p = null;
        Set<String> names = players.keySet();
        
        for (String currentName : names) {
            if (name.equalsIgnoreCase(currentName))
                p = players.get(currentName);
        }
        
        return p;
     }
 
     /**
      * Respawns the passed player and forces them to look at the room.
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
         player.look();
     }
 }
