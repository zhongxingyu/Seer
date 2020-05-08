 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
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
     private Date lastRun = new Date(Long.MIN_VALUE);
 
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
     * Does formatting and output and should respond to the following config options:
     * --number (number of posts)
     * --since (since a date in format yyyy-mm-dd, which is a date object)
     * --title
     * --description
     * --newest (optional)
     */
     public void display() {
         int number = argParser.getNumber();
         Date since = argParser.getSince();
         Pattern title = argParser.getTitle();
         boolean isByDate = argParser.isByDate();
         boolean isByAlpha = argParser.isByAlpha();
         boolean isNewest = argParser.isNewest();
         boolean isDescription = argParser.isDescription();
         
         if (title != null) {
         	displayByTitle(number, since, isDescription, isDescription, title, isNewest);
         }
         else if (isByDate) {
         	displayByDate(number, since, isDescription, isNewest);
 		}
 		else {
 			displayByFeeds(number, since, isByAlpha, isDescription, isNewest);
 		}
         
         this.lastRun = new Date();
         
     }
     
     /**
      * displayByFeeds is called by display, which is the default display setting if no date or title arguments are provided.
      * @param number Number of articles to be displayed
      * @param since Earliest date from which an article can be displayed
      * @param isByAlpha Determines whether the display should be alphabetically by title
      * @param isDescription Determines whether to print the articles description too
      */
     public void displayByFeeds(int number, Date since, boolean isByAlpha, boolean isDescription, boolean isNewest) {
     	ArrayList<SyndFeedImpl> curFeeds;
     	if (isByAlpha)
     		curFeeds = sortPostsByAlpha();
     	else
     		curFeeds = feeds;
     	
 		for (SyndFeedImpl feed : curFeeds) {
 			System.out.println(feed.getTitle().toUpperCase());
 			int articleNum = 1;
 		    for (Iterator i = feed.getEntries().iterator(); i.hasNext();) {
 		        SyndEntryImpl entry = (SyndEntryImpl) i.next();
 		        if ((isNewest && entry.getPublishedDate().after(lastRun)) || !isNewest) {
 			         System.out.println("(" + articleNum + ")" + entry.getTitle() + "\t" + entry.getPublishedDate() + "\t" + entry.getLink());
 						if (isDescription) {
 								System.out.println(entry.getDescription());
 						}
 						articleNum++;		        	
 		        }
 
 		     }
 		    System.out.println();
 		}
     }
 
     /**
      * displayByFeeds is called by display, which displays articles by date rather than by news source.
      * @param number Number of articles to be displayed
      * @param since Earliest date from which an article can be displayed
      * @param isDescription Determines whether a description is included with the article
      */
     public void displayByDate(int number, Date since, boolean isDescription, boolean isNewest) {
         ArrayList<SyndEntryImpl> posts;
         posts = sortPostsByDate();
     	
         int articleNum = 1;
         for (int i = 0; i < number; i++) {
         	SyndEntryImpl post = posts.get(i);
 	        if ((isNewest && post.getPublishedDate().after(lastRun)) || !isNewest) {
 				String feedOutput = "(" + articleNum + ")" + post.getTitle() + "\t" + post.getPublishedDate() + "\t" + post.getLink();
 				System.out.println(feedOutput);
 				if (isDescription) {
 					System.out.println(post.getDescription());
 				}
 				articleNum++;
 	        }   	
         }
     }
     
     /**
     * displayByTitle displayes articles that match a given regular expression for a title
      * @param number Number of posts
      * @param since Earliest date from which an article can be displayed
      * @param isByAlpha Determines whether we sort the titles alphabetically
      * @param isDescription Determines whether we show the article description
      * @param title The pattern we are using to match article titles
      */
     public void displayByTitle(int number, Date since, boolean isByAlpha, boolean isDescription, Pattern title, boolean isNewest) {
     	ArrayList<SyndFeedImpl> curFeeds;
     	if (isByAlpha)
     		curFeeds = sortPostsByAlpha();
     	else
     		curFeeds = feeds;
     	
     	int articleNum = 1;
 		for (SyndFeedImpl feed : curFeeds) {
 		    for (Iterator i = feed.getEntries().iterator(); i.hasNext();) {
 		        SyndEntryImpl entry = (SyndEntryImpl) i.next();
 		        if ((isNewest && entry.getPublishedDate().after(lastRun)) || !isNewest) {
 			        Matcher matcher = title.matcher(entry.getTitle());
 			        if (matcher.find()) {
 				         System.out.println("(" + articleNum + ")" + entry.getTitle() + "\t" + entry.getPublishedDate() + "\t" + entry.getLink());
 							if (isDescription) {
 									System.out.println(entry.getDescription());
 							}
 							articleNum++;
 			        }        	
 		        }
 		     }
 		}
     }
     
     /**
     * TODO: double check the object return type
     * Gets all posts from a particular feed and will accept a synd feed impl as a parameter
     * @param curFeed the current feed from which we want to get posts
     * @return an array list of SyndEntry objects, which are the posts
     */
     public List<SyndEntryImpl> getPostsFromFeed(SyndFeedImpl curFeed) {
     	return (List<SyndEntryImpl>) curFeed.getEntries();
 
     }
 
     /**
     * Gets all posts from all subscribed feeds.
     * @return allPosts an array list of all posts
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
     * This is called by the sortPosts method.  It sorts posts when the mode is alpha; that is, the user wants feeds sorted alphabetically
     * @return posts The posts sorted alphabetically
     */
     public ArrayList<SyndFeedImpl> sortPostsByAlpha() {
     	ArrayList<SyndFeedImpl> sortedFeeds = new ArrayList<SyndFeedImpl>(feeds);
         Collections.sort(sortedFeeds, new Comparator<SyndFeedImpl>() {
             public int compare(SyndFeedImpl o1, SyndFeedImpl o2) {
                 String a = o1.getTitle();
                 String b = o2.getTitle();
                 return a.compareTo(b);
             }
         });
         return sortedFeeds;
     }
 
     /**
     * This is called by the sortPosts method.  It sorts posts when the mode is date; that is, the user wants posts sorted chronologically
     * @return posts The posts sorted by date
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
     * Parses the command line arguments provided by the user.  
     * @param args The arguments provided by the suer
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
