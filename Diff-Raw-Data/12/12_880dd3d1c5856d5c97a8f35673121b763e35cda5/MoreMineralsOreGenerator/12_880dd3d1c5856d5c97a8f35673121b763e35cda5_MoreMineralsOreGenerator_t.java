 package clashsoft.mods.moreminerals;
 
 import java.util.Random;
 
 import clashsoft.clashsoftapi.util.CSArray;
 import clashsoft.clashsoftapi.util.CSRandom;
 import clashsoft.clashsoftapi.util.CSUtil;
 import clashsoft.mods.moretools.MoreToolsMod_Tools;
 import net.minecraft.block.Block;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.World;
 import net.minecraft.world.WorldProviderEnd;
 import net.minecraft.world.chunk.IChunkProvider;
 import net.minecraft.world.gen.feature.WorldGenMinable;
 import cpw.mods.fml.common.IWorldGenerator;
 
 public class MoreMineralsOreGenerator implements IWorldGenerator
 {
 	@Override
 	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
 	{
 		if (world.provider.isSurfaceWorld())
 			generateSurface(world, chunkX, chunkZ, random);
 		else if (world.provider instanceof WorldProviderEnd)
 			generateEnd(world, chunkX, chunkZ, random);
 		else if (world.provider.isHellWorld)
 			generateNether(world, chunkX, chunkZ, random);
 	}
 
 	public void generateSurface(World world, int chunkX, int chunkZ, Random random)
 	{
 		//More Minerals Stone, Dirt and Sand Ores
 		for (int i = 0; i < MoreMineralsMod.gentypes.length; i++)
 		{
 			int maxHeight = MoreMineralsMod.gentypes[i]; //height
 			if (maxHeight > 0 && !MoreMineralsMod.allnames[i].contains("%&"))
 			{
 				int amountPerChunk = (int)(MoreMineralsMod.gentypes[i] / 2);
 				int amountPerVeign = (int)(MathHelper.sqrt_double(MoreMineralsMod.gentypes[i]) * 1.5D);
 				int stoneID = MoreMineralsHelper.getOreFromMetadata(i, "stone").blockID;
 				int dirtID = MoreMineralsHelper.getOreFromMetadata(i, "dirt").blockID;
 				int sandID = MoreMineralsHelper.getOreFromMetadata(i, "sand").blockID;
 				int meta = i < 16 ? i : i - 16;
 
 				for(int j = 0; j < amountPerChunk; j++)
 				{
 					int randPosX = chunkX * 16 + random.nextInt(16);
 					int randPosY = random.nextInt(maxHeight);
 					int randPosZ = chunkZ * 16 + random.nextInt(16);
 					(new WorldGenMinable(stoneID, meta, amountPerVeign, Block.stone.blockID)).generate(world, random, randPosX, randPosY, randPosZ);
 				}
 				for (int j = 0; j < amountPerChunk / 4; j++)
 				{
 					int randPosX = chunkX * 16 + random.nextInt(16);
 					int randPosY = random.nextInt(maxHeight);
 					int randPosY2 = CSRandom.nextInt(random, 48, 80); //Sand wont generate lower than that.
 					int randPosY3 = CSRandom.nextInt(random, 5, 128);
 					int randPosZ = chunkZ * 16 + random.nextInt(16);
 					if (maxHeight >= 36) //Only common ores generate as dirt and sand ores.
 						(new WorldGenMinable(sandID, meta, amountPerVeign / 3, Block.sand.blockID)).generate(world, random, randPosX, randPosY2, randPosZ);
 
 					if (maxHeight >= 42)
 						(new WorldGenMinable(dirtID, meta, amountPerVeign / 2, Block.dirt.blockID)).generate(world, random, randPosX, randPosY3, randPosZ);
 				}
 			}
 		}
 
 		//Vanilla Dirt + Sand Ores
 		for (int i = 0; i < MoreMineralsMod.vanillagentypes.length; i++)
 		{
 			int maxHeight = MoreMineralsMod.vanillagentypes[i]; //height
 			int amountPerChunk = (int)(MoreMineralsMod.vanillagentypes[i] / 2);
 			int amountPerVeign = (int)(MathHelper.sqrt_double(MoreMineralsMod.vanillagentypes[i]) * 1.5D);
 			int blockID = MoreMineralsMod.vanillaSpecialOres2.blockID;
 			int meta = i;
 
 			for(int j = 0; j < amountPerChunk / 4; j++)
 			{
 				int randPosX = chunkX * 16 + random.nextInt(16);
 				int randPosY = random.nextInt(maxHeight);
 				int randPosY2 = CSRandom.nextInt(random, 48, 80); //Sand wont generate lower than that.
 				int randPosZ = chunkZ * 16 + random.nextInt(16);
 				if (maxHeight >= 32) //Only common ores generate as dirt and sand ores.
 					(new WorldGenMinable(blockID, meta + 7, amountPerVeign / 3, Block.sand.blockID)).generate(world, random, randPosX, randPosY2, randPosZ);
 
 				if (maxHeight >= 36)
 					(new WorldGenMinable(blockID, meta, amountPerVeign / 2, Block.dirt.blockID)).generate(world, random, randPosX, randPosY, randPosZ);
 			}
 		}
 	}
 
 	public void generateNether(World world, int chunkX, int chunkZ, Random random)
 	{
 		for (int i = 0; i < MoreMineralsMod.gentypes.length; i++)
 		{
 			if (MoreMineralsMod.gentypes[i] > 0 && !MoreMineralsMod.allnames[i].contains("%&"))
 			{
 			int maxHeight = MoreMineralsMod.gentypes[i] / 2 + 4; //height
 			int amountPerChunk = (int)(MoreMineralsMod.gentypes[i] / 3);
 			int amountPerVeign = (int)(MathHelper.sqrt_double(MoreMineralsMod.gentypes[i]) * 1.75D);
 			int netheroreID = i < 16 ? MoreMineralsMod.netherOres1.blockID : MoreMineralsMod.netherOres2.blockID;
 			int meta = i < 16 ? i : i - 16;
 
 			for(int j = 0; j < amountPerChunk; j++)
 			{
 				int randPosX = chunkX * 16 + random.nextInt(16);
 				int randPosY = random.nextInt(maxHeight > 128 ? 128 : maxHeight);
 				int randPosZ = chunkZ * 16 + random.nextInt(16);
 				(new WorldGenMinable(netheroreID, meta, amountPerVeign, Block.netherrack.blockID)).generate(world, random, randPosX, randPosY, randPosZ);	
 			}
 			}
 		}
 
 		//Vanilla Nether Ores
 		for (int i = 0; i < MoreMineralsMod.vanillagentypes.length; i++)
 		{
 			int maxHeight = MoreMineralsMod.vanillagentypes[i] / 2 + 4; //height
 			int amountPerChunk = (int)(MoreMineralsMod.gentypes[i] / 3);
 			int amountPerVeign = (int)(MathHelper.sqrt_double(MoreMineralsMod.gentypes[i]) * 1.8D);
 			int blockID = MoreMineralsMod.vanillaSpecialOres1.blockID;
 			int meta = i;
 
 			for(int j = 0; j < amountPerChunk; j++)
 			{
 				int randPosX = chunkX * 16 + random.nextInt(16);
 				int randPosY = random.nextInt(maxHeight > 128 ? 128 : maxHeight);
 				int randPosZ = chunkZ * 16 + random.nextInt(16);
 				(new WorldGenMinable(blockID, meta, amountPerVeign, Block.netherrack.blockID)).generate(world, random, randPosX, randPosY, randPosZ);	
 			}
 		}
 	}
 
 	public void generateEnd(World world, int chunkX, int chunkZ, Random random)
 	{
 		//More Minerals End Ores
 		for (int i = 0; i < MoreMineralsMod.gentypes.length; i++)
 		{
 			if (MoreMineralsMod.gentypes[i] > 0 && !MoreMineralsMod.allnames[i].contains("%&") && MoreMineralsMod.gentypes[i] <= 32)
 			{
 				int maxHeight = MoreMineralsMod.gentypes[i] * 2 + 5; //height
 				int amountPerChunk = (int)(MoreMineralsMod.gentypes[i] / 2);
 				int amountPerVeign = (int)(MathHelper.sqrt_double(MoreMineralsMod.gentypes[i]) * 0.8D);
 				int endoreID = i < 16 ? MoreMineralsMod.endOres1.blockID : MoreMineralsMod.endOres2.blockID;
 				int meta = i < 16 ? i : i - 16;
 
 				for(int j = 0; j < amountPerChunk; j++)
 				{
 					int randPosX = chunkX * 16 + random.nextInt(16);
 					int randPosY = random.nextInt(maxHeight > 128 ? 128 : maxHeight);
 					int randPosZ = chunkZ * 16 + random.nextInt(16);
 					(new WorldGenMinable(endoreID, meta, amountPerVeign, Block.whiteStone.blockID)).generate(world, random, randPosX, randPosY, randPosZ);	
 				}
 			}
 		}
 
 		//Vanilla End Ores
		for (int i = 0; i < MoreMineralsMod.vanillagentypes.length; i++)
 		{
			if (MoreMineralsMod.vanillagentypes[i] <= 32)
 			{
				int maxHeight = MoreMineralsMod.vanillagentypes[i] * 2 + 20; //height
				int amountPerChunk = (int)(MoreMineralsMod.vanillagentypes[i] / 2);
				int amountPerVeign = (int)(MathHelper.sqrt_double(MoreMineralsMod.vanillagentypes[i]));
 				int blockID = MoreMineralsMod.vanillaSpecialOres1.blockID;
 				int meta = i + 7;
 
 				for(int j = 0; j < amountPerChunk; j++)
 				{
 					int randPosX = chunkX * 16 + random.nextInt(16);
 					int randPosY = random.nextInt(maxHeight > 128 ? 128 : maxHeight);
 					int randPosZ = chunkZ * 16 + random.nextInt(16);
 					(new WorldGenMinable(blockID, meta, amountPerVeign, Block.whiteStone.blockID)).generate(world, random, randPosX, randPosY, randPosZ);	
 				}
 			}
 		}
 	}
 }
