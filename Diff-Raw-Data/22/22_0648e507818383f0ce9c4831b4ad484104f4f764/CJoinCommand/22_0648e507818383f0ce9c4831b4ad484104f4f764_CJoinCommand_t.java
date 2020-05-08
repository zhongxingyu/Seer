 /*
  * @author     ucchy
  * @license    LGPLv3
  * @copyright  Copyright ucchy 2013
  */
 package com.github.ucchyocean.ct.command;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.github.ucchyocean.ct.ColorTeaming;
 import com.github.ucchyocean.ct.config.TeamNameConfig;
 import com.github.ucchyocean.ct.config.TeamNameSetting;
 
 /**
  * colorjoin(cjoin)コマンドの実行クラス
  * @author ucchy
  */
 public class CJoinCommand implements CommandExecutor {
 
     private static final String PREERR = ChatColor.RED.toString();
 
     private ColorTeaming plugin;
 
     public CJoinCommand(ColorTeaming plugin) {
         this.plugin = plugin;
     }
 
     /**
      * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
      */
     public boolean onCommand(
             CommandSender sender, Command command, String label, String[] args) {
 
         if ( !(sender instanceof Player) ) {
             sender.sendMessage(
                     PREERR + "このコマンドはゲーム内からのみ実行可能です。");
             return true;
         }
 
         Player player = (Player)sender;
 
         TeamNameSetting team = null;
         if ( args.length == 0 || args[0].equalsIgnoreCase("random") ) {
 
             if ( !plugin.getCTConfig().isAllowPlayerJoinRandom() ) {
                 player.sendMessage(
                         PREERR +
                         "cjoinコマンドによるランダム参加は、許可されておりません。");
                 return true;
             }
 
             if ( plugin.getAPI().getPlayerTeamName(player) != null ) {
                 player.sendMessage(
                         PREERR + "あなたは既に、チームに所属しています。");
                 return true;
             }
 
             team = getLeastTeam();
             if ( team == null ) {
                 sender.sendMessage(
                         PREERR + "参加できるチームが無いようです。");
                 return true;
             }
 
             plugin.getAPI().addPlayerTeam(player, team);
 
            // サイドバー更新
            plugin.getAPI().refreshSidebarScore();
 
             // メンバー情報をlastdataに保存する
             plugin.getAPI().getCTSaveDataHandler().save("lastdata");
 
             return true;
 
         } else {
 
             if ( !plugin.getCTConfig().isAllowPlayerJoinAny() ) {
                 player.sendMessage(
                         PREERR +
                         "cjoin (group) コマンドによる任意チームへの参加は、許可されておりません。");
                 return true;
             }
 
             if ( plugin.getAPI().getPlayerTeamName(player) != null ) {
                 player.sendMessage(
                         PREERR + "あなたは既に、チームに所属しています。");
                 return true;
             }
 
             String target = args[0];
             ArrayList<TeamNameSetting> teams = plugin.getAPI().getTeamNameConfig().getTeamNames();
             if ( !TeamNameConfig.containsID(teams, target) ) {
                 sender.sendMessage(PREERR + target + " は設定できないチーム名です。");
                 return true;
             }
             TeamNameSetting tns = plugin.getAPI().getTeamNameConfig().getTeamNameFromID(target);
             plugin.getAPI().addPlayerTeam(player, tns);
 
            // サイドバー更新
             plugin.getAPI().makeSidebarScore();
 
             // メンバー情報をlastdataに保存する
             plugin.getAPI().getCTSaveDataHandler().save("lastdata");
 
             return true;
 
         }
     }
 
     /**
      * メンバー人数が最小のチームを返す。
      * @return メンバー人数が最小のチーム
      */
     private TeamNameSetting getLeastTeam() {
 
         HashMap<String, ArrayList<Player>> members =
                 plugin.getAPI().getAllTeamMembers();
         int least = 999;
         TeamNameSetting leastTeam = null;
 
         ArrayList<TeamNameSetting> teams = plugin.getAPI().getAllTeamNames();
         // ランダム要素を入れるため、チーム名をシャッフルする
         Collections.shuffle(teams);
 
         for ( TeamNameSetting tns : teams ) {
             if ( least > members.get(tns.getID()).size() ) {
                 least = members.get(tns.getID()).size();
                 leastTeam = tns;
             }
         }
 
         return leastTeam;
     }
 }
 
