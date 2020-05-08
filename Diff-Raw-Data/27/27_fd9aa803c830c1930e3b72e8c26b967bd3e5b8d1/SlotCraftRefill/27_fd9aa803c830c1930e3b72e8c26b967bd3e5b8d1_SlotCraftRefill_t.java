 package slimevoid.projectbench.container;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.inventory.SlotCrafting;
 import net.minecraft.item.ItemStack;
 import slimevoid.projectbench.core.lib.InventoryMatch;
 import slimevoid.projectbench.core.lib.ItemLib;
 
 // Referenced classes of package com.eloraam.redpower.base:
 //            ContainerAdvBench
 
 public class SlotCraftRefill extends SlotCrafting {
 
 	IInventory[] allInventories;
 	IInventory craftingMatrix;
 	ContainerProjectBench eventHandler;
 	private boolean PlayerFirstCraft;
 
 	public SlotCraftRefill(EntityPlayer entityplayer, IInventory matrix,
 			IInventory result, IInventory[] allinventories,
 			ContainerProjectBench evh, int i, int j, int k) {
 		super(entityplayer, matrix, result, i, j, k);
 		allInventories = allinventories;
 		craftingMatrix = matrix;
 		eventHandler = evh;
 	}
 
 	private InventoryMatch findMatch(ItemStack itemstack) {
 		
 		for (int j = 0; j < this.allInventories.length; j++) {
 			for (int i = 0; i < allInventories[j].getSizeInventory(); i++) {
 				if (j == 0 && i == 0)
 					i = 10;
 				
 				ItemStack test = allInventories[j].getStackInSlot(i);
 				if (test != null && test.stackSize != 0
						&& ItemLib.matchOre(itemstack, test)) {								
 					return new InventoryMatch(allInventories[j],i);
 				}
 
 			}
 		}
 		return null;
 	}
 
 	public boolean isLastUse() {
 		int bits = 0;
 
 		for (int i = 0; i < 9; i++) {
 			ItemStack st = this.craftingMatrix.getStackInSlot(i);
 			if (st == null) {
 				bits |= 1 << i;
 				continue;
 			}
 			if (!st.isStackable()) {
 				bits |= 1 << i;
 				continue;
 			}
 			if (st.stackSize > 1)
 				bits |= 1 << i;
 		}
 
 		if (bits == 511)
 			return false;
 		for (int k = 0; k < this.allInventories.length; k++) {
 			for (int i = 0; i < this.allInventories[k].getSizeInventory(); i++) {
 				if (k == 0 && i == 0)
 					i = 10;
 				ItemStack test = this.allInventories[k].getStackInSlot(i);
 				if (test == null || test.stackSize == 0)
 					continue;
 				int sc = test.stackSize;
 				for (int j = 0; j < 9; j++) {
 					if ((bits & 1 << j) > 0)
 						continue;
 					ItemStack st = this.craftingMatrix.getStackInSlot(j);
 					if (st == null || !ItemLib.matchOre(st, test))
 						continue;
 					bits |= 1 << j;
 					if (--sc == 0)
 						break;
 				}
 
 			}
 		}
 		return bits != 511;
 	}
 
 	@Override
 	public void onPickupFromSlot(EntityPlayer player, ItemStack ist) {
 		ItemStack plan[] = eventHandler.getPlanItems();
 		ItemStack cur[] = new ItemStack[9];
 		for (int i = 0; i < 9; i++) {
 			ItemStack st = this.craftingMatrix.getStackInSlot(i);
 			if (st == null)
 				cur[i] = null;
 			else
 				cur[i] = st.copy();
 		}
 
 		boolean last = isLastUse();
 		if (plan != null) {
 			for (int i = 0; i < 9; i++) {
 				if (cur[i] != null || plan[i] == null)
 					continue;
 				InventoryMatch p = findMatch(plan[i]);
 				if (p == null)
 					continue;
 				ItemStack is2 = p.InventoryMatch
 						.getStackInSlot(p.slotIndex);
 				if (is2 == null)
 					continue;
 				p.InventoryMatch.decrStackSize(
 						p.slotIndex, 1);
 				if (is2.getItem().hasContainerItem()) {
 					ItemStack is3 = is2.getItem().getContainerItemStack(is2);
 					p.InventoryMatch
 							.setInventorySlotContents(p.slotIndex, is3);
 				}
 			}
 
 		}
 		super.onPickupFromSlot(player, ist);
 		if (last) {
 			eventHandler.onCraftMatrixChanged(craftingMatrix);
 			return;
 		}
 		// boolean ch = false;
 		for (int i = 0; i < 9; i++) {
 			if (cur[i] == null)
 				continue;
 			ItemStack nsl = this.craftingMatrix.getStackInSlot(i);
 			if (plan != null && plan[i] != null)
 				continue;
 			InventoryMatch p;
 			if (nsl != null) {
 				if (ItemLib.matchOre(nsl, cur[i])
 						|| !cur[i].getItem().hasContainerItem())
 					continue;
 				ItemStack ctr = cur[i].getItem().getContainerItemStack(cur[i]);
 				if (ctr == null || ctr.getItem().itemID != nsl.getItem().itemID)
 					continue;
 				p = findMatch(cur[i]);
 				if (p.slotIndex >= 0) {
 					ItemStack i1 = p.InventoryMatch
 							.getStackInSlot(p.slotIndex);
 					p.InventoryMatch
 							.setInventorySlotContents(p.slotIndex, nsl);
 					this.craftingMatrix.setInventorySlotContents(i, i1);
 					// ch = true;
 				}
 				continue;
 			}
 			p = findMatch(cur[i]);
 			if (p.slotIndex >= 0) {
 				ItemStack i1 = p.InventoryMatch
 						.getStackInSlot(p.slotIndex);
 				this.craftingMatrix.setInventorySlotContents(i,
 						p.InventoryMatch.decrStackSize(
 								p.slotIndex, 1));
 				// ch = true;
 			}
 		}
 
 		eventHandler.onCraftMatrixChanged(craftingMatrix);
 	}
 
 }
