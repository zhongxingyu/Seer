 /*
  * Copyright ucchy 2013
  */
 package com.github.ucchyocean.cmt.listener;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 import com.github.ucchyocean.cmt.ColorMeTeaming;
 
 /**
  * @author ucchy
  * チャットが発生したときに、チームチャットへ転送するためのリスナークラス
  */
 public class CChatListener implements Listener {
 
     private static final String GLOBAL_CHAT_MARKER = "#GLOBAL#";
 
     /**
      * Playerがチャットを送信したときに発生するイベント
      * @param event
      */
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
     public void onPlayerChat(AsyncPlayerChatEvent event) {
 
        // チームチャット無効なら、何もせずに抜ける
        if ( !ColorMeTeaming.isTeamChatMode ) {
            return;
        }

         // GLOBALマーカーが付いていたら、/g コマンドを経由してきたので、
         // GLOBALマーカーを取り除いてから抜ける。
         if ( event.getMessage().startsWith(GLOBAL_CHAT_MARKER) ) {
             String newMessage = event.getMessage().substring(GLOBAL_CHAT_MARKER.length());
             event.setMessage(newMessage);
             return;
         }
 
         Player player = event.getPlayer();
         String color = ColorMeTeaming.getPlayerColor(player);
 
         // 所属する色が無効グループなら、何もせずに抜ける
         if ( ColorMeTeaming.ignoreGroups.contains(color) ) {
             return;
         }
 
         // チームメンバに送信する
         ColorMeTeaming.sendTeamChat(player, event.getMessage());
 
         // 元のイベントをキャンセル
         event.setCancelled(true);
     }
 }
