 package pl.spaceshooters.util;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.Map;
 
 import pl.spaceshooters.main.Spaceshooters;
 
 public final class Translator {
 	
 	private static Translator translator;
 	
 	private final Spaceshooters game;
 	private HashMap<String, String> translations;
 	
 	private Translator(Spaceshooters game) {
 		this.game = game;
 		translations = new HashMap<>();
 		
 		Language lang = Language.values()[game.getConfiguration().getInt("language", 0)];
		this.parse(new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/data/vanilla/lang/" + lang.getFileName() + ".slf"))));
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
 		game.reinit();
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
 	
 	public static synchronized Translator init(Spaceshooters game) {
 		if (game == null)
 			return null;
 		
 		return translator = (translator == null ? new Translator(game) : translator);
 	}
 }
