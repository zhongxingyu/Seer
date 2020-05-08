 package me.yuzko.gitty;
 
 import java.util.logging.Logger;
 
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 
 public class Base extends JavaPlugin
 	implements Listener
 	
 {
 	public static Logger logger = Logger.getLogger("Minecraft");
 
 
 	public Base(Base plugin)
 	{
 		this.plugin = plugin;
 	}
 
 	public void onEnable()
 	{
 		if(plugin == null)
 			plugin = this;
 		logger.info("[YMod] Plugin enabled!");
 		PluginManager pm = this.getServer().getPluginManager();
 	}
 	
 	public void onDisable()
 	{
 		logger.info("[YMod] Plugin disabled!");
 	}
 	
 	public static Base get()
 	{
 		return plugin;
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel,
 			String[] args)
 	{
 
 		if(!(sender instanceof Player))
 
 			return false;
 		else
			return Commands.method((Player)sender, cmd, commandLabel.toLowerCase(), args);
 	}
 	
 	public static String name = "Base";
 	public static Base plugin;
 	
 }
