 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package info.jeppes.ZoneCore;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.Plugin;
 
 /**
  *
  * @author Jeppe
  */
 public class ZoneConfig extends YamlConfiguration{
     private final Plugin plugin;
     private File file;
     private String fileName;
     public ZoneConfig(Plugin plugin, File file){
         this.plugin = plugin;
         this.file = file;
         if(!file.exists()){
             try {
                
//                new File(file.getPath()).mkdirs();
                 file.createNewFile();
             } catch (IOException ex) {
                 Logger.getLogger(ZoneConfig.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         fileName = file.getName();
         try {
             load(file);
         } catch (FileNotFoundException ex) {
             Logger.getLogger(ZoneConfig.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException | InvalidConfigurationException ex) {
             Logger.getLogger(ZoneConfig.class.getName()).log(Level.SEVERE, null, ex);
         }
         loadDefaults(plugin);
     }
     
     public final boolean loadDefaults(Plugin plugin){
         if(plugin == null){
             return false;
         }
         InputStream defConfigStream = plugin.getResource(file.getName());
         if (defConfigStream != null) {
             YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
             setDefaults(defConfig);
             options().copyDefaults(true);
             save();
             return true;
         }
         return false;
     }
     
     public String getFileName(){
         return fileName;
     }
     
     public void writeYML(String root, Object x){
         writeYML(this,file,root,x);
     }
     public static void writeYML(YamlConfiguration config, File configFile, String root, Object x){
         config.set(root, x);
         try {
             config.save(configFile);
         } catch (IOException ex) {
             Logger.getLogger(ZoneConfig.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     public void deleteYML(String root){
         deleteYML(this,file,root);
     }
     public static void deleteYML(YamlConfiguration config, File configFile, String root){
         config.set(root, null);
         try {
             config.save(configFile);
         } catch (IOException ex) {
             Logger.getLogger(ZoneConfig.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     public void writeYMLASync(String root, Object x){
         writeYMLASync(this,file,root,x);
     }
     public void writeYMLASync(final YamlConfiguration config, final File configFile, final String root, final Object x){
         Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable(){
             @Override
             public void run() {
                 try {
                     config.set(root, x);
                     config.save(configFile);
                 } catch (IOException ex) {
                     Logger.getLogger(ZoneConfig.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
 
         });
     }
     public void deleteYMLASync(String root){
         deleteYMLASync(this,file,root);
     }
     public void deleteYMLASync(final YamlConfiguration config, final File configFile, final String root){
         Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable(){
             @Override
             public void run() {
                 try {
                     config.set(root, null);
                     config.save(configFile);
                 } catch (IOException ex) {
                     Logger.getLogger(ZoneConfig.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
 
         });
     }
     public boolean save(){
         try {
             save(file);
         } catch (IOException ex) {
             Logger.getLogger(ZoneConfig.class.getName()).log(Level.SEVERE, null, ex);
             return false;
         }
         return true;
     }
 }
