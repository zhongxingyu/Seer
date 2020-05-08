 /*
  * Copyright ucchy 2013
  */
 package com.github.ucchyocean.cmt.command;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  * @author ucchy
  * colorglobal(g)コマンドの実行クラス
  */
 public class CChatGlobalCommand implements CommandExecutor {
 
    private static final String GLOBAL_CHAT_MARKER = "#GLOBAL#";
 
     /**
      * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
      */
     public boolean onCommand(
             CommandSender sender, Command command, String label, String[] args) {
 
         if ( args.length <= 0 ) {
             return false;
         }
 
         if ( !(sender instanceof Player) ) {
             sender.sendMessage(ChatColor.RED + "このコマンドは、ゲーム内からしか実行できません。");
             return true;
         }
 
         // メッセージの整形
         StringBuilder message = new StringBuilder();
         for ( String s : args ) {
             message.append(" " + s);
         }
 
         Player player = (Player)sender;
        String globalMessage = GLOBAL_CHAT_MARKER + message.toString();
 
         // コマンド実行者のチャットイベントとして処理
         player.chat(globalMessage);
 
         return true;
     }
 
 }
