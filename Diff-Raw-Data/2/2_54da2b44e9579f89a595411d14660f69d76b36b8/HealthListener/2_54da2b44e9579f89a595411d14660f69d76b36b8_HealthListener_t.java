 package me.limebyte.battlenight.core.listeners;
 
 import java.util.Set;
 
 import me.limebyte.battlenight.api.BattleNightAPI;
 import me.limebyte.battlenight.api.battle.Battle;
 import me.limebyte.battlenight.core.battle.SimpleBattle;
 import me.limebyte.battlenight.core.battle.SimpleTeamedBattle;
 import me.limebyte.battlenight.core.tosort.ConfigManager;
 import me.limebyte.battlenight.core.tosort.ConfigManager.Config;
 import me.limebyte.battlenight.core.util.BattlePlayer;
 import me.limebyte.battlenight.core.util.PlayerStats;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
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
 import org.bukkit.util.Vector;
 
 public class HealthListener extends APIRelatedListener {
 
     public HealthListener(BattleNightAPI api) {
         super(api);
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onEntityDamage(EntityDamageEvent event) {
         if (!(event.getEntity() instanceof Player)) return;
         Player player = (Player) event.getEntity();
         Player killer = getKiller(event);
         BattleNightAPI api = getAPI();
         Battle battle = api.getBattleManager().getBattle();
 
         if (api.getSpectatorManager().getSpectators().contains(player.getName())) {
             event.setCancelled(true);
             return;
         }
 
         if (!api.getLobby().contains(player) && !battle.containsPlayer(player)) return;
 
         if (event instanceof EntityDamageByEntityEvent) {
             EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
             subEvent.setCancelled(!canBeDamaged(player, killer, battle));
         }
 
         if (event.isCancelled()) return;
         if (battle != null && battle.isInProgress()) {
             if (event.getDamage() >= player.getHealth()) {
                 player.getWorld().playSound(player.getLocation(), Sound.HURT_FLESH, 20f, 1f);
                 event.setCancelled(true);
                 DamageCause cause = event.getCause();
                 if (killer == null) killer = player.getKiller();
                 Accolade accolade = Accolade.get(player, killer);
                 if (accolade != null) {
                     if (accolade == Accolade.BACKSTAB) {
                         api.getMessenger().tellBattle("BACKSTAB!!!");
                     }
                 }
 
                 BattlePlayer bPlayer = BattlePlayer.get(player.getName());
                 PlayerStats stats = bPlayer.getStats();
 
                 bPlayer.kill(killer, cause);
 
                 updateLeaders((SimpleBattle) battle, stats);
             }
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onEntityRegainHealth(EntityRegainHealthEvent event) {
         if (!(event.getEntity() instanceof Player)) return;
         Player player = (Player) event.getEntity();
 
         if (!ConfigManager.get(Config.MAIN).getBoolean("StopHealthRegen", true)) return;
         if (!getAPI().getBattleManager().getBattle().containsPlayer(player)) return;
 
         RegainReason reason = event.getRegainReason();
         if (reason == RegainReason.REGEN || reason == RegainReason.SATIATED) {
             event.setCancelled(true);
         }
     }
 
     private boolean canBeDamaged(Player damaged, Player damager, Battle battle) {
         if (damager == null) return true;
 
         if (getAPI().getSpectatorManager().getSpectators().contains(damager.getName())) return false;
 
         if (getAPI().getLobby().contains(damaged)) return false;
 
         if (battle.containsPlayer(damager)) {
             if (damager == damaged) return true;
 
             if (battle instanceof SimpleTeamedBattle) {
                 if (((SimpleTeamedBattle) battle).areEnemies(damager, damaged)) return true;
                 return ConfigManager.get(Config.MAIN).getBoolean("FriendlyFire", false);
             }
 
             return true;
         }
 
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
 
     private void updateLeaders(SimpleBattle battle, PlayerStats stats) {
         int leadingScore = 0;
         Set<String> leaders = (battle).leadingPlayers;
         Player leader = Bukkit.getPlayerExact(leaders.iterator().next());
 
         if (leader != null) {
             leadingScore = BattlePlayer.get(leader.getName()).getStats().getScore();
         }
 
         if (leadingScore > stats.getScore()) return;
         if (leadingScore < stats.getScore()) leaders.clear();
         leaders.add(leader.getName());
     }
 
     public enum Accolade {
         BACKSTAB;
 
         private static Accolade get(Player player, Player killer) {
             Location playerLoc = player.getLocation();
             Location killerLoc = killer.getLocation();
 
             // Backstab
             Vector playerVec = playerLoc.getDirection();
             Vector killerVec = killerLoc.getDirection();
             float angle = playerVec.angle(killerVec);
            double range = Math.PI / 3;
             if (angle <= range) return BACKSTAB;
 
             return null;
         }
     }
 
 }
