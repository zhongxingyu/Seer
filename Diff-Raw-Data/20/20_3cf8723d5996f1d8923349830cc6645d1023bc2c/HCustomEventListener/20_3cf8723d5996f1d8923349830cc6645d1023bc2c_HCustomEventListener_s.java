 package com.herocraftonline.dev.heroes;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.CustomEventListener;
 import org.bukkit.event.Event;
 
 import com.herocraftonline.dev.heroes.api.BlockBreakExperienceEvent;
 import com.herocraftonline.dev.heroes.api.InventoryCloseEvent;
 import com.herocraftonline.dev.heroes.api.KillExperienceEvent;
 import com.herocraftonline.dev.heroes.api.LevelEvent;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.util.Properties;
 
 public class HCustomEventListener extends CustomEventListener {
     protected Heroes plugin;
 
     public HCustomEventListener(Heroes plugin) {
         this.plugin = plugin;
     }
 
     @Override
     public void onCustomEvent(Event event) {
        System.out.print(event.getEventName() + " - just fired"); // Nothing appears.
         /*
          * Handle Inventory Rules
          */
         if (event instanceof InventoryCloseEvent) {
             Player player = ((InventoryCloseEvent) event).getPlayer();
            System.out.print("This doesn't"); // No output at all.
            System.out.print(player.getName() + " closed their inventory");
             // Check the Armor slots for armor they are not allowed to wear, unequip it and place in inventory
             // if inventory is full drop the item to the ground for them to sort out.
         }
        if (event.getEventName().equalsIgnoreCase("InventoryCloseEvent")) {
            System.out.print("Sigh"); // Same here.
        }
         /*
          * Handle Block Experience
          */
         if (event instanceof BlockBreakExperienceEvent) {
             BlockBreakExperienceEvent e = (BlockBreakExperienceEvent) event;
             Properties prop = plugin.getConfigManager().getProperties();
             Hero hero = plugin.getHeroManager().getHero(e.getPlayer());
             if (hero.getParty().getExp() == true) {
                 for (Player p : hero.getParty().getMembers()) {
                     Hero nHero = plugin.getHeroManager().getHero(p);
                     if (distance(p.getLocation(), e.getPlayer().getLocation()) < 50) {
                         nHero.setExperience(nHero.getExperience() + (e.getExp() / hero.getParty().getMembers().size()));
                     }
                 }
                 e.setExp(e.getExp() / hero.getParty().getMembers().size());
             }
             if (prop.getLevel(hero.getExperience()) != prop.getLevel(hero.getExperience() + e.getExp())) {
                 int eLevel = prop.getLevel(hero.getExperience() + e.getExp());
                 LevelEvent lEvent = new LevelEvent(e.getPlayer(), e.getExp(), prop.getLevel(e.getExp()), prop.getLevel(e.getExp()) - 1);
                 plugin.getServer().getPluginManager().callEvent(lEvent);
                 if (!lEvent.isCancelled()) {
                     eLevel = lEvent.getToLevel();
                     e.setExp(prop.levels[eLevel - 1]);
                 }
                 e.getPlayer().sendMessage(ChatColor.RED + "You just reached level" + ChatColor.BLUE + prop.getLevel(eLevel));
             }
         }
         /*
          * Handle Killing Experience
          */
         if (event instanceof KillExperienceEvent) {
             KillExperienceEvent e = (KillExperienceEvent) event;
             Properties prop = plugin.getConfigManager().getProperties();
             Hero hero = plugin.getHeroManager().getHero(e.getPlayer());
             if (hero.getParty().getExp() == true) {
                 for (Player p : hero.getParty().getMembers()) {
                     p.getNearbyEntities(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ());
                     Hero nHero = plugin.getHeroManager().getHero(p);
                     if (distance(p.getLocation(), e.getPlayer().getLocation()) < 50) {
                         nHero.setExperience(nHero.getExperience() + (e.getExp() / hero.getParty().getMembers().size()));
                     }
                 }
                 e.setExp(e.getExp() / hero.getParty().getMembers().size());
             }
             if (prop.getLevel(hero.getExperience()) != prop.getLevel(hero.getExperience() + e.getExp())) {
                 int eLevel = prop.getLevel(hero.getExperience() + e.getExp());
                 LevelEvent lEvent = new LevelEvent(e.getPlayer(), e.getExp(), prop.getLevel(e.getExp()), prop.getLevel(e.getExp()) - 1);
                 plugin.getServer().getPluginManager().callEvent(lEvent);
                 if (!lEvent.isCancelled()) {
                     eLevel = lEvent.getToLevel();
                     e.setExp(prop.levels[eLevel - 1]);
                 }
                 e.getPlayer().sendMessage(ChatColor.RED + "You just reached level" + ChatColor.BLUE + prop.getLevel(eLevel));
             }
         }
     }
 
     public double distance(Location p, Location q) {
         double dx = p.getX() - q.getX();
         double dy = p.getY() - q.getY();
         double dist = Math.sqrt(dx * dx + dy * dy);
         return dist;
     }
 }
