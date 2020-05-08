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
 	import org.bukkit.Chunk;
 	import org.bukkit.generator.BlockPopulator;
 	import org.bukkit.Location;
 	import org.bukkit.util.noise.SimplexNoiseGenerator;
 	import org.bukkit.World;
 //* IMPORTS: PANDORA
 	//* NOT NEEDED
 //* IMPORTS: OTHER
 	//* NOT NEEDED
 
 public class PandoraPopulator extends BlockPopulator
 {
 	private List<PandoraBiomePopulator> populators = new ArrayList<PandoraBiomePopulator>();
 	private Map<World, SimplexNoiseGenerator> noises, flippedNoises;
 	private PandoraBiomePopulator defaultPop;
 	private int octaves;
 	private double range, scale, amplitude, frequency;
 	private boolean useCustomMetrics = false;
 
 	public PandoraPopulator addBiome(PandoraBiomePopulator biome) {
 		if (biome == null)
 			return this;
 
 		populators.add(biome);
 		return this;
 	}
 
 	public double getBiomeNoise(World world, int x, int z, boolean invertSeed) {
 		if (world == null)
 			return 0D;
 		else if (!useCustomMetrics)
 			return 0D;

		if (noises == null)
 			noises = new HashMap<World, SimplexNoiseGenerator>();
		if (flippedNoises == null)
			flippedNoises = new HashMap<World, SimplexNoiseGenerator>();
 
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
 
		return ((range * (cnoise / amplitude)) + range);
 	}
 
 	public List<PandoraBiomePopulator> getBiomes(World world) {
 		return populators;
 	}
 
 	public PandoraBiomePopulator getDefaultBiome(World world) {
 		return defaultPop;
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
 
 		PandoraBiomePopulator pop = getPopulator(world, x, z);
 
 		if (pop == null)
 			return null;
 
 		int edgeMax = period * count;
 		int xMax = (x + edgeMax);
 		int zMax = (z + edgeMax);
 
 		Location cEdge = null;
 		Location tempEdge = null;
 		Location center = new Location(world, x, 0D, z);
 
 		for (int cx = (x - edgeMax); cx <= xMax; cx += period) {
 			for (int cz = (z - edgeMax); cz <= zMax; cz += period) {
 				PandoraBiomePopulator cPop = getPopulator(world, cx, cz);
 
 				if (cPop != pop && cEdge == null) {
 					cEdge = new Location(world, cx, 0D, cz);
 					continue;
 				}
 				else if (cPop != pop) {
 					tempEdge = new Location(world, cx, 0D, cz);
 
 					if (center.distance(cEdge) > center.distance(tempEdge))
 						cEdge = tempEdge;
 				}
 			}
 		}
 
 		if (cEdge == null)
 			return null;
 		else if (!inner) {
 			int cx = cEdge.getBlockX();
 			int cz = cEdge.getBlockZ();
 
 			return getNearestEdge(world, cx, cz, 1, period, true);
 		}
 
 		return cEdge;
 	}
 
 	public int getNearestEdgeBlockDistance(World world, int x, int z, int period, int count) {
 		return (int) getNearestEdgeDistance(world, x, z, period, count);
 	}
 
 	public double getNearestEdgeDistance(World world, int x, int z, int period, int count) {
 		Location edge = getNearestEdge(world, x, z, period, count);
 		if (edge == null)
 			return -1D;
 
 		return (new Location(world, x, 0D, z)).distance(edge);
 	}
 
 	private PandoraBiomePopulator getPopulator(World world, int x, int z) {
 		if (world == null)
			return defaultPop;
 
 		PandoraBiomePopulator populator = defaultPop;
 		double temperature = getTemperature(world, x, z);
 		double humidity = getHumidity(world, x, z);
 		double tempRange = populator.maxTemperature - populator.minTemperature;
 		double humidityRange = populator.maxHumidity - populator.minHumidity;
 
 		for (PandoraBiomePopulator cPop : populators) {
 			if (cPop.minTemperature > temperature)
 				continue;
 			else if (cPop.maxTemperature < temperature)
 				continue;
 			else if (cPop.minHumidity > humidity)
 				continue;
 			else if (cPop.maxHumidity < humidity)
 				continue;
 			else if (populator == defaultPop) {
 				populator = cPop;
 				tempRange = populator.maxTemperature - populator.minTemperature;
 				humidityRange = populator.maxHumidity - populator.minHumidity;
 				continue;
 			}
 			else if (tempRange <= (cPop.maxTemperature - cPop.minTemperature))
 				continue;
 			else if (humidityRange <= (cPop.maxHumidity - cPop.minHumidity))
 				continue;
 
 			populator = cPop;
 			tempRange = populator.maxTemperature - populator.minTemperature;
 			humidityRange = populator.maxHumidity - populator.minHumidity;
 		}
 
 		return populator;
 	}
 
 	public double getTemperature(World world, int x, int z) {
 		if (useCustomMetrics)
 			return getBiomeNoise(world, x, z, false);
 
 		return world.getTemperature(x, z);
 	}
 
 	public boolean isNearEdge(World world, int x, int z, int period, int count) {
 		if (world == null)
 			return false;
 
 		PandoraBiomePopulator pop = getPopulator(world, x, z);
 
 		if (pop == null)
 			return false;
 
 		int edgeMax = period * count;
 		int xMax = (x + edgeMax);
 		int zMax = (z + edgeMax);
 
 		for (int cx = (x - edgeMax); cx <= xMax; cx += period) {
 			for (int cz = (z - edgeMax); cz <= zMax; cz += period) {
 				if (getPopulator(world, cx, cz) != pop)
 					return true;
 			}
 		}
 
 		return false;
 	}
 
 	public boolean isUsingCustomBiomeMetrics() {
 		return useCustomMetrics;
 	}
 
 	public void populate(World world, Random random, Chunk source) {
 		int xPos = source.getX() << 4;
 		int zPos = source.getZ() << 4;
 
 		for (int cx = 0; cx < 16; cx++) {
 			int cxPos = cx + xPos;
 			for (int cz = 0; cz < 16; cz++) {
 				int czPos = cz + zPos;
 				PandoraBiomePopulator pop = getPopulator(world, cxPos, czPos);
 
 				if (pop == null)
 					return;
 
 				pop.populate(world, random, source, cxPos, czPos);
 			}
 		}
 	}
 
 	public PandoraPopulator setDefaultBiome(PandoraBiomePopulator biome) {
 		if (biome == null)
 			return this;
 
 		defaultPop = biome;
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
