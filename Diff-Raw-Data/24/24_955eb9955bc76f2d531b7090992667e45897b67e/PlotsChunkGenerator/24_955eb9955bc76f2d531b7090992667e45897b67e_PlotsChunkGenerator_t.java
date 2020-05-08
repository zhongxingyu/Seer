 package us.fitzpatricksr.cownet.commands.plots;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Biome;
 import org.bukkit.generator.BlockPopulator;
 import org.bukkit.generator.ChunkGenerator;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 import java.util.logging.Logger;
 
 /**
  * Generator for a flat map with square plots separated by roads.
  */
 public class PlotsChunkGenerator extends ChunkGenerator {
 	private static final Logger logger = Logger.getLogger("Minecraft");
 	private int plotSize;
 	private int plotHeight;
 	private Material path = Material.DOUBLE_STEP;
 	private byte pathId = (byte) path.getId();
 
 	public PlotsChunkGenerator(int size, int height, Material base, Material surface, Material path) {
 		this.plotSize = size + 7;
 		this.plotHeight = height;
 
 		this.pathId = (byte) path.getId();
 	}
 
 	public List<BlockPopulator> getDefaultPopulators(World world) {
 		return new ArrayList<BlockPopulator>();
 	}
 
 	public Location getFixedSpawnLocation(World world, Random rand) {
 		return new Location(world, 0, 18, 0);
 	}
 
 	private boolean isPathBlock(int x, int z) {
 		return (x % this.plotSize == 0) ||
 				(z % this.plotSize == 0) ||
 				((x + 1) % this.plotSize == 0) ||
 				((z + 1) % this.plotSize == 0) ||
 				((x - 1) % this.plotSize == 0) ||
 				((z - 1) % this.plotSize == 0) ||
 				((x + 2) % this.plotSize == 0) ||
 				((z + 2) % this.plotSize == 0) ||
 				((x - 2) % this.plotSize == 0) ||
 				((z - 2) % this.plotSize == 0);
 	}
 
 	private byte getPathBlock(int x, int z) {
 		return pathId;
 	}
 
 	public byte[][] generateBlockSections(World world, Random random, int chunkX, int chunkZ, BiomeGrid biomes) {
 		Chunk chunk = new Chunk();
 		int worldChunkX = chunkX << 4;
 		int worldChunkZ = chunkZ << 4;
 		byte bedId = (byte) Material.BEDROCK.getId();
 		byte baseId = (byte) Material.DIRT.getId();
 
 		for (int x = 0; x < 16; ++x) {
 			for (int z = 0; z < 16; ++z) {
 				chunk.setBlock(x, 0, z, bedId);
 
 				for (int y = 1; y < this.plotHeight; y++) {
 					chunk.setBlock(x, y, z, baseId);
 				}
 				if (this.isPathBlock(worldChunkX + x, worldChunkZ + z)) {
 					chunk.setBlock(x, this.plotHeight, z, getPathBlock(worldChunkX + x, worldChunkZ + z));
 				} else {
 					chunk.setMaterial(x, this.plotHeight, z, getBiomeSurfaceBlock(biomes.getBiome(x, z), random));
 					chunk.setMaterial(x, this.plotHeight + 1, z, getBiomeAboveSurfaceBlock(biomes.getBiome(x, z), random));
 				}
 			}
 		}
 
 		//now make the waters deeeeeeeper
 		double depthModifier = 0.5;
 		for (int y = plotHeight; y > 1; y--) {
 			for (int x = 0; x < 16; x++) {
 				for (int z = 0; z < 16; z++) {
 					if (chunk.getMaterial(x, y, z) == Material.WATER) {
 						//OK, we found a water square, it's it's completely surrounded by other water, make it deeper
 						// check (x-1,z-1) -> (x+1,z+1) to see if they're all water
 						int howWetIsIt = chunk.isWetValue(x - 1, y, z - 1) +
 								chunk.isWetValue(x, y, z - 1) +
 								chunk.isWetValue(x + 1, y, z - 1) +
 								chunk.isWetValue(x - 1, y, z) +
 								chunk.isWetValue(x + 1, y, z) +
 								chunk.isWetValue(x - 1, y, z + 1) +
 								chunk.isWetValue(x, y, z + 1) +
 								chunk.isWetValue(x + 1, y, z + 1);
 						if (howWetIsIt >= 6 + (plotHeight - y) / 2) {
 							// even if it's not surrounded, give it a 20% chance of being deep
 							chunk.setMaterial(x, y - 1, z, Material.WATER);
 						}
 					}
 				}
 				depthModifier = depthModifier * 2;
 			}
 		}
 		return chunk.getData();
 	}
 
 	private static class Chunk {
 		private byte[][] data;
 
 		public Chunk() {
 			data = new byte[16][];
 		}
 
 		public Chunk(byte[][] data) {
 			this.data = data;
 		}
 
 		public Material getMaterial(int x, int y, int z) {
 			return Material.getMaterial(getBlockType(x, y, z));
 		}
 
 		public int getBlockType(int x, int y, int z) {
 			if (data[y >> 4] == null) {
 				return (byte) 0;
 			}
 			return data[y >> 4][((y & 0xF) << 8) | (z << 4) | x];
 		}
 
 		void setMaterial(int x, int y, int z, Material m) {
 			setBlock(x, y, z, (byte) m.getId());
 		}
 
 		void setBlock(int x, int y, int z, byte blkid) {
 			if (blkid == 0) return;
 			if (data[y >> 4] == null) {
 				data[y >> 4] = new byte[4096];
 			}
 			data[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = blkid;
 		}
 
 		public byte[][] getData() {
 			return data;
 		}
 
 		private int isWetValue(int x, int y, int z) {
 			return isWet(x, y, z) ? 1 : 0;
 		}
 
 		private boolean isWet(int x, int y, int z) {
 			if (x < 0) x = 0;
 			if (x > 15) x = 15;
 			if (z < 0) z = 0;
 			if (z > 15) z = 15;
 			Material m = getMaterial(x, y, z);
 			return m == Material.WATER || m == Material.DOUBLE_STEP;
 		}
 
 	}
 
 	public Material getBiomeSurfaceBlock(Biome b, Random random) {
 		BiomeContents contents = biomeContents.get(b);
 		return (contents == null) ? Material.GRASS : contents.nextMaterial(random);
 	}
 
 	public Material getBiomeAboveSurfaceBlock(Biome b, Random random) {
 		BiomeContents contents = biomeDecorations.get(b);
 		return (contents == null) ? Material.AIR : contents.nextMaterial(random);
 	}
 
 	private static HashMap<Biome, BiomeContents> biomeContents = new HashMap<Biome, BiomeContents>();
 
 	static {
 		contentsFor(biomeContents, Material.GRASS, Biome.EXTREME_HILLS, Biome.FOREST_HILLS, Biome.TAIGA_HILLS, Biome.SMALL_MOUNTAINS).add(Material.STONE, 2.0);
		contentsFor(biomeContents, Material.GRASS, Biome.PLAINS, Biome.FOREST, Biome.TAIGA, Biome.JUNGLE, Biome.JUNGLE_HILLS);
 		contentsFor(biomeContents, Material.SAND, Biome.DESERT, Biome.BEACH).add(Material.SANDSTONE, 10).add(Material.GRAVEL, 2);
		contentsFor(biomeContents, Material.SNOW_BLOCK, Biome.ICE_PLAINS, Biome.ICE_MOUNTAINS);
 		contentsFor(biomeContents, Material.WATER, Biome.OCEAN, Biome.RIVER, Biome.FROZEN_OCEAN, Biome.FROZEN_RIVER);
 		contentsFor(biomeContents, Material.AIR, Biome.SKY);
 		contentsFor(biomeContents, Material.NETHERRACK, Biome.HELL).add(Material.SOUL_SAND, 20);
		contentsFor(biomeContents, Material.GRASS, Biome.SWAMPLAND, Biome.MUSHROOM_ISLAND, Biome.MUSHROOM_SHORE);
 	}
 
 	private static HashMap<Biome, BiomeContents> biomeDecorations = new HashMap<Biome, BiomeContents>();
 
 	static {
		//		contentsFor(biomeDecorations, Material.AIR, Biome.RAINFOREST).add(Material.SAPLING, 0.2).add(Material.LEAVES, 0.2);
 		contentsFor(biomeDecorations, Material.AIR, Biome.SWAMPLAND, Biome.JUNGLE, Biome.JUNGLE_HILLS);
		//		contentsFor(biomeDecorations, Material.AIR, Biome.SEASONAL_FOREST, Biome.FOREST, Biome.FOREST_HILLS).add(Material.LEAVES, 0.2).add(Material.LONG_GRASS, 0.2).add(Material.SAPLING, 0.2).add(Material.MELON_BLOCK, 0.1);
		//		contentsFor(biomeDecorations, Material.AIR, Biome.TAIGA, Biome.TAIGA_HILLS, Biome.SAVANNA, Biome.EXTREME_HILLS, Biome.SMALL_MOUNTAINS).add(Material.YELLOW_FLOWER, 0.1).add(Material.RED_ROSE, 0.1);
		//		contentsFor(biomeDecorations, Material.AIR, Biome.SHRUBLAND);
 		contentsFor(biomeDecorations, Material.AIR, Biome.DESERT, Biome.DESERT_HILLS).add(Material.CACTUS, 0.1).add(Material.DEAD_BUSH, 0.02);
 		contentsFor(biomeDecorations, Material.AIR, Biome.PLAINS).add(Material.LONG_GRASS, 0.3).add(Material.SAPLING, 0.1).add(Material.CROPS, 0.1).add(Material.SUGAR_CANE_BLOCK, 0.1);
		//		contentsFor(biomeDecorations, Material.AIR, Biome.TUNDRA).add(Material.SNOW, 20).add(Material.ICE, 2);
 		contentsFor(biomeDecorations, Material.ICE, Biome.FROZEN_OCEAN, Biome.FROZEN_RIVER).add(Material.SNOW, 2).add(Material.AIR, 2);
		//		contentsFor(biomeDecorations, Material.AIR, Biome.ICE_DESERT, Biome.ICE_PLAINS, Biome.ICE_MOUNTAINS).add(Material.SNOW, 50).add(Material.ICE, 25);
 		contentsFor(biomeDecorations, Material.AIR, Biome.MUSHROOM_ISLAND, Biome.MUSHROOM_SHORE).add(Material.RED_MUSHROOM, 0.2);
 		contentsFor(biomeDecorations, Material.AIR, Biome.RIVER).add(Material.WATER_LILY, 1);
 		contentsFor(biomeDecorations, Material.AIR, Biome.BEACH, Biome.HELL, Biome.SKY, Biome.OCEAN);
 	}
 
 	private static BiomeContents contentsFor(HashMap<Biome, BiomeContents> map, Material fill, Biome... biomes) {
 		BiomeContents results = new BiomeContents(fill);
 		for (Biome b : biomes) {
 			map.put(b, results);
 		}
 		return results;
 	}
 
 	private static class BiomeContents {
 		private Material[] contents = new Material[1000];
 		private int nextFree = 0;
 
 		public BiomeContents(Material fill) {
 			for (int i = 0; i < contents.length; i++) contents[i] = fill;
 		}
 
 		public BiomeContents add(Material material, double dFrequency) {
 			int frequency = (int) (dFrequency * 10);
 			for (int i = nextFree; i < contents.length && i < nextFree + frequency; i++) {
 				contents[i] = material;
 			}
 			nextFree += frequency;
 			return this;
 		}
 
 		public Material nextMaterial(Random r) {
 			return contents[Math.abs(r.nextInt()) % contents.length];
 		}
 	}
 }
