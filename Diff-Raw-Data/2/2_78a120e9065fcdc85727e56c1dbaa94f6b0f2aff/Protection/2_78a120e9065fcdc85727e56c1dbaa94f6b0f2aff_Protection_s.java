 package de.minestar.moneypit.data.protection;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import org.bukkit.entity.Player;
 
 import com.bukkit.gemo.utils.UtilPermissions;
 
 import de.minestar.moneypit.data.BlockVector;
 import de.minestar.moneypit.data.subprotection.SubProtection;
 
 public class Protection {
     private final int ID;
     private final BlockVector vector;
     private final String owner;
     private ProtectionType type;
     private HashSet<String> guestList;
     private HashMap<BlockVector, SubProtection> subProtections;
 
     /**
      * Constructor
      * 
      * @param owner
      * @param type
      */
     public Protection(int ID, BlockVector vector, String owner, ProtectionType type) {
         this.ID = ID;
         this.vector = vector;
         this.owner = owner;
         this.type = type;
         this.guestList = null;
         this.subProtections = null;
     }
 
     /**
      * Get the ID of this protection
      * 
      * @return the unique ID
      */
     public int getID() {
         return ID;
     }
 
     /**
      * Get the vector of this protection
      * 
      * @return the vector
      */
     public BlockVector getVector() {
         return this.vector;
     }
 
     /**
      * Get the ModuleID
      * 
      * @return the ModuleID of this Protection
      */
     public int getModuleID() {
         return this.vector.getLocation().getBlock().getTypeId();
     }
 
     /**
      * Get the owner of this protection
      * 
      * @return the owner;
      */
     public String getOwner() {
         return owner;
     }
 
     /**
      * Is this protection public?
      * 
      * @return <b>true</b> if it is public, otherwise <b>false</b>
      */
     public boolean isPublic() {
         return this.type == ProtectionType.PUBLIC;
     }
 
     /**
      * Is this protection a giftprotection?
      * 
      * @return <b>true</b> if it is a giftprotection, otherwise <b>false</b>
      */
     public boolean isGift() {
         return this.type == ProtectionType.GIFT;
     }
 
     /**
      * Is this protection private?
      * 
      * @return <b>true</b> if it is private, otherwise <b>false</b>
      */
     public boolean isPrivate() {
         return this.type == ProtectionType.PRIVATE;
     }
 
     /**
      * Get the type of this protection
      * 
      * @return the type
      */
     public ProtectionType getType() {
         return type;
     }
 
     /**
      * Add a guest to the guestlist
      * 
      * @param guest
      */
     public void addGuest(String guest) {
         if (this.guestList == null) {
             this.guestList = new HashSet<String>();
         }
         this.guestList.add(guest.toLowerCase());
     }
 
     /**
      * Remove a guest from the guestlist
      * 
      * @param guest
      */
     public void removeGuest(String guest) {
         if (this.guestList != null) {
             this.guestList.remove(guest.toLowerCase());
         }
         if (this.guestList.size() < 1) {
             this.guestList = null;
         }
     }
 
     /**
      * Get the complete guestlist for this protection
      * 
      * @return the complett guestlist
      */
     public HashSet<String> getGuestList() {
         return this.guestList;
     }
 
     /**
      * Set the complete guestlist for this protection
      * 
      */
     public void setGuestList(HashSet<String> guestList) {
         this.guestList = guestList;
     }
 
     /**
      * Clear the complete guestlist
      */
     public void clearGuestList() {
         if (this.guestList != null) {
             this.guestList.clear();
             this.guestList = null;
         }
     }
 
     /**
      * Check if someone is on the guestlist
      * 
      * @param guest
      * @return <b>true</b> if the name is found, otherwise <b>false</b>
      */
     public boolean isGuest(String guest) {
         if (this.guestList != null) {
             return this.guestList.contains(guest.toLowerCase());
         }
         return false;
     }
 
     /**
      * Check if the given name is the owner of this protection
      * 
      * @param otherName
      * @return <b>true</b> if the name is the ownername, otherwise <b>false</b>
      */
     public boolean isOwner(String otherName) {
         return this.owner.equalsIgnoreCase(otherName);
     }
 
     /**
      * Get all SubProtection
      * 
      * @return a Collection<SubProtection> of SubProtection
      */
     public Collection<SubProtection> getSubProtections() {
         if (this.subProtections != null) {
             return this.subProtections.values();
         }
         return null;
     }
 
     /**
      * Check if this Protection has any SubProtections
      * 
      * @return <b>true</b> if we have at least one SubProtection, otherwise <b>false</b>
      */
     public boolean hasAnySubProtection() {
         if (this.subProtections != null) {
             return (this.subProtections.size() > 0);
         }
         return false;
     }
 
     /**
      * Add a SubProtection
      * 
      * @param subProtection
      */
     public void addSubProtection(SubProtection subProtection) {
         if (subProtection.getVector().equals(this.vector)) {
             return;
         }
 
         if (this.subProtections == null) {
             this.subProtections = new HashMap<BlockVector, SubProtection>();
         }
 
         if (!this.hasSubProtection(subProtection.getVector())) {
             this.subProtections.put(subProtection.getVector(), subProtection);
         }
     }
 
     /**
      * Remove a SubProtection
      * 
      * @param vector
      */
     public void removeSubProtection(BlockVector vector) {
         if (this.subProtections != null) {
             this.subProtections.remove(vector);
         }
 
         if (!this.hasAnySubProtection()) {
             this.subProtections = null;
         }
     }
 
     /**
      * Check if the given Vector is a SubProtection of this protection
      * 
      * @param otherName
      * @return <b>true</b> if the name is the ownername, otherwise <b>false</b>
      */
     public boolean hasSubProtection(BlockVector vector) {
         if (this.subProtections != null) {
             return this.subProtections.containsKey(vector);
         }
         return false;
     }
 
     /**
      * Check if the player can edit this protection
      * 
      * @param player
      * @return <b>true</b> if the player can, otherwise <b>false</b>
      */
     public boolean canEdit(Player player) {
         return this.isOwner(player.getName()) || UtilPermissions.playerCanUseCommand(player, "moneypit.admin");
     }
 
     /**
      * Check if the player can access this protection
      * 
      * @param player
      * @return <b>true</b> if the player can, otherwise <b>false</b>
      */
     public boolean canAccess(Player player) {
        return this.canAccess(player.getName());
     }
 
     /**
      * Check if the player can access this protection
      * 
      * @param playerName
      * @return <b>true</b> if the player can, otherwise <b>false</b>
      */
     public boolean canAccess(String playerName) {
         return this.isPublic() || this.isGift() || this.isOwner(playerName) || this.isGuest(playerName);
     }
 
     public boolean equals(Protection protection) {
         return this.ID == protection.ID && this.vector.equals(protection.getVector());
     }
 
     @Override
     public String toString() {
         return "Protection={ ID:" + this.ID + " ; Type:" + this.type.name() + " ; Owner:" + this.owner + " ; " + this.vector.toString() + " }";
     }
 }
