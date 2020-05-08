 package com.crystalclash.util;
 
import java.util.Locale;
 import java.util.ResourceBundle;
 
 public class I18n {
	private static Locale currentLocale;
 	private static ResourceBundle messages;
 
 	public static void load() {
		load(new String("en"), new String("US"));
 	}
 
 	public static void load(String country, String language) {
		currentLocale = new Locale(language, country);
		loadResource();
	}

	private static void loadResource() {
		messages = ResourceBundle.getBundle("data/locales/locale", currentLocale);
 	}
 
 	public static String t(String key) {
 		return messages.getString(key);
 	}
}
