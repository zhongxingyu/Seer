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
 import java.util.HashMap;
 import java.util.List;
 import mmo.Core.MMO;
 import mmo.Core.MMOMinecraft;
 import mmo.Core.MMOPlugin;
 import mmo.Core.util.EnumBitSet;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.util.config.Configuration;
 import org.getspout.spoutapi.event.screen.ScreenCloseEvent;
 import org.getspout.spoutapi.event.screen.ScreenListener;
 import org.getspout.spoutapi.event.screen.ScreenOpenEvent;
 import org.getspout.spoutapi.gui.Color;
 import org.getspout.spoutapi.gui.ContainerType;
 import org.getspout.spoutapi.gui.GenericContainer;
 import org.getspout.spoutapi.gui.GenericGradient;
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.gui.RenderPriority;
 import org.getspout.spoutapi.gui.ScreenType;
 import org.getspout.spoutapi.gui.Widget;
 import org.getspout.spoutapi.gui.WidgetAnchor;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 public class MMOChat extends MMOPlugin {
 
 	static final ChatAPI chat = ChatAPI.instance;
 	static final HashMap<Player, Widget> chatbar = new HashMap<Player, Widget>();
 
 	static public boolean config_default_color = false;
 
 	@Override
 	public EnumBitSet mmoSupport(EnumBitSet support) {
 		support.set(Support.MMO_PLAYER);
 		return support;
 	}
 
 	@Override
 	public void onEnable() {
 		super.onEnable();
 		ChatAPI.plugin = this;
 		ChatAPI.cfg = cfg;
 		MMOMinecraft.addAPI(chat);
 
 		mmoPlayerListener mpl = new mmoPlayerListener();
 		pm.registerEvent(Type.PLAYER_CHAT, mpl, Priority.Normal, this);
 		pm.registerEvent(Type.PLAYER_COMMAND_PREPROCESS, mpl, Priority.Normal, this);
 
 		pm.registerEvent(Type.CUSTOM_EVENT, new Channels(), Priority.Normal, this);
 
 		pm.registerEvent(Type.CUSTOM_EVENT, new mmoScreenListener(), Priority.Monitor, this);
 	}
 
 	@Override
 	public void loadConfiguration(Configuration cfg) {
 		if (cfg.getKeys().contains("default_channel")) {
 			cfg.getString("default.channel", cfg.getString("default_channel", "Chat"));
 			cfg.removeProperty("default_channel");
 		} else {
 			cfg.getString("default.channel", "Chat");
 		}
 		config_default_color = cfg.getBoolean("default.colour", config_default_color);
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
 			chat.addChannel(channel);
 			for (String alias : cfg.getStringList("channel." + channel + ".alias", new ArrayList<String>())) {
 				chat.addAlias(channel, alias);
 			}
 		}
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		if (!(sender instanceof Player)) {
 			return false;
 		}
 		Player player = (Player) sender;
 		if (command.getName().equalsIgnoreCase("channel")) {
 			String channel;
 			if (args.length > 0 && (channel = chat.findChannel(args[0])) != null) {
 				if (args.length > 1) {
 					if (args[1].equalsIgnoreCase("hide")) {
 						chat.hideChannel(player, channel);
 						return true;
 					} else if (args[1].equalsIgnoreCase("show")) {
 						chat.hideChannel(player, channel);
 						return true;
 					}
 				} else {
 					if (chat.setChannel(player, channel)) {
 						sendMessage(player, "Channel changed to %s", chat.getChannel(player));
 					} else {
 						sendMessage(player, "Unknown channel");
 					}
 					return true;
 				}
 			} else {
 				sendMessage(player, "Currently speaking on %s", chat.getChannel(player));
 				return true;
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
 
 	@Override
 	public void onPlayerJoin(Player player) {
 		chat.load(player);
 	}
 
 	@Override
 	public void onPlayerQuit(Player player) {
 		chat.unload(player.getName());
 	}
 
 	public class mmoPlayerListener extends PlayerListener {
 
 		@Override
 		public void onPlayerChat(PlayerChatEvent event) {
 			if (chat.doChat(null, event.getPlayer(), event.getMessage())) {
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
 						&& chat.doChat(null, event.getPlayer(), message)) {
 					event.setCancelled(true);
 				} else if ((channel = chat.findChannel(channel)) != null
 						&& cfg.getBoolean("channel." + channel + ".command", true)
 						&& chat.doChat(channel, event.getPlayer(), MMO.removeFirstWord(message))) {
 					event.setCancelled(true);
 				}
 			}
 		}
 	}
 
 	public class mmoScreenListener extends ScreenListener {
 
 		@Override
 		public void onScreenOpen(ScreenOpenEvent event) {
 			if (!event.isCancelled() && event.getScreenType() == ScreenType.CHAT_SCREEN) {
 				Color black = new Color(0f, 0f, 0f, 0.5f), white = new Color(1f, 1f, 1f, 0.5f);
 				SpoutPlayer player = event.getPlayer();
				Widget label, bar = chatbar.get(player);
				if (bar == null) {
					bar = new GenericContainer(
							label = new GenericLabel(ChatColor.GRAY + chat.getChannel(player)).setResize(true).setFixed(true).setMargin(3, 3, 0, 3),
							new GenericGradient(black).setPriority(RenderPriority.Highest),
							new GenericGradient(white).setMaxWidth(1).setPriority(RenderPriority.High),
							new GenericGradient(white).setMaxWidth(1).setMarginLeft(label.getWidth() + 5).setPriority(RenderPriority.High),
							new GenericGradient(white).setMaxHeight(1).setPriority(RenderPriority.High)
						).setLayout(ContainerType.OVERLAY).setAnchor(WidgetAnchor.BOTTOM_LEFT).setY(-27).setX(4).setHeight(13).setWidth(label.getWidth() + 6).setVisible(false);
					chatbar.put(player, bar);
					player.getMainScreen().attachWidget(plugin, bar);
				}
 				bar.setVisible(true);
 			}
 		}
 
 		@Override
 		public void onScreenClose(ScreenCloseEvent event) {
 			if (!event.isCancelled() && event.getScreenType() == ScreenType.CHAT_SCREEN) {
 				Widget bar = chatbar.remove(event.getPlayer());
 				if (bar != null) {
 					bar.setVisible(false);
 				}
 			}
 		}
 	}
 }
