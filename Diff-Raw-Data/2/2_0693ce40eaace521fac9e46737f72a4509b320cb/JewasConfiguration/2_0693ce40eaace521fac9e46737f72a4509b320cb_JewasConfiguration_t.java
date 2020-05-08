 package jewas.configuration;
 
 /**
  * Created by IntelliJ IDEA.
  * User: driccio
  * Date: 19/07/11
  * Time: 11:10
  * To change this template use File | Settings | File Templates.
  */
 public abstract class JewasConfiguration {
     /**
      * The default application configuration file path to use.
      */
    private static final String APPLICATION_CONFIGURATION_FILE_PATH = "conf/jewas.conf";
 
     /**
      * The key to use in the application configuration file to define the template folder.
      */
     private static final String TEMPLATE_PATH_KEY = "templates.path";
 
     /**
      * The default value of the template folder.
      */
     private static final String TEMPLATE_PATH_DEFAULT_VALUE = "templates/";
 
     /**
      * The key to use in the application configuration file to define the static resources folder.
      */
     private static final String STATIC_RESOURCES_PATH_KEY = "static.resources.path";
 
     /**
      * The default value of the static resources folder.
      */
     private static final String STATIC_RESOURCES_DEFAULT_VALUE = "public/";
 
     /**
      * The delegate to use to get the properties.
      */
     protected static JewasConfigurationDelegate delegate =
             new DefaultJewasConfigurationDelegate(APPLICATION_CONFIGURATION_FILE_PATH);
 
     /**
      * Get the value of the given key if defined, else the default value.
      * @param key the key
      * @param defaultValue the default value
      * @return the value of the given key if defined, else the default value.
      */
     private static String getValueOfKeyOrDefaultValue(String key, String defaultValue) {
         String value = delegate.getProperties().getProperty(key);
 
         if (value == null) {
             value = defaultValue;
         }
 
         return value;
     }
 
     /**
      *
      * @return the path of the templates folder.
      */
     public static String getTemplatesPath() {
         return getValueOfKeyOrDefaultValue(TEMPLATE_PATH_KEY, TEMPLATE_PATH_DEFAULT_VALUE);
     }
 
     /**
      *
      * @return the path of the static resources folder.
      */
     public static String getStaticResourcesPath() {
         return getValueOfKeyOrDefaultValue(STATIC_RESOURCES_PATH_KEY, STATIC_RESOURCES_DEFAULT_VALUE);
     }
 }
