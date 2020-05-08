 package com.herocraftonline.dev.heroes.damage;
 
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Egg;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Snowball;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.SkillUseInfo;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.Skill;
 
 public class DamageManager {
 
     private Heroes plugin;
 
     private HeroesDamageListener listener;
     private Map<Material, Integer> itemDamage;
     private Map<ProjectileType, Integer> projectileDamage;
     private Map<CreatureType, Integer> creatureHealth;
     private Map<CreatureType, Integer> creatureDamage;
     private Map<DamageCause, Double> environmentalDamage;
     private Map<Integer, SkillUseInfo> spellTargs = new HashMap<Integer, SkillUseInfo>();
 
     public DamageManager(Heroes plugin) {
         this.plugin = plugin;
         listener = new HeroesDamageListener(plugin, this);
         Bukkit.getServer().getPluginManager().registerEvents(listener, plugin);
     }
 
     public void addSpellTarget(Entity o, Hero hero, Skill skill) {
         SkillUseInfo skillInfo = new SkillUseInfo(hero, skill);
         spellTargs.put(o.getEntityId(), skillInfo);
     }
 
     public boolean isSpellTarget(Entity o) {
         return spellTargs.containsKey(o.getEntityId());
     }
 
     public SkillUseInfo getSpellTargetInfo(Entity o) {
         return spellTargs.get(o.getEntityId());
     }
 
     public Integer getEntityDamage(CreatureType type) {
         return creatureDamage.get(type);
     }
 
     public Integer getEntityMaxHealth(CreatureType type) {
         return creatureHealth.get(type);
     }
 
     public Double getEnvironmentalDamage(DamageCause cause) {
         return environmentalDamage.get(cause);
     }
 
     public Integer getItemDamage(Material item, HumanEntity entity) {
         if (entity != null && entity instanceof Player) {
             Hero hero = plugin.getHeroManager().getHero((Player) entity);
            HeroClass heroClass = hero.getHeroClass();
            HeroClass secondClass = hero.getSecondClass();
             Integer classDamage = heroClass.getItemDamage(item);
             if (classDamage != null) {
                 classDamage += (int) (heroClass.getItemDamageLevel(item) * hero.getLevel(heroClass));
             }
             Integer secondDamage = null;
             if (secondClass != null) {
                 secondDamage = secondClass.getItemDamage(item);
                 if (secondDamage != null) {
                     secondDamage += (int) (secondClass.getItemDamageLevel(item) * hero.getLevel(secondClass));
                 }
             }
 
             if (classDamage != null && secondDamage != null) {
                 return classDamage > secondDamage ? classDamage : secondDamage;
             } else if (classDamage != null) {
                 return classDamage;
             } else if (secondDamage != null) {
                 return secondDamage;
             }
         }   
         return itemDamage.get(item);
     }
 
     public Integer getProjectileDamage(ProjectileType type, HumanEntity entity) {
         if (entity != null && entity instanceof Player) {
            Hero hero = plugin.getHeroManager().getHero((Player) entity);
            HeroClass heroClass = hero.getHeroClass();
            HeroClass secondClass = hero.getSecondClass();
             Integer classDamage = heroClass.getProjectileDamage(type);
            if (classDamage != null) {
                classDamage += (int) (heroClass.getProjDamageLevel(type) * hero.getLevel(heroClass));
            }
            Integer secondDamage = null;
            if (secondClass != null) {
                secondDamage = secondClass.getProjectileDamage(type);
                if (secondDamage != null) {
                    secondDamage += (int) (secondClass.getProjDamageLevel(type) * hero.getLevel(secondClass));
                }
            }

            if (classDamage != null && secondDamage != null) {
                return classDamage > secondDamage ? classDamage : secondDamage;
            } else if (classDamage != null) {
                 return classDamage;
            } else if (secondDamage != null) {
                return secondDamage;
            }
         }
         return projectileDamage.get(type);
     }
 
     public void load(Configuration config) {
         Set<String> keys;
 
         Heroes.properties.potHealthPerTier = config.getDouble("potions.health-per-tier", .1);
 
         creatureHealth = new EnumMap<CreatureType, Integer>(CreatureType.class);
         ConfigurationSection section = config.getConfigurationSection("creature-health");
         if (section != null) {
             keys = section.getKeys(false);
             if (keys != null) {
                 for (String key : keys) {
                     CreatureType type = CreatureType.fromName(key);
                     if (type == null)
                         continue;
 
                     int health = section.getInt(key, 20);
                     if (health <= 0)
                         health = 20;
 
                     creatureHealth.put(type, health);
                 }
             }
         }
 
         creatureDamage = new EnumMap<CreatureType, Integer>(CreatureType.class);
         section = config.getConfigurationSection("creature-damage");
         if (section != null) {
             keys = section.getKeys(false);
             if (keys != null) {
                 for (String key : keys) {
                     CreatureType type = CreatureType.fromName(key);
                     if (type == null)
                         continue;
 
                     creatureDamage.put(type, section.getInt(key, 10));
                 }
             }
         }
 
         itemDamage = new EnumMap<Material, Integer>(Material.class);
         section = config.getConfigurationSection("item-damage");
         if (section != null) {
             keys = section.getKeys(false);
             if (keys != null) {
                 for (String key : keys) {
                     Material item = Material.matchMaterial(key);
                     if (item == null)
                         continue;
 
                     itemDamage.put(item, section.getInt(key, 2));
                 }
             }
         }
 
         environmentalDamage = new EnumMap<DamageCause, Double>(DamageCause.class);
         section = config.getConfigurationSection("environmental-damage");
         if (section != null) {
             keys = section.getKeys(false);
             if (keys != null) {
                 for (String key : keys) {
                     try {
                         DamageCause cause = DamageCause.valueOf(key.toUpperCase());
                         double damage = section.getDouble(key, 0.0);
                         environmentalDamage.put(cause, damage);
                     } catch (IllegalArgumentException e) {}
                 }
             }
         }
 
         projectileDamage = new EnumMap<ProjectileType, Integer>(ProjectileType.class);
         section = config.getConfigurationSection("projectile-damage");
         if (section != null) {
             keys = section.getKeys(false);
             if (keys != null) {
                 for (String key : keys) {
                     ProjectileType type = ProjectileType.valueOf(key.toUpperCase());
                     if (type == null)
                         continue;
 
                     projectileDamage.put(type, section.getInt(key, 0));
                 }
             }
         }
     }
 
     public SkillUseInfo removeSpellTarget(Entity o) {
         return spellTargs.remove(o.getEntityId());
     }
 
     public enum ProjectileType {
         ARROW,
         EGG,
         SNOWBALL;
 
         public static ProjectileType matchProjectile(final String name) {
             if (name.equalsIgnoreCase("arrow"))
                 return ARROW;
             else if (name.equalsIgnoreCase("snowball"))
                 return SNOWBALL;
             else if (name.equalsIgnoreCase("egg"))
                 return EGG;
             else
                 return null;
         }
 
         public static ProjectileType valueOf(Entity entity) {
             if (entity instanceof Arrow)
                 return ARROW;
             else if (entity instanceof Snowball)
                 return SNOWBALL;
             else if (entity instanceof Egg)
                 return EGG;
             else
                 return null;
         }
     }
 
     public int getEntityHealth(LivingEntity lEntity) {
         return listener.getHealth(lEntity);
     }
 
     public int getEntityMaxHealth(LivingEntity lEntity) {
         return listener.getMaxHealth(lEntity);
     }
 }
