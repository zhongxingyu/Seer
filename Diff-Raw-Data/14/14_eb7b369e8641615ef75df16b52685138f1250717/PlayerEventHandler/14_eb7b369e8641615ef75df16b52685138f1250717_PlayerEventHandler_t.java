 /*
     ChatParty Plugin for Minecraft Bukkit Servers
     Copyright (C) 2013 Felix Schmidt
     
     This file is part of ChatParty.
 
     ChatParty is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     ChatParty is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with ChatParty.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.github.schmidtbochum.chatparty;
 
 import java.util.Set;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.metadata.FixedMetadataValue;
 
 public class PlayerEventHandler implements Listener
 {
 	private ChatPartyPlugin plugin;
 	
 	public PlayerEventHandler(ChatPartyPlugin plugin) 
 	{
 		this.plugin = plugin;
 	}
 	
 	//when a player successfully joins the server...
 	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
 	void onPlayerJoin(PlayerJoinEvent event) 
 	{
 		Player player = event.getPlayer();
 		
 		plugin.registerSpy(player);
 		
 		Party party = plugin.getPlayerParty(player);
 		
 		if(party == null)
 		{
 			player.removeMetadata("party", plugin);
 			return;
 		}
 		
 		player.setMetadata("party", new FixedMetadataValue(plugin, party.name));
 		
 		if(party.leaders.contains(player.getName())) 
 		{
 			player.setMetadata("isPartyLeader", new FixedMetadataValue(plugin, true));
 		} else {
 			player.removeMetadata("isPartyLeader", plugin);
 		}
 		
 		party.activePlayers.add(player);
 		
 	}
 	
 	//when a player quits...
 	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
 	void onPlayerQuit(PlayerQuitEvent event) 
 	{
 		Player player = event.getPlayer();
 		if(player.hasMetadata("party")) 
 		{
 			String partyName = player.getMetadata("party").get(0).asString();
 			Party party = plugin.loadParty(partyName);
 			
 			party.activePlayers.remove(player);
 			
 			player.removeMetadata("party", plugin);
 			player.removeMetadata("isPartyLeader", plugin);
 		}
 		plugin.unregisterSpy(player);
 	}
 	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
 	void onPlayerChat(AsyncPlayerChatEvent event) 
 	{
 		Player player = event.getPlayer();
 		
 		if(player.hasMetadata("ignore")) 
 		{
 			player.removeMetadata("ignore", plugin);
 			return;
 		}
 		
 		if(player.hasMetadata("partyToggle")  && player.hasMetadata("party"))
 		{
 			String message = event.getMessage();
 
 			String partyName = player.getMetadata("party").get(0).asString();
 			Party party = plugin.loadParty(partyName);
 			
 			party.sendPlayerMessage(player, message);
 			plugin.sendSpyChatMessage(party, player, message);
 			
 			event.setCancelled(true);
 		}
 		else if(player.hasMetadata("globalChatToggle"))
 		{
 			plugin.sendMessage(player, "Message cancelled. Type /chat to enable the global chat.");
 			
 			event.setCancelled(true);
 		}
 		else
 		{
 			Set<Player> recipients = event.getRecipients();
 			
			/* Set iterator */
			Iterator<Player> recipientIterator = recipients.iterator();
			Player playerInstance = null;
			
			while(recipientIterator.hasNext()) {
				playerInstance = recipientIterator.next();
				if (playerInstance.hasMetadata("globalChatToggle")) {
					/* Remove an objet from a set with the iterator */
					recipientIterator.remove();
 				}
 			}
 		}
 	}
 }
