 package newsrack.filter;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import newsrack.NewsRack;
 import newsrack.archiver.Source;
 import newsrack.database.DB_Interface;
 import newsrack.database.NewsItem;
 import newsrack.filter.Filter.RuleTerm;
 import newsrack.user.User;
 import newsrack.util.StringUtils;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * The class <code>Category</code> represents a category for
  * holding news items as specified by its filter
  *
  * DESIGN AND IMPLEMENTATION NOTES:
  * Right now, this Category class implements the "Category-as-a-filter"
  * functionality.
  * (*) Category as a filter: In this incarnation, the result of streaming a
  *     set of articles through this category filter is an output RSS feed -- the
  *     articles that are accepted by the filter need not necessarily be stored
  *     in this Category object.  To provide the filtering mechanism, it also
  *     implements a filtering/classification algorithm (which is, at this time,
  *     hardcoded, not parameterizable, or pluggable .. the details of the 
  *     algorithm are also not available publicly).
  *
  * The following storage aspect of a category is now being handled by the
  * database layer
  * (*) Category as a folder: In this incarnation, a category is a container for
  *     all the articles that are accepted by the filter.  
  * 
  * @author  Subramanya Sastry
  * Version 2.0 November 10, 2006
  */
 
 public class Category implements Comparable, java.io.Serializable
 {
 // ############### STATIC FIELDS AND METHODS ############
 	public static final List<Category> EMPTY_LIST = new ArrayList<Category>();
 	private static String       _indent    = "";
 	private static DB_Interface _db;	// Cached link to the DB Interface object
 
    	// Logging output for this class
    protected static final Log _log = LogFactory.getLog(Category.class);
 
 	public static void init(DB_Interface db)
 	{
 		_db = db;
 		Filter.init(db);
 	}
 
 	/*
 	 * gets a category, given its name, from a list of categories
 	 * This is just a helper method.
 	 * @param cats     List of categories
 	 * @param catName  Name of the category
 	 */
 	protected static Category getCategory(final Collection<Category> cats, final String catName)
 	{
 		for (final Category c : cats) {
 			if (catName.equals(c.getName()))
 				return c;
 		}
 		return null;
 	}
 
 	public static void deleteNewsItems(Long catKey, List<Long> keys)
 	{
 		_db.deleteNewsItemsFromCategory(catKey, keys);
 	}
 
 	public static void main(final String[] args)
 	{
 		if (args.length == 0) {
 			if (_log.isInfoEnabled()) _log.info("Usage: java Category <file>");
 			System.exit(0);
 		}
 	}
 
 // ############### NON-STATIC FIELDS AND METHODS ############
 	/**
 	 * The following 7 fields are used to implement the functionality 
 	 * of 'category-as-a-filter'
 	 */
 	private   Long     _key;				// Database key
    private   int      _catId;				// Unique category id
 	protected String 	 _name;				// Name of the category
 	private   Filter   _filter;			// The filter corresponding to this category
 	private   Issue    _issue;				// Issue to which this category belongs! (might be null, if only defined as part of a collection)
 	private   Category _parent;	   	// Parent category if this is nested in other cats
 	protected Collection<Category> _children;	// Nested categories
 	private   Date       _lastUpdateTime; // Date when new items were last added to this category
 	private   int        _numArticles;	// Number of articles in this category
 	private   OutputFeed _outputFeed;	// Output rss feed for this category
 	private   int        _numItemsSinceLastDownload;	// Number of new items since last download
 	private   String     _taxonomyPath;	// Unique global taxonomy path
 
 	/**
 	 * Dummy empty constructor
 	 */
 	public Category() 
 	{ 
 		_children = new ArrayList<Category>();
 	}
 
 	/**
 	 * Constructor used while building this from the DB
 	 */
 	public Category(Long key, String name, Filter f, int id)
 	{
 		_key = key;
 		_name = name;
 		_filter = f;
 		_catId = id;
 		_children = new ArrayList<Category>();
 	}
 
 	/**
 	 * Build a leaf category
 	 */
 	public Category(final String name, final Filter f) throws Exception
 	{
 		_key = null;
 
 			// Verify that the name is an acceptable news rack name
 		try {
 			StringUtils.validateName("category", name);
 			_name = name; 
 		}
 		catch (final Exception e) {
 			_log.error("Error validating category name " + name, e);
 			throw e;
 		}
 		_filter = f;
 		_children = new ArrayList<Category>();
 	}
 
 	/**
 	 * Build a leaf category
 	 */
 	public Category(final String name, final RuleTerm r) throws Exception
 	{
 		_key = null;
 
 			// Verify that the name is an acceptable news rack name
 		try {
 			StringUtils.validateName("category", name);
 			_name = name; 
 		}
 		catch (final Exception e) {
 			_log.error("Error validating category name " + name, e);
 			throw e;
 		}
 		_filter = new Filter(_name, r);
 		_children = new ArrayList<Category>();
 	}
 
 	/**
 	 * Build a container category
 	 */
 	public Category(final String name, final Collection<Category> cats) throws Exception
 	{
 		_key = null;
 		try {
 			StringUtils.validateName("category", name);
 			_name = name; 
 		}
 		catch (final Exception e) {
 			_log.error("Error validating category name " + name, e);
 			throw e;
 		}
 		_children = cats;
 
 		for (Category c: cats)
 			c._parent = this;
 	}
 
 
 	public Category clone()
 	{
 		// NOTE: This method is called only during parsing
 
 		Category cloneCat = new Category(); 
 		cloneCat._key    = _key;
 		cloneCat._name   = _name;
 		cloneCat._catId  = _catId;
 		cloneCat._filter = _filter;
 		if (!isLeafCategory()) {
 			cloneCat._children = new ArrayList<Category>();
 			for (Category c: _children) {
 				Category cc = c.clone();
 				cloneCat._children.add(cc);
 				cc._parent = cloneCat;
 			}
 		}
 		return cloneCat;
 	}
 
 	public int compareTo(final Object o)
 	{
 		if (o instanceof Category) {
 			final Category c = (Category)o;
 			final int x = _name.compareTo(c._name);
 			if (x == 0)
 				return getUser().getUid().compareTo(c.getUser().getUid());
 			else
 				return x;
 		} else
 			throw new ClassCastException("Category.compareTo: Found " + o.getClass() + " instead of Category!");
 	}
 
    public void setKey(Long k) { _key = k; }
 
    public Long getKey() { return _key; }
 
    public void setParent(Category p) { _parent = p; }
 
 	public Category getParent() { return _parent; }
 
 	public void addChild(Category c) { _children.add(c); }
 
 	public void setChildren(List<Category> cats) { _children = cats; }
 
 	public Collection<Category> getChildren() { return (_children == null) ? EMPTY_LIST : _children; /* FIXME: any way to return a read-only list?? */ }
 
 	/** Get all leaf categories rooted at this sub-tree */
 	public Collection<Category> getLeafCats()
 	{
 		List<Category> l = new ArrayList();
 		for (Category c: _children) {
 			if (c.isLeafCategory())
 				l.add(c);
 			else
 				l.addAll(c.getLeafCats());
 		}
 		return l;
 	}
 
 	public void setIssue(Issue i) { _issue = i; }
 
 	public Issue getIssue() { return _issue; }
 
 	public User getUser() { return getIssue().getUser(); }
 
 	public Filter getFilter() { return _filter; }
 
 	public void setFilter(Filter f) { _filter = f; }
 
 	public boolean isTopLevelCategory() { return (_parent == null); }
 
 	public boolean isLeafCategory() { return (_filter != null); }
 
 	public String getName() { return _name; }
 
 	public int getCatId() { return _catId; }
 
 	public void setTaxonomyPath(String p) { _taxonomyPath = p; }
 
 	/** Return the fully qualified category path for the category */
 	public String getTaxonomyPath() { return _taxonomyPath; }
 
 	/** get the total number of articles in this category (including all nested cats) */
 	public int getNumArticles() { return _numArticles; }
 
 	public void setNumArticles(int n) { _numArticles = n; }
 
 	public void setNumItemsSinceLastDownload(int n) { _numItemsSinceLastDownload = n; }
 
 	public int getNumItemsSinceLastDownload() { return _numItemsSinceLastDownload; }
 
 	public Date getLastUpdateTime() { return _lastUpdateTime; }
 
 	public void setLastUpdateTime(Date d) { _lastUpdateTime = d; }
 
 	/**
 	 * get the time when this category was last updated 
     * as a string representation
 	 */
 	public String getLastUpdateTime_String()
 	{
 		if (_lastUpdateTime == null)
 			return "--";
 		else
 			synchronized (NewsRack.DF) {
 				return (NewsRack.DF.format(_lastUpdateTime));
 			}
 	}
 
 	public boolean updatedWithinLastNHours(final int numHours)
 	{
 		if (_lastUpdateTime == null) {
 			if (_log.isErrorEnabled()) _log.error("NULL _lastUpdateTime for " + _issue.getName() + " and cat " + getName());
 			return false;
 		}
 
 		final long now = System.currentTimeMillis();
 		final long lut = _lastUpdateTime.getTime();
 		return ((now - lut) < (numHours * 3600 * 1000)) ? true : false;
 	}
 
 	/*
 	 * gets a category, given its name.  Note that the
 	 * requested category must be defined at the top-level.
 	 * Nested categories cannot be fetched.
     *
 	 * @param catName  Name of the category
 	 */
 	public Category getCategory(final String catName)
 	{
 		return (isLeafCategory()) ? null : getCategory(_children, catName);
 	}
 
 	/**
 	 * This method pretty-prints the category.  Category nesting
 	 * is indicated by appropriately indenting the nested categories.
 	 */
 	public String toString()
 	{
 		final String old = _indent;
 
 		final StringBuffer sb = new StringBuffer(old + _name);
 		if (_issue != null)
 			sb.append(" .. in Issue " + _issue.getName());
 		if (_filter != null) {
 			sb.append(" <- (");
 			sb.append(_filter._ruleString);
 			sb.append(")\n");
 		}
 		else if (!isLeafCategory()) {
 			_indent = new String(old + StringUtils.TAB);
 
 			sb.append(": [\n");
 			for (Category c: _children)
 				sb.append(c.toString());
 			sb.append(old + "]\n");
 
 			_indent = old;
 		}
 
 		return sb.toString();
 	}
 
 	/**
 	 * This method generates an XML taxonomy.  There is also going to
 	 * be appropriate indenting of the nested categories.
 	 */
 /**
 	public String getTaxonomy()
 	{
 		String oldI = _indent;
 		String newI = new String(oldI + StringUtils.TAB);
 		_indent = newI;
 
 		StringBuffer sb = new StringBuffer();
 		sb.append(oldI + "<category>\n");
 		sb.append(newI + "<name> " + _name + " </name>\n");
 		sb.append(newI + "<id> " + getCatId() + " </id>\n");
 		if (_filter != null) {
 			sb.append(newI + "<rule>" + _filter._ruleString + "</rule>\n");
 		}
 		else if (!isLeafCategory()) {
 			for (Category c: _children)
 				sb.append(c.getTaxonomy());
 		}
 		sb.append(oldI + "</category>\n");
 		_indent = oldI;
 
 		return sb.toString();
 	}
 **/
 
 	/**
 	 * gets the news classified into this category
 	 * @param n Number of news items that need to be returned
 	 */
 	public List<NewsItem> getNews(final int n) { return _db.getNews(this, n); }
 
 	/**
 	 * Return classified news for this category
 	 * @param start       starting date (in yyyy.mm.dd format)
 	 * @param end         end date      (in yyyy.mm.dd format)
 	 * @param src         the news source from which we need the news (can be null)
 	 * @param startIndex  starting article
 	 * @param numArts     number of articles to fetch
 	 */
 	public List<NewsItem> getNews(Date start, Date end, Source src, int startIndex, int numArts) 
 	{ 
 		return _db.getNews(this, start, end, src, startIndex, numArts); 
 	}
 
 	/**
 	 * gets the news classified into this category 
 	 * -- starting at a specified index
 	 * @param startId  The starting index
 	 * @param n        Number of news items that need to be returned
 	 */
 	public List<NewsItem> getNews(final int startId, final int n) { return _db.getNews(this, startId, n); }
 
 	/**
 	 * This method clears all previously categorized news
 	 */
 	public void clearNews() { _db.clearNews(this); }
 
 	/**
 	 * Check if a news item is present in this category.
 	 * @param a  News Item to check for
 	 */
 	public boolean containsArticle(final NewsItem a) { return _db.newsItemPresentInCategory(this, a); }
 
 	/**
 	 * This method attempts to file an article into this category, if
 	 * it is a leaf category, or attempts to file the article in all
 	 * the sub-categories, if it is a non-leaf category.
 	 *
 	 * @param article      The article to be filed
 	 * @param numTokens    Number of tokens encountered in the article
 	 * @param matchScores  A table of all concepts that matched along with
 	 *                     their corresponding match scores
 	 * @return             The hit score for this category
 	 *                     If a leaf category, the match score is determined
 	 *                       by the filtering rule -- algorithm is not yet
 	 *                       publicly documented.
 	 *                     If a non-leaf category, the match score is the
 	 *                       maximum of match score of its sub-categories
 	 */
 	public synchronized Score getMatchScore(NewsItem article, int numTokens, Hashtable matchScores)
 	{
 		if (_log.isDebugEnabled()) _log.debug(" --> get match score for " + _name);
 
 		ArrayList<Category> matchedCats = new ArrayList<Category>();
 		int matchScore  = 0;
 		if (isLeafCategory()) {
 			if (_log.isDebugEnabled()) _log.debug("---- trying to match <" + _name + "> with rule <" + _filter._ruleString + "> ----");
 
 				// Match the rule for this category
 			matchScore = _filter.getMatchScore(article, numTokens, matchScores);
 			if (matchScore >= _filter.getMinMatchScore()) {
 					// NOTE: we are checking this here rather than before trying to match to minimize
 					// the # of db queries ... if there is no match with the filter, then, we won't bother
 					// querying the db ... so, all is well!
 				if (!containsArticle(article))
 				   matchedCats.add(this);
 			}
 		}
       else {
 			// Go through all nested categories and match rules
 			if (_log.isDebugEnabled()) _log.debug("CAT:"+ _name + ": Processing subcats ...");
 			for (Category subCat: _children) {
 				Score subCatScore = subCat.getMatchScore(article, numTokens, matchScores);
 					// Match score for this cat = max(match scores of sub-cats)
 				if (subCatScore.value() > matchScore)
 					matchScore = subCatScore.value();
 
 					// Accumulate matched leaf cats
 				matchedCats.addAll(subCatScore.getMatchedCats());
 			}
 			if (_log.isDebugEnabled()) _log.debug("CAT:"+ _name + ": DONE processing subcats ...");
       }
 
 			// Note that if this article has already been added to the category, matchedCats will be empty
 		if (!matchedCats.isEmpty()) {
 		   if (_log.isInfoEnabled()) _log.info("ADDING " + article.getTitle() + " to " + getUser().getUid() + ":" + getIssue().getName() + ":" + _name);
          if (_outputFeed != null)
             _outputFeed.addNewsItem(article, matchedCats); 	// Add the news item to the RSS feed 
 			_db.addNewsItem(article, this, matchScore); 		// Record this in the database
 			_lastUpdateTime = new Date(); 						// Set last update time
 			if (_log.isDebugEnabled()) _log.debug("Added to category " + _name);
 		}
 
 		// FIXME: This assumes that category names are unique across
 		// the issue.  If not, the semantics are such that if a category
 		// name is repeated in an issue, then the "[cat-name]" shortcut
 		// is undefined.  If the latter, clarify in the documentation
 		// so that users know this clearly.  Otherwise, I have to implement
 		// scoping of category references.
 		final Score retVal = new Score(matchScore, matchedCats);
 		matchScores.put("[" + _name + "]", retVal);
 
 			// Get rid of match scores for all nested cats since they
 			// won't be accessible in the taxonomy outside the current cat!
 		for (Category subCat: _children)
 			matchScores.remove("[" + subCat._name + "]");
 
 		return retVal;
 	}
 
 	protected int updateCatMap(int catId, final Map catMap)
 	{
 		Object o = catMap.get(this);
 
 			// If there is a pre-existing ID for this category use that!
 		if (o != null) {
 			_catId = ((Integer)o).intValue();
       }
 		else {
 			catId++;
 			_catId = catId;
 			o = new Integer(catId);
 		}
 			// Add a mapping from catId --> category
 		catMap.put(o, this);
 
 			// Assign to nested cats!
 		if (!isLeafCategory()) {
 			for (Category c: _children)
 				catId = c.updateCatMap(catId, catMap);
       }
 		return catId;
 	}
 
 	protected void setupForDownloading(Issue issue)
 	{
 		if (_log.isDebugEnabled()) _log.debug("setupFordownloading for issue " + issue.getName() + " and cat " + getName());
 
 		_issue = issue;
 
 			// Initialize directory paths
 		String basePath = (_parent == null) ? _issue.getTaxonomyPath() : _parent._taxonomyPath;
 		_taxonomyPath = basePath + File.separator + StringUtils.getOSFriendlyName(_name);
 
 			// ORDER IMPORTANT!  Process nested categories
 			// only after processing this category and setting
 			// up all the paths, etc.
 		if (!isLeafCategory()) {
 			for (Category c: _children)
 				c.setupForDownloading(issue);
       }
 	}
 
 	protected void collectUsedConcepts(Set<Concept> usedConcepts)
 	{
 		if (isLeafCategory()) {
 			_filter.collectUsedConcepts(usedConcepts); // Traverse the expression tree and collect all used concepts
       }
 		else {
 			for (Category c: _children)
 				c.collectUsedConcepts(usedConcepts);
       }
 	}
 
 	public String getRSSDir() { return NewsRack.getBaseRssDir() + _taxonomyPath + File.separator; }
 
 	public String getRSSFeedURL() {
 		return NewsRack.getServerURL() + File.separator
 						+ NewsRack.getDirPathProperty("rssDir")
 		            + _taxonomyPath + File.separator + NewsRack.getProperty("rssfeedName");
 	}
 
 	private void initFeed()
 	{
 		if (_log.isDebugEnabled()) _log.debug("Initializing feed for category: " + getName());
 
 		String feedName  = getRSSDir() + NewsRack.getProperty("rssfeedName");
 		String feedTitle = "NewsRack: " + _taxonomyPath;
 		String feedDesc  = "This is a custom RSS feed generated by NewsRack for user " + getUser().getUid()
 								 + " for category path " + _taxonomyPath + " in issue " + getIssue().getName();
 		_outputFeed = new OutputFeed(feedName, getRSSDir(), _name, getRSSFeedURL(), feedTitle, feedDesc, _taxonomyPath);
 		_numItemsSinceLastDownload = 0;
 	}
 
 	protected void readInCurrentRSSFeed()
 	{
 		if (_outputFeed == null) 
 			initFeed();
 
 		if (_log.isDebugEnabled()) _log.debug("Reading in current feed for category: " + getName());
 		try {
 			_outputFeed.readInCurrentRSSFeed();
 		}
 		catch (Exception e) {
 			_log.error("Caught exception reading current feed for cat: " + getName(), e);
 		}
 
 			// Process children
 		if (!isLeafCategory()) {
 			for (Category c: _children)
 				c.readInCurrentRSSFeed();
 		}
 	}
 
 	protected void freeRSSFeed()
 	{
 			// Free up space
 		_outputFeed = null;
 
 			// Process children
 		if (!isLeafCategory()) {
 			for (Category c: _children)
 				c.freeRSSFeed();
 		}
 	}
 
 	public void invalidateRSSFeed()
 	{
 		if (_outputFeed == null) 
 			initFeed();
 
 			// Reset the feed
 		_outputFeed.invalidate();
 		_numItemsSinceLastDownload = 0;
 
 			// Reset feed for all nested categories
 		if (!isLeafCategory()) {
 			for (Category c: _children)
 				c.invalidateRSSFeed();
 		}
 	}
 
 	public void updateRSSFeed()
 	{
 		_outputFeed.update();
 		_numItemsSinceLastDownload = _outputFeed.getNumItemsSinceLastDownload();
 
 			// 3. Process nested categories
 		if (!isLeafCategory()) {
 			for (Category c: _children)
 				c.updateRSSFeed();
       }
 	}
 }
