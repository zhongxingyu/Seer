 /*
  * utils - I18n.java - Copyright © 2009 David Roden
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
 
 package net.pterodactylus.util.i18n;
 
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Field;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.MissingResourceException;
 import java.util.Properties;
 import java.util.StringTokenizer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.KeyStroke;
 
 import net.pterodactylus.util.io.Closer;
 import net.pterodactylus.util.logging.Logging;
 
 /**
  * Class that handles i18n.
  *
  * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
  */
 public class I18n {
 
 	/** Logger. */
 	private static final Logger logger = Logging.getLogger(I18n.class.getName());
 
 	/** List of I18nables that are notified when the language changes. */
 	private static final List<I18nable> i18nables = new ArrayList<I18nable>();
 
 	/** The base name for the properties. */
 	private static String applicationName;
 
 	/** The path of the i18n properties. */
 	private static String propertiesPath;
 
 	/** The current locale. */
 	private static Locale currentLocale;
 
 	/** The default language. */
 	private static Properties defaultLanguage;
 
 	/** The current language. */
 	private static Properties currentLanguage;
 
 	/**
 	 * Sets the name of the application. The name is used as base name for the
 	 * properties files.
 	 *
 	 * @param applicationName
 	 *            The name of the application
 	 * @param propertiesPath
 	 *            The path of the properties (including a leading slash but not
 	 *            a trailing slash)
 	 */
 	public static void setApplicationName(String applicationName, String propertiesPath) {
 		I18n.applicationName = applicationName;
 		defaultLanguage = new Properties();
 		InputStream inputStream = null;
 		try {
 			inputStream = I18n.class.getResourceAsStream(propertiesPath + "/" + applicationName + ".properties");
 			if (inputStream != null) {
 				defaultLanguage.load(inputStream);
 			}
 		} catch (IOException e) {
 			/* something is fucked. */
 		}
 		setLocale(Locale.getDefault(), false);
 	}
 
 	/**
 	 * Returns whether the given key exists.
 	 *
 	 * @param key
 	 *            The key to check for
 	 * @return {@code true} if the key exists, {@code false} otherwise
 	 */
 	public static boolean has(String key) {
 		if (applicationName == null) {
 			throw new IllegalStateException("applicationName has not been set");
 		}
 		return currentLanguage.containsKey(key);
 	}
 
 	/**
 	 * Returns the translated value for a key. The translated values may contain
 	 * placeholders that are replaced with the given parameters.
 	 *
 	 * @see MessageFormat
 	 * @param key
 	 *            The key to get
 	 * @param parameters
 	 *            The parameters in case the translated value contains
 	 *            placeholders
 	 * @return The translated message, or the key itself if no translation could
 	 *         be found
 	 */
 	public static String get(String key, Object... parameters) {
 		if (applicationName == null) {
 			throw new IllegalStateException("applicationName has not been set");
 		}
 		String value = null;
 		value = currentLanguage.getProperty(key);
 		if (value == null) {
			logger.log(Level.WARNING, "please fix “" + key + "” for “" + getLocale().getLanguage() + "”!", new Throwable());
 			/* TODO - replace with value when done! */
 			return null;
 		}
 		if ((parameters != null) && (parameters.length > 0)) {
 			return MessageFormat.format(value, parameters);
 		}
 		return value;
 	}
 
 	/**
 	 * Returns the keycode from the value of the given key. You can specify the
 	 * constants in {@link KeyEvent} in the properties file, e.g. VK_S for the
 	 * keycode ‘s’ when used for mnemonics.
 	 *
 	 * @param key
 	 *            The key under which the keycode is stored
 	 * @return The keycode
 	 */
 	public static int getKey(String key) {
 		if (applicationName == null) {
 			throw new IllegalStateException("applicationName has not been set");
 		}
 		String value = currentLanguage.getProperty(key);
 		if ((value != null) && value.startsWith("VK_")) {
 			try {
 				Field field = KeyEvent.class.getField(value);
 				return field.getInt(null);
 			} catch (SecurityException e) {
 				/* ignore. */
 			} catch (NoSuchFieldException e) {
 				/* ignore. */
 			} catch (IllegalArgumentException e) {
 				/* ignore. */
 			} catch (IllegalAccessException e) {
 				/* ignore. */
 			}
 		}
 		logger.log(Level.WARNING, "please fix “" + key + "”!", new Throwable());
 		return KeyEvent.VK_UNDEFINED;
 	}
 
 	/**
 	 * Returns a key stroke for use with swing accelerators.
 	 *
 	 * @param key
 	 *            The key of the key stroke
 	 * @return The key stroke, or <code>null</code> if no key stroke could be
 	 *         created from the translated value
 	 */
 	public static KeyStroke getKeyStroke(String key) {
 		if (applicationName == null) {
 			throw new IllegalStateException("applicationName has not been set");
 		}
 		String value = currentLanguage.getProperty(key);
 		if (value == null) {
 			return null;
 		}
 		StringTokenizer keyTokens = new StringTokenizer(value, "+- ");
 		int modifierMask = 0;
 		while (keyTokens.hasMoreTokens()) {
 			String keyToken = keyTokens.nextToken();
 			if ("ctrl".equalsIgnoreCase(keyToken)) {
 				modifierMask |= InputEvent.CTRL_DOWN_MASK;
 			} else if ("alt".equalsIgnoreCase(keyToken)) {
 				modifierMask |= InputEvent.ALT_DOWN_MASK;
 			} else if ("shift".equalsIgnoreCase(keyToken)) {
 				modifierMask |= InputEvent.SHIFT_DOWN_MASK;
 			} else {
 				if (keyToken.startsWith("VK_")) {
 					if (keyToken.equals("VK_UNDEFINED")) {
 						return null;
 					}
 					try {
 						Field field = KeyEvent.class.getField(keyToken);
 						return KeyStroke.getKeyStroke(field.getInt(null), modifierMask);
 					} catch (SecurityException e) {
 						/* ignore. */
 					} catch (NoSuchFieldException e) {
 						/* ignore. */
 					} catch (IllegalArgumentException e) {
 						/* ignore. */
 					} catch (IllegalAccessException e) {
 						/* ignore. */
 					}
 				}
 				return KeyStroke.getKeyStroke(keyToken.charAt(0), modifierMask);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Sets the current locale.
 	 *
 	 * @param newLocale
 	 *            The new locale to use
 	 */
 	public static void setLocale(Locale newLocale) {
 		if (applicationName == null) {
 			throw new IllegalStateException("applicationName has not been set");
 		}
 		setLocale(newLocale, true);
 	}
 
 	/**
 	 * Sets the current locale.
 	 *
 	 * @param newLocale
 	 *            The new locale to use
 	 * @param notify
 	 *            <code>true</code> to notify registered {@link I18nable}s after
 	 *            the language was changed
 	 */
 	private static void setLocale(Locale newLocale, boolean notify) {
 		if (applicationName == null) {
 			throw new IllegalStateException("applicationName has not been set");
 		}
 		currentLocale = newLocale;
 		InputStream inputStream = null;
 		try {
 			currentLanguage = new Properties(defaultLanguage);
 			if (newLocale == Locale.ENGLISH) {
 				if (notify) {
 					notifyI18nables();
 				}
 				return;
 			}
 			inputStream = I18n.class.getResourceAsStream(propertiesPath + "/" + applicationName + "_" + newLocale.getLanguage() + ".properties");
 			if (inputStream != null) {
 				currentLanguage.load(inputStream);
 				if (notify) {
 					notifyI18nables();
 				}
 			}
 		} catch (MissingResourceException mre1) {
 			currentLocale = Locale.ENGLISH;
 		} catch (IOException ioe1) {
 			currentLocale = Locale.ENGLISH;
 		} finally {
 			Closer.close(inputStream);
 		}
 	}
 
 	/**
 	 * Returns the current locale.
 	 *
 	 * @return The current locale
 	 */
 	public static Locale getLocale() {
 		return currentLocale;
 	}
 
 	/**
 	 * Finds all available locales.
 	 *
 	 * @return All available locales
 	 */
 	public static List<Locale> findAvailableLanguages() {
 		List<Locale> availableLanguages = new ArrayList<Locale>();
 		availableLanguages.add(Locale.ENGLISH);
 		availableLanguages.add(Locale.GERMAN);
 		return availableLanguages;
 	}
 
 	/**
 	 * Registers the given I18nable to be updated when the language is changed.
 	 *
 	 * @param i18nable
 	 *            The i18nable to register
 	 */
 	public static void registerI18nable(I18nable i18nable) {
 		i18nables.add(i18nable);
 	}
 
 	/**
 	 * Deregisters the given I18nable to be updated when the language is
 	 * changed.
 	 *
 	 * @param i18nable
 	 *            The i18nable to register
 	 */
 	public static void deregisterI18nable(I18nable i18nable) {
 		i18nables.remove(i18nable);
 	}
 
 	//
 	// PRIVATE METHODS
 	//
 
 	/**
 	 * Notifies all registered {@link I18nable}s that the language was changed.
 	 */
 	private static void notifyI18nables() {
 		for (I18nable i18nable : i18nables) {
 			i18nable.updateI18n();
 		}
 	}
 
 }
