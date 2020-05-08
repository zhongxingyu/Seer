 package com.thepastimers.Warp;
 
 import com.thepastimers.CombatLog.CombatLog;
 import com.thepastimers.Database.Database;
 import com.thepastimers.Permission.Permission;
 import com.thepastimers.Worlds.Worlds;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: solum
  * Date: 7/7/13
  * Time: 8:05 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Warp extends JavaPlugin implements Listener {
     Database database;
     Permission permission;
     CombatLog combatLog;
     Worlds worlds;
 
     @Override
     public void onEnable() {
         getLogger().info("Pattern init");
 
         getServer().getPluginManager().registerEvents(this,this);
 
         database = (Database)getServer().getPluginManager().getPlugin("Database");
         if (database == null) {
             getLogger().warning("Unable to load Database plugin. Some functionality may not be available");
         }
 
         permission = (Permission)getServer().getPluginManager().getPlugin("Permission");
         if (permission == null) {
             getLogger().warning("Unable to load Permission plugin. Some functionality may not be available");
         }
 
         combatLog = (CombatLog)getServer().getPluginManager().getPlugin("CombatLog");
         if (combatLog == null) {
             getLogger().warning("Unable to load CombatLog plugin. Some functionality may not be available");
         }
 
         worlds = (Worlds)getServer().getPluginManager().getPlugin("Worlds");
         if (worlds == null) {
             getLogger().warning("Unable to load Worlds plugin. Some functionality may not be available.");
         }
 
         getLogger().info("Table info:");
         getLogger().info(WarpData.getTableInfo());
 
         getLogger().info("Pattern init complete");
     }
 
     @Override
     public void onDisable() {
         getLogger().info("Pattern disable");
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         String playerName = "";
 
         if (sender instanceof Player) {
             playerName = ((Player)sender).getName();
         } else {
             playerName = "CONSOLE";
         }
 
         //if (worlds != null && worlds.getPlayerWorldType(playerName) == Worlds.VANILLA) {
         //    return false;
         //}
 
         String command = cmd.getName();
 
         if (command.equalsIgnoreCase("warp")) {
             if (permission == null || !permission.hasPermission(playerName,"warp_warp") || playerName.equalsIgnoreCase("CONSOLE")) {
                 sender.sendMessage(ChatColor.RED + "You don't have permission to use this command (warp_warp)");
                 return true;
             }
 
             if (combatLog != null) {
                 int seconds = combatLog.secondsSinceCombat(playerName);
                 if (seconds > -1 && seconds < 10) {
                     sender.sendMessage(ChatColor.RED + "You were recently in combat. You must wait another " + (10-seconds) + " seconds before you can use /warp");
                     return true;
                 }
             }
 
             if (args.length > 0) {
                 String name = args[0];
                 List<WarpData> warpDataList = (List<WarpData>)database.select(WarpData.class,"warp = '" + database.makeSafe(name) + "'");
 
                 if (warpDataList.size() == 0) {
                     sender.sendMessage(ChatColor.RED + "Warp " + name + " cannot be found");
                     return true;
                 }
 
                 WarpData wd = warpDataList.get(0);

                if (getServer().getWorld(wd.getWorld()) == null) {
                    sender.sendMessage(ChatColor.RED + "The world this warp goes to does not currently exist");
                    return true;
                }
                 Location l = new Location(getServer().getWorld(wd.getWorld()),wd.getX(),wd.getY(),wd.getZ());
                 if (l == null) {
                     sender.sendMessage(ChatColor.RED + "This warp appears to be invalid");
                     return true;
                 }
                 sender.sendMessage(ChatColor.GREEN + "Warping you to " + wd.getWarp());
                 Player p = (Player)sender;
                 p.teleport(l);
             } else {
                 sender.sendMessage("Warps (Usage: /warp <name>):");
                 List<WarpData> warpDataList = (List<WarpData>)database.select(WarpData.class,"1");
 
                 StringBuilder sb = new StringBuilder();
                 for (int i=0;i<warpDataList.size();i++) {
                     WarpData wd = warpDataList.get(i);
                     sb.append(wd.getWarp());
                     // done so comma doens't get appended to last entry
                     if (i < warpDataList.size()-1) {
                         sb.append(",");
                     }
                 }
                 if (sb.length() > 0) {
                     sender.sendMessage(sb.toString());
                 }
             }
         } else if ("setwarp".equalsIgnoreCase(command)) {
             if (permission == null || !permission.hasPermission(playerName,"warp_setwarp") || playerName.equalsIgnoreCase("CONSOLE")) {
                 sender.sendMessage(ChatColor.RED + "You don't have permission to use this command (warp_setwarp)");
                 return true;
             }
 
             if (args.length > 0) {
                 String name = args[0];
                 Player p = (Player)sender;
                 Location l = p.getLocation();
 
                 List<WarpData> warpDataList = (List<WarpData>)database.select(WarpData.class,"name = '" + database.makeSafe(name) + "'");
                 if (warpDataList.size() != 0) {
                     sender.sendMessage(ChatColor.RED + "A warp by that name already exists");
                     return true;
                 }
 
                 WarpData wd = new WarpData();
                 wd.setWorld(l.getWorld().getName());
                 wd.setX(l.getBlockX());
                 wd.setY(l.getBlockY());
                 wd.setZ(l.getBlockZ());
                 wd.setWarp(name);
                 if (wd.save(database)) {
                     sender.sendMessage(ChatColor.GREEN + "Warp " + name + " created at " + l.getWorld().getName() + " (" + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() + ")");
                 } else {
                     sender.sendMessage(ChatColor.RED + "Unable to save warp");
                 }
             } else {
                 sender.sendMessage("/setwarp <name>");
             }
         } else {
             return false;
         }
 
         return true;
     }
 }
