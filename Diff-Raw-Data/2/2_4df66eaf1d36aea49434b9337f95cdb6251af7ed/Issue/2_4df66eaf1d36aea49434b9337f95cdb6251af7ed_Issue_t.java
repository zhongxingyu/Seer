 package newsrack.filter;
 
 import newsrack.util.IOUtils;
 import newsrack.util.StringUtils;
 import newsrack.util.ParseUtils;
 import newsrack.util.ProcessReader;
 import newsrack.NewsRack;
 import newsrack.user.User;
 import newsrack.database.DB_Interface;
 import newsrack.database.NewsIndex;
 import newsrack.database.NewsItem;
 import newsrack.archiver.Source;
 import newsrack.archiver.Feed;
 
 import org.w3c.dom.Node;
 import org.w3c.dom.Element;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Date;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.HashSet;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Hashtable;
 import java.util.Enumeration;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.io.File;
 import java.io.Reader;
 import java.io.PrintWriter;
 import java.io.DataInputStream;
 import java.lang.Class;
 import java.lang.reflect.Method;
 import java.lang.reflect.Constructor;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * The class <code>Issue</code> encapsulates an issue that some
  * user might be interested in monitoring.  A user specifies one or
  * more issues that (s)he is interested in and provides definitions
  * for every issue.  An issue is fully specified by the category
  * definitions.
  *
  * Note that an issue is also a category -- a root category
  * containing everything else.  So, class Issue extends class Category
  *
  * @author  Subramanya Sastry
  * @version 1.0 13/05/04
  */
 
 public class Issue implements java.io.Serializable
 {
 // ############### STATIC FIELDS AND METHODS ############
 	private final static String PREDEF_KWORDS[] = {
 //		"WORD",       "[^ \\t\\n\\r]+",
       "WORD",       "[^ \\t\\n\\r\\\\.,;:?+*&\\^%$#@!|~'/`\\\"\\-\\{\\}()\\[\\]<>]+",
 		"WS",         "[ \\t\\n\\r]",
 		"SPACE",      "{WS}+",
 		"HYPHEN",     "{SPACE}?[-]*{SPACE}?",
 		"NUMBER",     "[:digit:]+([,\\.][:digit:]+)*",
 		"NUM_RANGE",  "{NUMBER}[-]{NUMBER}",
 	};
 
 		// Populate the table of predefined keywords with the
 		// corresponding JFLEX macros.
 	public static Hashtable PREDEFINED_KEYWORDS;
 
 	public static final String JFLEX_SPACE               = "\" {SPACE} \"";
 	public static final String JFLEX_HYPHEN              = "\" {HYPHEN} \"";
 	public static final String ISSUE_SCANNER_SUFFIX      = "_IssueScanner";
 	public static final String JFLEX_SCANNER_TOKEN_CLASS = "newsrack.filter.ConceptToken";
 	public static final String CATCHALL_TOKEN_NAME       = "CATCHALL_TOKEN";
 
 	private static final List<Category> EMPTY_LIST = new ArrayList<Category>();
 
    	/* Logging output for this class. */
    private static Log _log = LogFactory.getLog(Issue.class);
 
 	private static DB_Interface _db;	// Cached link to the DB Interface object
 
 	public static void init(DB_Interface db)
 	{
 		PREDEFINED_KEYWORDS = new Hashtable();
 		int n = PREDEF_KWORDS.length;
 		for (int i = 0; i < n; i += 2) {
 			PREDEFINED_KEYWORDS.put(PREDEF_KWORDS[i], PREDEF_KWORDS[i+1]);
 		}
 		_db = db;
 	}
 
 	private static int copyPredefinedKeyword(StringBuffer re, int i, char[] cs)
 	{
 			// This is a newsrack predefined keywords
 			// Read in the entire keyword as is without changes
 		char c;
 		do {
 			c = cs[i++];
 			re.append(c);
 		} while (c != ']');
 		return i;
 	}
 
 	private static HashSet processKeywordSubstringList(Hashtable keywords, String rootKw, String newKw, HashSet kwsh)
 	{
 		Iterator it      = kwsh.iterator();
 		HashSet  kwshNew = new HashSet();
 			// Append "newKw" to the end of every keyword substring
 			// present in the keyword substring list.
 			// ["a b c", "b c", "c"] ==> ["a b c d", "b c d", "c d", "d"] 
 		it = kwsh.iterator();
 		while (it.hasNext()) {
 			kwshNew.add((String)it.next() + ' ' + newKw);
 		}
 		kwshNew.add(newKw);
 
 			// Now, for every element in the set of keyword substrings, check
 			// if that keyword substring has been defined by some other concept -- 
 			// if so, add it to the set of concepts that define 'rootKw'.
 			//
 			// As a result, whenever rootKw is seen in the text (ex: "a b c d") all
 			// concepts which define keywords that are substrings of rootKw ("a b c d")
 			// (ex: "b c", "a b c", "d") will get triggered.
 		HashSet rootKwCptSet = (HashSet)keywords.get(rootKw);
 		it = kwshNew.iterator();
 		while (it.hasNext()) {
 			String kw = (String)it.next();
 			if (_log.isDebugEnabled()) _log.debug("Processing keyword substring: " + kw);
 			if (!kw.equals(rootKw)) {
 				HashSet kwCptSet = (HashSet)keywords.get(kw); 
 				if (kwCptSet != null) {
 					rootKwCptSet.addAll(kwCptSet);
 				}
 			}
 		}
 
 			// return the new keyword substring set
 		return kwshNew;
 	}
 
 	/**
 	 * Check if a keyword will be matched by an existing set of keywords
 	 * @param keywords  Existing set of keywords
 	 * @param k         New keyword -- which has to be checked for equivalence
 	 *                  among the existing keyword set
 	 */
 	private static void identifyMultiTokens(Hashtable keywords, String k)
 	{
 		if (_log.isDebugEnabled()) _log.debug("IdentifyMultiTokens for " + k);
 
 		// If "water privatisation" is present in one concept and "water" is
 		// present in another concept, when "water privatisation" is seen
 		// in the text, both the concepts should be triggered.
 
 		StringBuffer buf  = new StringBuffer();
 		int          n    = k.length();
 		char[]       cs   = k.toCharArray();
 		HashSet      kwsh = new HashSet();					// List of keyword substrings
 
 		if (_log.isDebugEnabled()) _log.debug("### Identifying multi-tokens for " + k + " ###");
 		for (int i = 0; i < n; i++) {
 			char c = cs[i];
 			if (c == '[') {
 				i = copyPredefinedKeyword(buf, i, cs);
 			}
 			else if (c == ' ') {
 				kwsh = processKeywordSubstringList(keywords, k, buf.toString(), kwsh);
 				buf  = new StringBuffer();
 			}
 			else {
 				buf.append(c);
 			}
 		}
 		processKeywordSubstringList(keywords, k, buf.toString(), kwsh);
 	}
 
 	private static int emitPredefinedKeywords(StringBuffer s, int i, char[] cs)
 	{
 			// This is a newsrack predefined keywords
 			// Read in the entire keyword as is without changes
 		StringBuffer buf = new StringBuffer();
 		char c;
 		i++;
 		while (true) {
 			c = cs[i];
 			i++;
 			if (c == ']')
 				break;
 			buf.append(c);
 		}
 			// Any errors with unknown keywords would have been caught during 
 			// initialization (NormalizeKeyword) -- so no need to check for errors here.
 		s.append(Issue.PREDEFINED_KEYWORDS.get(buf.toString()));
 		return i;
 	}
 
 	private static void genMultiToken_JFLEX_RegExp(PrintWriter pw, String k, HashSet hs)
 	{
 		if (_log.isDebugEnabled()) _log.debug("GMTJRE: Generating multi-token for " + k);
 
 		/* Transform the keyword into a regular expression
 		 * A ' ' character will match one or more white space characters */
 		StringBuffer re = new StringBuffer("\"");
 		int          n  = k.length();
 		char[]       cs = k.toCharArray();
 
 		for (int i = 0; i < n; i++) {
 			char c = cs[i];
 			switch (c) {
 			case ' ': re.append(JFLEX_SPACE); break;
 			case '[': i = emitPredefinedKeywords(re, i, cs); break;
 			default : re.append(c); break;
 			}
 		}
 
 		re.append("\"");
 		pw.println(re);
 		pw.println("\t\t{");
 		pw.println("\t\t\tString cs[] = {");
 		Iterator it = hs.iterator();
 		while (it.hasNext()) {
 			pw.println("\t\t\t\t\"" + ((Concept)it.next()).getLexerToken().getToken() + "\",");
 		}
 		pw.println("\t\t\t};");
 		pw.println("\t\t\treturn token(cs);");
 		pw.println("\t\t}");
 	}
 
 	private static void gen_JFLEX_RegExp(String kword, StringBuffer buf)
 	{
 		/* Transform the keyword into a regular expression
 		 * A ' ' character will match one or more white space characters */
 
 		int    n  = kword.length();
 		char[] cs = kword.toCharArray();
 
 		buf.append("\"");
 		for (int i = 0; i < n; i++) {
 			char c = cs[i];
 			switch (c) {
 			case ' ': buf.append(Issue.JFLEX_SPACE); break;
 			case '[': i = emitPredefinedKeywords(buf, i, cs); break;
 			default : buf.append(c); break;
 			}
 		}
 		buf.append("\"");
 	}
 
 	private static void gen_JFLEX_RegExps(Concept c, PrintWriter pw, Hashtable keywords)
 	{
 		if (_log.isDebugEnabled()) _log.debug("GJRE: Generating regexp for " + c.getName());
 
 		StringBuffer buf   = new StringBuffer("");
 		Iterator     it    = c.getKeywords();
 		boolean      first = true;
 		boolean      empty = true;
 		while (it.hasNext()) {
 			String  kw   = (String)it.next();
 			HashSet cSet = (HashSet)keywords.get(kw);
 				// If this keyword is part of just one concept,
 				// emit it right away
 			if (cSet.size() == 1) {
 				if (first)
 					first = false;
 				else
 					buf.append(" | ");
 				empty = false;
 				gen_JFLEX_RegExp(kw, buf);
 			}
 		}
 		if (!empty) {
 			pw.println(buf);
 			pw.println("\t\t{ return token(\"" + c.getLexerToken().getToken() + "\"); }");
 		}
 	}
 
 	private static void processMatchedConcept(String token, Hashtable tokTable, PrintWriter pw)
 	{
 			// Increment match count of the matched concept
 		Count cnt = (Count)tokTable.get(token);
 		if (cnt == null)
 			tokTable.put(token, new Count(1));
 		else
 			cnt.increment();
 
 			// Output the concept to the token file for debugging purposes
 		if (pw != null)
 			pw.println(token);
 	}
 
 // ############### NON-STATIC FIELDS AND METHODS ############
 /** Note that this class extends Category and inherits many fields and methods **/
 	private   Long    	_key;
 	protected String 		_name;
 	private   User    	_user;
 	private   boolean 	_private;
 	private   boolean 	_frozen;
 	private   boolean 	_validated;
 	private   Map     	_catMap;
 	private   Collection<Source>  _sources;		// Sources monitored for this topic
 	protected List<Category> 		_topLevelCats; // Top-level categories
 	private   Date    	_lastUpdateTime; 	// Date when new items were last added to this category
 	private   int     	_numArticles;		// Number of articles in this issue
 	private   OutputFeed _outputFeed;		// Output rss feed for this issue
 	private   int        _numItemsSinceLastDownload;	// Number of new items since last download
 	private   String     _taxonomyPath;		// Unique global taxonomy path
 
 	/* These are transient and need not be persisted. */
 				 private Set<Concept> _usedConcepts;		// Concepts used by all filters in this topic
 	transient private Method       _lexerScanMethod;	// Method that will be invoked to scan news articles
 	transient private Method       _lexerResetMethod;	// Method that will be invoked to reset the lexer
 	transient private Constructor  _lexerConstr;			// Constructor for the lexer class
 	transient private Object       _lexer;					// The lexer object for this class
 
 	public Issue() { }
 
 	private void init(String name, boolean pFlag, boolean vFlag, boolean fFlag)
 	{
 		_key = null;
 		_name = name;
 		_catMap   = new HashMap();
 		_private = pFlag;
 		_validated = vFlag;
 		_frozen = fFlag;
 	}
 
 	public Issue(String name, User u) throws Exception
 	{
 		super();
 		init(name, false, false, false);
 		_user = u;
 		try {
 			StringUtils.validateName("issue", _name); // Verify that the name is an acceptable news rack name
 		}
 		catch (Exception e) {
 			throw e;
 		}
 	}
 
 	public Issue(String name, boolean pFlag, boolean vFlag, boolean fFlag)
 	{
 		super();
 		init(name, pFlag, vFlag, fFlag);
 	}
 
    public void    setKey(Long k)  { _key = k; }
    public Long    getKey() 	    { return _key; }
 	public User    getUser()       { return _user; }
 	public void    setUser(User u) { _user = u; }
 	public String  getName()       { return _name; }
 	public Long    getUserKey()    { return _user.getKey(); }
 	public boolean isPrivate()     { return _private; }
 	public boolean isValidated()   { return _validated; }
 
 	/**
 	 * Checks if downloading is frozen for this issue 
 	 * If an issue is frozen, no fresh news is downloaded,
 	 * but old news continues to be available for browse.
 	 */
 	public boolean isFrozen() { return _frozen; }
 
 	/**
 	 * Freezes this issue.
 	 * If an issue is frozen, no fresh news is downloaded,
 	 * but old news continues to be available for browse.
 	 */
 	public void    freeze()   { _frozen = true; _db.updateTopic(this); }
 
 	/**
 	 * Unfreezes this issue.
 	 * If an issue is frozen, no fresh news is downloaded,
 	 * but old news continues to be available for browse.
 	 */
 	public void    unfreeze() { _frozen = false; _db.updateTopic(this); }
 
 	public void resetMaxNewsID() { _db.resetMaxNewsIdForIssue(this); }
 
 	/**
 	 * Return classified news for this issue
 	 * @param start       starting date (in yyyy.mm.dd format)
 	 * @param end         end date      (in yyyy.mm.dd format)
 	 * @param src         the news source from which we need the news (can be null)
 	 * @param startIndex  starting article
 	 * @param numArts     number of articles to fetch
 	 */
 /**
 	public List<NewsItem> getNews(Date start, Date end, Source src, int startIndex, int numArts) 
 	{ 
 		return _db.getNews(this, start, end, src, startIndex, numArts); 
 	}
 **/
 
 	/** Gets the sources monitored by this issue */
 	public Collection<Source> getMonitoredSources() { return (_sources == null) ? null : _sources; }
 
 	public void addSources(Collection<Source> srcs) { _sources = srcs; }
 
 	public Source getSourceByTag(String tag) { return _db.getSource(this, tag); }
 
 	/**
 	 * Gets a category, given its id.
 	 *
 	 * @param catID  ID of the category
 	 */
 	public Category getCategory(int catID)
 	{
 		return (Category)_catMap.get(new Integer(catID));
 	}
 
 	/**
 	 * Gets the top-level categories of this issue
 	 */
 	public Collection<Category> getCategories()
 	{
 		return (_topLevelCats == null) ? EMPTY_LIST : _topLevelCats;
 	}
 
 	public void setCategories(Collection<Category> cats)
 	{
 		_topLevelCats = new ArrayList<Category>();
 		_topLevelCats.addAll(cats);
    
          // Set up a cat-id <--> category map
          // Issue nodes always have id 0
       _catMap.put(new Integer(0), this);
       LinkedList<Category> l = new LinkedList<Category>();
       l.addAll(_topLevelCats);
       while (!l.isEmpty()) {
          Category c = l.removeFirst();
          _catMap.put(new Integer(c.getCatId()), c);
          l.addAll(c.getChildren());
       }
 	}
 
 	public void addCategories(Collection<Category> cats)
 	{
 			// Set this field before anything else!
 		setCategories(cats);
 
 			// Initialize paths
 		_taxonomyPath = _user.getUid() + File.separator + StringUtils.getOSFriendlyName(_name);
 		for (Category c: getCategories())
 			c.setupForDownloading(this);
 
 			// Read, update, and output a cat-id <--> category map
 			// Issue nodes always have id 0
 		Integer iid = new Integer(0);
 		_catMap.put(this, iid);
 		_catMap.put(iid, this);
 
 		int catId = readInExistingCatMap();
 		for (Category c:getCategories())
       	catId = c.updateCatMap(catId, _catMap);
 
 		if (!NewsRack.testing())
 			outputCatMap();
 	}
 
 	private int parseChildren(String nodeName, Collection<Category> childCats, int maxID, Node n)
 	{
 		// Process nested categories!
 		Iterator children = ParseUtils.getChildrenByTagName(n, "cat");
 		while (children.hasNext()) {
 			Node   x     = (Node)children.next();
 			String xName = ((Element)x).getAttribute("name");
 			try {
 				Category c = Category.getCategory(childCats, xName);
 				if (c != null) {
 					String xId = ((Element)x).getAttribute("id");
 					int    cId = Integer.parseInt(xId);
 					if (_log.isDebugEnabled()) _log.debug("CAT Name = " + xName + "; id = " + xId);
 						// Add a mapping from category -> catID
 					_catMap.put(c, new Integer(cId));
 					if (maxID < cId)
 						maxID = cId;
 					maxID = parseChildren(c.getName(), c.getChildren(), maxID, x);
 				}
 			}
 			catch (Exception e) {
 				_log.error("Exception while parsing cat map for node " + nodeName + " for cat " + xName, e);
 			}
 		}
 
 		return maxID;
 	}
 
 	public void setTaxonomyPath(String p) { _taxonomyPath = p; }
 
 	/** Return the fully qualified category path for the issue */
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
 			if (_log.isErrorEnabled()) _log.error("NULL _lastUpdateTime for " + getName());
 			return false;
 		}
 
 		final long now = System.currentTimeMillis();
 		final long lut = _lastUpdateTime.getTime();
 		return ((now - lut) < (numHours * 3600 * 1000)) ? true : false;
 	}
 
 	private int readInExistingCatMap()
 	{
 			// This is necessary so that once a category ID has been assigned, it is *for life*
 			// This ensures that if a URL has "leaked out" to the world that has a category ID in it,
 			// the id does not change in the face of change in categories and the profile ...
 		String dir = _user.getWorkDir();
 		String osFriendlyName = StringUtils.getOSFriendlyName(getName());
 		String catMapFile = dir + osFriendlyName + "_ISSUE_CAT_MAP.xml";
 		if ((new File(catMapFile)).exists()) {
 			try {
 				return parseChildren(getName(), getCategories(), 0, ParseUtils.getParsedDocument(catMapFile, false).getDocumentElement());  
 			}
 			catch (Exception e) {
 				_log.error("Error parsing cat map file " + catMapFile, e);
 				return 0;
 			}
 		}
 		else {
 			return 0;
 		}
 	}
 
 	private void outputCatMap(Collection<Category> children, PrintWriter pw, StringBuffer sb)
 	{
 		String       s   = sb.toString();
 		StringBuffer nsb = new StringBuffer(s + "\t");
 		for (Category x: children) {
 			if (x.isLeafCategory()) {
 				pw.println(s + "<cat name=\"" + x._name + "\" id=\"" + x.getCatId() + "\" />");
 			}
 			else {
 				pw.println(s + "<cat name=\"" + x._name + "\" id=\"" + x.getCatId() + "\">");
 				outputCatMap(x.getChildren(), pw, nsb);
 				pw.println(s + "</cat>");
 			}
 		}
 	}
 
 	private void outputCatMap()
 	{
 		String dir = _user.getWorkDir();
 		String osFriendlyName = StringUtils.getOSFriendlyName(getName());
 		String catMapFile = dir + osFriendlyName + "_ISSUE_CAT_MAP.xml";
 		try {
 			PrintWriter pw = IOUtils.getUTF8Writer(catMapFile);
 			pw.println("<issue>");
 			outputCatMap(this.getCategories(), pw, new StringBuffer("\t"));
 			pw.println("</issue>");
 			pw.close();
 		}
 		catch (Exception e) {
 			_log.error("Exception while outputing cat map " + catMapFile, e);
 		}
 	}
 
 	/**
 	 * Initializes the issue
 	 */
 	public void initialize() 
 	{ 
 		resetMaxNewsID();
 		_validated = true;
 	}
 
 		// Invalidate the issue
 	public void invalidate()
 	{
 		resetMaxNewsID();
 			// Exceptions might be raised if there were parsing errors
 		try { invalidateRSSFeed(); } catch (Exception e) { }
 	}
 
 	/**
 	 * This method downloads news (for every referenced news source) and returns the downloaded news items.
 	 */
 	public Collection downloadNews()
 	{
 			// Download all news
 		if (_log.isInfoEnabled()) _log.info("-- ISSUE: " + _name + " START DOWNLOADING NEWS --");
 		List l = new ArrayList();
 		for (Source s: getMonitoredSources()) {
 			try {
 				l.addAll(s.read());
 			}
 			catch (Exception e) {
 				_log.error("Error downloading news for source " + s.getFeed().getTag(), e);
 			}
 		}
 		if (_log.isInfoEnabled()) _log.info("-- DONE DOWNLOADING NEWS --");
 
 		return l;
 	}
 
 	/**
 	 * Output the taxonomy in XML
 	 **/
 /**
 	public String getTaxonomy()
 	{
 		StringBuffer sb = new StringBuffer();
 		sb.append("<issue>\n");
 		sb.append("<name>" + _name + "</name>\n");
 		for (Category c: getCategories())
 			sb.append(c.getTaxonomy());
 		sb.append("</issue>\n");
 		return sb.toString();
 	}
 **/
 
 	public String toString()
 	{
 		StringBuffer sb = new StringBuffer("ISSUE(");
 		sb.append(_name);
 		sb.append(", ");
 		sb.append(_private);
 		sb.append(")\n");
 		sb.append("---- categories for this issue ----\n");
 		for (Category c: getCategories())
 			sb.append(c);
 		sb.append("------------------------------------\n");
 
 		return sb.toString();
 	}
 
 	private String getScannerDir()
 	{
 		return _user.getWorkDir();
 	}
 
 	private Iterator<Concept> getUsedConcepts()
 	{
 		if (_usedConcepts == null) {
 			_usedConcepts = new HashSet<Concept>();
 			for (Category c: getCategories())
 				c.collectUsedConcepts(_usedConcepts);
 		}
 
 		return _usedConcepts.iterator(); 
 	}
 
 	/**
 	 * Generate regular expressions for a JFLEX based lexical scanner.
 	 * These regular expressions recognize concepts that have been
 	 * used in this issue.
 	 */
 	public void gen_JFLEX_RegExps()
 	{
 		PrintWriter pw = null;
 		String      scannerClassName = StringUtils.getJavaAndOSFriendlyName(getName());
 		String      fn = getScannerDir() + scannerClassName + ".keywords.jflex";
 		try {
 			pw = IOUtils.getUTF8Writer(fn);
 		}
 		catch (java.io.IOException e) {
 			_log.error("While trying to create jflex file " + fn + ", caught exception ", e);
 			return;
 		}
 		pw.println("%%\n");
 		pw.println("%pack");
 		pw.println("%unicode");
 		pw.println("%caseless");
 		pw.println("%public");
 		pw.println("%class " + scannerClassName + ISSUE_SCANNER_SUFFIX);
 		pw.println("%type " + JFLEX_SCANNER_TOKEN_CLASS);
 		pw.println("");
 			// Output the predefined macros
 		int n = PREDEF_KWORDS.length;
 		for (int i = 0; i < n; i += 2) {
 			pw.println(PREDEF_KWORDS[i] + " = " + PREDEF_KWORDS[i+1]);
 		}
 		pw.println("");
 		pw.println("%{");
 		pw.println("\tpublic " + JFLEX_SCANNER_TOKEN_CLASS + " token(String concept) { return new " + JFLEX_SCANNER_TOKEN_CLASS + "(concept, yytext()); }");
 		pw.println("\tpublic " + JFLEX_SCANNER_TOKEN_CLASS + " token(String[] cs) { return new " + JFLEX_SCANNER_TOKEN_CLASS + "(cs, yytext()); }");
 		pw.println("%}");
 		pw.println("\n%state ART_TEXT\n");
 		pw.println("%%");
 		pw.println("<YYINITIAL> {");
 		pw.println("/* Ignore everything till the article headline or article body is available */");
 		pw.println("\"<" + newsrack.archiver.HTMLFilter.getHTMLTagSignallingEndOfPreamble() + ">\" { yybegin(ART_TEXT); }");
 		pw.println("\"<pre>\" { yybegin(ART_TEXT); }");
 		pw.println("{WORD} | \\n | . { }");
 		pw.println("}\n");
 		pw.println("<ART_TEXT> {");
 		pw.println("\"</pre>\" { yybegin(YYINITIAL); }");
 
 			// Set up concept tokens!
 		HashMap<String, Concept> tokenMap = new HashMap<String, Concept>();
 		for (Iterator e = getUsedConcepts(); e.hasNext(); ) {
 			Concept c = (Concept)e.next();
 			Concept x = tokenMap.get(c.getName());
 				// No conflict!
 			if (x == null) {
 				tokenMap.put(c.getName(), c);
 
 					// IMPORTANT: If the lexer token is already set, don't reset it!
 					// Some other issue might have already set it to be a qualified name!
 				if (c.getLexerToken() == null) {
 					c.setLexerToken(new ConceptToken(c.getName()));
 					_db.updateConceptLexerToken(c);
 				}
 			}
 				// Conflict!!  Qualify with collection name ... conflicts are expected to be rare
 			else {
 				String xToken = x.getCollection().getName() + ":" + x.getName();
 				x.setLexerToken(new ConceptToken(xToken));
 				_db.updateConceptLexerToken(x);
 				tokenMap.put(xToken, x);
 
 				String cToken = c.getCollection().getName() + ":" + c.getName();
 				c.setLexerToken(new ConceptToken(cToken));
 				_db.updateConceptLexerToken(c);
 				tokenMap.put(cToken, c);
 			}
 		}
 
 			// Identify all keywords
 		Hashtable keywords = new Hashtable();
 		for (Iterator e = getUsedConcepts(); e.hasNext(); ) {
 			Concept  c  = (Concept)e.next();
 			if (_log.isDebugEnabled()) _log.debug("GJRE: Generating keywords for " + c.getName());
 			Iterator it = c.getKeywords();
 			while (it.hasNext()) {
 				Object k = it.next();
 				if (_log.isDebugEnabled()) _log.debug(" .... added " + k);
 					// Add to the multitoken keywords table
 				HashSet hs = (HashSet)keywords.get(k);
 				if (hs == null) {
 					hs = new HashSet();
 					keywords.put(k, hs);
 				}
 				hs.add(c);
 			}
 		}
 
 			// Identify keywords that can trigger recognition of
 			// multiple concepts.  For example, if a keyword "india" is
 			// present in multiple concepts, when "india" is seen in the text,
 			// all the relevant concepts should be triggered.  Similarly if
 			// "water privatisation" is present in one concept and "water" is
 			// present in another concept, when "water privatisation" is seen
 			// in the text, both the concepts should be triggered.
 		for (Enumeration e = keywords.keys(); e.hasMoreElements(); )
 			identifyMultiTokens(keywords, (String)e.nextElement());
 
 			// Spit out regular tokens for concepts
 		for (Iterator e = getUsedConcepts(); e.hasNext(); )
 			gen_JFLEX_RegExps(((Concept)e.next()), pw, keywords);
 
 			// Spit out multi-tokens
 		for (Enumeration e = keywords.keys(); e.hasMoreElements(); ) {
 			String  kw = (String)e.nextElement();
 			HashSet hs = (HashSet)keywords.get(kw);
 			if (hs.size() > 1)
 				genMultiToken_JFLEX_RegExp(pw, kw, hs);
 		}
 
 // If "rain-water" is seen in the text, allow for "rain" and "water
 // to be matched separately -- to allow this, I am getting rid of
 // the HYPHENWORD rule in the jflex specification.
 //		pw.println("{HYPHENWORD} | {DOTWORD} | {WORD} | \\n | .");
 
 		pw.println("{WORD} | \\n | .");
 		pw.println("\t\t{ return " + JFLEX_SCANNER_TOKEN_CLASS + ".CATCHALL_TOKEN; }");
 		pw.println("}");
 		pw.close();
 
 			// Clear them out and free up space!
 		_usedConcepts = null;
 	}
 
 	private void loadScannerClass(String workDir)
 	{
 		try {
 			ClassLoader cl               = getClass().getClassLoader();
 			String      scannerClassName = StringUtils.getJavaAndOSFriendlyName(getName()) + ISSUE_SCANNER_SUFFIX;
 			Class       scannerClass     = null;
 			if (workDir != null) {
 				URL[] us = new URL[1];
 				us[0] = new URL("file:" + workDir);
 				scannerClass = (new URLClassLoader(us, cl)).loadClass(scannerClassName);
 			}
 			else {
 				scannerClass = Class.forName(scannerClassName);
 			}
 			if (scannerClass == null) {
 				StringUtils.error("COULD NOT LOAD scanner class " + scannerClassName + " for issue " + _name);
 				return;
 			}
 			Class[] methodArgTypes = new Class[1];
 			methodArgTypes[0] = Class.forName("java.io.Reader");
 			_lexerScanMethod  = scannerClass.getMethod("yylex", (java.lang.Class[])null);
 			_lexerResetMethod = scannerClass.getMethod("yyreset", methodArgTypes);
 
 			Class[] constrTypes = new Class[1];
 			constrTypes[0] = Class.forName("java.io.Reader");
 			_lexerConstr   = scannerClass.getConstructor(constrTypes);
 		}
 		catch (Exception e) {
 			_log.error("Exception loading scanner class", e);
 			return;
 		}
 	}
 
 	/**
 	 * This method runs javacc/jflex on the scanner files to generate
 	 * a Java scanner class, and compiles the Java file to generated
 	 * the scanner class file.
 	 */
 	public void compileScanners(String workDir)
 	{
 		try {
 				// Run jflex on the generated ".jflex" scanner files
 //			String args[] = new String[1];
 //			args[0] = GetScannerDir() + GetJavaAndOSFriendlyName() + ".keywords.jflex";
 //			JFlex.Main.main(args);
 			String scannerClassName = StringUtils.getJavaAndOSFriendlyName(getName());
 			String jflexFileName = getScannerDir() + scannerClassName + ".keywords.jflex";
 			File   jflexFile     = new File(jflexFileName);
 			Reader r             = IOUtils.getUTF8Reader(jflexFileName);
 			JFlex.Main.generate(r, jflexFile);
 
 		// FIXME: Does this assume that the underlying system is a Unix system??
 				// Compile the generated Java scanner files
 			String args[];
 			String webappPath = NewsRack.getWebappPath();
 			if (webappPath != null) {
 					// Servlet based execution
 				args = new String[4];
 				args[0] = NewsRack.getProperty("java.compiler");
 				args[1] = "-classpath";
 				args[2] = webappPath + File.separator + "WEB-INF" + File.separator + "classes";
 				args[3] = getScannerDir() + scannerClassName + "_IssueScanner.java";
 			}
 			else {
 					// Non-servlet command-line execution
 				args = new String[2];
 				args[0] = NewsRack.getProperty("java.compiler");
 				args[1] = getScannerDir() + scannerClassName + "_IssueScanner.java";
 				if (_log.isInfoEnabled()) _log.info("args[0] - " + args[0]);
 				if (_log.isInfoEnabled()) _log.info("args[1] - " + args[1]);
 			}
 			Process p = java.lang.Runtime.getRuntime().exec(args);
 			(new ProcessReader("stdout", args[0], p.getInputStream(), System.out)).start();
 			(new ProcessReader("stderr", args[0], p.getErrorStream(), System.err)).start();
 			if (p.waitFor() != 0)
 				_log.error(args[0] + ": Got a non-zero exit status!");
 		}
 		catch (Exception e) {
 			_log.error("Exception while generating scanners", e);
 		}
 
 		loadScannerClass(workDir);
 	}
 
 	/**
 	 * This method classifies a news item based on tokens that have
 	 * been identified by the scanner.
 	 * @param ni        the news item to be classified
 	 * @param numTokens number of tokens encountered
 	 * @param tokTable  the table of recognized tokens/concepts
 	 */
 	public void classifyArticle(NewsItem ni, int numTokens, Hashtable tokTable)
 	{
 		int matchCount = 0;
 		ArrayList<Category> matchedCats = new ArrayList<Category>();
 		for (Category c: getCategories()) {
 			Count mv = (Count)tokTable.get("[" + c.getName() + "]");
 				// The category might have been processed while processing another category
 				// Ex: because of a rule like this: [Slums and Courts] = [Slums] and Courts
 				// [Slums and Courts] references the [Slums] category and so, while processing
 				// this cat, the [Slums] cat might have been processed.
 				// If so, avoid duplicate processing.
 			if (mv == null)
 				mv = c.getMatchCount(ni, numTokens, tokTable);
 			else
 				if (_log.isDebugEnabled()) _log.debug("CAT " + c.getName() + " in issue " + getName() + " has already been processed!");
 			if (mv.value() > matchCount)
 				matchCount = mv.value();
 			matchedCats.addAll(mv.getMatchedCats());
 		}
 
 		if (!matchedCats.isEmpty()) {
 			_outputFeed.addNewsItem(ni, matchedCats); 	// Add the news item to the RSS feed 
 			_lastUpdateTime = new Date(); 					// Set last update time
 		}
 	}
 
 	/**
 	 * Fetch the news item that is stored in 'newsItemFileName', examine the tokens
 	 * in 'tokTable', and classify the news item accordingly.
 	 */
 	private void classifyArticle(String newsItemFileName, Hashtable tokTable, Hashtable newsTable, List allArts, List unclassifiedArts)
 	{
 		NewsItem ni = (NewsItem)newsTable.get(newsItemFileName);
 		if (ni == null) {
 			_log.error("Did not find news item for " + newsItemFileName);
 		}
 		else {
 			allArts.add(ni);
 				// FIXME: 250 is arbitrary!
 			classifyArticle(ni, 250, tokTable);
 			if (ni.getCategories().size() == 0)
 				unclassifiedArts.add(ni);
 		}
 	}
 
 	private void initScanner(Reader r, String workDir) throws Exception
 	{
 		Object[] args = new Object[1];
 		args[0] = r;
 		if (_lexer == null) {
 			// Load the scanner class, if necessary
 			if (_lexerConstr == null)
 				loadScannerClass(workDir);
 			_lexer = _lexerConstr.newInstance(args);
 		}
 		else {
 			_lexerResetMethod.invoke(_lexer, args);
 		}
 	}
 
 	private int scanNewsItem(PrintWriter pw, Hashtable tokTable) throws Exception
 	{
 		int numTokens = 0;
 
 			// MAIN SCANNER LOOP
 		while (true) {
 			ConceptToken tok = (ConceptToken)_lexerScanMethod.invoke(_lexer, (java.lang.Object [])null);
 			if (tok == null) {
 				break;
 			}
 
 				// Process token
 			numTokens++;
 			if (tok != ConceptToken.CATCHALL_TOKEN) {
 				if (tok.isMultiToken()) {
 					if (_log.isDebugEnabled()) _log.debug("MULTI token " + tok.getToken());
 					String[] toks = tok.getTokens();
 					for (int i = 0; i < toks.length; i++) {
 						processMatchedConcept(toks[i], tokTable, pw);
 					}
 				}
 				else {
 					processMatchedConcept(tok.getToken(), tokTable, pw);
 				}
 			}
 		}
 
 		return numTokens;
 	}
 
 	/**
 	 * This method scans a set of news items to identify concepts
 	 * relevant to this issue and classifies them ...
 	 * For purposes of debugging, it also generates a token file, which
 	 * has the following format:
 	 * <pre>
 	 * ISSUE: name              | ISSUE: narmada
 	 * FILE: article_1          | FILE: 20.9.2004/rediff/filtered/20dam.htm
 	 * concept1                 | dam
 	 * concept2                 | narmada
 	 * ... other tokens ...     | ... other tokens ...
 	 * FILE: article_2          | FILE: 20.9.2004/rediff/filtered/20ssp.htm
 	 * concept1                 | ssp
 	 * concept2                 | dam
 	 * ... other tokens ...     | ... other tokens ...
 	 * ....                     | ....
 	 * FILE: article_n          | FILE: 20.9.2004/rediff/filtered/20pm.htm
 	 * tokens for article_n     | india
 	 * ....                     | ....
 	 * </pre>
 	 *
 	 * @param NewsItems The collection of news items that have to be scanned and classified
 	 */
 	public void scanAndClassifyNewsItems(Feed f, Collection<NewsItem> newsItems)
 	{
 		if (f != null)
 			scanAndClassifyNewsItems(f, newsItems, true);
 		else
 			scanAndClassifyNewsItems(f, newsItems, false);
 
 		if (NewsRack.inDebugMode())
 			_db.printStats();
 	}
 
 	private void scanAndClassifyNewsItems(Feed f, Collection<NewsItem> newsItems, boolean skipProcessed)
 	{
 		if (_log.isDebugEnabled()) _log.debug("... request to scan and classify for " + getName() + " for feed " + ((f == null) ? null: f._feedName));
 
 		Long maxNewsId = (long)0;
 		try {
 			PrintWriter pw = null;
 			String      fn = _user.getWorkDir() + StringUtils.getOSFriendlyName(getName()) + ".tokens";
 			try {
 				if (_log.isDebugEnabled()) _log.debug("Looking for utf8 writer for " + fn);
 				pw = IOUtils.getUTF8Writer(fn);
 			}
 			catch (java.io.IOException e) {
 				_log.error("While trying to create tokens file " + fn + ", caught exception ", e);
 			}
 
 			if (pw != null)
 				pw.println("ISSUE: " + _name);
 
 			User u = _user;
 			for (NewsItem ni: newsItems) {
 				if (skipProcessed) {
 					if (ni == null) {
 						_log.error("ERROR: Got null news item for feed: " + f.getTag());
 						continue;
 					}
 					else {
 						// Keep track of the max news id
 						long niKey = ni.getKey();
 						if (maxNewsId < niKey)
 							maxNewsId = niKey;
 					}
 				}
 
 					// Ignore if it has already been processed!
 				if (skipProcessed && _db.newsItemHasBeenProcessedForIssue(ni, this)) {
 					if (_log.isDebugEnabled()) _log.debug("Ignoring news id " + ni.getKey() + " with title " + ni.getTitle());
 					continue;
 				}
 
 				if (pw != null)
 					pw.println("FILE: " + ni.getRelativeFilePath());
 
 				Reader r = null;
 				try {
 					Hashtable tokTable = new Hashtable();
 					r = ni.getReader();
 					initScanner(r, _user.getWorkDir());
 					int numTokens = scanNewsItem(pw, tokTable);
 					classifyArticle(ni, numTokens, tokTable);
 				}
 				catch (java.io.FileNotFoundException e) {
					_log.error("ScanAndClassify: FNFE: key - " + ni.getKey() + " Expected: " + ni.getFilteredFilePath());	// Don't print the stack trace
 				}
 				catch (java.io.IOException e) {
 					_log.error("IO ERROR ", e);
 				}
 				finally {
 					if (r != null) r.close();
 				}
 
 				if (pw != null)
 					pw.flush();
 			}
 
 			if (pw != null)
 				pw.close();
 		}
 		catch (Exception e) {
 			_log.error("Exception scanning/classifying .. ", e);
 			return;
 		}
 
 		if (skipProcessed)
 			_db.updateMaxNewsIdForIssue(this, f, maxNewsId);
 	}
 
 	public void storeNewsToArchive()
 	{
 		_db.commitNewsToArchive(this);
 	}
 
 	/**
 	 * This method clears all previously categorized news
 	 */
 	public void clearNews()
 	{
 		for (Category c: getCategories())
 			c.clearNews();
 	}
 
 	public void reclassifyNews(Source s, boolean allDates, Date sd, Date ed)
 	{
 		if (_log.isInfoEnabled()) _log.info("### Getting index files for " + s.getFeed().getTag() + " ###");
 		Iterator newsIndexes = (allDates) ? _db.getIndexesOfAllArchivedNews(s)
 													 : _db.getIndexesOfAllArchivedNews(s, sd, ed);
 		while (newsIndexes.hasNext()) {
 			NewsIndex newsIndex = (NewsIndex)newsIndexes.next();
 			if (_log.isInfoEnabled()) _log.info("--> Classifying news for date: " + newsIndex.getCreationTime() + " for feed: " + s.getFeed().getTag());
 				// Ignore the already processed flag for news items
 			scanAndClassifyNewsItems(s.getFeed(), _db.getArchivedNews(newsIndex), false);
 		}
 		if (_log.isInfoEnabled()) _log.info("--> Sorting and storing news for " + s.getFeed().getTag());
 		updateRSSFeed();
 		storeNewsToArchive();
 	}
 
 	public String getRSSDir() { return NewsRack.getBaseRssDir() + _taxonomyPath + File.separator; }
 
 	public String getRSSFeedURL() {
 		return NewsRack.getServerURL() + File.separator
 						+ NewsRack.getDirPathProperty("rssDir")
 		            + _taxonomyPath + File.separator + NewsRack.getProperty("rssfeedName");
 	}
 
 	private void initFeed()
 	{
 		if (_log.isDebugEnabled()) _log.debug("Initializing feed for issue: " + getName());
 
 		String feedName  = getRSSDir() + NewsRack.getProperty("rssfeedName");
 		String feedTitle = "NewsRack: " + _taxonomyPath;
 		String feedDesc  = "This is a custom RSS feed generated by NewsRack for user " + getUser().getUid() + " for topic " + _name;
 		_outputFeed = new OutputFeed(feedName, getRSSDir(), _name, getRSSFeedURL(), feedTitle, feedDesc, _taxonomyPath);
 		_numItemsSinceLastDownload = 0;
 	}
 
 	public void readInCurrentRSSFeed()
 	{
 		if (_outputFeed == null) 
 			initFeed();
 
 			// Read in current RSS feed so that new items can be added to it
 		try {
 			if (_log.isDebugEnabled()) _log.debug("Reading in current feed for issue: " + getName());
 			_outputFeed.readInCurrentRSSFeed();
 		}
 		catch (Exception e) {
 			_log.error("Caught exception reading current feed for topic: " + getName(), e);
 		}
 
 			// Process children
 		for (Category c: getCategories())
 			c.readInCurrentRSSFeed();
 	}
 
 	public void freeRSSFeed()
 	{
 			// Free up space
 		_outputFeed = null;
 
 			// Process children
 		for (Category c: getCategories())
 			c.freeRSSFeed();
 	}
 
 	public void invalidateRSSFeed()
 	{
 		if (_outputFeed == null) 
 			initFeed();
 
 			// Reset the feed
 		_outputFeed.invalidate();
 		_numItemsSinceLastDownload = 0;
 
 			// Reset feed for all nested categories
 		for (Category c: getCategories())
 			c.invalidateRSSFeed();
 	}
 
 	public void updateRSSFeed()
 	{
 		_outputFeed.update();
 		_numItemsSinceLastDownload = _outputFeed.getNumItemsSinceLastDownload();
 
 		for (Category c: getCategories())
 			c.updateRSSFeed();
 	}
 
 	public static void main(String[] args)
 	{
 		if (args.length == 2) {
 			try {
 				Issue  i = new Issue(args[0], false, true, false);
 				Reader r = new java.io.FileReader(args[1]);
 				PrintWriter pw = new PrintWriter(args[0] + ".tokens");
 				i.initScanner(r, null);
 				i.scanNewsItem(pw, new Hashtable());
 				r.close();
 				pw.close();
 			}
 			catch (Exception e) {
 				System.err.println("Exception" + e);
 				e.printStackTrace();
 			}
 		}
 		else {
 			System.out.println("Usage: java newsrack.filter.Issue <issue-name> <file-to-filter>\n  It is assumed that the jflex scanner class has been compiled and is available in the classpath");
 		}
 	}
 }
