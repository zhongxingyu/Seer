 package com.ementalo.arrowlimiter;
 
import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.*;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.*;
 import org.bukkit.util.config.Configuration;
 import org.yaml.snakeyaml.*;
 import org.yaml.snakeyaml.constructor.SafeConstructor;
 
 
 public class ArrowLimiter extends JavaPlugin
 {
 	public static final String AUTHORS = "ementalo";
 	public static final Logger logger = Logger.getLogger("Minecraft");
 	private static Yaml yaml = new Yaml(new SafeConstructor());
 	public Configuration config = null;
 	public HashMap<Player, Double> timeLimit = new HashMap<Player, Double>();
 	public HashMap<Block, Double> dispenserLimit = new HashMap<Block, Double>();
	public static PermissionHandler permissionHandler = null;
 	Plugin permPlugin = null;
 
 	@SuppressWarnings("LoggerStringConcat")
 	public void onEnable()
 	{
 		config = getConfiguration();
 		try
 		{
 			LoadSettings();
 		}
 		catch (Throwable ex)
 		{
 			logger.log(Level.SEVERE, "Error with processing config file", ex);
 		}
 		ArrowLimiterPlayerListener playerListener = new ArrowLimiterPlayerListener(this);
 		ArrowLimiterEntityListener entityListener = new ArrowLimiterEntityListener(this);
 		ArrowLimiterServerListener serverListener = new ArrowLimiterServerListener(this);
 		ArrowLimiterBlockListener blockListener = new ArrowLimiterBlockListener(this);
 		getServer().getPluginManager().registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.High, this);
 		getServer().getPluginManager().registerEvent(Type.ENTITY_DAMAGE, entityListener, Priority.High, this);
 		getServer().getPluginManager().registerEvent(Type.PLUGIN_ENABLE, serverListener, Priority.Low, this);
 		getServer().getPluginManager().registerEvent(Type.PLUGIN_DISABLE, serverListener, Priority.Low, this);
 		if (config.getBoolean("limitdispensers", false))
 		{
 			getServer().getPluginManager().registerEvent(Type.BLOCK_DISPENSE, blockListener, Priority.Low, this);
 		}
 		logger.info("Loaded " + this.getDescription().getName() + " build " + this.getDescription().getVersion() + " maintained by " + AUTHORS);
 
 	}
 
 	public void onDisable()
 	{
 	}
 
 	@Override
 	public void onLoad()
 	{
 	}
 
 	public void LoadSettings() throws Exception
 	{
 		if (!this.getDataFolder().exists())
 		{
 			this.getDataFolder().mkdirs();
 		}
 		config.load();
 		final List<String> keys = config.getKeys(null);
 		if (!keys.contains("timedelay"))
 			config.setProperty("timedelay", 1.00);
 		if (!keys.contains("damage"))
 			config.setProperty("damage", 1);
 		if (!keys.contains("custom-msg"))
 			config.setProperty("custom-msg", "");
 		if (!keys.contains("showmsg"))
 			config.setProperty("showmsg", true);
 		if (!keys.contains("limitdispensers"))
 			config.setProperty("limitdispensers", false);
 		if (!keys.contains("dispenserdelay"))
 			config.setProperty("dispenserdelay", 1.00);
 		config.save();
 		config.load();
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
		if (permissionHandler == null)
		{
			permissionHandler = ((Permissions)permPlugin).getHandler();
		}
		return permissionHandler.has(base, node);
 	}
 
 	public Plugin getPermPlugin()
 	{
 		return this.permPlugin;
 	}
 
 	public void setPermPlugin(Plugin plugin)
 	{
 		this.permPlugin = plugin;
 	}
 }
