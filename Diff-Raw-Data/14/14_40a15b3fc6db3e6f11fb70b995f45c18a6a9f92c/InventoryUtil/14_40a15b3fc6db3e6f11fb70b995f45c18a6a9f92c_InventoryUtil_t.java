 package de.philworld.bukkit.magicsigns.util;
 
 import org.bukkit.Material;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 public class InventoryUtil {
 
 	/**
 	 * Removes a specific amount of items from an inventory.
 	 * 
 	 * <p>
 	 * Thanks to bergerkiller:
 	 * {@code http://forums.bukkit.org/threads/remove-items-from-an-inventory.27853/}
 	 */
 	public static void removeItems(Inventory inv, Material type, int amount) {
		for (int i = 0; i < inv.getContents().length; i++) {
			ItemStack is = inv.getItem(i);
 			if (is != null && is.getType() == type) {
 				int newamount = is.getAmount() - amount;
 				if (newamount > 0) {
 					is.setAmount(newamount);
 					break;
 				}
				inv.clear(i);
 				amount = -newamount;
 				if (amount == 0)
 					break;
 			}
 		}
 	}
 
 }
