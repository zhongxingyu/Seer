 package save;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import java.util.Properties;
 
 /**
  * A class for storing Akrasia settings. Settings stored here should be considered temporary
  * as server-side settings will be phased into .properties files and client-side settings
  * will be phased into the MySQL database.
  */
 public class Settings {
     private static boolean loaded = false;
     private static Properties props = null;
     
     // Database Settings ( //TODO: to be moved to a .properties file )
     public final static String DBTableNameDefault = "akrasia";
     public final static String DBUserDefault      = "akrasia";
     public final static String DBPasswordDefault  = "admin";
 
     // Network Settings
 
     // In-game Options
 
     // Key Bindings
     
     public static String getProperty(String str){
         if(!loaded){
             props = new Properties();
             try{
                 props.load(new BufferedReader(new FileReader("Akrasia.properties")));
             }
             catch(FileNotFoundException e){
                 // Create the new file
                 props.setProperty("DBTableName", Settings.DBTableNameDefault);
                 props.setProperty("DBUser", Settings.DBUserDefault);
                 props.setProperty("DBPassword", Settings.DBPasswordDefault);
                
                 try{
                     props.store(new BufferedWriter(new FileWriter("Akrasia.properties")), "Akrasia Server Side Properties \n Generated at:");
                } catch (IOException e2) { }
             }
             catch(IOException e){
                 e.printStackTrace();
             }
         }
         
         return props.getProperty(str);
     }
}
