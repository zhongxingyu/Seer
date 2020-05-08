 package com.koletar.jj.chestkeeper;
 
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author jjkoletar
  */
 public class CKChest implements ConfigurationSerializable {
     private static final int SMALL_CHEST_SIZE = 27;
     private static final int LARGE_CHEST_SIZE = 54;
     private ItemStack[] contents;
     private Inventory inventory;
     private boolean modified;
     private String title;
 
     public CKChest(String title, boolean isLargeChest) {
         contents = new ItemStack[isLargeChest ? LARGE_CHEST_SIZE : SMALL_CHEST_SIZE];
         this.title = title;
     }
 
     public CKChest(Map<String, Object> me) {
         if (me.size() - 2 != SMALL_CHEST_SIZE && me.size() - 2 != LARGE_CHEST_SIZE) { //Minus two offsets for the == and _title
             throw new IllegalArgumentException("Size of item list is not the size of a large or small chest");
         }
         contents = new ItemStack[me.size() - 2];
         for (Map.Entry<String, Object> entry : me.entrySet()) {
             if (entry.getKey().equalsIgnoreCase("_title")) {
                 title = entry.getValue().toString();
                 continue;
             } else if (entry.getKey().equalsIgnoreCase("==")) {
                 continue;
             }
             int i = -1;
             try {
                 i = Integer.valueOf(entry.getKey());
             } catch (NumberFormatException nfe) {
                 throw new IllegalArgumentException("A key wasn't an integer, " + entry.getKey());
             }
             ItemStack is;
             try {
                 is = (ItemStack) entry.getValue();
             } catch (ClassCastException cce) {
                 throw new IllegalArgumentException("A value wasn't an itemstack");
             }
             try {
                 contents[i] = is;
             } catch (ArrayIndexOutOfBoundsException aioobe) {
                 throw new IllegalArgumentException("A key was out of bounds with the array");
             }
         }
     }
 
     public Map<String, Object> serialize() {
         Map<String, Object> me = new HashMap<String, Object>();
         for (int i = 0; i < contents.length; i++) {
             me.put(String.valueOf(i), contents[i]);
         }
         me.put("_title", title);
         return me;
     }
 
     public Inventory getInventory(int magic) {
         if (inventory == null) {
             String invTitle = title + makeMagic(magic);
             if (invTitle.length() > 32) {
                 invTitle = invTitle.substring(0, 32);
                 if (invTitle.endsWith("\u00A7")) {
                     invTitle = invTitle.substring(0, invTitle.length() - 1);
                 }
             }
             inventory = Bukkit.createInventory(null, contents.length, invTitle);
             ChestKeeper.trace("Title is: " + title + makeMagic(magic));
         }
         if (modified) {
             return inventory;
         }
         inventory.setContents(contents);
         modified = true;
         return inventory;
     }
 
     public boolean save() {
         if (inventory == null || inventory.getViewers().size() > 1) {
             return false;
         }
         if (!modified) {
             return true;
         }
         contents = inventory.getContents();
         modified = false;
         return true;
     }
 
     public boolean isModified() {
         return modified;
     }
 
     public void kick() {
         if (inventory != null) {
             for (HumanEntity he : inventory.getViewers()) {
                 he.closeInventory();
             }
         }
     }
 
     public void empty() {
         if (inventory != null) {
             if (modified) {
                 inventory.clear();
             } else {
                 contents = new ItemStack[contents.length];
                 modified = true;
             }
         }
     }
 
     private static String makeMagic(int magic) {
         StringBuilder sb = new StringBuilder();
         char[] digits = String.valueOf(magic).toCharArray();
         for (int i = 0; i < digits.length; i++) {
             sb.append("\u00A7");
             if (digits[i] == '-') {
                 sb.append('f');
             } else {
                 sb.append(digits[i]);
             }
         }
         return sb.toString();
     }
 
     public void setName(String name) {
         this.title = name;
         kick();
         save();
         inventory = null;
     }
 
     public boolean isLargeChest() {
         return contents.length == LARGE_CHEST_SIZE;
     }
 
     public boolean upgrade() {
         if (contents.length == LARGE_CHEST_SIZE) {
             return false;
         }
         kick();
         save();
         ItemStack[] newContents = new ItemStack[LARGE_CHEST_SIZE];
         for (int i = 0; i < contents.length; i++) {
            newContents[i] = contents[i] == null ? null : contents[i].clone();
         }
         contents = newContents;
         inventory = null;
         return true;
     }
 
     public String getTitle() {
         return title;
     }
 
     protected void setItems(ItemStack[] in) {
         contents = new ItemStack[contents.length];
         for (int i = 0; i < in.length && i < contents.length; i++) {
             contents[i] = in[i];
         }
     }
 }
