 package vc.pvp.skywars.controllers;
 
 import com.google.common.collect.Lists;
 import com.sk89q.worldedit.CuboidClipboard;
 import com.sk89q.worldedit.Vector;
 import org.bukkit.*;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.generator.ChunkGenerator;
 import vc.pvp.skywars.config.PluginConfig;
 import vc.pvp.skywars.game.Game;
 import vc.pvp.skywars.utilities.LogUtils;
 import vc.pvp.skywars.utilities.WEUtils;
 
 import java.util.Map;
 import java.util.Queue;
 import java.util.Random;
 import java.util.logging.Level;
 
 public class WorldController {
 
     private static final int PASTE_HEIGHT = 75;
     private static WorldController worldController;
     private World islandWorld;
     private Queue<int[]> freeIslands = Lists.newLinkedList();
     private int nextId;
 
     public WorldController() {
         generateIslandCoordinates();
         islandWorld = createWorld();
     }
 
     private void generateIslandCoordinates() {
         for (int xxx = 0; xxx < PluginConfig.getIslandsPerWorld(); xxx++) {
             for (int zzz = 0; zzz < PluginConfig.getIslandsPerWorld(); zzz++) {
                 int[] coordinates = new int[] { xxx, zzz };
 
                 if (!freeIslands.contains(coordinates)) {
                     freeIslands.add(coordinates);
                 }
             }
         }
     }
 
     public void unload(Game game) {
         int[] islandCoordinates = game.getIslandCoordinates();
         int islandX = islandCoordinates[0];
         int islandZ = islandCoordinates[1];
         int islandSize = PluginConfig.getIslandSize();
 
         int minX = (islandX * islandSize) >> 4;
         int minZ = (islandZ * islandSize) >> 4;
         int maxX = (islandX * islandSize + islandSize) >> 4;
         int maxZ = (islandZ * islandSize + islandSize) >> 4;
 
         for (int xxx = minX; xxx < maxX; xxx++) {
             for (int zzz = minZ; zzz < maxZ; zzz++) {
                 Chunk chunk = game.getWorld().getChunkAt(xxx, zzz);
 
                 if (chunk.isLoaded()) {
                     for (Entity entity : chunk.getEntities()) {
                         if (!(entity instanceof Player)) {
                             entity.remove();
                         }
                     }
                     chunk.unload(false);
                 }
             }
         }
     }
 
     public World create(Game game, CuboidClipboard schematic) {
         if (freeIslands.size() == 0) {
             LogUtils.log(Level.INFO, getClass(), "No more free islands left. Generating new world.");
 
             generateIslandCoordinates();
             islandWorld = createWorld();
         }
 
         int[] islandCoordinates = freeIslands.poll();
         game.setIslandCoordinates(islandCoordinates);
 
         int islandX = islandCoordinates[0];
         int islandZ = islandCoordinates[1];
         int islandSize = PluginConfig.getIslandSize();
 
         int midX = islandX * islandSize + islandSize / 2;
         int midZ = islandZ * islandSize + islandSize / 2;
 
         if (PluginConfig.buildSchematic()) {
             WEUtils.buildSchematic(game, new Location(islandWorld, midX, PASTE_HEIGHT, midZ), schematic);
         } else {
             WEUtils.pasteSchematic(new Location(islandWorld, midX, PASTE_HEIGHT, midZ), schematic);
         }
 
         Map<Integer, Vector> spawns = SchematicController.get().getCachedSpawns(schematic);
         Vector isleLocation = new Vector(midX, PASTE_HEIGHT, midZ);
 
         for (Map.Entry<Integer, Vector> entry : spawns.entrySet()) {
             Vector spawn = entry.getValue().add(isleLocation).add(schematic.getOffset());
             Location location = new Location(islandWorld, spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ());
 
             game.addSpawn(entry.getKey(), location);
            createSpawnHousing(location);
         }
 
         return islandWorld;
     }
 
     private void createSpawnHousing(Location location) {
         World world = location.getWorld();
 
         int x = location.getBlockX();
         int y = location.getBlockY();
         int z = location.getBlockZ();
 
         world.getBlockAt(x, y - 1, z).setTypeId(20, false);
         world.getBlockAt(x, y + 3, z).setTypeId(20, false);
 
         world.getBlockAt(x + 1, y, z).setTypeId(20, false);
         world.getBlockAt(x + 1, y + 1, z).setTypeId(20, false);
         world.getBlockAt(x + 1, y + 2, z).setTypeId(20, false);
 
         world.getBlockAt(x - 1, y, z).setTypeId(20, false);
         world.getBlockAt(x - 1, y + 1, z).setTypeId(20, false);
         world.getBlockAt(x - 1, y + 2, z).setTypeId(20, false);
 
         world.getBlockAt(x, y, z + 1).setTypeId(20, false);
         world.getBlockAt(x, y + 1, z + 1).setTypeId(20, false);
         world.getBlockAt(x, y + 2, z + 1).setTypeId(20, false);
 
         world.getBlockAt(x, y, z - 1).setTypeId(20, false);
         world.getBlockAt(x, y + 1, z - 1).setTypeId(20, false);
         world.getBlockAt(x, y + 2, z - 1).setTypeId(20, false);
     }
 
     private World createWorld() {
         WorldCreator worldCreator = new WorldCreator("island-" + getNextId());
         worldCreator.environment(World.Environment.NORMAL);
         worldCreator.generateStructures(false);
         worldCreator.generator(new ChunkGenerator() {
             @SuppressWarnings("deprecation")
             @Override
             public byte[] generate(World world, Random random, int x, int z) {
                 return new byte[32768];
             }
 
             @Override
             public Location getFixedSpawnLocation(World world, Random random) {
                 if (!world.isChunkLoaded(0, 0)) {
                     world.loadChunk(0, 0);
                 }
 
                 return new Location(world, 0.0D, 64.0D, 0.0D);
             }
         });
 
         World world = worldCreator.createWorld();
         world.setDifficulty(Difficulty.NORMAL);
         world.setSpawnFlags(false, false);
         world.setPVP(true);
         world.setStorm(false);
         world.setThundering(false);
         world.setWeatherDuration(Integer.MAX_VALUE);
         world.setAutoSave(false);
         world.setKeepSpawnInMemory(false);
         world.setTicksPerAnimalSpawns(0);
         world.setTicksPerMonsterSpawns(0);
 
         world.setGameRuleValue("doMobSpawning", "false");
         world.setGameRuleValue("mobGriefing", "false");
         world.setGameRuleValue("doFireTick", "false");
 
         return world;
     }
 
     private int getNextId() {
         int id = nextId++;
 
         if (nextId == Integer.MAX_VALUE) {
             nextId = 0;
         }
 
         return id;
     }
 
     public static WorldController get() {
         if (worldController == null) {
             worldController = new WorldController();
         }
 
         return worldController;
     }
 
 }
