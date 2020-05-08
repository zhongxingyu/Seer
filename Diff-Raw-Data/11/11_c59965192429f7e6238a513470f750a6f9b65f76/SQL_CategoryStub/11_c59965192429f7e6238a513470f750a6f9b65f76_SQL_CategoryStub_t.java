 package newsrack.database.sql;
 
 import java.util.Set;
 import java.util.List;
 import java.util.Iterator;
 import java.util.Hashtable;
 import java.util.Date;
 
 import newsrack.user.User;
 import newsrack.database.NewsItem;
 import newsrack.filter.Issue;
 import newsrack.filter.Filter;
 import newsrack.filter.Count;
 import newsrack.filter.Category;
 import newsrack.filter.Concept;
 
 class SQL_CategoryStub extends Category
 {
 	Long _userKey;		// User who defined this category
 	Long _filtKey;		// Filter for this category
 	Long _parentCat;	// Category key for the parent
 	Long _issueKey;	// Issue key that this cat belongs to
 
 	public SQL_CategoryStub(Long userKey, Long catKey, String name, int catId, Long parentCat, Long filtKey)
 	{
 		super(catKey, name, null, catId);
 		_userKey = userKey;
 		_filtKey = filtKey;
 		_parentCat = parentCat;
 	}
 
 	public boolean isTopLevelCategory() { return (getParent() == null); }
 
 	public boolean isLeafCategory() { return (getFilter() != null); }
 
 	protected void setIssueKey(Long k) { _issueKey = k; }
 
 	protected void setupForDownloading(Issue issue)
 	{
 		getFilter();
 		getParent();
 		super.setupForDownloading(issue);
 	}
 
 	protected void collectUsedConcepts(Set<Concept> usedConcepts)
 	{
 		getFilter();
 		getParent();
 		super.collectUsedConcepts(usedConcepts);
 	}
 
/**
 	public String getTaxonomy()
 	{
 		getFilter();
 		getParent();
 		return super.getTaxonomy();
 	}
**/
 
 	public User getUser()
 	{
 		User u = super.getUser();
 		if (u == null)
 			u = SQL_DB._sqldb.getUser(_userKey); 
 		else
 			_log.debug("_userKey is " + _userKey);
 		return u;
 	}
 
 	public Issue getIssue()
 	{
 		Issue i = super.getIssue();
 		if ((i == null) && (_issueKey != null) && (_issueKey != -1))
 			i = SQL_DB._sqldb.getIssue(_issueKey);
 		else
 			_log.debug("_issueKey is " + _issueKey);
 
 		return i;
 	}
 
 	public Filter getFilter()
 	{
 		if (_log.isDebugEnabled()) _log.debug("filter key for " + getName() + " is " + _filtKey);
 		if ((_filtKey == null) || (_filtKey == -1)) {
 			return null;
 		}
 		else {
 			Filter f = super.getFilter();
 			if (f == null) {
 				f = SQL_DB._sqldb.getFilter(_filtKey); 
 				super.setFilter(f);
 			}
 			return f;
 		}
 	}
 
 	public Category getParent()
 	{
 		if ((_parentCat == null) || (_parentCat == -1)) {
 			_log.debug("No parent!");
 			return null;
 		}
 		else {
 			Category p = super.getParent();
 			if (p == null) {
 				_log.debug("Fetching parent from db!");
 				p = SQL_DB._sqldb.getCategory(_parentCat);
 				super.setParent(p);
 			}
 			else {
 				_log.debug("Returning parent directly!");
 			}
 			return p;
 		}
 	}
 
 	public List<Category> getChildren()
 	{
 		List<Category> ch = super.getChildren();
 		if (!isLeafCategory() && ch.isEmpty()) {
 			List<Category> children = (List<Category>)SQL_Stmt.GET_NESTED_CATS.execute(new Object[]{getKey()});
 			setChildren(children);
 			for (Category c: children) {
 				c.setParent(this);
 				c.setIssue(this.getIssue());
 			}
 
 			ch = children;
 		}
 		return ch;
 	}
 
 	public synchronized Count getMatchCount(final NewsItem article, final int numTokens, final Hashtable matchCounts)
 	{
 		getFilter();
 		getParent();
 		return super.getMatchCount(article, numTokens, matchCounts);
 	}
 }
