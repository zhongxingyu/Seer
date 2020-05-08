 package tk.kirlian.DuckShop.permissions;
 
 import org.bukkit.entity.Player;
 
 import tk.kirlian.DuckShop.DuckShop;
 import java.util.logging.Logger;
 
 /**
  * A fallback permissions handler that only lets admins do anything.
  * @see PermissionsProvider
  */
 public class OpsOnlyPermissionsProvider extends PermissionsProvider {
     private static OpsOnlyPermissionsProvider provider;
     private DuckShop plugin;
     private Logger log;
 
     private OpsOnlyPermissionsProvider(DuckShop plugin) {
         this.plugin = plugin;
        this.log = plugin.log;
     }
 
     public static OpsOnlyPermissionsProvider getInstance(DuckShop plugin) {
         if(provider == null) {
             provider = new OpsOnlyPermissionsProvider(plugin);
         }
         return provider;
     }
 
     public String getName() {
         return "OpsOnly";
     }
 
     public int getPriority() {
         return 100;
     }
 
     public boolean isAvailable() {
         return true;
     }
 
     public boolean playerHasPermission(Player player, String permission) {
         return player.isOp();
     }
 }
