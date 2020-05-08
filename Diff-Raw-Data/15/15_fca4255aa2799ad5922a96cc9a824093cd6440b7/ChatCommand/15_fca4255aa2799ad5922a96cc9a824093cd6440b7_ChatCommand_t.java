 package com.titankingdoms.nodinchan.titanchat.command.commands;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.titankingdoms.nodinchan.titanchat.TitanChat.MessageLevel;
 import com.titankingdoms.nodinchan.titanchat.channel.Channel;
 import com.titankingdoms.nodinchan.titanchat.command.CommandBase;
 import com.titankingdoms.nodinchan.titanchat.command.info.*;
 import com.titankingdoms.nodinchan.titanchat.event.BroadcastEvent;
 import com.titankingdoms.nodinchan.titanchat.event.EmoteEvent;
 import com.titankingdoms.nodinchan.titanchat.event.WhisperEvent;
 import com.titankingdoms.nodinchan.titanchat.event.util.Message;
 import com.titankingdoms.nodinchan.titanchat.processing.ChatPacket;
 
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
  * ChatCommand - Chat related commands
  * 
  * @author NodinChan
  *
  */
 public class ChatCommand extends CommandBase {
 	
 	/**
 	 * Broadcast Command - Broadcasts the message globally
 	 */
 	@Command
 	@Aliases("bc")
 	@Description("Broadcasts the message globally")
 	@Usage("broadcast [message]")
 	public void broadcast(CommandSender sender, Channel channel, String[] args) {
 		if (args.length < 1) { invalidArgLength(sender, "broadcast"); return; }
 		
 		if (hasPermission(sender, "TitanChat.broadcast")) {
 			if (!plugin.getConfig().getBoolean("chat." + ((sender instanceof Player) ? "player" : "server") + ".enable")) {
 				plugin.send(MessageLevel.WARNING, sender, "Broadcast Command Disabled");
 				return;
 			}
 			
 			StringBuilder str = new StringBuilder();
 			
 			for (String word : args) {
 				if (str.length() > 0)
 					str.append(" ");
 				
 				str.append(word);
 			}
 			
 			String format = plugin.getFormatHandler().broadcastFormat(sender);
 			
 			BroadcastEvent event = new BroadcastEvent(sender, new Message(format, str.toString()));
 			plugin.getServer().getPluginManager().callEvent(event);
 			
 			String[] lines = plugin.getFormatHandler().splitAndFormat(event.getFormat(), "%message", event.getMessage());
 			
 			for (String line : lines)
 				plugin.getServer().broadcastMessage(line);
 			
 		} else { plugin.send(WARNING, sender, "You do not have permission"); }
 	}
 	
 	/**
 	 * Emote Command - Action emote shown in channel
 	 */
 	@Command
 	@CommandOption(requireChannel = true)
 	@Aliases({ "em", "me" })
 	@Description("Sends an action emote to the channel")
 	@Usage("emote [action]")
 	public void emote(CommandSender sender, Channel channel, String[] args) {
 		if (args.length < 1) { invalidArgLength(sender, "emote"); return; }
 		
 		if (hasPermission(sender, "TitanChat.emote." + channel.getName())) {
 			if (!plugin.getConfig().getBoolean("chat.channel-emote.enable")) {
 				plugin.send(MessageLevel.WARNING, sender, "Emote Command Disabled");
 				return;
 			}
 			
 			StringBuilder str = new StringBuilder();
 			
 			for (String word : args) {
 				if (str.length() > 0)
 					str.append(" ");
 				
 				str.append(word);
 			}
 			
 			String format = plugin.getFormatHandler().emoteFormat(sender, channel.getName());
 			
 			EmoteEvent event = new EmoteEvent(sender, new Message(format, str.toString()));
 			plugin.getServer().getPluginManager().callEvent(event);
 			
 			channel.send(event.getFormat().replace("%action", event.getMessage()));
 			
 		} else { plugin.send(WARNING, sender, "You do not have permission"); }
 	}
 	
 	/**
 	 * Send Command - Sends a message to the channel
 	 */
 	@Command
 	@CommandOption(requireChannel = true)
 	@Aliases("s")
 	@Description("Sends a message to the channel")
 	@Usage("send [message]")
 	public void send(CommandSender sender, Channel channel, String[] args) {
 		if (channel.handleCommand(sender, "send", args))
 			return;
 		
 		if (args.length > 0) {
 			StringBuilder str = new StringBuilder();
 			
 			for (String arg : args) {
 				if (str.length() > 0)
 					str.append(" ");
 				
 				str.append(arg);
 			}
 			
 			if (sender instanceof Player) {
 				channel.sendMessage((Player) sender, str.toString());
 				return;
 			}
 			
 			channel.send(plugin.getFormatHandler().serverToChannelFormat(channel).replace("%message", str.toString()));
 			
 		} else { invalidArgLength(sender, "send"); return; }
 	}
 	
 	/**
 	 * Whisper Command - Whisper messages to players
 	 */
 	@Command
 	@Aliases("w")
 	@Description("Whispers the message to the player")
 	@Usage("whisper [player] [message]")
 	public void whisper(CommandSender sender, Channel channel, String[] args) {
 		if (args.length < 2) { invalidArgLength(sender, "whisper"); return; }
 		
 		if (hasPermission(sender, "TitanChat.whisper")) {
 			if (!plugin.getConfig().getBoolean("chat." + ((sender instanceof Player) ? "player" : "server") + ".enable")) {
 				plugin.send(MessageLevel.WARNING, sender, "Whisper Command Disabled");
 				return;
 			}
 			
 			CommandSender recipant = null;
 			
 			if (!args[0].equalsIgnoreCase("#!")) {
 				if (plugin.getPlayer(args[0]) != null)
 					recipant = plugin.getPlayer(args[0]);
 				else
 					plugin.send(MessageLevel.WARNING, sender, "Player not online");
 				
 			} else { recipant = plugin.getServer().getConsoleSender(); }
 			
 			if (recipant == null)
 				return;
 			
 			StringBuilder str = new StringBuilder();
 			
 			for (int arg = 1; arg < args.length; arg++) {
 				if (str.length() > 0)
 					str.append(" ");
 				
 				str.append(args[arg]);
 			}
 			
 			String format = plugin.getFormatHandler().whisperFormat(sender);
 			String sendFormat = plugin.getFormatHandler().colourise("&5[You -> " + sender.getName() + "] %message");
 			
 			WhisperEvent event = new WhisperEvent(sender, recipant, new Message(format, str.toString()));
 			plugin.getServer().getPluginManager().callEvent(event);
 			
 			String[] lines = plugin.getFormatHandler().splitAndFormat(event.getFormat(), "%message", event.getMessage());
 			String[] sendLines = plugin.getFormatHandler().splitAndFormat(sendFormat, "%message", event.getMessage());
 			
 			if (event.getRecipant() instanceof Player) {
 				ChatPacket packet = new ChatPacket((Player) event.getRecipant(), lines);
 				plugin.getChatProcessor().sendPacket(packet);
 				
 			} else { event.getRecipant().sendMessage(lines); }
 			
 			if (event.getSender() instanceof Player) {
 				ChatPacket packet = new ChatPacket((Player) event.getSender(), sendLines);
 				plugin.getChatProcessor().sendPacket(packet);
 				
 			} else { event.getSender().sendMessage(sendLines); }
 			
 		} else { plugin.send(WARNING, sender, "You do not have permission"); }
 	}
 }
