 package bstramke.NetherStuffs.DemonicFurnace;
 
 import java.util.Iterator;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.inventory.Container;
 import net.minecraft.inventory.ICrafting;
 import net.minecraft.inventory.Slot;
 import net.minecraft.item.ItemStack;
 import bstramke.NetherStuffs.NetherStuffs;
 import bstramke.NetherStuffs.Items.NetherItems;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ContainerDemonicFurnace extends Container {
 	protected TileDemonicFurnace furnace;
 	private int lastCookTime = 0;
 	private int lastBurnTime = 0;
 	private int lastItemBurnTime = 0;
 
 	public ContainerDemonicFurnace(TileDemonicFurnace tile_entity, InventoryPlayer player_inventory) {
 		this.furnace = tile_entity;
 
 		this.addSlotToContainer(new Slot(furnace, 0, 56, 17));
 		this.addSlotToContainer(new Slot(furnace, 1, 56, 53));
 		this.addSlotToContainer(new SlotDemonicFurnace(player_inventory.player, furnace, 2, 116, 35));
 
 		bindPlayerInventory(player_inventory);
 	}
 
 	@Override
 	public boolean canInteractWith(EntityPlayer player) {
 		return furnace.isUseableByPlayer(player);
 	}
 
 	protected void bindPlayerInventory(InventoryPlayer player_inventory) {
 		int var3;
 
 		for (var3 = 0; var3 < 3; ++var3) {
 			for (int var4 = 0; var4 < 9; ++var4) {
 				this.addSlotToContainer(new Slot(player_inventory, var4 + var3 * 9 + 9, 8 + var4 * 18, 84 + var3 * 18));
 			}
 		}
 
 		for (var3 = 0; var3 < 9; ++var3) {
 			this.addSlotToContainer(new Slot(player_inventory, var3, 8 + var3 * 18, 142));
 		}
 
 	}
 
 	/**
 	 * Updates crafting matrix; called from onCraftMatrixChanged. Args: none
 	 */
 	@Override
 	public void updateCraftingResults() {
 		super.updateCraftingResults();
 		Iterator var1 = this.crafters.iterator();
 
 		while (var1.hasNext()) {
 			ICrafting var2 = (ICrafting) var1.next();
 
 			if (this.lastCookTime != this.furnace.furnaceCookTime) {
 				var2.sendProgressBarUpdate(this, 0, this.furnace.furnaceCookTime);
 			}
 
 			if (this.lastBurnTime != this.furnace.furnaceBurnTime) {
 				var2.sendProgressBarUpdate(this, 1, this.furnace.furnaceBurnTime);
 			}
 
 			if (this.lastItemBurnTime != this.furnace.currentItemBurnTime) {
 				var2.sendProgressBarUpdate(this, 2, this.furnace.currentItemBurnTime);
 			}
 		}
 
 		this.lastCookTime = this.furnace.furnaceCookTime;
 		this.lastBurnTime = this.furnace.furnaceBurnTime;
 		this.lastItemBurnTime = this.furnace.currentItemBurnTime;
 	}
 
 	@SideOnly(Side.CLIENT)
 	@Override
 	public void updateProgressBar(int par1, int par2) {
 		if (par1 == 0) {
 			this.furnace.furnaceCookTime = par2;
 		}
 
 		if (par1 == 1) {
 			this.furnace.furnaceBurnTime = par2;
 		}
 
 		if (par1 == 2) {
 			this.furnace.currentItemBurnTime = par2;
 		}
 	}
 
 	@Override
 	public void addCraftingToCrafters(ICrafting par1ICrafting) {
 		super.addCraftingToCrafters(par1ICrafting);
 		par1ICrafting.sendProgressBarUpdate(this, 0, this.furnace.furnaceCookTime);
 		par1ICrafting.sendProgressBarUpdate(this, 1, this.furnace.furnaceBurnTime);
 		par1ICrafting.sendProgressBarUpdate(this, 2, this.furnace.currentItemBurnTime);
 	}
 
 	@Override
 	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int slot_index) {
 		ItemStack stack = null;
 		Slot slot_object = (Slot) inventorySlots.get(slot_index);
		System.out.println(slot_index);
 
 		if (slot_object != null && slot_object.getHasStack()) {
 			ItemStack stack_in_slot = slot_object.getStack();
 			stack = stack_in_slot.copy();
 
 			if (slot_index == this.furnace.nOutputSlot) {
 				if (!this.mergeItemStack(stack_in_slot, 3, 38, true)) {
 					return null;
 				}
 				slot_object.onSlotChange(stack_in_slot, stack);
 			} else if (slot_index >= 3 && slot_index <= 29) { // player inventory
 				if (stack_in_slot.itemID == NetherItems.NetherWoodCharcoal.shiftedIndex) {
 					if (!this.mergeItemStack(stack_in_slot, this.furnace.nFuelSlot, this.furnace.nFuelSlot + 1, false)) {
 						return null;
 					}
 				} else if (stack_in_slot.itemID == new ItemStack(bstramke.NetherStuffs.NetherStuffs.NetherOreBlockId, 0, 0).itemID) {
 					if (!this.mergeItemStack(stack_in_slot, this.furnace.nSmeltedSlot, this.furnace.nSmeltedSlot + 1, false)) {
 						return null;
 					}
 				} else if (!this.mergeItemStack(stack_in_slot, 29, 38, false)) {
 					return null;
 				}
 			} else if (slot_index > 29 && slot_index < 39) { // player inventory slot bar
 				if (stack_in_slot.itemID == NetherItems.NetherWoodCharcoal.shiftedIndex) {
 					if (!this.mergeItemStack(stack_in_slot, this.furnace.nFuelSlot, this.furnace.nFuelSlot + 1, false)) {
 						return null;
 					}
 				} else if (stack_in_slot.itemID == new ItemStack(bstramke.NetherStuffs.NetherStuffs.NetherOreBlockId, 0, 0).itemID) {
 					if (!this.mergeItemStack(stack_in_slot, this.furnace.nSmeltedSlot, this.furnace.nSmeltedSlot + 1, false)) {
 						return null;
 					}
 				} else if (!this.mergeItemStack(stack_in_slot, 3, 29, false)) {
 					return null;
 				}
 			} else if (!this.mergeItemStack(stack_in_slot, 3, 38, false)) {
 				return null;
 			}
 
 			if (stack_in_slot.stackSize == 0) {
 				slot_object.putStack(null);
 			} else {
 				slot_object.onSlotChanged();
 			}
 
 			if (stack_in_slot.stackSize == stack.stackSize)
 				return null;
 
 			slot_object.onPickupFromSlot(par1EntityPlayer, stack_in_slot);
 		}
 
 		return stack;
 	}
 }
