 package com.qzx.au.core;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import net.minecraft.block.Block;
 import net.minecraft.inventory.ISidedInventory;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet132TileEntityData;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 
 import net.minecraftforge.common.ForgeDirection;
 
 public abstract class TileEntityAU extends TileEntity implements ISidedInventory, ISidedTileEntity {
 	// public World worldObj
 	// public int xCoord, yCoord, zCoord
 	// protected boolean tileEntityInvalid
 	// public int blockMetadata = -1
 	// public Block blockType
 	protected int nrAccessPlayers;
 	protected ForgeDirection direction; // NBT
 	private Block camoBlock; // NBT
 	private byte camoMeta; // NBT
 	private String owner; // NBT
 	protected int nrUpgrades; // NBT
 	protected ItemStack[] upgradeContents; // NBT
 	protected int nrSlots; // NBT
 	protected ItemStack[] slotContents; // NBT
 	protected int firstValidSlot;
 
 	protected SidedBlockInfo sidedBlockInfo;
 
 	protected TileEntityAU(){
 		super();
 		this.nrAccessPlayers = 0;
 		this.direction = (this.canRotateVertical() ? ForgeDirection.UP : ForgeDirection.NORTH);
 		this.camoBlock = null;
 		this.camoMeta = 0;
 		this.owner = null;
 		this.nrUpgrades = 0;
 		this.upgradeContents = null;
 		this.nrSlots = 0;
 		this.slotContents = null;
 		this.firstValidSlot = 0;
 		this.sidedBlockInfo = null;
 	}
 
 	//////////
 
 	protected void markChunkModified(){
 		#ifdef MC152
 		this.worldObj.updateTileEntityChunkAndDoNothing(this.xCoord, this.yCoord, this.zCoord, this);
 		#else
 		this.worldObj.markTileEntityChunkModified(this.xCoord, this.yCoord, this.zCoord, this);
 		#endif
 	}
 
 	//////////
 
 	public void dropContents(World world, int x, int y, int z){
 		// drop upgrades
 		if(this.upgradeContents != null)
 			for(int i = 0; i < this.upgradeContents.length; i++)
 				ItemUtils.dropItemAsEntity(world, x, y, z, this.upgradeContents[i]);
 		// drop inventory
 		if(this.slotContents != null)
 			for(int i = this.firstValidSlot; i < this.slotContents.length; i++)
 				ItemUtils.dropItemAsEntity(world, x, y, z, this.slotContents[i]);
 	}
 
 	public void dropContents(World world, int x, int y, int z, ItemStack item){
 
 // TODO: if retention, store NBT from TileEntity into Item
 //		ItemUtils.dropItemAsEntity() has code to do this
 
 		this.dropContents(world, x, y, z);
 	}
 
 	/////////
 	// NBT //
 	/////////
 
 	@Override
 	public void readFromNBT(NBTTagCompound nbt){
 		super.readFromNBT(nbt);
 
 		// direction
 		if(this.canRotate())
 			this.direction = ForgeDirection.values()[nbt.getByte("face")];
 
 		// block camo
 		if(this.canCamo()){
 			this.camoBlock = Block.blocksList[nbt.getInteger("camoID")];
 			this.camoMeta = nbt.getByte("camoMeta");
 		}
 
 		// placed by
 		if(this.canOwn())
 			this.owner = nbt.getString("owner");
 
 		// upgrade contents
 		if(this.nrUpgrades > 0){
 			NBTTagList taglist = nbt.getTagList("upgrades");
 			this.upgradeContents = new ItemStack[this.getSizeInventory()];
 			for(int i = 0; i < taglist.tagCount(); i++){
 				NBTTagCompound nbt_slot = (NBTTagCompound)taglist.tagAt(i);
 				int slot = nbt_slot.getByte("slot") & 0xff;
 				if(slot >= 0 && slot < this.upgradeContents.length)
 					this.upgradeContents[slot] = ItemStack.loadItemStackFromNBT(nbt_slot);
 			}
 		}
 
 		// slot contents (inventory, etc)
 		if(this.nrSlots > 0){
 			NBTTagList taglist = nbt.getTagList("contents");
 			this.slotContents = new ItemStack[this.getSizeInventory()];
 			for(int i = 0; i < taglist.tagCount(); i++){
 				NBTTagCompound nbt_slot = (NBTTagCompound)taglist.tagAt(i);
 				int slot = nbt_slot.getByte("slot") & 0xff;
 				if(slot >= 0 && slot < this.slotContents.length)
 					this.slotContents[slot] = ItemStack.loadItemStackFromNBT(nbt_slot);
 			}
 		}
 
 		// sided slots
 		if(this.sidedBlockInfo != null) this.sidedBlockInfo.readFromNBT(nbt);
 	}
 
 	@Override
 	public void writeToNBT(NBTTagCompound nbt){
 		super.writeToNBT(nbt);
 
 		// direction
 		if(this.canRotate())
 			nbt.setByte("face", (byte)this.direction.ordinal());
 
 		// block camo
 		if(this.canCamo() && this.camoBlock != null){
 			nbt.setInteger("camoID", this.camoBlock.blockID);
 			nbt.setByte("camoMeta", (byte)this.camoMeta);
 		}
 
 		// placed by
 		if(this.owner != null)
 			nbt.setString("owner", this.owner);
 
 		// upgrade contents
 		if(this.nrUpgrades > 0){
 			NBTTagList taglist = new NBTTagList();
 			for(int i = 0; i < this.upgradeContents.length; i++){
 				if(this.upgradeContents[i] != null){
 					NBTTagCompound nbt_slot = new NBTTagCompound();
 					nbt_slot.setByte("slot", (byte)i);
 					this.upgradeContents[i].writeToNBT(nbt_slot);
 					taglist.appendTag(nbt_slot);
 				}
 			}
 			nbt.setTag("upgrades", taglist);
 		}
 
 		// slot contents (inventory, etc)
 		if(this.nrSlots > 0){
 			NBTTagList taglist = new NBTTagList();
 			for(int i = 0; i < this.slotContents.length; i++){
 				if(this.slotContents[i] != null){
 					NBTTagCompound nbt_slot = new NBTTagCompound();
 					nbt_slot.setByte("slot", (byte)i);
 					this.slotContents[i].writeToNBT(nbt_slot);
 					taglist.appendTag(nbt_slot);
 				}
 			}
 			nbt.setTag("contents", taglist);
 		}
 
 		// sided slots
 		if(this.sidedBlockInfo != null) this.sidedBlockInfo.writeToNBT(nbt);
 	}
 
 	////////
 	// UI //
 	////////
 
 	public ContainerAU getContainer(InventoryPlayer inventory){
 		// override for custom GUI
 		// return new ContainerAU(inventory, this);
 		return null;
 	}
 
 	//////////
 
 	/////////////////
 	// directional //
 	/////////////////
 
 	public boolean canRotate(){
 		return true;
 	}
 	public boolean canRotateVertical(){
 		return true;
 	}
 
 	public boolean placeOppositeDirection(){
 		return false;
 	}
 
 	public void setDirection(ForgeDirection direction, boolean server){
 		if(direction == this.direction || !this.canRotate()) return;
 		this.direction = direction;
 
 		if(server){
 			PacketUtils.sendToAllAround(this.worldObj, PacketUtils.MAX_RANGE, AUCore.packetChannel, Packets.CLIENT_DIRECTION,
 										this.xCoord, this.yCoord, this.zCoord, direction.ordinal());
 			this.markChunkModified();
 		} else
 			this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
 	}
 
 	public ForgeDirection getDirection(){
 		return this.direction;
 	}
 
 	////////////////
 	// block camo //
 	////////////////
 
 	public boolean canCamo(){
 		return true;
 	}
 
 	public ItemStack getCamoBlock(){
 		return (this.canCamo() && this.camoBlock != null ? new ItemStack(this.camoBlock, 1, this.camoMeta) : null);
 	}
 
 	public void setCamoBlock(ItemStack itemstack){
 		// server
 		int blockID = (itemstack == null ? 0 : itemstack.itemID);
 		Block block = (itemstack == null ? null : Block.blocksList[blockID]);
		byte metadata = (byte)(itemstack == null ? 0 : itemstack.getItemDamage());
 		int currentID = (this.camoBlock == null ? 0 : this.camoBlock.blockID);
 
		if((currentID == blockID && this.camoMeta == metadata) || !this.canCamo()) return;
 		this.camoBlock = block;
 		this.camoMeta = metadata;
 
 		PacketUtils.sendToAllAround(this.worldObj, PacketUtils.MAX_RANGE, AUCore.packetChannel, Packets.CLIENT_BLOCK_CAMO,
 									this.xCoord, this.yCoord, this.zCoord, blockID, metadata);
 		this.markChunkModified();
 	}
 	public void setCamoBlock(int blockID, byte metadata){
 		// client
 		this.camoBlock = Block.blocksList[blockID];
 		this.camoMeta = metadata;
 
 		this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
 	}
 
 	public Icon getCamoIcon(int side){
 		return (this.camoBlock != null ? this.camoBlock.getIcon(side, this.camoMeta) : null);
 	}
 
 	///////////
 	// owner //
 	///////////
 
 	public boolean canOwn(){
 		return false;
 	}
 
 	public String getOwner(){
 		return this.owner;
 	}
 
 	public void setOwner(String owner){
 		if(!this.canOwn()) return;
 		this.owner = owner;
 		this.markChunkModified();
 	}
 
 	////////////////
 	// TileEntity //
 	////////////////
 
 	@Override
 	public boolean canUpdate(){
 		return false; // return true if updateEntity() should be called
 	}
 	// @Override public void updateEntity();
 
 	@Override
 	public Packet getDescriptionPacket(){
 		NBTTagCompound tag = new NBTTagCompound();
 		this.writeToNBT(tag);
 		return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 1, tag);
 	}
 	@Override
 	public void onDataPacket(INetworkManager net, Packet132TileEntityData packet){
 		#ifdef MC152
 		this.readFromNBT(packet.customParam1);
 		#else
 		this.readFromNBT(packet.data);
 		#endif
 	}
 
 	// @Override public void onInventoryChanged(){
 
 	// public void onChunkUnload();
 
 	////////////////
 	// IInventory //
 	////////////////
 
 	public int getSizeInventory(){
 		return this.nrSlots;
 	}
 
 	public boolean isInvNameLocalized(){
 		return false;
 	}
 	public String getInvName(){
 		// override and return name
 		return "au.tileentity.AU";
 	}
 
 	public ItemStack getStackInSlot(int slotIndex){
 		return slotIndex >= 0 && slotIndex < this.nrSlots ? this.slotContents[slotIndex] : null;
 	}
 	public ItemStack decrStackSize(int slotIndex, int amount){
 		ItemStack itemstack = this.getStackInSlot(slotIndex);
 		if(itemstack == null) return null;
 
 // TODO: if upgrade slot, call method in ItemUpgrade to verify it can be removed
 
 // TODO: return null if fake slot (SlotPattern, SlotResult, etc)
 
 		if(itemstack.stackSize <= amount){
 			this.slotContents[slotIndex] = null;
 			this.onInventoryChanged();
 			return itemstack;
 		}
 		ItemStack ret_itemstack = this.slotContents[slotIndex].splitStack(amount);
 		if(this.slotContents[slotIndex].stackSize == 0)
 			this.slotContents[slotIndex] = null;
 		this.onInventoryChanged();
 		return ret_itemstack;
 	}
 	public ItemStack getStackInSlotOnClosing(int slotIndex){
 		return null;
 	}
 	#ifdef MC152
 	public boolean isStackValidForSlot(int slotIndex, ItemStack itemstack){
 	#else
 	public boolean isItemValidForSlot(int slotIndex, ItemStack itemstack){
 	#endif
 		if(this.sidedBlockInfo == null) return false;
 		SidedSlotInfo info = this.sidedBlockInfo.getSlotInfoFromIndex((byte)slotIndex);
 		return (info == null ? false : info.isItemValid(itemstack)); // ignore itemstack size
 	}
 	public void setInventorySlotContents(int slotIndex, ItemStack itemstack){
 		if(slotIndex < 0 || slotIndex >= this.nrSlots) return;
 
 // TODO: return if fake slot? (SlotPattern, SlotResult, etc)
 
 // TODO: if upgrade slot, call method in ItemUpgrade to verify it can be removed
 
 // TODO: set maxStackSize per slot
 		int maxStackSize = 64;
 
 		this.slotContents[slotIndex] = itemstack;
 		if(itemstack != null && itemstack.stackSize > maxStackSize)
 			itemstack.stackSize = maxStackSize;
 		this.onInventoryChanged();
 	}
 
 	public int getInventoryStackLimit(){
 		return 64; // handled by SlotAU
 	}
 
 	public boolean isUseableByPlayer(EntityPlayer player){
 		if(this.worldObj == null) return true;
 		if(this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord, this.zCoord) != this) return false;
 		return player.getDistanceSq((double)this.xCoord + 0.5D, (double)this.yCoord + 0.5D, (double)this.zCoord + 0.5D) <= 64.0D;
 	}
 
 	public void setAccessPlayers(int value){
 		// client
 		this.nrAccessPlayers = value;
 
 		this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
 	}
 
 	public void openChest(){
 		if(this.worldObj == null || this.worldObj.isRemote) return;
 		this.nrAccessPlayers++;
 
 		PacketUtils.sendToAllAround(this.worldObj, PacketUtils.MAX_RANGE, AUCore.packetChannel, Packets.CLIENT_ACCESS_PLAYERS,
 									this.xCoord, this.yCoord, this.zCoord, this.nrAccessPlayers);
 	}
 
 	public void closeChest(){
 		if(this.worldObj == null || this.worldObj.isRemote) return;
 		this.nrAccessPlayers--;
 
 		PacketUtils.sendToAllAround(this.worldObj, PacketUtils.MAX_RANGE, AUCore.packetChannel, Packets.CLIENT_ACCESS_PLAYERS,
 									this.xCoord, this.yCoord, this.zCoord, this.nrAccessPlayers);
 	}
 
 	/////////////////////
 	// ISidedInventory //
 	/////////////////////
 
 	public int[] getAccessibleSlotsFromSide(int side){
 		if(this.sidedBlockInfo == null) return null;
 		return this.sidedBlockInfo.getAccessibleSlotsFromSide((byte)side);
 	}
 	public boolean canInsertItem(int slotIndex, ItemStack itemstack, int side){
 		if(this.sidedBlockInfo == null) return false;
 		return this.sidedBlockInfo.canInsert((byte)slotIndex, (byte)side);
 	}
 	public boolean canExtractItem(int slotIndex, ItemStack itemstack, int side){
 		if(this.sidedBlockInfo == null) return false;
 		return this.sidedBlockInfo.canExtract((byte)slotIndex, (byte)side);
 	}
 
 	//////////////////////
 	// ISidedTileEntity //
 	//////////////////////
 
 	public SidedBlockInfo getSidedBlockInfo(){
 		return this.sidedBlockInfo;
 	}
 	public Class<? extends ISidedRenderer> getSidedRenderer(){
 		return null;
 	}
 	public void toggleSide(byte side){
 		if(this.worldObj == null || this.worldObj.isRemote) return;
 		SidedBlockInfo info = this.sidedBlockInfo;
 		if(info != null)
 			if(info.canToggle(side)){
 				byte old_slot = info.getSlot(side);
 				byte new_slot = info.toggleSide(side);
 				if(old_slot != new_slot){
 					PacketUtils.sendToAllAround(this.worldObj, PacketUtils.MAX_RANGE, AUCore.packetChannel, Packets.CLIENT_SIDED_IO,
 												this.xCoord, this.yCoord, this.zCoord, side, new_slot);
 					this.markChunkModified();
 				}
 			}
 	}
 	public void setSide(byte side, byte slot){
 		// client
 		SidedBlockInfo info = this.sidedBlockInfo;
 		if(info != null)
 			if(info.canToggle(side)){
 				info.setSide(side, slot);
 				this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
 			}
 	}
 }
