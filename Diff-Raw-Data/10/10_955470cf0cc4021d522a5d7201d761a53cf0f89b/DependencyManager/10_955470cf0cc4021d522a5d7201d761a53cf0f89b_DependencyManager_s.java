 /**
  * 
  */
 package org.morganm.heimdall;
 
 import net.milkbowl.vault.Vault;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.server.PluginDisableEvent;
 import org.bukkit.event.server.PluginEnableEvent;
 import org.bukkit.plugin.Plugin;
import org.morganm.heimdall.util.PermissionSystem;
 
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 import com.griefcraft.lwc.LWCPlugin;
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 
 import de.diddiz.LogBlock.LogBlock;
 
 /** This class manages all of our plugin dependencies. It exists so that if a plugin is
  * reloaded, our reference to that plugin is also updated.
  * 
  * @author morganm
  *
  */
 public class DependencyManager implements Listener {
 	private final Heimdall heimdall;
 	private LWCBridge lwcBridge;
 	
 	public DependencyManager(final Heimdall plugin) {
 		this.heimdall = plugin;
 		this.lwcBridge = new LWCBridge(heimdall);
 	}
 	
 	public LWCBridge getLWCBridge() {
 		return lwcBridge;
 	}
 
 	@EventHandler
 	public void onPluginEnable(final PluginEnableEvent event) {
 		final Plugin plugin = event.getPlugin();
 		
 		if( plugin.getDescription().getName().equals("LWC") && plugin instanceof LWCPlugin ) {
 			heimdall.verbose("detected LWC plugin load, updating plugin reference");
 			lwcBridge.updateLWC((LWCPlugin) plugin);
 			return;
 		}
 
 		if( plugin.getDescription().getName().equals("LogBlock") && plugin instanceof LogBlock ) {
 			heimdall.verbose("detected LogBlock plugin load, updating plugin reference");
 			heimdall.getBlockHistoryManager().pluginLoaded((LogBlock) plugin);
 			return;
 		}
 
 		// detect if the permission system we use just got reloaded
 		switch(heimdall.getPermissionSystem().getSystemInUse()) {
		case PermissionSystem.VAULT:
 			if( plugin.getDescription().getName().equals("Vault") && plugin instanceof Vault ) {
 				heimdall.verbose("detected Vault plugin load, updating plugin reference");
 				heimdall.getPermissionSystem().setupPermissions(heimdall.isVerboseEnabled());
 			}
 			break;
 			
		case PermissionSystem.WEPIF:
 			if( plugin.getDescription().getName().equals("WorldEdit") && plugin instanceof WorldEditPlugin ) {
 				heimdall.verbose("detected WorldEdit plugin load, updating plugin reference");
 				heimdall.getPermissionSystem().setupPermissions(heimdall.isVerboseEnabled());
 			}
 			break;
 			
		case PermissionSystem.PEX:
 			if( plugin.getDescription().getName().equals("PermissionsEx") && plugin instanceof PermissionsEx ) {
 				heimdall.verbose("detected PEX plugin load, updating plugin reference");
 				heimdall.getPermissionSystem().setupPermissions(heimdall.isVerboseEnabled());
 			}
 			break;
 		}
 	}
 
 	@EventHandler
 	public void onPluginDisable(final PluginDisableEvent event) {
 		final Plugin plugin = event.getPlugin();
 		
 		if( plugin.getDescription().getName().equals("LWC") && plugin instanceof LWCPlugin ) {
 			heimdall.verbose("detected LWC plugin unload, removing plugin reference");
 			lwcBridge.updateLWC(null);
 			return;
 		}
 
 		if( plugin.getDescription().getName().equals("LogBlock") && plugin instanceof LogBlock ) {
 			heimdall.verbose("detected LogBlock plugin unload, removing plugin reference");
 			heimdall.getBlockHistoryManager().pluginUnloaded((LogBlock) plugin);
 			return;
 		}
 	}
 }
