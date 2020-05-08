 package ch.ethz.origo.jerpa.jerpalang;
 
 import java.io.IOException;
 import java.util.Properties;
 
 /**
  * 
  * 
 * @author Vaclav Souhrada (v.souhrada@gmail.com)
 * @version 0.1.1 (1/31/2010)
  * @since 0.1.0 (09/15/09)
  */
 public class LangUtils {
 
 	public static final String MAIN_FILE_PATH = "ch.ethz.origo.jerpa.jerpalang.JERPA";
 	public static final String JERPA_LANG_FILE = "ch/ethz/origo/jerpa/jerpalang/JERPA_LANGUAGE.properties";
 	
 	public static final String SIGNAL_PERSP_LANG_FILE_KEY = "perspective.signalprocessing.lang"; 
 
 	public static String[] getListOfLanguage() {
 		return LangUtils.loadProperties(LangUtils.MAIN_FILE_PATH).getProperty(
 				"menu.language.item").trim().split(",");
 	}
 
 	/**
 	 * Load properties
 	 * 
 	 * @throws PropertiesException
 	 * @version 0.1.0
 	 * @since 0.1.0
 	 */
 	public static java.util.Properties loadProperties(String path) {
 		Properties properties = new Properties();
 		try {
 			properties.load(ClassLoader.getSystemResourceAsStream(path));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return properties;
 	}
 	
 	
 	public static String getPerspectiveLangPathProp(String propertyName) {
 		return LangUtils.loadProperties(LangUtils.JERPA_LANG_FILE).getProperty(propertyName);
 	}
 	
 	public static void main(String[] args) {
 		System.out.println(getPerspectiveLangPathProp("perspective.signalprocessing.lang"));
 	}
 
}
