 package org.makumba.parade.init;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 import org.apache.log4j.Logger;
 
 public class ParadeProperties {
 
     
     static String DEFAULT_PROPERTYFILE = "/parade.properties";
 
     private static Properties config;
 
     static public String paradeBaseRelativeToTomcatWebapps = ".." + File.separator + ".." + File.separator;
 
     static Logger logger = Logger.getLogger(ParadeProperties.class.getName());
 
     static {
 
         try {
             config = new Properties();
             config.load(ParadeProperties.class.getResourceAsStream(DEFAULT_PROPERTYFILE));
         } catch (Throwable t) {
            logger.error("Error while loading parade.properties. Make sure you have configured a parade.properties in webapp/WEB-INF/classes (you can copy the example file)", t);
         }
     }
 
     public static String getProperty(String configProperty) {
         return config.getProperty(configProperty);
     }
 
     public static List getElements(String configProperty) {
         List l = new LinkedList();
 
         String s = getProperty(configProperty);
         if (s == null)
             return null;
         StringTokenizer st = new StringTokenizer(s, ",");
         while (st.hasMoreElements()) {
             l.add(((String) st.nextToken()).trim());
         }
         return l;
     }
     
     public static String getParadeBase() {
         String paradeBase=".\\";
         try {
             paradeBase = new java.io.File("." + java.io.File.separator).getCanonicalPath();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         return paradeBase;
     }
     
     public static String getClassesPath() {
        return new java.io.File(RowProperties.class.getResource(DEFAULT_PROPERTYFILE).getPath()).getParent();
     }
 
 }
