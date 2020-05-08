 package vc.pvp.skywars.listeners;
 
import org.bukkit.Bukkit;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
import vc.pvp.skywars.SkyWars;
 import vc.pvp.skywars.controllers.PlayerController;
 import vc.pvp.skywars.game.Game;
 import vc.pvp.skywars.game.GameState;
 import vc.pvp.skywars.player.GamePlayer;
 
 public class EntityListener implements Listener {
 
     @EventHandler(ignoreCancelled = true)
     public void onEntityDamage(EntityDamageEvent event) {
         if (event.getEntityType() != EntityType.PLAYER) {
             return;
         }
 
         Player player = (Player) event.getEntity();
         GamePlayer gamePlayer = PlayerController.get().get(player);
 
         if (!gamePlayer.isPlaying()) {
             return;
         }
 
         Game game = gamePlayer.getGame();
 
         if (game.getState() == GameState.WAITING) {
             event.setCancelled(true);
         } else if (event.getCause() == EntityDamageEvent.DamageCause.FALL && gamePlayer.shouldSkipFallDamage()) {
             gamePlayer.setSkipFallDamage(false);
             event.setCancelled(true);
         } else if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
             event.setCancelled(true);
             gamePlayer.getGame().onPlayerDeath(gamePlayer, null);
         }
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onPlayerDeath(final PlayerDeathEvent event) {
         Player player = event.getEntity();
        final GamePlayer gamePlayer = PlayerController.get().get(player);
 
         if (!gamePlayer.isPlaying()) {
             return;
         }
 
        if (player.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            Bukkit.getScheduler().runTaskLater(SkyWars.get(), new Runnable() {
                @Override
                public void run() {
                    gamePlayer.getGame().onPlayerDeath(gamePlayer, event);
                }
            }, 1L);
        } else {
            gamePlayer.getGame().onPlayerDeath(gamePlayer, event);
        }
     }
 }
