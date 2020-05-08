 package link;
 
 import java.util.ArrayList;
 import utils.Formatter;
 import utils.WebPage;
 
 
 public class Youtube {
 
    private static ArrayList<LinkInfo> cache = new ArrayList<>();
     private static final int VIDEO_ID_LENGTH = 11;
     private static final int NOT_FOUND = -1;
 
     private static void downloadParseSave(String videoID) {
         try {
             WebPage entry = loadVideoEntry(videoID);
             String title = getVideoTitleFromRawXML(entry);
             cache.add( new LinkInfo(videoID, title) );
         } catch (Exception e) {
             System.err.println( e.getMessage() );
         }
     }
 
     private static WebPage loadVideoEntry(String videoID) throws Exception {
         return WebPage.loadWebPage("http://gdata.youtube.com/feeds/api/videos/" + videoID, "UTF-8");
     }
 
     private static String getVideoTitleFromRawXML(WebPage entry) throws Exception {
         String content = entry.getContent();
         String toSearch = "<title type='text'>";
         int pos = content.indexOf(toSearch);
         if (pos == NOT_FOUND)
             throw new Exception("Cannot find video title in the XML source.");
 
         int begin = pos + toSearch.length();
         int end = content.indexOf("</title>", begin);
         return Formatter.removeHTML( content.substring(begin, end) );
     }
 
     private static void downloadIfNeeded(String videoID) {
         if ( !cacheContains(videoID) )
             downloadParseSave(videoID);
     }
 
     private static String getVideoIDOrEmptyString(String message) {
         int beginPosition = getPositionWhereVideoIDStarts(message);
         if (beginPosition == NOT_FOUND)
             return "";
 
         return message.substring(beginPosition, beginPosition + VIDEO_ID_LENGTH);
     }
 
     private static int getPositionWhereVideoIDStarts(String message) {
         int pos = videoIDPosition(message, "youtube.com/watch?v=");
         return (pos == NOT_FOUND) ? videoIDPosition(message, "youtu.be/") : pos;
     }
 
     private static int videoIDPosition(String message, final String URL_PATTERN) {
         int position = message.indexOf(URL_PATTERN);
         return (position != NOT_FOUND) ? position + URL_PATTERN.length() : NOT_FOUND;
     }
 
     private static boolean cacheContains(String videoID) {
         return getCachedInfo(videoID) != null;
     }
 
     private static LinkInfo getCachedInfo(String videoID) {
         for (LinkInfo li : cache)
             if ( li.hasVideoID(videoID) )
                 return li;
 
         return null;
     }
 
     public static boolean isYoutubeMessage(String message) {
         return !getVideoIDOrEmptyString(message).equals("");
     }
 
     public static String getLinkInfo(String message) {
         try {
         String newVideoID = getVideoIDOrEmptyString(message);
         downloadIfNeeded(newVideoID);
         LinkInfo li = getCachedInfo(newVideoID);
         return "YouTube: " + li.title();
         } catch (Exception e) {
             System.out.println( e.getMessage() );
             return "";
         }
     }
 
 }
