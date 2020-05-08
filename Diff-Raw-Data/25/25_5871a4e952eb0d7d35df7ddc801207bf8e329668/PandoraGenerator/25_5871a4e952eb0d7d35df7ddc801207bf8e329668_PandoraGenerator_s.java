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
 	import java.util.List;
 	import java.util.Random;
 //* IMPORTS: BUKKIT
 	import org.bukkit.generator.BlockPopulator;
 	import org.bukkit.generator.ChunkGenerator;
 	import org.bukkit.generator.ChunkGenerator.BiomeGrid;
 	import org.bukkit.Location;
 	import org.bukkit.util.noise.SimplexNoiseGenerator;
 	import org.bukkit.World;
 //* IMPORTS: OTHER
 	//* NOT NEEDED
 
 public class PandoraGenerator extends ChunkGenerator
 {
 	private List<PandoraBiome>	generators = new ArrayList<PandoraBiome>();
 	private List<BlockPopulator>	populators = new ArrayList<BlockPopulator>();
 	private PandoraBiome lastGen, defaultGen, tempGen;
 	private SimplexNoiseGenerator noise;
 	private Location center, currentEdge, tempEdge;
 	private int lastX, lastZ, xPos, zPos, currentX, currentY, currentZ, xs, zs;
 	private int cXPos, cZPos, index, edgeXPos, edgeZPos, edgeMax, octaves;
 	private double temperature, humidity, tempRange, humidityRange;
 	private double range, scale, amplitude, frequency, cnoise;
 	private byte byteId;
 	private byte[] depTempColumn, depTempChunk, tempColumn;
 	private byte[][] tempChunk;
 	private short shortId;
 	private short[] extTempColumn;
 	private short[][] extTempChunk;
 	private boolean useCustomMetrics = false;
 
 	public PandoraGenerator addBiome(PandoraBiome biome) {
 		if(biome == null)
 			return this;
 
 		generators.add(biome);
 		return this;
 	}
 
 	public PandoraGenerator addPopulator(BlockPopulator populator) {
 		if(populator == null)
 			return this;
 
 		populators.add(populator);
 		return this;
 	}
 
 	public boolean canSpawn(World world, int x, int z) {
 		getGenerator(world, x, z);
 
 		if(lastGen == null)
 			return false;
 
 		lastGen.synchronize(world);
 		return lastGen.canSpawn(x, z);
 	}
 
 	@Deprecated
 	public byte[] generate(World world, Random rand, int x, int z) {
 		depTempChunk = new byte[32768];
 		xPos = x * 16;
 		zPos = z * 16;
 
 		for(currentX = 0; currentX < 16; currentX++) {
 			cXPos = currentX + xPos;
 			for(currentZ = 0; currentZ < 16; currentZ++) {
 				cZPos = currentZ + zPos;
 				getGenerator(world, cXPos, cZPos);
 
 				if(lastGen == null)
 					continue;
 
 				lastGen.synchronize(world);
 				depTempColumn = lastGen.generate(rand, cXPos, cZPos);
 
 				if(depTempColumn == null || depTempColumn.length < 128)
 					depTempColumn = new byte[128];
 
 				index = ((currentX * 16) + currentZ) * 128;
 
 				if(index < 0)
 					index = 0;
 				if((index + 128) > depTempChunk.length)
 					continue;
 
 				System.arraycopy(depTempColumn, 0, depTempChunk, index, 128);
 			}
 		}
 
 		return depTempChunk;
 	}
 
 	public byte[][] generateBlockSections(World world, Random rand, int x, int z, BiomeGrid biomes)
 	{
 		tempChunk = new byte[world.getMaxHeight() / 16][];
 		xPos = x * 16;
 		zPos = z * 16;
 
 		for(currentX = 0; currentX < 16; currentX++) {
 			cXPos = currentX + xPos;
 			for(currentZ = 0; currentZ < 16; currentZ++) {
 				cZPos = currentZ + zPos;
 				getGenerator(world, cXPos, cZPos);
 
 				if(lastGen == null)
 					return null;
 
 				lastGen.synchronize(world);
 				tempColumn = lastGen.generateSections(rand, cXPos, cZPos, biomes);
 
 				if(tempColumn == null || tempColumn.length < world.getMaxHeight())
 					return null;
 
 				for(currentY = 0; currentY < world.getMaxHeight(); currentY++) {
 					byteId = tempColumn[currentY];
 					setBlock(tempChunk, currentX, currentY, currentZ, byteId);
 				}
 			}
 		}
 
 		return tempChunk;
 	}
 
 	public short[][] generateExtBlockSections(World world, Random rand, int x, int z, BiomeGrid biomes)
 	{
 		extTempChunk = new short[world.getMaxHeight() / 16][];
 		xPos = x * 16;
 		zPos = z * 16;
 
 		for(currentX = 0; currentX < 16; currentX++) {
 			cXPos = currentX + xPos;
 			for(currentZ = 0; currentZ < 16; currentZ++) {
 				cZPos = currentZ + zPos;
 				getGenerator(world, cXPos, cZPos);
 
 				if(lastGen == null)
 					return null;
 
 				lastGen.synchronize(world);
 				extTempColumn = lastGen.generateExtSections(rand, cXPos, cZPos, biomes);
 
 				if(extTempColumn == null || extTempColumn.length < world.getMaxHeight())
 					return null;
 
 				for(currentY = 0; currentY < world.getMaxHeight(); currentY++) {
 					shortId = extTempColumn[currentY];
 					setBlock(extTempChunk, currentX, currentY, currentZ, shortId);
 				}
 			}
 		}
 
 		return extTempChunk;
 	}
 
 	public double getBiomeNoise(World world, int x, int z, boolean invertSeed) {
 		if(world == null)
 			return 0D;
 
 		if(!useCustomMetrics)
 			return 0D;
 
 		if(!invertSeed)
 			this.noise = new SimplexNoiseGenerator(world.getSeed());
 		else
 			this.noise = new SimplexNoiseGenerator(~(world.getSeed()));
 
 		range /= 2D;
 		xs = (int) Math.round(x / scale);
 		zs = (int) Math.round(z / scale);
 		cnoise = noise.getNoise(xs, zs, octaves, frequency, amplitude);
 
 		return ((range * cnoise) + range);
 	}
 
 	public List<PandoraBiome> getDefaultBiomes(World world) {
 		return generators;
 	}
 
 	public List<BlockPopulator> getDefaultPopulators(World world) {
 		return populators;
 	}
 
 	public Location getFixedSpawnLocation(World world, Random rand) {
 		return null;
 	}
 
 	private void getGenerator(World world, int x, int z) {
 		if((x == lastX && z == lastZ && lastGen != null) || world == null)
 			return;
 
 		temperature = getTemperature(world, x, z);
 		humidity = getHumidity(world, x, z);
 
 		lastGen = defaultGen;
 
 		for(PandoraBiome currentGen : generators) {
 			if(currentGen.minTemperature > temperature)
 				continue;
 			else if(currentGen.maxTemperature < temperature)
 				continue;
 			else if(currentGen.minHumidity > humidity)
 				continue;
 			else if(currentGen.maxHumidity < humidity)
 				continue;
 			else if(lastGen == defaultGen) {
 				lastGen = currentGen;
 				tempRange = lastGen.maxTemperature - lastGen.minTemperature;
 				humidityRange = lastGen.maxHumidity - lastGen.minHumidity;
 				continue;
 			}
 			else if(tempRange <= (currentGen.maxTemperature - currentGen.minTemperature))
 				continue;
 			else if(humidityRange <= (currentGen.maxHumidity - currentGen.minHumidity))
 				continue;
 
 			lastGen = currentGen;
 			tempRange = lastGen.maxTemperature - lastGen.minTemperature;
 			humidityRange = lastGen.maxHumidity - lastGen.minHumidity;
 		}
 	}
 
 	public double getHumidity(World world, int x, int z) {
 		if(useCustomMetrics)
 			return getBiomeNoise(world, x, z, true);
 
 		return world.getHumidity(x, z);
 	}
 
 	public Location getNearestEdge(World world, int x, int z, int period, int count) {
 		return getNearestEdge(world, x, z, period, count, false);
 	}
 
 	public Location getNearestEdge(World world, int x, int z, int period, int count, boolean inner) {
 		if(world == null)
 			return null;
 
 		getGenerator(world, x, z);
 
 		if(lastGen == null)
 			return null;
 
 		tempGen = lastGen;
 		edgeMax = period * count;
 
 		currentEdge = null;
 		tempEdge = null;
 
 		center = new Location(world, (double) x, 0D, (double) z);
 
 		for(edgeXPos = (x - edgeMax); edgeXPos <= (x + edgeMax); edgeXPos += period) {
 			for(edgeZPos = (z - edgeMax); edgeZPos <= (z + edgeMax); edgeZPos += period) {
 				getGenerator(world, edgeXPos, edgeZPos);
 
 				if(lastGen != tempGen && currentEdge == null) {
 					currentEdge = new Location(world, (double) edgeXPos, 0D, (double) edgeZPos);
 					continue;
 				}
 				else if(lastGen != tempGen) {
 					tempEdge = new Location(world, (double) edgeXPos, 0D, (double) edgeZPos);
 
 					if(center.distance(currentEdge) > center.distance(tempEdge))
 						currentEdge = tempEdge;
 				}
 			}
 		}
 
 		if(currentEdge == null) {
 			return null;
 		}
 		else if(!inner) {
 			edgeXPos = currentEdge.getBlockX();
 			edgeZPos = currentEdge.getBlockZ();
 
 			return getNearestEdge(world, edgeXPos, edgeZPos, 1, period, true);
 		}
 
 		return currentEdge;
 	}
 
 	public int getNearestEdgeBlockDistance(World world, int x, int z, int period, int count) {
 		return (int) getNearestEdgeDistance(world, x, z, period, count);
 	}
 
 	public double getNearestEdgeDistance(World world, int x, int z, int period, int count) {
 		if(getNearestEdge(world, x, z, period, count) == null)
 			return -1D;
 
 		return center.distance(currentEdge);
 	}
 
 	public double getTemperature(World world, int x, int z) {
 		if(useCustomMetrics)
 			return getBiomeNoise(world, x, z, false);
 
 		return world.getTemperature(x, z);
 	}
 
 	public boolean isNearEdge(World world, int x, int z, int period, int count) {
 		if(world == null)
 			return false;
 
 		getGenerator(world, x, z);
 
 		if(lastGen == null)
 			return false;
 
 		tempGen = lastGen;
 		edgeMax = period * count;
 
 		for(edgeXPos = (x - edgeMax); edgeXPos <= (x + edgeMax); edgeXPos += period) {
 			for(edgeZPos = (z - edgeMax); edgeZPos <= (z + edgeMax); edgeZPos += period) {
 				getGenerator(world, edgeXPos, edgeZPos);
 
 				if(lastGen != tempGen)
 					return true;
 			}
 		}
 
 		return false;
 	}
 
 	public boolean isUsingCustomBiomeMetrics() {
 		return useCustomMetrics;
 	}
 
 	private void setBlock(byte[][] result, int x, int y, int z, byte id) {
 		if(result[y >> 4] == null)
 			result[y >> 4] = new byte[4096];
 
 		result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = id;
 	}
 
 	private void setBlock(short[][] result, int x, int y, int z, short id) {
 		if(result[y >> 4] == null)
 			result[y >> 4] = new short[4096];
 
 		result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = id;
 	}
 
 	public PandoraGenerator setDefaultBiome(PandoraBiome biome) {
 		if(biome == null)
 			return this;
 
 		defaultGen = biome;
 		return this;
 	}
 	
 	public void setUseCustomBiomeMetrics(boolean setting) {
 		setUseCustomBiomeMetrics(setting, 100D, 1D, 2, 100D, 15D);
 	}
 	
 	public void setUseCustomBiomeMetrics(boolean setting, double range, double scale, int octaves, double amplitude, double frequency) {
 		this.useCustomMetrics = setting;
 		this.range = range;
 		this.scale = scale;
 		this.octaves = octaves;
 		this.amplitude = amplitude;
 		this.frequency = (frequency / 1000D);
 	}
 }
