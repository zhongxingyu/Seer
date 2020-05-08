 package gmail.theultimatebudgie.liteperms;
 
 import gmail.theultimatebudgie.liteperms.commands.CommandListener;
 
 import java.io.IOException;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class LitePerms extends JavaPlugin{
 	private static String PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.DARK_AQUA + "lp" + ChatColor.DARK_GRAY + "]";
 	public static String INFO = PREFIX + "[" + ChatColor.AQUA + "Info" + ChatColor.DARK_GRAY + "] " + ChatColor.RESET;
	public static String WARNING = PREFIX + "[" + ChatColor.DARK_RED + "Warning" + ChatColor.BLACK + "] " + ChatColor.RESET;
 	public static String SUCCESS = ChatColor.DARK_GREEN + "Success! " + ChatColor.RESET;
 	public static String USAGE = ChatColor.RED + "Wrong Usage! " + ChatColor.RESET;
 	
 	Listeners listeners;
 	YmlReader yaml;
 	
     @Override
     public void onEnable(){
     	listeners = new Listeners(this);
     	getServer().getPluginManager().registerEvents(listeners, this);
     	
     	getCommand("lp").setExecutor(new CommandListener(this));
     	
     	yaml = new YmlReader(this);
     	try {
 			yaml.checkConfigs();
 		} catch (IOException e) {
 			e.printStackTrace();
 			Bukkit.getPluginManager().disablePlugin(this);
 		}
     	yaml.loadPermissions();
     }
  
     @Override
     public void onDisable() {
         // TODO Insert logic to be performed when the plugin is disabled
     }
 
 	public YmlReader getYaml() {
 		return yaml;
 	}
 	
 }
