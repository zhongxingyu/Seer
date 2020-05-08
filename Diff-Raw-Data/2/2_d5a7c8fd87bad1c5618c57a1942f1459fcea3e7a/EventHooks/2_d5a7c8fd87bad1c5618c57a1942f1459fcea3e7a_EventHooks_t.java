 /*******************************************************************************
  * EventHooks.java
  * Copyright (c) 2013 WildBamaBoy.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  ******************************************************************************/
 
 package arrowsplus;
 
 import java.io.File;
 import java.text.DecimalFormat;
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.util.ChatMessageComponent;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.Event.Result;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.player.ArrowNockEvent;
 import net.minecraftforge.event.entity.player.PlayerEvent;
 import net.minecraftforge.event.entity.player.PlayerInteractEvent;
 import net.minecraftforge.event.world.WorldEvent;
 
 /**
  * Contains methods that perform a function when an event in Minecraft occurs.
  */
 public class EventHooks 
 {
 	/**
 	 * Fires when the world is loading. Loads world properties for every single player into memory server side only.
 	 * 
 	 * @param 	event	An instance of the WorldEvent.Load event.
 	 */
 	@ForgeSubscribe
 	public void worldLoadEventHandler(WorldEvent.Load event)
 	{
 		if (!event.world.isRemote && !ArrowsPlus.instance.hasLoadedProperties)
 		{
 			MinecraftServer server = MinecraftServer.getServer();
 
 			if (server.isDedicatedServer())
 			{
 				ArrowsPlus.instance.log("Loading world properties for dedicated server...");
 
 				String worldName = MinecraftServer.getServer().worldServers[0].getSaveHandler().getWorldDirectoryName();
 				File worldPropertiesFolderPath = new File(ArrowsPlus.instance.runningDirectory + "/config/ArrowsPlus/ServerWorlds/" + worldName);
 
 				if (!worldPropertiesFolderPath.exists())
 				{
 					ArrowsPlus.instance.log("Creating folder " + worldPropertiesFolderPath.getPath());
 					worldPropertiesFolderPath.mkdirs();
 				}
 
 				for (File file : worldPropertiesFolderPath.listFiles())
 				{
 					ArrowsPlus.instance.playerWorldManagerMap.put(file.getName(), new WorldPropertiesManager(worldName, file.getName()));
 				}
 			}
 
 			else
 			{
 				ArrowsPlus.instance.log("Loading world properties for integrated server...");
 
 				String worldName = MinecraftServer.getServer().worldServers[0].getSaveHandler().getWorldDirectoryName();
 				File worldPropertiesFolderPath = new File(ArrowsPlus.instance.runningDirectory + "/config/ArrowsPlus/Worlds/" + worldName);
 
 				if (!worldPropertiesFolderPath.exists())
 				{
 					ArrowsPlus.instance.log("Creating folder " + worldPropertiesFolderPath.getPath());
 					worldPropertiesFolderPath.mkdirs();
 				}
 
 				for (File file : worldPropertiesFolderPath.listFiles())
 				{
 					ArrowsPlus.instance.playerWorldManagerMap.put(file.getName(), new WorldPropertiesManager(worldName, file.getName()));
 				}
 			}
 
 			ArrowsPlus.instance.hasLoadedProperties = true;
 		}
 	}
 
 	/**
 	 * Fires when the world is saving.
 	 * 
 	 * @param 	event	An instance of the WorldEvent.Unload event.
 	 */
 	@ForgeSubscribe
 	public void worldSaveEventHandler(WorldEvent.Unload event)
 	{
 		try
 		{
 			for (WorldPropertiesManager manager : ArrowsPlus.instance.playerWorldManagerMap.values())
 			{
 				manager.saveWorldProperties();
 			}
 		}
 
 		catch (Throwable e)
 		{
 			ArrowsPlus.instance.log(e);
 		}
 	}
 
 	/**
 	 * Fires when the player nocks an arrow into a bow.
 	 * 
 	 * @param 	event	An instance of the arrow nock event.
 	 */
 	@ForgeSubscribe
 	public void arrowNockEventHandler(ArrowNockEvent event)
 	{
 		float cameraPitch = event.entityPlayer.cameraPitch;
 	}
 
 	/**
 	 * Fires when the player left or right clicks a block.
 	 * 
 	 * @param 	event	An instance of the PlayerInteract event.
 	 */
 	@ForgeSubscribe
 	public void playerInteractEventHandler(PlayerInteractEvent event)
 	{
 		if (event.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK)
 		{
 			handlePlayerLeftClickBlock(event);
 		}
 
 		else if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)
 		{
 			handlePlayerRightClickBlock(event);
 		}
 	}
 
 	/**
 	 * Handles right clicking on a block.
 	 * 
 	 * @param 	event	An instance of the PlayerInteract event.
 	 */
 	private void handlePlayerRightClickBlock(PlayerInteractEvent event) 
 	{
 		//Check to see what block was right clicked.
 		Block blockInstance = Block.blocksList[event.entityPlayer.worldObj.getBlockId(event.x, event.y, event.z)];
 
 		if (blockInstance != null)
 		{
 			//It's a sapling.
 			if (blockInstance instanceof BlockSaplingBase)
 			{
 				//See if the player is using bone meal on it and try to make it grow if so.
 				ItemStack currentStack = event.entityPlayer.inventory.getStackInSlot(event.entityPlayer.inventory.currentItem);
 				int itemId = currentStack.itemID;
 				int itemMeta = currentStack.getItemDamage();
 
 				//Have to put the bone meal in a stack with 15 damage.
 				ItemStack compareStack = new ItemStack(Item.dyePowder, 1, 15);
 				if (itemId == compareStack.itemID && itemMeta == compareStack.getItemDamage())
 				{
 					//Spawn particles.
 					blockInstance.setBlockBoundsBasedOnState(event.entityPlayer.worldObj, event.x, event.y, event.z);
 
 					Random itemRand = event.entityPlayer.worldObj.rand;
 					for (int j1 = 0; j1 < 15; ++j1)
 					{
 						double d0 = itemRand.nextGaussian() * 0.02D;
 						double d1 = itemRand.nextGaussian() * 0.02D;
 						double d2 = itemRand.nextGaussian() * 0.02D;
 						event.entityPlayer.worldObj.spawnParticle("happyVillager", (double)((float)event.x + itemRand.nextFloat()), (double)event.y + (double)itemRand.nextFloat() * blockInstance.getBlockBoundsMaxY(), (double)((float)event.z + itemRand.nextFloat()), d0, d1, d2);
 					}
 
 					//Cast the block to a sapling so we can access the grow method.
 					BlockSaplingBase sapling = (BlockSaplingBase)blockInstance;
 					sapling.tryGrow(event.entityPlayer.worldObj, event.x, event.y, event.z, event.entityPlayer.worldObj.rand);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Handles left clicking a block.
 	 * 
 	 * @param 	event	An instance of the PlayerInteract event.
 	 */
 	private void handlePlayerLeftClickBlock(PlayerInteractEvent event)
 	{
 		//Only run server-side.
 		if (!event.entityPlayer.worldObj.isRemote)
 		{
 			//Turn our player into an EntityPlayerMP so we can tell if we're in creative mode or not.
 			EntityPlayerMP entityPlayerMP = (EntityPlayerMP)event.entityPlayer;
 
 			//Only run this if not in creative mode.
 			if (!entityPlayerMP.theItemInWorldManager.isCreative())
 			{
 				int blockID = event.entityPlayer.worldObj.getBlockId(event.x, event.y, event.z);
 				int meta = event.entityPlayer.worldObj.getBlockMetadata(event.x, event.y, event.z);
 
 				handleExperienceGain(event, blockID, meta);
 				handleAttemptToCutTree(event, blockID, meta);
 			}
 		}
 	}
 
 	/**
 	 * Left click event. Checks to see if the player's experience needs to be upped.
 	 * 
 	 * @param 	event	Instance of the event.
 	 * @param 	blockID	Block ID that was clicked.
 	 * @param 	meta	Meta of block that was clicked.
 	 */
 	private void handleExperienceGain(PlayerInteractEvent event, int blockID, int meta)
 	{
 		//Get data about the current event.
 		EntityPlayer player = event.entityPlayer;
 
 		//Get what the player previously did.
 		PlayerInteractEntry entry = ArrowsPlus.instance.playerBlockHarvestingMap.get(player.username);
 		int previousBlockID = 0;
 		int previousMeta = 0;
 
 		if (entry != null)
 		{
 			previousBlockID = entry.blockID;
 			previousMeta = entry.meta;
 		}
 
 		//Check for any differences.
 		if (previousBlockID != blockID || previousMeta != meta || event.x != entry.x || event.y != entry.y || event.z != entry.z)
 		{
 			//Something is different, so update the PlayerInteractEntry in the map for this player. Get the player's world properties manager, too.
 			ArrowsPlus.instance.playerBlockHarvestingMap.put(player.username, new PlayerInteractEntry(event.x, event.y, event.z, blockID, meta));
 			WorldPropertiesManager manager = ArrowsPlus.instance.playerWorldManagerMap.get(event.entityPlayer.username);
 
 			if (manager != null)
 			{
 				//Check to see if the previous event's block ID is different from this one's.
 				int blockIDOnPreviousCoordinates = 0;
 				int blockMetaOnPreviousCoordinates = 0;
 
 				if (entry != null)
 				{
 					blockIDOnPreviousCoordinates = player.worldObj.getBlockId(entry.x, entry.y, entry.z);
 					blockMetaOnPreviousCoordinates = player.worldObj.getBlockMetadata(entry.x, entry.y, entry.z);
 				}
 
 				//Only run if the previous event's block ID or meta value is different, meaning the same block is no longer there.
 				if (blockIDOnPreviousCoordinates != previousBlockID || blockMetaOnPreviousCoordinates != previousMeta)
 				{
 					//Save the previous experience in case it changes.
 					float previousExperience = manager.worldProperties.stat_WoodcuttingExperience;
 
 					//Check if the previous block ID was regular wood. If it's gone, then assume the player harvested it and give them experience.
 					if (previousBlockID == Block.wood.blockID)
 					{
 						//Debug mode gives 1 point of experience.
 						if (!ArrowsPlus.instance.inDebugMode)
 						{
 							manager.worldProperties.stat_WoodcuttingExperience += 0.080F;
 							manager.saveWorldProperties();
 						}
 
 						else
 						{
 							manager.worldProperties.stat_WoodcuttingExperience += 1.0F;
 							manager.saveWorldProperties();
 						}
 					}
 
 					//Now check if it was an arrow tree.
 					else if (previousBlockID == ArrowsPlus.instance.blockArrowTreeLog.blockID)
 					{
 						//Debug mode gives 1 point of experience. Arrow trees give more experience based on level (meta).
 						if (!ArrowsPlus.instance.inDebugMode)
 						{
 							manager.worldProperties.stat_WoodcuttingExperience += 0.080F * (meta + 1);
 							manager.saveWorldProperties();
 						}
 
 						else
 						{
 							manager.worldProperties.stat_WoodcuttingExperience += 1.0F;
 							manager.saveWorldProperties();
 						}
 					}
 
 					//See if experience has changed, meaning we need to check for level-up.
 					if (previousExperience != manager.worldProperties.stat_WoodcuttingExperience)
 					{
 						//Save code for DARK GREEN and the current experience so things are easier to read.
 						String DARKGREEN = "\u00a72";
 						float currentExperience = manager.worldProperties.stat_WoodcuttingExperience;
 
 						//Determine what level the player achieved and tell them what they can do.
 						if (previousExperience < 10.0F && currentExperience >= 10.0F)
 						{
 							player.sendChatToPlayer(new ChatMessageComponent().func_111079_a(DARKGREEN + "You have reached level 10. You are now able to cut Alder trees."));
 							player.worldObj.playSoundAtEntity(player, "random.levelup", 0.75F, 1.0F);
 						}
 
 						else if (previousExperience < 20.0F && currentExperience >= 20.0F)
 						{
 							player.sendChatToPlayer(new ChatMessageComponent().func_111079_a(DARKGREEN + "You have reached level 20. You are now able to cut Sycamore & Maple trees."));
 							player.worldObj.playSoundAtEntity(player, "random.levelup", 0.75F, 1.0F);
 						}
 
 						else if (previousExperience < 30.0F && currentExperience >= 30.0F)
 						{
 							player.sendChatToPlayer(new ChatMessageComponent().func_111079_a(DARKGREEN + "You have reached level 30. You are now able to cut Gum trees."));
 							player.worldObj.playSoundAtEntity(player, "random.levelup", 0.75F, 1.0F);
 						}
 
 						else if (previousExperience < 40.0F && currentExperience >= 40.0F)
 						{
 							player.sendChatToPlayer(new ChatMessageComponent().func_111079_a(DARKGREEN + "You have reached level 40."));	
 							player.worldObj.playSoundAtEntity(player, "random.levelup", 0.75F, 1.0F);
 						}
 
 						else if (previousExperience < 50.0F && currentExperience >= 50.0F)
 						{
 							player.sendChatToPlayer(new ChatMessageComponent().func_111079_a(DARKGREEN + "You have reached level 50. You are now able to cut Ash trees."));
 							player.worldObj.playSoundAtEntity(player, "random.levelup", 0.75F, 1.0F);
 						}
 
 						else if (previousExperience < 60.0F && currentExperience >= 60.0F)
 						{
 							player.sendChatToPlayer(new ChatMessageComponent().func_111079_a(DARKGREEN + "You have reached level 60. You are now able to cut Beech trees."));
 							player.worldObj.playSoundAtEntity(player, "random.levelup", 0.75F, 1.0F);
 						}
 
 						else if (previousExperience < 70.0F && currentExperience >= 70.0F)
 						{
 							player.sendChatToPlayer(new ChatMessageComponent().func_111079_a(DARKGREEN + "You have reached level 70. You are now able to cut Hickory trees."));
 							player.worldObj.playSoundAtEntity(player, "random.levelup", 0.75F, 1.0F);
 						}
 
 						else if (previousExperience < 80.0F && currentExperience >= 80.0F)
 						{
 							player.sendChatToPlayer(new ChatMessageComponent().func_111079_a(DARKGREEN + "You have reached level 80."));
 							player.worldObj.playSoundAtEntity(player, "random.levelup", 0.75F, 1.0F);
 						}
 
 						else if (previousExperience < 90.0F && currentExperience >= 90.0F)
 						{
 							player.sendChatToPlayer(new ChatMessageComponent().func_111079_a(DARKGREEN + "You have reached level 90. You are now able to cut Mahogany trees."));
 							player.worldObj.playSoundAtEntity(player, "random.levelup", 0.75F, 1.0F);
 						}
 
 						else if (previousExperience < 100.0F && currentExperience >= 100.0F)
 						{
 							player.sendChatToPlayer(new ChatMessageComponent().func_111079_a(DARKGREEN + "You have reached level 100. You are now able to cut Sypherus trees."));
 							player.worldObj.playSoundAtEntity(player, "random.levelup", 0.75F, 1.0F);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Left click event. Checks to see if the player is allowed to cut a tree.
 	 * 
 	 * @param 	event	Instance of the event.
 	 * @param 	blockID	Block ID that was clicked.
 	 * @param 	meta	Meta of block that was clicked.
 	 */
 	private void handleAttemptToCutTree(PlayerInteractEvent event, int blockID, int meta)
 	{
 		if (blockID == ArrowsPlus.instance.blockArrowTreeLog.blockID)
 		{				
 			WorldPropertiesManager manager = ArrowsPlus.instance.playerWorldManagerMap.get(event.entityPlayer.username);
 
 			if (manager != null)
 			{
 				float levelCurrent = manager.worldProperties.stat_WoodcuttingExperience;
 				float levelRequired = 0.0F;
 
 				switch (meta)
 				{
 				case 0: levelRequired = 0.0F; break;
 				case 1: levelRequired = 0.0F; break;
 				case 2: levelRequired = 10.0F; break;
 				case 3: levelRequired = 20.0F; break;
 				case 4: levelRequired = 30.0F; break;
 				case 5: levelRequired = 20.0F; break;
 				case 6: levelRequired = 50.0F; break;
 				case 7: levelRequired = 60.0F; break;
 				case 8: levelRequired = 20.0F; break;
 				case 9: levelRequired = 80.0F; break;
 				case 10: levelRequired = 90.0F; break;
 				case 11: levelRequired = 100.0F; break;
 				default: levelRequired = 100.0F; break;
 				}
 
 				if (levelCurrent < levelRequired)
 				{		
 					/** The code for red in Minecraft's text system. */
 					String RED = "\u00a7C";
 
 					event.entityPlayer.addChatMessage(RED + "You do not have enough experience to cut this tree.");
					event.entityPlayer.addChatMessage(RED + "Your experience: " + new DecimalFormat("#.##").format(levelCurrent) + ". Required: " + levelRequired);
 
 					PlayerEvent.BreakSpeed breakSpeedEvent = new PlayerEvent.BreakSpeed(event.entityPlayer, ArrowsPlus.instance.blockArrowTreeLog, meta, 0.01F);
 
 					MinecraftForge.EVENT_BUS.post(breakSpeedEvent);
 					event.setResult(Result.DENY);
 					event.setCanceled(true);
 				}
 			}
 		}
 	}
 }
