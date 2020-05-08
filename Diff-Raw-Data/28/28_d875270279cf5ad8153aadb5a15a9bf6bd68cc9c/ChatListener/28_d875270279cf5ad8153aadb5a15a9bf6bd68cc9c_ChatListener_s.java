 package me.cyberkitsune.prefixchat;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChatEvent;
 
 public class ChatListener implements Listener {
 
 	private KitsuneChat plugin;
 	private KitsuneChatUtils util;
 
 	public ChatListener(KitsuneChat plugin) {
 		this.plugin = plugin;
 		util = new KitsuneChatUtils(plugin);
 	}
 
 	@EventHandler
 	public void playerChat(PlayerChatEvent evt) {
 		evt.setCancelled(true);
 		String message = KitsuneChatUtils.colorizeString(evt.getMessage());
 		boolean emote = false;
 		for(String str : plugin.prefixes) {
 			if(message.startsWith(str+plugin.getConfig().getString("emote.prefix"))) {
 				emote = true;
 			}
 		}
 		if(message.startsWith(plugin.getConfig().getString("emote.prefix"))) {
 			emote = true;
 		}
 		if(emote) {
 			message = message.replaceFirst("\\"+plugin.getConfig().getString("emote.prefix"), "");
 		}
 
 		if (evt.getMessage().startsWith(plugin.getConfig().getString("global.prefix"))) {
 			evt.setCancelled(false); // We don't need to cancel an event that goes to everyone. Let vanilla handle it.
 			evt.setFormat(plugin.util.formatChatPrefixes(message, plugin.getConfig().getString(emote ? "global.meformat" : "global.sayformat"), evt));
 		} else if (evt.getMessage().startsWith(plugin.getConfig().getString("world.prefix"))) {
 			List<Player> worldPlayers = evt.getPlayer().getWorld().getPlayers();
 			for (Player plr : worldPlayers) {
 				plr.sendMessage(util.formatChatPrefixes(message, plugin.getConfig().getString(emote ? "world.meformat" : "world.sayformat"), evt));
 			}
 			plugin.mcLog.info(util.formatChatPrefixes(message, plugin.getConfig().getString(emote ? "world.meformat" : "world.sayformat"), evt));
 		} else if (evt.getMessage().startsWith(
 				plugin.getConfig().getString("admin.prefix"))) {
 			if (evt.getPlayer().hasPermission("kitsunechat.adminchat")) {
 				for (Player plr : plugin.getServer().getOnlinePlayers()) {
 					if (plr.hasPermission("kitsunechat.adminchat")) {
 						plr.sendMessage(util.formatChatPrefixes(message, plugin.getConfig().getString(emote ? "admin.meformat" : "admin.sayformat"), evt));
 					}
 				}
 				plugin.mcLog.info(util.formatChatPrefixes(message, plugin.getConfig().getString(emote ? "admin.meformat" : "admin.sayformat"), evt));
 			} else {
 				evt.getPlayer().sendMessage(ChatColor.RED+ "You do not have permissions to use admin chat.");
 			}
 		} else if (evt.getMessage().startsWith(
 				plugin.getConfig().getString("party.prefix"))) {
 			if (plugin.party.isInAParty(evt.getPlayer())) {
 				Set<Player> channelPlayers = plugin.party
 						.getPartyMembers(plugin.party.getPartyName(evt
 								.getPlayer()));
 				for (Player plr : channelPlayers) {
 					plr.sendMessage(util.formatChatPrefixes(message, plugin.getConfig().getString(emote ? "party.meformat" : "party.sayformat"), evt));
 				}
 				plugin.mcLog.info(util.formatChatPrefixes(message, plugin.getConfig().getString(emote ? "party.meformat" : "party.sayformat"), evt));
 			} else {
 				evt.getPlayer()
 						.sendMessage(
 								ChatColor.YELLOW
 										+ "[KitsuneChat] You are not currently in a channel.");
 			}
 
 		} else if ((evt.getMessage().startsWith(plugin.getConfig().getString(
 				"local.prefix")))) {
 			Set<Player> local = KitsuneChatUtils.getNearbyPlayers(plugin
 					.getConfig().getInt("local.radius"), evt.getPlayer());
 			for (Player plr : local) {
 				plr.sendMessage(util.formatChatPrefixes(message, plugin.getConfig().getString(emote ? "local.meformat" : "local.sayformat"), evt));
 			}
 			plugin.mcLog.info(util.formatChatPrefixes(message, plugin.getConfig().getString(emote ? "local.meformat" : "local.sayformat"), evt));
 			if(local.size() <= 1) {
 				if(!evt.getPlayer().hasPermission("kitsunechat.nodefault.global")) {
 					evt.getPlayer().sendMessage(ChatColor.GRAY+"(Nobody can hear you, try defaulting to global chat with /kc "+plugin.getConfig().getString("global.prefix")+")");
 				} else {
 					evt.getPlayer().sendMessage(ChatColor.GRAY+"(Nobody can hear you, try talking globally by starting your message with "+plugin.getConfig().getString("global.prefix")+")");
 				}
 			}
 
 		} else { //Default chat
 			List<String> prefixes = Arrays.asList(plugin.getConfig().getString("global.prefix"), plugin.getConfig().getString("local.prefix"), plugin.getConfig().getString("admin.prefix"), plugin.getConfig().getString("party.prefix"), plugin.getConfig().getString("world.prefix"));
 			boolean pass = false;
 			for(String str : prefixes ) {
 				if(plugin.dataFile.getUserChannel(evt.getPlayer()).equals(str)) {
 					if(evt.getPlayer().hasPermission("kitsunechat.nodefault."+plugin.util.getChannelName(str, false)) && !plugin.util.getChannelName(str, false).equalsIgnoreCase("local")) { //Local failsafe
 						evt.getPlayer().sendMessage(ChatColor.GRAY+"(You do not have permission to talk in "+plugin.util.getChannelName(str, false)+" by default. Changing you to local chat.)");
 						pass = false;
 					} else {
 						pass = true;
 					}
 				}
 			}
 			if(pass) {
 				if(!emote) {
 				evt.setMessage(plugin.dataFile.getUserChannel(evt.getPlayer())+message);
 				} else {
 					evt.setMessage(plugin.dataFile.getUserChannel(evt.getPlayer())+plugin.getConfig().getString("emote.prefix")+message);
 				}
 			} else {
				plugin.dataFile.setUserChannel(evt.getPlayer(), plugin.getConfig().getString("local.prefix"));
				
 			}
 			playerChat(evt);
 			return;
 		}
 		
 		
			//plugin.mcLog.info(evt.getPlayer().getName()+" : "+message);
 		
 
 	}
 
 }
