 //    Copyright (C) 2011  Ryan Michela
 //
 //    This program is free software: you can redistribute it and/or modify
 //    it under the terms of the GNU General Public License as published by
 //    the Free Software Foundation, either version 3 of the License, or
 //    (at your option) any later version.
 //
 //    This program is distributed in the hope that it will be useful,
 //    but WITHOUT ANY WARRANTY; without even the implied warranty of
 //    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //    GNU General Public License for more details.
 //
 //    You should have received a copy of the GNU General Public License
 //    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
 package com.ryanmichela.tsZombiePlague;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityRegainHealthEvent;
 import org.bukkit.event.entity.FoodLevelChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerItemBreakEvent;
 import org.bukkit.event.player.PlayerItemHeldEvent;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class SugarEatListener implements Listener {
 
     private ZombieDamage damageTracker;
     private Plugin plugin;
     private Map<String, Boolean> eaters = new HashMap<String, Boolean>();
 
     public SugarEatListener(ZombieDamage damageTracker, Plugin plugin) {
         this.damageTracker = damageTracker;
         this.plugin = plugin;
     }
 
     @EventHandler
     public void onPlayerInteract(PlayerInteractEvent event) {
         int antidoteId = plugin.getConfig().getInt("antidoteId");
 
         // First check to see if the player has eaten cake
         if (antidoteId == Material.CAKE_BLOCK.getId() &&
                 event.getAction() == Action.RIGHT_CLICK_BLOCK && event.hasBlock() &&
                 event.getClickedBlock().getType() == Material.CAKE_BLOCK &&
                 event.getPlayer().getFoodLevel() < 20) {
             damageTracker.reduceDamage(event.getPlayer());
             return;
         }
 
        // If the player has begun to eat an edible item, note it in the eaters collection
        if (event.getItem() != null &&
                antidoteId == event.getItem().getTypeId() &&
                 event.getItem().getType().isEdible() &&
                 (event.getAction() == Action.RIGHT_CLICK_AIR ||
                 event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
             eaters.put(event.getPlayer().getName(), Boolean.TRUE);
             return;
         }
 
        // If the player has begun to eat an inedible item, note it in the eaters collection
        if (event.getItem() != null &&
                antidoteId != event.getItem().getTypeId() &&
                 event.getItem().getType().isEdible() &&
                 (event.getAction() == Action.RIGHT_CLICK_AIR ||
                 event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
             eaters.put(event.getPlayer().getName(), Boolean.FALSE);
             return;
         }
     }
 
     @EventHandler
     public void onFoodLevelChangeEvent(FoodLevelChangeEvent event) {
         Player player = (Player)event.getEntity();
         if (eaters.containsKey(player.getName())) {
             int deltaFood = event.getFoodLevel() - player.getFoodLevel();
             if (deltaFood > 0 && (eaters.get(player.getName()) == Boolean.TRUE)) { //player gained food
                 damageTracker.reduceDamage(player);
                 eaters.put(player.getName(), Boolean.FALSE);
             }
         }
     }
 }
