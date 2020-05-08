 package com.herocraftonline.dev.heroes;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.BlockPlaceEvent;
 
 import com.herocraftonline.dev.heroes.classes.HeroClass.ExperienceType;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Properties;
 
 public class HBlockListener extends BlockListener {
 
     private final Heroes plugin;
     private int blockTrackingDuration = 0;
     public static Map<Location, Long> placedBlocks;
 
     public HBlockListener(Heroes plugin) {
         this.plugin = plugin;
     }
 
     public void init() {
         final int maxTrackedBlocks = plugin.getConfigManager().getProperties().maxTrackedBlocks;
         blockTrackingDuration = plugin.getConfigManager().getProperties().blockTrackingDuration;
         placedBlocks = new LinkedHashMap<Location, Long>() {
             private static final long serialVersionUID = 2623620773233514414L;
             private final int MAX_ENTRIES = maxTrackedBlocks;
 
             @Override
             protected boolean removeEldestEntry(Map.Entry<Location, Long> eldest) {
                 return size() > MAX_ENTRIES || eldest.getValue() + blockTrackingDuration <= System.currentTimeMillis();
             }
         };
     }
 
     @Override
     public void onBlockBreak(BlockBreakEvent event) {
         if (event.isCancelled())
             return;
         Properties prop = plugin.getConfigManager().getProperties();
         Player player = event.getPlayer();
 
         if (prop.disabledWorlds.contains(player.getWorld().getName()))
             return;
 
         Block block = event.getBlock();
 
         // Get the Hero representing the player
         Hero hero = plugin.getHeroManager().getHero(player);
 
         double addedExp = 0;
 
         ExperienceType et = null;
        if (hero.hasExperienceType(ExperienceType.MINING)) {
            if (prop.miningExp.containsKey(block.getType())) {
                 addedExp = prop.miningExp.get(block.getType());
                 et = ExperienceType.MINING;
            }
        } else if(hero.hasExperienceType(ExperienceType.FARMING)) {
            if (prop.farmingExp.containsKey(block.getType())) {
                addedExp = prop.farmingExp.get(block.getType());
                et = ExperienceType.FARMING;
            }
        } else if (hero.hasExperienceType(ExperienceType.LOGGING)) {
            if (prop.loggingExp.containsKey(block.getType())) {
                 addedExp = prop.loggingExp.get(block.getType());
                 et = ExperienceType.LOGGING;
            }
         }
         if (addedExp == 0) {
             return;
         } else if (wasBlockPlaced(block)) {
             if (hero.isVerbose()) {
                 Messaging.send(player, "No experience gained - block placed too recently.");
             }
             placedBlocks.remove(block.getLocation());
             return;
         } else {
             hero.gainExp(addedExp, et);
         }
     }
 
     @Override
     public void onBlockPlace(BlockPlaceEvent event) {
         if (event.isCancelled())
             return;
 
         Block block = event.getBlock();
         Material material = block.getType();
 
         Properties prop = plugin.getConfigManager().getProperties();
         if (prop.disabledWorlds.contains(block.getWorld().getName()))
             return;
         if (prop.miningExp.containsKey(material) || prop.loggingExp.containsKey(material) || prop.farmingExp.containsKey(material)) {
             Location loc = block.getLocation();
             placedBlocks.put(loc, System.currentTimeMillis());
         }
     }
 
     private boolean wasBlockPlaced(Block block) {
         Location loc = block.getLocation();
 
         if (placedBlocks.containsKey(loc)) {
             long timePlaced = placedBlocks.get(loc);
             if (timePlaced + blockTrackingDuration > System.currentTimeMillis()) {
                 return true;
             } else {
                 placedBlocks.remove(loc);
                 return false;
             }
         }
         return false;
     }

 }
