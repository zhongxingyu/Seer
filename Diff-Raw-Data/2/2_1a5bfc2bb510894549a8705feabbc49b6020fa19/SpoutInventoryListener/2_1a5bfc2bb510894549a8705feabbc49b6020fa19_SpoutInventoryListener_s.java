 package com.herocraftonline.dev.heroes.spout;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.inventory.ItemStack;
 import org.getspout.spoutapi.event.inventory.InventoryCraftEvent;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClass.ExperienceType;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class SpoutInventoryListener implements Listener {
 
     private Heroes plugin;
 
     public SpoutInventoryListener(Heroes heroes) {
         plugin = heroes;
         Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onInventoryCraft(InventoryCraftEvent event) {
        if (event.getResult() == null || event.isCancelled()) {
             return;
         }
 
         if (event.isShiftClick() && event.getPlayer().getInventory().firstEmpty() == -1) {
             return;
         }
 
         ItemStack cursor = event.getCursor();
         ItemStack result = event.getResult();
         int amountCrafted = result.getAmount();
 
         if (!event.isShiftClick() && cursor != null) {
             if (cursor.getType() != result.getType() || cursor.getType().getMaxStackSize() <= cursor.getAmount() + amountCrafted) {
                 return;
             }
         }
 
         Player player = event.getPlayer();
         Hero hero = plugin.getHeroManager().getHero(player);
         if (!hero.canCraft(result)) {
             Messaging.send(hero.getPlayer(), "You don't know how to craft $1", result.getType().name().toLowerCase().replace("_", " "));
             event.setCancelled(true);
             return;
         }
 
         if (Heroes.properties.craftingExp.containsKey(result.getType())) {
             if (hero.canGain(ExperienceType.CRAFTING)) {
                 hero.gainExp(Heroes.properties.craftingExp.get(result.getType()) * amountCrafted, ExperienceType.CRAFTING);
                 return;
             }
         }
     }
 }
