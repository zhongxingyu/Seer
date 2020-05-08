 package ro.narc.liquiduu;
 
 import cpw.mods.fml.common.Side;
 import cpw.mods.fml.common.asm.SideOnly;
 
 import net.minecraft.src.Container;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.ICrafting;
 import net.minecraft.src.IInventory;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.Slot;
 
 public class ContainerAccelerator extends Container {
     public TileEntityAccelerator accelerator;
 
     public ContainerAccelerator(IInventory inventory, TileEntityAccelerator accelerator) {
         this.accelerator = accelerator;
 
         // Input slot: 9, 25
         // Output slot: 116, 25
         // Liquid item input: 152, 8
         // Machine display: 61, 24
         addSlotToContainer(new Slot(accelerator, 0, 9, 25));
         addSlotToContainer(new SlotOutput(accelerator, 1, 116, 25));
         addSlotToContainer(new SlotUUM(accelerator, 2, 152, 8));
         addSlotToContainer(new Slot(accelerator, -1, 61, 24));
 
         bindPlayerInventory(inventory);
     }
 
     public void bindPlayerInventory(IInventory inventory) {
         // main 9x3 slot inventory at 8, 84; inventory slots 9-35; container slots 4-30
         for(int i = 0; i < 3; i++) {
             for(int j = 0; j < 9; j++) {
                 addSlotToContainer(new Slot(inventory, 9 + (i * 9) + j,
                         8 + (j * 18), 84 + (i * 18)));
             }
         }
 
         // hotbar at 8, 142; inventory slots 0-8; container slots 31-39
         for(int i = 0; i < 9; i++) {
             addSlotToContainer(new Slot(inventory, i,
                     8 + (i * 18), 142));
         }
     }
 
     @Override
     public boolean canInteractWith(EntityPlayer player) {
         return accelerator.isUseableByPlayer(player);
     }
 
     // I'm guessing par2 = mouseButton and par3 = shiftKeyDown
     @Override
     public ItemStack slotClick(int slotnum, int par2, int par3, EntityPlayer player) {
         if(slotnum == 3) {
             return null; // The machine in slot 4 is for display ONLY!
         }
         return super.slotClick(slotnum, par2, par3, player);
     }
 
     @Override
     public ItemStack transferStackInSlot(EntityPlayer player, int slotnum) {
         Slot slot = (Slot)inventorySlots.get(slotnum);
 
         if(slot == null || (!slot.getHasStack())) {
             return null;
         }
 
         ItemStack originalStack = slot.getStack();
         ItemStack workStack = originalStack.copy();
 
         // Slots 0, 1 and 2 are perfectly normal:
        if((slotnum == 0) || (slotnum == 2)) {
             if(!mergeItemStack(workStack, 4, inventorySlots.size(), false)) {
                 return null;
             }
         }
         else if(slotnum >= 4) { // From player inventory
             if(accelerator.isItemStackUUM(workStack)) {
                 if(!mergeItemStack(workStack, 2, 3, false)) {
                     return null;
                 }
             }
             else {
                 if(!mergeItemStack(workStack, 0, 1, false)) {
                     return null;
                 }
             }
         }
 
         if(slotnum == 1) { // Output slot does need to notify the machine.
             accelerator.getOutput(originalStack.stackSize, true);
         }
 
         if(workStack.stackSize == 0) {
             slot.putStack(null);
         }
         else {
             slot.onSlotChanged();
         }
 
         return originalStack;
     }
 
     @Override
     public void updateCraftingResults() {
         super.updateCraftingResults();
 
         for(int i = 0; i < crafters.size(); i++) {
             accelerator.sendGUINetworkData(this, (ICrafting) crafters.get(i));
         }
     }
 
     @SideOnly(Side.CLIENT)
     @Override
     public void updateProgressBar(int key, int value) {
         accelerator.getGUINetworkData(key, value);
     }
 }
