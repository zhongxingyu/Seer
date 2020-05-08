 package tk.kirlian.protection.methods;
 
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
 import tk.kirlian.protection.ProtectionMethod;
 
 /**
  * A placeholder method that always denies access.
  * <p>
  * This should be used for testing only.
  */
 public class FailMethod implements ProtectionMethod {
 
     @Override
     public boolean isEnabled() {
         return true;
     }
 
     @Override
     public String toString() {
         return "FailMethod";
     }
 
     @Override
     public boolean canAccess(String playerName, Block block) {
         return false;
     }
 
     @Override
     public boolean canAccess(Player player, Block block) {
         return false;
     }
 }
