 package ak.ChainDestruction;
 
 import java.io.DataOutputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import net.minecraft.block.Block;
 import net.minecraft.enchantment.EnchantmentHelper;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemBlock;
 import net.minecraft.item.ItemStack;
 import net.minecraft.stats.StatList;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.ChunkPosition;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
 import net.minecraftforge.event.entity.player.PlayerInteractEvent;
 import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
 import ak.MultiToolHolders.ItemMultiToolHolder;
 import ak.MultiToolHolders.ToolHolderData;
 
 import com.google.common.io.ByteArrayDataInput;
 
 import cpw.mods.fml.common.network.PacketDispatcher;
 
 public class InteractBlockHook
 {
 	private int[] blockPos = new int[]{0,0,0,0,0,0};
 	private ArrayList blocklist = new ArrayList();
 	private int minX;
 	private int maxX;
 	private int minY;
 	private int maxY;
 	private int minZ;
 	private int maxZ;
 	private boolean toggle = false;
 	private boolean digUnderToggle = false;
 	private boolean digUnder = ChainDestruction.digUnder;
 	@ForgeSubscribe
 	public void PlayerInteractBlock(PlayerInteractEvent event)
 	{
 		EntityPlayer player = event.entityPlayer;
 		World world = player.worldObj;
 		ItemStack item = event.entityPlayer.getCurrentEquippedItem();
 		if(item != null && ChainDestruction.enableItems.contains(item.itemID))
 		{
 			int blockid = world.getBlockId(event.x, event.y, event.z);
 			if(event.action == Action.RIGHT_CLICK_BLOCK)
 			{
 				if(player.isSneaking() && !ChainDestruction.enableBlocks.contains(blockid))
 				{
 					ChainDestruction.enableBlocks.add(blockid);
 					player.addChatMessage(String.format("Add Block Id: %d", blockid));
 				}
 				else if(!player.isSneaking() && ChainDestruction.enableBlocks.contains(blockid))
 				{
 					ChainDestruction.enableBlocks.remove(blockid);
 					player.addChatMessage(String.format("Remove Block Id: %d", blockid));
 				}
 			}
 			else if(event.action == Action.LEFT_CLICK_BLOCK
 					&& ChainDestruction.enableBlocks.contains(blockid)
 					&& ChainDestruction.enableItems.contains(item.itemID))
 			{
 				int meta = world.getBlockMetadata(event.x, event.y, event.z);
 				blockPos[0] = event.x;
 				blockPos[1] = event.y;
 				blockPos[2] = event.z;
 				blockPos[3] = event.face;
 				blockPos[4] = blockid;
 				blockPos[5] = meta;
 			}
 		}
 	}
 	
 	@ForgeSubscribe
 	public void LivingUpdate(LivingUpdateEvent event)
 	{
 		if(event.entityLiving instanceof EntityPlayer)
 		{
 			EntityPlayer player = (EntityPlayer) event.entityLiving;
 			ItemStack item = player.getCurrentEquippedItem();
 			World world = event.entityLiving.worldObj;
 			boolean isAir = world.isAirBlock(blockPos[0], blockPos[1], blockPos[2]);
 			if(blockPos[4] != 0 && isAir && !world.isRemote)
 			{
 				ChainMethod(world, player, item);
 			}
 			if(world.isRemote)
 			{
 				this.toggle = CDKeyHandler.regItemKeyDown && !this.toggle;
 				this.digUnderToggle = CDKeyHandler.digUnderKeyDown && !this.digUnderToggle;
 				if(this.digUnderToggle)
 				{
 					this.digUnder = !this.digUnder;
 					player.addChatMessage(String.format("Dig Under %b", this.digUnder));
 					this.digUnderToggle = false;
 				}
 				PacketDispatcher.sendPacketToServer(PacketHandler.getPacketRegKeyToggle(this));
 			}
 			if(this.toggle && item != null)
 			{
 				if(player.isSneaking() && ChainDestruction.enableItems.contains(item.itemID))
 				{
 					ChainDestruction.enableItems.remove(item.itemID);
 					player.addChatMessage(String.format("Remove Tool Id %d", item.itemID));
 					toggle = false;
 				}
 				if(!player.isSneaking() && !ChainDestruction.enableItems.contains(item.itemID))
 				{
 					ChainDestruction.enableItems.add(item.itemID);
 					player.addChatMessage(String.format("Add Tool Id %d", item.itemID));
 					toggle = false;
 				}
 			}
 			ChainDestruction.digUnder = this.digUnder;
 		}
 	}
 	public void ChainMethod(World world, EntityPlayer player, ItemStack item)
 	{
 		Block block = Block.blocksList[blockPos[4]];
 		if(ChainDestruction.enableBlocks.contains(blockPos[4]) &&  item != null && ChainDestruction.enableItems.contains(item.itemID))
 		{
 			getFirstDestroyedBlock(world,player,block, item);
 			setBlockBounds(player);
 			ChainDestroyBlock(world,player,block, item);
 			blocklist.clear();
 			for(int i = 0;i<5;i++)
 			{
 				blockPos[i] = 0;
 			}
 		}
 	}
 	public void getFirstDestroyedBlock(World world, EntityPlayer player, Block block, ItemStack item)
 	{
 		List<EntityItem> list = world.getEntitiesWithinAABB(EntityItem.class, player.boundingBox.expand(5d, 5d, 5d));
 		if(list == null)
 			return;
		getDestroyedBlockItem(world, player, block, item ,list);
	}
	public void getDestroyedBlockItem(World world, EntityPlayer player, Block block, ItemStack item, List<EntityItem> list)
	{
 		double d0;
 		double d1;
 		double d2;
 		float f1 = player.rotationYaw * 0.01745329F;
 		int i1 = EnchantmentHelper.getFortuneModifier(player);
 		for(EntityItem eItem: list)
 		{
 			if(eItem.getEntityItem().getItem() instanceof ItemBlock && eItem.getEntityItem().itemID == block.blockID
 					|| eItem.getEntityItem().itemID == block.idDropped(blockPos[5], world.rand, i1))
 			{
 				eItem.delayBeforeCanPickup = 0;
 				d0 = player.posX - MathHelper.sin(f1) * 0.5D;
 				d1 = player.posY;
 				d2 = player.posZ + MathHelper.cos(f1) * 0.5D;
 				eItem.setPosition(d0, d1, d2);
 			}
 		}
 	}
 	public void ChainDestroyBlock(World world, EntityPlayer player, Block block, ItemStack item)
 	{
 		int dx = 0;
 		int dy = 0;
 		int dz = 0;
 		ChunkPosition chunk;
 		int id;
 		int meta;
 		boolean flag = false;
 		
 		for(int side = 0;side <6;side++)
 		{
 			if(side == blockPos[3])
 				continue;
 			dx = ForgeDirection.getOrientation(side).offsetX;
 			dy = ForgeDirection.getOrientation(side).offsetY;
 			dz = ForgeDirection.getOrientation(side).offsetZ;
 			chunk = new ChunkPosition(blockPos[0] + dx,blockPos[1] + dy,blockPos[2] + dz);
 			id = world.getBlockId(chunk.x, chunk.y, chunk.z);
 			if(checkChunkInBounds(chunk) && id == block.blockID/* && !blocklist.contains(chunk)*/)
 			{
 				this.SearchBlock(world, player, block, chunk, ForgeDirection.OPPOSITES[side]);
 			}
 		}
 		if(ChainDestruction.loadMTH && item.getItem() instanceof ItemMultiToolHolder)
 		{
 			ToolHolderData tooldata = ((ItemMultiToolHolder)item.getItem()).tools;
 			int slotNum = ((ItemMultiToolHolder)item.getItem()).SlotNum;
 			item = tooldata.tools[slotNum];
 		}
 		Iterator it = blocklist.iterator();
		List<EntityItem> list;
 		while(it.hasNext() && !flag)
 		{
 			chunk = (ChunkPosition) it.next();
 			meta = world.getBlockMetadata(chunk.x, chunk.y, chunk.z);
 			if(item == null)
 			{
 				flag = true;
 				break;
 			}
 			if(item.getItem().onBlockDestroyed(item, world, block.blockID, chunk.x, chunk.y, chunk.z, player))
 			{
 				if(world.setBlock(chunk.x, chunk.y, chunk.z, 0))
 				{
 					block.onBlockDestroyedByPlayer(world, block.blockID, chunk.x, chunk.y, chunk.z);
//					this.harvestBlock(world, player, chunk.x, chunk.y, chunk.z, meta, block);
					block.harvestBlock(world, player, MathHelper.ceiling_double_int( player.posX), MathHelper.ceiling_double_int( player.posY), MathHelper.ceiling_double_int( player.posZ), meta);
 					if(item.stackSize == 0)
 					{
 						player.destroyCurrentEquippedItem();
 						flag = true;
 						break;
 					}
 				}
 				else flag = true;
 			}
 			else flag = true;
 		}
 	}
 	public void SearchBlock(World world, EntityPlayer player, Block block, ChunkPosition chunkpos, int face)
 	{
 		int dx = 0;
 		int dy = 0;
 		int dz = 0;
 		int id;
 		ChunkPosition chunk;
 		blocklist.add(chunkpos);
 		for(int side = 0;side <6;side++)
 		{
 			if(side == face)
 				continue;
 			dx = ForgeDirection.getOrientation(side).offsetX;
 			dy = ForgeDirection.getOrientation(side).offsetY;
 			dz = ForgeDirection.getOrientation(side).offsetZ;
 
 			chunk = new ChunkPosition(chunkpos.x + dx,chunkpos.y + dy,chunkpos.z + dz);
 			id = world.getBlockId(chunk.x, chunk.y, chunk.z);
 			if(checkChunkInBounds(chunk) && id == block.blockID && !blocklist.contains(chunk))
 			{
 				this.SearchBlock(world, player, block, chunk, ForgeDirection.OPPOSITES[side]);
 			}
 		}
 	}
 	public boolean checkChunkInBounds(ChunkPosition chunk)
 	{
 		boolean bx,by,bz;
 		bx = chunk.x >= minX && chunk.x <= maxX;
 		by = chunk.y >= minY && chunk.y <= maxY;
 		bz = chunk.z >= minZ && chunk.z <= maxZ;
 		return bx && by && bz;
 	}
 	public void setBlockBounds(EntityPlayer player)
 	{
 		minX = blockPos[0] - ChainDestruction.maxDestroyedBlock;
 		maxX = blockPos[0] + ChainDestruction.maxDestroyedBlock;
 		if(ChainDestruction.digUnder)
 			minY = blockPos[1] - ChainDestruction.maxDestroyedBlock;
 		else if(blockPos[3] != 1)
 			minY = MathHelper.floor_double(player.posY);
 		else
 			minY = MathHelper.floor_double(player.posY) - 1;
 		maxY = blockPos[1] + ChainDestruction.maxDestroyedBlock;
 		minZ = blockPos[2] - ChainDestruction.maxDestroyedBlock;
 		maxZ = blockPos[2] + ChainDestruction.maxDestroyedBlock;
 	}
 	public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int meta, Block block)
 	{
 		player.addStat(StatList.mineBlockStatArray[block.blockID], 1);
 		player.addExhaustion(0.025F);
 		if (block.canSilkHarvest(world, player, x, y, z, meta) && EnchantmentHelper.getSilkTouchModifier(player))
 		{
 			ItemStack itemstack = this.createStackedBlock(block, meta);
 
 			if (itemstack != null)
 			{
 				this.dropBlockAsItem_do(world, x, y, z, itemstack, player);
 			}
 		}
 		else
 		{
 			int i1 = EnchantmentHelper.getFortuneModifier(player);
 			this.dropBlockAsItemWithChance(world, x, y, z, meta, 1.0f, i1, player, block);
 		}
 	}
 	public void dropBlockAsItemWithChance(World world, int x, int y, int z, int meta, float par6, int fortune, EntityPlayer player, Block block)
 	{
 		if (!world.isRemote)
 		{
 			ArrayList<ItemStack> items = block.getBlockDropped(world, x, y, z, meta, fortune);
 
 			for (ItemStack item : items)
 			{
 				if (world.rand.nextFloat() <= par6)
 				{
 					this.dropBlockAsItem_do(world, x, y, z, item, player);
 				}
 			}
 		}
 	}
 	public void dropBlockAsItem_do(World world, int x, int y, int z, ItemStack item, EntityPlayer player)
 	{
 		if (!world.isRemote && world.getGameRules().getGameRuleBooleanValue("doTileDrops"))
 		{
 			EntityItem entityitem;
 			double d0;
 			double d1;
 			double d2;
 			float f1 = player.rotationYaw * 0.01745329F;
 			if(ChainDestruction.dropOnPlayer)
 			{
 				d0 = player.posX - MathHelper.sin(f1) * 0.5D;
 				d1 = player.posY;
 				d2 = player.posZ + MathHelper.cos(f1) * 0.5D;
 				entityitem = new EntityItem(world, d0, d1, d2, item);
 				entityitem.delayBeforeCanPickup = 0;
 			}
 			else
 			{
 				float f = 0.7F;
 				d0 = (double)(world.rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
 				d1 = (double)(world.rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
 				d2 = (double)(world.rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
 				entityitem = new EntityItem(world, (double)x + d0, (double)y + d1, (double)z + d2, item);
 				entityitem.delayBeforeCanPickup = 10;
 			}
 			world.spawnEntityInWorld(entityitem);
 		}
 	}
 	protected ItemStack createStackedBlock(Block block, int meta)
 	{
 		int j = 0;
 
 		if (block.blockID >= 0 && block.blockID < Item.itemsList.length && Item.itemsList[block.blockID].getHasSubtypes())
 		{
 			j = meta;
 		}
 
 		return new ItemStack(block.blockID, 1, j);
 	}
  	public void readPacketData(ByteArrayDataInput data)
  	{
  		try
  		{
  			this.toggle = data.readBoolean();
  			this.digUnder = data.readBoolean();
  		}
  		catch (Exception e)
  		{
  			e.printStackTrace();
  		}
  	}
  	public void writePacketData(DataOutputStream dos)
  	{
  		try
  		{
  			dos.writeBoolean(this.toggle);
  			dos.writeBoolean(this.digUnder);
  		}
  		catch (Exception e)
  		{
  			e.printStackTrace();
  		}
  	}
 }
