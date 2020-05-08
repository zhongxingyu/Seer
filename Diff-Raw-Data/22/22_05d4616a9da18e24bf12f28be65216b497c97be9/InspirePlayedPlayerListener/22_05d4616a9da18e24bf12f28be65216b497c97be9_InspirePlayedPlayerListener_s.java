 /**
  * InspirePlayed - A Bukkit playtime plugin for Minecraft
  * Copyright (C) 2011 Melvin "VeiN" Chien <melvin.chien@gmail.com>
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.gmail.melvinchien.InspirePlayed;
 
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerListener;
 //import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 public class InspirePlayedPlayerListener extends PlayerListener {
 	private final InspirePlayed plugin;
 
 	public InspirePlayedPlayerListener(InspirePlayed instance) {
 		plugin = instance;
 	}
 
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		String player = event.getPlayer().getName();
 
 		if (plugin.mapTimes.containsKey(player))
 			plugin.mapTimes.get(player).login();
 		else if (plugin.mapTimes.containsKey(player.toLowerCase())) {
 			plugin.mapTimes.put(player, plugin.mapTimes.get(player.toLowerCase()));
 			plugin.mapTimes.get(player).login();
 		} else {
 			InspireTime it = new InspireTime();
 			plugin.mapTimes.put(player, it);
 		}
 	}
 
 	public void onPlayerKick(PlayerKickEvent event) {
 		String player = event.getPlayer().getName();
 		plugin.mapTimes.get(player).logout();
 		plugin.saveData();
 	}
 
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		String player = event.getPlayer().getName();
 		plugin.mapTimes.get(player).logout();
 		plugin.saveData();
 	}
 
 
 
 
 }
