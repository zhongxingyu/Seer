 package tk.allele.inventory;
 
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Chest;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * Miscellaneous inventory utility methods that aren't implemented in Bukkit.
  */
 public class Inventories {
     private static final BlockFace[] chestFaces = {BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH};
 
     private Inventories() {
     }
 
     public static Chest findNeighboringChest(Chest baseChest) {
         Block base = baseChest.getBlock();
         for (BlockFace blockFace : chestFaces) {
             Block neighbor = base.getRelative(blockFace);
             if (neighbor.getState() instanceof Chest) {
                 return (Chest) neighbor.getState();
             }
         }
         return null;
     }
 
     /**
      * Return either the chest's own Inventory or a DoubleChestInventory,
      * depending on whether there are other chests next to it.
      */
     public static Inventory getInventoryForChest(Chest baseChest) {
         Chest neighboringChest = findNeighboringChest(baseChest);
         // If there is a neighboring chest, combine the two chests into one
         if (neighboringChest != null) {
             return new DoubleChestInventory(baseChest, neighboringChest);
             // Otherwise, just use the chest's inventory directly
         } else {
             return baseChest.getInventory();
         }
     }
 
     /**
      * Remove an item from the inventory, taking into account durability
      * values.
      *
      * @return The amount left over.
      */
     public static int removeItem(Inventory inventory, ItemStack delItem) {
         int toDelete = delItem.getAmount();
 
         for (int i = 0; i != inventory.getSize(); ++i) {
             ItemStack invItem = inventory.getItem(i);
 
            // Skip if the slot is empty
            if (invItem == null) {
                continue;
            }

             // Check if this is the right kind of item
             if (invItem.getTypeId() == delItem.getTypeId() &&
                     invItem.getDurability() == delItem.getDurability()) {
                 int amount = invItem.getAmount();
 
                 if (amount <= toDelete) {
                     toDelete -= amount;
                     // We've used up the whole stack, so clear it
                     inventory.clear(i);
                 } else {
                     // Split the stack and store
                     invItem.setAmount(amount - toDelete);
                     inventory.setItem(i, invItem);
                     toDelete = 0;
                 }
             }
 
             // Quit when done
             if (toDelete == 0) {
                 break;
             }
         }
         return toDelete;
     }
 }
