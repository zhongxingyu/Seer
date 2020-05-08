 package com.herocraftonline.dev.heroes.classes;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 
 import org.bukkit.Material;
 import org.bukkit.util.config.Configuration;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClass.ArmorItems;
 import com.herocraftonline.dev.heroes.classes.HeroClass.ArmorType;
 import com.herocraftonline.dev.heroes.classes.HeroClass.ExperienceType;
 import com.herocraftonline.dev.heroes.classes.HeroClass.WeaponItems;
 import com.herocraftonline.dev.heroes.classes.HeroClass.WeaponType;
 import com.herocraftonline.dev.heroes.damage.DamageManager.ProjectileType;
 import com.herocraftonline.dev.heroes.skill.OutsourcedSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 
 public class HeroClassManager {
 
     private final Heroes plugin;
     private Set<HeroClass> classes;
     private HeroClass defaultClass;
 
     public HeroClassManager(Heroes plugin) {
         this.plugin = plugin;
         this.classes = new HashSet<HeroClass>();
     }
 
     public boolean addClass(HeroClass c) {
         return classes.add(c);
     }
 
     public HeroClass getClass(String name) {
         for (HeroClass c : classes) {
             if (name.equalsIgnoreCase(c.getName())) return c;
         }
         return null;
     }
 
     public Set<HeroClass> getClasses() {
         return classes;
     }
 
     public HeroClass getDefaultClass() {
         return defaultClass;
     }
 
     public void loadClasses(File file) {
         Configuration config = new Configuration(file);
         config.load();
         List<String> classNames = config.getKeys("classes");
         if (classNames == null) {
             Heroes.log(Level.WARNING, "You have no classes defined in your setup!");
             return;
         }
         for (String className : classNames) {
             HeroClass newClass = new HeroClass(className);
             // HeroClass newClass = new HeroClass(className.substring(0, 1).toUpperCase() +
             // className.substring(1).toLowerCase());
 
             newClass.setDescription(config.getString("classes." + className + ".description", ""));
             newClass.setExpModifier(config.getDouble("classes." + className + ".expmodifier", 1.0D));
 
             List<String> defaultType = new ArrayList<String>();
             defaultType.add("DIAMOND");
 
             StringBuilder aLimits = new StringBuilder();
             StringBuilder wLimits = new StringBuilder();
 
             List<String> armor = config.getStringList("classes." + className + ".permitted-armor", defaultType);
             if (armor == null) {
                 plugin.debugLog(Level.WARNING, className + " has no permitted-armor section");
             } else {
                 for (String a : armor) {
                     // If it's a generic type like 'DIAMOND' or 'LEATHER' we add all the possible entries.
                     if (!a.contains("_")) {
                         try {
                             ArmorType aType = ArmorType.valueOf(a);
                             newClass.addAllowedArmor(aType + "_HELMET");
                             aLimits.append(" ").append(aType).append("_HELMET");
                             newClass.addAllowedArmor(aType + "_CHESTPLATE");
                             aLimits.append(" ").append(aType).append("_CHESTPLATE");
                             newClass.addAllowedArmor(aType + "_LEGGINGS");
                             aLimits.append(" ").append(aType).append("_LEGGINGS");
                             newClass.addAllowedArmor(aType + "_BOOTS");
                             aLimits.append(" ").append(aType).append("_BOOTS");
                         } catch (IllegalArgumentException e) {
                             Heroes.log(Level.WARNING, "Invalid armor type (" + a + ") defined for " + className);
                         }
                     } else {
                         String type = a.substring(0, a.indexOf("_"));
                         String item = a.substring(a.indexOf("_") + 1, a.length());
                         try {
                             ArmorType aType = ArmorType.valueOf(type);
                             ArmorItems aItem = ArmorItems.valueOf(item);
                             newClass.addAllowedArmor(aType + "_" + aItem);
                             aLimits.append(" ").append(aType).append("_").append(aItem);
                         } catch (IllegalArgumentException e) {
                             if(a == "*") {
                                 newClass.addAllowedArmor("*"); 
                             }else {
                                 Heroes.log(Level.WARNING, "Invalid armor type (" + type + "_" + item + ") defined for " + className);
                             }
                         }
                     }
                 }
             }
 
             List<String> weapon = config.getStringList("classes." + className + ".permitted-weapon", defaultType);
             if (armor == null) {
                 plugin.debugLog(Level.WARNING, className + " has no permitted-weapon section");
             } else {
                 for (String w : weapon) {
                     // A BOW has no ItemType so we just add it straight away.
                     if (w.equalsIgnoreCase("BOW")) {
                         newClass.addAllowedWeapon("BOW");
                         wLimits.append(" BOW");
                         continue;
                     }
                     // If it's a generic type like 'DIAMOND' or 'LEATHER' we add all the possible entries.
                     if (!w.contains("_")) {
                         try {
                             WeaponType wType = WeaponType.valueOf(w);
                             newClass.addAllowedWeapon(wType + "_PICKAXE");
                             wLimits.append(" ").append(wType).append("_PICKAXE");
                             newClass.addAllowedWeapon(wType + "_AXE");
                             wLimits.append(" ").append(wType).append("_AXE");
                             newClass.addAllowedWeapon(wType + "_HOE");
                             wLimits.append(" ").append(wType).append("_HOE");
                             newClass.addAllowedWeapon(wType + "_SPADE");
                             wLimits.append(" ").append(wType).append("_SPADE");
                             newClass.addAllowedWeapon(wType + "_SWORD");
                             wLimits.append(" ").append(wType).append("_SWORD");
                         } catch (IllegalArgumentException e) {
                             Heroes.log(Level.WARNING, "Invalid weapon type (" + w + ") defined for " + className);
                         }
                     } else {
                         String type = w.substring(0, w.indexOf("_"));
                         String item = w.substring(w.indexOf("_") + 1, w.length());
                         try {
                             WeaponType wType = WeaponType.valueOf(type);
                             WeaponItems wItem = WeaponItems.valueOf(item);
                             newClass.addAllowedWeapon(wType + "_" + wItem);
                             wLimits.append(" - ").append(wType).append("_").append(wItem);
                         } catch (IllegalArgumentException e) {
                             if(w == "*") {
                                newClass.addAllowedArmor("*"); 
                             }else {
                                 Heroes.log(Level.WARNING, "Invalid weapon type (" + type + "_" + item + ") defined for " + className);
                             }
                         }
                     }
 
                 }
             }
 
             plugin.debugLog(Level.INFO, "Allowed Weapons - " + wLimits.toString());
             plugin.debugLog(Level.INFO, "Allowed Armor - " + aLimits.toString());
 
             List<String> itemDamages = config.getKeys("classes." + className + ".item-damage");
             if (itemDamages == null) {
                 plugin.debugLog(Level.WARNING, className + " has no item damage section");
             } else {
                 for (String materialName : itemDamages) {
                     Material material = Material.matchMaterial(materialName);
                     if (material != null) {
                         int damage = config.getInt("classes." + className + ".item-damage." + materialName, 0);
                         newClass.setItemDamage(material, damage);
                     } else {
                         Heroes.log(Level.WARNING, "Invalid material (" + material + ") defined for " + className);
                     }
                 }
             }
 
             List<String> projectileDamages = config.getKeys("classes." + className + ".projectile-damage");
             if (projectileDamages == null) {
                 plugin.debugLog(Level.WARNING, className + " has no projectile damage section");
             } else {
                 for (String projectileName : projectileDamages) {
                     try {
                         ProjectileType type = ProjectileType.valueOf(projectileName);
                         int damage = config.getInt("classes." + className + ".projectile-damage." + projectileName, 0);
                         newClass.setProjectileDamage(type, damage);
                     } catch (IllegalArgumentException e) {
                         Heroes.log(Level.WARNING, "Invalid projectile type (" + projectileName + ") defined for " + className);
                     }
                 }
             }
 
             List<String> skillNames = config.getKeys("classes." + className + ".permitted-skills");
             if (skillNames == null) {
                 plugin.debugLog(Level.WARNING, className + " has no permitted-skills section");
             } else {
                 for (String skillName : skillNames) {
                     try {
                         Skill skill = (Skill) plugin.getCommandHandler().getCommand(skillName);
                         if (skill == null) {
                             Heroes.log(Level.WARNING, "Skill " + skillName + " defined for " + className + " not found.");
                             continue;
                         }
 
                         ConfigurationNode skillSettings = Configuration.getEmptyNode();
                         List<String> settings = config.getKeys("classes." + className + ".permitted-skills." + skillName);
                         if (settings != null) {
                             for (String key : settings) {
                                 skillSettings.setProperty(key, config.getProperty("classes." + className + ".permitted-skills." + skillName + "." + key));
                             }
                         }
                         newClass.addSkill(skillName, skillSettings);
                     } catch (IllegalArgumentException e) {
                         Heroes.log(Level.WARNING, "Invalid skill (" + skillName + ") defined for " + className + ". Skipping this skill.");
                     }
                 }
             }
 
             List<String> permissionSkillNames = config.getKeys("classes." + className + ".permission-skills");
             if (permissionSkillNames != null) {
                 for (String skill : permissionSkillNames) {
                     try {
                         ConfigurationNode skillSettings = Configuration.getEmptyNode();
                         skillSettings.setProperty("level", config.getInt("classes." + className + ".permission-skills." + skill + ".level", 1));
                         newClass.addSkill(skill, skillSettings);
 
                         String usage = config.getString("classes." + className + ".permission-skills." + skill + ".usage", "");
                         String[] permissions = config.getStringList("classes." + className + ".permission-skills." + skill + ".permissions", null).toArray(new String[0]);
                         OutsourcedSkill oSkill = new OutsourcedSkill(plugin, skill, permissions, usage);
                         plugin.getCommandHandler().addCommand(oSkill);
                     } catch (IllegalArgumentException e) {
                         Heroes.log(Level.WARNING, "Invalid permission skill (" + skill + ") defined for " + className + ". Skipping this skill.");
                     }
                 }
             }
 
             Double baseMaxHealth = config.getDouble("classes." + className + ".base-max-health", 20);
             Double maxHealthPerLevel = config.getDouble("classes." + className + ".max-health-per-level", 0);
             newClass.setBaseMaxHealth(baseMaxHealth);
             newClass.setMaxHealthPerLevel(maxHealthPerLevel);
 
             List<String> experienceNames = config.getStringList("classes." + className + ".experience-sources", null);
             Set<ExperienceType> experienceSources = new HashSet<ExperienceType>();
             if (experienceNames == null) {
                 plugin.debugLog(Level.WARNING, className + " has no experience-sources section");
             } else {
                 for (String experience : experienceNames) {
                     try {
                         boolean added = experienceSources.add(ExperienceType.valueOf(experience));
                         if (!added) {
                             Heroes.log(Level.WARNING, "Duplicate experience source (" + experience + ") defined for " + className + ".");
                         }
                     } catch (IllegalArgumentException e) {
                         Heroes.log(Level.WARNING, "Invalid experience source (" + experience + ") defined for " + className + ". Skipping this source.");
                     }
                 }
             }
             newClass.setExperienceSources(experienceSources);
 
             boolean added = addClass(newClass);
             if (!added) {
                 Heroes.log(Level.WARNING, "Duplicate class (" + className + ") found. Skipping this class.");
             } else {
                 Heroes.log(Level.INFO, "Loaded class: " + className);
                 if (config.getBoolean("classes." + className + ".default", false)) {
                     Heroes.log(Level.INFO, "Default class found: " + className);
                     defaultClass = newClass;
                 }
             }
         }
 
         plugin.getConfigManager().print(config.getAll(), "");
 
         for (HeroClass unlinkedClass : classes) {
             String className = unlinkedClass.getName();
             String parentName = config.getString("classes." + className + ".parent");
             plugin.debugLog(Level.INFO, "classes." + className + ".parent: " + parentName);
             if (parentName != null && (!parentName.isEmpty() || parentName.equals("null"))) {
                 HeroClass parent = getClass(parentName);
                 if (parent != null) {
                     parent.getSpecializations().add(unlinkedClass);
                     unlinkedClass.setParent(parent);
                 } else {
                     Heroes.log(Level.WARNING, "Cannot assign " + className + " a parent class as " + parentName + " does not exist.");
                 }
             }
         }
 
         if (defaultClass == null) {
             Heroes.log(Level.SEVERE, "You are missing a default class, this will cause A LOT of issues!");
         }
 
         // Save the Configuration setup to file, we do this so that any defaults values loaded are saved to file.
         config.save();
     }
 
     public boolean removeClass(HeroClass c) {
         return classes.remove(c);
     }
 
     public void setDefaultClass(HeroClass defaultClass) {
         this.defaultClass = defaultClass;
     }
 
 }
