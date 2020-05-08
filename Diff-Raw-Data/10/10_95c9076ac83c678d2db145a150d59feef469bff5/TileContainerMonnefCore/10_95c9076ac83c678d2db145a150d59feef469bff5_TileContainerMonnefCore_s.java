 /*
  * Jaffas and more!
  * author: monnef
  */
 
 package monnef.core.block;
 
 import monnef.core.common.ContainerRegistry;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.tileentity.TileEntity;
 
 public abstract class TileContainerMonnefCore extends ContainerMonnefCore {
 
     protected TileEntity tile;
     private ContainerRegistry.ContainerDescriptor descriptor;
 
     protected TileContainerMonnefCore(InventoryPlayer inventoryPlayer, TileEntity tile) {
         super(inventoryPlayer, (IInventory) tile);
         this.tile = tile;
 
         setupDescriptor();
 
         constructSlotsFromTileAndBindPlayerInventory(tile);
        if (inventorySlots.size() != getSlotsCount()) {
             throw new RuntimeException("Expected count of slots is " + getSlotsCount() + ", but current number of slots is " + inventorySlots.size() + ".");
         }
     }
 
     private void setupDescriptor() {
         descriptor = ContainerRegistry.getContainerPrototype(tile.getClass());
     }
 
     @Override
     public int getSlotsCount() {
         return descriptor.getSlotsCount();
     }
 
     @Override
     public int getOutputSlotsCount() {
         return descriptor.getOutputSlotsCount();
     }
 
     @Override
     public boolean canInteractWith(EntityPlayer player) {
         return ((IInventory) tile).isUseableByPlayer(player);
     }
 
     @Override
     public void constructSlotsFromInventory(IInventory inv) {
         // empty, slots are being created in constructSlotsFromTile
     }
 
     public void constructSlotsFromTileAndBindPlayerInventory(TileEntity te) {
         constructSlotsFromTile(te);
         bindPlayerInventory(playerInventory);
     }
 
     public abstract void constructSlotsFromTile(TileEntity te);
 }
