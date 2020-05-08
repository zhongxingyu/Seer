 package com.herocraftonline.dev.heroes;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.CustomEventListener;
 import org.bukkit.event.Event;
 import org.bukkit.inventory.ItemStack;
 
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.inventory.InventoryChangeEvent;
 import com.herocraftonline.dev.heroes.inventory.InventoryCloseEvent;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 
 public class HCustomEventListener extends CustomEventListener {
     protected Heroes plugin;
 
     public HCustomEventListener(Heroes plugin) {
         this.plugin = plugin;
     }
 
     @Override
     public void onCustomEvent(Event event) {
         /*
          * Handle Inventory Rules
          */
         if (event instanceof InventoryCloseEvent) {
             // hmm this is no longer needed.
         }
         if (event instanceof InventoryChangeEvent) {
             InventoryChangeEvent e = (InventoryChangeEvent) event;
             Player p = e.getPlayer();
 
            // ItemStack slot = e.getSlot();
             ItemStack cursor = e.getCursor();
             int slotNumber = e.getSlotNumber();
 
             if (cursor.getType() == Material.AIR) {
                 return;
             }
 
             Hero hero = plugin.getHeroManager().getHero(p);
             HeroClass clazz = hero.getPlayerClass();
 
             // The following will check the players class armor and weapon restrictions.
             if (slotNumber == 5 || slotNumber == 6 || slotNumber == 7 || slotNumber == 8) {
                 // 5 = Helm
                 // 6 = Chest
                 // 7 = Legs
                 // 8 = Shoes
                 // System.out.print("Armor Slot - Slot = " + slot.getType() + " Cursor = " + cursor.getType());
                String item = cursor.getType().toString();
                // System.out.print(item);
                if (!(clazz.getAllowedArmor().contains(item))) {
                     // System.out.print("Player cannot wear armor");
                     // TODO: Alert the player of the restriction.
                    plugin.getMessager().send(p, "You cannot equip that! - $1", item);
                     e.setCancelled(true);
                     return;
                 }
             }
 
             if (slotNumber >= 36 && slotNumber <= 44) {
                 // Slots 36->44 are the Hotbar Slots.
                 // System.out.print("HotBar Slot - Slot = " + slot.getType() + " Cursor = " + cursor.getType());
                String item = cursor.getType().toString();
                // System.out.print(item);
                if (!(clazz.getAllowedWeapons().contains(item))) {
                     // System.out.print("Player cannot equip weapon");
                     // TODO: Alert the player of the restriction.
                    plugin.getMessager().send(p, "You cannot equip that! - $1", item);
                     e.setCancelled(true);
                     return;
                 }
             }
         }
         /*
          * Handle Block Experience
          */
         // if (event instanceof BlockBreakExperienceEvent) {
         // BlockBreakExperienceEvent e = (BlockBreakExperienceEvent) event;
         // Properties prop = plugin.getConfigManager().getProperties();
         // Hero hero = plugin.getHeroManager().getHero(e.getPlayer());
         // if (hero.getParty().getExp() == true) {
         // for (Player p : hero.getParty().getMembers()) {
         // Hero nHero = plugin.getHeroManager().getHero(p);
         // if (distance(p.getLocation(), e.getPlayer().getLocation()) < 50) {
         // nHero.setExperience(nHero.getExperience() + (e.getExp() / hero.getParty().getMembers().size()));
         // }
         // }
         // e.setExp(e.getExp() / hero.getParty().getMembers().size());
         // }
         // if (prop.getLevel(hero.getExperience()) != prop.getLevel(hero.getExperience() + e.getExp())) {
         // int eLevel = prop.getLevel(hero.getExperience() + e.getExp());
         // LevelEvent lEvent = new LevelEvent(e.getPlayer(), e.getExp(), prop.getLevel(e.getExp()), prop.getLevel(e.getExp()) - 1);
         // plugin.getServer().getPluginManager().callEvent(lEvent);
         // if (!lEvent.isCancelled()) {
         // eLevel = lEvent.getToLevel();
         // e.setExp(prop.levels[eLevel - 1]);
         // }
         // e.getPlayer().sendMessage(ChatColor.RED + "You just reached level" + ChatColor.BLUE + prop.getLevel(eLevel));
         // }
         // }
         /*
          * Handle Killing Experience
          */
         // if (event instanceof KillExperienceEvent) {
         // KillExperienceEvent e = (KillExperienceEvent) event;
         // Properties prop = plugin.getConfigManager().getProperties();
         // Hero hero = plugin.getHeroManager().getHero(e.getPlayer());
         // if (hero.getParty().getExp() == true) {
         // for (Player p : hero.getParty().getMembers()) {
         // p.getNearbyEntities(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ());
         // Hero nHero = plugin.getHeroManager().getHero(p);
         // if (distance(p.getLocation(), e.getPlayer().getLocation()) < 50) {
         // nHero.setExperience(nHero.getExperience() + (e.getExp() / hero.getParty().getMembers().size()));
         // }
         // }
         // e.setExp(e.getExp() / hero.getParty().getMembers().size());
         // }
         // if (prop.getLevel(hero.getExperience()) != prop.getLevel(hero.getExperience() + e.getExp())) {
         // int eLevel = prop.getLevel(hero.getExperience() + e.getExp());
         // LevelEvent lEvent = new LevelEvent(e.getPlayer(), e.getExp(), prop.getLevel(e.getExp()), prop.getLevel(e.getExp()) - 1);
         // plugin.getServer().getPluginManager().callEvent(lEvent);
         // if (!lEvent.isCancelled()) {
         // eLevel = lEvent.getToLevel();
         // e.setExp(prop.levels[eLevel - 1]);
         // }
         // e.getPlayer().sendMessage(ChatColor.RED + "You just reached level" + ChatColor.BLUE + prop.getLevel(eLevel));
         // }
         // }
     }
 
     public double distance(Location p, Location q) {
         double dx = p.getX() - q.getX();
         double dy = p.getY() - q.getY();
         double dist = Math.sqrt(dx * dx + dy * dy);
         return dist;
     }
 }
