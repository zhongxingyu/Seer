 package net.playminecraft.enderblock;
 
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 
 public class EnderBlock extends JavaPlugin {
 	private EBEntityListener entityListener;
 	private String name;
 	private String version;
 	public PluginManager pm;
 	
 	@Override
 	public void onDisable() {
 		name = this.getDescription().getName();
 		version = this.getDescription().getVersion();
 
 		EBLogger.info(name + " " + version + " disabled");
 	}
 
 	@Override
 	public void onEnable() {
 		pm = getServer().getPluginManager();
 		name = this.getDescription().getName();
 		version = this.getDescription().getVersion();
 
 		pm.registerEvent(Event.Type.ENDERMAN_PLACE, entityListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.ENDERMAN_PICKUP, entityListener, Priority.Normal, this);
 
 		EBLogger.info(name + " " + version + " enabled");
 	}
 }
