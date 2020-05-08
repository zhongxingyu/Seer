 package net.kuehldesign.backuptube.app.common;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class BackupTubeCommon {
     // locations of various files and directories; directories should NOT have a trailing slash
     public static final String LOCATION_VIDEOS = "Videos";
     public static final String LOCATION_VIDEOS_DELETED = "Deleted Videos";
     public static final String LOCATION_VIDEO = ""; // video file, relative to video's folder, if using another dir REQUIRES a trailing slash
     public static final String LOCATION_VIDEO_DATAFILE = "video.html"; // data for each video, relative to the video's folder
     public static final String LOCATION_DATAFILE = "config.json"; // data in the root directory with info on all videos
 
     public static final String TEMPLATE_TITLE = "<!TITLE>";
     public static final String TEMPLATE_VIDEO_TITLE = "<!VIDEO_TITLE>";
     public static final String TEMPLATE_VIDEO_FILE = "<!VIDEO_FILE>";
     public static final String TEMPLATE_CLIENT_TYPE = "<!CLIENT_TYPE>";
     public static final String TEMPLATE_CLIENT_VERSION = "<!CLIENT_VERSION>";
     public static final String TEMPLATE_GEN_DATE = "<!GEN_DATE>";
     public static final String TEMPLATE_INFO = "<!INFO>";
 
     public static Gson getPrettyGson() {
         return new GsonBuilder().setPrettyPrinting().create();
     }
 
     public static String escapeFileName(String fileName) {
         String newFileName = "";
        String allowedCharacters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0234567890 ";
 
         // make it alphanumeric for max compatibility
         for (int i = 0; i < fileName.length(); i ++) {
             String c = fileName.substring(i, i + 1);
 
             if (allowedCharacters.indexOf(c) > (- 1)) {
                 newFileName += c;
             }
         }
 
         return newFileName;
     }
 
     public static boolean isGoodSaveDir(String saveDir) {
         if (! fileExists(saveDir)) {
             boolean wasCreated = new File(saveDir).mkdirs();
 
             if (! wasCreated) {
                 return false; // permission error etc
             } else {
                 return true;
             }
         } else {
             return true;
         }
     }
 
     public static boolean fileExists(String saveDir) {
         return new File(saveDir).exists();
     }
 
     public static String fixDir(String dir) {
         if (! dir.endsWith("/")) {
             dir += "/";
         }
 
         return dir;
     }
 
     public static long getCurrentTime() {
         return new Date().getTime();
     }
 
     public static String getTimeString(long timeToFormat) {
         SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
         return sdf.format(new Date(timeToFormat));
     }
 
     public static String getHTMLTemplate() {
         InputStream file = BackupTubeCommon.class.getResourceAsStream("stored/resource/video.html");
 
         if (file == null) {
             return null;
         }
 
         BufferedInputStream buffer = new BufferedInputStream(file);
         BufferedReader data = new BufferedReader(new InputStreamReader(buffer));
 
         String html = "";
         String line;
 
         try {
             while ((line = data.readLine()) != null) {
                 html += line + "\n";
             }
         } catch (IOException ex) {
             
         }
 
         return html;
     }
 }
