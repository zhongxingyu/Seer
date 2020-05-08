 
 package me.exphc.FreeTrade;
 
 import java.util.logging.Logger;
 import java.util.regex.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.SortedSet;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentSkipListSet;
 import java.util.Date;
 import java.sql.Timestamp;
 import java.io.*;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.*;
 import org.bukkit.command.*;
 import org.bukkit.entity.*;
 import org.bukkit.inventory.*;
 import org.bukkit.enchantments.*;
 import org.bukkit.configuration.*;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.event.player.*;
 import org.bukkit.event.*;
 import org.bukkit.block.*;
 import org.bukkit.*;
 
 import info.somethingodd.bukkit.OddItem.OddItem;
 
 import org.bukkit.craftbukkit.CraftServer;
 
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.server.EntityPlayer;
 import net.minecraft.server.ItemInWorldManager;
 
 enum Obtainability 
 { 
     NORMAL, SILKTOUCH, CREATIVE, HACKING, NEVER
 };
 
 class ItemQuery
 {
 
     ItemStack itemStack;
     static Logger log = Logger.getLogger("Minecraft");
 
     // Map between item names/aliases and id;dmg string
     // TODO: switch to more appropriate data structures, ItemStacks?
     static ConcurrentHashMap<String,String> name2CodeName;
     static ConcurrentHashMap<String,String> codeName2Name;
 
     // TODO: switch to sets
     static ConcurrentHashMap<String,Boolean> isTradableMap;
     static ConcurrentHashMap<Material,Boolean> isDurableMap;
     static ConcurrentHashMap<Material,Boolean> isEnchantableMap;
     static ConcurrentHashMap<Material,Boolean> isCountableMap;
 
     public ItemQuery(String s) {
         //Pattern onp = Pattern.compile( "^(\\d+)"
 
         Pattern p  = Pattern.compile(
             "^(\\d*)" +             // quantity
             "([# :;-]?)" +          // separator / stack flag
             "([^/\\\\]+)" +         // name
             "([/\\\\]?)" +          // separator / damage flag
             "([\\d%]*)" +           // use / damage
             "/?([^/]*)$");          // enchant
         Matcher m = p.matcher(s);
         int quantity;
 
         if (!m.find()) {
             throw new UsageException("Unrecognized item specification: " + s);
         }
 
         String quantityString = m.group(1);
         String isStackString = m.group(2);
         String nameString = m.group(3);
         String dmgOrUsesString = m.group(4);
         String usesString = m.group(5);
         String enchString = m.group(6);
 
         if (quantityString.equals("")) {
             quantity = 1;
         } else {
             quantity = Integer.parseInt(quantityString);
             if (quantity < 1) {
                 throw new UsageException("Invalid quantity: " + quantity);
             }
         }
 
 
         // Name
 
         // Allowed to be any case, with or without any word separators
         nameString = nameString.replaceAll("[ _-]", "").toLowerCase();
 
         if (nameString.contains("*")) {
             // Wildcard expressions
             SortedSet<String> results = wildcardLookupName(nameString);
             if (results.size() == 0) {
                 throw new UsageException("No items match pattern " + nameString);
             }
             if (results.size() > 1) {
                 StringBuffer nameMatches = new StringBuffer();
                 for (String resultName: results) {
                     nameMatches.append(resultName + ", ");
                 }
 
                 throw new UsageException("Found " + results.size() + " matching items: " + nameMatches);
             }
             // Exactly one hit, use it
             nameString = results.first().toLowerCase();
         } 
         // First try built-in name lookup
         itemStack = directLookupName(nameString);
 
         if (itemStack == null) {
             // If available, try OddItem for better names or clever suggestions
             if (Bukkit.getServer().getPluginManager().getPlugin("OddItem") != null) {
                 try {
                     itemStack = OddItem.getItemStack(nameString).clone();
                 } catch (IllegalArgumentException suggestion) {
                     throw new UsageException("No such item '" + nameString + "', did you mean '" + suggestion.getMessage() + "'?");
                 }
             } else {
                 // Worst case, lookup name from Bukkit itself
                 // Not very good because doesn't include damage value subtypes
                 Material material = Material.matchMaterial(nameString);
                 if (material == null) {
                     throw new UsageException("Unrecognized item name: " + nameString + " (no suggestions available)");
                 }
                 itemStack = new ItemStack(material);
             }
         }
 
         if (itemStack == null) {
             throw new UsageException("Unrecognized item name: " + nameString);
         }
 
         // Quantity, shorthand 10# = 10 stacks
         if (isStackString.equals("#")) {
             quantity *= Math.abs(itemStack.getType().getMaxStackSize());
         }
         itemStack.setAmount(quantity);
 
         // Damage value aka durability
         // User specifies how much they want left, 100% = unused tool
         short maxDamage = itemStack.getType().getMaxDurability();
         
         if (usesString != null && !usesString.equals("")) {
             short damage;
             short value;
 
             if (isCountable(itemStack.getType()) || !isDurable(itemStack.getType())) {
                 // Countable items specify damage directly
                 // -or- non-durable items
 
                 // TODO: restrict setting durability on non-durable items?? admin function
                 if (!isDurable(itemStack.getType())) {
                     log.info("Warning: setting durability on non-durable item "+nameStack(itemStack));
                 }
 
                 damage = Short.parseShort(usesString);
                 // TODO: clamp? for maps at least, if request beyond last + 2, will give last + 1
                 // but, if request last + 1, then will create a new map in that slot
             } else {
                 // Tools - uses, percentages
                 if (usesString.endsWith("%")) {
                     String percentageString = usesString.substring(0, usesString.length() - 1);
                     double percentage = Double.parseDouble(percentageString);
 
                     value = (short)(percentage / 100.0 * maxDamage);
                 } else {
                     value = Short.parseShort(usesString);
                 }
 
                 // Normally, convenient for user to specify percent or times left (inverse of damage), /
                 // Allow \ separator to specify damage itself
                 if (dmgOrUsesString.equals("\\")) {
                     damage = value;
                 } else {
                     damage = (short)(maxDamage - value);
                 }
 
 
                 if (damage > maxDamage) {
                     damage = maxDamage;     // Breaks right after one use
                 }
 
                 if (damage < 0) {
                     damage = 0;             // Completely unused
                 }
             }
 
             itemStack.setDurability(damage);
         } else {
             // If they didn't specify a durability, but they want a durable item, assume no damage (0)
             // TODO: only assume 0 for wants. For gives, need to use value from inventory! Underspecified
         }
 
         // Enchantments
         if (enchString != null && !enchString.equals("")) {
             // TODO: rethink enchantable and other item subtype permissions
             // Should always be expressible as new ItemQuery(), but may want to restrict
             // based on player permissions what they can trade
             /*
             if (!isEnchantable(itemStack.getType())) {
                 throw new UsageException("Not enchantable");
                 // TODO: allow permission to override, if want e.g. enchanted shears for testing
             }*/
 
             EnchantQuery enchs = new EnchantQuery(enchString);
 
             itemStack.addUnsafeEnchantments(enchs.all);
         }
     }
 
     public ItemQuery(String s, OfflinePlayer p) {
         if (s.equals("this")) {
             if (p.getPlayer() == null) {
                 throw new UsageException("Cannot specify 'this' on offline player");
             }
             itemStack = p.getPlayer().getItemInHand().clone();
             if (itemStack == null) {
                 throw new UsageException("No item in hand");
             }
         } else {
             itemStack = (new ItemQuery(s)).itemStack;
         }
     }
 
 
     // Return whether an item degrades when used
     public static boolean isDurable(Material m) {
         if (m == null) return false;    // TODO: non-standard items
 
         return isDurableMap.containsKey(m);
     }
    
     // Return whether an item can be legitimately enchanted
     public static boolean isEnchantable(Material m) {
         if (m == null) return false;    // TODO: non-standard items
 
         return isEnchantableMap.containsKey(m);
     }
 
     // Return whether an item is numbered, like maps in vanilla (map0, map1..)
     public static boolean isCountable(Material m) {
         if (m == null) return false;    // TODO: non-standard items
 
         return isCountableMap.containsKey(m);
     }
 
 
     public static String nameStack(ItemStack itemStack) {
         if (isNothing(itemStack)) {
             return "nothing";
         }
 
         String name, usesString, enchString;
         Material m = itemStack.getType();
        
         if (isDurable(m)) {
             // Percentage remaining
             
             // Round down so '100%' always means completely unused? (1 dmg = 99%)
             //int percentage = Math.round(Math.floor((m.getMaxDurability() - itemStack.getDurability()) * 100.0 / m.getMaxDurability()))
             // but then lower percentages are always one lower..
             // So just special-case 100% to avoid misleading
             int percentage;
             if (itemStack.getDurability() == 0) {
                 percentage = 100;
             } else {
                 percentage = (int)((m.getMaxDurability() - itemStack.getDurability()) * 100.0 / m.getMaxDurability());
                 if (percentage == 100) {
                     percentage = 99;
                 }
             }
 
             usesString = "/" + percentage + "%";
         } else {
             usesString = "";
         }
 
         // Find canonical name of item
         String codeName;
 
         codeName = itemStack.getTypeId() + "";
         name = codeName2Name.get(codeName);
         if (name == null) {
             // durability here actually is overloaded to mean a different item
             codeName = itemStack.getTypeId() + ";" + itemStack.getDurability();
             name = codeName2Name.get(codeName);
         }
 
         if (name == null) {
             name = "unknown="+codeName;
         }
 
         // Countable items are numbered (map/0, map/1...)
         if (isCountable(itemStack.getType())) {
             name += "/" + itemStack.getDurability();
         }
 
         // Enchantments
         if (EnchantQuery.hasEnchantments(itemStack)) {
             Map<Enchantment,Integer> enchs = itemStack.getEnchantments();
             enchString = "/" + EnchantQuery.nameEnchs(enchs);
         } else {
             enchString = "";
         }
 
         return itemStack.getAmount() + ":" + name + usesString + enchString;
     }
 
     // Return whether two item stacks have the same item, taking into account 'subtypes'
     // stored in the damage value (ex: blue wool, only same as blue wool) - but will
     // ignore damage for durable items (ex: diamond sword, 50% = diamond sword, 100%)
     public static boolean isSameType(ItemStack a, ItemStack b) {
         if (a.getType() != b.getType()) {
             return false;
         }
 
         Material m = a.getType();
 
         if (isDurable(m)) {
             return true;
         }
     
         return a.getDurability() == b.getDurability();
     }
 
     // Return whether two item stacks are identical - except for amount!
     // Compares type, durability, enchantments
     public static boolean isIdenticalItem(ItemStack a, ItemStack b) {
         if (a == null || b == null) {
             return false;
         }
         if (a == null && b == null) {
             return true;
         }
 
         if (a.getType() != b.getType()) {
             return false;
         }
 
         // Same subtype -or- durability for tools
         if (a.getDurability() != b.getDurability()) {
             return false;
         }
 
         // Same enchantments
         // Compare by name to avoid complex duplicate enchantment traversing code
         String enchNameA = EnchantQuery.nameEnchs(a.getEnchantments());
         String enchNameB = EnchantQuery.nameEnchs(b.getEnchantments());
 
         if (!enchNameA.equals(enchNameB)) {
             return false;
         }
 
         return true;
     }
 
     // Identical and same amounts too
     public static boolean isIdenticalStack(ItemStack a, ItemStack b) {
         return isIdenticalItem(a, b) && a.getAmount() == b.getAmount();
     }
 
     // Return whether item is configured to be allowed to be traded
     public static boolean isTradable(ItemStack items) {
         // Durability always stored, but 0 for durable items
         return isTradableMap.containsKey(items.getTypeId() + ";" + (isDurable(items.getType()) ? 0 : items.getDurability()));
     }
 
 
     // Configuration
 
     public static void loadConfig(YamlConfiguration config) {
         Map<String,Object> configValues = config.getValues(true);
         MemorySection itemsSection = (MemorySection)configValues.get("items");
         int i = 0;
     
         name2CodeName = new ConcurrentHashMap<String, String>();
         codeName2Name = new ConcurrentHashMap<String, String>();
         
         isDurableMap = new ConcurrentHashMap<Material, Boolean>();
         isEnchantableMap = new ConcurrentHashMap<Material, Boolean>();
         isCountableMap = new ConcurrentHashMap<Material, Boolean>();
         ConcurrentHashMap<String,Boolean> isTradableMapUnfiltered = new ConcurrentHashMap<String, Boolean>();
 
         HashSet<Obtainability> tradableCategories = new HashSet<Obtainability>();
 
         for (String obtainString: config.getStringList("tradableCategories")) {
             tradableCategories.add(Obtainability.valueOf(obtainString.toUpperCase()));
         }
 
         for (String codeName: itemsSection.getKeys(false)) {
             String properName = config.getString("items." + codeName + ".name");
 
             // How this item can be obtained
             String obtainString = config.getString("items." + codeName + ".obtain");
             Obtainability obtain = (obtainString == null) ? Obtainability. NORMAL : Obtainability.valueOf(obtainString.toUpperCase());
             boolean tradable = tradableCategories.contains(obtain);
             // TODO: whitelist, blacklist (with wildcards! search)
             if (tradable) {
                 isTradableMapUnfiltered.put(codeName.contains(";") ? codeName : codeName + ";0", tradable);
             }
 
             // Add aliases from config
             List<String> aliases = config.getStringList("items." + codeName + ".aliases");
             if (aliases != null) {
                 for (String alias: aliases) {
                     name2CodeName.put(alias, codeName);
                     i += 1;
                 } 
             }
 
             // Generate 'proper name' alias, preprocessed for lookup
             String smushedProperName = properName.replaceAll(" ","");
             String aliasProperName = smushedProperName.toLowerCase();
             name2CodeName.put(aliasProperName, codeName);
             i += 1;
             codeName2Name.put(codeName, smushedProperName);
 
             // Generate numeric alias
             name2CodeName.put(codeName, codeName);
             i += 1;
 
 
             // Whether loses durability when used or not (include in trades)
             String purpose = config.getString("items." + codeName + ".purpose");
             Material material = codeName2ItemStack(codeName).getType();
             boolean durable = purpose != null && (purpose.equals("armor") || purpose.equals("tool") || purpose.equals("weapon"));
 
             if (durable) {
                 isDurableMap.put(material, new Boolean(true));
             }
 
             // Items are enchantable if durable, unless overridden (for shears, etc.)
             boolean enchantable = config.getBoolean("items." + codeName + ".enchant", durable);
 
             if (enchantable) {
                 isEnchantableMap.put(material, new Boolean(true));
             }
    
             if (config.getBoolean("items." + codeName + ".count", false)) {
                 isCountableMap.put(material, new Boolean(true));
             }
         }
         log.info("Loaded " + i + " item aliases");
 
         // Whitelist tradable items
         for (String whiteString: config.getStringList("tradableWhitelist")) {
             ItemStack itemStack = directLookupName(whiteString);
 
             isTradableMapUnfiltered.put(itemStack.getTypeId() + ";" + itemStack.getDurability(), true);
         }
 
 
         // Filter through blacklist
         isTradableMap = new ConcurrentHashMap<String, Boolean>();
         SKIP: for (String tradableCodeName: isTradableMapUnfiltered.keySet()) {
             // Is this blacklisted?
             for (String blackString: config.getStringList("tradableBlacklist")) {
                 ItemStack itemStack = directLookupName(blackString);
 
                 if (tradableCodeName.equals(itemStack.getTypeId() + ";" + itemStack.getDurability())) {
                     continue SKIP;
                 }
             }
 
             // No, add to real list
             isTradableMap.put(tradableCodeName, true);
         }
     }
 
     // Parse a material code string with optional damage value (ex: 35;11)
     public static ItemStack codeName2ItemStack(String codeName) {
         Pattern p = Pattern.compile("^(\\d+)[;:/]?(\\d*)([+]?.*)$");
         Matcher m = p.matcher(codeName);
         int typeCode;
         short dmgCode;
 
         if (!m.find()) {
             // This is an error in the config file (TODO: preparse or detect earlier)
             throw new UsageException("Invalid item code format: " + codeName);
         }
 
         // typeid
         typeCode = Integer.parseInt(m.group(1));
         // ;damagevalue 
         if (m.group(2) != null && !m.group(2).equals("")) {
             dmgCode = Short.parseShort(m.group(2));
         } else {
             dmgCode = 0;
         }
             
         ItemStack item = new ItemStack(typeCode, 1, dmgCode);
 
         // +enchantcode@enchantlevel...
         if (m.group(3) != null && !m.group(3).equals("")) {
             String[] parts = m.group(3).split("[+]");
 
             for (String part: parts) {
                 if (part.length() == 0) {
                     continue;
                 }
 
                 String[] idAndLevel = part.split("@");
                 if (idAndLevel.length != 2) {
                     throw new UsageException("Invalid item code: " + codeName + ", enchantment spec: " + part);
                 }
                 int id, level;
                 try {
                     id = Integer.parseInt(idAndLevel[0]);
                     level = Integer.parseInt(idAndLevel[1]);
                 } catch (Exception e) {
                     throw new UsageException("Invalid item code: " + codeName + ", enchantment id/level: " + part);
                 }
 
                 Enchantment ench = Enchantment.getById(id);
 
                 // Add unsafe, since plugins might want to (ab)use enchantment for other purposes
                 item.addUnsafeEnchantment(ench, level);
             }
         }
 
         return item;
     }
 
     // Get an ItemStack directly from one of its names or aliases, or null
     private static ItemStack directLookupName(String nameString) {
         String materialCode = name2CodeName.get(nameString);
 
         if (materialCode == null) {
             return null;
         }
 
         return codeName2ItemStack(materialCode);
     }
 
     // Get proper names of all aliases matching a wildcard pattern
     private static SortedSet<String> wildcardLookupName(String pattern) {
         SortedSet<String> results = new TreeSet<String>();
 
         Iterator it = name2CodeName.entrySet().iterator();
         while (it.hasNext()) {
             Map.Entry pair = (Map.Entry)it.next();
 
             String name = (String)pair.getKey();
             String codeName = (String)pair.getValue();
 
             if (matchesWildcard(pattern, name) && isTradableMap.containsKey(codeName)) {
                 results.add(codeName2Name.get(codeName).replace(" ",""));
             }
         }
 
         return results;
     }
 
     // Return whether a wildcard pattern (with asterisks = anything) matches
     private static boolean matchesWildcard(String needle, String haystack) {
         String[] cards = needle.split("\\*");
 
         for (String card : cards) {
             int i = haystack.indexOf(card);
             if (i == -1) {
                 return false;
             }
 
             haystack = haystack.substring(i + card.length());
         }
 
         return true;
     }
 
     public static boolean isNothing(ItemStack itemStack) {
         return itemStack == null || itemStack.getType() == Material.AIR;
     }
 }
 
 class EnchantQuery
 {
     static Logger log = Logger.getLogger("Minecraft");
 
     Map<Enchantment,Integer> all;
 
     static ConcurrentHashMap<String, Enchantment> name2Code;
     static ConcurrentHashMap<Enchantment, String> code2Name;
 
     public EnchantQuery(String allString) {
         all = new HashMap<Enchantment,Integer>();
 
         String[] enchStrings = allString.split("[, /-]+");
         for (String enchString: enchStrings) {
             Pattern p = Pattern.compile("^([A-Za-z-]*[a-z])([IV0-9]*)$");
             Matcher m = p.matcher(enchString);
 
             if (!m.find()) {
                 throw new UsageException("Unrecognizable enchantment: '" + enchString + "'");
             }
 
             String baseName = m.group(1);
             String levelString = m.group(2);
 
             Enchantment ench = enchFromBaseName(baseName);
             int level = levelFromString(levelString);
 
             // Odd, what's the point of having a separate 'wrapper' class?
             // Either way, it has useful methods for us
             //EnchantmentWrapper enchWrapper = new EnchantmentWrapper(ench.getId());
             EnchantmentWrapper enchWrapper = wrapEnch(ench);
 
             // TODO: restrict max level? need to figure out permissions here
             // sometimes (often), exceeding the "max" is useful
             if (level > enchWrapper.getMaxLevel()) {
                 log.info("Warning: exceeding max level enchantment "+ench+" "+level+" > " + enchWrapper.getMaxLevel());
                 //level = ench.getMaxLevel();
             }
 
             log.info("Enchantment: " + ench + ", level="+level);
 
             all.put(enchWrapper, new Integer(level));
         }
     }
 
     // Return whether all the enchantments can apply to an item
     public boolean canEnchantItem(ItemStack item) {
         Iterator it = all.entrySet().iterator();
         while (it.hasNext()) {
             Map.Entry pair = (Map.Entry)it.next();
 
             EnchantmentWrapper ench = wrapEnch(pair.getKey());
             Integer level = (Integer)pair.getValue();
 
             if (!ench.canEnchantItem(item)) {
                 log.info("Cannot apply enchantment " + ench + " to " + item);
                 return false;
             }
         }
         return true;
     }
 
     public static boolean hasEnchantments(ItemStack item) {
         Map<Enchantment,Integer> enchs = item.getEnchantments();
 
         return enchs.size() != 0;
     }
 
     public String toString() {
         return nameEnchs(all);
     }
 
     public static String nameEnchs(Map<Enchantment,Integer> all) {
         StringBuffer names = new StringBuffer();
         Iterator it = all.entrySet().iterator();
 
         while (it.hasNext()) {
             Map.Entry pair = (Map.Entry)it.next();
 
             Object obj = pair.getKey();
 
             EnchantmentWrapper ench = wrapEnch(pair.getKey());
             Integer level = (Integer)pair.getValue();
 
             names.append(nameEnch(ench));
             names.append(levelToString(level));
             names.append(","); 
         }
 
         // Remove the trailing comma
         // Would have liked to just build an array then join it, but not easier in Java either 
         if (names.length() > 1) { 
             names.deleteCharAt(names.length() - 1);
         }
 
         return names.toString();
     }
 
     // Get an EnchantmentWrapper from either an EnchantmentWrapper or Enchantment
     // Not sure why Bukkit chose to have two classes, but EnchantmentWrapper is more functional
     public static EnchantmentWrapper wrapEnch(Object obj) {
         if (obj instanceof EnchantmentWrapper) {
             return (EnchantmentWrapper)obj;
         }
 
         Enchantment ench = (Enchantment)obj;
 
         return new EnchantmentWrapper(ench.getId());
     }
 
 
 
     static Enchantment enchFromBaseName(String name) {
         // Built-in config file database..
         name = name.toLowerCase();
         Enchantment ench = name2Code.get(name);
         if (ench != null) {
             return ench;
         }
       
         // Bukkit itself?
         ench = Enchantment.getByName(name);
         if (ench != null) {
             return ench;
         }
 
         throw new UsageException("Unrecognized enchantment: " + name);
     }
 
     static String nameEnch(EnchantmentWrapper ench) {
         String name = code2Name.get(ench);
         if (name != null) {
             return name;
         }
         return "Unknown(" + ench.getId() + ")";
         // There is ench.getName(), but the names don't match in-game
     }
 
     static int levelFromString(String s) {
         if (s.equals("") || s.equals("I")) {
             return 1;
         } else if (s.equals("II")) { 
             return 2;
         } else if (s.equals("III")) {
             return 3;
         } else if (s.equals("IV")) {
             return 4;
         } else if (s.equals("V")) {
             return 5;
         } else {
             return Integer.parseInt(s);
         }
     }
 
     static String levelToString(int n) {
         switch (n) {
         case 1: return "I";
         case 2: return "II";
         case 3: return "III";
         case 4: return "IV";
         case 5: return "V";
         default: return Integer.toString(n);
         }
     }
 
     // Return wheather itemA has >= enchantments than itemB
     public static boolean equalOrBetter(ItemStack itemA, ItemStack itemB) {
         Map<Enchantment,Integer> enchsB = itemB.getEnchantments();
         Iterator it = enchsB.entrySet().iterator();
 
         while (it.hasNext()) {
             Map.Entry pair = (Map.Entry)it.next();
 
             EnchantmentWrapper enchB = (EnchantmentWrapper)pair.getKey();
             int levelB = ((Integer)pair.getValue()).intValue();
 
             if (!itemA.containsEnchantment(Enchantment.getById(enchB.getId()))) {
                 log.info("Missing enchantment " + nameEnch(enchB) + " not on " + itemA + " (doesn't match " + itemB + ")");
                 return false;
             }
 
             int levelA = itemA.getEnchantmentLevel(Enchantment.getById(enchB.getId()));
             log.info("Level " + levelB + " vs " + levelA);
             if (levelA < levelB) {
                 log.info("Lower enchantment level " + levelA + " < " + levelB);
                 return false;
             }
        } 
        return true;
     }
 
     public static void loadConfig(YamlConfiguration config) {
         Map<String,Object> configValues = config.getValues(true);
         MemorySection enchantsSection = (MemorySection)configValues.get("enchants");
         int i = 0;
     
         name2Code = new ConcurrentHashMap<String, Enchantment>();
         code2Name = new ConcurrentHashMap<Enchantment, String>();
         
         for (String codeString: enchantsSection.getKeys(false)) {
             Enchantment ench = Enchantment.getById(Integer.parseInt(codeString));
             String properName = config.getString("enchants." + codeString + ".name");
 
             List<String> aliases = config.getStringList("enchants." + codeString + ".aliases");
 
             if (aliases != null) {
                 for (String alias: aliases) {
                     name2Code.put(alias, ench);
                     i += 1;
                 } 
             }
 
             // Generate 'proper name' alias, preprocessed for lookup
             String smushedProperName = properName.replaceAll(" ","");
             String aliasProperName = smushedProperName.toLowerCase();
             name2Code.put(aliasProperName, ench);
             i += 1;
             code2Name.put(ench, smushedProperName);
         }
         log.info("Loaded " + i + " enchantment aliases");
     }
 }
 
 class Order implements Comparable
 {
     OfflinePlayer player;
     ItemStack want, give;
     boolean free;
 
     public Order(OfflinePlayer p, String wantString, String giveString) {
         player = p;
 
         if (wantString.startsWith("!")) {
             if (player.getPlayer() == null || !player.getPlayer().hasPermission("freetrade.rawitems")) {
                 throw new UsageException("You do not have permission to request raw items");
             }
             // TODO: merge into ItemQuery! and better permissions (allow raw if is item they can trade, already exists)
             want = ItemQuery.codeName2ItemStack(wantString.replace("!", ""));
             // TODO: allow !-less raw items, but need quantity separator (.?)
         } else {
             want = (new ItemQuery(wantString, p)).itemStack;
         }
 
         if (giveString.startsWith("!")) {
             if (player.getPlayer() == null || !player.getPlayer().hasPermission("freetrade.rawitems")) {
                 throw new UsageException("You do not have permission to request raw items");
             }
             give = ItemQuery.codeName2ItemStack(giveString.replace("!", ""));
         } else {
             give = (new ItemQuery(giveString, p)).itemStack;
         }
 
         if (ItemQuery.isIdenticalItem(want, give)) {
             throw new UsageException("You can't trade items for themselves");
         }
     }
 
     public Order(OfflinePlayer p, ItemStack w, ItemStack g) {
         player = p;
         want = w;
         give = g;
     }
 
     public String toString() {
         // TODO: pregenerate in initialization as description, no need to relookup
         return player.getName() + " wants " + ItemQuery.nameStack(want) + " for " + ItemQuery.nameStack(give);
     }
 
     // Convert to a command that can be executed to recreate the order
     public String serialize() {
         // TODO: would be nice to return a String[] instead
         String cmd = "want " + 
             player.getName() + " " + 
             ItemQuery.nameStack(want) + " " +
             ItemQuery.nameStack(give);
 
         return cmd;
     }
 
     public static Order deserialize(String s) {
         String[] parts = s.split(" ");
         if (parts.length != 4) {
             throw new UsageException("Invalid serialized order parts: " + s);
         }
         if (!parts[0].equals("want")) {
             throw new UsageException("Invalid serialized order command: " + s);
         }
         String playerString = parts[1];
         String wantString = parts[2];
         String giveString = parts[3];
 
         OfflinePlayer player = Bukkit.getOfflinePlayer(playerString);
         if (player == null) {
             throw new UsageException("Sorry, player "+playerString+" not found, cannot resurrect order");
         }
 
         return new Order(player, wantString, giveString);
     }
 
     // Required for ConcurrentSkipListSet - Comparable interface
     public int compareTo(Object obj) {
         if (!(obj instanceof Order)) {
             return -1;
         }
         Order rhs = (Order)obj;
 
         return toString().compareTo(rhs.toString());
 
         //return player.getName().compareTo(rhs.player.getName()) || ItemQuery.isIdenticalStack(want, rhs.want) || ItemQuery.isIdenticalStack(give, rhs.give);
     }
 }
 
 // Exception to be reported back to player as invalid usage
 class UsageException extends RuntimeException
 {
     String message;
 
     public UsageException(String msg) {
         message = msg;
     }
 
     public String toString() {
         return "UsageException: " + message;
     }
 
     public String getMessage() {
         return message;
     }
 }
 
 // Completed order record
 class Transaction
 {
     OfflinePlayer playerA, playerB;
     ItemStack itemsA, itemsB;   // received by corresponding player
     Timestamp whenExecuted;
 
     FreeTrade plugin;
 
 
     public Transaction(FreeTrade pl, OfflinePlayer pa, ItemStack a, OfflinePlayer pb, ItemStack b) {
         plugin = pl;
         playerA = pa; 
         playerB = pb;
         itemsA = a;
         itemsB = b;
     
         // Timestamp
         whenExecuted = new Timestamp((new Date()).getTime());
     }
 
     public String toString() {
         // CSV
         // TODO: items more easily parseable? for 3rd parties
         return whenExecuted + "," +
             playerA.getName() + "," + 
             ItemQuery.nameStack(itemsA) + "," + 
             playerB.getName() + "," + 
             ItemQuery.nameStack(itemsB);
     }
     
     // Log to file
     public void log() {
         String filename = plugin.getDataFolder() + System.getProperty("file.separator") + "transactions.csv"; 
 
         try {
             BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true));
 
             writer.write(toString());
             writer.newLine();
             writer.close();
         } catch (IOException e) {
             plugin.log.info("Failed to save transaction! " + e.getMessage());
         }
 
     }
 }
 
 // TODO: ought to be a built-in class for this (or in WorldGuard?)
 class Zone
 {
     int minX, minZ, maxX, maxZ;
 
     public Zone(int mx, int mz, int xx, int xz) {
         minX = mx;
         minZ = mz;
         maxX = xx;
         maxZ = xz;
     }
 
     public Zone(List objs) {
         minX = ((Integer)objs.get(0)).intValue();
         minZ = ((Integer)objs.get(1)).intValue();
         maxX = ((Integer)objs.get(2)).intValue();
         maxZ = ((Integer)objs.get(3)).intValue();
     }
 
     public String toString() {
         return minX + "<x<" + maxX + ", " + minZ + "<z<" + maxZ;
     }
 
     public boolean within(Location loc) {
         return loc.getX() > minX && loc.getX() < maxX && loc.getZ() > minZ && loc.getZ() < maxZ;
     }
 }
 
 class Market
 {
     ConcurrentSkipListSet<Order> orders;
     static Logger log = Logger.getLogger("Minecraft");
 
     Zone tradeZone = null;
     int tradeTerminalRadius = 0;
     Material tradeTerminalMaterial;
     ItemStack tradeTerminalBlock;
 
     FreeTrade plugin;
 
     public Market(FreeTrade pl) {
         orders = new ConcurrentSkipListSet<Order>();
 
         plugin = pl;
         // Note: will also want to load()
     }
 
     public void loadConfig(YamlConfiguration config) {
         // TODO: figure out how to fix 'unchecked conversion' warning. getList() returns a List<Object>, so...
         List tradeZoneObj = config.getList("tradeZone");
 
         if (tradeZoneObj != null) {
             tradeZone = new Zone(tradeZoneObj);
             log.info("Enforcing trade zone: " + tradeZone);
         }
 
         tradeTerminalRadius = config.getInt("tradeTerminalRadius");
         tradeTerminalBlock = (new ItemQuery(config.getString("tradeTerminalBlock"))).itemStack;
         tradeTerminalMaterial = tradeTerminalBlock.getType();  // limitation: type only
 
         if (config.getBoolean("tradeTerminalCraftable")) {
             // Two ways to create trade terminal: 
             // 2 enderpearls (for teleporting items, sending and receiving end)
             // 4 lapis lazuli ore (watery-looking material, travels through the aether)
             // into a sponge, sucks the items through to their destination
             // TODO: configurable recipes
             ShapelessRecipe recipe = new ShapelessRecipe(tradeTerminalBlock);
             recipe.addIngredient(2, Material.ENDER_PEARL);
             Bukkit.getServer().addRecipe(recipe);
 
             recipe = new ShapelessRecipe(tradeTerminalBlock);
             recipe.addIngredient(4, Material.LAPIS_ORE);
             Bukkit.getServer().addRecipe(recipe);
         }
     }
 
     public boolean showOutstanding(CommandSender sender) {
         sender.sendMessage("Open orders:");
            
         int i = 0;
         for (Order order: orders) {
             i += 1;
             sender.sendMessage(i + ". " + order);
         }
 
         sender.sendMessage("To add or fulfill an order:");
 
         return false;
     }
 
     // Save open orders to disk
     public void save() {
         String filename = getOutstandingSaveFilename();
 
         try {
             BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
 
             for (Order order: orders) {
                 writer.write(order.serialize());
                 writer.newLine();
             }
 
             writer.close();
         } catch (IOException e) {
             log.info("Failed to save orders! " + e.getMessage());
         }
     }
 
     // Load open orders from disk
     public void load() {
         String filename = getOutstandingSaveFilename();
 
         try {
             BufferedReader reader = new BufferedReader(new FileReader(filename));
 
             orders.clear();
 
             String line;
             do {
                 line = reader.readLine();
                 if (line != null) {
                     try {
                         Order order = Order.deserialize(line);
                         orders.add(order);
                     } catch (Exception e) {
                         log.info("Bad order: " + line + " (" + e.getMessage() + "), ignored");
                     }
                 }
             } while (line != null);
 
             reader.close();
         } catch (IOException e) {
             log.info("Failed to load orders! " + e.getMessage());
         }
     }
 
     private String getOutstandingSaveFilename() {
         return plugin.getDataFolder() + System.getProperty("file.separator") + "outstanding.txt"; 
     }
 
 
     public void cancelOrder(OfflinePlayer player, String s) {
         if (s == null || s.equals("-")) {
             cancelOrders(player);
             return;
         }
 
         ItemStack wanted = (new ItemQuery(s, player)).itemStack;
         int i = 0;
 
         for (Order order: orders) {
             if (order.player.equals(player) && ItemQuery.isIdenticalItem(order.want, wanted)) {
                 cancelOrder(order);
                 i += 1;
             }
         }
 
         if (player.getPlayer() != null) {
             player.getPlayer().sendMessage("Canceled " + i + " orders");
         }
     }
 
     // Cancel all orders for a player
     public void cancelOrders(OfflinePlayer player) {
         int i = 0;
         for (Order order: orders) {
             if (order.player.equals(player)) {
                 cancelOrder(order);
                 i += 1;
             }
         }
         if (player.getPlayer() != null) {
             player.getPlayer().sendMessage("Canceled all your " + i + " orders");
         }
     }
 
     public void cancelOrder(Order order) {
         if (!orders.remove(order)) {
             for (Order o: orders) {
                 log.info("Compare " + o + " = " + o.compareTo(order));
             }
 
             throw new UsageException("Failed to find order to cancel: " + order);
         }
         Bukkit.getServer().broadcastMessage("Closed order " + order);
         
         save();
     }
 
     public void placeOrder(Order order) {
         Player onlinePlayer = order.player.getPlayer();
 
         if (onlinePlayer == null) {
             throw new UsageException("Offline player tried to place order");
         }
 
         // Admin conjuring permission
         if (ItemQuery.isNothing(order.give)) {
             if (!onlinePlayer.hasPermission("freetrade.conjure")) {
                 throw new UsageException("You must specify or select what you want to trade for");
             }
 
             recvItems(order.player, order.want);
             return;
         }
 
         // Item obliteration destruction permission
         if (ItemQuery.isNothing(order.want)) {
             if (!onlinePlayer.hasPermission("freetrade.obliterate")) {
                 throw new UsageException("You do not have permission to trade items for nothing");
                 // tip: throw in lava or cacti instead
             }
             takeItems(order.player, order.give);
             return;
         }
 
         // Trade restrictions
         if (!onlinePlayer.hasPermission("freetrade.trade")) {
             throw new UsageException("You are not allowed to trade");
         }
 
         if (tradeZone != null && !tradeZone.within(onlinePlayer.getLocation())) {
             throw new UsageException("You must be within the trade zone " + tradeZone + " to trade");
         }
 
         
         // TODO: Trade machine nearby? (if enabled)
         if (tradeTerminalRadius != 0) {
             Location location = onlinePlayer.getLocation();
             World world = onlinePlayer.getWorld();
            
             int r = tradeTerminalRadius;
             int ox = location.getBlockX(), oy = location.getBlockY(), oz = location.getBlockZ();
             log.info("original= "+ox+","+oy+","+oz);
             boolean found = false;
 
             DONE: for (int x = -r; x < r; x += 1) {
                 for (int y = -r; y < r; y += 1) {
                     for (int z = -r; z < r; z += 1) {
                         Block block = world.getBlockAt(x+ox, y+oy, z+oz);
                         //log.info("blockat("+x+","+y+","+z+") = " + block);
                         if (block.getType().equals(tradeTerminalMaterial)) {
                             found = true;
                         }
                     }
                 }
             }
             log.info("Found = " + found);
             if (!found) {
                 throw new UsageException("You are not within " + tradeTerminalRadius + " blocks of a " +
                     ItemQuery.codeName2Name.get(tradeTerminalMaterial.getId() + "") + " trading terminal");
             }
         }
 
 
         // TODO: if asking for identical want, different give, then update give? (Updating orders)
         // Not sure, might want to try asking for all different things for same item if really want it..
 
         // Restricted by admin?
         // TODO: show reason (unobtainable, blacklisted)
         if (!ItemQuery.isTradable(order.want)) {
             throw new UsageException("Trading " + ItemQuery.nameStack(order.want) + " is prohibited");
         }
         if (!ItemQuery.isTradable(order.give)) {
             throw new UsageException("Trading " + ItemQuery.nameStack(order.give) + " is prohibited");
         }
 
         // You can only give what you have
         if (!hasItems(order.player, order.give)) {
             throw new UsageException("You don't have " + ItemQuery.nameStack(order.give) + " to give");
         }
 
         if (matchOrder(order)) {
             // Executed
             return;
         }
 
         // Not fulfilled; add to outstanding to match with future order 
         // Broadcast to all players so they know someone wants something, then add
         Bukkit.getServer().broadcastMessage("Wanted: " + order);
         orders.add(order);
 
         // Save to disk on every order
         save();
     }
 
     // Transfer items from one player to another
     public static void transferItems(OfflinePlayer fromPlayer, OfflinePlayer toPlayer, ItemStack items) {
         /* // XXX: This is very important to prevent offline failures until have exception rollback
         Player fromPlayer = fromPlayerOffline.getPlayer();
         Player toPlayer = toPlayerOffline.getPlayer();
 
         if (fromPlayer == null) {
             // TODO: open up player's .dat, edit. Offline transfers!
             throw new UsageException("Sorry, from player "+fromPlayer.getDisplayName()+" is offline, cannot transfer items");
         }
 
 
         if (toPlayer == null) {
             // TODO: offline player support
             throw new UsageException("Sorry, to player "+fromPlayer.getDisplayName()+" is offline, cannot transfer items");
         }*/
 
 
         // Online player transfer
 
         if (!hasItems(fromPlayer, items)) {
             throw new UsageException("Player " + fromPlayer.getName() + " doesn't have " + ItemQuery.nameStack(items));
         }
 
         int missing = -1;
         Exception takeException = null;
         try {
             missing = takeItems(fromPlayer, items);
         } catch (Exception e) {
             missing = -1;
             takeException = e;
         }
 
         if (missing == -1 || missing > 0) {
             // Rollback order
             // TODO: verify
             items.setAmount(items.getAmount() - missing);
             recvItems(fromPlayer, items);
 
             // TODO: try to prevent this from happening, by watching inventory changes, player death, etc
             if (missing > 0) {
                 throw new UsageException("Player " + fromPlayer.getName() + " doesn't have enough " + ItemQuery.nameStack(items) + ", missing " + missing + ", reverted");
             } else {
                 throw new UsageException("Player " + fromPlayer.getName() + "  could not have items taken ("+takeException.getMessage()+"), reverted");
             }
         }
 
         try {
             recvItems(toPlayer, items);
         } catch (Exception recvException) {
             // Give back
             // TODO: this needs to be BULLETPROOF to avoid item duping
             recvItems(fromPlayer, items);
 
             throw new UsageException("Player " + toPlayer.getName() + " could not receive items ("+recvException.getMessage()+"), reverted");
         }
 
         Bukkit.getServer().broadcastMessage(toPlayer.getName() + " received " + 
             ItemQuery.nameStack(items) + " from " + fromPlayer.getName());
     }
 
     // Remove items from player's inventory, return # of items player had < amount (insufficient items)
     // Based on OddItem
     public static int takeItems(OfflinePlayer offlinePlayer, ItemStack goners) {
         Player player = offlinePlayer.getPlayer();
         boolean offline = player == null;
 
         if (offline) {
             player = loadOfflinePlayer(offlinePlayer);
         }
 
         int remaining = takeItemsOnline(player, goners);
 
         if (offline) {
             saveOfflinePlayer(player);
         }
 
         return remaining;
     }
 
     private static int takeItemsOnline(Player player, ItemStack goners) {
         player.saveData();
 
         ItemStack[] inventory = player.getInventory().getContents();
 
         int remaining = goners.getAmount();
         int i = 0;
 
         for (ItemStack slot: inventory) {
             if (ItemQuery.isIdenticalItem(slot, goners)) {
                 if (remaining > slot.getAmount()) {
                     remaining -= slot.getAmount();
                     slot.setAmount(0);
                 } else if (remaining > 0) {
                     slot.setAmount(slot.getAmount() - remaining);
                     remaining = 0;
                 } else {
                     slot.setAmount(0);
                 }
 
                 // If removed whole slot, need to explicitly clear it
                 // ItemStacks with amounts of 0 are interpreted as 1 (possible Bukkit bug?)
                 if (slot.getAmount() == 0) {
                     player.getInventory().clear(i);
                 }
             }
 
             i += 1;
 
             if (remaining == 0) {
                 break;
             }
         }
 
         return remaining;
     }
 
     // Return whether player has at least the items in the stack
     public static boolean hasItems(OfflinePlayer offlinePlayer, ItemStack items) {
         Player player = offlinePlayer.getPlayer();
 
         if (player == null) {
            player = loadOfflinePlayer(player);
         }
 
         return hasItemsOnline(player, items);
         // not saved - no changes
     }
 
     private static boolean hasItemsOnline(Player player, ItemStack items) {
         ItemStack[] inventory = player.getInventory().getContents();
 
         int remaining = items.getAmount();
 
         for (ItemStack slot: inventory) {
             if (ItemQuery.isIdenticalItem(slot, items)) {
                 remaining -= slot.getAmount();
             }
         }
         
         return remaining <= 0;
     }
 
     // Have a player receive items in their inventory
     public static void recvItems(OfflinePlayer offlinePlayer, ItemStack items) {
         Player player = offlinePlayer.getPlayer();
         boolean offline = player == null;
 
         if (offline) {
             player = loadOfflinePlayer(offlinePlayer);
         }
 
         recvItemsOnline(player, items);
 
         if (offline) {
             saveOfflinePlayer(player);
         }
     }
 
     // Save a temporary online player object created by loadOfflinePlayer()
     private static void saveOfflinePlayer(Player player) {
         player.saveData();
         // TODO: need to destroy entity?
     }
 
     // Load an offline player into a temporary online Player
     private static Player loadOfflinePlayer(OfflinePlayer player) {
         List<File> files = getOfflinePlayerDataFiles(player);
 
         for (File file: files) {
             // Load offline player .dat
             // see also https://github.com/lishd/OpenInv/blob/master/src/lishid/openinv/commands/OpenInvPluginCommand.java#L81
             net.minecraft.server.MinecraftServer console = ((CraftServer)Bukkit.getServer()).getServer();
 
             net.minecraft.server.ItemInWorldManager manager = new net.minecraft.server.ItemInWorldManager(console.getWorldServer(0));
 
             net.minecraft.server.EntityPlayer entity = new net.minecraft.server.EntityPlayer(
                 console,
                 console.getWorldServer(0),
                 player.getName(),
                 manager);
 
             if (entity == null) {
                 throw new UsageException("Failed to load offline player entity " + player.getName());
             }
             
             Player onlinePlayer = (Player)entity.getBukkitEntity();
 
             if (onlinePlayer == null) {
                 throw new UsageException("Failed to load offline player " + player.getName());
             }
 
             log.info("Found! " + onlinePlayer);
 
             // read .dat
             onlinePlayer.loadData();
 
             return onlinePlayer;
 
             /*
             PlayerInventory inv = onlinePlayer.getInventory();
             for (ItemStack slot: inv.getContents()) {
                 log.info("slot "+slot);
             }*/
 
             // TODO: multiple players? destroy other entities?
             //break;
         }
 
         throw new UsageException("Could not find offline player "+player.getName());
     }
 
     // Get all player .dat files for an offline player
     // Searches through all worlds, so could conceivably find >1
     // (with multiworld plugin? but I haven't seen any in world_nether or world_the_end)
     private static List<File> getOfflinePlayerDataFiles(OfflinePlayer player) {
         List<File> playerFiles = new ArrayList<File>(1);
 
         String thisPlayerName = player.getName();
 
         List<World> worlds = Bukkit.getWorlds();
         for (World world: worlds) {
             File players = new File(world.getWorldFolder(), "players");
 
             for (File playerFile: players.listFiles()) {
                 String playerName = playerFile.getName().replaceFirst("\\.dat$", "");
 
                 if (playerName.trim().equalsIgnoreCase(thisPlayerName)) {
                     playerFiles.add(playerFile);
                 }
             }
         }
 
         return playerFiles;
     }
 
     private static void recvItemsOnline(Player player, ItemStack items) {
         int remaining = items.getAmount();
 
         // Get maximum size per stack, then add individually
         // Prevents non-stackable items (potions, signs, boats, etc.) and semi-stackable
         // (enderpearls, eggs, snowballs, etc.) from being stacked to 64
         int stackSize;
 
         if (player.hasPermission("freetrade.bigstacks")) {
             stackSize = remaining;
         } else {
             stackSize = Math.abs(items.getType().getMaxStackSize());
             // Surprisingly, this always returns -1, see http://forums.bukkit.org/threads/getmaxstacksize-always-return-1.1154/#post-13147
             //int stackSize = Math.abs(items.getMaxStackSize());
         }
         // TODO: optional "huge stacks", beyond 64. setAmount(200) in inventory slot. Seems to work.
 
 
         do
         {
             int amount;
 
             if (remaining > stackSize) {
                 amount = stackSize;
                 remaining -= stackSize;
             } else {
                 amount = remaining;
                 remaining = 0;
             }
 
             // https://bukkit.atlassian.net/browse/BUKKIT-621
             // ItemStack cannot clone all items, uses addEnchantment instead of addUnsafeEnchantment
             //ItemStack oneStack = items.clone();
             // Workaround: clone ourselves
             ItemStack oneStack = new ItemStack(items.getTypeId(), amount, items.getDurability());
             oneStack.addUnsafeEnchantments(items.getEnchantments());
 
             // This fails with NPE on invalid items (/w !123) (TODO: bug report, null check)
             //    public void setData(int i) {
             //        this.damage = (this.id > 0) && (this.id < 256) ? Item.byId[this.id].filterData(i) : i; // CraftBukkit
             //    }
             // Item.byId isn't defined for invalid items..
             // -but- it does work for new items (CB modded with IC2, try /w !233
             HashMap<Integer,ItemStack> excess = player.getInventory().addItem(oneStack);
 
             // If player's inventory if full, drop excess items on the floor
             Iterator it = excess.entrySet().iterator();
             while (it.hasNext()) {
                 Map.Entry pair = (Map.Entry)it.next();
 
                 int unknown = ((Integer)pair.getKey()).intValue(); // hmm? always 0
                 ItemStack excessItems = (ItemStack)pair.getValue();
 
                 player.getWorld().dropItemNaturally(player.getLocation(), excessItems);
             }
         } while (remaining > 0);
 
         // How did the items transport themselves between the players? Magic, as indicated by smoke.
         // (Note only smoke on receiving end, and only if receiving player is online)
         player.playEffect(player.getLocation(), Effect.SMOKE, 0);
     }
 
 
     public boolean matchOrder(Order newOrder) {
         int i = 0;
         for (Order oldOrder: orders) {
             i += 1;
 
             //log.info("oldOrder: " + oldOrder);
             //log.info("newOrder: " + newOrder);
 
             // Are they giving what anyone else wants?
             if (!ItemQuery.isSameType(newOrder.give, oldOrder.want) ||
                 !ItemQuery.isSameType(newOrder.want, oldOrder.give)) {
                 log.info("Not matched, different types");
                 continue;
             }
 
             double newRatio = (double)newOrder.give.getAmount() / newOrder.want.getAmount();
             double oldRatio = (double)oldOrder.want.getAmount() / oldOrder.give.getAmount();
 
 
             // Offering a better or equal deal? (Quantity = relative value)
             log.info("ratio " + newRatio + " >= " + oldRatio);
             if (!(newRatio >= oldRatio)) { 
                 log.info("Not matched, worse relative value");
                 continue;
             }
 
             // TODO: refactor into ItemStackX compareTo(), so can check if item is 'better than' other item
             // Generalized to 'betterness'
 
             // Is item less damaged or equally damaged than wanted? (Durability)
             if (ItemQuery.isDurable(newOrder.give.getType())) {
                 if (newOrder.give.getDurability() > oldOrder.want.getDurability()) {
                     log.info("Not matched, worse damage new, " + newOrder.give.getDurability() + " < " + oldOrder.want.getDurability());
                     continue;
                 }
             }
             if (ItemQuery.isDurable(oldOrder.give.getType())) {
                 if (oldOrder.give.getDurability() > newOrder.want.getDurability()) {
                     log.info("Not matched, worse damage old, " + oldOrder.give.getDurability() + " < " + newOrder.want.getDurability());
                     continue;
                 }
             }
 
             // Does the item have at least the enchantments and levels that are wanted? (Enchantments)
             if (ItemQuery.isEnchantable(newOrder.give.getType())) {
                 if (!EnchantQuery.equalOrBetter(newOrder.give, oldOrder.want)) {
                     log.info("Not matched, insufficient magic new " + EnchantQuery.nameEnchs(newOrder.give.getEnchantments()) + 
                         " < " + EnchantQuery.nameEnchs(oldOrder.want.getEnchantments()));
                     continue;
                 }
             } else {
                 // Not legitimately enchantmentable, means enchant is used as a 'subtype', just like
                 // damage is if !isDurable, so match exactly.
                 if (!EnchantQuery.nameEnchs(newOrder.give.getEnchantments()).equals(
                      EnchantQuery.nameEnchs(oldOrder.want.getEnchantments()))) {
                      log.info("Not matched, non-identical enchantments new " + EnchantQuery.nameEnchs(newOrder.give.getEnchantments()) +
                      " != " + EnchantQuery.nameEnchs(oldOrder.want.getEnchantments()));
                      continue;
                 }
             }
 
             if (ItemQuery.isEnchantable(oldOrder.give.getType())) {
                 if (!EnchantQuery.equalOrBetter(oldOrder.give, newOrder.want)) {
                     log.info("Not matched, insufficient magic old " + EnchantQuery.nameEnchs(oldOrder.give.getEnchantments()) + 
                         " < " + EnchantQuery.nameEnchs(newOrder.want.getEnchantments()));
                     continue;
                 }
             } else { 
                 if (!EnchantQuery.nameEnchs(oldOrder.give.getEnchantments()).equals(
                      EnchantQuery.nameEnchs(newOrder.want.getEnchantments()))) {
                      log.info("Not matched, non-identical enchantments old " + EnchantQuery.nameEnchs(oldOrder.give.getEnchantments()) +
                      " != " + EnchantQuery.nameEnchs(newOrder.want.getEnchantments()));
                      continue;
                 }
             }        
 
         
             // Determine how much of the order can be fulfilled
             int remainingWant = oldOrder.want.getAmount() - newOrder.give.getAmount();
             int remainingGive = oldOrder.give.getAmount() - newOrder.want.getAmount();
 
             log.info("remaining want="+remainingWant+", give="+remainingGive);
 
             // They get what they want!
 
             // Calculate amount that can be exchanged
             ItemStack exchWant = new ItemStack(oldOrder.want.getType(), Math.min(oldOrder.want.getAmount(), newOrder.give.getAmount()), newOrder.give.getDurability());
             ItemStack exchGive = new ItemStack(oldOrder.give.getType(), Math.min(oldOrder.give.getAmount(), newOrder.want.getAmount()), oldOrder.give.getDurability());
             exchWant.addUnsafeEnchantments(newOrder.give.getEnchantments());
             exchGive.addUnsafeEnchantments(oldOrder.give.getEnchantments());
 
             log.info("exchWant="+ItemQuery.nameStack(exchWant));
             log.info("exchGive="+ItemQuery.nameStack(exchGive));
 
             transferItems(newOrder.player, oldOrder.player, exchWant);
             transferItems(oldOrder.player, newOrder.player, exchGive);
 
             Transaction t = new Transaction(plugin, oldOrder.player, exchWant, newOrder.player, exchGive);
 
             t.log();
 
             /*
             oldOrder.player.getInventory().addItem(exchWant);
             newOrder.player.getInventory().remove(exchWant);
             Bukkit.getServer().broadcastMessage(oldOrder.player.getDisplayName() + " received " + 
                 ItemQuery.nameStack(exchWant) + " from " + newOrder.player.getDisplayName());
 
             newOrder.player.getInventory().addItem(exchGive);
             oldOrder.player.getInventory().remove(exchGive);
             Bukkit.getServer().broadcastMessage(newOrder.player.getDisplayName() + " received " + 
                 ItemQuery.nameStack(exchGive) + " from " + oldOrder.player.getDisplayName());
             */
 
    
     
             // Remove oldOrder from orders, if complete, or add partial if incomplete
             if (remainingWant == 0) {
                 // This order is finished, old player got everything they wanted
                 // Note: remainingWant can be negative if they got more than they bargained for
                 // (other player offered a better deal than expected). Either way, done deal.
                 cancelOrder(oldOrder);
                 return true;
             } else if (remainingWant > 0) {
                 oldOrder.want.setAmount(remainingWant);
                 oldOrder.give.setAmount(remainingGive);
 
                 Bukkit.getServer().broadcastMessage("Updated order: " + oldOrder);
                 return true;
             } else if (remainingWant < 0) {
                 // TODO: test better
                 cancelOrder(oldOrder);
 
                 newOrder.want.setAmount(-remainingGive);
                 newOrder.give.setAmount(-remainingWant);
                 log.info("Adding new partial order");
                 return false;
             }
 
         }
         return false;
     }
 }
 
 // Watch trader for things they might to do invalidate their orders
 class TraderListener implements Listener
 {
     Logger log = Logger.getLogger("Minecraft");
     Market market;
     FreeTrade plugin;
 
     public TraderListener(FreeTrade pl, Market m) {
         plugin = pl;
         market = m;
         
         Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
     }
 
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerDropItem(PlayerDropItemEvent event)
     {
         // Re-validate order, see if they dropped an item they were going to give
         for (Order order: market.orders) {      // TODO: hash lookup of player? Performance
             if (order.player.equals(event.getPlayer())) {
                 if (!Market.hasItems(order.player, order.give)) {
                     if (order.player.getPlayer() != null) {
                         order.player.getPlayer().sendMessage("Order invalidated by item drop");
                     }
                     market.cancelOrder(order);
                 }
             }
         }
     }
     
     // TODO: player death, drop events
     // TODO: player an item, changes damage, or uses up (either way invalidates order)
 }
 
 public class FreeTrade extends JavaPlugin {
     Logger log = Logger.getLogger("Minecraft");
     Market market;
     YamlConfiguration config;
 
     TraderListener listener;
 
     public void onEnable() {
 
         market = new Market(this);
         loadConfig();
         market.load();
 
         listener = new TraderListener(this, market);
 
         log.info(getDescription().getName() + " enabled");
     }
 
     public void onDisable() {
         market.save();
 
         log.info(getDescription().getName() + " disabled");
     }
 
     public void loadConfig() {
         String filename = getDataFolder() + System.getProperty("file.separator") + "FreeTrade.yml";
         File file = new File(filename);
 
         if (!file.exists()) {
             if (!newConfig(file)) {
                 throw new UsageException("Could not create new configuration file");
             }
         }
 
         config = YamlConfiguration.loadConfiguration(new File(filename));
         if (config == null) {
             throw new UsageException("Failed to load configuration file " + filename);
         }
         if (config.getInt("version") < 1) {
             throw new UsageException("Configuration file version is outdated");
         }
 
         EnchantQuery.loadConfig(config);
         ItemQuery.loadConfig(config);
         market.loadConfig(config);
     }
 
     // Copy default configuration
     public boolean newConfig(File file) {
         FileWriter fileWriter;
         if (!file.getParentFile().exists()) {
             file.getParentFile().mkdir();
         }
 
         try {
             fileWriter = new FileWriter(file);
         } catch (IOException e) {
             log.severe("Couldn't write config file: " + e.getMessage());
             Bukkit.getServer().getPluginManager().disablePlugin(Bukkit.getServer().getPluginManager().getPlugin("FreeTrade"));
             return false;
         }
 
         BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(getResource("FreeTrade.yml"))));
         BufferedWriter writer = new BufferedWriter(fileWriter);
         try {
             String line = reader.readLine();
             while (line != null) {
                 writer.write(line + System.getProperty("line.separator"));
                 line = reader.readLine();
             }
             log.info("Wrote default config");
         } catch (IOException e) {
             log.severe("Error writing config: " + e.getMessage());
         } finally {
             try {
                 writer.close();
                 reader.close();
             } catch (IOException e) {
                 log.severe("Error saving config: " + e.getMessage());
                 Bukkit.getServer().getPluginManager().disablePlugin(Bukkit.getServer().getPluginManager().getPlugin("FreeTrade"));
             }
         }
         return true;
     }
 
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         Player player;
         int n = 0;
 
         if (!cmd.getName().equalsIgnoreCase("want")) {
             return false;
         }
 
         // /want
         if (args.length == 0) {
             return market.showOutstanding(sender);
         }
 
         if (sender instanceof Player) {
             player = (Player)sender;
         } else {
             // Get player name from first argument
             player = Bukkit.getServer().getPlayer(args[0]);
             if (player == null) {
                 sender.sendMessage("no such player");
                 return false;
             }
             n++;
         }
         if (args.length < 1+n) {
             return false;
         }
 
         String wantString, giveString;
         wantString = args[n];
 
         if (args.length < 2+n) {
             // Omitted last arg, use item in hand
             giveString = "this";
         } else if (args.length > 2+n) {
             // Too many args, don't try to interpret
             return false;
         } else {
             if (args[n+1].equalsIgnoreCase("for")) {
                 giveString = args[n+2];
             } else {
                 giveString = args[n+1];
             }
         }
 
         Order order;
 
         try {
             if (wantString.equals("-")) {
                 log.info("cancelall");
                 market.cancelOrder(player, null);
             } else if (giveString.equals("-")) {
                 market.cancelOrder(player, wantString);
             } else { 
                 order = new Order(player, wantString, giveString);
 
                 sender.sendMessage(order.toString());
                 market.placeOrder(order);
             }
         } catch (UsageException e) {
             log.info("Sending usage exception: " + player.getDisplayName() + " - " + e );
             player.sendMessage(e.getMessage());
             return false;
         } 
 
         return true;
     }
 }
 
 
