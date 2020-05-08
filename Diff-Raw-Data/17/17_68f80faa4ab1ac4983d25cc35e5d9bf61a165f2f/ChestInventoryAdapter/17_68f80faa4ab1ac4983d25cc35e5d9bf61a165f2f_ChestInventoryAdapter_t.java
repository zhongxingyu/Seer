 package tk.kirlian.DuckShop.trading;
 
 import org.bukkit.Location;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Chest;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.block.Action;
 import org.bukkit.block.BlockFace;
 
 import tk.kirlian.DuckShop.DuckShop;
 import tk.kirlian.DuckShop.items.Item;
 import tk.kirlian.DuckShop.errors.*;
 
 /**
  * Extends the InventoryAdapter to support chests.
  * @see InventoryAdapter
  */
 public class ChestInventoryAdapter extends InventoryAdapter {
     private void initialize(String ownerName, Chest chest)
       throws ChestProtectionException {
         setPlayerName(ownerName);
         setInventory(chest.getInventory());
 
        if(getPlayer() != null) {
            // Test if the owner of the sign can access the chest
            PlayerInteractEvent event = new PlayerInteractEvent(getPlayer(), Action.RIGHT_CLICK_BLOCK, null, chest.getBlock(), BlockFace.SELF);
            plugin.getServer().getPluginManager().callEvent(event);
 
            // A chest protection plugin would usually cancel a right-click
            // event on a protected chest
            if(event.isCancelled()) {
                throw new ChestProtectionException();
            }
         }
     }
 
     /**
      * Create an instance of this adapter.
      *
      * @throws InvalidChestException if the location doesn't point to a chest
      * @throws ChestProtectionException if the chest is protected
      */
     public ChestInventoryAdapter(DuckShop plugin, String ownerName, Chest chest)
       throws ChestProtectionException {
         super(plugin);
         initialize(ownerName, chest);
     }
 
     /**
      * Create an instance of this adapter.
      *
      * @throws InvalidChestException if the location doesn't point to a chest
      * @throws ChestProtectionException if the chest is protected
      */
     public ChestInventoryAdapter(DuckShop plugin, String ownerName, Location chestLocation)
       throws InvalidChestException, ChestProtectionException {
         super(plugin);
         BlockState state = chestLocation.getBlock().getState();
         if(state instanceof Chest) {
             initialize(ownerName, (Chest)state);
         } else {
             throw new InvalidChestException();
         }
     }
 }
