 package net.minesocket.potionpermissions;
 
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class PotionPermissions extends JavaPlugin {
 
 	
 	private final PotionListener pL = new PotionListener(this);
 	public void onEnable() {
 		
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(pL, this);
 		
 	}
 	
 	public void onDisable() {
 		//
 	}
 }
