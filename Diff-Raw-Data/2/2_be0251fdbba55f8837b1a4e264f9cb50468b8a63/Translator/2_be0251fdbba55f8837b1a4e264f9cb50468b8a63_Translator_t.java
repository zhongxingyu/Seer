 package spaceshooters.localization;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 
 import spaceshooters.config.Configuration;
 
 public final class Translator {
 	
 	private static final Translator INSTANCE = new Translator();
 	
 	private HashMap<String, String> translations;
 	
 	private Translator() {
 		translations = new HashMap<>();
 		
 		String lang = Configuration.getConfiguration().getLauncher(Configuration.LAUNCHER_LANG);
 		
 		if (lang.equalsIgnoreCase("polski")) {
 			this.parse(new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/lang/polish.slf"))));
 		} else {
 			this.parse(new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/lang/english.slf"))));
 		}
 	}
 	
 	public String getTranslated(String key) {
		return translations.get(key) != null ? translations.get(key) : key;
 	}
 	
 	private HashMap<String, String> parse(BufferedReader r) {
 		try {
 			String line;
 			while ((line = r.readLine()) != null) {
 				String[] put = line.split("=", 2);
 				System.out.print(put[0] + " = " + put[1]);
 				System.out.println();
 				translations.put(put[0], put[1]);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		return translations;
 	}
 	
 	public static Translator getTranslator() {
 		return INSTANCE;
 	}
 }
