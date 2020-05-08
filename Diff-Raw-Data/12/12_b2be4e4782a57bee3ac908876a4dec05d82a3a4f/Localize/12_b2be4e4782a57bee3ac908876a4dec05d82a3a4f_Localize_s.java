 package web.controllers;
 
 import play.i18n.Lang;
 import play.mvc.Controller;
 
 import java.util.List;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 /**
  * Singleton/static Class handling the localization of the web page and other parts of the program
  * providing mainly two methods for translation, one with fallback (get) the other without(getString).
  * the second can be used without instantiating the singleton class, it then returns the standard language.
  * 
  * @author Lukas Ehnle, PSE Gruppe 14
  *
  */
 public class Localize extends Controller {
 	/** instance of this singleton class */
 	private static Localize instance = null;
 	/**
 	 * a list containing all available languages,
 	 * meaning a language file (e.g. messages_en.properties) was found
 	 */
 	private static List<Lang> available;
 	/**
 	 * the default language, which is returned by the fallback mechanism,
 	 * if the requested language doesn't exist.
 	 */
 	private static String standard = "en";
 	
 	/**
 	 * ResourceBundle control, with configuration that allows
 	 * to read utf-8 encoded property files.
 	 */
 	private static UTF8Control control = new UTF8Control();
 	
 	/**
 	 * private constructor because of singleton pattern used.
 	 */
 	private Localize() {
 		available = Lang.availables();
 	}
 	
 
 	/**
 	 * method to get a localized string.
 	 * @param message the key of the string to localize
 	 * @return returns a localized string or if the language is not supported 
 	 * the string in the standard language and if that is also not found returns the key
 	 */
 	public static String get(String message) {
 		return Localize.instance().get(message, language()); 
 	}
 	
 	/**
 	 * method to get a localized string without standard language fallback.
 	 * @param message the key of the string to localize
 	 * @return returns a localized string or an empty string
 	 */
 	public static String getString(String message) {
 		return ResourceBundle.getBundle("messages_" + language()).getString(message);
 	}
 	
 	/**
 	 * Method to get all available translations.
 	 * @return returns a string array with abbreviations e.g. "de" or "en" of the available languages
 	 */
 	public static String[] getAvailables() {
 		String[] tmp = new String[available.size()];
 		for (int i = 0; i < tmp.length; i++) {
 			tmp[i] = available.get(i).code();
 		}
 		return tmp;
 	}
 	/**
 	 * method to automatically localize the page in the best language.
 	 * @return returns a language in string form e.g. "en" or "de"
 	 * if language was chosen manually that language is returned, else the preferred languages
 	 * as sent by the browser are checked if any fit the available languages and that language is returned
 	 * else the standard language is returned.
 	 */
 	public static String language() {
 		//could be the 
		if(instance == null) {
 			return standard;
 		}
 		String lang = session("lang");
 		if (lang == null || !available.contains(Lang.forCode(lang))) {
 	    	List<Lang> want = request().acceptLanguages();
 	    	for (Lang w: want) {
 	    		if (available.contains(w)) {
 	    			session("lang", w.code());
 	    			lang = w.code();
 	    			break;
 	    		}
 	    	}
 	    	if (lang == null) {
 	    		lang = standard;
 	    	}
 		}
     	return lang;
 	}
 	
 	/**
 	 * get localization for a key and a language.
 	 * with automatic fallback to standard language.
 	 * 
 	 * @param key the key for a localized string
 	 * @param lang the language to get
 	 * @return returns a localized string
 	 */
 	private String get(String key, String lang) {
 		try {
 			return ResourceBundle.getBundle("messages_" + lang, control).getString(key);
 			//default language if message not found
 		} catch (MissingResourceException e) {
 			if (!lang.equals(standard)) {
 				return get(key, standard);
 			} else {
 				return key;
 			}
 		}
 	}
 	/**
 	 * instantiates this class if not already done and returns this instance.
 	 * @return returns a Localize object.
 	 */
 	private static Localize instance() {
		if(Localize.instance == null) {
 			Localize.instance = new Localize();
 		}
 		return Localize.instance;
 	}
 }
