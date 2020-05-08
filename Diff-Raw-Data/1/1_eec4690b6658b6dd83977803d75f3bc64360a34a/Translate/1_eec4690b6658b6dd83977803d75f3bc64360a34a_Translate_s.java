 /*
  * MyResidence, Bukkit plugin for managing your towns and residences
  * Copyright (C) 2011, Michael Hohl
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
 
 package at.co.hohl.myresidence.translations;
 
 import org.bukkit.util.config.Configuration;
 
 import java.io.File;
 import java.util.logging.Logger;
 
 /**
  * Static class used for translating texts.
  *
  * @author Michael Hohl
  */
 public final class Translate {
   private static Configuration translations;
 
   /**
    * Loads the translations from file system.
    *
    * @param languageCode the code of the language to retrieve. (for example de or en).
    */
   public static void load(String languageCode) {
     File translationFile = new File(Translate.class.getClassLoader()
             .getResource("myresidence_" + languageCode.toLowerCase() + ".yml").toExternalForm());
 
     if (!(translationFile.exists() && translationFile.canRead())) {
       translationFile = new File(Translate.class.getClassLoader().getResource("myresidence_en.yml").toExternalForm());
       Logger.getLogger("Minecraft").warning("[MyResidence] Use default translation, " +
               "because there exists no localization for your language.");
     }
 
 
     translations = new Configuration(translationFile);
   }
 
   /**
    * Retrieves a translation for the passed message id.
    *
    * @param messageId the id of the message to retrieve the translation for.
    * @return the translation or a message which tells that the translation is missing.
    */
   public static String get(String messageId) {
     if (translations == null) {
       load("en");
     }
 
     return translations.getString(messageId, "&cMiss translation for: '" + messageId + "'.");
   }
 
   // Do not instantiate these helper class!
 
   private Translate() {
   }
 }
