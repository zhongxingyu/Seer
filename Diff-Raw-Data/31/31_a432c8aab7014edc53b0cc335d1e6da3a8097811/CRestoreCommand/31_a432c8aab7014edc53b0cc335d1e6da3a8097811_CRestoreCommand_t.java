 /*
  * @author     ucchy
  * @license    GPLv3
  * @copyright  Copyright ucchy 2013
  */
 package com.github.ucchyocean.cmt.command;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 import com.github.ucchyocean.cmt.ColorMeTeaming;
 
 /**
  * @author ucchy
  * colorrestore(crestore)コマンドの実行クラス
  */
 public class CRestoreCommand implements CommandExecutor {
 
     private static final String PREERR = ChatColor.RED.toString();
     private static final String PREINFO = ChatColor.GRAY.toString();
 
     /**
      * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
      */
     public boolean onCommand(
             CommandSender sender, Command command, String label, String[] args) {
 
         String profileName = "lastdata";
         if ( args.length >= 1 ) {
             profileName = args[0];
         }
 
        if ( !ColorMeTeaming.sdhandler.isExist(profileName) ) {
            sender.sendMessage(PREERR + profileName + " というデータが存在しません。");
            return true;
        }

         boolean result = ColorMeTeaming.sdhandler.load(profileName);
 
         if ( result ) {
             sender.sendMessage(PREINFO + "メンバー状況を、" + profileName + " から復帰しました。");
         } else {
             sender.sendMessage(PREERR + "メンバー状況の復帰に失敗しました。");
         }
 
         return true;
     }
 }
