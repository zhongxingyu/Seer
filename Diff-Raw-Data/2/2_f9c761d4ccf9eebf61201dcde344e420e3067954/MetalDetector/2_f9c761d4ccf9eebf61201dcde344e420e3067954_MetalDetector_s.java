 package me.cvenomz.MetalDetector;
 
 import java.util.logging.Logger;
 
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class MetalDetector extends JavaPlugin{
 
 	private MetalDetectorPlayerListener playerListener;
 	private Logger log = Logger.getLogger("Minecraft");
	private double version = 0.1;
 	
 	@Override
 	public void onDisable() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onEnable() {
 		// TODO Auto-generated method stub
 		playerListener = new MetalDetectorPlayerListener();
 		getServer().getPluginManager().registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
 		log.info("[MetalDetector] MetalDetector " + version + " enabled");
 	}
 
 }
