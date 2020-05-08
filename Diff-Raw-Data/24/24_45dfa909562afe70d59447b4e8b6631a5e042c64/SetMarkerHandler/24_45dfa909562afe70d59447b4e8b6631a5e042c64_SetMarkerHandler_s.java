 //    Copyright (C) 2012  Ryan Michela
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
 
 package com.ryanmichela.surveytools;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.RedstoneTorch;
 
 /**
  */
 public class SetMarkerHandler implements Listener {
     @EventHandler(priority = EventPriority.LOW)
     public void onPlayerInteract(PlayerInteractEvent event) {
         if (event.isCancelled()) return;
 
         Player player = event.getPlayer();
 
         // Verify that the player is holding a redstone torch
         ItemStack inHand = player.getItemInHand();
         if (inHand == null) return;
         if (inHand.getType() != Material.REDSTONE_TORCH_OFF && inHand.getType() != Material.REDSTONE_TORCH_ON) return;
 
         // Verify that the player is shift-right-clicking
         if (!player.isSneaking()) return;
         if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
 
         // Verify that the player is interacting with another redstone torch
         if (event.getClickedBlock().getType() != Material.REDSTONE_TORCH_OFF && event.getClickedBlock().getType() != Material.REDSTONE_TORCH_ON) return;
 
         // All checks passed, set the marker
         ConfigurationSection config = STPlugin.instance.getConfig();
         SurveyMarkers markers = STPlugin.instance.markers;
         Location lastSurveyMark = STPlugin.instance.markers.getSurveyMark(player);
         Location clickedBlockLocation = event.getClickedBlock().getLocation();
 
         if (lastSurveyMark == null) {
            player.sendMessage(config.getString(ChatColor.translateAlternateColorCodes('&', "SurveyMarkSetMessage")));
             markers.setSurveyMark(player, clickedBlockLocation);
         } else if (!lastSurveyMark.equals(clickedBlockLocation)) {
            player.sendMessage(config.getString(ChatColor.translateAlternateColorCodes('&', "SurveyMarkReplaceMessage")));
             markers.clearSurveyMark(player);
             markers.setSurveyMark(player, clickedBlockLocation);
         } else if (lastSurveyMark.equals(clickedBlockLocation)) {
            player.sendMessage(config.getString(ChatColor.translateAlternateColorCodes('&', "SurveyMarkCleared")));
             markers.clearSurveyMark(player);
         }
 
         event.setCancelled(true);
     }
 }
