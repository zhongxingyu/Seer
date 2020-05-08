 package mods.minecraft.darth.dc.world;
 
 import java.util.Random;
 
 import mods.minecraft.darth.dc.lib.BlockIDs;
 import net.minecraft.world.World;
 import net.minecraft.world.chunk.IChunkProvider;
 import net.minecraft.world.gen.feature.WorldGenMinable;
 
 import cpw.mods.fml.common.IWorldGenerator;
 
 public class OreGeneration implements IWorldGenerator
 {
     //The variable used for random generation
     Random random;
     
     @Override
     public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
     {
         this.random = random;
         generateUnknownOre(world, chunkX * 16, chunkZ * 16);
     }
 
     
     private void generateUnknownOre(World world, int i, int j)
     {
        
         int xCoord = i + this.random.nextInt(16);
        int yCoord = this.random.nextInt(7);
         int zCoord = j + this.random.nextInt(16);
         
         if (yCoord < 4)
         {
             int tempRand = (this.random.nextInt(3) * 4) + 1;
             new WorldGenMinable(BlockIDs.UNKNOWN_ORE, tempRand).generate(world, random, xCoord, yCoord, zCoord);
            //System.out.println("Big Ore ... Rand:" + tempRand + " " + xCoord + " " + yCoord + " " + zCoord);
         }
         
         if (yCoord > 4)
         {
            int tempRand = this.random.nextInt(4) + 1;
             new WorldGenMinable(BlockIDs.UNKNOWN_ORE, tempRand).generate(world, random, xCoord, yCoord, zCoord);
            //System.out.println("Small Ore ... Rand:" + tempRand + " " + xCoord + " " + yCoord + " " + zCoord);
         }
         
         if (yCoord == 4)
         {
            //System.out.println("No ore at: "  + xCoord + " " + yCoord + " " + zCoord);
             //No ore this chunk
         }
     
         
     }
 }
