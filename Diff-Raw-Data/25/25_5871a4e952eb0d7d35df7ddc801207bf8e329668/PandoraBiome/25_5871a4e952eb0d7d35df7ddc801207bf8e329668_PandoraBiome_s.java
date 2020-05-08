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
 	import java.lang.String;
 	import java.util.Random;
 //* IMPORTS: BUKKIT
 	import org.bukkit.block.Block;
 	import org.bukkit.generator.ChunkGenerator.BiomeGrid;
 	import org.bukkit.Material;
 	import org.bukkit.util.noise.SimplexNoiseGenerator;
 	import org.bukkit.World;
 //* IMPORTS: OTHER
 	//* NOT NEEDED
 
 public abstract class PandoraBiome
 {
 	public SimplexNoiseGenerator noise;
 	public double minTemperature, maxTemperature, minHumidity, maxHumidity;
 	private Block highest;
 	private boolean spawnable;
 	private String msg;
 	private int xs, zs;
 	private double cnoise;
 	private World world;
 
 	public boolean canSpawn(int x, int z) {
 		highest = world.getBlockAt(x, world.getHighestBlockYAt(x, z), z);
 
 		switch(world.getEnvironment()) {
 			case NETHER:
 				highest = null;
 				return true;
 			case THE_END:
 				spawnable = true;
 				spawnable = ((highest.getType() != Material.AIR) ? spawnable : false);
 				spawnable = ((highest.getType() != Material.WATER) ? spawnable : false);
 				spawnable = ((highest.getType() != Material.LAVA) ? spawnable : false);
 				highest = null;
 				return spawnable;
 			case NORMAL:
 			default:
 				spawnable = false;
 				spawnable = ((highest.getType() != Material.SAND) ? spawnable : true);
 				spawnable = ((highest.getType() != Material.GRAVEL) ? spawnable : true);
 				highest = null;
 				return spawnable;
 		}
 	}
 
 	@Deprecated
 	public byte[] generate(Random random, int x, int z) {
 		msg = "Custom generator is missing required methods: ";
 		msg += "generate(), generateSections(), and generateExtSections()";
 		throw new UnsupportedOperationException(msg);
 	}
 
 	public short[] generateExtSections(Random random, int x, int z, BiomeGrid biomes) {
 		return null;
 	}
 
 	public byte[] generateSections(Random random, int x, int z, BiomeGrid biomes) {
 		return null;
 	}
 
 	public int getNoise(int x, int z, double range, double scale, int octaves, double amplitude, double frequency) {
 		range /= 2;
 		xs = (int) Math.round(x / scale);
 		zs = (int) Math.round(z / scale);
 		cnoise = noise.getNoise(xs, zs, octaves, frequency, amplitude);
 		return (int) ((range * cnoise) + range);
 	}
 
 	public double getTemperature(int x, int z) {
 		if(world.getGenerator() == null || !(world.getGenerator() instanceof PandoraGenerator))
 			return world.getTemperature(x, z);
 
 		return ((PandoraGenerator) world.getGenerator()).getTemperature(world, x, z);
 	}
 
 	public double getHumidity(int x, int z) {
 		if(world.getGenerator() == null || !(world.getGenerator() instanceof PandoraGenerator))
 			return world.getHumidity(x, z);
 
 		return ((PandoraGenerator) world.getGenerator()).getHumidity(world, x, z);
 	}
 
 	public void synchronize(World world) {
 		if(this.world == world)
 			return;
 
 		this.world = world;
 		this.noise = new SimplexNoiseGenerator(world.getSeed());
 	}
 }
