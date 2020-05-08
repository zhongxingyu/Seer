 package sammko.quantumCraft.core;
 
 import java.util.Random;
 
 import sammko.quantumCraft.items.ItemInitializator;
 
 import net.minecraft.world.World;
 import net.minecraft.world.chunk.IChunkProvider;
 import net.minecraft.world.gen.feature.WorldGenMinable;
 
 import cpw.mods.fml.common.IWorldGenerator;
 
 public class WorldGenOres implements IWorldGenerator {
 
 	byte heightTreshold = 45;
 
 	int x,y,z = 0;
 	int o = 0;
 	
 	@Override
 	public void generate(Random random, int xo, int zo, World w,
 			IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
 		
 		for(int i=0;i<1;i++){
 			ranomize(xo, zo, random);
			if (o == 0) new WorldGenMinable(ItemInitializator.OrePositronium.blockID, 4 + random.nextInt(2)).generate(w, random, x, y, z); else
			if (o == 1) new WorldGenMinable(ItemInitializator.OreRadium.blockID, 4 + random.nextInt(2)).generate(w, random, x, y, z); else
			if (o == 2) new WorldGenMinable(ItemInitializator.OreGammatronium.blockID, 4 + random.nextInt(2)).generate(w, random, x, y, z); else
			if (o == 3) new WorldGenMinable(ItemInitializator.OreNeutrinium.blockID, 4 + random.nextInt(2)).generate(w, random, x, y, z); else
			if (o == 4) new WorldGenMinable(ItemInitializator.OreDepleted.blockID, 4 + random.nextInt(2)).generate(w, random, x, y, z); else
			if (o == 5) new WorldGenMinable(ItemInitializator.OrePlutonium.blockID, 0 + random.nextInt(2)).generate(w, random, x, y, z);
 			
 			System.out.println("[QuantumCraft] Generated vein of "+o+". Its at " + x + ", " + z);
 		}
 		
 	}
 	
 	public void ranomize(int xo, int zo, Random random)
 	{
 		
 		x = xo + random.nextInt(16);
 		y = random.nextInt(heightTreshold);
 		z = zo + random.nextInt(16);
 		o = random.nextInt(6);
 	}
 }
