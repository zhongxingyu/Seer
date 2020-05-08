 package com.xpansive.bukkit.expansiveterrain.biome;
 
 import org.bukkit.generator.BlockPopulator;
 
 import com.xpansive.bukkit.expansiveterrain.populator.*;
 import com.xpansive.bukkit.expansiveterrain.terrain.*;
 
 public class DesertBiomeGenerator extends BiomeGenerator {
     private final BlockPopulator[] populators = new BlockPopulator[] { 
            new CactusPopulator(
                    1, // Min height
                    6, // Max height
                    5, // Patch radius
                    4, // Patch chance
                    80) // New cactus chance
     };
     
     private final TerrainGenerator terrainGen = new DesertTerrainGenerator();
 
     @Override
     public BlockPopulator[] getPopulators() {
         return populators;
     }
 
     @Override
     public TerrainGenerator getTerrainGenerator() {
         return terrainGen;
     }
 
 }
