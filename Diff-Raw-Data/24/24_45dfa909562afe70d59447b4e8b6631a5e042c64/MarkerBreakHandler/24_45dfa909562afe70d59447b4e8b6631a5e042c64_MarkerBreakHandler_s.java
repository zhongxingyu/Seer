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
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 
 /**
  */
 public class MarkerBreakHandler implements Listener {
     @EventHandler
     public void onBlockBreak(BlockBreakEvent event) {
         // Verify that a redstone torch was broken
         if (event.getBlock().getType() != Material.REDSTONE_TORCH_ON && event.getBlock().getType() != Material.REDSTONE_TORCH_OFF) return;
 
         Location lastSurveyMark = STPlugin.instance.markers.getSurveyMark(event.getPlayer());
         if (lastSurveyMark == null) return;
 
         if (lastSurveyMark.equals(event.getBlock().getLocation())) {
             ConfigurationSection config = STPlugin.instance.getConfig();
             SurveyMarkers markers = STPlugin.instance.markers;
 
            event.getPlayer().sendMessage(config.getString(ChatColor.translateAlternateColorCodes('&', "SurveyMarkDestroyed")));
             markers.clearSurveyMark(event.getPlayer());
         }
     }
 }
