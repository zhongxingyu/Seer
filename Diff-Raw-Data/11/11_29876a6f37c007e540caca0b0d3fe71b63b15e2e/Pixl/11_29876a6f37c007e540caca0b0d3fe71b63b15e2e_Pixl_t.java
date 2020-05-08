 package hackhalo2.creative.Pixl;
 
 import java.util.HashMap;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.nijiko.permissions.PermissionHandler;
 
 public class Pixl extends JavaPlugin {
     public PluginDescriptionFile pdf;
     public String version = null;
     public HashMap<Player, Boolean> toggled = new HashMap<Player, Boolean>();
     public HashMap<Player, Integer> set = new HashMap<Player, Integer>();
 
     private final PixlPlayer pListener = new PixlPlayer(this);
     private final PixlServer pServer = new PixlServer(this);
 
     //Hook Handling
     //Permissions
     public String permissionsType; //name of the hooked Permissions plugin
     public static PermissionHandler Permissions = null; //for Permissions
     public boolean permissionsEnabled = false; //enabled boolean
     //Help
     public boolean helpEnabled = false; //enabled boolean
 
     @Override
     public void onEnable() {
 	
 	pdf = this.getDescription();
 	version = pdf.getVersion();
 	//Set up the Plugin Manager
 	PluginManager pm = getServer().getPluginManager();
 	//Register Listeners
 	pm.registerEvent(Event.Type.PLUGIN_ENABLE, pServer, Event.Priority.Lowest, this);
 	pm.registerEvent(Event.Type.PLUGIN_DISABLE, pServer, Event.Priority.Lowest, this);
 	pm.registerEvent(Event.Type.PLAYER_JOIN, pListener, Event.Priority.Normal, this);
 	pm.registerEvent(Event.Type.PLAYER_KICK, pListener, Event.Priority.Normal, this);
 	pm.registerEvent(Event.Type.PLAYER_INTERACT, pListener, Event.Priority.Normal, this);
 	System.out.println("[Pixl] "+version+" Loaded");
 
 	getCommand("pixl").setExecutor(new PixlCommand(this));
     }
 
     @Override
     public void onDisable() { }
 
     public void setToggle(final Player p, final boolean v) { toggled.put(p, v); }	
     public boolean isToggled(final Player p) {
 	if(toggled.containsKey(p)) { return toggled.get(p); }
 	else { return false; }
     }
 
     public void removeValue(final Player p) { set.remove(p); }
     public void setValue(final Player p, final int v) { set.put(p, v); }
     public Integer isSet(final Player p) {
 	if(set.containsKey(p)) { return set.get(p); }
 	else { return null; }
     }
 
     public boolean checkPermissions(Player p, String s, boolean f) {
 	if(isToggled(p) || f) { //check to see if player is toggled or forced
 	    if(permissionsEnabled) { return Permissions.has(p, s); }
 	    else if(p.isOp()) { return true; }
 	}
 	return false;
     }
 }
