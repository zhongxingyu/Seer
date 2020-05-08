 package com.ementalo.tcl;
 
 import java.util.logging.Level;
 
 import com.ementalo.tcl.Permissions.*;
 import org.bukkit.event.server.PluginDisableEvent;
 import org.bukkit.event.server.PluginEnableEvent;
 import org.bukkit.event.server.ServerListener;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 
 
 public class TeleConfimLiteServerListener extends ServerListener {
     TeleConfirmLite parent = null;
 
     public TeleConfimLiteServerListener(TeleConfirmLite parent) {
         this.parent = parent;
     }
 
     @Override
     public void onPluginEnable(PluginEnableEvent event) {
         if (parent.permsBase != null) return;
         if (Config.useBukkitPerms) {
             parent.permsBase = new BukkitPerms();
             TeleConfirmLite.log.log(Level.INFO, "[TeleConfirmLite] Using bukkit permissions");
             return;
         }
 
         final PluginManager pm = parent.getServer().getPluginManager();
         Plugin permPlugin;
 
 
         permPlugin = pm.getPlugin("bPermissions");
         if (permPlugin != null && permPlugin.isEnabled()) {
             parent.permsBase = new bPermissionsPerms();
             permPlugin = null;
             TeleConfirmLite.log.log(Level.INFO, "[TeleConfirmLite] Found bPermissions. Using it for permissions");
             return;
         }
 
         permPlugin = pm.getPlugin("GroupManager");
         if (permPlugin != null && permPlugin.isEnabled()) {
             parent.permsBase = new GroupManagerPerms(permPlugin);
             permPlugin = null;
             TeleConfirmLite.log.log(Level.INFO, "[TeleConfirmLite] Found GroupManager. Using it for permissions");
             return;
         }
 
         permPlugin = pm.getPlugin("PermissionsEx");
         if (permPlugin != null && permPlugin.isEnabled()) {
             parent.permsBase = new PexPerms();
             permPlugin = null;
             TeleConfirmLite.log.log(Level.INFO, "[TeleConfirmLite] Found PermissionsEx. Using it for permissions");
             return;
         }
 
         permPlugin = pm.getPlugin("Permissions");
         if (permPlugin != null && permPlugin.isEnabled()) {
            parent.permsBase = new P3Perms(event.getPlugin());
             permPlugin = null;
             TeleConfirmLite.log.log(Level.INFO, "[TeleConfirmLite] Found Permissions. Using it for permissions");
         }
     }
 
     @Override
     public void onPluginDisable(PluginDisableEvent event) {
         if (parent.permsBase == null) return;
         String pluginName = event.getPlugin().getDescription().getName();
         if (pluginName.equalsIgnoreCase("Permissions")
                 || pluginName.equalsIgnoreCase("bPermissions")
                 || pluginName.equalsIgnoreCase("GroupManager")
                 || pluginName.equalsIgnoreCase("PermissionsEx")) {
             parent.permsBase = null;
             TeleConfirmLite.log.log(Level.INFO, "[TeleConfirmLite] " + pluginName + " disabled. Commands available to all");
         }
     }
 }
