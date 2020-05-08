 package de.minestar.moneypit.data.subprotection;
 
 import java.util.ArrayList;
 
 import org.bukkit.entity.Player;
 
 public class SubProtectionHolder {
     private ArrayList<SubProtection> protections = null;
 
     /**
      * Get the amount of SubProtection in this SubProtectionHolder
      * 
      * @return the amount of protections
      */
     public int getSize() {
         if (this.protections != null) {
             return this.protections.size();
         }
         return 0;
     }
 
     /**
      * Get all SubProtections
      * 
      * @return all SubProtections
      */
     public ArrayList<SubProtection> getProtections() {
         return this.protections;
     }
 
     /**
      * Add a SubProtection
      * 
      * @param subProtection
      */
     public void addProtection(SubProtection subProtection) {
         if (this.protections == null) {
             this.protections = new ArrayList<SubProtection>();
         }
         if (!this.hasProtection(subProtection)) {
             this.protections.add(subProtection);
         }
     }
 
     /**
      * Remove a SubProtection
      * 
      * @param subProtection
      */
     public void removeProtection(SubProtection subProtection) {
         if (this.protections != null) {
             for (int i = 0; i < this.protections.size(); i++) {
                 if (this.protections.get(i).equals(subProtection)) {
                     this.protections.remove(i);
                     break;
                 }
             }
             if (this.protections.size() < 1) {
                 this.protections = null;
             }
         }
     }
 
     /**
      * Check if we have the SubProtection
      * 
      * @param subProtection
      * @return <b>true</b> if we have the SubProtection, otherwise <b>false</b>
      */
     public boolean hasProtection(SubProtection subProtection) {
         if (this.protections != null) {
             for (int i = 0; i < this.protections.size(); i++) {
                 if (this.protections.get(i).equals(subProtection)) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     /**
      * Check if a player can access all SubSrotections
      * 
      * @param player
      * @return <b>true</b> if the player can access all SubSrotections,
      *         otherwise <b>false</b>
      */
     public boolean canAccessAll(Player player) {
         if (this.getSize() > 0) {
             for (SubProtection subProtection : this.protections) {
                 if (!subProtection.canAccess(player)) {
                     return false;
                 }
             }
         }
         return true;
     }
 
     /**
      * Check if a player can edit all SubSrotections
      * 
      * @param player
      * @return <b>true</b> if the player can edit all SubSrotections, otherwise
      *         <b>false</b>
      */
     public boolean canEditAll(Player player) {
         if (this.getSize() > 0) {
             for (SubProtection subProtection : this.protections) {
                 if (!subProtection.canEdit(player)) {
                     return false;
                 }
             }
         }
         return true;
     }

    public SubProtection getProtection(int index) {
        if (this.getSize() > 0) {
            return this.protections.get(index);
        }
        return null;
    }
 }
