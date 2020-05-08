 package slimevoid.collaborative.container;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.inventory.InventoryCraftResult;
 import net.minecraft.inventory.InventoryCrafting;
 import net.minecraft.inventory.Slot;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.CraftingManager;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import slimevoid.collaborative.container.slot.SlotCraftRefill;
 import slimevoid.collaborative.container.slot.SlotPlan;
 import slimevoid.collaborative.core.lib.CommandLib;
 import slimevoid.collaborative.core.lib.ConfigurationLib;
 import slimevoid.collaborative.core.lib.ItemLib;
 import slimevoid.collaborative.inventory.InventoryMatch;
 import slimevoid.collaborative.inventory.InventorySubCraft;
 import slimevoid.collaborative.network.packet.PacketGui;
 import slimevoid.collaborative.tileentity.TileEntityWorkBench;
 import slimevoidlib.inventory.ContainerBase;
 import slimevoidlib.inventory.InventorySubUpdate;
 
 public class ContainerWorkBench extends ContainerBase {
 
 	public List<IInventory>		externalInventories;
 	public List<IInventory>		externalSlotInventories;
 	SlotCraftRefill				slotCraft;
 	public IInventory			craftResult;
 	public InventoryCrafting	fakeInv;
 	public InventoryCrafting	craftMatrix;
 
 	public int					satisfyMask;
 	public boolean				playerInventoryUsed;
 
 	public ContainerWorkBench(InventoryPlayer playerInventory, TileEntityWorkBench tileentity) {
 		super(playerInventory, tileentity, tileentity.worldObj, 0, 140);
 
 		// add n,e,w,s inventories
 		this.fakeInv = new InventoryCrafting(new ContainerNull(), 3, 3);
 		this.onCraftMatrixChanged(this.craftMatrix);
 	}
 
 	protected void bindLocalInventory() {
 		this.craftMatrix = new InventorySubCraft(this, (TileEntityWorkBench) this.customInventory);
 		this.craftResult = new InventoryCraftResult();
 
 		// Crafting Matrix
 		for (int row = 0; row < 3; ++row) {
 			for (int column = 0; column < 3; ++column) {
 				this.addSlotToContainer(new Slot(this.craftMatrix, column + row
 																	* 3, 48 + column * 18, 18 + row * 18));
 			}
 		}
 
 		// Plan slot
 		this.addSlotToContainer(new SlotPlan(new InventorySubUpdate(this, this.customInventory, 9, 1), 0, 17, 36));
 
 		// Craft slot - Source inventories
 		this.bindCraftingInventory();
 
 		// Bench inventory
 		for (int row = 0; row < 2; ++row) {
 			for (int column = 0; column < 9; ++column) {
 				int slotIndex = column + (row * 9);
 				this.addSlotToContainer(new Slot(new InventorySubUpdate(this, this.customInventory, 10, 18), slotIndex, 8 + column * 18, row * 18 + 90));
 			}
 		}
 	}
 
 	public List<IInventory> getSourceInventories(EntityPlayer entityplayer) {
 		List<IInventory> sourceInventories = new ArrayList<IInventory>();
 		if (this.customInventory != null) {
 			sourceInventories.add(this.customInventory);
 		}
 		if (this.playerInventory != null /*
 										 * !ConfigurationLib.isPlayerInventoryLocked
 										 * (entityplayer)
 										 */) {
 			sourceInventories.add(this.playerInventory);
 		}
 		// TODO :: Add additional Inventory Logic
 		if (this.externalInventories != null
 			&& this.externalInventories.size() > 0) {
 			for (IInventory externalInventory : this.externalInventories) {
 				sourceInventories.add(externalInventory);
 			}
 		}
 		return sourceInventories;
 	}
 
 	private void bindCraftingInventory() {
 
 		/*
 		 * Place Holders for additional inventories
 		 */
 		this.externalInventories = new ArrayList<IInventory>();
 		this.externalSlotInventories = new ArrayList<IInventory>();
 
 		// Gather source inventories for Craft Output refill logic
 		List<IInventory> sourceInventoryList = this.getSourceInventories(this.playerInventory.player);
 		IInventory[] sourceInventories = new IInventory[sourceInventoryList.size()];
 		int i = 0;
 		for (IInventory source : sourceInventoryList) {
 			try {
 				sourceInventories[i] = source;
 				i++;
 			} catch (Exception e) {
 				e.printStackTrace();
 				break;
 			}
 		}
 
 		if (sourceInventories != null && sourceInventories.length > 0) {
 			// crafting result
 			slotCraft = new SlotCraftRefill(this.playerInventory.player, this.craftMatrix, this.craftResult, sourceInventories, this, 0, 143, 35);
 			this.addSlotToContainer(this.slotCraft);
 		}
 	}
 
 	public int getSatisfyMask() {
 		this.playerInventoryUsed = false;
 		ItemStack plan = this.customInventory.getStackInSlot(9);
 		ItemStack items[] = null;
 		if (plan != null) {
 			items = getShadowItems(plan);
 		}
 		int bits = 0;
 		for (int i = 0; i < 9; i++) {
 			ItemStack st = this.customInventory.getStackInSlot(i);
 			if (st != null) {
 				bits |= 1 << i;
 				continue;
 			}
 			if (items == null || items[i] == null) bits |= 1 << i;
 		}
 
 		if (bits == 511) {
 			return 511;
 		}
 		for (int i = 0; i < 18; i++) {
 			ItemStack test = this.customInventory.getStackInSlot(10 + i);
 			if (test == null || test.stackSize == 0) {
 				continue;
 			}
 			int sc = test.stackSize;
 			for (int j = 0; j < 9; j++) {
 				if ((bits & 1 << j) > 0) {
 					continue;
 				}
 				ItemStack st = this.customInventory.getStackInSlot(j);
 				if (st != null) {
 					continue;
 				}
 				st = items[j];
 				if (st == null || !ItemLib.matchOre(st,
 													test)) {
 					continue;
 				}
 				bits |= 1 << j;
 				if (--sc == 0) {
 					break;
 				}
 			}
 
 		}
 		if (bits == 511) {
 			return 511;
 		}
 		if (ConfigurationLib.isPlayerInventoryLocked(playerInventory.player)) {
 			this.playerInventoryUsed = true;
 		}
 		for (int i = 0; i < this.playerInventory.getSizeInventory(); i++) {
 			ItemStack test = this.playerInventory.getStackInSlot(i);
 			if (test == null || test.stackSize == 0) {
 				continue;
 			}
 			int sc = test.stackSize;
 			for (int j = 0; j < 9; j++) {
 				if ((bits & 1 << j) > 0) {
 					continue;
 				}
 				ItemStack st = this.customInventory.getStackInSlot(j);
 				if (st != null) {
 					continue;
 				}
 				st = items[j];
 				if (st == null || !ItemLib.matchOre(st,
 													test)) {
 					continue;
 				}
 				bits |= 1 << j;
 
 				if (--sc == 0) {
 					break;
 				}
 			}
 
 		}
 		return bits;
 	}
 
 	private InventoryMatch findMatch(ItemStack a) {
 		for (int i = 0; i < 18; i++) {
 			ItemStack test = this.customInventory.getStackInSlot(10 + i);
 			if (test != null && test.stackSize != 0 && ItemLib.matchOre(a,
 																		test)) {
 				return new InventoryMatch(this.customInventory, 10 + i);
 			}
 		}
 		for (int i = 0; i < this.playerInventory.getSizeInventory(); i++) {
 			ItemStack test = this.playerInventory.getStackInSlot(i);
 			if (test != null && test.stackSize != 0 && ItemLib.matchOre(a,
 																		test)) {
 				return new InventoryMatch(this.playerInventory, i);
 			}
 		}
 		// TODO: add External Inventories
 		return null;
 	}
 
 	public ItemStack[] getPlanItems() {
 		ItemStack plan = this.customInventory.getStackInSlot(9);
 		if (plan == null) {
 			return null;
 		} else {
 			return getShadowItems(plan);
 		}
 	}
 
 	public static ItemStack[] getShadowItems(ItemStack ist) {
 		if (ist.stackTagCompound == null) return null;
 		NBTTagList require = ist.stackTagCompound.getTagList("requires");
 		if (require == null) {
 			return null;
 		}
 		ItemStack tr[] = new ItemStack[9];
 		for (int i = 0; i < require.tagCount(); i++) {
 			NBTTagCompound item = (NBTTagCompound) require.tagAt(i);
 			ItemStack is2 = ItemStack.loadItemStackFromNBT(item);
 			int sl = item.getByte("Slot");
 			if (sl >= 0 && sl < 9) tr[sl] = is2;
 		}
 
 		return tr;
 	}
 
 	@Override
 	public void onCraftMatrixChanged(IInventory inventory) {
 		ItemStack plan = this.customInventory.getStackInSlot(9);
 		ItemStack items[] = null;
 		if (plan != null) {
 			items = getShadowItems(plan);
 		}
 		for (int i = 0; i < 9; i++) {
 			ItemStack tos = this.customInventory.getStackInSlot(i);
 			if (tos == null && items != null && items[i] != null) {
 				InventoryMatch match = this.findMatch(items[i]);
 				if (match != null) {
 					tos = match.inventoryMatch.getStackInSlot(match.slotIndex);
 				}
 			}
 			this.fakeInv.setInventorySlotContents(	i,
 													tos);
 		}
 		this.satisfyMask = getSatisfyMask();
 		if (this.satisfyMask == 511) {
 			this.craftResult.setInventorySlotContents(	0,
 														CraftingManager.getInstance().findMatchingRecipe(	fakeInv,
 																											this.world));
 		} else {
 			this.craftResult.setInventorySlotContents(	0,
 														null);
 		}
 	}
 
 	@Override
 	public ItemStack slotClick(int par1, int par2, int par3, EntityPlayer par4EntityPlayer) {
 		ItemStack stack = super.slotClick(	par1,
 											par2,
 											par3,
 											par4EntityPlayer);
 		/*
 		 * Slot plan = this.getSlot(ContainerLib.PLAN_SLOT); if (plan != null &&
 		 * plan.getStack() != null && plan.getStack().getItem() instanceof
 		 * ItemPlan) { }
 		 */
 		this.onCraftMatrixChanged(this.craftMatrix);
 		return stack;
 	}
 
 	@Override
 	public boolean canInteractWith(EntityPlayer entityplayer) {
 		return this.customInventory.isUseableByPlayer(entityplayer);
 	}
 
 	@
 
 	Override
 	public void putStackInSlot(int par1, ItemStack par2ItemStack) {
 		super.putStackInSlot(	par1,
 								par2ItemStack);
 		// this.onCraftMatrixChanged(this.craftMatrix);
 	}
 
 	@Override
 	public void putStacksInSlots(ItemStack[] par1ArrayOfItemStack) {
 		super.putStacksInSlots(par1ArrayOfItemStack);
 		// this.onCraftMatrixChanged(this.craftMatrix);
 	}
 
 	/**
 	 * Called when a player shift-clicks on a slot. You must override this or
 	 * you will crash when someone does that.
 	 */
 	@Override
 	public ItemStack transferStackInSlot(EntityPlayer entityplayer, int slotShiftClicked) {
 		ItemStack itemstackCopy = null;
 		Slot slot = (Slot) this.inventorySlots.get(slotShiftClicked);
 
 		if (slot != null && slot.getHasStack()) {
 			ItemStack stackInSlot = slot.getStack();
 			itemstackCopy = stackInSlot.copy();
 			if (slotShiftClicked != 9
 				&& (stackInSlot.itemID == ConfigurationLib.itemPlanBlank.itemID || stackInSlot.itemID == ConfigurationLib.itemPlanFull.itemID)) {
				if (moveSingleItem(	stackInSlot,
									9)) {// try to place into plan
											// slot
 					if ((slotShiftClicked >= 11 && slotShiftClicked < 29)
 						|| !this.mergeItemStack(stackInSlot,
 												11,
 												29,
 												false)) {// else place in
 															// internal
 															// inventory
 						if ((slotShiftClicked >= 29)
 							|| !this.mergeItemStack(stackInSlot,
 													29,
 													65,
 													false)) {// else place in
 																// player
 																// inventory
 							return null;
 						}
 					}
 				}
 			} else if (slotShiftClicked < 9 || slotShiftClicked == 10) {
 				if (!this.mergeItemStack(	stackInSlot,
 											11,
 											65,
 											true)) {
 					return null;
 				}
 			} else if (slotShiftClicked < 29) { // if internal inventory shift
 												// click into player inventory
 				if (!this.mergeItemStack(	stackInSlot,
 											29,
 											65,
 											true)) {
 					return null;
 				}
 			} else if (!this.mergeItemStack(stackInSlot,
 											11,
 											29,
 											false)) { // if player then go into
 														// internal inventory
 														// first
 				if (!this.mergeItemStack(	stackInSlot,
 											0,
 											9,
 											false)) {// then crafting grid
 					return null;
 				}
 			}
 			if (stackInSlot.stackSize == 0) {
 				slot.putStack(null);
 			} else {
 				slot.onSlotChanged();
 			}
 			if (stackInSlot.stackSize != itemstackCopy.stackSize) {
 				slot.onPickupFromSlot(	entityplayer,
 										stackInSlot);
 			} else {
 				return null;
 			}
 		}
 		return itemstackCopy;
 	}
 
	private boolean moveSingleItem(ItemStack stackInSlot, int i) {
		Slot slot = (Slot) this.inventorySlots.get(i);
		ItemStack itemstack1 = slot.getStack();
		if (itemstack1 == null) {
			slot.putStack(stackInSlot.copy());
			slot.getStack().stackSize = 1;
			slot.onSlotChanged();
			--stackInSlot.stackSize;
			return true;
		}
		return false;
	}

 	@Override
 	protected void retrySlotClick(int par1, int par2, boolean par3, EntityPlayer par4EntityPlayer) {
 		this.satisfyMask = this.getSatisfyMask();
 		if (this.playerInventoryUsed) {
 			this.playerInventoryUsed = false;
 		} else {
 			super.retrySlotClick(	par1,
 									par2,
 									par3,
 									par4EntityPlayer);
 		}
 	}
 
 	public void handleGuiEvent(PacketGui packet) {
 		if (this.world == null || this.world.isRemote) {
 			return;
 		}
 		if (!packet.getCommand().equals(CommandLib.CREATE_PLAN)) {
 			return;
 		}
 		ItemStack blank = this.customInventory.getStackInSlot(9);
 		if (blank == null
 			|| blank.itemID != ConfigurationLib.itemPlanBlank.itemID) {
 			return;
 		}
 		ItemStack plan = new ItemStack(ConfigurationLib.itemPlanFull);
 		plan.stackTagCompound = new NBTTagCompound();
 		NBTTagCompound result = new NBTTagCompound();
 		craftResult.getStackInSlot(0).writeToNBT(result);
 		plan.stackTagCompound.setCompoundTag(	"result",
 												result);
 		NBTTagList requires = new NBTTagList();
 		for (int i = 0; i < 9; i++) {
 			ItemStack is1 = craftMatrix.getStackInSlot(i);
 			if (is1 != null) {
 				ItemStack ist = new ItemStack(is1.itemID, 1, is1.getItemDamage());
 				NBTTagCompound item = new NBTTagCompound();
 				ist.writeToNBT(item);
 				item.setByte(	"Slot",
 								(byte) i);
 				requires.appendTag(item);
 			}
 		}
 
 		plan.stackTagCompound.setTag(	"requires",
 										requires);
 		this.customInventory.setInventorySlotContents(	9,
 														plan);
 		this.onCraftMatrixChanged(this.craftMatrix);
 	}
 }
