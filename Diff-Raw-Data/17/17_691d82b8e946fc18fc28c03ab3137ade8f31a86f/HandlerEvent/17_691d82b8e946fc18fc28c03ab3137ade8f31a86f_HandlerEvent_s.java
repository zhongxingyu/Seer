 package loecraftpack.common.logic;
 
 import java.util.List;
 
 import loecraftpack.LoECraftPack;
 import loecraftpack.common.blocks.BlockProtectionMonolith;
 import loecraftpack.common.blocks.TileProtectionMonolith;
 import loecraftpack.common.entity.EntityCustomArrow;
 import loecraftpack.common.items.ItemAccessory;
 import loecraftpack.dimensionaltransfer.TeleporterCustom;
 import loecraftpack.enums.LivingEventId;
 import loecraftpack.packet.PacketHelper;
 import loecraftpack.packet.PacketIds;
 import loecraftpack.ponies.abilities.mechanics.MechanicTreeBucking;
 import loecraftpack.ponies.inventory.HandlerExtendedInventoryServer;
 import loecraftpack.proxies.ClientProxy;
 import net.minecraft.block.Block;
 import net.minecraft.enchantment.EnchantmentHelper;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityList;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.projectile.EntityArrow;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.biome.BiomeGenBase;
 import net.minecraft.world.chunk.Chunk;
 import net.minecraft.world.gen.feature.WorldGenMinable;
 import net.minecraft.world.gen.feature.WorldGenerator;
 import net.minecraftforge.client.event.ClientChatReceivedEvent;
 import net.minecraftforge.client.event.RenderWorldLastEvent;
 import net.minecraftforge.event.Event.Result;
 import net.minecraftforge.event.EventPriority;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.living.LivingAttackEvent;
 import net.minecraftforge.event.entity.living.LivingDeathEvent;
 import net.minecraftforge.event.entity.player.ArrowLooseEvent;
 import net.minecraftforge.event.entity.player.BonemealEvent;
 import net.minecraftforge.event.entity.player.FillBucketEvent;
 import net.minecraftforge.event.entity.player.PlayerDropsEvent;
 import net.minecraftforge.event.entity.player.PlayerInteractEvent;
 import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
 import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
 import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
 import net.minecraftforge.event.world.WorldEvent;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class HandlerEvent
 {
 	
 	
 	/*@ForgeSubscribe
 	public void onJump(LivingJumpEvent event)
 	{
 		event.entityLiving.motionY += 1;
 	}*/
 	
 	
 	
 	
   /********************************************************************************************/
  /**  MONOLITH  ******************************************************************************/
 /********************************************************************************************/
 	
 	@ForgeSubscribe
 	public void onMonolithInteract(PlayerInteractEvent event)
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
 		
 		List<TileProtectionMonolith> list = BlockProtectionMonolith.monoliths.get(event.entityPlayer.worldObj.getWorldInfo().getDimension());
 		if (list != null)
 		for(TileProtectionMonolith te : list)
 		{
 			if (te.xCoord == event.x && te.yCoord == event.y && te.zCoord == event.z)
 				PacketDispatcher.sendPacketToServer(PacketHelper.Make("loecraftpack", PacketIds.monolithUpdate, event.x, event.y, event.z));
 			if (te.Owners.size() > 0)
 			{
 				if (!te.Owners.contains(event.entityPlayer.username) && te.pointIsProtected(x, z))
 				{
 					event.setCanceled(true);
 					//event.entityPlayer.skinUrl = "http://skins.minecraft.net/MinecraftSkins/" + StringUtils.stripControlCodes("Tekner") + ".png";
 					//Minecraft.getMinecraft().renderEngine.obtainImageData(event.entityPlayer.skinUrl, new ImageBufferDownload());
 					//event.setCanceled(true);
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
 	
 	
 	
 	
   /********************************************************************************************/
  /**  ABILITIES  *****************************************************************************/
 /********************************************************************************************/
 	
 	@ForgeSubscribe
 	public void onAbilityInteract(PlayerInteractEvent event)
 	{
 		EntityPlayer player = event.entityPlayer;
 		
 		  /*******************/
 		 /**Earth Pony Buck**/
 		/*******************/
 		if (player.getHeldItem()==null && MechanicTreeBucking.canBuck(player))
 		{
 			if (player.worldObj.getBlockId(event.x, event.y, event.z) == LoECraftPack.blockZapAppleLog.blockID ||
 				player.worldObj.getBlockId(event.x, event.y, event.z) == LoECraftPack.blockAppleBloomLog.blockID)
 			{
 				System.out.println("BUCK");
 				MechanicTreeBucking.buckTree(player.worldObj, event.x, event.y, event.z, 0/*Do: BuckTree - fortune*/);
 				if (player.worldObj.isRemote)
 				{
 					//Do: EarthPonyBuck - consume stamina
 				}
 			}
 		}
 	}
 	
 	
 	
 	
   /********************************************************************************************/
  /**  QUEST CHAT  ****************************************************************************/
 /********************************************************************************************/
 	
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
     
 	
 	
 	
   /***********************************************************************************************/
  /**  BONE-MEAL ON CUSTOM SAPPLINGS  ************************************************************/
 /***********************************************************************************************/
 	
 	@ForgeSubscribe
 	public void onBonemeal(BonemealEvent event)
 	{
 		if (!event.world.isRemote)
 		{
 			  /****************************/
 			 /**Grow Apple-Bloom Sapling**/
 			/****************************/
 			
 			if (event.world.getBlockId(event.X, event.Y, event.Z) == LoECraftPack.blockAppleBloomSapling.blockID)
 			{
 			    if ((double)event.world.rand.nextFloat() < 0.45D)
 			    {
 			    	LoECraftPack.blockAppleBloomSapling.grow(event.world, event.X, event.Y, event.Z, event.world.rand);
 			    }
 			    event.setResult(Result.ALLOW);
 			}
 			
 			  /**************************/
 			 /**Grow Zap-Apple Sapling**/
 			/**************************/
 			
 			if (event.world.getBlockId(event.X, event.Y, event.Z) == LoECraftPack.blockZapAppleSapling.blockID)
 			{
 			    if ((double)event.world.rand.nextFloat() < 0.45D)
 			    {
 			    	LoECraftPack.blockZapAppleSapling.grow(event.world, event.X, event.Y, event.Z, event.world.rand);
 			    }
 			    event.setResult(Result.ALLOW);
 			}
 		}
 	}
     
 	
 	
 	
   /***********************************************************************************************/
  /**  TERRAIN GENERATION  ***********************************************************************/
 /***********************************************************************************************/
 	
 	/*
 	 * NOTE (WorldGenMinable):
 	 * the value number is actually 2 greater than the amount that can be generated in a cluster
 	 * so compensate for that.
 	 */
 	
 		  /***************/
 		 /**Hidden Ores**/
 		/***************/
 	@ForgeSubscribe
 	public void onDecorateWorldPre(DecorateBiomeEvent.Pre event)
 	{
 		BiomeGenBase biome = event.world.getBiomeGenForCoords(event.chunkX, event.chunkZ);
 		
 		if(biome.biomeID != BiomeGenBase.hell.biomeID)
 		{
 			//rare - lower elevation
 			generateOre(1, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID, 10, 3, Block.stone.blockID), 17,  40);//Laughter
 			generateOre(1, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID, 11, 3, Block.stone.blockID), 17,  40);//Generosity
 			generateOre(1, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID, 12, 3, Block.stone.blockID), 17,  40);//Kindness
 			generateOre(1, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID, 13, 3, Block.stone.blockID), 17,  40);//Magic
 			generateOre(1, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID, 14, 3, Block.stone.blockID), 17,  40);//Loyalty
 			generateOre(1, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID, 15, 3, Block.stone.blockID), 17,  40);//Honesty
 			
 			//rare - higher elevation
 			generateOre(1, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID, 10, 3, Block.stone.blockID), 41, 128);//Laughter
 			generateOre(1, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID, 11, 3, Block.stone.blockID), 41, 128);//Generosity
 			generateOre(1, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID, 12, 3, Block.stone.blockID), 41, 128);//Kindness
 			generateOre(1, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID, 13, 3, Block.stone.blockID), 41, 128);//Magic
 			generateOre(1, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID, 14, 3, Block.stone.blockID), 41, 128);//Loyalty
 			generateOre(1, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID, 15, 3, Block.stone.blockID), 41, 128);//Honesty
 			
 			//rare - general
 			generateOre(3, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID,  9, 3, Block.stone.blockID), 17, 128);//Heart
 			
 		}
 	}
 	
 	@ForgeSubscribe
 	public void onDecorateWorldPost(DecorateBiomeEvent.Post event)
 	{
 		BiomeGenBase biome = event.world.getBiomeGenForCoords(event.chunkX, event.chunkZ);
 		
 		if(biome.biomeID != BiomeGenBase.hell.biomeID)
 		{
 			//Common
 			generateOre(7, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID,  0, 6, Block.stone.blockID),  5, 128);//Sapphire
 			generateOre(7, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID,  1, 4, Block.stone.blockID),  5, 128);//Fire Ruby
 			generateOre(7, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID,  2, 4, Block.stone.blockID),  5, 128);
 			generateOre(7, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID,  3, 4, Block.stone.blockID),  5, 128);
 			generateOre(7, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID,  4, 4, Block.stone.blockID),  5, 128);
 			generateOre(7, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID,  5, 4, Block.stone.blockID),  5, 128);
 			generateOre(7, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID,  6, 4, Block.stone.blockID),  5, 128);
 			generateOre(7, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID,  7, 6, Block.stone.blockID),  5, 128);//Onyx
 			
 			//Tom
 			generateOre(3, event, new WorldGenMinable(LoECraftPack.blockGemOre.blockID,  8, 5, Block.stone.blockID),  5,  16);//Tom
 			
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
 	
 		  /****************************/
 		 /**Common Generate Ore Vein**/
 		/****************************/
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
     
 	
 	
 	
   /***********************************************************************************************/
  /**  CUSTOM TELEPORTERS  ***********************************************************************/
 /***********************************************************************************************/
 	
 		  /********************************/
 		 /**Build all Custom Teleporters**/
 		/********************************/
 	@ForgeSubscribe
 	public void onWorldLoad(WorldEvent.Load event)
 	{
 		TeleporterCustom.buildTeleporters(event.world);
 	}
 	
 		  /********************************/
 		 /**Clear all Custom Teleporters**/
 		/********************************/
 	@ForgeSubscribe
 	public void onWorldUnload(WorldEvent.Unload event)
 	{
 		TeleporterCustom.clearTeleporters(event.world);
 	}
     
 	
 	
 	
   /********************************************************************************************/
  /**  RENDERS  *******************************************************************************/
 /********************************************************************************************/
 	
 		  /****************************/
 		 /**Render Hidden Ore Vision**/
 		/****************************/
 	@SideOnly(Side.CLIENT)
 	@ForgeSubscribe
 	public void onfinalRender(RenderWorldLastEvent event)
 	{
         ClientProxy.renderHiddenOre.drawBlockPhantomTexture(event);
 	}
     
 	
 	
 	
   /********************************************************************************************/
  /**  CUSTOM ARROWS  *************************************************************************/
 /********************************************************************************************/
 	
 		  /****************************************/
 		 /**Alert Wolves Code, for custom arrows**/
 		/****************************************/
 	@ForgeSubscribe
     public void onLivingAttack(LivingAttackEvent event)
     {
 		if(event.entityLiving instanceof EntityPlayer)
 		{
 	    	Entity entity = event.source.getEntity();
 	        if (entity instanceof EntityCustomArrow && ((EntityCustomArrow)entity).shootingEntity != null)
 	        {
 	            entity = ((EntityArrow)entity).shootingEntity;
 	            
 	            if (entity instanceof EntityLiving)
 		        {
 	            	((EntityPlayer)event.entityLiving).alertWolves((EntityLiving)entity, false);
 		        }
 	        }
 		}
     }
     
 	
 	
 	
   /********************************************************************************************/
  /**  Player Drops Inventory EVENT  **********************************************************/
 /********************************************************************************************/
 	
 	@ForgeSubscribe
 	public void onPlayerLoot(PlayerDropsEvent event)
 	{
 		HandlerExtendedInventoryServer.dropAllitems(event.entityPlayer);
 	}
 	
 	
 	
 	
   /********************************************************************************************/
  /**  Enchantment Effect EVENT  **************************************************************/
 /********************************************************************************************/
 	
 	@ForgeSubscribe
 	public void onAttackEchantmentEffect(LivingAttackEvent event)
 	{
 		Entity sourceEntity = event.source.getEntity();
 		if (sourceEntity!= null && event.entityLiving != null)
 		{
 			int IDEntity = EntityList.getEntityID(event.entityLiving);
 			//System.out.println("attack from "+sourceEntity);
 			//System.out.println("target id"+IDEntity);
 			
 			ItemStack tool = null;
 			
 			if (sourceEntity instanceof EntityLiving)
 			{
 				if (sourceEntity instanceof EntityPlayer)
 				{
 					EntityPlayer attackingPlayer = (EntityPlayer)sourceEntity;
 					tool = attackingPlayer.getHeldItem();
 				}
 			}
 			
 			if (tool != null)
 			{
 				//System.out.println("The tool is "+tool.getItemName());
 				
 				  /****************************************/
 				 /**Handle the effects of each type here**/
 				/****************************************/
 				
 				int electricLevel = EnchantmentHelper.getEnchantmentLevel(LoECraftPack.electricEnchant.effectId, tool);
 				//System.out.println("electric "+electricLevel);
 				
				if (IDEntity == 50 /*Creeper*/)
				{
 					event.entityLiving.getDataWatcher().updateObject(17, Byte.valueOf((byte)1));
				}
 			}
 		}
 	}
 	
 	
 	
 	
   /********************************************************************************************/
  /**  ACCESSORY EVENT  ***********************************************************************/
 /********************************************************************************************/
 	
 		  /*********/
 		 /**Death**/
 		/*********/
 	@ForgeSubscribe(priority=EventPriority.HIGHEST)
 	public void onDeathPreEvent(LivingDeathEvent event)
 	{
 		ItemAccessory.applyLivingEvent(event, LivingEventId.LIVING_DEATH_PRE);
 	}
 	
 	@ForgeSubscribe
 	public void onDeathEvent(LivingDeathEvent event)
 	{
 		ItemAccessory.applyLivingEvent(event, LivingEventId.LIVING_DEATH);
 	}
 	
 		  /*********/
 		 /**Sleep**/
 		/*********/
 	@ForgeSubscribe
 	public void onSleepEvent(PlayerSleepInBedEvent event)
 	{
 		ItemAccessory.applyLivingEvent(event, LivingEventId.PLAYER_SLEEP_IN_BED);
 	}
 	
 		  /***************/
 		 /**Loose Arrow**/
 		/***************/
 	@ForgeSubscribe
 	public void onArrowLoose(ArrowLooseEvent event)
 	{
 		ItemAccessory.applyLivingEvent(event, LivingEventId.ARROW_LOOSE);
 	}
 	
 	
 	
 	
   /********************************************************************************************/
  /**  TEST CODE  *****************************************************************************/
 /********************************************************************************************/
 	
 		  /***********************/
 		 /**Ore Generation Info**/
 		/***********************/
 	@ForgeSubscribe
 	public void onPlayerInteract(PlayerInteractEvent event)
 	{
 		EntityPlayer player = event.entityPlayer;
 		
 		if (player.getHeldItem() != null)
 		{
 			if (player.getHeldItem().itemID == LoECraftPack.itemPickaxeGem.itemID)
 			{
 				if (event.action == Action.RIGHT_CLICK_BLOCK)
 				{
 					//Testing code: ore generation
 					int countR = 0;
 					int countC = 0;
 					Chunk chunk = player.worldObj.getChunkFromChunkCoords(player.chunkCoordX, player.chunkCoordZ);
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
 			}
 			else if (player.getHeldItem().itemID == Item.stick.itemID &&
 					 player.worldObj.getBlockId(event.x, event.y, event.z) == Block.beacon.blockID &&
 					 event.action == Action.LEFT_CLICK_BLOCK)
 			{
 				//testing code: teleport to skyland
 				TeleporterCustom.refreshTeleporter(TeleporterCustom.teleporterSkyLands, LoECraftPack.SkylandDimensionID);
 				TeleporterCustom.teleporterSkyLands.travelToDimension(event.entityPlayer);
 				event.setResult(Result.DENY);
 			}
 		}
 	}
 	
 		  /********************/
 		 /**Check Protection**/
 		/********************/
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
 
 		List<TileProtectionMonolith> list = BlockProtectionMonolith.monoliths.get(event.entityPlayer.worldObj.getWorldInfo().getDimension());
 		if (list != null)
 		for(TileProtectionMonolith te : list)
 		{
 			if (te.Owners.size() > 0)
 			{
 				if (!te.Owners.contains(event.entityPlayer.username) && te.pointIsProtected(x, z))
 					event.setCanceled(true);
 			}
 		}
 	}
 }
