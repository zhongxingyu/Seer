 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.parts.redcastlemedia.multitallented.models;
 
 import java.io.File;
 import java.io.IOException;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 /**
  *
  * @author Multitallented
  */
 public abstract class YMLProxy extends YamlConfiguration {
     
     public boolean createIfNotExists(File file) {
         if (file.exists()) {
            try {
                this.load(file);
            } catch (Exception e) {
              
            }
             return true;
         }
         try {
             file.createNewFile();
         } catch (IOException ioe) {
             System.out.println("could not create new " + file.getName());
             return false;
         }
         createFile(file);
         return true;
     }
     
     public abstract void createFile(File file);
 }
