 package cz.opt.morgulplugin;
 
 import java.util.logging.Logger;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import cz.opt.morgulplugin.config.Config;
 import cz.opt.morgulplugin.database.DataBase;
 import cz.opt.morgulplugin.entity.MorgPlayer;
 import cz.opt.morgulplugin.event.CommandEvent;
 import cz.opt.morgulplugin.eventmanager.BlockEventManager;
 import cz.opt.morgulplugin.eventmanager.ChatEventManager;
 import cz.opt.morgulplugin.eventmanager.PlayerEventManager;
 import cz.opt.morgulplugin.managers.ChatManager;
 import cz.opt.morgulplugin.managers.CommandManager;
 import cz.opt.morgulplugin.managers.EconomyManager;
 import cz.opt.morgulplugin.managers.LoginManager;
 import cz.opt.morgulplugin.managers.PlayerManager;
 import cz.opt.morgulplugin.managers.StatManager;
 
 public final class MorgulPlugin extends JavaPlugin
 {
 	private static final String SECTION = "System";
 	public static MorgulPlugin thisPlugin;
 	private static boolean debugMode;
 	private static Logger log;
 	public static void log(String text)
 	{
 		log.info(text);
 	}
 	
 	public static void debug(String text)
 	{
 		if(debugMode)
 			log.info(text);
 	}
 	
 	@Override
 	public void onLoad()
 	{
 		thisPlugin = this;
 		log = this.getLogger();
 		if(!Config.loadConfig())
 			MorgulPlugin.log("Config could not be Loaded.");
 		else
 		{
 			MorgulPlugin.log("Config Loaded.");	
 			debugMode = Boolean.parseBoolean(Config.get(SECTION, "debugmode"));
 			if(!DataBase.setUp())
 			{
 				MorgulPlugin.log("Database could not be SetUp.");
 				return;
 			}
 			else
 				MorgulPlugin.log("Database SetUp.");
			
 			PlayerManager.init();
 			MorgulPlugin.log("PlayerManager Init.");
 			MorgPlayer.init();
 			MorgulPlugin.log("MorgulPlayer Init.");
 			CommandManager.init();
 			MorgulPlugin.log("CommandManager Init.");
 			LoginManager.init();
 			MorgulPlugin.log("LoginManager Init.");
 			EconomyManager.init();
 			MorgulPlugin.log("EconomyManager Init.");
 			ChatManager.init();
 			MorgulPlugin.log("ChatManager Init.");
 			StatManager.init();
 			MorgulPlugin.log("StatManager Init.");
 		}	
 	}
 	
 	@Override
 	public void onEnable()
 	{
 		this.getServer().getPluginManager().registerEvents(new PlayerEventManager(), this);
 		this.getServer().getPluginManager().registerEvents(new ChatEventManager(), this);
 		this.getServer().getPluginManager().registerEvents(new BlockEventManager(), this);
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
 	{
 		return CommandManager.sendCommand(new CommandEvent(sender, command, label, args));
 	}
 }
