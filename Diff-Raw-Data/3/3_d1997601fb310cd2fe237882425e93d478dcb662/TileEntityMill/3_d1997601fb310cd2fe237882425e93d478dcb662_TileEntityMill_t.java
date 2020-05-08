 package adanaran.mods.bfr.entities;
 
 import adanaran.mods.bfr.blocks.BlockStove;
 import adanaran.mods.bfr.crafting.MillRecipes;
 import adanaran.mods.bfr.inventory.ContainerMill;
 import adanaran.mods.bfr.items.ItemMillstone;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.ISidedInventory;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.tileentity.TileEntity;
 
 public class TileEntityMill extends TileEntity implements ISidedInventory {
 
 	private static final int[] slots_top = new int[] { 0 };
 	private static final int[] slots_bottom = new int[] { 2, 1 };
 	private static final int[] slots_sides = new int[] { 1 };
 
 	/**
 	 * The ItemStacks that hold the items currently being used in the mill
 	 */
 	private ItemStack[] millItemStacks = new ItemStack[3];
 	/**
 	 * The number of ticks the current item is milled;
 	 */
 	public int millTurningTime;
 	private String tEntityName;
 
 	/**
 	 * Reads a tile entity from NBT.
 	 */
 	public void readFromNBT(NBTTagCompound par1NBTTagCompound) {
 		super.readFromNBT(par1NBTTagCompound);
 		NBTTagList nbttaglist = par1NBTTagCompound.getTagList("Items");
 		this.millItemStacks = new ItemStack[this.getSizeInventory()];
 
 		for (int i = 0; i < nbttaglist.tagCount(); ++i) {
 			NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbttaglist
 					.tagAt(i);
 			byte b0 = nbttagcompound1.getByte("Slot");
 
 			if (b0 >= 0 && b0 < this.millItemStacks.length) {
 				this.millItemStacks[b0] = ItemStack
 						.loadItemStackFromNBT(nbttagcompound1);
 			}
 		}
 		this.millTurningTime = par1NBTTagCompound.getShort("MillTurnTime");
 
 		if (par1NBTTagCompound.hasKey("CustomName")) {
 			this.tEntityName = par1NBTTagCompound.getString("CustomName");
 		}
 	}
 
 	/**
 	 * Returns the name of the inventory.
 	 */
 	public String getInvName() {
 		return this.isInvNameLocalized() ? this.tEntityName : "container.mill";
 	}
 
 	/**
 	 * If this returns false, the inventory name will be used as an unlocalized
 	 * name, and translated into the player's language. Otherwise it will be
 	 * used directly.
 	 */
 	public boolean isInvNameLocalized() {
 		return this.tEntityName != null && this.tEntityName.length() > 0;
 	}
 
 	/**
 	 * Sets the custom display name to use when opening a GUI linked to this
 	 * tile entity.
 	 */
 	public void setGuiDisplayName(String par1Str) {
 		this.tEntityName = par1Str;
 	}
 
 	/**
 	 * Writes a tile entity to NBT.
 	 */
 	public void writeToNBT(NBTTagCompound par1NBTTagCompound) {
 		super.writeToNBT(par1NBTTagCompound);
 		par1NBTTagCompound.setShort("MillTurnTime",
 				(short) this.millTurningTime);
 		NBTTagList nbttaglist = new NBTTagList();
 
 		for (int i = 0; i < this.millItemStacks.length; ++i) {
 			if (this.millItemStacks[i] != null) {
 				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
 				nbttagcompound1.setByte("Slot", (byte) i);
 				this.millItemStacks[i].writeToNBT(nbttagcompound1);
 				nbttaglist.appendTag(nbttagcompound1);
 			}
 		}
 
 		par1NBTTagCompound.setTag("Items", nbttaglist);
 
 		if (this.isInvNameLocalized()) {
 			par1NBTTagCompound.setString("CustomName", this.tEntityName);
 		}
 	}
 
 	@Override
 	public int getSizeInventory() {
 		return this.millItemStacks.length;
 	}
 
 	@Override
 	public ItemStack getStackInSlot(int i) {
 		return this.millItemStacks[i];
 	}
 
 	@Override
 	public ItemStack decrStackSize(int i, int j) {
 		if (this.millItemStacks[i] != null) {
 			ItemStack itemstack;
 
 			if (this.millItemStacks[i].stackSize <= j) {
 				itemstack = this.millItemStacks[i];
 				this.millItemStacks[i] = null;
 				return itemstack;
 			} else {
 				itemstack = this.millItemStacks[i].splitStack(j);
 
 				if (this.millItemStacks[i].stackSize == 0) {
 					this.millItemStacks[i] = null;
 				}
 
 				return itemstack;
 			}
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public ItemStack getStackInSlotOnClosing(int i) {
 		if (this.millItemStacks[i] != null) {
 			ItemStack itemstack = this.millItemStacks[i];
 			this.millItemStacks[i] = null;
 			return itemstack;
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public void setInventorySlotContents(int i, ItemStack itemstack) {
 		this.millItemStacks[i] = itemstack;
 
 		if (itemstack != null
 				&& itemstack.stackSize > this.getInventoryStackLimit()) {
 			itemstack.stackSize = this.getInventoryStackLimit();
 		}
 	}
 
 	@Override
 	public int getInventoryStackLimit() {
 		return 64;
 	}
 
 	@Override
 	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
 		return this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord,
 				this.zCoord) != this ? false : entityplayer.getDistanceSq(
 				(double) this.xCoord + 0.5D, (double) this.yCoord + 0.5D,
 				(double) this.zCoord + 0.5D) <= 64.0D;
 	}
 
 	@Override
 	public void openChest() {
 		// Without function
 	}
 
 	@Override
 	public void closeChest() {
 		// Without function
 	}
 
 	@Override
 	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
 		return i == 2 ? false
 				: (i == 1 ? itemstack.getItem() instanceof ItemMillstone : true);
 	}
 
 	@Override
 	public int[] getAccessibleSlotsFromSide(int var1) {
 		return var1 == 0 ? slots_bottom : (var1 == 1 ? slots_top : slots_sides);
 	}
 
 	@Override
 	public boolean canInsertItem(int slot, ItemStack itemstack, int j) {
 		return this.isItemValidForSlot(slot, itemstack);
 	}
 
 	@Override
 	public boolean canExtractItem(int slot, ItemStack itemstack, int j) {
 		return j != 0 || slot != 1;
 	}
 
 	public int getMillProgressScaled(int i) {
 		return this.millTurningTime * i / 200;
 	}
 
 	public boolean isMilling() {
		return millItemStacks[0] != null 
				&& millItemStacks[0].getItem() instanceof ItemMillstone
 				&& millItemStacks[1] != null
 				&& (millItemStacks[2] == null || millItemStacks[2]
 						.isItemEqual(MillRecipes.getInstance()
 								.getSmeltingResult(millItemStacks[1])));
 	}
 
 	public void updateEntity() {
 		boolean flag = false;
 		if (isMilling()) {
 			// damage millstone
 			millItemStacks[0].damageItem(1, null);
 			if (this.millItemStacks[0].stackSize <= 0) {
 				this.millItemStacks[0] = null;
 			}
 			millTurningTime++;
 			if (this.millTurningTime == 100) {
 				// if milled long enough
 				this.millTurningTime = 0;
 				this.millItem();
 				flag = true;
 			}
 		} else {
 			this.millTurningTime = 0;
 		}
 		if (isMilling() != this.millTurningTime > 0) {
 			flag = true;
 			BlockStove.updateStoveBlockState(this.millTurningTime > 0,
 					this.worldObj, this.xCoord, this.yCoord, this.zCoord);
 		}
 		if (flag) {
 			this.onInventoryChanged();
 		}
 	}
 
 	private void millItem() {
 		// add millResult
 		ItemStack result = MillRecipes.getInstance().getSmeltingResult(
 				millItemStacks[1]);
 		if (millItemStacks[2] == null) {
 			millItemStacks[2] = result.copy();
 		} else if (this.millItemStacks[2].isItemEqual(result)) {
 			millItemStacks[2].stackSize += result.stackSize;
 		}
 
 	}
 }
