 package tk.allele.permissions.methods;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import tk.allele.permissions.PermissionsException;
 import tk.allele.permissions.PermissionsMethod;
 
 import java.util.logging.Logger;
 
 /**
  * Adapter for Bukkit's Superperms.
  * @see tk.allele.permissions.PermissionsMethod
  */
 public class BukkitPermissions implements PermissionsMethod {
     Plugin plugin;
     Logger log;
     String prefix;
     boolean hasSuperperms;
 
     public BukkitPermissions(Plugin plugin, Logger log, String prefix) {
         this.plugin = plugin;
         this.log = log;
         this.prefix = prefix;
         try {
            Player.class.getMethod("hasPermission");
             this.hasSuperperms = true;
         } catch (NoSuchMethodException e) {
             this.hasSuperperms = false;
         }
     }
 
     @Override
     public String toString() {
         return "Superperms";
     }
 
     @Override
     public int getPriority() {
         return 99;
     }
 
     @Override
     public boolean isAvailable() {
         return hasSuperperms;
     }
 
     @Override
     public boolean playerHasPermission(Player player, String permission) {
         return player.hasPermission(prefix + permission);
     }
 
     @Override
     public void throwIfCannot(Player player, String permission)
       throws PermissionsException {
         if(!playerHasPermission(player, permission)) {
             throw new PermissionsException(player, permission);
         }
     }
 }
