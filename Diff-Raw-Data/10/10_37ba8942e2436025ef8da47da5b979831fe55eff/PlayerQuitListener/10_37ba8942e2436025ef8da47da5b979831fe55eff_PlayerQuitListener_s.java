 /*
  * Copyright ucchy 2013
  */
 package com.github.ucchyocean.cmt.listener;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import com.github.ucchyocean.cmt.ColorMeTeaming;
 import com.github.ucchyocean.cmt.ColorMeTeamingConfig;
 
 /**
  * @author ucchy
  * プレイヤーがログアウトしたときに、通知を受け取って処理するクラス
  */
 public class PlayerQuitListener implements Listener {
 
     private static final String PRENOTICE = ChatColor.LIGHT_PURPLE.toString();
 
     /**
      * Playerがログアウトしたときに発生するイベント
      * @param event
      */
     @EventHandler
     public void onPlayerQuit(PlayerQuitEvent event) {
 
         Player player = event.getPlayer();
         String color = ColorMeTeaming.getPlayerColor(player);
 
         // ログアウトしたプレイヤーが、大将だった場合、逃げたことを全体に通知する。
         if ( ColorMeTeaming.leaders.containsKey(color) &&
                 ColorMeTeaming.leaders.get(color).contains(player) ) {
             String message = String.format(PRENOTICE + "%s チームの大将、%s は逃げ出した！",
                     color, player.getName());
             ColorMeTeaming.sendBroadcast(message);
             ColorMeTeaming.leaders.get(color).remove(player);
 
             if ( ColorMeTeaming.leaders.get(color).size() >= 1 ) {
                 message = String.format(PRENOTICE + "%s チームの残り大将は、あと %d 人です。",
                         color, ColorMeTeaming.leaders.get(color).size());
             } else {
                 message = String.format(PRENOTICE + "%s チームの大将は全滅しました！", color);
             }
             ColorMeTeaming.sendBroadcast(message);
         }
 
         // 色設定を削除する
        if ( ColorMeTeamingConfig.colorRemoveOnQuit ) {
            ColorMeTeaming.removePlayerColor(player);
        }
     }
 
 }
