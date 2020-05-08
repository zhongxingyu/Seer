 package shadow.mods.metallurgy.precious;
 
 import java.util.Random;
 
 import shadow.mods.metallurgy.MetallurgyWorldGenMinable;
 import shadow.mods.metallurgy.MetallurgyWorldGenNetherMinable;
 import shadow.mods.metallurgy.nether.NetherConfig;
 import shadow.mods.metallurgy.nether.mod_MetallurgyNether;
 
 import net.minecraft.src.Block;
 import net.minecraft.src.IChunkProvider;
 import net.minecraft.src.World;
 import cpw.mods.fml.common.IWorldGenerator;
 
 public class PreciousWorldGen implements IWorldGenerator
 {
 
 	@Override
 	public void generate(Random rand, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
 
 		for(int i = 0; i < OrePrecious.numMetals; i++)
 			if(PreciousConfig.metalEnabled[i])
 				generateOre(world, rand, chunkX * 16, chunkZ * 16, i);
 	}
 
 	public static void generateOre(World world, Random rand, int chunkX, int chunkZ, int meta)
 	{
 		for(int i = 0; i < PreciousConfig.VeinCount[meta]; i++)
 		{
 			int randPosX = chunkX + rand.nextInt(16);
 			int randPosY = rand.nextInt(PreciousConfig.OreHeight[meta]);
 			int randPosZ = chunkZ + rand.nextInt(16);
			(new MetallurgyWorldGenNetherMinable(mod_MetallurgyPrecious.PreciousMetalsVein.blockID, meta, PreciousConfig.OreCount[meta])).generate(world, rand, randPosX, randPosY, randPosZ);
 		}
 	}
 
 }
