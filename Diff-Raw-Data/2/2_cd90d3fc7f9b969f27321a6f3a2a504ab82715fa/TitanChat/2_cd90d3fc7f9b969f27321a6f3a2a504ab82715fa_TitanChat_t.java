 package com.titankingdoms.nodinchan.titanchat;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.persistence.PersistenceException;
 
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 
 import com.nodinchan.ncbukkit.NCBL;
 import com.titankingdoms.nodinchan.titanchat.channel.Channel;
 import com.titankingdoms.nodinchan.titanchat.event.EmoteEvent;
 import com.titankingdoms.nodinchan.titanchat.event.util.Message;
 import com.titankingdoms.nodinchan.titanchat.metrics.Metrics;
 import com.titankingdoms.nodinchan.titanchat.metrics.Metrics.Graph;
 import com.titankingdoms.nodinchan.titanchat.metrics.Metrics.Plotter;
 import com.titankingdoms.nodinchan.titanchat.permission.DefaultPermissions;
 import com.titankingdoms.nodinchan.titanchat.processing.ChatProcessor;
 import com.titankingdoms.nodinchan.titanchat.util.Debugger;
 import com.titankingdoms.nodinchan.titanchat.util.FormatHandler;
 import com.titankingdoms.nodinchan.titanchat.util.PermissionsHandler;
 import com.titankingdoms.nodinchan.titanchat.util.displayname.DisplayName;
 import com.titankingdoms.nodinchan.titanchat.util.displayname.DisplayNameChanger;
 
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
 
 public final class TitanChat extends JavaPlugin {
 	
 	private static TitanChat instance;
 	
 	private String NAME;
 	
 	private static final Logger log = Logger.getLogger("TitanLog");
 	private static final Debugger db = new Debugger(0);
 	
 	private ChatProcessor processor;
 	
 	private TitanChatListener listener;
 	private TitanChatManager manager;
 	private DefaultPermissions defPerms;
 	private DisplayNameChanger displayname;
 	private FormatHandler format;
 	private PermissionsHandler permHandler;
 	
 	private boolean silenced = false;
 	
 	public void chatLog(String line) {
 		if (getConfig().getBoolean("logging.colouring"))
 			getServer().getConsoleSender().sendMessage(line.trim());
 		else
 			getServer().getConsoleSender().sendMessage(line.replaceAll("(?i)(\u00A7)([0-9a-fk-or])", "").trim());
 	}
 	
 	public String createList(List<String> list) {
 		StringBuilder str = new StringBuilder();
 		
 		for (String item : list) {
 			if (str.length() > 0)
 				str.append(", ");
 			
 			str.append(item);
 		}
 		
 		db.i("TitanChat: Creating string out of string list: " + str.toString());
 		return str.toString();
 	}
 	
 	public boolean enableChannels() {
 		return getConfig().getBoolean("channels.enable");
 	}
 	
 	public boolean enableJoinMessage() {
 		return getConfig().getBoolean("channels.messages.join");
 	}
 	
 	public boolean enableLeaveMessage() {
 		return getConfig().getBoolean("channels.messages.leave");
 	}
 	
 	public File getChannelDir() {
 		return new File(getDataFolder(), "channels");
 	}
 	
 	public ChatProcessor getChatProcessor() {
 		return processor;
 	}
 	
 	@Override
 	public List<Class<?>> getDatabaseClasses() {
 		return Arrays.asList(new Class<?>[] { DisplayName.class });
 	}
 	
 	public DefaultPermissions getDefPerms() {
 		return defPerms;
 	}
 	
 	public DisplayNameChanger getDisplayNameChanger() {
 		return displayname;
 	}
 	
 	public FormatHandler getFormatHandler() {
 		return format;
 	}
 	
 	public static TitanChat getInstance() {
 		return instance;
 	}
 	
 	@Override
 	public Logger getLogger() {
 		return log;
 	}
 	
 	public TitanChatManager getManager() {
 		return manager;
 	}
 	
 	public OfflinePlayer getOfflinePlayer(String name) {
 		OfflinePlayer player = getServer().getOfflinePlayer(name);
 		return player;
 	}
 	
 	public PermissionsHandler getPermissionsHandler() {
 		return permHandler;
 	}
 	
 	public Player getPlayer(String name) {
 		return getServer().getPlayer(name);
 	}
 	
 	private boolean initMetrics() {
 		log(Level.INFO, "Hooking Metrics");
 		
 		try {
 			Metrics metrics = new Metrics(this);
 			
 			if (metrics.isOptOut())
 				return true;
 			
 			Graph metricsStats = metrics.createGraph("Stats");
 			
 			metricsStats.addPlotter(new Plotter("Characters") {
 				
 				@Override
 				public int getValue() {
 					return (int) listener.getCharacters();
 				}
 			});
 			
 			metricsStats.addPlotter(new Plotter("Lines") {
 				
 				@Override
 				public int getValue() {
 					return (int) listener.getLines();
 				}
 			});
 			
 			metricsStats.addPlotter(new Plotter("Words") {
 				
 				@Override
 				public int getValue() {
 					return (int) listener.getWords();
 				}
 			});
 			
 			metrics.addGraph(metricsStats);
 			
 			return metrics.start();
 			
 		} catch (Exception e) { return false; }
 	}
 	
 	public boolean isSilenced() {
 		return silenced;
 	}
 	
 	public boolean isStaff(Player player) {
 		return permHandler.has(player, "TitanChat.staff");
 	}
 	
 	public void log(Level level, String msg) {
 		log.log(level, "[" + NAME + "] " + msg);
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		db.i("TitanChat: On command " + cmd.getName());
 		
 		if (cmd.getName().equals("titanchat")) {
 			if (args.length < 1 || (args[0].startsWith("@") && args.length < 2)) {
 				db.i("TitanChat: On command: No arguments");
 				
 				sender.sendMessage(ChatColor.AQUA + "You are running " + this);
 				send(MessageLevel.INFO, sender, "\"/titanchat commands [page]\" for command list");
 				return true;
 			}
 			
 			if (args[0].equalsIgnoreCase("updatelib")) {
 				updateLib();
 				return true;
 			}
 			
 			String command = args[0];
 			String chName = null;
 			
 			if (args[0].startsWith("@")) {
 				command = args[1];
 				chName = args[0].substring(1);
 			}
 			
 			String[] arguments = new String[0];
 			
 			if (args[0].startsWith("@"))
 				arguments = Arrays.copyOfRange(args, 2, args.length);
 			else
 				arguments = Arrays.copyOfRange(args, 1, args.length);
 			
 			db.i("TitanChat: CommandManager executing command:");
 			manager.getCommandManager().execute(sender, command, chName, arguments);
 			return true;
 		}
 		
 		if (cmd.getName().equalsIgnoreCase("broadcast")) {
 			StringBuilder str = new StringBuilder();
 			
 			for (String arg : args) {
 				if (str.length() > 0)
 					str.append(" ");
 				
 				str.append(arg);
 			}
 			
 			getServer().dispatchCommand(sender, "titanchat broadcast " + str.toString());
 			return true;
 		}
 		
 		if (cmd.getName().equalsIgnoreCase("emote")) {
 			if (sender instanceof Player && !permHandler.has((Player) sender, "TitanChat.emote")) {
 				send(MessageLevel.WARNING, sender, "You do not have permission");
 				return true;
 			}
 			
 			if (!getConfig().getBoolean("chat." + ((sender instanceof Player) ? "player" : "server") + ".enable")) {
 				send(MessageLevel.WARNING, sender, "Emote Command Disabled");
 				return true;
 			}
 			
 			StringBuilder str = new StringBuilder();
 			
 			for (String word : args) {
 				if (str.length() > 0)
 					str.append(" ");
 				
 				str.append(word);
 			}
 			
 			String format = getFormatHandler().emoteFormat(sender, "");
 			
 			EmoteEvent event = new EmoteEvent(sender, new Message(format, str.toString()));
 			getServer().getPluginManager().callEvent(event);
 			
 			String[] lines = this.format.splitAndFormat(event.getFormat(), "%action", event.getMessage());
 			
 			for (String line : lines)
 				getServer().broadcastMessage(line);
 			
 			return true;
 		}
 		
 		if (cmd.getName().equalsIgnoreCase("whisper")) {
 			StringBuilder str = new StringBuilder();
 			
 			for (String arg : args) {
 				if (str.length() > 0)
 					str.append(" ");
 				
 				str.append(arg);
 			}
 			
 			getServer().dispatchCommand(sender, "titanchat whisper " + str.toString());
 			return true;
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public void onDisable() {
 		log(Level.INFO, "is now disabling...");
 		log(Level.INFO, "Unloading managers...");
 		
 		manager.getAddonManager().unload();
 		manager.getChannelManager().unload();
 		manager.getCommandManager().unload();
 		displayname.unload();
 		
 		log(Level.INFO, "is now disabled");
 	}
 	
 	@Override
 	public void onEnable() {
 		log(Level.INFO, "is now enabling...");
 		
 		try {
 			getDatabase().find(DisplayName.class).findRowCount();
 			
 		} catch (PersistenceException e) {
 			log(Level.INFO, "Setting up display name database...");
 			installDDL();
 		}
 		
 		register(listener = new TitanChatListener());
 		
 		if (!initMetrics())
 			log(Level.WARNING, "Failed to hook into Metrics");
 		
 		if (!getChannelDir().exists()) {
 			log(Level.INFO, "Creating channel directory...");
			saveResource("channels/", false);
 		}
 		
 		manager = new TitanChatManager();
 		defPerms = new DefaultPermissions();
 		displayname = new DisplayNameChanger();
 		format = new FormatHandler();
 		permHandler = new PermissionsHandler();
 		
 		Debugger.load(getConfig().getString("logging.debug"));
 		
 		for (Player player : getServer().getOnlinePlayers())
 			displayname.apply(player);
 		
 		manager.load();
 		defPerms.load();
 		
 		if (manager.getChannelManager().getDefaultChannels().isEmpty()) {
 			log(Level.SEVERE, "A default channel is not defined");
 			getServer().getPluginManager().disablePlugin(this);
 			return;
 		}
 		
 		processor = new ChatProcessor();
 		getServer().getPluginManager().registerEvents(processor, this);
 		log(Level.INFO, "is now enabled");
 	}
 	
 	@Override
 	public void onLoad() {
 		instance = this;
 		NAME = "TitanChat " + instance.toString().split(" ")[1];
 		
 		File config = new File(getDataFolder(), "config.yml");
 		
 		if (!config.exists()) {
 			log(Level.INFO, "Loading default config...");
 			saveResource("config.yml", false);
 		}
 		
 		if (getConfig().getBoolean("auto-library-update"))
 			updateLib();
 	}
 	
 	public void register(Listener listener) {
 		getServer().getPluginManager().registerEvents(listener, this);
 	}
 	
 	public void send(MessageLevel level, CommandSender sender, String msg) {
 		db.i("@" + sender.getName() + ": " + msg);
 		String format = "[" + level.getColour() + "TitanChat" + ChatColor.WHITE + "] " + level.getColour() + "%msg";
 		sender.sendMessage(this.format.splitAndFormat(format, "%msg", msg));
 	}
 	
 	public void send(MessageLevel level, Channel channel, String msg) {
 		db.i("@Channel " + channel.getName() + ": " + msg);
 		channel.send("[" + level.getColour() + "TitanChat" + ChatColor.WHITE + "] " + level.getColour() + msg);
 	}
 	
 	public void send(MessageLevel level, List<Player> players, String msg) {
 		for (Player player : players)
 			send(level, player, msg);
 	}
 	
 	public void setSilenced(boolean silenced) {
 		db.i("Setting silenced to " + silenced);
 		this.silenced = silenced;
 	}
 	
 	private void updateLib() {
 		PluginManager pm = getServer().getPluginManager();
 		
 		NCBL libPlugin = (NCBL) pm.getPlugin("NC-BukkitLib");
 		
 		File destination = new File(getDataFolder().getParentFile().getParentFile(), "lib");
 		destination.mkdirs();
 		
 		File lib = new File(destination, "NC-BukkitLib.jar");
 		File pluginLib = new File(getDataFolder().getParentFile(), "NC-BukkitLib.jar");
 		
 		boolean inPlugins = false;
 		boolean download = false;
 		
 		try {
 			URL url = new URL("http://bukget.org/api/plugin/nc-bukkitlib");
 			
 			JSONObject jsonPlugin = (JSONObject) new JSONParser().parse(new InputStreamReader(url.openStream()));
 			JSONArray versions = (JSONArray) jsonPlugin.get("versions");
 			
 			if (libPlugin == null) {
 				getLogger().log(Level.WARNING, "Missing NC-Bukkit lib");
 				inPlugins = true;
 				download = true;
 				
 			} else {
 				double currentVer = libPlugin.getVersion();
 				double newVer = currentVer;
 				
 				for (int ver = 0; ver < versions.size(); ver++) {
 					JSONObject version = (JSONObject) versions.get(ver);
 					
 					if (version.get("type").equals("Release")) {
 						newVer = Double.parseDouble(((String) version.get("name")).split(" ")[1].trim().substring(1));
 						break;
 					}
 				}
 				
 				if (newVer > currentVer) {
 					getLogger().log(Level.WARNING, "NC-Bukkit lib outdated");
 					download = true;
 				}
 			}
 			
 			if (download) {
 				getLogger().log(Level.INFO, "Downloading NC-Bukkit lib");
 				
 				String dl_link = "";
 				
 				for (int ver = 0; ver < versions.size(); ver++) {
 					JSONObject version = (JSONObject) versions.get(ver);
 					
 					if (version.get("type").equals("Release")) {
 						dl_link = (String) version.get("dl_link");
 						break;
 					}
 				}
 				
 				if (dl_link == null)
 					throw new Exception();
 				
 				URL link = new URL(dl_link);
 				ReadableByteChannel rbc = Channels.newChannel(link.openStream());
 				FileOutputStream output = null;
 				
 				if (inPlugins) {
 					output = new FileOutputStream(pluginLib);
 					output.getChannel().transferFrom(rbc, 0, 1 << 24);
 					libPlugin = (NCBL) pm.loadPlugin(pluginLib);
 					
 				} else {
 					output = new FileOutputStream(lib);
 					output.getChannel().transferFrom(rbc, 0, 1 << 24);
 				}
 				
 				output.close();
 				getLogger().log(Level.INFO, "Downloaded NC-Bukkit lib");
 			}
 			
 			libPlugin.hook(this);
 			
 		} catch (Exception e) { getLogger().log(Level.WARNING, "Failed to check for library update"); }
 	}
 	
 	public boolean voiceless(Player player, Channel channel, boolean message) {
 		if (permHandler.has(player, "TitanChat.voice"))
 			return false;
 		
 		if (isSilenced()) {
 			if (message)
 				send(MessageLevel.WARNING, player, "The server is silenced");
 			
 			return true;
 		}
 		
 		if (manager.getChannelManager().isSilenced(channel)) {
 			if (message)
 				send(MessageLevel.WARNING, player, "The channel is silenced");
 			
 			return true;
 		}
 		
 		if (manager.getChannelManager().getParticipant(player).isMuted(channel)) {
 			if (message)
 				send(MessageLevel.WARNING, player, "You have been muted");
 			
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public enum MessageLevel {
 		INFO(ChatColor.GOLD),
 		NONE(ChatColor.WHITE),
 		PLUGIN(ChatColor.AQUA),
 		WARNING(ChatColor.RED);
 		
 		private ChatColor colour;
 		
 		private MessageLevel(ChatColor colour) {
 			this.colour = colour;
 		}
 		
 		public ChatColor getColour() {
 			return colour;
 		}
 	}
 }
