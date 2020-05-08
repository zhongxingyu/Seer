 package net.mms_projects.copyit;
 
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 public class Messages {
 	private static final String BUNDLE_NAME = "messages"; //$NON-NLS-1$
 
 	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
 			.getBundle(BUNDLE_NAME);
 
 	private Messages() {
 	}
 
 	public static String getString(String key) {
		String text = AndroidResourceLoader.getString("@string/" + key);
		if (text != null) {
			return text;
		}
 		try {
 			return RESOURCE_BUNDLE.getString(key);
 		} catch (MissingResourceException e) {
			return '!' + key + '!';
 		}
 	}
 
 	public static String getString(String key, Object... formatArgs) {
 		String raw = getString(key);
 		return String.format(raw, formatArgs);
 	}
 }
