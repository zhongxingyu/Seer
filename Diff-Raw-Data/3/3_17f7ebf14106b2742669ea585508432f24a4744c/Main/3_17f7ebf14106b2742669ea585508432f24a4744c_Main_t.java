 /**
  * 
  */
 package com.minestar.MineStarWarp;
 
 import java.util.logging.Logger;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * @author Meldanor
  * 
  */
 public class Main extends JavaPlugin {
 
     private static Logger log = Logger.getLogger("Minecraft");
 
     private static final String PLUGIN_NAME = "MineStarWarp";
 
     public static void writeToLog(String info) {
 
         log.info("[" + PLUGIN_NAME + "]:" + info);
     }
 
     public void onDisable() {
 
         writeToLog("disabled");
 
     }
 
     public void onEnable() {
 
         PluginManager pm = this.getServer().getPluginManager();
 
         writeToLog("enabled");
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command,
             String label, String[] args) {
         if (!(sender instanceof Player))
             return false;
         
         
         return true;
     }
 
 }
