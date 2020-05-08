 package com.titankingdoms.nodinchan.titanchat;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.Method;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 import java.util.Arrays;
 import java.util.List;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.persistence.PersistenceException;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import com.titankingdoms.nodinchan.titanchat.addon.AddonManager;
 import com.titankingdoms.nodinchan.titanchat.channel.ChannelManager;
 import com.titankingdoms.nodinchan.titanchat.command.CommandManager;
 import com.titankingdoms.nodinchan.titanchat.metrics.Metrics;
 import com.titankingdoms.nodinchan.titanchat.util.Debugger;
 import com.titankingdoms.nodinchan.titanchat.util.FormatHandler;
 import com.titankingdoms.nodinchan.titanchat.util.PermsBridge;
 import com.titankingdoms.nodinchan.titanchat.util.displayname.DisplayName;
 import com.titankingdoms.nodinchan.titanchat.util.displayname.DisplayNameChanger;
 import com.titankingdoms.nodinchan.titanchat.util.variable.Variable;
 
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
  * TitanChat - Main Class
  * 
  * @author NodinChan
  *
  */
 public final class TitanChat extends JavaPlugin {
 	
 	private static TitanChat instance;
 	
 	private String NAME;
 	
 	private static final Logger log = Logger.getLogger("TitanLog");
 	private static final Debugger db = new Debugger(1);
 	
 	private AddonManager addonManager;
 	private ChannelManager chManager;
 	private CommandManager cmdManager;
 	private DisplayNameChanger displayname;
 	private FormatHandler format;
 	private PermsBridge permBridge;
 	private Variable variable;
 	
 	private boolean silenced = false;
 	
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
 	 * Creates a new list with items seperated with commas
 	 * 
 	 * @param array The string array to create a list from
 	 * 
 	 * @return The created list of items
 	 */
 	public String createList(String[] array) {
 		return createList(Arrays.asList(array));
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
 	 * Gets the AddonManager
 	 * 
 	 * @return The AddonManager
 	 */
 	public AddonManager getAddonManager() {
 		return addonManager;
 	}
 	
 	/**
 	 * Gets the Channel directory
 	 * 
 	 * @return The Channel directory
 	 */
 	public File getChannelDir() {
 		return new File(getDataFolder(), "channels");
 	}
 	
 	/**
 	 * Gets the ChannelManager
 	 * 
 	 * @return The ChannelManager
 	 */
 	public ChannelManager getChannelManager() {
 		return chManager;
 	}
 	
 	/**
 	 * Gets the CommandManager
 	 * 
 	 * @return The CommandManager
 	 */
 	public CommandManager getCommandManager() {
 		return cmdManager;
 	}
 	
 	@Override
 	public List<Class<?>> getDatabaseClasses() {
 		return Arrays.asList(new Class<?>[] { DisplayName.class });
 	}
 	
 	/**
 	 * Gets the DisplayNameChanger
 	 * 
 	 * @return The DisplayNameChanger of TitanChat
 	 */
 	public DisplayNameChanger getDisplayNameChanger() {
 		return displayname;
 	}
 	
 	/**
 	 * Gets the FormatHandler
 	 * 
 	 * @return The FormatHandler
 	 */
 	public FormatHandler getFormatHandler() {
 		return format;
 	}
 	
 	/**
 	 * Gets an instance of this
 	 * 
 	 * @return TitanChat instance
 	 */
 	public static TitanChat getInstance() {
 		return instance;
 	}
 	
 	/**
 	 * Gets the Logger of the plugin
 	 */
 	@Override
 	public Logger getLogger() {
 		return log;
 	}
 	
 	/**
 	 * Gets OfflinePlayer by name
 	 * 
 	 * @param name The name of the OfflinePlayer
 	 * 
 	 * @return The OfflinePlayer with the name
 	 */
 	public OfflinePlayer getOfflinePlayer(String name) {
 		OfflinePlayer player = getServer().getOfflinePlayer(name);
 		return player;
 	}
 	
 	/**
 	 * Gets the PermsBridge
 	 * 
 	 * @return The built-in PermsBridge
 	 */
 	public PermsBridge getPermsBridge() {
 		return permBridge;
 	}
 	
 	/**
 	 * Gets Player by name
 	 * 
 	 * @param name The name of the Player
 	 * 
 	 * @return The Player with the name
 	 */
 	public Player getPlayer(String name) {
 		OfflinePlayer player = getOfflinePlayer(name);
 		return (player.isOnline()) ? player.getPlayer() : null;
 	}
 	
 	/**
 	 * Gets the Variable manager
 	 * 
 	 * @return The Variable manager
 	 */
 	public Variable getVariableManager() {
 		return variable;
 	}
 
 	/**
 	 * Initialises the NCLib
 	 * 
 	 * @return True if the Lib is initialised
 	 */
 	private boolean initLib() {
 		try {
 			File destination = new File(getDataFolder().getParentFile().getParentFile(), "lib");
 			destination.mkdirs();
 			
 			File lib = new File(destination, "NC-BukkitLib.jar");
 			
 			boolean download = false;
 			
 			if (!lib.exists()) {
 				System.out.println("Missing NC-Bukkit lib");
 				download = true;
 				
 			} else {
 				JarFile jarFile = new JarFile(lib);
 				
 				double version = 0;
 				
 				if (jarFile.getEntry("version.yml") != null) {
 					JarEntry element = jarFile.getJarEntry("version.yml");
 					BufferedReader reader = new BufferedReader(new InputStreamReader(jarFile.getInputStream(element)));
 					version = Double.parseDouble(reader.readLine().substring(9).trim());
 					
 				} else {
 					System.out.println("Missing version.yml");
 					download = true;
 				}
 				
 				if (!download) {
 					if (version == 0) {
 						System.out.println("NC-Bukkit lib outdated");
 						download = true;
 						
 					} else {
 						HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://www.nodinchan.com/NC-BukkitLib/version.yml").openConnection();
 						BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
 						
 						if (Double.parseDouble(reader.readLine().replace("NC-BukkitLib Version ", "").trim()) > version) {
 							System.out.println("NC-Bukkit lib outdated");
 							download = true;
 						}
 					}
 				}
 			}
 			
 			if (download) {
 				System.out.println("Downloading NC-Bukkit lib...");
 				URL url = new URL("http://www.nodinchan.com/NC-BukkitLib/NC-BukkitLib.jar");
 				ReadableByteChannel rbc = Channels.newChannel(url.openStream());
 				FileOutputStream output = new FileOutputStream(lib);
 				output.getChannel().transferFrom(rbc, 0, 1 << 24);
 				System.out.println("Downloaded NC-Bukkit lib");
 			}
 			
 			URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
 			
 			for (URL url : sysLoader.getURLs()) {
 				if (url.sameFile(lib.toURI().toURL()))
 					return true;
 			}
 			
 			try {
 				Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
 				method.setAccessible(true);
 				method.invoke(sysLoader, lib.toURI().toURL());
 				
 			} catch (Exception e) { return false; }
 			
 			return true;
 			
 		} catch (Exception e) { e.printStackTrace(); }
 		
 		return false;
 	}
 	
 	/**
 	 * Initialises Metrics
 	 * 
 	 * @return True is Metrics is initialised
 	 */
 	private boolean initMetrics() {
 		log(Level.INFO, "Hooking Metrics");
 		
 		try {
 			Metrics metrics = new Metrics(this);
 			
 			if (metrics.isOptOut())
 				return true;
 			
 			return metrics.start();
 			
 		} catch (Exception e) { return false; }
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
 		return permBridge.has(player, "TitanChat.staff");
 	}
 	
 	/**
 	 * Sends the message to the log
 	 * 
 	 * @param level Level of the announcement
 	 * 
 	 * @param msg The message to send
 	 */
 	public void log(Level level, String msg) {
 		log.log(level, "[" + NAME + "] " + msg);
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
 					addonManager.unload();
 					chManager.unload();
 					cmdManager.unload();
 					variable.unload();
 					addonManager.load();
 					chManager.load();
 					cmdManager.load();
 					format.load();
 					log(Level.INFO, "Configs reloaded");
 					return true;
 				}
 				
 				if (args[0].equalsIgnoreCase("broadcast")) {
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
 					
 					String[] lines = getFormatHandler().regroup(message, str.toString());
 					
 					for (int line = 0; line < lines.length; line++) {
 						if (line < 1)
 							getServer().broadcastMessage(getFormatHandler().colourise(message.replace("%message", lines[0])));
 						else
 							getServer().broadcastMessage(lines[line]);
 					}
 					
 					String console = "<" + ChatColor.RED + "Server" + ChatColor.RESET + "> ";
 					
 					getServer().getConsoleSender().sendMessage(console + message.replace("%message", message.replace("%message", str.toString())));
 					return true;
 				}
 				
 				log(Level.INFO, "Please use commands in-game");
 				return true;
 			}
 
 			db.i("CommandManager executing command:");
 			cmdManager.execute((Player) sender, args[0], Arrays.copyOfRange(args, 1, args.length));
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
 				
 				String[] lines = getFormatHandler().regroup(message, str.toString());
 				
 				for (int line = 0; line < lines.length; line++) {
 					if (line < 1)
 						getServer().broadcastMessage(getFormatHandler().colourise(message.replace("%message", lines[0])));
 					else
 						getServer().broadcastMessage(lines[line]);
 				}
 				
 				String console = "<" + ChatColor.RED + "Server" + ChatColor.RESET + "> ";
 				
 				getServer().getConsoleSender().sendMessage(console + message.replace("%message", message.replace("%message", str.toString())));
 				return true;
 			}
 			
 			if (!getConfig().getBoolean("broadcast.player.enable")) {
 				sendWarning((Player) sender, "Command disabled");
 				return true;
 			}
 			
 			if (permBridge.has((Player) sender, "TitanChat.broadcast"))
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
 				
 				String[] lines = getFormatHandler().regroup(message, str.toString());
 				
 				for (int line = 0; line < lines.length; line++) {
 					if (line < 1)
 						getServer().broadcastMessage(getFormatHandler().colourise(message.replace("%action", lines[0])));
 					else
 						getServer().broadcastMessage(getFormatHandler().colourise(lines[line]));
 				}
 				
 				String console = "* " + ChatColor.RED + "Server " + ChatColor.RESET;
 				
 				getServer().getConsoleSender().sendMessage(console + str.toString());
 				return true;
 			}
 			
 			if (!getConfig().getBoolean("emote.player.enable")) {
 				sendWarning((Player) sender, "Command disabled");
 				return true;
 			}
 			
 			if (permBridge.has((Player) sender, "TitanChat.emote.server"))
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
 					String[] lines = getFormatHandler().regroup(message, str.toString());
 					getPlayer(args[0]).sendMessage(message.replace("%message", lines[0]));
 					getPlayer(args[0]).sendMessage(Arrays.copyOfRange(lines, 1, lines.length));
 					getPlayer(args[0]).sendMessage(message.replace("%message", getFormatHandler().colourise(str.toString())));
 					log(Level.INFO, "[Server -> " + getPlayer(args[0]).getName() + "] " + str.toString());
 					
 				} else { log(Level.WARNING, "Player not online"); }
 			}
 			
 			if (!getConfig().getBoolean("whisper.player.enable")) {
 				sendWarning((Player) sender, "Command disabled");
 				return true;
 			}
 			
 			if (permBridge.has((Player) sender, "TitanChat.whisper"))
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
 		
 		log(Level.INFO, "Unloading managers...");
 
 		addonManager.unload();
 		chManager.unload();
 		cmdManager.unload();
 		displayname.unload();
 		variable.unload();
 		
 		log(Level.INFO, "is now disabled");
 	}
 	
 	/**
 	 * Called when the Plugin enables
 	 */
 	@Override
 	public void onEnable() {
 		log(Level.INFO, "is now enabling...");
 		
 		if (!initMetrics())
 			log(Level.WARNING, "Failed to hook into Metrics");
 		
 		File config = new File(getDataFolder(), "config.yml");
 		
 		if (!config.exists()) {
 			log(Level.INFO, "Loading default config...");
 			saveResource("config.yml", false);
 		}
 		
 		if (getChannelDir().mkdir()) {
 			log(Level.INFO, "Creating channel directory...");
 			saveResource("channels/Default.yml", false);
 			saveResource("channels/Password.yml", false);
 			saveResource("channels/Private.yml", false);
 			saveResource("channels/Public.yml", false);
 			saveResource("channels/README.txt", false);
 			saveResource("channels/Staff.yml", false);
 		}
 		
 		try {
 			getDatabase().find(DisplayName.class).findRowCount();
 			
 		} catch (PersistenceException e) {
 			log(Level.INFO, "Setting up display name database...");
 			installDDL();
 		}
 		
 		addonManager = new AddonManager();
 		chManager = new ChannelManager();
 		cmdManager = new CommandManager();
 		displayname = new DisplayNameChanger();
 		format = new FormatHandler();
 		permBridge = new PermsBridge();
 		variable = new Variable();
 		
 		Debugger.load(this);
 		
 		register(new TitanChatListener());
 		
 		for (Player player : getServer().getOnlinePlayers())
 			displayname.apply(player);
 		
 		addonManager.load();
 		try { chManager.load(); } catch (Exception e) {}
 		cmdManager.load();
 		format.load();
 		
 		if (chManager.getDefaultChannel() == null && enableChannels()) {
 			log(Level.SEVERE, "A default channel is not defined");
 			getServer().getPluginManager().disablePlugin(this);
 			return;
 		}
 		
 		log(Level.INFO, "is now enabled");
 	}
 	
 	/**
 	 * Called when Plugin loads
 	 */
 	@Override
 	public void onLoad() {
 		instance = this;
 		NAME = "TitanChat " + instance.toString().split(" ")[1];
 		
 		if (!initLib())
 			log(Level.WARNING, "Failed to initialise Loader lib");
 	}
 	
 	/**
 	 * Registers the Listener
 	 * 
 	 * @param listener The Listener to register
 	 */
 	public void register(Listener listener) {
 		getServer().getPluginManager().registerEvents(listener, this);
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
 	 * Checks for an update
 	 * 
 	 * @return The value of the new version
 	 */
 	protected double updateCheck() {
 		URL url;
 		try {
 			url = new URL("http://dev.bukkit.org/server-mods/titanchat/files.rss");
 			
 			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openConnection().getInputStream());
 			doc.getDocumentElement().normalize();
 			
 			Node node = doc.getElementsByTagName("item").item(0);
 			
 			if (node.getNodeType() == 1) {
 				Element element = (Element) node;
 				Element name = (Element) element.getElementsByTagName("title").item(0);
 				return Double.valueOf(name.getChildNodes().item(0).getNodeValue().split(" ")[1].trim().substring(1));
 			}
 			
 		} catch (Exception e) {}
 		
		return Double.valueOf(getDescription().getVersion().trim().split(" ")[0].trim().substring(1));
 	}
 	
 	/**
 	 * Check if default formatting should be used
 	 * 
 	 * @return True if default formatting should be used
 	 */
 	public boolean useDefaultFormat() {
 		return getConfig().getBoolean("formatting.use-built-in");
 	}
 }
