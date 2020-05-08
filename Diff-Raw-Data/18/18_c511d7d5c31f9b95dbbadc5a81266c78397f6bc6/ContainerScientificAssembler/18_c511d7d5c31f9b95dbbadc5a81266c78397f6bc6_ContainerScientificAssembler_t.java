 package mods.minecraft.darth.dc.inventory;
 
 import mods.minecraft.darth.dc.client.gui.slots.SlotOutput;
 import mods.minecraft.darth.dc.client.gui.slots.SlotWorkbench;
 import mods.minecraft.darth.dc.tileentity.TileScientificAssembler;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.inventory.Slot;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 
 public class ContainerScientificAssembler extends ContainerDC
 {
     protected TileScientificAssembler tileEntity;
     //private World worldObj;
     public IInventory craftResult;
     
     public ContainerScientificAssembler(InventoryPlayer inventoryPlayer, TileScientificAssembler te, World world)
     {
         super(te.getSizeInventory());
         
         craftResult = new InventoryCraftResultAssembler();
         this.tileEntity = te;
         //this.worldObj = world;
         
         this.addSlotToContainer(new SlotOutput(tileEntity, TileScientificAssembler.SLOT_RESULT, 124, 35));
         
         //the Slot constructor takes the IInventory and the slot number in that it binds to
         //and the x-y coordinates it resides on-screen
         
         int x;
         int y;
         
         //crafting grid
         for (x = 0; x < 3; x++)
         {
             for (y = 0; y < 3; y++)
                 this.addSlotToContainer(new SlotWorkbench(tileEntity.craftMatrix, y + x * 3, 30 + y * 18, 17 + x * 18));
         }
         
         
         //bottom inventory
         for(x = 0; x < 9; x++)
             this.addSlotToContainer(new Slot(tileEntity, 10 + x, 8 + x * 18, 82));
         
         this.bindPlayerInventory(inventoryPlayer);
         
         this.onCraftMatrixChanged(tileEntity);
     }
     
     protected void bindPlayerInventory(InventoryPlayer inventoryPlayer)
     {
         int i;
         int j;
         
         //player main inv
         for (i = 0; i < 3; i++)
         {
             for (j = 0; j < 9; j++)
                 addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 111 + i * 18)); 
         }
         
         //player hotbar
         for (j = 0; j < 9; ++j)
             this.addSlotToContainer(new Slot(inventoryPlayer, j, 8 + j * 18, 169));
     }
     
     @Override
     public ItemStack transferStackInSlot(EntityPlayer player, int slot)
     {
         ItemStack stack = null;
         Slot slotObject = (Slot) inventorySlots.get(slot);
         
         //null checks and checks if the item can be stacked (maxStackSize > 1)
         if (slotObject != null && slotObject.getHasStack())
         {
             ItemStack stackInSlot = slotObject.getStack();
             stack = stackInSlot.copy();
 
             if (slot == 0)
             {
                 if (!this.mergeItemStack(stackInSlot, 10, 46, true))
                     return null;
 
                 slotObject.onSlotChange(stackInSlot, stack);
             }
             
             //merges the item into player inventory since its in the tileEntity
             if (slot < TileScientificAssembler.INVENTORY_SIZE)
             {
                if (!this.mergeItemStack(stackInSlot, 19, 55, true))
                     return null;
             }
             //places it into the tileEntity is possible since its in the player inventory
             else if (slot != 0 && !this.mergeItemStack(stackInSlot, 10, TileScientificAssembler.INVENTORY_SIZE - 1, false))
                 return null;
 
             
             if (stackInSlot.stackSize == 0)
                 slotObject.putStack(null);
             else
                 slotObject.onSlotChanged();
 
             if (stackInSlot.stackSize == stack.stackSize)
                 return null;
 
             slotObject.onPickupFromSlot(player, stackInSlot);
         }
         
         return stack;
     }
     
     /**
      * Callback for when the crafting matrix is changed.
      */
     public void onCraftMatrixChanged(IInventory inv)
     {
         super.onCraftMatrixChanged(inv);
         ItemStack output = tileEntity.findRecipeOutput();
         craftResult.setInventorySlotContents(0, output);
     }
     
     @Override
     public boolean canInteractWith(EntityPlayer entityplayer)
     {
         return tileEntity.isUseableByPlayer(entityplayer);
     }
 
 }
