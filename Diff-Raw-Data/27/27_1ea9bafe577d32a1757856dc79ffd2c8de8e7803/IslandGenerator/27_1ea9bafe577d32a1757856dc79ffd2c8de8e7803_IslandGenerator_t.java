 package com.vloxlands.gen.island;
 
 import com.vloxlands.game.voxel.Voxel;
 import com.vloxlands.game.world.Island;
 import com.vloxlands.gen.structure.CrystalGenerator;
 import com.vloxlands.gen.structure.SpikeGenerator;
 import com.vloxlands.gen.structure.TopLayerGenerator;
 import com.vloxlands.gen.structure.TreeGenerator;
import com.vloxlands.settings.CFG;
 
 public class IslandGenerator extends Thread
 {
 	/**
 	 * Override L
 	 */
 	public static final boolean OVR_L = true;
 	/**
 	 * Override R
 	 */
 	public static final boolean OVR_R = false;
 	
 	
 	public static final float MIN_VEIN_DISTANCE = 30;
 	
 	public float progress;
 	
 	public Island finishedIsland;
 	
 	private int actions;
 	private int quotient;
 	
 	int minSize, maxSize;
 	float yPos;
 	
 	public IslandGenerator(int minSize, int maxSize, float yPos)
 	{
		CFG.p(yPos);
 		this.minSize = minSize;
 		this.maxSize = maxSize;
 		progress = quotient = 0;
 		this.yPos = yPos;
 		
 		setName("IslandGenerator Thread");
 	}
 	
 	@Override
 	public void run()
 	{
 		finishedIsland = generateIsland();
 	}
 	
 	private Island generateIsland()
 	{
 		int radius = getRandomRadius();
 		
 		quotient = radius + 2;
 		
 		Island L = generatePerfectIsland(Island.SIZE / 2, Island.SIZE / 2, Island.SIZE / 2, radius);
 		// Island R = generatePerfectIsland((int) (128 + radius / Math.PI), Island.SIZE / 2, 128, radius);
 		// Island m = mergeIslandData(L, R, OVR_L);
 		
 		new TreeGenerator(Island.SIZE / 2, Island.SIZE / 2).generate(L, this);
 		
 		new CrystalGenerator(yPos).generate(L, this);
 		
 		L.grassify();
 		return L;
 	}
 	
 	public void updateProgress()
 	{
 		progress = actions / (float) quotient;
 		actions++;
 	}
 	
 	/**
 	 * Does count <code>radius + 2</code> for Quotient
 	 * 
 	 * @param size Either a Constant or the real Radius
 	 */
 	private Island generatePerfectIsland(int x, int y, int z, int radius)
 	{
 		// int radius = size == ISLAND_SMALL || size == ISLAND_MEDIUM || size == ISLAND_BIG ? getRandomRadius(size) : size;
 		int topLayers = (int) ((float) Math.random() * 3 + 3 + radius / 8.0f);
 		
 		Island island = new Island();
 		
 		// -- top Layers -- //
 		new TopLayerGenerator(x, y, z, radius, topLayers).generate(island, this);
 		
 		// -- Spikes -- //
 		for (int i = 0; i < radius; i++)
 		{
 			new SpikeGenerator(x, y, z, radius, topLayers).generate(island, this);
 		}
 		
 		// clearing upmost level
 		
 		for (int i = 0; i < Island.SIZE; i++) // x axis
 		{
 			for (int j = 0; j < Island.SIZE; j++) // z axis
 			{
 				if (island.getVoxelId(i, y, j) == Voxel.get("STONE").getId())
 				{
 					island.setVoxel(i, y, j, Voxel.get("DIRT").getId());
 				}
 				
 			}
 		}
 		updateProgress();
 		
 		return island;
 	}
 	
 	private int getRandomRadius()
 	{
 		return (int) (Math.random() * (maxSize - minSize)) + minSize;
 	}
 	
 	public Island mergeIslandData(Island L, Island R, boolean rule)
 	{
 		Island island = rule ? L.clone() : R.clone();
 		
 		Island ovr = rule ? R : L;
 		for (int x = 0; x < Island.SIZE; x++)
 		{
 			for (int y = 0; y < Island.SIZE; y++)
 			{
 				for (int z = 0; z < Island.SIZE; z++)
 				{
 					if (ovr.getVoxelId(x, y, z) == Voxel.get("AIR").getId()) continue;
 					
 					island.setVoxel(x, y, z, ovr.getVoxelId(x, y, z));
 				}
 			}
 		}
 		updateProgress();
 		return island;
 	}
 	
 	
 }
