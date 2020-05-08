 package widux.creativetools;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.Container;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.inventory.Slot;
 import net.minecraft.item.ItemStack;
 
 public class ContainerItemDuplicator extends Container
 {
 	
 	private TileEntityItemDuplicator teItem;
 	
 	public ContainerItemDuplicator(IInventory inventory, TileEntityItemDuplicator teItem)
 	{
 		this.teItem = teItem;
 		
 		this.addSlotToContainer(new Slot(teItem, 0, 48, 35));
 		this.addSlotToContainer(new Slot(teItem, 1, 108, 35));
 		
 		addPlayerInventory(inventory);
 	}
 	
     public boolean canInteractWith(EntityPlayer player)
     {
         return this.teItem.isUseableByPlayer(player);
     }
     
     @Override
     public ItemStack transferStackInSlot(EntityPlayer player, int slotID)
     {
     	ItemStack item = null;
         Slot slot = (Slot) this.inventorySlots.get(slotID);
 
         if (slot != null && slot.getHasStack())
         {
             ItemStack itemInSlot = slot.getStack();
             item = itemInSlot.copy();
 
             if (slotID < 9)
             {
                if (!this.mergeItemStack(itemInSlot, 2, 38, true))
                 {
                     return null;
                 }
             }
             else if (!this.mergeItemStack(itemInSlot, 0, 9, false))
             {
                 return null;
             }
 
             if (itemInSlot.stackSize == 0)
             {
                 slot.putStack((ItemStack)null);
             }
             else
             {
                 slot.onSlotChanged();
             }
 
             if (itemInSlot.stackSize == item.stackSize)
             {
                 return null;
             }
 
             slot.onPickupFromSlot(player, itemInSlot);
         }
         
         return item;
     }
 	
 	protected void addPlayerInventory(IInventory playerInv)
     {
     	for (int i = 0; i < 3; i++)
     	{
     		for (int j = 0; j < 9; j++)
     		{
     			addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
     		}
     	}
 
     	for (int i = 0; i < 9; i++)
     	{
     		addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 142));
     	}
     }
 	
 }
