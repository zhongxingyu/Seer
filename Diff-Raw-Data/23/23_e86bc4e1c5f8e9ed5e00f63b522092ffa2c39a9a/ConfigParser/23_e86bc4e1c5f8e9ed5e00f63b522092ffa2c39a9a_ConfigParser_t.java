 package stockwatch;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 public class ConfigParser {
    private final static String fileName = System.getProperty("user.home") + "/.dotConfig";
 
     private Properties appProperties;
     private InputStream propertiesFile;
     
     private String directoryPath;
     
     private boolean dumpToFile;
     private boolean dumpToDatabase;
 
     public ConfigParser() {
         try {
             appProperties = new Properties();
             propertiesFile = new FileInputStream(fileName);
             
             appProperties.load(propertiesFile);
             
             dumpToFile = Boolean.parseBoolean(appProperties.getProperty("app.dumpToFile"));
             dumpToDatabase = Boolean.parseBoolean(appProperties.getProperty("app.dumpToDatabase"));
             
             directoryPath = appProperties.getProperty("app.directoryPath");
         } catch (FileNotFoundException e) {
             e.printStackTrace();
             System.exit(-1);
         } catch (IOException e) {
             e.printStackTrace();
             System.exit(-1);
         }
     }
 
     public String getDirectoryPath() {
         return directoryPath;
     }
     
     public boolean dumpToFile() {
         return dumpToFile;
     }
     
     public boolean dumpToDatabase() {
         return dumpToDatabase;
     }
 }
