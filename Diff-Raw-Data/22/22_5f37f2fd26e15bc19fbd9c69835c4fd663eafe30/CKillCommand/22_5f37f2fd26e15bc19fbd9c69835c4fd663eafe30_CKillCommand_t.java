/*
  * Copyright ucchy 2013
  */
 package com.github.ucchyocean.cmt.command;
 
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.github.ucchyocean.cmt.ColorMeTeaming;
 
 /**
  * @author ucchy
  * colorkill(ckill)コマンドの実行クラス
  */
 public class CKillCommand implements CommandExecutor {
 
     private static final String PRE_LINE_MESSAGE =
             "=== Kill Death Counts Information ===";
 
     /**
      * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
      */
     public boolean onCommand(
             CommandSender sender, Command command, String label, String[] args) {
 
         if ( args.length == 0 || (args.length >= 1 && args[0].equalsIgnoreCase("say")) ) {
 
             boolean isBroadcast = false;
             if ( args.length == 1 && args[0].equalsIgnoreCase("say") ) {
                 isBroadcast = true;
             }
 
             // グループは存在するが、得点データがない場合、このタイミングで作成しておく
             Hashtable<String, Vector<Player>> members = new Hashtable<String, Vector<Player>>();
             Enumeration<String> keys_all = members.keys();
             while ( keys_all.hasMoreElements() ) {
                 String key = keys_all.nextElement();
                 if ( !ColorMeTeaming.killDeathCounts.containsKey(key) ) {
                     ColorMeTeaming.killDeathCounts.put(key, new int[3]);
                 }
             }
 
             // 全グループの得点を集計して、得点順に並べる
             Vector<String> groups = new Vector<String>();
             Vector<Integer> points = new Vector<Integer>();
 
             Enumeration<String> keys = ColorMeTeaming.killDeathCounts.keys();
             while ( keys.hasMoreElements() ) {
                 String group = keys.nextElement();
                 int point = 0;
                 if ( ColorMeTeaming.killDeathCounts.containsKey(group) ) {
                     int[] counts = ColorMeTeaming.killDeathCounts.get(group);
                     point = counts[0] * ColorMeTeaming.killPoint +
                             counts[1] * ColorMeTeaming.deathPoint +
                             counts[2] * ColorMeTeaming.tkPoint;
                 }
 
                 int index = 0;
                 while ( groups.size() > index && points.elementAt(index) > point ) {
                     index++;
                 }
                 groups.add(index, group);
                 points.add(index, point);
             }
 
             // 全グループの得点を表示する
             if ( !isBroadcast ) {
                 sender.sendMessage(ChatColor.GRAY + PRE_LINE_MESSAGE);
             } else {
                 ColorMeTeaming.sendBroadcast(ChatColor.LIGHT_PURPLE + PRE_LINE_MESSAGE);
             }
 
             for ( int rank=1; rank<=groups.size(); rank++ ) {
                 String group = groups.elementAt(rank-1);
                 int point = points.elementAt(rank-1);
                 int[] counts = ColorMeTeaming.killDeathCounts.get(group);
                 String message = String.format(
                         "%d. %s %dpoints (%dkill, %ddeath, %dtk)",
                         rank, group, point, counts[0], counts[1], counts[2]);
 
                 if ( !isBroadcast ) {
                     sender.sendMessage(ChatColor.GRAY + message);
                 } else {
                     ColorMeTeaming.sendBroadcast(ChatColor.RED + message);
                 }
             }
 
         } else if ( args.length >= 1 && args[0].equalsIgnoreCase("clear") ) {
 
             ColorMeTeaming.killDeathCounts.clear();
             sender.sendMessage(ChatColor.GRAY + "KillDeath数をリセットしました。");
         }
 
         return true;
     }
 
 }
