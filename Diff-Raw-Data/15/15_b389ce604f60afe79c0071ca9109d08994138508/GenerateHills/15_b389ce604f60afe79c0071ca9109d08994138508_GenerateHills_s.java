 package nl.naxanria.selgen;
 
 import no.runsafe.framework.minecraft.RunsafeWorld;
 import org.bukkit.Material;
 import org.bukkit.util.noise.SimplexOctaveGenerator;
 
 public class GenerateHills
 {
 
 	public void generate(int startX, int startZ, int endX, int endZ, RunsafeWorld world)
 	{
 		generate(startX, startZ, endX, endZ, 64, 64, world.getRaw().getSeed(), 1 / 64.0, 0.3, 0.5, true, world);
 	}
 
 	public void generate(int startX, int startZ, int endX, int endZ, double multitude,
 														double seaLevel, long seed, double scale, double frequency, double amplitude, boolean hollow, RunsafeWorld world)
 	{
 		SimplexOctaveGenerator generator = new SimplexOctaveGenerator(seed, 8);
 		generator.setScale(scale);
 
 		for(int x = startX; x <= endX; x++)
 		{
 			for (int z = startZ; z <= endZ; z++)
 			{
 				double maxHeight = generator.noise(x, z, frequency, amplitude) * multitude + seaLevel;
				//System.out.println(String.format("[%d,%d] %f", x, z, maxHeight));
 				if(hollow)
 				{
 					for(int y = (int)maxHeight - depth; y < maxHeight; y++)
 					{
 						world.getBlockAt(x, y, z).setTypeId(Material.STONE.getId());
 					}
 					world.getBlockAt(x,(int) maxHeight, z).setTypeId(Material.GRASS.getId());
 				}
 				else
 				{
 					for(int y = 0; y < maxHeight; y++)
 					{
 						world.getBlockAt(x, y, z).setTypeId(Material.STONE.getId());
 					}
 					world.getBlockAt(x, (int) maxHeight, z).setTypeId(Material.GRASS.getId());
 				}
 
 			}
 		}
 
 	}
 
 	public int getDepth()
 	{
 		return depth;
 	}
 
 	public void setDepth(int depth)
 	{
 		this.depth = depth;
 	}
 
 	private int depth = 4;
 
 }
