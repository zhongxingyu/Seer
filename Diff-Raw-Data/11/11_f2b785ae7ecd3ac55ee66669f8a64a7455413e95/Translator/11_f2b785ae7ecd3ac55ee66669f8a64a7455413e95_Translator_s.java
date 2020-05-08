 package pl.spaceshooters.util;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.Map;
 
 import pl.spaceshooters.config.Configuration;
 
 public final class Translator {
 	
 	private HashMap<String, String> translations;
 	
 	private Translator() {
 		translations = new HashMap<>();
 		
		Language lang = Language.values()[Configuration.getConfiguration().getInt(Configuration.LAUNCHER_LANG)];
 		this.changeLanguage(lang);
 	}
 	
 	public void addTranslation(String key, String value) {
 		translations.put(key, value);
 	}
 	
 	public void addAllTranslations(Map<String, String> map) {
 		translations.putAll(map);
 	}
 	
 	public String getTranslated(String key) {
 		return translations.get(key) != null ? translations.get(key) : key;
 	}
 	
 	public void changeLanguage(Language lang) {
 		this.parse(new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/data/vanilla/lang/" + lang.getFileName() + ".slf"))));
 	}
 	
 	private HashMap<String, String> parse(BufferedReader r) {
 		try {
 			String line;
 			while ((line = r.readLine()) != null) {
 				String[] put = line.split("=", 2);
 				translations.put(put[0], put[1]);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		return translations;
 	}
 	
 	public static Translator getTranslator() {
 		return Instance.INSTANCE;
 	}
 	
 	private static final class Instance {
 		private static final Translator INSTANCE = new Translator();
 	}
 }
