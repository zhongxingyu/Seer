 package edgruberman.bukkit.simplelocks;
 
 import java.util.Arrays;
import java.util.List;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 
 public class Lock {
     private static String title;
     
     private Block block = null;
     
     /**
      * Sign block associated with lock.
      * 
      * @return Block with lock information.
      */
     public Block getBlock() {
         return this.block;
     }
     
     /**
      * Sign block containing lock information
      * 
      * @param block Block with lock information.
      */
     public void setBlock(Block block) {
         this.block = block;
     }
     
     /**
      * Utility interaction for managing information on existing lock.
      * 
      * @param block Existing sign containing lock information.
      */
     public Lock(Block block) {
         this(block, null, null, null);
     }
     
     /**
      * Create new lock from nothing with only owner access.
      * 
      * @param block Block to place lock at.
      * @param attachedFace Face adjacent to lockable block.
      * @param owner Owner of the lock.
      */
     public Lock(Block block, BlockFace attachedFace, Player owner) {
         this(block, attachedFace, owner.getName(), null);
     }
     
     /**
      * Create new lock from nothing with only owner access.
      * 
      * @param block Block to place lock at.
      * @param attachedFace Face adjacent to lockable block.
      * @param owner Owner of the lock.
      */
     public Lock(Block block, BlockFace attachedFace, String owner) {
         this(block, attachedFace, owner, null);
     }
     
     /**
      * Take existing sign, convert to lock, and attach to first lockable object behind sign.
      * 
      * @param block Existing sign containing lock request.
      * @param owner Owner of the lock.
      * @param lines All four lines of the sign containing any additional names with access to lock on lines 2, 3, or 4. (Only two will be added maximum.)
      */
     public Lock(Block block, Player owner, String[] lines) {
         this(block, null, owner.getName(), lines);
     }
     
     public Lock(Block block, BlockFace attachedFace, String owner, String[] lines) {
         this.setBlock(block);
         
         if (owner == null) return;
         
         BlockFace attachedTo = null;
         if (attachedFace == null) {
             attachedTo = this.setAttachedFace();
         } else {
             attachedTo = this.setAttachedFace(attachedFace);
         }
         if (attachedTo == null) return; //TODO raise error
         
         this.setOwner(owner);
         
         if (lines != null && lines.length >= 2) {
             for (String line : Arrays.asList(lines).subList(1, Math.min(lines.length, 4))) {
                 this.addAccess(line);
             }
         }
        
         this.setClosed();
     }
     
     /**
      * Find nearest lockable object based on initial orientation. 
      */
     public BlockFace setAttachedFace() {
         Block check = null;
         
         Block sign = this.getBlock();
         
         org.bukkit.material.Sign material = this.getSignMaterial();
         
         BlockFace attachedTo = null;        
         switch (material.getFacing().getOppositeFace()) {
             case NORTH:
             case EAST:
             case SOUTH:
             case WEST:
                 check = sign.getRelative(material.getFacing().getOppositeFace());
                 if (!check.getType().equals(Material.CHEST)) return null;
                 if (Lock.getLock(check) != null) return null;
                 attachedTo = material.getFacing().getOppositeFace();
                 break;
             case NORTH_EAST:
                 check = sign.getRelative(BlockFace.NORTH);
                 if (check.getType().equals(Material.CHEST) 
                         && Lock.getLock(check) == null) {
                     attachedTo = BlockFace.NORTH;
                     break;
                 }
                 check = sign.getRelative(BlockFace.EAST);
                 if (!check.getType().equals(Material.CHEST)) return null; 
                 if (Lock.getLock(check) != null) return null;
                 attachedTo = BlockFace.EAST;
                 break;
             case SOUTH_EAST:
                 check = sign.getRelative(BlockFace.SOUTH);
                 if (check.getType().equals(Material.CHEST) 
                         && Lock.getLock(check) == null) {
                     attachedTo = BlockFace.SOUTH;
                     break;
                 }
                 check = sign.getRelative(BlockFace.EAST);
                 if (!check.getType().equals(Material.CHEST)) return null; 
                 if (Lock.getLock(check) != null) return null;
                 attachedTo = BlockFace.EAST;
                 break;
             case SOUTH_WEST:
                 check = sign.getRelative(BlockFace.SOUTH);
                 if (check.getType().equals(Material.CHEST) 
                         && Lock.getLock(check) == null) {
                     attachedTo = BlockFace.SOUTH;
                     break;
                 }
                 check = sign.getRelative(BlockFace.WEST);
                 if (!check.getType().equals(Material.CHEST)) return null; 
                 if (Lock.getLock(check) != null) return null;
                 attachedTo = BlockFace.WEST;
                 break;
             case NORTH_WEST:
                 check = sign.getRelative(BlockFace.NORTH);
                 if (check.getType().equals(Material.CHEST) 
                         && Lock.getLock(check) == null) {
                     attachedTo = BlockFace.NORTH;
                     break;
                 }
                 check = sign.getRelative(BlockFace.WEST);
                 if (!check.getType().equals(Material.CHEST)) return null; 
                 if (Lock.getLock(check) != null) return null;
                 attachedTo = BlockFace.WEST;
                 break;
         }
         
         return this.setAttachedFace(attachedTo);
     }
     
     public BlockFace setAttachedFace(BlockFace attachedFace) {
         if (attachedFace == null) return null;
         
         Block locked = this.getBlock().getRelative(attachedFace);
         if (Lock.getLock(locked) != null) return null; //TODO raise error
         
         org.bukkit.material.Sign material = new org.bukkit.material.Sign(Material.WALL_SIGN);
         material.setFacingDirection(attachedFace.getOppositeFace());
         this.getBlock().setTypeIdAndData(material.getItemTypeId(), material.getData(), true);
         
         return material.getAttachedFace();
     }
     
     public void setOwner(String owner) {
         this.getSignBlock().setLine(1, Lock.left(owner, 15));
     }
     
     public void addAccess(String name) {
         name = Lock.left(name, 15);
         
         // Do not add access if name already has access.
         if (this.hasAccess(name)) return;
         
         org.bukkit.block.Sign state = this.getSignBlock();
         
         // Find first free line to add access to.
         Integer addTo = null;
         if (state.getLine(2).length() == 0) addTo = 2;
         else if (state.getLine(3).length() == 0) addTo = 3;
         
         // Do not add access if no more room on sign.
         if (addTo == null) return; // TODO raise error
         
         state.setLine(addTo, name);
     }
     
     public void removeAccess(String name) {
         name = Lock.left(name, 15);
         
         // Do not remove access if name does not already have access.
         if (!this.hasAccess(name)) return;
         
         // Find line with matching name.
         org.bukkit.block.Sign state = this.getSignBlock();
         Integer removeFrom = null;
         if (state.getLine(2) != null && state.getLine(2).equals(name)) removeFrom = 2;
         else if (state.getLine(3) != null && state.getLine(3).equals(name)) removeFrom = 3;
         
         if (removeFrom == null) return; 
         
         state.setLine(removeFrom, "");
     }
     
     /**
      * Force clients to render sign again to recognize new or removed text.
      */
     public void refresh() {
         // Toggle attached direction.
         org.bukkit.material.Sign material = new org.bukkit.material.Sign(this.getBlock().getType());
         material.setData(this.getSignMaterial().getData());
         
         material.setFacingDirection(material.getAttachedFace());
         this.getBlock().setData(material.getData());
         
         material.setFacingDirection(material.getAttachedFace());
         this.getBlock().setData(material.getData());
     }
     
     public void setClosed() {
         this.getSignBlock().setLine(0, Lock.left(Lock.title, 15));
     }
     
     /**
      * This function exists because raising exceptions confuses me.
      * 
      * @return true if this lock object was successful at creating a lock in the world.
      */
     public boolean isLock() {
         return Lock.isLock(this.getBlock());
     }
     
     public boolean hasAccess(Player player) {
         return this.hasAccess(player.getName());
     }
     
     public boolean hasAccess(String name) {
        if (this.isOwner(name)) return true;
        
        List<String> access = Arrays.asList(this.getSignBlock().getLines())
            .subList(2, Math.min(this.getSignBlock().getLines().length, 4));
        for (String member : access) {
            if (member.equalsIgnoreCase(name)) return true;
        }
        
        return false;
     }
     
     public String getOwner() {
         return this.getSignBlock().getLine(1);
     }
     
     public boolean isOwner(Player player) {
         return this.isOwner(player.getName());
     }
     
     public boolean isOwner(String name) {
        return this.getSignBlock().getLine(1).equalsIgnoreCase(name);
     }
     
     public Block getLocked() {
         return this.getBlock().getRelative(this.getSignMaterial().getAttachedFace());
     }
     
     public String getDescription() {
         Block locked = this.getLocked();
         
         String description = "---- Lock";
         description += "\nOwner: " + this.getOwner();
         description += "\nAccess: " + this.getSignBlock().getLine(2) + " " + this.getSignBlock().getLine(3);
         description += "\n" + locked.getType().toString() + " at x: " + locked.getX() + " y: " + locked.getY() + " z: " + locked.getZ();
         
         return description;
     }
     
     /**
      * Utility function access sign text easier.
      * 
      * @return Sign block state.
      */
     public org.bukkit.block.Sign getSignBlock() {
         if (this.getBlock() == null) return null;
         
         return (org.bukkit.block.Sign) this.getBlock().getState();
     }
         
     /**
      * Utility function to manage direction of sign easier.
      * 
      * @return Sign material data.
      */
     public org.bukkit.material.Sign getSignMaterial() {
         if (this.getBlock() == null) return null;
         
         org.bukkit.material.Sign material = new org.bukkit.material.Sign(this.getBlock().getType());
         material.setData(this.getBlock().getData());
         return material;
     }
     
     public static void setTitle(String title) {
         Lock.title = Lock.left(title, 15);
     }
     
     public static String getTitle() {
         return Lock.title;
     }
     
     public static Lock getLock(Block block) {
         if (Lock.isLock(block)) return new Lock(block);
         
         if (!block.getType().equals(Material.CHEST)) return null;
         
         return Lock.getChestLock(block);
     }
     
     /**
      * Indicates if block is a lock for any object.
      * 
      * @param block Block to check if it is a lock.
      * @return true if block is a lock.
      */
     public static boolean isLock(Block block) {
         return Lock.isLock(block, null);
     }
 
     /**
      * Indicates if block is a lock for a specific object.
      * 
      * @param block Block to check if it is a lock.
      * @param attachedTo BlockFace that locked object is relative to.
      * @return true if block is a lock attached as specified.
      */
     public static boolean isLock(Block block, BlockFace attachedTo) {
         org.bukkit.material.Sign material = new org.bukkit.material.Sign(block.getType());
         
         // Locks are always wall signs.
         if (!material.isWallSign())
             return false;
 
         if (attachedTo != null) {
             // Locks must be directly attached to locked object.
             material.setData(block.getData());
             if (!material.getAttachedFace().equals(attachedTo))
                 return false;
         }
 
         // First line of sign must contain standard lock title.
         if (!((org.bukkit.block.Sign) block.getState()).getLine(0).equals(Lock.title))
             return false;
         
         return true;
     }
     
     private static Lock getChestLock(Block chest) {
         return Lock.getChestLock(chest, null);
     }
     
     private static Lock getChestLock(Block chest, Block otherHalf) {
         Block block;
         
         block = chest.getRelative(BlockFace.NORTH);
         if (Lock.isLock(block, BlockFace.NORTH.getOppositeFace())) return new Lock(block);
         
         block = chest.getRelative(BlockFace.EAST);
         if (Lock.isLock(block, BlockFace.EAST.getOppositeFace())) return new Lock(block);
         
         block = chest.getRelative(BlockFace.SOUTH);
         if (Lock.isLock(block, BlockFace.SOUTH.getOppositeFace())) return new Lock(block);
         
         block = chest.getRelative(BlockFace.WEST);
         if (Lock.isLock(block, BlockFace.WEST.getOppositeFace())) return new Lock(block);
         
         // If this is the second half of a chest don't look further.
         if (otherHalf != null) return null;
         
         // Check if second half of chest exists and has a lock.
         block = chest.getRelative(BlockFace.NORTH);
         if (block.getType().equals(Material.CHEST))
             return Lock.getChestLock(block, chest);
         
         block = chest.getRelative(BlockFace.EAST);
         if (block.getType().equals(Material.CHEST))
             return Lock.getChestLock(block, chest);
         
         block = chest.getRelative(BlockFace.SOUTH);
         if (block.getType().equals(Material.CHEST))
             return Lock.getChestLock(block, chest);
         
         block = chest.getRelative(BlockFace.WEST);
         if (block.getType().equals(Material.CHEST))
             return Lock.getChestLock(block, chest);
  
         return null;
     }
     
     private static String left(String s, int len) {
         if (s.length() <= len) return s;
 
         return s.substring(0, len);
     }
 }
