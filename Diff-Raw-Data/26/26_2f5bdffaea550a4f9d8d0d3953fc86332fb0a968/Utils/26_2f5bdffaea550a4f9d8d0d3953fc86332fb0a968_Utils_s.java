 package models.utils;
 
 import com.google.common.base.Preconditions;
 import models.ActivityWrapper;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.select.Elements;
 import play.Play;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.Properties;
 
 /**
  * .
  *
  * @author fsznajderman
  *         date :  07/03/12
  */
 public class Utils {
 
 
     public static final String PATH_GOOGLE_KEY_PROPERTIES = "google.info.path";
 
     public static String getActivityTitle(ActivityWrapper activityWrapper) {
         return extractContent("b", activityWrapper);
 
     }
 
     public static String getActivityAuthor(ActivityWrapper activityWrapper) {
         return extractContent("a", activityWrapper);
     }
 
     private static String extractContent(String baliseName, ActivityWrapper activityWrapper) {
         Document doc = Jsoup.parse(activityWrapper.getContent());
 
         Elements elem = doc.getElementsByTag(baliseName);
        return elem.get(0).html();
 
     }
 
 
     public static String getValueFromGplusConf(final String key) {
         String path = Play.configuration.getProperty(PATH_GOOGLE_KEY_PROPERTIES);
 
         Preconditions.checkNotNull(path);
         final Properties p = new Properties();
         try {
             p.load(new FileInputStream(new File(path)));
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         return p.getProperty(key);
 
     }
 }
 
 
