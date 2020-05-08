 package machinitech.common.tile;
 
 import machinitech.api.MachiniTechRecipes;
 import machinitech.common.block.MachineSmelterSmall;
import machinitech.common.block.OreMachiniTech;
 import machinitech.common.item.MachiniTechCoil;
 import machinitech.common.item.MachiniTechItem;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.Container;
 import net.minecraft.inventory.ISidedInventory;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemBlock;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.ItemTool;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.tileentity.TileEntityFurnace;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.liquids.ILiquidTank;
 import net.minecraftforge.liquids.ITankContainer;
 import net.minecraftforge.liquids.LiquidContainerRegistry;
 import net.minecraftforge.liquids.LiquidStack;
 import net.minecraftforge.liquids.LiquidTank;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 public class TileEntitySmelter extends MachiniTechMachine implements ISidedInventory, ITankContainer {
 
 	//The different Item slots
 	/*
 	 * Old version
 	 * 0-7 Fuel
 	 * 8 Coil
 	 * 9-16 Main Input
 	 * 17-22 Liquid I/O Slots
 	 * 23 Box
 	 * 24 Main Output
 	 */
 	/*
 	 * New version
 	 * 0-7 Main Input
 	 * 8 Box
 	 * 9-16 Main Output
 	 * 17 Fuel
 	 * 18 Coil
 	 * 19-24 Liquid I/O Slots
 	 * 25 Determining phantom slot (Cannot put smelt items if items in input != slot's item)
 	 */
 	private ItemStack[] inv = new ItemStack[26];
 	public int furnaceBurnTime = 0;
 	private static final short ASH_TIME = 800;
 	private ItemStack smeltable = null;
 	private short ashProgress = 0;
 	private short coilTier = 0;
 	private LiquidTank tank;
 	private LiquidTank another;
 	private short heat = 0;
 	private Container cont;
 	/**
 	 * The number of ticks that the furnace will keep burning
 	 */
 	public int currentItemBurnTime = 0;
 	/**
 	 * Name, used in renaming
 	 */
 	private String name;
 	/**
 	 * The amount of buckets that each liquid tank can hold
 	 */
 	private static final int TANK_CAP = 8;
 	/**
 	 * The amount of time an item cooks for
 	 */
 	private short itemCookTime = 200;
 	public TileEntitySmelter() {
 		super(0);
 		tank = new LiquidTank(LiquidContainerRegistry.BUCKET_VOLUME * TANK_CAP);
 		another = new LiquidTank(LiquidContainerRegistry.BUCKET_VOLUME * TANK_CAP);
 	}
 
 	@Override
 	public int getSizeInventory() {
 		return inv.length;
 	}
 
 	@Override
 	public ItemStack getStackInSlot(int i) {
 		return inv[i];
 	}
 	
 	public void readFromNBT(NBTTagCompound par1NBTTagCompound) {
         super.readFromNBT(par1NBTTagCompound);
         NBTTagList nbttaglist = par1NBTTagCompound.getTagList("Items");
         this.inv = new ItemStack[this.getSizeInventory()];
 
         for (int i = 0; i < nbttaglist.tagCount(); ++i)
         {
             NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.tagAt(i);
             byte b0 = nbttagcompound1.getByte("Slot");
 
             if (b0 >= 0 && b0 < this.inv.length)
             {
                 this.inv[b0] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
             }
         }
 
         this.furnaceBurnTime = par1NBTTagCompound.getShort("BurnTime");
         this.progress = par1NBTTagCompound.getShort("CookTime");
         this.itemCookTime = par1NBTTagCompound.getShort("ItemCookTime");
         this.coilTier = par1NBTTagCompound.getShort("CoilTier");
         this.currentItemBurnTime = this.getItemBurnTime(this.inv[17]);
         this.heat = par1NBTTagCompound.getShort("Heat");
 
         if (par1NBTTagCompound.hasKey("CustomName"))
         {
             this.name = par1NBTTagCompound.getString("CustomName");
         }
     }
 
     /**
      * Writes a tile entity to NBT.
      */
     public void writeToNBT(NBTTagCompound par1NBTTagCompound) {
         super.writeToNBT(par1NBTTagCompound);
         par1NBTTagCompound.setShort("BurnTime", (short)this.furnaceBurnTime);
         par1NBTTagCompound.setShort("CookTime", (short)this.progress);
         par1NBTTagCompound.setShort("ItemCookTime", this.itemCookTime);
         par1NBTTagCompound.setShort("CoilTier", this.coilTier);
         par1NBTTagCompound.setShort("Heat", this.heat);
         NBTTagList nbttaglist = new NBTTagList();
 
         for (int i = 0; i < this.inv.length; ++i) {
             if (this.inv[i] != null) {
                 NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                 nbttagcompound1.setByte("Slot", (byte)i);
                 this.inv[i].writeToNBT(nbttagcompound1);
                 nbttaglist.appendTag(nbttagcompound1);
             }
         }
 
         par1NBTTagCompound.setTag("Items", nbttaglist);
 
         if (this.isInvNameLocalized()) {
             par1NBTTagCompound.setString("CustomName", this.name);
         }
     }
     
     public int getCookProgressScaled(int par1) {
         return this.progress * par1 / this.itemCookTime;
     }
 
 	@Override
 	public ItemStack decrStackSize(int i, int j) {
 		ItemStack stack = getStackInSlot(i);
 		if (i == 18) {
 			this.coilTier = 0;
 		}
 		if (i == 25) {
 			this.smeltable = null;
 		}
         if (stack != null) {
                 if (stack.stackSize <= j) {
                         setInventorySlotContents(i, null);
                 } else {
                         stack = stack.splitStack(j);
                         if (stack.stackSize == 0) {
                                 setInventorySlotContents(i, null);
                         }
                 }
         }
         return stack;
 	}
 
 	@Override
 	public ItemStack getStackInSlotOnClosing(int i) {
 		ItemStack stack = getStackInSlot(i);
         if (stack != null) {
                 setInventorySlotContents(i, null);
         }
         return stack;
 	}
 
 	@Override
 	public void setInventorySlotContents(int i, ItemStack itemstack) {
 		inv[i] = itemstack;
         if (itemstack != null && itemstack.stackSize > getInventoryStackLimit()) {
                 itemstack.stackSize = getInventoryStackLimit();
         }
 		if (i == 18) {
 			if (this.getStackInSlot(18) != null && this.getStackInSlot(18).getItem() instanceof MachiniTechCoil) {
 				this.coilTier = (short)((MachiniTechCoil)itemstack.getItem()).getTier(itemstack.getItemDamage());
 			}
 		}
 		if (i == 25) {
 			this.smeltable = itemstack;
 		}
 	}
 
 	@Override
 	public String getInvName() {
 		return "Smelter";
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
 		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this && entityplayer.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64;
 	}
 
 	@Override
 	public void openChest() {
 		
 	}
 
 	@Override
 	public void closeChest() {
 		
 	}
 	
 	public static int getItemBurnTime(ItemStack par0ItemStack) {
 		return TileEntityFurnace.getItemBurnTime(par0ItemStack);
     }
 	
 	@Override
 	public void updateEntity() {
 		boolean active = this.isActive();
         boolean update = false;
         if (this.furnaceBurnTime > 0) {
             --this.furnaceBurnTime;
             if (this.heat < (this.coilTier * 2000) + 2000) {
             	this.heat += (this.coilTier + 1);
             	this.itemCookTime = (short) ((this.heat / -50) + 200);//Well. Anyone wanna wait for a long time for this smelter to heat up?
             } else if (this.heat >= (this.coilTier * 2000) + 2000) {
             	this.heat = (short) ((this.coilTier * 2000) + 2000);
             }
         } else {
         	if (this.heat > 0) {
         		this.heat--;
         	}
         }
         if (!this.worldObj.isRemote) {
             if (this.furnaceBurnTime == 0) {//Always. Burn. Fuel.
                 this.currentItemBurnTime = getItemBurnTime(this.inv[17]);
                 this.furnaceBurnTime = getItemBurnTime(this.inv[17]);
                 if (this.furnaceBurnTime > 0) {
                     update = true;
                     //Reduce the fuel slot
                     if (this.inv[17] != null) {
                         --this.inv[17].stackSize;
                         if (this.inv[17].stackSize == 0) {
                             this.inv[17] = this.inv[17].getItem().getContainerItemStack(inv[17]);
                         }
                     }
                 }
             }
             if (this.isActive() && this.canSmelt()) {
                 ++this.progress;
                 //Done smelting?
                 if (this.progress >= this.itemCookTime) {
                     this.progress = 0;
                     this.smeltItems();
                     update = true;
                 }
                 ++this.ashProgress;
                 if (this.ashProgress == ASH_TIME) {
                 	if (this.inv[8] != null) {
                 		if (this.inv[8].stackSize != this.inv[8].getMaxStackSize()) {
                 			this.inv[8].stackSize++;
                 		}
                 	} else {
                 		this.inv[8] = new ItemStack(MachiniTechItem.ash);
                 	}
                 	this.ashProgress = 0;
                 }
             }
             else {
                 this.progress = 0;
             }
             if (active != this.furnaceBurnTime > 0) {
                 update = true;
                 MachineSmelterSmall.update(!active, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
             }
         }
         if (update) {
             this.onInventoryChanged();
         }
 	}
 	/**
 	 * Returns true four or more slots of the smelter can smelt an item without interference
 	 * Does not account for fuel
 	 * @return A boolean determining whether the furnace can smelt
 	 */
 	private boolean canSmelt() {
 		int goodSlots = 0;
		int heatreq = 10000;
		if (this.getStackInSlot(25) != null) {
			heatreq = OreMachiniTech.getBasedIDMeta(this.getStackInSlot(25).itemID, this.getStackInSlot(25).getItemDamage()).getProps().getSmeltingHeat();
		}
 		for (int s = 0; s < 8; s++) {
 			ItemStack in = this.getStackInSlot(s);
 			ItemStack out = this.getStackInSlot(s + 9);
 			ItemStack res = MachiniTechRecipes.getSmeltingResult(in);
 			if (in != null) {//If one input is null, skip instead
 				if (smeltable == null) {//To catch null pointers and because you can't anyway
 					return false;
 				}
 				if (!in.isItemEqual(smeltable)) {//Only smelt smeltable
 					return false;
 				}
 				if (out == null) {
 					goodSlots++;
 					continue;
 				} else if (res != null && res.stackSize + out.stackSize <= out.getMaxStackSize() && out.isItemEqual(res)) {
 					goodSlots++;
 				}
 			}
 		}
 		return goodSlots >= 4 && this.heat >= heatreq;//4 or more can smelt
 	}
 	
 	private void smeltItems() {
 		if (this.canSmelt()) {
 			for (int s = 0; s < 8; s++) {
 				ItemStack in = getStackInSlot(s);
 				ItemStack out = getStackInSlot(s + 9);
 				ItemStack res = MachiniTechRecipes.getSmeltingResult(in);
 				if (in != null) {//Prevent NullPointers
 					if (this.inv[s + 9] == null) {//If nothing, copy item and quantity of the result
 						this.inv[s + 9] = res.copy();
 					} else if (this.inv[s + 9].isItemEqual(res)) {//If the ItemStacks are the same, add the quantity
 						inv[s + 9].stackSize += res.stackSize;
 				    }
 					//Reduce the input
 					--this.inv[s].stackSize;
 				    if (this.inv[s].stackSize <= 0) {
 				    	this.inv[s] = null;
 				    }
 				}
 			}
 		}
 	}
 	public int getBurnTimeRemainingScaled(int par1) {
         if (this.currentItemBurnTime == 0) {
             this.currentItemBurnTime = this.itemCookTime;
         }
         return this.furnaceBurnTime * par1 / this.currentItemBurnTime;
     }
 	@Override
 	public boolean isStackValidForSlot(int i, ItemStack itemstack) {
 		if (i >= 0 && i <= 7) {
 			if (MachiniTechRecipes.getSmeltingResult(itemstack) != null) {
 				return true;
 			}
 		}
 		if (i == 17 && getItemBurnTime(itemstack) != 0) {
 			return true;
 		}
 		if (i == 18 && this.getStackInSlot(18) == null && itemstack.getItem() instanceof MachiniTechCoil) {
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public int[] getAccessibleSlotsFromSide(int var1) {
 		int[] res = null;
 		switch(var1) {//pl means placeholder. Because Java is kinda stupid and only allows {x, y, z} in array initialisers.
 		case 0:
 			int[] pl1 = {9, 10, 11, 12, 13, 14, 15, 16};
 			res = pl1;
 			break;
 		case 1:
 			int[] pl2 = {0, 1, 2, 3, 4, 5, 6, 7};
 			res = pl2;
 			break;
 		default:
 			res = new int[2];
 			res[0] = 17;
 			res[1] = 18;
 			break;//Even though it's not really necessary
 		}
 		return res;
 	}
 
 	@Override
 	public boolean canInsertItem(int slot, ItemStack itemstack, int side) {
 		if (side == 1) {
 			if (slot >= 0 && slot <= 7) {
 				return MachiniTechRecipes.getSmeltingResult(itemstack) != null;
 			}
 		} else if (side == 0) {
 			return false;
 		} else {
 			if (slot == 17) {
 				return getItemBurnTime(itemstack) > 0;
 			} else if (slot == 18) {
 				return (this.getStackInSlot(18) == null && itemstack.getItem() instanceof MachiniTechCoil);
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean canExtractItem(int slot, ItemStack itemstack, int side) {
 		if (side == 0) {
 			return slot >= 9 && slot <= 16;
 		} else {
 			return false;
 		}
 	}
 
 	public boolean isActive() {
 		return this.furnaceBurnTime > 0;
 	}
 	
 	@Override
 	public ILiquidTank[] getTanks(ForgeDirection direction)
 	{
 		return new ILiquidTank[] { tank, another };
 	}
 
 	@Override
 	public int fill(ForgeDirection from, LiquidStack resource, boolean doFill) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public int fill(int tankIndex, LiquidStack resource, boolean doFill) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public LiquidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public LiquidStack drain(int tankIndex, int maxDrain, boolean doDrain) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public ILiquidTank getTank(ForgeDirection direction, LiquidStack type) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void setName(String displayName) {
 		this.name = displayName;
 	}
 
 	public short getItemCookTime() {
 		return this.itemCookTime;
 	}
 
 	public void setItemCookTime(int par2) {
 		this.itemCookTime = (short) par2;
 	}
 
 	public short getHeat() {
 		return this.heat;
 	}
 
 	public void setHeat(short par2) {
 		this.heat = par2;
 	}
 
 }
