 package com.vartala.soulofw0lf.rpgapi.loaders;
 
 import com.vartala.soulofw0lf.rpgapi.RpgAPI;
 import com.vartala.soulofw0lf.rpgapi.diseaseapi.Disease;
 import com.vartala.soulofw0lf.rpgapi.diseaseapi.DiseaseListeners;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by: soulofw0lf
  * Date: 7/29/13
  * Time: 11:49 PM
  * <p/>
  * This file is part of the Rpg Suite Created by Soulofw0lf and Linksy.
  * <p/>
  * The Rpg Suite is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * <p/>
  * The Rpg Suite is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * <p/>
  * You should have received a copy of the GNU General Public License
  * along with The Rpg Suite Plugin you have downloaded.  If not, see <http://www.gnu.org/licenses/>.
  */
 public class DiseaseLoader {
     RpgAPI rpg;
 
     public DiseaseLoader(RpgAPI Rpg){
         this.rpg = Rpg;
         this.rpg.diseaseListener = new DiseaseListeners(this.rpg);
         YamlConfiguration diseaseConfig = YamlConfiguration.loadConfiguration(new File("plugins/RpgDiseases/RpgDisease.yml"));
         YamlConfiguration diseaseLocale = YamlConfiguration.loadConfiguration(new File("plugins/RpgDiseases/Locale/DiseaseLocale.yml"));
         if (diseaseLocale.get("Disease Commands") == null){
             diseaseLocale.set("Disease Commands.Add Disease", "disadd");
         }
         try {
             diseaseLocale.save(new File("plugins/RpgDiseases/Locale/DiseaseLocale.yml"));
         } catch (IOException e){
             e.printStackTrace();
         }
         RpgAPI.commandSettings.put("Add Disease", diseaseLocale.getString("Disease Commands.Add Disease"));
         RpgAPI.commands.add(diseaseLocale.getString("Disease Commands.Add Disease"));
         if (diseaseConfig.get("Diseases") == null){
             diseaseConfig.set("Diseases.Withering Death.Severity Level", 5);
             diseaseConfig.set("Diseases.Withering Death.Spreadable", true);
             diseaseConfig.set("Diseases.Withering Death.Use Air Spread", true);
             diseaseConfig.set("Diseases.Withering Death.Air Spread Chance", 12.5);
             diseaseConfig.set("Diseases.Withering Death.Air Spread Distance", 15);
             diseaseConfig.set("Diseases.Withering Death.Use Hit Spread", true);
             diseaseConfig.set("Diseases.Withering Death.Hit Spread Chance", 12.5);
             diseaseConfig.set("Diseases.Withering Death.Stackable", true);
             diseaseConfig.set("Diseases.Withering Death.Fatal", true);
             diseaseConfig.set("Diseases.Withering Death.Ticks Before Death", 20);
             diseaseConfig.set("Diseases.Withering Death.Gets Worse", false);
             diseaseConfig.set("Diseases.Withering Death.Morphs Into", "");
             diseaseConfig.set("Diseases.Withering Death.Regresses To", "");
             diseaseConfig.set("Diseases.Withering Death.Morph Progression Timer", 0);
             diseaseConfig.set("Diseases.Withering Death.Effect Damage Dealt", true);
             diseaseConfig.set("Diseases.Withering Death.Adjust Damage Dealt By", -1.5);
             diseaseConfig.set("Diseases.Withering Death.Miss Chance", true);
             diseaseConfig.set("Diseases.Withering Death.Miss Percentage", 5.5);
             diseaseConfig.set("Diseases.Withering Death.Effect Damage Taken", true);
             diseaseConfig.set("Diseases.Withering Death.Adjust Damage Taken By", 3.5);
             diseaseConfig.set("Diseases.Withering Death.Effects.SLOW.Duration", 120);
             diseaseConfig.set("Diseases.Withering Death.Effects.SLOW.Amplifier", 1);
             diseaseConfig.set("Diseases.Withering Death.Behavior Timer", 0);
             diseaseConfig.set("Diseases.Withering Death.Use Damage Over Time", true);
             diseaseConfig.set("Diseases.Withering Death.Damage Per DOT Tick", 2.5);
             diseaseConfig.set("Diseases.Withering Death.Dot Tick Time", 6);
             diseaseConfig.set("Diseases.Withering Death.Adjust Health", false);
             diseaseConfig.set("Diseases.Withering Death.Adjust Health Amount", 0);
             diseaseConfig.set("Diseases.Withering Death.Behaviors.Use 1st Behavior true or false once behaviors are added", false);
             diseaseConfig.set("Diseases.Withering Death.Behaviors.Use 2nd Behavior true or false once behaviors are added", false);
             diseaseConfig.set("Diseases.Withering Death.Behaviors.Use 3rd Behavior true or false once behaviors are added", false);
             diseaseConfig.set("Diseases.Withering Death.Behaviors.Use 4th Behavior true or false once behaviors are added", false);
             try {
                 diseaseConfig.save(new File("plugins/RpgDiseases/RpgDisease.yml"));
             } catch (IOException e){
                 e.printStackTrace();
             }
             for (String key : diseaseConfig.getConfigurationSection("Diseases").getKeys(false)){
                 Disease dis = new Disease();
                 dis.setSeverity(diseaseConfig.getInt("Diseases." + key + ".Severity Level"));
                 dis.setSpreadable(diseaseConfig.getBoolean("Diseases." + key + ".Spreadable"));
                 dis.setAirSpread(diseaseConfig.getBoolean("Diseases." + key + ".Use Air Spread"));
                 dis.setAirSpreadChance(diseaseConfig.getDouble("Diseases." + key + ".Air Spread Chance"));
                 dis.setSpreadDistance(diseaseConfig.getDouble("Diseases." + key + ".Air Spread Distance"));
                 dis.setHitSpread(diseaseConfig.getBoolean("Diseases." + key + ".Use Hit Spread"));
                 dis.setHitSpreadChance(diseaseConfig.getDouble("Diseases." + key + ".Hit Spread Chance"));
                 dis.setStackable(diseaseConfig.getBoolean("Diseases." + key + ".Stackable"));
                 dis.setFatal(diseaseConfig.getBoolean("Diseases." + key + ".Fatal"));
                 dis.setTicksBeforeDeath(diseaseConfig.getInt("Diseases." + key + ".Ticks Before Death"));
                 dis.setGetsWorse(diseaseConfig.getBoolean("Diseases." + key + ".Gets Worse"));
                 dis.setProgressiveDisease(diseaseConfig.getString("Diseases." + key + ".Morphs Into"));
                 dis.setRegressiveDisease(diseaseConfig.getString("Diseases." + key + ".Regresses To"));
                 dis.setProgressionTime(diseaseConfig.getInt("Diseases." + key + ".Morph Progression Timer"));
                 dis.setEffectDamage(diseaseConfig.getBoolean("Diseases." + key + ".Effect Damage Dealt"));
                 dis.setAdjustDamage(diseaseConfig.getDouble("Diseases." + key + ".Adjust Damage Dealt By"));
                 dis.setMiss(diseaseConfig.getBoolean("Diseases." + key + ".Miss Chance"));
                 dis.setMissChance(diseaseConfig.getDouble("Diseases." + key + ".Miss Percentage"));
                 dis.setEffectDamageTaken(diseaseConfig.getBoolean("Diseases." + key + ".Effect Damage Taken"));
                 dis.setAdjustDamageTaken(diseaseConfig.getDouble("Diseases." + key + ".Adjust Damage Taken By"));
                 List<PotionEffect> pots = new ArrayList<>();
                for (String type : diseaseConfig.getConfigurationSection("Diseases." + key + ".effects").getKeys(false)){
                     PotionEffect pE = new PotionEffect(PotionEffectType.getByName(type.toUpperCase()), diseaseConfig.getInt("Diseases." + key + ".Effects." + type + ".Duration"), diseaseConfig.getInt("Diseases." + key + ".Effects." + type + ".Amplifier"));
                     pots.add(pE);
                 }
                 dis.setDiseasePots(pots);
                 dis.setDiseaseTime(diseaseConfig.getInt("Diseases." + key + ".Behavior Timer"));
                 dis.setDot(diseaseConfig.getBoolean("Diseases." + key + ".Use Damage Over Time"));
                 dis.setDotDamage(diseaseConfig.getDouble("Diseases." + key + ".Damage Per DOT Tick"));
                 dis.setDotTimer(diseaseConfig.getInt("Diseases." + key + ".Dot Tick Time"));
                 dis.setHealthEffect(diseaseConfig.getBoolean("Diseases." + key + ".Adjust Health"));
                 dis.setHealthChange(diseaseConfig.getDouble("Diseases." + key + ".Adjust Health Amount"));
                 RpgAPI.diseases.add(dis);
                 System.out.print(dis.getDiseaseName());
             }
         }
     }
 }
