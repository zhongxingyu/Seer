 package com.titankingdoms.nodinchan.titanchat.util;
 
import java.util.logging.Level;

 import org.bukkit.entity.Player;
 
 import com.titankingdoms.nodinchan.titanchat.TitanChat;
 import com.titankingdoms.nodinchan.titanchat.channel.CustomChannel;
 import com.titankingdoms.nodinchan.titanchat.channel.StandardChannel;
 import com.titankingdoms.nodinchan.titanchat.channel.Variables;
 import com.titankingdoms.nodinchan.titanchat.events.MessageFormatEvent;
 
 /**
  * FormatHandler - Handles formatting
  * 
  * @author NodinChan
  *
  */
 public final class FormatHandler {
 	
 	private final TitanChat plugin;
 	
 	public FormatHandler(TitanChat plugin) {
 		this.plugin = plugin;
 	}
 	
 	/**
 	 * Gets the broadcast format and formats the message
 	 * 
 	 * @param player The sender
 	 * 
 	 * @param msg The message
 	 * 
 	 * @return The formatted message
 	 */
 	public String broadcastFormat(Player player, String msg) {
 		String message = plugin.getConfig().getString("broadcast.player.format");
 		
 		String playerPrefix = plugin.getPlayerPrefix(player);
 		String playerSuffix = plugin.getPlayerSuffix(player);
 		String groupPrefix = plugin.getGroupPrefix(player);
 		String groupSuffix = plugin.getGroupSuffix(player);
 		
 		message = message.replace("%prefix", playerPrefix);
 		message = message.replace("%gprefix", groupPrefix);
 		message = message.replace("%player", player.getDisplayName());
 		message = message.replace("%suffix", playerSuffix);
 		message = message.replace("%gsuffix", groupSuffix);
 		
 		MessageFormatEvent formatEvent = new MessageFormatEvent(player, message);
 		plugin.getServer().getPluginManager().callEvent(formatEvent);
 		
 		message = formatEvent.getFormat();
 		
 		StringBuilder str = new StringBuilder();
 		
 		for (String word : message.split(" ")) {
 			if (str.length() > 0)
 				str.append(" ");
 			
 			str.append(colourise(word));
 		}
 		
 		message = str.toString();
 		
 		message = message.replace("%message", colourise(msg));
 		
 		return message;
 	}
 	
 	/**
 	 * Colourises the message
 	 * 
 	 * @param text The message
 	 * 
 	 * @return The colourised text
 	 */
 	public String colourise(String text) {
 		return text.replaceAll("(&([a-f0-9A-Fk-oK-O]))", "\u00A7$2");
 	}
 	
 	/**
 	 * Check if Channel convert colours
 	 * 
 	 * @param name The Channel name
 	 * 
 	 * @return True if converts
 	 */
 	public boolean colours(String name) {
		if (plugin.getChannelManager().getStaffChannel().equals(plugin.getChannelManager().getChannel(name)))
			return true;
 		
 		if (plugin.getChannelManager().getChannel(name) instanceof CustomChannel)
 			return true;
 		
		if (((StandardChannel) plugin.getChannelManager().getChannel(name)) == null)
			plugin.log(Level.INFO, "Debug 2");
		
 		return ((StandardChannel) plugin.getChannelManager().getChannel(name)).getVariables().convert();
 	}
 	
 	/**
 	 * Decolourises the message
 	 * 
 	 * @param message The message
 	 * 
 	 * @return The decolourised text
 	 */
 	public String decolourise(String message) {
 		return message.replaceAll("(&([a-f0-9A-Fk-oK-O]))", "");
 	}
 	
 	/**
 	 * Gets the emote format and formats the message
 	 * 
 	 * @param player The sender
 	 * 
 	 * @param msg The message
 	 * 
 	 * @return The formatted message
 	 */
 	public String emoteFormat(Player player, String msg) {
 		String message = plugin.getConfig().getString("emote.player.format");
 		
 		String playerPrefix = plugin.getPlayerPrefix(player);
 		String playerSuffix = plugin.getPlayerSuffix(player);
 		String groupPrefix = plugin.getGroupPrefix(player);
 		String groupSuffix = plugin.getGroupSuffix(player);
 
 		message = message.replace("%prefix", playerPrefix);
 		message = message.replace("%gprefix", groupPrefix);
 		message = message.replace("%player", player.getDisplayName());
 		message = message.replace("%suffix", playerSuffix);
 		message = message.replace("%gsuffix", groupSuffix);
 		
 		MessageFormatEvent formatEvent = new MessageFormatEvent(player, message);
 		plugin.getServer().getPluginManager().callEvent(formatEvent);
 		
 		message = formatEvent.getFormat();
 		
 		StringBuilder str = new StringBuilder();
 		
 		for (String word : message.split(" ")) {
 			if (str.length() > 0)
 				str.append(" ");
 			
 			str.append(colourise(word));
 		}
 		
 		message = str.toString();
 		
 		message = message.replace("%action", colourise(msg));
 		
 		return message;
 	}
 	
 	public String format(Player player) {
 		String message = "";
 		
 		String playerPrefix = plugin.getPlayerPrefix(player);
 		String playerSuffix = plugin.getPlayerSuffix(player);
 		String groupPrefix = plugin.getGroupPrefix(player);
 		String groupSuffix = plugin.getGroupSuffix(player);
 		String chatColour = plugin.getConfig().getString("channels.chat-display-colour");
 		
 		if (plugin.useDefaultFormat()) {
 			message = colourise("<" + playerPrefix + "&1$s" + playerSuffix + "&f> " + chatColour + "%2$s");
 			
 		} else {
 			message = plugin.getConfig().getString("formatting.format");
 
 			message = message.replace("%prefix", playerPrefix);
 			message = message.replace("%gprefix", groupPrefix);
 			message = message.replace("%player", "%1$s");
 			message = message.replace("%suffix", playerSuffix);
 			message = message.replace("%gsuffix", groupSuffix);
 			
 			MessageFormatEvent formatEvent = new MessageFormatEvent(player, message);
 			plugin.getServer().getPluginManager().callEvent(formatEvent);
 			
 			message = formatEvent.getFormat();
 			
 			StringBuilder str = new StringBuilder();
 			
 			for (String word : message.split(" ")) {
 				if (str.length() > 0)
 					str.append(" ");
 				
 				str.append(colourise(word));
 			}
 			
 			message = str.toString();
 			
 			message = message.replace("%message", "%2$s");
 		}
 		
 		return message;
 	}
 	
 	public String format(Player player, String channel, String msg) {
 		String message = "";
 		
 		Variables variables = ((StandardChannel) plugin.getChannelManager().getChannel(channel)).getVariables();
 		
 		String name = player.getDisplayName();
 		String tag = variables.getTag();
 		String playerPrefix = plugin.getPlayerPrefix(player);
 		String playerSuffix = plugin.getPlayerSuffix(player);
 		String groupPrefix = plugin.getGroupPrefix(player);
 		String groupSuffix = plugin.getGroupSuffix(player);
 		String chatColour = variables.getChatColour();
 		String nameColour = variables.getNameColour();
 		
 		if (plugin.useDefaultFormat()) {
 			if (colours(channel) || plugin.has(player, "TitanChat.colours"))
 				message = colourise(tag + " " + playerPrefix + nameColour + name + playerSuffix + "&f: " + chatColour + msg);
 			else
 				message = colourise(tag + " " + playerPrefix + nameColour + name + playerSuffix + "&f: " + chatColour) + decolourise(msg);
 			
 		} else {
 			message = variables.getFormat();
 			
 			message = message.replace("%tag", tag);
 			message = message.replace("%prefix", playerPrefix);
 			message = message.replace("%gprefix", groupPrefix);
 			message = message.replace("%player", nameColour + name);
 			message = message.replace("%suffix", playerSuffix);
 			message = message.replace("%gsuffix", groupSuffix);
 			
 			MessageFormatEvent formatEvent = new MessageFormatEvent(player, message);
 			plugin.getServer().getPluginManager().callEvent(formatEvent);
 			
 			message = formatEvent.getFormat();
 			
 			StringBuilder str = new StringBuilder();
 			
 			for (String word : message.split(" ")) {
 				if (str.length() > 0)
 					str.append(" ");
 				
 				str.append(colourise(word));
 			}
 			
 			message = str.toString();
 			
 			if (colours(channel) || plugin.has(player, "TitanChat.colours"))
 				message = message.replace("%message", colourise(chatColour + msg));
 			else
 				message = message.replace("%message", colourise(chatColour) + decolourise(msg));
 		}
 		
 		return message;
 	}
 	
 	public String whisperFormat(Player player, String msg) {
 		String message = plugin.getConfig().getString("whisper.player.format");
 		
 		String playerPrefix = plugin.getPlayerPrefix(player);
 		String playerSuffix = plugin.getPlayerSuffix(player);
 		String groupPrefix = plugin.getGroupPrefix(player);
 		String groupSuffix = plugin.getGroupSuffix(player);
 
 		message = message.replace("%prefix", playerPrefix);
 		message = message.replace("%gprefix", groupPrefix);
 		message = message.replace("%player", player.getDisplayName());
 		message = message.replace("%suffix", playerSuffix);
 		message = message.replace("%gsuffix", groupSuffix);
 		
 		MessageFormatEvent formatEvent = new MessageFormatEvent(player, message);
 		plugin.getServer().getPluginManager().callEvent(formatEvent);
 		
 		message = formatEvent.getFormat();
 		
 		StringBuilder str = new StringBuilder();
 		
 		for (String word : message.split(" ")) {
 			if (str.length() > 0)
 				str.append(" ");
 			
 			str.append(colourise(word));
 		}
 		
 		message = str.toString();
 		
 		message = message.replace("%message", colourise(msg));
 		
 		return message;
 	}
 }
