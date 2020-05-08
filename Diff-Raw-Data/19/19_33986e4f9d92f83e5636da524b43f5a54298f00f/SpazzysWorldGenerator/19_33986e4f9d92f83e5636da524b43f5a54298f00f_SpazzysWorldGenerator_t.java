 package spazzysmod.world.gen.feature;
 
 import java.util.Random;
 
 import net.minecraft.world.World;
 import net.minecraft.world.chunk.IChunkProvider;
 import net.minecraft.world.gen.feature.WorldGenMinable;
import spazzysmod.blocks.MarsBlocks;
 import spazzysmod.blocks.MoonBlocks;
 import spazzysmod.blocks.SpazzysBlocks;
 import cpw.mods.fml.common.IWorldGenerator;
 
 public class SpazzysWorldGenerator implements IWorldGenerator {
 
 	@Override
 	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
 	{
 		switch (world.provider.dimensionId) {
 		case -1:
 			generateNether(world, random, chunkX * 16, chunkZ * 16);
 		case 0:
 			generateSurface(world, random, chunkX * 16, chunkZ * 16);
 		case 1:
 			generateEnd(world, random, chunkX * 16, chunkZ * 16);
 		case 10:
 			generateMoon(world, random, chunkX * 16, chunkZ * 16);
 		case 12:
			generateMarsMeteor(world, random, chunkX * 16, chunkZ * 16);
 		}
 
 	}
 
 	private void generateEnd(World world, Random random, int i, int j) {
 
 	}
 
 	private void generateMoon(World world, Random random, int i, int j)
 	{
 			int randPosX = i + random.nextInt(16);
 			int randPosY = random.nextInt(65);
 			int randPosZ = j + random.nextInt(16);
 			int block = world.getBlockId(i, randPosY, j);
 			
 			if(block == MoonBlocks.moonStone.blockID && randPosY > 60)
 			{
 				new WorldGenMeteor()
 				.generate(world, random, randPosX, randPosY, randPosZ);
 			}		
 	}
 
	private void generateMarsMeteor(World world, Random random, int i, int j) 
	{
		int randPosX = i + random.nextInt(16);
		int randPosY = random.nextInt(65);
		int randPosZ = j + random.nextInt(16);
		int block = world.getBlockId(i, randPosY, j);
		
		if(block == MarsBlocks.marsRock.blockID && randPosY > 60)
		{
			new WorldGenMeteor()
			.generate(world, random, randPosX, randPosY, randPosZ);
		}		
 	}
 
 	private void generateNether(World world, Random random, int i, int j) {
 
 	}
 
 	private void generateSurface(World world, Random random, int i, int j) {
 		for (int a = 0; a < 6; a++) {
 			int randPosX = i + random.nextInt(16);
 			int randPosY = random.nextInt(16);
 			int randPosZ = j + random.nextInt(16);
 
 			new WorldGenMinable(SpazzysBlocks.titaniumBlock.blockID, 4)
 			.generate(world, random, randPosX, randPosY, randPosZ);
 		}
 	}
 
 }
