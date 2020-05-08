 package plugins.echo.i18n;
 
 import freenet.support.Logger;
 
 import java.util.Properties;
 
 import java.util.MissingResourceException;
 import java.io.IOException;
 
 import nu.xom.Nodes;
 import nu.xom.Element;
 import nu.xom.Text;
 import nu.xom.Document;
 
 import java.io.*; // FIXME
 
 /**
 * 	This class provides a trivial internationalization framework
 */
 public class I18n {
 	
 	public static final String[] AVAILABLE_LANGUAGES = { "en", "fr" };
 	public static final String DEFAULT_LANGUAGE = "en"; 
 	public static final String PREFIX = "echo.i18n.";
 	public static final String SUFFIX = ".properties";
 	
 	private static String selectedLanguage;
 	private static Properties translation  = new Properties();
 	private static Properties defaultTranslation = null;
 
 	
 	private static final I18n INSTANCE = new I18n();
 		
 	private I18n() {}
 
 	
 	/**
 	* 	Set the language
 	* 	@param language the ISO code of the language 
 	*/
 	public static void setLanguage(String language) throws MissingResourceException {
 		
 		Logger.normal("I18n", "Changing the current language to : " + language);
 		loadTranslation(language, translation);
 		
 		if(! language.equals(DEFAULT_LANGUAGE))
 			loadTranslation(DEFAULT_LANGUAGE, defaultTranslation);
 		
 		selectedLanguage = language;
 	}
 	
 	/**
 	*	Load a translation
 	*	@param language the ISO code of the language
 	*	@param props the properties to load into 
 	*/
 	private static void loadTranslation(String language, Properties props) throws MissingResourceException {
 		
 		if(props == null)
 			props = new Properties();
 		try {
			props.load(INSTANCE.getClass().getResourceAsStream("/i18n/" + PREFIX + language + SUFFIX));
 		} catch (IOException ioe) {
 			Logger.error("I18n", "IOException while accessing the " + language +"file" + ioe.getMessage(), ioe);
 			throw new MissingResourceException("Unable to load the translation file for " + language, "i18n", language);
 		}
 	}
 	
 	/**
 	*	Return the translation of the key	
 	*	@param key
 	*	@return the translated String in the selected language, the translated String in the default language or the key itself if the key is not found in the default language		
 	*/
 	public static String getString(String key) {
 	
 		String str = translation.getProperty(key);
 		if(str != null)
 			return str;
 		else {
 			Logger.normal("I18n", "The translation for " + key + " hasn't been found (" + selectedLanguage + ")! please tell the maintainer.");
 			
 			if(selectedLanguage.equals(DEFAULT_LANGUAGE))
 				return key;
 			
 			str = defaultTranslation.getProperty(key);
 			if(str != null)
 				return str;
 			else {
 				Logger.normal("I18n", "The translation for " + key + " hasn't been found in the default language !! Please tell the maintainer.");
 				return key;
 			}
 		}	
 	}
 	
 	/**
 	*	Translate a whole XML document, replace i18n elements by the translations of their keys
 	*	@param doc the nu.xom.Document to translate
 	*/
 	public static void translateXML(Document doc) {
 	
 		nu.xom.Nodes i18nNodes = doc.query("//i18n");	
 		
 		for(int i=0; i < i18nNodes.size(); i++) {
 			
 			String key = ((Element) i18nNodes.get(i)).getAttributeValue("key");
 			String translatedKey = getString(key);
 			
 			i18nNodes.get(i).getParent().replaceChild(i18nNodes.get(i), new Text(translatedKey));
 		}
 	}
 }
