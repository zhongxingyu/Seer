 package com.tacoid.puyopuyo;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Properties;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.files.FileHandle;
 
 public class I18nManager {
 	
 	/* Static section */
 	
 	static private I18nManager instance = null;
 	
 	public static I18nManager getInstance() {
 		if(instance == null) {
 			instance = new I18nManager();
 		}
 		
 		return instance;
 	}
 	
 	/* ************** */
 	
 	public enum Language {
 		/* Lister ici l'ensemble des langages support�s */
 		FRENCH("fr"),
 		ENGLISH("en");
 		
 		private final String string;
 		
 		private Language(String string) {
 			this.string = string;
 		}
 		
 		public String toString() {
 			return string;
 		}
 	}
 	
 	private Properties strings = null;
 	private Language lang;
 	
 	private I18nManager() {
 		lang = Language.FRENCH;
 		strings = new Properties();
 	}
 	
 	public void setLanguage(Language lang) {
 		this.lang = lang;
 		
 		System.out.println("assets/strings-"+lang+".properties");
 		FileHandle file = Gdx.files.internal("strings-"+lang+".properties");		
 
 		try {
 			strings.load(file.read());
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public Language getLanguage() {
 		return lang;
 	}
 	
 	public String getString(String name) {
 		return strings.getProperty(name);
 	}
 
	public void setLanguage(String preference) throws Exception {
 		for(Language l : Language.values()) {
 			if(l.string.equals(preference)) {
 				lang = l;
				return;
 			}
 		}
		throw new Exception("Langage unknown");
 	}
 }
