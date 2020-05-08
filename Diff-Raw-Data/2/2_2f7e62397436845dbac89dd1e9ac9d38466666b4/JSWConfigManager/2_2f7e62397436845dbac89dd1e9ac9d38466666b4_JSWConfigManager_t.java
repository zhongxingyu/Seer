 package com.imjake9.server.warps.utils;
 
 import com.imjake9.server.lib.EconomyManager;
 import com.imjake9.server.warps.JSWarps;
 import java.io.*;
 import java.util.HashMap;
 import java.util.Map;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 
 public class JSWConfigManager {
     
     private static String currentPlayer = null;
     private static FileConfiguration currentConfig = null;
     
     /**
      * Loads in the basic configuration data.
      */
     public static void loadConfiguration() {
         
         JSWarps plugin = JSWarps.getPlugin();
         File dataFolder = plugin.getDataFolder();
         
         // Load in default config
         if (!new File(dataFolder.getPath() + File.separator + "config.yml").exists()) {
             InputStream in = plugin.getResource("default.yml");
             if (!dataFolder.exists())
                 dataFolder.mkdirs();
             try {
                 OutputStream out = new FileOutputStream(new File(dataFolder, "config.yml"));
                 byte[] buf = new byte[1024];
                 int len;
                 while ((len = in.read(buf)) > 0) {
                     out.write(buf, 0, len);
                 }
                 out.close();
                 in.close();
             } catch (IOException ex) {
                 JSWarps.getPlugin().getMessager().severe("Error creating default config file.");
                 ex.printStackTrace();
             }
         }
         
         // Get config data
         plugin.getConfig().options().copyDefaults(true);
         
         // Set up warps data
         File f = new File(dataFolder.getPath() + File.separator + "warps");
         if(!f.exists()) f.mkdirs();
         
         // Set up homes data
         File homes = new File(dataFolder.getPath() + File.separator + "warps" + File.separator + "homes.txt");
         if(!homes.exists()) {
             try {
                 homes.createNewFile();
             } catch (IOException ex) {
                 JSWarps.getPlugin().getMessager().severe("Could not create homes file.");
                 ex.printStackTrace();
             }
         }
         
         // Set up public warp data
        File pub = new File(dataFolder.getPath() + File.separator + "warps" + File.separator + "public.txt");
         if(!pub.exists()) {
             try {
                 pub.createNewFile();
             } catch (IOException ex) {
                 JSWarps.getPlugin().getMessager().severe("Could not create public warp file.");
                 ex.printStackTrace();
             }
         }
         
         // Load in all warps
         Map<String, File> warpFiles = new HashMap<String, File>();
         for (File g : f.listFiles()) {
             String name = g.getName();
             int index = name.lastIndexOf(".");
             name = (index != -1) ? name.substring(0, index) : name;
             warpFiles.put(name, g);
         }
         
         JSWarpsManager.loadWarps(warpFiles);
         
     }
     
     /**
      * Gets individual economy player data.
      * 
      * @param player
      * @return 
      */
     public static FileConfiguration getPlayerData(String player) {
         player = player.toLowerCase();
         currentPlayer = player;
         
         File f = new File(JSWarps.getPlugin().getDataFolder().getPath() + File.separator + "players" + File.separator + player + ".yml");
         if (!f.exists()) try {
             if (!f.getParentFile().exists()) f.getParentFile().mkdirs();
             f.createNewFile();
         } catch (IOException ex) {
             JSWarps.getPlugin().getMessager().severe("Couldn't create data folder for player '" + player + "'.");
             ex.printStackTrace();
         }
         YamlConfiguration config = YamlConfiguration.loadConfiguration(f);
         
         currentConfig = config;
         
         return config;
     }
     
     /**
      * Saves economy data for the currently active player.
      */
     public static void savePlayerData() {
         if (currentPlayer == null || currentConfig == null)
             throw new RuntimeException("Can't call savePlayerData before registering loading a player.");
         
         savePlayerData(currentPlayer, currentConfig);
     }
     
     /**
      * Saves economy data for a particular player.
      * 
      * @param player
      * @param config 
      */
     public static void savePlayerData(String player, FileConfiguration config) {
         player = player.toLowerCase();
         File f = new File(JSWarps.getPlugin().getDataFolder().getPath() + File.separator + "players" + File.separator + player + ".yml");
         try {
             config.save(f);
         } catch (IOException ex) {
             JSWarps.getPlugin().getMessager().severe("Couldn't save data folder for player '" + player + "'.");
             ex.printStackTrace();
         }
     }
     
     /**
      * Gets all warps usable by a player.
      * 
      * @param player
      * @return 
      */
     public static int getUsableWarps(Player player) {
         if (EconomyManager.isUsingEconomy())
             return getDefaultWarps(player) + getUnlockedWarps(player);
         else return getDefaultWarps(player);
     }
     
     /**
      * Gets all warps possible to even have by a player.
      * 
      * @param player
      * @return 
      */
     public static int getPotentialWarps(Player player) {
         if (EconomyManager.isUsingEconomy())
             return getDefaultWarps(player) + getUnlockableWarps(player);
         else return getDefaultWarps(player);
     }
     
     /**
      * Gets warps usable by a player without economy extras.
      * 
      * @param player
      * @return 
      */
     public static int getDefaultWarps(Player player) {
         JSWarps plugin = JSWarps.getPlugin();
         FileConfiguration config = plugin.getConfig();
         
         // Find best warps instance
         if (config.getConfigurationSection("maxwarps.permissions") != null) {
             int ret = -1;
             // Look through permissions nodes
             for (String key : config.getConfigurationSection("maxwarps.permissions").getKeys(false)) {
                 if (player.hasPermission("jswarps.meta." + key) || player.hasPermission("meta." + key)) {
                     if (ret < config.getInt("maxwarps.permissions." + key, 0))
                         ret = config.getInt("maxwarps.permissions." + key, 0);
                 }
             }
             if (ret != -1)
                 return ret;
         }
         
         if (config.get("maxwarps.worlds." + player.getWorld().getName()) != null)
             return config.getInt("maxwarps.worlds." + player.getWorld().getName(), 0);
         return plugin.getConfig().getInt("maxwarps.default", 0);
     }
     
     /**
      * Gets the number of warps unlocked by a player.
      * 
      * @param player
      * @return 
      */
     public static int getUnlockedWarps(Player player) {
         FileConfiguration config = getPlayerData(player.getName().toLowerCase());
         return config.getInt("slots", 0);
     }
     
     /**
      * Gets the number of warps available to unlock by a player.
      * 
      * @param player
      * @return 
      */
     public static int getUnlockableWarps(Player player) {
         JSWarps plugin = JSWarps.getPlugin();
         FileConfiguration config = plugin.getConfig();
         
         if (config.getConfigurationSection("warpslots.permissions") != null) {
             int ret = -1;
             for (String key : config.getConfigurationSection("warpslots.permissions").getKeys(false)) {
                 if (player.hasPermission("jswarps.meta." + key) || player.hasPermission("meta." + key)) {
                     if (ret < config.getInt("warpslots.permissions." + key, 0))
                         ret = config.getInt("warpslots.permissions." + key, 0);
                 }
             }
             if (ret != -1)
                 return ret;
         }
         if (config.get("warpslots.worlds." + player.getWorld().getName()) != null)
             return config.getInt("warpslots.worlds." + player.getWorld().getName(), 0);
         return config.getInt("warpslots.default", 0);
     }
     
     /**
      * Gets the price of a new warp slot for a player.
      * 
      * @param player
      * @return 
      */
     public static double getSlotPrice(Player player) {
         JSWarps plugin = JSWarps.getPlugin();
         FileConfiguration config = plugin.getConfig();
         
         int unlocked = getUnlockedWarps(player);
         double ret = config.getDouble("slots.baseprice", 0);
         int scaleFactor = config.getInt("slots.scalefactor", 2);
         if (config.getBoolean("slots.use-multiplication", true))
             ret *= Math.pow(scaleFactor, unlocked);
         else
             ret += scaleFactor * unlocked;
         return ret;
     }
 
 }
