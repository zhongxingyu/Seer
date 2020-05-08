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
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Properties;
 
 public class HBlockListener extends BlockListener {
 
     private final Heroes plugin;
     private Map<Location, Long> placedBlocks;
 
     public HBlockListener(Heroes plugin) {
         this.plugin = plugin;
     }
 
     @SuppressWarnings("serial")
     public void init() {
         final int maxTrackedBlocks = plugin.getConfigManager().getProperties().maxTrackedBlocks;
         placedBlocks = new LinkedHashMap<Location, Long>() {
             private final int MAX_ENTRIES = maxTrackedBlocks;
 
             @Override
             protected boolean removeEldestEntry(Map.Entry<Location, Long> eldest) {
                 return size() > MAX_ENTRIES;
             }
         };
     }
 
     @Override
     public void onBlockPlace(BlockPlaceEvent event) {
         if (event.isCancelled()) {
             return;
         }
 
         Block block = event.getBlock();
         Material material = block.getType();
 
         switch (material) {
             case COBBLESTONE:
             case CLAY:
             case DIRT:
             case GOLD_ORE:
             case GRASS:
             case GRAVEL:
             case IRON_ORE:
             case NETHERRACK:
             case SAND:
             case SANDSTONE:
             case OBSIDIAN:
             case LOG:
                 Location loc = block.getLocation();
                 if (placedBlocks.containsKey(loc)) {
                     placedBlocks.remove(loc);
                 }
                 placedBlocks.put(loc, System.currentTimeMillis());
         }
     }
 
     private boolean wasBlockPlaced(Block block) {
         Location loc = block.getLocation();
         int blockTrackingDuration = plugin.getConfigManager().getProperties().blockTrackingDuration;
 
         if (placedBlocks.containsKey(loc)) {
             long timePlaced = placedBlocks.get(loc);
             if (timePlaced + blockTrackingDuration > System.currentTimeMillis()) {
                 return true;
             } else {
                 return false;
             }
         }
         return false;
     }
 
     @Override
     public void onBlockBreak(BlockBreakEvent event) {
         if (event.isCancelled()) {
             return;
         }
 
         Block block = event.getBlock();
         Player player = event.getPlayer();
 
         // Get the Hero representing the player
         Hero hero = plugin.getHeroManager().getHero(player);
         // Get the player's class definition
         HeroClass playerClass = hero.getHeroClass();
         // Get the sources of experience for the player's class
         Set<ExperienceType> expSources = playerClass.getExperienceSources();
         Properties prop = plugin.getConfigManager().getProperties();
 
         int addedExp = 0;
 
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
 
         if (addedExp != 0 && prop.getLevel(hero.getExperience()) != prop.maxLevel) {
             if (wasBlockPlaced(block)) {
                 if (hero.isVerbose()) {
                     Messaging.send(player, "No experience gained - block placed too recently.");
                 }
                 return;
             }
         }
 
         placedBlocks.remove(block.getLocation());
         
         hero.gainExp(addedExp, block.getType() == Material.LOG ? ExperienceType.LOGGING : ExperienceType.MINING);
     }
 
 }
