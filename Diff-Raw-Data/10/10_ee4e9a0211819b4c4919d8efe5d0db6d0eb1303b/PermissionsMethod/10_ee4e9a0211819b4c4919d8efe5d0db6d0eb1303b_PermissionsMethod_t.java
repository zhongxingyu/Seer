 package tk.kirlian.permissions;
 
 import org.bukkit.entity.Player;
 import tk.kirlian.util.priority.Prioritizable;
 
 /**
  * Provides a consistent interface to various ways of deciding permissions.
  */
 public interface PermissionsMethod extends Prioritizable {
     /**
      * Return the human-readable name of this PermissionsMethod.
      */
     public String toString();
 
     /**
      * Decide whether a player has permission to do something.
      */
     public boolean playerHasPermission(Player player, String permission);
 
     /**
      * Throw a {@link PermissionsException} if a player doesn't have permission to do this.
     *
     * @throws PermissionsException if the player doesn't have permission.
      */
     public void throwIfCannot(Player player, String permission) throws PermissionsException;
 }
