 package com.koletar.jj.chestkeeper;
 
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 /**
  * @author jjkoletar
  */
 public class CKUser implements ConfigurationSerializable {
     private String username;
     private String defaultChest;
     private SortedMap<String, CKChest> chests;
     private HashMap<String, CKChest> inventoryPairings;
     private int magic;
 
     public CKUser(String username) {
         this.username = username;
         chests = new TreeMap<String, CKChest>();
         inventoryPairings = new HashMap<String, CKChest>();
         magic = username.hashCode();
     }
 
     public CKUser(Map<String, Object> me) {
         chests = new TreeMap<String, CKChest>();
         inventoryPairings = new HashMap<String, CKChest>();
         for (Map.Entry<String, Object> entry : me.entrySet()) {
             if (entry.getKey().equals("defaultChest")) {
                 defaultChest = entry.getValue() == null ? "" : entry.getValue().toString();
             } else if (entry.getKey().equals("username")) {
                 username = entry.getValue().toString();
             } else if (entry.getKey().equals("magic")) {
                 magic = Integer.valueOf(entry.getValue().toString());
             } else if (entry.getValue() instanceof CKChest) {
                 chests.put(entry.getKey(), (CKChest) entry.getValue());
             }
         }
     }
 
     public Map<String, Object> serialize() {
         Map<String, Object> me = new HashMap<String, Object>();
         me.put("defaultChest", defaultChest);
         me.put("username", username);
         me.put("magic", magic);
         for (Map.Entry<String, CKChest> entry : chests.entrySet()) {
             me.put(entry.getKey(), entry.getValue());
         }
         return me;
     }
 
     public boolean equals(Object o) {
         return o instanceof CKUser && ((CKUser) o).username.equals(username);
     }
 
     public int hashCode() {
         return username.hashCode() + 1;
     }
 
     public boolean createChest(String name, boolean isLargeChest) {
         if (name.equalsIgnoreCase("defaultChest") || name.equalsIgnoreCase("username") || name.equalsIgnoreCase("magic") || chests.containsKey(name.toLowerCase())) {
             return false;
         }
         chests.put(name.toLowerCase(), new CKChest(name, isLargeChest));
         return true;
     }
 
     public CKChest getChest() {
         String key = (defaultChest == null || defaultChest.equals("")) && chests.size() > 0 ? chests.firstKey() : defaultChest;
         return chests.get(key);
     }
 
     public CKChest getChest(String name) {
         return chests.get(name.toLowerCase());
     }
 
     public Inventory openChest() {
         String key = (defaultChest == null || defaultChest.equals("")) && chests.size() > 0 ? chests.firstKey() : defaultChest;
         return openChest(key);
     }
 
     public Inventory openChest(String name) {
         String lowerName = name.toLowerCase();
         CKChest chest = chests.get(lowerName);
         if (chest == null) {
             return null;
         }
         Inventory inventory = chest.getInventory(magic);
         inventoryPairings.put(inventory.getTitle(), chest);
         return inventory;
     }
 
     public boolean save(Inventory inventory) {
         if (inventoryPairings.containsKey(inventory.getTitle())) {
             return inventoryPairings.get(inventory.getTitle()).save();
         }
         return false;
     }
 
     public void forceClean() {
         for (CKChest chest : inventoryPairings.values()) {
             chest.kick();
             chest.save();
         }
     }
 
     public String getUsername() {
         return username;
     }
 
     public int getNumberOfChests() {
         return chests.size();
     }
 
     public Set<String> getChestNames() {
         return chests.keySet();
     }
 
     public boolean removeChest(String name) {
         if (chests.containsKey(name.toLowerCase())) {
             chests.remove(name.toLowerCase());
             return true;
         }
         return false;
     }
 
     public boolean setDefaultChest(String name) {
         if (name == null) return true;
         if (chests.containsKey(name.toLowerCase())) {
             defaultChest = name.toLowerCase();
             return true;
         }
         return false;
     }
 
     public boolean hasChest(String name) {
         return chests.containsKey(name.toLowerCase());
     }
 
     public void mv(String from, String to) {
         CKChest chest = chests.get(from.toLowerCase());
         chest.setName(to);
         chests.remove(from.toLowerCase());
         chests.put(to.toLowerCase(), chest);
         if (defaultChest.equalsIgnoreCase(from)) {
             defaultChest = to.toLowerCase();
         }
     }
 
     public void fromVC(BufferedReader chestYml, String defaultChest) {
         String currentChest = null;
         boolean isLargeChest = false;
         boolean areReadingItems = false;
         ItemStack currentItem = null;
         List<ItemStack> items = new LinkedList<ItemStack>();
         try {
             boolean done = false;
             while (!done) {
                 String line = chestYml.readLine();
                 if (line != null) {
                     if (line.equals("")) {
                         //Skip
                     } else if (!line.substring(0, 1).equals(" ")) {
                         if (currentChest != null) {
                             CKChest chest = new CKChest(currentChest, isLargeChest);
                             if (currentItem != null) {
                                 items.add(currentItem);
                             }
                             chest.setItems(items.toArray(new ItemStack[items.size()]));
                             chests.put(currentChest.toLowerCase(), chest);
                             items.clear();
                         }
                        currentChest = line.substring(0, line.length() - 1);
                     } else if (line.startsWith("  type: ")) {
                         isLargeChest = line.contains("large");
                     } else if (line.startsWith("  eitems:")) {
                         areReadingItems = true;
                     } else if (areReadingItems && line.equals("  - !!com.aranai.virtualchest.ItemStackSave")) {
                         if (currentItem != null) {
                             items.add(currentItem);
                         }
                         currentItem = new ItemStack(1);
                     } else if (areReadingItems && line.startsWith("    count: ")) {
                         int i = Integer.valueOf(line.substring(11));
                         currentItem.setAmount(i);
                     } else if (areReadingItems && line.startsWith("    damage: ")) {
                         short s = Short.valueOf(line.substring(12));
                         currentItem.setDurability(s);
                     } else if (areReadingItems && line.startsWith("    id: ")) {
                         int id = Integer.valueOf(line.substring(8));
                         currentItem.setTypeId(id);
                     } else if (areReadingItems && line.startsWith("      ")) {
                         String ench = line.substring(6);
                         String[] bits = ench.split(": ");
                         currentItem.addUnsafeEnchantment(Enchantment.getById(Integer.valueOf(bits[0])), Integer.valueOf(bits[1]));
                     }
                 } else {
                     done = true;
                 }
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
         setDefaultChest(defaultChest);
     }
 }
