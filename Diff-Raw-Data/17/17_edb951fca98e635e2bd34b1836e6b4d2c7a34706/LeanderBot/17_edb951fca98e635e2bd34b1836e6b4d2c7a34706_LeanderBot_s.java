 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 
 import org.jibble.pircbot.IrcException;
 import org.jibble.pircbot.NickAlreadyInUseException;
 import org.jibble.pircbot.PircBot;
 
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.fetcher.FeedFetcher;
 import com.sun.syndication.fetcher.FetcherException;
 import com.sun.syndication.fetcher.impl.FeedFetcherCache;
 import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
 import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
 import com.sun.syndication.io.FeedException;
 
 public class LeanderBot extends PircBot {
 
     /**
      * Keeps track of the latest entries.
      */
     private List<String> queue;
     private URL feedURL;
     private String branch;
     private String channel;
     private FeedFetcherCache feedInfoCache;
     private FeedFetcher fetcher;
 
     public LeanderBot() throws NickAlreadyInUseException, IOException, IrcException {
         this.setName("rednael");
         this.setVerbose(true);
         this.connect("irc.freenode.net");
         channel = "#cdk";
         this.joinChannel(channel);
         queue = new ArrayList<String>();
         branch = "cdk-1.2.x";
         feedURL = new URL(
             "http://cdk.git.sourceforge.net/git/gitweb.cgi?p=cdk;a=rss;h=refs/heads/"
                 + branch
         );
         feedInfoCache = HashMapFeedInfoCache.getInstance();
         fetcher = new HttpURLFeedFetcher(feedInfoCache);
     }
    
     private void boot() throws IllegalArgumentException, IOException, FeedException, FetcherException {
         SyndFeed feed = null;
         feed = fetcher.retrieveFeed(feedURL);
         int itemCount = 0;
         List<SyndEntry> entries = feed.getEntries();
         for (SyndEntry entry : entries) {
             itemCount++;
             String link = entry.getLink();
             queue.add(link);
         }
         // feeds have the latest entry first, but we want them at the last
         // position
         Collections.reverse(queue);
     }
    
     private void update() {
         try {
             SyndFeed feed = null;
             feed = fetcher.retrieveFeed(feedURL);
             List<SyndEntry> entries = feed.getEntries();
             for (SyndEntry entry : entries) {
                 String title = entry.getTitle();
                 String link = entry.getLink();
                 if (!queue.contains(link)) {
                     queue.add(link);
                     StringBuffer message = new StringBuffer();
                     message.append('[').append(branch).append("] ");
                     message.append(title);
                     String author = entry.getAuthor();
                     if (author.indexOf('<') != -1) {
                         author = author.substring(0, author.indexOf('<'));
                     }
                     message.append("  ").append(link);
                     sendMessage(channel, message.toString());
                 }
             }
         } catch (Exception exception) {
             exception.printStackTrace();
         }
     }
    
     public void onMessage(String channel, String sender,
                        String login, String hostname, String message) {
     }
    
     public static void main(String[] args) throws Exception {
        LeanderBot bot = new LeanderBot();  
         bot.boot();
        
         Random random = new Random();
         while (bot.isConnected()) {
             bot.update();
             Thread.sleep(55000 + random.nextInt(10000));
         }
     }
 
 }
 
 
