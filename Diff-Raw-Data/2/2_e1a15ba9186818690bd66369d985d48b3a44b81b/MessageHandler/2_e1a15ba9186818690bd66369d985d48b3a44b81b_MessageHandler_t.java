 /*
  * MCTrade
  * Copyright (C) 2012 Fogest <http://fogest.net16.net> and contributors
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
 
 package me.fogest.mctrade;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class MessageHandler {
 	private String prefix;
 	private MCTrade plugin;
 	
 	public MessageHandler(String prefix) {
 		this.prefix = prefix;
 		plugin = MCTrade.getPlugin();
 	}
 	public void tellPlayer(Player player, String message) {
 		player.sendMessage(ChatColor.GOLD + prefix + ChatColor.WHITE + message);
 	}
 	public void tellPlayer(Player player, Msg msg){
		player.sendMessage(ChatColor.GOLD + prefix + ChatColor.WHITE + msg.toString());
 	}
 	public void tellAll(String message) {
 		plugin.getServer().broadcastMessage(ChatColor.GOLD + prefix + ChatColor.WHITE + message);
 	}
 	public void info(String message){
 		plugin.getLogger().info(message);
 	}
 	public void warning(String message) {
 		plugin.getLogger().warning(message);
 	}
 	public void severe(String message) {
 		plugin.getLogger().severe(message);
 	}
 }
