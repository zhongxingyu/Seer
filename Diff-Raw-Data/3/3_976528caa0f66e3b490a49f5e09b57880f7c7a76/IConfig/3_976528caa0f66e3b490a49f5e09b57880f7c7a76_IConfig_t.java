 package me.greatman.plugins.inn;
 
 import java.io.File;
 import java.util.List;
 
 import org.bukkit.util.config.Configuration;
 /**
  * @description Contains all the functions for configuration file handling
  * @author greatman
  *
  */
 public class IConfig {
     public static double cost;
     public IConfig(Inn instance) {
        configCheck();
        load();
     }
     public String directory = "plugins" + File.separator + "Tickets";
     File file = new File(directory + File.separator + "config.yml");
     public void configCheck(){
         
         new File(directory).mkdir();
         if(!file.exists()){
             try {
                 file.createNewFile();
                 addDefaults();
 
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
         } else {
 
             loadkeys();
         }
     }
     public void write(String root, Object x){
         Configuration config = load();
         config.setProperty(root, x);
         config.save();
     }
     public Boolean readBoolean(String root){
         Configuration config = load();
         return config.getBoolean(root, false);
     }
 
     public Double readDouble(String root){
         Configuration config = load();
         return config.getDouble(root, 0);
     }
     public List<String> readStringList(String root){
         Configuration config = load();
         return config.getKeys(root);
     }
     public String readString(String root){
         Configuration config = load();
         return config.getString(root);
     }
     public int readInteger(String root,int def){
     	Configuration config = load();
     	return config.getInt(root, def);
     }
     private Configuration load(){
 
         try {
             Configuration config = new Configuration(file);
             config.load();
             return config;
 
         } catch (Exception e) {
             e.printStackTrace();
         }
         return null;
     }
     private void addDefaults(){
         ILogger.info("Generating Config File...");
         write("Inn.cost", 100);
      loadkeys();
     }
     private void loadkeys(){
         ILogger.info("Loading Config File...");
         cost = readDouble("Ticket.cost");
         }
 }
