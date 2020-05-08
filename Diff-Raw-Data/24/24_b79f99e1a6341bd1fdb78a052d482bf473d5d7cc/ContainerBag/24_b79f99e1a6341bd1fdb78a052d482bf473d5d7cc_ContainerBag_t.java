 package ak.HyperDimensionalBag;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.inventory.Container;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.inventory.Slot;
 import net.minecraft.item.ItemStack;
 
 public class ContainerBag extends Container
 {
 	IInventory BagInv;
 	int metadmg;
 	public ContainerBag(InventoryPlayer invPlayer, IInventory inv, int meta)
 	{
 		BagInv = inv;
 		metadmg = meta;
 		inv.openChest();
 		for(int i = 0;i<6;i++){
 			for(int j = 0;j<9;j++){
				this.addSlotToContainer(new SlotBag(inv, j + i * 9, 8 + j * 18, 14 + i * 18));
 			}
 		}
 		bindPlayerInventory(invPlayer);
 	}
 	protected void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
 		for (int i = 0; i < 3; i++) {
 			for (int j = 0; j < 9; j++) {
 				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9,
 						8 + j * 18, 126 + i * 18));
 			}
 		}
 
 		for (int i = 0; i < 9; i++) {
 			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 184));
 		}
 	}
 	@Override
 	public boolean canInteractWith(EntityPlayer entityplayer) {
 		ItemStack item = entityplayer.getCurrentEquippedItem();
 		return  item != null && item.getItem() instanceof ItemHDBag && item.getItemDamage() == metadmg;
 	}
 	 public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2)
 	 {
 		 ItemStack itemstack = null;
 		 Slot slot = (Slot)this.inventorySlots.get(par2);
 
 		 if (slot != null && slot.getHasStack())
 		 {
 			 ItemStack itemstack1 = slot.getStack();
 			 itemstack = itemstack1.copy();
 
 			 if (par2 < this.BagInv.getSizeInventory())
 			 {
 				 if (!this.mergeItemStack(itemstack1, this.BagInv.getSizeInventory(), this.inventorySlots.size(), true))
 				 {
 					 return null;
 				 }
 			 }
 			 else if(itemstack1.getItem() instanceof ItemHDBag)
 				 return null;
 			 else if (!this.mergeItemStack(itemstack1, 0, this.BagInv.getSizeInventory(), false))
 			 {
 				 return null;
 			 }
 
 			 if (itemstack1.stackSize == 0)
 			 {
 				 slot.putStack((ItemStack)null);
 			 }
 			 else
 			 {
 				 slot.onSlotChanged();
 			 }
 		 }
 
 		 return itemstack;
 	 }
 }
