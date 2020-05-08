 /** TheWalls2: The Walls 2 plugin.
   * Copyright (C) 2012  Andrew Stevanus (Hoot215) <hoot893@gmail.com>
   * 
   * This program is free software: you can redistribute it and/or modify
   * it under the terms of the GNU Affero General Public License as published by
   * the Free Software Foundation, either version 3 of the License, or
   * (at your option) any later version.
   * 
   * This program is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty of
   * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   * GNU Affero General Public License for more details.
   * 
   * You should have received a copy of the GNU Affero General Public License
   * along with this program.  If not, see <http://www.gnu.org/licenses/>.
   */
 
 package me.Hoot215.TheWalls2;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.WorldCreator;
 import org.bukkit.entity.Player;
 
 public class TheWalls2World {
 	public static boolean isRestoring = false;
 	
 	public static void reloadWorld(final TheWalls2 plugin) {
 		plugin.getServer().broadcastMessage(ChatColor.AQUA + "[TheWalls2] "
 				+ ChatColor.YELLOW + "World is being unloaded...");
 		isRestoring = true;
 		for (Player player : plugin.getServer().getWorld(TheWalls2.worldName)
 				.getPlayers()) {
 			if (player == null)
 				break;
 			
 			player.kickPlayer("[TheWalls2] You can't be in the world when " +
 					"it unloads! Please re-join in a few seconds.");
 		}
 		for (Player player : plugin.getRespawnQueue().getPlayerList()) {
 			if (player == null)
 				break;
 			
 			player.kickPlayer("[TheWalls2] You can't be in the world when " +
 					"it unloads! Please re-join in a few seconds.");
 		}
 		
 		if (plugin.getServer().unloadWorld(TheWalls2.worldName, false)) {
 			plugin.getServer().broadcastMessage(ChatColor.AQUA + "[TheWalls2] "
 					+ ChatColor.YELLOW + "World is being loaded...");
 			WorldCreator wc = new WorldCreator(TheWalls2.worldName);
 			World world = plugin.getServer().createWorld(wc);
 			world.setAutoSave(false);
 			plugin.getLocationData().setWorld(world);
 			isRestoring = false;
 		}
 		else {
 			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,
 					new Runnable() {
 				public void run() {
 					if (Bukkit.getServer().unloadWorld(TheWalls2.worldName,
 							false)) {
 						WorldCreator wc = new WorldCreator(TheWalls2.worldName);
 						Bukkit.getServer().createWorld(wc).setAutoSave(false);
 					}
 					else {
 						System.out.println("[TheWalls2] " +
 								"The world failed to unload!");
 					}
 					isRestoring = false;
 				}
			}, 20L);
 		}
 	}
 }
