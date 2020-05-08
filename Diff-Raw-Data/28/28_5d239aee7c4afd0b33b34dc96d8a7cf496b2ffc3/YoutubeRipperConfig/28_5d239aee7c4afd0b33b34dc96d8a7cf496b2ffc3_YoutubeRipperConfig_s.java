 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.ytripper;
 
 import java.io.BufferedWriter;
import java.io.FileWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author mmain
  */
 public class YoutubeRipperConfig {
     protected static String downloadDirectory;
     
     static {
         load();
     }
     
     public static void load() {
         Properties configFile = new Properties();
         try {
             configFile.load(new FileInputStream(new File("ytripper.conf")));
         } catch (IOException ex) {
             Logger.getLogger(YoutubeRipperConfig.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         downloadDirectory = configFile.getProperty("downloadDirectory");
     }
     
     public static void save() {
         try {
             FileWriter fstream = null;
             fstream = new FileWriter("ytripper.conf");
 
             BufferedWriter out = new BufferedWriter(fstream);
             out.write("#Download Directory\n");
             out.write("downloadDirectory=" + downloadDirectory);
             out.close();
         } catch (IOException ex) {
             Logger.getLogger(YoutubeRipperConfig.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     public static String getDownloadDirectory() {
         return downloadDirectory;
     }
     
     public static void setDownloadDirectory(String downloadDirectory) {
         YoutubeRipperConfig.downloadDirectory = downloadDirectory;
     }
 }
