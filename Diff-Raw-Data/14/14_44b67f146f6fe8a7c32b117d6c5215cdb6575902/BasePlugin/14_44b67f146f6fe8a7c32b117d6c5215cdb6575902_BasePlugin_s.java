 package uk.co.jacekk.bukkit.baseplugin;
 
 import java.io.File;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitScheduler;
 
 import uk.co.jacekk.bukkit.baseplugin.config.PluginConfig;
 import uk.co.jacekk.bukkit.baseplugin.util.PluginLogger;
 
 public abstract class BasePlugin extends JavaPlugin {
 	
	public final PluginDescriptionFile description = this.getDescription();
 	
	public final PluginLogger log = new PluginLogger(this);
	
	public File baseDir;
	public String baseDirPath;
 	
 	public Server server;
 	public PluginManager pluginManager;
 	public BukkitScheduler scheduler;
 	
 	public PluginConfig config;
 	
 	public void onEnable(boolean createFolder){
 		this.baseDir = this.getDataFolder();
 		this.baseDirPath = this.baseDir.getAbsolutePath();
 		
 		this.server = this.getServer();
 		this.pluginManager = this.server.getPluginManager();
 		this.scheduler = this.server.getScheduler();
 		
 		if (createFolder && !this.baseDir.exists()){
 			this.baseDir.mkdirs();
 		}
 	}
 	
 	public String formatMessage(String message, boolean colour, boolean version){
 		StringBuilder line = new StringBuilder();
 		
 		if (colour){
 			line.append(ChatColor.BLUE);
 		}
 		
 		line.append("[");
 		line.append(this.description.getName());
 		
 		if (version){
 			line.append(" v");
 			line.append(this.description.getVersion());
 		}
 		
 		line.append("] ");
 		line.append(message);
 		
 		return line.toString();
 	}
 	
 	public String formatMessage(String message, boolean colour){
 		return this.formatMessage(message, colour, !colour);
 	}
 	
 	public String formatMessage(String message){
 		return this.formatMessage(message, true, false);
 	}
 	
 }
