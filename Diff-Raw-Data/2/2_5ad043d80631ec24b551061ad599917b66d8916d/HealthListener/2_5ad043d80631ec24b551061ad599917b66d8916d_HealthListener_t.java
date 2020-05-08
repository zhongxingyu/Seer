 package me.limebyte.battlenight.core.listeners;
 
 import me.limebyte.battlenight.api.BattleNightAPI;
 import me.limebyte.battlenight.api.battle.Battle;
 import me.limebyte.battlenight.core.tosort.ConfigManager;
 import me.limebyte.battlenight.core.tosort.ConfigManager.Config;
 import me.limebyte.battlenight.core.util.BattlePlayer;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Sound;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityRegainHealthEvent;
 import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.Vector;
 
 public class HealthListener extends APIRelatedListener {
 
     public HealthListener(BattleNightAPI api) {
         super(api);
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onEntityDamage(EntityDamageEvent event) {
         if (!(event.getEntity() instanceof Player)) return;
         Player player = (Player) event.getEntity();
         Player damager = getKiller(event);
         BattleNightAPI api = getAPI();
         Battle battle = api.getBattle();
 
         BattlePlayer bPlayer = BattlePlayer.get(player.getName());
         if (!bPlayer.isAlive()) {
             event.setCancelled(true);
             return;
         }
 
         if (!api.getLobby().contains(player) && (battle != null && !battle.containsPlayer(player))) return;
 
         if (event instanceof EntityDamageByEntityEvent) {
             EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
             subEvent.setCancelled(!canBeDamaged(player, damager, battle));
         }
 
         if (event.isCancelled()) return;
         if (battle != null && battle.isInProgress()) {
             if (event.getDamage() >= player.getHealth()) {
                 player.getWorld().playSound(player.getLocation(), Sound.HURT_FLESH, 1f, 1f);
                 event.setCancelled(true);
                 DamageCause cause = event.getCause();
                 if (damager == null) damager = player.getKiller();
                 DeathCause accolade = DeathCause.get(player, damager, cause);
 
                 bPlayer.kill(damager, cause, accolade);
             }
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onEntityRegainHealth(EntityRegainHealthEvent event) {
         if (!(event.getEntity() instanceof Player)) return;
         Player player = (Player) event.getEntity();
 
         if (!ConfigManager.get(Config.MAIN).getBoolean("StopHealthRegen", true)) return;
        if (getAPI().getBattle() == null || !getAPI().getBattle().containsPlayer(player)) return;
 
         RegainReason reason = event.getRegainReason();
         if (reason == RegainReason.REGEN || reason == RegainReason.SATIATED) {
             event.setCancelled(true);
         }
     }
 
     private boolean canBeDamaged(Player damaged, Player damager, Battle battle) {
         if (damager == null) return true;
 
         BattlePlayer bDamager = BattlePlayer.get(damager.getName());
         if (!bDamager.isAlive()) return false;
 
         if (getAPI().getLobby().contains(damaged)) return false;
 
         return true;
     }
 
     private Player getKiller(EntityDamageEvent event) {
         Player killer = null;
         if (event instanceof EntityDamageByEntityEvent) {
             Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
             if (damager instanceof Projectile) {
                 LivingEntity shooter = ((Projectile) damager).getShooter();
                 if (shooter instanceof Player) killer = (Player) shooter;
             } else {
                 if (damager instanceof Player) killer = (Player) damager;
             }
         }
         return killer;
     }
 
     public enum DeathCause {
         PUNCH("$k punched $p"),
         STAB("$k stabbed $p."),
         BACKSTAB("$k backstabbed $p."),
         SHOT("$k shot $p.");
 
         private String deathMessage;
 
         private DeathCause(String deathMessage) {
             this.deathMessage = deathMessage;
         }
 
         public String getMessage() {
             return deathMessage;
         }
 
         private static DeathCause get(Player player, Player killer, DamageCause cause) {
             if (cause == DamageCause.ENTITY_ATTACK && killer != null) {
                 ItemStack weapon = killer.getItemInHand();
 
                 if (weapon != null && isSword(weapon.getType())) {
                     // Stab and Backstab
                     Location playerLoc = player.getLocation();
                     Location killerLoc = killer.getLocation();
                     Vector playerVec = playerLoc.getDirection();
                     Vector killerVec = killerLoc.getDirection();
                     float angle = playerVec.angle(killerVec);
                     double range = Math.PI / 3;
                     return angle <= range ? BACKSTAB : STAB;
                 } else if (weapon == null || weapon.getType() == Material.AIR) {
                     // Punch
                     return PUNCH;
                 } else if (cause == DamageCause.PROJECTILE) {
                     return SHOT;
                 }
             }
 
             return null;
         }
 
         private static boolean isSword(Material material) {
             return material.toString().contains("SWORD");
         }
     }
 
 }
