 package assets.fyresmodjam;
 
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.world.World;
 import net.minecraft.world.chunk.IChunkProvider;
 import net.minecraft.world.gen.feature.WorldGenMinable;
 import cpw.mods.fml.common.IWorldGenerator;
 
 public class PillarGen implements IWorldGenerator {
     @Override
     public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
         if(world.provider.dimensionId == 0 && random.nextInt(25) == 0) {
         	boolean placed = false;
         	
         	for(int y = 127, added = 0; y > 30 && !placed && added < 4; y--) {
         		for(int x = chunkX * 16; x < chunkX * 16 + 16 && !placed && added < 4; x++) {
         			for(int z = chunkZ * 16; z < chunkZ * 16 + 16 && !placed && added < 4; z++) {
        				if(random.nextInt(10) != 0 || world.isAirBlock(x, y, z) || Block.blocksList[world.getBlockId(x, y, z)].isBlockReplaceable(world, x, y, z) || world.getBlockId(x, y, z) == ModjamMod.blockTrap.blockID || world.getBlockId(x, y, z) == Block.leaves.blockID) {continue;}
         				
         				Block block = ModjamMod.blockPillar;
         		        
         		        if(block.canPlaceBlockAt(world, x, y + 1, z)) { 
         		        	world.setBlock(x, y + 1, z, block.blockID);
         		        	world.setBlockMetadataWithNotify(x, y + 1, z, 0, 0);
         		        	
         		        	world.setBlock(x, y + 2, z, block.blockID);
         		        	world.setBlockMetadataWithNotify(x, y + 2, z, 1, 0);
         		        	
         		        	placed = random.nextBoolean();
         		        	
         		        	y -= 10;
         		        	added++;
         		        	
         		        	//System.out.println(x + ", " + y + ", " + z);
         		        }
                 	}
             	}
         	}
         }
     }
 }
