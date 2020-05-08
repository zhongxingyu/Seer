 package net.minesocket.potionpermissions;
 
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class PotionPermissions extends JavaPlugin {
 
 	
 	private final PotionListener pL = new PotionListener(this);
	private final InventoryCloseListener iL = new InventoryCloseListener(this);
 	public void onEnable() {
 		
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(pL, this);
		pm.registerEvents(iL, this);
 		
 	}
 	
 	public void onDisable() {
 		//
 	}
 }
