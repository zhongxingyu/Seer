 package net.mms_projects.copyit;
 
import java.util.Locale;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 public class Messages {
 	private static final String BUNDLE_NAME = "messages"; //$NON-NLS-1$
 
	private static ResourceBundle RESOURCE_BUNDLE;
	private static ResourceBundle FALLBACK_RESOURCE_BUNDLE;
 
 	private Messages() {
 	}
 
 	public static String getString(String key) {
 		try {
			return getBundle().getString(key);
		} catch (MissingResourceException e1) {
			try {
				return getFallbackBundle().getString(key);
			} catch (MissingResourceException e2) {
			}
 		}
 		String text = AndroidResourceLoader.getString("@string/" + key);
 		if (text != null) {
 			return text;
 		}
 		return '!' + key + '!';
 	}
 
 	public static String getString(String key, Object... formatArgs) {
 		String raw = getString(key);
 		return String.format(raw, formatArgs);
 	}

	private static ResourceBundle getBundle() {
		Locale locale = Locale.getDefault();
		if (RESOURCE_BUNDLE == null) {
			RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, locale);
		}
		return RESOURCE_BUNDLE;
	}

	private static ResourceBundle getFallbackBundle() {
		if (FALLBACK_RESOURCE_BUNDLE == null) {
			FALLBACK_RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
		}
		return FALLBACK_RESOURCE_BUNDLE;
	}
 }
