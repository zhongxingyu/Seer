 package tk.allele.inventory;
 
 import org.bukkit.Material;
 import org.bukkit.block.Chest;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 import java.util.Arrays;
 import java.util.HashMap;
 
 /**
  * Implements an abstraction to double chests.
  */
 public class DoubleChestInventory implements Inventory {
     Inventory inventoryA, inventoryB;
 
     public DoubleChestInventory(Inventory inventoryA, Inventory inventoryB) {
         this.inventoryA = inventoryA;
         this.inventoryB = inventoryB;
     }
 
     public DoubleChestInventory(Chest chestA, Chest chestB) {
         this(chestA.getInventory(), chestB.getInventory());
     }
 
     @Override
     public int getSize() {
         return (inventoryA.getSize() + inventoryB.getSize());
     }
 
     @Override
     public String getName() {
         return (inventoryA.getName() + "+" + inventoryB.getName());
     }
 
     @Override
     public ItemStack getItem(int i) {
         if(i < inventoryA.getSize()) {
             return inventoryA.getItem(i);
         } else {
             return inventoryB.getItem(i - inventoryA.getSize());
         }
     }
 
     @Override
     public void setItem(int i, ItemStack itemStack) {
         if(i < inventoryA.getSize()) {
             inventoryA.setItem(i, itemStack);
         } else {
            inventoryB.setItem(i, itemStack);
         }
     }
 
     @Override
     public HashMap<Integer, ItemStack> addItem(ItemStack... itemStacks) {
         HashMap<Integer, ItemStack> leftoverMap = inventoryA.addItem(itemStacks);
         if(leftoverMap.isEmpty()) {
             return leftoverMap;
         } else {
             ItemStack[] leftover = leftoverMap.values().toArray(new ItemStack[itemStacks.length]);
             return inventoryB.addItem(leftover);
         }
     }
 
     @Override
     public HashMap<Integer, ItemStack> removeItem(ItemStack... itemStacks) {
         HashMap<Integer, ItemStack> leftoverMap = inventoryA.removeItem(itemStacks);
         if(leftoverMap.isEmpty()) {
             return leftoverMap;
         } else {
             ItemStack[] leftover = leftoverMap.values().toArray(new ItemStack[itemStacks.length]);
             return inventoryB.removeItem(leftover);
         }
     }
 
     @Override
     public ItemStack[] getContents() {
         throw new UnsupportedOperationException("Cannot get contents of a double chest");
     }
 
     @Override
     public void setContents(ItemStack[] itemStacks) {
         if(itemStacks.length != getSize()) {
             throw new IllegalArgumentException("Invalid inventory size; expected " + getSize());
         } else {
             ItemStack[] arrayA = Arrays.copyOf(itemStacks, inventoryA.getSize());
             ItemStack[] arrayB = Arrays.copyOfRange(itemStacks, inventoryA.getSize(), inventoryA.getSize() + inventoryB.getSize());
             inventoryA.setContents(arrayA);
             inventoryB.setContents(arrayB);
         }
     }
 
     @Override
     public boolean contains(int materialId) {
         return inventoryA.contains(materialId) || inventoryB.contains(materialId);
     }
 
     @Override
     public boolean contains(Material material) {
         return inventoryA.contains(material) || inventoryB.contains(material);
     }
 
     @Override
     public boolean contains(ItemStack itemStack) {
         return inventoryA.contains(itemStack) || inventoryB.contains(itemStack);
     }
 
     @Override
     public boolean contains(int materialId, int amount) {
         throw new UnsupportedOperationException("Not implemented");
     }
 
     @Override
     public boolean contains(Material material, int amount) {
         throw new UnsupportedOperationException("Not implemented");
     }
 
     @Override
     public boolean contains(ItemStack itemStack, int amount) {
         throw new UnsupportedOperationException("Not implemented");
     }
 
     @Override
     public HashMap<Integer, ? extends ItemStack> all(int materialId) {
         throw new UnsupportedOperationException("Not implemented");
     }
 
     @Override
     public HashMap<Integer, ? extends ItemStack> all(Material material) {
         throw new UnsupportedOperationException("Not implemented");
     }
 
     @Override
     public HashMap<Integer, ? extends ItemStack> all(ItemStack itemStack) {
         throw new UnsupportedOperationException("Not implemented");
     }
 
     @Override
     public int first(int materialId) {
         // Search the first inventory
         int index = inventoryA.first(materialId);
         if(index != -1) {
             return index;
         } else {
             // If it's not in the first, try the second
             index = inventoryB.first(materialId);
             if(index != -1) {
                 return getSize() + index;
             } else {
                 return index;
             }
         }
     }
 
     @Override
     public int first(Material material) {
         // Search the first inventory
         int index = inventoryA.first(material);
         if(index != -1) {
             return index;
         } else {
             // If it's not in the first, try the second
             index = inventoryB.first(material);
             if(index != -1) {
                 return getSize() + index;
             } else {
                 return index;
             }
         }
     }
 
     @Override
     public int first(ItemStack itemStack) {
         // Search the first inventory
         int index = inventoryA.first(itemStack);
         if(index != -1) {
             return index;
         } else {
             // If it's not in the first, try the second
             index = inventoryB.first(itemStack);
             if(index != -1) {
                 return getSize() + index;
             } else {
                 return index;
             }
         }
     }
 
     @Override
     public int firstEmpty() {
         int index = inventoryA.firstEmpty();
         if(index != -1) {
             return index;
         } else {
             index = inventoryB.firstEmpty();
             if(index != -1) {
                 return getSize() + index;
             } else {
                 return index;
             }
         }
     }
 
     @Override
     public void remove(int materialId) {
         inventoryA.remove(materialId);
         inventoryB.remove(materialId);
     }
 
     @Override
     public void remove(Material material) {
         inventoryA.remove(material);
         inventoryB.remove(material);
     }
 
     @Override
     public void remove(ItemStack itemStack) {
         inventoryA.remove(itemStack);
         inventoryB.remove(itemStack);
     }
 
     @Override
     public void clear(int index) {
         if(index < inventoryA.getSize()) {
             inventoryA.clear(index);
         } else {
             inventoryB.clear(index - inventoryA.getSize());
         }
     }
 
     @Override
     public void clear() {
         inventoryA.clear();
         inventoryB.clear();
     }
 }
