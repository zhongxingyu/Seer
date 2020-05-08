 /*
  * Copyright (c) 2012-2013 Sean Porter <glitchkey@gmail.com>
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.pandora;
 
 //* IMPORTS: JDK/JRE
 	import java.util.ArrayList;
 	import java.util.HashMap;
 	import java.util.List;
 	import java.util.Map;
 	import java.util.Random;
 //* IMPORTS: BUKKIT
 	import org.bukkit.generator.BlockPopulator;
 	import org.bukkit.generator.ChunkGenerator;
 	import org.bukkit.generator.ChunkGenerator.BiomeGrid;
 	import org.bukkit.Location;
 	import org.bukkit.util.noise.SimplexNoiseGenerator;
 	import org.bukkit.World;
 //* IMPORTS: PANDORA
 	//* NOT NEEDED
 //* IMPORTS: OTHER
 	//* NOT NEEDED
 
 public class PandoraGenerator extends ChunkGenerator
 {
 	private List<PandoraBiome>	generators = new ArrayList<PandoraBiome>();
 	private List<BlockPopulator>	populators = new ArrayList<BlockPopulator>();
 	private Map<World, SimplexNoiseGenerator> noises, flippedNoises;
 	private PandoraBiome defaultGen;
 	private int octaves;
 	private double range, scale, amplitude, frequency;
 	private boolean useCustomMetrics = false;
 
 	public PandoraGenerator addBiome(PandoraBiome biome) {
 		if (biome == null)
 			return this;
 
 		generators.add(biome);
 		return this;
 	}
 
 	public PandoraGenerator addPopulator(BlockPopulator populator) {
 		if (populator == null)
 			return this;
 
 		populators.add(populator);
 		return this;
 	}
 
 	public PandoraGenerator addPopulator(PandoraPopulator populator) {
 		if (populator == null)
 			return this;
 
 		populators.add((BlockPopulator) populator);
 		return this;
 	}
 
 	public boolean canSpawn(World world, int x, int z) {
 		PandoraBiome biome = getGenerator(world, x, z);
 
 		if (biome == null)
 			return false;
 
 		return biome.canSpawn(world, x, z);
 	}
 
 	@Deprecated
 	public byte[] generate(World world, Random rand, int x, int z) {
 		byte[] chunk = new byte[32768];
 		int xPos = x << 4;
 		int zPos = z << 4;
 
 		for (int cx = 0; cx < 16; cx++) {
 			int cxPos = cx + xPos;
 
 			for (int cz = 0; cz < 16; cz++) {
 				int czPos = cz + zPos;
 				PandoraBiome biome = getGenerator(world, cxPos, czPos);
 
 				if (biome == null)
 					continue;
 
 				byte[] column = biome.generate(world, rand, cxPos, czPos);
 
 				if (column == null || column.length < 128)
 					column = new byte[128];
 
 				int index = ((cx << 4) + cz) << 7;
 
 				if ((index + 128) > chunk.length)
 					continue;
 
 				System.arraycopy(column, 0, chunk, index, 128);
 			}
 		}
 
 		return chunk;
 	}
 
 	public byte[][] generateBlockSections(World world, Random rand, int x, int z,
 		BiomeGrid biomes)
 	{
 		byte[][] chunk = new byte[world.getMaxHeight() / 16][];
 		int xPos = x << 4;
 		int zPos = z << 4;
 
 		for (int cx = 0; cx < 16; cx++) {
 			int cxPos = cx + xPos;
 
 			for (int cz = 0; cz < 16; cz++) {
 				int czPos = cz + zPos;
 				PandoraBiome biome = getGenerator(world, cxPos, czPos);
 
 				if (biome == null)
 					return null;
 
 				byte[] column;
 				column = biome.generateSections(world, rand, cxPos, czPos, biomes);
 
 				if (column == null || column.length < world.getMaxHeight())
 					return null;
 
 				for (int cy = 0; cy < world.getMaxHeight(); cy++) {
 					byte id = column[cy];
 					setBlock(chunk, cx, cy, cz, id);
 				}
 			}
 		}
 
 		return chunk;
 	}
 
 	public short[][] generateExtBlockSections(World world, Random rand, int x, int z,
 		BiomeGrid biomes)
 	{
 		short[][] chunk = new short[world.getMaxHeight() / 16][];
 		int xPos = x << 4;
 		int zPos = z << 4;
 
 		for (int cx = 0; cx < 16; cx++) {
 			int cxPos = cx + xPos;
 
 			for (int cz = 0; cz < 16; cz++) {
 				int czPos = cz + zPos;
 				PandoraBiome biome = getGenerator(world, cxPos, czPos);
 
 				if (biome == null)
 					return null;
 
 				short[] column = biome.generateExtSections(world, rand, cxPos,
 					czPos, biomes);
 
 				if (column == null || column.length < world.getMaxHeight())
 					return null;
 
 				for (int cy = 0; cy < world.getMaxHeight(); cy++) {
 					short id = column[cy];
 					setBlock(chunk, cx, cy, cz, id);
 				}
 			}
 		}
 
 		return chunk;
 	}
 
 	public double getBiomeNoise(World world, int x, int z, boolean invertSeed) {
 		if (world == null)
 			return 0D;
 		else if (!useCustomMetrics)
 			return 0D;
		else if (noises == null)
 			noises = new HashMap<World, SimplexNoiseGenerator>();
 
 		if (!invertSeed && !noises.containsKey(world))
 			noises.put(world, (new SimplexNoiseGenerator(world.getSeed())));
 		else if (invertSeed && !flippedNoises.containsKey(world))
 			flippedNoises.put(world, (new SimplexNoiseGenerator(~(world.getSeed()))));
 
 		SimplexNoiseGenerator noise;
 
 		if (!invertSeed)
 			noise = noises.get(world);
 		else
 			noise = flippedNoises.get(world);
 
 		int xs = (int) Math.round(((double) x) / scale);
 		int zs = (int) Math.round(((double) z) / scale);
 		double cnoise = noise.getNoise(xs, zs, octaves, frequency, amplitude);
 
		return ((range * cnoise) + range);
 	}
 
 	public List<PandoraBiome> getBiomes(World world) {
 		return generators;
 	}
 
 	public PandoraBiome getDefaultBiome(World world) {
 		return defaultGen;
 	}
 
 	public List<BlockPopulator> getDefaultPopulators(World world) {
 		return populators;
 	}
 
 	public Location getFixedSpawnLocation(World world, Random rand) {
 		return null;
 	}
 
 	private PandoraBiome getGenerator(World world, int x, int z) {
 		if (world == null)
 			return defaultGen;
 
 		PandoraBiome generator = defaultGen;
 		double temperature = getTemperature(world, x, z);
 		double humidity = getHumidity(world, x, z);
 		double tempRange = generator.maxTemperature - generator.minTemperature;
 		double humidityRange = generator.maxHumidity - generator.minHumidity;
 
 		for (PandoraBiome cGen : generators) {
 			if (cGen.minTemperature > temperature)
 				continue;
 			else if (cGen.maxTemperature < temperature)
 				continue;
 			else if (cGen.minHumidity > humidity)
 				continue;
			else if(cGen.maxHumidity < humidity)
 				continue;
 			else if (generator == defaultGen) {
 				generator = cGen;
 				tempRange = generator.maxTemperature - generator.minTemperature;
 				humidityRange = generator.maxHumidity - generator.minHumidity;
 				continue;
 			}
 			else if (tempRange <= (cGen.maxTemperature - cGen.minTemperature))
 				continue;
 			else if (humidityRange <= (cGen.maxHumidity - cGen.minHumidity))
 				continue;
 
 			generator = cGen;
 			tempRange = generator.maxTemperature - generator.minTemperature;
 			humidityRange = generator.maxHumidity - generator.minHumidity;
 		}
 
 		return generator;
 	}
 
 	public double getHumidity(World world, int x, int z) {
 		if (useCustomMetrics)
 			return getBiomeNoise(world, x, z, true);
 
 		return world.getHumidity(x, z);
 	}
 
 	public Location getNearestEdge(World world, int x, int z, int period, int count) {
 		return getNearestEdge(world, x, z, period, count, false);
 	}
 
 	public Location getNearestEdge(World world, int x, int z, int period, int count,
 		boolean inner)
 	{
 		if (world == null)
 			return null;
 
 		PandoraBiome biome = getGenerator(world, x, z);
 
 		if (biome == null)
 			return null;
 
 		int edgeMax = period * count;
 		int xMax = (x + edgeMax);
 		int zMax = (z + edgeMax);
 
 		Location cEdge = null;
 		Location tempEdge = null;
 
 		Location center = new Location(world, x, 0D, z);
 
 		for (int cx = (x - edgeMax); cx <= xMax; cx += period) {
 			for (int cz = (z - edgeMax); cz <= zMax; cz += period) {
 				PandoraBiome cGen = getGenerator(world, cx, cz);
 
 				if (cGen != biome && cEdge == null) {
 					cEdge = new Location(world, cx, 0D, cz);
 					continue;
 				}
 				else if (cGen != biome) {
 					tempEdge = new Location(world, cx, 0D, cz);
 
 					if (center.distance(cEdge) > center.distance(tempEdge))
 						cEdge = tempEdge;
 				}
 			}
 		}
 
 		if (cEdge == null)
 			return null;
 		else if (!inner) {
 			int edgeXPos = cEdge.getBlockX();
 			int edgeZPos = cEdge.getBlockZ();
 
 			return getNearestEdge(world, edgeXPos, edgeZPos, 1, period, true);
 		}
 
 		return cEdge;
 	}
 
 	public int getNearestEdgeBlockDistance(World world, int x, int z, int period, int count) {
 		return (int) getNearestEdgeDistance(world, x, z, period, count);
 	}
 
 	public double getNearestEdgeDistance(World world, int x, int z, int period, int count) {
 		Location currentEdge = getNearestEdge(world, x, z, period, count);
 
 		if (currentEdge == null)
 			return -1D;
 
 		return (new Location(world, x, 0D, z)).distance(currentEdge);
 	}
 
 	public List<BlockPopulator> getPopulators(World world) {
 		return populators;
 	}
 
 	public double getTemperature(World world, int x, int z) {
 		if (useCustomMetrics)
 			return getBiomeNoise(world, x, z, false);
 
 		return world.getTemperature(x, z);
 	}
 
 	public boolean isNearEdge(World world, int x, int z, int period, int count) {
 		if (world == null)
 			return false;
 
 		PandoraBiome biome = getGenerator(world, x, z);
 
 		if (biome == null)
 			return false;
 
 		int edgeMax = period * count;
 		int xMax = (x + edgeMax);
 		int zMax = (z + edgeMax);
 
 		for (int cx = (x - edgeMax); cx <= xMax; cx += period) {
 			for (int cz = (z - edgeMax); cz <= zMax; cz += period) {
 				if (getGenerator(world, cx, cz) != biome)
 					return true;
 			}
 		}
 
 		return false;
 	}
 
 	public boolean isUsingCustomBiomeMetrics() {
 		return useCustomMetrics;
 	}
 
 	private void setBlock(byte[][] result, int x, int y, int z, byte id) {
 		if (result[y >> 4] == null)
 			result[y >> 4] = new byte[4096];
 
 		result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = id;
 	}
 
 	private void setBlock(short[][] result, int x, int y, int z, short id) {
 		if (result[y >> 4] == null)
 			result[y >> 4] = new short[4096];
 
 		result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = id;
 	}
 
 	public PandoraGenerator setDefaultBiome(PandoraBiome biome) {
 		if (biome == null)
 			return this;
 
 		defaultGen = biome;
 		return this;
 	}
 	
 	public void setUseCustomBiomeMetrics(boolean setting) {
		setUseCustomBiomeMetrics(setting, 100D, 1D, 2, 100D, 15D);
 	}
 	
 	public void setUseCustomBiomeMetrics(boolean setting, double range, double scale,
 		int octaves, double amplitude, double frequency)
 	{
 		this.useCustomMetrics = setting;
 		this.range = range / 2D;
 		this.scale = scale;
 		this.octaves = octaves;
 		this.amplitude = amplitude;
 		this.frequency = (frequency / 1000D);
 	}
 }
