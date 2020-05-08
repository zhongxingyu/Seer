 package com.xpansive.bukkit.worldgen;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Random;
 import org.bukkit.Location;
 import org.bukkit.generator.BlockPopulator;
 import org.bukkit.generator.ChunkGenerator;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.util.noise.*;
 
 public class ExpansiveTerrainChunkGenerator extends ChunkGenerator {
 	VoronoiNoise v;
 
 	public byte[] generate(World world, Random random, int cx, int cz) {
 		byte[] result = new byte[32768];
 
 		if (v == null)
 			v = new VoronoiNoise(random);
 
		v.GenChunks(cx * 16, cz * 16, 128, 128, 48);
 
 		for (int x = 0; x < 16; x++) {
 			for (int z = 0; z < 16; z++) {
				int height = v.Voronoi(cx * 16 + x, cz * 16 + z) * 127 / 2000;
 
				// height = 127 - height;
 				height += 32;
 				height = Math.min(height, 127);
				// height = height * 48 + 64;
 				int dirtHeight = random.nextInt(3) + 2;
 				for (int y = 0; y <= height; y++) {
 					int offset = getOffset(x, y, z);
 
 					// cover the bottom layer with bedrock
 					if (y == 0)
 						result[offset] = (byte) Material.BEDROCK.getId();
 
 					// top layer gets grass
 					if (y == (int) height)
 						result[offset] = (byte) Material.GRASS.getId();
 
 					else if (y > height - dirtHeight)
 						result[offset] = (byte) Material.DIRT.getId();
 
 					else
 						result[offset] = (byte) Material.STONE.getId();

					// double perlin = p.Perlin_Noise(x, y, 500, 4, 2f, .7f);

					// double pixel = v.get((int)(x + perlin * 15), (int)(y +
					// perlin * 15), 4, .2f);
					// int pix = (int) (pixel * 128 + 128);
					// if (!(pix < 160 && pix > 130)) result[offset] = 0;
 				}
 			}
 		}
 
 		return result;
 
 	}
 
 	int getOffset(int x, int y, int z) {
 		return (x * 16 + z) * 128 + y;
 	}
 
 	public boolean canSpawn(World world, int x, int z) {
 		return true;
 	}
 
 	public List<BlockPopulator> getDefaultPopulators(World world) {
 		return Arrays.asList((BlockPopulator) new TreePopulator());
 	}
 
 	@Override
 	public Location getFixedSpawnLocation(World world, Random random) {
 		int x = random.nextInt(500) - 250;
 		int z = random.nextInt(250) - 250;
 		int y = world.getHighestBlockYAt(x, z);
 		return new Location(world, x, y, z);
 	}
 }
