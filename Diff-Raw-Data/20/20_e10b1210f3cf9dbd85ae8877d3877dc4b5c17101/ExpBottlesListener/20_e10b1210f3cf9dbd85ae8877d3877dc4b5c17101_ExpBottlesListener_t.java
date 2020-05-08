 package ca.mickealn.expbottles.events;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.ExpBottleEvent;
 import org.bukkit.event.inventory.InventoryOpenEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.inventory.ItemStack;
 
 import ca.mickealn.expbottles.util.ExpBottlesConfigurationContext;
 
 public final class ExpBottlesListener implements Listener {
 
     private final int expPerBottle;
 
     public ExpBottlesListener(ExpBottlesConfigurationContext configurationContext) {
         expPerBottle = configurationContext.expPerBottle;
     }
 
     @EventHandler
     public void onOpenEnhantTable(InventoryOpenEvent event) {
         if (event.getPlayer() instanceof Player && InventoryType.ENCHANTING.equals(event.getInventory().getType()) && Material.GLASS_BOTTLE.equals(event.getPlayer().getItemInHand().getType())) {
             Player player = (Player) event.getPlayer();
             int originalExp = player.getTotalExperience();
             int bottleCount = player.getItemInHand().getAmount();
             int numEnchantedBottles = Math.min(originalExp / expPerBottle, bottleCount);
 
             if (numEnchantedBottles > 0) {
                 player.setLevel(0); // Set everything to 0 to prevent doubling of EXP
                 player.setTotalExperience(0);
 
                int newTotalExperience = originalExp - numEnchantedBottles * expPerBottle;
                double newLevel, experienceToCurrentLevel;

                if (newTotalExperience >= 887) {
                    newLevel = (303 + Math.sqrt(56 * newTotalExperience - 32511)) / 14;
                    experienceToCurrentLevel = 3.5 * newLevel * newLevel - 151.5 * newLevel + 2220;
                } else if (newTotalExperience >= 292) {
                    newLevel = (59 + Math.sqrt(24 * newTotalExperience - 5159)) / 6;
                    experienceToCurrentLevel = 1.5 * newLevel * newLevel - 29.5 * newLevel + 360;
                } else {
                    newLevel = newTotalExperience / 17;
                    experienceToCurrentLevel = newLevel * 17;
                }

                player.setTotalExperience(newTotalExperience);
                player.setLevel((int) newLevel);
                player.setExp((float) ((newTotalExperience - experienceToCurrentLevel) / player.getExpToLevel()));

                 player.setItemInHand(new ItemStack(Material.EXP_BOTTLE, numEnchantedBottles));
                 if (bottleCount > numEnchantedBottles) {
                     player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.GLASS_BOTTLE, bottleCount - numEnchantedBottles));
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
