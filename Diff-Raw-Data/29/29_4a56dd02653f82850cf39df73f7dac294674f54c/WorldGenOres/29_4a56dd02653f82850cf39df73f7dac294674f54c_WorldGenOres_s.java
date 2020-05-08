 package sammko.quantumCraft.core;
 
 import java.util.Random;
 
 import net.minecraft.world.World;
 import net.minecraft.world.chunk.IChunkProvider;
 
 import cpw.mods.fml.common.IWorldGenerator;
 
 public class WorldGenOres implements IWorldGenerator {
 
 	byte heightTreshold = 32;
 	int x, y, z = 0;
 
 	@Override
 	public void generate(Random random, int chunkX, int chunkZ, World world,
 			IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
 		// TODO: improve to make this generate in veins
 		if (QuantumCraftSettings.WGenPlu) {
			for (int i = 0; i < 5; i++) {
				randomize(chunkX, chunkZ, random);
				if (world.getBlockId(x, y, z) == 1)
					world.setBlock(x, y, z, QuantumCraftSettings.OrePlutoniumID);
			}
 		}
 		if (QuantumCraftSettings.WGenPos) {
 			randomize(chunkX, chunkZ, random);
 			if (world.getBlockId(x, y, z) == 1)
 				world.setBlock(x, y, z, QuantumCraftSettings.OrePositroniumID);
 		}
 		if (QuantumCraftSettings.WGenRad) {
 			randomize(chunkX, chunkZ, random);
 			if (world.getBlockId(x, y, z) == 1)
 				world.setBlock(x, y, z, QuantumCraftSettings.OreRadiumID);
 		}
 		if (QuantumCraftSettings.WGenGam) {
 			randomize(chunkX, chunkZ, random);
 			if (world.getBlockId(x, y, z) == 1)
 				world.setBlock(x, y, z, QuantumCraftSettings.OreGammatroniumID);
 		}
 		if (QuantumCraftSettings.WGenNeu) {
 			randomize(chunkX, chunkZ, random);
 			if (world.getBlockId(x, y, z) == 1)
 				world.setBlock(x, y, z, QuantumCraftSettings.OreNeutriniumID);
 		}
 		if (QuantumCraftSettings.WGenDep) {
 			randomize(chunkX, chunkZ, random);
 			if (world.getBlockId(x, y, z) == 1)
 				world.setBlock(x, y, z, QuantumCraftSettings.OreDepletedID);
 		}
 	}
 
 	void randomize(int cx, int cz, Random r) {
 		x = cx * 16 + r.nextInt(16);
 		y = r.nextInt(heightTreshold);
 		z = cz * 16 + r.nextInt(16);
 	}
 
 }
