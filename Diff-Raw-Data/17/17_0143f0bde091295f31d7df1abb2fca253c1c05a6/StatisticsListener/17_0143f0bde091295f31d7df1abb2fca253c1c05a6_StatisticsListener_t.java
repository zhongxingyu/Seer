 package fr.aumgn.diamondrush.listeners;
 
import org.bukkit.entity.AnimalTamer;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 
 import fr.aumgn.diamondrush.DiamondRush;
 import fr.aumgn.diamondrush.event.game.DRGameStartEvent;
 import fr.aumgn.diamondrush.event.players.DRPlayerJoinEvent;
 import fr.aumgn.diamondrush.event.team.DRTotemBreakEvent;
 import fr.aumgn.diamondrush.game.Game;
 import fr.aumgn.diamondrush.game.GameStatistics;
 import fr.aumgn.diamondrush.game.Team;
 
 public class StatisticsListener implements Listener {
 
     private final Game game;
     private final GameStatistics statistics;
 
     public StatisticsListener(DiamondRush dr) {
         this.game = dr.getGame();
         this.statistics = game.getStatistics();
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onGameStart(DRGameStartEvent event) {
         for (Team team : game.getTeams()) {
             statistics.initTeam(team);
         }
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onPlayerJoin(DRPlayerJoinEvent event) {
         statistics.initPlayer(event.getPlayer());
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onPVPKill(EntityDeathEvent event) {
         LivingEntity targetEntity = event.getEntity();
         if (!(targetEntity instanceof Player)
                 || !(targetEntity.getLastDamageCause() instanceof EntityDamageByEntityEvent)) {
             return;
         }
 
         Entity damagerEntity = ((EntityDamageByEntityEvent) targetEntity.
                 getLastDamageCause()).getDamager();

        if (damagerEntity instanceof Projectile) {
            damagerEntity = ((Projectile) damagerEntity).getShooter();
        }

        if (damagerEntity instanceof Tameable) {
            AnimalTamer owner = ((Tameable) damagerEntity).getOwner();
            if (owner instanceof Entity) {
                damagerEntity = (Entity) owner;
            }
        }

         if (!(damagerEntity instanceof Player)) {
             return;
         }
 
         Player target = (Player) targetEntity;
         Player damager = (Player) damagerEntity;
         if (game.contains(target) && game.contains(damager)) {
             Team targetTeam = game.getTeam(target);
             Team damagerTeam = game.getTeam(damager);
 
             statistics.registerkill(damagerTeam, damager, targetTeam, target);
         }
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onTotemBlockBreak(DRTotemBreakEvent event) {
         Player player = event.getPlayer();
         statistics.registerTotemBlockBreak(event.getGame().getTeam(player), player);
     }
 }
