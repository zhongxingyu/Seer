 /*
  * Copyright ucchy 2013
  */
 package com.github.ucchyocean.ct.command;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 import com.github.ucchyocean.ct.ColorTeamingConfig;
 
 /**
  * @author ucchy
  * colorfriendlyfire(cff)コマンドの実行クラス
  */
 public class CFriendlyFireCommand implements CommandExecutor {
 
     /**
      * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
      */
     public boolean onCommand(
             CommandSender sender, Command command, String label, String[] args) {
 
         if ( args.length <= 0 ) {
             return false;
         }
 
         if ( args[0].equalsIgnoreCase("on") ) {
             ColorTeamingConfig.isFriendlyFireDisabler = true;
            //ColorTeaming.setFriendlyFilre(false);
             sender.sendMessage(ChatColor.GRAY + "仲間同士の攻撃が無効になりました。");
             ColorTeamingConfig.setConfigValue("firelyFireDisabler", true);
             return true;
         } else if ( args[0].equalsIgnoreCase("off") ) {
             ColorTeamingConfig.isFriendlyFireDisabler = false;
            //ColorTeaming.setFriendlyFilre(true);
             sender.sendMessage(ChatColor.GRAY + "仲間同士の攻撃が有効になりました。");
             ColorTeamingConfig.setConfigValue("firelyFireDisabler", false);
             return true;
         }
 
         return false;
     }
 
 }
