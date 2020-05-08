 package com.ementalo.commandalert;
 
 import com.nijikokun.bukkit.Permissions.Permissions;
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.FileHandler;
 import java.util.logging.Formatter;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.LogRecord;
 import java.util.logging.Logger;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 import org.yaml.snakeyaml.Yaml;
 import org.yaml.snakeyaml.constructor.SafeConstructor;
 
 
 public class CommandAlert extends JavaPlugin
 {
 	private CommandAlertPlayerListener playerListener = null;
 	private CommandAlertServerListener serverListener = null;
 	public static final Logger cmdAlertLog = Logger.getLogger("CommandAlert");
 	static final Logger log = Logger.getLogger("Minecraft");
 	private static Yaml yaml = new Yaml(new SafeConstructor());
	public Object permissions = null;
 	public Plugin permPlugin = null;
 	public Configuration config = null;
 	FileHandler fileHandle = null;
 
 	@Override
 	public void onDisable()
 	{
 		log.log(Level.INFO, "[CommandAlert] disabled");
 		if (fileHandle != null)
 		{
 			fileHandle.close();
 		}
 	}
 
 	@Override
 	public void onEnable()
 	{
 		config = getConfiguration();
 		try
 		{
 			LoadSettings();
 		}
 		catch (Exception ex)
 		{
 			log.log(Level.SEVERE, "[CommandAlert] Could not load the config file", ex);
 		}
 		if (logToFile())
 		{
 			SetupLogging();
 		}
 		playerListener = new CommandAlertPlayerListener(this);
 		serverListener = new CommandAlertServerListener(this);
 		log.info("Loaded " + this.getDescription().getName() + " build " + this.getDescription().getVersion() + " maintained by " + this.getDescription().getAuthors());
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Type.PLAYER_COMMAND_PREPROCESS, playerListener, Priority.Lowest, this);
 		pm.registerEvent(Type.PLUGIN_ENABLE, serverListener, Priority.Low, this);
 		pm.registerEvent(Type.PLUGIN_DISABLE, serverListener, Priority.Low, this);
 	}
 
 	public Boolean hasPermission(String node, Player base)
 	{
 		if (permPlugin == null)
 		{
 			if (base.isOp())
 			{
 				return true;
 			}
 			return false;
 		}
			Permissions pm = (Permissions)permPlugin;
			return pm.getHandler().has(base, node);
 	}
 
 	public void SetupLogging()
 	{
 		File logDir = new File(this.getDataFolder(), "logs/");
 		if (!logDir.exists())
 		{
 			logDir.mkdirs();
 		}
 		try
 		{
 			cmdAlertLog.setUseParentHandlers(false);
 			cmdAlertLog.setLevel(Level.INFO);
 			fileHandle = new FileHandler(this.getDataFolder() + "/logs/" + formatDateFromMs(System.currentTimeMillis(), "yyyy-MM-dd") + ".log", true);
 			fileHandle.setFormatter(new CommandAlertLog());
 			cmdAlertLog.addHandler(fileHandle);
 		}
 		catch (SecurityException e1)
 		{
 			log.log(Level.WARNING, "[CommandAlert] Could not create log file", e1);
 		}
 		catch (IOException e1)
 		{
 			log.log(Level.WARNING, "[CommandAlert] Could not create log file", e1);
 		}
 	}
 
 
 	class CommandAlertLog extends Formatter
 	{
 		@Override
 		public String format(LogRecord rec)
 		{
 			return calcDate(rec.getMillis()) + "[INFO] " + formatMessage(rec) + "\r\n";
 		}
 
 		private String calcDate(long millisecs)
 		{
 			return formatDateFromMs(millisecs, "yyyy-MM-dd HH:mm:ss ");
 		}
 
 		@Override
 		public String getHead(Handler h)
 		{
 			return "";
 		}
 
 		@Override
 		public String getTail(Handler h)
 		{
 			return "";
 		}
 	}
 
 	public String formatDateFromMs(long millisecs, String format)
 	{
 		SimpleDateFormat date_format = new SimpleDateFormat(format);
 		Date resultdate = new Date(millisecs);
 		return date_format.format(resultdate);
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args)
 	{
 		if (!(sender instanceof Player))
 		{
 			sender.sendMessage("Commands can only be used in game");
 			return true;
 		}
 
 		Player player = (Player)sender;
 		if (commandLabel.equalsIgnoreCase("cmdcheck") && hasPermission("commandalert.cmdcheck", player))
 		{
 			int id = 0;
 			try
 			{
 				id = Integer.parseInt(args[0]);
 			}
 			catch (NumberFormatException e)
 			{
 				return false;
 			}
 			Location playerLocation = playerListener.alertLocations[args.length < 1 ? playerListener.index - 1 : id];
 			if (args.length < 1)
 			{
 				if (playerLocation == null)
 				{
 					player.sendMessage("Error: That location is no longer in the history");
 					return true;
 				}
 				player.sendMessage("Teleporting to location history");
 				player.teleport(playerLocation);
 				return true;
 			}
 			if (args.length == 1)
 			{
 				if (playerListener.alertLocations[id] == null)
 				{
 					player.sendMessage(ChatColor.RED + "That id is not present in the current location history list");
 					return true;
 				}
 				player.sendMessage("Teleporting to last location history");
 				player.teleport(playerLocation);
 				return true;
 
 			}
 			if (args.length > 1)
 			{
 				return false;
 			}
 		}
 		if (commandLabel.equalsIgnoreCase("cmdalertr") && hasPermission("cmdalert.cmdalertr", player))
 		{
 			config.load();
 			playerListener.maxLocations = getLocationHistory();
 			playerListener.alertLocations = new Location[playerListener.maxLocations];
 			player.sendMessage("[CommandAlert] Reloaded Config");
 
 		}
 		return true;
 	}
 
 	public void LoadSettings() throws Exception
 	{
 		if (!this.getDataFolder().exists())
 		{
 			this.getDataFolder().mkdirs();
 		}
 		config.load();
 		final List<String> keys = config.getKeys(null);
 		if (!keys.contains("mode"))
 			config.setProperty("mode", "blacklist");
 		if (!keys.contains("commands"))
 			config.setProperty("commands", "warp, home, spawn");
 		if (!keys.contains("showInGameAlert"))
 			config.setProperty("showInGameAlert", true);
 		if (!keys.contains("logToConsole"))
 			config.setProperty("logToConsole", false);
 		if (!keys.contains("logToFile"))
 			config.setProperty("logToFile", false);
 		if (!keys.contains("locationHistory"))
 			config.setProperty("locationHistory", 30);
 		config.save();
 		config.load();
 	}
 
 	public Boolean logToFile()
 	{
 		return config.getBoolean("logToFile", false);
 	}
 
 	public Boolean showInGame()
 	{
 		return config.getBoolean("showInGameAlert", true);
 	}
 
 	public Boolean logToConsole()
 	{
 		return config.getBoolean("logToConsole", false);
 	}
 
 	public String getMode()
 	{
 		return config.getString("mode", "blacklist");
 	}
 
 	public ArrayList<String> getCommandList()
 	{
 		ArrayList<String> cmds = new ArrayList<String>();
 		for (String cmd : config.getString("commands", "warp, home, spawn").split(","))
 		{
 			cmd = cmd.trim();
 			if (cmd.isEmpty())
 			{
 				continue;
 			}
 			cmds.add(cmd.toLowerCase());
 		}
 		return cmds;
 	}
 
 	public Integer getLocationHistory()
 	{
 		return config.getInt("locationHistory", 30);
 	}
 }
