 package me.limebyte.battlenight.core.managers;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.logging.Level;
 
 import me.limebyte.battlenight.api.battle.PlayerClass;
 import me.limebyte.battlenight.core.util.BattleClass;
 import me.limebyte.battlenight.core.util.Messenger;
 import me.limebyte.battlenight.core.util.config.ConfigManager;
 import me.limebyte.battlenight.core.util.config.ConfigManager.Config;
 
 import org.bukkit.Material;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.inventory.ItemStack;
 
 public class ClassManager {
     private static List<PlayerClass> classes = new ArrayList<PlayerClass>();
     private static final Config configFile = Config.CLASSES;
 
     public static List<PlayerClass> getClasses() {
         return classes;
     }
 
     public static HashMap<String, PlayerClass> getClassNames() {
         HashMap<String, PlayerClass> classList = new HashMap<String, PlayerClass>();
 
         for (PlayerClass c : classes) {
             classList.put(c.getName(), c);
         }
 
         return classList;
     }
 
     public static void reloadClasses() {
         loadClasses();
         saveClasses();
     }
 
     public static void loadClasses() {
         Messenger.debug(Level.INFO, "Loading classes...");
         ConfigManager.reload(configFile);
         for (String className : ConfigManager.get(configFile).getConfigurationSection("Classes").getKeys(false)) {
             fixOldFiles(className);
             String armour = ConfigManager.get(configFile).getString("Classes." + className + ".Armour", "");
             String items = ConfigManager.get(configFile).getString("Classes." + className + ".Items", "");
             ConfigurationSection permissions = ConfigManager.get(configFile).getConfigurationSection("Classes." + className + ".Permissions");
             classes.add(new BattleClass(className, parseItems(items), sortArmour(parseItems(armour)), parsePermissions(permissions)));
         }
     }
 
     public static void saveClasses() {
         Messenger.debug(Level.INFO, "Saving classes...");
         for (PlayerClass c : classes) {
             ConfigManager.get(configFile).set("Classes." + c.getName() + ".Armour", parseItems(c.getArmour()));
             ConfigManager.get(configFile).set("Classes." + c.getName() + ".Items", parseItems(c.getItems()));
             ConfigManager.get(configFile).createSection("Classes." + c.getName() + ".Permissions", c.getPermissions());
         }
         ConfigManager.save(configFile);
     }
 
     private static List<ItemStack> parseItems(String rawItems) {
         String[] splitRawItems = new String[1];
 
         if (rawItems.contains(",")) {
             splitRawItems = rawItems.split(",");
         } else {
             splitRawItems[0] = rawItems;
         }
 
         List<ItemStack> items = new ArrayList<ItemStack>();
 
         for (String item : splitRawItems) {
             item = item.trim();
             if (item == null || item == "") {
                 continue;
             }
 
             int amount = 1;
             Map<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
             short data = 0;
 
             String[] part1 = item.split("x");
             String[] part2 = part1[0].split("e\\(");
             String[] part3 = part2[0].split(":");
 
             int id;
             try {
                 id = Integer.parseInt(part3[0]);
             } catch (NumberFormatException e1) {
                 Material mat = Material.getMaterial(part3[0].toUpperCase());
                 if (mat != null) {
                     id = mat.getId();
                 } else if (part3[0].equalsIgnoreCase("none")) {
                     id = Material.AIR.getId();
                 } else {
                     Messenger.debug(Level.WARNING, "Skipping ID: " + part3[0]);
                     continue;
                 }
             }
 
             // Do we have more than one item?
             if (part1.length == 2) {
                 int a = 1;
 
                 try {
                     a = Integer.parseInt(part1[1]);
                 } catch (NumberFormatException ex) {
                 }
 
                 amount = a;
             }
 
             // Do we have any enchantments?
             if (part2.length == 2) {
                 String[] part4 = part2[1].split("/");
 
                 for (String s : part4) {
                     String[] splitEnchantment = s.replace(")", "").split("~");
                     int e = -1;
                     int lvl = 1;
                     Enchantment enc;
 
                     // Checks
                     if (splitEnchantment.length == 0) {
                         continue;
                     }
 
                     // Get the enchantment id
                     try {
                         e = Integer.parseInt(splitEnchantment[0]);
                     } catch (NumberFormatException ex) {
                         continue;
                     }
 
                     enc = Enchantment.getById(e);
 
                     // More Checks
                     if (enc == null) {
                         continue;
                     }
 
                     // Do we have a level?
                     if (splitEnchantment.length == 2) {
                         try {
                             lvl = Integer.parseInt(splitEnchantment[1]);
                         } catch (NumberFormatException ex) {
                         }
                     } else {
                         lvl = enc.getStartLevel();
                     }
 
                     // Cap levels
                     if (lvl > enc.getMaxLevel()) {
                         lvl = enc.getMaxLevel();
                     }
 
                     // Add it
                     enchantments.put(enc, lvl);
                 }
             }
 
             // Do we have any data?
             if (part3.length == 2) {
                 short d = 0;
 
                 try {
                     d = Short.parseShort(part3[1]);
                 } catch (NumberFormatException ex) {
                 }
 
                 data = d;
             }
 
             ItemStack stack = null;
 
             if (id != Material.AIR.getId()) {
                 stack = new ItemStack(id, 1, data);
             }
 
             if (!enchantments.isEmpty()) {
                 try {
                     stack.addEnchantments(enchantments);
                 } catch (Exception ex) {
                     // TODO Log it
                 }
             }
 
             if (amount > 1) {
                 items.addAll(Arrays.asList(splitIntoStacks(stack, amount)));
             } else {
                 items.add(stack);
             }
 
         }
 
         return items;
     }
 
     private static String parseItems(List<ItemStack> items) {
         String rawItems = "";
 
         for (ItemStack item : items) {
 
             if (item == null) {
                 rawItems += ", none";
             } else {
                 int id = item.getTypeId();
                 int data = item.getDurability();
                 int amount = item.getAmount();
                 Map<Enchantment, Integer> enchantments = item.getEnchantments();
 
                 rawItems += ", " + id;
                 if (data > 0) {
                     rawItems += ":" + data;
                 }
                 if (!item.getEnchantments().isEmpty()) {
                     String rawEnchantments = "";
                     rawItems += "e(";
 
                     for (Map.Entry<Enchantment, Integer> enchantment : enchantments.entrySet()) {
                         rawEnchantments += "/" + enchantment.getKey().getId() + "~" + enchantment.getValue();
                     }
                     rawItems += rawEnchantments.substring(1);
 
                     rawItems += ")";
                 }
                 if (amount > 1) {
                     rawItems += "x" + amount;
                 }
             }
         }
 
         return rawItems.substring(2);
     }
 
     private static ItemStack[] splitIntoStacks(ItemStack item, int amount) {
         ItemStack[] items;
 
         if (item != null) {
             final int maxSize = item.getMaxStackSize();
             final int fullStacks = (int) Math.floor(amount / maxSize);
             final int finalStackAmount = amount % maxSize;
 
             ItemStack fullStack = item.clone();
             ItemStack finalStack = item.clone();
             fullStack.setAmount(maxSize);
             finalStack.setAmount(finalStackAmount);
 
             if (finalStackAmount > 0) {
                 items = new ItemStack[fullStacks + 1];
             } else {
                 items = new ItemStack[fullStacks];
             }
 
             for (int i = 0; i < fullStacks; i++) {
                 items[i] = fullStack;
             }
 
             if (finalStackAmount > 0) {
                 items[items.length - 1] = finalStack;
             }
         } else {
             items = new ItemStack[amount];
         }
 
         return items;
     }
 
     private static List<ItemStack> sortArmour(List<ItemStack> armour) {
         ItemStack helmet = null, chestplate = null, leggings = null, boots = null;
         for (ItemStack stack : armour) {
             if (ArmourType.HELMET.contains(stack)) {
                 helmet = stack;
             } else if (ArmourType.CHESTPLATE.contains(stack)) {
                 chestplate = stack;
             } else if (ArmourType.LEGGINGS.contains(stack)) {
                 leggings = stack;
             } else if (ArmourType.BOOTS.contains(stack)) {
                 boots = stack;
             }
         }
 
         List<ItemStack> sorted = new ArrayList<ItemStack>();
         sorted.add(0, helmet);
         sorted.add(1, chestplate);
         sorted.add(2, leggings);
         sorted.add(3, boots);
 
         return sorted;
     }
 
     public static PlayerClass getRandomClass() {
         Random random = new Random();
         int classNum = random.nextInt(classes.size());
         return classes.get(classNum);
     }
 
     private enum ArmourType {
         HELMET(Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET, Material.IRON_HELMET, Material.GOLD_HELMET, Material.DIAMOND_HELMET, Material.WOOL), CHESTPLATE(Material.LEATHER_CHESTPLATE,
                 Material.CHAINMAIL_CHESTPLATE, Material.IRON_CHESTPLATE, Material.GOLD_CHESTPLATE, Material.DIAMOND_CHESTPLATE), LEGGINGS(Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS,
                 Material.IRON_LEGGINGS, Material.GOLD_LEGGINGS, Material.DIAMOND_LEGGINGS), BOOTS(Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS, Material.IRON_BOOTS, Material.GOLD_BOOTS,
                 Material.DIAMOND_BOOTS);
 
         private Material[] materials;
 
         ArmourType(Material... materials) {
             this.materials = materials;
         }
 
         public boolean contains(ItemStack stack) {
             for (Material material : materials) {
                 if (stack == null) {
                     continue;
                 }
                 if (stack.getType().equals(material)) return true;
             }
             return false;
         }
     }
 
     private static void fixOldFiles(String className) {
         // Armour
         String armor = ConfigManager.get(configFile).getString("Classes." + className + ".Armor");
         String armour = ConfigManager.get(configFile).getString("Classes." + className + ".Armour");
         if (armor != null) {
             if (armour == null) {
                 ConfigManager.get(configFile).set("Classes." + className + ".Armour", armor);
             } else {
                 ConfigManager.get(configFile).set("Classes." + className + ".Armour", "none, none, none, none");
             }
             ConfigManager.get(configFile).set("Classes." + className + ".Armor", null);
         }
 
         // DummyItem
         int dummyItem = ConfigManager.get(configFile).getInt("DummyItem");
         if (dummyItem != 0) {
             ConfigManager.get(configFile).set("DummyItem", null);
         }
 
         ConfigManager.save(configFile);
     }
 
     private static HashMap<String, Boolean> parsePermissions(ConfigurationSection permissions) {
         HashMap<String, Boolean> map = new HashMap<String, Boolean>();
 
         if (permissions != null) {
             for (Map.Entry<String, Object> entry : permissions.getValues(false).entrySet()) {
                map.put(entry.getKey(), (Boolean) entry.getValue());
             }
         }
 
         return map;
     }
 }
