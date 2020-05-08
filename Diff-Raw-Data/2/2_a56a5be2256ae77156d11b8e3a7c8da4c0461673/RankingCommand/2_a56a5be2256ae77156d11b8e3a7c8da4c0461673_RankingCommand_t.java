 package com.titankingdoms.nodinchan.titanchat.command.commands;
 
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 
 import com.titankingdoms.nodinchan.titanchat.channel.Channel;
 import com.titankingdoms.nodinchan.titanchat.channel.ChannelManager;
 import com.titankingdoms.nodinchan.titanchat.command.Command;
 import com.titankingdoms.nodinchan.titanchat.command.CommandID;
 import com.titankingdoms.nodinchan.titanchat.command.CommandInfo;
 
 /*     Copyright (C) 2012  Nodin Chan <nodinchan@live.com>
  * 
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  * 
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  * 
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /**
  * RankingCommand - Command for promotion, demotion and whitelisting on Channels
  * 
  * @author NodinChan
  *
  */
 public class RankingCommand extends Command {
 
 	private ChannelManager cm;
 	
 	public RankingCommand() {
 		this.cm = plugin.getChannelManager();
 	}
 	
 	/**
 	 * Add Command - Whitelists the player for the channel
 	 */
 	@CommandID(name = "Add", triggers = "add")
 	@CommandInfo(description = "Whitelists the player for the channel", usage = "add [player] <channel>")
 	public void add(Player player, String[] args) {
 		if (args.length < 1) { invalidArgLength(player, "Add"); return; }
 		
 		try {
 			if (cm.exists(args[1])) {
 				Channel channel = cm.getChannel(args[1]);
 				
 				if (channel.canRank(player)) {
 					if (plugin.getPlayer(args[0]) != null) {
 						cm.whitelistMember(plugin.getPlayer(args[0]), channel);
 						plugin.sendInfo(player, plugin.getPlayer(args[0]).getDisplayName() + " has been added to the Member List");
 						
 					} else {
 						plugin.sendInfo(player, plugin.getOfflinePlayer(args[0]).getName() + " is offline");
 						cm.whitelistMember(plugin.getOfflinePlayer(args[0]), channel);
 						plugin.sendInfo(player, plugin.getPlayer(args[0]).getDisplayName() + " has been added to the Member List");
 					}
 					
 				} else { plugin.sendWarning(player, "You do not have permission"); }
 				
 			} else { plugin.sendWarning(player, "No such channel"); }
 			
 		} catch (IndexOutOfBoundsException e) {
 			Channel channel = cm.getChannel(player);
 			
 			if (channel == null) {
 				plugin.sendWarning(player, "Specify a channel or join a channel to use this command");
 				return;
 			}
 			
 			if (channel.canRank(player)) {
 				if (plugin.getPlayer(args[0]) != null) {
 					cm.whitelistMember(plugin.getPlayer(args[0]), channel);
 					plugin.sendInfo(player, plugin.getPlayer(args[0]).getDisplayName() + " has been added to the Member List");
 					
 				} else {
 					plugin.sendInfo(player, plugin.getOfflinePlayer(args[0]).getName() + " is offline");
 					cm.whitelistMember(plugin.getOfflinePlayer(args[0]), channel);
					plugin.sendInfo(player, plugin.getOfflinePlayer(args[0]).getName() + " has been added to the Member List");
 				}
 				
 			} else { plugin.sendWarning(player, "You do not have permission"); }
 		}
 	}
 	
 	/**
 	 * Demote Command - Demotes the player of the channel
 	 */
 	@CommandID(name = "Demote", triggers = "demote")
 	@CommandInfo(description = "Demotes the player of the channel", usage = "demote [player] <channel>")
 	public void demote(Player player, String[] args) {
 		if (args.length < 1) { invalidArgLength(player, "Demote"); }
 		
 		try {
 			if (cm.exists(args[1])) {
 				Channel channel = cm.getChannel(args[1]);
 				
 				if (channel.canRank(player)) {
 					if (plugin.getPlayer(args[0]) != null) {
 						Player targetPlayer = plugin.getPlayer(args[0]);
 						
 						if (channel.getAdminList().contains(targetPlayer.getName())) {
 							channel.getAdminList().remove(targetPlayer.getName());
 							channel.save();
 							
 							plugin.sendInfo(targetPlayer, "You have been demoted in " + channel.getName());
 							plugin.sendInfo(channel.getParticipants(), targetPlayer.getDisplayName() + " has been demoted");
 							
 						} else { plugin.sendWarning(player, targetPlayer.getDisplayName() + " is not an Admin"); }
 						
 					} else {
 						OfflinePlayer targetPlayer = plugin.getOfflinePlayer(args[0]);
 						plugin.sendInfo(player, targetPlayer.getName() + " is offline");
 						
 						if (channel.getAdminList().contains(targetPlayer.getName())) {
 							channel.getAdminList().remove(targetPlayer.getName());
 							channel.save();
 							
 							plugin.sendInfo(channel.getParticipants(), targetPlayer.getName() + " has been demoted");
 							
 						} else { plugin.sendWarning(player, targetPlayer.getName() + " is not an Admin"); }
 					}
 					
 				} else { plugin.sendWarning(player, "You do not have permission"); }
 				
 			} else { plugin.sendWarning(player, "No such channel"); }
 			
 		} catch (IndexOutOfBoundsException e) {
 			Channel channel = cm.getChannel(player);
 			
 			if (channel == null) {
 				plugin.sendWarning(player, "Specify a channel or join a channel to use this command");
 				return;
 			}
 			
 			if (channel.canRank(player)) {
 				if (plugin.getPlayer(args[0]) != null) {
 					Player targetPlayer = plugin.getPlayer(args[0]);
 					
 					if (channel.getAdminList().contains(targetPlayer.getName())) {
 						channel.getAdminList().remove(targetPlayer.getName());
 						channel.save();
 						
 						plugin.sendInfo(targetPlayer, "You have been demoted in " + channel.getName());
 						plugin.sendInfo(channel.getParticipants(), targetPlayer.getDisplayName() + " has been demoted");
 						
 					} else { plugin.sendWarning(player, targetPlayer.getDisplayName() + " is not an Admin"); }
 					
 				} else {
 					OfflinePlayer targetPlayer = plugin.getOfflinePlayer(args[0]);
 					plugin.sendInfo(player, targetPlayer.getName() + " not online");
 					
 					if (channel.getAdminList().contains(targetPlayer.getName())) {
 						channel.getAdminList().remove(targetPlayer.getName());
 						channel.save();
 						
 						plugin.sendInfo(channel.getParticipants(), targetPlayer.getName() + " has been demoted");
 						
 					} else { plugin.sendWarning(player, targetPlayer.getName() + " is not an Admin"); }
 				}
 				
 			} else { plugin.sendWarning(player, "You do not have permission"); }
 		}
 	}
 	
 	/**
 	 * Promote Command - Promotes the player of the channel
 	 */
 	@CommandID(name = "Promote", triggers = "promote")
 	@CommandInfo(description = "Promotes the player of the channel", usage = "promote [player] <channel>")
 	public void promote(Player player, String[] args) {
 		if (args.length < 1) { invalidArgLength(player, "Promote"); }
 		
 		try {
 			if (cm.exists(args[1])) {
 				Channel channel = cm.getChannel(args[1]);
 				
 				if (channel.canRank(player)) {
 					if (plugin.getPlayer(args[0]) != null) {
 						Player targetPlayer = plugin.getPlayer(args[0]);
 						
 						if (!channel.getAdminList().contains(player.getName())) {
 							cm.assignAdmin(targetPlayer, channel);
 							plugin.sendInfo(player, targetPlayer.getDisplayName() + " has been promoted");
 							plugin.sendInfo(channel.getParticipants(), targetPlayer.getDisplayName() + " has been promoted");
 							
 						} else { plugin.sendWarning(player, targetPlayer.getDisplayName() + " is already an Admin"); }
 						
 					} else {
 						OfflinePlayer targetPlayer = plugin.getOfflinePlayer(args[0]);
 						plugin.sendInfo(player, targetPlayer.getName() + " is offline");
 						
 						if (!channel.getAdminList().contains(player.getName())) {
 							cm.assignAdmin(targetPlayer, channel);
 							plugin.sendInfo(player, targetPlayer.getName() + " has been promoted");
 							plugin.sendInfo(channel.getParticipants(), targetPlayer.getName() + " has been promoted");
 							
 						} else { plugin.sendWarning(player, targetPlayer.getName() + " is already an Admin"); }
 					}
 					
 				} else { plugin.sendWarning(player, "You do not have permission"); }
 				
 			} else { plugin.sendWarning(player, "No such channel"); }
 			
 		} catch (IndexOutOfBoundsException e) {
 			Channel channel = cm.getChannel(player);
 			
 			if (channel == null) {
 				plugin.sendWarning(player, "Specify a channel or join a channel to use this command");
 				return;
 			}
 			
 			if (channel.canRank(player)) {
 				if (plugin.getPlayer(args[0]) != null) {
 					Player targetPlayer = plugin.getPlayer(args[0]);
 					
 					if (!channel.getAdminList().contains(player.getName())) {
 						cm.assignAdmin(targetPlayer, channel);
 						plugin.sendInfo(player, targetPlayer.getDisplayName() + " has been promoted");
 						plugin.sendInfo(channel.getParticipants(), targetPlayer.getDisplayName() + " has been promoted");
 						
 					} else { plugin.sendWarning(player, targetPlayer.getDisplayName() + " is already an Admin"); }
 					
 				} else {
 					OfflinePlayer targetPlayer = plugin.getOfflinePlayer(args[0]);
 					plugin.sendInfo(player, targetPlayer.getName() + " is offline");
 					
 					if (!channel.getAdminList().contains(player.getName())) {
 						cm.assignAdmin(targetPlayer, channel);
 						plugin.sendInfo(player, targetPlayer.getName() + " has been promoted");
 						plugin.sendInfo(channel.getParticipants(), targetPlayer.getName() + " has been promoted");
 						
 					} else { plugin.sendWarning(player, targetPlayer.getName() + " is already an Admin"); }
 				}
 				
 				
 			} else { plugin.sendWarning(player, "You do not have permission"); }
 		}
 	}
 }
