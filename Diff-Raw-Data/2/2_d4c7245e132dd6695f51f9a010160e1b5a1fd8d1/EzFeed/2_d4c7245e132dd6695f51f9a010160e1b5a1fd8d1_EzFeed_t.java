 package rss;
 
 import java.io.IOException;
 import java.text.*;
 import java.util.*;
 import utils.WebPage;
 import utils.XMLParser;
 
 import static utils.Constants.*;
 
 
 public class EzFeed extends Feed
 {
     private ArrayList<String> releases;
     private ArrayList<String> series;
     private boolean checked;
 
     public EzFeed() {
         super("EZTV", "http://www.ezrss.it/feed/");
 
         releases = new ArrayList<String>();
         series = new ArrayList<String>();
         checked = false;
 
         // move to config?
         series.add("Castle");
         series.add("Breaking Bad");
         series.add("Futurama");
         series.add("Simpsons");
         series.add("Game of Thrones");
         series.add("The Big Bang Theory"); 
         series.add("How I Met Your Mother");
         series.add("House");
     }
 
     @Override
     public List<String> check(byte maxMsgsCount) throws IOException {
         WebPage entry = WebPage.loadWebPage(address, "UTF-8");
         String content = entry.getContent();
 
         String message;
         String link;
         List<String> newEntries = new ArrayList<String>();
 
         titleItr = 0;
 
         String feedName = findNextTitle(content);
         if (feedName == null)
             return newEntries;
         
 
         while(true) {
             message = findNextTag(content, "<title><![CDATA[", "]]></title>", true);
 
             if(message == null || isIn(message))
                 break;
 
             if(!isShowTracked(message))
                 continue;
             
             link = findNextTag(content, "<link>", "</link>", false);
             
             if(link == null)
                 continue;
 
             if(checked)
                 newEntries.add(message + " | " + link);
             releases.add(message + " | " + link);
         }
         checked = true;
 
         return newEntries;
     }
 
     @Override
     public ArrayList<String> getLastMessages(int count) throws IOException {
         if(!checked)
             check((byte)0);
 
         ArrayList<String> list = new ArrayList<String>();
         int size = releases.size();
         if(size == 0)
             return list;
         
         if(count > size)
             count = size;
         
         for(byte i = 0; i < count; ++i)
             list.add(releases.get(i));
         for(byte i = (byte)count; i < size; ++i)
             releases.remove(i);
         
         return list;
     }
 
     private String findNextTag(String content, String tag, String endTag, boolean changeItr) {
         try {
             String str = XMLParser.getSnippet(content, titleItr, tag, endTag);
             if(changeItr)
                 titleItr = XMLParser.getNextOccurrenceIndex();
             return str;
         } catch (ParseException e) {
             return null;
         }
     }
 
     private boolean isShowTracked(String title) {
         for(String s : series)
             if(title.indexOf(s) != NOT_FOUND)
                 return true;
         return false;
     }
 
     private boolean isIn(String title) {
         for(String s : releases)
            if(s.indexOf(title) != NOT_FOUND)
                 return true;
         return false;
     }
 };
