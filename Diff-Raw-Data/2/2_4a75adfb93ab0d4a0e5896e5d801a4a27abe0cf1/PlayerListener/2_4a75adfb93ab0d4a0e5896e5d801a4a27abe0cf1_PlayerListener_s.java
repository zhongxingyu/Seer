 package fr.frozentux.craftguard2.listener;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.event.inventory.InventoryType.SlotType;
 import org.bukkit.inventory.ItemStack;
 
 import fr.frozentux.craftguard2.CraftGuardPlugin;
 
 /**
  * Listener for all players-related actions including login and inventory click
  * @author FrozenTux
  *
  */
 public class PlayerListener implements Listener {
 	
 	private CraftGuardPlugin plugin;
 	
 	public PlayerListener(CraftGuardPlugin plugin){
 		this.plugin = plugin;
 	}
 	
 	@EventHandler
 	public void onInventoryClick(InventoryClickEvent e){
 		SlotType slotType = e.getSlotType();
 		InventoryType invType = e.getInventory().getType();
 		int slot = e.getSlot();
 		Player player = (Player)e.getWhoClicked();
 		
 		if(slotType == SlotType.RESULT && (invType.equals(InventoryType.CRAFTING) || invType.equals(InventoryType.CRAFTING)) && slot == 0 && e.getInventory().getItem(0) != null){
 			ItemStack object = e.getInventory().getItem(slot);
 			int id = object.getTypeId();
 			byte metadata = object.getData().getData();
 			
 			e.setCancelled(!CraftPermissionChecker.checkCraft(player, id, metadata, plugin));
 			
 		}
 		
		if(!plugin.getConfiguration().getBooleanKey("checkFurnaces"))return;
 		
 		if(invType.equals(InventoryType.FURNACE) && (slotType == SlotType.CONTAINER || slotType == SlotType.FUEL || slotType == SlotType.QUICKBAR) && (e.isShiftClick() || e.getSlot() == 0 || e.getSlot() == 1)){
 			ItemStack object;
 			if(e.isShiftClick())object = e.getCurrentItem();
 			else{
 				if(e.getSlot() == 0 && e.getCursor() != null)object = e.getCursor();
 				else if(e.getSlot() == 1 && e.getInventory().getItem(0) != null)object = e.getInventory().getItem(0);
 				else return;
 			}
 			
 			int id = object.getTypeId();
 			byte metadata = object.getData().getData();
 			CraftPermissionChecker.checkFurnace(player, id, metadata, plugin);
 			
 		}
 		
 		
 	}
 	
 }
