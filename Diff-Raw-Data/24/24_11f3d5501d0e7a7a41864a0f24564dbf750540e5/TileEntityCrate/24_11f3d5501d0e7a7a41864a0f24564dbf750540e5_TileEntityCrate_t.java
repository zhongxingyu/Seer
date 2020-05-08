 package ml.boxes.tile;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import ml.boxes.Boxes;
 import ml.boxes.api.ContentBlacklist;
 import ml.boxes.network.packets.PacketDescribeCrate;
 import ml.core.PlayerUtils;
 import ml.core.block.BlockUtils;
 import ml.core.item.ItemUtils;
 import ml.core.tile.IRotatableTE;
 import net.minecraft.block.Block;
 import net.minecraft.client.renderer.RenderBlocks;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.ISidedInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraftforge.common.ForgeDirection;
 import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class TileEntityCrate extends TileEntity implements ISidedInventory, IRotatableTE, IEventedTE {
 
 	public int CRATE_SIZE = 64*72;
 	public ForgeDirection facing = ForgeDirection.NORTH;
 	private long lastRClick = 0L;
 
 	public ItemStack cItem; //Used by server to determine if it needs to send a packet. Used by client for which item to render
 	public int lItemCount;
 
 	@SideOnly(Side.CLIENT)
 	public boolean containedIsBlock;
 	@SideOnly(Side.CLIENT)
 	public String contentString;
 
 	public int itemCount = 0;
 
 	//public List<upgrade> Upgrades = new ArrayList<TileEntityCrate.upgrade>();
 	public boolean upg_label;
 
 	private ItemStack[] stacks;
 
 	public TileEntityCrate() {
 		stacks = new ItemStack[2];
 	}
 
 	public void sendPacket(){
 		PacketDispatcher.sendPacketToAllInDimension(getDescriptionPacket(), worldObj.getWorldInfo().getDimension());
 	}
 	
 	public void testTriggerPacket(){
 		if (cItem != stacks[0] || lItemCount != getTotalItems()) {
 			cItem = stacks[0];
 			lItemCount = getTotalItems();
 			sendPacket();
 		}
 	}
 
 	@SideOnly(Side.CLIENT)
 	public void updateClientDetails(){
 		if (cItem == null) return;
 
 		containedIsBlock = cItem.getItemSpriteNumber() == 0 && cItem.itemID < Block.blocksList.length && (Block.blocksList[cItem.itemID] != null) && RenderBlocks.renderItemIn3d(Block.blocksList[cItem.itemID].getRenderType());
 
 		contentString = "";
 		if (cItem.getMaxStackSize() != 1){
 			if (getTotalItems() >= cItem.getMaxStackSize()){
 				contentString += (int)Math.floor(getTotalItems()/cItem.getMaxStackSize()) + "x" + cItem.getMaxStackSize();
 				if (getTotalItems() % cItem.getMaxStackSize() != 0)
 					contentString += " + ";
 			}
 			if (getTotalItems() % cItem.getMaxStackSize() != 0){
 				contentString += getTotalItems() % cItem.getMaxStackSize();
 			}
 		} else {
 			contentString += getTotalItems();
 		}
 	}
 
 	@Override
 	public Packet getDescriptionPacket() {
 		return new PacketDescribeCrate(this).convertToPkt250();
 	}
 
 	@Override
 	public void writeToNBT(NBTTagCompound tag) {
 		super.writeToNBT(tag);
 		tag.setInteger("containedCount", itemCount);
 		tag.setInteger("facing", facing.ordinal());
 
 		NBTTagCompound upgTag = new NBTTagCompound();
 		upgTag.setBoolean("Label", upg_label);
 		tag.setTag("Upgrades", upgTag);
 
 		NBTTagList nbttaglist = new NBTTagList();
 		for (int i = 0; i < stacks.length; i++)
 		{
 			if (stacks[i] != null)
 			{
 				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
 				nbttagcompound1.setByte("Slot", (byte) i);
 				stacks[i].writeToNBT(nbttagcompound1);
 				nbttaglist.appendTag(nbttagcompound1);
 			}
 		}
 
 		tag.setTag("Items", nbttaglist);
 	}
 
 	@Override
 	public void readFromNBT(NBTTagCompound tag) {
 		super.readFromNBT(tag);
 		itemCount = tag.getInteger("containedCount");
 		facing = ForgeDirection.getOrientation(tag.getInteger("facing"));
 
 		NBTTagCompound upgTag = tag.getCompoundTag("Upgrades");
 		upg_label = upgTag.getBoolean("Label");
 
 		NBTTagList nbttaglist = tag.getTagList("Items");
 		for (int i = 0; i < nbttaglist.tagCount(); i++)
 		{
 			NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbttaglist.tagAt(i);
 			int j = nbttagcompound1.getByte("Slot") & 0xff;
 			if (j >= 0 && j < stacks.length)
 			{
 				stacks[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
 			}
 		}
 	}
 
 	public int getTotalItems(){
 		return itemCount + (stacks[0] != null ? stacks[0].stackSize : 0) + (stacks[1] != null ? stacks[1].stackSize : 0);
 	}
 
 	public void consolidateStacks(){
 		int tItems = getTotalItems();
 		if ((tItems > 0 || upg_label) && (stacks[0] != null || stacks[1] != null)){
 			ItemStack tis = stacks[0] != null ? stacks[0].copy() : stacks[1].copy();
 			tis.stackSize = 0;
 
 			if (stacks[0] == null)
 				stacks[0] = tis.copy();
 			if (stacks[1] == null)
 				stacks[1] = tis.copy();
 
			stacks[0].stackSize = Math.max(0, tItems-(CRATE_SIZE-64));
 			stacks[1].stackSize = Math.min(tItems, stacks[1].getMaxStackSize());
			tItems -= stacks[0].stackSize;
 			tItems -= stacks[1].stackSize;
 			itemCount = tItems;
 		} else {
 			itemCount = 0;
 			stacks[0] = null;
 			stacks[1] = null;
 		}
		//onInventoryChanged(); //TODO Maybe be smarter about this
 		testTriggerPacket();
 	}
	
	@Override
	public void onInventoryChanged() {
		System.out.println(stacks[0]);
		consolidateStacks();
		System.out.println(stacks[0]);
		super.onInventoryChanged();
	}
 
 	@Override
 	public void updateEntity() {
 		if (!worldObj.isRemote){
 			consolidateStacks();
 		} else {
 			
 		}
 		super.updateEntity();
 	}
 
 	@Override
 	public int getSizeInventory() {
 		return 2;
 	}
 
 	@Override
 	public ItemStack getStackInSlot(int i) {
 		return stacks[i];
 	}
 
 	@Override
 	public ItemStack decrStackSize(int i, int j) {
 		if (this.stacks[i] != null)
 		{
 			ItemStack var3;
 
 			if (this.stacks[i].stackSize <= j)
 			{
 				var3 = this.stacks[i];
 				this.stacks[i] = null;
 				this.onInventoryChanged();
 				return var3;
 			}
 			else
 			{
 				var3 = this.stacks[i].splitStack(j);
 
 				if (this.stacks[i].stackSize == 0)
 				{
 					this.stacks[i] = null;
 				}
 
 				this.onInventoryChanged();
 				return var3;
 			}
 		}
 		else
 		{
 			return null;
 		}
 	}
 
 	@Override
 	public ItemStack getStackInSlotOnClosing(int i) {
 		return null;
 	}
 
 	@Override
 	public void setInventorySlotContents(int i, ItemStack itemstack) {
 		stacks[i] = itemstack;
 		onInventoryChanged();
 	}
 
 	@Override
 	public String getInvName() {
 		return "crate";
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
 	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
 		return false;
 	}
 
 	@Override
 	public void openChest() {}
 
 	@Override
 	public void closeChest() {}
 
 	public boolean allowItem(ItemStack is){
 		return is != null && !is.isItemDamaged() && !ContentBlacklist.ItemBlacklisted(ContentBlacklist.LIST_CRATE, is) && (stacks[0] == null || (ItemStack.areItemStackTagsEqual(stacks[0], is) && stacks[0].isItemEqual(is)));
 	}
 
 	@Override
 	public boolean isStackValidForSlot(int i, ItemStack itemstack) {
 		return allowItem(itemstack);
 	}
 
 	@Override
 	public int[] getAccessibleSlotsFromSide(int var1) {
 		switch (ForgeDirection.getOrientation(var1)) {
 		case DOWN:
 			return new int[]{1};
 		}
 		return new int[]{0};
 	}
 
 	@Override
 	public boolean canInsertItem(int i, ItemStack itemstack, int j) {
 		return j!=0;
 	}
 
 	@Override
 	public boolean canExtractItem(int i, ItemStack itemstack, int j) {
 		return j==0;
 	}
 
 	@Override
 	public ForgeDirection getFacing() {
 		return facing;
 	}
 	
 	@Override
 	public void setFacing(ForgeDirection fd) {
 		facing = fd;
 	}
 
 	@Override
 	public ForgeDirection[] getValidFacingDirections() {
 		return ForgeDirection.VALID_DIRECTIONS;
 	}
 	
 	@Override
 	public boolean onRightClicked(EntityPlayer pl, ForgeDirection side) {
 		if (worldObj.isRemote)
 			return true;
 
 		if (worldObj.getWorldTime()-lastRClick < 10L){
 			if (stacks[0] != null){
 				for (int i=0; i<pl.inventory.mainInventory.length; i++){
 					ItemStack is = pl.inventory.getStackInSlot(i);
 					if (is != null && isStackValidForSlot(0, is)){
 						int chg = Math.min(is.stackSize, CRATE_SIZE-getTotalItems());
 						itemCount += pl.inventory.decrStackSize(i, chg).stackSize;
 					}
 				}
 				PlayerUtils.syncClientInventory(pl);
 				consolidateStacks();
 			}
 		} else {
 			ItemStack is = pl.inventory.getCurrentItem();
 			if (is != null && allowItem(is)){
 				int chg = Math.min(is.stackSize, CRATE_SIZE-getTotalItems());
				if (stacks[0] == null) {
					stacks[0] = pl.inventory.decrStackSize(pl.inventory.currentItem, chg);;
				} else {
					stacks[0].stackSize += chg;
					pl.inventory.decrStackSize(pl.inventory.currentItem, chg);
				}
 				consolidateStacks();
 			}
 		}
 		lastRClick = worldObj.getWorldTime();
 
 		return true;
 	}
 
 	@Override
 	public void onLeftClicked(EntityPlayer pl) {
 		if (worldObj.isRemote)
 			return;
 
 		if (stacks[0] != null){
 			int exAmount = Math.min(getTotalItems(), pl.isSneaking() ? 1 : stacks[0].getMaxStackSize());
 			ItemStack nis = stacks[0].copy();
 			nis.stackSize = exAmount;
 			itemCount -= nis.stackSize;
 
 			ForgeDirection fd = BlockUtils.getPlacedForgeDir(pl, xCoord, yCoord, zCoord);
 
 			EntityItem ei = new EntityItem(worldObj, xCoord+0.5F+fd.offsetX*0.75F, yCoord+0.5F+fd.offsetY*0.75F, zCoord+0.5F+fd.offsetZ*0.75F, nis);
 			ei.motionX = fd.offsetX*0.2D;
 			ei.motionY = fd.offsetY*0.2D;
 			ei.motionZ = fd.offsetZ*0.2D;
 			ei.delayBeforeCanPickup = 10;
 			worldObj.spawnEntityInWorld(ei);
 
 			consolidateStacks();
 		}
 	}
 
 	@Override
 	public void hostBroken() {
 		if (worldObj.isRemote)
 			return;
 
 		if (stacks[0] != null){
 			int titems = getTotalItems();
 			while (titems > 0){
 				ItemStack nis = stacks[0].copy();
 				nis.stackSize = Math.min(nis.getMaxStackSize(), titems);
 				titems -= nis.stackSize;
 				ItemUtils.dropItemIntoWorld(worldObj, xCoord, yCoord, zCoord, nis, 0.7F);
 			}
 		}
 	}
 
 	@Override
 	public void hostPlaced(EntityLiving pl, ItemStack is) {}
 	
 	@Override
 	public boolean onAttemptUpgrade(EntityPlayer pl, ItemStack is, int side) {
 		if (is != null){
 			if (!upg_label && is.isItemEqual(new ItemStack(Boxes.ItemResources, 1, 1))){
 				is.stackSize -= 1;
 				upg_label = true;
 				sendPacket();
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public void onNeighborBlockChange() {}
 }
