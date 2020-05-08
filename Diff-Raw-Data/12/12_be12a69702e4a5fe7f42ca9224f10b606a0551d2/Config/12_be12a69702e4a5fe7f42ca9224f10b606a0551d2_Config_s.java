 package config;
 
 import io.CSVEngine;
 import java.io.File;
 import java.io.IOException;
import java.net.URL;
 import java.util.HashMap;
 
 
 /**
  *
  * @author jonathanrainer
  */
 public class Config {
     /*
      * A HashMap to assist to store configuration details such that it's easy
      * to get them back
      */
     private HashMap<String, String> configs;
     
     /**
      * Method to load in the configuration files through loading in each tuple
      * into the HashMap
      * @param fileName  The location of the configuration file.
      * @throws IOException If the configuration cannot be found or read from
      */
     
     public Config(File file) throws IOException
     {
         // Load in the CSV file from the same directory as the .jar files
         CSVEngine csvEngine = new CSVEngine();
         configs = csvEngine.importCSVFile(file);
                 
         // In order to guarantee the Operating System used we query the JVM
         // itself and then from there put the final tuple into the HashMaps to
         // identify the Operating System
         String osName = System.getProperty("os.name");
         osName = osName.toLowerCase();
         if(osName.startsWith("win"))
         {
             configs.put("os","Windows");
         }
         else if(osName.startsWith("mac"))
         {
             configs.put("os", "Mac");
         }
         else if(osName.startsWith("linux"))
         {
             configs.put("os", "Unix");
         }
         else
         {
             configs.put("os", "Unsupported");
         }
          
     }
     
     /**
      * Return the HashMap that stores the configuration
      * @return The HashMap which stores the Configuration details
      */
     public HashMap<String, String> getConfigs() {
         return configs;
     }
     
     /**
      * Allows a new configuration to be loaded 
      * @param configs The new HashMap containing the configuration
      */
     
     public void setConfiguration(HashMap<String,String> configs)
     {
         this.configs = configs;
     }
 }
