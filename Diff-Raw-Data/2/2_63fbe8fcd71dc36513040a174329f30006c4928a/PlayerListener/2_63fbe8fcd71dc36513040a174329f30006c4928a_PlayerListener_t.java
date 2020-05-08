 /*
  * @author     ucchy
  * @license    GPLv3
  * @copyright  Copyright ucchy 2013
  */
 package com.github.ucchyocean.spm;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 
 /**
  * プレイヤーの行動を検知するリスナークラス
  * @author ucchy
  */
 public class PlayerListener implements Listener {
 
     /**
      * プレイヤーの死亡を検知するメソッド
      * @param event
      */
     @EventHandler
     public void onPlayerDeath(PlayerDeathEvent event) {
 
         Player player = event.getEntity();
         MatchingData data = SoupPVPMixer.instance.getMatchingDataFromPlayer(player);
 
         if ( data == null ) {
             return;
         }
 
         String winner;
         if ( data.getPlayer1().name.equals(player.getName()) ) {
             winner = data.getPlayer2().name;
         } else {
             winner = data.getPlayer1().name;
         }
 
         // 勝者のインベントリをクリアして、客席にテレポートする
         if ( SoupPVPMixer.config.winnerTeleportToSpectator ) {
             Player winnerPlayer = Bukkit.getPlayerExact(winner);
             SoupPVPMixer.clearInvAndHeal(winnerPlayer);
             if ( SoupPVPMixer.config.teleport.containsKey("spectator") ) {
                 Location loc = SoupPVPMixer.config.teleport.get("spectator");
                 winnerPlayer.teleport(loc, TeleportCause.PLUGIN);
             }
         }
 
         // 敗者のインベントリをクリアして、客席にテレポートする
         if ( SoupPVPMixer.config.loserRespawnToSpectator ) {
             Player loserPlayer = event.getEntity();
             SoupPVPMixer.clearInvAndHeal(loserPlayer);
             event.getDrops().clear();
             if ( SoupPVPMixer.config.teleport.containsKey("spectator") ) {
                 Location loc = SoupPVPMixer.config.teleport.get("spectator");
                 loserPlayer.teleport(loc, TeleportCause.PLUGIN);
             }
         }
 
         // マッチングデータを削除する
         SoupPVPMixer.instance.removeMatching(data);
     }
 
     /**
      * プレイヤーのサーバー退出を検知するメソッド
      * @param event
      */
     @EventHandler
     public void onPlayerQuit(PlayerQuitEvent event) {
 
         // 参加者から除去する
         SoupPVPMixer.instance.removeFromParticipant(event.getPlayer());
 
         // 負け判定をする場合、
         if ( SoupPVPMixer.config.loseByLogout ) {
 
             // マッチングされているプレイヤーなら、試合を強制中断して
             // ログアウトした側を負けとみなす
             MatchingData data =
                     SoupPVPMixer.instance.getMatchingDataFromPlayer(event.getPlayer());
             if ( data == null ) {
                 return;
             }
 
             Player winner = data.getAnotherPlayer(event.getPlayer());
             Player loser = event.getPlayer();
 
             // 勝者にメッセージを送る
            winner.sendMessage(Utility.replaceColorCode(Messages.get("endByLogout")));
 
             // ポイントを変動する
             SoupPVPMixer.bp.changePoints(winner, loser);
 
             // 勝者のインベントリをクリアして、客席にテレポートする
             if ( SoupPVPMixer.config.winnerTeleportToSpectator ) {
                 SoupPVPMixer.clearInvAndHeal(winner);
                 if ( SoupPVPMixer.config.teleport.containsKey("spectator") ) {
                     Location loc = SoupPVPMixer.config.teleport.get("spectator");
                     winner.teleport(loc, TeleportCause.PLUGIN);
                 }
             }
 
             // 敗者はキャッシュして、リスポーン時にテレポートする
             if ( SoupPVPMixer.config.loserRespawnToSpectator ) {
                 SoupPVPMixer.clearInvAndHeal(loser);
                 if ( SoupPVPMixer.config.teleport.containsKey("spectator") ) {
                     Location loc = SoupPVPMixer.config.teleport.get("spectator");
                     loser.teleport(loc, TeleportCause.PLUGIN);
                 }
             }
 
             // マッチングを削除する
             SoupPVPMixer.instance.removeMatching(data);
         }
     }
 }
