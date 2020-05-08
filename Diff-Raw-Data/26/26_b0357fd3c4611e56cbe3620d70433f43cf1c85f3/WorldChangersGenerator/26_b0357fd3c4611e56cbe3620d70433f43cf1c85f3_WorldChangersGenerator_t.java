 package worldChangers.TuxCraft.world;
 
 import java.util.Random;
 
 import net.minecraft.world.World;
 import net.minecraft.world.chunk.IChunkProvider;
 import worldChangers.TuxCraft.WorldGenUtils;
 import cpw.mods.fml.common.IWorldGenerator;
 
 public class WorldChangersGenerator implements IWorldGenerator {
 	
	public static final int spawnRate = 80;
 	
 	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		if(chunkGenerator != null && chunkProvider != null)
		{
			switch (world.provider.dimensionId) {
			case 0:
				generateOverworld(world, random, chunkX, chunkZ);
				break;
			case 1:
				generateEnd(world, random, chunkX, chunkZ);
				break;
			case -1:
				generateNether(world, random, chunkX, chunkZ);
				break;
			}
 		}
 	}
 
 	public void generateOverworld(World world, Random random, int chunkX, int chunkZ) {
 		generateVolcano(world, random, chunkX, chunkZ);
 	}
 
 	private boolean generateVolcano(World world, Random random, int chunkX, int chunkZ) {
 		int x = chunkX * 16 + random.nextInt(16);
 		int z = chunkZ * 16 + random.nextInt(16);
 		int y = WorldGenUtils.getHighestBlock(world, x, z);
 
 		if ((world.getBiomeGenForCoords(x, z).minHeight > 0.0F) && (world.getBiomeGenForCoords(x, z).maxHeight < 0.4F)) {
 			if (random.nextInt(spawnRate) == 1) {
 				long start = System.currentTimeMillis();
 				new WorldGenVolcano(true).generate(world, random, x, y, z);
 				System.out.println("Volcono spawned at " + x + " " + y + " " + z + " In biome " + String.valueOf(world.getBiomeGenForCoords(x, z).biomeName) + ": Took " + (System.currentTimeMillis() - start) + " milliseconds");
 			}
 
 			if (random.nextInt(spawnRate) == 1) {
 				long start = System.currentTimeMillis();
 				new WorldGenCrater().generate(world, random, x, y, z);
 				System.out.println("Crater spawned at " + x + " " + y + " " + z + " In biome " + String.valueOf(world.getBiomeGenForCoords(x, z).biomeName) + ": Took " + (System.currentTimeMillis() - start) + " milliseconds");
 			}
 
 		}
 
 		return true;
 	}
 
 	public void generateEnd(World world, Random random, int chunkX, int chunkZ) {
 	}
 
 	public void generateNether(World world, Random random, int chunkX, int chunkZ) {
 		int x = chunkX * 16 + random.nextInt(16);
 		int z = chunkZ * 16 + random.nextInt(16);
 		int y = WorldGenUtils.getHighestBlock(world, x, z);
 		
 		if (random.nextInt(spawnRate) == 1)  {
 			long start = System.currentTimeMillis();
 			new WorldGenGhastHive(true).generate(world, random, x, y, z);
 			System.out.println("Ghast Hive spawned at " + x + " " + y + " " + z + ": Took " + (System.currentTimeMillis() - start) + " milliseconds");
 		}
 	}
 }
