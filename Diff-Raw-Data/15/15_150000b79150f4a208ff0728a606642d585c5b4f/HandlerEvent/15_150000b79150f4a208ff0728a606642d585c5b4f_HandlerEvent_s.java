 package loecraftpack.common.logic;
 
 import loecraftpack.LoECraftPack;
 import loecraftpack.common.blocks.BlockProtectionMonolith;
 import loecraftpack.common.blocks.TileProtectionMonolith;
 import loecraftpack.common.worldgen.BiomeDecoratorEverFree;
 import loecraftpack.packet.PacketHelper;
 import loecraftpack.packet.PacketIds;
 import loecraftpack.ponies.abilities.mechanics.MechanicHiddenOres;
 import loecraftpack.ponies.abilities.mechanics.MechanicTreeBucking;
 import net.minecraft.block.Block;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.world.biome.BiomeGenBase;
 import net.minecraft.world.chunk.Chunk;
 import net.minecraft.world.gen.feature.WorldGenMinable;
 import net.minecraft.world.gen.feature.WorldGenerator;
 import net.minecraftforge.client.event.ClientChatReceivedEvent;
 import net.minecraftforge.event.Event.Result;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.player.BonemealEvent;
 import net.minecraftforge.event.entity.player.FillBucketEvent;
 import net.minecraftforge.event.entity.player.PlayerInteractEvent;
 import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
 import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
 import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate;
 import cpw.mods.fml.common.network.PacketDispatcher;
 
 public class HandlerEvent
 {
 	/*@ForgeSubscribe
 	public void onJump(LivingJumpEvent event)
 	{
 		event.entityLiving.motionY += 1;
 	}*/
 	
 	@ForgeSubscribe
 	public void onBlock(PlayerInteractEvent event)
 	{
 		int x = event.x, z = event.z;
 		if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)
 		{
 			switch(event.face)
 			{
 				case 2:
 					z --;
 					break;
 				case 3:
 					z ++;
 					break;
 				case 4:
 					x --;
 					break;
 				case 5:
 					x ++;
 					break;
 			}
 		}
 		for(TileProtectionMonolith te : BlockProtectionMonolith.monoliths)
 		{
 			if (te.xCoord == event.x && te.yCoord == event.y && te.zCoord == event.z)
 				PacketDispatcher.sendPacketToServer(PacketHelper.Make("loecraftpack", PacketIds.monolithUpdate, event.x, event.y, event.z));
 			if (te.Owners.size() > 0)
 			{
 				if (!te.Owners.contains(event.entityPlayer.username) && te.pointIsProtected(x, z))
 				{
 					//event.entityPlayer.skinUrl = "http://skins.minecraft.net/MinecraftSkins/" + StringUtils.stripControlCodes("Tekner") + ".png";
 					//Minecraft.getMinecraft().renderEngine.obtainImageData(event.entityPlayer.skinUrl, new ImageBufferDownload());
 					//event.setCanceled(true);
 					
					//event.entityPlayer.setMoveForward(5);
 					event.entityPlayer.travelToDimension(8);
 				}
 			}
 			else if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && event.entityPlayer.getDistanceSq(te.xCoord, te.yCoord, te.zCoord) <= 100)
 			{
 				te.Owners.add(event.entityPlayer.username);
 				PacketDispatcher.sendPacketToServer(PacketHelper.Make("loecraftpack", PacketIds.monolithSetOwner, te.xCoord, te.yCoord, te.zCoord, te.getOwners()));
 				PacketDispatcher.sendPacketToServer(PacketHelper.Make("loecraftpack", PacketIds.monolithUpdate, event.x, event.y, event.z));
 			}
 		}
 	}
 	
 	@ForgeSubscribe
 	public void onBucket(FillBucketEvent event)
 	{
 		int x = event.target.blockX, z = event.target.blockZ;
 		
 		if (event.target.typeOfHit.ordinal() == 0)
 		{
 			switch(event.target.sideHit)
 			{
 				case 2:
 					z --;
 					break;
 				case 3:
 					z ++;
 					break;
 				case 4:
 					x --;
 					break;
 				case 5:
 					x ++;
 					break;
 			}
 		}
 		for(TileProtectionMonolith te : BlockProtectionMonolith.monoliths)
 		{
 			if (te.Owners.size() > 0)
 			{
 				if (!te.Owners.contains(event.entityPlayer.username) && te.pointIsProtected(x, z))
 					event.setCanceled(true);
 			}
 		}
 	}
 	
 	@ForgeSubscribe
 	public void onChatReceived(ClientChatReceivedEvent event)
 	{
 		if (event.message.equals("npcchat"))
 			event.setCanceled(true);
 		else if (event.message.length() > 0)
 		{
 			if (event.message.substring(0, 1).equals("`"))
 			{
 				event.setCanceled(true);
 				LogicDialog.AddMessage(event.message.substring(1).replaceAll("\n", "\n").split("\\`"));
 			}
 			else if (event.message.equals("next") || event.message.equals("done") || event.message.equals("accept") || event.message.equals("decline"))
 				event.setCanceled(true);
 		}
 	}
 	
 	@ForgeSubscribe
 	public void onBonemeal(BonemealEvent event)
 	{
 		if (!event.world.isRemote)
 		{
 			//event use on Apple-Bloom Sapling
 			if (event.world.getBlockId(event.X, event.Y, event.Z) == LoECraftPack.blockAppleBloomSapling.blockID)
 			{
 			    if ((double)event.world.rand.nextFloat() < 0.45D)
 			    {
 			    	LoECraftPack.blockAppleBloomSapling.grow(event.world, event.X, event.Y, event.Z, event.world.rand);
 			    }
 			    event.setResult(Result.ALLOW);
 			}
 			
 			//event use on Zap-Apple Sapling
 			if (event.world.getBlockId(event.X, event.Y, event.Z) == LoECraftPack.blockZapAppleSapling.blockID)
 			{
 			    if ((double)event.world.rand.nextFloat() < 0.45D)
 			    {
 			    	LoECraftPack.blockZapAppleSapling.grow(event.world, event.X, event.Y, event.Z, event.world.rand);
 			    }
 			    event.setResult(Result.ALLOW);
 			}
 			
 			//testing event: use on AppleLogs
 			if (event.world.getBlockId(event.X, event.Y, event.Z) == LoECraftPack.blockZapAppleLog.blockID ||
 				event.world.getBlockId(event.X, event.Y, event.Z) == LoECraftPack.blockAppleBloomLog.blockID)
 			{
 				System.out.println("BUCK");
 				MechanicTreeBucking.buckTree(event.world, event.X, event.Y, event.Z, 0/*fortune*/);
 			    event.setResult(Result.ALLOW);
 			}
 		}
 	}
 	
 	
 	@ForgeSubscribe
 	public void onPlayerInteract(PlayerInteractEvent event)
 	{
 		EntityPlayer entityPlayer = event.entityPlayer;
 		System.out.print("click - ");
 		//test code
 		if (entityPlayer.getHeldItem() != null && entityPlayer.getHeldItem().itemID == LoECraftPack.itemPickaxeGem.itemID && event.action != Action.LEFT_CLICK_BLOCK)
 		{
 			MechanicHiddenOres.switchHiddenOreRevealState(entityPlayer);
 		}
 		if (entityPlayer.getHeldItem() != null && entityPlayer.getHeldItem().itemID == LoECraftPack.itemPickaxeGem.itemID && event.action == Action.RIGHT_CLICK_BLOCK)
 		{
 			int countR = 0;
 			int countC = 0;
 			Chunk chunk = entityPlayer.worldObj.getChunkFromChunkCoords(entityPlayer.chunkCoordX, entityPlayer.chunkCoordZ);
 			for (int y = 0; y<256; y++)
 			{
 				for (int x = 0; x<16; x++)
 				{
 					for (int z = 0; z<16; z++)
 					{
 						if (chunk.getBlockID(x, y, z)==LoECraftPack.blockGemOre.blockID)
 						{
 							if (chunk.getBlockMetadata(x, y, z)>7)
 								countR++;
 							else
 								countC++;
 						}
 					}
 				}
 			}
 			System.out.println("common ores: "+countC);
 			System.out.println("rare   ores: "+countR);
 		}
 		System.out.println();
 	}
 	
 	
 	/*
 	 * NOTE (WorldGenMinable):
 	 * the value number is actually 2 greater than the amount that can be generated in a cluster
 	 * so compensate for that.
 	 */
 	@ForgeSubscribe
 	public void onDecorateWorldPre(DecorateBiomeEvent.Pre event)
 	{
 		BiomeGenBase biome = event.world.getBiomeGenForCoords(event.chunkX, event.chunkZ);
 		
 		if(biome.biomeID != BiomeGenBase.hell.biomeID)
 		{
 			//rare
 			generateOre(4, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID, 10, 3, Block.stone.blockID), 17, 128);//Laughter
 			generateOre(4, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID, 11, 3, Block.stone.blockID), 17, 128);//Generosity
 			generateOre(4, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID, 12, 3, Block.stone.blockID), 17, 128);//Kindness
 			generateOre(4, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID, 13, 3, Block.stone.blockID), 17, 128);//Magic
 			generateOre(4, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID, 14, 3, Block.stone.blockID), 17, 128);//Loyalty
 			generateOre(4, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID, 15, 3, Block.stone.blockID), 17, 128);//Honesty
 			
 			generateOre(4, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID,  9, 3, Block.stone.blockID), 17, 128);//Heart
 			generateOre(6, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID,  8, 3, Block.stone.blockID), 17, 128);//Tom
 		}
 	}
 	
 	@ForgeSubscribe
 	public void onDecorateWorldPost(DecorateBiomeEvent.Post event)
 	{
 		BiomeGenBase biome = event.world.getBiomeGenForCoords(event.chunkX, event.chunkZ);
 		
 		if(biome.biomeID != BiomeGenBase.hell.biomeID)
 		{
 			//Common
 			generateOre(10, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID, 0, 6, Block.stone.blockID), 5, 128);//Sapphire
 			generateOre(10, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID, 1, 4, Block.stone.blockID), 5, 128);//Fire Ruby
 			generateOre(10, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID, 2, 4, Block.stone.blockID), 5, 128);
 			generateOre(10, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID, 3, 4, Block.stone.blockID), 5, 128);
 			generateOre(10, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID, 4, 4, Block.stone.blockID), 5, 128);
 			generateOre(10, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID, 5, 4, Block.stone.blockID), 5, 128);
 			generateOre(10, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID, 6, 4, Block.stone.blockID), 5, 128);
 			generateOre(10, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID, 7, 6, Block.stone.blockID), 5, 128);//Onyx
 			
 			//random apple blooms
 			if (biome.biomeID != 0 && biome.biomeID != BiomeGenBase.desert.biomeID && biome.biomeID != BiomeGenBase.frozenOcean.biomeID)
 			{
 				if (event.rand.nextInt(20)==0)
 		        {
 		            int k = event.chunkX + event.rand.nextInt(16) + 8;
 		            int l = event.chunkZ + event.rand.nextInt(16) + 8;
 		            WorldGenerator worldgenerator = LoECraftPack.worldGeneratorAppleBloom;
 		            worldgenerator.setScale(1.0D, 1.0D, 1.0D);
 		            worldgenerator.generate(event.world, event.rand, k, event.world.getHeightValue(k, l), l);
 		        }
 			}
 		}
 	}
 	
 	protected void generateOre(int sets, DecorateBiomeEvent event, WorldGenerator worldGenerator, int minHeight, int maxHeight)
 	{
 		for(int i=0; i<sets; i++)
 		{
 			int x = event.chunkX + event.rand.nextInt(16);
 	        int y = event.rand.nextInt(maxHeight - minHeight) + minHeight;
 	        int z = event.chunkZ + event.rand.nextInt(16);
 	        worldGenerator.generate(event.world, event.rand, x, y, z);
 		}
 	}
 	
 	
 	
 	
 	//the following cannot be called because forge hasn't enable the terrain_gen bus (the Dastards)
 	@ForgeSubscribe
 	public void onDecorateTree(Decorate event)
 	{
 		BiomeGenBase biome = event.world.getBiomeGenForCoords(event.chunkX, event.chunkZ);
 		
 		if (biome.biomeID == LoECraftPack.biomeGeneratorEverFreeForest.biomeID && event.type == Decorate.EventType.TREE)
 		{
 			if (BiomeDecoratorEverFree.growZapApples(event.world, event.rand, event.chunkX, event.chunkZ))
 			{
 				event.setResult(Result.DENY);
 				int i = biome.theBiomeDecorator.treesPerChunk;
 				
 				for (int j = 0; j < i; ++j)
 		        {
 		            int k = event.chunkX + event.rand.nextInt(16) + 8;
 		            int l = event.chunkZ + event.rand.nextInt(16) + 8;
 		            WorldGenerator worldgenerator = LoECraftPack.worldGeneratorZapAppleForest;
 		            worldgenerator.setScale(1.0D, 1.0D, 1.0D);
 		            worldgenerator.generate(event.world, event.rand, k, event.world.getHeightValue(k, l), l);
 		        }
 				 
 			}
 			
 		}
 	}
 }
