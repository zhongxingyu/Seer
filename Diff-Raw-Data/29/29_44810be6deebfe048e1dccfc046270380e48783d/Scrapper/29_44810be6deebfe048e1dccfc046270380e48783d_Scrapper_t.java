 /*
  * movie-renamer-core
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
 package fr.free.movierenamer.scrapper;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Locale;
 
 import fr.free.movierenamer.settings.Settings;
 import fr.free.movierenamer.utils.Cache;
 import fr.free.movierenamer.utils.CacheObject;
 import fr.free.movierenamer.utils.LocaleUtils.AvailableLanguages;
 import fr.free.movierenamer.utils.LocaleUtils.Language;
 import fr.free.movierenamer.utils.ScrapperUtils.AvailableApiIds;
import java.util.logging.Level;
 
 /**
  * Class Scrapper
  *
  * @author Nicolas Magré
  * @author Simon QUÉMÉNEUR
  */
 public abstract class Scrapper {
 
   private final Locale defaultLanguage;
   private final List<AvailableLanguages> supportedLanguages;
   private Locale language;
 
   protected Scrapper(AvailableLanguages... supportedLanguages) {
     if (supportedLanguages == null || supportedLanguages.length == 0 || supportedLanguages[0] == null) {
       throw new NullPointerException("defaultLanguage must not be null");
     } else {
       this.defaultLanguage = supportedLanguages[0].getLocale();
       this.supportedLanguages = Arrays.asList(supportedLanguages);
     }
   }
 
   public abstract String getName();
 
   protected abstract String getHost();
 
   protected final CacheObject getCache() {
     String host = getHost();
     String cacheName = getCacheName();
     if (host != null && cacheName != null) {
       return new CacheObject(host, Cache.getCache(cacheName));
     } else {
       return null;
     }
   }
 
   protected String getCacheName() {
     return null;
   }
 
   public final List<AvailableLanguages> getSupportedLanguages() {
     return supportedLanguages;
   }
 
   protected final Locale getLanguage() {
     if (hasLanguageSupport() && language != null) {
       return language;
     } else {
       return defaultLanguage;
     }
   }
 
   public final boolean hasLanguageSupport() {
     return supportedLanguages != null && supportedLanguages.size() > 1;
   }
 
   public final void setLanguage(Locale language) {
    // 2013-03-08:EV:if no language support, choose the supported one.
    if (!hasLanguageSupport()) { 
      for (Language lang : getSupportedLanguages()) { this.language = lang.getLocale();break; }
      Settings.LOGGER.log(Level.WARNING, "Try to set Language ({0}) to scrapper ({1}) which has no language support ! Select default language ({0})", new Object[]{language, getName(),this.language});
     }
    else {
        this.language = null;
        for (Language lang : getSupportedLanguages()) {
            if (lang.getLocale().getLanguage().equals(language.getLanguage())) {
                this.language = lang.getLocale();
                break;
            }
        }
        if (this.language == null) {
            throw new NullPointerException("Try to set Language ("+language+") to scrapper ("+getName()+") which has no support for it ! : ");
        }
     }
   }
 
   @Override
   public final String toString() {
     return String.format("%s", getName());
   }
 }
