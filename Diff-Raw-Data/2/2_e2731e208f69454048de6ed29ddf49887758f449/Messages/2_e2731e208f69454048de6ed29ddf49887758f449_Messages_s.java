 package org.hhu.c2c.openlr.l10n;
 
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 /**
  * Accessor to the localization bundle of the application
  * 
  * @author Oliver Schrenk <oliver.schrenk@uni-duesseldorf.de>
  * @version %I%, %G%
  * 
  */
 public class Messages {
 
 	/**
 	 * The bundle name
 	 */
	private static final String BUNDLE_NAME = "org.hhu.c2c.openlr.i10n"; //$NON-NLS-1$
 
 	/** Holds the localization bundle */
 	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
 			.getBundle(BUNDLE_NAME);
 
 	/**
 	 * Prevent the class from being instantiated
 	 */
 	private Messages() {
 	}
 
 	/**
 	 * Returns the bundle string with the given key
 	 * 
 	 * @param key
 	 * @return the bundle string with the given key
 	 */
 	public static String getString(String key) {
 		try {
 			return RESOURCE_BUNDLE.getString(key);
 		} catch (MissingResourceException e) {
 			return '!' + key + '!';
 		}
 	}
 
 	/**
 	 * Returns the formatted bundle string with the given key
 	 * 
 	 * @param key
 	 * @param objects
 	 *            the objects thatare used in the formatted string
 	 * @return the formatted bundle string with the given key
 	 */
 	public static String getString(String key, Object... objects) {
 		try {
 			return String.format(RESOURCE_BUNDLE.getString(key), objects);
 		} catch (MissingResourceException e) {
 			return '!' + key + '!';
 		}
 	}
 }
