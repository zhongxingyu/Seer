 package pro.homiecraft;
 
 import java.util.logging.Logger;
 
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import pro.homiecraft.Commands.*;
 
 public class TheCommandBook extends JavaPlugin{
 	public static TheCommandBook pluginST;
 	public TheCommandBook plugin;
 	public Logger log = Logger.getLogger("Minecraft");
 	
 	
 	public void onDisable() {
 		
 	}
 	
 	public void onEnable() {
 		//Updater updater = new Updater(this, "TheCommandBook", this.getFile(), Updater.UpdateType.DEFAULT, false);
 		
 		PluginManager pm = getServer().getPluginManager();
 			pm.registerEvents(new Welcome(), this);
			
		TheCommandBook.pluginST = this;
 		
 		loadConfiguration();
 		getCommand(this);
 	}
 
 	private void getCommand(TheCommandBook theCommandBook) {
 		this.getCommand("setspawn").setExecutor(new tcbSetSpawn());
 		this.getCommand("spawn").setExecutor(new tcbSpawn());
 	}
 	
 	public void loadConfiguration() {
		if(!getDataFolder().exists()){
			getDataFolder().mkdir();
			}		
		
 		this.getConfig().options().copyDefaults(true);
 		this.saveConfig();
 		this.reloadConfig();
 	}
 }
