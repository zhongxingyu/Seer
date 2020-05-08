 /*
  * Copyright (C) 2013-2014 Dabo Ross <http://www.daboross.net/>
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
 package net.daboross.bukkitdev.skywars.api.translations;
 
 import lombok.NonNull;
 
 /**
  * Static access to SkyTranslations. Short name for convenience
  */
 public class SkyTrans {
 
     private static SkyTranslations instance;
 
     /**
      * Gets the translation for the given key and the given arguments.
      *
      * @param key  The key to search for a translation of.
      * @param args The arguments to apply to the translation.
      * @return The final message to be given in game.
      */
     public static String get(@NonNull TransKey key, Object... args) {
         if (instance == null) {
             return "translation-not-found[" + key.key + "]";
         }
         if (key.args == 0) {
             return instance.get(key);
         } else if (key.args < args.length) {
             throw new IllegalArgumentException("Not enough args for key " + key.key);
         } else {
            return String.format(instance.get(key), args);
         }
     }
 
     public static void setInstance(final SkyTranslations instance) {
         SkyTrans.instance = instance;
     }
 }
