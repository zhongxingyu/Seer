 package net.marcuswhybrow.minecraft.law;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import net.marcuswhybrow.minecraft.law.utilities.Inventory;
 
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public final class InventoryManager {
 	public static enum Action {CONFISCATE, RESTORE};
 	
 	private static Map<String, ItemStack[]> inventories = new HashMap<String, ItemStack[]>();
 	
 	private InventoryManager() {
 		
 	}
 	
 	public static void confiscate(Player player) {
		inventories.put(player.getName(), Inventory.getAsArray(player));
 		Inventory.saveToFile(player);
 		Inventory.clear(player);
 	}
 	
 	public static boolean restore(Player player) {
 		ItemStack[] storedInventory = inventories.get(player.getName().toLowerCase());
 		
 		if (storedInventory == null) {
 			// If the inventory is not in memory, load from file
 			boolean isRestored = Inventory.restoreFromFile(player);
 			Inventory.deleteFile(player);
 			return isRestored;
 		}
 		
 		// Restore from memory and delete the saved file
 		Inventory.setFromArray(player, storedInventory);
 		Inventory.deleteFile(player);
 		return true;
 	}
 	
 	public static void preformAction(Player player, Action action) {
 		switch (action) {
 		case CONFISCATE:
 			InventoryManager.confiscate(player);
 			break;
 		case RESTORE:
 			InventoryManager.restore(player);
 			break;
 		}
 	}
 }
