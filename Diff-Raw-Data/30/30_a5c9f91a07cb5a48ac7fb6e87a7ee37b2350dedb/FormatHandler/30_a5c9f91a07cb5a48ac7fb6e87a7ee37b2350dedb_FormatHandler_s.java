 package com.titankingdoms.nodinchan.titanchat.util;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.titankingdoms.nodinchan.titanchat.TitanChat;
 import com.titankingdoms.nodinchan.titanchat.channel.Channel;
 import com.titankingdoms.nodinchan.titanchat.channel.util.Info;
 import com.titankingdoms.nodinchan.titanchat.event.chat.MessageFormatEvent;
 
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
 
 public final class FormatHandler {
 	
 	private static TitanChat plugin;
 	
 	protected static final Debugger db = new Debugger(5);
 	
 	private final Pattern pattern = Pattern.compile("(?i)(&)([0-9a-fk-or])");
 	
 	public FormatHandler() {
 		FormatHandler.plugin = TitanChat.getInstance();
 	}
 	
 	public String broadcastFormat(CommandSender sender) {
 		String format = plugin.getConfig().getString("chat.server.broadcast");
 		
 		if (sender instanceof Player) {
 			format = plugin.getConfig().getString("chat.player.broadcast");
 			format = format.replace("%player", ((Player) sender).getDisplayName());
 			format = format.replace("%name", sender.getName());
 			format = infoParse((Player) sender, format);
 		}
 		
 		return plugin.getFormatHandler().colourise(format);
 	}
 	
 	public String colour(Player sender, String msg) {
 		StringBuffer str = new StringBuffer();
 		Matcher match = pattern.matcher(msg);
 		
 		while (match.find()) {
 			ChatColor colour = ChatColor.getByChar(match.group(2).toLowerCase());
 			
 			if (sender.hasPermission("TitanChat.format.&" + colour.getChar()))
 				match.appendReplacement(str, colour.toString());
 			else
 				match.appendReplacement(str, "");
 		}
 		
 		return match.appendTail(str).toString();
 	}
 	
 	public String colourise(String text) {
 		return text.replaceAll(pattern.toString(), "\u00A7$2");
 	}
 	
 	public String decolourise(String message) {
 		return message.replaceAll(pattern.toString(), "");
 	}
 	
 	public String emoteFormat(CommandSender sender, String channel) {
 		String format = plugin.getConfig().getString("chat.server.emote");
 		
 		if (channel != null && !channel.isEmpty())
 			format = plugin.getConfig().getString("chat.channel-emote.format");
 		else if (sender instanceof Player)
 			format = plugin.getConfig().getString("chat.player.emote");
 		
 		if (sender instanceof Player) {
 			format = format.replace("%player", ((Player) sender).getDisplayName());
 			format = format.replace("%name", sender.getName());
 			format = infoParse((Player) sender, format);
 		}
 		
 		return plugin.getFormatHandler().colourise(format);
 	}
 	
 	public String format(Player sender, String channel) {
 		String format = "%tag %prefix%player%suffix&f: %message";
 		
 		Info info = plugin.getManager().getChannelManager().getChannel(channel).getInfo();
 		
 		if (plugin.getConfig().getBoolean("formatting.use-custom-format"))
 			format = info.getFormat();
 		
 		format = format.replace("%player", sender.getDisplayName());
 		format = format.replace("%name", sender.getName());
 		format = format.replace("%tag", info.getTag());
 		format = format.replace("%message", info.getChatColour() + "%message");
		format = infoParse(sender, format);
 		
 		MessageFormatEvent event = new MessageFormatEvent(sender, format);
 		plugin.getServer().getPluginManager().callEvent(event);
 		
 		return plugin.getFormatHandler().colourise(event.getFormat());
 	}
 	
	public String infoParse(Player sender, String format) {
 		Pattern pattern = Pattern.compile("(?i)(%)([a-z0-9]+)");
 		Matcher match = pattern.matcher(format);
 		
 		while (match.find()) {
 			String infoType = match.group(2);
 			String info = plugin.getInfoHandler().getInfo(sender, infoType, "");
 			format = format.replace("%" + infoType, info);
 		}
 		
 		return format;
 	}
 	
 	public String serverToChannelFormat(Channel channel) {
		String format = plugin.getConfig().getString("chat.serverToChannel");
 		
 		Info info = channel.getInfo();
 		format = format.replace("%tag", info.getTag());
 		format = format.replace("%message", info.getChatColour() + "%message");
 		
 		return plugin.getFormatHandler().colourise(format);
 	}
 	
 	public String[] split(String line) {
 		List<String> lines = new LinkedList<String>();
 		
 		while (line.length() > 119) {
			int end = 119;
			
			if (line.charAt(end) != ' ')
				end = line.lastIndexOf(' ', 119);
 			
 			if (end == -1)
 				end = 119;
 			
 			lines.add(line.substring(0, end));
 			line = (ChatColor.getLastColors(lines.get(lines.size() - 1)) + line.substring(end)).trim();
 		}
 		
 		if (line.length() > 0)
 			lines.add(line);
 		
 		return lines.toArray(new String[0]);
 	}
 	
 	public String[] splitAndFormat(String format, String variable, String line) {
 		return split(format.replace(variable, line));
 	}
 	
 	public String whisperFormat(CommandSender sender) {
 		String format = plugin.getConfig().getString("chat.server.whisper");
 		
 		if (sender instanceof Player) {
 			format = plugin.getConfig().getString("chat.player.whisper");
 			format = format.replace("%player", ((Player) sender).getDisplayName());
 			format = format.replace("%name", ((Player) sender).getName());
 			format = infoParse((Player) sender, format);
 		}
 		
 		return plugin.getFormatHandler().colourise(format);
 	}
 }
