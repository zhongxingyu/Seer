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
			if (o == 0) new WorldGenMinable(QuantumCraftSettings.OrePositroniumID, 4 + random.nextInt(2)).generate(w, random, x, y, z); else
			if (o == 1) new WorldGenMinable(QuantumCraftSettings.OreRadiumID, 4 + random.nextInt(2)).generate(w, random, x, y, z); else
			if (o == 2) new WorldGenMinable(QuantumCraftSettings.OreGammatroniumID, 4 + random.nextInt(2)).generate(w, random, x, y, z); else
			if (o == 3) new WorldGenMinable(QuantumCraftSettings.OreNeutriniumID, 4 + random.nextInt(2)).generate(w, random, x, y, z); else
			if (o == 4) new WorldGenMinable(QuantumCraftSettings.OreDepletedID, 4 + random.nextInt(2)).generate(w, random, x, y, z); else
			if (o == 5) new WorldGenMinable(QuantumCraftSettings.OrePlutoniumID, 0 + random.nextInt(2)).generate(w, random, x, y, z);
 			
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
