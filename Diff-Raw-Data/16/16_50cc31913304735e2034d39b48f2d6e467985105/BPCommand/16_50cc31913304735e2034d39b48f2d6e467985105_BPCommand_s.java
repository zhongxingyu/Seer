 /*
  * @author     ucchy
  * @license    LGPLv3
  * @copyright  Copyright ucchy 2013
  */
 package com.github.ucchyocean.bp;
 
 import java.util.ArrayList;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  * BattlePointsのコマンド実行クラス
  * @author ucchy
  */
 public class BPCommand implements CommandExecutor {
 
     private static final String[] COMMANDS = {
         "rank", "kdrank", "krank", "drank", "set", "reload", "team",
     };
     
     private BattlePoints plugin;
     
     /**
      * コンストラクタ
      * @param plugin 
      */
     public BPCommand(BattlePoints plugin) {
         this.plugin = plugin;
     }
 
     /**
      * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
      */
     public boolean onCommand(
             CommandSender sender, Command command, String label, String[] args) {
 
         if ( args.length <= 0 ) {
             return false;
         }
 
         if ( !isValidCommand(args[0]) ) {
             return false;
         }
 
         if ( !sender.hasPermission("battlepoints." + args[0]) ) {
             sender.sendMessage(ChatColor.RED + "You don't have permission \"battlepoints." + args[0] + "\".");
             return true;
         }
 
         if ( args[0].equalsIgnoreCase("rank") ) {
             int numberOfView = 10;
             if ( args.length >= 2 && Utility.tryIntParse(args[1]) ) {
                 numberOfView = Integer.parseInt(args[1]);
             }
             viewRank(sender, numberOfView);
             return true;
 
         } else if ( args[0].equalsIgnoreCase("kdrank") ) {
             int numberOfView = 10;
             if ( args.length >= 2 && Utility.tryIntParse(args[1]) ) {
                 numberOfView = Integer.parseInt(args[1]);
             }
             viewKDRank(sender, numberOfView);
             return true;
 
         } else if ( args[0].equalsIgnoreCase("krank") ) {
             int numberOfView = 10;
             if ( args.length >= 2 && Utility.tryIntParse(args[1]) ) {
                 numberOfView = Integer.parseInt(args[1]);
             }
             viewKillRank(sender, numberOfView);
             return true;
 
         } else if ( args[0].equalsIgnoreCase("drank") ) {
             int numberOfView = 10;
             if ( args.length >= 2 && Utility.tryIntParse(args[1]) ) {
                 numberOfView = Integer.parseInt(args[1]);
             }
             viewDeathRank(sender, numberOfView);
             return true;
 
         } else if ( args[0].equalsIgnoreCase("set") ) {
             if ( args.length >= 3 && Utility.tryIntParse(args[2]) ) {
                 int point = Integer.parseInt(args[2]);
                 plugin.setPoint(args[1], point);
                 sender.sendMessage(ChatColor.GRAY +
                         "set player " + args[1] + " point to " + point + ".");
                 return true;
             } else {
                 sender.sendMessage(ChatColor.RED + "invalid command parameter.");
                 return false;
             }
 
         } else if ( args[0].equalsIgnoreCase("add") ) {
             if ( args.length >= 3 && Utility.tryIntParse(args[2]) ) {
                 int point = Integer.parseInt(args[2]);
                 int newpoint = plugin.addPoint(args[1], point);
                 sender.sendMessage(ChatColor.GRAY +
                         "set player " + args[1] + " point to " + newpoint + ".");
                 return true;
             } else {
                 sender.sendMessage(ChatColor.RED + "invalid command parameter.");
                 return false;
             }
 
         } else if ( args[0].equalsIgnoreCase("reload") ) {
             plugin.reloadDatas();
             sender.sendMessage(ChatColor.GRAY + "reloaded configuration.");
             return true;
 
         } else if ( args[0].equalsIgnoreCase("team") ) {
             if ( BattlePoints.ctbridge == null ) {
                 sender.sendMessage(ChatColor.RED + "ColorTeaming cooperation feature was disabled.");
                return false;
             }
             int numberOfGroups = 2;
             if ( args.length >= 2 && args[1].matches("[2-9]") ) {
                 numberOfGroups = Integer.parseInt(args[1]);
             }
 
            return BattlePoints.ctbridge.doTeaming(sender, numberOfGroups);
         }
 
         return false;
     }
 
     /**
      * ランキングをsenderの画面に表示する
      * @param sender ランキング表示対象
      * @param numberOfView 表示する個数
      */
     private void viewRank(CommandSender sender, int numberOfView) {
         ArrayList<BPUserData> users = BPUserData.getAllUserData();
         BPUserData.sortUserData(users);
         displayRanking(sender, users, numberOfView, 
                 "===== Battle Points Ranking =====");
     }
 
     /**
      * KDレートランキングをsenderの画面に表示する
      * @param sender ランキング表示対象
      * @param numberOfView 表示する個数
      */
     private void viewKDRank(CommandSender sender, int numberOfView) {
         ArrayList<BPUserData> users = BPUserData.getAllUserData();
         BPUserData.sortUserDataByKDRate(users);
         displayRanking(sender, users, numberOfView, 
                 "===== K/D Rate Ranking =====");
     }
 
     /**
      * killカウントランキングをsenderの画面に表示する
      * @param sender ランキング表示対象
      * @param numberOfView 表示する個数
      */
     private void viewKillRank(CommandSender sender, int numberOfView) {
         ArrayList<BPUserData> users = BPUserData.getAllUserData();
         BPUserData.sortUserDataByKillCount(users);
         displayRanking(sender, users, numberOfView, 
                 "===== Kill Count Ranking =====");
     }
 
     /**
      * deathカウントランキングをsenderの画面に表示する
      * @param sender ランキング表示対象
      * @param numberOfView 表示する個数
      */
     private void viewDeathRank(CommandSender sender, int numberOfView) {
         ArrayList<BPUserData> users = BPUserData.getAllUserData();
         BPUserData.sortUserDataByDeathCount(users);
         displayRanking(sender, users, numberOfView, 
                 "===== Death Count Ranking =====");
     }
     
     /**
      * senderにランキングデータを表示する
      * @param sender 
      * @param data 
      * @param numberOfView 
      */
     private void displayRanking(CommandSender sender, 
             ArrayList<BPUserData> datas, int numberOfView, String headerString) {
         
         BPConfig config = plugin.getBPConfig();
         
         String playerName = null;
         boolean foundPlayer = false;
         if ( sender instanceof Player ) {
             playerName = ((Player)sender).getName();
         } else {
             foundPlayer = true;
         }
 
         if ( numberOfView > datas.size() ) {
             numberOfView = datas.size();
         }
 
         sender.sendMessage(ChatColor.LIGHT_PURPLE + headerString);
         
         for ( int i=0; i<numberOfView; i++ ) {
             BPUserData data = datas.get(i);
             String rank = config.getRankFromPoint(data.point);
             String color = config.getColorFromRank(rank);
             double rate = data.getKDRate();
             ChatColor headColor = ChatColor.WHITE;
             if ( data.name.equals(playerName) ) {
                 foundPlayer = true;
                 headColor = ChatColor.RED;
             }
             sender.sendMessage(String.format(headColor +
                     "%d. %s%s - %s - %dP, %dK, %dD, %.2f%%",
                     (i+1), color, data.name, rank,
                     data.point, data.kills, data.deaths, rate));
         }
         
         if ( !foundPlayer ) {
             for ( int i=numberOfView; i<datas.size(); i++ ) {
                 BPUserData data = datas.get(i);
                 if ( data.name.equals(playerName) ) {
                     sender.sendMessage(ChatColor.LIGHT_PURPLE 
                             + "===== Your Score =====");
                     String rank = config.getRankFromPoint(data.point);
                     String color = config.getColorFromRank(rank);
                     double rate = data.getKDRate();
                     sender.sendMessage(String.format(ChatColor.RED +
                             "%d. %s%s - %s - %dP, %dK, %dD, %.2f%%",
                             (i+1), color, data.name, rank,
                             data.point, data.kills, data.deaths, rate));
                     break;
                 }
             }
         }
     }
 
     private boolean isValidCommand(String command) {
 
         for ( String c : COMMANDS ) {
             if ( c.equalsIgnoreCase(command) ) {
                 return true;
             }
         }
         return false;
     }
 }
