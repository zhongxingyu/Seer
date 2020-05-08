 
 package com.exphc.FreeTrade;
 
 import java.util.logging.Logger;
 import java.util.regex.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.concurrent.ConcurrentHashMap;
 import java.io.*;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.*;
 import org.bukkit.command.*;
 import org.bukkit.entity.*;
 import org.bukkit.inventory.*;
 import org.bukkit.enchantments.*;
 import org.bukkit.configuration.*;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.*;
 
 import info.somethingodd.bukkit.OddItem.OddItem;
 
 class ItemQuery
 {
     ItemStack itemStack;
     static Logger log = Logger.getLogger("Minecraft");
 
     // Map between item names/aliases and id;dmg string
     static ConcurrentHashMap<String,String> name2CodeName;
     static ConcurrentHashMap<String,String> codeName2Name;
 
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
 
         log.info("q="+quantityString+", name="+nameString);
 
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
 
         // TODO: wildcards, *, match one or if multiple list all matching (i.e. *pot*), for a search
 
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
 
             itemStack.setDurability(damage);
         } else {
             // If they didn't specify a durability, but they want a durable item, assume no damage (0)
             // TODO: only assume 0 for wants. For gives, need to use value from inventory! Underspecified
         }
 
         // TODO: enchantments
         if (enchString != null && !enchString.equals("")) {
             EnchantQuery enchs = new EnchantQuery(enchString);
 
             itemStack.addEnchantments(enchs.all);
         }
     }
 
     public ItemQuery(String s, Player p) {
         if (s.equals("this")) {
             itemStack = p.getItemInHand();
             if (itemStack == null) {
                 throw new UsageException("No item in hand");
             }
         } else {
             itemStack = (new ItemQuery(s)).itemStack;
         }
     }
 
 
     // Return whether an item degrades when used
     public static boolean isDurable(Material m) {
         return isTool(m) || isWeapon(m) || isArmor(m);
     }
 
 
     // Return whether the "damage"(durability) value is overloaded to mean
     // a different kind of material, instead of actual damage on a tool
     public static boolean dmgMeansSubtype(Material m) {
         // TODO: should this check be inverted, just checking for tools/weapons/armor (isDurable??) and returning true instead?
         switch (m) {
         // Blocks
         case SAPLING:
         case LEAVES:
         case LOG:
         case WOOL:
         case DOUBLE_STEP:
         case STEP:
         case SMOOTH_BRICK:
 
         // Items
         case COAL:
         case INK_SACK:
         case POTION:
         case MAP:           // for some reason not indicated on http://www.minecraftwiki.net/wiki/Data_values
         case MONSTER_EGGS:
 
         // Materials not legitimately acquirable in inventory, but here for completeness
         // (refer to Material enum, entries with MaterialData classes)
         // See http://www.minecraftwiki.net/wiki/Data_values#Data for their data value usage
 
         // Note that environmental data (age,orientation,etc.) on IDs used as both blocks
         // but not required for items does NOT cause true to be returned here.
         case WATER:                // item: WATER_BUCKET
         case STATIONARY_WATER:
         case LAVA:                 // item: LAVA_BUCKET
         case STATIONARY_LAVA:
         //case DISPENSER:          // orientation, but not relevant to item
         case BED_BLOCK:            // item: BED
         //case POWERED_RAIL:       // whether powered, but not relevant to item
         //case DETECTOR_RAIL:      // whether detecting, but not relevant to item
         case PISTON_STICKY_BASE:
         case LONG_GRASS:           // TODO: http://www.minecraftwiki.net/wiki/Data_values#Tall_Grass, can enchanted tool acquire?
         case PISTON_BASE:
         case PISTON_EXTENSION:
         //case TORCH:               // orientation, but not relevant to item
         //case WOOD_STAIRS:         // orientation, but not relevant to item   
         case REDSTONE_WIRE:         // item: REDSTONE
         case CROPS:                 // item: SEEDS
         case SOIL:                  // TODO: can silk touch acquire farmland?
         //case FURNANCE:            // orientation, but not relevant to item
         //case BURNING_FURNANCE:    // TODO: supposedly silk touch can acquire?
         case SIGN_POST:             // item: SIGN
         case WOODEN_DOOR:           // item: WOOD_DOOR (confusing!)
         //case LADDER:              // orientation, but not relevant to item
         //case RAILS:               // orientation, but not relevant to item 
         //case COBBLESTONE_STAIRS:  // orientation, but not relevant to item
         case WALL_SIGN:             // item: SIGN
         //case LEVER:               // orientation & state, but not relevant to item
         //case STONE_PLATE:         // state, but not relevant to item
         case IRON_DOOR_BLOCK:       // item: IRON_DOOR
         //case WOOD_PLATE:          // state, but not relevant to item
         case REDSTONE_TORCH_OFF:  
         //case REDSTONE_TORCH_ON:   // orientation, but not relevant to item
         //case STONE_BUTTON:        // state, but not relevant to item
         //case CACTUS:              // age, but not relevant to item
         case SUGAR_CANE_BLOCK:      // item: 338
         //case PUMPKIN:             // orientation, but not relevant to item
         //case JACK_O_LATERN:       // orientation, but not relevant to item
         case CAKE_BLOCK:            // item: CAKE
         case DIODE_BLOCK_OFF:       // item: DIODE
         case DIODE_BLOCK_ON:        // item: DIODE
         //case TRAP_DOOR:           // orientation, but not relevant to type
         //case ENDER_PORTAL_FRAME:    // TODO: has data, but no class in Bukkit? there are others
             return true;
 
         default:
             return false;
         }
     }
 
     public static boolean isTool(Material m) {
         switch (m) {
         case WOOD_PICKAXE:  case STONE_PICKAXE: case IRON_PICKAXE:  case GOLD_PICKAXE:  case DIAMOND_PICKAXE:
         case WOOD_AXE:      case STONE_AXE:     case IRON_AXE:      case GOLD_AXE:      case DIAMOND_AXE:
         case WOOD_SPADE:    case STONE_SPADE:   case IRON_SPADE:    case GOLD_SPADE:    case DIAMOND_SPADE:
         case WOOD_HOE:      case STONE_HOE:     case IRON_HOE:      case GOLD_HOE:      case DIAMOND_HOE:
         case FLINT_AND_STEEL:
         case FISHING_ROD:
             return true;
         default:
             return false;
         }
     }
 
     public static boolean isWeapon(Material m) {
         switch (m) {
         case WOOD_SWORD:    case STONE_SWORD:   case IRON_SWORD:    case GOLD_SWORD: case DIAMOND_SWORD:
         case BOW:
             return true;
         default:
             return false;
         }
     }
 
     public static boolean isArmor(Material m) {
         switch (m) {
         case LEATHER_BOOTS:     case IRON_BOOTS:      case GOLD_BOOTS:      case DIAMOND_BOOTS:
         case LEATHER_LEGGINGS:  case IRON_LEGGINGS:   case GOLD_LEGGINGS:   case DIAMOND_LEGGINGS:
         case LEATHER_CHESTPLATE:case IRON_CHESTPLATE: case GOLD_CHESTPLATE: case DIAMOND_CHESTPLATE:
         case LEATHER_HELMET:    case IRON_HELMET:     case GOLD_HELMET:     case DIAMOND_HELMET:
         // PUMPKIN is wearable on head, but isn't really armor, it doesn't take any damage
             return true;
         default:
             return false;
         }
     }
 
 
 
     public static String nameStack(ItemStack itemStack) {
         if (itemStack == null) {
             return "nothing";
         }
 
         String name, usesString, enchString;
         Material m = itemStack.getType();
        
         // If all else fails, use generic name from Bukkit
         name = itemStack.getType().toString();
 
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
        if (isDurable(m)) {
            // durable items don't have unique names for each durability
            codeName = itemStack.getTypeId() + "";
        } else {
             // durability here actually is overloaded to mean a different item
             codeName = itemStack.getTypeId() + ";" + itemStack.getDurability();
         }
 
        name = codeName2Name.get(codeName);
         if (name == null) {
             name = "unknown="+codeName;
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
 
 
     // Configuration
 
     public static void loadConfig(YamlConfiguration config) {
         Map<String,Object> configValues = config.getValues(true);
         MemorySection itemsSection = (MemorySection)configValues.get("items");
         int i = 0;
     
         name2CodeName = new ConcurrentHashMap<String, String>();
         codeName2Name = new ConcurrentHashMap<String, String>();
 
         for (String codeName: itemsSection.getKeys(false)) {
             log.info("codeName="+codeName);
 
             String properName = config.getString("items." + codeName + ".name");
 
             String obtainString = config.getString("items." + codeName + ".obtain");
             log.info("\tobtain="+obtainString);
 
             // Add aliases from config
             List<String> aliases = config.getStringList("items." + codeName + ".aliases");
             log.info("\taliases="+aliases);
             if (aliases != null) {
                 for (String alias: aliases) {
                     name2CodeName.put(alias, codeName);
                     i += 1;
                 } 
             }
 
             // Generate 'proper name' alias, preprocessed for lookup
             String smushedProperName = properName.replaceAll(" ","");
             String aliasProperName = smushedProperName.toLowerCase();
             log.info("\tname="+properName);
             name2CodeName.put(aliasProperName, codeName);
             i += 1;
             codeName2Name.put(codeName, smushedProperName);
 
             // Generate numeric alias
             name2CodeName.put(codeName, codeName);
             i += 1;
         }
         log.info("Loaded " + i + " item aliases");
 
     }
 
     // Parse a material code string with optional damage value (ex: 35;11)
     private static ItemStack codeName2ItemStack(String codeName) {
         Pattern p = Pattern.compile("^(\\d+)[;:/]?(\\d*)$");
         Matcher m = p.matcher(codeName);
         int typeCode;
         short dmgCode;
 
         if (!m.find()) {
             // This is an error in the config file (TODO: preparse or detect earlier)
             throw new UsageException("Invalid item code format: " + codeName);
         }
 
         typeCode = Integer.parseInt(m.group(1));
         if (m.group(2) != null && !m.group(2).equals("")) {
             dmgCode = Short.parseShort(m.group(2));
         } else {
             dmgCode = 0;
         }
             
         return new ItemStack(typeCode, 1, dmgCode);
     }
 
     // Get an ItemStack directly from one of its names or aliases, or null
     private static ItemStack directLookupName(String nameString) {
         String materialCode = name2CodeName.get(nameString);
 
         if (materialCode == null) {
             return null;
         }
 
         return codeName2ItemStack(materialCode);
     }
 
 }
 
 class EnchantQuery
 {
     static Logger log = Logger.getLogger("Minecraft");
 
     Map<Enchantment,Integer> all;
 
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
             EnchantmentWrapper enchWrapper = new EnchantmentWrapper(ench.getId());
 
             if (level > enchWrapper.getMaxLevel()) {
                 level = ench.getMaxLevel();
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
 
             EnchantmentWrapper ench = (EnchantmentWrapper)pair.getKey();
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
 
             EnchantmentWrapper ench = (EnchantmentWrapper)pair.getKey();
             Integer level = (Integer)pair.getValue();
 
             names.append(enchName(ench));
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
 
     static Enchantment enchFromBaseName(String n) {
         // TODO: something like OddItem for enchantment names! hideous, this
         n = n.toLowerCase();
         Enchantment ench = Enchantment.getByName(n);
         if (ench != null) {
             return ench;
         }
 
         // Armor
         if (n.equals("protection") || n.equals("p")) {
             return Enchantment.PROTECTION_ENVIRONMENTAL;
         } else if (n.equals("fire-protection") || n.equals("fireprotection") || n.equals("fire") || n.equals("fp")) {
             return Enchantment.PROTECTION_FIRE;
         } else if (n.equals("feather-falling") || n.equals("featherfalling") || n.equals("feather") || n.equals("falling") || n.equals("fall") || n.equals("ff")) {
             return Enchantment.PROTECTION_FALL;
         } else if (n.equals("blast-protection") || n.equals("blastprotection") || n.equals("blast") || n.equals("explosion-protection") || n.equals("bp")) {
             return Enchantment.PROTECTION_EXPLOSIONS;
         } else if (n.equals("projectile-protection") || n.equals("projectileprotection") || n.equals("projectile") || n.equals("bp")) {
             return Enchantment.PROTECTION_PROJECTILE;
         } else if (n.equals("respiration") || n.equals("oxygen") || n.equals("r")) {
             return Enchantment.OXYGEN; 
         } else if (n.equals("aqua-affinity") || n.equals("aquaaffinity") || n.equals("aqua") || n.equals("waterworker") || n.equals("aa")) {
             return Enchantment.WATER_WORKER;
         // Weapons
         } else if (n.equals("sharpness") || n.equals("damage-all") || n.equals("s")) {
             return Enchantment.DAMAGE_ALL;
         } else if (n.equals("smite") || n.equals("damage-undead") || n.equals("sm")) {
             return Enchantment.DAMAGE_UNDEAD;
         } else if (n.equals("bane-of-arthropods") || n.equals("bane") || n.equals("arthropods") || n.equals("b")) {
             return Enchantment.DAMAGE_ARTHROPODS;
         } else if (n.equals("knockback") || n.equals("k")) {
             return Enchantment.KNOCKBACK; 
         } else if (n.equals("fire-aspect") || n.equals("fireaspect") || n.equals("fire") || n.equals("fa")) {
             return Enchantment.FIRE_ASPECT;
         } else if (n.equals("looting") || n.equals("loot") || n.equals("loot-bonus-mobs") || n.equals("l")) {
             return Enchantment.LOOT_BONUS_MOBS;
         // Tools
         } else if (n.equals("efficiency") || n.equals("dig-speed") || n.equals("e")) {
             return Enchantment.DIG_SPEED;
         } else if (n.equals("silk-touch") || n.equals("silktouch") || n.equals("silk") || n.equals("st")) {
             return Enchantment.SILK_TOUCH;
         } else if (n.equals("unbreaking") || n.equals("durability") || n.equals("u")) {
             return Enchantment.DURABILITY;
         } else if (n.equals("fortune") || n.equals("loot-bonus-blocks") || n.equals("f")) {
             return Enchantment.LOOT_BONUS_BLOCKS;
         } else {
            throw new UsageException("Unrecognized enchantment name: " + n);
         }
     }
 
     static String enchName(EnchantmentWrapper ench) {
         switch (ench.getId()) {
         case 0: return "Protection";
         case 1: return "FireProtection";
         case 2: return "FeatherFalling";
         case 3: return "BlastProtection";
         case 4: return "ProjectileProtection";
         case 5: return "Respiration";
         case 6: return "AquaAffinity";
         case 16: return "Sharpness";
         case 17: return "Smite";
         case 18: return "BaneOfArthropods";
         case 19: return "Knockback";
         case 20: return "FireAspect";
         case 21: return "Looting";
         case 32: return "Efficiency";
         case 33: return "SilkTouch";
         case 34: return "Unbreaking";
         case 35: return "Fortune";
         default: return "Unknown(" + ench.getId() + ")";
         }
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
                 log.info("Missing enchantment " + enchName(enchB) + " not on " + itemA + " (doesn't match " + itemB + ")");
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
 }
 
 class Order
 {
     Player player;
     ItemStack want, give;
     boolean exact;
     boolean free;
 
     public Order(Player p, String wantString, String giveString) {
         player = p;
 
         if (wantString.contains("!")) {
             exact = true;
             wantString = wantString.replace("!", "");
         }
         if (giveString.contains("!")) {
             exact = true;
             giveString = giveString.replace("!", "");
         }
 
         want = (new ItemQuery(wantString, p)).itemStack;
 
         if (giveString.equals("nothing") || giveString.equals("-")) {
             // TODO: permissions
             free = true;
             give = null;
         } else {
             give = (new ItemQuery(giveString, p)).itemStack;
         }
     }
 
     public Order(Player p, ItemStack w, ItemStack g, boolean e) {
         player = p;
         want = w;
         give = g;
         exact = e;
     }
 
     public String toString() {
         // TODO: pregenerate in initialization as description, no need to relookup
         return player.getDisplayName() + " wants " + ItemQuery.nameStack(want) + " for " + ItemQuery.nameStack(give) + (exact ? " (exact)" : "");
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
 
 class Market
 {
     ArrayList<Order> orders;
     Logger log = Logger.getLogger("Minecraft");
 
     public Market() {
         // TODO: load from file, save to file
         orders = new ArrayList<Order>();
     }
 
     public boolean showOutstanding(CommandSender sender) {
         sender.sendMessage("Open orders:");
 
         for (int i = 0; i < orders.size(); i++) {
             sender.sendMessage(i + ". " + orders.get(i));
         }
 
         sender.sendMessage("To add or fulfill an order:");
 
         return false;
     }
 
     public void placeOrder(Order order) {
         if (order.free) {
             // TODO: permissions
             order.player.getInventory().addItem(order.want);
             return;
         }
 
         if (matchOrder(order)) {
             // Executed
             return;
         }
 
         // Not fulfilled; add to outstanding to match with future order 
         // Broadcast to all players so they know someone wants something, then add
         Bukkit.getServer().broadcastMessage("Wanted: " + order);
         orders.add(order);
     }
 
     public boolean matchOrder(Order newOrder) {
         for (int i = 0; i < orders.size(); i++) {
             Order oldOrder = orders.get(i);
 
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
 
             // TODO: enchantment checks
             if (!EnchantQuery.equalOrBetter(newOrder.give, oldOrder.want)) {
                 log.info("Not matched, insufficient magic new " + EnchantQuery.nameEnchs(newOrder.give.getEnchantments()) + 
                     " < " + EnchantQuery.nameEnchs(oldOrder.want.getEnchantments()));
                 continue;
             }
             if (!EnchantQuery.equalOrBetter(oldOrder.give, newOrder.want)) {
                 log.info("Not matched, insufficient magic old " + EnchantQuery.nameEnchs(oldOrder.give.getEnchantments()) + 
                     " < " + EnchantQuery.nameEnchs(newOrder.want.getEnchantments()));
                 continue;
             }
             
         
             // TODO: Generalize to "betterness"
 
 
         
             // Determine how much of the order can be fulfilled
             int remainingWant = oldOrder.want.getAmount() - newOrder.give.getAmount();
             int remainingGive = oldOrder.give.getAmount() - newOrder.want.getAmount();
 
             log.info("remaining want="+remainingWant+", give="+remainingGive);
 
             // They get what they want!
             // TODO: ensure contains() before removing
 
             // Calculate amount exchanged
             ItemStack exchWant = new ItemStack(oldOrder.want.getType(), Math.min(oldOrder.want.getAmount(), newOrder.give.getAmount()), newOrder.give.getDurability());
             ItemStack exchGive = new ItemStack(oldOrder.give.getType(), Math.min(oldOrder.give.getAmount(), newOrder.want.getAmount()), oldOrder.give.getDurability());
 
             exchWant.addEnchantments(newOrder.give.getEnchantments());
             exchGive.addEnchantments(oldOrder.give.getEnchantments());
 
             log.info("exchWant="+ItemQuery.nameStack(exchWant));
             log.info("exchGive="+ItemQuery.nameStack(exchGive));
 
             oldOrder.player.getInventory().addItem(exchWant);
             newOrder.player.getInventory().remove(exchWant);
             Bukkit.getServer().broadcastMessage(oldOrder.player.getDisplayName() + " received " + 
                 ItemQuery.nameStack(exchWant) + " from " + newOrder.player.getDisplayName());
 
             newOrder.player.getInventory().addItem(exchGive);
             oldOrder.player.getInventory().remove(exchGive);
             Bukkit.getServer().broadcastMessage(newOrder.player.getDisplayName() + " received " + 
                 ItemQuery.nameStack(exchGive) + " from " + oldOrder.player.getDisplayName());
 
     
             // Remove oldOrder from orders, if complete, or add partial if incomplete
             if (remainingWant == 0) {
                 // This order is finished, old player got everything they wanted
                 // Note: remainingWant can be negative if they got more than they bargained for
                 // (other player offered a better deal than expected). Either way, done deal.
                 orders.remove(oldOrder);
                 log.info("Closed order " + oldOrder);
                 return true;
             } else if (remainingWant > 0) {
                 oldOrder.want.setAmount(remainingWant);
                 oldOrder.give.setAmount(remainingGive);
 
                 Bukkit.getServer().broadcastMessage("Updated order: " + oldOrder);
                 return true;
             } else if (remainingWant < 0) {
                 orders.remove(oldOrder);
                 orders.remove("Closed order " + oldOrder);
 
                 newOrder.want.setAmount(-remainingGive);
                 newOrder.give.setAmount(-remainingWant);
                 log.info("Adding new partial order");
                 return false;
             }
 
         }
         return false;
     }
 }
 
 public class FreeTrade extends JavaPlugin {
     Logger log = Logger.getLogger("Minecraft");
     Market market = new Market();
 
     public void onEnable() {
         loadConfig();
         log.info(getDescription().getName() + " enabled");
     }
 
     public void onDisable() {
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
 
         YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(filename));
         if (config == null) {
             throw new UsageException("Failed to load configuration file " + filename);
         }
         if (config.getInt("version") < 1) {
             throw new UsageException("Configuration file version is outdated");
         }
 
 
         ItemQuery.loadConfig(config);
 
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
         if (args.length < 2+n) {
             return false;
         }
 
         String wantString, giveString;
         wantString = args[n];
         if (args[n+1].equalsIgnoreCase("for")) {
             giveString = args[n+2];
         } else {
             giveString = args[n+1];
         }
 
         Order order;
 
         try {
             order = new Order(player, wantString, giveString);
         } catch (UsageException e) {
             log.info("Sending usage exception: " + player.getDisplayName() + " - " + e );
             player.sendMessage(e.getMessage());
             return false;
         } 
 
         sender.sendMessage(order.toString());
         market.placeOrder(order);
 
         return true;
     }
 }
 
 
