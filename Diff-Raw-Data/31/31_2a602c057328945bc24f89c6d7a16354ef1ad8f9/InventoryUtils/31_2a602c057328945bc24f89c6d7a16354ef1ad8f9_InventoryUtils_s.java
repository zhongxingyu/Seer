 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ru.vgTrade.Util;
 
 import org.bukkit.Material;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
 * @author pasha
  */
 public class InventoryUtils {
     static JavaPlugin plugin = null;
     
     static final public Integer[] LEFT_SLOTS = {0,1,2,3,9,10,11,12,18,19,20,21,27,28,29};
             //{0,1,2,9,10,11,12,18,19,20,21,27,28,29,30};
     static final public Integer[] RIGHT_SLOTS = {5,6,7,8,14,15,16,17,23,24,25,26,33,34,35};
             //{6,7,8,14,15,16,17,23,24,25,26,32,33,34,35};
     static final public Integer[] BUTTON_SLOTS = {4,13,22,30,31,32};
     static final public Integer CONFIRM_SLOT = 4;
     static final public Integer DECLINE_SLOT = 22;
     static final public Integer P1_CONFIRM_SLOT = 30;
     static final public Integer P2_CONFIRM_SLOT = 32;
     
     public static void setPlugin(JavaPlugin pl) {
         plugin = pl;
     }
     
     public static Inventory createInventory(String id) {
         Inventory inv = null;
         
         if (plugin != null) {
             inv = plugin.getServer().createInventory(null, 36, id);
             for (int i=0; i<BUTTON_SLOTS.length; i++) {
                 inv.setItem(BUTTON_SLOTS[i], new ItemStack(280));
             }
             inv.setItem(CONFIRM_SLOT, new ItemStack(Material.WOOL, 1, (short)1, (byte)5));
             inv.setItem(DECLINE_SLOT, new ItemStack(Material.WOOL, 1, (short)1, (byte)14));
             inv.setItem(P1_CONFIRM_SLOT, new ItemStack(Material.WOOL, 1, (short)1, (byte)15));
             inv.setItem(P2_CONFIRM_SLOT, new ItemStack(Material.WOOL, 1, (short)1, (byte)15));
         }
         
         return inv;
     }
     
     public static ItemStack[] getLeftContents(Inventory inv) {
         ItemStack[] items = null;
         
         if (inv == null) {
             Log.severe("getLeftContents: inventory is null");
             return items;
         }
         
         int count = 0;
         ItemStack[] ist = inv.getContents();
         if (ist == null) {
             return items;
         }
         
         for (int i=0; i<LEFT_SLOTS.length; i++) {
             ItemStack is = ist[LEFT_SLOTS[i]];
             if (is != null && is.getType() != Material.AIR)
                 count ++;
         }
         
         if (count > 0) {
             items = new ItemStack[count];
             int num = 0;
         
             for (int i=0; i<LEFT_SLOTS.length; i++) {
                 ItemStack is = ist[LEFT_SLOTS[i]];
                 if (is != null && is.getType() != Material.AIR)
                 {
                     items[num++] = is;
                 }
             }
         }
         
         return items;
     }
     
     public static ItemStack[] getRightContents(Inventory inv) {
         ItemStack[] items = null;
         
         if (inv == null) {
             Log.severe("getRightContents: inventory is null");
             return items;
         }
         
         int count = 0;
         ItemStack[] ist = inv.getContents();
         if (ist == null) {
             return items;
         }
         
         for (int i=0; i<RIGHT_SLOTS.length; i++) {
             ItemStack is = ist[RIGHT_SLOTS[i]];
             if (is != null && is.getType() != Material.AIR)
                 count ++;
         }
         
         if (count > 0) {
             items = new ItemStack[count];
             int num = 0;
         
             for (int i=0; i<RIGHT_SLOTS.length; i++) {
                 ItemStack is = ist[RIGHT_SLOTS[i]];
                 if (is != null && is.getType() != Material.AIR)
                 {
                     items[num++] = is;
                 }
             }
         }
         
         return items;
     }
 }
