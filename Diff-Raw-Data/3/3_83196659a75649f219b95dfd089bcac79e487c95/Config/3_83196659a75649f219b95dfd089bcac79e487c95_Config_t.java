 package com.undeadscythes.udsplugin;
 
 import java.util.*;
 import org.apache.commons.lang.*;
 import org.bukkit.*;
 import org.bukkit.configuration.file.*;
 import org.bukkit.entity.*;
 import org.bukkit.inventory.*;
 
 /**
  * Storage of config values to help aid maintenance.
  * @author UndeadScythes
  */
 public final class Config {
     public static boolean blockEndermen;
     public static boolean blockSilverfish;
     public static boolean blockCreepers;
     public static boolean blockTNT;
     public static boolean blockWither;
     public static byte mapData;
     public static int undoCount;
     public static int compassRange;
     public static int drainRange;
     public static int expandCost;
     public static int mapCost;
     public static int homeCost;
     public static int shopCost;
     public static int vipCost;
     public static int clanCost;
     public static int baseCost;
     public static int cityCost;
     public static int buildCost;
     public static int spawnerEXP;
     public static int moveRange;
     public static int editRange;
     public static int butcherRange;
     public static int worldBorder;
     public static int worldBorderSq;
     public static int vipSpawns;
     public static long minecartTTL;
     public static long dragonRespawn;
     public static long pvpTime;
     public static long requestTTL;
     public static long vipTime;
     public static long slowTime;
     public static String currency;
     public static String currencies;
     public static String serverOwner;
     public static String welcome;
     public static String welcomeAdmin;
     public static World mainWorld;
     public static Material welcomeGift;
     public static List<String> serverRules;
     public static List<Material> whitelistVIP;
     public static List<Kit> kits;
     public static Map<RegionFlag, Boolean> globalFlags;
     public static Map<String, Integer> mobRewards;
 
     public final static List<Material> WATER = new ArrayList<Material>(Arrays.asList(Material.WATER, Material.STATIONARY_WATER));
     public final static List<Material> RAILS = new ArrayList<Material>(Arrays.asList(Material.RAILS, Material.POWERED_RAIL, Material.DETECTOR_RAIL));
     public final static org.bukkit.util.Vector HALF_BLOCK = new org.bukkit.util.Vector(.5, .5, .5);
     public static final List<EntityType> HOSTILE_MOBS = new ArrayList<EntityType>(Arrays.asList(EntityType.BLAZE, EntityType.CAVE_SPIDER, EntityType.CREEPER, EntityType.ENDERMAN, EntityType.ENDER_DRAGON, EntityType.GHAST, EntityType.MAGMA_CUBE, EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SLIME, EntityType.SPIDER, EntityType.WITCH, EntityType.WITHER, EntityType.ZOMBIE));
     public static final List<EntityType> PASSIVE_MOBS = new ArrayList<EntityType>(Arrays.asList(EntityType.BAT, EntityType.CHICKEN, EntityType.COW, EntityType.MUSHROOM_COW, EntityType.OCELOT, EntityType.PIG, EntityType.SHEEP, EntityType.SQUID, EntityType.VILLAGER));
     public static final List<EntityType> NEUTRAL_MOBS = new ArrayList<EntityType>(Arrays.asList(EntityType.IRON_GOLEM, EntityType.PIG_ZOMBIE, EntityType.SNOWMAN, EntityType.WOLF));
     public final static String INT_REGEX = "[0-9][0-9]*";
 
     private static UDSPlugin plugin;
 
     /**
      * Load the online 'easy-access' config class with values from the file on disk.
      * @param config
      */
     public static void loadConfig(final UDSPlugin plugin) {
         Config.plugin = plugin;
         plugin.saveDefaultConfig();
         plugin.getConfig();
         plugin.reloadConfig();
         final FileConfiguration config = plugin.getConfig();
         undoCount = config.getInt("range.undo");
         buildCost = config.getInt("cost.build");
         currencies = config.getString("currency.plural");
         requestTTL = config.getLong("request-timeout") * Timer.SECOND;
         slowTime = config.getLong("auto-save") * Timer.MINUTE;
         vipSpawns = config.getInt("vip.spawns");
         vipTime = config.getLong("vip.time") * Timer.DAY;
         welcome = config.getString("welcome.message");
         welcomeGift = Material.getMaterial(config.getString("welcome.gift").toUpperCase());
         worldBorder = config.getInt("range.world");
         worldBorderSq = (int)Math.pow(worldBorder, 2);
         welcomeAdmin = config.getString("welcome.admin");
         butcherRange = (int)Math.pow(config.getInt("range.butcher"), 2);
         pvpTime = config.getLong("pvp-time") * Timer.SECOND;
         spawnerEXP = config.getInt("exp.spawner");
         serverOwner = config.getString("server-owner");
         dragonRespawn = config.getLong("respawn-dragon") * Timer.MINUTE;
         cityCost = config.getInt("cost.city");
         currency = config.getString("currency.singular");
         mapCost = config.getInt("cost.map");
         homeCost = config.getInt("cost.home");
         shopCost = config.getInt("cost.shop");
         vipCost = config.getInt("cost.vip");
         clanCost = config.getInt("cost.clan");
         baseCost = config.getInt("cost.base");
         drainRange = config.getInt("range.drain");
         moveRange = config.getInt("range.move");
         editRange = config.getInt("range.edit");
         whitelistVIP = new ArrayList<Material>();
         for(int id : config.getIntegerList("item-whitelist")) {
             whitelistVIP.add(Material.getMaterial(id));
         }
         kits = new ArrayList<Kit>();
         for(String kit : config.getStringList("kits")) {
             final String[] kitSplit = kit.split(",");
             final List<ItemStack> items = new ArrayList<ItemStack>();
             for(Object item : ArrayUtils.subarray(kitSplit, 3, kitSplit.length -1)) {
                 items.add(new ItemStack(Material.getMaterial(Integer.parseInt((String)item))));
             }
             kits.add(new Kit(kitSplit[0], Integer.parseInt(kitSplit[1]), items, PlayerRank.getByName(kitSplit[2])));
         }
         expandCost = config.getInt("cost.expand");
         mapData = (byte)config.getInt("map-data");
         serverRules = new ArrayList<String>(config.getStringList("server-rules"));
         blockCreepers = config.getBoolean("block.creeper");
         blockTNT = config.getBoolean("block.tnt");
         blockWither = config.getBoolean("block.wither");
         blockEndermen = config.getBoolean("block.enderman");
         blockSilverfish = config.getBoolean("block.silverfish");
         mobRewards = new HashMap<String, Integer>();
         for(String reward : config.getStringList("mob-rewards")) {
             mobRewards.put(reward.split(":")[0], Integer.parseInt(reward.split(":")[1]));
         }
         compassRange = config.getInt("range.compass");
         globalFlags = new HashMap<RegionFlag, Boolean>();
         for(RegionFlag flag : RegionFlag.values()) {
            String flagname = "global-flags." + flag.toString().toLowerCase();
            globalFlags.put(flag, config.getBoolean(flagname));
         }
         mainWorld = Bukkit.getWorld(config.getString("world-name"));
         minecartTTL = config.getLong("minecart.life") * Timer.SECOND;
     }
 
     /**
      *
      */
     public static void updateConfig() {
         plugin.getConfig().options().copyDefaults(true);
         plugin.saveConfig();
         loadConfig(plugin);
     }
 
     /**
      *
      */
     public static void reload() {
         loadConfig(plugin);
     }
 
     private Config() {}
 }
