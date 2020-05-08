 package ca.mickealn.expbottles;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.ExpBottleEvent;
 import org.bukkit.event.inventory.InventoryOpenEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.inventory.ItemStack;
 
 public final class ExpBottlesListener implements Listener {
     private final int expPerBottle = 17;
 
     @EventHandler
     public void onOpenEnhantTable(InventoryOpenEvent event) {
         if (event.getPlayer() instanceof Player && InventoryType.ENCHANTING.equals(event.getInventory().getType()) && Material.GLASS_BOTTLE.equals(event.getPlayer().getItemInHand().getType())) {
             Player player = (Player) event.getPlayer();
             int originalExp = player.getTotalExperience();
            int numEnchantedBottles = Math.min(originalExp / expPerBottle, player.getItemInHand().getAmount());
            int stackSize = player.getItemInHand().getAmount();
 
             if (numEnchantedBottles > 0) {
                 player.setLevel(0); // Set everything to 0 to prevent doubling of EXP
                 player.setTotalExperience(0);
                 player.setItemInHand(new ItemStack(Material.EXP_BOTTLE, numEnchantedBottles));
                 player.giveExp(Math.max(0, originalExp - numEnchantedBottles * expPerBottle));
                if (numEnchantedBottles > 0) {
                    player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.GLASS_BOTTLE, stackSize - numEnchantedBottles));
                 }
             }
 
             event.setCancelled(true);
         }
     }
 
     @EventHandler
     public void onExpEvent(ExpBottleEvent event) {
         event.setExperience(expPerBottle);
     }
 }
