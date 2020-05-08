 package com.crystalclash.util;
 
import java.io.IOException;
import java.util.PropertyResourceBundle;
 import java.util.ResourceBundle;
 
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

 public class I18n {
 	private static ResourceBundle messages;
 
 	public static void load() {
		load("en", "US");
 	}
 
 	public static void load(String country, String language) {
		FileHandle rootFileHandle = Gdx.files.internal(String.format("data/locales/locale_%s_%s", country, language));
		try {
			messages = new PropertyResourceBundle(rootFileHandle.read());
		} catch (IOException e) {
			load();
		}
 	}
 
 	public static String t(String key) {
 		return messages.getString(key);
 	}
}
