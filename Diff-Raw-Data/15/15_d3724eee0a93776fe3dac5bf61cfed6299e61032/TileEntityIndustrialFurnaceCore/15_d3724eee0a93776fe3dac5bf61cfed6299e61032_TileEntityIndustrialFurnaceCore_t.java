 package com.madpcgaming.mt.tileentitys;
 
 import net.minecraft.block.Block;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.tileentity.TileEntityFurnace;
 
 import com.madpcgaming.mt.blocks.BlockIndustrialFurnaceCore;
 import com.madpcgaming.mt.blocks.ModBlocks;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class TileEntityIndustrialFurnaceCore extends TileEntity implements
 		ISidedInventory {
 	private boolean isValidMultiBlock = false;
 	public int furnaceBurnTime = 0;
 	public int currentItemBurnTime = 0;
 	public int furnaceCookTime = 0;
 	
 	private static final int[] sidedSlotSides = new int[] {0};
 	private static final int[] sidedSlotBottom = new int[] {2,1};
 	private static final int[] sidedSlotTop = new int[] {1};
 	
 	private ItemStack[] furnaceItems = new ItemStack[3];
 	
 	public TileEntityIndustrialFurnaceCore()
 	{
 		
 	}
 	
 	public boolean getIsValid(){
 		return isValidMultiBlock;
 	}
 	public void invalidateMultiBlock(){
 		isValidMultiBlock = false;
 		
 		int metadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
 		metadata = metadata & BlockIndustrialFurnaceCore.MASK_DIR;
 		worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, metadata, 2);
 		
 		furnaceBurnTime = 0;
 		currentItemBurnTime = 0;
 		furnaceCookTime = 0;
 		
 		revertDummies();
 	}
 	
 	public boolean checkIfProperlyFormed()
 	{
 		int dir = (getBlockMetadata() & BlockIndustrialFurnaceCore.MASK_DIR);
 		
 		int depthMultiplier = ((dir == BlockIndustrialFurnaceCore.META_DIR_NORTH || dir == BlockIndustrialFurnaceCore.META_DIR_WEST) ? 1 : -1);
 		boolean forwardZ = ((dir == BlockIndustrialFurnaceCore.META_DIR_NORTH) || (dir == BlockIndustrialFurnaceCore.META_DIR_SOUTH));
 		
 		for (int horiz = -1; horiz <= 1; horiz++){
 			
 			for(int vert = -1; vert <= 1; vert++){
 				
 				for(int depth = 0; depth <= 2; depth++){
 					int x = xCoord +(forwardZ ? horiz : (depth * depthMultiplier));
 					int y = yCoord + vert;
 					int z = zCoord + (forwardZ ? (depth * depthMultiplier) : horiz);
 					
 					int blockId = worldObj.getBlockId(x, y, z);
 					
 					if(horiz == 0 && vert == 0)
 					{
 						if(depth == 0)
 							continue;
 						if(depth == 1)
 						{
 							if(blockId != 0)
 								return false;
 							else
 								continue;
 						}
 					}
 					
 					if (blockId != Block.brick.blockID)
 						return false;
 				}
 			}
 		}
 		return true;
 	}
 	
 	public void convertDummies()
 	{
 		int dir = (getBlockMetadata() & BlockIndustrialFurnaceCore.MASK_DIR);
 		
 		int depthMultiplier = ((dir == BlockIndustrialFurnaceCore.META_DIR_NORTH || dir == BlockIndustrialFurnaceCore.META_DIR_WEST) ? 1 : -1);
 		boolean forwardZ = ((dir == BlockIndustrialFurnaceCore.META_DIR_NORTH) || (dir == BlockIndustrialFurnaceCore.META_DIR_SOUTH));
 		
 		for(int horiz = -1; horiz <= 1; horiz++)
 		{
 			for(int vert = -1; vert <= 1; vert++)
 			{
 				for(int depth = 0; depth <= 2; depth++)
 				{
 					int x = xCoord + (forwardZ ? horiz : (depth * depthMultiplier));
 					int y = yCoord + vert;
 					int z = zCoord + (forwardZ ? (depth * depthMultiplier) : horiz);
 					
 					if (horiz == 0 && vert == 0 && (depth == 0 || depth == 1))
 						continue;
 					worldObj.setBlock(x, y, z, ModBlocks.IndustrialFurnaceDummy.blockID);
 					worldObj.markBlockForUpdate(x, y, z);
 					TileEntityIndustrialFurnaceDummy dummyTE = (TileEntityIndustrialFurnaceDummy)worldObj.getBlockTileEntity(x, y, z);
 					dummyTE.setCore(this);
 				}
 			}
 		}
 		isValidMultiBlock = true;
 	}
 	
 	private void revertDummies()
 	{
 		int dir = (getBlockMetadata() & BlockIndustrialFurnaceCore.MASK_DIR);
 		int depthMultiplier = ((dir == BlockIndustrialFurnaceCore.META_DIR_NORTH || dir == BlockIndustrialFurnaceCore.META_DIR_WEST) ? 1 : -1);
 		boolean forwardZ = ((dir == BlockIndustrialFurnaceCore.META_DIR_NORTH) || (dir == BlockIndustrialFurnaceCore.META_DIR_SOUTH));
 		
 		for(int horiz = -1; horiz <= 1; horiz++)
 		{
 			for(int vert = -1; vert <=1; vert++)
 			{
 				for(int depth = 0; depth <= 2; depth++)
 				{
 					int x = xCoord + (forwardZ ? horiz : (depth * depthMultiplier));
 					int y = yCoord + vert;
 					int z = zCoord + (forwardZ ? (depth * depthMultiplier) : horiz);
 					
 					int blockId = worldObj.getBlockId(x, y, z);
 					
 					if(horiz == 0 && vert == 0 && (depth == 0 || depth == 1))
 						continue;
 					if(blockId != Block.brick.blockID)
 						continue;
 					
 					worldObj.setBlock(x, y, z, Block.brick.blockID);
 					worldObj.markBlockForUpdate(x, y, z);
 				}
 			}
 		}
 		
 		isValidMultiBlock = false;
 	}
 	
 	@Override
 	public void updateEntity()
 	{
 		if(!isValidMultiBlock)
 			return;
 		boolean flag = furnaceBurnTime > 0;
 		boolean flag1 = false;
 		int metadata = getBlockMetadata();
 		int isActive = (metadata >> 3);
 		
 		if(furnaceBurnTime > 0)
 			furnaceBurnTime--;
 		
 		if(!this.worldObj.isRemote)
 		{
 			if(furnaceBurnTime == 0 && canSmelt())
 			{
 				currentItemBurnTime = furnaceBurnTime = TileEntityFurnace.getItemBurnTime(furnaceItems[1]);
 				
 				if(furnaceBurnTime > 0)
 				{
 					flag1 = true;
 					
 					if(furnaceItems[1] != null){
 						furnaceItems[1].stackSize--;
 						
 						if(furnaceItems[1].stackSize == 0)
 							furnaceItems[1] = furnaceItems[1].getItem().getContainerItemStack(furnaceItems[1]);
 					}
 				}
 			}
 			
 			if(isBurning() && canSmelt())
 			{
 				furnaceCookTime++;
 				
 				if(furnaceCookTime == 100)
 				{
 					furnaceCookTime = 0;
 					smeltItem();
 					flag1 = true;
 				}
 			}
 			else
 			{
 				furnaceCookTime = 0;
 			}
 			
 			if(isActive == 0 && furnaceBurnTime > 0)
 			{
 				flag1 = true;
 				metadata = getBlockMetadata();
 				isActive = 1;
 				metadata = (isActive << 3) | (metadata & BlockIndustrialFurnaceCore.META_ISACTIVE);
 				
 				worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, metadata, 2);
 			}
 		}
 		if(flag1)
 			onInventoryChanged();
 	}
 
 	@Override
 	public int getSizeInventory() {
 		
 		return furnaceItems.length;
 	}
 
 	@Override
 	public ItemStack getStackInSlot(int slot) {
 		
 		return furnaceItems[slot];
 	}
 
 	@Override
 	public ItemStack decrStackSize(int slot, int count) {		
 		if(this.furnaceItems[slot] != null)
 		{
 			ItemStack itemStack;
 			
 			itemStack = furnaceItems[slot].splitStack(count);
 				
 			if(furnaceItems[slot].stackSize <= 0)
 				furnaceItems[slot] = null;
 				
 			return itemStack;
 		}
 		
 		return null;
 	}
 
 	@Override
 	public ItemStack getStackInSlotOnClosing(int slot)
 	{
 		if(furnaceItems[slot] != null)
 		{
 			ItemStack stack = furnaceItems[slot];
 			furnaceItems[slot] = null;
 			return stack;
 		}
 		
 		return null;
 	}
 
 	@Override
 	public void setInventorySlotContents(int slot, ItemStack itemStack)
 	{
 		furnaceItems[slot] = itemStack;
 		
 		if(itemStack != null && itemStack.stackSize > getInventoryStackLimit())
 			itemStack.stackSize = getInventoryStackLimit();
 	}
 
 	@Override
 	public String getInvName() {
 		
 		return null;
 	}
 
 	@Override
 	public boolean isInvNameLocalized() {
 		
 		return false;
 	}
 
 	@Override
 	public int getInventoryStackLimit() {
 		return 64;
 	}
 
 	@Override
 	public boolean isUseableByPlayer(EntityPlayer entityPlayer) {
 		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this ? false : entityPlayer.getDistanceSq((double)xCoord + 0.5, (double)yCoord + 0.5, (double)zCoord + 0.5) <= 64.0;
 	}
 
 	@Override
 	public void openChest() {
 		
 
 	}
 
 	@Override
 	public void closeChest() {
 		
 
 	}
 	
 	//@Override
 	public boolean isStackValidForSlot(int slot, ItemStack itemStack)
 	{
 		return slot == 2 ? false : (slot == 1 ? TileEntityFurnace.isItemFuel(itemStack) : true);
 	}
 
 	public int[] getSizeInventorySide(int par1)
 	{
 		return par1 == 0 ? sidedSlotBottom : (par1 == 1 ? sidedSlotTop : sidedSlotSides);
 	}
 
 	@Override
 	public boolean isItemValidForSlot(int slot, ItemStack itemStack) {
 		
 		return slot == 2 ? false : (slot == 1 ? TileEntityFurnace.isItemFuel(itemStack) : true);
 	}
 
 	@Override
 	public int[] getAccessibleSlotsFromSide(int var1) {
 		
 		return null;
 	}
 
 	@Override
 	public boolean canInsertItem(int i, ItemStack itemstack, int j) {
 		
 		return this.isItemValidForSlot(i, itemstack);
 	}
 
 	@Override
 	public boolean canExtractItem(int i, ItemStack itemstack, int j) {
 		
		return j != 0 || i != 1 || itemstack.itemID == Item.bucketEmpty.itemID;
 	}
 	@Override
 	public void writeToNBT(NBTTagCompound tagCompound)
 	{
 		super.writeToNBT(tagCompound);
 		
 		tagCompound.setBoolean("isValidMultiblock", isValidMultiBlock);
 		System.out.println("Is valid? " + (isValidMultiBlock ? "Yes" : "No"));
 		
 		tagCompound.setShort("BurnTime", (short)furnaceBurnTime);
 		tagCompound.setShort("CookTime", (short)furnaceCookTime);
 		NBTTagList itemsList = new NBTTagList();
 		
 		for(int i = 0; i < furnaceItems.length; i++)
 		{
 			if(furnaceItems[i] != null)
 			{
 				NBTTagCompound slotTag = new NBTTagCompound();
 				slotTag.setByte("Slot", (byte)i);
 				furnaceItems[i].writeToNBT(slotTag);
 				itemsList.appendTag(slotTag);
 			}
 			
 			tagCompound.setTag("Items", itemsList);
 		}
 	}
 	
 	@SideOnly(Side.CLIENT)
 	public int getCookProgressScaled(int scaleVal)
 	{
 		return furnaceCookTime * scaleVal / 100;
 	}
 	
 	@SideOnly(Side.CLIENT)
 	public int getBurnTimeRemainingScaled(int scaleVal)
 	{
 		if(currentItemBurnTime == 0)
 			currentItemBurnTime = 100;
 		
 		return furnaceBurnTime * scaleVal / currentItemBurnTime;
 	}
 	
 	public boolean isBurning()
 	{
 		return furnaceBurnTime > 0;
 	}
 	
 	private boolean canSmelt()
 	{
 		if(furnaceItems[0] == null)
 			return false;
 		else
 		{
 			ItemStack itemStack = FurnaceRecipes.smelting().getSmeltingResult(furnaceItems[0]);
 			if(itemStack == null)
 				return false;
 			if(furnaceItems[2] == null)
 				return true;
 			if(!furnaceItems[2].isItemEqual(itemStack))
 				return false;
 			
 			int resultingStackSize = furnaceItems[2].stackSize + itemStack.stackSize;
 			return (resultingStackSize <= getInventoryStackLimit() && resultingStackSize <= itemStack.getMaxStackSize());
 		}
 	}
 	
 	public void smeltItem()
 	{
 		if(canSmelt())
 		{
 			ItemStack itemStack = FurnaceRecipes.smelting().getSmeltingResult(furnaceItems[0]);
 			
 			if(furnaceItems[2] == null)
 				furnaceItems[2] = itemStack.copy();
 			else if(furnaceItems[2].isItemEqual(itemStack))
 				furnaceItems[2].stackSize += itemStack.stackSize;
 			
 			furnaceItems[0].stackSize--;
 			if(furnaceItems[0].stackSize <= 0)
 				furnaceItems[0] = null;
 		}
 	}
 
 }
