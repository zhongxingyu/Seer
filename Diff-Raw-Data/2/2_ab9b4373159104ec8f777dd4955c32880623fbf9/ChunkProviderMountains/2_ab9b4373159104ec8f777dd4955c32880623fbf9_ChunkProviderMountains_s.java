 /**
  * 
  */
 package net.nexisonline.spade.chunkproviders;
 
 import java.util.Random;
 
 import libnoiseforjava.NoiseGen.NoiseQuality;
 import libnoiseforjava.module.Perlin;
 import libnoiseforjava.module.RidgedMulti;
 
 import org.bukkit.ChunkProvider;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Biome;
 
 /**
  * @author Rob
  * 
  */
 public class ChunkProviderMountains extends ChunkProvider {
 	private RidgedMulti terrainNoise;
 	private Perlin continentNoise;
 	private int continentNoiseOctaves = 16;
 	private NoiseQuality noiseQuality = NoiseQuality.QUALITY_STD;
 	private double ContinentNoiseFrequency;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.bukkit.ChunkProvider#onLoad(org.bukkit.World, long)
 	 */
 	@Override
 	public void onLoad(World world, long seed) {
 		double Frequency = 0.1;
 		double Lacunarity = 0.05;
 		double Persistance = 0.25;
 		int OctaveCount = continentNoiseOctaves = 4;
 
 		try {
 			terrainNoise = new RidgedMulti();
 			continentNoise = new Perlin();
 			terrainNoise.setSeed((int) seed);
 			continentNoise.setSeed((int) seed + 2);
 			new Random((int) seed);
 
 			terrainNoise.setFrequency(Frequency);
 			terrainNoise.setNoiseQuality(noiseQuality);
 			terrainNoise.setOctaveCount(OctaveCount);
 			terrainNoise.setLacunarity(Lacunarity);
 
 			continentNoise.setFrequency(ContinentNoiseFrequency);
 			continentNoise.setNoiseQuality(noiseQuality);
 			continentNoise.setOctaveCount(continentNoiseOctaves);
 			continentNoise.setLacunarity(Lacunarity);
 			continentNoise.setPersistence(Persistance);
 		} catch (Exception e) {
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.bukkit.ChunkProvider#generateChunk(int, int, byte[],
 	 * org.bukkit.block.Biome[], double[])
 	 */
 	@Override
 	public void generateChunk(World world, int X, int Z, byte[] abyte, Biome[] biomes,
 			double[] temperature) {
 		int minHeight = 128;
 		for (int x = 0; x < 16; x++) {
 			for (int z = 0; z < 16; z++) {
 				double heightoffset = (continentNoise.getValue(
 						(double) (x + (X * 16)) / 10d,
 						(double) (z + (Z * 16)) / 10d, 0) + 1d);// *5d; // 2.0
 				double height = 30 + heightoffset;
 				height += (int) ((terrainNoise.getValue(x + (X * 16), z
						+ (Z * 16), 0) + heightoffset));
 				if (height < minHeight)
 					minHeight = (int) height;
 				for (int y = 0; y < 128; y++) {
 					// If below height, set rock. Otherwise, set air.
 					byte block = (y <= height) ? (byte) 1 : (byte) 0; // Fill
 					block = (y <= 63 && block == 0) ? (byte) 9 : block; // Water
 					// double _do = ((CaveNoise.GetValue(x + (X * chunksize.X),
 					// z + (Z * chunksize.Z), y * CaveDivisor) + 1) / 2.0);
 					// bool d3 = _do > CaveThreshold;
 
 					// if(d3)
 					// {
 					// if (y <= 63)
 					// block = 3;
 					// else
 					// block = 0;
 					// }
 					// else
 					// block = (d3) ? b[x, y, z] : (byte)1;
 					abyte[getBlockIndex(x,y,z)]=(byte) ((y<2) ? Material.BEDROCK.getId() : block);
 				}
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.bukkit.ChunkProvider#populateChunk(int, int, byte[],
 	 * org.bukkit.block.Biome[])
 	 */
 	@Override
 	public void populateChunk(World world, int x, int z, byte[] abyte, Biome[] biomes) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.bukkit.ChunkProvider#hasCustomTerrainGenerator()
 	 */
 	@Override
 	public boolean hasCustomTerrainGenerator() {
 		// TODO Auto-generated method stub
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.bukkit.ChunkProvider#hasCustomPopulator()
 	 */
 	@Override
 	public boolean hasCustomPopulator() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.bukkit.ChunkProvider#hasCustomCaves()
 	 */
 	@Override
 	public boolean hasCustomCaves() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.bukkit.ChunkProvider#generateCaves(java.lang.Object, int, int,
 	 * byte[])
 	 */
 	@Override
 	public void generateCaves(World world, int x, int z, byte[] abyte) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
