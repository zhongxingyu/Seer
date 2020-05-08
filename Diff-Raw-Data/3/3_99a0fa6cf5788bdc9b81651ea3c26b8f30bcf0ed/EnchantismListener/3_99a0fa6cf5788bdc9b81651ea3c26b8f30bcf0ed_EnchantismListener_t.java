 package tk.thundaklap.enchantism;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.inventory.InventoryDragEvent;
 import org.bukkit.event.inventory.InventoryOpenEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.EnchantingInventory;
 import org.bukkit.inventory.ItemStack;
 
 public class EnchantismListener implements Listener {
 
 
     @EventHandler(priority = EventPriority.HIGH)
     public void onPlayerInteract(PlayerInteractEvent event) {
        
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.ENCHANTMENT_TABLE) {
             event.setCancelled(true);
 
             Player thePlayer = event.getPlayer();
             
             Enchantism.openInventories.put(thePlayer, new EnchantInventory(thePlayer, thePlayer.getTargetBlock(null, 500).getLocation(), Enchantism.getInstance().configuration.requireBookshelves));
 
         }
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onInventoryClose(InventoryCloseEvent event) {
         
         if(Enchantism.openInventories.get((Player)event.getPlayer()) != null){
             Enchantism.openInventories.remove((Player)event.getPlayer());
         }
     }
 
     @EventHandler(priority = EventPriority.HIGH)
     public void onInventoryClick(InventoryClickEvent event) {
         
         EnchantInventory inventory;
         
         if((inventory = Enchantism.openInventories.get((Player)event.getWhoClicked())) != null){
             inventory.inventoryClicked(event);
         }
     }
 
     @EventHandler(priority = EventPriority.HIGH)
     public void onInventoryDrag(InventoryDragEvent event) {
         EnchantInventory inventory;
         
         if((inventory = Enchantism.openInventories.get((Player)event.getWhoClicked())) != null){
             inventory.inventoryDragged(event);
         }
     }
 }
