 package com.titankingdoms.nodinchan.titanchat.channel;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.util.logging.Level;
 
 import org.bukkit.entity.Player;
 
 import com.titankingdoms.nodinchan.titanchat.TitanChat;
 import com.titankingdoms.nodinchan.titanchat.debug.Debugger;
 
 /**
  * ChannelManager - Manages Channels
  * 
  * @author NodinChan
  *
  */
 public final class ChannelManager {
 	
 	private final TitanChat plugin;
 	
 	private static ChannelManager instance;
 	
 	private final ChannelLoader loader;
 	
 	private static final Debugger db = new Debugger(3);
 	
 	private int channelAmount = 0;
 	private int customChAmount = 0;
 	
 	private List<Channel> channels;
 	
 	private Map<Channel, Map<String, List<String>>> channelInvitors;
 	private final Map<CustomChannel, JarFile> jarFiles;
 	
 	public ChannelManager(TitanChat plugin) {
 		this.plugin = plugin;
 		ChannelManager.instance = this;
 		this.loader = new ChannelLoader(plugin);
 		this.channels = new ArrayList<Channel>();
 		this.channelInvitors = new HashMap<Channel, Map<String, List<String>>>();
 		this.jarFiles = new HashMap<CustomChannel, JarFile>();
 	}
 	
 	/**
 	 * Assigns the Player as an admin of the Channel
 	 * 
 	 * @param player The Player to be assigned admin
 	 * 
 	 * @param channel The Channel to assign the Player to
 	 */
 	public void assignAdmin(Player player, Channel channel) {
 		db.i("Assigning player " + player.getName() +
 				" as admin of channel " + channel.getName());
 		
 		channel.getAdminList().add(player.getName());
 		channel.save();
 		plugin.sendInfo(player, "You are now an Admin of " + channel.getName());
 	}
 	
 	/**
 	 * Switching the Player from a Channel to another
 	 * 
 	 * @param player The Player to switch
 	 * 
 	 * @param channel The Channel to join
 	 */
 	public void chSwitch(Player player, Channel channel) {
 		db.i("Channel switch of player " + player.getName() +
 				" from channel " + getChannel(player).getName() +
 				" to channel " + channel.getName());
 		
 		getChannel(player).leave(player);
 		channel.join(player);
 	}
 
 	/**
 	 * Creates a new Channel with the given name
 	 * 
 	 * @param player The Channel creator
 	 * 
 	 * @param name The Channel name
 	 */
 	public void createChannel(Player player, String name) {
 		db.i("Player " + player.getName() +
 				" is creating channel " + name);
 		
		StandardChannel channel = new StandardChannel(name, Type.PUBLIC);
 		channels.add(channel);
 		
 		assignAdmin(player, channel);
 		chSwitch(player, channel);
 		
		channel.getConfig().options().copyDefaults(true);
 		channel.save();
 		
 		plugin.sendInfo(player, "You have created " + channel.getName());
 	}
 	
 	/**
 	 * Deletes the Channel with the given name
 	 * 
 	 * @param player The Channel deleter
 	 * 
 	 * @param name The Channel name
 	 */
 	public void deleteChannel(Player player, String name) {
 		db.i("Player " + player.getName() +
 				" is deleting channel " + name);
 		
 		Channel channel = getChannel(name);
 		
		for (String participant : channel.getParticipants()) {
 			if (plugin.getPlayer(participant) != null)
 				chSwitch(plugin.getPlayer(participant), getSpawnChannel(player));
 		}
 		
 		channels.remove(channel);
 		
		plugin.sendWarning(channel.getParticipants(), channel.getName() + " has been deleted");
 		
 		File file = new File(plugin.getChannelDir(), name + ".yml");
 		file.delete();
 		
 		plugin.sendInfo(player, "You have deleted " + channel.getName());
 	}
 	
 	/**
 	 * Check if a Channel by that name exists
 	 * 
 	 * @param name The Channel name
 	 * 
 	 * @return True if the Channel exists
 	 */
 	public boolean exists(String name) {
 		return getChannel(name) != null;
 	}
 	
 	/**
 	 * Creates a list of Channels that can be accessed by the Player
 	 * 
 	 * @param player The Player to check
 	 * 
 	 * @return The list of accessible Channels of the Player
 	 */
 	public List<String> getAccessList(Player player) {
 		List<String> channels = new ArrayList<String>();
 		
 		for (Channel channel : this.channels) {
 			if (channel.canAccess(player))
 				channels.add(channel.getName());
 		}
 		
 		return channels;
 	}
 	
 	/**
 	 * Gets the Channel of the given name
 	 * 
 	 * @param name The Channel name
 	 * 
 	 * @return The Channel if it exists, otherwise null
 	 */
 	public Channel getChannel(String name) {
 		for (Channel channel : channels) {
 			if (channel.getName().equalsIgnoreCase(name))
 				return channel;
 		}
 		
 		return null;
 	}
 
 	/**
 	 * Gets the Channel of the Player
 	 * 
 	 * @param player The Player to check
 	 * 
 	 * @return The Channel if it exists, otherwise null
 	 */
 	public Channel getChannel(Player player) {
 		for (Channel channel : channels) {
 			if (channel.getParticipants().contains(player.getName()))
 				return channel;
 		}
 		
 		return null;
 	}
 
 	/**
 	 * Gets the number of loaded Channels
 	 * 
 	 * @return The number of loaded Channels
 	 */
 	public int getChannelAmount() {
 		return channelAmount;
 	}
 
 	/**
 	 * Gets the Default Channel of the Server
 	 * 
 	 * @return The Default Channel
 	 */
 	public Channel getDefaultChannel() {
 		for (Channel channel : channels) {
 			if (channel.getType().equals(Type.DEFAULT))
 				return channel;
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Gets the exact name of the Channel of the given name
 	 * 
 	 * @param name The Channel name
 	 * 
 	 * @return The exact name of the Channel
 	 */
 	public String getExact(String name) {
 		return getChannel(name).getName();
 	}
 	
 	public List<String> getFollowers(Channel channel) {
 		List<String> followers = new ArrayList<String>();
 		followers.addAll(channel.getFollowerList());
 		
 		for (Player player : plugin.getServer().getOnlinePlayers()) {
 			if (plugin.getWildcardAvoider().has(player, "TitanChat.follow." + channel.getName()) && !followers.contains(player.getName()))
 				followers.add(player.getName());
 		}
 		
 		return followers;
 	}
 
 	/**
 	 * Gets an instance of this
 	 * 
 	 * @return ChannelManager instance
 	 */
 	public static ChannelManager getInstance() {
 		return instance;
 	}
 	
 	/**
 	 * Gets resource out of the JAR file of an Channel
 	 * 
 	 * @param channel The Channel
 	 * 
 	 * @param fileName The file to look for
 	 * 
 	 * @return The file if found, otherwise null
 	 */
 	public InputStream getResource(CustomChannel channel, String fileName) {
 		try {
 			JarFile jarFile = jarFiles.get(channel);
 			Enumeration<JarEntry> entries = jarFile.entries();
 			
 			while (entries.hasMoreElements()) {
 				JarEntry element = entries.nextElement();
 				
 				if (element.getName().equalsIgnoreCase(fileName))
 					return jarFile.getInputStream(element);
 			}
 			
 		} catch (IOException e) {}
 		
 		return null;
 	}
 	
 	/**
 	 * Gets the Spawn Channel of the Player
 	 * 
 	 * @param player The Player to check
 	 * 
 	 * @return The Spawn Channel
 	 */
 	public Channel getSpawnChannel(Player player) {
 		for (Channel channel : channels) {
 			if (plugin.getWildcardAvoider().has(player, "TitanChat.spawn." + channel.getName()) && channel.canAccess(player))
 				return channel;
 		}
 		
 		if (getStaffChannel() != null) {
 			if (plugin.has(player, "TitanChat.admin"))
 				return getStaffChannel();
 		}
 		
 		return getDefaultChannel();
 	}
 	
 	/**
 	 * Gets the Staff Channel
 	 * 
 	 * @return The Staff Channel
 	 */
 	public Channel getStaffChannel() {
 		for (Channel channel : channels) {
 			if (channel.getType().equals(Type.STAFF))
 				return channel;
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Loads the Channel of the given name
 	 * 
 	 * @param name The Channel name
 	 * 
 	 * @return The loaded Channel
 	 */
 	public StandardChannel loadChannel(String name) {
 		StandardChannel channel = new StandardChannel(name);
 		
 		channel.setType(channel.getConfig().getString("type"));
 		
 		channel.setGlobal(channel.getConfig().getBoolean("global"));
 		
 		channel.setPassword(channel.getConfig().getString("password"));
 		
 		if (channel.getConfig().getStringList("admins") != null)
 			channel.getAdminList().addAll(channel.getConfig().getStringList("admins"));
 		
 		if (channel.getConfig().getStringList("blacklist") != null)
 			channel.getBlackList().addAll(channel.getConfig().getStringList("blacklist"));
 		
 		if (channel.getConfig().getStringList("followers") != null)
 			channel.getFollowerList().addAll(channel.getConfig().getStringList("followers"));
 		
 		if (channel.getConfig().getStringList("whitelist") != null)
 			channel.getWhiteList().addAll(channel.getConfig().getStringList("whitelist"));
 		
 		loadChannelVariables(channel);
 		
 		return channel;
 	}
 	
 	/**
 	 * Loads all channels
 	 * 
 	 * @throws Exception
 	 */
 	public void load() throws Exception {
 		if (!plugin.enableChannels()) { plugin.log(Level.INFO, "Channels disabled"); return; }
 		
 		for (CustomChannel channel : loader.load()) { if (exists(channel.getName())) { continue; } register(channel); }
 		customChAmount = channels.size();
 		
 		for (String fileName : plugin.getChannelDir().list()) {
 			if (exists(fileName.replace(".yml", "")) || fileName.equals("README.yml"))
 				continue;
 			
 			StandardChannel channel = loadChannel(fileName.replace(".yml", ""));
 			
 			if (channel == null)
 				continue;
 			
 			loadChannelVariables(channel);
 			
 			channels.add(channel);
 		}
 		
 		sortChannels();
 		
 		for (Player player : plugin.getServer().getOnlinePlayers()) {
 			if (getChannel(player) == null)
 				getSpawnChannel(player).join(player);
 		}
 		
 		plugin.log(Level.INFO, "No. of channels: " + (channelAmount = channels.size() - customChAmount));
 		plugin.log(Level.INFO, "No. of custom channels: " + customChAmount);
 		plugin.log(Level.INFO, "Channels loaded");
 	}
 	
 	/**
 	 * Loads the ChannelVariables of the Channel
 	 * 
 	 * @param channel The Channel to load
 	 */
 	public void loadChannelVariables(StandardChannel channel) {
 		ChannelVariables variables = channel.getVariables();
 		variables.setChatColour(channel.getConfig().getString("chat-display-colour"));
 		variables.setConvert(channel.getConfig().getBoolean("colour-code"));
 		
 		if (channel.getConfig().getString("format") != null)
 			variables.setFormat(channel.getConfig().getString("format"));
 		
 		variables.setNameColour(channel.getConfig().getString("name-display-colour"));
 		variables.setTag(channel.getConfig().getString("tag"));
 	}
 	
 	/**
 	 * Called when a Player is invited to join a Channel
 	 * 
 	 * @param channel The Channel invited into
 	 * 
 	 * @param invitor The one who invited
 	 * 
 	 * @param invitee The one who was invited
 	 */
 	public void onInvite(Channel channel, Player invitor, Player invitee) {
 		Map<String, List<String>> invitors = channelInvitors.get(channel);
 		List<String> invitorlist = (invitors.get(invitee.getName()) != null) ? invitors.get(invitee.getName()) : new ArrayList<String>();
 		invitorlist.add(invitor.getName());
 		invitors.put(invitee.getName(), invitorlist);
 		channelInvitors.put(channel, invitors);
 	}
 	
 	/**
 	 * Called when a Player responds to an invitation
 	 * 
 	 * @param channel The Channel invited into
 	 * 
 	 * @param invitee The one who was invited
 	 * 
 	 * @param accept True if the Player accepts the invitation
 	 */
 	public void onInviteRespond(Channel channel, Player invitee, boolean accept) {
 		Map<String, List<String>> invitors = channelInvitors.get(channel);
 		
 		for (String invitor : invitors.get(invitee.getName())) {
 			if (plugin.getPlayer(invitor) != null) {
 				if (accept)
 					plugin.sendInfo(plugin.getPlayer(invitor), invitee.getDisplayName() + " has accepted your invitation");
 				else
 					plugin.sendInfo(plugin.getPlayer(invitor), invitee.getDisplayName() + " has declined your invitation");
 			}
 		}
 		
 		invitors.remove(invitee.getName());
 		channelInvitors.put(channel, invitors);
 	}
 	
 	/**
 	 * Registers the Custom Channel
 	 * 
 	 * @param channel The Custom Channel to be registered
 	 */
 	public void register(Channel channel) {
 		channels.add(channel);
 	}
 	
 	/**
 	 * Reloads all Channels
 	 */
 	public void reload() {
 		for (Channel channel : channels) {
 			if (channel instanceof CustomChannel) {
 				((CustomChannel) channel).reload();
 			}
 			
 			if (channel instanceof StandardChannel) {
 				channel.reloadConfig();
 				loadChannelVariables((StandardChannel) channel);
 			}
 		}
 		
 		channels.clear();
 		
 		try { load(); } catch (Exception e) {}
 	}
 	
 	/**
 	 * Saves the JAR files of the Channels for future use
 	 * 
 	 * @param channel The Channel
 	 * 
 	 * @param jarFile The JAR file of the Channel
 	 */
 	protected void setJarFile(CustomChannel channel, JarFile jarFile) {
 		jarFiles.put(channel, jarFile);
 	}
 	
 	/**
 	 * Sorts the Channels
 	 */
 	public void sortChannels() {
 		List<Channel> channels = new ArrayList<Channel>();
 		List<String> names = new ArrayList<String>();
 		
 		for (Channel channel : this.channels) {
 			names.add(channel.getName());
 		}
 		
 		Collections.sort(names);
 		
 		for (String name : names) {
 			channels.add(getChannel(name));
 		}
 		
 		this.channels = channels;
 	}
 	
 	/**
 	 * Unloads the Channels
 	 */
 	public void unload() {
 		for (Channel channel : channels) { channel.save(); }
 		channels.clear();
 	}
 	
 	/**
 	 * Whitelists the Player to the Channel
 	 * 
 	 * @param player The Player to whitelist
 	 * 
 	 * @param channel The Channel to whitelist the Player to
 	 */
 	public void whitelistMember(Player player, Channel channel) {
 		db.i("Adding player " + player.getName() +
 				" to whitelist of channel " + channel.getName());
 		channel.getWhiteList().add(player.getName());
 		channel.save();
 		plugin.sendInfo(player, "You are now a Member of " + channel.getName());
 	}
 }
