 package teamm.mods.virtious.world.gen;
 
 import java.util.Random;
 
 import teamm.mods.virtious.block.VirtiousFlower;
 import teamm.mods.virtious.lib.VirtiousBlocks;
 import net.minecraft.block.Block;
 import net.minecraft.world.World;
 import net.minecraft.world.gen.feature.WorldGenerator;
 
 public class VirtiousGenAmberTree extends WorldGenerator
 {
 	int leavesId,logId;
 	boolean fromSapling;
 
 	public VirtiousGenAmberTree(int leavesId, int logId, boolean fromSapling)
 	{
 		super(fromSapling);
 		this.leavesId = leavesId;
 		this.logId = logId;
 		this.fromSapling = fromSapling;
 	}
 
 	@Override
 	public boolean generate(World world, Random random, int i, int j, int k) 
 	{
 		if(!((VirtiousFlower)VirtiousBlocks.saplingVirtian).canThisPlantGrowOnThisBlockID(world.getBlockId(i, j - 1, k)))
 			return false;
 		
 		int leavesHeight = (random.nextInt(2) + 3) * 2;
 		int trunk = random.nextInt(2) + 2;
 				
 		int treeHeight = trunk + leavesHeight;
 		
 		if(this.fromSapling)
 		{
			for(int h = 1; h < treeHeight; h++){
 			    Block block = Block.blocksList[world.getBlockId(i, j + h, k)];
				if(!block.canBeReplacedByLeaves(world, i, j + h, k))
					return false;
 			}
 		}
 		
 		int cycleCount = random.nextInt();
 
 		for(int y = 0; y < treeHeight; y++)
 		{
 			world.setBlock(i, j + y, k, logId);
 				
 			int radius = Math.round(leavesHeight / 2);
 			int center = trunk + radius;
 
 			int radius2 = radius * radius;
 			if(y >= trunk)
 			{
 				world.setBlock(i, j + y, k, logId);
 
 				for(int x = -radius; x <= radius; x++)
 				{
 					for(int z = -radius; z <= radius; z++)
 					{
 						if(/*(x % 2 == 0 || z % 2 == 0) &&*/ cycleCount++ % 2 == 0 && x*x + (center - y) * (center - y) + z*z <= radius2)
 							addLeaves(world, i + x, j + y, k + z);
 					}
 				}
 			}
 		}
 		world.setBlock(i, j + treeHeight , k, leavesId);
 		return true;
 	}
 
 	private boolean addLeaves(World world, int x, int y, int z)
 	{
 		int id = world.getBlockId(x, y, z);
 	    Block block = Block.blocksList[id];
 		if(block == null || block.canBeReplacedByLeaves(world, x, y, z))
 		{
 			world.setBlock(x, y, z, leavesId);
 			return true;
 		}
 		return false;
 	}
 }
