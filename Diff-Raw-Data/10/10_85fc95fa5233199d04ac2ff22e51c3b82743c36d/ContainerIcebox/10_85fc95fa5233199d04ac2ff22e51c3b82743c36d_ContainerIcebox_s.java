 package com.teammetallurgy.agriculture.gui;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.inventory.Container;
 import net.minecraft.inventory.Slot;
 import net.minecraft.item.ItemStack;
 
 import com.teammetallurgy.agriculture.machines.icebox.InventoryIcebox;
 import com.teammetallurgy.agriculture.machines.icebox.TileEntityIcebox;
 import com.teammetallurgy.agriculture.machines.oven.TileEntityOven;
 
 public class ContainerIcebox extends Container
 {
 	private final InventoryIcebox oven;
 	private final TileEntityIcebox teOven;
 
 	public ContainerIcebox(InventoryPlayer invePlayer, TileEntityIcebox teOven)
 	{
 		oven = teOven.getInventory();
 		oven.openChest();
 
 		this.teOven = teOven;
 		int i;
 
 		// Fuel Slot
		addSlotToContainer(new Slot(oven, 0, 8, 34));
 
 		for (int x = 0; x < 4; x++)
 		{
 			for (int y = 0; y < 3; y++)
 			{
 				addSlotToContainer(new Slot(oven, x + 1 + y * 4, 53 + x * 18, 15 + y * 18));
 			}
 		}
 
 		for (i = 0; i < 3; ++i)
 		{
 			for (int j = 0; j < 9; ++j)
 			{
 				addSlotToContainer(new Slot(invePlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
 			}
 		}
 
 		for (i = 0; i < 9; ++i)
 		{
 			addSlotToContainer(new Slot(invePlayer, i, 8 + i * 18, 142));
 		}
 	}
 
 	@Override
 	public boolean canDragIntoSlot(Slot par1Slot)
 	{
 		return true;
 	}
 
 	@Override
 	public boolean canInteractWith(EntityPlayer entityplayer)
 	{
 		return true;
 	}
 
 	public TileEntityIcebox getOven()
 	{
 		return teOven;
 	}
 
 	protected boolean mergeItemStack2(ItemStack par1ItemStack, int par2, int par3, boolean par4)
 	{
 		boolean flag1 = false;
 		int k = par2;
 
 		if (par4)
 		{
 			k = par3 - 1;
 		}
 
 		Slot slot;
 		ItemStack itemstack1;
 
 		if (par1ItemStack.isStackable())
 		{
 			if (par1ItemStack.stackSize > 0 && (!par4 && k < par3 || par4 && k >= par2))
 			{
 				slot = (Slot) inventorySlots.get(k);
 				itemstack1 = slot.getStack();
 
 				if (itemstack1 != null && slot.isItemValid(par1ItemStack) && itemstack1.itemID == par1ItemStack.itemID && (!par1ItemStack.getHasSubtypes() || par1ItemStack.getItemDamage() == itemstack1.getItemDamage()) && ItemStack.areItemStackTagsEqual(par1ItemStack, itemstack1))
 				{
 					final int l = itemstack1.stackSize + par1ItemStack.stackSize;
 
 					if (l <= slot.getSlotStackLimit())
 					{
 						par1ItemStack.stackSize = 0;
 						itemstack1.stackSize = l;
 						slot.onSlotChanged();
 						flag1 = true;
 					} else if (itemstack1.stackSize < slot.getSlotStackLimit())
 					{
 						par1ItemStack.stackSize -= par1ItemStack.getMaxStackSize() - itemstack1.stackSize;
 						itemstack1.stackSize = slot.getSlotStackLimit();
 						slot.onSlotChanged();
 						flag1 = true;
 					}
 				}
 
 				if (par4)
 				{
 					--k;
 				} else
 				{
 					++k;
 				}
 			}
 		}
 
 		if (par1ItemStack.stackSize > 0)
 		{
 			if (par4)
 			{
 				k = par3 - 1;
 			} else
 			{
 				k = par2;
 			}
 
 			while (!par4 && k < par3 || par4 && k >= par2)
 			{
 				slot = (Slot) inventorySlots.get(k);
 				itemstack1 = slot.getStack();
 
 				if (itemstack1 == null && slot.isItemValid(par1ItemStack))
 				{
 					final ItemStack itemStack = par1ItemStack.copy();
 					itemStack.stackSize = 1;
 					slot.putStack(itemStack);
 					slot.onSlotChanged();
 					par1ItemStack.stackSize--;
 					flag1 = true;
 					break;
 				}
 
 				if (par4)
 				{
 					--k;
 				} else
 				{
 					++k;
 				}
 			}
 		}
 
 		return flag1;
 	}
 
 	@Override
 	public void onContainerClosed(EntityPlayer entityplayer)
 	{
 		super.onContainerClosed(entityplayer);
 		oven.closeChest();
 	}
 
 	@Override
 	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2)
 	{
 		ItemStack itemstack = null;
 		final Slot slot = (Slot) inventorySlots.get(par2);
 
 		if (slot != null && slot.getHasStack())
 		{
 			final ItemStack itemstack1 = slot.getStack();
 			itemstack = itemstack1.copy();
 
 			itemstack.getItemDamage();
 
 			if (par2 <= 19)
 			{
 				if (!mergeItemStack(itemstack1, 20, inventorySlots.size(), true))
 				{
 					return null;
 				}
 			} else
 			{
 				if (TileEntityOven.getItemBurnTime(itemstack) > 0)
 				{
 					if (!mergeItemStack(itemstack1, 0, 1, false))
 					{
 						return null;
 					}
 				} else
 				{
 					if (!mergeItemStack2(itemstack1, 1, 13, false))
 					{
 						return null;
 					}
 				}
 			}
 
 			if (itemstack1.stackSize == 0)
 			{
 				slot.putStack((ItemStack) null);
 			} else
 			{
 				slot.onSlotChanged();
 			}
 
 			if (itemstack1.stackSize == itemstack.stackSize)
 			{
 				return null;
 			}
 
 			slot.onPickupFromSlot(par1EntityPlayer, itemstack1);
 		}
 
 		return null;
 
 	}
 
 }
