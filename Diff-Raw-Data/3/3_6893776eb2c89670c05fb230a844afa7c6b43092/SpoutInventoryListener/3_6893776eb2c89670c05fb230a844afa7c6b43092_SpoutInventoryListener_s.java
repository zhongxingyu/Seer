 package com.herocraftonline.dev.heroes.spout;
 
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.getspout.spoutapi.event.inventory.InventoryCraftEvent;
 import org.getspout.spoutapi.event.inventory.InventoryListener;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClass.ExperienceType;
 import com.herocraftonline.dev.heroes.hero.Hero;
 
 public class SpoutInventoryListener extends InventoryListener {
 
     private Heroes plugin;
 
     public SpoutInventoryListener(Heroes heroes) {
         plugin = heroes;
     }
     
     @Override
     public void onInventoryCraft(InventoryCraftEvent event) {
         if (event.getResult() == null)
             return;
         
         if (!event.isShiftClick() && event.getCursor() != null && event.getCursor().getType().getMaxStackSize() == event.getCursor().getAmount())
             return;
         
         ItemStack result = event.getResult();
         
         if (event.getCursor() != null && event.getCursor().getType() != result.getType())
             return;
         
         if (plugin.getConfigManager().getProperties().craftingExp.containsKey(result.getType())) {
             Player player = event.getPlayer();
             Hero hero = plugin.getHeroManager().getHero(player);
             if (hero.getHeroClass().getExperienceSources().contains(ExperienceType.CRAFTING)) {
                 hero.gainExp(plugin.getConfigManager().getProperties().craftingExp.get(result.getType()), ExperienceType.CRAFTING);
                 return;
             }
         }
     }
 }
