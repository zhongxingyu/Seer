 package com.titankingdoms.nodinchan.titanchat;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 import com.titankingdoms.nodinchan.titanchat.channel.Channel;
 import com.titankingdoms.nodinchan.titanchat.channel.CustomChannel;
 
 /**
  * TitanChatListener - Listeners
  * 
  * @author NodinChan
  *
  */
 public final class TitanChatListener implements Listener {
 
 	private TitanChat plugin;
 	
 	public TitanChatListener(TitanChat plugin) {
 		this.plugin = plugin;
 	}
 	
 	/**
 	 * Listens to the PlayerChatEvent
 	 * 
 	 * @param event PlayerChatEvent
 	 */
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onPlayerChat(PlayerChatEvent event) {
 		Player player = event.getPlayer();
 		String msg = event.getMessage();
 		
 		if (plugin.enableChannels()) {
 			event.setCancelled(true);
 			
 			if (msg.startsWith("@") && !msg.substring(1).startsWith(" ")) {
 				Channel channel = plugin.getChannelManager().getChannel(msg.split(" ")[0].substring(1));
 					
 				if (channel != null) {
 					if (!channel.canAccess(player))
 						return;
 					
 					if (!plugin.hasVoice(player)) {
 						if (plugin.isSilenced()) { plugin.sendWarning(player, "The server is silenced"); return; }
 						if (channel.isSilenced()) { plugin.sendWarning(player, "The channel is silenced"); return; }
 						if (channel.getMuteList().contains(player.getName())) { plugin.sendWarning(player, "You have been muted"); return; }
 						if (plugin.muted(player)) { plugin.sendWarning(player, "You have been muted"); return; }
 					}
 					
 					String message = msg.replace(msg.split(" ")[0] + " ", "");
 					
 					if (channel instanceof CustomChannel) {
 						((CustomChannel) channel).sendMessage(player, ((CustomChannel) channel).format(player, message));
 						return;
 					}
 					
 					message = plugin.getFormatHandler().format(player, channel.getName(), message);
 					channel.sendMessage(player, message);
 					
 				} else { plugin.sendWarning(player, "No such channel"); }
 				
 				return;
 			}
 			
 			Channel channel = plugin.getChannelManager().getChannel(player);
 			
 			if (!plugin.hasVoice(player)) {
 				if (plugin.isSilenced()) { plugin.sendWarning(player, "The server is silenced"); return; }
 				if (channel.isSilenced()) { plugin.sendWarning(player, "The channel is silenced"); return; }
 				if (channel.getMuteList().contains(player.getName())) { plugin.sendWarning(player, "You have been muted"); return; }
 				if (plugin.muted(player)) { plugin.sendWarning(player, "You have been muted"); return; }
 			}
 			
 			if (channel instanceof CustomChannel) {
 				((CustomChannel) channel).sendMessage(player, ((CustomChannel) channel).format(player, msg));
 				return;
 			}
 			
 			String message = plugin.getFormatHandler().format(player, channel.getName(), msg);
 			channel.sendMessage(player, message);
 			
 		} else {
 			event.setFormat(plugin.getFormatHandler().format(player));
 			event.setMessage(plugin.getFormatHandler().colourise(msg));
 		}
 		
 		plugin.getAddonManager().chatMade(player.getName(), msg);
 	}
 	
 	/**
 	 * Listens to the PlayerJoinEvent
 	 * 
 	 * @param event PlayerJoinEvent
 	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		if (!plugin.enableChannels())
 			return;
 		
 		if (plugin.getChannelManager().getChannel(event.getPlayer()) != null)
 			return;
 		
 		Channel channel = plugin.getChannelManager().getSpawnChannel(event.getPlayer());
 		channel.join(event.getPlayer());
 		
 		if (plugin.isSilenced())
 			plugin.sendWarning(event.getPlayer(), "All channels are silenced");
 		else if (channel.isSilenced())
 			plugin.sendWarning(event.getPlayer(), channel.getName() + " is silenced");
 	}
 }
