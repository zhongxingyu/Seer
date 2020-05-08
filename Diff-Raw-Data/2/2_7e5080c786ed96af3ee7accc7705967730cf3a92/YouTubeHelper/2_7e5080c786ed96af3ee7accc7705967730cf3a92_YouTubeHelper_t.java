 package net.kuehldesign.backuptube.site.youtube;
 
 import com.google.gson.Gson;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.LinkedList;
 import net.kuehldesign.backuptube.exception.FatalBackupException;
 import net.kuehldesign.backuptube.exception.MalformedFeedURLException;
 import net.kuehldesign.backuptube.exception.UnableToOpenURLConnectionException;
 import net.kuehldesign.backuptube.site.youtube.video.YouTubeVideo;
 import net.kuehldesign.backuptube.site.youtube.video.YouTubeVideoGroup;
 import net.kuehldesign.backuptube.video.DownloadableVideo;
import net.kuehldesign.jnetutils.JNetUtils;
import net.kuehldesign.jnetutils.exception.UnableToGetSourceException;
 
 public class YouTubeHelper {
     public static final int FORMAT_3GP_LOW     = 1;
     public static final int FORMAT_3GP_MEDIUM  = 2;
     public static final int FORMAT_3GP_HIGH    = 3;
     public static final int FORMAT_FLV_LOW     = 4;
     public static final int FORMAT_FLV_MEDIUM  = 5;
     public static final int FORMAT_FLV_MEDIUM2 = 6;
     public static final int FORMAT_FLV_HIGH    = 7;
     public static final int FORMAT_MP4_HIGH    = 8;
     public static final int FORMAT_MP4_720P    = 9;
     public static final int FORMAT_MP4_1080P   = 10;
     public static final int FORMAT_MP4_4K      = 11;
     
     private static int maxResults = 50;
 
     public static int getMaxResults() {
         return maxResults;
     }
 
     public static URL getFeedURL(String user, int startIndex) throws FatalBackupException, MalformedFeedURLException {
         // max-results is 50
         // start-index should increase by 50
 
         try {
             return new URL("http://gdata.youtube.com/feeds/api/users/" + URLEncoder.encode(user, "UTF-8") + "/uploads?max-results=" + URLEncoder.encode(String.valueOf(getMaxResults()), "UTF-8") + "&alt=json&start-index=" + URLEncoder.encode(String.valueOf(startIndex), "UTF-8"));
         } catch (UnsupportedEncodingException ex) {
             throw new FatalBackupException("UTF-8 encoding not supported");
         } catch (MalformedURLException ex) {
             throw new MalformedFeedURLException("Malformed URL [101]");
         }
     }
 
     public static LinkedList<DownloadableVideo> getVideos(String user) throws FatalBackupException, UnableToOpenURLConnectionException {
         LinkedList<DownloadableVideo> videos = new LinkedList();
         int total = (- 1);
         int startIndex = 1; // YouTube is dumb and starts at 1 instead of 0...
         int i = 0;
 
         while (true) { // this is broken out of when the videos are all added
             URL url;
 
             try {
                 url = getFeedURL(user, startIndex);
             } catch (MalformedFeedURLException ex) {
                 throw new FatalBackupException("Unable to get feed URL");
             }
 
             URLConnection connection;
 
             YouTubeVideoGroup videoGroup;
 
             try {
                 connection = url.openConnection();
                 videoGroup = new Gson().fromJson(new InputStreamReader(connection.getInputStream()), YouTubeVideoGroup.class);
             } catch (IOException ex) {
                 throw new UnableToOpenURLConnectionException("Unable to open URL connection; does YouTube video exist?");
             }
 
             LinkedList<YouTubeVideo> feedVideos = videoGroup.getVideos();
 
             if (feedVideos != null) {
                 total = feedVideos.size();
 
                 for (YouTubeVideo video : feedVideos) {
                     videos.add(video);
                 }
 
                 startIndex += total;
             } else {
                 break;
             }
         }
 
         return videos;
     }
 }
