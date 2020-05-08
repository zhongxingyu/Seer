 package edgruberman.bukkit.simplelocks;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 
 /**
  * Sign block that defines access information for the attached block.
  */
 public class Lock {
 
     public final Locksmith locksmith;
     public final Sign sign;
 
     /**
      * Existing lock
      *
      * @param block sign containing lock information
      */
     Lock(final Locksmith locksmith, final Block block) {
         this.locksmith = locksmith;
         this.sign = (Sign) block.getState();
     }
 
     /**
      * Create new lock in-game
      *
      * @param block changed to wall sign containing lock information
      * @param attached face adjacent to lockable
      * @param access player name or group name
      */
     Lock(final Locksmith locksmith, final Block block, final BlockFace attached, final String access) {
         this.locksmith = locksmith;
         final Block locked = block.getRelative(attached);
         if (this.locksmith.isLocked(locked)) throw new IllegalArgumentException("Block already locked");
 
         final org.bukkit.material.Sign material = new org.bukkit.material.Sign(Material.WALL_SIGN);
         material.setFacingDirection(attached.getOppositeFace());
         block.setTypeIdAndData(material.getItemTypeId(), material.getData(), true);
         //this.sign.setData(material);
         this.sign = (Sign) block.getState();
 
         this.sign.setLine(0, this.locksmith.title);
 
         this.addAccess(access); // Updates block for previous state changes also
     }
 
     public List<String> getAccess() {
         final String[] lines = this.sign.getLines();
         final List<String> access = new ArrayList<String>();
         for (int i = 1; i <= 3; i++)
             if (lines[i].length() > 0)
                 access.add(lines[i]);
 
         return access;
     }
 
     /**
      * Explicitly (group memberships ignored) grants access to lock
      *
      * @param name player name or partial group name
      */
     public void addAccess(final String name) {
         if (name.length() < 1 || name.length() > Locksmith.MAXIMUM_SIGN_LINE_LENGTH)
             throw new IllegalArgumentException("Lock access name must be between 1 and " + Locksmith.MAXIMUM_SIGN_LINE_LENGTH + " characters; name: " + name);
 
         if (this.hasExplicitAccess(name))
             throw new IllegalStateException("Lock access has already been granted for name: " + name);
 
         Integer blank = null;
         if (this.sign.getLine(1).length() == 0) blank = 1;
         else if (this.sign.getLine(2).length() == 0) blank = 2;
         else if (this.sign.getLine(3).length() == 0) blank = 3;
         if (blank == null)
             throw new IllegalStateException("Lock has no blank access lines left to add access for name: " + name);
 
         this.sign.setLine(blank, name);
         this.update();
     }
 
     /**
      * Revokes explicit (group memberships ignored) lock access
      *
      * @param name player name or partial group name
      */
     public void removeAccess(final String name) {
         final String compare = name.toLowerCase();
 
         Integer direct = null;
         if (this.sign.getLine(1) != null && this.sign.getLine(1).toLowerCase().equals(compare)) direct = 1;
         else if (this.sign.getLine(2) != null && this.sign.getLine(2).toLowerCase().equals(compare)) direct = 2;
         else if (this.sign.getLine(3) != null && this.sign.getLine(3).toLowerCase().equals(compare)) direct = 3;
         if (direct == null)
             throw new IllegalStateException("Lock does not grant access to name: " + name);
 
         this.sign.setLine(direct, "");
         this.update();
     }
 
     /**
      * Determines if name has been explicitly (group memberships ignored)
      * assigned as having access (ownership ignored).
      *
      * @param name player name or partial group name
      * @return true if name is explicitly listed as having access; false otherwise
      */
     public boolean hasExplicitAccess(final String name) {
         final String compare = name.toLowerCase();
         for (final String line : this.getAccess())
             if (line.toLowerCase().equals(compare))
                 return true;
 
         return false;
     }
 
     /**
      * Determines if player has any type of access
      * (owner or access, explicit or group)
      *
      * @return true if player has access or is owner; false otherwise
      */
     public boolean hasAccess(final Player player) {
         if (player.hasPermission("simplelocks.override")) return true;
 
         if (this.hasExplicitAccess(player.getName())) return true;
 
         for (final String line : this.getAccess())
             if (player.isPermissionSet(line) && player.hasPermission(line))
                 return true;
 
         return false;
     }
 
     public Block getLocked() {
         final org.bukkit.material.Sign material = (org.bukkit.material.Sign) this.sign.getData();
         return this.sign.getBlock().getRelative(material.getAttachedFace());
     }
 
     public void update() {
         if (!this.sign.update())
             throw new IllegalStateException("Unable to update sign block; State was changed external to plugin");
     }
 
 }
