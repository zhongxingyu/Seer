 /*******************************************************************************
  * Copyright (c) 2013 Travis Ralston.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser Public License v2.1
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  *
  * Contributors:
  * turt2live (Travis Ralston) - initial API and implementation
  ******************************************************************************/
 package com.turt2live.antishare;
 
 import com.feildmaster.lib.configuration.PluginWrapper;
 import com.turt2live.antishare.compatibility.SelfCompatibility;
 import com.turt2live.antishare.config.ASConfig;
 import com.turt2live.antishare.config.ConfigConvert;
 import com.turt2live.antishare.io.ItemMap;
 import com.turt2live.antishare.manager.*;
 import com.turt2live.antishare.regions.Region;
 import com.turt2live.antishare.util.GamemodeAbstraction;
 import com.turt2live.antishare.util.Messages;
 import com.turt2live.metrics.EMetrics;
 import com.turt2live.metrics.tracker.FixedTracker;
 import net.gravitydevelopment.updater.Updater;
 import net.gravitydevelopment.updater.Updater.UpdateResult;
 import net.gravitydevelopment.updater.Updater.UpdateType;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * AntiShare plugin class
  *
  * @author turt2live
  */
 public class AntiShare extends PluginWrapper {
 
     /**
      * AntiShare instance
      */
     public static AntiShare p;
     /**
      * AntiShare tool material
      */
     public static final Material ANTISHARE_TOOL = Material.BLAZE_ROD;
     /**
      * AntiShare tool for creating cuboids
      */
     public static final Material ANTISHARE_CUBOID_TOOL = Material.SLIME_BALL;
     /**
      * Used to force-set a block
      */
     public static final Material ANTISHARE_SET_TOOL = Material.BLAZE_POWDER;
     /**
      * AntiShare tool data value. This is to avoid competition with general actions
      */
     public static final short ANTISHARE_TOOL_DATA = 0; // TODO: Temp fix for #123
 
     // Folder locations
     public File generalDataDirectory;
     public File inventoriesDirectory;
     public File blocksDirectory;
     public File entitiesDirectory;
     public File worldConfigurations;
     public File regionConfigurations;
 
     // Files
     private File simpleNoticeFile;
 
     // Private stuff
     private EMetrics metrics;
     private String build;
     private Messages messages;
     private ASConfig config;
     private HookManager hooks;
     private WorldConfigManager worldConfigs;
     private MoneyManager money;
     private CuboidManager cuboids;
     private BlockManager blocks;
     private InventoryManager inventories;
     private RegionManager regions;
     private final List<String> disabledSNPlayers = new ArrayList<String>();
     private SplitManager splits;
 
     @Override
     public void onEnable() {
         p = this;
 
         // Create folder structure
         generalDataDirectory = new File(getDataFolder(), "data");
         inventoriesDirectory = new File(generalDataDirectory, "inventories");
         blocksDirectory = new File(generalDataDirectory, "blocks");
         entitiesDirectory = new File(generalDataDirectory, "entities");
         worldConfigurations = new File(getDataFolder(), "world_configurations");
         regionConfigurations = new File(getDataFolder(), "region_configurations");
         if (!getDataFolder().exists()) {
             getDataFolder().mkdirs();
         }
         if (!generalDataDirectory.exists()) {
             generalDataDirectory.mkdirs();
         }
         if (!inventoriesDirectory.exists()) {
             inventoriesDirectory.mkdirs();
         }
         if (!blocksDirectory.exists()) {
             blocksDirectory.mkdirs();
         }
         if (!entitiesDirectory.exists()) {
             entitiesDirectory.mkdirs();
         }
         if (!worldConfigurations.exists()) {
             worldConfigurations.mkdirs();
         }
         if (!regionConfigurations.exists()) {
             regionConfigurations.mkdirs();
         }
 
         // Setup files
         simpleNoticeFile = new File(generalDataDirectory, "disabled-simplenotice-users.txt");
 
         // Load disable SimpleNotice users
         try {
             if (simpleNoticeFile.exists()) {
                 BufferedReader in = new BufferedReader(new FileReader(simpleNoticeFile));
                 String line;
                 while ((line = in.readLine()) != null) {
                     disabledSNPlayers.add(line);
                 }
                 in.close();
             } else {
                 simpleNoticeFile.createNewFile();
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         // Start messages
         messages = new Messages();
 
         // Do configuration conversion before cleaning it up
         ConfigConvert.doConvert();
 
         // Check configuration
         getConfig().loadDefaults(getResource("config.yml"));
         if (getConfig().needsUpdate()) {
             getConfig().saveDefaults();
         }
         getConfig().load();
 
         // Get build number
         try {
             BufferedReader in = new BufferedReader(new InputStreamReader(getResource("plugin.yml")));
             String line;
             while ((line = in.readLine()) != null) {
                 if (line.startsWith("build: ")) {
                     line = line.replace("build: ", "");
                     build = line;
                     break;
                 }
             }
             in.close();
         } catch (IOException e) {
         }
 
         // Set value in configuration
         String val = getDescription().getVersion() + "|" + getServer().getVersion() + "|" + getServer().getOnlineMode() + "|" + build;
         if (!getConfig().getString("other.version_string", "").equalsIgnoreCase(val)) {
             getConfig().set("other.version_string", val);
             saveConfig();
         }
 
         // Start configuration
         config = new ASConfig(null, null);
 
         // Start update checker, if needed
         if (!getConfig().getBoolean("other.ignore-updates", false)) {
             getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
                 @Override
                 public void run() {
                     Updater update = new Updater(p, 35181, getFile(), UpdateType.NO_DOWNLOAD, true);
                     if (hasUpdate(getDescription().getVersion(), update.getLatestName()) && update.getResult() == UpdateResult.UPDATE_AVAILABLE) {
                         getLogger().warning(getMessages().getMessage("update", update.getLatestName(), getDescription().getVersion()));
                     }
                 }
             }, 0, 20 * 60 * 60 * 60); // Once an hour
         }
 
         // Pre-load
         blocks = new BlockManager();
         blocks.load();
 
         // Cleanup / Compatibility
         SelfCompatibility.migrateWorldConfigurations();
         SelfCompatibility.migratePlayerData();
         SelfCompatibility.cleanup520Inventories();
         SelfCompatibility.cleanup530Inventories();
         SelfCompatibility.cleanupOldInventories();
         SelfCompatibility.cleanup520blocks();
         SelfCompatibility.cleanup530FileStructure();
         SelfCompatibility.cleanupYAML();
         SelfCompatibility.convertTo540Inventories();
         SelfCompatibility.update540WorldConfigurations();
 
         // Start managers
         hooks = new HookManager();
         worldConfigs = new WorldConfigManager();
         money = new MoneyManager();
         cuboids = new CuboidManager();
         inventories = new InventoryManager();
         regions = new RegionManager();
         splits = new SplitManager();
 
         // Load managers
         hooks.load();
         worldConfigs.load();
         money.load();
         cuboids.load();
         inventories.load();
         regions.load();
         splits.load();
 
         // Start listeners
         getServer().getPluginManager().registerEvents(worldConfigs, this);
         getServer().getPluginManager().registerEvents(new ASListener(), this);
 
         // Start command stuff
         getCommand("antishare").setExecutor(new CommandHandler());
 
         // Offline Mode Warning
         if (!getServer().getOnlineMode()) {
             getLogger().severe("**********************");
             getLogger().severe(messages.getMessage("offlinemode"));
             getLogger().severe("**********************");
         }
 
         // Spam console
         getLogger().info(messages.getMessage("enabled"));
 
         // Load player information
         loadPlayerInformation();
 
         // Start metrics
         try {
             metrics = new EMetrics(this);
             Plugin mcmmo = getServer().getPluginManager().getPlugin("mcMMO");
             metrics.addTracker(new FixedTracker("mcMMO Servers", mcmmo != null ? "Found" : "Not Found"));
             metrics.startMetrics();
         } catch (IOException e) { // Metrics error
             e.printStackTrace();
         }
     }
 
     @Override
     public void onDisable() {
         // Save managers
         blocks.save();
         cuboids.save();
         regions.save();
         inventories.save();
         money.save();
 
         // Save disabled SimpleNotice users
         try {
             BufferedWriter out = new BufferedWriter(new FileWriter(simpleNoticeFile, false));
             for (String user : disabledSNPlayers) {
                 out.write(user + "\r\n");
             }
             out.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         // Shut down tasks
         getServer().getScheduler().cancelTasks(this);
 
         // Spam console
         getLogger().info(messages.getMessage("disabled"));
     }
 
     /**
      * Reload AntiShare
      */
     public void reload() {
         reloadConfig();
         config = new ASConfig(null, null);
         messages.reload();
         hooks.reload();
         worldConfigs.reload();
         cuboids.reload();
         regions.reload();
         inventories.reload();
         blocks.reload();
         splits.reload();
         loadPlayerInformation();
     }
 
     private boolean hasUpdate(String current, String latest) {
        if (current.toLowerCase().endsWith("-snapshot")) return false;
         current = current.toLowerCase();
         latest = latest.toLowerCase();
         if (current.contains("v")) current = current.split("v")[1];
         if (latest.contains("v")) latest = latest.split("v")[1];
 
         double currentVal = Double.valueOf(current.replaceFirst("\\.", ""));
         double latestVal = Double.valueOf(latest.replaceFirst("\\.", ""));
 
         return latestVal > currentVal;
     }
 
     private void loadPlayerInformation() {
         getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
             @Override
             public void run() {
                 for (Player player : getServer().getOnlinePlayers()) {
                     inventories.loadPlayer(player.getName());
                     Region playerRegion = regions.getRegion(player.getLocation());
                     if (playerRegion != null) {
                         playerRegion.alertSilentEntry(player);
                     }
                 }
                 if (inventories != null) {
                     int loaded = inventories.getLoaded();
                     if (loaded > 0) {
                         getLogger().info(messages.getMessage("inventories-loaded", String.valueOf(loaded)));
                     }
                 }
             }
         });
     }
 
     /**
      * Gets the active split manager
      *
      * @return the split manager
      */
     public SplitManager getSplitManager() {
         return splits;
     }
 
     /**
      * Gets the active block manager
      *
      * @return the block manager
      */
     public BlockManager getBlockManager() {
         return blocks;
     }
 
     /**
      * Gets the active inventory manager
      *
      * @return the inventory manager
      */
     public InventoryManager getInventoryManager() {
         return inventories;
     }
 
     /**
      * Gets the active region manager
      *
      * @return the region manager
      */
     public RegionManager getRegionManager() {
         return regions;
     }
 
     /**
      * Gets the active money manager
      *
      * @return the money manager
      */
     public MoneyManager getMoneyManager() {
         return money;
     }
 
     /**
      * Gets the active cuboid manager
      *
      * @return the cuboid manager
      */
     public CuboidManager getCuboidManager() {
         return cuboids;
     }
 
     /**
      * Gets the active world configuration manager
      *
      * @return the world configuration manager
      */
     public WorldConfigManager getWorldConfigs() {
         return worldConfigs;
     }
 
     /**
      * Gets the active hook manager
      *
      * @return the hook manager
      */
     public HookManager getHookManager() {
         return hooks;
     }
 
     /**
      * Gets the AntiShare configuration settings
      *
      * @return the AntiShare configuration settings
      */
     public ASConfig settings() {
         return config;
     }
 
     /**
      * Gets the message handler
      *
      * @return the message handler
      */
     public Messages getMessages() {
         return messages;
     }
 
     /**
      * Determines if a player decided to turn off SimpleNotice support
      *
      * @param name the player name
      * @return true if enabled (gets messages through SimpleNotice)
      */
     public boolean isSimpleNoticeEnabled(String name) {
         return !disabledSNPlayers.contains(name);
     }
 
     /**
      * Enables SimpleNotice support for a user
      *
      * @param name the user
      */
     public void enableSimpleNotice(String name) {
         disabledSNPlayers.remove(name);
     }
 
     /**
      * Disables SimpleNotice support for a user
      *
      * @param name the user
      */
     public void disableSimpleNotice(String name) {
         disabledSNPlayers.add(name);
     }
 
     /**
      * Gets the AntiShare build number
      *
      * @return the build number
      */
     public String getBuild() {
         return build;
     }
 
     /**
      * Gets the message prefix for AntiShare
      *
      * @return the message prefix
      */
     public String getPrefix() {
         return messages.getMessage("prefix");
     }
 
     /**
      * Determines if a player is blocked from doing something
      *
      * @param player          the player
      * @param allowPermission the "allow" permission
      * @param material        the material applied to the permissions (or null for none)
      * @return true if blocked
      */
     public boolean isBlocked(Player player, String allowPermission, Material material) {
         return isBlocked(player, allowPermission, material, false);
     }
 
     /**
      * Determines if a player is blocked from doing something
      *
      * @param player          the player
      * @param allowPermission the "allow" permission
      * @param material        the material applied to the permissions (or null for none)
      * @param specialOnly     true to only check permission.[item] permissions
      * @return true if blocked
      */
     public boolean isBlocked(Player player, String allowPermission, Material material, boolean specialOnly) {
         boolean check1 = isBlocked(player, allowPermission, material == null ? null : material.name(), specialOnly);
         @SuppressWarnings("deprecation")
         boolean check2 = isBlocked(player, allowPermission, String.valueOf(material == null ? null : material.getId()), specialOnly);
         if (check1 || check2) {
             return true;
         }
         if (check2) {
             // TODO: Magic value
             getMessages().magicValue();
         }
         if (material != null) {
             List<String> materials = ItemMap.getNamesFromID(material);
             for (String mat : materials) {
                 if (isBlocked(player, allowPermission, mat, specialOnly)) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     /**
      * Determines if a player is blocked from doing something
      *
      * @param player          the player
      * @param allowPermission the "allow" permission
      * @param target          the target to apply to this permission, spaces will removed
      * @return true if blocked
      */
     public boolean isBlocked(Player player, String allowPermission, String target) {
         return isBlocked(player, allowPermission, target, false);
     }
 
     /**
      * Determines if a player is blocked from doing something
      *
      * @param player          the player
      * @param allowPermission the "allow" permission
      * @param target          the target to apply to this permission, spaces will removed
      * @param specialOnly     true to only check permission.[item] permissions
      * @return true if blocked
      */
     public boolean isBlocked(Player player, String allowPermission, String target, boolean specialOnly) {
         if (target != null) {
             if (player.hasPermission(allowPermission + "." + target)) {
                 return false;
             }
         }
         if (specialOnly) {
             return false;
         }
         if (player.hasPermission(PermissionNodes.AFFECT_CREATIVE) && player.getGameMode() == GameMode.CREATIVE) {
             return true;
         }
         if (player.hasPermission(PermissionNodes.AFFECT_SURVIVAL) && player.getGameMode() == GameMode.SURVIVAL) {
             return true;
         }
         if (player.hasPermission(PermissionNodes.AFFECT_ADVENTURE) && player.getGameMode() == GameMode.ADVENTURE) {
             return true;
         }
         if (player.hasPermission(allowPermission)) {
             return false;
         }
         if (GamemodeAbstraction.isCreative(player.getGameMode()) && GamemodeAbstraction.isAdventureCreative()) {
             if (player.hasPermission(PermissionNodes.AFFECT_CREATIVE) || player.hasPermission(PermissionNodes.AFFECT_ADVENTURE)) {
                 return true;
             }
         }
         return false;
     }
 }
