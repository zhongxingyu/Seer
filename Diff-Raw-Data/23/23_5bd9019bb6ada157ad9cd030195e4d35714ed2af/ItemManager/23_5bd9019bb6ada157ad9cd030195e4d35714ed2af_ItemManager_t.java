 package de.minestar.buycraft.manager;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 
 import org.bukkit.Material;
 import org.bukkit.block.Chest;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 import de.minestar.buycraft.core.Core;
 import de.minestar.buycraft.shops.ShopType;
 import de.minestar.minestarlibrary.utils.ConsoleUtils;
 
 public class ItemManager {
     /**
      * Singleton
      */
     private static ItemManager itemManager = null;
 
     /**
      * private vars
      */
     private HashMap<Integer, String> itemListByID;
     private HashMap<String, Integer> itemListByName;
 
     /**
      * Constructor
      */
     public ItemManager() {
         if (ItemManager.itemManager == null) {
             itemListByID = new HashMap<Integer, String>();
             itemListByName = new HashMap<String, Integer>();
             this.loadItems();
             ItemManager.itemManager = this;
         }
     }
 
     /**
      * Load the items from a TXT-File
      */
     public void loadItems() {
         try {
             BufferedReader in = new BufferedReader(new FileReader("plugins/BuyCraft/items.txt"));
             String zeile = null;
             while ((zeile = in.readLine()) != null) {
                 String[] split = zeile.split(",");
                 if (split.length > 1) {
                     try {
                         this.itemListByID.put(Integer.valueOf(split[1]), split[0].toLowerCase());
                         this.itemListByName.put(split[0].toLowerCase(), Integer.valueOf(split[1]));
                     } catch (Exception e) {
                         ConsoleUtils.printError(Core.NAME, "Cannot parse: " + zeile);
                     }
                 }
             }
            in.close();
         } catch (IOException e) {
             e.printStackTrace();
             ConsoleUtils.printError(Core.NAME, "Fehler beim lesen der Datei: plugins/BuyCraft/items.txt");
         }
     }
 
     /**
      * Is an item allowed?
      * 
      * @param itemName
      * @return
      */
     public boolean isItemAllowed(String itemName) {
         return this.isItemIDAllowed(this.getItemId(itemName));
     }
 
     /**
      * Is an item allowed?
      * 
      * @param TypeID
      * @return
      */
     public boolean isItemIDAllowed(int TypeID) {
         return this.itemListByID.containsKey(TypeID);
     }
 
     /**
      * Get the ItemID for a given ItemName
      * 
      * @param itemName
      * @return
      */
     public int getItemId(String itemName) {
         itemName = itemName.toLowerCase();
         try {
             if (this.itemListByID.containsKey(Integer.valueOf(itemName))) {
                 return Integer.valueOf(itemName);
             } else {
                 return -1;
             }
         } catch (Exception e) {
             if (this.itemListByName.containsKey(itemName)) {
                 return this.itemListByName.get(itemName);
             } else {
                 return -1;
             }
         }
     }
 
     /**
      * Get the ItemName for an given TypeID
      * 
      * @param TypeID
      * @return
      */
     public String getItemName(int TypeID) {
         if (!this.isItemIDAllowed(TypeID))
             return "NOT ALLOWED";
         return this.itemListByID.get(TypeID);
     }
 
     // /////////////////////////////////////////////////////////////
     //
     // STATIC METHODS
     //
     // /////////////////////////////////////////////////////////////
 
     /**
      * Get/Create the instance
      * 
      * @return the instance
      */
     public static ItemManager getInstance() {
         if (ItemManager.itemManager != null) {
             return ItemManager.itemManager;
         }
         new ItemManager();
         return ItemManager.itemManager;
     }
 
     /**
      * Get the ratios for a given line
      * 
      * @param line
      * @return
      */
     public static int[] getRatio(String line) {
         int[] ratios = new int[2];
         ratios[0] = 0;
         ratios[1] = 0;
         String[] split = line.split(":");
         if (split.length == 2) {
             try {
                 ratios[0] = Integer.valueOf(split[0]);
                 ratios[1] = Integer.valueOf(split[1]);
             } catch (Exception e) {
                 ratios[0] = 0;
                 ratios[1] = 0;
             }
         }
         return ratios;
     }
 
     /**
      * Extract the line. This will replace { & }.
      * 
      * @param line
      * @return
      */
     public static String extractItemLine(String line) {
         return line.replace("{", "").replace("}", "");
     }
 
     public static String getNameForID(int TypeID) {
         Material material = Material.getMaterial(TypeID);
         if (material != null) {
             return material.name();
         }
         return "NOT DEFINED";
     }
 
     public static boolean isChestEmtpy(ShopType shop) {
         return ItemManager.isChestEmtpy(shop.getChest());
     }
 
     public static boolean isChestEmtpy(Chest chest) {
         Inventory inventory = chest.getInventory();
         for (int i = 0; i < inventory.getSize(); i++) {
             if (inventory.getItem(i) == null || inventory.getItem(i).getTypeId() == Material.AIR.getId())
                 continue;
             return true;
         }
         return true;
     }
 
     public static int countItemInInventory(ShopType shop, int itemID) {
         return ItemManager.countItemInInventory(shop.getChest(), itemID, (short) 0);
     }
 
     public static int countItemInInventory(ShopType shop, int itemID, short itemData) {
         return ItemManager.countItemInInventory(shop.getChest(), itemID, itemData);
     }
 
     public static int countItemInInventory(Chest chest, int itemID) {
         return ItemManager.countItemInInventory(chest, itemID, (short) 0);
     }
 
     public static int countItemInInventory(Chest chest, int itemID, short itemData) {
         int count = 0;
         Inventory inventory = chest.getInventory();
         ItemStack item;
         for (int i = 0; i < inventory.getSize(); i++) {
             item = inventory.getItem(i);
             if (item == null || item.getTypeId() != itemID || item.getEnchantments().size() > 0)
                 continue;
             if (item.getDurability() == itemData) {
                 count += item.getAmount();
             }
         }
         return count;
     }
     public static int countAllItemsInInventory(ShopType shop) {
         return ItemManager.countAllItemsInInventory(shop.getChest());
     }
 
     public static int countAllItemsInInventory(Chest chest) {
         int count = 0;
         Inventory inventory = chest.getInventory();
         ItemStack item;
         for (int i = 0; i < inventory.getSize(); i++) {
             item = inventory.getItem(i);
             if (item == null || item.getTypeId() == Material.AIR.getId())
                 continue;
             count += item.getAmount();
         }
         return count;
     }
 }
