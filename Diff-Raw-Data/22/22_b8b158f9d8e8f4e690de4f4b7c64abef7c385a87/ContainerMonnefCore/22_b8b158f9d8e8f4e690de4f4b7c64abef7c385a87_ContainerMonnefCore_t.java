 /*
  * Jaffas and more!
  * author: monnef
  */
 
 package monnef.core.block;
 
 import monnef.core.MonnefCorePlugin;
 import monnef.core.common.ContainerRegistry;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.inventory.Container;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.inventory.Slot;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 
 public abstract class ContainerMonnefCore extends Container {
     private static final boolean PRINT_DEBUG_TRANSFER_MESSAGES = false;
 
     protected TileEntity tile;
     private ContainerRegistry.ContainerDescriptor descriptor;
 
     protected ContainerMonnefCore(InventoryPlayer inventoryPlayer, TileEntity tile) {
         this.tile = tile;
         if (!(tile instanceof IInventory)) {
             throw new RuntimeException("Linked tile entity must implement IInventory.");
         }
 
         setupDescriptor();
 
         constructSlots((IInventory) tile);
        if (inventorySlots.size() != getSlotsCount()) {
            throw new RuntimeException("Expected count of slots is " + getSlotsCount() + ", but current number of slots is " + inventorySlots.size() + ".");
        }
 
         bindPlayerInventory(inventoryPlayer);
     }
 
     private void setupDescriptor() {
         descriptor = ContainerRegistry.getContainerPrototype(tile.getClass());
     }
 
     public int getYSize() {
         return 166;
     }
 
     public int getYPlayerInvShift() {
         return 0;
     }
 
     public int getXSize() {
         return 176;
     }
 
     public int getXPlayerInvShift() {
         return 0;
     }
 
     protected void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
         int yShift = getYSize() - 82 + getYPlayerInvShift(); // 84
         int xShift = getXSize() - 168 + getXPlayerInvShift(); // 8
         for (int i = 0; i < 3; i++) {
             for (int j = 0; j < 9; j++) {
                 addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9,
                         xShift + j * 18, yShift + i * 18));
             }
         }
 
         for (int i = 0; i < 9; i++) {
             addSlotToContainer(new Slot(inventoryPlayer, i, xShift + i * 18, 142 - 84 + yShift));
         }
     }
 
     @Override
     public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
         ItemStack stack = null;
         Slot slotObject = (Slot) inventorySlots.get(slot);
 
         int slots = getSlotsCount();
 
         if (PRINT_DEBUG_TRANSFER_MESSAGES && MonnefCorePlugin.debugEnv) {
             MonnefCorePlugin.Log.printDebug(this.getClass().getSimpleName() + ": transferStackInSlot - slot#=" + slot);
         }
 
         //null checks and checks if the item can be stacked (maxStackSize > 1)
         if (slotObject != null && slotObject.getHasStack()) {
             ItemStack stackInSlot = slotObject.getStack();
             stack = stackInSlot.copy();
 
             //merges the item into player inventory since its in the tileEntity
             if (slot < slots) {
                 if (!this.mergeItemStack(stackInSlot, slots, 36 + slots, true)) {
                     return null;
                 }
             }
             //places it into the tileEntity is possible since its in the player inventory
             else {
                 if (!this.mergeItemStack(stackInSlot, 0, getInputSlotsCount(), false)) {
                     return null;
                 }
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
 
     public int getSlotsCount() {
         return descriptor.getSlotsCount();
     }
 
     public int getOutputSlotsCount() {
         return descriptor.getOutputSlotsCount();
     }
 
     public int getInputSlotsCount() {
         return getSlotsCount() - getOutputSlotsCount();
     }
 
     public int getStartIndexOfOutput() {
         return getSlotsCount() - getOutputSlotsCount();
     }
 
     @Override
     public boolean canInteractWith(EntityPlayer player) {
         return ((IInventory) tile).isUseableByPlayer(player);
     }
 
     /**
      * Creates slots via addSlotToContainer.
      * Output slots *must* be created last!
      *
      * @param inv The inventory.
      */
     public abstract void constructSlots(IInventory inv);
 
     protected boolean mergeItemStack(ItemStack stack, int startingIndex, int endingIndex, boolean fromEnd) {
         return super.mergeItemStack(stack, startingIndex, endingIndex, fromEnd);
     }
 }
