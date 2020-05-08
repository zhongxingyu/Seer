 package mods.elysium.api;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import mods.elysium.Elysium;
 import net.minecraft.block.Block;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.WeightedRandom;
 import net.minecraft.util.WeightedRandomItem;
 import net.minecraft.world.World;
 
 public class Plants {
 	
 
     static final List<GrassEntry> grassList = new ArrayList<GrassEntry>();
     static final List<SeedEntry> seedList = new ArrayList<SeedEntry>();
 
 	
 	static class GrassEntry extends WeightedRandomItem {
 		public final Block block;
 		public final int metadata;
 
 		public GrassEntry(Block block, int meta, int weight) {
 			super(weight);
 			this.block = block;
 			this.metadata = meta;
 		}
 	}
 
 	static class SeedEntry extends WeightedRandomItem {
 		public final ItemStack seed;
 
 		public SeedEntry(ItemStack seed, int weight) {
 			super(weight);
 			this.seed = seed;
 		}
 	}
 
 	/**
 	 * Register a new plant to be planted when bonemeal is used on grass.
 	 * 
 	 * @param block
 	 *            The block to place.
 	 * @param metadata
 	 *            The metadata to set for the block when being placed.
 	 * @param weight
 	 *            The weight of the plant, where red flowers are 10 and yellow
 	 *            flowers are 20.
 	 */
 	public static void addGrassPlant(Block block, int metadata, int weight) {
 		grassList.add(new GrassEntry(block, metadata, weight));
 	}
 
 	/**
 	 * Register a new seed to be dropped when breaking tall grass.
 	 * 
 	 * @param seed
 	 *            The item to drop as a seed.
 	 * @param weight
 	 *            The relative probability of the seeds, where wheat seeds are
 	 *            10.
 	 */
 	public static void addGrassSeed(ItemStack seed, int weight) {
 		seedList.add(new SeedEntry(seed, weight));
 	}
 	
 	public static void plantGrass(World world, int x, int y, int z)
     {
         GrassEntry grass = (GrassEntry)WeightedRandom.getRandomItem(world.rand, grassList);
         if (grass == null || grass.block == null || !grass.block.canBlockStay(world, x, y, z))
         {
             return;
         }
         if (grass.block.canBlockStay(world, x, y, z))
         	world.setBlock(x, y, z, grass.block.blockID, grass.metadata, 3);
     }
	
	public static ItemStack getGrassSeed(World world)
    {
        SeedEntry entry = (SeedEntry)WeightedRandom.getRandomItem(world.rand, seedList);
        if (entry == null || entry.seed == null)
        {
            return null;
        }
        return entry.seed.copy();
    }
 }
