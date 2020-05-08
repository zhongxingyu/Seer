 package newsrack.filter;
 
 import java.io.File;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import newsrack.database.NewsItem;
 import newsrack.util.IOUtils;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.sun.syndication.feed.synd.SyndCategory;
 import com.sun.syndication.feed.synd.SyndCategoryImpl;
 import com.sun.syndication.feed.synd.SyndContent;
 import com.sun.syndication.feed.synd.SyndContentImpl;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndEntryImpl;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.feed.synd.SyndFeedImpl;
 import com.sun.syndication.io.SyndFeedInput;
 import com.sun.syndication.io.SyndFeedOutput;
 import com.sun.syndication.io.XmlReader;
 
 public class OutputFeed implements java.io.Serializable
 {
 // ############### STATIC FIELDS AND METHODS ############
 	private static final short MAX_RSS_ITEMS = 25;
    private static final Log   _log          = LogFactory.getLog(OutputFeed.class);
 
 	/** This is there to workaround bugs/strict-checking in my/Rome code **/
 	static void rssFeedRomeWorkaround_FIXME(final SyndFeed f, String title, String url, String desc)
 	{
 		f.setFeedType("rss_2.0");
 		f.setTitle(title);
 		f.setLink(url);
 		f.setDescription(desc);
 	}
 
 // ############### NON-STATIC FIELDS AND METHODS ############
 	private SyndFeed  _rssFeed;		// RSS feed for this category
 	private ArrayList _feedItems;		// List of items in this feed
 	private ArrayList _newItemsSinceLastDownload; 	// List of items added since last download
 	private int       _numItemsSinceLastDownload;	// Number of new items since last download
 
 	private String _rssDir;
 	private String _feedFileName;
 	private String _name;
 	private String _url;
 	private String _title;
 	private String _desc;
 	private String _taxonomyPath;
 
 	public OutputFeed(String feedFile, String rssDir, String name, String url, String title, String desc, String taxoPath)
 	{
 		_feedFileName = feedFile;
 		_rssDir = rssDir;
 		_name = name;
 		_url = url;
 		_title = title;
 		_desc = desc;
 		_taxonomyPath = taxoPath;
 
 		_newItemsSinceLastDownload = new ArrayList();
 		_numItemsSinceLastDownload = 0;
 	}
 
 	public int getNumItemsSinceLastDownload() { return _numItemsSinceLastDownload; }
 
 	private void createNewRSSFeed()
 	{
 		final SyndFeed f = new SyndFeedImpl();
 		rssFeedRomeWorkaround_FIXME(f, _title, _url, _desc);
 		f.setPublishedDate(new Date());
 		final SyndCategory sc = new SyndCategoryImpl();
 		sc.setName(_name);
 		sc.setTaxonomyUri("http://newsrack.in/" + _taxonomyPath);		// FIXME: BAD!!
 		final ArrayList cats = new ArrayList(1);
 		cats.add(sc);
 		f.setCategories(cats);
 
 		_rssFeed   = f;
 		_feedItems = new ArrayList();
 		_rssFeed.setEntries(_feedItems);
 		_numItemsSinceLastDownload = 0;
 		_newItemsSinceLastDownload = new ArrayList();
 	}
 
 	private void purgeOldFeedEntries()
 	{
 		final int numFeedItems = _feedItems.size();
 		final int numNewItems  = _newItemsSinceLastDownload.size();
 
 			/* Just retains MAX_RSS_ITEMS */
 		if (numFeedItems >= MAX_RSS_ITEMS) {
 			/* ..... But, what if more than these many items were added to
 				 * this category in one download?  So, need to retain either 
 				 * MAX_RSS_ITEMS or all items added since last download */
 			if (numNewItems < MAX_RSS_ITEMS) {
 					/* Okay, prune to MAX_RSS_ITEMS */
 				int       i        = 0;
 				final ArrayList newItems = new ArrayList();
 				final Iterator it        = _feedItems.iterator();
 				while (i < MAX_RSS_ITEMS) {
 					final SyndEntry sfe = (SyndEntry)it.next();
 					newItems.add(sfe);
 					i++;
 				}
 				_rssFeed.setEntries(newItems);
 				_feedItems = newItems;
 			}
 			else {
 					/* Just retain everything added since the last download */
 				_rssFeed.setEntries(_newItemsSinceLastDownload);
 				_feedItems = _newItemsSinceLastDownload;
 			}
       }
 		_numItemsSinceLastDownload = numNewItems;
 		_newItemsSinceLastDownload = new ArrayList();
 	}
 
 	public void readInCurrentRSSFeed()
 	{
 		final File feedFile = new File(_feedFileName);
		if (!feedFile.exists() || (feedFile.exists() && (feedFile.length() == 0))) {
 			IOUtils.createDir(_rssDir);
 			createNewRSSFeed();
 			publish();
 		}
 		else {
 			try {
 				_rssFeed   = (new SyndFeedInput()).build(new XmlReader(feedFile));
 				_feedItems = (ArrayList)_rssFeed.getEntries();
 			}
 			catch (final Exception e) {
 				_log.error("ERROR reading feed " + _feedFileName, e);
 			}
 			rssFeedRomeWorkaround_FIXME(_rssFeed, _title, _url, _desc);
 		}
 	}
 
 	public void addNewsItem(final NewsItem ni, final List<Category> cats)
 	{
 		final Category  firstCat = cats.get(0);
 		final String    src = ni.getSourceNameForUser(firstCat.getUser());
 		final SyndEntry si = new SyndEntryImpl();
 		si.setTitle(ni.getTitle() + " - " + src);
 		si.setLink(ni.getURL());
 		final Date d = ni.getDate();
 		if (d != null)
 			si.setPublishedDate(d);
 		final String auth = ni.getAuthor();
 		if (auth != null)
 			si.setAuthor(auth);
 
 			// Set up categories
 		final ArrayList syndCats = new ArrayList();
 		for (final Category c: cats) {
 			final SyndCategory sc = new SyndCategoryImpl();
 			sc.setTaxonomyUri("http://newsrack.in/" + c.getTaxonomyPath());
 			sc.setName(c._name);
 			syndCats.add(sc);
 		}
 		si.setCategories(syndCats);
 
       	// ROME API ALWAYS REQUIRES SOME DESCRIPTION
       final SyndContent description = new SyndContentImpl();
       description.setType("text/plain");
 		final String desc = ni.getDescription();
 		if (desc != null)
 			description.setValue(desc);
       si.setDescription(description);
 
 			// Add to the list of items (at the HEAD)
 		if (_log.isDebugEnabled()) _log.debug("ADDING " + si + " + to feed items ");
 		_feedItems.add(0, si);
 		_newItemsSinceLastDownload.add(0, si);
 		_numItemsSinceLastDownload = _newItemsSinceLastDownload.size();
 	}
 
 	private void publish()
 	{
 		// FIXME: Assumes a flat-file system for publishing RSS feeds
 		// which is okay .. but assumptions of this sort are distributed
 		// throughout the system.  Clean up the documentation by writing
 		// a proper design spec, and then all these fixmes can go away.
 
 		_rssFeed.setPublishedDate(new Date()); // Update the publishing date of the feed
 		purgeOldFeedEntries();
 		try {
 			PrintWriter pw = IOUtils.getUTF8Writer(_feedFileName);
 			(new SyndFeedOutput()).output(_rssFeed, pw);
 			pw.close();
 		}
 		catch (Exception e) {
 			_log.error("Exception while writing RSS feed " + _feedFileName, e);
 		}
 	}
 
 	public void invalidate()
 	{
 		File feedFile = new File(_feedFileName);
 		if (!feedFile.exists()) {
 			IOUtils.createDir(_rssDir);
 			createNewRSSFeed();
 			publish();
 		}
 	}
 
 	public void update()
 	{
 			// 0. If there have been no new items since last download
 			// there is nothing to store!
 		if (_numItemsSinceLastDownload == 0)
 			return;
 
 			// 1. Publish the  RSS feed only if new items have been
 			// added to the category
 		publish();
 	}
 }
