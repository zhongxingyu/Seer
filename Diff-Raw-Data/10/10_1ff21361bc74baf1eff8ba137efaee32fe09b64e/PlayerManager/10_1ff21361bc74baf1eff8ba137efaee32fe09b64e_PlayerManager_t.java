 package mud;
 
import mud.characters.Player;
 import file.FileManipulator;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import mud.network.server.Connection;
 
 /**
  * A PlayerManager stores player data, and provides functionality for routinely
  * saving player data locally as necessary.
  *
  * @author Japhez
  */
 public class PlayerManager {
 
     public static final String PATH = "players//";
     public static final String EXTENSION = ".player";
     private HashMap<String, Player> players;
     private Thread playerDataSaver;
 
     /**
      * Creates a new player manager.
      */
     public PlayerManager() {
         players = new HashMap<>();
         playerDataSaver = new Thread(new PlayerDataSaver(), "Player data saver");
         playerDataSaver.start();
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
      * Returns the player with the passed name if they exist in the system.
      * Otherwise returns null.
      *
      * @param name
      * @return the player, or null
      */
     public Player getPlayer(String name) {
         Player p = null;
         for (String currentName : players.keySet()) {
             if (name.equalsIgnoreCase(currentName)) {
                 p = players.get(currentName);
             }
         }
         return p;
     }
 
     /**
      * Saves the passed player's data locally, overwriting any pre-existing
      * data.
      *
      * @param player the player whose data to save
      */
     public void savePlayer(Player player) {
         Connection connection = player.getConnection();
         player.setConnection(null);
         FileManipulator.writeObject(player, GameMaster.MAIN_DATA_PATH + PATH, player.getName() + EXTENSION);
         player.setConnection(connection);
     }
 
     /**
      * Attempts to load in the player with the passed name, and then return
      * them. If the player doesn't exist, returns null.
      *
      * @param playerName the name of the player to load in
      * @return a reference to the player if successful, null if not
      */
     public Player loadPlayer(String playerName) {
         if (FileManipulator.fileExists(GameMaster.MAIN_DATA_PATH + PATH, playerName + EXTENSION)) {
             //Retrieve the player
             Player player = (Player) FileManipulator.readObject(GameMaster.MAIN_DATA_PATH + PATH, playerName + EXTENSION);
             if (player.getCurrentRoom() == null) {
                 System.out.println("Attempting to load a player with a null current room...");
             }
             //Add them to the player list
             players.put(player.getName(), player);
             //Return a reference to the found player
             return player;
         } else {
             return null;
         }
     }
 
     /**
      * Starts the DataSaver runnable.
      */
     public void startDataSaver() {
         playerDataSaver.start();
     }
 
     /**
      * Interrupts the DataSaver runnable, which will then stop itself.
      */
     public void stopDataSaver() {
         playerDataSaver.interrupt();
     }
 
     /**
      * A runnable that regularly checks player data and saves it locally if
      * necessary.
      */
     public class PlayerDataSaver implements Runnable {
 
         private final int SCAN_INTERVAL = 5000;
 
         @Override
         public void run() {
             while (!Thread.interrupted()) {
                 //Check through player list on an interval, saving them when necessary
                 for (Player p : players.values()) {
                     //If the player needs saving, re-serialize their data
                    if (p.needsSaving()) {
                         savePlayer(p);
                         p.hasBeenSaved();
                     }
                 }
                 try {
                     //Once the player list has been checked, sleep the set amount of time
                     Thread.sleep(SCAN_INTERVAL);
                 } catch (InterruptedException ex) {
                     Logger.getLogger(PlayerManager.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }
     }
 }
