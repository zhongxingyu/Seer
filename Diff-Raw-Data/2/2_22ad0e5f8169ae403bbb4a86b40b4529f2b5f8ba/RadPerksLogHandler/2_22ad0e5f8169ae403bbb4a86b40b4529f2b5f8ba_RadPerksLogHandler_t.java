package me.draosrt.radperks;
 
 import java.util.logging.Logger;
 
 import org.bukkit.plugin.PluginDescriptionFile;
 
 public class RadPerksLogHandler {
 	
 	private RadPerks plugin;
 	private Logger logger;
 	
 	public RadPerksLogHandler(RadPerks plugin){
 		this.plugin = plugin;
 		this.logger = Logger.getLogger("Minecraft");
 		
 	}
 	private String buildString(String message){
 		PluginDescriptionFile pdFile = plugin.getDescription();
 		
 		return pdFile.getName() + " " + pdFile.getVersion() + ": " + message;
 	}
 	
 	public void info(String message){
 		this.logger.info(this.buildString(message));
 	}
 	public void warn(String message){
 		this.logger.warning(this.buildString(message));
 	
 
 }
 }
