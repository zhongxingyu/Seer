 package teamm.mods.virtious.world.gen;
 
 import java.util.Random;
 
 import teamm.mods.virtious.Virtious;
 import teamm.mods.virtious.lib.VirtiousBlocks;
 
 import net.minecraft.world.World;
 import net.minecraft.world.chunk.IChunkProvider;
 import cpw.mods.fml.common.IWorldGenerator;
 
 public class VirtiousOreGenerator implements IWorldGenerator
 {
 
 	@Override
 	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) 
 	{
 		if(world.provider.dimensionId == Virtious.dimensionID)
 		{
			generateVirtious(random, chunkX *16, chunkZ * 16, world);
 		}
 	} 
 	
	public void generateVirtious(Random random, int blockX, int blockZ, World world)
 	{
 		//Tak Ore
 		for(int l = 0; l < 17; l ++)
 		{
			new VirtiousGenMinable(VirtiousBlocks.oreTak.blockID, 9).generate(world, random, blockX + random.nextInt(16), random.nextInt(128), blockZ + random.nextInt(16));;
 		}
 	}
 	
 
 }
