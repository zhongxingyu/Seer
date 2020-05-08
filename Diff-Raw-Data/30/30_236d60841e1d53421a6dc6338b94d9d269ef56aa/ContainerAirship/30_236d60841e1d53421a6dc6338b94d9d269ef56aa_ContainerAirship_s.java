 package mods.pchan3.steamship;
 
 import mods.pchan3.PChan3Mods;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.inventory.Container;
 import net.minecraft.inventory.Slot;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 
 public class ContainerAirship extends Container {
 	private EntityAirship airship;
 	private int numRows;
     public ContainerAirship(InventoryPlayer inventoryPlayer, EntityAirship air) {
     	this.airship=air;
         this.numRows = (air.getSizeInventory()-2)/ 4;
         air.openChest();
         this.windowId=PChan3Mods.instance.GUI_ID;//Avoid conflict with other screen
         this.addSlotToContainer(new Slot(air, 0, 134, 16));
         this.addSlotToContainer(new Slot(air, 1, 134, 52));
 
 	for (int k = 0; k < this.numRows; k++) {
 	    for (int l = 0; l < 4; l++) {
 	    	this.addSlotToContainer(new Slot(air, l + k * 4 + 2, 8 + l * 18, 16 + k * 18));
 	    }
 	}
 	
     for (int i = 0; i < 3; i++) {
             for (int j = 0; j < 9; j++) {
             this.addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
             }
     }
     for (int i = 0; i < 9; i++) {
             this.addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
     }
 }
 
 	@Override
     public ItemStack transferStackInSlot(EntityPlayer player, int i) {
 		ItemStack itemstack = null;
 		Slot slot = (Slot) this.inventorySlots.get(i);
 		if (slot != null && slot.getHasStack()) {
 		    ItemStack itemstack1 = slot.getStack();
 		    itemstack = itemstack1.copy();
 		    if (itemstack1.itemID==Item.coal.itemID && i!=0)
 		    	this.mergeItemStack(itemstack1, 0, 1, false);//Put coal into fuel slot
 		    else if (itemstack1.itemID==Item.arrow.itemID && i!=1)
 		    	this.mergeItemStack(itemstack1, 1, 2, true);//Put arrows into arrow slot
 		    else if (i < this.airship.getSizeInventory()) //From airship inventory to player inventory
 		    { 
		    	if(!this.mergeItemStack(itemstack1, this.airship.getSizeInventory(), this.inventorySlots.size(), true))
		    		return null;
 		    } 	    
		    else if (!this.mergeItemStack(itemstack1, 2, this.airship.getSizeInventory(), false))
		    	return null;
 		    
 		    if (itemstack1.stackSize == 0) 
 		    {
 		    	slot.putStack((ItemStack)null);
 		    } 
 		    else 
 		    {
 		    	slot.onSlotChanged();
 		    }
 		    if (itemstack1.stackSize != itemstack.stackSize)
 		    {
 		    	slot.onPickupFromSlot(player, itemstack1);
 		    } 
 		    else 
 		    {
 		    	return null;
 		    }
 		}
 		return itemstack;
     }
     @Override
     public boolean canInteractWith(EntityPlayer player) {
     	return this.airship.isUseableByPlayer(player);
     }
     @Override
     public void onCraftGuiClosed(EntityPlayer par1EntityPlayer)
     {
         super.onCraftGuiClosed(par1EntityPlayer);
         this.airship.closeChest();
     }
 }
