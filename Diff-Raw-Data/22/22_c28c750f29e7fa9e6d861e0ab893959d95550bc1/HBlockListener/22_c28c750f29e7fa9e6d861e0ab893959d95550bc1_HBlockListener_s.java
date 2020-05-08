 package com.herocraftonline.dev.heroes;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.BlockPlaceEvent;
 
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.classes.HeroClass.ExperienceType;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Properties;
 
 public class HBlockListener extends BlockListener {
 
     private final Heroes plugin;
     public static Map<Location, Long> placedBlocks;
    int blockTrackingDuration = 0;
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
                return size() > MAX_ENTRIES || eldest.getValue() + blockTrackingDuration > System.currentTimeMillis();
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
         // Get the player's class definition
         HeroClass playerClass = hero.getHeroClass();
         // Get the sources of experience for the player's class
         Set<ExperienceType> expSources = playerClass.getExperienceSources();
 

         double addedExp = 0;
 
         if (expSources.contains(ExperienceType.MINING)) {
             if (prop.miningExp.containsKey(block.getType())) {
                 addedExp = prop.miningExp.get(block.getType());
             }
         }
 
         if (expSources.contains(ExperienceType.LOGGING)) {
             if (prop.loggingExp.containsKey(block.getType())) {
                 addedExp = prop.loggingExp.get(block.getType());
             }
         }
 
         int postMultiplierExp = (int) (addedExp * hero.getHeroClass().getExpModifier());
         if (postMultiplierExp != 0 && !hero.isMaster()) {
             if (wasBlockPlaced(block)) {
                 if (hero.isVerbose()) {
                     Messaging.send(player, "No experience gained - block placed too recently.");
                 }
                 placedBlocks.remove(block.getLocation());
                 return;
             }
         }
         hero.gainExp(addedExp, prop.loggingExp.containsKey(block.getType()) ? ExperienceType.LOGGING : ExperienceType.MINING);
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
         if (prop.miningExp.containsKey(material) || prop.loggingExp.containsKey(material)) {
             Location loc = block.getLocation();
             if (placedBlocks.containsKey(loc)) {
                 placedBlocks.remove(loc);
             }
             placedBlocks.put(loc, System.currentTimeMillis());
         }
     }
 
     private boolean wasBlockPlaced(Block block) {
         Location loc = block.getLocation();
        
 
         if (placedBlocks.containsKey(loc)) {
             long timePlaced = placedBlocks.get(loc);
            if (timePlaced + blockTrackingDuration > System.currentTimeMillis())
                 return true;
            else {
                placedBlocks.remove(block.getLocation());
                 return false;
             }
         }
         return false;
     }
 
 }
