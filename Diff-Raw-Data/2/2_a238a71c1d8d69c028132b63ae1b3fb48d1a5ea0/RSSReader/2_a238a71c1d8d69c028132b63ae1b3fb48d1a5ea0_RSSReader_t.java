 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 
 import be.ugent.twijug.jclops.CLManager;
 
 import com.sun.syndication.feed.synd.SyndEntryImpl;
 import com.sun.syndication.feed.synd.SyndFeedImpl;
 import com.sun.tools.javac.util.List;
 
 
 /**
  * A class for parsing and outputting RSS subscriptions
  * @author Andrew Bacon, Holly French, Veronica Lynn
  * CS 204, Spring 2013
  * date: 16 April 2013
  */
 
 public class RSSReader {
 
     private ArrayList<SyndFeedImpl> feeds;
     private ArgParser argParser;
 
    public RSSReader(ArrayList<SyndFeedImpl> allFeeds) {
         feeds = allFeeds;
     }
 
 
   /**
    * Gets all the posts, sorts them if necessary, and displays the result.
    */
   public void run() {
     // For every feed, fetch posts into some array (local or instance var?)
     // Then sort these posts
     // Then display them according to the flags we've parsed.
   }
 
     /**
     * does formatting and output and should respond to the following config options:
     * --number (number of posts)
     * --since (since a date in format yyyy-mm-dd, which is a date object)
     * --title
     * --description
     * --newest (optional)
     */
     public void display() {
         System.out.println();
     }
 
     /**
     * TODO: double check the object return type
     * gets all posts from a particular feed and will accept a synd feed impl as a parameter
     * @param SyndFeedImpl curFeed the current feed from which we want to get posts
     * @return an array list of SyndEntry objects, which are the posts
     */
     public List<SyndEntryImpl> getPostsFromFeed(SyndFeedImpl curFeed) {
         //allPosts = null;
         //return allPosts;
     	return (List<SyndEntryImpl>) curFeed.getEntries();
 
     }
 
     /**
     * gets all posts from a particular feed and will accept a synd feed impl as a parameter
     */
     public ArrayList<SyndEntryImpl> getAllPosts() {
         // worst case we will call this in the constructor to populate in inst var if needed.
     	ArrayList<SyndEntryImpl> allPosts = new ArrayList<SyndEntryImpl>();
     	for (SyndFeedImpl feed : feeds) {
     		List<SyndEntryImpl> curPosts = getPostsFromFeed(feed);
     		for (SyndEntryImpl post : curPosts) 
     			allPosts.add(post);  		
     	}
     	return allPosts;
     }
 
     /**
     * sort posts
     */
     public ArrayList<SyndEntryImpl> sortPosts(String sortMode) {
         // TODO: do we want the functionality of sorting a subset of posts?
         ArrayList<SyndEntryImpl> posts;
         if (sortMode.equals("alpha"))
             posts = sortPostsByAlpha();
         else if (sortMode.equals("date"))
             posts = sortPostsByDate();
         else
             posts = null;
         return posts;
     }
 
     /**
     * don't need a param because it's sorting alphabetically
     */
     public ArrayList<SyndEntryImpl> sortPostsByAlpha() {
         ArrayList<SyndEntryImpl> posts = getAllPosts();
         Collections.sort(posts, new Comparator<SyndEntryImpl>() {
             public int compare(SyndEntryImpl o1, SyndEntryImpl o2) {
                 String a = o1.getTitle();
                 String b = o2.getTitle();
                 return a.compareTo(b);
             }
         });
         return posts;
     }
 
     /**
     * sort chronologically
     */
     public ArrayList<SyndEntryImpl> sortPostsByDate() {
         ArrayList<SyndEntryImpl> posts = getAllPosts();
         Collections.sort(posts, new Comparator<SyndEntryImpl>() {
             public int compare(SyndEntryImpl o1, SyndEntryImpl o2) {
                 Date a = o1.getPublishedDate();
                 Date b = o2.getPublishedDate();
                 return a.compareTo(b);
             }
         });
         return posts;
     }
 
     /**
     * parse arguments n stuff
     */
     public void parseArguments(String[] args) {
         // This is the object we'll be using
         argParser = new ArgParser();
 
         // Retrieve and process the command line arguments, setting the appropriate instance variables in test
         CLManager options = new CLManager(argParser);
         options.parse(args);
 
         // Collect any remaining command line arguments that were not parsed.
         String[] remaining = options.getRemainingArguments();
 
         // Get the filename out of the remaining options
         if (remaining.length > 0) {
             // Note: we make an assumption here that the first "extraneous" argument is the feed file.
             argParser.setFilename(remaining[0]);
         } else {
             // the program should exit if the feed file is not specified on the command line.
             System.err.println("Error: no input filename specified.");
             System.exit(-1);
         }
 
         // DEBUG: were the instance variables set correctly?
         System.out.println("Instance variables:");
         System.out.println("Sort by alpha: " + argParser.isByAlpha());
         System.out.println("Date: " + argParser.getSince());
         System.out.println("Number of feeds to list: " + argParser.getNumber());
     }
 
 
    /** Instantiates a new RSSReader, calls it with the arguments from
    * the command line.
    *
    *
    * See: http://grepcode.com/file/repo1.maven.org/maven2/org.rometools/rome-fetcher/1.2/org/rometools/fetcher/samples/FeedReader.java
    */
     public static void main(String[] args) {
         parseArguments(args);
         // Parse the file. Return some array, FeedUrls.
         ArrayList<String> feedUrls = null;
         RSSReader reader = RSSReader(feedUrls);
         // or like:
         // reader.set_sort('name');
         // reader.run('args');
     }
 
 }
