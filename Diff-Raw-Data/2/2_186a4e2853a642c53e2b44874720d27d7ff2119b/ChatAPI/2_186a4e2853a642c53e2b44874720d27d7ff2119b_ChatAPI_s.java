 /*
  * This file is part of mmoMinecraft (https://github.com/mmoMinecraftDev).
  *
  * mmoMinecraft is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package mmo.Chat;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import mmo.Core.ChatAPI.Chat;
 import mmo.Core.util.ArrayListString;
 import mmo.Core.MMO;
 import mmo.Core.MMOPlugin;
 import mmo.Core.util.HashMapString;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.util.config.Configuration;
 
 public class ChatAPI implements Chat {
 
 	private ChatAPI() { // Prevent instantiation
 	}
 	/**
 	 * Singleton instance
 	 */
 	public final static ChatAPI instance = new ChatAPI();
 	// ...and now the class...
 	final static ArrayListString channelList = new ArrayListString();
 	final static HashMapString<String> aliasList = new HashMapString<String>();
 	final static HashMapString<String> playerChannel = new HashMapString<String>();
 	final static HashMapString<List<String>> playerHidden = new HashMapString<List<String>>();
 	static MMOPlugin plugin;
 	static Configuration cfg;
 
 	public void addChannel(String name) {
 		if (!channelList.contains(name)) {
 			channelList.add(name);
 		}
 	}
 
 	public void addAlias(String channel, String alias) {
 		addChannel(channel);
 		if (!aliasList.containsKey(alias)) {
 			aliasList.put(alias, channel);
 		}
 	}
 
 	@Override
 	public boolean doChat(String chan, Player from, String message) {
 		if (chan == null) {
 			chan = getChannel(from);
 		}
 		String channel = channelList.get(chan);
 		if (!cfg.getBoolean("channel." + channel + ".enabled", true) || !useChannel(from, channel)) {
 			// Report it as unknown if no permission or it's disabled
 			plugin.sendMessage(from, "Unknown channel: %s", chan);
 			return false;
 		}
 		boolean me = false;
 		if (MMO.firstWord(message).equalsIgnoreCase("/me")) {
 			message = MMO.removeFirstWord(message);
 			me = true;
 		}
 		if (message.isEmpty()) {
 			setChannel(from, channel);
 			plugin.sendMessage(from, "Channel changed to %s", channel);
 			return true;
 		}
 		String format = cfg.getString("channel." + channel + ".format" + (me ? "me" : ""));
 		if (format == null) {
 			format = cfg.getString("default.format" + (me ? "me" : ""));
 		}
 		if (format == null) {
 			format = me ? "[%1$s] * %2$s %4$s" : "[%1$s] %2$s: %4$s";
 		}
 		ArrayListString filters = new ArrayListString();
 		HashMapString<String[]> args = new HashMapString<String[]>();
 		for (String filter : cfg.getStringList("channel." + channel + ".filters", new ArrayList<String>())) {
 			String name = MMO.firstWord(filter);
 			filters.add(name);
 			args.put(name, MMO.smartSplit(MMO.removeFirstWord(filter)));
 		}
 		MMOChatEventAPI event = new MMOChatEventAPI(from, filters, args, format, message);
 		plugin.getServer().getPluginManager().callEvent(event);
 		Set<Player> recipients = event.getRecipients();
 		if (recipients.isEmpty()) {
 			plugin.sendMessage(from, "You seem to be talking to yourself...");
 		} else {
 			if (cfg.getBoolean("channel." + channel + ".log", false)) {
 				plugin.log("[%1$s] %2$s: %3$s", channel, from.getName(), message);
 			}
 			for (Player to : recipients) {
 				String msg = event.getMessage(to);
 				if (msg != null && !msg.isEmpty() && seeChannel(to, channel)) {
 					String fmt = event.getFormat(to).replaceAll("(?:&)([a-fA-F0-9])", "\u00A7$1");
 					if (MMOChat.config_default_color) {
 						msg = msg.replaceAll("(?:&)([a-fA-F0-9])", "\u00A7$1");
 					}
 					msg = msg.replaceAll("&", "\u0026");
 					plugin.sendMessage(false, to, fmt,
 							channel,
 							MMO.getColor(to, from) + from.getName() + ChatColor.WHITE,
 							MMO.getColor(from, to) + to.getName() + ChatColor.WHITE,
 							msg);
 				}
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public boolean hideChannel(Player player, String channel) {
 		if (channelList.contains(channel)) {
 			List<String> list = playerHidden.get(player.getName());
 			if (list == null) {
 				playerHidden.put(player.getName(), list = new ArrayListString());
 			}
 			if (!list.contains(channel)) {
 				list.add(channel);
 			}
 			plugin.setStringList(player, "hidden", list);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean showChannel(Player player, String channel) {
 		if (channelList.contains(channel)) {
 			List<String> list = playerHidden.get(player.getName());
 			if (list == null) {
 				playerHidden.put(player.getName(), list = new ArrayListString());
 			}
 			if (list.contains(channel)) {
 				list.remove(channel);
 			}
 			plugin.setStringList(player, "hidden", list);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean seeChannel(Player player, String channel) {
 		if (playerHidden.containsKey(player.getName())) {
 			for (String chan : playerHidden.get(player.getName())) {
 				if (channel.equalsIgnoreCase(chan)) {
 					return false;
 				}
 			}
 		}
 		String[] perms = {
 			"mmo.chat." + channel + ".see",
 			"mmo.chat.*.see",
 			"mmo.chat." + channel,
 			"mmo.chat.*"};
 		for (String perm : perms) {
 			if (player.isPermissionSet(perm)) {
 				return player.hasPermission(perm);
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public boolean useChannel(Player player, String channel) {
 		String[] perms = {
 			"mmo.chat." + channel + ".use",
 			"mmo.chat.*.use",
 			"mmo.chat." + channel,
 			"mmo.chat.*"};
 		for (String perm : perms) {
 			if (player.isPermissionSet(perm)) {
 				return player.hasPermission(perm);
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public boolean setChannel(Player player, String channel) {
 		String name = player.getName();
 		if (channelList.contains(channel)) {
 			playerChannel.put(name, channel);
 			plugin.setString(player, "channel", channel);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public String findChannel(String channel) {
 		if (channelList.contains(channel)) {
 			return channelList.get(channel);
 		}
 		if (aliasList.containsKey(channel)) {
 			return aliasList.get(channel);
 		}
 		return null;
 	}
 
 	@Override
 	public boolean isChannel(String channel) {
 		if (channelList.contains(channel)) {
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public String getChannel(Player player) {
 		String channel = playerChannel.get(player.getName());
 		channel = channel == null ? cfg.getString("default.channel", "Chat") : channel;
 		if (!channelList.contains(channel)) {
 			plugin.log("ERROR - set 'default.channel' to one that exists...");
 			return channelList.get(0);
 		}
 		return channelList.get(channel);
 	}
 
 	/**
 	 * Load the default channel for a player
 	 * @param player
 	 */
 	public void load(Player player) {
 		playerChannel.put(player.getName(), plugin.getString(player, "channel", cfg.getString("default.channel", "Chat")));
		playerHidden.put(player.getName(), plugin.getStringList(player, "hidden", null));
 	}
 
 	/**
 	 * Free a player's default channel
 	 * @param player
 	 */
 	public void unload(String player) {
 		playerChannel.remove(player);
 		playerHidden.remove(player);
 	}
 }
