 /*
 This file is part of Legends.
 
     Legends is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Legends is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Legends.  If not, see <http://www.gnu.org/licenses/>.
 */
 package com.github.legendsdev.legends.library.database;
 
 import com.github.legendsdev.legends.library.BasicInfo;
 import com.github.legendsdev.legends.library.LPlayer;
 import com.github.legendsdev.legends.library.armor.Armor;
 import com.github.legendsdev.legends.library.armor.ArmorHandler;
 import com.github.legendsdev.legends.library.database.helper.YAMLHelper;
 import com.github.legendsdev.legends.library.lclass.LClass;
 import com.github.legendsdev.legends.library.race.Race;
 import com.github.legendsdev.legends.library.weapon.Weapon;
 import com.github.legendsdev.legends.library.weapon.WeaponHandler;
 
 import java.io.FileNotFoundException;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 /**
  * Thread safe class for accessing YAML database.
  *
  * @author B2OJustin
  */
 public class YAMLDataSource implements DataSource {
     private static final Logger logger = Logger.getLogger(YAMLDataSource.class.getName());
     private String configPath = "";
     private final String RACE_PATH = "races/";
     private final String SKILL_PATH = "skills/";
     private final String PLAYER_PATH = "players/";
     private final String CLASS_PATH = "classes/";
 
     @Override
     public String getName() {
         return "YAML";
     }
 
     public YAMLDataSource(String configPath) {
         this.configPath = configPath;
     }
 
     @Override
     public Logger getLogger() {
         return YAMLDataSource.logger;
     }
 
     @Override
     public synchronized LPlayer loadLPlayer(String name) {
         return null; //TODO loadLPlayer method stub
     }
 
     @Override
     @SuppressWarnings({"LoggerStringConcat", "unchecked"})
     public synchronized Race loadRace(String name) {
         String filePath = configPath + RACE_PATH + name.replace(" ", "_") + ".yml";
         Race race = new Race();
         try {
             YAMLHelper yml = new YAMLHelper(filePath);
 
             for(String mainKey : yml.getKeys("")) {
                 switch(mainKey) {
                     case "name":
                         race.setName(yml.getString("name"));
                         break;
                     case "description":
                         race.setDescription(yml.getStringList("description"));
                         break;
 
                     // Weapons
                     case "Weapons":
                         for(String wepKey : yml.getKeys("Weapons")) {
                             switch(wepKey) {
                                 case "default":
                                     if(yml.getString("Weapons.default").equals("allow")) {
                                         race.getWeaponRestrictions().setDefault(true);
                                     }
                                     else race.getWeaponRestrictions().setDefault(false);
                                 break;
                                 case "permitted-weapon":
                                     for(String wepID : yml.getKeys("Weapons.permitted-weapon")) {
                                         Weapon weapon = WeaponHandler.getInstance().get(wepID);
                                         race.getWeaponRestrictions().setAllowed(weapon, true);
                                         LinkedHashMap<String, Object> infoMap = yml.getObject(LinkedHashMap.class, String.format("Weapons.permitted-weapon.%s", wepID));
                                         loadInfo(race.getWeaponInfo(weapon), infoMap);
                                     }
                                 break;
                                 case "restricted-weapon":
                                     for(String wepID : yml.getStringList("Weapons.restricted-weapon")) {
                                         Weapon weapon = WeaponHandler.getInstance().get(wepID);
                                         race.getWeaponRestrictions().setAllowed(weapon, false);
                                     }
                             }
                         }
                     break;
 
                     // Armor
                     case "Armor":
                         for(String armorKey : yml.getKeys("Armor")) {
                             switch(armorKey) {
                                 case "default":
                                     if(yml.getString("Armor.default").equals("allow")) {
                                         race.getArmorRestrictions().setDefault(true);
                                     }
                                     else race.getArmorRestrictions().setDefault(false);
                                 break;
                                 case "permitted-armor":
                                     for(String armorID : yml.getKeys("Armor.permitted-armor")) {
                                         Armor armor = ArmorHandler.getInstance().get(armorID);
                                         race.getArmorRestrictions().setAllowed(armor, true);
                                         LinkedHashMap<String, Object> infoMap = yml.getObject(LinkedHashMap.class, String.format("Armor.permitted-armor.%s", armorID));
                                         loadInfo(race.getArmorInfo(armor), infoMap);
                                     }
                                 break;
                                 case "restricted-armor":
                                     for(String armorID : yml.getStringList("Armor.restricted-armor")) {
                                         Armor armor = ArmorHandler.getInstance().get(armorID);
                                         race.getArmorRestrictions().setAllowed(armor, false);
                                     }
                             }
                         }
                     break;
                 }
             }
 
         } catch (FileNotFoundException e) {
             logger.warning("Could not find file for race '" + name + "' at " + filePath);
             e.printStackTrace();
         } catch (ClassCastException e) {
             logger.warning("You seem to have an error in your yaml. Could not load race '" + name + "'");
             e.printStackTrace();
         }
 
         return race;
     }
 
     private <T extends BasicInfo> T loadInfo(T basicInfo, LinkedHashMap<String, Object> infoMap) throws ClassCastException {
        if(infoMap == null) return null;
         for(Map.Entry<String, Object> entry : infoMap.entrySet()) {
             switch(entry.getKey()) {
                 case "bonus-defense":
                     int bonusDefense = (int) entry.getValue();
                     basicInfo.addBonusDefense(bonusDefense);
                 break;
                case "bonus-damage":
                    int bonusDamage = (int) entry.getValue();
                    basicInfo.addBonusDamage(bonusDamage);
                break;
                 case "required-level":
                     int requiredLevel = (int) entry.getValue();
                     basicInfo.getLevelRestrictions().setMinLevel(requiredLevel);
                 break;
                 case "maximum-level":
                     int maximumLevel = (int) entry.getValue();
                     basicInfo.getLevelRestrictions().setMaxLevel(maximumLevel);
                 break;
             }
         }
         return (T) basicInfo;
     }
 
     @Override
     public synchronized LClass loadLClass(String name) {
         return null; //TODO loadLClass method stub
     }
 }
