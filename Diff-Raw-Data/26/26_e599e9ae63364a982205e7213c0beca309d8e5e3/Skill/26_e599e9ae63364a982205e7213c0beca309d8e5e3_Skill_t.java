 package com.herocraftonline.dev.heroes.skill;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.EnumSet;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Location;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.MemoryConfiguration;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.command.BasicCommand;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 /**
  * The root class of the skill heirarchy. This class implements the basic functionality of every Heroes skill including
  * configuration handling, area-based player notifications and event registration. Because this class extends
  * {@link com.herocraftonline.dev.heroes.command.BaseCommand}, the constructor of every skill should define a name,
  * description, usage, min and max arguments and at least one identifier. Any registered events must provide an event
  * listener, usually created as an inner class.
  * </br>
  * </br>
  * <b>Skill Framework:</b>
  * <ul>
  * <li>{@link ActiveSkill}</li>
  * <ul>
  * <li>{@link TargettedSkill}</li>
  * </ul>
  * <li>{@link PassiveSkill}</li> <li>{@link OutsourcedSkill}</li> </ul>
  * <b>Note:</b> All skill identifiers <i>must</i> begin with <i>skill</i>, e.g. "skill fireball".
  */
 public abstract class Skill extends BasicCommand {
 
     public final Heroes plugin;
     private Configuration defaultConfig = new MemoryConfiguration();
     private ConfigurationSection config;
 
     private final Set<SkillType> types = EnumSet.noneOf(SkillType.class);
 
     /**
      * The constructor of every skill must define:
      * <ul>
      * <li><code>name</code></li>
      * <li><code>description</code></li>
      * <li><code>usage</code></li>
      * <li><code>minArgs</code></li>
      * <li><code>maxArgs</code></li>
      * <li><code>identifiers</code></li>
      * <li><code>notes</code> (optional)</li>
      * </ul>
      * 
      * @param plugin
      *            the active Heroes instance
      */
     public Skill(Heroes plugin, String name) {
         super(name);
         config = SkillManager.allSkillsConfig.getConfigurationSection(getName());
         this.plugin = plugin;
     }
 
     public void addSpellTarget(Entity o, Hero hero) {
         plugin.getDamageManager().addSpellTarget(o, hero, this);
     }
 
     /**
      * Helper method that broadcasts a message to all players within 30 blocks of the specified source. These messages
      * can be suppressed by players on an individual basis.
      * 
      * @param source
      *            the <code>Location</code> to measure from
      * @param message
      *            the content of the message
      * @param args
      *            any text in the message of the format $<i>n</i> where <i>n</i>
      *            is an integer will be replaced with the <i>n</i>th element of
      *            this array
      */
     public void broadcast(Location source, String message, Object... args) {
         if (message == null || message.isEmpty())
             return;
 
         final Player[] players = plugin.getServer().getOnlinePlayers();
         for (Player player : players) {
             Location playerLocation = player.getLocation();
             Hero hero = plugin.getHeroManager().getHero(player);
             if (hero.isSuppressing(this)) {
                 continue;
             }
             if (source.getWorld().equals(playerLocation.getWorld())) {
                 if (playerLocation.distance(source) < 30) {
                     Messaging.send(player, message, args);
                 }
             }
         }
     }
 
     /**
      * Tests if the target is damagable from a source. Also adds a last-damage cause to the target which allows proper
      * xp.
      * Returns if the damage check was successful.
      * 
      * @param player
      * @param target
      * @return
      */
     public boolean damageCheck(Player player, LivingEntity target) {
         if (player.equals(target))
             return false;
 
         EntityDamageByEntityEvent damageEntityEvent = new EntityDamageByEntityEvent(player, target, DamageCause.CUSTOM, 0);
         plugin.getServer().getPluginManager().callEvent(damageEntityEvent);
         if (damageEntityEvent.isCancelled())
             return false;
 
         //Reverse damage check to make sure the target can damage the player - this prevents the player from attacking the target while invulnerable
         damageEntityEvent = new EntityDamageByEntityEvent(target, player, DamageCause.CUSTOM, 0);
         plugin.getServer().getPluginManager().callEvent(damageEntityEvent);
         if (damageEntityEvent.isCancelled())
             return false;
         return true;
     }
 
     /**
      * The end of the execution path of a skill, this method is called whenever a command with a registered identifier
      * is used.
      * 
      * @param sender
      *            the <code>CommandSender</code> issuing the command
      * @param args
      *            the arguments provided with the command
      */
     @Override
     public abstract boolean execute(CommandSender sender, String identifier, String[] args);
 
     public ConfigurationSection getConfig() {
         return config;
     }
 
     /**
      * Creates and returns a <code>ConfigurationNode</code> containing all the default data for the skill. By default,
      * this configuration is empty.
      * 
      * @return an empty configuration
      */
     public ConfigurationSection getDefaultConfig() {
         return defaultConfig;
     }
 
     public Heroes getPlugin() {
         return plugin;
     }
 
 
     /**
      * Retrieves a <code>Boolean</code> value from the skill's configuration. Data from the provided
      * <code>HeroClass</code> will be preferred over the skill's own data, if found. If the setting is found in neither
      * of these sources, the default value is returned.
      * 
      * @param heroClass
      *            the class to search for skill data
      * @param setting
      *            the name of the data entry to retrieve
      * @param def
      *            the default value to be used if no entry is found
      * @return the stored setting
      */
     public boolean getSetting(Hero hero, String setting, boolean def) {
         if (hero == null)
             return getConfig().getBoolean(setting, def);
         int val1 = -1;
         int val2 = -1;
         
         if (hero.getHeroClass().hasSkill(this.getName()))
             val1 = hero.getHeroClass().getSkillSettings(getName()).getBoolean(setting, def) ? 1 : 0;
         
         if (hero.getSecondClass() != null && hero.getSecondClass().hasSkill(this.getName()))
             val2 = hero.getSecondClass().getSkillSettings(getName()).getBoolean(setting, def) ? 1 :0;
 
         if (val1 == -1 && val2 == -1)
             return def;
         else if (val2 != -1 && val2 <= val1)
             return val2 == 1 ? true : false;
         else
             return val1 == 1 ? true : false;
     }
 
     /**
      * Retrieves a <code>double</code> value from the skill's configuration. Data from the provided
      * <code>HeroClass</code> will be preferred over the skill's own data, if found. If the setting is found in neither
      * of these sources, the default value is returned.
      * 
      * @param hero
      *            the class to search for skill data
      * @param setting
      *            the name of the data entry to retrieve
      * @param def
      *            the default value to be used if no entry is found
      * @return the stored setting
      */
     public double getSetting(Hero hero, String setting, double def, boolean lower) {
         if (hero == null)
             return getConfig().getDouble(setting, def);
 
         double val1 = -1;
         double val2 = -1;
         if (hasSetting(hero.getHeroClass(), setting))
             val1 = hero.getHeroClass().getSkillSettings(getName()).getDouble(setting, def);
         if (hasSetting(hero.getSecondClass(), setting)) {
             val2 = hero.getSecondClass().getSkillSettings(getName()).getDouble(setting, def);
         }
 
         if (val1 != -1 && val2 != -1) {
             if (lower)
                 return val1 < val2 ? val1 : val2;
             else
                 return val1 > val2 ? val1 : val2;
         } else if (val1 != -1)
             return val1;
         else if (val2 != -1)
             return val2;
         else
             return config.getDouble(setting, def);
     }
 
     /**
      * Retrieves a <code>int</code> value from the skill's configuration. Data from the provided <code>HeroClass</code>
      * will be preferred over the skill's own data, if found. If the setting is found in neither of these sources, the
      * default value is returned.
      * 
      * @param heroClass
      *            the class to search for skill data
      * @param setting
      *            the name of the data entry to retrieve
      * @param def
      *            the default value to be used if no entry is found
      * @return the stored setting
      */
     public int getSetting(Hero hero, String setting, int def, boolean lower) {
         if (hero == null)
             return getConfig().getInt(setting, def);
 
         int val1 = -1;
         int val2 = -1;
         if (hasSetting(hero.getHeroClass(), setting))
             val1 = hero.getHeroClass().getSkillSettings(getName()).getInt(setting, def);
         if (hasSetting(hero.getSecondClass(), setting)) {
             val2 = hero.getSecondClass().getSkillSettings(getName()).getInt(setting, def);
         }
 
         if (val1 != -1 && val2 != -1) {
             if (lower)
                 return val1 < val2 ? val1 : val2;
             else
                 return val1 > val2 ? val1 : val2;
         } else if (val1 != -1)
             return val1;
         else if (val2 != -1)
             return val2;
         else
             return getConfig().getInt(setting, def);
     }
 
     /**
      * Retrieves a <code>List of String</code> values from the skill's configuration. Data from the provided
      * <code>HeroClass</code> will be preferred over the skill's own data, if found. If the setting is found in neither
      * of these sources, the default value is returned.
      * 
      * @param heroClass
      *            the class to search for skill data
      * @param setting
      *            the name of the data entry to retrieve
      * @param def
      *            the default value to be used if no entry is found
      * @return the stored setting
      */
     public List<String> getSetting(Hero hero, String setting, List<String> def) {
         if (hero == null) {
             List<String> list = getConfig().getStringList(setting);
             return list != null ? list : def;
         }
 
         List<String> vals = new ArrayList<String>();
         if (hasSetting(hero.getHeroClass(), setting)) {
             List<String> list = hero.getHeroClass().getSkillSettings(getName()).getStringList(setting);
             if (list.isEmpty())
                 vals.addAll(def);
             else
                 vals.addAll(list);
         } if (hasSetting(hero.getSecondClass(), setting)) {
             List<String> list = hero.getSecondClass().getSkillSettings(getName()).getStringList(setting);
             if (list.isEmpty())
                 vals.addAll(def);
             else
                 vals.addAll(list);
         }
         if (!vals.isEmpty())
             return vals;
         else {
             List<String> list = config.getStringList(setting);
             return list != null ? list : def;
         }
     }
 
     /**
      * Retrieves a <code>String</code> value from the skill's configuration. Data from the provided
      * <code>HeroClass</code> will be preferred over the skill's own data, if found. If the setting is found in neither
      * of these sources, the default value is returned.
      * 
      * @param heroClass
      *            the class to search for skill data
      * @param setting
      *            the name of the data entry to retrieve
      * @param def
      *            the default value to be used if no entry is found
      * @return the stored setting
      */
     public String getSetting(Hero hero, String setting, String def) {
         if (hero == null)
             return getConfig().getString(setting, def);
         else if (hasSetting(hero.getHeroClass(), setting))
             return hero.getHeroClass().getSkillSettings(getName()).getString(setting, def);
         else if (hasSetting(hero.getSecondClass(), setting))
             return hero.getSecondClass().getSkillSettings(getName()).getString(setting, def);
         else
             return getConfig().getString(setting, def);
     }
 
     /**
      * Retrieves all <code>String</code> keys from the skill's configuration. Data from the provided
      * <code>HeroClass</code> and
      * the main configuration will be returned
      * 
      * @param heroClass
      *            the class to search for skill data
      * @return the stored setting
      */
     public List<String> getSettingKeys(Hero hero) {
         Set<String> keys = new HashSet<String>();
         keys.addAll(getConfig().getKeys(false));
 
         if (hasSection(hero.getHeroClass(), null)) {
             keys.addAll(hero.getHeroClass().getSkillSettings(getName()).getKeys(false));
         }
         if (hero.getSecondClass() != null && hasSection(hero.getSecondClass(), null)) {
             keys.addAll(hero.getSecondClass().getSkillSettings(getName()).getKeys(false));
         }
         return new ArrayList<String>(keys);
     }
 
     /**
      * Retrieves all <code>String</code> keys from the skill's configuration. Data from the provided
      * <code>HeroClass</code> and
      * the main configuration will be returned
      * 
      * @param heroClass
      *            the class to search for skill data
      * @return the stored setting
      */
     public List<String> getSettingKeys(Hero hero, String setting) {
         Set<String> keys = new HashSet<String>();
         
         ConfigurationSection section = getConfig().getConfigurationSection(setting);
         if (section != null)
             keys.addAll(section.getKeys(false));
         
         if (hasSection(hero.getHeroClass(), setting)) {
             section = hero.getHeroClass().getSkillSettings(getName()).getConfigurationSection(setting);
             keys.addAll(section.getKeys(false));
         }
         if (hasSection(hero.getSecondClass(), setting)) {
             section = hero.getSecondClass().getSkillSettings(getName()).getConfigurationSection(setting);
             keys.addAll(section.getKeys(false));
         }
         return new ArrayList<String>(keys);
     }
 
     public Set<SkillType> getTypes() {
         return Collections.unmodifiableSet(this.types);
     }
 
     public boolean hasSection(HeroClass heroClass, String setting) {
         if (heroClass == null)
             return false;
         if (heroClass.getSkillSettings(getName()) == null)
             return false;
         if (setting == null)
             return !heroClass.getSkillSettings(getName()).getKeys(false).isEmpty();
 
         return heroClass.getSkillSettings(getName()).getConfigurationSection(setting) != null;
     }
 
     /**
      * An initialization method called after all configuration data is loaded.
      */
     public abstract void init();
 
     @Override
     public boolean isShownOnHelpMenu() {
         return false;
     }
 
     public boolean isType(SkillType type) {
         return types.contains(type);
     }
 
     /**
      * Sets the configuration containing all settings related to the skill. This should only be used by the skill loader
      * in most cases.
      * 
      * @param config
      *            the new skill configuration
      */
     public void setConfig(ConfigurationSection config) {
         this.config = config;
     }
 
     private boolean hasSetting(HeroClass heroClass, String setting) {
        if (heroClass == null || !heroClass.hasSkill(getName()))
             return false;
        
        return heroClass.hasSkillSettings(getName() + "." + setting);
     }
 
     /**
      * Helper method to make registering an event a little easier.
      * 
      * @param type
      *            the type of event
      * @param listener
      *            the listener used to handle the event
      * @param priority
      *            the priority given to the event handler
      */
     protected void registerEvent(Type type, Listener listener, Priority priority) {
         plugin.getServer().getPluginManager().registerEvent(type, listener, priority, plugin);
     }
 
     protected void setTypes(SkillType... types) {
         this.types.addAll(Arrays.asList(types));
     }
 }
