 package com.titankingdoms.nodinchan.titanchat.channel;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 import com.nodinchan.loader.Loadable;
 import com.titankingdoms.nodinchan.titanchat.TitanChat;
 
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
 	
 	public Channel(String name) {
 		this(name, Type.UNKNOWN);
 	}
 	
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
 	
 	public Channel(String name, ChannelVariables variables) {
 		super(name);
 		this.plugin = TitanChat.getInstance();
 		this.type = Type.CUSTOM;
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
 		if (plugin.has(player, "TitanChat.access.*") || plugin.has(player, "TitanChat.access." + super.getName()))
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
 		if (plugin.has(player, "TitanChat.ban.*") || plugin.has(player, "TitanChat.ban." + super.getName()))
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
 		if (plugin.has(player, "TitanChat.kick.*") || plugin.has(player, "TitanChat.kick." + super.getName()))
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
 		if (plugin.has(player, "TitanChat.silence") || plugin.has(player, "TitanChat.mute"))
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
 		if (plugin.has(player, "TitanChat.rank.*") || plugin.has(player, "TitanChat.rank." + super.getName()))
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
 	 * Get the config
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
 	 * @param player
 	 * @param message
 	 */
 	public void sendMessage(Player player, String message) {}
 	
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
 }
