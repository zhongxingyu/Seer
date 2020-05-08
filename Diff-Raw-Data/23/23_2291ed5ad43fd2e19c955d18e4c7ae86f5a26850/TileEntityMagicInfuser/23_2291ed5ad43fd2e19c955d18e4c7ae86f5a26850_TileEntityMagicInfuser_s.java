 package net.blazecoding.magicpowders.tileentity;
 
 import net.blazecoding.magicpowders.item.ModItems;
 import net.blazecoding.magicpowders.recipe.MagicInfuserRecipes;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.tileentity.TileEntity;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 public class TileEntityMagicInfuser extends TileEntity implements IInventory {
 
 	private ItemStack infuserItemStacks[];
 
 	public int dualBurnTime;
 	public int currentItemBurnTime;
 	public int dualCookTime;
 
 	private String magicInfuserName;
 
 	public TileEntityMagicInfuser() {
 		infuserItemStacks = new ItemStack[4];
 		dualBurnTime = 0;
 		currentItemBurnTime = 0;
 		dualCookTime = 0;
 	}
 
 	public int getSizeInventory() {
 		return infuserItemStacks.length;
 	}
 
 	public ItemStack getStackInSlot(int i) {
 		return infuserItemStacks[i];
 	}
 
 	public void setInventorySlotConatainers(int i, ItemStack itemstack) {
 
 		infuserItemStacks[i] = itemstack;
 
 		if (itemstack != null && itemstack.stackSize > getInventoryStackLimit()) {
 			itemstack.stackSize = getInventoryStackLimit();
 		}
 
 	}
 
 	public void readFromNBT(NBTTagCompound nbttagcompound) {
 
 		super.readFromNBT(nbttagcompound);
		NBTTagList nbttaglist = nbttagcompound.getTagList("Items");
 		infuserItemStacks = new ItemStack[getSizeInventory()];
 
 		for (int i = 0; i < nbttaglist.tagCount(); i++) {
 
 			NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbttaglist.tagAt(i);
			byte byte0 = nbttagcompound1.getByte("Slot");
 
 			if (byte0 >= 0 && byte0 < infuserItemStacks.length) {
 				infuserItemStacks[byte0] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
 			}
 
 		}
 
		dualBurnTime = nbttagcompound.getShort("BurnTime");
		dualCookTime = nbttagcompound.getShort("CookTime");
 		currentItemBurnTime = getItemBurnTime(infuserItemStacks[1]);
 
 	}
 
 	public void writeToNBT(NBTTagCompound nbttagcompound) {
 		super.writeToNBT(nbttagcompound);
		nbttagcompound.setShort("BurnTime", (short) dualBurnTime);
		nbttagcompound.setShort("CookTime", (short) dualCookTime);
 		NBTTagList nbttaglist = new NBTTagList();
 
 		for (int i = 0; i < infuserItemStacks.length; i++) {
 
 			if (infuserItemStacks[i] != null) {
 
 				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
 				nbttagcompound1.setByte("Slot", (byte) i);
 				infuserItemStacks[i].writeToNBT(nbttagcompound1);
 				nbttaglist.appendTag(nbttagcompound1);
 
 			}
 
 		}
 		nbttagcompound.setTag("Items", nbttaglist);
 	}
 
 	public int getInventoryStackLimit() {
 		return 64;
 	}
 
 	public int getCookProgressScaled(int i) {
 		return (dualCookTime * i) / 200;
 	}
 
 	public int getBurnTimeRemainingScaled(int i) {
 
 		if (currentItemBurnTime == 0) {
 			currentItemBurnTime = 200;
 		}
 
 		return (dualBurnTime * i) / currentItemBurnTime;
 
 	}
 
 	public boolean isBurning() {
 		return dualBurnTime > 0;
 	}
 
 	public void updateEntity() {
 
 		boolean flag = dualBurnTime > 0;
 		boolean flag1 = false;
 
 		if (dualBurnTime > 0) {
 			dualBurnTime--;
 		}
 
 		if (!worldObj.isRemote) {
 
 			if (dualBurnTime == 0 && canSmelt()) {
 
 				currentItemBurnTime = dualBurnTime = getItemBurnTime(infuserItemStacks[2]);
 
 				if (dualBurnTime > 0) {
 
 					flag1 = true;
 
 					if (infuserItemStacks[2] != null) {
 
 						if (infuserItemStacks[2].stackSize == 0) {
 
 							infuserItemStacks[2] = new ItemStack(infuserItemStacks[2].getItem().setFull3D());
 
 						} else {
 
 							infuserItemStacks[2].stackSize--;
 
 						}
 
 						if (infuserItemStacks[2].stackSize == 0) {
 							infuserItemStacks[2] = null;
 						}
 
 					}
 
 				}
 
 			}
 
 			if (isBurning() && canSmelt()) {
 
 				dualCookTime++;
 
 				if (dualCookTime == 200) {
 
 					dualCookTime = 0;
 					smeltItem();
 					flag1 = true;
 
 				}
 
 			} else {
 
 				dualCookTime = 0;
 
 			}
 
 			if (flag != (dualBurnTime > 0)) {
 				flag1 = true;
 			}
 
 		}
 
 		if (flag1) {
 			onInventoryChanged();
 		}
 
 	}
 
 	private boolean canSmelt() {
 
 		if (infuserItemStacks[0] == null || infuserItemStacks[1] == null) {
 			return false;
 		}
 
		ItemStack itemstack = MagicInfuserRecipes.getSmeltingResult(infuserItemStacks[0].getItem().itemID, infuserItemStacks[1].getItem().itemID);
 
 		if (itemstack == null) {
 			return false;
 		}
 
 		if (infuserItemStacks[3] == null) {
 			return true;
 		}
 
 		if (!infuserItemStacks[3].isItemEqual(itemstack)) {
 			return false;
 		}
 
 		if (infuserItemStacks[3].stackSize < getInventoryStackLimit() && infuserItemStacks[3].stackSize < infuserItemStacks[3].getMaxStackSize()) {
 			return true;
 
 		} else {
 			return infuserItemStacks[3].stackSize < itemstack.getMaxStackSize();
 		}
 
 	}
 
 	public void smeltItem() {
 
 		if (!canSmelt()) {
 			return;
 		}
 
		ItemStack itemstack = MagicInfuserRecipes.getSmeltingResult(infuserItemStacks[0].getItem().itemID, infuserItemStacks[1].getItem().itemID);
 
 		if (infuserItemStacks[3] == null) {
 
 			infuserItemStacks[3] = itemstack.copy();
 
 		} else if (infuserItemStacks[3].itemID == itemstack.itemID) {
 			infuserItemStacks[3].stackSize++;
 		}
 
 		for (int i = 0; i < 2; i++) {
 
 			if (infuserItemStacks[i].stackSize <= 0) {
 				infuserItemStacks[i] = new ItemStack(infuserItemStacks[i].getItem().setFull3D());
 
 			} else {
 
 				infuserItemStacks[i].stackSize--;
 			}
 
 			if (infuserItemStacks[i].stackSize <= 0) {
 				infuserItemStacks[i] = null;
 			}
 
 		}
 
 	}
 
 	private int getItemBurnTime(ItemStack itemstack) {
 
 		if (itemstack == null) {
 			return 0;
 		}
 
 		int i = itemstack.getItem().itemID;
 
 		if (i == ModItems.magicDust.itemID) {
 
 			return 2000;
 
 		} else {
 			return GameRegistry.getFuelValue(itemstack);
 		}
 
 	}
 
 	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
 
 		if (worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this) {
 			return false;
 		} else {
 			return entityplayer.getDistanceSq((double) xCoord + 0.5D, (double) yCoord + 0.5D, (double) zCoord + 0.5D) <= 64D;
 		}
 
 	}
 
 	public ItemStack decrStackSize(int i, int j) {
 
 		if (infuserItemStacks[i] != null) {
 
 			if (infuserItemStacks[i].stackSize <= j) {
 
 				ItemStack itemstack = infuserItemStacks[i];
 				infuserItemStacks[i] = null;
 				return itemstack;
 
 			}
 
 			ItemStack itemstack1 = infuserItemStacks[i].splitStack(j);
 
 			if (infuserItemStacks[i].stackSize == 0) {
 				infuserItemStacks[i] = null;
 			}
 
 			return itemstack1;
 
 		} else {
 			return null;
 		}
 
 	}
 
 	public void setInventorySlotContents(int i, ItemStack itemstack) {
 
 		infuserItemStacks[i] = itemstack;
 
 		if (itemstack != null && itemstack.stackSize > getInventoryStackLimit()) {
 			itemstack.stackSize = getInventoryStackLimit();
 		}
 
 	}
 
 	public String getInvName() {
 		return "container.infuser";
 	}
 
 	public void openChest() {
 	}
 
 	public void closeChest() {
 	}
 
 	public ItemStack getStackInSlotOnClosing(int i) {
 
 		if (infuserItemStacks[i] != null) {
 
 			ItemStack itemstack = infuserItemStacks[i];
 			infuserItemStacks[i] = null;
 			return itemstack;
 
 		} else {
 
 			return null;
 
 		}
 
 	}
 
 	public boolean isInvNameLocalized() {
 		return (this.magicInfuserName != null) && (this.magicInfuserName.length() > 0);
 	}
 
 	public void setCustomName(String name) {
 		this.magicInfuserName = name;
 	}
 
 	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
 		return false;
 	}
 
 }
