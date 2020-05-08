 package net.amunak.bukkit.flywithfood;
 
 /**
  * Copyright 2013 Jiří Barouš
  *
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Sound;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.FoodLevelChangeEvent;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerGameModeChangeEvent;
 import org.bukkit.event.player.PlayerItemConsumeEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerToggleFlightEvent;
 
 class FlyWithFoodEventListener implements Listener {
 
     protected FlyWithFood plugin;
     private Log log;
 
     public FlyWithFoodEventListener(FlyWithFood p) {
         this.plugin = p;
         this.log = FlyWithFood.log;
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onPlayerJoin(PlayerJoinEvent e) {
         this.plugin.flyablePlayers.put(e.getPlayer(), new FlyablePlayerRecord());
         this.plugin.checkFlyingCapability(e.getPlayer());
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onPlayerChangedWorld(PlayerChangedWorldEvent e) {
         this.plugin.checkFlyingCapability(e.getPlayer());
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onPlayerRespawn(PlayerRespawnEvent e) {
         this.plugin.checkFlyingCapability(e.getPlayer());
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onPlayerGameModeChange(PlayerGameModeChangeEvent e) {
         this.plugin.checkFlyingCapability(e.getPlayer());
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onPlayerWorldChange(PlayerChangedWorldEvent e) {
         this.plugin.checkFlyingCapability(e.getPlayer());
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onPlayerToggleFlight(PlayerToggleFlightEvent e) {
         this.plugin.checkFlyingCapability(e.getPlayer());
 
         if (e.isFlying() && e.getPlayer().getGameMode().equals(GameMode.SURVIVAL)) {
             if (this.plugin.config.getBoolean("options.drainHunger.enable")
                     && !e.getPlayer().hasPermission("fly.nohunger")
                     && e.getPlayer().getFoodLevel() < this.plugin.hungerMin) {
                 e.setCancelled(true);
                 e.getPlayer().sendMessage(ChatColor.BLUE + "Your food level is under " + ChatColor.DARK_PURPLE + ((double) this.plugin.hungerMin / 2) + ChatColor.BLUE + ". You are too weak to fly.");
             }
             if (!e.getPlayer().getAllowFlight()) {
                 e.setCancelled(true);
             }
         }
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onFoodLevelChange(FoodLevelChangeEvent e) {
         log.fine(e.getEntityType().toString() + " thrown foodlevelchange");
         if (e.getEntity() instanceof Player) {
             this.plugin.foodLevelCheck((Player) e.getEntity(), e.getFoodLevel());
         }
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
     public void onPlayerItemConsume(PlayerItemConsumeEvent e) {
         log.fine("itemConsume fired");
         Player p = e.getPlayer();
         if (!e.isCancelled() && p.getGameMode().equals(GameMode.SURVIVAL)
                 && this.plugin.config.getBoolean("options.limitFoodConsumption.enable")
                 && p.isFlying()
                 && !p.hasPermission("fly.eatanything")
                 && this.plugin.listOfFood.contains(e.getItem().getType().toString())) {
             if (this.plugin.maxFoodEaten == 0) {
                 p.sendMessage(ChatColor.BLUE + "Sorry, you cannot eat while flying.");
                 e.setCancelled(true);
             } else {
                 if (this.plugin.listOfForbiddenFood.contains(e.getItem().getType().toString())) {
                     p.sendMessage(ChatColor.BLUE + "Sorry, you cannot consume this while flying.");
                     e.setCancelled(true);
                 } else if (this.plugin.maxFoodEaten > 0) {
                     if (this.plugin.flyablePlayers.get(p).foodEaten < this.plugin.maxFoodEaten) {
                         this.plugin.flyablePlayers.get(p).foodEaten += 1;
                         p.sendMessage(ChatColor.BLUE + "You can eat " + ChatColor.DARK_PURPLE
                                 + (this.plugin.maxFoodEaten - this.plugin.flyablePlayers.get(p).foodEaten)
                                 + ChatColor.BLUE + " more food during this flight.");
                     } else {
                         p.sendMessage(ChatColor.BLUE + "You have already consumed "
                                 + ChatColor.DARK_PURPLE + this.plugin.maxFoodEaten
                                + ChatColor.BLUE + "piece" + (this.plugin.maxFoodEaten > 1 ? "s" : "")
                                 + " of food during this flight. You can eat no more.");
                         e.setCancelled(true);
                     }
                 }
             }
         }
         log.fine("itemConsume ended for " + p.getName() + " with cancelled: " + e.isCancelled());
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onPlayerMove(PlayerMoveEvent e) {
         //Reset eaten food when grounded
         if (this.plugin.config.getBoolean("options.limitFoodConsumption.enable") && !e.getPlayer().isFlying() && e.getPlayer().isOnGround()) {
             this.plugin.flyablePlayers.get(e.getPlayer()).foodEaten = 0;
         }
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onPlayerQuit(PlayerQuitEvent e) {
         this.plugin.flyablePlayers.remove(e.getPlayer());
     }
 }
