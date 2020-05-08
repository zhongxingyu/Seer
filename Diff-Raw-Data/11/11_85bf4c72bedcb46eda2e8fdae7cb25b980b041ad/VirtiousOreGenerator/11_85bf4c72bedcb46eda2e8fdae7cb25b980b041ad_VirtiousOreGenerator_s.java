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
			generateVirtious(random, chunkX, chunkZ, world);
 		}
 	} 
 	
	public void generateVirtious(Random random, int chunkx, int chunkz, World world)
 	{
 		//Tak Ore
 		for(int l = 0; l < 17; l ++)
 		{
			int i1 = chunkx + random.nextInt(128);
			int i2 = random.nextInt(64);
			int i3 = chunkz + random.nextInt(128);
			(new VirtiousGenMinable(VirtiousBlocks.oreTak.blockID, 9)).generate(world, random, i1, i2, i3);
 		}
 	}
 	
 
 }
