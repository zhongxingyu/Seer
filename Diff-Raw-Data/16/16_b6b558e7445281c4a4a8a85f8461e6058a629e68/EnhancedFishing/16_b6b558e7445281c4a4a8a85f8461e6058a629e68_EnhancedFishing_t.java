 package com.norcode.bukkit.enhancedfishing;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.World;
 import org.bukkit.block.Biome;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class EnhancedFishing extends JavaPlugin {
 
     private List<Permission> loadedPermissions = null;
     private WorldConfiguration defaultConfiguration;
     private HashMap<String, WorldConfiguration> worldConfigurations;
     private ConfigAccessor treasureConfig;
     public WorldConfiguration getDefaultConfiguration() {
         return defaultConfiguration;
     }
 
     @Override
     public void onEnable() {
         treasureConfig = new ConfigAccessor(this, "treasure.yml");
         FileConfiguration config = getConfig();
         config.options().copyDefaults(true);
         saveConfig();
         getServer().getPluginManager().registerEvents(new FishingListener(this), this);
         worldConfigurations = new HashMap<String, WorldConfiguration>();
         loadConfig();
     }
 
     private void loadConfig() {
         worldConfigurations.clear();
         defaultConfiguration = new WorldConfiguration(this, "default");
         for (String key: getConfig().getKeys(false)) {
             if (!key.equals("default")) {
                 getLogger().info("Loading custom world configuration for " + key);
                World world = getServer().getWorld(key);
                 if (world == null) {
                    getLogger().warning("world '" + key + "' does not exist.");
                     continue;
                 }
                worldConfigurations.put(key.toLowerCase(), new WorldConfiguration(this, key));
             }
         }
     }
 
     public WorldConfiguration getWorldConfiguration(World world) {
         return getWorldConfiguration(world.getName());
     }
 
     public WorldConfiguration getWorldConfiguration(String world) {
         WorldConfiguration cfg = worldConfigurations.get(world.toLowerCase());
         if (cfg == null) {
             cfg = defaultConfiguration;
         }
         return cfg;
     }
 
     public List<Permission> getLoadedPermissions() {
         return loadedPermissions;
     }
 
     public void setLoadedPermissions(List<Permission> loadedPermissions) {
         this.loadedPermissions = loadedPermissions;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command,
             String label, String[] args) {
         if (args[0].toLowerCase().equals("reload")) {
             if (sender.hasPermission("enhancedfishing.admin")) {
                 this.loadConfig();
                 sender.sendMessage("EnhancedFishing config has been reloaded from disk.");
                 return true;
             }
         }
         return false;
     }
 
     public ConfigAccessor getTreasureConfig() {
         return treasureConfig;
     }
 }
