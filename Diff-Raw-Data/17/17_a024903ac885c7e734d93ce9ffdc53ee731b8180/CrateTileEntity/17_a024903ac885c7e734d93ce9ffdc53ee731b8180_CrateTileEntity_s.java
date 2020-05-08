 package com.five35.minecraft.fractalcrates;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet132TileEntityData;
 import net.minecraft.tileentity.TileEntity;
 
 public class CrateTileEntity extends TileEntity implements IInventory {
	private ItemStack contents;
 
 	public CrateTileEntity() {}
 
 	@Override
 	public void closeChest() {
 		// crates don't track being opened or closed
 	}
 
 	@Override
 	public ItemStack decrStackSize(final int slot, final int count) {
 		if (slot == 0 && this.contents != null) {
 			final ItemStack stack;
 
 			if (this.contents.stackSize <= count) {
 				stack = this.contents;
 
 				this.contents = null;
 			} else {
 				stack = this.contents.splitStack(count);
 			}
 
 			this.onInventoryChanged();
 
 			return stack;
 		}
 
 		return null;
 	}
 
 	@Override
 	public Packet getDescriptionPacket() {
 		final NBTTagCompound tag = new NBTTagCompound();
 
 		this.writeToNBT(tag);
 
 		return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 0, tag);
 	}
 
 	@Override
 	public int getInventoryStackLimit() {
 		return 64;
 	}
 
 	@Override
 	public String getInvName() {
 		// TODO custom GUI name?
 
 		return "container.fractalCrate";
 	}
 
 	@Override
 	public int getSizeInventory() {
 		return 1;
 	}
 
 	@Override
 	public ItemStack getStackInSlot(final int slot) {
 		return slot == 0 ? this.contents : null;
 	}
 
 	@Override
 	public ItemStack getStackInSlotOnClosing(final int slot) {
 		// this is only called by containers which do not keep their inventory when closed
 
 		return null;
 	}
 
 	@Override
 	public boolean isInvNameLocalized() {
 		// TODO custom GUI name?
 
 		return false;
 	}
 
 	@Override
 	public boolean isItemValidForSlot(final int slot, final ItemStack stack) {
 		return true;
 	}
 
 	@Override
 	public boolean isUseableByPlayer(final EntityPlayer player) {
 		if (this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord, this.zCoord) != this) {
 			return false;
 		}
 
 		return player.getDistanceSq(this.xCoord + .5, this.yCoord + .5, this.zCoord + .5) <= 64;
 	}
 
 	@Override
 	public void onDataPacket(final INetworkManager manager, final Packet132TileEntityData packet) {
 		this.readFromNBT(packet.customParam1);
 	}
 
 	@Override
 	public void openChest() {
 		// crates don't track being opened or closed
 	}
 
 	@Override
 	public void readFromNBT(final NBTTagCompound tag) {
 		super.readFromNBT(tag);
 		this.readStack(tag);
 	}
 
 	public void readStack(final NBTTagCompound tag) {
 		if (tag.hasKey("Contents")) {
 			this.contents = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("Contents"));
 		}
 	}
 
 	@Override
 	public void setInventorySlotContents(final int slot, final ItemStack stack) {
 		if (slot == 0) {
 			this.contents = stack;
 			this.onInventoryChanged();
 		}
 	}
 
 	public void writeStack(final NBTTagCompound tag) {
 		if (this.contents != null) {
 			final NBTTagCompound tagContents = new NBTTagCompound();
 
 			this.contents.writeToNBT(tagContents);
 
 			tag.setCompoundTag("Contents", tagContents);
 		}
 	}
 
 	@Override
 	public void writeToNBT(final NBTTagCompound tag) {
 		super.writeToNBT(tag);
 		this.writeStack(tag);
 	}
 }
