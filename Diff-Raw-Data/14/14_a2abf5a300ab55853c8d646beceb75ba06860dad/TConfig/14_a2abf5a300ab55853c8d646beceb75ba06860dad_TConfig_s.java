 package com.lebelw.Tickets;
 
 import java.io.File;
 import java.util.List;
 
 import org.bukkit.util.config.Configuration;
 /**
  * @description Contains all the functions for configuration file handling
  * @author greatman
  *
  */
 public class TConfig {
     public static String hostname;
     public static String type;
     public static String username;
     public static String password;
     public static String database;
     public static double cost;
     public static int chance;
     public static boolean convert;
     public static int lotteryitem;
     public TConfig(Tickets instance) {
         
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
     public void resetconvert() {
     	write("Database.convert",false);
     }
     private void write(String root, Object x){
         Configuration config = load();
         config.setProperty(root, x);
         config.save();
     }
     private Boolean readBoolean(String root){
         Configuration config = load();
         return config.getBoolean(root, false);
     }
 
     private Double readDouble(String root){
         Configuration config = load();
         return config.getDouble(root, 0);
     }
     private List<String> readStringList(String root){
         Configuration config = load();
         return config.getKeys(root);
     }
     private String readString(String root){
         Configuration config = load();
         return config.getString(root);
     }
     private int readInteger(String root,int def){
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
         TLogger.info("Generating Config File...");
         write("Database.type", "sqlite");
     	write("Database.hostname", "localhost");
     	write("Database.username", "root");
     	write("Database.password", "");
     	write("Database.database", "tickets");
     	write("Database.convert", false);
    	write("Ticket.cost","10");
     	write("Lottery.chance",1000);
     	write("Lottery.itemid",354);
      loadkeys();
     }
     private void loadkeys(){
         TLogger.info("Loading Config File...");
         type = readString("Database.type");
         hostname = readString("Database.hostname");
         username = readString("Database.username");
         password = readString("Database.password");
         database = readString("Database.database");
         convert = readBoolean("Database.convert");
         cost = readDouble("Ticket.cost");
         chance = readInteger("Lottery.chance", 1000);
         lotteryitem = readInteger("Lottery.itemid",354);
         }
 }
