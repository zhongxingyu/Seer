 /*
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version. This program is distributed in the hope that it will be
  * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
  * Public License for more details. You should have received a copy of the GNU
  * Lesser General Public License along with this program. If not, see
  * <http://www.gnu.org/licenses/>
  */
 package slimevoid.tmf.machines.tileentities;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import slimevoid.lib.util.SlimevoidHelper;
 import slimevoid.tmf.fuel.IFuelHandlerTMF;
 import slimevoid.tmf.minerals.items.ItemMineral;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet132TileEntityData;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.tileentity.TileEntityFurnace;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ISidedInventory;
 
 public abstract class TileEntityMachine extends TileEntity implements IInventory, ISidedInventory {
 	/** The number of ticks that the machine will keep burning */
 	public int burnTime = 0;
 	/** The number of ticks that a fresh copy of the currently-burning item would keep the furnace burning for */
 	public int currentItemBurnTime = 0;
 	
 	/** The number of ticks that the current item has been cooking for */
 	public int cookTime = 0;
 	/** The number of ticks that a fresh copy of the currently-burning item would take to cook */
 	public int currentItemCookTime = 0;
 	
 	/** The currently-burning item's width */
 	public int currentItemWidth = 0;
 		
 	@Override
 	public boolean isUseableByPlayer(EntityPlayer player) {
 		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this 
 				? false 
 				: SlimevoidHelper.isUseableByPlayer(
						this.worldObj,
 						player,
 						this.xCoord,
 						this.yCoord,
 						this.zCoord,
 						0.5D,
 						0.5D,
 						0.5D,
 						64.0D);
 	}
 
 	@Override
 	public void openChest() {}
 
 	@Override
 	public void closeChest() {}
 
 	@Override
 	public ItemStack decrStackSize(int index, int ammount) {
 		if (getStackInSlot(index) != null) {
 			ItemStack newStack;
 			
 			if (getStackInSlot(index).stackSize <= ammount) {
 				newStack = getStackInSlot(index);
 				setInventorySlotContents(index, null);
 				return newStack;
 			} else {
 				newStack = getStackInSlot(index).splitStack(ammount);
 				
 				if (getStackInSlot(index).stackSize == 0) {
 					setInventorySlotContents(index, null);
 				}
 				
 				return newStack;
 			}
 		} else {
 			return null;
 		}
 	}
 	
 	@Override
 	public ItemStack getStackInSlotOnClosing(int index) {
 		if (getStackInSlot(index) != null) {
 			ItemStack stack = getStackInSlot(index);
 			setInventorySlotContents(index, null);
 			return stack;
 		} else {
 			return null;
 		}
 	}
 	
 	@Override
 	public int getInventoryStackLimit() {
 		return 64;
 	}
 	
 	@Override
 	public void readFromNBT(NBTTagCompound ntbCompound) {
 		super.readFromNBT(ntbCompound);
 		
 		burnTime = ntbCompound.getShort("BurnTime");
         cookTime = ntbCompound.getShort("CookTime");
         currentItemBurnTime = ntbCompound.getShort("CurrentBurnTime");
         currentItemCookTime = ntbCompound.getShort("CurrentCookTime");
         currentItemWidth = ntbCompound.getShort("CurrentWidth");
 	}
 	
 	@Override
 	public void writeToNBT(NBTTagCompound ntbCompound) {
 		super.writeToNBT(ntbCompound);
 		
 		ntbCompound.setShort("BurnTime", (short)burnTime);
 		ntbCompound.setShort("CookTime", (short)cookTime);
 		ntbCompound.setShort("CurrentBurnTime", (short)currentItemBurnTime);
 		ntbCompound.setShort("CurrentCookTime", (short)currentItemCookTime);
 		ntbCompound.setShort("CurrentWidth", (short)currentItemWidth);
 	}
 	
 	@Override
 	public void updateEntity() {
 		boolean wasBurning = isBurning();
 		boolean inventoryChanged = false;
 		
 		if ( isBurning() )
 			--burnTime;
 		
 		if ( !worldObj.isRemote ) {
 			if ( !isBurning() && canSmelt() ) {
 				currentItemBurnTime = burnTime = getCurrentFuelBurnTime();
 				currentItemCookTime = getCurrentFuelBurnSpeed();
 				currentItemWidth = getCurrentFuelBurnWidth();
 				if ( burnTime > 0 ) {
 					inventoryChanged = true;
 					if ( getCurrentFuelStack() != null ) {
 						--getCurrentFuelStack().stackSize;
 						
 						if ( getCurrentFuelStack().stackSize == 0 ) {
 							setCurrentFuelStack(getCurrentFuelStack().getItem().getContainerItemStack(getCurrentFuelStack()));
 						}
 					}
 				}
 			}
 
 			if ( isBurning() && canSmelt() ) {
 				++cookTime;
 				if ( cookTime == currentItemCookTime ) {
 					cookTime = 0;
 					smeltItem();
 					inventoryChanged = true;
 				}
 			} else {
 				cookTime = 0;
 			}
 			
 			if ( wasBurning != isBurning() ) {
 				inventoryChanged = true;
 				updateMachineBlockState(isBurning(), worldObj, xCoord, yCoord, zCoord);
 			}
 		}
 		
 		if ( inventoryChanged ) {
 			onInventoryChanged();
 		}
 	}
 
 	public boolean isBurning() {
 		return burnTime > 0;
 	}
     
     
 	public abstract void smeltItem();
 	protected abstract boolean canSmelt();
 	
 	public abstract int getCurrentFuelBurnTime();
 	public abstract int getCurrentFuelBurnSpeed();
 	public abstract int getCurrentFuelBurnWidth();
 	public abstract ItemStack getCurrentFuelStack();
 	public abstract void setCurrentFuelStack(ItemStack stack);
 	public abstract void updateMachineBlockState(boolean isBurning, World world, int x, int y, int z);
 
 	public int getItemBurnTime(ItemStack stack) {
 		if ( stack == null )
 			return 0;
 
 		if ( stack.getItem() instanceof IFuelHandlerTMF ) {
 			return ((ItemMineral)stack.getItem()).getBurnTime(stack);
 		} else {
 			return TileEntityFurnace.getItemBurnTime(stack);
 		}
 	}
 	public int getItemBurnSpeed(ItemStack stack) {
 		if ( stack == null )
 			return 0;
 		
 		if ( stack.getItem() instanceof IFuelHandlerTMF ) {
 			return ((ItemMineral)stack.getItem()).getBurnSpeed(stack);
 		} else {
 			return 200;
 		}
 	}
 	public int getItemBurnWidth(ItemStack stack) {
 		if ( stack == null )
 			return 0;
 		
 		if ( stack.getItem() instanceof IFuelHandlerTMF ) {
 			return ((ItemMineral)stack.getItem()).getBurnWidth(stack);
 		} else {
 			return 1;
 		}
 	}
 	public boolean isItemFuel(ItemStack stack) {
 		return getItemBurnTime(stack) > 0 && getItemBurnSpeed(stack) > 0 && getItemBurnWidth(stack) > 0;
 	}
 	
 	@Override
 	public Packet getDescriptionPacket() {
 		NBTTagCompound nbttagcompound = new NBTTagCompound();
 		this.writeToNBT(nbttagcompound);
 		return new Packet132TileEntityData(
 				this.xCoord,
 				this.yCoord,
 				this.zCoord,
 				0,
 				nbttagcompound);
 	}
 	
 	@Override
 	public void onDataPacket(INetworkManager netmanager, Packet132TileEntityData pkt) {
 		this.readFromNBT(pkt.customParam1);
 		this.onInventoryChanged();
 	}
 	
 	@SideOnly(Side.CLIENT)
 	public int getCookProgressScaled(int par1) {
 		if ( currentItemCookTime <= 0 )
 			return 0;
 		
     	return cookTime * par1 / currentItemCookTime;
 	}
 	
 	@SideOnly(Side.CLIENT)
 	public int getBurnTimeRemainingScaled(int par1) {
 		if (currentItemBurnTime == 0) {
 			currentItemBurnTime = 200;
 		}
 	
 		return burnTime * par1 / currentItemBurnTime;
 	}
 	
 	@Override
 	public void onInventoryChanged() {
 		super.onInventoryChanged();
 		this.onInventoryHasChanged(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
 	}
 
 	/**
 	 * If we need to send information to the client
 	 * it should be done here
 	 */
 	protected abstract void onInventoryHasChanged(World world, int x, int y, int z);
 }
