 /**
  * 
  */
 package com.codeorb.myisle.worldgenerator;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.generator.BlockPopulator;
 
 
//CURRENTLY BROKEN!!!  If someone could look this over, that'd be great.
 public class IslandPopulator extends BlockPopulator{
 
     @Override
     public void populate(World world, Random rand, Chunk chunk) {
         //Chunk's x and y are chunklocation, not block location.
         if (chunk.getX() % 16 == 0 && chunk.getZ() % 16 == 0)
             createIsland(new Location(world, chunk.getX()*16, 50, chunk.getZ()*16));
     }
 
     private void createIsland(Location loc){
        System.out.println("Generating an island.");
         Location center = new Location(loc.getWorld(), loc.getBlockX()+5, loc.getBlockY(), loc.getBlockZ()+5);
         ArrayList<Block> blocks = new ArrayList<Block>();
         for (int x = loc.getBlockX(); x < loc.getBlockX()+10; x++)
             for (int z = loc.getBlockZ(); z < loc.getBlockZ()+10; z++){
                 Location target = new Location(loc.getWorld(), x, loc.getBlockY(), z);
                 if (center.distance(target) < 4) 
                     blocks.add(target.getBlock());
             }
         buildIsland(blocks);
 
     }
 
     private void buildIsland(List<Block> blocks){
        long start = System.currentTimeMillis();
         for (Block b : blocks){
            b.setType(Material.BEDROCK);
         }
        System.out.println((System.currentTimeMillis()-start)/1000);
 
     }
 
 
 }
