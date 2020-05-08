 package controllers;
 import play.mvc.*;
 import play.mvc.Http.RequestBody;
 import play.i18n.Lang;
 
 import java.util.List;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 /**
  * Static Class handling the localisation of the webpage and other parts of the program
  * providing mainly two methods for translation, one with fallback (get) the other without(getString)
  * @author Lukas Ehnle
  *
  */
 public class Localize extends Controller{
 	private static List<Lang> available = Lang.availables();
 	//default language
 	private static String standard = "en";
 	
 	//private because singleton
 	private Localize(Lang standard){
 	}
 	
 
 	/**
 	 * method to get a localized string
 	 * @param message the key of the string to localize
 	 * @return returns a localized string or if the language is not supported 
 	 * the string in the standard language and if that is also not found returns the key
 	 */
 	public static String get(String message) {
 		String loc = message;
 		try {
 			loc = ResourceBundle.getBundle("messages_" + language()).getString(message);
 			//default language if message not found
 		} catch (MissingResourceException e) {
 			try {
 				loc = ResourceBundle.getBundle("messages_" + standard).getString(message);
 			} catch (MissingResourceException f) {}
 		}
 		return loc;
 		
 	}
 	/**
 	 * method to get a localized string without standard language fallback
 	 * @param message the key of the string to localize
 	 * @return returns a localized string or an empty string
 	 */
 	public static String getString(String message) {
 		return ResourceBundle.getBundle("messages_" + language()).getString(message);
 	}
 	
 	/**
 	 * Method to get all available translations
 	 * @return returns a string array with abbreviations e.g. "de" or "en" of the available languages
 	 */
 	public static String[] getAvailables() {
 		String[] tmp = new String[available.size()];
 		for(int i = 0; i < tmp.length; i++) {
 			tmp[i] = available.get(i).code();
 		}
 		return tmp;
 	}
 	/**
 	 * method to automatically localize the page in the best language
 	 * @return returns a language in string form e.g. "en" or "de"
 	 * if language was chosen manually that language is returned, else the preferred languages
 	 * as sent by the browser are checked if any fit the available languages and that language is returned
 	 * else the standard language is returned.
 	 */
 	public static String language() {
 		String lang = session("lang");
 		if(lang == null || !available.contains(Lang.forCode(lang))){
 	    	List<Lang> want = request().acceptLanguages();
 	    	for (Lang w: want) {
 	    		if(available.contains(w)) {
 	    			session("lang", w.code());
 	    			lang = w.code();
 	    			break;
 	    		}
 	    	}
 	    	if(lang == null) {
 	    		lang = standard;
 	    	}
 		}
     	return lang;
 	}
	/**
	 * method to change the previous language to a selected
	 */
	public static void changeLanguage(){
    	RequestBody body = request().body();
    	if(body.asFormUrlEncoded() != null && body.asFormUrlEncoded().containsKey("lang")) {
	    	session("lang", request().body().asFormUrlEncoded().get("lang")[0]);
    	}
    }
 }
