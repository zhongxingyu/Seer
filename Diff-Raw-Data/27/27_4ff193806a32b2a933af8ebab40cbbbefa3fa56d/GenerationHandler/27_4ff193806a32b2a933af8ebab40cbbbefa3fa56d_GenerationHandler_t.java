 package com.cgm616.LoremIpsum.core;
 
 import java.util.Random;
 
 import com.cgm616.LoremIpsum.block.ModBlocks;
 
 import net.minecraft.world.World;
 import net.minecraft.world.chunk.IChunkProvider;
 import net.minecraft.world.gen.feature.WorldGenMinable;
 import cpw.mods.fml.common.IWorldGenerator;
 
 public class GenerationHandler implements IWorldGenerator{
 
 	@Override
 	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
 		switch(world.provider.dimensionId)
 		{
 		case -1: generateNether(world, random, chunkX*16,chunkZ*16);
 		case 0: generateSurface(world, random, chunkX*16,chunkZ*16);
 		case 1: generateEnd(world, random, chunkX*16,chunkZ*16);
 		}
 	}
 
 	private void generateEnd(World world, Random random, int i, int j) {
 
 	}
 
 	private void generateNether(World world, Random random, int i, int j) {
 
 	}
 
 	private void generateSurface(World world, Random random, int blockX, int blockZ)
 	{
		// Corundum
		for(int i = 0; i < 1; i++)
 		{
 			int xCoord = blockX + random.nextInt(16);
 			int yCoord = random.nextInt(16);
 			int zCoord = blockZ + random.nextInt(16);
 			(new WorldGenMinable(ModBlocks.oreCorundum.blockID, 2)).generate(world,random,xCoord,yCoord,zCoord);
 		}
		
		// Silver
		for(int j = 0; j < 6; j++)
		{
			int xCoord = blockX + random.nextInt(16);
			int yCoord = random.nextInt(44);
			int zCoord = blockZ + random.nextInt(16);
			(new WorldGenMinable(ModBlocks.oreSilver.blockID, 6)).generate(world,random,xCoord,yCoord,zCoord);
		}
 	}
 	
 	
 
 }
