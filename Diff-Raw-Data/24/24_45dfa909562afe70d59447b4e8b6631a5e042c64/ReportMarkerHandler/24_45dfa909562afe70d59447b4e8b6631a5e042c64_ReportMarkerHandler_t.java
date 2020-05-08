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
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.RedstoneTorch;
 
 /**
  */
 public class ReportMarkerHandler implements Listener {
     @EventHandler
     public void onPlayerInteract(PlayerInteractEvent event) {
         if (event.isCancelled()) return;
 
         Player player = event.getPlayer();
         ConfigurationSection config = STPlugin.instance.getConfig();
 
         // Verify that the player is holding a redstone torch
         ItemStack inHand = player.getItemInHand();
         if (inHand == null) return;
         if (inHand.getType() != Material.REDSTONE_TORCH_OFF && inHand.getType() != Material.REDSTONE_TORCH_ON) return;
 
         // Verify that the player is shift-right-clicking
         if (!player.isSneaking()) return;
         if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
 
         // Verify that the player has an active survey marker
         Location lastSurveyMarkLocation = STPlugin.instance.markers.getSurveyMark(player);
         if (lastSurveyMarkLocation == null) {
             player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("SurveyMarkNotSet")));
             event.setCancelled(true);
             return;
         }
 
         // Verify that the player did not shift-left-click the survey marker
         Location clickedBlockLocation = event.getClickedBlock().getLocation();
         if (lastSurveyMarkLocation.equals(clickedBlockLocation)) {
             event.setCancelled(true);
             return;
         }
 
         // Calculate survey relative to the block the survey marker is attached to
         Block lastSurveyMarkBlock = player.getWorld().getBlockAt(lastSurveyMarkLocation);
        RedstoneTorch lastSurveyMarkBlockData = (RedstoneTorch)lastSurveyMarkBlock.getState().getData();
        BlockFace attachedFace = lastSurveyMarkBlockData.getAttachedFace();
         Location torchAttachedToLocation = lastSurveyMarkBlock.getRelative(attachedFace).getLocation();
 
         // All checks passed, print out the survey report
         int dx = clickedBlockLocation.getBlockX() - torchAttachedToLocation.getBlockX(); //pos=E, neg=W
         int dy = clickedBlockLocation.getBlockY() - torchAttachedToLocation.getBlockY(); //pos=U, neg=D
         int dz = torchAttachedToLocation.getBlockZ() - clickedBlockLocation.getBlockZ(); //pos=N, neg=S
 
         String ew = null;
         String ud = null;
         String ns = null;
 
         if (dx > 0) ew = "East";
         if (dx < 0) ew = "West";
         if (dy > 0) ud = "Up";
         if (dy < 0) ud = "Down";
         if (dz > 0) ns = "North";
         if (dz < 0) ns = "South";
 
         if (dx == 0 && dz == 0) ud = "Due" + ud;
         if (dx == 0 && dy == 0) ns = "Due" + ns;
         if (dy == 0 && dz == 0) ew = "Due" + ew;
 
         player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("SurveyReportHeader")));
         String reportLine = config.getString("ReportLine");
         if (ns != null) {
             player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(reportLine, Math.abs(dz), config.getString(ns))));
         }
         if (ew != null) {
             player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(reportLine, Math.abs(dx), config.getString(ew))));
         }
         if (ud != null) {
             player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(reportLine, Math.abs(dy), config.getString(ud))));
         }
 
         event.setCancelled(true);
     }
 }
