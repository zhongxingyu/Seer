 package tk.allele.permissions.methods;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import tk.allele.permissions.PermissionsException;
 import tk.allele.permissions.PermissionsMethod;
 
 import java.util.logging.Logger;
 
 /**
  * A fallback permissions handler that only lets admins do anything.
 * <p>
 * This should only be needed on CraftBukkit builds before Superperms was
 * introduced, i.e. before build 1000.
 *
  * @see PermissionsMethod
  */
 public class OpsOnlyPermissions implements PermissionsMethod {
     private Plugin plugin;
     private Logger log;
 
     public OpsOnlyPermissions(Plugin plugin, Logger log) {
         this.plugin = plugin;
         this.log = log;
     }
 
     @Override
     public String toString() {
         return "OpsOnly";
     }
 
     @Override
     public int getPriority() {
         return 100;
     }
 
     @Override
     public boolean isAvailable() {
         return true;
     }
 
     @Override
     public boolean playerHasPermission(Player player, String permission) {
         return player.isOp();
     }
 
     @Override
     public void throwIfCannot(Player player, String permission)
       throws PermissionsException {
         if(!playerHasPermission(player, permission)) {
             throw new PermissionsException(player, permission);
         }
     }
 }
