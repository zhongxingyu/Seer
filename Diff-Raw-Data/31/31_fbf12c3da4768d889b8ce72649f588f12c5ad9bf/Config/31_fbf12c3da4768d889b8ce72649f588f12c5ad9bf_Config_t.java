 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pl.chormon.LoginPoint;
 
 import java.text.MessageFormat;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 
 /**
  *
  * @author Chormon
  */
 public class Config {
 
     private static LoginPoint plugin;
 
     public static void initConfig(LoginPoint main) {
         main.reloadConfig();
         main.getConfig().options().copyDefaults(true);
         main.saveConfig();
         plugin = main;
     }
     
     public static String getMessage(String path)
     {
         return getMessage(path, null);
     }
 
    public static String getMessage(String path, Object... params) {
         String message = plugin.getConfig().getString("messages." + path);
         for (ChatColor color : ChatColor.values()) {
             String key = color.name().toLowerCase() + "%";
             if (message.contains(key)) {
                 message = message.replaceAll(key, color.toString());
             }
         }
         if (params != null) {
             return MessageFormat.format(message, params);
         } else {
             return message;
         }
     }
     
     public static Location getLoginPoint() {
         String name = plugin.getConfig().getString("loginpoint.world");
         double x = plugin.getConfig().getDouble("loginpoint.x");
         double y = plugin.getConfig().getDouble("loginpoint.y");
         double z = plugin.getConfig().getDouble("loginpoint.z");
         World world = null;
         if(name != null)
             world = plugin.getServer().getWorld(name);
         return new Location(world, x, y, z);
     }
 
     public static void setLoginPoint(Location location) {
         plugin.getConfig().set("loginpoint.world", location.getWorld().getName());
         plugin.getConfig().set("loginpoint.x", ((int)location.getX()));
         plugin.getConfig().set("loginpoint.y", (int)location.getY());
         plugin.getConfig().set("loginpoint.z", ((int)location.getZ()));
         plugin.saveConfig();
        String info = Config.getMessage("setLoginPoint", location.getX(), location.getY(), location.getZ(), location.getWorld().getName());
         plugin.getLogger().info(info);
     }
 
     public static void setLoginPoint(Location location, CommandSender sender) {
         setLoginPoint(location);
        sender.sendMessage(Config.getMessage("setLoginPoint", location.getX(), location.getY(), location.getZ(), location.getWorld().getName()));
     }
 }
