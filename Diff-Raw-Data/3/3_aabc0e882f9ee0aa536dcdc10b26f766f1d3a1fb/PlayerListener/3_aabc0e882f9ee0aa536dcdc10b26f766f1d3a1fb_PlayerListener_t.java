 /*
  * ExtraAuth - Extra authentication for bukkit, for accessing account or other plugins (which uses my API)
  * Copyright (C) 2013 Dan Printzell
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 
 package me.wildn00b.extraauth.listener;
 
 import java.util.HashMap;
 import java.util.UUID;
 
 import me.wildn00b.extraauth.ExtraAuth;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockIgniteEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityCombustByBlockEvent;
 import org.bukkit.event.entity.EntityCombustByEntityEvent;
 import org.bukkit.event.entity.EntityCombustEvent;
 import org.bukkit.event.entity.EntityDamageByBlockEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityTargetEvent;
 import org.bukkit.event.entity.FoodLevelChangeEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 
 public class PlayerListener implements Listener {
 
   public HashMap<UUID, Long> messageTimeout = new HashMap<UUID, Long>();
 
   private final ExtraAuth extraauth;
 
   public PlayerListener(ExtraAuth extraauth) {
     this.extraauth = extraauth;
   }
 
   public boolean isFrozen(Player player) {
     if (extraauth.DB.IsAuth(player))
       return false;
     if (!(Boolean) extraauth.Settings._("FreezePlayer", true))
       return false;
 
     if (messageTimeout.containsKey(player.getUniqueId())) {
       final long time = System.currentTimeMillis()
           - messageTimeout.get(player.getUniqueId()).longValue();
       if (time >= 2000) {
         player.sendMessage(ChatColor.YELLOW + "[ExtraAuth] " + ChatColor.GOLD
             + extraauth.Lang._("FreezeMessage"));
         messageTimeout.put(player.getUniqueId(), time);
       }
     } else {
       player.sendMessage(ChatColor.YELLOW + "[ExtraAuth] " + ChatColor.GOLD
           + extraauth.Lang._("FreezeMessage"));
       messageTimeout.put(player.getUniqueId(), System.currentTimeMillis());
     }
 
     return true;
   }
 
   @EventHandler(priority = EventPriority.HIGHEST)
   public void onBlockBreak(BlockBreakEvent event) {
     if (!event.isCancelled() && event.getPlayer() != null
         && isFrozen(event.getPlayer()))
       event.setCancelled(true);
   }
 
   @EventHandler(priority = EventPriority.HIGHEST)
   public void onBlockIgnite(BlockIgniteEvent event) {
     if (!event.isCancelled() && event.getPlayer() != null
         && isFrozen(event.getPlayer()))
       event.setCancelled(true);
   }
 
   @EventHandler(priority = EventPriority.HIGHEST)
   public void onBlockPlace(BlockPlaceEvent event) {
     if (!event.isCancelled() && event.getPlayer() != null
         && isFrozen(event.getPlayer()))
       event.setCancelled(true);
   }
 
   @EventHandler(priority = EventPriority.HIGHEST)
   public void onEntityCombust(EntityCombustEvent event) {
     if (event.getEntity() != null && event.getEntity() instanceof Player
         && !event.isCancelled()) {
       final Player player = (Player) event.getEntity();
       if (isFrozen(player))
         event.setCancelled(true);
     }
   }
 
   @EventHandler(priority = EventPriority.HIGHEST)
   public void onEntityCombustByBlock(EntityCombustByBlockEvent event) {
     if (event.getEntity() != null && event.getEntity() instanceof Player
         && !event.isCancelled()) {
       final Player player = (Player) event.getEntity();
       if (isFrozen(player))
         event.setCancelled(true);
     }
   }
 
   @EventHandler(priority = EventPriority.HIGHEST)
   public void onEntityCombustByEntity(EntityCombustByEntityEvent event) {
     if (event.getEntity() != null && event.getEntity() instanceof Player
         && !event.isCancelled()) {
       final Player player = (Player) event.getEntity();
       if (isFrozen(player))
         event.setCancelled(true);
     }
   }
 
   @EventHandler(priority = EventPriority.HIGHEST)
   public void onEntityDamage(EntityDamageEvent event) {
     if (event.getEntity() != null && event.getEntity() instanceof Player
         && !event.isCancelled()) {
       final Player player = (Player) event.getEntity();
       if (isFrozen(player)) {
         event.setDamage(0);
         event.setCancelled(true);
       }
     }
   }
 
   @EventHandler(priority = EventPriority.HIGHEST)
   public void onEntityDamageByBlock(EntityDamageByBlockEvent event) {
     if (event.getEntity() != null && event.getEntity() instanceof Player
         && !event.isCancelled()) {
       final Player player = (Player) event.getEntity();
       if (isFrozen(player)) {
         event.setDamage(0);
         event.setCancelled(true);
       }
     }
   }
 
   @EventHandler(priority = EventPriority.HIGHEST)
   public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
     if (event.getEntity() != null && event.getEntity() instanceof Player
         && !event.isCancelled()) {
       final Player player = (Player) event.getEntity();
       if (isFrozen(player)) {
         event.setDamage(0);
         event.setCancelled(true);
       }
     }
   }
 
   @EventHandler(priority = EventPriority.HIGHEST)
   public void onEntityTarget(EntityTargetEvent event) {
    if (event.getTarget() != null
        && event.getTarget().getType() == EntityType.PLAYER) {
       final Player player = (Player) event.getTarget();
       if (isFrozen(player) && (!event.isCancelled()))
         event.setCancelled(true);
 
     }
   }
 
   @EventHandler(priority = EventPriority.HIGHEST)
   public void onFoodLevelChange(FoodLevelChangeEvent event) {
     if (event.getEntity() != null && event.getEntity() instanceof Player
         && !event.isCancelled() && isFrozen((Player) event.getEntity()))
       event.setCancelled(true);
   }
 
   @EventHandler(priority = EventPriority.HIGHEST)
   public void onPlayerChat(AsyncPlayerChatEvent event) {
     if (!event.isCancelled() && event.getPlayer() != null)
       if (extraauth.DB.Contains(event.getPlayer().getName()))
         if (!event.getMessage().startsWith("/auth")
             && !event.getMessage().startsWith("/extraauth"))
           if ((Boolean) extraauth.Settings._("BlockChat", true)) {
             if (isFrozen(event.getPlayer()))
               event.getPlayer().sendMessage(
                   ChatColor.YELLOW + "[ExtraAuth] " + ChatColor.GOLD
                       + extraauth.Lang._("FreezeMessage"));
             event.setCancelled(true);
           }
   }
 
   @EventHandler(priority = EventPriority.HIGHEST)
   public void onPlayerInteract(PlayerInteractEvent event) {
     if (!event.isCancelled() && event.getPlayer() != null
         && isFrozen(event.getPlayer()))
       event.setCancelled(true);
   }
 
   @EventHandler(priority = EventPriority.MONITOR)
   public void onPlayerLogin(PlayerLoginEvent event) {
     if (event.getResult() == Result.ALLOWED && event.getPlayer() != null) {
       extraauth.DB.Connecting(event.getPlayer(), event.getKickMessage());
       event.getPlayer().sendMessage(
           ChatColor.YELLOW + "[ExtraAuth] " + ChatColor.GOLD
               + extraauth.Lang._("FreezeMessage"));
     }
   }
 
   @EventHandler(priority = EventPriority.HIGHEST)
   public void onPlayerMove(PlayerMoveEvent event) {
     if (isFrozen(event.getPlayer()) && event.getPlayer() != null) {
       final Location loc = event.getFrom();
       loc.setPitch(event.getTo().getPitch());
       loc.setYaw(event.getTo().getYaw());
       event.setTo(loc);
     }
   }
 
   @EventHandler(priority = EventPriority.MONITOR)
   public void onPlayerQuit(PlayerQuitEvent event) {
     extraauth.DB.Disconnect(event.getPlayer());
   }
 
   @EventHandler(priority = EventPriority.HIGHEST)
   public void onPlayerTeleport(PlayerTeleportEvent event) {
     if (isFrozen(event.getPlayer()))
       event.setTo(event.getFrom());
   }
 }
