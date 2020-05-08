 package tux2.spoutlwc;
 
 import java.util.HashSet;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 import com.griefcraft.lwc.*;
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 /**
  * spoutlwc for Bukkit
  *
  * @author tux2
  */
 public class SpoutLWC extends JavaPlugin {
 	private static PermissionHandler Permissions;
 	
 	LWC lwc = null;
     public HashSet<Byte> transparentBlocks = new HashSet<Byte>();
     
     ConcurrentHashMap<SpoutPlayer, PlayerLwcGUI> guiscreens = new ConcurrentHashMap<SpoutPlayer, PlayerLwcGUI>();
     ConcurrentHashMap<SpoutPlayer, UnlockGUI> unlockscreens = new ConcurrentHashMap<SpoutPlayer, UnlockGUI>();
 
     public SpoutLWC() {
         super();
         
         //Setting transparent blocks.
         transparentBlocks.add((byte) 0); // Air
         transparentBlocks.add((byte) 8); // Water
         transparentBlocks.add((byte) 9); // Stationary Water
         transparentBlocks.add((byte) 20); // Glass
         transparentBlocks.add((byte) 30); // Cobweb
         transparentBlocks.add((byte) 65); // Ladder
         transparentBlocks.add((byte) 66); // Rail
         transparentBlocks.add((byte) 78); // Snow
         transparentBlocks.add((byte) 83); // Sugar Cane
         transparentBlocks.add((byte) 101); // Iron Bars
         transparentBlocks.add((byte) 102); // Glass Pane
         transparentBlocks.add((byte) 106); // Vines
     }
 
    
 
     public void onEnable() {
     	setupPermissions();
     	Plugin lwcPlugin = getServer().getPluginManager().getPlugin("LWC");
     	if(lwcPlugin != null) {
     	    lwc = ((LWCPlugin) lwcPlugin).getLWC();
    	    LWCScreenListener serverListener = new LWCScreenListener(this);
 
             // Register our events
             PluginManager pm = getServer().getPluginManager();
             pm.registerEvent(Type.CUSTOM_EVENT, new LWCScreenListener(this), Priority.Normal, this);
             pm.registerEvent(Type.CUSTOM_EVENT, new LWCInputListener(this), Priority.Normal, this);
             pm.registerEvent(Type.PLUGIN_ENABLE, serverListener, Priority.Monitor, this);
             pm.registerEvent(Type.PLUGIN_DISABLE, serverListener, Priority.Monitor, this);
            
 
             // EXAMPLE: Custom code, here we just output some info so we can check all is well
             PluginDescriptionFile pdfFile = this.getDescription();
             System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
     	}else {
     		System.out.println("[SpoutLWC] Lightweight chest not found! This plugin will not work!");
     	}
     }
     
     public void onDisable() {
         // NOTE: All registered events are automatically unregistered when a plugin is disabled
 
         // EXAMPLE: Custom code, here we just output some info so we can check all is well
         System.out.println("Goodbye world!");
     }
 
 	public boolean hasPermissions(Player player, String node) {
 	    if (Permissions != null) {
 	        return Permissions.has(player, node);
 	    } else {
 	        return player.hasPermission(node);
 	    }
 	}
 	
 	private void setupPermissions() {
 	    Plugin permissions = this.getServer().getPluginManager().getPlugin("Permissions");
 	
 	    if (Permissions == null) {
 	        if (permissions != null) {
 	            Permissions = ((Permissions)permissions).getHandler();
 	        } else {
 	        }
 	    }
 	}
 }
 
