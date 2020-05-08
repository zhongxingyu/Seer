 package newsrack.database.sql.scripts;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import newsrack.NewsRack;
 import newsrack.database.DB_Interface;
 import newsrack.database.NewsIndex;
 import newsrack.database.NewsItem;
 import newsrack.database.sql.SQL_NewsIndex;
 import newsrack.database.sql.SQL_NewsItem;
 import newsrack.database.sql.SQL_StmtExecutor;
 import newsrack.database.sql.SQL_ValType;
 import newsrack.filter.Category;
 import newsrack.filter.Issue;
 import newsrack.user.User;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class FixupTools
 {
    private static Log _log = LogFactory.getLog((new FixupTools()).getClass());
 
 	private static DB_Interface _db;
 
 	public static void findNewsItemsWithMissingFiles(long newsIndexKey)
 	{
 		NewsIndex            ni   = _db.getNewsIndex(newsIndexKey);
 		Collection<NewsItem> news = _db.getArchivedNews(ni);
       StringBuffer         buf  = new StringBuffer();
 		for (NewsItem n: news) {
 			try {
 				n.getReader().close();
 			}
 			catch (java.io.FileNotFoundException e) {
             buf.append(n.getKey()).append(", ");
 			}
 			catch (Exception e) {
 				System.out.println("Exception: " + e + " for news item: " + n.getKey());
 			}
 		}
 
       buf.append("-1");
 
       System.out.println("select count(*) from news_collections where ni_key = " + newsIndexKey + ";");
       System.out.println("select count(*) from cat_news where n_key in (" + buf + ");");
       System.out.println("delete from news_collections where n_key in (" + buf + ");");
       System.out.println("delete from news_items where n_key in (" + buf + ");");
       System.out.println("delete from news_item_url_md5_hashes where n_key in (" + buf + ");");
       System.out.println("delete from cat_news where n_key in (" + buf + ");");
 	}
 
    public static void changeNewsIndexDate(Long niKey, Date newDate)
    {
 		((SQL_NewsIndex)_db.getNewsIndex(niKey)).changeDate(newDate);
    }
 
 	public static void addNewsItemsToCat(Long catKey, String newsKeysFile)
 	{
 		try {
 			Category       cat = _db.getCategory(catKey);
 			BufferedReader br  = new BufferedReader(new java.io.FileReader(newsKeysFile));
 			String         line;
 
 			do {
 				line = br.readLine();
 				if (line != null) {
                NewsItem ni = _db.getNewsItem(Long.parseLong(line));
                //System.out.println("Will add " + ni.getTitle());
 					_db.addNewsItem(ni, cat, 2);
             }
 			} while (line != null);
 
 			br.close();
 		}
 		catch (Exception e) {
 			System.out.println("Exception: " + e);
 		}
 	}
 
 	public static void updateCountsForAllIssues()
 	{
       List<Issue> issues = User.getAllValidatedIssues();
 		for (Issue i: issues) {
 			i.storeNewsToArchive();
 		}
 	}
 
    public static void refetchNewsForNewsIndex(NewsIndex ni, Long minLength)
    {
       Collection<NewsItem> news = _db.getArchivedNews(ni);
       List<NewsItem> downloadedNews = new ArrayList<NewsItem>();
       for (NewsItem n: news) {
          File origOrig = ((SQL_NewsItem)n).getOrigFilePath();
          File origFilt = ((SQL_NewsItem)n).getFilteredFilePath();
          if (origFilt.exists() && (origFilt.length() < minLength)) {
             System.out.println("Will have to download " + origFilt + " again!");
             origOrig.delete();
             origFilt.delete();
             try {
                n.download(_db);
                downloadedNews.add(n);
                newsrack.util.StringUtils.sleep(4);
             }
             catch (Exception e) {
                System.err.println("Error downloading news item: " + e);
             }
          }
          else if (origFilt.exists()) {
             System.out.println("No need to download " + origFilt + " again .. it has size " + origFilt.length());
          }
          else {
             System.out.println("Bath path: " + origFilt);
          }
       }
 
          /* Now fetch all topics that monitor this feed! */
       List<Long> tkeys = (List<Long>)SQL_StmtExecutor.query("SELECT DISTINCT(t_key) FROM topic_sources WHERE feed_key = ?",
                                                             new SQL_ValType[] {SQL_ValType.LONG},
                                                             new Object[]{((SQL_NewsIndex)ni).getFeedKey()},
                                                             SQL_StmtExecutor._longProcessor,
                                                             false);
          // Reclassify!
       for (Long tkey: tkeys) {
          Issue i = _db.getIssue(tkey);
          if (!i.isFrozen()) {
             System.out.println("Reclassifying for " + i.getName() + " for user " + i.getUser().getUid());
             i.scanAndClassifyNewsItems(null, downloadedNews);
          }
       }
 
          // GC!
       System.gc();
    }
 
    public static void refetchNewsForNewsIndex(Long niKey, Long minLength)
    {
       refetchNewsForNewsIndex(_db.getNewsIndex(niKey), minLength);
    }
 
    public static void refetchFeedInDateRange(Long feedKey, Long minLength, Date startDate, Date endDate)
    {
       java.util.Iterator<? extends NewsIndex> nis = _db.getIndexesOfAllArchivedNews(feedKey, startDate, endDate);
       while (nis.hasNext()) {
          NewsIndex ni = nis.next();
          System.out.println("Will fetch news for index: " + ni.getKey() + "; date is " + ni.getCreationTime());
          refetchNewsForNewsIndex(ni, minLength);
       }
    }
 
 	public static int getNextNestedSetId(int nsId, Category cat)
 	{
 			// Do a pre-order traversal of the category tree and assign left-right nested-set ids for all categories
 			// Lookup and read about nested sets if you don't know what they are -- quite simple and elegant solution
 			// for storing hierarchical objects in a database while supporting efficient operations on the hierarchy.
 		int next = nsId+1;
 		for (Category cc: cat.getChildren())
 			next = getNextNestedSetId(next, cc) + 1;
 
 			// Save to db!
 		Long cKey = cat.getKey();
 		SQL_StmtExecutor.update("UPDATE categories SET lft = ?, rgt = ? WHERE c_key = ?",
 										new SQL_ValType[] {SQL_ValType.INT, SQL_ValType.INT, SQL_ValType.LONG},
 										new Object[] {nsId, next, cKey});
 
 		return next;
 	}
 
 	public static void assignNestedSetIds()
 	{
 		List<Issue> issues = _db.getAllValidatedIssues();
 		for (Issue i: issues) {
 				// Assign and save nested-set ids for all cats
 			int next = 1;
 			for (Category c: i.getCategories())
 				next = getNextNestedSetId(next, c) + 1;
 		}
 	}
 
 	public static void outputLocalFilePathsForCategorizedNews(Long catKey)
 	{
 		Category c = _db.getCategory(catKey);
 		List<NewsItem> news;
 		int start = 0;
 		do {
 		  news = _db.getNews(c, start, 1000);
 		  start += 1000;
 		  for (NewsItem ni: news) {
 			  File f =  ni.getFilteredFilePath();
 			  System.out.println((f == null) ? ("NULL PATH for " + ni.getKey()) : f.getPath());
 		  }
 		} while ((news != null) && !news.isEmpty());
 	}
 
 	public static void canonicalizeURLs(Long feedKey, Date startDate, Date endDate)
    {
       java.util.Iterator<? extends NewsIndex> nis = _db.getIndexesOfAllArchivedNews(feedKey, startDate, endDate);
       while (nis.hasNext()) {
          Collection<NewsItem> news = _db.getArchivedNews(nis.next());
       	for (NewsItem n: news)
 				((SQL_NewsItem)n).canonicalizeURL();
 		}
 	}
 
 	public static void canonicalizeURLs(String domain, Date startDate, Date endDate)
    {
       List<Long> fkeys = (List<Long>)SQL_StmtExecutor.query("SELECT feed_key FROM feeds WHERE url_root like ?",
                                                             new SQL_ValType[] {SQL_ValType.STRING},
                                                             new Object[]{"%" + domain + "%"},
                                                             SQL_StmtExecutor._longProcessor,
                                                             false);
 		for (Long k: fkeys) {
 			System.out.println("------ Canonicalizing for feed: " + k + " ------");
 			canonicalizeURLs(k, startDate, endDate);
 		}
 	}
 
 	public static void revalidateUsers(Long ukey)
 	{
 		if (ukey == null) {
 			/* Users that dont import from anyone else! */
 			List<Long> ukeys_1 = (List<Long>)SQL_StmtExecutor.query(
 											"SELECT u_key FROM users WHERE NOT EXISTS (SELECT * FROM import_dependencies WHERE importing_user_key = u_key) AND validated=1",
 											new SQL_ValType[] {},
 											new Object[]{},
 											SQL_StmtExecutor._longProcessor,
 											false);
 
 			/* Invalidate */
 			for (Long k: ukeys_1)
 				_db.getUser(k).invalidateAllIssues();
 
 			/* Validate */
 			for (Long k: ukeys_1) {
 				User u = _db.getUser(k);
 				try {
 					u.validateAllIssues(false);
 				}
 				catch (Exception e) {
 					System.out.println("Exception " + e + " validating user: " + u.getName());
 				}
 			}
 		}
 		else {
 			try {
 				_db.getUser(ukey).invalidateAllIssues();
 				_db.getUser(ukey).validateAllIssues(false);
 			}
 			catch (Exception e) {
 				System.out.println("Exception " + e + " validating user: " + ukey);
 			}
 		}
 	}
 
 	public static void main(String[] args) throws Exception
 	{
 		if (args.length < 2) {
 			System.out.println("Usage: java newsrack.database.sql.scripts.FixupTools <properties-file> <action> [<other-optional-args>]");
 			System.exit(0);
 		}
 
    	String appPropertiesFile = args[0];
 		String action = args[1];
 
 		System.out.println("Properties file: " + appPropertiesFile);
 		NewsRack.startup(null, appPropertiesFile);
       _db = NewsRack.getDBInterface();
 
       if (action.equals("update-counts")) {
 	      updateCountsForAllIssues();
       }
       else if (action.equals("find-newsitems-without-files")) {
 	      findNewsItemsWithMissingFiles(Long.parseLong(args[2]));
       }
       else if (action.equals("change-newsindex-date")) {
 	      changeNewsIndexDate(Long.parseLong(args[2]), newsrack.database.sql.SQL_NewsItem.DATE_PARSER.get().parse(args[3]));
       }
       else if (action.equals("add-news-to-cat")) {
 	      addNewsItemsToCat(Long.parseLong(args[2]), args[3]);
       }
       else if (action.equals("refetch-news")) {
          refetchNewsForNewsIndex(Long.parseLong(args[2]), Long.parseLong(args[3]));
       }
       else if (action.equals("canonicalize-urls-for-feed")) {
          Date sd = newsrack.web.BrowseAction.DATE_PARSER.get().parse(args[3]);
          Date ed = newsrack.web.BrowseAction.DATE_PARSER.get().parse(args[4]);
          canonicalizeURLs(Long.parseLong(args[2]), sd, ed);
       }
       else if (action.equals("canonicalize-urls-for-domain")) {
          Date sd = newsrack.web.BrowseAction.DATE_PARSER.get().parse(args[3]);
          Date ed = newsrack.web.BrowseAction.DATE_PARSER.get().parse(args[4]);
          canonicalizeURLs(args[2], sd, ed);
       }
       else if (action.equals("refetch-feeds-in-date-range")) {
          Date sd = newsrack.web.BrowseAction.DATE_PARSER.get().parse(args[2]);
          Date ed = newsrack.web.BrowseAction.DATE_PARSER.get().parse(args[3]);
          Long minLength = Long.parseLong(args[4]);
          for (int i = 5; i < args.length; i++) {
             Long fk = Long.parseLong(args[i]);
             refetchFeedInDateRange(fk, minLength, sd, ed);
          }
       }
       else if (action.equals("setup-topic-nested-sets")) {
 			assignNestedSetIds();
       }
 		else if (action.equals("get-cat-files")) {
 	      outputLocalFilePathsForCategorizedNews(Long.parseLong(args[2]));
 		}
 		else if (action.equals("revalidate-users")) {
 			revalidateUsers(args.length > 2 ? Long.parseLong(args[2]) : null);
 		}
       else {
          System.out.println("Unknown action: " + action);
       }
 
       System.out.println("\n -- DONE --");
       System.out.flush();
 	}
 }
