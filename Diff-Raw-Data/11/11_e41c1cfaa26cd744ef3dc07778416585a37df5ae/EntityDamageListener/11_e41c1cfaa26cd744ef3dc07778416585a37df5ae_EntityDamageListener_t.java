 /*
  * Copyright ucchy 2013
  */
 package com.github.ucchyocean.cmt.listener;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 
 import com.github.ucchyocean.cmt.ColorMeTeaming;
 import com.github.ucchyocean.cmt.ColorMeTeamingConfig;
 
 /**
  * @author ucchy
  * 仲間同士の攻撃が発生したかどうかを確認するクラス
  */
 public class EntityDamageListener implements Listener {
 
     /**
      * Entity が Entity に、ダメージを与えたときに発生するイベント。
      * @param event
      */
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
     public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
 
         // 0以下のダメージのイベントは無視する
         if (event.getDamage() <= 0) {
             return;
         }
 
         // 無効状態なら、何もしない
         if (!ColorMeTeamingConfig.isFriendlyFireDisabler) {
             return;
         }
 
         // 加害者と被害者の取得
         Entity attacker = event.getDamager();
         Entity defender = event.getEntity();
 
         // 両方プレイヤーの場合（＝剣や素手などの直接攻撃）
         if ( attacker instanceof Player && defender instanceof Player ) {
 
             // ColorMe のカラーを取得し、同じ色かどうか確認する
             String attackerColor = ColorMeTeaming.getPlayerColor((Player)attacker);
             String defenderColor = ColorMeTeaming.getPlayerColor((Player)defender);
 
            // 同じ色で、ignoreGroupsでなければ、
            if ( attackerColor.equalsIgnoreCase(defenderColor) &&
                    !ColorMeTeamingConfig.ignoreGroups.contains(attackerColor) ) {
                 // 攻撃イベントをキャンセルしちゃう。
                 event.setCancelled(true);
             }
 
         // 加害者が飛来物(Projectile)、被害者がプレイヤーの場合
         } else if ( attacker instanceof Projectile && defender instanceof Player ) {
 
             Projectile projectile = (Projectile)attacker;
             LivingEntity shooter = projectile.getShooter();
 
             // 飛来物を打ったのがプレイヤーなら、
             if ( shooter instanceof Player ) {
 
                 // ColorMe のカラーを取得し、同じ色かどうか確認する
                 String attackerColor = ColorMeTeaming.getPlayerColor((Player)shooter);
                 String defenderColor = ColorMeTeaming.getPlayerColor((Player)defender);
 
                // 同じ色で、ignoreGroupsでなければ、
                if ( attackerColor.equalsIgnoreCase(defenderColor) &&
                        !ColorMeTeamingConfig.ignoreGroups.contains(attackerColor) ) {
                     // 攻撃イベントをキャンセルしちゃう。
                     event.setCancelled(true);
                 }
             }
         }
     }
 }
