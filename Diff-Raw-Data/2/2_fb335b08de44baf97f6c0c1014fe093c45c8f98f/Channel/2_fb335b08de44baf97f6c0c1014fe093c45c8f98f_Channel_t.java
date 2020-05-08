 package com.titankingdoms.nodinchan.titanchat.channel;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 import com.nodinchan.ncloader.Loadable;
 import com.titankingdoms.nodinchan.titanchat.TitanChat;
 import com.titankingdoms.nodinchan.titanchat.events.MessageReceiveEvent;
 import com.titankingdoms.nodinchan.titanchat.events.MessageSendEvent;
 import com.titankingdoms.nodinchan.titanchat.util.Debugger;
 
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
  * Channel - Channel base
  * 
  * @author NodinChan
  *
  */
 public class Channel extends Loadable {
 	
 	protected final TitanChat plugin;
 	
 	private Type type;
 	
 	private boolean global;
 	private boolean silenced;
 	
 	private final List<String> adminlist;
 	private final List<String> blacklist;
 	private final List<String> followerlist;
 	private final List<String> invitelist;
 	private final List<String> mutelist;
 	private final List<String> participants;
 	private final List<String> whitelist;
 	
 	private File configFile = null;
 	private FileConfiguration config = null;
 	
 	/**
 	 * New Channel instance of Type unknown
 	 * 
 	 * @param name Channel name
 	 */
 	public Channel(String name) {
 		this(name, Type.UNKNOWN);
 	}
 	
 	/**
 	 * New Channel instance of Type type
 	 * 
 	 * @param name Channel name
 	 * 
 	 * @param type Channel type
 	 */
 	public Channel(String name, Type type) {
 		super(name);
 		this.plugin = TitanChat.getInstance();
 		this.type = type;
 		this.global = false;
 		this.silenced = false;
 		this.adminlist = new ArrayList<String>();
 		this.blacklist = new ArrayList<String>();
 		this.followerlist = new ArrayList<String>();
 		this.invitelist = new ArrayList<String>();
 		this.mutelist = new ArrayList<String>();
 		this.participants = new ArrayList<String>();
 		this.whitelist = new ArrayList<String>();
 	}
 	
 	/**
 	 * Check if the Player has access
 	 * 
 	 * @param player The Player to check
 	 * 
 	 * @return True if the Player has access
 	 */
 	public boolean canAccess(Player player) {
 		if (plugin.getPermsBridge().has(player, "TitanChat.access.*") || plugin.getPermsBridge().has(player, "TitanChat.access." + super.getName()))
 			return true;
 		if (blacklist.contains(player.getName()))
 			return false;
 		if (type.equals(Type.DEFAULT) || type.equals(Type.PUBLIC))
 			return true;
 		if (adminlist.contains(player.getName()) || whitelist.contains(player.getName()))
 			return true;
 		
 		return false;
 	}
 	
 	/**
 	 * Check if the Player can ban
 	 * 
 	 * @param player The Player to check
 	 * 
 	 * @return True if the Player can ban
 	 */
 	public boolean canBan(Player player) {
 		if (type.equals(Type.DEFAULT) || type.equals(Type.STAFF))
 			return false;
 		if (plugin.getPermsBridge().has(player, "TitanChat.ban.*") || plugin.getPermsBridge().has(player, "TitanChat.ban." + super.getName()))
 			return true;
 		if (adminlist.contains(player.getName()))
 			return true;
 		
 		return false;
 	}
 	
 	/**
 	 * Check if the Player can kick
 	 * 
 	 * @param player The Player to check
 	 * 
 	 * @return True if the Player can kick
 	 */
 	public boolean canKick(Player player) {
 		if (plugin.getPermsBridge().has(player, "TitanChat.kick.*") || plugin.getPermsBridge().has(player, "TitanChat.kick." + super.getName()))
 			return true;
 		if (adminlist.contains(player.getName()))
 			return true;
 		
 		return false;
 	}
 	
 	/**
 	 * Check if the Player can mute
 	 * 
 	 * @param player The Player to check
 	 * 
 	 * @return True if the Player can mute
 	 */
 	public boolean canMute(Player player) {
 		if (plugin.getPermsBridge().has(player, "TitanChat.silence") || plugin.getPermsBridge().has(player, "TitanChat.mute"))
 			return true;
 		if (adminlist.contains(player.getName()))
 			return true;
 		
 		return false;
 	}
 	
 	/**
 	 * Check if the Player can rank
 	 * 
 	 * @param player The Player to check
 	 * 
 	 * @return True if the Player can rank
 	 */
 	public boolean canRank(Player player) {
 		if (plugin.getPermsBridge().has(player, "TitanChat.rank.*") || plugin.getPermsBridge().has(player, "TitanChat.rank." + super.getName()))
 			return true;
 		if (adminlist.contains(player.getName()))
 			return true;
 		
 		return false;
 	}
 	
 	/**
 	 * Check if a Channel equals another
 	 */
 	@Override
 	public final boolean equals(Object object) {
 		if (object instanceof Channel)
 			return ((Channel) object).getName().equals(getName());
 		
 		return false;
 	}
 	
 	/**
 	 * Gets the admin list
 	 * 
 	 * @return The admin list
 	 */
 	public List<String> getAdminList() {
 		return adminlist;
 	}
 	
 	/**
 	 * Gets the blacklist
 	 * 
 	 * @return The blacklist
 	 */
 	public List<String> getBlackList() {
 		return blacklist;
 	}
 	
 	/**
 	 * Gets the config
 	 * 
 	 * @return The config
 	 */
 	public FileConfiguration getConfig() {
 		if (config == null) { reloadConfig(); }
 		return config;
 	}
 	
 	/**
 	 * Gets the follower list
 	 * 
 	 * @return The follower list
 	 */
 	public List<String> getFollowerList() {
 		return followerlist;
 	}
 	
 	/**
 	 * Gets the invite list
 	 * 
 	 * @return The invite list
 	 */
 	public List<String> getInviteList() {
 		return invitelist;
 	}
 	
 	/**
 	 * Gets the mute list
 	 * 
 	 * @return The mute list
 	 */
 	public List<String> getMuteList() {
 		return mutelist;
 	}
 	
 	/**
 	 * Gets the channel type
 	 * 
 	 * @return Channel type
 	 */
 	public final Type getType() {
 		return type;
 	}
 	
 	/**
 	 * Gets the whitelist
 	 * 
 	 * @return The whitelist
 	 */
 	public List<String> getWhiteList() {
 		return whitelist;
 	}
 	
 	/**
 	 * Check if global
 	 * 
 	 * @return True if the channel is global
 	 */
 	public boolean isGlobal() {
 		return global;
 	}
 	
 	/**
 	 * Check if silenced
 	 * 
 	 * @return True if the channel is silenced
 	 */
 	public boolean isSilenced() {
 		return silenced;
 	}
 	
 	/**
 	 * Called when a player joins the channel
 	 * 
 	 * @param player The player joining
 	 */
 	public void join(Player player) {}
 	
 	/**
 	 * Gets the participant list
 	 * 
 	 * @return The participant list
 	 */
 	public List<String> getParticipants() {
 		return participants;
 	}
 	
 	/**
 	 * Called when a player leaves the channel
 	 * 
 	 * @param player The player leaving
 	 */
 	public void leave(Player player) {}
 	
 	/**
 	 * Reloads the config
 	 */
 	public void reloadConfig() {
 		if (configFile == null) { configFile = new File(plugin.getChannelDir(), super.getName() + ".yml"); }
 		
 		config = YamlConfiguration.loadConfiguration(configFile);
 		
 		InputStream defConfigStream = plugin.getResource("channel.yml");
 		
 		if (defConfigStream != null) {
 			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
 			config.setDefaults(defConfig);
 		}
 	}
 	
 	/**
 	 * Called when TitanChat disables
 	 */
 	public void save() {}
 	
 	public void saveConfig() {
 		if (configFile == null || config == null) { return; }
 		try { config.save(configFile); } catch (IOException e) { plugin.log(Level.SEVERE, "Could not save config to " + configFile); }
 	}
 	
 	/**
 	 * Called when a message is to be sent
 	 * 
 	 * @param sender The message sender
 	 * 
 	 * @param message The message to be sent
 	 */
 	public void sendMessage(Player sender, String message) {
 		sendMessage(sender, new ArrayList<Player>(), message);
 	}
 	
 	/**
 	 * Called when a message is to be sent
 	 * 
 	 * @param sender The message sender
 	 * 
 	 * @param recipants The players to send to
 	 * 
 	 * @param message The message to be sent
 	 */
 	protected final boolean sendMessage(Player sender, List<Player> recipants, String message) {
 		MessageSendEvent sendEvent = new MessageSendEvent(sender, recipants, message);
 		plugin.getServer().getPluginManager().callEvent(sendEvent);
 		
 		if (sendEvent.isCancelled()) { return false; }
 		
 		for (Player recipant : sendEvent.getRecipants()) {
			MessageReceiveEvent receiveEvent = new MessageReceiveEvent(sender, recipant, sendEvent.getMessage());
 			plugin.getServer().getPluginManager().callEvent(receiveEvent);
 			
 			if (receiveEvent.isCancelled()) { continue; }
 			
 			receiveEvent.getRecipant().sendMessage(receiveEvent.getMessage());
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Called when a message is to be sent
 	 * 
 	 * @param sender The message sender
 	 * 
 	 * @param recipants The players to send to
 	 * 
 	 * @param message The message to be sent
 	 */
 	protected final boolean sendMessage(Player sender, Player[] recipants, String message) {
 		return sendMessage(sender, Arrays.asList(recipants), message);
 	}
 	
 	/**
 	 * Sets whether the channel is global
 	 * 
 	 * @param global True if channel should be global
 	 */
 	public void setGlobal(boolean global) {
 		this.global = global;
 	}
 	
 	/**
 	 * Sets whether the channel is silenced
 	 * 
 	 * @param silenced True if channel should be silenced
 	 */
 	public void setSilenced(boolean silenced) {
 		this.silenced = silenced;
 	}
 	
 	/**
 	 * Sets the type of the channel
 	 * 
 	 * @param type The type
 	 */
 	public void setType(String type) {
 		this.type = Type.fromName(type);
 	}
 	
 	/**
 	 * Returns the Channel as a String
 	 */
 	@Override
 	public String toString() {
 		return "Channel:" + super.getName() + " : " + type.getName();
 	}
 	
 	/**
 	 * Type - Types of Channels
 	 * 
 	 * @author NodinChan
 	 *
 	 */
 	public enum Type {
 		CUSTOM("custom"),
 		DEFAULT("default"),
 		PASSWORD("password"),
 		PRIVATE("private"),
 		PUBLIC("public"),
 		STAFF("staff"),
 		UNKNOWN("unknown");
 		
 		private String name;
 
 		private final static Debugger db = new Debugger(3);
 		private static final Map<String, Type> NAME_MAP = new HashMap<String, Type>();
 		
 		private Type(String name) {
 			this.name = name;
 		}
 		
 		static {
 			for (Type type : EnumSet.allOf(Type.class)) {
 				db.i("Adding Type: " + type.name);
 				NAME_MAP.put(type.name, type);
 			}
 		}
 		
 		public static Type fromName(String name) {
 			return NAME_MAP.get(name);
 		}
 		
 		public String getName() {
 			return name;
 		}
 	}
 }
