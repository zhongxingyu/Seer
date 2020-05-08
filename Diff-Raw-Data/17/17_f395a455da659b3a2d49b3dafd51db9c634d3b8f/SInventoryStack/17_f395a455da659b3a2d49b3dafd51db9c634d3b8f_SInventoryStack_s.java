 
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
 
     public SInventoryStack(ISyncableObjectOwner owner, IInventory inventory, int slot) {
         super(owner);
         this.inventory = inventory;
         this.slot = slot;
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
