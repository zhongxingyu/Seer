 /*
 Copyright (c) 2012, Mushroom Hostage
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
     * Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.
     * Neither the name of the <organization> nor the
       names of its contributors may be used to endorse or promote products
       derived from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
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
 import java.util.Properties;
 import java.lang.reflect.Field;
 import java.sql.Timestamp;
 import java.io.*;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.*;
 import org.bukkit.command.*;
 import org.bukkit.entity.*;
 import org.bukkit.inventory.*;
 import org.bukkit.enchantments.*;
 import org.bukkit.configuration.*;
 import org.bukkit.configuration.file.*;
 import org.bukkit.event.player.*;
 import org.bukkit.event.entity.*;
 import org.bukkit.event.inventory.*;
 import org.bukkit.event.*;
 import org.bukkit.block.*;
 import org.bukkit.*;
 
 import info.somethingodd.bukkit.OddItem.OddItem;
 
 import org.bukkit.craftbukkit.CraftServer;
 
 // Offline player loading
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.server.EntityPlayer;
 import net.minecraft.server.ItemInWorldManager;
 
 // Native item names
 import net.minecraft.server.Item;
 //import net.minecraft.server.ItemStack; // not imported, but used below
 import net.minecraft.server.LocaleLanguage;
 
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
     static ConcurrentHashMap<Material,Boolean> isntEnchantableMap;  // if present, overrides isDurable
     static ConcurrentHashMap<Material,Boolean> isCountableMap;
 
     static FreeTrade plugin;
 
     public ItemQuery(String s) {
         Pattern p = Pattern.compile(
             "^(\\d*)" +             // quantity
             "([# :.-]?)" +         // separator / stack flag
             "([^/\\\\]*)" +         // name
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
 
         // Bare "quantity", no name, like "52"
         // Use this as the raw name instead (but for most power, prefix with "." for empty quantity)
         if (nameString.equals("")) {
             nameString = quantityString;
             quantityString = "";
         }
 
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
             // This will either return 1 match, or raise usage exception if 0 or >1
             nameString = wildcardMatchOne(nameString);
         }
         // First try built-in name lookup
         itemStack = directLookupName(nameString);
 
         if (itemStack == null) {
             // try "raw item" name, code name (like 52 = monster spawner)
             try {
                 itemStack = ItemQuery.codeName2ItemStack(nameString);
             } catch (UsageException e) {
                 // try augumented raw item name, like 17;2
                 try {
                     itemStack = ItemQuery.codeName2ItemStack(quantityString + nameString);
                     quantity = 1;
                 } catch (UsageException e2) {
                     // fall through
                 }
             }
         }
         
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
                     // Last ditch effort, try wildcard - this will raise exception if 0 or >1 match
                     nameString = wildcardMatchOne("*" + nameString + "*");
                     itemStack = directLookupName(wildcardMatchOne(nameString));
 
                     //throw new UsageException("Unrecognized item name: " + nameString + " (no suggestions available)");
                 } else {
                     itemStack = new ItemStack(material);
                 }
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
         // Note, we call native max damage method, not trusting Bukkit
         short maxDamage = (short)getMaxDamage(itemStack.getTypeId());
         
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
             log.info("set damage = "+itemStack.getDurability());
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
 
         int id = m.getId();
 
         if (id == 0) {
             return false;   // air doesn't have an item
         }
 
         // Native methods support durable custom items, unlike Bukkit
         // see also http://forums.bukkit.org/threads/suggestion-org-bukkit-material-isdurable.57910/ for if Bukkit adds this
         // Test with ArmorBronzeHelmet
         return getNativeItem(id).g();   // MCP isDamageable() = maxDamage > 0 && !hasSubtypes
     }
 
     // Get max durability for an item ID
     public static int getMaxDamage(int id) {
         if (id == 0) {
             return -1;
         }
 
         // Bukkit doesn't know about custom items, will get e.g. IC2 armorbronzehelmet wrong
         //return m.getMaxDurability() 
         net.minecraft.server.Item item = getNativeItem(id);
         if (item == null) {
             // Possibly not an item.. 4096 fix + Jammy Furniture Mod, arm chair 701, block not item
             return -1;
         } else {
             return item.getMaxDurability();
         }
     }
 
     // Get the real deal, not fake wrappers that don't know anything
     public static net.minecraft.server.Item getNativeItem(int id) {
         net.minecraft.server.Item item = net.minecraft.server.Item.byId[id];
 
         if (item == null) {
             log.info("no item for id "+id);
         }
 
         return item;
     }
    
     // Return whether an item can be legitimately enchanted
     public static boolean isEnchantable(Material m) {
         // Usually, durable implies enchantability
         // exceptions in vanilla: shears, fishing rods, flint & steel, and hoes 
         // but also see EnchantMore
         return isDurable(m) && !isntEnchantableMap.containsKey(m);
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
                 int maxDamage = getMaxDamage(itemStack.getTypeId());
                 percentage = (int)((maxDamage - itemStack.getDurability()) * 100.0 / maxDamage);
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
     // TODO: I really don't like this
     public static boolean isTradable(ItemStack items) {
         // Durability always stored, but 0 for durable items
         String codeName = items.getTypeId() + ";" + (isDurable(items.getType()) ? 0 : items.getDurability());
 
         return isTradable(codeName);
 
     }
     public static boolean isTradable(String codeName) {
         Object obj = isTradableMap.get(codeName);
         if (obj == null) {
             return false;
         }
 
         Boolean bool = (Boolean)obj;
         return bool.booleanValue();
     }
 
 
     // Configuration
 
     public static int loadItems(FileConfiguration config, ConcurrentHashMap<String,Boolean> isTradableMapUnfiltered, HashSet<Obtainability> tradableCategories) {
         int i = 0;
         
         Map<String,Object> configValues = config.getValues(true);
         MemorySection itemsSection = (MemorySection)configValues.get("items");
 
         if (itemsSection == null) {
             return 0;
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
                     putItemAlias(alias, codeName);
                     i += 1;
                 } 
             }
 
             // Generate 'proper name' alias, preprocessed for lookup
             i += 1;
             putItem(getSmushedName(properName), codeName);
 
             // TODO: store somewhere? used to be used for durability, no longer
             // might be cool for plugins to ask "isSword()", and work with custom items
             String purpose = config.getString("items." + codeName + ".purpose");
 
             // Items are enchantable if durable, unless overridden (for shears, etc.)
             Material material = codeName2ItemStack(codeName).getType();
             if (!config.getBoolean("items." + codeName + ".enchant", true)) {
                 isntEnchantableMap.put(material, new Boolean(false));
             }
    
             if (config.getBoolean("items." + codeName + ".count", false)) {
                 isCountableMap.put(material, new Boolean(true));
             }
         }
 
         return i;
     }
 
     public static void loadConfig(YamlConfiguration config) {
         int i = 0;
     
         name2CodeName = new ConcurrentHashMap<String, String>();
         codeName2Name = new ConcurrentHashMap<String, String>();
         
         isntEnchantableMap = new ConcurrentHashMap<Material, Boolean>();
         isCountableMap = new ConcurrentHashMap<Material, Boolean>();
         isTradableMap = new ConcurrentHashMap<String, Boolean>();
         ConcurrentHashMap<String,Boolean> isTradableMapUnfiltered = new ConcurrentHashMap<String, Boolean>();
 
         HashSet<Obtainability> tradableCategories = new HashSet<Obtainability>();
 
         for (String obtainString: config.getStringList("tradableCategories")) {
             tradableCategories.add(Obtainability.valueOf(obtainString.toUpperCase()));
         }
 
         // Vanilla items
         i += loadItems(config, isTradableMapUnfiltered, tradableCategories);
         log.info("Loaded " + i + " vanilla item aliases");
 
         // Extra items
         File extraFile = new File(plugin.getDataFolder(), "extra.yml");
         if (config.getBoolean("scanNativeItems", true) && !extraFile.exists()) {
             scanNativeItems(config);    
         }
         if (extraFile.exists()) {
             FileConfiguration extraConfig = YamlConfiguration.loadConfiguration(extraFile);
             if (extraConfig == null) {
                 log.info("Failed to load extra.yml");
             } else {
                 i += loadItems(extraConfig, isTradableMapUnfiltered, tradableCategories);
             }
         } 
        
         log.info("Loaded total " + i + " item aliases");
 
         // Whitelist tradable items
         for (String whiteString: config.getStringList("tradableWhitelist")) {
             ItemStack itemStack = directLookupName(whiteString);
 
             isTradableMapUnfiltered.put(itemStack.getTypeId() + ";" + itemStack.getDurability(), true);
         }
 
 
         // Filter through blacklist
         SKIP: for (String tradableCodeName: isTradableMapUnfiltered.keySet()) {
             // Is this blacklisted?
             for (String blackString: config.getStringList("tradableBlacklist")) {
                 ItemStack itemStack = directLookupName(blackString);
 
                 if (tradableCodeName.equals(itemStack.getTypeId() + ";" + itemStack.getDurability())) {
                     isTradableMap.put(tradableCodeName, false);
                     continue SKIP;
                 }
             }
 
             // No, add to real list
             isTradableMap.put(tradableCodeName, true);
         }
     }
 
     // Return normalized name smushed together, i.e. Nether Wart => NetherWart
     // You need to lowercase it yourself if you want to
     public static String getSmushedName(String name) {
         return name.replaceAll("[ _-]", "");
     }
 
     // Load native item names from net.minecraft.server
     // Useful for mods that add new items
     private static void scanNativeItems(YamlConfiguration config) {
         // Write new custom 'extra items' config file
         File extraFile = new File(plugin.getDataFolder(), "extra.yml");
         FileConfiguration extraConfig = YamlConfiguration.loadConfiguration(extraFile);
 
         HashMap<String,String> properNames = new HashMap<String,String>();
 
         int count = 0;
 
 
         // Language translation file for native item names
         Properties translateTable = null;
         try {
             // MCP "StringTranslate"
             net.minecraft.server.LocaleLanguage localeLanguage = net.minecraft.server.LocaleLanguage.a(); // singleton instance
 
             Field translateTableField = net.minecraft.server.LocaleLanguage.class.getDeclaredField("b");
             translateTableField.setAccessible(true);
 
             translateTable = (Properties)translateTableField.get(localeLanguage);
         } catch (Exception e) {
             log.info("Failed to get translateTable: " + e);
             e.printStackTrace();
             return;
         }
         //log.info("translateTable = "+translateTable);
         //System.exit(0);
 
 
         // MCP calls this "itemsList"
         net.minecraft.server.Item[] itemsById = net.minecraft.server.Item.byId;
 
         int start = config.getInt("scanNativeItemsBlockStart", Material.DRAGON_EGG.getId() + 1);  // first after dragon egg TODO: 1.2: redstone lamp 124
         log.info("Starting scan at id "+start);
 
 
         // TODO: ModLoaderMP mods org.bukkit.Material, lookupId (was byId) and lookupName (was BY_NAME)!
         // See http://forums.bukkit.org/threads/give-and-material-java.59789/#post-965633
         // This is the source of the "Adding Material" and "Aliasing material" messages!
         // We should try to use it!  see .patch at http://minecraft.maeyanie.com/
         // addMaterial(int id) adds id with name "X" + id, not much use,
         // addMaterial(id,name), setMaterialName() are more usefue
         for (int id = start; id < itemsById.length; id += 1) {
             // skip vanilla items
             if (id == config.getInt("scanNativeItemsLastBlock", 255)) {
                 id = config.getInt("scanNativeItemsItemStart", 383 + 1);   // spawn egg TODO: 1.2 update for past Fire Charge
                 log.info("skipped to "+id);
             }
             if (id == config.getInt("scanNativeItemsSkipDisc", 2256)) {
                 log.info("skipped to "+id);
                 id = config.getInt("scanNativeItemsSkipDiscEnd", 2266 + 1);
             }
 
             net.minecraft.server.Item item = itemsById[id];
             if (item == null) {
                 continue;
             }
 
             String name = item.l(); // getStatName()
 
             if (name == null) {
                 continue;
             }
             // TODO: also get aliases, ModLoaderMP loads them
             
             String properName = getNormalizedNativeName(name, id, -1, translateTable);
 
             /*
             TODO: if has subtypes, we should add those as separate items!!
             problem is, Item only knows about top-level items (like INK_SACK aka dyePowder)
             each subclass (ItemDye, etc.) defines its subtype damage values
             */
 
             // Is damage used for subtypes?
             // item.g() // isDamageable() = maxDamage > 0 && !hasSubtypes
 
             if (item instanceof net.minecraft.server.ItemBlock) {
                 net.minecraft.server.ItemBlock itemBlock = (net.minecraft.server.ItemBlock)item;
                 log.info("scanning item block: " + id); // + " (max="+item.getMaxDurability());
 
                 // Block metadata is 4 bits, but item data is 16 bits. block 136 = elorram.base.BlockMicro, thousands! scan (almost) all
                 String priorNativeName = null;
                 //for (int data = 0; data < 16; data += 1) {
 
                 // start damage value scanning here.. this technically should be the most negative number, but, 
                 // mods hardly (ever?) use negative damage values (do any?) - so cut scan time in half by starting at 0.
                 // (Curiously, negative damage values work in vanilla items.. negative damage wool, still colored, but different stacking)
                 int damageStart = config.getInt("scanNativeItemsDamageStart", 0); 
                 //int damageStart = config.getInt("scanNativeItemsDamageStart", Short.MIN_VALUE); 
 
                 // Scan until the end if we can
                 int damageEnd = config.getInt("scanNativeItemsDamageEnd", Short.MAX_VALUE);
 
                 // If we detect we're unhelpfully getting foo.1, foo.2, foo.3, names, infinitely, then stop scanning once we reach
                 // this number. BC-IC2 Crossover and Forestry have this problem - but the names show up fine on the client (and in NEI).
                 // TODO: report/fix bugs in those mods? would be nice to have automatic server-side human-readable names!
                 // meanwhile, the server admin has to edit the config and fix it themselves manually
                 int damageSequenceLimit = config.getInt("scanNativeItemsDamageSequenceLimit", 10);
 
 
                 for (int data = damageStart; data < damageEnd; data += 1) {
                     net.minecraft.server.ItemStack is = new net.minecraft.server.ItemStack(id, 1, data);
 
                     String nativeName;
                     try {
                         nativeName = itemBlock.a(is);
                     } catch (IndexOutOfBoundsException e) {
                         // RP2, for example, throws this on 144 with data >4
                         // Wish more mods did this
                         continue;
                     } catch (NullPointerException e) {
                         // Railcraft for 1.2.5 throws NPE on block 253
                         continue;
                     }
 
                     if (nativeName == null) {
                         // null name likely means no block
                         continue;
                     }
                     if (priorNativeName != null && nativeName.equals(priorNativeName)) {
                         // we're likely at the tail end of the block metadata
                         // and further metadata will probably return the same values
                         // (most likely)
                         break;
                     }
 
                     properName = getNormalizedNativeName(nativeName, id, data, translateTable);
                     //log.info("\t"+data+" = "+properName);
                     if (damageSequenceLimit != -1 && data == damageSequenceLimit) {
                         // we're getting unhelpful foo.1, foo.2... names, to infinite (BC-IC2 Crossover and Forestry)
                         // they could go on forever, so stop here, if enabled
                         if (properName.endsWith("x" + damageSequenceLimit)) {
                             log.info("Sequential item name detected - "+properName+" ("+nativeName+") - limited scan, please review results");
                             break;
                         }
                     }
 
 
                     String codeName = id + ";" + data;
 
                     if (properNames.containsKey(properName)) {
                         // Only allow one of each name - although it may miss some items with identical names
                         if (config.getBoolean("scanNativeItemsLogDupe", false)) {
                             log.info("dupe "+properName+" is "+codeName+" and was "+properNames.get(properName));
                         }
                         if (config.getBoolean("scanNativeItemsContinueDupe", true)) {
                             continue;
                         } else {
                             break;
                         }
                     }
 
                     /* this never happens
                     if (codeName2Name.containsKey(codeName)) {
                         log.info("alias " + codeName + " = " + properName + " (" + codeName2Name.get(codeName));
                     }
                     */
 
                     // Add to config
                     properNames.put(properName, codeName);
                     //extraConfig.set("items." + codeName + ".name", properName);
                     count += 1;
                     //extraItems.set(codeName + ".source", );   // TODO: get from mod?
 
                     putItem(properName, codeName);
                     isTradableMap.put(codeName, true);  // TODO: control tradeability of custom items?
                 }
             }
 
             else if (item.e()) {    // getHasSubTypes(), accesses hasSubtypes (obfuscated bR)
                 // TODO: why doesn't this ever get called??
 
                 for (int damage = 0; damage < item.getMaxDurability(); damage += 1) {
                     String codeName = id + ";" + damage;
 
                     // To get subtype name we have to create a native ItemStack
                     net.minecraft.server.ItemStack is = new net.minecraft.server.ItemStack(id, 1, damage);
                     // getItemNameIS(ItemStack)
                     String nativeName = item.a(is);
                     properName = getNormalizedNativeName(nativeName, id, damage, translateTable);
 
                     putItem(properName, codeName);
                     // TODO: restrict native items from mods?
                     isTradableMap.put(codeName, true);
                 }
             } else {
                 String codeName = String.valueOf(id);
 
                 //extraConfig.set("items." + codeName + ".name", properName);
                 properNames.put(properName, codeName);
                 count += 1;
                 putItem(properName, codeName);
                 // TODO: control tradeability?
                 isTradableMap.put(codeName + ";0", true);   // always stores durability
             }
         }
 
         // Items were stored by proper name to de-dupe
         // Build config
         for (Map.Entry<String, String> entry : properNames.entrySet()) {
             String properName = entry.getKey();
             String codeName = entry.getValue();
 
             extraConfig.set("items." + codeName + ".name", properName);
         }
 
         count = properNames.size();
 
         plugin.log.info("saving "+count+" items");
         try {
             // Note: YamlConfiguration doesn't scale. hangs on 2820732 items
             extraConfig.save(extraFile);
         } catch (Exception e) {
             plugin.log.info("Failed to save extra items file: " + e);
         }
         plugin.log.info("saved extra items");
 
         //System.exit(-1);
     }
 
     // Make us aware of an item name/code mapping
     // @param properName    Properly capitalized and spaced human-readable name
     // @param codeName      Code string recognizable by codeName2ItemStack
     private static boolean putItem(String properName, String codeName) {
         //log.info("id "+codeName+" = "+properName);
 
         // TODO: detect conflicts!
         name2CodeName.put(properName.toLowerCase(), codeName);
         codeName2Name.put(codeName, properName);
 
         return true;
     }
 
     // Add another name for an existing item (putItem)
     private static boolean putItemAlias(String aliasName, String codeName) {
         // TODO: detect conflicts!
         name2CodeName.put(aliasName.toLowerCase(), codeName);
 
         return true;
     }
 
     // Get a semi-human-readable name from localized nms Item name
     private static String getNormalizedNativeName(String name, int id, int damage, Properties nativeTranslateTable) {
         if (name == null || name.equals("") || name.equals("null.name")) {
             // some blocks like 97 'silverfish block' don't have names
             if (damage == -1) {
                 return "id"+id;
             } else {
                 return "id"+id+"x"+damage;
             }
         }
 
         String key = name + ".name";
         if (nativeTranslateTable.containsKey(key)) {
             // RedPower awesomely provides human-readable names for us, via redpower.lang
             return getSmushedName(nativeTranslateTable.getProperty(key));
         }
         // IC2/BC2 isn't so nice (TODO: why not? bug?)
         //log.info("no translated name for "+name);
 
         // l() tries to localize name (item.foo.name) to human-readable string, but
         // doesn't always succeed for new items. TODO: why not? displays on client just fine
         name = name.replaceFirst("^item\\.", "");
         name = name.replaceFirst("\\.name$", "");
         // or for built-in items not meant to be displayed in-game
         name = name.replaceFirst("^tile\\.", "");
         // note: do not replace 'block' prefix, since some mods use to distinguish vs items
 
         // another level of prefixing used in IC2
         name = name.replaceFirst("^item", "");
         // and suffixing in BC
         name = name.replaceFirst("Item$", "");
 
         // Initial case for mods that don't (most do)
         if (Character.isLowerCase(name.charAt(0))) {
             name = Character.toTitleCase(name.charAt(0)) + name.substring(1);
         }
 
         // To make names alphabetic (and consistency with id###x###), replace .foo with xfoo 
         // Forestry - Machine.1 -> Machinex1, BC-IC2 no name id#x#
         name = name.replaceFirst("\\.", "x");
 
         // TODO: marbleBrick => Marble Brick (autospacing, un-camel-case) - note - not actually needed for RP2 since it has string tables!
 
 
         return getSmushedName(name);
     }
 
     // Parse a material code string with optional damage value (ex: 35;11)
     // This can create arbitrary item stacks -- it doesn't lookup known items
     public static ItemStack codeName2ItemStack(String codeName) {
         Pattern p = Pattern.compile("^(\\d+)[;:/]?([\\d-]*)([+]?.*)$");
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
             // TODO: is this signed? or unsigned? cloth.black vs blackwool
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
 
             if (matchesWildcard(pattern, name)) {
                 if (isTradable(codeName) || isTradable(codeName + ";0")) {   // sorry
                     results.add(codeName2Name.get(codeName).replace(" ",""));
                 } else {
                     log.info("matched but not tradable: " + pattern + " = " + name);
                 }
             }
         }
 
         return results;
     }
 
     // Lookup exactly one item name using a wildcard expression
     // Returns the name string if 1 matched
     // Otherwise throws an exception, 0 or >1 matched
     private static String wildcardMatchOne(String nameString) {
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
         return results.first().toLowerCase();
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
 
         String[] enchStrings = allString.split("[, /]+");
         for (String enchString: enchStrings) {
             // ench/lvl is a short, so support up to any level, either positive or negative
             // Note that we also support level 0 (allowed in nms, not Bukkit API though, interpreted as not enchanted)
             // TODO: also support enchanted items but with no effect (ench tag, empty = glowing but no text)
             // see http://dev.bukkit.org/server-mods/enchanter/forum/32858-suggestion-enchantment-levels-127/#p1
             Pattern p = Pattern.compile("^([A-Za-z-]*[a-z])([IV0-9-]*)$");
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
             String smushedProperName = ItemQuery.getSmushedName(properName);
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
 
         want = (new ItemQuery(wantString, p)).itemStack;
 
         give = (new ItemQuery(giveString, p)).itemStack;
 
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
         return Market.getPlayerDisplayName(player) + " wants " + ItemQuery.nameStack(want) + " for " + ItemQuery.nameStack(give);
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
         }
     }
 
     public boolean showOutstanding(CommandSender sender) {
         sender.sendMessage("Open orders:");
            
         int i = 0;
         for (Order order: orders) {
             i += 1;
             sender.sendMessage(i + ". " + order);
         }
 
         sender.sendMessage("To cancel type /want -, or to add/fulfill an order:");
         sender.sendMessage("(examples: 64coal, #coal, dpick, dpick/100%, dpick/st, etc.)");
 
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
         Bukkit.getServer().broadcastMessage("Type /want to see all offers, or to accept: ");
         Bukkit.getServer().broadcastMessage(" hold " + ItemQuery.nameStack(order.want) + ", then type /want "+ItemQuery.nameStack(order.give));
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
             throw new UsageException("Player " + Market.getPlayerDisplayName(fromPlayer) + " doesn't have " + ItemQuery.nameStack(items));
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
                 throw new UsageException("Player " + Market.getPlayerDisplayName(fromPlayer) + " doesn't have enough " + ItemQuery.nameStack(items) + ", missing " + missing + ", reverted");
             } else {
                 throw new UsageException("Player " + Market.getPlayerDisplayName(fromPlayer) + "  could not have items taken ("+takeException.getMessage()+"), reverted");
             }
         }
 
         try {
             recvItems(toPlayer, items);
         } catch (Exception recvException) {
             // Give back
             // TODO: this needs to be BULLETPROOF to avoid item duping
             recvItems(fromPlayer, items);
 
             throw new UsageException("Player " + Market.getPlayerDisplayName(toPlayer) + " could not receive items ("+recvException.getMessage()+"), reverted");
         }
 
         Bukkit.getServer().broadcastMessage(Market.getPlayerDisplayName(toPlayer) + " received " + 
             ItemQuery.nameStack(items) + " from " + Market.getPlayerDisplayName(fromPlayer));
     }
 
     // Get an offline player preferred name, their display name if they're online
     public static String getPlayerDisplayName(OfflinePlayer player) {
         if (player.getPlayer() != null) {
             return player.getPlayer().getDisplayName();
         } else {
             return player.getName();
         }
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
             player = loadOfflinePlayer(offlinePlayer);
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
 
             // Remove oldOrder from orders, if complete, or add partial if incomplete
             if (remainingWant == 0) {
                 // This order is finished, old player got everything they wanted
                 log.info("remainingWant=0, this order is finished");
                 cancelOrder(oldOrder);
                 return true;
             } else if (remainingWant > 0) {
                 // They still want more. Update the partial order.
                 oldOrder.want.setAmount(remainingWant);
                 oldOrder.give.setAmount(remainingGive);
 
                 Bukkit.getServer().broadcastMessage("Updated order: " + oldOrder);
                 log.info("remainingWant>0, still want more");
                 return true;
             }
             // remainingWant can be negative if they got more than they bargained for
             // (other player offered a better deal than expected). Either way, done deal.
 
             else if (remainingWant < 0) {
                 if (remainingGive < 0) {
                     // test case:
                     //       w foo 10d 10cobble
                     //       w bar 6cobble 10d
                     // ->
                     // close w foo 6d 6cobble
                     // new   w foo 4d 4cobble
                     // (and reverse)
                     cancelOrder(oldOrder);
 
                     newOrder.want.setAmount(-remainingGive);
                     newOrder.give.setAmount(-remainingWant);
                     log.info("remainingWant<0, Adding new partial order (remainingWant="+remainingWant+")");
                     log.info("remainingGive<0 ="+remainingGive);
                     return false;
                 } else {
                     // test case:
                     // w foo 1d 64cobble
                     // w bar 1cobble 64diamond
                     // Offered 64cob, but got it for 1cob. Better deal than expected. But everyone got what they want.
                     cancelOrder(oldOrder);
 
                     log.info("remainingWant<0, but remainingGive="+remainingGive+", got better deal than expected, closing");
                     return true;
 
                 }
             }
 
         }
         return false;
     }
 
     // Check player's orders for if they still have what they are offering
     public void revalidateOrders(Player player, String reason) {
         for (Order order: this.orders) {      // TODO: hash lookup of player? Performance
             if (order.player.equals(player)) {
                 if (!Market.hasItems(order.player, order.give)) {
                     if (order.player.getPlayer() != null) {
                         order.player.getPlayer().sendMessage(reason + ": " + order);
                     }
                     this.cancelOrder(order);
                 }
             }
         }
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
         market.revalidateOrders(event.getPlayer(), "Order invalidated by item drop");
     }
     
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerDeath(PlayerDeathEvent event) {
         final Player player = event.getEntity();
 
         // Run after event fires so player is dead and loses items
         Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
             public void run() {
                 market.revalidateOrders(player, "Order invalidated by death");
             }
         });
     }
 
     @EventHandler(priority = EventPriority.NORMAL) 
     public void onInventoryClose(InventoryCloseEvent event) {
         HumanEntity human = event.getPlayer();
         if (!(human instanceof Player)) {
             return;
         }
 
         Player player = (Player)human;
 
         market.revalidateOrders(player, "Order invalidated inventory change");
     }
 
     // TODO: check when player uses an item, so damage changes, or uses it up and breaks it
     // (either way invalidates order)
     // unfortunately, not easy to detect, don't want to check on every item use either
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
     }
 
     public void onDisable() {
         market.save();
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
 
         ItemQuery.plugin = this;
 
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
 
 
