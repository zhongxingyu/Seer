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
 import mmo.Core.MMO;
 import mmo.Core.MMOPlugin;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.util.config.Configuration;
 
 public class MMOChat extends MMOPlugin {
 
 	@Override
 	public void onEnable() {
 		super.onEnable();
 		MMO.mmoChat = true;
 
 		getDatabase().find(ChatDB.class);//.findRowCount();
 
 		mmoPlayerListener mpl = new mmoPlayerListener();
 		pm.registerEvent(Type.PLAYER_CHAT, mpl, Priority.Normal, this);
 		pm.registerEvent(Type.PLAYER_COMMAND_PREPROCESS, mpl, Priority.Normal, this);
 
 		pm.registerEvent(Type.CUSTOM_EVENT, new Channels(), Priority.Normal, this);
 
 		Chat.plugin = this;
 		Chat.cfg = cfg;
 		
 		Chat.load();
 	}
 
 	@Override
 	public void loadConfiguration(Configuration cfg) {
 		cfg.getString("default_channel", "Chat");
		List<String> keys = cfg.getKeys("channel");
		if (keys == null || keys.isEmpty()) {
 			cfg.getBoolean("channel.Chat.enabled", true);
 			cfg.getBoolean("channel.Chat.command", true);
 			cfg.getBoolean("channel.Chat.log", true);
 			cfg.getString("channel.Chat.filters", "Server");
 
 			cfg.getBoolean("channel.Shout.enabled", true);
 			cfg.getBoolean("channel.Shout.command", true);
 			cfg.getBoolean("channel.Shout.log", true);
 			cfg.getString("channel.Shout.filters", "World");
 
 			cfg.getBoolean("channel.Yell.enabled", true);
 			cfg.getBoolean("channel.Yell.command", true);
 			cfg.getBoolean("channel.Yell.log", true);
 			cfg.getString("channel.Yell.filters", "Yell");
 
 			cfg.getBoolean("channel.Say.enabled", true);
 			cfg.getBoolean("channel.Say.command", true);
 			cfg.getBoolean("channel.Say.log", true);
 			cfg.getString("channel.Say.filters", "Say");
 
 			cfg.getBoolean("channel.Tell.enabled", true);
 			cfg.getBoolean("channel.Tell.command", true);
 			cfg.getBoolean("channel.Tell.log", false);
 			cfg.getString("channel.Tell.filters", "Tell");
 
 			cfg.getBoolean("channel.Reply.enabled", true);
 			cfg.getBoolean("channel.Reply.command", true);
 			cfg.getBoolean("channel.Reply.log", false);
 			cfg.getString("channel.Reply.filters", "Reply");
 
 			cfg.getBoolean("channel.Party.enabled", false);
 			cfg.getBoolean("channel.Party.command", false);
 			cfg.getBoolean("channel.Party.log", false);
 			cfg.getString("channel.Party.filters", "Party");
 		}
 		for (String channel : cfg.getKeys("channel")) {
 			// Add all channels, even disabled ones - check is dynamic
 			Chat.addChannel(channel);
 		}
 	}
 
 	@Override
 	public void onDisable() {
 //		mmo.autoUpdate();
 		MMO.mmoChat = false;
 		super.onDisable();
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		if (!(sender instanceof Player)) {
 			return false;
 		}
 		Player player = (Player) sender;
 		if (command.getName().equalsIgnoreCase("channel")) {
 			String channel;
 			if (args.length > 0 && (channel = Chat.findChannel(args[0])) != null) {
 				if (args.length > 1) {
 					if (args[1].equalsIgnoreCase("hide")) {
 						Chat.hideChannel(player, channel);
 						return true;
 					} else if (args[1].equalsIgnoreCase("show")) {
 						Chat.hideChannel(player, channel);
 						return true;
 					}
 				} else {
 					if (Chat.setChannel(player, channel)) {
 						sendMessage(player, "Channel changed to %s", Chat.getChannel(player));
 					} else {
 						sendMessage(player, "Unknown channel");
 					}
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public List<Class<?>> getDatabaseClasses() {
 		List<Class<?>> list = new ArrayList<Class<?>>();
 		list.add(ChatDB.class);
 		return list;
 	}
 
 	public class mmoPlayerListener extends PlayerListener {
 
 		@Override
 		public void onPlayerChat(PlayerChatEvent event) {
 			if (Chat.doChat(null, event.getPlayer(), event.getMessage())) {
 				event.setCancelled(true);
 			}
 		}
 
 		@Override
 		public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
 			String message = event.getMessage();
 			String channel = MMO.firstWord(message);
 			if (channel != null && !channel.isEmpty()) {
 				channel = channel.substring(1);
 				if ("me".equalsIgnoreCase(channel)
 						  && Chat.doChat(null, event.getPlayer(), message)) {
 					event.setCancelled(true);
 				} else if ((channel = Chat.findChannel(channel)) != null
 						  && cfg.getBoolean("channel." + channel + ".command", true)
 						  && Chat.doChat(channel, event.getPlayer(), MMO.removeFirstWord(message))) {
 					event.setCancelled(true);
 				}
 			}
 		}
 	}
 }
