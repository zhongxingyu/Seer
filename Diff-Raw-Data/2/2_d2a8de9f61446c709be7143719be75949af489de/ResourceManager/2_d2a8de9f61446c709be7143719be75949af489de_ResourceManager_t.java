 package com.core.util;
 
 import java.util.*;
 import java.io.*;
 
 public class ResourceManager {
 
     public static Object getTestResource(String key) {
         try {
 
             PropertyResourceBundle resources = (PropertyResourceBundle)
                 ResourceBundle.getBundle("com.core.test.test");
 
             return (String)(resources.getObject(key));
         } catch (MissingResourceException mre) {
             System.out.println(mre);
             System.exit(1);
         }
 
         return null;
     }
 
     public static String getResourcePath() {
         return "resource";
     }
 
     public static String getUserDataPath() {
        File file = new File("user_data");
         if (!file.exists()) {
             file.mkdir();
         }
         return "user_data";
     }
 
     public static void mkUserPath(String userID) {
         File file = new File(getUserPath(userID));
         if (!file.exists()) {
             file.mkdir();
         }
     }
 
     public static void mvTempToUserPath(String userID) {
         File file = new File(getTempPath());
         if (file.exists()) {
             file.renameTo(new File(getUserPath(userID)));
         }
     }
 
     public static String getUserPath(String userID) {
         return getUserDataPath()+"/"+userID;
     }
 
     public static String getAudioResourcePath() {
         return getResourcePath() + "/mp3/";
     }
 
     public static String getPagesPath() {
         return getResourcePath() + "/pages/";
     }
 
     public static String getTempPath() {
         File tmp = new File("tmp/");
         if (!tmp.exists()) {
             tmp.mkdir();
         }
         return "tmp/";
     }
 
     public static String getPageText(String name) {
         return getContents(getPagesPath() + "/" + name + ".html");
     }
 
     static public String getContents(String aFileName) {
         //...checks on aFile are elided
         File aFile = new File(aFileName);
         StringBuilder contents = new StringBuilder();
         try {
           //use buffering, reading one line at a time
           //FileReader always assumes default encoding is OK!
           BufferedReader input =  new BufferedReader(new FileReader(aFile));
           try {
             String line = null; //not declared within while loop
             /*
             * readLine is a bit quirky :
             * it returns the content of a line MINUS the newline.
             * it returns null only for the END of the stream.
             * it returns an empty String if two newlines appear in a row.
             */
             while (( line = input.readLine()) != null){
               contents.append(line);
               contents.append(System.getProperty("line.separator"));
             }
           }
           finally {
             input.close();
           }
         }
         catch (IOException ex){
           ex.printStackTrace();
         }
         return contents.toString();
     }
 
 
 }
 
