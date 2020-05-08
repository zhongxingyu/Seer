 package de.hswt.hrm.i18n;
 
 import java.io.IOException;
 import java.util.Properties;
 
 import org.osgi.framework.Bundle;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.hswt.hrm.common.Config;
 
 /**
  * Provides methods for internationalization.
  * Translation files should be included in the plugin bundle of the calling class.
  */
 public final class I18n {
 	private final static Logger LOG = LoggerFactory.getLogger(I18n.class);
     private final I18nCache cache;
     private final Bundle bundle;
     private Config config;
     private Properties translations;
     
     /**
      * Create I18n with bundle and a newly created cache.
      * 
      * @param bundle Bundle for which this class handles translations.
      */
     public I18n(final Bundle bundle) {
     	this(bundle, new I18nCache());
     }
     
     /**
      * Create I18n with bundle and cache.
      * 
      * @param bundle Bundle for which this class handles translations.
      * @param cache Cache is used to cache translation files.
      */
     public I18n(final Bundle bundle, final I18nCache cache) {
     	this.bundle = bundle;
     	this.cache = cache;
     	
     	// TODO maybe we can handle config injection better
     	config = Config.getInstance();
     }
     
     /**
      * Translate the given key into the currently locale (set in the config class).
      * 
      * @param key Key for which the translation should be returned.
      * @return The translation for the key or the key itself if no translation could be found.
      */
     public String tr(final String key) {
     	if (translations == null) {
     		try {
     			// FIXME: en_en should be the default language
     			String locale = config.getProperty(Config.Keys.LOCALE, "de_de");
 				this.translations = cache.getTranslations(bundle, locale);
 			} 
     		catch (IOException e) {
 				LOG.error("Could not load translation file for bundle '"
 						+ bundle.getSymbolicName() + "'.", e);
 				return key;
 			}
     	}
     	
    	if (!translations.containsKey(key)) {
    		LOG.warn("No translation found for '" + escape(key) + "' in '" 
     				+ bundle.getSymbolicName() + "'.");
     	}
         
        return translations.getProperty(key, key);
     }
     
     /**
      * Escape all spaces with a backslash.
      * 
      * @param key
      * @return The escaped key.
      */
     private static String escape(final String key) {
        return key.replace(" ", "\\ ");
     }
 
 }
