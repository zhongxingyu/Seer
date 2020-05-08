 package com.norcode.bukkit.enhancedfishing;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.bukkit.block.Biome;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Fish;
 import org.bukkit.entity.Player;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 import org.bukkit.plugin.PluginManager;
 
 public class WorldConfiguration {
     private static final String ENV_MODIFIER_BOAT = "environmental.boat-modifier";
     private static final String ENV_MODIFIER_CROWDING = "environmental.crowding-modifier";
     private static final String ENV_CONFIG_CROWDING_RADIUS = "environmental.crowding-radius";
     private static final String ENV_MODIFIER_MOBS = "environmental.mobs-modifier";
     private static final String ENV_CONFIG_MOBS_RADIUS = "environmental.mobs-radius";
     private static final String ENV_MODIFIER_RAIN = "environmental.rain-modifier";
     private static final String ENV_MODIFIER_LIGHTNING = "environmental.lightning-modifier";
     private static final String ENV_CONFIG_LIGHTNING_RADIUS = "environmental.lightning-radius";
     private static final String ENV_MODIFIER_SUNRISE = "environmental.sunrise-modifier";
     private static final String ENV_CONFIG_SUNRISE_START = "environmental.sunrise-start";
     private static final String ENV_CONFIG_SUNRISE_END = "environmental.sunrise-end";
     
     private static final String ENCH_MODIFIER_EFFICIENCY_LEVEL = "enchantments.efficiency-level-modifier";
     private static final String ENCH_FORTUNE_LEVEL_CHANCE = "enchantments.fortune-level-chance";
     private static final String ENCH_LOOTING_LEVEL_CHANCE = "enchantments.looting-level-chance";
     private static final String ENCH_EFFICIENCY_ENABLED = "enchantments.efficiency";
     private static final String ENCH_FORTUNE_ENABLED = "enchantments.fortune";
     private static final String ENCH_LOOTING_ENABLED = "enchantments.looting";
     private static final String ENCH_THORNS_ENABLED = "enchantments.thorns";
     private static final String ENCH_FIRE_ASPECT_ENABLED = "enchantments.fire-aspect";
     
     private EnhancedFishing plugin;
     private String world;
     private HashMap<Permission, Double> loadedPermissions;
     private LootTable lootTable;
     private boolean enabled;
     private double baseCatchChance;
     private DoubleModifier sunriseModifier;
     private double sunriseStart;
     private double sunriseEnd;
     private DoubleModifier rainModifier;
     private DoubleModifier crowdingModifier;
     private double crowdingRadius;
 
     private DoubleModifier mobsModifier;
     private double mobsRadius;
 
     private DoubleModifier lightningModifier;
     private double lightningRadius;
 
     private DoubleModifier boatModifier;
 
     private HashMap<Biome, DoubleModifier> biomeModifiers = new HashMap<Biome, DoubleModifier>();
     private boolean efficiencyEnabled; // Better Odds
     private DoubleModifier efficiencyLevelModifier;
     private boolean lootingEnabled;    // Catch treasure instead of fish
     private double lootingLevelChance;
     private boolean fortuneEnabled;    // catch multiple fish at once
     private double fortuneLevelChance;
     private boolean fireAspectEnabled; // catch cooked fish
     private boolean thornsEnabled;      // deal damage when hook hits mobs.
 
     public WorldConfiguration(EnhancedFishing plugin, String world) {
         this.world = world;
         this.plugin = plugin;
         this.loadedPermissions = new HashMap<Permission, Double>();
         this.lootTable = new LootTable(plugin, world);
         this.loadConfig();
     }
 
 
     public boolean isEnabled() {
         return enabled;
     }
     
     public double getBaseCatchChance() {
         return baseCatchChance;
     }
 
     public double getBaseCatchChance(Player p) {
         double chance = getBaseCatchChance();
         if (!this.world.equals("default")) {
             chance = plugin.getDefaultConfiguration().getBaseCatchChance(p);
         }
         for (Permission node: loadedPermissions.keySet()) {
             if (p.hasPermission(node)) {
                 chance = loadedPermissions.get(node);
             }
         }
         return chance;
     }
 
     public DoubleModifier getSunriseModifier() {
         return sunriseModifier;
     }
 
     public double getSunriseStart() {
         return sunriseStart;
     }
 
     public double getSunriseEnd() {
         return sunriseEnd;
     }
 
     public DoubleModifier getRainModifier() {
         return rainModifier;
     }
 
     public DoubleModifier getCrowdingModifier() {
         return crowdingModifier;
     }
 
     public double getCrowdingRadius() {
         return crowdingRadius;
     }
 
     public DoubleModifier getMobsModifier() {
         return mobsModifier;
     }
 
     public double getMobRadius() {
         return mobsRadius;
     }
 
     public DoubleModifier getLightningModifier() {
         return lightningModifier;
     }
 
 
     public double getLightningRadius() {
         return lightningRadius;
    }
 
     public DoubleModifier getBoatModifier() {
         return boatModifier;
     }
 
     public DoubleModifier getBiomeModifier(Biome b) {
         DoubleModifier mod = biomeModifiers.get(b);
         if (mod == null) {
             return plugin.getDefaultConfiguration().getBiomeModifier(b);
         }
         return mod;
     }
 
     public DoubleModifier getEfficiencyLevelModifier() {
         return efficiencyLevelModifier;
     }
 
     public boolean isEfficiencyEnabled() {
         return efficiencyEnabled;
     }
 
     public double getLootingLevelChance() {
         return lootingLevelChance;
     }
 
     public double getFortuneLevelChance() {
         return fortuneLevelChance;
     }
 
     public boolean isLootingEnabled() {
         return lootingEnabled;
     }
 
     public boolean isFortuneEnabled() {
         return fortuneEnabled;
     }
 
     public boolean isFireAspectEnabled() {
         return fireAspectEnabled;
     }
 
     public boolean isThornsEnabled() {
         return thornsEnabled;
     }
 
     public LootTable getLootTable() {
         return lootTable;
     }
 
     public ConfigurationSection getConfig() {
        return plugin.getConfig().getConfigurationSection(this.world.toLowerCase());
     }
 
     private ConfigurationSection getDefaultConfig() {
         return plugin.getConfig().getConfigurationSection("default");
     }
 
     private void loadConfig() {
         PluginManager pm = plugin.getServer().getPluginManager();
         for (Permission p: loadedPermissions.keySet()) {
             pm.removePermission(p);
         }
         loadedPermissions.clear();
         lootTable.reload();
         enabled = getConfig().getBoolean("enabled", getDefaultConfig().getBoolean("enabled", true));
         if (!enabled) return;
         baseCatchChance = getConfig().getDouble("bite-chance.default", getDefaultConfig().getDouble("bite-chance.default", 0.002));
         ConfigurationSection sec = getConfig().getConfigurationSection("bite-chance");
         if (sec == null) {
             sec = getDefaultConfig().getConfigurationSection("bite-chance");
         }
         if (sec != null) {
             for (String nodename: sec.getKeys(false)) {
                 if (nodename != "default") {
                     String name = "enhancedfishing.bite-chance." + nodename.toLowerCase();
                     Permission node = pm.getPermission(name);
                     if (node == null) {
                         node = new Permission(name,PermissionDefault.FALSE);
                         pm.addPermission(node);
                     }
                     loadedPermissions.put(node, sec.getDouble(nodename.toLowerCase()));
                     node.recalculatePermissibles();
                 }
             }
         }
         boatModifier = new DoubleModifier(getConfig().getString(ENV_MODIFIER_BOAT, getDefaultConfig().getString(ENV_MODIFIER_BOAT)));
         crowdingModifier = new DoubleModifier(getConfig().getString(ENV_MODIFIER_CROWDING, getDefaultConfig().getString(ENV_MODIFIER_CROWDING)));
         crowdingRadius = getConfig().getDouble(ENV_CONFIG_CROWDING_RADIUS, getDefaultConfig().getDouble(ENV_CONFIG_CROWDING_RADIUS));
         mobsModifier = new DoubleModifier(getConfig().getString(ENV_MODIFIER_MOBS, getDefaultConfig().getString(ENV_MODIFIER_MOBS)));
         mobsRadius = getConfig().getDouble(ENV_CONFIG_MOBS_RADIUS, getDefaultConfig().getDouble(ENV_CONFIG_MOBS_RADIUS));
         rainModifier = new DoubleModifier(getConfig().getString(ENV_MODIFIER_RAIN, getDefaultConfig().getString(ENV_MODIFIER_RAIN)));
         lightningModifier = new DoubleModifier(getConfig().getString(ENV_MODIFIER_LIGHTNING, getDefaultConfig().getString(ENV_MODIFIER_LIGHTNING)));
         lightningRadius = getConfig().getDouble(ENV_CONFIG_LIGHTNING_RADIUS, getDefaultConfig().getDouble(ENV_CONFIG_LIGHTNING_RADIUS));
         sunriseModifier = new DoubleModifier(getConfig().getString(ENV_MODIFIER_SUNRISE, getDefaultConfig().getString(ENV_MODIFIER_SUNRISE)));
         sunriseStart = getConfig().getDouble(ENV_CONFIG_SUNRISE_START, getDefaultConfig().getDouble(ENV_CONFIG_SUNRISE_START));
         sunriseEnd = getConfig().getDouble(ENV_CONFIG_SUNRISE_END, getDefaultConfig().getDouble(ENV_CONFIG_SUNRISE_END));
         efficiencyLevelModifier = new DoubleModifier(getConfig().getString(ENCH_MODIFIER_EFFICIENCY_LEVEL, getDefaultConfig().getString(ENCH_MODIFIER_EFFICIENCY_LEVEL)));
         efficiencyEnabled = getConfig().getBoolean(ENCH_EFFICIENCY_ENABLED, getDefaultConfig().getBoolean(ENCH_EFFICIENCY_ENABLED, true));
         lootingEnabled = getConfig().getBoolean(ENCH_LOOTING_ENABLED, getDefaultConfig().getBoolean(ENCH_LOOTING_ENABLED, true));
         lootingLevelChance = getConfig().getDouble(ENCH_LOOTING_LEVEL_CHANCE, getDefaultConfig().getDouble(ENCH_LOOTING_LEVEL_CHANCE, 0.15));
         fortuneEnabled = getConfig().getBoolean(ENCH_FORTUNE_ENABLED, getDefaultConfig().getBoolean(ENCH_FORTUNE_ENABLED, true));
         fortuneLevelChance = getConfig().getDouble(ENCH_FORTUNE_LEVEL_CHANCE, getDefaultConfig().getDouble(ENCH_FORTUNE_LEVEL_CHANCE, 0.15));
         fireAspectEnabled = getConfig().getBoolean(ENCH_FIRE_ASPECT_ENABLED, getDefaultConfig().getBoolean(ENCH_FIRE_ASPECT_ENABLED, true));
         thornsEnabled = getConfig().getBoolean(ENCH_THORNS_ENABLED, getDefaultConfig().getBoolean(ENCH_THORNS_ENABLED, true));
         ConfigurationSection bSec = getConfig().getConfigurationSection("biomes");
         if (bSec == null) {
             bSec = getDefaultConfig().getConfigurationSection("biomes");
         }
         for (String biomeName: bSec.getKeys(false)) {
             Biome b = Biome.valueOf(biomeName.toUpperCase());
             if (b == null) {
                 plugin.getLogger().warning("Unknown biome specified in configuration for world " + world + ": " + biomeName);
             } else {
                 biomeModifiers.put(b, new DoubleModifier(bSec.getString(biomeName, getDefaultConfig().getString("biomes." + biomeName))));
             }
         }
     }
 
 }
