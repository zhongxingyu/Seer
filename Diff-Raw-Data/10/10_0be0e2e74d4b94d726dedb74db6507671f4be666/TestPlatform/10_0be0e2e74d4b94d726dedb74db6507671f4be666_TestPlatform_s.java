 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.tehbeard.beardstat.dataproviders;
 
 import com.tehbeard.beardstat.DbPlatform;
 import com.tehbeard.beardstat.containers.EntityStatBlob;
 import java.io.File;
 import java.io.InputStream;
 import java.sql.SQLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author James
  */
 class TestPlatform implements DbPlatform {
 
     public TestPlatform() {
     }
 
     public Logger getLogger() {
         return Logger.getLogger("stat");
     }
 
     public File getDataFolder() {
         return new File(".");
     }
 
     public void mysqlError(SQLException sqlException, String file) {
         sqlException.printStackTrace();
         getLogger().log(Level.SEVERE, "file: {0}", file);
     }
 
     public InputStream getResource(String file) {
         return getClass().getClassLoader().getResourceAsStream(file);
     }
 
     public void saveConfig() {
     }
 
     public boolean configValueIsSet(String key) {
         return true;
     }
 
     public void configValueSet(String key, Object val) {
     }
 
     public void loadEvent(EntityStatBlob esb) {
         
     }
     
 }
