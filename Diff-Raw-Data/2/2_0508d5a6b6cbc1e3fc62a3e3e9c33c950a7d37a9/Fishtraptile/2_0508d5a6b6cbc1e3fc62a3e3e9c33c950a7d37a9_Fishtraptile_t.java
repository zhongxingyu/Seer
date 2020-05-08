 package industrialscience.modules.fishing;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.ListIterator;
 import java.util.Set;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.tileentity.TileEntity;
 
 public class Fishtraptile extends TileEntity implements IInventory {
    static final int UPDATESLOT = 0;
     static final int FISHSLOT = 1;
     private ItemStack[] Inventory;
     private String name = "Basic Fishtrap";
 
     public Fishtraptile() {
         Inventory = new ItemStack[2];
     }
 
     @Override
     public int getSizeInventory() {
         return Inventory.length;
     }
 
     @Override
     public ItemStack getStackInSlot(int i) {
         return Inventory[i];
     }
 
     @Override
     public ItemStack decrStackSize(int i, int j) {
         ItemStack stack = getStackInSlot(i);
 
         if (stack != null) {
 
             if (stack.stackSize <= j) {
                 setInventorySlotContents(i, null);
             } else {
                 stack = stack.splitStack(j);
                 if (stack.stackSize == 0) {
                     setInventorySlotContents(i, null);
                 }
             }
         }
 
         return stack;
     }
 
     @Override
     public ItemStack getStackInSlotOnClosing(int i) {
         ItemStack stack = getStackInSlot(i);
 
         if (stack != null) {
             setInventorySlotContents(i, null);
         }
 
         return stack;
     }
 
     @Override
     public void setInventorySlotContents(int i, ItemStack itemstack) {
         Inventory[i] = itemstack;
         if (itemstack != null && itemstack.stackSize > getInventoryStackLimit()) {
             itemstack.stackSize = getInventoryStackLimit();
         }
 
     }
 
     @Override
     public String getInvName() {
         return name;
     }
 
     @Override
     public int getInventoryStackLimit() {
         return 64;
     }
 
     @Override
     public boolean isUseableByPlayer(EntityPlayer entityplayer) {
         return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this
                 && entityplayer.getDistanceSq(xCoord + 0.5, yCoord + 0.5,
                         zCoord + 0.5) < 64;
     }
 
     @Override
     public void openChest() {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void closeChest() {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void readFromNBT(NBTTagCompound tagCompound) {
         super.readFromNBT(tagCompound);
 
         NBTTagList tagList = tagCompound.getTagList("Inventory");
 
         for (int i = 0; i < tagList.tagCount(); i++) {
             NBTTagCompound tag = (NBTTagCompound) tagList.tagAt(i);
 
             byte slot = tag.getByte("Slot");
 
             if (slot >= 0 && slot < Inventory.length) {
                 Inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
             }
         }
     }
 
     @Override
     public void writeToNBT(NBTTagCompound tagCompound) {
         super.writeToNBT(tagCompound);
 
         NBTTagList itemList = new NBTTagList();
 
         for (int i = 0; i < Inventory.length; i++) {
             ItemStack stack = Inventory[i];
 
             if (stack != null) {
                 NBTTagCompound tag = new NBTTagCompound();
 
                 tag.setByte("Slot", (byte) i);
                 stack.writeToNBT(tag);
                 itemList.appendTag(tag);
             }
         }
 
         tagCompound.setTag("Inventory", itemList);
     }
 
     public void addFish(int fishamout, int neededwater, int range,
             int waterforextrafish) {
         int waterblocks = countWater(range);
         int fishes = fishamout;
         if (waterblocks >= neededwater) {
             waterblocks = waterblocks - neededwater;
             if (waterblocks > 0) {
 
             }
             ItemStack stack = null;
             if (Inventory[FISHSLOT] != null) {
                 stack = Inventory[FISHSLOT];
                 stack.stackSize = stack.stackSize + fishes;
             } else {
                 stack = new ItemStack(Item.fishRaw, fishes);
             }
             Inventory[FISHSLOT] = stack;
         }
     }
 
     protected class Cordinate {
         int x;
         int y;
         int z;
 
         Cordinate(int x, int y, int z) {
             this.x = x;
             this.y = y;
             this.z = z;
         }
 
         @Override
         public boolean equals(Object e) {
             if (!(e instanceof Cordinate))
                 return false;
             Cordinate c = (Cordinate) e;
             if (x != c.getX() || y != c.getY() || z != c.getZ())
                 return false;
             return true;
         }
 
         public int getX() {
             return x;
         }
 
         public int getY() {
             return y;
         }
 
         public int getZ() {
             return z;
         }
 
     }
 
     protected class Coordinateset implements Set<Cordinate> {
         private ArrayList<Cordinate> list = new ArrayList<Fishtraptile.Cordinate>();
 
         @Override
         public boolean add(Cordinate e) {
             if (!isAlredayInList(e))
                 return list.add(e);
             return false;
         }
 
         private boolean isAlredayInList(Cordinate e) {
             for (Iterator<Cordinate> iterator = list.iterator(); iterator
                     .hasNext();) {
                 Cordinate cordinate = iterator.next();
                 if (cordinate.equals(e))
                     return true;
             }
             return false;
         }
 
         @Override
         public boolean addAll(Collection<? extends Cordinate> c) {
             return list.addAll(c);
         }
 
         @Override
         public void clear() {
             list.clear();
         }
 
         @Override
         public Object clone() {
             return list.clone();
         }
 
         @Override
         public boolean contains(Object o) {
             return list.contains(o);
         }
 
         @Override
         public boolean containsAll(Collection<?> arg0) {
             return list.containsAll(arg0);
         }
 
         public void ensureCapacity(int minCapacity) {
             list.ensureCapacity(minCapacity);
         }
 
         @Override
         public int hashCode() {
             return list.hashCode();
         }
 
         public int indexOf(Object o) {
             return list.indexOf(o);
         }
 
         @Override
         public boolean isEmpty() {
             return list.isEmpty();
         }
 
         @Override
         public Iterator<Cordinate> iterator() {
             return list.iterator();
         }
 
         public int lastIndexOf(Object o) {
             return list.lastIndexOf(o);
         }
 
         public ListIterator<Cordinate> listIterator() {
             return list.listIterator();
         }
 
         public ListIterator<Cordinate> listIterator(int arg0) {
             return list.listIterator(arg0);
         }
 
         public Cordinate remove(int index) {
             return list.remove(index);
         }
 
         @Override
         public boolean remove(Object o) {
             return list.remove(o);
         }
 
         @Override
         public boolean removeAll(Collection<?> arg0) {
             return list.removeAll(arg0);
         }
 
         @Override
         public boolean retainAll(Collection<?> arg0) {
             return list.retainAll(arg0);
         }
 
         @Override
         public int size() {
             return list.size();
         }
 
         @Override
         public Object[] toArray() {
             return list.toArray();
         }
 
         @Override
         public <T> T[] toArray(T[] a) {
             return list.toArray(a);
         }
 
         @Override
         public String toString() {
             return list.toString();
         }
 
         public void trimToSize() {
             list.trimToSize();
         }
     }
 
     protected int countWater(int range) {
         Set<Cordinate> waterblocks = new Coordinateset();
         for (int i = 1; i < range + 1; i++) {
             if (worldObj.getBlockId(xCoord + i, yCoord, zCoord) == 9) {
                 waterblocks.add(new Cordinate(xCoord + i, yCoord, zCoord));
                 if (worldObj.getBlockId(xCoord + i, yCoord + 1, zCoord) == 9) {
                     waterblocks.add(new Cordinate(xCoord + i, yCoord + 1,
                             zCoord));
                 }
                 if (worldObj.getBlockId(xCoord + i, yCoord - 1, zCoord) == 9) {
                     waterblocks.add(new Cordinate(xCoord + i, yCoord - 1,
                             zCoord));
                 }
                 if (worldObj.getBlockId(xCoord + i, yCoord, zCoord - 1) == 9) {
                     waterblocks.add(new Cordinate(xCoord + i, yCoord,
                             zCoord - 1));
                 }
                 if (worldObj.getBlockId(xCoord + i, yCoord, zCoord + 1) == 9) {
                     waterblocks.add(new Cordinate(xCoord + i, yCoord,
                             zCoord + 1));
                 }
 
             } else {
                 break;
             }
 
         }
         for (int i = 1; i < range + 1; i++) {
             if (worldObj.getBlockId(xCoord, yCoord + i, zCoord) == 9) {
                 waterblocks.add(new Cordinate(xCoord, yCoord + i, zCoord));
                 if (worldObj.getBlockId(xCoord + 1, yCoord + i, zCoord) == 9) {
                     waterblocks.add(new Cordinate(xCoord + 1, yCoord + i,
                             zCoord));
                 }
                 if (worldObj.getBlockId(xCoord - 1, yCoord + i, zCoord) == 9) {
                     waterblocks.add(new Cordinate(xCoord - 1, yCoord + i,
                             zCoord));
                 }
                 if (worldObj.getBlockId(xCoord, yCoord + i, zCoord - 1) == 9) {
                     waterblocks.add(new Cordinate(xCoord, yCoord + i,
                             zCoord - 1));
                 }
                 if (worldObj.getBlockId(xCoord, yCoord + i, zCoord + 1) == 9) {
                     waterblocks.add(new Cordinate(xCoord, yCoord + i,
                             zCoord + 1));
                 }
 
             } else {
                 break;
             }
 
         }
         for (int i = 1; i < range + 1; i++) {
             if (worldObj.getBlockId(xCoord - i, yCoord, zCoord) == 9) {
                 waterblocks.add(new Cordinate(xCoord - i, yCoord, zCoord));
                 if (worldObj.getBlockId(xCoord - i, yCoord + 1, zCoord) == 9) {
                     waterblocks.add(new Cordinate(xCoord - i, yCoord + 1,
                             zCoord));
                 }
                 if (worldObj.getBlockId(xCoord - i, yCoord - 1, zCoord) == 9) {
                     waterblocks.add(new Cordinate(xCoord - i, yCoord - 1,
                             zCoord));
                 }
                 if (worldObj.getBlockId(xCoord - i, yCoord, zCoord - 1) == 9) {
                     waterblocks.add(new Cordinate(xCoord - i, yCoord,
                             zCoord - 1));
                 }
                 if (worldObj.getBlockId(xCoord - i, yCoord, zCoord + 1) == 9) {
                     waterblocks.add(new Cordinate(xCoord - i, yCoord,
                             zCoord + 1));
                 }
 
             } else {
                 break;
             }
 
         }
         for (int i = 1; i < range + 1; i++) {
             if (worldObj.getBlockId(xCoord, yCoord - i, zCoord) == 9) {
                 waterblocks.add(new Cordinate(xCoord, yCoord - i, zCoord));
                 if (worldObj.getBlockId(xCoord + 1, yCoord - i, zCoord) == 9) {
                     waterblocks.add(new Cordinate(xCoord + 1, yCoord - i,
                             zCoord));
                 }
                 if (worldObj.getBlockId(xCoord - 1, yCoord - i, zCoord) == 9) {
                     waterblocks.add(new Cordinate(xCoord - 1, yCoord - i,
                             zCoord));
                 }
                 if (worldObj.getBlockId(xCoord, yCoord - i, zCoord - 1) == 9) {
                     waterblocks.add(new Cordinate(xCoord, yCoord - i, zCoord));
                 }
                 if (worldObj.getBlockId(xCoord, yCoord - i, zCoord + 1) == 9) {
                     waterblocks.add(new Cordinate(xCoord, yCoord - i,
                             zCoord + 1));
                 }
 
             } else {
                 break;
             }
 
         }
         for (int i = 1; i < range + 1; i++) {
             if (worldObj.getBlockId(xCoord, yCoord, zCoord - i) == 9) {
                 waterblocks.add(new Cordinate(xCoord, yCoord, zCoord - i));
                 if (worldObj.getBlockId(xCoord + 1, yCoord, zCoord - i) == 9) {
                     waterblocks.add(new Cordinate(xCoord + 1, yCoord, zCoord
                             - i));
                 }
                 if (worldObj.getBlockId(xCoord - 1, yCoord, zCoord - i) == 9) {
                     waterblocks.add(new Cordinate(xCoord - 1, yCoord, zCoord
                             - i));
                 }
                 if (worldObj.getBlockId(xCoord, yCoord + 1, zCoord - i) == 9) {
                     waterblocks.add(new Cordinate(xCoord, yCoord + 1, zCoord
                             - i));
                 }
                 if (worldObj.getBlockId(xCoord, yCoord - 1, zCoord - i) == 9) {
                     waterblocks.add(new Cordinate(xCoord, yCoord - 1, zCoord
                             - i));
                 }
 
             } else {
                 break;
             }
 
         }
         for (int i = 1; i < range + 1; i++) {
             if (worldObj.getBlockId(xCoord, yCoord, zCoord + i) == 9) {
                 waterblocks.add(new Cordinate(xCoord, yCoord, zCoord + i));
                 if (worldObj.getBlockId(xCoord + 1, yCoord, zCoord + i) == 9) {
                     waterblocks.add(new Cordinate(xCoord + 1, yCoord, zCoord
                             + i));
                 }
                 if (worldObj.getBlockId(xCoord - 1, yCoord, zCoord + i) == 9) {
                     waterblocks.add(new Cordinate(xCoord - 1, yCoord, zCoord
                             + i));
                 }
                 if (worldObj.getBlockId(xCoord, yCoord - 1, zCoord + i) == 9) {
                     waterblocks.add(new Cordinate(xCoord, yCoord - 1, zCoord
                             + i));
                 }
                 if (worldObj.getBlockId(xCoord, yCoord + 1, zCoord + i) == 9) {
                     waterblocks.add(new Cordinate(xCoord, yCoord + 1, zCoord
                             + i));
                 }
 
             } else {
                 break;
             }
 
         }
         return waterblocks.size();
 
     }
 
     @Override
     public boolean isInvNameLocalized() {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public boolean isStackValidForSlot(int i, ItemStack itemstack) {
         // TODO Auto-generated method stub
         return false;
     }
 
 }
