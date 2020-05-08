 package zh.usefulthings.worldgen;
 
 import java.util.Random;
 
 import scala.Console;
 
 import zh.usefulthings.UsefulThings;
 
 import net.minecraft.block.Block;
 import net.minecraft.world.World;
 import net.minecraft.world.biome.BiomeGenBase;
 import net.minecraft.world.chunk.IChunkProvider;
 import net.minecraft.world.gen.feature.WorldGenMinable;
 import net.minecraftforge.common.BiomeDictionary;
 import cpw.mods.fml.common.IWorldGenerator;
 
 public class ZHWorldGen implements IWorldGenerator 
 {
 	private static final int seaLevel = 63;
 
 	@Override
 	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) 
 	{
 		//switch(world.provider.dimensionId)
 		//create and use a dimension blacklist here...
 		generateOverworld(world,random, chunkX * 16, chunkZ * 16);
 		
 	}
 
 	private void generateOverworld(World world, Random rand, int x, int z) 
 	{	
 		
 		BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
 		
 		if(UsefulThings.dimBlacklist.contains(world.provider.dimensionId))
 			return;
 		
 		if(UsefulThings.worldTypeBlacklist.contains(world.getWorldInfo().getTerrainType().getWorldTypeName()))
 			return;
 		
 		//Generate my ores
 		for(int i = 0; i < UsefulThings.gen.length; i++)
 		{ 
 			if (UsefulThings.gen[i])
 			{
 				for(int j=0; j < UsefulThings.numVeins[i]; j++) 
 				{ 
 					int posX = x + rand.nextInt(16); //X coordinate to gen at
 					int posY = rand.nextInt(UsefulThings.maxY[i] - UsefulThings.minY[i]) + UsefulThings.minY[i]; //Y coordinate to gen at
 					int posZ = z + rand.nextInt(16); //Z coordinate to gen at
 					//id,meta,max size,target
 					new WorldGenMinable(UsefulThings.zhOres.blockID,i,UsefulThings.veinSize[i],Block.stone.blockID).generate(world, rand, posX, posY, posZ);
 				}
 			}
 			
 		}
 		if(UsefulThings.genClay && !BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.SWAMP))
 		{
 			for(int i = 0; i < 10; i++)
 			{
 				int posX = x + rand.nextInt(16); //X coordinate to gen at
 				int posY = rand.nextInt(10) + 45; //Y coordinate to gen at
 				int posZ = z + rand.nextInt(16); //Z coordinate to gen at
 				//id,max size,target
 				new WorldGenMinable(Block.blockClay.blockID,20,Block.dirt.blockID).generate(world, rand, posX, posY, posZ);
 			}
 		}
 		else if(UsefulThings.genClay)
 		{
 			for(int i = 0; i < 10; i++)
 			{
 				int posX = x + rand.nextInt(16); //X coordinate to gen at
 				int posY = rand.nextInt(10) + 45; //Y coordinate to gen at
 				int posZ = z + rand.nextInt(16); //Z coordinate to gen at
 				//id,max size,target
 				new WorldGenMinable(Block.blockClay.blockID,20,Block.dirt.blockID).generate(world, rand, posX, posY, posZ);
 			}
 			for(int i = 0; i < 20; i++)
 			{
 				int posX = x + rand.nextInt(16); //X coordinate to gen at
 				int posY = rand.nextInt(10) + 50; //Y coordinate to gen at
 				int posZ = z + rand.nextInt(16); //Z coordinate to gen at
 				//id,max size,target
 				new WorldGenMinable(Block.blockClay.blockID,20,Block.sand.blockID).generate(world, rand, posX, posY, posZ);
 			}
 		}
 
 		if(UsefulThings.genCactusFruit && BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.DESERT))
 		{
 			if (rand.nextInt(15) < 1)
 				new ZHWorldGenCactusFruit().generate(world,rand,x,world.getHeightValue(x, z),z);
 		}
 		
 		if(UsefulThings.genMelon)
 		{
 			int rarity = -1;
 			
 			if(BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.WATER)) 
 				rarity = 100;
 			else if (BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.JUNGLE))
 				rarity = 100;
 			else if (BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.PLAINS))
 				rarity = 200;
 			else if (BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.FOREST))
 				rarity = 300;
 			
 			if(rarity > 0 && rand.nextInt(rarity) < 1)
 			{
 				new ZHWorldGenMelon().generate(world, rand, x, world.getHeightValue(x, z), z);
 			}			
 		}
 		
 		if(UsefulThings.genFlax)
 		{
 			int rarity = -1;
 			
 			if (BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.PLAINS))
 				rarity = 50;
 			else if (BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.FOREST))
 				rarity = 100;
 			else if (BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.MOUNTAIN))
 				rarity = 200;
 			
 			if(rarity > 0 && rand.nextInt(rarity) < 1)
 			{
				new ZHWorldGenPlant(UsefulThings.flaxCrop.blockID,64,15,2).generate(world, rand, x, world.getHeightValue(x, z), z);
 			}
 		}
 		
 		
 	}
 		
 	
 
 	
 
 }
