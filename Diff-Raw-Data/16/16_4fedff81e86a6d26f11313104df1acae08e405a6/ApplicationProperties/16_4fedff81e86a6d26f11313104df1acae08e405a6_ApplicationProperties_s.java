 package pl.psnc.dl.wf4ever;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 
 import pl.psnc.dl.wf4ever.connection.DigitalLibraryFactory;
 
 /**
  * Application properties.
  * 
  * @author piotrekhol
  * 
  */
 public final class ApplicationProperties {
 
     /** logger. */
     private static final Logger LOGGER = Logger.getLogger(ApplicationProperties.class);
 
     /** application properties file name. */
     private static final String PROPERTIES_FILE = "application.properties";
 
     /** application name in Maven. */
     private static String name;
 
     /** application version in Maven. */
     private static String version;
 
 
     /**
      * Private constructor.
      */
     private ApplicationProperties() {
         //nope
     }
 
 
     /**
      * Read application properties.
      */
     public static void load() {
         InputStream inputStream = DigitalLibraryFactory.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
         if (inputStream == null) {
             LOGGER.error("Application properties file not found! ");
             throw new RuntimeException("Application properties file not found! ");
         }
         Properties properties = new Properties();
         try {
             properties.load(inputStream);
         } catch (IOException e) {
             LOGGER.error("Unable to read application properties", e);
             throw new RuntimeException("Unable to read application properties", e);
         } finally {
             try {
                 inputStream.close();
             } catch (IOException e) {
                 LOGGER.warn("Exception when closing the properties input stream", e);
             }
         }
        name = properties.getProperty("name");
        version = properties.getProperty("version");
     }
 
 
     public static String getName() {
         return name;
     }
 
 
     public static String getVersion() {
         return version;
     }
 
 }
