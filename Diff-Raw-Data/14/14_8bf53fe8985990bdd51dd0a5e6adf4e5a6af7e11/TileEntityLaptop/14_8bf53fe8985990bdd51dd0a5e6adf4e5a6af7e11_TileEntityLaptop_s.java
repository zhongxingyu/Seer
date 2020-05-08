 package net.cortexmodders.atomtech.tileentity;
 
 import net.cortexmodders.atomtech.blocks.BlockLaptop;
 import net.cortexmodders.atomtech.blocks.ModBlocks;
 import net.cortexmodders.atomtech.item.ModItems;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet132TileEntityData;
 import net.minecraft.util.AxisAlignedBB;
 
 public class TileEntityLaptop extends TilePoweredBase implements IInventory
 {
     private byte data = 7;
     private ItemStack[] inv;
 
     private float lidAngleX = -180.0F;
     public static final float LID_ANGLE_OPEN = -276.0F;
     public static final float LID_ANGLE_CLOSED = -180.0F;
 
     // local coords
     public final static AxisAlignedBB flashDriveHitPosition = AxisAlignedBB.getAABBPool().getAABB(1, 0, 0.3125, 1, 0.125, 0.375);
 
     public TileEntityLaptop()
     {
         super(20000);
         this.inv = new ItemStack[1];
         this.powerUsedTick = 1;
     }
 
     @Override
     public boolean canRecievePower()
     {
         return !this.isBroken();
     }
 
     @Override
     public void closeChest() {}
 
     @Override
     public ItemStack decrStackSize(final int slot, final int amt) 
     {
         ItemStack stack = this.getStackInSlot(slot);
         if (stack != null)
             if (stack.stackSize <= amt)
                 this.setInventorySlotContents(slot, null);
             else
             {
                 stack = stack.splitStack(amt);
                 if (stack.stackSize == 0)
                     this.setInventorySlotContents(slot, null);
             }
         return stack;
     }
 
     /**
      * makes the laptop more broken.
      * 
      */
     public void degradeCondition()
     {
         if(this.getCondition() < 2)
             this.data++;
     }
 
     public void fix()
     {
         data &= ~3;
     }
 
     /**
      * condition of laptop. 0 = best, 1 = ok, 2 = broken.
      */
     public byte getCondition()
     {
         return (byte) (data & 3);
     }
 
     public byte getData()
     {
         return this.data;
     }
 
     @Override
     public Packet getDescriptionPacket()
     {
         NBTTagCompound tag = new NBTTagCompound();
         this.writeToNBT(tag);
         return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 0, tag);
     }
 
     @Override
     public int getInventoryStackLimit() {
         return 1;
     }
 
     @Override
     public String getInvName()
     {
         return "tileentitylaptop";
     }
 
     public float getLidAngle() {
         return this.lidAngleX;
     }
 
     @Override
     public int getSizeInventory() {
         return this.inv.length;
     }
 
     @Override
     public ItemStack getStackInSlot(final int slot) {
         return this.inv[slot];
     }
 
     @Override
     public ItemStack getStackInSlotOnClosing(final int slot) {
         ItemStack stack = this.getStackInSlot(slot);
         if (stack != null)
             this.setInventorySlotContents(slot, null);
         return stack;
     }
 
     public byte getState()
     {
     	return (byte) ((data >> 3) & 7);
     }
 
     public boolean hasFlashDrive()
     {
         return this.isItemValidForSlot(0, this.getStackInSlot(0));
     }
 
     public void incrementState()
     {
     	this.setState((byte) (this.getState() + 1));
     }
     
     /**
      * returns if laptop is broken or not.
      * 
      * @return
      */
     public boolean isBroken()
     {
         return (data & 3) >= 2;
     }
     
     @Override
     public boolean isInvNameLocalized()
     {
         return false;
     }
     
     @Override
     public boolean isItemValidForSlot(final int slot, final ItemStack stack)
     {
         if(stack != null && stack.getItem().equals(ModItems.flashDrive) && slot == 0)
             return true;
         return false;
     }
 
     public boolean isLidClosed()
     {
         return (data & 4) != 0;
     }
 
     @Override
     public boolean isUseableByPlayer(final EntityPlayer player) {
         return this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord, this.zCoord) == this &&
                 player.getDistanceSq(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5) < 64;
     }
 
     @Override
     public void onDataPacket(final INetworkManager net, final Packet132TileEntityData pkt)
     {
         NBTTagCompound tag = pkt.data;
         this.readFromNBT(tag);
     }
 
     @Override
     public void openChest() {}
 
     @Override
     public void readFromNBT(final NBTTagCompound tag)
     {
         super.readFromNBT(tag);
         NBTTagList tagList = tag.getTagList("Inventory");
         for (int i = 0; i < tagList.tagCount(); i++) {
             NBTTagCompound tag2 = (NBTTagCompound) tagList.tagAt(i);
             byte slot = tag2.getByte("Slot");
             if (slot >= 0 && slot < this.inv.length)
                 this.inv[slot] = ItemStack.loadItemStackFromNBT(tag2);
         }
         this.data = tag.getByte("data");
         this.lidAngleX = tag.getFloat("lidAngle");
     }
 
     public void setCondition(final byte condition)
     {
         data &= ~3;
         data |= condition & 3;
     }
 
     public void setData(final byte data)
     {
         this.data = data;
     }
 
     @Override
     public void setInventorySlotContents(final int slot, final ItemStack stack) {
         this.inv[slot] = stack;
         if (stack != null && stack.stackSize > this.getInventoryStackLimit())
             stack.stackSize = this.getInventoryStackLimit();
     }
 
     public void setLidClosed(final boolean closeLid) 
     {
         if(closeLid)
         {
             data |= 4;
         }
         else
         {
             data &= ~4;
         }
     }
 
     public void setState(final byte newState)
     {
     	data &= ~56;
     	data |= ((newState & 7) << 3);
     }
 
     public void toggleLid()
     {
         this.setLidClosed(!this.isLidClosed());
     }
 
     @Override
     public void updateEntity()
     {
         if(!this.worldObj.isRemote)
         {
             if(this.worldObj.getBlockId(this.xCoord, this.yCoord, this.zCoord) != ModBlocks.laptop.blockID)
                 this.worldObj.removeBlockTileEntity(this.xCoord, this.yCoord, this.zCoord);
             
             if(this.powerLevel > 0)
             	this.setPower(this.getPower() - this.powerUsedTick);
             System.out.println(this.getPower());
         }
         if(!this.isBroken())
         {
             if(this.isLidClosed() && this.lidAngleX != LID_ANGLE_CLOSED)
                 this.lidAngleX += 4.0F;
             else if(!this.isLidClosed() && this.lidAngleX != LID_ANGLE_OPEN)
                 this.lidAngleX -= 4.0F;
         }
         else if(this.hasFlashDrive() && !this.worldObj.isRemote)
             BlockLaptop.ejectFlashDrive(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
     }
 
     @Override
     public void writeToNBT(final NBTTagCompound tag)
     {
         super.writeToNBT(tag);
         tag.setByte("data", this.data);
         tag.setFloat("lidAngle", this.lidAngleX);
         NBTTagList itemList = new NBTTagList();
         for (int i = 0; i < this.inv.length; i++) {
             ItemStack stack = this.inv[i];
             if (stack != null) {
                 NBTTagCompound tag2 = new NBTTagCompound();
                 tag2.setByte("Slot", (byte) i);
                 stack.writeToNBT(tag2);
                 itemList.appendTag(tag2);
             }
         }
         tag.setTag("Inventory", itemList);
     }
 }
