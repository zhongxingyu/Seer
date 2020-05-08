 /*
  * This file is part of VanillaClassic.
  *
  * Copyright (c) 2012, Greatman <http://www.github.com/greatman/>
  * VanillaClassic is licensed under the SpoutDev License Version 1.
  *
  * VanillaClassic is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * In addition, 180 days after any changes are published, you can use the
  * software, incorporating those changes, under the terms of the MIT license,
  * as described in the SpoutDev License Version 1.
  *
  * VanillaClassic is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License,
  * the MIT license and the SpoutDev License Version 1 along with this program.
  * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
  * License and see <http://www.spout.org/SpoutDevLicenseV1.txt> for the full license,
  * including the MIT license.
  */
 package com.greatmancode.vanillaclassic.world.generator.flat;
 
 import org.spout.api.generator.GeneratorPopulator;
 import org.spout.api.generator.Populator;
 import org.spout.api.geo.World;
 import org.spout.api.geo.cuboid.Chunk;
 import org.spout.api.geo.discrete.Point;
 import org.spout.api.util.cuboid.CuboidShortBuffer;
 
 import com.greatmancode.vanillaclassic.material.VanillaClassicMaterials;
 import com.greatmancode.vanillaclassic.world.generator.VanillaClassicGenerator;
 
 public class FlatGenerator implements VanillaClassicGenerator {
 
 	private final int height;
 
 	public FlatGenerator(int height) {
 		this.height = height;
 	}
 
 	@Override
	public void generate(CuboidShortBuffer blockData, int chunkX, int chunkY, int chunkZ, World world) {
 		//Thanks DDoS
 		int x = chunkX << 4, z = chunkZ << 4;
 		for (int dx = x; dx < x + 16; ++dx) {
 			for (int dz = z; dz < z + 16; ++dz) {
 				final int startY = chunkY << Chunk.BLOCKS.BITS;
 				final int endY = Math.min(Chunk.BLOCKS.SIZE + startY, height);
 				for (int y = startY; y < endY; y++) {
 					if (y <= 0) {
						blockData.set(dx, y, dz, VanillaClassicMaterials.BEDROCK.getId());
 					} else if (y == height - 1) {
						blockData.set(dx, y, dz, VanillaClassicMaterials.GRASS.getId());
 					} else {
						blockData.set(dx, y, dz, VanillaClassicMaterials.DIRT.getId());
 					}
 				}
 			}
 		}
 
 	}
 
 	@Override
 	public Populator[] getPopulators() {
 		return new Populator[0];
 	}
 
 	@Override
 	public GeneratorPopulator[] getGeneratorPopulators() {
 		return new GeneratorPopulator[0];
 	}
 
 	@Override
 	public String getName() {
 		return "FlatGenerator";
 	}
 
 	@Override
 	public Point getSafeSpawn(World world) {
 		return new Point(world, 0, height, 0);
 	}
 
 	@Override
 	public int[][] getSurfaceHeight(World world, int chunkX, int chunkZ) {
 		int[][] heights = new int[Chunk.BLOCKS.SIZE][Chunk.BLOCKS.SIZE];
 		for (int x = 0; x < Chunk.BLOCKS.SIZE; x++) {
 			for (int z = 0; z < Chunk.BLOCKS.SIZE; z++) {
 				heights[x][z] = height - 1;
 			}
 		}
 		return heights;
 	}
 
 }
