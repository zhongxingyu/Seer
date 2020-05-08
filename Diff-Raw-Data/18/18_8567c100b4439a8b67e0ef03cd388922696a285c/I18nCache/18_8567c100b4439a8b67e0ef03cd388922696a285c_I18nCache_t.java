 package de.hswt.hrm.i18n;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import org.osgi.framework.Bundle;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.hswt.hrm.common.BundleUtil;
 
 /**
  * Acts like a common cache for all translation files. This should not be used directly.
  * Its used by the I18n class.
  */
 public class I18nCache {
 	private final static Logger LOG = LoggerFactory.getLogger(I18nCache.class);
 	private static Map<String, Properties> cache = new HashMap<>();
 	
 	/**
 	 * Get the translations for a bundle and a locale.
 	 * 
 	 * @param bundle 
 	 * @param locale
 	 * @return Translations for the given bundle and locale.
 	 * @throws IOException
 	 */
 	public Properties getTranslations(final Bundle bundle, final String locale) 
 			throws IOException {
 		
 		String symbolicName = getSymcolicName(bundle, locale);
 		
 		if (cache.containsKey(symbolicName)) {
 			return cache.get(symbolicName);
 		}
 		
 		return load(bundle, locale);
 	}
 	
 	private String getSymcolicName(final Bundle bundle, final String locale) {
 		return bundle.getSymbolicName() + "." + locale;
 	}
 	
 	private Properties load(final Bundle bundle, final String locale) throws IOException {
 		String symbolicName = getSymcolicName(bundle, locale);
 
 		// TODO: refactor folder to a separate folder with default "i18n"
 		String path = "resource/i18n/" + locale.toLowerCase() + ".properties";
 		InputStream inStream = BundleUtil.getStreamForFile(bundle, path);
 		Properties props = loadFromFile(inStream);
 		inStream.close();
 
 		cache.put(symbolicName, props);
		LOG.debug("Translation load from '" + path + "'.");
 		return props;
 	}
 	
     private Properties loadFromFile(final InputStream inStream) throws IOException {
         Properties props = new Properties();
         props.load(inStream);
         
         return props;
     }
 }
