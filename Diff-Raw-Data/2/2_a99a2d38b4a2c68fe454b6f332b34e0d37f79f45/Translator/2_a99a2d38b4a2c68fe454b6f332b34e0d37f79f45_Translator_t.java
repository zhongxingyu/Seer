 package edgruberman.bukkit.obituaries;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockState;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.ThrownPotion;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.potion.Potion;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 import org.bukkit.potion.PotionType;
 
 /**
  * Language manager.
  */
 class Translator {
 
     final Map<DamageCause, String> deaths = new HashMap<DamageCause, String>();
     final Map<EntityType, String> entities = new HashMap<EntityType, String>();
     final Map<EntitySubtype, String> entitySubtypes = new HashMap<EntitySubtype, String>();
     final Map<String, String> owners = new HashMap<String, String>();
 
     final Map<EntityType, String> itemDefaults = new HashMap<EntityType, String>();
     final String itemFormat;
 
     final String potionFormatBase;
     final String potionFormatLevel;
     final String potionFormatExtended;
     final String potionFormatThrown;
     final String potionFormatDrunk;
     final Map<PotionEffectType, String> potionEffects = new HashMap<PotionEffectType, String>();
     final Map<PotionType, String> potionTypes = new HashMap<PotionType, String>();
 
     final String enchanted;
 
     private final Plugin plugin;
 
     /**
      * Associates a material id (int) and damage value (short) to text.
      * null damage values apply to any damage value, but a direct damage value
      * match will override.
      */
     private final Map<Integer, Map<Short, String>> materials = new HashMap<Integer, Map<Short, String>>();
 
     Translator(final Plugin plugin, final ConfigurationSection config) {
         this.plugin = plugin;
         if (config == null) {
             this.itemFormat = null;
             this.enchanted = null;
             this.potionFormatBase = null;
             this.potionFormatLevel = null;
             this.potionFormatExtended = null;
             this.potionFormatThrown = null;
             this.potionFormatDrunk = null;
             return;
         }
 
         this.loadDeaths(config.getConfigurationSection("deaths"));
         this.loadMaterial(config.getConfigurationSection("materials"));
         this.loadEntities(config.getConfigurationSection("entities"));
         this.loadEntitySubtypes(config.getConfigurationSection("entitySubtypes"));
         this.loadOwners(config.getConfigurationSection("owners"));
 
         this.loadDefaultItems(config.getConfigurationSection("item.defaults"));
         this.itemFormat = config.getString("item.format");
         this.plugin.getLogger().config("Item Format: " + this.itemFormat);
         this.enchanted = config.getString("enchanted");
         this.plugin.getLogger().config("Enchanted: " + this.enchanted);
 
         this.potionFormatBase = config.getString("potion.format.base");
         this.potionFormatLevel = config.getString("potion.format.level");
         this.potionFormatExtended = config.getString("potion.format.extended");
         this.plugin.getLogger().config("Potion Format; Base: " + this.potionFormatBase + "; Level: " + this.potionFormatLevel + "; Extended: " + this.potionFormatExtended);
         this.potionFormatThrown = config.getString("potion.format.thrown");
         this.potionFormatDrunk = config.getString("potion.format.drunk");
         this.plugin.getLogger().config("Potion Format; Thrown: " + this.potionFormatThrown + "; Drunk: " + this.potionFormatDrunk);
         this.loadPotionEffects(config.getConfigurationSection("potion.effects"));
         this.loadPotionTypes(config.getConfigurationSection("potion.types"));
     }
 
     private void loadDefaultItems(final ConfigurationSection config) {
         this.itemDefaults.clear();
         if (config == null) return;
 
         for (final String description : config.getKeys(false)) {
             EntityType type;
             try {
                 type = EntityType.valueOf(description);
             } catch (final IllegalArgumentException e) {
                 this.plugin.getLogger().warning("Unable to identify EntityType " + config.getCurrentPath() + ": " + description + "; " + e.getMessage());
                 continue;
             }
 
             this.itemDefaults.put(type, config.getString(description));
         }
 
         this.plugin.getLogger().config(this.itemDefaults.size() + " default item name(s) loaded");
     }
 
     private void loadDeaths(final ConfigurationSection config) {
         this.deaths.clear();
         if (config == null) return;
 
         for (final String name : config.getKeys(false)) {
             DamageCause cause;
             try {
                 cause = DamageCause.valueOf(name);
             } catch (final IllegalArgumentException e) {
                 this.plugin.getLogger().warning("Unable to identify DamageCause " + config.getCurrentPath() + ": " + name);
                 continue;
             }
 
             this.deaths.put(cause, config.getString(cause.name()));
         }
 
         this.plugin.getLogger().config(this.deaths.size() + " death message format(s) loaded");
     }
 
     private void loadOwners(final ConfigurationSection config) {
         this.owners.clear();
         if (config == null) return;
 
         for (final String key : config.getKeys(false))
             this.owners.put(key, config.getString(key));
 
         this.plugin.getLogger().config(this.owners.size() + " owner format(s) loaded");
     }
 
     private void loadPotionEffects(final ConfigurationSection config) {
         this.potionEffects.clear();
         if (config == null) return;
 
         for (final String key : config.getKeys(false)) {
             final PotionEffectType effect = PotionEffectType.getByName(key);
             if (effect == null) {
                 this.plugin.getLogger().warning("Unable to identify PotionEffectType " + config.getCurrentPath() + ": " + key);
                 continue;
             }
 
             this.potionEffects.put(effect, config.getString(key));
         }
         this.plugin.getLogger().config(this.potionEffects.size() + " potion effect name(s) loaded");
     }
 
     private void loadPotionTypes(final ConfigurationSection config) {
         this.potionTypes.clear();
         if (config == null) return;
 
         for (final String name : config.getKeys(false)) {
             final PotionType type;
             try {
                  type = PotionType.valueOf(name);
             } catch (final IllegalArgumentException e) {
                 this.plugin.getLogger().warning("Unable to identify PotionType " + config.getCurrentPath() + ": " + name);
                 continue;
             }
 
             this.potionTypes.put(type, config.getString(name));
         }
         this.plugin.getLogger().config(this.potionEffects.size() + " potion type name(s) loaded");
     }
 
     private void loadEntities(final ConfigurationSection config) {
         this.entities.clear();
         if (config == null) return;
 
         for (final String name : config.getKeys(false)) {
             EntityType type;
             try {
                 type = EntityType.valueOf(name);
             } catch (final IllegalArgumentException e) {
                 this.plugin.getLogger().warning("Unable to identify EntityType " + config.getCurrentPath() + ": " + name + "; " + e.getMessage());
                 continue;
             }
 
             this.entities.put(type, config.getString(name));
         }
 
         this.plugin.getLogger().config(this.entities.size() + " entity name(s) loaded");
     }
 
     private void loadEntitySubtypes(final ConfigurationSection config) {
         this.entitySubtypes.clear();
         if (config == null) return;
 
         for (final String name : config.getKeys(false)) {
             EntitySubtype subtype;
             try {
                 subtype = EntitySubtype.valueOf(name);
             } catch (final IllegalArgumentException e) {
                 this.plugin.getLogger().warning("Unable to identify EntitySubtype " + config.getCurrentPath() + ": " + name + "; " + e.getMessage());
                 continue;
             }
 
             this.entitySubtypes.put(subtype, config.getString(name));
         }
 
         this.plugin.getLogger().config(this.entitySubtypes.size() + " entity subtype name(s) loaded");
     }
 
     private void loadMaterial(final ConfigurationSection config) {
         this.materials.clear();
         if (config == null) return;
 
         for (final String key : config.getKeys(false)) {
             String name = key;
 
             // Separate and parse damage value if it exists
             Short damage = null;
             if (name.contains(":")) {
                 try {
                     damage = Short.parseShort(name.split(":")[1]);
                 } catch (final NumberFormatException e) {
                     this.plugin.getLogger().warning("Unable to parse damage value " + config.getCurrentPath() + ": " + key + "; " + e.getMessage());
                     continue;
                 }
 
                 name = name.split(":")[0];
             }
 
             // Identify material by name
             final Material material = Material.getMaterial(name);
             if (material == null) {
                 this.plugin.getLogger().warning("Unable to identify Material " + config.getCurrentPath() + ": " + key);
                 continue;
             }
 
             // Store the language text
             if (!this.materials.containsKey(material.getId())) this.materials.put(material.getId(), new HashMap<Short, String>());
             this.materials.get(material.getId()).put(damage, config.getString(key));
         }
 
         this.plugin.getLogger().config(this.materials.size() + " material name(s) loaded");
     }
 
     public String formatMaterial(final int id, final Short damage) {
         final Map<Short, String> values = this.materials.get(id);
 
         // When no entry exists for this material id at all, return null
         if (values == null) return null;
 
         // When no specific damage value override exists, return the default material name (null if there is no default)
         if (!values.containsKey(damage)) return values.get(null);
 
         // Otherwise return the specific damage value override (which could be null itself)
         return values.get(damage);
     }
 
     public String formatMaterial(final Material material) {
         return this.formatMaterial(material.getId(), null);
     }
 
     public String formatMaterial(final Block block) {
         return this.formatMaterial(block.getTypeId(), (short) block.getData());
     }
 
     public String formatMaterial(final BlockState state) {
         return this.formatMaterial(state.getTypeId(), (short) state.getRawData());
     }
 
     public String formatPotion(final Potion potion) {
         String type = this.potionTypes.get(potion.getType());
         if (type == null) type = potion.getType().name();
         String formatted = String.format(this.potionFormatBase, "", type);
         if (potion.getLevel() > 1) formatted = String.format(this.potionFormatLevel, formatted, (potion.getLevel() == 2 ? "II" : potion.getLevel()));
         if (potion.hasExtendedDuration()) formatted = String.format(this.potionFormatExtended, formatted);
         return formatted;
     }
 
     public String formatMaterial(final ItemStack item) {
         return this.formatMaterial(item.getTypeId(), item.getDurability());
     }
 
     public String formatItem(final ItemStack item) {
         String formatted = this.formatMaterial(item);
 
        if (item.getType() == Material.POTION && item.getDurability() != PotionType.WATER.getDamageValue())
             formatted = this.formatPotion(Potion.fromItemStack(item));
 
         // TODO enumerate enchantments
         if (this.enchanted != null && item.getEnchantments().size() > 0)
             formatted = String.format(this.enchanted, formatted);
 
         return formatted;
     }
 
     public String getDeathMessageFormat(final DamageCause cause) {
         return this.deaths.get(cause);
     }
 
     public String formatEntity(final Entity entity) {
         String formatted = this.formatEntitySubtype(entity);
         if (formatted == null) formatted = this.formatEntityType(entity);
         return formatted;
     }
 
     public String formatPotionEffect(final PotionEffect effect) {
         final String formatted = this.potionEffects.get(effect.getType());
         if (formatted != null) return formatted;
 
         return effect.getType().getName();
     }
 
     public String formatEntityType(final Entity entity) {
         return this.entities.get(entity.getType());
     }
 
     public String formatEntitySubtype(final Entity entity) {
         return this.entitySubtypes.get(EntitySubtype.of(entity));
     }
 
     public String formatName(final Entity entity) {
         if (entity instanceof Player)
             return ((Player) entity).getDisplayName();
 
         String name = this.formatEntity(entity);
 
         if (entity instanceof ThrownPotion) {
             String effects = "";
             for (final PotionEffect effect : ((ThrownPotion) entity).getEffects()) {
                 if (effects.length() != 0) effects += ", ";
                 effects += this.formatPotionEffect(effect);
             }
             name = String.format(this.potionFormatBase, name, effects);
         }
 
         if (name != null) return name;
 
         return entity.getType().name();
     }
 
 }
