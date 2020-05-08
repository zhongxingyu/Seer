 /*
  * Settings.java
  * 
  * Copyright (c) 2012 Lolmewn <info@lolmewn.nl>. 
  * 
  * Sortal is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Sortal is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Sortal.  If not, see <http ://www.gnu.org/licenses/>.
  */
 
 package nl.lolmewn.sortal;
 
 import java.io.File;
import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 /**
  *
  * @author Lolmewn <info@lolmewn.nl>
  */
 
 public class Settings {
 
     private Main plugin;
     private boolean useMySQL;
     private boolean update;
     private double version;
     private boolean debug;
     private int warpCreatePrice;
     private int warpUsePrice;
     private String signContains;
     private String dbUser, dbPass, dbPrefix, dbHost, dbDatabase;
     private int dbPort;
     
     private boolean perWarpPerm;
     private boolean signCreatorIsPrivateUser;
     
     private String createWarp, delWarp, list, unregister, directWarp, setUses, placeSign, warp;
     
     protected File settingsFile;
     protected YamlConfiguration c;
     
     private Localisation localisation;
 
     public boolean isSignCreatorIsPrivateUser() {
         return signCreatorIsPrivateUser;
     }
     
     public Localisation getLocalisation(){
         return this.localisation;
     }
 
     protected String getDbDatabase() {
         return dbDatabase;
     }
 
     protected String getDbHost() {
         return dbHost;
     }
 
     protected String getDbPass() {
         return dbPass;
     }
 
     protected int getDbPort() {
         return dbPort;
     }
 
     protected String getDbPrefix() {
         return dbPrefix;
     }
 
     protected String getDbUser() {
         return dbUser;
     }
 
     public String getSignContains() {
         return signContains;
     }
 
     public boolean useMySQL() {
         return useMySQL;
     }
 
     public int getWarpCreatePrice() {
         return warpCreatePrice;
     }
 
     public int getWarpUsePrice() {
         return warpUsePrice;
     }
 
     public boolean isDebug() {
         return debug;
     }
 
     public boolean isUpdate() {
         return update;
     }
 
     public double getVersion() {
         return version;
     }
 
     public boolean isPerWarpPerm() {
         return perWarpPerm;
     }
     
     /*
      * Only used if there's an error when starting MySQL
      */
     protected void setUseMySQL(boolean use){
         this.useMySQL = use;
     }
 
     public void setWarpCreatePrice(int warpCreatePrice) {
         this.warpCreatePrice = warpCreatePrice;
     }
 
     public void setWarpUsePrice(int warpUsePrice) {
         this.warpUsePrice = warpUsePrice;
     }
 
     public String getDefaultCreateWarp() {
         return createWarp;
     }
 
     public String getDefaultDelWarp() {
         return delWarp;
     }
 
     public String getDefaultDirectWarp() {
         return directWarp;
     }
 
     public String getDefaultList() {
         return list;
     }
 
     public String getDefaultPlaceSign() {
         return placeSign;
     }
 
     public String getDefaultSetUses() {
         return setUses;
     }
 
     public String getDefaultUnregister() {
         return unregister;
     }
 
     public String getDefaultWarp() {
         return warp;
     }
     
     private Main getPlugin(){
         return this.plugin;
     }
     
     public Settings(Main main) {
         this.plugin = main;
         this.settingsFile = new File(main.getDataFolder(), "settings.yml");
         if(!this.settingsFile.exists()){
             this.extractSettings(YamlConfiguration.loadConfiguration(settingsFile));
         }
         this.c = YamlConfiguration.loadConfiguration(settingsFile);
         this.localisation = new Localisation();
         
         this.loadSettings();
     }
 
     private void loadSettings() {
         if(c.contains("showWhenWarpGetsLoaded")){
             //Old version of config file
             this.convert(c);
         }
         addNewDefauls(c);
        try {
            c.load(settingsFile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidConfigurationException ex) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
        }
         
         this.useMySQL = c.getBoolean("useMySQL");
         this.dbUser = c.getString("MySQL-User");
         this.dbPass = c.getString("MySQL-Pass");
         this.dbHost = c.getString("MySQL-Host");
         this.dbPort = c.getInt("MySQL-Port");
         this.dbDatabase = c.getString("MySQL-Database");
         this.dbPrefix = c.getString("MySQL-Prefix");
         this.warpCreatePrice = c.getInt("warpCreatePrice");
         this.warpUsePrice = c.getInt("warpUsePrice");
         this.signContains = c.getString("signContains", "[Sortal]");
         this.update = c.getBoolean("update", true);
         this.version = c.getDouble("version");
         this.debug = c.getBoolean("debug", false);
         this.perWarpPerm = c.getBoolean("perWarpPerm", false);
         
         this.createWarp = c.getString("permissions.createwarp", "op");
         this.delWarp = c.getString("permissions.delwarp", "op");
         this.list = c.getString("permissions.list", "op");
         this.unregister = c.getString("permissions.unregister", "op");
         this.directWarp = c.getString("permissions.directwarp", "op");
         this.setUses = c.getString("permissions.setuses", "op");
         this.placeSign = c.getString("permissions.placesign", "op");
         this.warp = c.getString("permissions.warp", "true");
         
         this.signCreatorIsPrivateUser = c.getBoolean("signCreatorIsPrivateUser", true);
         if(this.isDebug()){
             this.printSettings(YamlConfiguration.loadConfiguration(this.settingsFile)); //re-init file
         }
     }
 
     private void extractSettings(YamlConfiguration c) {
         try {
             this.settingsFile.createNewFile();
             this.addNewDefauls(c);
         } catch (IOException ex) {
             Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     private void printSettings(YamlConfiguration c) {
         for(String path : c.getConfigurationSection("").getKeys(true)){
             this.getPlugin().getLogger().info("[Debug] CONFIG: " + path + ":" + c.get(path, null));
         }
     }
 
     private void convert(YamlConfiguration c) {
         final HashMap<String, Object> values = new HashMap<String, Object>();
         boolean useVault = c.getBoolean("plugins.useVault", false);
         if(useVault){
             values.put("warpCreatePrice", c.getInt("warpCreatePrice"));
             values.put("warpUsePrice", c.getInt("warpUsePrice"));
         }
         values.put("signContains" , c.getString("signContains", "[Sortal]"));
         values.put("update", c.getBoolean("auto-update", true));
         values.put("debug", c.getBoolean("debug", false));
         values.put("useMySQL", c.getBoolean("useMySQL", false));
         if(c.getBoolean("useMySQL")){
             values.put("MySQL-User", c.getString("MySQL.username", "root"));
             values.put("MySQL-Pass", c.getString("MySQL.password", "p4ssw0rd"));
             values.put("MySQL-Database", c.getString("MySQL.database", "minecraft"));
             values.put("MySQL-Host", c.getString("MySQL.host", "localhost"));
         }
         
         HashMap<String, String> local = new HashMap<String, String>();
         local.put("noPermissions", c.getString("no-permissions"));
         local.put("commands.createNameForgotten", c.getString("warpCreateNameForgotten"));
         local.put("commands.deleteNameForgotten", c.getString("warpDeleteNameForgotten"));
         local.put("commands.nameInUse", c.getString("nameInUse"));
         local.put("paymentComplete", c.getString("moneyPayed").replace("MONEY", "$MONEY"));
         local.put("commands.warpCreated", c.getString("warpCreated").replace("WARPNAME", "$WARP"));
         local.put("noMoney", c.getString("notEnoughMoney"));
         local.put("commands.warpDeleted", c.getString("warpDeleted").replace("WARPNAME", "$WARP"));
         local.put("commands.warpNotFound", c.getString("warpDoesNotExist"));
         local.put("noPlayer", c.getString("notAplayer"));
         
         this.getLocalisation().addOld(local);
         
         if(this.settingsFile.delete()){
             this.getPlugin().getLogger().info("Old Config deleted, values stored..");
             this.extractSettings(c);
             this.addSettingsToConfig(settingsFile, values);
         }else{
             this.getPlugin().getLogger().warning("Couldn't delete old settings file! Using all defaults");
         }
     }
     
     private void addSettingsToConfig(File f, HashMap<String, Object> values){
         for(String path : values.keySet()){
             c.set(path, values.get(path));
         }
         try {
             c.save(f);
         } catch (IOException ex) {
             this.getPlugin().getLogger().log(Level.SEVERE, null, ex);
         }
         this.getPlugin().getLogger().info("Saved old settings in new settings file!");
     }
     
     protected void addSettingToConfig(File f, String path, Object value){
         c.set(path, value);
         try {
             c.save(f);
         } catch (IOException ex) {
             this.getPlugin().getLogger().log(Level.SEVERE, null, ex);
         }
     }
 
     private void addNewDefauls(YamlConfiguration c) {
         c.addDefault("useMySQL", false);
         c.addDefault("MySQL-User", "root");
         c.addDefault("MySQL-Pass", "herpDerpPassHere");
         c.addDefault("MySQL-Host", "localhost");
         c.addDefault("MySQL-Port", 3306);
         c.addDefault("MySQL-Database", "minecraft");
         c.addDefault("MySQL-Prefix", "Sortal_");
         c.addDefault("warpCreatePrice", 0);
         c.addDefault("warpUsePrice", 0);
         c.addDefault("perWarpPerm", false);
         c.addDefault("signContains", "[Sortal]");
         c.addDefault("update", true);
         
         c.addDefault("version", 5.2);
         c.addDefault("debug", false);
         c.addDefault("signCreatorIsPrivateUser", true);
         
         c.addDefault("permissions.warp", "true");
         c.addDefault("permissions.createwarp", "op");
         c.addDefault("permissions.delwarp", "op");
         c.addDefault("permissions.list", "op");
         c.addDefault("permissions.unregister", "op");
         c.addDefault("permissions.directwarp", "op");
         c.addDefault("permissions.setuses", "op");
         c.addDefault("permissions.placesign", "op");
         
         c.options().header("Find out what these values mean: http://dev.bukkit.org/server-mods/sortal/pages/settings");
         c.options().copyDefaults(true);
         try {
             c.save(settingsFile);
         } catch (IOException ex) {
             Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 }
