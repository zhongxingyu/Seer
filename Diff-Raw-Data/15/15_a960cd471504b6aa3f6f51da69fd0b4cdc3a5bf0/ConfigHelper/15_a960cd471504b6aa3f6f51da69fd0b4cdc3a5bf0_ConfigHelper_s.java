 package bencvt.minecraft.server.extrafluffy;
 
 import java.util.ArrayList;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.bukkit.Material;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.EntityType;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * Validate and further parse config.yml into data structures convenient for the plugin.
  */
 public class ConfigHelper
 {
     public class ExtraDropAmount {
         /** Probability that any extra items will be dropped at all. 0.0:never, 1.0:always. */
         public double chance;
         /** If extra items are to be dropped, how many (lower bound, inclusive). */
         public int min;
         /** If extra items are to be dropped, how many (upper bound, inclusive). */
         public int max;
         /** Whether to group all the extra items into a single item stack entity. */
         public boolean stack;
     }
 
     public class ExtraDeathLoot {
         public EntityType entityType;
         public ItemStack item;
         public ExtraDropAmount dropAmount;
     }
 
     public ExtraDropAmount EXTRA_SHEAR_WOOL;
     public ExtraDropAmount EXTRA_SPLIT_SLIME_BALLS;
     public Set<EntityType> CLEAR_DEATH_LOOT;
     public List<ExtraDeathLoot> EXTRA_DEATH_LOOT;
 
     public void load(ConfigurationSection mainSection) {
 
         EXTRA_SHEAR_WOOL = getExtraDropAmount(mainSection.getConfigurationSection("extra-shear-wool"));
 
         EXTRA_SPLIT_SLIME_BALLS = getExtraDropAmount(mainSection.getConfigurationSection("extra-split-slime-balls"));
 
         CLEAR_DEATH_LOOT = new LinkedHashSet<EntityType>();
         for (String entityTypeName : mainSection.getStringList("clear-death-loot")) {
             CLEAR_DEATH_LOOT.add(matchEntityType(entityTypeName));
         }
 
         EXTRA_DEATH_LOOT = new ArrayList<ExtraDeathLoot>();
         ConfigurationSection extraDeathLootSection = mainSection.getConfigurationSection("extra-death-loot");
         for (String subSectionName : extraDeathLootSection.getKeys(false)) {
             ConfigurationSection subSection = extraDeathLootSection.getConfigurationSection(subSectionName);
             ExtraDeathLoot lootInfo = new ExtraDeathLoot();
             lootInfo.entityType = matchEntityType(subSectionName);
             lootInfo.item = subSection.getItemStack("item");
             if (lootInfo.item == null) {
                 // The node isn't a full serialized ItemStack. Assume it's a scalar
                 // and simply match it to a material type, either name or id.
                lootInfo.item = new ItemStack(Material.matchMaterial(subSection.get("item").toString()));
             }
             if (lootInfo.item == null) {
                 throw new IllegalArgumentException(subSectionName + ": invalid item " + String.valueOf(subSection.get("item")));
             }
             lootInfo.dropAmount = getExtraDropAmount(subSection);
             EXTRA_DEATH_LOOT.add(lootInfo);
         }
     }
 
     private ExtraDropAmount getExtraDropAmount(ConfigurationSection subSection) {
         ExtraDropAmount result = new ExtraDropAmount();
        result.chance = subSection.getDouble("chance");
         result.min = subSection.getInt("min");
         result.max = subSection.getInt("max");
         result.stack = subSection.getBoolean("stack");
        if (!subSection.isDouble("chance") || result.chance < 0.0 || result.chance > 1.0) {
             throw new IllegalArgumentException("chance: " + String.valueOf(subSection.get("chance")) + " is not a number between 0.0 and 1.0");
         }
         if (!subSection.isInt("min")) {
             throw new IllegalArgumentException("min: " + String.valueOf(subSection.get("min")) + " is not an integer");
         }
         if (!subSection.isInt("max")) {
             throw new IllegalArgumentException("max: " + String.valueOf(subSection.get("max")) + " is not an integer");
         }
         if (!subSection.isBoolean("stack")) {
             throw new IllegalArgumentException("stack: " + String.valueOf(subSection.get("stack")) + " is not an boolean");
         }
         return result;
     }
 
     private static EntityType matchEntityType(String value) {
         int sep = value.indexOf("-");
         if (sep >= 0) {
             value = value.substring(0, sep);
         }
         EntityType result = EntityType.fromName(value);
         if (result == null) {
             // accept aliases
             if (value.equalsIgnoreCase("MagmaCube")) {
                 return EntityType.MAGMA_CUBE; // "LavaSlime"
             } else if (value.equalsIgnoreCase("Mooshroom")) {
                 return EntityType.MUSHROOM_COW; // "MushroomCow"
             } else if (value.equalsIgnoreCase("Ocelot")) {
                 return EntityType.OCELOT; // "Ozelot"
             } else if (value.equalsIgnoreCase("SnowGolem")) {
                 return EntityType.SNOWMAN; // "SnowMan"
             } else if (value.equalsIgnoreCase("IronGolem")) {
                 return EntityType.IRON_GOLEM; // "VillagerGolem"
             } else if (value.equalsIgnoreCase("Player")) {
                 return EntityType.PLAYER; // no registered name
             }
             try {
                 result = EntityType.fromId(Integer.parseInt(value));
             } catch (NumberFormatException e) {
                 // do nothing
             }
         }
         if (result == null) {
             throw new IllegalArgumentException(value + " does not match an entity type");
         }
         return result;
     }
 
     private static String getEntityTypeName(EntityType entityType) {
         String result = entityType.getName();
         if (result == null) {
             result = entityType.toString().toLowerCase();
             result = result.substring(0, 1).toUpperCase() + result.substring(1);
         }
         return result;
     }
 
     public void logSummary(Logger log) {
         log.info("Sheared sheep have a " +
                 yPercentChanceOfDroppingZ(EXTRA_SHEAR_WOOL) +
                 " extra wool blocks.");
 
         log.info("Large slimes have a " +
                 yPercentChanceOfDroppingZ(EXTRA_SPLIT_SLIME_BALLS) +
                 " slime balls when splitting.");
 
         for (EntityType entityType : CLEAR_DEATH_LOOT) {
             log.info(getEntityTypeName(entityType) + "s do not drop their normal items.");
         }
 
         for (ExtraDeathLoot lootInfo : EXTRA_DEATH_LOOT) {
             log.info(getEntityTypeName(lootInfo.entityType) + "s have a " +
                     yPercentChanceOfDroppingZ(lootInfo.dropAmount) + " " +
                     lootInfo.item + "s.");
         }
     }
 
     private static String yPercentChanceOfDroppingZ(ExtraDropAmount dropAmount) {
         return String.valueOf(dropAmount.chance*100.0) + "% chance of dropping " +
                 dropAmount.min + " to " + dropAmount.max + " " +
                 (dropAmount.stack ? "stacked" : "individual");
     }
 }
