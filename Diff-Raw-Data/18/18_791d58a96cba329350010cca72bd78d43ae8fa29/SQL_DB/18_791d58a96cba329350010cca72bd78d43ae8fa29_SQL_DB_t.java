 package newsrack.database.sql;
 
 import newsrack.database.DB_Interface;
 import newsrack.database.ObjectCache;
 import newsrack.database.NewsIndex;
 import newsrack.database.NewsItem;
 
 import newsrack.NewsRack;
 import newsrack.user.User;
 import newsrack.util.IOUtils;
 import newsrack.util.StringUtils;
 import newsrack.util.Tuple;
 import newsrack.util.Triple;
 import newsrack.archiver.Feed;
 import newsrack.archiver.Source;
 import newsrack.filter.Concept;
 import newsrack.filter.Category;
 import newsrack.filter.Filter;
 import newsrack.filter.Filter.FilterOp;
 import newsrack.filter.Filter.RuleTerm;
 import newsrack.filter.Issue;
 import newsrack.filter.PublicFile;
 import newsrack.filter.Privacy;
 import newsrack.filter.NR_Collection;
 import newsrack.filter.NR_CollectionType;
 import newsrack.filter.NR_SourceCollection;
 import newsrack.filter.NR_ConceptCollection;
 import newsrack.filter.NR_CategoryCollection;
 import newsrack.filter.NR_FilterCollection;
 
 import static newsrack.filter.Filter.FilterOp.*;
 import static newsrack.filter.NR_CollectionType.*;
 import static newsrack.database.sql.SQL_Stmt.*;
 import static newsrack.database.sql.SQL_ValType.*;
 
 import java.io.File;
 import java.io.Reader;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.PrintWriter;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Map;
 import java.util.Collection;
 import java.util.Hashtable;
 import java.util.HashMap;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.LinkedList;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.sql.*;
 import java.sql.Connection;
 
 import snaq.db.ConnectionPool;
 
 import org.apache.commons.digester.*;
 import org.apache.commons.digester.xmlrules.*;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * class <code>SQL_DB</code> implements db backend using
  * the MySQL RDBMS and the file system of the underlying OS.
  */
 public class SQL_DB extends DB_Interface
 {
 // ############### STATIC FIELDS AND METHODS ############
 	private static String GLOBAL_USERS_ROOTDIR;
 	private static String GLOBAL_NEWS_ARCHIVE_DIR;
 	private static String USER_INFO_DIR;
 	private static String DB_NAME;
 	private static String DB_USER;
 	private static String DB_PASSWORD;
 	private static String DB_URL;
 	private static String DB_DRIVER;
 	private static int    DB_CONNPOOL_SIZE;
 	private static int    DB_MAX_CONNS;
 
 	private static final String SRC_COLLECTION = "SRC";
 	private static final String CPT_COLLECTION = "CPT";
 	private static final String CAT_COLLECTION = "CAT";
 	private static final Object[] EMPTY_ARGS = new Object[] {};
 
 	// date format used for directory names for archiving news
 		// Solution courtesy: http://publicobject.com/2006/05/simpledateformat-considered-harmful.html
 		// Use a thread local -- since this object cannot be shared between threads!
    private static final ThreadLocal<SimpleDateFormat> DATE_PARSER = new ThreadLocal<SimpleDateFormat>() {
 		protected SimpleDateFormat initialValue() { return new SimpleDateFormat("yyyy" + File.separator + "M" + File.separator + "d"); }
 	};
 
    	// Logging output for this class
    static Log _log = LogFactory.getLog(SQL_DB.class);
 		// THE database!
 	static SQL_DB _sqldb = null;
 		// Connection pooling
 	static ConnectionPool _dbPool = null;
 
 	public static DB_Interface getInstance()
 	{
 		if (_sqldb == null)
 			_sqldb = new SQL_DB();
 
 		return _sqldb;
 	}
 
 	private static void createFile(String fname) throws java.io.IOException
 	{
 		File f = new File(fname);
 		try {
 			if (!f.exists())
 				f.createNewFile();
 		}
 		catch (java.io.IOException e) {
 			StringUtils.error("Error creating file " + f);
 			throw e;
 		}
 	}
 
 	private static boolean inBetweenDates(String ddy, String ddm, String ddd, String stDate, String endDate)
 	{
 		if (ddd.length() == 1) ddd = "0" + ddd;
 		if (ddm.length() == 1) ddm = "0" + ddm;
 		String   dirDate = ddy + ddm + ddd;
 		int      dDate   = Integer.parseInt(dirDate);
 		int      sDate   = Integer.parseInt(stDate);
 		int      eDate   = Integer.parseInt(endDate);
 		return (sDate <= dDate) && (dDate <= eDate);
 	}
 
 	private static String getDateString(String y, String m, String d)
 	{
 		if (m.startsWith("0")) m = m.substring(1);
 		if (d.startsWith("0")) d = d.substring(1);
 		return y + File.separator + m + File.separator + d;
 	}
 
    protected static Tuple<String,String> splitURL(String url)
    {
 		int i = 1 + url.indexOf('/', 7);	// Ignore leading "http://"
       return new Tuple(url.substring(0, i), url.substring(i));
    }
 
 /**
 	public static void main(String[] args)
 	{
 		NewsRack.loadDefaultProperties();
 		SQL_DB sqldb = new SQL_DB();
 		sqldb.initDirPaths();
 	}
 **/
 
 // ############### NON-STATIC FIELDS AND METHODS ############
 
 	private ObjectCache _cache;
 	private Map<Tuple<Long,Long>, String> _sourceNames;
 	private Map<Long, List<Category>> _leafCatsToCommit;
 
 	private void initDirPaths() 
 	{
 		GLOBAL_USERS_ROOTDIR    = NewsRack.getDirPathProperty("sql.userHome");
 		GLOBAL_NEWS_ARCHIVE_DIR = NewsRack.getDirPathProperty("sql.archiveHome");
 		USER_INFO_DIR           = NewsRack.getDirPathProperty("sql.userInfoDir");
 	}
 
 	private SQL_DB() 
 	{
 			// Initialize paths
 		initDirPaths();
 
 		try {
 			IOUtils.createDir(GLOBAL_USERS_ROOTDIR);
 			IOUtils.createDir(GLOBAL_NEWS_ARCHIVE_DIR);
 			IOUtils.createDir(GLOBAL_NEWS_ARCHIVE_DIR + "orig");
 			IOUtils.createDir(GLOBAL_NEWS_ARCHIVE_DIR + "filtered");
 			IOUtils.createDir(GLOBAL_NEWS_ARCHIVE_DIR + "tmp");
 		}
 		catch (Exception e) {
 			_log.error(e);
 			throw new Error("Error initializing DB interface.  Cannot continue intialization");
 		}
 
 			// Set up the db driver
 		DB_DRIVER        = NewsRack.getProperty("sql.driver");
 		DB_URL           = NewsRack.getProperty("sql.dbUrl");
 		DB_NAME          = NewsRack.getProperty("sql.dbName");
 		DB_USER          = NewsRack.getProperty("sql.user");
 		DB_PASSWORD      = NewsRack.getProperty("sql.password");
 		DB_CONNPOOL_SIZE = Integer.parseInt(NewsRack.getProperty("sql.dbConnPoolSize"));
 		DB_MAX_CONNS     = Integer.parseInt(NewsRack.getProperty("sql.dbMaxConnections"));
 
 			// Load the connection driver and initialize the connection pool
 		try {
 			Class.forName(DB_DRIVER).newInstance();
 			String jdbcUrl = DB_URL + "/" + DB_NAME 
 								  + "?useUnicode=true"
 								  + "&characterEncoding=UTF-8"
 								  + "&characterSetResults=utf8";
 			_log.info("JDBC URL IS " + jdbcUrl);
 			_dbPool = new ConnectionPool("NR_DB_POOL",
 												  DB_CONNPOOL_SIZE,
 												  DB_MAX_CONNS,
 												  180000, 	// Unused connections in the pool expire after 3 minutes
 												  jdbcUrl,
 												  DB_USER,
 												  DB_PASSWORD);
          _dbPool.setLogging(true);
 				// Initialize the pool with one connection .. rest are created on demand
 			_dbPool.init(1);
 		}
 		catch (Exception e) {
 			_log.error("Error initializing SQL DB -- ");
 			e.printStackTrace();
 		}
 
          // Initialize the sql stament
       SQL_Stmt.init(_log, this);
       SQL_StmtExecutor.init(_dbPool, _log, this);
 
 			// create a cache
 		_cache = new ObjectCache();
 
 			// create a mapping from <feed,user> --> source-name
 		_sourceNames = new HashMap<Tuple<Long,Long>, String>();
 	}
 
 	/**
 	 * This method returns the global news archive directory
 	 */
 	public String getGlobalNewsArchive()
 	{
 		return GLOBAL_NEWS_ARCHIVE_DIR;
 	}
 
 	/** This method clears out the cache */
 	public void clearCache()
 	{
 		_cache.clearCaches();
 	}
 
 	/** This method clears out the downloaded news table */
 	public void clearDownloadedNewsTable()
 	{
 		_cache.removeEntriesForGroups(new String[]{"DN_NEWS"});
 		CLEAR_DOWNLOADED_NEWS_TABLE.execute(new Object[]{});
 	}
 
 	private String getUserHome(User u)
 	{
 		return GLOBAL_USERS_ROOTDIR + u.getUid() + File.separator;
 	}
 
 	/**
 	 * The profile of the user is initialized from the database
 	 * reading any configuration files, initializing fields, etc.
 	 * for the user.
 	 *
 	 * @param u  User whose profile has to be initialized
 	 */
    public void initProfile(User u) throws Exception
 	{
 		_log.info("Request to init profile for user " + u.getUid());
 		u.validateIssues(false);
 	}
 
 	/**
 	 * This method returns the file upload area
 	 * @param u   User who is upload files
 	 */
 	public String getFileUploadArea(User u)
 	{
 		return getUserHome(u) + USER_INFO_DIR;
 	}
 
 	/**
 	 * This method returns the path of a work directory for the user
 	 * @param u  User who requires the work directory
 	 */
 	public String getUserSpaceWorkDir(User u)
 	{
 		return (u == null) ? "" : getFileUploadArea(u) + NewsRack.getDirPathProperty("sql.userWorkDir");
 	}
 
 	/**
 	 * This method initializes the DB interface
 	 */
 	public void init()
 	{
 		// nothing else to do ... the constructor has done everything
 		_leafCatsToCommit = new HashMap<Long, List<Category>>();
 	}
 
 	public Source getSource(Long key)
 	{
 		if (_log.isDebugEnabled()) _log.debug("Looking for source with key: " + key);
 
 		if (key == null)
 			return null;
 
 		Source s = (Source)_cache.get("SOURCE", key);
 		if (s == null) {
 			s = (Source)GET_SOURCE.get(key);
 			if (s != null) {
 				_cache.add("SOURCE", s.getUserKey(), key, s);
 				_cache.add("SOURCE", s.getUserKey(), s.getTag(), s);
 			}
 		}
 		return s;
 	}
 
 	public Feed getFeed(Long key)
 	{
 		if (_log.isDebugEnabled()) _log.debug("Looking for feed with key: " + key);
 
 		if (key == null)
 			return null;
 
 		Feed f = (Feed)_cache.get("FEED", key);
 		if (f == null) {
 			f = (Feed)GET_FEED.get(key);
 			if (f != null)
 				_cache.add("FEED", key, f);
 		}
 		return f;
 	}
 
 	public User getUser(Long key)
 	{
 		if (_log.isDebugEnabled()) _log.debug("Looking for user with key: " + key);
 
 		if (key == null)
 			return null;
 
 		User u = (User)_cache.get("USER", key);
 		if (u == null) {
 			u = (User)GET_USER.get(key);
 			if (u != null)
 				_cache.add("USER", key, u);
 		}
 
 		return u;
 	}
 
 	public Issue getIssue(Long key)
 	{
 		if (_log.isDebugEnabled()) _log.debug("Looking for issue with key: " + key);
 
 		if (key == null)
 			return null;
 
 		Issue i = (Issue)_cache.get("ISSUE", key);
 		if (i == null) {
 			i = (Issue)GET_ISSUE.get(key);
 			if (i != null)
 				_cache.add("ISSUE", i.getUserKey(), key, i);
 		}
 		return i;
 	}
 
 	public Concept getConcept(Long key)
 	{
 		if (_log.isDebugEnabled()) _log.debug("Looking for concept with key: " + key);
 
 		if (key == null)
 			return null;
 
 		Concept c = (Concept)_cache.get("CONCEPT", key);
 		if (c == null) {
 			Tuple<Long, Concept> t = (Tuple<Long, Concept>)GET_CONCEPT.get(key);
 			if (t != null) {
 				_cache.add("CONCEPT", t._a, key, t._b);
 				c = t._b;
 			}
 		}
 		return c;
 	}
 
 	public Filter getFilter(Long key)
 	{
 		if (_log.isDebugEnabled()) _log.debug("Looking for filter with key: " + key);
 
 		if (key == null)
 			return null;
 
 		Filter f = (Filter)_cache.get("FILTER", key);
 		if (f == null) {
 			Tuple<Long, Filter> t = (Tuple<Long, Filter>)GET_FILTER.get(key);
 			if (t != null) {
 				f = t._b;
 				_cache.add("FILTER", t._a, key, f);
 			}
 		}
 		return f;
 	}
 
 	public Category getCategory(Long key)
 	{
 		if (_log.isDebugEnabled()) _log.debug("Looking for category with key: " + key);
 
 		if (key == null)
 			return null;
 
 		Category c = (Category)_cache.get("CATEGORY", key);
 		if (c == null) {
 			Tuple<Long, Category> t = (Tuple<Long, Category>)GET_CATEGORY.get(key);
 			if (t != null) {
 				c = t._b;
 				_cache.add("CATEGORY", t._a, key, c);
 			}
 		}
 		return c;
 	}
 
 	public NewsItem getNewsItem(Long key)
 	{
 		NewsItem n = (NewsItem)_cache.get("NEWSITEM", key);
 		if (n == null) {
 			n = (NewsItem)GET_NEWS_ITEM.get(key);
 			if (n != null) {
 					// Add newsitem both by key & url
 				_cache.add("NEWSITEM", key, n);
 				_cache.add("NEWSITEM", n.getURL(), n);
 			}
 		}
 		return n;
 	}
 
 	public Feed getFeedWithTag(String feedTag)
 	{
 			// FIXME: Caching?
 		return (Feed)GET_FEED_FROM_TAG.execute(new Object[]{feedTag});
 	}
 
 	/**
 	 * Get a source object for a feed, given a feed url, a user and his/her preferred tag for the source
 	 * @param u       user requesting the source 
 	 * @param srcTag tag assigned by the user to the feed
 	 */
 	public Source getSource(User u, String srcTag)
 	{
 		String k = u.getUid() + ":" + srcTag;
 		Source s = (Source)_cache.get("SOURCE", k);
 		if (s == null) {
 			s = (Source)GET_USER_SOURCE.execute(new Object[]{u.getKey(), srcTag});
 			if (s != null) {
 				_cache.add("SOURCE", u.getKey(), s.getKey(), s);
 				_cache.add("SOURCE", u.getKey(), k, s);
 				s.setUser(u);
 			}
 		}
 		return s;
 	}
 
 	public Source getSource(Issue i, String srcTag)
 	{
 		User   u = i.getUser();
 		String k = i.getKey() + ":" + srcTag;
 		Source s = (Source)_cache.get("SOURCE", k);
 		if (s == null) {
 			s = (Source)GET_TOPIC_SOURCE.execute(new Object[]{i.getKey(), srcTag});
 			if (s != null) {
 				String[] cacheGrps = new String[] {u.getKey().toString(), i.getKey().toString()};
 				_cache.add("SOURCE", cacheGrps, s.getKey(), s);
 				_cache.add("SOURCE", cacheGrps, k, s);
 				s.setUser(u);
 			}
 			else {
 				_log.info("DID NOT FIND source: " + k);
 			}
 		}
 		return s;
 	}
 
 
 	public Long getSourceKey(Long userKey, Long feedKey, String srcTag)
 	{
 		return (Long)GET_USER_SOURCE_KEY.execute(new Object[] {userKey, feedKey, srcTag});
 	}
 
 	String getSourceName(Long feedKey, Long userKey)
 	{
 		/* Lot of users will use the default feed name ... this map will contain an entry
 		 * for the <feed, user> combination only for those who use custom names for feeds
 		 * CHECK addSource for the code that adds entries to this map */
 		String name = _sourceNames.get(new Tuple<Long, Long>(feedKey, userKey));
 		if (name == null)
 			name = getFeed(feedKey).getName();
 
 		return name;
 	}
 
 	void addSource(Long uKey, Source s)
 	{
 		Long sKey = s.getKey();
 		if (sKey == null) {
 			Long fKey = s.getFeed().getKey();
 				// Check if there is some other matching source object in the DB
 				// This can happen because the same source can be part of multiple collections
 			sKey = getSourceKey(uKey, fKey, s.getTag());
 			if (sKey == null) {
 				sKey = (Long)INSERT_USER_SOURCE.execute(new Object[] {uKey, fKey, s.getName(), s.getTag(), s.getCacheableFlag(), s.getCachedTextDisplayFlag()});
 					/* Lot of users will use the default feed name ... try to exploit that fact
 					 * by recording a name entry only for those using customized source names 
 					 * IMPORTANT: If you change this logic, do fix up 'getSourceName()' */
 				if (!s.getFeed().getName().equals(s.getName()))
 					_sourceNames.put(new Tuple<Long,Long>(s.getFeed().getKey(), uKey), s.getName());
 			}
 			s.setKey(sKey);
 		}
 	}
 
 	/**
 	 * Returns a system-wide unique id (string) for a rss feed with given URL.
 	 * @param feedURL  URL of the rss feed whose id is requested
 	 * @param sTag     Source tag used by the user (non-unique)
 	 * @param feedName Name for the feed (for use in webpages and rss feeds)
 	 */
 /**
 	public String getUniqueFeedTag(final String feedURL, final String sTag, final String feedName)
 	{
 		Tuple<String,String> t = splitURL(feedURL);
 		Object tag = GET_UNIQUE_FEED_TAG.execute(new Object[] {t._a, t._b});
 		if (tag != null) {
 			return (String)tag;
 		}
 		else if (sTag != null) {
 			Object intKey = INSERT_FEED.execute(new Object[] {feedName, t._a, t._b});
 			String fTag = intKey + "." + sTag;
 			SET_FEED_TAG.execute(new Object[] {fTag, intKey});
 			return fTag;
 		}
 		else {
 			return null;
 		}
 	}
 **/
 
 	public Feed getFeed(final String feedURL, String sTag, String feedName)
 	{
 		Tuple<String,String> t = splitURL(feedURL);
 		Feed f = (Feed)GET_FEED_FROM_URL.execute(new Object[] {t._a, t._b});
 		if (f != null) {
 			return f;
 		}
 		else {
 				// Parse the feed to fill in feed tag & name 
 			if ((sTag == null) || (feedName == null)) {
 				f = Feed.buildNewFeed(feedURL, sTag, feedName);
 				feedName = f.getName();
 				sTag = f.getTag();
 			}
 			Object intKey = INSERT_FEED.execute(new Object[] {feedName, t._a, t._b});
 			String fTag = intKey + "." + sTag;
 			SET_FEED_TAG.execute(new Object[] {fTag, intKey});
 			return new Feed((Long)intKey, fTag, feedName, feedURL);
 		}
 	}
 
 	public void updateConceptLexerToken(Concept c)
 	{
 		Long cKey = c.getKey();
 		if (cKey == null)
 			_log.error("ERROR! Looks like we have an uncommitted concept here!");
 		else
 			UPDATE_CONCEPT_TOKEN.execute(new Object[]{c.getLexerToken().getToken(), cKey});
 	}
 
 	/**
 	 * This does nothing in this version!
 	 */
 	public void loadUserTable() { }
 
 	/**
 	 * This method updates the database with changes made to an user's entry
 	 * @param u User whose info. needs to be updated 
 	 */
 	public void updateUser(User u) 
 	{ 
 		UPDATE_USER.execute(new Object[]{u.getPassword(), u.getName(), u.getEmail(), u.isValidated(), u.getKey()});
 
 			// Clear the cache
 		_cache.remove("USER", u.getKey());
 		_cache.remove("USER", u.getUid());
 	}
 
 	/**
 	 * This method updates a user attribute in the db (ex: registration date, last login, last edit, etc.)
 	 */
 	public void updateUserAttribute(User u, String attr, Object value)
 	{
 		if (attr.equals(LAST_LOGIN))
 		   UPDATE_LOGIN_DATE.execute(new Object[]{new Timestamp(((Date)value).getTime()), u.getKey()});
 		else
 			_log.error("Ignoring user attribute (" + attr + ") update request for user " + u.getUid());
 	}
 
 	/**
 	 * This method updates the database with changes made to a feed
 	 * @param f Feed whose info. needs to be updated
 	 */
 	public void updateFeedCacheability(Feed f)
 	{
 		UPDATE_FEED_CACHEABILITY.execute(new Object[] {f.getCacheableFlag(), f.getCachedTextDisplayFlag(), f.getKey()});
 
 		_cache.remove("FEED", f.getKey());
 	}
 
 	/**
 	 * This method shuts down the DB interface
 	 */
 	public void shutdown()
 	{
 		_dbPool.releaseForcibly();	// Release mysql connection pool resources
       _log.info("SQL DB has shut down! .... ");
 	}
 
 	public void printStats()
 	{
 		if (_log.isInfoEnabled())
 			_log.info(getStats());
 	}
 
 	/** This method returns stats */
 	public String getStats()
 	{
 		StringBuffer sb = new StringBuffer();
 		_cache.printStats(sb);
 		sb.append(SQL_StmtExecutor.getStats());
 		return sb.toString();
 	}
 
 	/**
 	 * @param fromUid  Uid of the user whose collection is being imported by another user
 	 * @param toUid    Uid of the user who is importing a collection from another user
 	 */
    public void recordImportDependency(String fromUid, String toUid)
 	{
 		// Don't bother with checking whether there are duplicates or not ... the sql query handles it!
 		INSERT_IMPORT_DEPENDENCY.execute(new Object[]{getUser(fromUid).getKey(), getUser(toUid).getKey()});
 	}
 
 	public List<Long> getCollectionImportersForUser(User u)
 	{
 		return (List<Long>)GET_IMPORTING_USERS.execute(new Object[]{u.getKey()});
 	}
 
 	/**
 	 * Gets the list of users who export collections for 'u'
 	 * @param u  User whose importers are required
 	 */
 	public List<Long> getCollectionExportersForUser(User u)
 	{
 		return (List<Long>)GET_EXPORTING_USERS.execute(new Object[]{u.getKey()});
 	}
 
 	public void clearImportDependenciesForUser(User u)
 	{
 			// Remove all of this users's import dependencies on other users
 		DELETE_IMPORT_DEPENDENCIES_FOR_USER.execute(new Object[]{u.getKey()});
 	}
 
 	/**
 	 * Record a collection (source, concept, category) with the database
 	 * @param c The collection to be added
 	 */
 	public void addProfileCollection(NR_Collection c)
 	{
 		if (_log.isDebugEnabled()) _log.debug("Add of collection " + c.getName() + " of type " + c.getType().toString());
 
 			// If there exists a collection with the same name, we simply add entries from this
 			// collection to the existing one!
 		Long collKey;
 		User creator = c.getCreator();
 		NR_Collection existingColl = getCollection(c.getType(), creator.getUid(), c.getName());
 		if (existingColl != null) {
 			collKey = existingColl.getKey();
 		}
 		else {
 			collKey = (Long)INSERT_COLLECTION.execute(new Object[]{c.getName(), c.getType().toString(), creator.getKey(), creator.getUid()});
 			c.setKey(collKey);
 		}
 
 		Long uKey = creator.getKey();
 
 		Object collParams[] = new Object[2];
 		collParams[0] = collKey;
 
 		Iterator entries = c._entries.iterator();
 
 		// Add all entries to the database
 		// @todo: Optimize this by inserting all entries in 1 shot!
 		switch (c.getType()) {
 			case SOURCE:
 				Object params[] = new Object[6];
 				params[0] = uKey;
 				while (entries.hasNext()) {
 					Source s = (Source)entries.next();
 					addSource(uKey, s);
 					collParams[1] = s.getKey();
 					INSERT_ENTRY_INTO_COLLECTION.execute(collParams);
 				}
 				break;
 
 			case CONCEPT:
 				while (entries.hasNext()) {
 					Concept cpt = (Concept)entries.next();
 					addConcept(uKey, cpt);
 					collParams[1] = cpt.getKey();
 					INSERT_ENTRY_INTO_COLLECTION.execute(collParams);
 				}
 				break;
 
 			case CATEGORY:
 				while (entries.hasNext()) {
 					Category cat = (Category)entries.next();
 					addCategory(uKey, cat);
 					collParams[1] = cat.getKey();
 					INSERT_ENTRY_INTO_COLLECTION.execute(collParams);
 				}
 				break;
 
 			default:
 				break;
 		}
 	}
 
 	/**
 	 * Gets a named collection for a user
 	 * @param type  Type of collection to be fetched
 	 * @param uid   User whose collection needs to be fetched 
 	 * @param name  Name of the collection
 	 */
 	public NR_Collection getCollection(NR_CollectionType type, String uid, String name)
 	{
 		return (NR_Collection)GET_COLLECTION.execute(new Object[]{uid, name, type.toString()});
 	}
 
 	public List<Source> getAllSourcesFromCollection(long collectionKey)
 	{
 		return (List<Source>)GET_ALL_SOURCES_FROM_USER_COLLECTION.get(collectionKey);
 	}
 
 	public Source getSourceFromCollection(long collKey, String userSrcTag)
 	{
 		return (Source)GET_SOURCE_FROM_USER_COLLECTION.execute(new Object[]{collKey, userSrcTag});
 	}
 
 	public List<Concept> getAllConceptsFromCollection(long collectionKey)
 	{
 		return (List<Concept>)GET_ALL_CONCEPTS_FROM_USER_COLLECTION.get(collectionKey);
 	}
 
 	public Concept getConceptFromCollection(long collKey, String cptName)
 	{
 		Tuple<Long, Concept> t = (Tuple<Long, Concept>)GET_CONCEPT_FROM_USER_COLLECTION.execute(new Object[]{collKey, cptName});
 		return t._b;
 	}
 
 	private void addConcept(Long userKey, Concept c)
 	{
 		if (_log.isDebugEnabled()) _log.debug("Add of concept " + userKey + ", " + c.getName() + ", " + c.getDefn());
 
 		Long cKey = c.getKey();
 		if (cKey == null) {
 			StringBuffer sb = new StringBuffer();
 			Iterator it = c.getKeywords();
 			while (it.hasNext()) {
 				sb.append(it.next()).append('\n');
 			}
 			c.setKey((Long)INSERT_CONCEPT.execute(new Object[] {userKey, c.getName(), c.getDefn(), sb.toString()}));
 		}
 	}
 
 	public List<Filter> getAllFiltersFromCollection(long collectionKey)
 	{
 		return (List<Filter>)GET_ALL_FILTERS_FROM_USER_COLLECTION.get(collectionKey);
 	}
 
 	public Filter getFilterFromCollection(long collKey, String filterName)
 	{
 		Tuple<Long, Filter> t = (Tuple<Long, Filter>)GET_FILTER_FROM_USER_COLLECTION.execute(new Object[]{collKey, filterName});
 		return t._b;
 	}
 
 	public List<Category> getAllCategoriesFromCollection(long collectionKey)
 	{
 		return (List<Category>)GET_ALL_CATEGORIES_FROM_USER_COLLECTION.get(collectionKey);
 	}
 
 	private void fetchChildren(Long collKey, Category parent)
 	{
 		List<Category> children = (List<Category>)GET_NESTED_CATS_FROM_USER_COLLECTION.execute(new Object[]{collKey, parent.getKey()});
 		if (!children.isEmpty()) {
 			for (Category c: children) {
 				fetchChildren(collKey, c);
 			}
 			parent.setChildren(children);
 		}
 	}
 
 	public Category getCategoryFromCollection(long collKey, String catName)
 	{
 			// ONLY top-level cats (with their children) can be fetched, i.e. those whose parents are -1
 		Tuple<Long, Category> t = (Tuple<Long, Category>)GET_CATEGORY_FROM_USER_COLLECTION.execute(new Object[]{collKey, catName, -1});
 		if (t == null) {
 			return null;
 		}
 		else {
 			Category c = t._b;
 			fetchChildren(collKey, c);
 			return c;
 		}
 	}
 
 	/**
 	 * This method uploads a file from the user's computer to the user's info space.
 	 * The file in the user's space will have the same base name as the file being
 	 * uploaded.  For example, if the user is uploading "../../myfiles/my.concepts.xml"
 	 * the uploaded file will have the name "my.concepts.xml".
 	 *
 	 * @param fname  The name of the file being uploaded.
 	 * @param is     The input stream from which contents of the uploaded file can be read.
 	 * @param u      The user who is uploading the file.
 	 */
 	public void uploadFile(String fname, InputStream is, User u) throws java.io.IOException
 	{
 		if (fname.indexOf(File.separator) != -1)
 			throw new java.io.IOException("Cannot have / in file name.  Access denied");
 
 		String localFileName = getFileUploadArea(u) + fname;
 		_log.info("Upload of file " + fname + " into " + localFileName);
 		IOUtils.copyStreamToLocalFile(is, localFileName);
 
       INSERT_USER_FILE.execute(new Object[] {u.getKey(), fname});
 	}
 
 	/**
 	 * This method adds a file to the user's info space
 	 *
 	 * @param is     The input stream from which the file should be uploaded. 
 	 * @param u      The user who is uploaded the file .
 	 */
 	public void       addFile(String fname, User u) throws java.io.IOException
 	{
 		if (fname.indexOf(File.separator) != -1)
 			throw new java.io.IOException("Cannot have / in file name.  Access denied");
 
 		String localFileName = getFileUploadArea(u) + fname;
 		_log.info("Add of file " + fname + " into " + localFileName);
       INSERT_USER_FILE.execute(new Object[] {u.getKey(), fname});
 	}
 
 	/**
 	 * This method provides a path in the local file system for a file
 	 * in the user's space.  Note that this ptth might be a temporary
 	 * path where the file has been made available.
 	 */
 	public String getRelativeFilePath(User u, String fname)
 	{
 		return GLOBAL_USERS_ROOTDIR + u.getUid() + File.separator + USER_INFO_DIR + fname;
 	}
 
 	/**
 	 * This method returns a byte stream for reading a file in a user's space
 	 *
 	 * @param u      The user whose user space is being accessed
 	 * @param fname  The file being read.
 	 * @return Returns a byte stream for reading the file
 	 */
 	public InputStream getInputStream(User u, String fname) throws java.io.IOException
 	{
 		if (fname.indexOf(File.separator) != -1)
 			throw new java.io.IOException("Cannot have / in file name.  Access denied");
 		return new FileInputStream(getFileUploadArea(u) + fname);
 	}
 
 	/**
 	 * This method returns a character reader for reading a file in a user's space
 	 *
 	 * @param u      The user whose user space is being accessed
 	 * @param fname  The file being read.
 	 * @return Returns a reader for reading the file
 	 */
 	public Reader getFileReader(User u, String fname) throws java.io.IOException
 	{
 		if (fname.indexOf(File.separator) != -1)
 			throw new java.io.IOException("Cannot have / in file name.  Access denied");
 		return IOUtils.getUTF8Reader(getFileUploadArea(u) + fname);
 	}
 
 	/**
 	 * This method returns an output stream for writing a file in a user's space
 	 *
 	 * @param u      The user whose user space is being accessed
 	 * @param fname  The file being written to.
 	 * @return Returns an output stream for writing the file
 	 */
 	public OutputStream getOutputStream(User u, String fname) throws java.io.IOException
 	{
 		if (fname.indexOf(File.separator) != -1)
 			throw new java.io.IOException("Cannot have / in file name.  Access denied");
 		return new FileOutputStream(getFileUploadArea(u) + fname);
 	}
 
 	/**
 	 * This method returns a byte stream for reading a file in some user's space
 	 * different from the user who is using the system.  Note that the download will
 	 * succeed only if the user requesting the file has appropriate access rights.
 	 *
 	 * @param reqUser The user who is requesting the file.
 	 * @param uid     The user whose file is being requested.
 	 * @param fname   Name of the file being requested.
 	 * @return        Returns a byte stream for reading the file, if the file
 	 *                exists and is accessible.  Else throws an IO exception
 	 */
 	public InputStream getInputStream(User reqUser, String uid, String fname) throws java.io.IOException
 	{
 		User u = (User)getUser(uid);
 		if (u == null) {
 			throw new java.io.IOException("User " + uid + " unknown!");
 		}
 		else if (!u.fileAccessible(reqUser, fname)) {
 			throw new java.io.IOException("File " + fname + " in " + uid + "'s space is not accessible.");
 		}
 		else if (fname.indexOf(File.separator) != -1) {
 			throw new java.io.IOException("Cannot have " + File.separator + " in file name.  Access denied!");
 		}
 		else {
 			return new FileInputStream(getFileUploadArea(u) + fname);
 		}
 	}
 
 	/**
 	 * This method returns a character reader for reading a file in some user's space
 	 * different from the user who is using the system.  Note that the download will
 	 * succeed only if the user requesting the file has appropriate access rights.
 	 *
 	 * @param reqUser The user who is requesting the file.
 	 * @param uid     The user whose file is being requested.
 	 * @param fname   Name of the file being requested.
 	 * @return        Returns a reader for reading the file, if the file
 	 *                exists and is accessible.  Else throws an IO exception
 	 */
 	public Reader getFileReader(User reqUser, String uid, String fname) throws java.io.IOException
 	{
 		User u = (User)getUser(uid);
 		if (u == null) {
 			throw new java.io.IOException("User " + uid + " unknown!");
 		}
 		else if (!u.fileAccessible(reqUser, fname)) {
 			throw new java.io.IOException("File " + fname + " in " + uid + "'s space is not accessible");
 		}
 		else if (fname.indexOf(File.separator) != -1) {
 			throw new java.io.IOException("Cannot have " + File.separator + " in file name.  Access denied!");
 		}
 		else {
 			return getFileReader(u, fname);
 		}
 	}
 
 	private Reader getReader(String pathPrefix, String pathSuffix, List<Long> feedKeys) throws java.io.IOException
 	{
 		for (Long f: feedKeys) {
 			String fullPath = pathPrefix + File.separator + getFeed(f).getTag() + File.separator + pathSuffix;
 			if ((new File(fullPath)).isFile())
 				return IOUtils.getUTF8Reader(fullPath);
 		}
 
 		return null;
 	}
 
 	/**
 	 * This method returns a character reader for displaying a news item
 	 * that has been archived in the local installation of News Rack.
 	 *
 	 * @param niPath  Path of the news item relative to the news archive.
 	 * @return Returns a reader object for reading the news item
 	 */
 	public Reader getNewsItemReader(NewsItem ni) throws java.io.IOException
 	{
 		if (ni.getKey() == null) {
 			_log.error("NewsItem with url " + ni.getURL() + " is not in the db!");
 			return null;
 		}
 
 			// Convert 12.11.2005 --> 2005/11/12/
 		String[] dateStr = ni.getDateString().split("\\.");
 		String pathPrefix = GLOBAL_NEWS_ARCHIVE_DIR + "filtered" + File.separator + dateStr[2] + File.separator + dateStr[1] + File.separator + dateStr[0];
 
 			// 1. FAST common case -- try with md5-hashed local name + feed passed in with the news item
 		String localName = ((SQL_NewsItem)ni).getLocalFileName();
 		String fullPath = pathPrefix + File.separator + ni.getFeed().getTag() + File.separator + localName;
 		if ((new File(fullPath)).isFile())
 			return IOUtils.getUTF8Reader(fullPath);
 
 			// 2. Didn't work .. check if this news item has been associated with other feeds
 		List<Long> allFeedKeys = (List<Long>)GET_ALL_FEEDS_FOR_NEWS.get(ni.getKey());
 		Reader r = getReader(pathPrefix, localName, allFeedKeys);
 
 			// 3. Check with local name stored in the db -- backward compatibility
 			// IMPORTANT: Check this *before* checking with old-style naming because
 			// there can be multiple news items with the same base file name!
 			// Ex: http://.../index.html --> map to "index.html", "ni1.index.html", etc.
 			//     Using base file name will return "index.html" for all these urls 
 			//     which would be incorrect!
 		if (r == null)
 			r = getReader(pathPrefix, (String)GET_NEWS_ITEM_LOCALNAME.get(ni.getKey()), allFeedKeys);
 
 			// 4. Check with old syle naming -- backward compatibility
 		if (r == null)
 			r = getReader(pathPrefix, StringUtils.getBaseFileName(ni.getURL()), allFeedKeys);
 
 		if (r != null)
 			return r;
 		else
 			throw new java.io.FileNotFoundException();
 	}
 
 	/**
 	 * This method deletes a file from the user's space
 	 *
 	 * @param u    User who has requested a file to be deleted
 	 * @param name The file to be deleted
 	 */
 	public void deleteFile(User u, String name)
 	{
       DELETE_USER_FILE.execute(new Object[] {u.getKey(), name});
 
 			// Move the file to the attic!
 		String fua   = getFileUploadArea(u);
 		String attic = fua + "attic" + File.separator;
 		IOUtils.createDir(attic);
 		File f1 = new File(fua + name);
 		File f2 = new File(attic + name);
 		f1.renameTo(f2);
 	}
 
 	/**
 	 * This method registers the user with the database .. in the process,
 	 * it might initialize user space.
 	 *
 	 * @param u   User who has to be registered
 	 */
 	public void registerUser(User u)
 	{
 		IOUtils.createDir(getUserHome(u));
 		IOUtils.createDir(getFileUploadArea(u));
 		IOUtils.createDir(getUserSpaceWorkDir(u));
 		u.setKey((Long)INSERT_USER.execute(new Object[]{u.getUid(), u.getPassword(), u.getName(), u.getEmail()}));
 	}
 
    public User getUser(final String uid)
    {
 		if (_log.isDebugEnabled()) _log.debug("Looking for user with uid: " + uid);
 
 		if (uid == null)
 			return null;
 
 		User u = (User)_cache.get("USER", uid);
 		if (u == null) {
 			u = (User)GET_USER_FROM_UID.execute(new Object[]{uid});
 			if (u != null)
 				_cache.add("USER", uid, u);
 		}
 
 		return u;
    }
 
    public List<User> getAllUsers()
    {
 		/* For now, we won't cache this info */
       return (List<User>)GET_ALL_USERS.execute(EMPTY_ARGS);
    }
 
    public List<Issue> getAllIssues()
    {
 		List<Issue> issues = new ArrayList<Issue>();
       List<Long> tkeys = (List<Long>)GET_ALL_ISSUE_KEYS.execute(EMPTY_ARGS);
 		for (Long k: tkeys)
 			issues.add(getIssue(k));
 		return issues;
    }
 
    public List<Issue> getAllValidatedIssues()
    {
 		List<Issue> issues = new ArrayList<Issue>();
       List<Long> tkeys = (List<Long>)GET_ALL_VALIDATED_ISSUE_KEYS.execute(EMPTY_ARGS);
 		for (Long k: tkeys)
 			issues.add(getIssue(k));
 		return issues;
    }
 
    public List<Feed> getAllActiveFeeds()
 	{
 		/* For now, we won't cache this info */
       return (List<Feed>)GET_ALL_ACTIVE_FEEDS.execute(EMPTY_ARGS);
 	}
 
    public List<Feed> getAllFeeds()
 	{
       return (List<Feed>)GET_ALL_FEEDS.execute(EMPTY_ARGS);
 	}
 
    public Issue getIssue(User u, String issueName)
    {
 		String key = u.getUid() + ":" + issueName;
 		Issue i = (Issue)_cache.get("ISSUE", key);
 		if (i == null) {
 			i = (Issue)GET_ISSUE_BY_USER_KEY.execute(new Object[]{u.getKey(), issueName});
 			if (i != null) {
 				_cache.add("ISSUE", u.getKey(), key, i);
 				_cache.add("ISSUE", u.getKey(), i.getKey(), i);
 				i.setUser(u);
 			}
 		}
 		return i;
    }
 
    public List<Issue> getIssues(User u)
    {
 		List<Issue> issues = (List<Issue>)GET_ALL_ISSUES_BY_USER_KEY.execute(new Object[]{u.getKey()});
 		for (Issue i: issues) {
 			i.setUser(u);
 			_cache.add("ISSUE", u.getKey(), u.getUid() + ":" + i.getName(), i);
 			_cache.add("ISSUE", u.getKey(), i.getKey(), i);
 		}
 		return issues;
    }
 
    public List<String> getFiles(User u)
    {
       return (List<String>)GET_ALL_FILES_BY_USER_KEY.execute(new Object[]{u.getKey()});
    }
 
    public List<PublicFile> getAllPublicUserFiles()
    {
       return (List<PublicFile>)GET_ALL_PUBLIC_FILES.execute(EMPTY_ARGS);
    }
 
 	public void invalidateUserProfile(User u)
 	{
 		// FIXME: Concurrency issues not thought through!
 		// What happens if a user is disabling it here, while concurrently
 		// some other user (sharing that account) is modifying and validating it?
 	
 		Long uKey = u.getKey();
 		Object[] params = new Object[]{uKey};
 
 			// Get rid of collections and entries
 		DELETE_ALL_COLLECTION_ENTRIES_FOR_USER.execute(params);
 		DELETE_ALL_COLLECTIONS_FOR_USER.execute(params);
 
 			// Invalidate all issues and categories
 			// Do not delete them!
 		UPDATE_TOPICS_VALID_STATUS_FOR_USER.execute(new Object[] {false, uKey});
 		UPDATE_CATS_FOR_USER.execute(new Object[] {false, uKey});
 
 			// Delete all sources, concepts, and filters -- they will be rebuilt!
 		DELETE_ALL_TOPIC_SOURCES_FOR_USER.execute(params);
 		DELETE_ALL_SOURCES_FOR_USER.execute(params);
 		DELETE_ALL_CONCEPTS_FOR_USER.execute(params);
 		DELETE_ALL_FILTER_TERMS_FOR_USER.execute(params);
 		DELETE_ALL_FILTERS_FOR_USER.execute(params);
 
 			// Clear the cache of u's entries
 		_cache.purgeCacheEntriesForUser(u);
 	}
 
 	/**
 	 * Gets the set of all sources monitored by the user across all topics 
 	 */
 	public Collection<Source> getMonitoredSourcesForAllTopics(User u)
 	{
 		return (Collection<Source>)GET_ALL_MONITORED_SOURCES_FOR_USER.execute(new Object[]{u.getKey()});
 	}
 
 	/**
 	 * Initialize the database for downloading news for a particular date
 	 * from a particular source.
 	 *
 	 * @param f        Feed from which news is being downloaded
 	 * @param pubDate  Date for which news is being downloaded
 	 */
 	public void initializeNewsDownload(Feed f, Date pubDate)
 	{
 			// Remove all entries for 'f' from the downloaded news table
 		// CLEAR_DOWNLOADED_NEWS_FOR_FEED.execute(new Object[]{f.getKey()});
 
 			// Create the output directories, if they don't exist
 		getArchiveDirForOrigArticles(f, pubDate);
 		getArchiveDirForFilteredArticles(f, pubDate);
 	}
 
 	/**
 	 * This method returns a NewsItem object for an article
 	 * that has already been downloaded
 	 *
 	 * @param url   URL of the article
 	 * @returns a news item object for the article
 	 */
 	public NewsItem getNewsItemFromURL(String url)
 	{
 		try {
 			NewsItem n = (NewsItem)_cache.get("NEWSITEM", url);
 			if (n == null) {
       		n = (NewsItem)GET_NEWS_ITEM_FROM_URL.execute(new Object[]{url});
 				//Tuple<String,String> t = splitURL(url);
       		//n = (NewsItem)GET_NEWS_ITEM_FROM_URL.execute(new Object[]{t._a, t._b});
 				if (n != null) {
 						// Add newsitem both by key & url
 					_cache.add("NEWSITEM", n.getKey(), n);
 					_cache.add("NEWSITEM", url, n);
 				}
 			}
 			return n;
 		}
 		catch (Exception e) {
 				// FIXME: Bad boy Subbu! ... But what to do ... if I declare that the stmt executor throws an
 				// exception, I have to add try-catch everywhere which will make everything a bloody mess ...
 				// So, I am using this workaround for now
 			if (!(e instanceof SQL_UniquenessConstraintViolationException)) {
 				_log.error("Exception while fetching news item", e);
 				return null;
 			}
 
 				// This should not happen at all!  But, present because:
 				// * need to have a backup against some bug ...
 				// * of duplicates introduced by the earlier codebase
 			_log.error("Aha! Duplicate news items found for url: " + url);
 			NewsItem n     = (NewsItem)((SQL_UniquenessConstraintViolationException)e).firstResult;
 			Long     nKey  = n.getKey();
 			Long     niKey = n.getNewsIndex().getKey();
 
 			List<Long> allItems = (List<Long>)GET_ALL_NEWS_ITEMS_WITH_URL.execute(new Object[]{url});
 //			Tuple<String,String> t = splitURL(url);
 //			List<Long> allItems = (List<Long>)GET_ALL_NEWS_ITEMS_WITH_URL.execute(new Object[]{t._a, t._b});
 			for (Long k: allItems) {
 				if (!k.equals(nKey)) {
 					_log.error(" ... Deleting duplicate news item with key: " + k);
 					DELETE_NEWS_ITEM.delete(k);
 					DELETE_URL_HASH_ENTRY.delete(k);
 
 						// Replace all occurences of 'k' with 'nKey' in news_collections & cat_news tables
 					UPDATE_SHARED_NEWS_ITEM_ENTRIES.execute(new Object[] {nKey, k});
 					UPDATE_CAT_NEWS.execute(new Object[] {nKey, k});
 				}
 			}
 
 			return n;
 		}
 	}
 
 	private Triple getLocalPathTerms(String path)
 	{
 			// Get fields from the path
 			// Take care of old-style path names -- backward compatibility code
 		path = path.replaceFirst("filtered/", "").replaceAll("//", "/").replaceAll("/", File.separator);
 		String[] flds = path.split(File.separator);
 		return new Triple(flds[0], flds[1], flds[2]);
 	}
 
 	/**
 	 * This method returns a NewsItem object for an article
 	 * that has already been downloaded
 	 *
 	 * @param path   Path of the article (where it is stored)
 	 * @returns a news item object for the article
 	 */
 	public NewsItem getNewsItemFromLocalCopyPath(String path)
 	{
 		if (path.indexOf("/") != -1) {
 				/* Request for OLD style local copy path names! 
 				 * Potentially 3 separate queries! ... but, this is a deprecated request 
 				 * and present only for backward compatibility.  Won't worry about performance */
 			Triple t = getLocalPathTerms(path);
 			String localName = (String)t._c;
 			String feedTag   = (String)t._b;
 			String dateStr   = (String)t._a;
 
 				// Since we removed date string from the DB and converted date-string to a mysql date time,
 				// convert the datestring 12.11.2005 to a mysql date 2005-11-12
 			String[] dateParts = dateStr.split("\\.");
          if (dateParts[1].length() == 1) dateParts[1] = '0' + dateParts[1];
          if (dateParts[0].length() == 1) dateParts[0] = '0' + dateParts[0];
 			dateStr = ((new StringBuffer(dateParts[2])).append('-').append(dateParts[1]).append('-').append(dateParts[0])).toString();
 
 			Feed   f         = getFeedWithTag(feedTag);
 			Long   niKey     = getNewsIndexKey(f.getKey(), dateStr);
 			return (NewsItem)GET_NEWS_ITEM_FROM_LOCALPATH.execute(new Object[] {dateStr, f.getKey(), localName, niKey});
 		}
 		else {
 				// Ex: 663f59096f4ab33251cde3cc303214c7:348545
 			int      i       = 1 + path.indexOf(':');
 			String   md5Hash = path.substring(0, i-1);
 			Long     nKey    = Long.parseLong(path.substring(i));
 			NewsItem ni      = getNewsItem(nKey);
 
 				// Bad path
 			if (ni == null || !StringUtils.md5(ni.getURL()).equals(md5Hash)) {
 				_log.error("BAD local path for news item: " + path);
 				if (ni != null)
 					_log.error("url: " + ni.getURL() + "; md5 - " + StringUtils.md5(ni.getURL()) + "; md5hash - " + md5Hash);
 				return null;
 			}
 			else {
 				return ni;
 			}
 		}
 	}
 
 	/**
 	 * This method returns a NewsItem object for an article, if it has already
 	 * been downloaded.  In an ideal world, only the URL is sufficient, but, in
 	 * some cases, providing more details guarantees greater success of finding
 	 * an existing object!  The other parameters have been provided for other 
 	 * DB backends which can retrieve news objects using that extra information.
 	 *
 	 * @param url      URL of the article
 	 * @param f        News feed object
 	 * @param d        Date of publishing
 	 * @param baseName Expected base name of the article in the archives
 	 * @returns a news item object for the article
 	 */
 	public NewsItem getNewsItem(String url, Feed f, Date d, String baseName)
 	{
 		// Try getting the news item with just the URL
 		return getNewsItemFromURL(url);
 	}
 
 	/**
 	 * This method creates a NewsItem object for the specified article.
 	 *
 	 * @param url      URL of the article
 	 * @param f        News source object
 	 * @param d        Date of publishing
 	 * @returns a news item object for the article
 	 */
 	public NewsItem createNewsItem(String url, Feed f, Date d)
 	{
 			// Create item
 		return new SQL_NewsItem(url, f.getKey(), d);
 	}
 
 	public SQL_NewsIndex getNewsIndex(Long niKey)
 	{
 		// _log.info("Looking for news index with key: " + niKey);
 
 		SQL_NewsIndex ni = (SQL_NewsIndex)_cache.get("SQL_NEWS_INDEX", niKey);
 		if (ni == null) {
 			ni = (SQL_NewsIndex)GET_NEWS_INDEX.get(niKey);
 			if (ni != null)
 				_cache.add("SQL_NEWS_INDEX", niKey, ni);
 		}
 		return ni;
 	}
 
 	private Long getNewsIndexKey(Long feedKey, String dateStr)
 	{
 		// Since we removed date string from the DB and converted date-string to a mysql date time,
 		// convert the datestring 12.11.2005 to a mysql date 2005-11-12
 		// String[] dateParts = dateStr.split("\\.");
 		// dateStr = ((new StringBuffer(dateParts[2])).append('-').append(dateParts[1]).append('-').append(dateParts[0])).toString();
 
 		String cacheKey = "NIKEY:" + feedKey + ":" + dateStr;
 		Long niKey = (Long)_cache.get("SQL_NEWS_INDEX", cacheKey);
 		if (niKey == null) {
       	niKey = (Long)GET_NEWS_INDEX_KEY.execute(new Object[] {feedKey, dateStr});
 			if (niKey != null)
 				_cache.add("SQL_NEWS_INDEX", cacheKey, niKey);
 		}
 		return niKey;
 	}
 
 	void recordDownloadedNewsItem(Long feedKey, NewsItem ni)
 	{
 		SQL_NewsItem sni = (SQL_NewsItem)ni;
 
 			// Nothing will change in this case!
 		if (feedKey.equals(sni.getFeedKey()) && sni.inTheDB())
          return;
 
 		String dateStr = sni.getDateString();
 
 				// Since we removed date string from the DB and converted date-string to a mysql date time,
 				// convert the datestring 12.11.2005 to a mysql date 2005-11-12
 		String[] dateParts = dateStr.split("\\.");
 		dateStr = ((new StringBuffer(dateParts[2])).append('-').append(dateParts[1]).append('-').append(dateParts[0])).toString();
 
          /* IMPORTANT: Use feed id from the source and not from
 			 * the news item because we are adding the news item to
 			 * the index of the source.  If the news item has been
 			 * downloaded previously while processing another source,
           * then n.getFeedKey() will be different from feedKey! */
 		Long niKey = getNewsIndexKey(feedKey, dateStr);
 		if ((niKey == null) || (niKey == -1)) {
 				// Add a new news index entry to the news index table
          niKey = (Long)INSERT_NEWS_INDEX.execute(new Object[] {feedKey, new java.sql.Date(sni.getDate().getTime())});
          if ((niKey == null) || (niKey == -1)) {
             _log.error("Got an invalid key creating a news index entry");
             return;
          }
 		}
 
 			// Add it to the db, if necessary
 		if (!sni.inTheDB()) {
 				// Synchronize over an unique String object of the url so that 2 threads operating on the same url
 				// get serialized, but, other threads can go ahead without a problemo!
 			String u = sni.getURL().intern();
 			synchronized(u) {
 				SQL_NewsItem x = (SQL_NewsItem)getNewsItemFromURL(u);
 				if (x == null) {
 					Long key = (Long)INSERT_NEWS_ITEM.execute(new Object[] {niKey, sni._urlRoot, sni._urlTail, sni._title, sni._description, sni._author});
 					INSERT_URL_HASH.execute(new Object[] {key, u}); // Add a url hash too -- should I start using triggers?? 
 					sni.setKey(key);
 					sni.setNewsIndexKey(niKey); // Record the news index that the news item belongs to!
 				}
 				else {
 						// Some other thread has beat me to it!  So, don't add the news item to the db
 						// Copy over the attributes of that item ...
 					sni.copy(x);
 					_log.info("Aha! Prevented a duplicate news object problem for url: " + u);
 				}
 			}
 		}
 		else if (_log.isDebugEnabled()) {
 			_log.debug("news item: " + sni._title + ": already in the DB " + sni.getKey() + " in index " + sni._newsIndexKey);
 		}
 
 			// Record the news-item <--> news_index association
 			//
 			// NOTE: The insert statement will fail if the insert will violate uniqueness of (n_key, ni_key)
 			// but, because it is an INSERT IGNORE, we get the desired effect!
       INSERT_INTO_NEWS_COLLECTION.execute(new Object[] {sni.getKey(), niKey, feedKey});
 
 			// Add this news item to the list of recently downloaded news items
 		INSERT_INTO_RECENT_DOWNLOAD_TABLE.execute(new Object[] {feedKey, sni.getKey()});
 	}
 
 	/**
 	 * Record a downloaded news item
 	 *
 	 * @param f    News feed from where the news item was downloaded 
 	 * @param ni   The downloaded news item
 	 *
 	 * NOTE: ni is not guaranteed to be a news item that has been
 	 * newly downloaded ... it could as well be a news item that has
 	 * been obtained by querying the database.
 	 */
 	public void recordDownloadedNewsItem(Feed f, NewsItem ni)
 	{
 		recordDownloadedNewsItem(f.getKey(), ni);
 	}
 
 	/**
 	 * Gets the list of downloaded news items for a feed in the most recent download phase
 	 */
 	public Collection<NewsItem> getDownloadedNews(Feed f)
 	{
 		String     cacheKey = "DN" + ":" + f.getKey();
 		List<Long> keys     = (List<Long>)_cache.get("FEED", cacheKey);
 		if (keys == null) {
 			_log.info("CACHE MISS for " + cacheKey);
 			keys = (List<Long>)GET_DOWNLOADED_NEWS_KEYS_FOR_FEED.get(f.getKey());
 			_cache.add("FEED", new String[] {"DN_NEWS"}, cacheKey, keys);
 		}
 
 		List<NewsItem> news = new ArrayList<NewsItem>();
 		for (Long k: keys)
 			news.add(getNewsItem(k));
 
 		return news;
 	}
 
 	/**
 	 * Perform any necessary clean up after news download is finished
 	 * from a particular source
 	 *
 	 * @param f  Feed from which news was downloaded
 	 */
 	public void finalizeNewsDownload(Feed f)
 	{
 			// Nothing else to do!
 		if (NewsRack.inDebugMode())
 			printStats();
 	}
 
 	private Long persistRuleTerm(Long uKey, Long filtKey, RuleTerm r)
 	{
 		if (_log.isDebugEnabled()) _log.debug("Add of rule term " + r + " for filter: " + filtKey);
 
 		long   key;
 		Object op1    = r.getOperand1();
 		Object op2    = r.getOperand2();
 		Long   op1Key = null;
 		Long   op2Key = null;
 		switch (r.getType()) {
 			case LEAF_CONCEPT:
 				Concept c = (Concept)op1;
 				op1Key = c.getKey();
 				if (op1Key == null) {
 					Long collKey = (Long)GET_COLLECTION_KEY.execute(new Object[]{uKey, c.getCollection().getName(), NR_CollectionType.CONCEPT.toString()});
 					op1Key  = (Long)GET_CONCEPT_KEY_FROM_USER_COLLECTION.execute(new Object[]{collKey, c.getName()});
 					if (op1Key == null) {
 						_log.error("ERROR! Unpersisted concept: " + op1);
 							// Trigger a null pointer exception!  Workaround to avoid declaring a throws clause everywhere
 							// if I use a throw here...  I know ... BAD SUBBU
 						_log.error("Dummy: " + op1Key.longValue());
 					}
 				}
 				op2Key = new Long(((Filter.LeafConcept)r).getMinOccurences());
 				break;
 
 			case LEAF_CAT:
 				op1Key = ((Category)op1).getKey();
 				if (op1Key == null) {
 					_log.error("ERROR! Unpersisted category: " + op1);
 						// Trigger a null pointer exception!  Workaround to avoid declaring a throws clause everywhere
 						// if I use a throw here...  I know ... BAD SUBBU
 					_log.error("Dummy: " + op1Key.longValue());
 				}
 				break;
 
 			case LEAF_FILTER:
 				op1Key = ((Filter)op1).getKey();
 				if (op1Key < 0) {
 					_log.error("ERROR! Unpersisted filter: " + op1);
 						// Trigger a null pointer exception!  Workaround to avoid declaring a throws clause everywhere
 						// if I use a throw here...  I know ... BAD SUBBU
 					op1Key = null;
 					_log.error("Dummy: " + op1Key.longValue());
 				}
 				break;
 
 			case NOT_TERM:
 				op1Key = persistRuleTerm(uKey, filtKey, (RuleTerm)op1);
 				break;
 
 			case CONTEXT_TERM:
 				op1Key = persistRuleTerm(uKey, filtKey, (RuleTerm)op1);
 				break;
 
 			case AND_TERM:
 			case OR_TERM:
 				op1Key = persistRuleTerm(uKey, filtKey, (RuleTerm)op1);
 				op2Key = persistRuleTerm(uKey, filtKey, (RuleTerm)op2);
 				break;
 		}
 
 		Long retVal = (Long)INSERT_RULE_TERM.execute(new Object[] {filtKey, Filter.getValue(r.getType()), op1Key, op2Key});
 			// For context terms, the list of concepts are treated specially
 			// They are stored with a term type value -1 and with a key <category-key, rule-term-key>
 		if (r.getType() == CONTEXT_TERM) {
 			List<Concept> cpts = (List<Concept>)op2;
 			for (Concept cpt: cpts) {
 				INSERT_RULE_TERM.execute(new Object[] {filtKey, -1, retVal, cpt.getKey()});
 			}
 		}
 		return retVal;
 	}
 
 	/**
 	 * Add a category to the database
 	 *
 	 * @param uKey user that defined this category
 	 * @param cat Category that needs to be added
 	 */
 	public void addCategory(Long uKey, Category cat)
 	{
 		if (_log.isDebugEnabled()) _log.debug("Add of category " + cat.getName());
 
 			// Persist the filter first
 		Filter f    = cat.getFilter();
 		Long   fKey = (f == null) ? Long.valueOf(-1) : f.getKey();
 		if ((f != null) && ((fKey == null) || (fKey == -1))) {
 			if (_log.isDebugEnabled()) _log.debug("Add of filter " + f.getName() + " with rule" + f.getRuleString());
 			fKey = (Long)INSERT_FILTER.execute(new Object[] {uKey, f.getName(), f.getRuleString() });
 			f.setKey(fKey);
 			Long rKey = persistRuleTerm(uKey, fKey, f.getRule());
 			UPDATE_FILTER.execute(new Object[] {rKey, fKey});
 		}
 
 		Long parent = (cat.getParent() == null) ? -1 : cat.getParent().getKey();
 		Long cKey   = cat.getKey();
 			// 1. check if the category already exists
 			//    if it doesn't exist, add it!
 			//    if it exists, update info
 		if ((cKey == null) || (cKey == -1)) {
 			Long   tKey = (cat.getIssue() == null)  ? null : cat.getIssue().getKey();
 			Triple info = (tKey == null) ? null : (Triple)GET_CAT_INFO.execute(new Object[]{tKey, cat.getCatId()});
 			if (info == null) {
 					// Now, persist the category
 				cKey = (Long)INSERT_CAT.execute(new Object[] {cat.getName(), uKey, tKey, cat.getCatId(), parent, fKey, cat.getTaxonomyPath()});
 				cat.setKey(cKey);
 			}
 			else {
 				cKey = (Long)info._a;
 				cat.setKey(cKey);
 				cat.setNumArticles((Integer)info._b);
 				cat.setLastUpdateTime((Date)info._c);
          	UPDATE_CAT.execute(new Object[] {true, fKey, cat.getName(), cat.getCatId(), parent, cat.getTaxonomyPath(), cKey});
 			}
 		}
 		else {
 				// Change status to valid
          UPDATE_CAT.execute(new Object[] {true, fKey, cat.getName(), cat.getCatId(), parent, cat.getTaxonomyPath(), cKey});
 		}
 
 			// Process nested categories
 		for (Category c: cat.getChildren())
 			addCategory(uKey, c);
 	}
 
 	/**
 	 * This method updates the database with changes made to an issue
 	 * @param i Issue that needs to be updated
 	 */
 	public void updateTopic(Issue i)
 	{ 
 		UPDATE_TOPIC_INFO.execute(new Object[]{i.isValidated(), i.isFrozen(), i.isPrivate(), i.getKey()});
 	}
 
 	/**
 	 * Create storage in the database for recording news for an issue
 	 * @param i   Issue for which space needs to be allocated
 	 */
 	public void addIssue(Issue i)
 	{
 		Long iKey = i.getKey();
 
 			// 1. check if the issue already exists
 			//    if it doesn't exist, add it!
 			//    if it exists, update info
 		if ((iKey == null) || (iKey == -1)) {
 			Triple info = (Triple)GET_ISSUE_INFO.execute(new Object[]{i.getName(), i.getUser().getKey()});
 			if (info == null) {
 				iKey = (Long)INSERT_TOPIC.execute(new Object[] {i.getUser().getKey(), i.getName(), i.isValidated(), i.isFrozen(), i.isPrivate(), i.getTaxonomyPath()});
 				i.setKey(iKey);
 			}
 			else {
 				iKey = (Long)info._a;
 				i.setKey(iKey);
 				i.setNumArticles((Integer)info._b);
 				i.setLastUpdateTime((Date)info._c);
 
 					// Update flags of the new object based on what is in the db!
 				Issue existingI = getIssue(iKey);
 				i.setFreezeFlag(existingI.isFrozen());
 				i.setPrivateFlag(existingI.isPrivate());
 
 				updateTopic(i);
 			}
 		}
 		else {
 			updateTopic(i);
 		}
 
 		Long uKey = i.getUserKey();
 
 			// 2. Add monitored sources
 			//
 			// FIXME: Warn the user that when he/she is picking
 			// multiple sources that reference the same feed?
 		for (Source s: i.getMonitoredSources()) {
 			if (s.getFeed() == null)
 				_log.error("ERROR: No feed for source with tag: " + s.getTag());
 			Long sKey = s.getKey();
 			Long fKey = s.getFeed().getKey();
 			if (sKey == null)
 				sKey = getSourceKey(uKey, fKey, s.getTag());
 			INSERT_TOPIC_SOURCE.execute(new Object[] {iKey, sKey, fKey});
 		}
 
 			// 3. Add categories
 		for (Category c: i.getCategories())
 			addCategory(uKey, c);
 
 			// 4. Assign and save nested-set ids for all cats
 		int next = 1;
 		for (Category c: i.getCategories())
 			next = 1 + getNextNestedSetId(next, c);
 	}
 
 	private int getNextNestedSetId(int nsId, Category cat)
 	{
 			// Do a pre-order traversal of the category tree and assign left-right nested-set ids for all categories
 			// Lookup and read about nested sets if you don't know what they are -- quite simple and elegant solution
 			// for storing hierarchical objects in a database while supporting efficient operations on the hierarchy.
 		int next = nsId+1;
 		for (Category cc: cat.getChildren())
 			next = getNextNestedSetId(next, cc) + 1;
 
 			// Save to db!
 		Long cKey = cat.getKey();
 		SQL_StmtExecutor.update("UPDATE categories SET lft = ?, rgt = ? WHERE cat_key = ?",
 										new SQL_ValType[] {SQL_ValType.INT, SQL_ValType.INT, SQL_ValType.LONG},
 										new Object[] {nsId, next, cKey});
 
 		return next;
 	}
 
 	/**
 	 * Remove a category from the database.
 	 * @param c Category that needs to be removed
 	 */
 	public void removeCategory(Category c)
 	{
 		if (c.isLeafCategory()) {
 			Filter f = c.getFilter();
 				// Delete filter terms first and delete filter next
 			DELETE_FILTER_TERMS.execute(new Object[]{f.getKey()});
 			DELETE_FILTER.execute(new Object[]{f.getKey()});
 		}
 		else {
 			for (Category ch: c.getChildren())
 				removeCategory(ch);
 		}
 			// Lastly, delete category after processing filter/nested-cats
 		DELETE_CATEGORY.execute(new Object[]{c.getKey()});
 	}
 
 	/**
 	 * Remove the issue from the database.
 	 * @param i   Issue that needs to be removed.
 	 */
 	public void removeIssue(Issue i)
 	{
 			// Remove categories
 		for (Category c: i.getCategories())
 			removeCategory(c);
 		
 			// Remove sources
 		DELETE_FROM_TOPIC_SOURCE_TABLE.execute(new Object[]{i.getKey()});
 	}
 
 	/**
 	 * Record a classified news item!
 	 * @param ni   News Item that has been classified in category c
 	 * @param c    Category into which ni has been classified
 	 * @param matchCount  Match weight
 	 */
 	public void addNewsItem(NewsItem ni, Category cat, int matchCount)
 	{
 		if (cat.isLeafCategory()) {
 			// NOTE: To be strictly correct, I need to purge news from this cat, my ancestors & children too .
 			// But, no need to be an eager beaver about this since the info will be stale only for a short time.
 			// When the news is committed, all staleness will disappear!
 			//
 			// purgeCacheNewsEntriesForCat(cat, false, false);
 
 				// Add the news into the news index
 			SQL_NewsItem sni = (SQL_NewsItem)ni;
 			if (!sni.inTheDB())
 				_log.error("News item " + sni + " not in the db yet!");
 			SQL_NewsIndex idx = sni.getNewsIndex();
          INSERT_INTO_CAT_NEWS_TABLE.execute(new Object[] {cat.getKey(), sni.getKey(), idx.getKey(), idx.getCreationTime()});
 
 				// Increment # of unique articles in the category
 			cat.setNumArticles(1+cat.getNumArticles());
 
 				// Do not commit anything to the database yet!
 				// It is done in one pass after the download phase is complete!
 				// Record cat in a table
 			List<Category> l = (List<Category>)_leafCatsToCommit.get(cat.getIssue().getKey());
 			if (l == null) {
 				l = new ArrayList<Category>();
 				_leafCatsToCommit.put(cat.getIssue().getKey(), l);
 			}
 			l.add(cat);
 
 				// Don't commit right away -- because this will lead to a spate of cache purges.
 				// It is okay to have stale info for a short time.
 				//
 				// FIXME: Relies on the fact that the category objects are live
 				// through the entire news classification phase
 			//commitCatToDB(cat, false);
 		}
 	}
 
 	/**
 	 * returns true if a news item has been processed for an issue -- to avoid
 	 * reprocessing the same news item over and over
 	 *
 	 * NOTE: This method and the updateMaxNewsIdForIssue method assume that
 	 * ids of news items increase monotonically as new items are downloaded. 
 	 */
 	public boolean newsItemHasBeenProcessedForIssue(NewsItem ni, Issue i)
 	{
 		String  cacheKey = "IFINFO:" + i.getKey();
 		HashMap maxNiKeyMap = (HashMap)_cache.get("ISSUE", cacheKey);
 		if (maxNiKeyMap == null) {
 			maxNiKeyMap = new HashMap();
 			_cache.add("ISSUE", new String[]{i.getUserKey().toString(), cacheKey}, cacheKey, maxNiKeyMap);
 		}
 		Long maxNiKey = (Long)maxNiKeyMap.get(ni.getFeedKey());
 		if (maxNiKey == null) {
 				// 1. There can be multiple entries for a feed because a user might have
 				//    specified the same feed multiple times (inadvertently) using different
 				//    names for the feeds
 				// 2. We might get no results in the case where
 				//    (a) 2 feeds F1 and F2 contain the same news item N (same url) 
 				//    (b) news item N is downloaded in the context of F1
 				//    (c) the current topic monitors feed F2, but not F1
 				//    (d) so, when we try to fetch a topic-source-row for n1.feed, we try to
 				//        fetch it for F1, and we don't get any hits
 				//    We will not try to fix this problem ... and conservatively indicate that
 				//    the news item has not been processed.
 			List<Long> niKeys = (List<Long>)GET_TOPIC_SOURCE_ROW.execute(new Object[]{i.getKey(), ni.getFeedKey()});
 			if (niKeys.isEmpty())
 				return false;
 
 			maxNiKey = niKeys.get(0);
 			maxNiKeyMap.put(ni.getFeedKey(), maxNiKey);
 		}
 
 		return ((maxNiKey != null) && (maxNiKey > ni.getKey()));
 	}
 
 	/**
 	 * updates the max id of the news item that has been processed for an issue.
 	 *
 	 * NOTE: This method and the newsItemHasBeenProcessedForIssue method assume that
 	 * ids of news items increase monotonically as new items are downloaded. 
 	 */
 	public void updateMaxNewsIdForIssue(Issue i, Feed f, Long maxId)
 	{
 		String cacheKey = "IFINFO:" + i.getKey();
 		HashMap maxNiKeyMap = (HashMap)_cache.get("ISSUE", cacheKey);
 		if (maxNiKeyMap == null) {
 			maxNiKeyMap = new HashMap();
 			_cache.add("ISSUE", new String[]{i.getUserKey().toString(), cacheKey}, cacheKey, maxNiKeyMap);
 		}
 
 			// Add an updated value to the cache so that we don't have to the hit the db next time
 		maxNiKeyMap.put(f.getKey(), maxId);
 
 			// Update the db
 		UPDATE_TOPIC_SOURCE_INFO.execute(new Object[]{maxId, i.getKey(), f.getKey()});
 	}
 
 	public void resetMaxNewsIdForIssue(Issue i)
 	{
 		if (i.getKey() != null) {
 			RESET_ALL_TOPIC_SOURCES.execute(new Object[]{i.getKey()});
 			_cache.removeEntriesForGroups(new String[]{"IFINFO:" + i.getKey()});
 		}
 	}
 
 	private void purgeCacheNewsEntriesForCat(Category c, boolean processAncestors, boolean processChildren)
 	{
 		_cache.removeEntriesForGroups(new String[]{"CATNEWS:" + c.getKey()});
 
 		if (processChildren) {
 				// No need to process ancestors for my children, because they get processed in the recursive call ..
 			for (Category cc: c.getChildren())
 				purgeCacheNewsEntriesForCat(cc, false, true);
 		}
 		if (processAncestors && (c.getParent() != null)) {
 				// No need to process children for my ancestors, because they get processed in this recursive call ...
 			purgeCacheNewsEntriesForCat(c.getParent(), true, false);
 		}
 	}
 
 	private void purgeCacheEntriesForCat(Category c, boolean processAncestors, boolean processChildren)
 	{
 			// Remove cache entries for this category
 		_cache.remove("CATEGORY", c.getKey());
 
 			// If c is a non-leaf cat, if requested, purge cache entries for children
 			// This is required, for example, when we delete news items from a non-leaf category
 		if (processChildren) {
 				// No need to process ancestors for my children, because they get processed in the recursive call ..
 			for (Category cc: c.getChildren())
 				purgeCacheEntriesForCat(cc, false, true);
 		}
 
 			// Ancestors have links to their children
 		if (processAncestors && (c.getParent() != null)) {
 				// No need to process children for my ancestors, because they get processed in this recursive call ...
 			purgeCacheEntriesForCat(c.getParent(), true, false);
 		}
 	}
 
 	private void purgeIssueAndUserEntries(Issue i)
 	{
 		User u = i.getUser();
 		_cache.remove("ISSUE", i.getKey());
 		_cache.remove("ISSUE", u.getUid() + ":" + i.getName());
 		_cache.remove("USER", u.getKey());
 		_cache.remove("USER", u.getUid());
 	}
 
 	/**
 	 * Remove a classified news item from a category
 	 * @param catKey   Category key (globally unique)
 	 * @param nKey    NewsItem key (globally unique)
 	 */
 	public void deleteNewsItemFromCategory(Long catKey, Long nKey)
    {
 		Category cat = getCategory(catKey);
 
 		if (cat.isLeafCategory()) {
 			Integer numDeleted = (Integer)DELETE_NEWS_FROM_CAT.execute(new Object[] {catKey, nKey});
 			cat.setNumArticles(cat.getNumArticles() - numDeleted);
 			commitCatToDB(cat, true);
 		}
 		else {
 				// Get keys of all leaf categories that are nested within the requested cat
 			List<Long> cKeys = getKeysForAllNestedLeafCats(cat);
 
 				// Add conditions to fetch news from all the nested leaf cats
 			StringBuffer queryBuf = new StringBuffer("DELETE FROM cat_news WHERE n_key = ? AND c_key IN (");
 			Iterator<Long> it = cKeys.iterator();
 			while (it.hasNext()) {
 				queryBuf.append(it.next());
 				if (it.hasNext())
 					queryBuf.append(",");
 			}
 			queryBuf.append(")");
 			SQL_StmtExecutor.delete(queryBuf.toString(), new SQL_ValType[] {LONG}, new Object[]{nKey});
 
 				// Update counts
 			UPDATE_ART_COUNTS_FOR_ALL_TOPIC_LEAF_CATS.execute(new Object[]{cat.getIssue().getKey()});
 
 				// Purge cache of stale category, issue, and user objects
 			purgeCacheEntriesForCat(cat, true, true);
 			purgeIssueAndUserEntries(cat.getIssue());
 		}
 
 			// Purge cached news
 		purgeCacheNewsEntriesForCat(cat, true, true);
    }
 
 	/**
 	 * Remove a classified news item from a category
 	 * @param catKey  Category key (globally unique)
 	 * @param nKeys   NewsItem keys (globally unique)
 	 */
 	public void deleteNewsItemsFromCategory(Long catKey, List<Long> nKeys)
    {
 		Category cat = getCategory(catKey);
 
 			// Initialize
 		StringBuffer queryBuf = new StringBuffer("DELETE FROM cat_news WHERE");
 
 			// Set up conditions for categories
 		if (cat.isLeafCategory()) {
 			queryBuf.append(" c_key = ").append(catKey);
 		}
 		else {
 				// Get keys of all leaf categories that are nested within the requested cat
 			List<Long> cKeys = getKeysForAllNestedLeafCats(cat);
 
 				// Add conditions to fetch news from all the nested leaf cats
 			queryBuf.append(" c_key IN (");
 			Iterator<Long> it = cKeys.iterator();
 			while (it.hasNext()) {
 				queryBuf.append(it.next());
 				if (it.hasNext())
 					queryBuf.append(",");
 			}
 			queryBuf.append(")");
 		}
 
 			// Set up conditions for news items
 		queryBuf.append(" AND n_key IN (");
 		Iterator<Long> it = nKeys.iterator();
 		while (it.hasNext()) {
 			queryBuf.append(it.next());
 			if (it.hasNext())
 				queryBuf.append(",");
 		}
 		queryBuf.append(")");
 
 			// Execute the delete statement
 		Integer numDeleted = (Integer)SQL_StmtExecutor.delete(queryBuf.toString(), new SQL_ValType[] {}, new Object[]{});
 
 			// Purge cached news
 		purgeCacheNewsEntriesForCat(cat, true, true);
 		if (cat.isLeafCategory()) {
 			cat.setNumArticles(cat.getNumArticles() - numDeleted);
 			commitCatToDB(cat, true);
 		}
 		else {
 			// It is not possible to update article counts without hitting the db because
 			// we don't know which categories were updated ...
 
 				// Update counts
 			UPDATE_ART_COUNTS_FOR_ALL_TOPIC_LEAF_CATS.execute(new Object[]{cat.getIssue().getKey()});
 
 				// Purge cache of stale category, issue, and user objects
 			purgeCacheEntriesForCat(cat, true, true);
 			purgeIssueAndUserEntries(cat.getIssue());
 		}
    }
 
 	/**
 	 * Checks if a category contains a news item
 	 * @param c    Category which needs to be checked
 	 * @param ni   News Item that needs to be checked
 	 * @return true if category c contains the news item ni
 	 */
 	public boolean newsItemPresentInCategory(Category cat, NewsItem ni)
 	{
 		SQL_NewsItem sni = (SQL_NewsItem)ni;
       Object v = CAT_NEWSITEM_PRESENT.execute(new Object[] {cat.getKey(), sni.getKey(), sni.getNewsIndex().getKey()});
 		return (v != null);
 	}
 
 	private List<Long> getKeysForAllNestedLeafCats(Category cat)
 	{
 		String query = "SELECT c2.cat_key FROM categories c, categories c2 " +
 		               "WHERE c.cat_key = ? AND c2.t_key = c.t_key AND c2.lft > c.lft AND c2.rgt < c.rgt AND c2.rgt = c2.lft + 1"; 
 		return (List<Long>)SQL_StmtExecutor.execute(query,
 																  SQL_StmtType.QUERY,
 																  new Object[] {cat.getKey()},
 																  new SQL_ValType[] {LONG},
 																  null,
 																  new GetLongResultProcessor(),
 																  false);
 	}
 
 	/**
 	 * Gets list of articles classified in a category -- starting at a specified index
 	 */
 	public List<NewsItem> getNews(Category cat, Date start, Date end, Source src, int startId, int numArts)
 	{
 			// Caching non-datestamp requests right now 
 		Long   catKey   = cat.getKey();
 		String cacheKey = (start == null) ? "CATNEWS:" + catKey + (src == null ? "" : ":" + src.getKey()) + ":" + startId + ":" + numArts : null;
 		Object keys     = (start == null) ? (List)_cache.get("LIST", cacheKey) : null;
 
 			// Cache miss
 		if (keys == null) {
 				// Initialize ...
 			StringBuffer      queryBuf    = new StringBuffer();
 			List              argList     = new ArrayList();
 			List<SQL_ValType> argTypeList = new ArrayList<SQL_ValType>();
 
 				// Init query
 			queryBuf.append("SELECT");
 			queryBuf.append(cat.isLeafCategory() ? " c.n_key" : " DISTINCT(c.n_key)");
 			queryBuf.append(" FROM cat_news c");
 
 				// Add conditions for feed-specific news
 			if (src != null) {
 				queryBuf.append(" JOIN news_collections nc ON nc.n_key = c.n_key AND nc.feed_key = ?");
 				argList.add(src.getFeed().getKey());
 				argTypeList.add(SQL_ValType.LONG);
 			}
 
 				// Add category-specific conditions
 			if (cat.isLeafCategory()) {
 				queryBuf.append(" WHERE c.c_key = ?");
 				argList.add(catKey);
 				argTypeList.add(SQL_ValType.LONG);
 			}
 			else {
 					// Get keys of all leaf categories that are nested within the requested cat
 				List<Long> cKeys = getKeysForAllNestedLeafCats(cat);
 
 					// Add conditions to fetch news from all the nested leaf cats
 				queryBuf.append(" WHERE c.c_key IN (");
 				Iterator<Long> it = cKeys.iterator();
 				while (it.hasNext()) {
 					queryBuf.append(it.next());
 					if (it.hasNext())
 						queryBuf.append(",");
 				}
 				queryBuf.append(")");
 			}
 
 				// Add conditions for date-limited news
 			if (start != null) {
 				queryBuf.append(" AND date_stamp >= ? AND date_stamp <= ?");
 				argList.add(new java.sql.Date(start.getTime()));
 				argList.add(new java.sql.Date(end.getTime()));
 				argTypeList.add(SQL_ValType.DATE);
 				argTypeList.add(SQL_ValType.DATE);
 			}
 
 				// Add sorting and limiting constraints
 			queryBuf.append(" ORDER by date_stamp DESC, n_key DESC LIMIT ?, ?");
 			argList.add(startId);
 			argList.add(numArts);
 			argTypeList.add(SQL_ValType.INT);
 			argTypeList.add(SQL_ValType.INT);
 
 				// Have to do this nonsense because generic type info and type parameter info is lost at runtime ... 
 			Object[] tmp = argTypeList.toArray();
 			SQL_ValType[] argTypes = new SQL_ValType[tmp.length];
 			int i = 0;
 			for (Object v: tmp) {
 				argTypes[i] = (SQL_ValType)v;
 				i++;
 			}
 
 			//_log.info("Executing: " + queryBuf.toString() + " with start value " + startId);
 
 				// Run the query and fetch news!
 			keys = SQL_StmtExecutor.execute(queryBuf.toString(), SQL_StmtType.QUERY, argList.toArray(), argTypes, null, new GetLongResultProcessor(), false);
 
 				// Caching non-datestamp requests right now 
 			if (start == null)
 				_cache.add("LIST", new String[]{cat.getUser().getKey().toString(), "CATNEWS:" + cat.getKey()}, cacheKey, keys);
 		}
 
 			// Set up the list of news items
 		List<NewsItem> news = new ArrayList<NewsItem>();
 		for (Long k: (List<Long>)keys)
 			news.add(getNewsItem(k));
 
 		return news;
 	}
 
 	public List<NewsItem> getNews(Category c, int startIndex, int numArts)
 	{
 		return getNews(c, null, null, null, startIndex, numArts);
 	}
 
 	/**
 	 * Gets list of articles classified in a category
 	 * @param cat     Category for which news is being sought
 	 * @param numArts Number of articles requested
 	 */
 	public List<NewsItem> getNews(Category cat, int numArts)
 	{
 		return getNews(cat, null, null, null, 0, numArts);
 	}
 
 	public List<NewsItem> getNews(Issue i, Date start, Date end, Source src, int startId, int numArts)
 	{
 		List<NewsItem> news = new ArrayList<NewsItem>();
 		List<Long> keys;
 		if (start == null)
 			keys = (List<Long>)GET_NEWS_KEYS_FROM_ISSUE.execute(new Object[] {i.getKey(), startId, numArts});
 		else
 			keys = (List<Long>)GET_NEWS_KEYS_FROM_ISSUE_BETWEEN_DATES.execute(new Object[] {i.getKey(), new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()), startId, numArts});
 		for (Long k: keys)
 			news.add(getNewsItem(k));
 
 		return news;
 	}
 
 	protected List<Category> getClassifiedCatsForNewsItem(SQL_NewsItem ni)
 	{
 	/**
       List<Category> cats = (List<Category>)GET_CATS_FOR_NEWSITEM.get(ni.getKey());
 		if (cats == null)
 			cats = new ArrayList<Category>();
 		if (_log.isDebugEnabled()) _log.debug("For news item: " + ni.getKey() + ", found " + cats.size() + " categories!");
 	**/
 
 			// To take advantage of caching (and avoid zillions of identical objects in the cache), fetch category keys and fetch categories by key
 		List<Category>cats = new ArrayList<Category>();
 		List<Long> catKeys = (List<Long>)GET_CAT_KEYS_FOR_NEWSITEM.get(ni.getKey());
 		for (Long k: catKeys)
 			cats.add(getCategory(k));
 
 			// Don't cache -- far too many news items are fetched, and they are not necessarily frequently accessed
 			// If we want to optimize, we need to profile for ni.getKey(), and start caching after a threshold
 		return cats;
 	}
 
 /**
 	protected int getClassifiedCatCountForNewsItem(SQL_NewsItem ni)
 	{
       Integer numArts = (Integer)GET_CATCOUNT_FOR_NEWSITEM.get(ni.getKey());
 		return (numArts == null) ? 0 : numArts;
 	}
 **/
 
 	/**
 	 * Clears the list of articles classified in a category
 	 * @param c Category for which news is to be cleared
 	 */
 	public void clearNews(Category cat)
 	{
 			// 1. Remove all articles from the cat news table
 			// 2. Reset article count (retain old update time)
       CLEAR_CAT_NEWS.execute(new Object[] {cat.getKey()});
 		cat.setNumArticles(0);
 		commitCatToDB(cat, true);
 
 			// Purge cache of stale news
 		purgeCacheNewsEntriesForCat(cat, false, false);
 
 			// Reset news for all nested categories
 		for (Category c: cat.getChildren())
 			c.clearNews();
 	}
 
    private static String getDateString(Date d) { return DATE_PARSER.get().format(d); }
 
 	/**
 	 * Get the directory where articles from news-source 'src'
 	 * published on date 'artDate' will be stored -- directory path
 	 * is relative to the global news archive dir
 	 *
 	 * @param s  Source from which news is being downloaded
 	 * @param d  Date of publishing
 	 */
 	public String getArchiveDir(Feed f, Date d)
 	{
 			// Get the directory name for the source
 		return getDateString(d) + File.separator + f.getTag() + File.separator;
 	}
 
 	/**
 	 * Get the directory where downloaded articles are stored in raw HTML form 
 	 * -- directory path is relative to the global news archive directory
 	 * This method creates the directory, if necessary!
 	 *
 	 * @param f        Feed from which article is being downloaded
 	 * @param artDate  Date when the article is published -- provided by the RSS feed 
 	 */
 	public String getArchiveDirForOrigArticles(Feed f, Date artDate)
 	{
 		String d = "orig" + File.separator + getArchiveDir(f, artDate);
 		IOUtils.createDir(GLOBAL_NEWS_ARCHIVE_DIR + d);
 		return d;
 	}
 
 	/**
 	 * Get the directory where downloaded articles are stored after they
 	 * are filtered (i.e. after their text content is extracted)
 	 * -- directory path is relative to the global news archive dir.
 	 * This method creates the directory, if necessary!
 	 *
 	 * @param f        Feed from which article is being downloaded
 	 * @param artDate  Date when the article is published -- provided by the RSS feed 
 	 */
 	public String getArchiveDirForFilteredArticles(Feed f, Date artDate)
 	{
 		String d = "filtered" + File.separator + getArchiveDir(f, artDate);
 		IOUtils.createDir(GLOBAL_NEWS_ARCHIVE_DIR + d);
 		return d;
 	}
 
 	/**
 	 * Get the directory where index files and rss/atom files for a feed
 	 * for a particular date is stored
 	 *
 	 * @param f     news feed
 	 * @param date  Date when news is being downloaded
 	 */
 	public String getArchiveDirForIndexFiles(Feed f, Date date)
 	{
 		String d = "filtered" + File.separator + getArchiveDir(f, date) + "index";
 		IOUtils.createDir(GLOBAL_NEWS_ARCHIVE_DIR + d);
 		return d;
 	}
 
 	protected String getBaseNameForArticle(String url)
 	{
       // FIXME: Potential race condition ... If 2 threads happen to process the same source at the
       // same time (which can only happen very rarely -- if the Download button is clicked during
       // the time the automatic download is happening), then, it can happen that the 2 threads
       // processing 2 urls with the same url base name could get the same filename and thus
       // over-write one another.  I am not fixing this race condition now because the real fix
       // is to get rid of the Download button or at least not allow 2 threads to process the same
       // new source!
 
 			// We've changed the base name now!
 		return StringUtils.md5(url);
 	}
 
 	/**
 	 * Get a print writer for writing the raw HTML of the article into
 	 *
 	 * @param ni The news item for which the writer is requested
     *
     * @returns null if a file exists for this news item!
 	 */
 	public PrintWriter getWriterForOrigArticle(NewsItem ni)
 	{
 		String url  = ni.getURL();
 		Feed   feed = ni.getFeed();
 		Date   d    = ni.getDate();
 		String fpath = GLOBAL_NEWS_ARCHIVE_DIR + getArchiveDirForOrigArticles(feed, d) + ((SQL_NewsItem)ni).getLocalFileName();
       File f = new File(fpath);
 			// Allow overwriting for empty files
       if (f.exists() && (f.length() > 0))
          return null;
 
 		try {
 			return IOUtils.getUTF8Writer(fpath);
 		}
 		catch (java.io.IOException e) {
 			_log.error("SQL: getWriterForOrigArt: Error opening file for " + fpath);
 			return null;
 		}
 	}
 
 	/**
 	 * Get a print writer for writing the filtered article into (i.e. after their text content is extracted)
 	 *
 	 * @param ni The news item for which the writer is requested
     *
     * @returns null if a file exists for this news item!
 	 */
 	public PrintWriter getWriterForFilteredArticle(NewsItem ni)
 	{
 		String url  = ni.getURL();
 		Feed   feed = ni.getFeed();
 		Date   d    = ni.getDate();
 		String fpath = GLOBAL_NEWS_ARCHIVE_DIR + getArchiveDirForFilteredArticles(feed, d) +  ((SQL_NewsItem)ni).getLocalFileName();
       File f = new File(fpath);
 			// Allow overwriting for empty files
       if (f.exists() && (f.length() > 0))
          return null;
 
 		if (_log.isInfoEnabled()) _log.info("Will write (in UTF-8) to " + fpath);
 		try {
 			return IOUtils.getUTF8Writer(fpath);
 		}
 		catch (java.io.IOException e) {
 			_log.error("SQL: getWriterForFilteredArt: Error opening file for " + fpath);
 			return null;
 		}
 	}
 
 	/**
 	 * Delete the requested filtered article from the archive!
 	 *
 	 * @param ni The news item that is to be deleted from the archive
 	 */
 	public void deleteFilteredArticle(NewsItem ni)
 	{
 		String url  = ni.getURL();
 		Feed   feed = ni.getFeed();
 		Date   d    = ni.getDate();
 		String fpath = GLOBAL_NEWS_ARCHIVE_DIR + getArchiveDirForFilteredArticles(feed, d) +  ((SQL_NewsItem)ni).getLocalFileName();
 		File f = new File(fpath);
 		if (f.exists()) {
 			if (!f.delete())
 				_log.error("Could not delete file " + fpath);
 		}
 	}
 
 	private void updateIssueForCat(Category cat, Issue i)
 	{
 		cat.setIssue(i);
 		for (Category c: cat.getChildren())
 			updateIssueForCat(c, i);
 	}
 
 	private void commitCatToDB(Category cat, boolean purgeAllStaleCacheEntries)
 	{
 		if (_log.isDebugEnabled()) _log.debug("Setting article count for cat " + cat.getName() + ":" + cat.getKey() + ": to " + cat.getNumArticles());
 
 			// When a topic is freshly created, till new articles are added to it,
 			// categories may not have a valid last update time.
 		Date lut = cat.getLastUpdateTime();
 		if (lut != null)
 			lut = new Timestamp(lut.getTime());
 
 		if (cat.isLeafCategory())
 			UPDATE_LEAF_CAT_NEWS_INFO.execute(new Object[] {lut, cat.getNumItemsSinceLastDownload(), cat.getKey()});
 		else
 			UPDATE_CAT_NEWS_INFO.execute(new Object[] {cat.getNumArticles(), lut, cat.getNumItemsSinceLastDownload(), cat.getKey()});
 
 		if (purgeAllStaleCacheEntries) {
 			purgeCacheEntriesForCat(cat, true, false);
 			purgeIssueAndUserEntries(cat.getIssue());
 
 				// Check out a new copy of the issue & update issue references in this category and all sub-categories
 				// This is because the caller might continue to use the 'cat' object that is passed in!
 			updateIssueForCat(cat, getIssue(cat.getIssue().getKey()));
 		}
 /**
 				// Update info in place!  FIXME: This should not be necessary ... but, in case the count has gone off-key ...
 		else if (cat.isLeafCategory()) {
 			Triple info = (Triple)GET_CAT_INFO.execute(new Object[]{cat.getIssue().getKey(), cat.getCatId()});
 			cat.setNumArticles((Integer)info._b);
 		}
 **/
 	}
 
 	private int updateArtCountsForCat(Category cat)
 	{
 			// Process in depth-first order!
 		if (!cat.isLeafCategory()) {
 			int orig = cat.getNumArticles();
 			int n = 0;
 			for (Category ch: cat.getChildren())
 				n += updateArtCountsForCat(ch);
 
 			if (orig != n) {
 				cat.setNumArticles(n);
 				commitCatToDB(cat, false);
 			}
 		}
 
 		return cat.getNumArticles();
 	}
 
 	private void updateArtCounts(Issue i)
 	{
 		int n    = 0;
 		int orig = i.getNumArticles();
 		for (Category c: i.getCategories())
 			n += updateArtCountsForCat(c);
 
 		if (orig != n) {
 				// Update info in the db!
 			if (_log.isDebugEnabled()) _log.debug("Setting article count for issue " + i.getName() + ":" + i.getKey() + ": to " + n);
 			UPDATE_ARTCOUNT_FOR_TOPIC.execute(new Object[] {n, new Timestamp(i.getLastUpdateTime().getTime()), i.getNumItemsSinceLastDownload(), i.getKey()});
 
 				// Update info in the in-memory object
 			i.setNumArticles(n);
 
 				// Sanity checks
 			if ((getIssue(i.getKey()) != i) || (i.getUser().getIssue(i.getName()) != i)) {
 				_log.error("ERROR! We seem to have multiple entries in the cache for the same issue!");
 
 					// Purge stale cache entries!
 				purgeIssueAndUserEntries(i);
 
 					// Check out a fresh copy of the issue and update issue references in the issue's category subtree!
 				i = getIssue(i.getKey());
 				for (Category c: i.getCategories())
 					updateIssueForCat(c, i);
 			}
 		}
 	}
 
 	/**
 	 * This method archives news for a category in the appropriate place
 	 * @param c	Category whose news needs to be committed
 	 */
 	public void commitNewsToArchive(Issue i)
 	{
 			// Commit all leaf cats in the issue
 		List<Category> leafCats = _leafCatsToCommit.get(i.getKey());
 		if (leafCats != null) {
			for (Category c: leafCats) {
 				commitCatToDB(c, false);
		      purgeCacheNewsEntriesForCat(c, true, false);
         }
 
 			leafCats.clear();
 		}
 
 			// Update article counts
 		updateArtCounts(i);
 	}
 
 	private Collection<NewsItem> getNewsForIndex(long indexKey)
 	{
 			// NOTE: This whole generics thing is pointless here!
 			// The sql query returns a list.  This base type check will be enforced at runtime.
 			// But, at runtime, the actual generic type parameter itself is not available.
 			// so, I can cast it to damn well what I please!  If it were available, this typecheck
 			// will fail because, the query processor creates a generic list, not a list of newsitems.
       return (List<NewsItem>)GET_NEWS_FROM_NEWSINDEX.execute(new Object[] {indexKey});
 	}
 
 	public Collection<NewsItem> getArchivedNews(NewsIndex index)
 	{
 		return getNewsForIndex(((SQL_NewsIndex)index).getKey());
 	}
 
 	/**
 	 * This method goes through the news archive and fetches news
 	 * for a desired news source for a desired date.
 	 *
 	 * @param s    Source for which news has to be fetched
 	 * @param date Date for which news has to be fetched (in format yyyymmdd)
 	 */
 	public Collection<NewsItem> getArchivedNews(Source s, String y, String m, String d)
 	{
 		if (m.startsWith("0")) m = m.substring(1);
 		if (d.startsWith("0")) d = d.substring(1);
 		//String dateStr = d + "." + m + "." + y;
 		String dateStr = y + "-" + m + "-" + d;
 		long   feedId  = s.getFeed().getKey();
 
 		_log.info("REQUESTED NEWS for " + s._name + ":" + dateStr);
 
 		Long niKey = getNewsIndexKey(feedId, dateStr);
 		return ((niKey == null) || (niKey == -1)) ? null : getNewsForIndex(niKey);
 	}
 
 	/**
 	 * This method goes through the entire news archive and fetches news
 	 * index files for a desired news source.  Note that for the same
 	 * news source, multiple index files can be returned.  This can happen,
 	 * for instance, when the news archive is organized by date, and so,
 	 * the iterator will return one news index file for each date.
 	 *
 	 * @param s   Source for which news indexes have to be fetched
 	 */
 	public Iterator<? extends NewsIndex> getIndexesOfAllArchivedNews(Source s)
 	{
 		List<SQL_NewsIndex> nis = (List<SQL_NewsIndex>)GET_ALL_NEWS_INDEXES_FROM_FEED_ID.execute(new Object[] {s.getFeed().getTag()});
       return nis.iterator();
 	}
 
 	/**
 	 * This method goes through the entire news archive and fetches news
 	 * index files for a desired news source.  Note that for the same
 	 * news source, multiple index files can be returned.  This can happen,
 	 * for instance, when the news archive is organized by date, and so,
 	 * the iterator will return one news index file for each date.
 	 *
 	 * @param s   Source for which news indexes have to be fetched
 	 * @param sd  Start date (inclusive) from which index files have to be fetched
 	 * @param ed  End date (inclusive) beyond which index files should not be fetched
 	 */
 	public Iterator<? extends NewsIndex> getIndexesOfAllArchivedNews(Source s, Date sd, Date ed)
 	{
 		if (_log.isInfoEnabled()) {
 			_log.info("Start: " + sd);
 			_log.info("End  : " + ed);
 		}
       return getIndexesOfAllArchivedNews(s.getFeed().getKey(), sd, ed);
 	}
 
 	public Iterator<? extends NewsIndex> getIndexesOfAllArchivedNews(Long feedKey, Date sd, Date ed)
 	{
 		return ((List<SQL_NewsIndex>)GET_ALL_NEWS_INDEXES_BETWEEN_DATES_FROM_FEED_ID.execute(new Object[]{feedKey, new java.sql.Date(sd.getTime()), new java.sql.Date(ed.getTime())})).iterator();
 	}
 }
