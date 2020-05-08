 package xhizors.MyWordfilter;
 
 import java.util.Random;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class MyWordfilter extends JavaPlugin {
 	
 	private final MyWordfilterPlayerListener playerListener = new MyWordfilterPlayerListener(this);
 	public final MyWordfilterReader reader = new MyWordfilterReader(this);
 	private PluginManager pm;
 	public static final Logger log = Logger.getLogger("Minecraft");
 	public static String pluginName;
	public boolean permissionsEnabled = true;
 	public PermissionHandler permissions = null;
 	public ConcurrentHashMap<String, String> filter = new ConcurrentHashMap<String, String>();
 	
 	public void onDisable() {
 		log.info("[" + pluginName + "] is disabled.");
 	}
 
 	public void onEnable() {
 		pm = getServer().getPluginManager();
 		PluginDescriptionFile pdfFile = this.getDescription();
 		pluginName = pdfFile.getName();
 		log.info("[" + pluginName + "] version " + pdfFile.getVersion() + " is enabled.");
 		setupPermissions();
 		reader.parseFilter();
 		
 		pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Event.Priority.Low, this);
 		pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener, Event.Priority.Low, this);
 	}
 	
 	public String filterMessage(String msg, Player ply) {
 		//return msg.replaceAll("(&([a-f0-9]))", "\u00A7$2");
 		for (String key : filter.keySet()) {
 			String c = "";
 			if (filter.get(key).contains("&r")) {
 				Random rand = new Random();
 				int i = rand.nextInt(16);
 				if (i > 9) {
 					switch (i) {
 					case 10: c = "a"; break;
 					case 11: c = "b"; break;
 					case 12: c = "c"; break;
 					case 13: c = "d"; break;
 					case 14: c = "e"; break;
 					case 15: c = "f"; break;
 					}
 				} else {
 					c = c + i;
 				}
 			}
 			msg = msg.replaceAll(key, filter.get(key).replaceAll("&r", "&"+c).replaceAll("(&([a-f0-9]))", "\u00A7$2"));
 		}
 		return msg;
 	}
 	
     public void setupPermissions() {
         Plugin perm = this.getServer().getPluginManager().getPlugin("Permissions");
         if(permissions == null) {
             if(perm != null) {
                 permissions = ((Permissions)perm).getHandler();
             } else {
                 log.info("["+ pluginName + "] Permission system not enabled. Using ops.txt");
                 permissionsEnabled = false;
             }
         }
     }
     
     public boolean hasPerm(Player ply, String perm) {
     	if (permissionsEnabled) {
 			if (permissions.has(ply, "MyWordfilter.toggleOp")) return true;
 			else ply.sendMessage("You do not have permission for this.");
 		}
 		else if (ply.isOp()) return true;
 		else ply.sendMessage("You do not have permission for this.");
     	return false;
     }
 }
