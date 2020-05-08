 package com.undeadscythes.udsplugin;
 
 import com.undeadscythes.udsplugin.utilities.*;
 import java.util.*;
 import org.apache.commons.lang.*;
 import org.bukkit.*;
 import org.bukkit.configuration.file.*;
 import org.bukkit.entity.*;
 import org.bukkit.inventory.*;
 
 /**
  * Collection of values taken from the plugin's configuration file.
  * 
  * @author UndeadScythes
  */
 public class Config {
     public static boolean BLOCK_CREEPERS;
     public static boolean BLOCK_ENDERMEN;
     public static boolean BLOCK_SILVERFISH;
     public static boolean BLOCK_TNT;
     public static boolean BLOCK_WITHER;
     public static byte MAP_DATA;
     public static int BASE_COST;
     public static int BUILD_COST;
     public static int CITY_COST;
     public static int CLAN_COST;
     public static int EXPAND_COST;
     public static int HOME_COST;
     public static int MAP_COST;
     public static int SHOP_COST;
     public static int VIP_COST;
     public static int UNDO_COUNT;
     public static int DRAIN_RANGE;
     public static int MOVE_RANGE;
     public static int EDIT_RANGE;
     public static int COMPASS_RANGE;
     public static int BUTCHER_RANGE;
     public static int VIP_SPAWNS;
     public static int WORLD_BORDER;
     public static int WORLD_BORDER_SQRD;
     public static int SPAWNER_EXP;
     public static long REQUEST_TTL;
     public static long MINECART_TTL;
     public static long PVP_TIME;
     public static long SLOW_TIME;
     public static long DRAGON_RESPAWN;
     public static long VIP_TIME;
     public static String CURRENCIES;
     public static String WELCOME_MSG;
     public static String WELCOME_ADMIN;
     public static String SERVER_OWNER;
     public static String CURRENCY;
     public static String MAIN_WORLD;
     public static Material WELCOME_GIFT;
     public static List<String> SERVER_RULES;
     public static ArrayList<Material> VIP_WHITELIST;
     public static ArrayList<Kit> KITS;
     public static EnumMap<EntityType, Integer> MOB_REWARDS;
     public static double PISTON_POWER;
     public static EnumMap<RegionFlag, Boolean> GLOBAL_FLAGS;
     public static List<String> SHARES;
     public static String GMAIL_ADDRESS;
     public static double SKULL;
     public static String GMAIL_PASSWORD;
     
     public static void saveDefaults() {
         UDSPlugin.getPlugin().saveDefaultConfig();
     }
     
     public static void copyDefaults() {
         UDSPlugin.getPlugin().getConfig().options().copyDefaults(true);
     }
     
     public static void save() {
         UDSPlugin.getPlugin().saveConfig();
     }
     
     public static void init() {
         saveDefaults();
         copyDefaults();
         save();
         VIP_WHITELIST = new ArrayList<Material>(29);
         MOB_REWARDS = new EnumMap<EntityType, Integer>(EntityType.class);
         GLOBAL_FLAGS = new EnumMap<RegionFlag, Boolean>(RegionFlag.class);
         KITS = new ArrayList<Kit>(3);
         load();
     }
     
     public static void load() {
         final FileConfiguration config = UDSPlugin.getPlugin().getConfig();
         BLOCK_CREEPERS = config.getBoolean("block.creeper");
         BLOCK_ENDERMEN = config.getBoolean("block.endermen");
         BLOCK_SILVERFISH = config.getBoolean("block.silverfish");
         BLOCK_TNT = config.getBoolean("block.tnt");
         BLOCK_WITHER = config.getBoolean("block.wither");
         MAP_DATA = (byte)config.getInt("map-data");
         BASE_COST = config.getInt("cost.base");
         BUILD_COST = config.getInt("cost.build");
         CITY_COST = config.getInt("cost.city");
         CLAN_COST = config.getInt("cost.clan");
         EXPAND_COST = config.getInt("cost.expand");
         HOME_COST = config.getInt("cost.home");
         MAP_COST = config.getInt("cost.map");
         SHOP_COST = config.getInt("cost.shop");
         VIP_COST = config.getInt("cost.vip");
         UNDO_COUNT = config.getInt("range.undo");
         DRAIN_RANGE = config.getInt("range.drain");
         MOVE_RANGE = config.getInt("range.move");
         EDIT_RANGE = config.getInt("range.edit");
         COMPASS_RANGE = config.getInt("range.compass");
         BUTCHER_RANGE = config.getInt("range.butcher");
         VIP_SPAWNS = config.getInt("vip.spawns");
         WORLD_BORDER = config.getInt("range.world");
         WORLD_BORDER_SQRD = WORLD_BORDER * WORLD_BORDER;
         SPAWNER_EXP = config.getInt("exp.spawner");
         REQUEST_TTL = config.getLong("request-timeout") * TimeUtils.SECOND;
         MINECART_TTL = config.getLong("minecart.life") * TimeUtils.SECOND;
         PVP_TIME = config.getLong("pvp-time") * TimeUtils.SECOND;
         SLOW_TIME = config.getLong("auto-save") * TimeUtils.MINUTE;
         DRAGON_RESPAWN = config.getLong("respawn-dragon") * TimeUtils.MINUTE;
         VIP_TIME = config.getLong("vip.time") * TimeUtils.DAY;
         CURRENCIES = config.getString("currency.plural");
         WELCOME_MSG = config.getString("welcome.message");
         WELCOME_ADMIN = config.getString("welcome.admin");
         SERVER_OWNER = config.getString("server-owner");
         CURRENCY = config.getString("currency.singular");
         MAIN_WORLD = config.getString("world-name");
         WELCOME_GIFT = Material.getMaterial(config.getString("welcome.gift"));
         SERVER_RULES = config.getStringList("server-rules");
         PISTON_POWER = config.getDouble("piston-power");
         SHARES = config.getStringList("inventory-shares");
         GMAIL_ADDRESS = config.getString("gmail.email");
         SKULL = config.getDouble("head-drop-chance");
         GMAIL_PASSWORD = config.getString("gmail.password");
         VIP_WHITELIST.clear();
         for(int typeId : config.getIntegerList("item-whitelist")) {
             VIP_WHITELIST.add(Material.getMaterial(typeId));
         }
         KITS.clear();
         for(String kit : config.getStringList("kits")) {
             final String[] kitSplit = kit.split(",");
             final List<ItemStack> items = new ArrayList<ItemStack>(ArrayUtils.subarray(kitSplit, 3, kitSplit.length -1).length);
             for(Object item : ArrayUtils.subarray(kitSplit, 3, kitSplit.length -1)) {
                 items.add(new ItemStack(Material.getMaterial(Integer.parseInt((String)item))));
             }
             KITS.add(new Kit(kitSplit[0], Integer.parseInt(kitSplit[1]), items, PlayerRank.getByName(kitSplit[2])));
         }
         MOB_REWARDS.clear();
         for(EntityType entityType : EntityType.values()) {
             String entityName = "mob-rewards." + entityType.getName();
             if(entityName != null) {
                 entityName = entityName.toLowerCase();
                 MOB_REWARDS.put(entityType, config.getInt(entityName));
             }
         }
         GLOBAL_FLAGS.clear();
         for(RegionFlag flag : RegionFlag.values()) {
             final String flagname = "global-flags." + flag.toString().toLowerCase();
             GLOBAL_FLAGS.put(flag, config.getBoolean(flagname));
         }
     }
     
     public static void reload() {
         UDSPlugin.getPlugin().reloadConfig();
         load();
     }
     
     private Config() {}
 }
