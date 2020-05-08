 package link;
 
 import java.util.HashMap;
 
 
 public class YoutubeCache {
 
     private static HashMap<String, String> links;
     private static String lastUsedVideoID;
 
     static {
         links = new HashMap<String, String>();
         lastUsedVideoID = "";
     }
 
     public static void add(String videoID, String title) {
        lastUsedVideoID = videoID;
         if ( !contains(videoID) )
             links.put(videoID, title);
     }
 
     public static boolean contains(String videoID) {
         return links.containsKey(videoID);
     }
 
     public static String get(String videoID) {
         String title = links.get(videoID);
        return (title == null) ? "" : title;
     }
 
     public static String getLastTitle() {
         return get(lastUsedVideoID);
     }
 
 }
