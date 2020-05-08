 package voidrunner101.SoulCraft.common.tileentity;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet132TileEntityData;
 import voidrunner101.SoulCraft.common.proxy.CommonProxy;
 
 public class TileCompressor extends SCTileEntity implements IInventory {
 
 	private ItemStack[] inv;
 	
 	public TileCompressor() {
 		inv = new ItemStack[1];
 	}
 	
 	@Override
 	public int getSizeInventory() {
 		return inv.length;
 	}
 
 	@Override
 	public ItemStack getStackInSlot(int slot) {
 		if(slot > getSizeInventory()) {
 			return null;
 		}
 		return inv[slot];
 	}
 
 	@Override
 	public ItemStack decrStackSize(int slot, int amount) {
 		ItemStack stack = getStackInSlot(slot);
 		if(stack != null)
 		{
 			if(stack.stackSize <= amount)
 			{
 				setInventorySlotContents(slot, null);
 			} 
 			else
 			{
 				stack = stack.splitStack(amount);
 				if(stack.stackSize == 0)
 				{
 					setInventorySlotContents(slot, null);
 				}
 			}
 		}
 		return stack;
 	}
 
 	@Override
 	public ItemStack getStackInSlotOnClosing(int slot) {
 		ItemStack stack = getStackInSlot(slot);
 		if(stack != null)
 		{
 			setInventorySlotContents(slot, null);
 		}
 		return stack;
 	}
 
 	@Override
 	public void setInventorySlotContents(int slot, ItemStack stack) {
 		inv[slot] = stack;
 		if(stack != null && stack.stackSize > getInventoryStackLimit()) {
 			stack.stackSize = getInventoryStackLimit();
 		}
 	}
 
 	@Override
 	public String getInvName() {
 		return "Compressor";
 	}
 
 	@Override
 	public int getInventoryStackLimit() {
 		return 1024;
 	}
 	
 	@Override
 	public boolean isUseableByPlayer(EntityPlayer player) {
 		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this && player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64;
 	}
 	
 	public void readFromNBT(NBTTagCompound par1NBTTagCompound)
     {
         super.readFromNBT(par1NBTTagCompound);
         NBTTagList var2 = par1NBTTagCompound.getTagList("Items");
         this.inv = new ItemStack[this.getSizeInventory()];
 
         for (int var3 = 0; var3 < var2.tagCount(); ++var3)
         {
             NBTTagCompound var4 = (NBTTagCompound)var2.tagAt(var3);
             int var5 = var4.getByte("Slot") & 255;
 
             if (var5 >= 0 && var5 < this.inv.length)
             {
                 this.inv[var5] = ItemStack.loadItemStackFromNBT(var4);
             }
         }
     }
 
 	@Override
 	public void writeToNBT(NBTTagCompound par1NBTTagCompound)
     {
         super.writeToNBT(par1NBTTagCompound);
         NBTTagList var2 = new NBTTagList();
 
         for (int var3 = 0; var3 < this.inv.length; ++var3)
         {
             if (this.inv[var3] != null)
             {
                 NBTTagCompound var4 = new NBTTagCompound();
                 var4.setByte("Slot", (byte)var3);
                 this.inv[var3].writeToNBT(var4);
                 var2.appendTag(var4);
             }
         }
 
         par1NBTTagCompound.setTag("Items", var2);
     }
 	
 	public boolean insertItemInSlot(ItemStack stack, int slot) {
 		if(slot < inv.length) {
 			ItemStack currStack = this.getStackInSlot(slot);
 			if(stack == null) {return false;}
 			if(currStack == null) {
 				setInventorySlotContents(slot, stack);
 				return true;
 			}
 			if(currStack.stackSize == 0){currStack = null; insertItemInSlot(stack, slot);}
 			if(currStack.getItem().itemID == stack.getItem().itemID && currStack.getItemDamage() == stack.getItemDamage()) {
 				currStack.stackSize += stack.stackSize;
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public void openChest() {}
 
 	@Override
 	public void closeChest() {}
 }
