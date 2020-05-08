 /*
  * Copyright ucchy 2013
  */
 package com.github.ucchyocean.cmt.command;
 
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Hashtable;
 
 import org.bukkit.ChatColor;
import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.github.ucchyocean.cmt.ColorMeTeaming;
 import com.github.ucchyocean.cmt.ColorMeTeamingConfig;
 import com.github.ucchyocean.cmt.Utility;
 
 /**
  * @author ucchy
  * colorteaming(ct)コマンドの実行クラス
  */
 public class CTeamingCommand implements CommandExecutor {
 
     private static final String PREERR = ChatColor.RED.toString();
     private static final String PREINFO = ChatColor.GRAY.toString();
 
     /**
      * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
      */
     public boolean onCommand(
             CommandSender sender, Command command, String label, String[] args) {
 
         if ( args.length < 1 ) {
             return false;
         }
 
         if ( args[0].equalsIgnoreCase("reload") ) {
 
             ColorMeTeamingConfig.reloadConfig();
             sender.sendMessage("config.ymlの再読み込みを行いました。");
             return true;
 
         } else if ( args[0].equalsIgnoreCase("removeall") ) {
 
            Location defaultSpawn = ColorMeTeamingConfig.defaultWorld.getSpawnLocation();

             Hashtable<String, ArrayList<Player>> members = ColorMeTeaming.getAllColorMembers();
             Enumeration<String> keys = members.keys();
             while ( keys.hasMoreElements() ) {
                 String group = keys.nextElement();
                 for ( Player p : members.get(group) ) {
                     ColorMeTeaming.removePlayerColor(p);
                    p.setBedSpawnLocation(defaultSpawn, true);
                 }
             }
             sender.sendMessage(PREINFO + "全てのグループが解散しました。");
 
             // 保護領域の更新
             if ( ColorMeTeamingConfig.protectRespawnPointWithWorldGuard ) {
                 ColorMeTeaming.wghandler.refreshGroupMembers();
             }
 
             return true;
 
         } else if ( args.length >= 2 && args[0].equalsIgnoreCase("remove") ) {
 
             Hashtable<String, ArrayList<Player>> members = ColorMeTeaming.getAllColorMembers();
             String group = args[1];
             if ( !members.containsKey(group) ) {
                 sender.sendMessage(PREERR + "グループ " + group + " は存在しません。");
                 return true;
             }

            Location defaultSpawn = ColorMeTeamingConfig.defaultWorld.getSpawnLocation();

             for ( Player p : members.get(group) ) {
                 ColorMeTeaming.removePlayerColor(p);
                p.setBedSpawnLocation(defaultSpawn, true);
                 p.sendMessage(PREINFO + "グループ " + group + " が解散しました。");
             }
             sender.sendMessage(PREINFO + "グループ " + group + " が解散しました。");
 
             // 保護領域の更新
             if ( ColorMeTeamingConfig.protectRespawnPointWithWorldGuard ) {
                 ColorMeTeaming.wghandler.refreshGroupMembers();
             }
 
             return true;
 
         } else if ( args.length >= 3 && args[0].equalsIgnoreCase("add") ) {
 
             String group = args[1];
             if ( !Utility.isValidColor(group) ) {
                 sender.sendMessage(PREERR + "グループ " + group + " はColorMeに設定できないグループ名です。");
                 return true;
             }
 
             Player player = ColorMeTeaming.getPlayerExact(args[2]);
             if ( player == null ) {
                 sender.sendMessage(PREERR + "プレイヤー " + args[2] + " は存在しません。");
                 return true;
             }
 
             ColorMeTeaming.setPlayerColor(player, group);
 
             // 保護領域の更新
             if ( ColorMeTeamingConfig.protectRespawnPointWithWorldGuard ) {
                 ColorMeTeaming.wghandler.refreshGroupMembers();
             }
 
             return true;
         }
 
         return false;
     }
 
 }
