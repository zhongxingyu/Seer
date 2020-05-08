 package me.DDoS.Quarantine.player;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import me.DDoS.Quarantine.zone.QZone;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import me.DDoS.Quarantine.util.QInventoryItem;
 import me.DDoS.Quarantine.util.QUtil;
 import me.DDoS.Quarantine.Quarantine;
import me.DDoS.Quarantine.util.QDataErrors;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 /**
  *
  * @author DDoS
  */
 public class QPlayerData {
 
     protected final Player player;
     protected final QZone zone;
     protected int money;
     protected int health;
     protected int foodLevel;
     protected final int preGameHealth;
     protected final int preGameFoodLevel;
     protected Location lastLoc;
     protected int score;
     protected Set<String> keys;
 
     protected QPlayerData(Player player, QZone zone) {
 
         this.player = player;
         this.zone = zone;
         this.preGameHealth = player.getHealth();
         this.preGameFoodLevel = player.getFoodLevel();
 
     }
 
     protected QPlayerData(QPlayerData player) {
 
         this.player = player.getPlayer();
         this.zone = player.getZone();
         this.money = player.getMoney();
         this.health = player.getHealth();
         this.foodLevel = player.getFoodLevel();
         this.preGameHealth = player.getPreGameHealth();
         this.preGameFoodLevel = player.getPreGameFoodLevel();
         this.lastLoc = player.getLastLoc();
         this.score = player.getScore();
         this.keys = player.getKeys();
 
     }  
 
     public Player getPlayer() {
 
         return player;
 
     }
 
     public QZone getZone() {
 
         return zone;
 
     }
 
     public int getHealth() {
 
         return health;
 
     }
     
     public int getFoodLevel() {
         
         return foodLevel;
         
     }
 
     public Set<String> getKeys() {
 
         return keys;
 
     }
 
     public Location getLastLoc() {
 
         return lastLoc;
 
     }
 
     public int getMoney() {
 
         return money;
 
     }
 
     public int getPreGameHealth() {
 
         return preGameHealth;
 
     }
     
     public int getPreGameFoodLevel() {
 
         return preGameFoodLevel;
 
     }
 
     public int getScore() {
 
         return score;
 
     }
 
     public boolean load() {
 
         YamlConfiguration config = new YamlConfiguration();
 
         File mainDir = new File("plugins/Quarantine/" + zone.getName() + "/PlayerData");
 
         if (!mainDir.exists()) {
 
             mainDir.mkdirs();
 
         }
 
         File dir = new File(mainDir.getPath() + "/" + player.getName() + ".yml");
 
         if (!dir.exists()) {
 
             try {
 
                 dir.createNewFile();
 
             } catch (IOException ex) {
 
                 logError(QDataErrors.DATA_LOAD, ex);
                 return false;
 
             }
         }
 
         try {
 
             config.load(dir);
 
         } catch (Exception ex) {
 
             logError(QDataErrors.DATA_LOAD, ex);
             return false;
             
         }
 
         money = config.getInt("money", zone.getDefaultMoney());
         health = config.getInt("health", 20);
         foodLevel = config.getInt("foodLevel", 20);
         score = config.getInt("score", 0);
 
         if (zone.getEntrance() != null) {
 
             World world = player.getServer().getWorld(config.getString("lastLoc.World", zone.getEntrance().getWorld().getName()));
             double x = config.getDouble("lastLoc.X", zone.getEntrance().getX());
             double y = config.getDouble("lastLoc.Y", zone.getEntrance().getY());
             double z = config.getDouble("lastLoc.Z", zone.getEntrance().getZ());
             float yaw = (float) config.getDouble("lastLoc.Yaw", zone.getEntrance().getYaw());
             float pitch = (float) config.getDouble("lastLoc.Pitch", zone.getEntrance().getPitch());
 
             lastLoc = new Location(world, x, y, z, yaw, pitch);
             
             if (!zone.isInZone(lastLoc)) {
                 
                 lastLoc = zone.getEntrance();
                 QUtil.tell(player, ChatColor.RED + "Your last location was outside the zone. It has been reset to the entrance.");
                 
             }
 
         } else {
 
             lastLoc = null;
 
         }
 
         List<String> keyList = config.getStringList("keys");
 
         if (keyList != null) {
 
             keys = Sets.newHashSet(keyList);
 
         } else {
 
             keys = new HashSet<String>();
 
         }
 
         return true;
     }
 
     public boolean save(boolean lastLoc) {
 
         YamlConfiguration config = new YamlConfiguration();
 
         File mainDir = new File("plugins/Quarantine/" + zone.getName() + "/PlayerData");
 
         if (!mainDir.exists()) {
 
             mainDir.mkdirs();
 
         }
 
         File dir = new File(mainDir.getPath() + "/" + player.getName() + ".yml");
 
         if (!dir.exists()) {
 
             try {
 
                 dir.createNewFile();
 
             } catch (IOException ex) {
 
                 logError(QDataErrors.DATA_SAVE, ex);
                 return false;
 
             }
         }
 
         try {
 
             config.load(dir);
 
         } catch (Exception ex) {
 
             logError(QDataErrors.DATA_SAVE, ex);
             return false;
 
         }
 
         health = player.getHealth();
         foodLevel = player.getFoodLevel();
 
         config.set("money", money);
         config.set("health", health);
         config.set("foodLevel", foodLevel);
         config.set("score", score);
         config.set("keys", Lists.newArrayList(keys));
 
         if (lastLoc) {
 
             Location loc = player.getLocation();
             config.set("lastLoc.World", loc.getWorld().getName());
             config.set("lastLoc.X", loc.getX());
             config.set("lastLoc.Y", loc.getY());
             config.set("lastLoc.Z", loc.getZ());
             config.set("lastLoc.Yaw", loc.getYaw());
             config.set("lastLoc.Pitch", loc.getPitch());
 
         }
 
         try {
 
             config.save(dir);
 
         } catch (IOException ex) {
 
             logError(QDataErrors.DATA_SAVE, ex);
             return false;
 
         }
 
         return true;
 
     }
 
     public void deletePlayerDataFile() {
 
         File mainDir = new File("plugins/Quarantine/" + zone.getName() + "/PlayerData");
         File dir = new File(mainDir.getPath() + "/" + player.getName() + ".yml");
 
         dir.delete();
 
     }
 
     protected boolean restoreInventory() {
 
         File mainDir = new File("plugins/Quarantine/" + zone.getName() + "/PlayerInventories");
 
         if (!mainDir.exists()) {
 
             mainDir.mkdirs();
 
         }
 
         File backupFile = new File(mainDir.getPath() + "/" + player.getName() + ".inv");
 
         try {
 
             if (!backupFile.exists()) {
 
                 Set<Integer> items = zone.getKit().keySet();
                 PlayerInventory inv = player.getInventory();
 
                 for (Integer item : items) {
 
                     inv.addItem(new ItemStack(item, zone.getKit().get(item)));
 
                 }
 
                 return true;
 
             }
 
             ObjectInputStream ois = new ObjectInputStream(new FileInputStream(backupFile));
             QInventoryItem[] fromFile = (QInventoryItem[]) ois.readObject();
             ois.close();
 
             ItemStack[] armor = new ItemStack[4];
             ItemStack[] items = new ItemStack[fromFile.length - 4];
 
             for (int i = 0; i < 4; i++) {
 
                 armor[i] = fromFile[i].getItem();
 
             }
 
             for (int i = 4; i < fromFile.length; i++) {
 
                 items[i - 4] = fromFile[i].getItem();
 
             }
 
             PlayerInventory inv = player.getInventory();
             inv.setArmorContents(armor);
 
             for (ItemStack stack : items) {
 
                 if (stack != null) {
 
                     inv.addItem(stack);
 
                 }
             }
 
         } catch (Exception ex) {
 
             logError(QDataErrors.INV_RESTORE, ex);
             return false;
 
         }
 
         return true;
 
     }
 
     protected boolean storeInventory() {
 
         ItemStack[] armor = player.getInventory().getArmorContents();
         ItemStack[] items = player.getInventory().getContents();
 
         File mainDir = new File("plugins/Quarantine/" + zone.getName() + "/PlayerInventories");
 
         if (!mainDir.exists()) {
 
             mainDir.mkdirs();
 
         }
 
         File backupFile = new File(mainDir.getPath() + "/" + player.getName() + ".inv");
 
         try {
 
             if (backupFile.exists()) {
 
                 backupFile.delete();
 
             }
 
             backupFile.createNewFile();
 
             QInventoryItem[] inv = new QInventoryItem[armor.length + items.length];
 
             for (int i = 0; i < armor.length; i++) {
 
                 inv[i] = new QInventoryItem(armor[i]);
 
             }
 
             for (int i = 0; i < items.length; i++) {
 
                 inv[armor.length + i] = new QInventoryItem(items[i]);
 
             }
 
             ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(backupFile));
             oos.writeObject(inv);
             oos.flush();
             oos.close();
 
         } catch (Exception ex) {
 
             logError(QDataErrors.INV_STORE, ex);
             return false;
 
         }
 
         clearInventory();
         return true;
 
     }
 
     public void clearInventory() {
 
         PlayerInventory inv = player.getInventory();
         inv.clear();
         inv.setHelmet(null);
         inv.setChestplate(null);
         inv.setLeggings(null);
         inv.setBoots(null);
 
     }
 
     public void deleteInventory() {
 
         File mainDir = new File("plugins/Quarantine/" + zone.getName() + "/PlayerInventories");
         File dir = new File(mainDir.getPath() + "/" + player.getName() + ".inv");
 
         if (dir.exists()) {
 
             dir.delete();
 
         }
     }
     
     private void logError(QDataErrors error, Exception ex) {
         
         switch (error) {
             
             case DATA_LOAD:
                 Quarantine.log.info("[Quarantine] Couldn't load data for player: " + player.getName());
                
             case DATA_SAVE:
                 Quarantine.log.info("[Quarantine] Couldn't save data for player: " + player.getName());
                 
             case INV_RESTORE:
                 Quarantine.log.info("[Quarantine] Could not restore inventory for player: " + player.getName());
                 
             case INV_STORE:
                 Quarantine.log.info("[Quarantine] Could not store inventory for player: " + player.getName());
             
         }
         
         Quarantine.log.info("[Quarantine] Error message: " + ex.getMessage());
         
     }
     
     @Override
     public int hashCode() {
         
         return player.hashCode() ^ zone.getName().hashCode();
         
     }
     
     @Override
     public boolean equals(Object o) {
         
         if (o == null) {
             
             return false;
             
         }
         
         if (o == this) {
             
             return true;
             
         }
         
         if (!(o instanceof QPlayerData)) {
             
             return false;
             
         }
         
         QPlayerData p = (QPlayerData) o;  
         return p.getPlayer().equals(player) && p.getZone().getName().equals(zone.getName());
         
     }
 }
