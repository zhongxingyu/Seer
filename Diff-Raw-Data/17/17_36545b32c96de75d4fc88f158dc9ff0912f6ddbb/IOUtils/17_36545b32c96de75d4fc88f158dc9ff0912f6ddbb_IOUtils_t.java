 package me.galaran.bukkitutils.pvprealm;
 
 import org.bukkit.configuration.file.FileConfiguration;
 
 import java.io.File;
 import java.io.IOException;
 
 public class IOUtils {
 
     public static void saveYml(FileConfiguration config, File file) {
         try {
             config.save(file);
         } catch (IOException ex) {
             ex.printStackTrace();
         }
     }
 
     public static void createFileIfNotExists(File file) {
         if (!file.exists()) {
             try {
                 file.createNewFile();
             } catch (IOException ex) {
                 ex.printStackTrace();
             }
         }
     }
 
     public static void createDirectoryPathIfNotExists(File dir) {
         if (!dir.exists()) {
             dir.mkdirs();
         }
     }
 }
