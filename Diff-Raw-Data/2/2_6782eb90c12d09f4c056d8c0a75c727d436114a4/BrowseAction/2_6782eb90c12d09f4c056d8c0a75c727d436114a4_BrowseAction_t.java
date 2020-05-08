 package newsrack.web;
 
 import newsrack.GlobalConstants;
 import newsrack.filter.Category;
 import newsrack.filter.Issue;
 import newsrack.archiver.Source;
 import newsrack.user.User;
 import newsrack.archiver.DownloadNewsTask;
 import newsrack.database.NewsItem;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.ArrayList;
 import java.io.IOException;
 
 import com.opensymphony.xwork2.Action;
 import com.opensymphony.xwork2.ActionSupport;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * class <code>BrowseAction</code> implements the functionality
  * browsing through news archives of a particular user as well
  * as other public archives
  */
 public class BrowseAction extends BaseAction
 {
    private static final ThreadLocal<SimpleDateFormat> DATE_PARSER = new ThreadLocal<SimpleDateFormat>() {
 		protected SimpleDateFormat initialValue() { return new SimpleDateFormat("yyyy.MM.dd"); }
 	};
 
    private static final ThreadLocal<SimpleDateFormat> SDF = new ThreadLocal<SimpleDateFormat>() {
 		protected SimpleDateFormat initialValue() { return new SimpleDateFormat("MMM dd yyyy kk:mm z"); }
 	};
 
    private static final Log _log = LogFactory.getLog(BrowseAction.class); /* Logger for this action class */
 
 		// FIXME:  Pick some other view scheme than this!
 	private static Date        _lastUpdateTime = null;
    private static List<Issue> _updatesMostRecent = null;
    private static List<Issue> _updatesLast24Hrs = null;
    private static List<Issue> _updatesMoreThan24Hrs = null;
 
 	private static final int DEF_NUM_ARTS_PER_PAGE = 20;
 	private static final int MIN_NUM_ARTS_PER_PAGE = 5;
 	private static final int MAX_NUM_ARTS_PER_PAGE = 200;
 
 		// Caching!
    public static void setIssueUpdateLists()
    {
       List<Issue> l1 = new ArrayList<Issue>();
       List<Issue> l2 = new ArrayList<Issue>();
       List<Issue> l3 = new ArrayList<Issue>();
 
       List<Issue> issues = User.getAllValidatedIssues();
 		for (Issue i: issues) {
          int n = i.getNumItemsSinceLastDownload();
          if ((n > 0) && (_lastUpdateTime != null) && (i.getLastUpdateTime() != null) && i.getLastUpdateTime().after(_lastUpdateTime))
             l1.add(i);
          else if (i.updatedWithinLastNHours(24))
             l2.add(i);
          else
             l3.add(i);
       }
 
       _updatesMostRecent    = l1;
       _updatesLast24Hrs     = l2;
       _updatesMoreThan24Hrs = l3;
 		_lastUpdateTime       = new Date();
    }
 
 	private String _lastDownloadTime;
 
 		/* These 4 params are for the common browse case:
 		 * top-level browse; user browse; issue browse */
 	private User     _issueOwner;
 	private Issue    _issue;
 	private Category _cat;
 	private List<Category> _catAncestors;
 	private int      _numArts;
 	private int      _start;
 	private int      _count;
 
 		/* These 4 params are for the uncommon browse case:
 		 * for browsing news by source */
 	private Source   _src;
 	private String   _d;
 	private String   _m;
 	private String   _y;
 
 		/* News list to be displayed */
 	private Collection<NewsItem> _news;
 
 	public String getLastDownloadTime() { return _lastDownloadTime; }
 	public Date   getLastUpdateTime()   { return _lastUpdateTime; }
 	public int    getNumArts()          { return _numArts; }
 	public int    getStart()            { return _start; }
 	public int    getCount()            { return _count; }
 	public Collection<NewsItem> getNews() { return _news; }
 
 	public User getOwner() { return _issueOwner; } 
 	public Issue getIssue() { return _issue; } 
 	public Category getCat() { return _cat; } 
 	public List<Category> getCatAncestors() { return _catAncestors; }
 
 	public String getDate() { return _d; }
 	public String getMonth() { return _m; }
 	public String getYear() { return _y; }
 	public Source getSource() { return _src; }
 
 	public List<Issue> getMostRecentUpdates() { return _updatesMostRecent; }
 	public List<Issue> getLast24HourUpdates() { return _updatesLast24Hrs; }
 	public List<Issue> getOldestUpdates()     { return _updatesMoreThan24Hrs; }
 
    public String execute()
 	{
 		/* Do some error checking, fetch the issue, and the referenced category
 		 * and pass control to the news display routine */
 		Date ldt = DownloadNewsTask.getLastDownloadTime();
 		_lastDownloadTime = SDF.get().format(ldt);
 
 		String uid = getParam("owner");
 		if (uid == null) {
 				// No uid, -- send them to the top-level browse page!
 			if ((_updatesMostRecent == null) || _lastUpdateTime.before(ldt))
 				setIssueUpdateLists();
 
 			return "browse.main";
 		}
 		else {
 			_issueOwner = User.getUser(uid);
 			if (_issueOwner == null) {
 					// Bad uid given!  Send the user to the top-level browse page
 				_log.info("Browse: No user with uid: " + uid);
 				return "browse.main";
 			}
 
 			String issueName = getParam("issue");
 			if (issueName == null) {
 					// No issue parameter for the user.  Send them to a issue listing page for that user!
 				return "browse.user";
 			}
 
 			_issue = _issueOwner.getIssue(issueName);
 			if (_issue == null) {
 					// Bad issue-name parameter.  Send them to a issue listing page for that user!
 				_log.info("Browse: No issue with name: " + issueName + " defined for user: " + uid);
 				return "browse.user";
 			}
 
 			String catId = getParam("catID");
 			if (catId == null) {
 					// No cat specified -- browse the issue!
 				return "browse.issue";
 			}
 
 			_cat = _issue.getCategory(Integer.parseInt(catId));
 			if (_cat == null) {
 					// Bad category!  Send them to a listing page for the issue! 
 				_log.info("Browse: Category with id " + catId + " not defined in issue " + issueName + " for user: " + uid);
 				return "browse.issue";
 			}
 
 				// Set up the ancestor list for the category
 			Category c = _cat;
 			LinkedList<Category> ancestors = new LinkedList<Category>();
 			while (c != null) {
 				c = c.getParent();
 				if (c != null)
 					ancestors.addFirst(c);
 			}
 			_catAncestors = ancestors;
 
 				// Display news in the current category in the current issue
 			if (!_cat.isLeafCategory()) {
 				return "browse.cat";
 			}
 			else {
 				_numArts = _cat.getNumArticles(); 
 					// Start
 				String startVal = getParam("start");
 				if (startVal == null) {
 					_start = 0;
 				}
 				else {
					_start = Integer.parseInt(startVal)-1;
 					if (_start < 0)
 						_start = 0;
 					else if (_start > _numArts)
 						_start = _numArts;
 				}
 
 					// Count
 				String countVal = getParam("count");
 				if (countVal == null) {
 					_count = DEF_NUM_ARTS_PER_PAGE;
 				}
 				else {
 					_count = Integer.parseInt(countVal);
 					if (_count < MIN_NUM_ARTS_PER_PAGE)
 						_count = MIN_NUM_ARTS_PER_PAGE;
 					else if (_count > MAX_NUM_ARTS_PER_PAGE)
 						_count = MAX_NUM_ARTS_PER_PAGE;
 				}
 
 					// Filter by source
 			   String srcTag = getParam("source_tag");
 				Source src    = null;
 				if ((srcTag != null) && (srcTag != ""))
 					src = _issue.getSourceByTag(srcTag);
 
 				Date startDate = null;
 				String sdStr = getParam("start_date");
 				if (sdStr != null) {
 					try {
 						startDate = DATE_PARSER.get().parse(sdStr);
 					}
 					catch (Exception e) {
 						addActionError(getText("bad.date", sdStr));
 						_log.info("Error parsing date: " + sdStr + e);
 					}
 				}
 
 					// Filter by start & end dates
 				Date endDate = null;
 				String edStr = getParam("end_date");
 				if (edStr != null) {
 					try {
 						endDate = DATE_PARSER.get().parse(edStr);
 					}
 					catch (Exception e) {
 						addActionError(getText("bad.date", edStr));
 						_log.info("Error parsing date: " + edStr + e);
 					}
 				}
 
 				//_log.info("Browse: owner uid - " + uid + "; issue name - " + issueName + "; catID - " + catId + "; start - " + _start + "; count - " + _count + "; start - " + startDate + "; end - " + endDate + "; srcTag - " + srcTag + "; src - " + (src != null ? src.getKey() : null));
 
 					// Fetch news!
 				_news  = _cat.getNews(startDate, endDate, src, _start, _count);
 
 				return "browse.news";
 			}
 		}
 	}
 
 	public String browseSource()
 	{
 			// If there is no valid session, send them to the generic browse page!
 		if (_user == null) {
 			_log.error("Expired session!");
 			return "browse.main";
 		}
 
 			// Fetch source
 		String srcId = getParam("srcId");
 		if (srcId == null) {
 			_log.error("No source id provided!");
 			return Action.INPUT;
 		}
 
 		_src = _user.getSourceByTag(srcId);
 		if (_src == null) {
 			_log.error("Unknown source: " + srcId);
 			return Action.INPUT;
 		}
 
 		_d = getParam("d");
 		_m = getParam("m");
 		_y = getParam("y");
 		if ((_d == null) || (_m == null) && (_y == null)) {
 			_log.error("Bad date params: d- " + _d + ", m- " + _m + ", y- " + _y);
 			return Action.INPUT;
 		}
 
 			// Fetch news for the source for the requested date
 		_news = _src.getArchivedNews(_y, _m, _d);
 		if (_news == null)
 			_news = new ArrayList<NewsItem>();
 
 		return Action.SUCCESS;
 	}
 }
