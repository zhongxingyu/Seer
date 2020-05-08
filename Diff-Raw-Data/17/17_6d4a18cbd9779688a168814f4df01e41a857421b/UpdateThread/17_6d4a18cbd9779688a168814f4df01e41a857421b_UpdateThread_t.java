 package erki.xpeter.parsers.rss;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.MalformedURLException;
 import java.net.Socket;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.util.LinkedList;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.gnu.stealthp.rsslib.RSSChannel;
 import org.gnu.stealthp.rsslib.RSSException;
 import org.gnu.stealthp.rsslib.RSSHandler;
 import org.gnu.stealthp.rsslib.RSSItem;
 import org.gnu.stealthp.rsslib.RSSParser;
 
 import erki.api.storage.Storage;
 import erki.api.util.Log;
 import erki.xpeter.Bot;
 import erki.xpeter.msg.Message;
 import erki.xpeter.util.Keys;
 import erki.xpeter.util.StorageKey;
 
 /**
  * This thread periodically checks if there are any new items in some feed. If there are the new
  * items are broadcast through all connections the bot has.
  * 
  * @author Edgar Kalkowski
  */
 public class UpdateThread extends Thread {
     
     private TreeMap<String, FeedData> feeds;
     
     private boolean killed = false;
     
     private Bot bot;
     
     private StorageKey<TreeMap<String, FeedData>> key;
     
     private Storage<Keys> storage;
     
     /**
      * Create a new UpdateThread.
      * 
      * @param feeds
      *        The feeds that shall be checked.
      * @param bot
      *        The bot instance to whose connections new items shall be broadcast.
      * @param key
      *        The key under which the feeds shall be stored.
      * @param storage
      *        The persistant storage facility to store the feeds to if something changed.
      */
     public UpdateThread(TreeMap<String, FeedData> feeds, Bot bot,
             StorageKey<TreeMap<String, FeedData>> key, Storage<Keys> storage) {
         super("RssUpdateThread");
         this.feeds = feeds;
         this.bot = bot;
         this.key = key;
         this.storage = storage;
     }
     
     @SuppressWarnings("unchecked")
     @Override
     public void run() {
         super.run();
         
         while (!killed) {
             Set<String> keys = feeds.keySet();
             
             for (String url : keys) {
                 RSSHandler handler = new RSSHandler();
                 FeedData feed = feeds.get(url);
                 
                 try {
                     RSSParser.parseXmlFile(new URL(url), handler, false);
                     RSSChannel channel = handler.getRSSChannel();
                     
                     for (RSSItem item : (Iterable<RSSItem>) channel.getItems()) {
                         
                         if (!feed.isKnown(item.toString())) {
                             feed.add(item.toString());
                             storage.add(key, feeds);
                             
                             if (feed.isVerbose()) {
                                 bot.broadcast(new Message("[" + feed.getTitle() + "] "
                                         + item.getTitle() + "\n" + item.getDescription() + " ("
                                         + getTinyUrl(item.getLink()) + ")"));
                             } else {
                                bot.broadcast(new Message("[" + feed.getTitle() + "] "
                                        + item.getTitle() + " (" + getTinyUrl(item.getLink()) + ")"));
                             }
                         }
                     }
                     
                     // Clean up old items.
                     LinkedList<String> items = new LinkedList<String>();
                     
                     for (RSSItem item : (Iterable<RSSItem>) channel.getItems()) {
                         items.add(item.toString());
                     }
                     
                     feed.cleanup(items);
                 } catch (RSSException e) {
                     Log.error(e);
                 } catch (MalformedURLException e) {
                     Log.error(e);
                 }
             }
             
             try {
                 Thread.sleep(200000);
             } catch (InterruptedException e) {
             }
         }
     }
     
     /**
      * Tries to make urls tiny using tinyurl.com.
      * 
      * @param url
      *        The long ugly url.
      * @return A tiny url or the long url if something about tinyurl.com failed.
      */
     private static String getTinyUrl(String url) {
         
         try {
             Socket socket = new Socket("tinyurl.com", 80);
             socket.setSoTimeout(5000);
             OutputStreamWriter socketOut = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
            BufferedReader socketIn = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), "UTF-8"));
             socketOut.write("GET api-create.php?url=" + url + " HTTP/1.0\r\n");
             socketOut.write("Host: tinyurl.com\r\n\r\n");
             socketOut.flush();
             String line;
             boolean header = true;
             
             while ((line = socketIn.readLine()) != null) {
                 Log.finest("Received line from server: " + line);
                 
                 if (header) {
                     
                     if (line.equals("")) {
                         header = false;
                     }
                     
                     continue;
                 } else {
                     return line;
                 }
             }
             
             return url;
         } catch (UnknownHostException e) {
             Log.error(e);
             return url;
         } catch (IOException e) {
             Log.error(e);
             return url;
         }
     }
     
     /** Immediately stop this thread. */
     public void kill() {
         killed = true;
         interrupt();
     }
 }
