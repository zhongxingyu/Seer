 package me.slaps.DMWrapper;
 
 import org.bukkit.event.Event;
 import org.bukkit.event.server.PluginEnableEvent;
 import org.bukkit.event.server.ServerListener;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 
 
 import com.gmail.haloinverse.DynamicMarket.DynamicMarket;
import com.nijiko.permissions.PermissionHandler;
 
 public class DMWrapperPluginListener extends ServerListener {
 	
     DMWrapper parent;
     
 	public DMWrapperPluginListener(DMWrapper plug) {
 	    parent = plug;
 	    plug.getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_ENABLE, this, Priority.Monitor, plug);
 	}
 	
 	@Override
     public void onPluginEnable(PluginEnableEvent event) {
     	if(event.getPlugin().getDescription().getName().equals("DynamicMarket")) {
             Plugin pluginDM = DynamicMarket.getTheServer().getPluginManager().getPlugin("DynamicMarket");
 	 
             if (pluginDM != null)	enableDynamicMarket((DynamicMarket)pluginDM);
     	}
 
    	if(event.getPlugin().getDescription().getName().equals("PermissionHandler")) {
        	Plugin pluginPerms = DynamicMarket.getTheServer().getPluginManager().getPlugin("PermissionHandler");
         	
	        if(pluginPerms != null)	enablePermissions((PermissionHandler)pluginPerms);
     	}
     }
 	
 	public void enableDynamicMarket(DynamicMarket plugin) {
 		DMWrapper.dm = plugin;
 		DMWrapper.dm.hookWrapper(parent);
   		DMWrapper.info("Successfully linked with DynamicMarket");
 	}
 	
	public void enablePermissions(PermissionHandler plugin) {
 		DMWrapper.perms = plugin;
 		DMWrapper.info("Successfully linked with Permissions");	  		
 	}
 	
 	public void tryEnablePlugins(PluginManager pm) {
 	  	if(pm.getPlugin("DynamicMarket").isEnabled() && DMWrapper.dm == null) {
 	  		Plugin plugin = pm.getPlugin("DynamicMarket");
 	  		enableDynamicMarket((DynamicMarket)plugin);
 	  	}
 	  	if(pm.getPlugin("Permissions").isEnabled() && DMWrapper.perms == null) {
 	  		Plugin plugin = pm.getPlugin("Permissions");
	  		enablePermissions((PermissionHandler)plugin);
 	  	}
 	}	
 }
