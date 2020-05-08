 package com.titankingdoms.nodinchan.titanchat;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.chat.Chat;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.titankingdoms.nodinchan.titanchat.channel.Channel;
 import com.titankingdoms.nodinchan.titanchat.channel.ChannelManager;
 import com.titankingdoms.nodinchan.titanchat.channel.CustomChannel;
 import com.titankingdoms.nodinchan.titanchat.channel.Channel.Type;
 import com.titankingdoms.nodinchan.titanchat.command.TitanChatCommandHandler;
 import com.titankingdoms.nodinchan.titanchat.permissionshook.PermissionsHook;
 import com.titankingdoms.nodinchan.titanchat.support.Support;
 import com.titankingdoms.nodinchan.titanchat.support.SupportLoader;
 import com.titankingdoms.nodinchan.titanchat.util.ConfigManager;
 import com.titankingdoms.nodinchan.titanchat.util.Format;
 
 /*
  *     TitanChat 2.1.5
  *     Copyright (C) 2012  Nodin Chan <nodinchan@nodinchan.net>
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
 
 public class TitanChat extends JavaPlugin {
 	
 	protected static Logger log = Logger.getLogger("TitanLog");
 	
 	private TitanChatCommandHandler cmdHandler;
 	private ChannelManager chManager;
 	private ConfigManager configManager;
 	private Format format;
 	private PermissionsHook permHook;
 	private SupportLoader loader;
 	
 	private File channelConfigFile = null;
 	private FileConfiguration channelConfig = null;
 	
 	private Channel defaultChannel = null;
 	private Channel staffChannel = null;
 	
 	private boolean silenced = false;
 	private boolean vaultSetup = false;
 	
 	private List<Channel> channels;
 	private List<com.titankingdoms.nodinchan.titanchat.support.Command> cmds;
 	private List<CustomChannel> customChannels;
 	private List<Support> supports;
 	
 	private Permission perm;
 	private Chat chat;
 	
 	public void assignAdmin(Player player, Channel channel) {
 		channel.getAdminList().add(player.getName());
 		sendInfo(player, "You are now an Admin of " + channel.getName());
 	}
 	
 	public boolean channelExist(String name) {
 		return getChannel(name) != null;
 	}
 	
 	public void channelSwitch(Player player, Channel oldCh, Channel newCh) {
 		oldCh.leave(player);
 		newCh.join(player);
 	}
 	
 	public boolean correctPass(Channel channel, String password) {
 		return channel.getPassword().equals(password);
 	}
 	
 	public void createChannel(Player player, String name) {
 		Channel channel = new Channel(this, name, Type.PUBLIC);
 		channels.add(channel);
 		
 		assignAdmin(player, channel);
 		channelSwitch(player, getChannel(player), channel);
 		sendInfo(player, "You have created " + channel.getName() + " channel");
 	}
 	
 	public String createList(List<String> list) {
 		StringBuilder str = new StringBuilder();
 		
 		for (String item : list) {
 			if (str.length() > 0)
 				str.append(", ");
 			
 			str.append(item);
 		}
 		
 		return str.toString();
 	}
 	
 	public void deleteChannel(Player player, Channel channel) {
		List<String> kicklist = new ArrayList<String>();
		
 		for (String participant : channel.getParticipants()) {
 			if (getPlayer(participant) != null) {
				kicklist.add(participant);
 				sendWarning(getPlayer(participant), channel.getName() + " has been deleted");
 			}
 		}

		for (String kick : kicklist) {
			channelSwitch(getPlayer(kick), channel, getSpawnChannel(getPlayer(kick)));
		}
 		
 		channels.remove(channel);
 	}
 	
 	public File getAddonDir() {
 		return new File(getDataFolder(), "addons");
 	}
 	
 	public Channel getChannel(String channelName) {
 		for (Channel channel : channels) {
 			if (channel.getName().equalsIgnoreCase(channelName))
 				return channel;
 		}
 		
 		return null;
 	}
 	
 	public Channel getChannel(Player player) {
 		for (Channel channel : channels) {
 			if (channel.getParticipants().contains(player.getName()))
 				return channel;
 		}
 		
 		return null;
 	}
 	
 	public int getChannelAmount() {
 		return channels.size() - customChannels.size();
 	}
 	
 	public FileConfiguration getChannelConfig() {
 		if (channelConfig == null) {
 			reloadChannelConfig();
 		}
 		
 		return channelConfig;
 	}
 	
 	public ChannelManager getChannelManager() {
 		return chManager;
 	}
 	
 	public File getChannelsFolder() {
 		return new File(getDataFolder(), "channels");
 	}
 	
 	public List<com.titankingdoms.nodinchan.titanchat.support.Command> getCommands() {
 		return cmds;
 	}
 	
 	public ConfigManager getConfigManager() {
 		return configManager;
 	}
 	
 	public List<Channel> getChannels() {
 		return channels;
 	}
 	
 	public CustomChannel getCustomChannel(Channel channel) {
 		for (CustomChannel customChannel : customChannels) {
 			if (customChannel.getName().equals(channel.getName()))
 				return customChannel;
 		}
 		
 		return null;
 	}
 	
 	public List<CustomChannel> getCustomChannels() {
 		return customChannels;
 	}
 	
 	public Channel getDefaultChannel() {
 		if (defaultChannel == null) {
 			for (Channel channel : channels) {
 				if (channel.getType().equals(Type.DEFAULT)) {
 					defaultChannel = channel;
 					break;
 				}
 			}
 		}
 		
 		return defaultChannel;
 	}
 	
 	public String getExactName(String channelName) {
 		return getChannel(channelName).getName();
 	}
 	
 	public Format getFormat() {
 		return format;
 	}
 	
 	public String getFormat(String channelName) {
 		if (getConfig().get("channels." + getExactName(channelName) + ".format") != null && !getConfig().getString("channels." + getExactName(channelName) + ".format").equalsIgnoreCase(""))
 			return getConfig().getString("channels." + getExactName(channelName) + ".format");
 		
 		return getConfig().getString("formatting.format");
 	}
 	
 	public String getGroupPrefix(Player player) {
 		if (chat != null) {
 			String prefix = chat.getGroupPrefix(player.getWorld(), perm.getPrimaryGroup(player));
 			return (prefix != null) ? prefix : "";
 		}
 		
 		return permHook.getGroupPrefix(player);
 	}
 	
 	public String getGroupSuffix(Player player) {
 		if (chat != null) {
 			String suffix = chat.getGroupSuffix(player.getWorld(), perm.getPrimaryGroup(player));
 			return (suffix != null) ? suffix : "";
 		}
 		
 		return permHook.getGroupSuffix(player);
 	}
 	
 	public Player getPlayer(String name) {
 		return getServer().getPlayer(name);
 	}
 	
 	public String getPlayerPrefix(Player player) {
 		if (chat != null) {
 			String prefix = chat.getPlayerPrefix(player.getWorld(), player.getName());
 			return (prefix != null) ? prefix : "";
 		}
 		
 		return permHook.getPlayerPrefix(player);
 	}
 	
 	public String getPlayerSuffix(Player player) {
 		if (chat != null) {
 			String suffix = chat.getPlayerSuffix(player.getWorld(), player.getName());
 			return (suffix != null) ? suffix : "";
 		}
 		
 		return permHook.getPlayerSuffix(player);
 	}
 	
 	public Channel getSpawnChannel(Player player) {
 		if (has(player, "TitanChat.admin") && has(player, "TitanChat.adminspawn")) {
 			if (staffChannel != null)
 				return staffChannel;
 		}
 		
 		for (Channel channel : channels) {
 			if (has(player, "TitanChat.spawn." + channel.getName()) && !has(player, "TitanChat.forced." + channel.getName()) && channel.canAccess(player))
 				return channel;
 		}
 		
 		return defaultChannel;
 	}
 	
 	public Channel getStaffChannel() {
 		if (staffChannel == null) {
 			for (Channel channel : channels) {
 				if (channel.getType().equals(Type.STAFF)) {
 					staffChannel = channel;
 					break;
 				}
 			}
 		}
 		
 		return staffChannel;
 	}
 	
 	public SupportLoader getSupportLoader() {
 		return loader;
 	}
 	
 	public List<Support> getSupports() {
 		return supports;
 	}
 	
 	public File getSupportsFolder() {
 		return new File(getDataFolder(), "supports");
 	}
 	
 	public boolean has(Player player, String permission) {
 		if (perm != null)
 			return perm.has(player, permission);
 		
 		return permHook.has(player, permission);
 	}
 	
 	public boolean hasVoice(Player player) {
 		return has(player, "TitanChat.voice");
 	}
 	
 	public boolean isSilenced() {
 		return silenced;
 	}
 	
 	public boolean isStaff(Player player) {
 		if (has(player, "TitanChat.admin"))
 			return true;
 		
 		return false;
 	}
 	
 	public void log(Level level, String msg) {
 		log.log(level, "[" + this + "] " + msg);
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
 		if (!(sender instanceof Player)) {
 			if (args[0].equalsIgnoreCase("reload")) {
 				log(Level.INFO, "Reloading configs...");
 				
 				reloadConfig();
 				reloadChannelConfig();
 				
 				channels.clear();
 				
 				try { chManager.loadChannels(); } catch (Exception e) {}
 				
 				log(Level.INFO, "Configs reloaded");
 				return true;
 			}
 			
 			log(Level.INFO, "Please use commands in-game");
 			return true;
 		}
 		
 		String[] arguments = parseCommand(args);
 		
 		Player player = (Player) sender;
 		
 		if (args.length < 1) {
 			player.sendMessage(ChatColor.AQUA + "TitanChat Commands");
 			player.sendMessage(ChatColor.AQUA + "Command: /titanchat [command] [arguments]");
 			player.sendMessage(ChatColor.AQUA + "Alias: /tc [command] [arguments]");
 			sendInfo(player, "'/titanchat commands [page]' for command list");
 			return true;
 		}
 		
 		if (cmd.getName().equalsIgnoreCase("titanchat")) {
 			cmdHandler.onCommand(player, args[0], arguments);
 			return true;
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public void onDisable() {
 		log(Level.INFO, "is now disabling...");
 		
 		log(Level.INFO, "Saving channel information...");
 		
 		for (Channel channel : channels) {
 			if (channel.getType().equals(Type.CUSTOM))
 				getCustomChannel(channel).unload();
 			else
 				channel.unload();
 		}
 		
 		log(Level.INFO, "Clearing useless data...");
 		
 		channels.clear();
 		cmds.clear();
 		customChannels.clear();
 		supports.clear();
 		
 		log(Level.INFO, "is now disabled");
 	}
 	
 	@Override
 	public void onEnable() {
 		log(Level.INFO, "is now enabling...");
 		
 		File config = new File(getDataFolder(), "config.yml");
 		File channelConfig = new File(getDataFolder(), "channel.yml");
 		
 		if (!config.exists()) {
 			log(Level.INFO, "Loading default config");
 			getConfig().options().copyHeader(true);
 			getConfig().options().copyDefaults(true);
 			saveConfig();
 		}
 		
 		if (!channelConfig.exists()) {
 			log(Level.INFO, "Loading default channel players config");
 			getChannelConfig().options().copyHeader(true);
 			getChannelConfig().options().copyDefaults(true);
 			saveChannelConfig();
 		}
 		
 		if (getChannelsFolder().mkdir())
 			log(Level.INFO, "Loading channels folder...");
 		
 		if (getSupportsFolder().mkdir())
 			log(Level.INFO, "Loading supports folder...");
 		
 		cmdHandler = new TitanChatCommandHandler(this);
 		chManager = new ChannelManager(this);
 		configManager = new ConfigManager(this);
 		format = new Format(this);
 		loader = new SupportLoader(this);
 		permHook = new PermissionsHook(this);
 		
 		channels = new ArrayList<Channel>();
 		cmds = new ArrayList<com.titankingdoms.nodinchan.titanchat.support.Command>();
 		customChannels = new ArrayList<CustomChannel>();
 		supports = new ArrayList<Support>();
 		
 		PluginManager pm = getServer().getPluginManager();
 		
 		vaultSetup = setupVault();
 		
 		pm.registerEvents(permHook, this);
 		
 		try { supports.addAll(loader.loadSupports()); } catch (Exception e) {}
 		
 		try { chManager.loadChannels(); } catch (Exception e) {}
 		
 		if (getDefaultChannel() == null) {
 			log(Level.WARNING, "Default channel not defined");
 			pm.disablePlugin(this);
 			return;
 		}
 		
 		log(Level.INFO, "Default Channel is " + getDefaultChannel().getName());
 		log(Level.INFO, "Staff Channel is " + getStaffChannel().getName());
 		
 		pm.registerEvents(new TitanChatListener(this), this);
 		
 		for (Player player : getServer().getOnlinePlayers()) {
 			getSpawnChannel(player).join(player);
 		}
 		
 		log(Level.INFO, "is now enabled");
 	}
 	
 	public String[] parseCommand(String[] args) {
 		StringBuilder str = new StringBuilder();
 		
 		for (String arg : args) {
 			if (str.length() > 0)
 				str.append(" ");
 			
 			if (arg.equals(args[0]))
 				continue;
 			
 			str.append(arg);
 		}
 		
 		return (str.toString().equals("")) ? new String[] {} : str.toString().split(" ");
 	}
 	
 	public void registerCommand(com.titankingdoms.nodinchan.titanchat.support.Command cmd) {
 		cmds.add(cmd);
 	}
 	
 	public void reloadChannelConfig() {
 		if (channelConfigFile == null) { channelConfigFile = new File(getDataFolder(), "channel.yml"); }
 		
 		channelConfig = YamlConfiguration.loadConfiguration(channelConfigFile);
 		
 		InputStream defConfigStream = getResource("channel.yml");
 		
 		if (defConfigStream != null) {
 			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
 			channelConfig.setDefaults(defConfig);
 		}
 	}
 	
 	public void saveChannelConfig() {
 		if (channelConfig == null || channelConfigFile == null) { return; }
 		try { channelConfig.save(channelConfigFile); } catch (IOException e) { log(Level.SEVERE, "Could not save config to " + channelConfigFile); }
 	}
 	
 	public void sendInfo(Player player, String info) {
 		player.sendMessage("[TitanChat] " + ChatColor.GOLD + info);
 	}
 	
 	public void sendWarning(Player player, String warning) {
 		player.sendMessage("[TitanChat] " + ChatColor.RED + warning);
 	}
 	
 	public void setSilenced(boolean silenced) {
 		this.silenced = silenced;
 	}
 	
 	public boolean setupChat() {
 		RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(Chat.class);
 		
 		if (chatProvider != null)
 			chat = chatProvider.getProvider();
 		
 		return chat != null;
 	}
 	
 	public boolean setupPermissions() {
 		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(Permission.class);
 		
 		if (permissionProvider != null)
 			perm = permissionProvider.getProvider();
 		
 		return perm != null;
 	}
 	
 	public boolean setupVault() {
 		if (getServer().getPluginManager().getPlugin("Vault") != null) {
 			setupChat();
 			setupPermissions();
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public boolean useDefaultFormat() {
 		return getConfig().getBoolean("formatting.use-built-in");
 	}
 	
 	public boolean vaultSetup() {
 		return vaultSetup;
 	}
 	
 	public void whitelistMember(Player player, String channelName) {
 		Channel channel = getChannel(channelName);
 		channel.getWhiteList().add(player.getName());
 		sendInfo(player, "You are now a Member of " + channel.getName());
 	}
 }
