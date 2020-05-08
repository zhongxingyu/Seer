 package com.entrocorp.linearlogic.oneinthegun.events;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.Flying;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.HandlerList;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityRegainHealthEvent;
 import org.bukkit.event.entity.FoodLevelChangeEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 
 import com.entrocorp.linearlogic.oneinthegun.OITG;
 import com.entrocorp.linearlogic.oneinthegun.game.Arena;
 
 public class GameListener implements Listener {
 
     private boolean listening = false;
 
     public boolean isRegistered() {
         return listening;
     }
 
     public void setRegistered(boolean registered) {
         if (listening == registered)
             return;
         if (registered)
             OITG.instance.getServer().getPluginManager().registerEvents(this, OITG.instance);
         else
             HandlerList.unregisterAll(this);
         listening = registered;
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onBuild(BlockPlaceEvent event) {
         Arena arena = OITG.instance.getArenaManager().getArena(event.getPlayer());
         if (arena != null && !arena.isBlockPlacingAllowed()) {
             event.setCancelled(true);
             event.getPlayer().sendMessage(OITG.prefix + ChatColor.RED + "Block placing is disabled in this arena.");
             return;
         }
     }
 
     @EventHandler
     public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
         Player defender = (event.getEntity() instanceof Player ? (Player) event.getEntity() : null),
                 attacker = (event.getDamager() instanceof Player ? (Player) event.getDamager() : null);
         if (defender == null && attacker == null)
             return;
 
         Arena defenderArena = OITG.instance.getArenaManager().getArena(defender),
                 attackerArena = OITG.instance.getArenaManager().getArena(attacker);
         if (defenderArena != null) {
             if (!defenderArena.isIngame()) {
                 event.setCancelled(true);
                 return;
             }
             if (attacker == null) {
                 if (event.getCause().equals(DamageCause.PROJECTILE)) {
                     LivingEntity shooter = ((Projectile) event.getDamager()).getShooter();
                     if (shooter == null)
                         return;
                     if (shooter instanceof Player) {
                         attacker = (Player) shooter;
                         attackerArena = OITG.instance.getArenaManager().getArena(attacker);
                         if (!defenderArena.equals(attackerArena)) {
                             event.setCancelled(true);
                             return;
                         }
                         if (!(event.getDamager() instanceof Arrow))
                             return;
                         event.setCancelled(true);
                         defenderArena.broadcast(ChatColor.GOLD + defender.getName() + ChatColor.GRAY + " was sniped by " +
                                 ChatColor.GOLD + attacker.getName() + ChatColor.GRAY + "!");
                         defenderArena.killPlayer(defender);
                         defenderArena.incrementKills(attacker);
                         return;
                     }
                     if ((shooter instanceof Creature || shooter instanceof Flying) && !defenderArena.isMobCombatAllowed()) {
                         event.setCancelled(true);
                         return;
                     }
                     if (defender.getHealth() - event.getDamage() <= 0.0) {
                         event.setCancelled(true);
                         defenderArena.broadcast(ChatColor.GOLD + defender.getName() + ChatColor.GRAY + " was killed by a " +
                         shooter.getType().toString().toLowerCase().replaceAll("_", " ") + "!");
                         defenderArena.killPlayer(defender);
                     }
                     return;
                 }
                 if (event.getCause().equals(DamageCause.ENTITY_ATTACK) || event.getCause().equals(DamageCause.ENTITY_EXPLOSION) ||
                         event.getCause().equals(DamageCause.WITHER)) {
                     if (!defenderArena.isMobCombatAllowed()) {
                         event.setCancelled(true);
                         return;
                     }
                     if (defender.getHealth() - event.getDamage() <= 0.0) {
                         event.setCancelled(true);
                         defenderArena.broadcast(ChatColor.GOLD + defender.getName() + ChatColor.GRAY + " was killed by a " +
                                 (event.getCause().equals(DamageCause.ENTITY_EXPLOSION) ? "creeper" :
                                 event.getCause().equals(DamageCause.WITHER) ? "wither" :
                                 event.getDamager().getType().toString().toLowerCase().replaceAll("_", " ")) + "!");
                         defenderArena.killPlayer(defender);
                     }
                 }
                 return;
             }
             if (!defenderArena.equals(attackerArena)) {
                 event.setCancelled(true);
                 return;
             }
             if (defender.getHealth() - event.getDamage() <= 0.0) {
                 event.setCancelled(true);
                 defenderArena.broadcast(ChatColor.GOLD + defender.getName() + ChatColor.GRAY + " was killed by " +
                         ChatColor.GOLD + attacker.getName() + ChatColor.GRAY + "!");
                 defenderArena.killPlayer(defender);
                 defenderArena.incrementKills(attacker);
             }
             return;
         }
         if (attackerArena == null)
             return;
         if (defender != null) {
             event.setCancelled(true);
             return;
         }
         if (!attackerArena.isIngame()) {
             event.setCancelled(true);
             return;
         }
         if (!attackerArena.isMobCombatAllowed())
             event.setCancelled(true);
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onEntityRegainHealth(EntityRegainHealthEvent event) {
         if (!(event.getEntity() instanceof Player))
             return;
         Player player = (Player) event.getEntity();
         Arena arena = OITG.instance.getArenaManager().getArena(player);
         if (arena != null && !arena.isHealthRegenAllowed())
             event.setCancelled(true);
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onHunger(FoodLevelChangeEvent event) {
         if (!(event.getEntity() instanceof Player))
             return;
         Player player = (Player) event.getEntity();
         Arena arena = OITG.instance.getArenaManager().getArena(player);
         if (arena != null && !arena.isHungerAllowed())
             event.setCancelled(true);
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onItemDrop(PlayerDropItemEvent event) {
         Arena arena = OITG.instance.getArenaManager().getArena(event.getPlayer());
         if (arena == null || arena.isItemDroppingAllowed())
             return;
         event.setCancelled(true);
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onItemPickup(PlayerPickupItemEvent event) {
         Arena arena = OITG.instance.getArenaManager().getArena(event.getPlayer());
         if (arena == null || arena.isItemPickupAllowed())
             return;
         event.setCancelled(true);
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onRespawn(PlayerRespawnEvent event) {
         Arena arena = OITG.instance.getArenaManager().getArena(event.getPlayer());
         if (arena == null)
             return;
         if (!arena.isIngame()) {
             event.setRespawnLocation(arena.getLobby());
         }
         event.setRespawnLocation(arena.getRandomSpawn());
         arena.incrementDeaths(event.getPlayer());
         arena.armPlayer(event.getPlayer());
     }
 }
