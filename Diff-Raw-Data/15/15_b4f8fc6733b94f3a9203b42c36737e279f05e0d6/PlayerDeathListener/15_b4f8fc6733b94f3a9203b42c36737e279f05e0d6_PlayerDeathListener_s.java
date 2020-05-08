 /*
  * Copyright ucchy 2013
  */
 package com.github.ucchyocean.cmt.listener;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import com.github.ucchyocean.cmt.ColorMeTeaming;
 
 /**
  * @author ucchy
  * プレイヤーが死亡したりログアウトしたときに、通知を受け取って処理するクラス
  */
 public class PlayerDeathListener implements Listener {
 
     private static final String PRENOTICE = ChatColor.LIGHT_PURPLE.toString();
 
     /**
      * Playerが死亡したときに発生するイベント
      * @param event
      */
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
     public void onPlayerDeath(PlayerDeathEvent event) {
 
         Player player = event.getEntity();
         String color = ColorMeTeaming.getPlayerColor(player);
 
         // Death数を加算
         if ( !ColorMeTeaming.ignoreGroups.contains(color) ) {
             // グループへ加算
             if ( !ColorMeTeaming.killDeathCounts.containsKey(color) ) {
                 ColorMeTeaming.killDeathCounts.put(color, new int[3]);
             }
             ColorMeTeaming.killDeathCounts.get(color)[1]++;
             // ユーザーへ加算
             if ( !ColorMeTeaming.killDeathUserCounts.containsKey(player) ) {
                 ColorMeTeaming.killDeathUserCounts.put(player, new int[3]);
             }
             ColorMeTeaming.killDeathUserCounts.get(player)[1]++;
         }
 
         // 死亡したプレイヤーが、大将だった場合、倒されたことを全体に通知する。
         if ( ColorMeTeaming.leaders.containsKey(color) &&
                 ColorMeTeaming.leaders.get(color).contains(player) ) {
             String message = String.format(PRENOTICE + "%s チームの大将、%s が倒されました！",
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
 
         // 倒したプレイヤーを取得
         // 直接攻撃で倒された場合は、killerをそのまま使う
         Player killer = player.getKiller();
         // 間接攻撃で倒された場合は、shooterを取得して使う
         if ( killer == null ) {
             return;
         } else if ( killer instanceof Projectile ) {
             EntityDamageEvent cause = event.getEntity().getLastDamageCause();
             LivingEntity shooter = ((Projectile) killer).getShooter();
             if ( cause instanceof EntityDamageByEntityEvent && shooter instanceof Player ) {
                 killer = (Player)shooter;
             } else {
                 return;
             }
         }
 
         String colorKiller = ColorMeTeaming.getPlayerColor(killer);
 
         // Kill数を加算
         if ( !ColorMeTeaming.ignoreGroups.contains(colorKiller) ) {
             // グループへ加算
             if ( !ColorMeTeaming.killDeathCounts.containsKey(colorKiller) ) {
                 ColorMeTeaming.killDeathCounts.put(colorKiller, new int[3]);
             }
             if ( color.equals(colorKiller) ) // 同じグループだった場合のペナルティ
                 ColorMeTeaming.killDeathCounts.get(colorKiller)[2]++;
             else
                 ColorMeTeaming.killDeathCounts.get(colorKiller)[0]++;
             // ユーザーへ加算
             if ( !ColorMeTeaming.killDeathUserCounts.containsKey(killer) ) {
                 ColorMeTeaming.killDeathUserCounts.put(killer, new int[3]);
             }
             if ( color.equals(colorKiller) ) // 同じグループだった場合のペナルティ
                 ColorMeTeaming.killDeathUserCounts.get(killer)[2]++;
             else
                 ColorMeTeaming.killDeathUserCounts.get(killer)[0]++;
         }
 
         // 色設定を削除する
         if ( ColorMeTeaming.autoColorRemove ) {
             ColorMeTeaming.removePlayerColor(player);
         }
     }
 
     /**
      * Playerがログアウトしたときに発生するイベント
      * @param event
      */
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
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
     }
 }
