 /*
  * This file is part of VanillaClassic.
  *
  * Copyright (c) 2012 - 2013, Greatman <http://www.github.com/greatman/>
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
 
 import java.util.Random;
 
 import org.spout.api.generator.LayeredWorldGenerator;
 import org.spout.api.geo.World;
 import org.spout.api.geo.discrete.Point;
 import com.greatmancode.vanillaclassic.material.VanillaClassicMaterials;
 import com.greatmancode.vanillaclassic.world.generator.VanillaClassicGenerator;
 
 public class FlatGenerator extends LayeredWorldGenerator implements VanillaClassicGenerator {
 	public FlatGenerator(int height) {
 		this.setFloorLayer(VanillaClassicMaterials.BEDROCK);
 		this.addLayer(0, height - 1, VanillaClassicMaterials.DIRT);
 		this.stackLayer(1, VanillaClassicMaterials.GRASS);
 	}
 
 	@Override
 	public String getName() {
 		return "VanillaFlat";
 	}
 
 	@Override
 	public Point getSafeSpawn(World world) {
 		final Random random = new Random();
 		final int x = 16 - random.nextInt(32);
 		final int z = 16 - random.nextInt(32);
		int y = world.getHeight() - 1;
 		for (; !world.getBlockMaterial(x, y, z).isSolid(); y--) {
 		}
 		return new Point(world, x, y + 1.5f, z);
 	}
 }
