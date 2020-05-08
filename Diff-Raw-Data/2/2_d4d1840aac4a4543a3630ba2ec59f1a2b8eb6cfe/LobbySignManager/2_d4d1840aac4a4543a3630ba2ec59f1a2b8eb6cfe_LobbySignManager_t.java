 package uk.thecodingbadgers.minekart.lobby;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.logging.Level;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import uk.thecodingbadgers.minekart.MineKart;
 
 /**
  * @author TheCodingBadgers
  * 
  * The manager for all lobby signs, handles all loading and updating of lobby
  * join signs.
  */
 public class LobbySignManager {
 
     /** The currently loaded signs. */
     private static Set<LobbySign> signs = new HashSet<LobbySign>();
     
     /**
      * Load signs.
      */
     public static void loadSigns() {
         File lobby = MineKart.getLobbyFolder();
         
         File[] files = lobby.listFiles();
         
         for (File file : files) {
             FileConfiguration config = YamlConfiguration.loadConfiguration(file);
             LobbySign sign = new LobbySign();
             sign.load(file, config);
             signs.add(sign);
         }
         
         MineKart.getInstance().getLogger().log(Level.INFO, "Loaded " + signs.size() + " signs");
     }
     
     /**
      * Adds a sign to the internal store.
      *
      * @param sign the lobby sign to add
      */
     public static void addSign(LobbySign sign) {
         signs.add(sign);
         
         File file;
         
         int i = 0;
         
         do {
             file = new File(MineKart.getLobbyFolder(), sign.getCourse().getName() + "-" + i + ".yml");
             i++;
         } while(file.exists());
         
         try {
             file.createNewFile();
             
             FileConfiguration config = YamlConfiguration.loadConfiguration(file);
             sign.save(file, config);
             config.save(file);
         } catch (IOException e) {
            MineKart.getInstance().getLogger().log(Level.SEVERE, "A error has occured whilst creating a lobby sign.", e);
             return;
         }
     }
     
     public static void removeSign(LobbySign sign) {
         signs.remove(sign);
         sign.destroy();
     }
     
     /**
      * Update all signs to latest information.
      */
     public static void updateSigns() {
         Set<LobbySign> signs = new HashSet<LobbySign>(LobbySignManager.signs);
         
         for (LobbySign sign : signs) {
             if (sign.isEnabled()) {
                 sign.update();
             }
         }
     }
     
     /**
      * Checks if is a location holds a lobby sign.
      *
      * @param loc the location to check
      * @return true, if is lobby sign, false otherwise
      */
     public static boolean isLobbySign(Location loc) {
         Set<LobbySign> signs = new HashSet<LobbySign>(LobbySignManager.signs);
         
         for (LobbySign sign : signs) {
             if (sign.getBlock().getLocation().equals(loc) && sign.isEnabled()) {
                 return true;
             }
         }
         
         return false;
     }
 
     /**
      * Gets a lobby sign by the block.
      *
      * @param block the block to query
      * @return the sign object, null otherwise
      */
     public static LobbySign getSignByLocation(Block block) {
         
         if (!(block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST)) {
             return null;
         }
         
         Location location = block.getLocation();
         Set<LobbySign> signs = new HashSet<LobbySign>(LobbySignManager.signs);
         
         for (LobbySign sign : signs) {
             if (sign.getBlock().getLocation().equals(location) && sign.isEnabled()) {
                 return sign;
             }
         }
         
         return null;
     }
 }
