 /*
  * @author     ucchy
  * @license    LGPLv3
  * @copyright  Copyright ucchy 2013
  */
 package com.github.ucchyocean.ct.command;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.github.ucchyocean.ct.ColorTeaming;
 import com.github.ucchyocean.ct.event.ColorTeamingPlayerLeaveEvent.Reason;
 
 /**
  * colorleave(cleave)コマンドの実行クラス
  * @author ucchy
  */
 public class CLeaveCommand implements CommandExecutor {
 
     private static final String PREERR = ChatColor.RED.toString();
 
     private ColorTeaming plugin;
 
     public CLeaveCommand(ColorTeaming plugin) {
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
 
         if ( !plugin.getCTConfig().isAllowPlayerLeave() ) {
             player.sendMessage(
                     PREERR + "cleaveコマンドによる離脱は、許可されておりません。");
             return true;
         }
 
         if ( plugin.getAPI().getPlayerTeamName(player) == null ) {
             player.sendMessage(
                     PREERR + "あなたはチームに所属していません。");
             return true;
         }
 
         plugin.getAPI().leavePlayerTeam(player, Reason.SELF);
        player.sendMessage(
                ChatColor.GREEN + "チームから離脱しました。");
 
         // メンバー情報をlastdataに保存する
         plugin.getAPI().getCTSaveDataHandler().save("lastdata");
 
         return true;
     }
 }
