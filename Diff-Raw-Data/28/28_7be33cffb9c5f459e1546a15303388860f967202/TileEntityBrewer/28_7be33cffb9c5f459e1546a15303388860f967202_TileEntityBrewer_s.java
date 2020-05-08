 package com.teammetallurgy.agriculture.machines.brewer;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet132TileEntityData;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.fluids.Fluid;
 import net.minecraftforge.fluids.FluidStack;
 import net.minecraftforge.fluids.FluidTank;
 import net.minecraftforge.fluids.FluidTankInfo;
 import net.minecraftforge.fluids.IFluidHandler;
 
 import com.teammetallurgy.agriculture.Agriculture;
 import com.teammetallurgy.agriculture.machines.FuelMachineTileEntity;
 import com.teammetallurgy.agriculture.recipes.BrewerRecipes;
 
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.relauncher.Side;
 
 public class TileEntityBrewer extends FuelMachineTileEntity implements IFluidHandler {
     private int amountLeftInput;
 
     private int amountRightInput;
     private final InventoryBrewer cabinet = new InventoryBrewer("", false, 3, this);
 
     private FluidStack fluidStack;
     private boolean hasUpdate = false;
 
     float leftDoorAngle;
 
     private FluidTank leftTank = new FluidTank(8000);
 
     float liquidAmount;
     float maxLiquidAmount;
 
     private int maxProcessingTime;
     int numUsingPlayers;
 
     // Left door
     float prevLeftDoorAngle;
     // Right door
     double prevRightDoorAngle;
 
     private boolean processing = false;
     private int processingTime;
 
     float rightDoorAngle;
 
     private FluidTank rightTank = new FluidTank(8000);
 
     float update = 0;
 
     @Override
     public boolean canDrain(final ForgeDirection from, final Fluid fluid)
     {
         return true;
     }
 
     @Override
     public boolean canFill(final ForgeDirection from, final Fluid fluid)
     {
         return true;
     }
 
     @Override
     public FluidStack drain(final ForgeDirection from, final FluidStack resource, final boolean doDrain)
     {
         if (resource == null) { return null; }
 
         if (!resource.isFluidEqual(rightTank.getFluid())) { return null; }
 
         return this.drain(from, resource.amount, doDrain);
     }
 
     @Override
     public FluidStack drain(final ForgeDirection from, final int maxDrain, final boolean doDrain)
     {
         return rightTank.drain(maxDrain, doDrain);
     }
 
     @Override
     public int fill(final ForgeDirection from, FluidStack resource, final boolean doFill)
     {
        if (resource == null) { return 0; }

        resource = resource.copy();
        int totalUsed = 0;

        final FluidStack fluid = leftTank.getFluid();

        if (fluid != null && fluid.amount > 0 && !fluid.isFluidEqual(resource)) { return 0; }

        while (resource.amount > 0)
        {
            final int used = leftTank.fill(resource, doFill);
            resource.amount -= used;
            if (used > 0)
            {
                hasUpdate = true;
            }

            totalUsed += used;
         }
        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        return totalUsed;
     }
 
     public int getAmountRightInput()
     {
         return amountRightInput;
     }
 
     public IInventory getBrewer()
     {
         return cabinet;
     }
 
     @Override
     public Packet getDescriptionPacket()
     {
         final NBTTagCompound tag = new NBTTagCompound();
         writeCustomNBT(tag);
         return new Packet132TileEntityData(xCoord, yCoord, zCoord, 1, tag);
     }
 
     @Override
     public IInventory getFuelInventory()
     {
         return cabinet;
     }
 
     @Override
     public int getFuelSlot()
     {
         return 1;
     }
 
     public IInventory getInventory()
     {
         return cabinet;
     }
 
     public FluidTank getLeftTank()
     {
         return leftTank;
     }
 
     public float getLiquidScaled()
     {
         return rightTank.getFluidAmount() / (float) rightTank.getCapacity();
     }
 
     public float getProcessScaled()
     {
         final float i = processingTime / (float) maxProcessingTime;
 
         if (i <= 0) { return 0; }
 
         return i;
     }
 
     public FluidTank getRightTank()
     {
         return rightTank;
     }
 
     @Override
     public FluidTankInfo[] getTankInfo(final ForgeDirection from)
     {
         return new FluidTankInfo[] { rightTank.getInfo() };
     }
 
     public boolean isLiquidContainer(final ItemStack stack)
     {
         return false;
     }
 
     @Override
     public void onDataPacket(final INetworkManager net, final Packet132TileEntityData pkt)
     {
         readCustomNBT(pkt.data);
     }
 
     @Override
     public void onInventoryChanged()
     {
     }
 
     private void process()
     {
         if (fluidStack != null)
         {
 
         }
 
     }
 
     @Override
     public void readCustomNBT(final NBTTagCompound tag)
     {
         final NBTTagList tagList = tag.getTagList("Items");
 
         for (int i = 0; i < tagList.tagCount(); i++)
         {
             final NBTTagCompound base = (NBTTagCompound) tagList.tagAt(i);
             final int slot = Integer.valueOf(base.getByte("Slot"));
             cabinet.setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(base));
         }
         final NBTTagCompound rightTankTag = (NBTTagCompound) tag.getTag("RightTank");
         final NBTTagCompound leftTankTag = (NBTTagCompound) tag.getTag("LeftTank");
         final NBTTagCompound proceedFluid = (NBTTagCompound) tag.getTag("ProcessedFluid");
 
         rightTank = rightTank.readFromNBT(rightTankTag);
         leftTank = leftTank.readFromNBT(leftTankTag);
 
         if (proceedFluid != null)
         {
             fluidStack = FluidStack.loadFluidStackFromNBT(proceedFluid);
         }
 
         amountLeftInput = tag.getInteger("AmountLeftInput");
         amountRightInput = tag.getInteger("AmountRightInput");
 
         fuelRemaining = tag.getInteger("FuelRemaining");
 
         processingTime = tag.getInteger("ProcessingTime");
         maxProcessingTime = tag.getInteger("MaxProcessingTime");
     }
 
     @Override
     public void readFromNBT(final NBTTagCompound tag)
     {
         super.readFromNBT(tag);
         readCustomNBT(tag);
     }
 
     @Override
     public boolean receiveClientEvent(final int id, final int value)
     {
         if (id == 1)
         {
             numUsingPlayers = value;
             return true;
         }
         else
         {
             return super.receiveClientEvent(id, value);
         }
     }
 
     public void sendPacket()
     {
 
         final ByteArrayOutputStream bos = new ByteArrayOutputStream(140);
         final DataOutputStream dos = new DataOutputStream(bos);
         try
         {
             dos.writeShort(2);
             dos.writeInt(xCoord);
             dos.writeInt(yCoord);
             dos.writeInt(zCoord);
             dos.writeByte(direction);
 
             dos.writeInt(processingTime);
             dos.writeInt(maxProcessingTime);
             dos.writeInt(fuelRemaining);
             dos.writeInt(amountLeftInput);
             dos.writeInt(amountRightInput);
 
             dos.writeInt(leftTank.getFluidAmount());
             dos.writeInt(rightTank.getFluidAmount());
 
             dos.writeBoolean(processing);
 
         }
         catch (final IOException e)
         {
             System.out.println(e);
         }
 
         final Packet250CustomPayload packet = new Packet250CustomPayload();
         packet.channel = Agriculture.MODID;
         packet.data = bos.toByteArray();
         packet.length = bos.size();
         packet.isChunkDataPacket = true;
 
         if (packet != null)
         {
             if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
             {
                 PacketDispatcher.sendPacketToAllAround(xCoord, yCoord, zCoord, 16, worldObj.provider.dimensionId, packet);
             }
             else
             {
                 PacketDispatcher.sendPacketToServer(packet);
             }
         }
 
     }
 
     public void setAmountLeftInput(final int amountLeftInput)
     {
         this.amountLeftInput = amountLeftInput;
     }
 
     public void setAmountRightInput(final int amountRightInput)
     {
         this.amountRightInput = amountRightInput;
     }
 
     @Override
     public void setFuelRemaining(final int fuelRemaining)
     {
         this.fuelRemaining = fuelRemaining;
     }
 
     public void setMaxProcessingTime(final int maxProcessingTime)
     {
         this.maxProcessingTime = maxProcessingTime;
     }
 
     public void setProcessing(final boolean processing)
     {
         this.processing = processing;
     }
 
     public void setProcessingTime(final int processingTime)
     {
         this.processingTime = processingTime;
     }
 
     @Override
     public void updateEntity()
     {
 
         if (processingTime-- <= 0)
         {
             process();
             processing = false;
         }
 
         if (processingTime > 0)
         {
             --currentItemBurnTime;
         }
 
         if (fuelRemaining <= 0)
         {
             burnFuel();
         }
 
         if (update-- <= 0)
         {
             if (hasUpdate)
             {
                 sendPacket();
             }
 
             update = 10;
 
             if (amountLeftInput > 0)
             {
                 if (leftTank.getFluid() != null)
                 {
                     amountLeftInput -= 100;
 
                     final FluidStack fluidStack = leftTank.getFluid().copy();
 
                     fluidStack.amount = 100;
 
                     leftTank.fill(fluidStack, true);
                 }
                 else
                 {
                     amountLeftInput = 0;
                 }
             }
 
             if (amountLeftInput < 0)
             {
 
                 if (leftTank.getFluid() != null)
                 {
                     amountLeftInput += 100;
                     leftTank.drain(100, true);
                 }
                 else
                 {
                     amountLeftInput = 0;
                 }
             }
 
             if (amountRightInput > 0)
             {
                 if (rightTank.getFluid() != null)
                 {
                     amountRightInput -= 100;
 
                     final FluidStack fluidStack = rightTank.getFluid().copy();
 
                     fluidStack.amount = 100;
 
                     rightTank.fill(fluidStack, true);
                 }
                 else
                 {
                     amountRightInput = 0;
                 }
             }
 
             if (amountRightInput < 0)
             {
 
                 if (leftTank.getFluid() != null)
                 {
                     amountLeftInput += 100;
                     rightTank.drain(100, true);
                 }
                 else
                 {
                     amountRightInput = 0;
                 }
             }
 
             if (amountRightInput == 0)
             {
                 fluidStack = null;
             }
         }
 
         final ItemStack itemStack2 = cabinet.getStackInSlot(2);
         final ItemStack itemStack = cabinet.getStackInSlot(0);
 
         if (processingTime <= 0 && amountLeftInput == 0 && amountRightInput == 0)
         {
             if (itemStack2 != null)
             {
                 FluidStack fluidStack = BrewerRecipes.getInstance().findMatchingFluid(itemStack2, leftTank.getFluid());
 
                 if (fluidStack == null)
                 {
                     fluidStack = BrewerRecipes.getInstance().findMatchingFluid(itemStack2, null);
                 }
 
                 if (fluidStack != null && !processing && amountLeftInput == 0 && amountRightInput == 0)
                 {
                     maxProcessingTime = processingTime = BrewerRecipes.getInstance().getProcessTime(itemStack2);
                     if (leftTank.fill(fluidStack, false) == fluidStack.amount - amountLeftInput)
                     {
                         processing = true;
                         amountLeftInput += fluidStack.amount - 100;
 
                         leftTank.fill(new FluidStack(fluidStack, 100), true);
 
                         --itemStack2.stackSize;
 
                         if (itemStack2.stackSize <= 0)
                         {
                             cabinet.setInventorySlotContents(2, itemStack2.getItem().getContainerItemStack(itemStack2));
                         }
                         if (!worldObj.isRemote)
                         {
                             sendPacket();
                         }
                     }
                 }
             }
 
             if (itemStack != null)
             {
                 FluidStack fluidStack = BrewerRecipes.getInstance().findMatchingFluid(itemStack, leftTank.getFluid());
 
                 if (fluidStack == null)
                 {
                     fluidStack = BrewerRecipes.getInstance().findMatchingFluid(itemStack, null);
                 }
 
                 if (fluidStack != null)
                 {
                     if (!processing && amountLeftInput == 0 && amountRightInput == 0 && fuelRemaining > 0)
                     {
                         if (leftTank.getFluid() != null && !fluidStack.isFluidEqual(leftTank.getFluid()) && rightTank.fill(fluidStack, false) == fluidStack.amount - amountRightInput && processingTime <= 0)
                         {
                             maxProcessingTime = processingTime = BrewerRecipes.getInstance().getProcessTime(itemStack);
                             processing = true;
 
                             --itemStack.stackSize;
 
                             amountRightInput += fluidStack.amount - 100;
 
                             amountLeftInput += -amountRightInput;
 
                             final FluidStack stack = fluidStack.copy();
                             stack.amount = 100;
 
                             rightTank.fill(stack, true);
 
                             leftTank.drain(100, true);
 
                             if (itemStack.stackSize <= 0)
                             {
                                 cabinet.setInventorySlotContents(0, itemStack.getItem().getContainerItemStack(itemStack));
                             }
                             if (!worldObj.isRemote)
                             {
                                 sendPacket();
                             }
                         }
                     }
                 }
                 final ItemStack itemStackResult = BrewerRecipes.getInstance().findMatchingItem(itemStack, rightTank.getFluid());
 
                 if (itemStackResult != null)
                 {
                     --fuelRemaining;
                     if (itemStack.stackSize == 1 && !processing)
                     {
                         maxProcessingTime = processingTime = BrewerRecipes.getInstance().getProcessTime(itemStackResult);
                         processing = true;
                     }
 
                     if (processingTime <= 0 && processing)
                     {
                         cabinet.setInventorySlotContents(0, itemStackResult.copy());
                         rightTank.drain(1000, true);
                         processing = false;
                     }
                     if (!worldObj.isRemote)
                     {
                         sendPacket();
                     }
 
                 }
             }
         }
 
         prevLeftDoorAngle = leftDoorAngle;
         if (numUsingPlayers == 0 && leftDoorAngle > 0.0F || numUsingPlayers > 0 && leftDoorAngle < 1.0F)
         {
             if (numUsingPlayers > 0)
             {
                 leftDoorAngle += 0.1;
             }
             else
             {
                 leftDoorAngle -= 0.1;
             }
 
             if (leftDoorAngle > 1.0F)
             {
                 leftDoorAngle = 1.0F;
             }
 
             if (leftDoorAngle < 0.0F)
             {
                 leftDoorAngle = 0.0F;
             }
         }
     }
 
     @Override
     public void writeCustomNBT(final NBTTagCompound tag)
     {
 
         final NBTTagList nbtTagList = new NBTTagList();
         for (int i = 0; i < cabinet.getSizeInventory(); ++i)
         {
             if (cabinet.getStackInSlot(i) != null)
             {
                 final NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                 nbttagcompound1.setByte("Slot", (byte) i);
                 cabinet.getStackInSlot(i).writeToNBT(nbttagcompound1);
                 nbtTagList.appendTag(nbttagcompound1);
             }
         }
 
         final NBTTagCompound rightTankTag = new NBTTagCompound();
         final NBTTagCompound leftTankTag = new NBTTagCompound();
 
         rightTank.writeToNBT(rightTankTag);
         leftTank.writeToNBT(leftTankTag);
 
         tag.setTag("RightTank", rightTankTag);
         tag.setTag("LeftTank", leftTankTag);
 
         tag.setTag("Items", nbtTagList);
 
         tag.setInteger("AmountLeftInput", amountLeftInput);
         tag.setInteger("AmountRightInput", amountRightInput);
 
         tag.setInteger("FuelRemaining", fuelRemaining);
 
         tag.setInteger("ProcessingTime", processingTime);
         tag.setInteger("MaxProcessingTime", maxProcessingTime);
 
         final NBTTagCompound processedFluid = new NBTTagCompound();
 
         if (fluidStack != null)
         {
             fluidStack.writeToNBT(processedFluid);
         }
 
         tag.setTag("ProcessedFluid", processedFluid);
 
     }
 
     @Override
     public void writeToNBT(final NBTTagCompound tag)
     {
         super.writeToNBT(tag);
         writeCustomNBT(tag);
     }
 }
