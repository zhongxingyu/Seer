 package net.cortexmodders.atomtech.tileentity;
 
 import net.cortexmodders.atomtech.lib.ATLogger;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet132TileEntityData;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.tileentity.TileEntityFurnace;
 import net.minecraftforge.common.ForgeDirection;
 import universalelectricity.api.CompatibilityModule;
 import universalelectricity.api.electricity.IVoltageOutput;
 import universalelectricity.api.energy.IConductor;
 import universalelectricity.api.energy.IEnergyContainer;
 import universalelectricity.api.energy.IEnergyInterface;
 import universalelectricity.api.vector.Vector3;
 import universalelectricity.api.vector.VectorHelper;
 
 public class TileEntityCoalGenerator extends AbstractTileElectricity implements IInventory, IEnergyInterface, IEnergyContainer, IVoltageOutput
 {
     
     private int fuelLevel = 0;
     private ItemStack fuelStack;
     
     public TileEntityCoalGenerator()
     {
         this.maxEnergy = 10;
     }
     
     // My methods
     
     public int addFuel(ItemStack item)
     {
         if(fuelStack != null)
         {
             if(fuelStack.itemID == item.itemID)
             {
                 if(fuelStack.stackSize < this.fuelStack.getMaxStackSize())
                 {
                     int sum = fuelStack.stackSize + item.stackSize;
                     int remainder = 0;
                     if(sum > this.fuelStack.getMaxStackSize())
                     {
                         remainder = this.fuelStack.getMaxStackSize() - sum;
                     }
                     
                     fuelStack.stackSize += (item.stackSize + remainder);
                     ATLogger.info(String.format("Current Fuel: %s, Remainder: %s", fuelStack.stackSize, remainder));
                     
                     return Math.abs(remainder);
                 }
             }
         }
         else
         {
             fuelStack = item;
         }
         
         return 0;
     } 
     
     public int getFuelLevel()
     {
         return this.fuelLevel;
     }
     
     public boolean onBlockActivated(EntityPlayer player, int side, float hitX, float hitY, float hitZ)
     {
         ItemStack heldItem = player.getHeldItem();
         ATLogger.info("Activated Block! ");
         if (heldItem != null)
         {
             if(TileEntityFurnace.getItemBurnTime(heldItem) > 0)
             {
                 int remainder = this.addFuel(heldItem);
                 
                 heldItem.stackSize = remainder;
                 
                 return remainder != 0;
             }
         }
         else
         {
             if(fuelStack != null && fuelStack.stackSize > 0)
             {
                 EntityItem item = new EntityItem(this.worldObj, this.xCoord, this.yCoord, this.zCoord, fuelStack);
                 this.worldObj.spawnEntityInWorld(item);
                 System.out.println("Extract item.");
             }
         }
         
         return false;
     }
     
     public void onBlockBroken()
     {
         
     }
     
     // TileEntity methods
     
     @Override
     public void updateEntity()
     {
         if(!this.worldObj.isRemote)
         {            
             if (this.fuelLevel > 0)
             {
                 if (this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord) < 4)
                     this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord) + 4, 3);
                 
                 if (this.fuelLevel % 10 == 0)
                 {
                     this.energy += 1;
                     this.produce();
                 }
                 
                 this.fuelLevel--;
                 //ATLogger.info("Fuel Level: " + fuelLevel);
             }
             else if(fuelLevel <= 0)
             {
                 if(this.fuelStack != null && this.fuelStack.stackSize > 0)
                 {
                     this.fuelStack.stackSize--;
                     this.fuelLevel = TileEntityFurnace.getItemBurnTime(this.fuelStack);
                     ATLogger.info("Get new fuel." + this.fuelStack.stackSize);
                 }
             }
            
            if (this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord) > 3)
             {
                 this.fuelLevel = 0;
                 this.fuelStack = null;
                 this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord) - 4, 3);
             }
         }
         
     }
     
     @Override
     public Packet getDescriptionPacket()
     {
         NBTTagCompound tag = new NBTTagCompound();
         this.writeToNBT(tag);
         return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 0, tag);
     }
     
     @Override
     public void onDataPacket(INetworkManager net, Packet132TileEntityData pkt)
     {
         NBTTagCompound tag = pkt.data;
         this.readFromNBT(tag);
     }
     
     @Override
     public void readFromNBT(NBTTagCompound tag)
     {
         super.readFromNBT(tag);
         this.fuelLevel = tag.getInteger("Fuel");
         this.fuelStack = ItemStack.loadItemStackFromNBT((NBTTagCompound) tag.getTag("FuelStack"));
     }
     
     @Override
     public void writeToNBT(NBTTagCompound tag)
     {
         super.writeToNBT(tag);
         
         tag.setInteger("Fuel", this.fuelLevel);
         NBTTagCompound itemTag = new NBTTagCompound();
         if(this.fuelStack != null)
             this.fuelStack.writeToNBT(itemTag);
         
         tag.setTag("FuelStack", itemTag);
     }
     
     // inventory methods
     
     @Override
     public int getSizeInventory()
     {
         return 1;
     }
     
     @Override
     public ItemStack getStackInSlot(int i)
     {
         return this.fuelStack;
     }
     
     @Override
     public ItemStack getStackInSlotOnClosing(int i)
     {
         if (this.fuelStack != null)
         {
             ItemStack itemstack = this.fuelStack;
             this.fuelStack = null;
             return itemstack;
         }
         else
             return null;
     }
     
     @Override
     public boolean isInvNameLocalized()
     {
         return false;
     }
     
     @Override
     public boolean isItemValidForSlot(int i, ItemStack itemstack)
     {
         return false;
     }
     
     @Override
     public boolean isUseableByPlayer(EntityPlayer entityplayer)
     {
         return true;
     }
     
     @Override
     public ItemStack decrStackSize(int i, int j)
     {
         if (this.fuelStack != null)
         {
             ItemStack itemstack;
             
             if (this.fuelStack.stackSize <= j)
             {
                 itemstack = this.fuelStack;
                 this.fuelStack = null;
                 return itemstack;
             }
             else
             {
                 itemstack = this.fuelStack.splitStack(j);
                 
                 if (this.fuelStack.stackSize == 0)
                     this.fuelStack = null;
                 
                 return itemstack;
             }
         }
         else
             return null;
     }
     
     @Override
     public void setInventorySlotContents(int i, ItemStack itemstack)
     {
         this.fuelStack = itemstack;
         
         if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit())
             itemstack.stackSize = this.getInventoryStackLimit();
         
     }
     
     @Override
     public int getInventoryStackLimit()
     {
         return 64;
     }
     
     @Override
     public String getInvName()
     {
         return "";
     }
     
     @Override
     public void openChest()
     {
     }
     
     @Override
     public void closeChest()
     {
     }
     
     // Universal Electricity Methods
     
     @Override
     public boolean canConnect(ForgeDirection direction)
     {
         TileEntity tile = VectorHelper.getTileEntityFromSide(worldObj, new Vector3(this), direction);
         if(tile instanceof IConductor)
             return true;
         
         return false;
     }
     
     @Override
     public long getEnergy(ForgeDirection direction)
     {
         return energy;
     }
     
     @Override
     public long getEnergyCapacity(ForgeDirection direction)
     {
         return this.maxEnergy;
     }
     
     @Override
     public void setEnergy(ForgeDirection direction, long energy)
     {
         this.energy = energy;
     }
     
     @Override
     public long onExtractEnergy(ForgeDirection direction, long extract, boolean doExtract)
     {
         //TODO fix this
         if(doExtract && canConnect(direction))
             return energy -= maxExtract;
         
         return 0;
     }
     
     @Override
     public long onReceiveEnergy(ForgeDirection arg0, long arg1, boolean arg2)
     {
         return 0;
     }
     
     @Override
     public long getVoltageOutput(ForgeDirection side)
     {
         return 0;
     }
     
     @Override
     protected long produce()
     {
         long totalUsed = 0;
         
         for (ForgeDirection direction : this.getOutputDirections())
         {
             if (this.getEnergy(direction) > 0)
             {
                 TileEntity tileEntity = new Vector3(this).modifyPositionFromSide(direction).getTileEntity(this.worldObj);
                 
                 if (tileEntity != null)
                 {
                     long used = CompatibilityModule.receiveEnergy(tileEntity, direction.getOpposite(), this.onExtractEnergy(direction, this.getEnergy(direction), false), true);
                     this.onExtractEnergy(direction, used, true);
                     totalUsed += used;
                 }
             }
         }
         
         return totalUsed;
     }
 }
