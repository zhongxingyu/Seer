 package com.minecarts.oldmcdonald.thread;
 
 import com.minecarts.oldmcdonald.OldMcDonald;
 import static org.bukkit.Bukkit.*;
 
 import com.minecarts.oldmcdonald.helper.EntityHelper;
 import com.minecarts.oldmcdonald.helper.SpawnReport;
 import com.minecarts.oldmcdonald.helper.SpawnReport.FailureReason;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.*;
 
 import java.text.MessageFormat;
 import java.util.Random;
 
 public class BasicSpawner implements Runnable{
     private OldMcDonald plugin;
     private Random rand = new Random();
     private SpawnReport report = null;
 
     public BasicSpawner(OldMcDonald plugin){
         this.plugin = plugin;
         this.createReport();
     }
 
     public void createReport(){
         this.report = new SpawnReport();
     }
 
     public void displayReport(){
         //Display a report to any monitoring players
         if(!plugin.getConfig().getBoolean("report_enabled")){
             plugin.log("Spawn reporting is not enabled, please enable to show the report.");
             return;
         }
         for(Player p : getOnlinePlayers()){
             if(!p.hasPermission("oldmcdonald.stats")) continue;
             p.sendMessage(ChatColor.DARK_GREEN + " ---- Animal Spawning Report ----");
              p.sendMessage(MessageFormat.format(ChatColor.GRAY + "Failure: frq: {0} reduc: {1} lght: {2} MAX[w: {3} near: {4}] bio: {5} blk: {6}",
                     report.getFailiureCountColor(FailureReason.CHANCE_FREQUENCY),
                     report.getFailiureCountColor(FailureReason.CHANCE_BIOME),
                     report.getFailiureCountColor(FailureReason.LEVEL_LIGHT),
                     report.getFailiureCountColor(FailureReason.MAX_WORLD),
                     report.getFailiureCountColor(FailureReason.MAX_NEARBY),
                     report.getFailiureCountColor(FailureReason.TYPE_BIOME),
                     report.getFailiureCountColor(FailureReason.TYPE_BLOCK)
             ));
             p.sendMessage(MessageFormat.format(ChatColor.GRAY + "Success: CH: {0} CO: {1} MU: {2} PI: {3} SH: {4} SQ: {5} WO: {6}",
                     report.getSuccessCountColor(CreatureType.CHICKEN),
                     report.getSuccessCountColor(CreatureType.COW),
                     report.getSuccessCountColor(CreatureType.MUSHROOM_COW),
                     report.getSuccessCountColor(CreatureType.PIG),
                     report.getSuccessCountColor(CreatureType.SHEEP),
                     report.getSuccessCountColor(CreatureType.SQUID),
                     report.getSuccessCountColor(CreatureType.WOLF)
             ));
             p.sendMessage(MessageFormat.format(ChatColor.GRAY + "Active: CH: {0} CO: {1} MU: {2} PI: {3} SH: {4} SQ: {5} WO: {6}",
                     EntityHelper.getTotalEntities(CreatureType.CHICKEN.getEntityClass(),p.getWorld()),
                     EntityHelper.getTotalEntities(CreatureType.COW.getEntityClass(),p.getWorld()),
                     EntityHelper.getTotalEntities(CreatureType.MUSHROOM_COW.getEntityClass(),p.getWorld()),
                     EntityHelper.getTotalEntities(CreatureType.PIG.getEntityClass(),p.getWorld()),
                     EntityHelper.getTotalEntities(CreatureType.SHEEP.getEntityClass(),p.getWorld()),
                     EntityHelper.getTotalEntities(CreatureType.SQUID.getEntityClass(),p.getWorld()),
                     EntityHelper.getTotalEntities(CreatureType.WOLF.getEntityClass(),p.getWorld())
             ));
             //p.sendMessage(" ---- ---------------------- ----");
         }
         this.createReport(); //Create a new report
     }
 
 
     public void run() {
         boolean reportEnabled = plugin.getConfig().getBoolean("report_enabled");
         int playerRadiusMin = plugin.getConfig().getInt("player.minimum");
         int playerRadiusMax = plugin.getConfig().getInt("player.maximum");
         String[] animalKeys = plugin.getConfig().getConfigurationSection("animals").getKeys(false).toArray(new String[0]);
 
         //So lets loop through the players online
         for(Player player : getOnlinePlayers()){
             //Pick a random creature to try to spawn
             CreatureType randomType = CreatureType.valueOf(animalKeys[rand.nextInt(animalKeys.length)]);
             //Make sure there aren't more than this many creatures in the world (guesstimate from last itteration of entities so
             // we're not looping through them each time)
             int totalEntities = EntityHelper.getTotalEntities(randomType.getEntityClass(),player.getWorld());
             if(totalEntities > plugin.getConfig().getInt("animals." + randomType +".maximum")){
                 report.incrementFailure(FailureReason.MAX_WORLD);
                 continue;
             }
             
             //See if we need to randomly skip this attempt due to frequency limitations to artificially make
             //  creatures more rare
             if((1 - plugin.getConfig().getDouble("animals."+randomType+".frequency")) > rand.nextDouble()){
                 if(reportEnabled) report.incrementFailure(FailureReason.CHANCE_FREQUENCY);
                 continue;
             }
 
             //Find a random location somewhere around the player
             double randomAngle = rand.nextDouble() * 2 * 3.14;
             double randomX = Math.cos(randomAngle);
             double randomZ = Math.sin(randomAngle);
 
             World world = player.getWorld();
             
             Location loc = (new Location(world,randomX,0,randomZ)).multiply(playerRadiusMin + rand.nextInt(playerRadiusMax));
             loc = loc.add(player.getLocation());
             Block spawnBlock = world.getHighestBlockAt(loc);
             Block checkBlock = spawnBlock.getRelative(BlockFace.DOWN);
 
             //Check to see if this block is in a valid biome for this creature type
            if(checkBlock.getBiome() != null && !plugin.getConfig().getStringList("animals."+randomType+".biomes").contains(checkBlock.getBiome().name())){
                 if(reportEnabled) report.incrementFailure(FailureReason.TYPE_BIOME);
                 continue;
             }
 
             //See if we need to reduce the spawn chance in this biome
             if((1 - plugin.getConfig().getDouble("animals."+randomType+".biome_reduction."+spawnBlock.getBiome().name(),1)) > rand.nextDouble()){
                 if(reportEnabled) report.incrementFailure(FailureReason.CHANCE_BIOME);
                 continue;
             }
 
             //Check the lighting level of this block
             if(spawnBlock.getLightLevel() < plugin.getConfig().getInt("animals."+randomType+".light")){
                 if(reportEnabled) report.incrementFailure(FailureReason.LEVEL_LIGHT);
                 continue;
             }
 
             //Check to see if it can spawn on this block type
             if(!plugin.getConfig().getStringList("animals."+randomType+".blocks").contains(checkBlock.getType().name())){
                 if(reportEnabled) report.incrementFailure(FailureReason.TYPE_BLOCK);
                 continue;
             }
 
             //Check to see if there are any nearby entities here
             int nearby_range = plugin.getConfig().getInt("animals."+randomType+".nearby_range");
             int nearby_count = plugin.getConfig().getInt("animals."+randomType+".nearby_count");
             int actualNearby = EntityHelper.countNearbyEntities(randomType.getEntityClass(),spawnBlock.getLocation(),nearby_range);
             if(actualNearby >= nearby_count){
                 if(reportEnabled) report.incrementFailure(FailureReason.MAX_NEARBY);
                 continue;
             }
 
             //Hey, things are looking good, lets spawn some entities!
             int clusterMin = plugin.getConfig().getInt("animals."+randomType+".cluster.min");
             int clusterMax = plugin.getConfig().getInt("animals."+randomType+".cluster.max");
             int randomClusterSize = clusterMin+rand.nextInt(clusterMax - clusterMin + 1); //nextInt(n) is not inclusive??
             for(int r=0; r<randomClusterSize; r++){ //between 1 and 4 pigs
                 world.spawnCreature(spawnBlock.getLocation(), randomType);
             }
             if(reportEnabled) report.incrementSuccess(randomType,randomClusterSize);
         }
     } //run()
 }
