 /*******************************************************************************
  * WorldGeneratorTrees.java
  * Copyright (c) 2013 WildBamaBoy.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  ******************************************************************************/
 
 package arrowsplus.gen;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import biomesoplenty.api.Biomes;
 
 import net.minecraft.world.World;
 import net.minecraft.world.biome.BiomeGenBase;
 import net.minecraft.world.chunk.IChunkProvider;
 import cpw.mods.fml.common.IWorldGenerator;
 
 /**
  * Forge hook for world generation. Does some logic before telling the real tree generator to begin making them.
  */
 public class WorldGeneratorTrees implements IWorldGenerator
 {
 	@Override
 	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) 
 	{
 		BiomeGenBase biome = world.getBiomeGenForCoords(chunkX * 16, chunkZ * 16);
 
 		int coordX = chunkX * 16; 
 		int coordZ = chunkZ * 16;
 
 		if (biome == BiomeGenBase.forest || biome == BiomeGenBase.forestHills || biome == BiomeGenBase.ocean ||
			biome == Biomes.forestHillsNew.get() || biome == Biomes.forestNew.get() || biome == Biomes.woodland.get() ||
			biome == Biomes.mountain.get() || biome == Biomes.meadow.get() || biome == Biomes.shield.get() || biome == Biomes.seasonalForest.get() ||
			biome == Biomes.deciduousForest.get() || biome == Biomes.borealForest.get()
 			)
 		{
 			for (int i = 0; i < 20; i++)
 			{
 				int x2 = coordX + random.nextInt(16);
 				int y2 = random.nextInt(90);
 				int z2 = coordZ + random.nextInt(16);
 
 				List<Integer>validMetas = new ArrayList<Integer>();
 				validMetas.add(0);
 				validMetas.add(1);
 				validMetas.add(2);
 				validMetas.add(3);
 				validMetas.add(4);
 				validMetas.add(5);
 				validMetas.add(6);
 				validMetas.add(9);
 
 				int meta = validMetas.get(random.nextInt(validMetas.size()));
 				
 				if (meta == 5 && random.nextBoolean() && random.nextBoolean() && random.nextBoolean())
 				{
 					//Swap soft maple for hard maple randomly.
 					meta = 8;
 				}
 				
 				new WorldGenArrowTrees(false, 6, meta).generate(world, random, x2, y2, z2);
 			}
 		}
 
		else if (biome == Biomes.mapleWoods.get())
 		{
 			for (int i = 0; i < 20; i++)
 			{
 				int x2 = coordX + random.nextInt(16);
 				int y2 = random.nextInt(90);
 				int z2 = coordZ + random.nextInt(16);
 
 				int meta = 5;
 				
 				if (random.nextBoolean() && random.nextBoolean() && random.nextBoolean())
 				{
 					//Swap soft maple for hard maple randomly.
 					meta = 8;
 				}
 				
 				new WorldGenArrowTrees(false, 6, meta).generate(world, random, x2, y2, z2);
 			}
 		}
 		
		else if (biome == BiomeGenBase.swampland || biome == Biomes.bog.get() ||
				biome == Biomes.swamplandNew.get() || biome == Biomes.deadSwamp.get() ||
				biome == Biomes.wetland.get() || biome == Biomes.lushSwamp.get() || biome == Biomes.fen.get())
 		{
 			for (int i = 0; i < 20; i++)
 			{
 				int x2 = coordX + random.nextInt(16);
 				int y2 = random.nextInt(90);
 				int z2 = coordZ + random.nextInt(16);
 
 				List<Integer>validMetas = new ArrayList<Integer>();
 				validMetas.add(1);
 				validMetas.add(3);
 				validMetas.add(4);
 				validMetas.add(6);
 				validMetas.add(9);
 				validMetas.add(10);
 				
 				int meta = validMetas.get(random.nextInt(validMetas.size()));
 				
 				new WorldGenArrowTrees(false, 6, meta).generate(world, random, x2, y2, z2);
 			}
 		}
 		
 		else if (biome == BiomeGenBase.taiga || biome == BiomeGenBase.taigaHills || 
 				 biome == BiomeGenBase.icePlains || biome == BiomeGenBase.iceMountains ||
				 biome == Biomes.alps.get() || biome == Biomes.alpsBase.get() || 
				 biome == Biomes.alpsForest.get() || biome == Biomes.frostForest.get()
 				 )
 		{
 			for (int i = 0; i < 20; i++)
 			{
 				int x2 = coordX + random.nextInt(16);
 				int y2 = random.nextInt(90);
 				int z2 = coordZ + random.nextInt(16);
 
 				List<Integer>validMetas = new ArrayList<Integer>();
 				validMetas.add(0);
 				validMetas.add(7);
 				validMetas.add(4);
 				validMetas.add(10);
 
 				int meta = validMetas.get(random.nextInt(validMetas.size()));
 				new WorldGenArrowTrees(false, 6, meta).generate(world, random, x2, y2, z2);
 			}
 		}
 
		else if (biome == BiomeGenBase.jungle || biome == BiomeGenBase.jungleHills || biome == Biomes.jungleNew.get() ||
				 biome == Biomes.jungleHillsNew.get() || biome == Biomes.rainforest.get())
 		{
 			if (random.nextBoolean())
 			{
 				for (int i = 0; i < 20; i++)
 				{
 					int x2 = coordX + random.nextInt(16);
 					int y2 = random.nextInt(90);
 					int z2 = coordZ + random.nextInt(16);
 
 					new WorldGenArrowTrees(false, 3, 11).generate(world, random, x2, y2, z2);
 				}
 			}
 		}
 	}
 }
