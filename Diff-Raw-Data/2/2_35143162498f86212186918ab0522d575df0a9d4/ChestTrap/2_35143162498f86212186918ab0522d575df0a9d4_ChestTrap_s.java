 package tk.tyzoid.plugins.ChestTrap;
 
 import java.util.Random;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.permissions.Permission;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 //TODO: Possible remove dependency of nijiko and use playerInstance.hasPermission(String) ?
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 import tk.tyzoid.plugins.ChestTrap.Lib.Data;
 import tk.tyzoid.plugins.ChestTrap.Lib.Settings;
 import tk.tyzoid.plugins.ChestTrap.Listeners.BListener;
 //import tk.tyzoid.plugins.ChestTrap.Listeners.EListener;
 import tk.tyzoid.plugins.ChestTrap.Listeners.PListener;
 
 public class ChestTrap extends JavaPlugin {
 	public String pluginname = "ChestTrap";
 	private final BListener blockListener = new BListener(this);
 	private final PListener playerlistener = new PListener(this);
 	//private final EListener entityListener = new EListener(this);
 	public Data chestData = new Data(this);
 	public Random randomNumbers = new Random(System.currentTimeMillis());
 	
 	public Settings settings = new Settings(this);
 
 	public PermissionHandler permissionHandler;
     public boolean permissionsExists = false;
     public boolean useSuperperms = false;
 
     public void onDisable() {
         System.out.println("[" + pluginname +"] " + pluginname + " is closing...");
     }
 
     public void onEnable() {
         PluginDescriptionFile pdfFile = this.getDescription();
         System.out.println("[" + pluginname + "] Starting " + pluginname + " v" + pdfFile.getVersion() + "...");
         
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvents(playerlistener, this);
         pm.registerEvents(blockListener, this);
         //pm.registerEvents(entityListener, this);
         
         
         settings.readSettings();
         setupPermissions();
         chestData.loadTraps();
     }
     
     private void setupPermissions() {
         Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
         
         if (permissionHandler == null) {
             if (permissionsPlugin != null) {
             	permissionsExists = true;
                 permissionHandler = ((Permissions) permissionsPlugin).getHandler();
                 System.out.println("[" + pluginname + "] Permissions found!");
             } else {
                 System.out.println("[" + pluginname + "] Permissions not detected. Looking for superperms.");
                 permissionsExists = false;
                 
                 try{
                 	@SuppressWarnings("unused")
 					Permission fakePerm = new Permission("fake.perm");
                 	useSuperperms = true;
                	System.out.println("[" + pluginname + "] Superpermis found.");
                 } catch(Exception e){
                 	System.out.println("[" + pluginname + "] Superperms not found. Using defaults.");
                 }
             }
         }
     }
     
     public boolean hasPermission(Player p, String node){
     	if(!useSuperperms){
     		return permissionHandler.has(p, node);
     	} else {
     		return p.hasPermission(node);
     	}
     }
     
     public boolean isInteger(String str){
     	try{
     		Integer.parseInt(str);
     		return true;
     	} catch(Exception e){
     		return false;
     	}
     }
 }
