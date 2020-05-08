 package me.limebyte.battlenight.core.listeners;
 
 import me.limebyte.battlenight.api.BattleNightAPI;
 import me.limebyte.battlenight.api.battle.Battle;
 import me.limebyte.battlenight.api.battle.TeamedBattle;
 import me.limebyte.battlenight.core.util.config.ConfigManager;
 import me.limebyte.battlenight.core.util.config.ConfigManager.Config;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityRegainHealthEvent;
 import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
 
 public class HealthListener implements Listener {
 
     private BattleNightAPI api;
 
     public HealthListener(BattleNightAPI api) {
         this.api = api;
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onEntityRegainHealth(EntityRegainHealthEvent event) {
         if (!(event.getEntity() instanceof Player)) return;
         Player player = (Player) event.getEntity();
 
         if (!ConfigManager.get(Config.MAIN).getBoolean("StopHealthRegen", true)) return;
         if (!api.getBattle().containsPlayer(player)) return;
 
         RegainReason reason = event.getRegainReason();
         if (reason == RegainReason.REGEN || reason == RegainReason.SATIATED) {
             event.setCancelled(true);
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onEntityDamage(EntityDamageEvent event) {
         if (!(event.getEntity() instanceof Player)) return;
         Player player = (Player) event.getEntity();
 
         if (event instanceof EntityDamageByEntityEvent) {
             EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
             Battle battle = api.getBattle();
             String name = player.getName();
 
             if (battle.getSpectators().contains(name)) {
                 event.setCancelled(true);
             }
 
             if (!battle.containsPlayer(player)) return;
 
             subEvent.setCancelled(!canBeDamaged(player, battle, subEvent));
 
         }
     }
 
     private boolean canBeDamaged(Player damaged, Battle battle, EntityDamageByEntityEvent event) {
         Entity eDamager = event.getDamager();
         Player damager;
 
         if (eDamager instanceof Projectile) {
             if (checkCrashBug(damaged, event)) return false;
 
             LivingEntity shooter = ((Projectile) eDamager).getShooter();
             if (shooter instanceof Player) {
                 damager = (Player) shooter;
             } else return true;
         } else {
             if (eDamager instanceof Player) {
                 damager = (Player) eDamager;
             } else return true;
         }
 
         if (battle.containsPlayer(damager)) {
             if (!battle.isInProgress()) return false;
             if (damager == damaged) return true;
 
             if (battle instanceof TeamedBattle) {
                 if (((TeamedBattle) battle).areEnemies(damager, damaged)) return true;
             }
 
            return ConfigManager.get(Config.MAIN).getBoolean("FriendlyFire", false);
         }
 
         return true;
     }
 
     private boolean checkCrashBug(Player damaged, EntityDamageByEntityEvent event) {
         // if (event.getDamage() >= damaged.getHealth()) return true;
         return false;
     }
 
 }
