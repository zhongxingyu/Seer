 
 package com.codisimus.plugins.chunkown;
 
 import org.bukkit.event.server.ServerListener;
 import com.codisimus.plugins.chunkown.register.payment.Methods;
 import org.bukkit.event.server.PluginEnableEvent;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 /**
  * Checks for Permission/Economy plugins whenever a Plugin is enabled
  * 
  * @author Codisimus
  */
 public class PluginListener extends ServerListener {
     public PluginListener() { }
     public static boolean useBP;
 
     @Override
     public void onPluginEnable(PluginEnableEvent event) {
         linkPermissions();
         linkEconomy();
     }
 
     /**
      * Finds and links a Permission Plugin
      *
      */
     public void linkPermissions() {
         //Return if we have already have a permissions plugin
         if (ChunkOwn.permissions != null)
             return;
 
         //Return if PermissionsEx is not enabled
         if (!ChunkOwn.pm.isPluginEnabled("PermissionsEx"))
             return;
 
        //Return if OP permissions will be used
         if (useBP)
             return;
 
         ChunkOwn.permissions = PermissionsEx.getPermissionManager();
         System.out.println("[ChunkOwn] Successfully linked with PermissionsEx!");
     }
 
     /**
      * Finds and links an Economy Plugin
      *
      */
     public void linkEconomy() {
         //Return if we already have an Economy Plugin
         if (Methods.hasMethod())
             return;
 
         //Return if no Economy is wanted
         if (Register.economy.equalsIgnoreCase("none"))
             return;
 
         //Set preferred Plugin if there is one
         if (!Register.economy.equalsIgnoreCase("auto"))
             Methods.setPreferred(Register.economy);
 
         //Find an Economy Plugin (will first look for preferred Plugin)
         Methods.setMethod(ChunkOwn.pm);
 
         //Reset Methods if the preferred Economy was not found
         if (!Methods.getMethod().getName().equalsIgnoreCase(Register.economy) && !Register.economy.equalsIgnoreCase("auto")) {
             Methods.reset();
             return;
         }
 
         Register.econ = Methods.getMethod();
         System.out.println("[ChunkOwn] Successfully linked with "+Register.econ.getName()+" "+Register.econ.getVersion()+"!");
     }
 }
