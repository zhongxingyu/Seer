 /*
  * Copyright ucchy 2013
  */
 package com.github.ucchyocean.bp;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 /**
  * @author ucchy
  * BattlePointのプレイヤー関連イベントの監視クラス
  */
 public class PlayerListener implements Listener {
 
     /**
      * Entity が Entity にダメージを与えたときに発生するイベント
      * @param event
      */
     @EventHandler
     public void onEntityDamageEvent(EntityDamageByEntityEvent event) {
 
         // 0以下のダメージのイベントは無視する
         if (event.getDamage() <= 0) {
             return;
         }
 
         // 加害者と被害者の取得
         Entity attacker = event.getDamager();
         Entity defender = event.getEntity();
 
         // 両方プレイヤーの場合（＝剣や素手などの直接攻撃）
         if ( attacker instanceof Player && defender instanceof Player ) {
 
             // 最終攻撃者を記録
             BattlePoints.lastAttackData.setLastDamage(
                     (Player)defender, (Player)attacker);
 
         // 加害者が飛来物(Projectile)、被害者がプレイヤーの場合
         } else if ( attacker instanceof Projectile && defender instanceof Player ) {
 
             Projectile projectile = (Projectile)attacker;
             LivingEntity shooter = projectile.getShooter();
 
             // 飛来物を打ったのがプレイヤーなら、
             if ( shooter instanceof Player ) {
 
                 // 最終攻撃者を記録
                 BattlePoints.lastAttackData.setLastDamage(
                        (Player)defender, (Player)attacker);
             }
         }
     }
 
     /**
      * Player がサーバーに参加したときに発生するイベント
      * @param event
      */
     @EventHandler
     public void onPlayerJoinEvent(PlayerJoinEvent event) {
 
         Player player = event.getPlayer();
         int point = BattlePoints.data.getPoint(player.getName());
         String rank = BPConfig.getRankFromPoint(point);
 
         // 一応、最終攻撃履歴を消去しておく
         BattlePoints.lastAttackData.removeLastDamage(player);
 
         // Suffixの更新
         BattlePoints.setPlayerSuffix(player, makeSuffix(rank, point));
 
         // Colorの更新
         BattlePoints.setPlayerColor(player, BPConfig.rankColors.get(rank).name().toLowerCase());
     }
 
     /**
      * Player が死亡したときに発生するイベント
      * @param event
      */
     @EventHandler
     public void onPlayerDeath(PlayerDeathEvent event) {
 
         Player loser = event.getEntity();
 
         // killer を取得。
         // 直接攻撃で倒された場合は、killerをそのまま使う
         // 間接攻撃で倒された場合は、shooterを取得して使う
         // それでも取得できないなら、LastAttackDataから取得する
         Player winner = loser.getKiller();
         if ( (winner != null) && (winner instanceof Projectile) ) {
             EntityDamageEvent cause = loser.getLastDamageCause();
             LivingEntity shooter = ((Projectile) winner).getShooter();
             if ( cause instanceof EntityDamageByEntityEvent && shooter instanceof Player ) {
                 winner = (Player)shooter;
             }
         }
         if ( (winner == null) || !(winner instanceof Player) ) {
             winner = BattlePoints.lastAttackData.getLastAttacker(loser);
         }
 
         // killer が取得できなかったら、ここで諦める
         if ( winner == null ) {
             //BattlePoints.sendBroadcast(String.format(
             //        ChatColor.LIGHT_PURPLE + "%s は自殺した！", loser.getName()));
             BattlePoints.lastAttackData.removeLastDamage(loser);
             return;
         }
 
         // サーバーメッセージでアナウンス
         //BattlePoints.sendBroadcast(String.format(
         //        ChatColor.LIGHT_PURPLE + "%s は %s によって倒された！",
         //        loser.getName(), winner.getName()));
 
         // 最終攻撃履歴の削除
         BattlePoints.lastAttackData.removeLastDamage(winner);
         BattlePoints.lastAttackData.removeLastDamage(loser);
 
         // ポイント計算
         int lastWinnerPoint = BattlePoints.data.getPoint(winner.getName());
         int lastLoserPoint = BattlePoints.data.getPoint(loser.getName());
         int rate = BPConfig.getEloRating(lastWinnerPoint, lastLoserPoint);
         int winnerPoint = lastWinnerPoint + rate;
         int loserPoint = lastLoserPoint - rate;
 
         // 勝者、敗者が上限、下限に達したら、補正を行う
         if ( winnerPoint > 9999 ) {
             winnerPoint = 9999;
         }
         if ( loserPoint < 0 ) {
             loserPoint = 0;
         }
 
         BattlePoints.data.setPoint(winner.getName(), winnerPoint);
         BattlePoints.data.setPoint(loser.getName(), loserPoint);
         String wRank = BPConfig.getRankFromPoint(winnerPoint);
         String lRank = BPConfig.getRankFromPoint(loserPoint);
 
         BattlePoints.sendBroadcast(String.format(
                 ChatColor.LIGHT_PURPLE + "Winner : " +
                 BPConfig.rankColors.get(wRank) + "%s " +
                 ChatColor.WHITE + "%dP(+%d)  " +
                 ChatColor.LIGHT_PURPLE + "Loser : " +
                 BPConfig.rankColors.get(lRank) + "%s " +
                 ChatColor.WHITE + "%dP(-%dP)",
                 winner.getName(),  winnerPoint, rate,
                 loser.getName(), loserPoint, rate));
 
         // 称号が変わったかどうかを確認する
         if ( !wRank.equals(BPConfig.getRankFromPoint(lastWinnerPoint)) ) {
             BattlePoints.sendBroadcast(String.format(
                     ChatColor.RED + "%s は、%s にランクアップした！",
                     winner.getName(), wRank));
         }
         if ( !lRank.equals(BPConfig.getRankFromPoint(lastLoserPoint)) ) {
             BattlePoints.sendBroadcast(String.format(
                     ChatColor.GRAY + "%s は、%s にランクダウンした",
                     loser.getName(), lRank));
         }
 
         // Suffixの更新
         BattlePoints.setPlayerSuffix(winner, makeSuffix(wRank, winnerPoint));
         BattlePoints.setPlayerSuffix(loser, makeSuffix(lRank, loserPoint));
 
         // Colorの更新
         BattlePoints.setPlayerColor(winner, BPConfig.rankColors.get(wRank).name().toLowerCase());
         BattlePoints.setPlayerColor(loser, BPConfig.rankColors.get(lRank).name().toLowerCase());
     }
 
     /**
      * 称号とポイントから、suffixerを生成する
      * @param rank 称号
      * @param point ポイント
      * @return suffixer
      */
     private String makeSuffix(String rank, int point) {
         String symbol = BPConfig.rankSymbols.get(rank);
         ChatColor color = BPConfig.rankColors.get(rank);
         return String.format("&f[%s%s%d&f]&r", color.toString(), symbol, point);
     }
 }
