 
 package me.heldplayer.util.HeldCore.sync;
 
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.CompressedStreamTools;
 import net.minecraft.nbt.NBTTagCompound;
 
 import com.google.common.io.ByteArrayDataInput;
 
 public class SInventoryStack extends BaseSyncable {
 
     private IInventory inventory;
     private int slot;
    private ItemStack prevValue;
 
     public SInventoryStack(ISyncableObjectOwner owner, IInventory inventory, int slot) {
         super(owner);
         this.inventory = inventory;
         this.slot = slot;
        this.prevValue = inventory.getStackInSlot(slot);
    }

    @Override
    public boolean hasChanged() {
        ItemStack stack = inventory.getStackInSlot(slot);
        if (prevValue == stack || (prevValue != null && stack != null && ItemStack.areItemStacksEqual(prevValue, stack))) {
            return false;
        }
        this.prevValue = stack;
        return true;
    }

    public void setValue(ItemStack value) {
        this.inventory.setInventorySlotContents(slot, value);
     }
 
     public ItemStack getValue() {
         return this.inventory.getStackInSlot(this.slot);
     }
 
     @Override
     public void read(ByteArrayDataInput in) throws IOException {
         boolean isNull = in.readBoolean();
         if (isNull) {
             this.inventory.setInventorySlotContents(slot, null);
         }
         else {
             NBTTagCompound tag = CompressedStreamTools.read(in);
             this.inventory.setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(tag));
         }
     }
 
     @Override
     public void write(DataOutputStream out) throws IOException {
         ItemStack stack = this.inventory.getStackInSlot(this.slot);
         if (stack == null) {
             out.writeBoolean(true);
         }
         else {
             out.writeBoolean(false);
             NBTTagCompound tag = stack.writeToNBT(new NBTTagCompound());
             CompressedStreamTools.write(tag, out);
         }
     }
 
     @Override
     public String toString() {
         return "InventoryStack: " + this.inventory.getStackInSlot(this.slot).toString();
     }
 
 }
