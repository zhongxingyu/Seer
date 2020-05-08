 package com.herocraftonline.dev.heroes.classes;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 
 import org.bukkit.util.config.Configuration;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClass.ArmorItems;
 import com.herocraftonline.dev.heroes.classes.HeroClass.ArmorType;
 import com.herocraftonline.dev.heroes.classes.HeroClass.ExperienceType;
 import com.herocraftonline.dev.heroes.classes.HeroClass.WeaponItems;
 import com.herocraftonline.dev.heroes.classes.HeroClass.WeaponType;
 
 public class ClassManager {
 
     private final Heroes plugin;
     private Set<HeroClass> classes;
     private HeroClass defaultClass;
 
     public ClassManager(Heroes plugin) {
         this.plugin = plugin;
         this.classes = new HashSet<HeroClass>();
     }
 
     public HeroClass getClass(String name) {
         for (HeroClass c : classes) {
             if (name.equalsIgnoreCase(c.getName())) {
                 return c;
             }
         }
         return null;
     }
 
     public boolean addClass(HeroClass c) {
         return classes.add(c);
     }
 
     public boolean removeClass(HeroClass c) {
         return classes.remove(c);
     }
 
     public Set<HeroClass> getClasses() {
         return classes;
     }
 
     public void loadClasses(File file) {
         Configuration config = new Configuration(file);
         config.load();
         List<String> classNames = config.getKeys("classes");
         for (String className : classNames) {
             HeroClass newClass = new HeroClass(className);
 
             List<String> defaultType = new ArrayList<String>();
             defaultType.add("DIAMOND");
 
             List<String> armor = config.getStringList("classes." + className + ".permitted-armor", defaultType);
             for (String a : armor) {
                 // If it's a generic type like 'DIAMOND' or 'LEATHER' we add all the possible entries.
                 if (!(a.contains("_"))) {
                     try {
                         ArmorType aType = ArmorType.valueOf(a);
                         newClass.addAllowedArmor(aType + "_HELMET");
                         newClass.addAllowedArmor(aType + "_CHESTPLATE");
                         newClass.addAllowedArmor(aType + "_LEGGINGS");
                         newClass.addAllowedArmor(aType + "_BOOTS");
                     } catch (IllegalArgumentException e) {
                         plugin.log(Level.WARNING, "Invalid armor type (" + a + ") defined for " + className);
                     }
                 } else {
                     String type = a.substring(0, a.indexOf("_"));
                     String item = a.substring(a.indexOf("_") + 1, a.length());
                     try {
                         ArmorType aType = ArmorType.valueOf(type);
                         ArmorItems aItem = ArmorItems.valueOf(item);
                         newClass.addAllowedArmor(aType + "_" + aItem);
                     } catch (IllegalArgumentException e) {
                         plugin.log(Level.WARNING, "Invalid armor type (" + type + "_" + item + ") defined for " + className);
                     }
                 }
             }
 
             List<String> weapon = config.getStringList("classes." + className + ".permitted-weapon", defaultType);
             for (String w : weapon) {
                 // A BOW has no ItemType so we just add it straight away.
                 if(w.equalsIgnoreCase("BOW")){
                     newClass.addAllowedWeapon("BOW");
                     continue;
                 }
                 // If it's a generic type like 'DIAMOND' or 'LEATHER' we add all the possible entries.
                 if (!(w.contains("_"))) {
                     try {
                         WeaponType wType = WeaponType.valueOf(w);
                         newClass.addAllowedWeapon(wType + "_PICKAXE");
                         newClass.addAllowedWeapon(wType + "_AXE");
                         newClass.addAllowedWeapon(wType + "_HOE");
                         newClass.addAllowedWeapon(wType + "_SPADE");
                         newClass.addAllowedWeapon(wType + "_SWORD");
                     } catch (IllegalArgumentException e) {
                         plugin.log(Level.WARNING, "Invalid weapon type (" + w + ") defined for " + className);
                     }
                 } else {
                     String type = w.substring(0, w.indexOf("_"));
                     String item = w.substring(w.indexOf("_") + 1, w.length());
                     try {
                         WeaponType wType = WeaponType.valueOf(type);
                         WeaponItems wItem = WeaponItems.valueOf(item);
                         newClass.addAllowedWeapon(wType + "_" + wItem);
                     } catch (IllegalArgumentException e) {
                         plugin.log(Level.WARNING, "Invalid weapon type (" + type + "_" + item + ") defined for " + className);
                     }
                 }
             }
 
             List<String> skillNames = config.getKeys("classes." + className + ".permitted-skills");
             for (String skill : skillNames) {
                 try {
                    int reqLevel = config.getInt("classes." + className + ".permitted-skill." + skill + ".level", 1);
                    int manaCost = config.getInt("classes." + className + ".permitted-skill." + skill + ".mana", 0);
                    int cooldown = config.getInt("classes." + className + ".permitted-skill." + skill + ".cooldown", 0);
                     newClass.addSkill(skill, reqLevel, manaCost, cooldown);
                 } catch (IllegalArgumentException e) {
                     plugin.log(Level.WARNING, "Invalid skill (" + skill + ") defined for " + className + ". Skipping this skill.");
                 }
             }
 
             List<String> experienceNames = config.getStringList("classes." + className + ".experience-sources", null);
             Set<ExperienceType> experienceSources = new HashSet<ExperienceType>();
             for (String experience : experienceNames) {
                 try {
                     boolean added = experienceSources.add(ExperienceType.valueOf(experience));
                     if (!added) {
                         plugin.log(Level.WARNING, "Duplicate experience source (" + experience + ") defined for " + className + ".");
                     }
                 } catch (IllegalArgumentException e) {
                     plugin.log(Level.WARNING, "Invalid experience source (" + experience + ") defined for " + className + ". Skipping this source.");
                 }
             }
             newClass.setExperienceSources(experienceSources);
 
             int maxSummon = config.getInt("classes." + className + ".tameMax", 0);
             try {
                 newClass.setTameMax(maxSummon);
             } catch (Exception e) {
                 plugin.log(Level.WARNING, "summon-max not set correctly -  (" + maxSummon + ") is not a number for - " + className);
             }
 
             boolean added = addClass(newClass);
             if (!added) {
                 plugin.log(Level.WARNING, "Duplicate class (" + className + ") found. Skipping this class.");
             } else {
                 plugin.log(Level.INFO, "Loaded class: " + className);
                 if (config.getBoolean("classes." + className + ".default", false)) {
                     plugin.log(Level.INFO, "Default class found: " + className);
                     defaultClass = newClass;
                 }
             }
         }
 
         for (HeroClass unlinkedClass : classes) {
             String className = unlinkedClass.getName();
             String parentName = config.getString("classes." + className + ".parent");
             if (parentName != null && (!parentName.isEmpty() || parentName.equals("null"))) {
                 HeroClass parent = getClass(parentName);
                 parent.getSpecializations().add(unlinkedClass);
                 unlinkedClass.setParent(parent);
             }
         }
     }
 
     public void setDefaultClass(HeroClass defaultClass) {
         this.defaultClass = defaultClass;
     }
 
     public HeroClass getDefaultClass() {
         return defaultClass;
     }
 
 }
