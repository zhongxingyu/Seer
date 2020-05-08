 package com.titankingdoms.nodinchan.titanchat;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.chat.Chat;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.titankingdoms.nodinchan.titanchat.addon.AddonManager;
 import com.titankingdoms.nodinchan.titanchat.channel.ChannelManager;
 import com.titankingdoms.nodinchan.titanchat.command.CommandManager;
 import com.titankingdoms.nodinchan.titanchat.debug.Debugger;
 import com.titankingdoms.nodinchan.titanchat.permissions.PermissionsHook;
 import com.titankingdoms.nodinchan.titanchat.permissions.WildcardNodes;
 import com.titankingdoms.nodinchan.titanchat.util.FormatHandler;
 import com.titankingdoms.nodinchan.titanpluginstats.ResultCheck;
 import com.titankingdoms.nodinchan.titanpluginstats.TitanPluginStats;
 
 /*
  *     TitanChat 3.1
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
 
 /**
  * TitanChat - Main Class
  * 
  * @author NodinChan
  *
  */
 public final class TitanChat extends JavaPlugin {
 	
 	private static TitanChat instance;
 	
	private final String url = getDescription().getWebsite();
 	
 	private static final Logger log = Logger.getLogger("TitanLog");
 	private static final Debugger db = new Debugger(1);
 	
 	private AddonManager addonManager;
 	private ChannelManager chManager;
 	private CommandManager cmdManager;
 	private FormatHandler format;
 	private PermissionsHook permHook;
 	
 	private boolean silenced = false;
 	
 	private List<String> muted;
 	
 	private Permission perm;
 	private Chat chat;
 	
 	/**
 	 * Creates a new list with items seperated with commas
 	 * 
 	 * @param list The string list to create a list from
 	 * 
 	 * @return The created list of items
 	 */
 	public String createList(List<String> list) {
 		db.i("Creating string out of stringlist: " + list.toString());
 		
 		StringBuilder str = new StringBuilder();
 		
 		for (String item : list) {
 			if (str.length() > 0)
 				str.append(", ");
 			
 			str.append(item);
 		}
 		
 		return str.toString();
 	}
 	
 	/**
 	 * Check if Channels are enabled
 	 * 
 	 * @return True if Channels are enabled
 	 */
 	public boolean enableChannels() {
 		return getConfig().getBoolean("channels.enable-channels");
 	}
 	
 	/**
 	 * Check if join messages are enabled
 	 * 
 	 * @return True if join messages are enabled
 	 */
 	public boolean enableJoinMessage() {
 		return getConfig().getBoolean("channels.messages.join");
 	}
 	
 	/**
 	 * Check if leave messages are enabled
 	 * 
 	 * @return True if leave messages are enabled
 	 */
 	public boolean enableLeaveMessage() {
 		return getConfig().getBoolean("channels.messages.leave");
 	}
 	
 	/**
 	 * Get the Addon directory
 	 * 
 	 * @return The Addon directory
 	 */
 	public File getAddonDir() {
 		return new File(getDataFolder(), "addons");
 	}
 	
 	/**
 	 * Get the AddonManager
 	 * 
 	 * @return The AddonManager
 	 */
 	public AddonManager getAddonManager() {
 		return addonManager;
 	}
 	
 	/**
 	 * Get the Channel directory
 	 * 
 	 * @return The Channel directory
 	 */
 	public File getChannelDir() {
 		return new File(getDataFolder(), "channels");
 	}
 	
 	/**
 	 * Get the ChannelManager
 	 * 
 	 * @return The ChannelManager
 	 */
 	public ChannelManager getChannelManager() {
 		return chManager;
 	}
 	
 	/**
 	 * Get the Command directory
 	 * 
 	 * @return The Command directory
 	 */
 	public File getCommandDir() {
 		return new File(getAddonDir(), "commands");
 	}
 	
 	/**
 	 * Get the CommandManager
 	 * 
 	 * @return The CommandManager
 	 */
 	public CommandManager getCommandManager() {
 		return cmdManager;
 	}
 	
 	/**
 	 * Get the Custom Channel directory
 	 * 
 	 * @return The Custom Channel directory
 	 */
 	public File getCustomChannelDir() {
 		return new File(getAddonDir(), "channels");
 	}
 	
 	/**
 	 * Get the FormatHandler
 	 * 
 	 * @return The FormatHandler
 	 */
 	public FormatHandler getFormatHandler() {
 		return format;
 	}
 	
 	/**
 	 * Get the group prefix of the Player
 	 * 
 	 * @param player The Player to find for
 	 * 
 	 * @return The group prefix
 	 */
 	public String getGroupPrefix(Player player) {
 		db.i("Getting group prefix of player " + player.getName());
 		
 		if (chat != null) {
 			String prefix = chat.getGroupPrefix(player.getWorld(), perm.getPrimaryGroup(player));
 			db.i("Returning: " + prefix);
 			return (prefix != null) ? prefix : "";
 		}
 
 		db.i("Returning PermissionsHook group prefix");
 		return permHook.getGroupPrefix(player);
 	}
 	
 	/**
 	 * Get the group suffix
 	 * 
 	 * @param player The Player to find for
 	 * 
 	 * @return The group suffix
 	 */
 	public String getGroupSuffix(Player player) {
 		db.i("Getting group suffix of player " + player.getName());
 		
 		if (chat != null) {
 			String suffix = chat.getGroupSuffix(player.getWorld(), perm.getPrimaryGroup(player));
 			db.i("Returning: " + suffix);
 			return (suffix != null) ? suffix : "";
 		}
 		
 		db.i("Returning PermissionsHook group suffix");
 		return permHook.getGroupSuffix(player);
 	}
 	
 	/**
 	 * Gets an instance of this
 	 * 
 	 * @return TitanChat instance
 	 */
 	public static TitanChat getInstance() {
 		return instance;
 	}
 	
 	@Override
 	public Logger getLogger() {
 		return log;
 	}
 	
 	public PermissionsHook getHook() {
 		return permHook;
 	}
 	
 	/**
 	 * Get Player by name
 	 * 
 	 * @param name The name of the Player
 	 * 
 	 * @return The Player with the name
 	 */
 	public Player getPlayer(String name) {
 		return getServer().getPlayer(name);
 	}
 	
 	/**
 	 * Get the prefix of the Player
 	 * 
 	 * @param player The Player to get the prefix from
 	 * 
 	 * @return The prefix of the Player
 	 */
 	public String getPlayerPrefix(Player player) {
 		db.i("Getting prefix of player: " + player.getName());
 		
 		if (chat != null) {
 			String prefix = chat.getPlayerPrefix(player.getWorld(), player.getName());
 			db.i("Returning: " + prefix);
 			return (prefix != null) ? prefix : getGroupPrefix(player);
 		}
 		
 		db.i("Returning PermissionsHook player prefix");
 		return permHook.getPlayerPrefix(player);
 	}
 	
 	/**
 	 * Get the suffix of the Player
 	 * 
 	 * @param player The Player to get the suffix from
 	 * 
 	 * @return The suffix of the player
 	 */
 	public String getPlayerSuffix(Player player) {
 		db.i("Getting suffix of player: " + player.getName());
 		
 		if (chat != null) {
 			String suffix = chat.getPlayerSuffix(player.getWorld(), player.getName());
 			db.i("Returning: " + suffix);
 			return (suffix != null) ? suffix : "";
 		}
 		
 		db.i("Returning PermissionsHook player suffix");
 		return permHook.getPlayerSuffix(player);
 	}
 	
 	/**
 	 * Gets the Wildcard avoider
 	 * 
 	 * @return The Wildcard avoider
 	 */
 	public WildcardNodes getWildcardAvoider() {
 		return permHook.getWildcardAvoider();
 	}
 	
 	/**
 	 * Check if the Player has the permission
 	 * 
 	 * @param player The Player to check
 	 * 
 	 * @param permission The permission
 	 * 
 	 * @return True if the Player has the permission
 	 */
 	public boolean has(Player player, String permission) {
 		if (perm != null)
 			return perm.has(player, permission);
 		
 		return permHook.has(player, permission);
 	}
 	
 	/**
 	 * Check if the Player has voice
 	 * 
 	 * @param player The Player to check
 	 * 
 	 * @return True if the Player has TitanChat.voice
 	 */
 	public boolean hasVoice(Player player) {
 		return has(player, "TitanChat.voice");
 	}
 	
 	/**
 	 * Check if the Server is silenced
 	 * 
 	 * @return True if the Server is silenced
 	 */
 	public boolean isSilenced() {
 		return silenced;
 	}
 	
 	/**
 	 * Check if the Player is staff
 	 * 
 	 * @param player The Player to check
 	 * 
 	 * @return True if the Player has TitanChat.admin
 	 */
 	public boolean isStaff(Player player) {
 		return has(player, "TitanChat.admin");
 	}
 	
 	/**
 	 * Sends the message to the log
 	 * 
 	 * @param level Level of the announcement
 	 * 
 	 * @param msg The message to send
 	 */
 	public void log(Level level, String msg) {
 		log.log(level, "[" + this + "] " + msg);
 	}
 	
 	/**
 	 * Mute/Unmute the Player
 	 * 
 	 * @param player The Player to mute/unmute
 	 * 
 	 * @param mute Whether to mute or unmute
 	 */
 	public void mute(Player player, boolean mute) {
 		db.i((mute ? "" : "un") + "muting player");
 		
 		if (mute)
 			muted.add(player.getName());
 		else
 			muted.remove(player.getName());
 	}
 	
 	/**
 	 * Check if player is muted
 	 * 
 	 * @param player The Player to check
 	 * 
 	 * @return True if the Player is muted
 	 */
 	public boolean muted(Player player) {
 		return muted.contains(player.getName());
 	}
 	
 	/**
 	 * Called when a Player uses a command
 	 * 
 	 * @param sender The sender who sent the command
 	 * 
 	 * @param cmd The Command used
 	 * 
 	 * @param label The exact word the Player used
 	 * 
 	 * @param args The list of words that follows
 	 * 
 	 * @return True if the Command is executed
 	 */
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		db.i("onCommand: " + cmd.getName());
 		
 		if (cmd.getName().equals("titanchat")) {
 			if (args.length < 1) {
 				db.i("onCommand: No arguments!");
 				
 				sender.sendMessage(ChatColor.AQUA + "You are running " + this);
 				
 				if (sender instanceof Player)
 					sendInfo((Player) sender, "\"/titanchat commands [page]\" for command list");
 				else
 					log(Level.INFO, "\"/titanchat commands [page]\" for command list");
 				
 				return true;
 			}
 			
 			if (!(sender instanceof Player)) {
 				if (args[0].equalsIgnoreCase("reload")) {
 					log(Level.INFO, "Reloading configs...");
 					reloadConfig();
 					chManager.reload();
 					log(Level.INFO, "Configs reloaded");
 					return true;
 				}
 				
 				log(Level.INFO, "Please use commands in-game"); return true;
 			}
 
 			db.i("CommandManager executing command:");
 			cmdManager.execute((Player) sender, args[0], parseCommand(args));
 			return true;
 		}
 		
 		if (cmd.getName().equalsIgnoreCase("broadcast")) {
 			if (!(sender instanceof Player)) {
 				if (!getConfig().getBoolean("broadcast.server.enable")) {
 					log(Level.WARNING, "Command disabled");
 					return true;
 				}
 				
 				String message = getConfig().getString("broadcast.server.format");
 				
 				StringBuilder str = new StringBuilder();
 				
 				for (String word : args) {
 					if (str.length() > 0)
 						str.append(" ");
 					
 					str.append(word);
 				}
 				
 				message = message.replace("%message", str.toString());
 				
 				getServer().broadcastMessage(getFormatHandler().colourise(message));
 				getLogger().info("<Server> " + getFormatHandler().decolourise(str.toString()));
 				return true;
 			}
 			
 			if (!getConfig().getBoolean("broadcast.player.enable")) {
 				sendWarning((Player) sender, "Command disabled");
 				return true;
 			}
 			
 			if (has((Player) sender, "TitanChat.broadcast"))
 				try { cmdManager.execute((Player) sender, "broadcast", args); } catch (Exception e) {}
 			else
 				sendWarning((Player) sender, "You do not have permission");
 			
 			return true;
 		}
 		
 		if (cmd.getName().equalsIgnoreCase("me")) {
 			if (!(sender instanceof Player)) {
 				if (!getConfig().getBoolean("emote.server.enable")) {
 					log(Level.WARNING, "Command disabled");
 					return true;
 				}
 				
 				String message = getConfig().getString("emote.server.format");
 				
 				StringBuilder str = new StringBuilder();
 				
 				for (String word : args) {
 					if (str.length() > 0)
 						str.append(" ");
 					
 					str.append(word);
 				}
 				
 				message = message.replace("%action", str.toString());
 				getServer().broadcastMessage(getFormatHandler().colourise(message));
 				getLogger().info("* Server " + getFormatHandler().decolourise(str.toString()));
 				return true;
 			}
 			
 			if (!getConfig().getBoolean("emote.player.enable")) {
 				sendWarning((Player) sender, "Command disabled");
 				return true;
 			}
 			
 			if (has((Player) sender, "TitanChat.emote.server"))
 				try { cmdManager.execute((Player) sender, "me", args); } catch (Exception e) {}
 			else
 				sendWarning((Player) sender, "You do not have permission");
 			
 			return true;
 		}
 		
 		if (cmd.getName().equalsIgnoreCase("whisper")) {
 			if (!(sender instanceof Player)) {
 				if (!getConfig().getBoolean("whisper.server.enable")) {
 					log(Level.WARNING, "Command disabled");
 					return true;
 				}
 				
 				if (getPlayer(args[0]) == null) {
 					log(Level.WARNING, "Player not online");
 					return true;
 				}
 				
 				String message = getConfig().getString("whisper.server.format");
 				
 				StringBuilder str = new StringBuilder();
 				
 				for (String word : args) {
 					if (str.length() > 0)
 						str.append(" ");
 					
 					str.append(word);
 				}
 				
 				if (args[0].equalsIgnoreCase("console")) {
 					log(Level.INFO, "You whispered to yourself: " + str.toString());
 					log(Level.INFO, message.replace("%message", str.toString()));
 					return true;
 				}
 				
 				if (getPlayer(args[0]) != null) {
 					log(Level.INFO, "You whispered to " + getPlayer(args[0]).getName() + ": " + str.toString());
 					getPlayer(args[0]).sendMessage(message.replace("%message", getFormatHandler().colourise(str.toString())));
 					getLogger().info("[Server -> " + getPlayer(args[0]).getName() + "] " + str.toString());
 					
 				} else { log(Level.WARNING, "Player not online"); }
 			}
 			
 			if (!getConfig().getBoolean("whisper.player.enable")) {
 				sendWarning((Player) sender, "Command disabled");
 				return true;
 			}
 			
 			if (has((Player) sender, "TitanChat.whisper"))
 				try { cmdManager.execute((Player) sender, "whisper", args); } catch (Exception e) {}
 			else
 				sendWarning((Player) sender, "You do not have permission");
 			
 			return true;
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Called when the Plugin disables
 	 */
 	@Override
 	public void onDisable() {
 		log(Level.INFO, "is now disabling...");
 		
 		getServer().getScheduler().cancelTasks(this);
 		
 		log(Level.INFO, "Unloading managers...");
 
 		addonManager.unload();
 		chManager.unload();
 		cmdManager.unload();
 		
 		log(Level.INFO, "is now disabled");
 	}
 	
 	/**
 	 * Called when the Plugin enables
 	 */
 	@Override
 	public void onEnable() {
 		log(Level.INFO, "is now enabling...");
 		instance = this;
 		
 		int s = getServer().getScheduler().scheduleAsyncRepeatingTask(this, new TitanPluginStats(this, url), 0, 108000);
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new ResultCheck(this, s, url), 1, 108000);
 		
 		if (getAddonDir().mkdir())
 			log(Level.INFO, "Creating addon directory...");
 		
 		if (getCustomChannelDir().mkdir())
 			log(Level.INFO, "Creating custom channel directory...");
 		
 		if (getCommandDir().mkdir())
 			log(Level.INFO, "Creating commands directory");
 		
 		if (getChannelDir().mkdir()) {
 			log(Level.INFO, "Creating channel directory...");
 			saveResource("channels/Default.yml", false);
 			saveResource("channels/Password.yml", false);
 			saveResource("channels/Private.yml", false);
 			saveResource("channels/Public.yml", false);
 			saveResource("channels/README.yml", false);
 			saveResource("channels/Staff.yml", false);
 		}
 		
 		muted = new ArrayList<String>();
 		
 		addonManager = new AddonManager(this);
 		chManager = new ChannelManager(this);
 		cmdManager = new CommandManager(this);
 		format = new FormatHandler(this);
 		permHook = new PermissionsHook(this);
 		
 		File config = new File(getDataFolder(), "config.yml");
 		
 		if (!config.exists()) {
 			log(Level.INFO, "Loading default config");
 			saveResource("config.yml", false);
 		}
 		
 		PluginManager pm = getServer().getPluginManager();
 		
 		Debugger.load(this);
 		
 		if (pm.getPlugin("Vault") != null) {
 			setupChatService();
 			setupPermissionService();
 		}
 		
 		pm.registerEvents(permHook, this);
 		pm.registerEvents(new TitanChatListener(this), this);
 		
 		try { chManager.load(); } catch (Exception e) {}
 		
 		addonManager.load();
 		cmdManager.load();
 		
 		if (chManager.getDefaultChannel() == null) {
 			log(Level.SEVERE, "A default channel is not defined");
 			pm.disablePlugin(this);
 			return;
 		}
 		
 		log(Level.INFO, "is now enabled");
 	}
 	
 	/**
 	 * Parses the command arguments
 	 * 
 	 * @param args Arguments to parse
 	 * 
 	 * @return The parsed arguments
 	 */
 	public String[] parseCommand(String[] args) {
 		db.i("Parsing command");
 		
 		StringBuilder str = new StringBuilder();
 		
 		for (String arg : args) {
 			if (str.length() > 0)
 				str.append(" ");
 			
 			if (arg.equals(args[0]))
 				continue;
 			
 			str.append(arg);
 		}
 		
 		db.i("Command arguments: " + str.toString());
 		
 		return (str.toString().equals("")) ? new String[] {} : str.toString().split(" ");
 	}
 	
 	/**
 	 * Sends an info to the Player
 	 * 
 	 * @param player The Player to send to
 	 * 
 	 * @param info The message
 	 */
 	public void sendInfo(Player player, String info) {
 		db.i("@" + player.getName() + ": " + info);
 		
 		player.sendMessage("[TitanChat] " + ChatColor.GOLD + info);
 	}
 	
 	/**
 	 * Sends an info to all the Players within the list
 	 * 
 	 * @param players String list of Players to send to
 	 * 
 	 * @param info The message
 	 */
 	public void sendInfo(List<String> players, String info) {
 		for (String player : players) {
 			if (getPlayer(player) != null)
 				sendInfo(getPlayer(player), info);
 		}
 	}
 	
 	/**
 	 * Sends a warning to the Player
 	 * 
 	 * @param player The Player to send to
 	 * 
 	 * @param warning The message
 	 */
 	public void sendWarning(Player player, String warning) {
 		db.i("Warning @" + player.getName() + ": " + warning);
 		
 		player.sendMessage("[TitanChat] " + ChatColor.RED + warning);
 	}
 	
 	/**
 	 * Sends a warning to all the Players within the list
 	 * 
 	 * @param players String list of Players to send to
 	 * 
 	 * @param warning The message
 	 */
 	public void sendWarning(List<String> players, String warning) {
 		for (String player : players) {
 			if (getPlayer(player) != null)
 				sendWarning(getPlayer(player), warning);
 		}
 	}
 	
 	/**
 	 * Sets whether the Server is silenced
 	 * 
 	 * @param silenced True if setting the Server to silenced
 	 */
 	public void setSilenced(boolean silenced) {
 		db.i("Setting silenced to " + silenced);
 		this.silenced = silenced;
 	}
 	
 	/**
 	 * Sets up the Chat Service of Vault
 	 * 
 	 * @return True if a Chat Service is present
 	 */
 	public boolean setupChatService() {
 		RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(Chat.class);
 		
 		if (chatProvider != null)
 			chat = chatProvider.getProvider();
 
 		db.i("Vault Chat Service is set up: " + (chat != null));
 		return chat != null;
 	}
 	
 	/**
 	 * Sets up the Permission Service of Vault
 	 * 
 	 * @return True if a Permission Service is present
 	 */
 	public boolean setupPermissionService() {
 		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(Permission.class);
 		
 		if (permissionProvider != null)
 			perm = permissionProvider.getProvider();
 		
 		db.i("Vault Permission Service is set up: " + (perm != null));
 		return perm != null;
 	}
 	
 	/**
 	 * Check if default formatting should be used
 	 * 
 	 * @return True if default formatting should be used
 	 */
 	public boolean useDefaultFormat() {
 		return getConfig().getBoolean("formatting.use-built-in");
 	}
 	
 	/**
 	 * Check if Vault is used
 	 * 
 	 * @return True if Vault is used
 	 */
 	public boolean usingVault() {
 		return perm != null;
 	}
 }
