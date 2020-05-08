 package me.supermaxman.xestats;
 
 import me.supermaxman.xestats.utils.MySQLUtils;
 
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityShootBowEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 public class XeStatsListener implements Listener {
     final XeStats plugin;
 
     public XeStatsListener(XeStats plugin) {
         this.plugin = plugin;
     }
 
 
     @EventHandler
     public void onJoin(PlayerJoinEvent event) {
         XeStats.qm.handleJoin(event.getPlayer());
     }
 
     @EventHandler
     public void onDie(PlayerDeathEvent event) {
         Player player = event.getEntity();
         MySQLUtils.addDeath(player);
         if (player.getKiller() != null) {
             Player killer = event.getEntity().getKiller();
             MySQLUtils.addKill(killer);
             double newRange = killer.getLocation().distance(player.getLocation());
             MySQLUtils.checkFarKill(killer, newRange);
 
 
         }
     }
 
     @EventHandler
     public void onQuit(PlayerQuitEvent event) {
         Player player = event.getPlayer();
         XeStats.qm.handleQuit(player);
     }
 
     @EventHandler
     public void onHit(EntityDamageByEntityEvent event) {
         if (event.getDamager() instanceof Player) {
             Player player = (Player) event.getDamager();
             MySQLUtils.addHit(player);
             MySQLUtils.addSwing(player);
         }
     }
 
 
     @EventHandler
     public void onSwing(PlayerInteractEvent event) {
         if (event.getAction() == Action.LEFT_CLICK_AIR) {
             Player player = event.getPlayer();
             MySQLUtils.addSwing(player);
         }
     }
 
     @EventHandler
     public void onShoot(EntityShootBowEvent event) {
         if (event.getEntity() instanceof Player) {
             Player player = (Player) event.getEntity();
             MySQLUtils.addSwing(player);
         }
     }
 
     @EventHandler
     public void onHitByArrow(EntityDamageEvent event) {
         if (event.getCause() == DamageCause.PROJECTILE) {
             if (((EntityDamageByEntityEvent) event).getDamager() instanceof Arrow) {
                 if (((Projectile) ((EntityDamageByEntityEvent) event).getDamager()).getShooter() instanceof Player) {
                     Player player = (Player) ((Projectile) ((EntityDamageByEntityEvent) event).getDamager()).getShooter();
                     MySQLUtils.addHit(player);
                 }
             }
         }
     }
 
 
 }
