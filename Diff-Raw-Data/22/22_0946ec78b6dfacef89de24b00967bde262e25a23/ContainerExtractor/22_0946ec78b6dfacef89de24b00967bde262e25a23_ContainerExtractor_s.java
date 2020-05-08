 package mods.nordwest.common.container;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import mods.nordwest.common.ExtractorRecipes;
 import mods.nordwest.tileentity.TileEntityExtractor;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.inventory.Container;
 import net.minecraft.inventory.ICrafting;
 import net.minecraft.inventory.Slot;
 import net.minecraft.inventory.SlotFurnace;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.tileentity.TileEntityFurnace;
 
 public class ContainerExtractor extends Container {
 	private TileEntityExtractor furnace;
 	private int lastCookTime = 0;
 	private int lastBurnTime = 0;
 	private int lastItemBurnTime = 0;
 
 	public ContainerExtractor(InventoryPlayer par1InventoryPlayer, TileEntityExtractor tileEntity) {
 		this.furnace = tileEntity;
 		this.addSlotToContainer(new Slot(tileEntity, 0, 56, 23));
 		this.addSlotToContainer(new Slot(tileEntity, 1, 56, 59));
 		this.addSlotToContainer(new SlotFurnace(par1InventoryPlayer.player, tileEntity, 2, 113, 41));
 		int i;
 		for (i = 0; i < 3; ++i) {
 			for (int j = 0; j < 9; ++j) {
 				this.addSlotToContainer(new Slot(par1InventoryPlayer, j + i * 9 + 9, 8 + j * 18, 87 + i * 18));
 			}
 		}
 		for (i = 0; i < 9; ++i) {
 			this.addSlotToContainer(new Slot(par1InventoryPlayer, i, 8 + i * 18, 145));
 		}
 		this.addSlotToContainer(new Slot(tileEntity, 3, 34, 23));
 		this.addSlotToContainer(new SlotFurnace(par1InventoryPlayer.player, tileEntity, 4, 138, 39));
 	}
 
 	@Override
 	public void addCraftingToCrafters(ICrafting par1ICrafting) {
 		super.addCraftingToCrafters(par1ICrafting);
 		par1ICrafting.sendProgressBarUpdate(this, 0, this.furnace.extractorCookTime);
 		par1ICrafting.sendProgressBarUpdate(this, 1, this.furnace.extractorBurnTime);
 		par1ICrafting.sendProgressBarUpdate(this, 2, this.furnace.currentItemBurnTime);
 	}
 
 	/**
 	 * Looks for changes made in the container, sends them to every listener.
 	 */
 	@Override
 	public void detectAndSendChanges() {
 		super.detectAndSendChanges();
 
 		for (int i = 0; i < this.crafters.size(); ++i) {
 			ICrafting icrafting = (ICrafting) this.crafters.get(i);
 			if (this.lastCookTime != this.furnace.extractorCookTime) {
 				icrafting.sendProgressBarUpdate(this, 0, this.furnace.extractorCookTime);
 			}
 			if (this.lastBurnTime != this.furnace.extractorBurnTime) {
 				icrafting.sendProgressBarUpdate(this, 1, this.furnace.extractorBurnTime);
 			}
 			if (this.lastItemBurnTime != this.furnace.currentItemBurnTime) {
 				icrafting.sendProgressBarUpdate(this, 2, this.furnace.currentItemBurnTime);
 			}
 		}
 		this.lastCookTime = this.furnace.extractorCookTime;
 		this.lastBurnTime = this.furnace.extractorBurnTime;
 		this.lastItemBurnTime = this.furnace.currentItemBurnTime;
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void updateProgressBar(int par1, int par2) {
 		if (par1 == 0) {
 			this.furnace.extractorCookTime = par2;
 		}
 		if (par1 == 1) {
 			this.furnace.extractorBurnTime = par2;
 		}
 		if (par1 == 2) {
 			this.furnace.currentItemBurnTime = par2;
 		}
 	}
 
 	@Override
 	public boolean canInteractWith(EntityPlayer par1EntityPlayer) {
 		return this.furnace.isUseableByPlayer(par1EntityPlayer);
 		//return true;
 	}
 
 	/**
 	 * Called when a player shift-clicks on a slot. You must override this or you will crash when someone does that.
 	 */
 	@Override
 	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int ind) {
		System.out.println(ind);
 		ItemStack itemstack = null;
 		Slot slot = (Slot) this.inventorySlots.get(ind);
 
 		if (slot != null && slot.getHasStack()) {
 			ItemStack itemstack1 = slot.getStack();
 			itemstack = itemstack1.copy();
 			if (ind == 2 || ind == 40 || ind == 39) {
 				if (!this.mergeItemStack(itemstack1, 3, 39, true)) {
 					return null;
 				}
 
 				slot.onSlotChange(itemstack1, itemstack);
 			} else if (ind != 1 && ind != 0) {
 				if (ExtractorRecipes.extracting().getExtractingResult(itemstack1) != null) {
 					if (!this.mergeItemStack(itemstack1, 0, 1, false)) {
 						return null;
 					}
 				} else if (TileEntityFurnace.isItemFuel(itemstack1)) {
 					if (!this.mergeItemStack(itemstack1, 1, 2, false)) {
 						return null;
 					}
 				} else if (itemstack1.getItem().itemID == Item.glassBottle.itemID) {
 					if (!this.mergeItemStack(itemstack1, 39, 40, false)) {
 						return null;
 					}
 				} else if (ind >= 3 && ind < 30) {
 					if (!this.mergeItemStack(itemstack1, 30, 39, false)) {
 						return null;
 					}
 				} else if (ind >= 30 && ind < 39 && !this.mergeItemStack(itemstack1, 3, 30, false)) {
 					return null;
 				}
 			} else if (!this.mergeItemStack(itemstack1, 3, 39, false)) {
 				return null;
 			}
 
 			if (itemstack1.stackSize == 0) {
 				slot.putStack((ItemStack) null);
 			} else {
 				slot.onSlotChanged();
 			}
 
 			if (itemstack1.stackSize == itemstack.stackSize) {
 				return null;
 			}
 
 			slot.onPickupFromSlot(par1EntityPlayer, itemstack1);
 		}
 
 		return itemstack;
 	}
 }
