 package worldChangers.TuxCraft.world;
 
 import java.util.Random;
 
 import net.minecraft.world.World;
 import net.minecraft.world.chunk.IChunkProvider;
 import worldChangers.TuxCraft.WorldGenUtils;
 import cpw.mods.fml.common.IWorldGenerator;
 
 public class WorldChangersGenerator implements IWorldGenerator {
 	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
 		switch (world.provider.dimensionId) {
 		case 0:
 			generateOverworld(world, random, chunkX * 16, chunkZ * 16);
 		case 1:
 			generateEnd(world, random, chunkX * 16, chunkZ * 16);
 		case -1:
 			generateNether(world, random, chunkX * 16, chunkZ * 16);
 		}
 	}
 
 	public void generateOverworld(World world, Random random, int chunkX, int chunkZ) {
 		generateVolcano(world, random, chunkX, chunkZ);
 	}
 
 	private boolean generateVolcano(World world, Random random, int chunkX, int chunkZ) {
 		int x = chunkX * 16 + random.nextInt(16);
 		int z = chunkZ * 16 + random.nextInt(16);
 		int y = WorldGenUtils.getHighestBlock(world, x, z);
 
 		if ((world.getBiomeGenForCoords(x, z).biomeName != "Ocean") && (world.getBiomeGenForCoords(x, z).biomeName != "River") && (world.getBiomeGenForCoords(x, z).biomeName != "Swampland") && (world.getBiomeGenForCoords(x, z).biomeName != "Beach") && (world.getBiomeGenForCoords(x, z).biomeName != "JungleHills")) {
 			// Change back to 100!
			if (random.nextInt(100) == 1) {
 				long start = System.currentTimeMillis();
 				new WorldGenTVolcano(true).generate(world, random, x, y, z);
 				System.out.println("Volcono spawned at " + x + " " + y + " " + z + ". In biome " + String.valueOf(world.getBiomeGenForCoords(x, z).biomeName) + ". Took " + (System.currentTimeMillis() - start));
 			}
 
 		}
 
 		return true;
 	}
 
 	public void generateEnd(World world, Random random, int chunkX, int chunkZ) {
 	}
 
 	public void generateNether(World world, Random random, int chunkX, int chunkZ) {
 	}
 }
