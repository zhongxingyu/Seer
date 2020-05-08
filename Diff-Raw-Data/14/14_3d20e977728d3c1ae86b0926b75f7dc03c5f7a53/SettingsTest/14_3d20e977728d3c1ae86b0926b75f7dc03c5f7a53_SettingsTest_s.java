 /*
  * mr-core
  * Copyright (C) 2012 Nicolas Magré
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
 package fr.free.movierenamer.settings;
 
 import java.util.Locale;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import fr.free.movierenamer.settings.Settings.SettingsProperty;
 
 /**
  * Class SettingsTest
 * 
  * @author Simon QUÉMÉNEUR
  */
 public class SettingsTest {
  
   private Settings instance;
 
  @Before 
   public void init() {
    Settings instance = Settings.getInstance();
     instance.setAutosave(false);
   }
  
   @Test
   public void get() {
     instance.clear();
     Assert.assertEquals(Locale.ENGLISH, instance.getAppLanguage());
   }
 
   @Test
   public void set() {
     instance.set(SettingsProperty.appLanguage, Locale.FRENCH);
     Assert.assertEquals(Locale.FRENCH, instance.getAppLanguage());
     instance.set(SettingsProperty.appLanguage, Locale.GERMAN);
     Assert.assertEquals(Locale.GERMAN, instance.getAppLanguage());
   }
 }
