 package mods.minecraft.darth.dc.tileentity;
 
 import mods.minecraft.darth.dc.DiscoveryCraft;
 import mods.minecraft.darth.dc.core.util.*;
 import mods.minecraft.darth.dc.inventory.InventoryCraftResultAssembler;
 import mods.minecraft.darth.dc.inventory.InventoryOutputAssembler;
 import mods.minecraft.darth.dc.inventory.sample.*;
 import mods.minecraft.darth.dc.inventory.sample.InventoryIterator.IInvSlot;
 import mods.minecraft.darth.dc.lib.Strings;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.Container;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.inventory.ISidedInventory;
 import net.minecraft.inventory.InventoryCrafting;
 import net.minecraft.inventory.SlotCrafting;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.IRecipe;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.ChatMessageComponent;
 import net.minecraft.util.ChunkCoordinates;
 
 import net.minecraftforge.common.ForgeDirection;
 
 public class TileScientificAssembler extends TileDC implements ISidedInventory
 {
     public static final int SLOT_RESULT = 0;
     public static final int INVENTORY_SIZE = 9 /*Crafting Grid*/ + 1 /*Output Slot*/ + (1 * 9) /*Storage*/;
     private static final int[] SLOTS = GeneralUtil.createSlotArray(0, INVENTORY_SIZE);
     
     private InventoryOutputAssembler resultInv = new InventoryOutputAssembler(1, "Sci Assembler Output", 64);
     private SimpleInventory storageInv = new SimpleInventory(9, "Sci Assembler Storage", 64);
     public InventoryCrafting craftMatrix = new LocalInventoryCrafting();
     public SlotCrafting craftSlot;
     private InventoryCraftResultAssembler craftResult = new InventoryCraftResultAssembler();
     private EntityPlayer internalPlayer;
     
     private IInventory inv = InventoryConcatenator.make().add(resultInv).add(craftMatrix).add(storageInv);
     
     
     private class LocalInventoryCrafting extends InventoryCrafting
     {
         public LocalInventoryCrafting()
         {
             super(new Container(){ @Override public boolean canInteractWith(EntityPlayer entityplayer) { return false; }}
             , 3, 3);
         }
     }
     
     private final class InternalPlayer extends EntityPlayer
     {
 
         public InternalPlayer()
         {
             super(TileScientificAssembler.this.worldObj, "[DiscoveryCraft]");
             posX = TileScientificAssembler.this.xCoord;
             posY = TileScientificAssembler.this.yCoord + 1;
             posZ = TileScientificAssembler.this.zCoord;
         }
 
         @Override
         public void sendChatToPlayer(ChatMessageComponent var1)
         {
         }
 
         @Override
         public boolean canCommandSenderUseCommand(int var1, String var2)
         {
             return false;
         }
 
         @Override
         public ChunkCoordinates getPlayerCoordinates()
         {
             return null;
         }
     }
     
     @Override
     public int getSizeInventory()
     {
         return INVENTORY_SIZE;
     }
 
     @Override
     public ItemStack getStackInSlot(int slot)
     {
         return inv.getStackInSlot(slot);
     }
 
     @Override
     public ItemStack decrStackSize(int slot, int amt)
     {
         return inv.decrStackSize(slot, amt);
     }
 
     @Override
     public ItemStack getStackInSlotOnClosing(int slot)
     {
         return inv.getStackInSlotOnClosing(slot);
     }
 
     @Override
     public void setInventorySlotContents(int slot, ItemStack itemStack)
     {
         inv.setInventorySlotContents(slot, itemStack);
     }
 
     @Override
     public String getInvName()
     {
         return this.hasCustomName() ? this.getCustomName() : Strings.CONTAINER_SCI_ASSEMBLER_NAME;
     }
 
     @Override
     public boolean isInvNameLocalized()
     {
         return this.hasCustomName();
     }
 
     @Override
     public int getInventoryStackLimit()
     {
         return 64;
     }
 
     @Override
     public void openChest()
     {
     }
 
     @Override
     public void closeChest()
     {      
     }
     
     @Override
     public String toString()
     {
         StringBuilder s = new StringBuilder();
         s.append(super.toString());
         s.append("TileScientificAssembler Data - ");
         
         return s.toString();
     }
     
     @Override
     public void readFromNBT(NBTTagCompound tagCompound)
     {
         super.readFromNBT(tagCompound);
         resultInv.readFromNBT(tagCompound);
         NBTUtil.readInvFromNBT(craftMatrix, "matrix", tagCompound);
 
         // Legacy Code
         if (tagCompound.hasKey("stackList"))
         {
             ItemStack[] stacks = new ItemStack[9];
             NBTUtil.readStacksFromNBT(tagCompound, "stackList", stacks);
             
             for (int i = 0; i < 9; i++)
                 craftMatrix.setInventorySlotContents(i, stacks[i]);
         }
     }
     
     @Override
     public void writeToNBT(NBTTagCompound tagCompound)
     {
         super.writeToNBT(tagCompound);
         resultInv.writeToNBT(tagCompound);
         NBTUtil.writeInvToNBT(craftMatrix, "matrix", tagCompound);
     }
 
     public ItemStack findRecipeOutput()
     {
         IRecipe recipe = findRecipe();
         
         if (recipe == null)
             return null;
 
         ItemStack result = recipe.getCraftingResult(craftMatrix);
         
         if (result != null)
             result = result.copy();
 
         return result;
     }
 
     public IRecipe findRecipe()
     {
         for (IInvSlot slot : InventoryIterator.getIterable(craftMatrix, ForgeDirection.UP))
         {
             ItemStack stack = slot.getStackInSlot();
             
             if (stack == null)
                 continue;
         }
 
         return CraftingUtil.findMatchingRecipe(craftMatrix, worldObj);
     }
 
     @Override
     public boolean isItemValidForSlot(int slot, ItemStack stack)
     {
         if (slot == SLOT_RESULT || slot < 10)
             return false;
         if (stack == null)
             return false;
         
         return true;
     }
     
     @Override
     public boolean canInsertItem(int slot, ItemStack stack, int side)
     {
         return isItemValidForSlot(slot, stack);
     }
 
     @Override
     public boolean canExtractItem(int slot, ItemStack stack, int side)
     {
         return slot == SLOT_RESULT;
     }
 
     @Override
     public int[] getAccessibleSlotsFromSide(int var1)
     {
         return SLOTS;
     }
     
     @Override
     public void updateEntity()
     {
         super.updateEntity();
         
         if (DiscoveryCraft.proxy.isRenderWorld(worldObj))
             return;
 
         if (craftSlot == null)
         {
             internalPlayer = new InternalPlayer();
             craftSlot = new SlotCrafting(internalPlayer, craftMatrix, craftResult, 0, 0, 0);
         }
         
         if (resultInv.getStackInSlot(SLOT_RESULT) != null)
             return;
 
         updateCrafting();
     }
     
     private void updateCrafting()
     {
         IRecipe recipe = findRecipe();
         
         if (recipe == null)
             return;
         
         ItemStack result = recipe.getCraftingResult(craftMatrix);
         
         if (result == null)
             return;
 
         result = result.copy();
        
         resultInv.setInventorySlotContents(SLOT_RESULT, result);
        pickup(result);
         //clean fake player inventory (crafting handler support)
         for (IInvSlot slot : InventoryIterator.getIterable(internalPlayer.inventory, ForgeDirection.UP))
         {
             ItemStack stack = slot.getStackInSlot();
             
             if (stack != null)
             {
                 slot.setStackInSlot(null);
                 ItemUtil.dropItems(worldObj, stack, xCoord, yCoord + 1, zCoord);
             }
         }
     }
    
    public void pickup(ItemStack out)
    {
        craftSlot.onPickupFromSlot(internalPlayer, out); //TODO
    }
 }
