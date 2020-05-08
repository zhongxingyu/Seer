 package mods.minecraft.darth.dc.tileentity;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.inventory.Container;
 import net.minecraft.inventory.Slot;
 import net.minecraft.item.ItemStack;
 
 public class ContainerScientificAssembler extends Container{
 	protected TileScientificAssembler tileEntity;
 	private int sn = 0;
     public ContainerScientificAssembler (InventoryPlayer inventoryPlayer, TileScientificAssembler te){
         tileEntity = te;
 
         //the Slot constructor takes the IInventory and the slot number in that it binds to
         //and the x-y coordinates it resides on-screen
         
         //crafting grid
         for (int x = 0; x < 3; x++) {
                 for (int y = 0; y < 3; y++) {
                 		
                         addSlotToContainer(new Slot(tileEntity, sn, 30 + x * 18, 17 + y * 18));
                         sn++;
                 }
         }
         //bottom inventory
         for(int x = 0; x < 9; x++){
         	addSlotToContainer(new Slot(tileEntity, sn, 8 + x * 18, 82));
         	sn++;
         }
         
         //result
         	addSlotToContainer(new Slot(tileEntity, sn, 124, 35));
         	sn++;
 
         //commonly used vanilla code that adds the player's inventory
         bindPlayerInventory(inventoryPlayer);
     }
     protected void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
         for (int i = 0; i < 3; i++) {
                 for (int j = 0; j < 9; j++) {
                        addSlotToContainer(new Slot(inventoryPlayer, sn,
                                         8 + j * 18, 111 + i * 18));
                        sn++;
                 }
         }
 
         for (int i = 0; i < 9; i++) {
                addSlotToContainer(new Slot(inventoryPlayer, sn, 8 + i * 18, 169));
                sn++;
         }
 }
 	@Override
 	public boolean canInteractWith(EntityPlayer entityplayer) {
 		
 		return tileEntity.isUseableByPlayer(entityplayer);
 	}
	
     public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
         ItemStack stack = null;
         Slot slotObject = (Slot) inventorySlots.get(slot);
 
         //null checks and checks if the item can be stacked (maxStackSize > 1)
         if (slotObject != null && slotObject.getHasStack()) {
                 ItemStack stackInSlot = slotObject.getStack();
                 stack = stackInSlot.copy();
 
                 //merges the item into player inventory since its in the tileEntity
                 if (slot < tileEntity.INVENTORY_SIZE) {
                         if (!this.mergeItemStack(stackInSlot, 0, 35, true)) {
                                 return null;
                         }
                 }
                 //places it into the tileEntity is possible since its in the player inventory
                 else if (!this.mergeItemStack(stackInSlot, 0, tileEntity.INVENTORY_SIZE, false)) {
                         return null;
                 }
 
                 if (stackInSlot.stackSize == 0) {
                         slotObject.putStack(null);
                 } else {
                         slotObject.onSlotChanged();
                 }
 
                 if (stackInSlot.stackSize == stack.stackSize) {
                         return null;
                 }
                 slotObject.onPickupFromSlot(player, stackInSlot);
         }
         return stack;
 }
 
 }
