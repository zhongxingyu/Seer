 package org.getchunky.chunky.protections.permission;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * @author dumptruckman
  */
 public enum Perm {
    CREEPER_GRIEF (new Permission("chunky.protections.grief.creeper", PermissionDefault.TRUE)),
     ;
 
     private Permission perm;
 
     Perm(Permission perm) {
         this.perm = perm;
     }
 
     private Permission getPerm() {
         return perm;
     }
 
     public boolean has(CommandSender sender) {
         return sender.hasPermission(perm);
     }
 
     public static void load(JavaPlugin plugin) {
         PluginManager pm = plugin.getServer().getPluginManager();
         for (Perm perm : Perm.values()) {
             pm.addPermission(perm.getPerm());
         }
     }
 }
 
