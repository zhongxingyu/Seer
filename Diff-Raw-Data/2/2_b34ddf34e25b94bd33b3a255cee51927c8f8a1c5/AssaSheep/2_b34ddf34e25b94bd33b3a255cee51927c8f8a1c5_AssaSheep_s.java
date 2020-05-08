 package com.Nicogeta.AssaSheep;
 
 import java.util.logging.Logger;
 
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class AssaSheep extends JavaPlugin{
 	Logger log = Logger.getLogger("Minecraft");
 	private PluginDescriptionFile info;
 	private final AssaSheepEntityListener entityListener = new AssaSheepEntityListener(this);
 
 	public void onEnable() {
 		info = getDescription();
 		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Normal, this);
 		log.info(info.getName() + " ver " + info.getVersion() + " by Nicogeta " + " ENABLED" );
 	}
 
 	public void onDisable() {
 		log.info(info.getName() + " DISABLEd");
 	}
 
 
 }
