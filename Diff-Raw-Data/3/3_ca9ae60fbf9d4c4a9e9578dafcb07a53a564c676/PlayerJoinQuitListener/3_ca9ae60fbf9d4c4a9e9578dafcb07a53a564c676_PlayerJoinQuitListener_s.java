 /*
  * @author     ucchy
  * @license    GPLv3
  * @copyright  Copyright ucchy 2013
  */
 package com.github.ucchyocean.mn;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 /**
  * プレイヤーのログインを取得するリスナークラス
  * @author ucchy
  */
 public class PlayerJoinQuitListener implements Listener {
 
     /**
      * プレイヤーがログインしたときに呼び出されるメソッド。
      * joinメッセージを他のプラグインに先に置き換えてもらうために、優先度を高くしている。
      * @param event
      */
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerJoin(PlayerJoinEvent event) {
 
         Player player = event.getPlayer();
         String nickname = UserConfiguration.getUserNickname(player.getName());
 
         // 保存していたニックネームを再設定
         if ( nickname != null ) {
            player.setDisplayName(ChatColor.AQUA + nickname + ChatColor.RESET);
 
             // ログイン時のメッセージを入れ替えする
             String message = event.getJoinMessage();
             event.setJoinMessage(message.replace(player.getName(), nickname));
         }
     }
 
     /**
      * プレイヤーがログアウトしたときに呼び出されるメソッド。
      * quitメッセージを他のプラグインに先に置き換えてもらうために、優先度を高くしている。
      * @param event
      */
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerQuit(PlayerQuitEvent event) {
 
         Player player = event.getPlayer();
         String nickname = UserConfiguration.getUserNickname(player.getName());
 
         // ログアウト時のメッセージを入れ替えする
         if ( nickname != null ) {
             String message = event.getQuitMessage();
             event.setQuitMessage(message.replace(player.getName(), nickname));
         }
     }
 
 }
