 package com.ementalo.commandalert;
 
 import java.util.logging.Level;
 
 import com.ementalo.commandalert.Permissions.*;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.server.PluginDisableEvent;
 import org.bukkit.event.server.PluginEnableEvent;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 
 
 public class CommandAlertServerListener implements Listener
 {
 	CommandAlert parent = null;
 
 	public CommandAlertServerListener(CommandAlert parent)
 	{
 		this.parent = parent;
 	}
 
     @EventHandler()
     public void onPluginEnable(PluginEnableEvent event) {
         if (parent.permsBase != null) return;
         if (parent.config.getUseBukkitPerms()) {
             parent.permsBase = new BukkitPerms();
            CommandAlert.log.log(Level.INFO, "[CommandAlert] Using bukkit permissions");
             return;
         }
 
         final PluginManager pm = parent.getServer().getPluginManager();
         Plugin permPlugin;
 
 
         permPlugin = pm.getPlugin("bPermissions");
         if (permPlugin != null && permPlugin.isEnabled()) {
             parent.permsBase = new bPermissionsPerms();
             permPlugin = null;
            CommandAlert.log.log(Level.INFO, "[CommandAlert] Found bPermissions. Using it for permissions");
             return;
         }
 
         permPlugin = pm.getPlugin("GroupManager");
         if (permPlugin != null && permPlugin.isEnabled()) {
             parent.permsBase = new GroupManagerPerms(permPlugin);
             permPlugin = null;
            CommandAlert.log.log(Level.INFO, "[CommandAlert] Found GroupManager. Using it for permissions");
             return;
         }
 
         permPlugin = pm.getPlugin("PermissionsEx");
         if (permPlugin != null && permPlugin.isEnabled()) {
             parent.permsBase = new PexPerms();
             permPlugin = null;
            CommandAlert.log.log(Level.INFO, "[CommandAlert] Found PermissionsEx. Using it for permissions");
             return;
         }
 
         permPlugin = pm.getPlugin("Permissions");
         if (permPlugin != null && permPlugin.isEnabled()) {
             parent.permsBase = new P3Perms(permPlugin);
             permPlugin = null;
            CommandAlert.log.log(Level.INFO, "[CommandAlert] Found Permissions. Using it for permissions");
         }
     }
 
     @EventHandler()
     public void onPluginDisable(PluginDisableEvent event) {
         if (parent.permsBase == null) return;
         String pluginName = event.getPlugin().getDescription().getName();
         if (pluginName.equalsIgnoreCase("Permissions")
                 || pluginName.equalsIgnoreCase("bPermissions")
                 || pluginName.equalsIgnoreCase("GroupManager")
                 || pluginName.equalsIgnoreCase("PermissionsEx")) {
             parent.permsBase = null;
            CommandAlert.log.log(Level.INFO, "[CommandAlert] " + pluginName + " disabled. Commands available to all");
         }
     }
 }
