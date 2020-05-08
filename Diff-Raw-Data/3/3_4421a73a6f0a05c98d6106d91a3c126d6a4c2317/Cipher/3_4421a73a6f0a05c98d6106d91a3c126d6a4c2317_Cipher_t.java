 /*
  * Copyright (C) 2013 AE97
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.ae97.totalpermissions.lang;
 
 import java.io.InputStream;
 import java.net.URL;
 import java.util.logging.Level;
 import net.ae97.totalpermissions.TotalPermissions;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 /**
  * @version 0.2
  * @author 1Rogue
  * @since 0.2
  */
 public class Cipher {
 
     private YamlConfiguration langFile;
     private final String langFileLoc = "https://raw.github.com/AE97/TotalPermissions/master/lang/";
 
     public Cipher(String language) {
         language += ".yml";
         try {
            InputStream temp = TotalPermissions.getPlugin().getResource(language);
            this.setLangFile(YamlConfiguration.loadConfiguration(temp));
         } catch (NullPointerException e) {
             TotalPermissions.getPlugin().getLogger().log(Level.SEVERE, "Language resource file is NULL! Trying web files instead...");
             try {
                 URL upstr = new URL(langFileLoc + language);
                 InputStream langs = upstr.openStream();
                 this.setLangFile(YamlConfiguration.loadConfiguration(langs));
                 langs.close();
             } catch (Exception ex) {
                 TotalPermissions.getPlugin().getLogger().log(Level.SEVERE, "Error grabbing language file from web!");
                 TotalPermissions.getPlugin().getLogger().log(Level.SEVERE, "Defaulting to english (en_US)");
                 this.setLangFile(YamlConfiguration.loadConfiguration(TotalPermissions.getPlugin().getResource("en_US.yml")));
             }
         }
     }
 
     private void setLangFile(YamlConfiguration file) {
         langFile = file;
     }
 
     public FileConfiguration getLangFile() {
         return langFile;
     }
 
     public String getString(String path) {
         return getString(path, "", "");
     }
 
     public String getString(String path, String varOne) {
         return getString(path, varOne, "");
     }
 
     public String getString(String path, String varOne, String varTwo) {
         return langFile.getString(path).replace("{0}", varOne).replace("{1}", varTwo);
     }
 }
