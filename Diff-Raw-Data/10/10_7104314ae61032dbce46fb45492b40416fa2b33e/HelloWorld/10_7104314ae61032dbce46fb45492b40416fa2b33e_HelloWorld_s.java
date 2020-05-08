 package gt.plugin.helloworld;
 
 import gt.general.Hero;
 import gt.general.HeroManager;
 
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 
 /**
  * Basic Bukkit Plugin to test stuff
  * 
  * @author Roman
  *
  */
 public class HelloWorld extends JavaPlugin {
 	
 	HeroManager heroManager;
 	
 	private static JavaPlugin plugin;
 	
 	public void onEnable() {
 		
 		HelloWorld.setPlugin(this);
 		
 		heroManager = new HeroManager(this);
 		
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(new BlockListener(), this);
 		pm.registerEvents(heroManager, this);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, null, 0, 10);
 	}
 
 	public static JavaPlugin getPlugin() {
 		return plugin;
 	}
 
 	private static void setPlugin(JavaPlugin plugin) {
 		HelloWorld.plugin = plugin;
 	}
 	
 }
