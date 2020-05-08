 package com.xpansive.bukkit.expansiveterrain.terrain;
 
 import java.util.Random;
 
 import org.bukkit.Material;
 import org.bukkit.World;
 
 public abstract class TerrainGenerator {
     public abstract void fillColumn(World world, Random random, int worldX, int worldZ, int x, int z, byte[] chunkData);
     
     public abstract double getHeightMultiplier();
     
    protected void setBlock(byte[] data, int x, int y, int z, Material type) {
         data[(x * 16 + z) * 128 + y] = (byte) type.getId();
     }
 }
