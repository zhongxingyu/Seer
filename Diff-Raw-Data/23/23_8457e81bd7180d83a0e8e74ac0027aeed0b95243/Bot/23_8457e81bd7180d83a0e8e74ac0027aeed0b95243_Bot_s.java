 package com.nuclearw.farion;
 
 import org.bukkit.entity.Player;
 import org.jibble.pircbot.Colors;
 import org.jibble.pircbot.PircBot;
 
 public class Bot extends PircBot {
 	private static Farion plugin;
 
 	public Bot(Farion instance) {
 		plugin = instance;
 
 		this.setName(Config.nick);
 	}
 
 	@Override
 	protected void onConnect() {
		plugin.getLogger().info("[Farion] Connected to IRC");
		plugin.getLogger().info("[Farion] " + Config.hostname + ", port " + Config.port);
 		identify(Config.nickServPassword);
 	}
 
 	@Override
 	protected void onDisconnect() {
		plugin.getLogger().info("[Farion] Disconnected from IRC.");
 
 		//Set a delayed task to attempt a rejoin
 		if(Config.retryConnect = true) {
			plugin.getLogger().info("[Farion] Retrying connect in 10 seconds...");
 
 			//Schedule the actual task
 			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 				public void run() {
 					Farion.connect();
 				}
 			}, 200L);
 
 		//If retryConnect is false, don't even bother.
 		} else {
			plugin.getLogger().info("[Farion] RetryConnect is off: No additional connection attempts."); 
 		}
 	}
 
 	@Override
 	protected void onMessage(String channel, String sender, String login, String hostname, String message) {
 		if (channel.equalsIgnoreCase(Config.channel)) {
 			message = Colors.removeFormattingAndColors(message);
 
 			if(message.equalsIgnoreCase(".players")) {
 				if(plugin.getServer().getOnlinePlayers().length == 0) {
 					sendMessage(Config.channel, "Nobody online here.");
 				} else {
 					String send = "Players online (" + plugin.getServer().getOnlinePlayers().length + "/" + plugin.getServer().getMaxPlayers() + "): ";
 					for(Player player : plugin.getServer().getOnlinePlayers()) {
 						send += player.getName() + ", ";
 					}
 					send = send.substring(0, send.length() - 2);
 					sendMessage(Config.channel, send);
 				}
 				return;
 			}
 
 			plugin.getServer().broadcastMessage("[IRC] <" + sender + "> " + message);
 			plugin.getLogger().info("[IRC][" + Config.channel + "] <" + sender + "> " + message);
 		} else if (channel.equalsIgnoreCase(Config.modChannel)) {
 			// TODO: Mod Channel
 		}
 	}
 }
