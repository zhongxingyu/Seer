 package me.limebyte.battlenight.core.listeners;
 
 import me.limebyte.battlenight.api.BattleNightAPI;
 import me.limebyte.battlenight.api.battle.SpectatorManager;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.inventory.InventoryOpenEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 
 public class SpectatorListener extends APIRelatedListener {
 
     public SpectatorListener(BattleNightAPI api) {
         super(api);
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerMove(PlayerMoveEvent event) {
         Player player = event.getPlayer();
         String name = player.getName();
 
         SpectatorManager manager = getAPI().getSpectatorManager();
 
         if (getAPI().getBattle().containsPlayer(player)) {
             for (String spec : manager.getSpectators()) {
                 Player spectator = Bukkit.getPlayerExact(spec);
                 if (spectator == null) continue;
 
                 if (manager.getTarget(spectator).getName() == name) {
                     spectator.teleport(player);
                 }
             }
         }
 
         if (manager.getSpectators().contains(player.getName())) {
             event.setCancelled(true);
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerInterract(PlayerInteractEvent event) {
         SpectatorManager manager = getAPI().getSpectatorManager();
         Player player = event.getPlayer();
 
         if (manager.getSpectators().contains(player.getName())) {
            event.setCancelled(true);
             manager.cycleTarget(player);
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onInventoryOpen(InventoryOpenEvent event) {
         SpectatorManager manager = getAPI().getSpectatorManager();
 
         if (manager.getSpectators().contains(event.getPlayer().getName())) {
             event.setCancelled(true);
         }
     }
 }
