 package com.jamesst20.jcommandessentials.Utils;
 
 import com.jamesst20.config.JYamlConfiguration;
 import com.jamesst20.jcommandessentials.JCMDEssentials.JCMDEss;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class WarpConfig {
 
     static JCMDEss plugin = JCMDEss.plugin;
     static JYamlConfiguration config;
     public static HashMap<String, Location> warps = new HashMap<String, Location>();
 
     public static void createWarp(Player player, String name) {
         if (config.getConfig().get("warps." + name) == null) {
             config.getConfig().set("warps." + name + ".world", player.getLocation().getWorld().getName());
             config.getConfig().set("warps." + name + ".x", player.getLocation().getX());
             config.getConfig().set("warps." + name + ".y", player.getLocation().getY());
             config.getConfig().set("warps." + name + ".z", player.getLocation().getZ());
             config.getConfig().set("warps." + name + ".yaw", player.getLocation().getYaw());
             config.getConfig().set("warps." + name + ".pitch", player.getLocation().getPitch());
             config.saveConfig();
             warps.put(name, player.getLocation());
             Methods.sendPlayerMessage(player, "You have created a new warp : " + Methods.red(name) + ".");
         } else {
             Methods.sendPlayerMessage(player, ChatColor.RED + "This warp already exist.");
         }
     }
 
    public static void deleteWarp(CommandSender player, String name) {
         if (warps.containsKey(name)) {
             config.getConfig().set("warps." + name, null);
             config.saveConfig();
             warps.remove(name);
             Methods.sendPlayerMessage(player, "You have removed a warp named " + Methods.red(name) + ".");
         } else {
             Methods.sendPlayerMessage(player, ChatColor.RED + "The warp " + name + " doesn't exist.");
         }
     }
 
     public static void reloadWarps() {
         config = new JYamlConfiguration(plugin, "warps.yml");
         warps.clear();
         if (config.getConfig().getConfigurationSection("warps") != null) {
             for (String keyName : config.getConfig().getConfigurationSection("warps").getKeys(false)) {
                 World world = Bukkit.getServer().getWorld(config.getConfig().getString("warps." + keyName + ".world"));
                 double x = config.getConfig().getDouble("warps." + keyName + ".x");
                 double y = config.getConfig().getDouble("warps." + keyName + ".y");
                 double z = config.getConfig().getDouble("warps." + keyName + ".z");
                 float yaw = (float) config.getConfig().getDouble("warps." + keyName + ".yaw");
                 float pitch = (float) config.getConfig().getDouble("warps." + keyName + ".pitch");
                 warps.put(keyName, new Location(world, x, y, z, yaw, pitch));
             }
         }
         config.saveConfig();
     }
 
     public static Location getWarpLocation(String name) {
         if (warps.containsKey(name)) {
             return warps.get(name);
         } else {
             return null;
         }
     }
 
     public static ArrayList<String> getWarpsName(){
         ArrayList<String> warpsName = new ArrayList<String>();
         for (String name:warps.keySet()){
             warpsName.add(name);
         }
         return warpsName;
     }
 }
