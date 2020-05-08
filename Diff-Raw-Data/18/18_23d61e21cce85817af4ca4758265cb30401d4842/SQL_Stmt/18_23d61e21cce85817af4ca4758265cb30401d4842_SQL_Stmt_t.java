 package newsrack.database.sql;
 
 import static newsrack.database.sql.SQL_ColumnSize.CAT_TBL_NAME;
 import static newsrack.database.sql.SQL_ColumnSize.FEED_TBL_FEEDURL;
 import static newsrack.database.sql.SQL_ColumnSize.NEWS_ITEM_TBL_URLROOT;
 import static newsrack.database.sql.SQL_ColumnSize.NEWS_ITEM_TBL_URLTAIL;
 import static newsrack.database.sql.SQL_ColumnSize.NONE;
 import static newsrack.database.sql.SQL_ColumnSize.USER_TBL_EMAIL;
 import static newsrack.database.sql.SQL_ColumnSize.USER_TBL_NAME;
 import static newsrack.database.sql.SQL_ColumnSize.USER_TBL_PASSWORD;
 import static newsrack.database.sql.SQL_ColumnSize.USER_TBL_UID;
 import static newsrack.database.sql.SQL_ValType.BOOLEAN;
 import static newsrack.database.sql.SQL_ValType.DATE;
 import static newsrack.database.sql.SQL_ValType.INT;
 import static newsrack.database.sql.SQL_ValType.LONG;
 import static newsrack.database.sql.SQL_ValType.STRING;
 import static newsrack.database.sql.SQL_ValType.TIMESTAMP;
 
 import java.sql.Date;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import newsrack.archiver.Feed;
 import newsrack.database.DB_Interface;
 import newsrack.filter.Category;
 import newsrack.filter.Concept;
 import newsrack.filter.Filter;
 import newsrack.filter.NR_CategoryCollection;
 import newsrack.filter.NR_Collection;
 import newsrack.filter.NR_CollectionType;
 import newsrack.filter.NR_ConceptCollection;
 import newsrack.filter.NR_FilterCollection;
 import newsrack.filter.NR_SourceCollection;
 import newsrack.filter.PublicFile;
 import newsrack.filter.Filter.FilterOp;
 import newsrack.filter.Filter.RuleTerm;
 import newsrack.filter.Filter.ProximityTerm;
 import newsrack.filter.UserFile;
 import newsrack.user.User;
 import newsrack.util.Triple;
 import newsrack.util.Tuple;
 
 import org.apache.commons.logging.Log;
 
 class GetCollectionResultProcessor extends AbstractResultProcessor
 {
 	public ResultProcessor getNewInstance() { return new GetCollectionResultProcessor(); }
 
 	public Object processResultSet(ResultSet rs) throws java.sql.SQLException
 	{
 		return new Object[] { rs.getLong(1), rs.getLong(2), rs.getString(3), NR_CollectionType.getType(rs.getString(4)) };
 	}
 
 	public Object processOutput(Object o)
 	{
 			// Check that the query returned some result.
 		if (o == null)
 			return null;
 
 		Object[] rset    = (Object[])o;
 		Long     cKey    = (Long)rset[0];
 		Long     fileKey = (Long)rset[1];
 		String   name    = (String)rset[2];
 		NR_CollectionType cType = (NR_CollectionType)rset[3];
 
 		NR_Collection c = null;
 		switch (cType) {
 			case SOURCE :
 				c = new NR_SourceCollection(SQL_Stmt._db.getUserFile(fileKey), name, null);
 				c.setKey(cKey);
 				break;
 
 			case CONCEPT :
 				c = new NR_ConceptCollection(SQL_Stmt._db.getUserFile(fileKey), name, null);
 				c.setKey(cKey);
 				break;
 
 			case CATEGORY :
 				c = new NR_CategoryCollection(SQL_Stmt._db.getUserFile(fileKey), name, null);
 				c.setKey(cKey);
 				break;
 
 			case FILTER :
 				c = new NR_FilterCollection(SQL_Stmt._db.getUserFile(fileKey), name, null);
 				c.setKey(cKey);
 				break;
 
 			default:
 				SQL_Stmt._log.error("Unsupported collection type: " + cType);
 				break;
 		}
 		return c;
 	}
 }
 
 class GetNewsIndexResultProcessor extends AbstractResultProcessor
 {
 	public Object processResultSet(ResultSet rs) throws java.sql.SQLException { return new SQL_NewsIndex(rs.getLong(1), rs.getLong(2), rs.getDate(3)); }
 }
 
 class GetUserResultProcessor extends AbstractResultProcessor
 {
 	public Object processResultSet(ResultSet rs) throws SQLException
 	{
 		User u = new User(rs.getString(2), rs.getString(3), rs.getBoolean(6));
 		u.setKey(rs.getLong(1));
 		u.setName(rs.getString(4));
 		u.setEmail(rs.getString(5));
 		return u;
 	}
 }
 
 class GetUserFileResultProcessor extends AbstractResultProcessor
 {
 	public Object processResultSet(ResultSet rs) throws SQLException
 	{
 		return new Object[] { rs.getLong(1), rs.getLong(2), rs.getString(3) };
 	}
 
 	private UserFile buildUserFile(Object[] rset)
 	{
 		UserFile f = new UserFile(SQL_Stmt._db.getUser((Long)rset[1]), (String)rset[2]);
 		f.setKey((Long)rset[0]);
 		return f;
 	}
 
 	public Object processOutput(Object o)
 	{
 		return buildUserFile((Object[])o);
 	}
 
 	public List processOutputList(List l)
 	{
 		List ol = new ArrayList();
 		for (Object o: l)
 			ol.add(buildUserFile((Object[])o));
 
 		return ol;
 	}
 }
 
 class GetPublicFilesResultProcessor extends AbstractResultProcessor
 {
 	public Object processResultSet(ResultSet rs) throws SQLException { return new Tuple<String, Long>(rs.getString(1), rs.getLong(2)); }
 
 	public List processOutputList(List l)
 	{
 		List ol = new ArrayList();
 		for (Object o: l) {
 			Tuple<String, Long> t = (Tuple<String, Long>)o;
 			ol.add(new PublicFile(t._a, SQL_Stmt._db.getUser(t._b).getUid()));
 		}
 		return ol;
 	}
 }
 
 class GetIssueResultProcessor extends AbstractResultProcessor
 {
 	public Object processResultSet(ResultSet rs) throws SQLException
 	{
 		return new SQL_IssueStub(rs.getLong(1), rs.getLong(2), rs.getString(3), rs.getInt(4), rs.getTimestamp(5), rs.getBoolean(6), rs.getBoolean(7), rs.getBoolean(8), rs.getString(9), rs.getInt(10));
 	}
 }
 
 class GetConceptTupleResultProcessor extends AbstractResultProcessor
 {
 	public Object processResultSet(ResultSet rs) throws SQLException
 	{
 		return new Tuple<Long, Concept>(rs.getLong(1), new SQL_ConceptStub(rs.getLong(2), rs.getString(3), rs.getString(4), rs.getString(5)));
 	}
 }
 
 class GetConceptResultProcessor extends AbstractResultProcessor
 {
 	public Object processResultSet(ResultSet rs) throws SQLException
 	{
 		return new SQL_ConceptStub(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4));
 	}
 }
 
 /**
  * Okay, here is how things work with rule trees.
  * - rule terms are mapped as <cat-key, op-type, left-term-key, right-term-key> tuples in the DB.
  * - right-term-key might be null for single-operand terms.
  * - context terms pose problems ... the context list has to be encoded.
  *   it is done as <cat-key, -1, context-term-key, concept-key> tuples.
  *   so, if we run into a -1 op-type, recover the concept-key and start building
  *   the context list.
  */
 class GetFilterResultProcessor extends AbstractResultProcessor
 {
 	private List<Object[]> _interimResults = new ArrayList<Object[]>();
 	private boolean _getUserKey;
 	private Long    _userKey;
 
 	public GetFilterResultProcessor(boolean getUserKey)
 	{
 		_getUserKey = getUserKey;
 	}
 
 	public ResultProcessor getNewInstance() { return new GetFilterResultProcessor(_getUserKey); }
 
 	public Object processResultSet(ResultSet rs) throws SQLException
 	{
 			// IMPORTANT: processResultSet methods *should complete* WITHOUT attempting 
 			// to acquire additional db resources!  Otherwise, we could deadlock.
 		_interimResults.add(new Object[] {rs.getLong(1), rs.getString(2), rs.getString(3), rs.getLong(4), rs.getInt(5)});
 		if (_getUserKey)
 			_userKey = rs.getLong(6);
 		return null;
 	}
 
 	private RuleTerm buildRuleTree(Long termKey, Map<Long, Object[]> rtMap, Map<Long, Object> operandMap)
 	{
 			// rtVals[0] -- rule term key
 			// rtVals[1] -- rule term type
 			// rtVals[2] -- arg 1 key
 			// rtVals[3] -- arg 2 key
 		Object[] rtVals = rtMap.get(termKey);
 		FilterOp op     = Filter.getTermType((Integer)rtVals[1]);
 		switch(op) {
 			case LEAF_CONCEPT:
 				return new Filter.LeafConcept(SQL_Stmt._db.getConcept((Long)rtVals[2]), ((Long)rtVals[3]).intValue());
 
 			case LEAF_FILTER:
 				return new Filter.LeafFilter(SQL_Stmt._db.getFilter((Long)rtVals[2]));
 
 			case SOURCE_FILTER:
 				return new Filter.SourceFilter((NR_SourceCollection)SQL_Stmt.GET_COLLECTION_BY_KEY.get((Long)rtVals[2]));
 
 			case LEAF_CAT:
 				return new Filter.LeafCategory(SQL_Stmt._db.getCategory((Long)rtVals[2]));
 
 			case NOT_TERM:
 				return new Filter.NegTerm(buildRuleTree((Long)rtVals[2], rtMap, operandMap));
 
 			case CONTEXT_TERM:
 				return new Filter.ContextTerm(buildRuleTree((Long)rtVals[2], rtMap, operandMap), (List)operandMap.get(termKey));
 
 			case AND_TERM:
 			case OR_TERM:
 				return new Filter.AndOrTerm(op, buildRuleTree((Long)rtVals[2], rtMap, operandMap), buildRuleTree((Long)rtVals[3], rtMap, operandMap));
 
 			case PROXIMITY_TERM:
 				return new Filter.ProximityTerm(SQL_Stmt._db.getConcept((Long)rtVals[2]), SQL_Stmt._db.getConcept((Long)rtVals[3]), ((Long)operandMap.get(termKey)).intValue());
 		}
 
 		SQL_Stmt._log.error("Fallen out of Ruleterm switch!  Should not have happened!  Investigate!");
 		return null;
 	}
 
 	private Filter buildFilter(Object[] sqlRowVals)
 	{
 			// sqlRowVals[0] is the filter key
 		List<Object[]>      ruleTerms  = (List<Object[]>)SQL_Stmt.GET_FILTER_TERMS.execute(new Object[] {(Long)sqlRowVals[0]});
 		Map<Long, Object>   operandMap = new HashMap<Long, Object>();
 		Map<Long, Object[]> rtMap      = new HashMap<Long, Object[]>();
 			// rtVals[0] -- rule term key
 			// rtVals[1] -- rule term type
 			// rtVals[2] -- arg 1 key
 			// rtVals[3] -- arg 2 key
 		for (Object[] rtVals: ruleTerms) {
 			rtMap.put((Long)rtVals[0], rtVals);
 				// Set up context concept lists for those rule term tuples for which the op-type value is CONTEXT_TERM_OPERAND_TYPE
 				// rtVals[2] is the key for the context-rule term
 			if (((Integer)rtVals[1]) == SQL_DB.CONTEXT_TERM_OPERAND_TYPE) {
 				List context = (List)operandMap.get(rtVals[2]);
 				if (context == null) {
 					context = new ArrayList<Concept>();
 					operandMap.put((Long)rtVals[2], context);
 				}
 				context.add(SQL_Stmt._db.getConcept((Long)rtVals[3]));
 			}
 				// Set up the proximity value for rule terms for which the op-type value is CONTEXT_TERM_OPERAND_TYPE
 				// rtVals[2] is the key for the context-rule term
 			if (((Integer)rtVals[1]) == SQL_DB.PROXIMITY_TERM_OPERAND_TYPE) {
 				operandMap.put((Long)rtVals[2], rtVals[3]);
 			}
 		}
 
 			// sqlRowVals[1] -- name; sqlRowVals[2] -- rule_string; sqlRowVals[3] -- rule_key; sqlRowVals[4] -- min_match_score
 		return new Filter((String)sqlRowVals[1], (String)sqlRowVals[2], buildRuleTree((Long)sqlRowVals[3], rtMap, operandMap), (Integer)sqlRowVals[4]);
 	}
 
 	public Object processOutput(Object o)
 	{
 		if (_interimResults.isEmpty()) {
 			return null;
 		}
 		else {
 			Filter f = buildFilter(_interimResults.get(0));
 			return (_getUserKey) ? new Tuple<Long, Filter>(_userKey, f) : f;
 		}
 	}
 
 	public List processOutputList(List l)
 	{
 		List filters = new ArrayList();
 		for (Object[] sqlRowVals: _interimResults)
 			filters.add(buildFilter(sqlRowVals));
 
 		return filters;
 	}
 }
 
 class GetCategoryResultProcessor extends AbstractResultProcessor
 {
 	private boolean _getNewsInfo;
 	private boolean _buildRuleTree;
 	private boolean _buildTaxonomy;
 	private List<Tuple<Category, Long>> _interimResults;
 	private Map<Long, Category> _catMap;	// cat key --> category
 	private Map<Long, Long> _parentMap;		// cat key --> parent cat key
 	private Long _userKey;
 
 	public GetCategoryResultProcessor(boolean getNewsInfo, boolean getFilter, boolean getParent) 
 	{
 		_getNewsInfo   = getNewsInfo;
 		_buildRuleTree = getFilter;
 		_buildTaxonomy = getParent;
 		if (_buildTaxonomy) {
 			_catMap    = new HashMap<Long, Category>();
 			_parentMap = new HashMap<Long, Long>();
 		}
 		if (_buildRuleTree) {
 			_interimResults = new ArrayList<Tuple<Category, Long>>();
 		}
 	}
 
 	public ResultProcessor getNewInstance() { return new GetCategoryResultProcessor(_getNewsInfo, _buildRuleTree, _buildTaxonomy); }
 
 	public Object processResultSet(ResultSet rs) throws SQLException
 	{
 			// IMPORTANT: processResultSet methods *should complete* WITHOUT attempting
 			// to acquire additional db resources!  Otherwise, we could deadlock.
 		_userKey = rs.getLong(6);
 		long filtKey = rs.getLong(5);
 		if (filtKey == 0)
 			filtKey = -1;
 
 		Category c = (_buildRuleTree) ? new Category(rs.getLong(1), rs.getString(2), null, rs.getInt(3))
 		                              : new SQL_CategoryStub(_userKey, rs.getLong(1), rs.getString(2), rs.getInt(3), rs.getLong(4), filtKey);
 		if (_getNewsInfo) {
 			c.setNumArticles(rs.getInt(8));
 			c.setLastUpdateTime(rs.getTimestamp(9));
 			if (!_buildRuleTree) /* In the build rule tree case, the issue info will be set elsewhere */
 				((SQL_CategoryStub)c).setIssueKey(rs.getLong(7));
 			c.setNumItemsSinceLastDownload(rs.getInt(10));
 			c.setTaxonomyPath(rs.getString(11));
 		}
 
 		if (_buildTaxonomy) {
 			_parentMap.put(c.getKey(), rs.getLong(4));
 			_catMap.put(c.getKey(), c);
 		}
 
 		if (_buildRuleTree)
 			_interimResults.add(new Tuple<Category,Long>(c, filtKey));
 
 		return c;
 	}
 
 	public Object processOutput(Object o)
 	{
 		if (_buildRuleTree && !_interimResults.isEmpty()) {
 				// set up filter for the category
 				// note that o will be the same as t._a
 			Tuple<Category, Long> t = _interimResults.get(0);
 			if (t._b != -1)
 				t._a.setFilter((Filter)SQL_Stmt.GET_FILTER_FOR_CAT.execute(new Object[]{t._b}));
 		}
 		return new Tuple<Long, Category>(_userKey, (Category)o);
 	}
 
 	public List processOutputList(List l)
 	{
 		if (_buildRuleTree) {
 				// set up filters for all the categories
 			for (Tuple<Category, Long> t: _interimResults) {
 				t._a.setFilter((Filter)SQL_Stmt.GET_FILTER_FOR_CAT.execute(new Object[]{t._b}));
 			}
 		}
 		if (_buildTaxonomy) {
 			List<Category> catList = l;
 			for (Category c: catList) {
 				Long parentKey = _parentMap.get(c.getKey());
 				if (parentKey != -1) {
 					Category parent = _catMap.get(parentKey);
 					c.setParent(parent);
 					parent.addChild(c);
 				}
 			}
 		}
 
 		return l;
 	}
 }
 
 class GetFeedResultProcessor extends AbstractResultProcessor
 {
 	public Object processResultSet(ResultSet rs) throws java.sql.SQLException
 	{
 		Long   feedKey  = rs.getLong(1);
 		String feedTag  = rs.getString(2);
 		String feedName = rs.getString(3);
 		String rssFeed  = rs.getString(4);
 		Feed f = new Feed(feedKey, feedTag, feedName, rssFeed, rs.getInt(7), rs.getInt(8));
 		f.setCacheableFlag(rs.getBoolean(5));
 		f.setShowCachedTextDisplayFlag(rs.getBoolean(6));
 		f.setIgnoreCommentsHeuristic(rs.getBoolean(9));
 		return f;
 	}
 }
 
 class GetSourceResultProcessor extends AbstractResultProcessor
 {
 	public Object processResultSet(ResultSet rs) throws java.sql.SQLException {
 		Long    srcKey    = rs.getLong(1);
 		Long    userKey   = rs.getLong(2);
 		Long    feedKey   = rs.getLong(3);
 		String  srcName   = rs.getString(4);
 		String  srcTag    = rs.getString(5);
 		boolean cacheable = rs.getBoolean(6);
 		boolean showCacheLinks = rs.getBoolean(7);
 		return new SQL_SourceStub(srcKey, feedKey, userKey, srcName, srcTag, cacheable, showCacheLinks);
 	}
 }
 
 class GetNewsItemResultProcessor extends AbstractResultProcessor
 {
 	public Object processResultSet(ResultSet rs) throws java.sql.SQLException
 	{
 		String urlRoot   = rs.getString(3);
 		String urlTail   = rs.getString(4);
 		String title     = rs.getString(5);
 		String desc      = rs.getString(6);
 		String author    = rs.getString(7);
 		Date   date      = rs.getDate(8);
 		Long   feedKey   = rs.getLong(9);
 		SQL_NewsItem ni = new SQL_NewsItem(urlRoot, urlTail, title, desc, author, feedKey, date);
 		ni.setKey(rs.getLong(1));
 		ni.setNewsIndexKey(rs.getLong(2));
 
 		return ni;
 	}
 }
 
 public enum SQL_Stmt 
 {
 	GET_NEWS_ITEM(
 		"SELECT n1.n_key, n1.primary_ni_key, n1.url_root, n1.url_tail, n1.title, n1.description, n1.author, n2.created_at, n2.feed_key" +
 			" FROM news_items n1, news_indexes n2" +
 			" WHERE n1.n_key = ? AND n1.primary_ni_key = n2.ni_key",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.QUERY,
 		null,
 		new GetNewsItemResultProcessor(),
 		true
 	),
    GET_NEWS_ITEM_FROM_URL(
  		"SELECT n.n_key, n.primary_ni_key, n.url_root, n.url_tail, n.title, n.description, n.author, ni.created_at, ni.feed_key" +
 			" FROM news_item_url_md5_hashes h, news_items n, news_indexes ni" +
 			" WHERE h.url_hash = md5(?) AND h.n_key = n.n_key AND n.primary_ni_key = ni.ni_key",
 		new SQL_ValType[] {STRING},
        SQL_StmtType.QUERY,
  		null,
  		new GetNewsItemResultProcessor(),
  		true
  	),
    GET_NEWS_ITEM_FROM_TITLE(
  		"SELECT n.n_key, n.primary_ni_key, n.url_root, n.url_tail, n.title, n.description, n.author, ni.created_at, ni.feed_key" +
 			" FROM news_items n, news_indexes ni, recent_news_title_hashes h" +
 			" WHERE n.primary_ni_key = ni.ni_key AND h.n_key = n.n_key AND h.title_hash = md5(?)",
 		new SQL_ValType[] {STRING},
        SQL_StmtType.QUERY,
  		null,
  		new GetNewsItemResultProcessor(),
  		false
  	),
 	GET_ALL_NEWS_ITEMS_WITH_URL(
 		"SELECT n_key FROM news_item_url_md5_hashes WHERE url_hash = md5(?)",
 		new SQL_ValType[] {STRING},
       SQL_StmtType.QUERY,
 		null,
 		new GetLongResultProcessor(),
 		false
 	),
 		/* NOTE: This query is present for backward compatibility -- will be deprecated in the future! */
    GET_NEWS_ITEM_FROM_LOCALPATH(
 		"SELECT n.n_key, n.primary_ni_key, n.url_root, n.url_tail, n.title, n.description, n.author, ?, ?" +
 			" FROM news_item_localnames l, news_items n" +
 			" WHERE l.local_file_name = ? AND l.n_key = n.n_key AND n.primary_ni_key = ?",
 		new SQL_ValType[] {STRING, LONG, STRING, LONG},
       SQL_StmtType.QUERY,
 		null,
 		new GetNewsItemResultProcessor(),
 		true
 	),
    GET_NEWS_ITEM_LOCALNAME(
       "SELECT local_file_name FROM news_item_localnames WHERE n_key=?",
       new SQL_ValType[] { LONG },
       SQL_StmtType.QUERY,
 		null,
 		new GetStringResultProcessor(),
 		true
    ),
 	GET_NEWS_INDEX(
 		"SELECT ni_key, feed_key, created_at FROM news_indexes WHERE ni_key = ?",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.QUERY,
 		null,
 		new GetNewsIndexResultProcessor(),
 		true
 	),
 	GET_NEWS_INDEX_KEY(
 		"SELECT ni_key FROM news_indexes WHERE feed_key = ? AND created_at = ?",
 		new SQL_ValType[] {LONG, STRING},
       SQL_StmtType.QUERY,
 		null,
 		new GetLongResultProcessor(),
 		true
 	),
 	GET_ALL_NEWS_INDEXES_FROM_FEED_ID(
 		"SELECT ni_key, feed_key, created_at FROM news_indexes n WHERE n.feed_key = ?",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.QUERY,
 		null,
 		new GetNewsIndexResultProcessor(),
 		false
 	),
 	GET_ALL_NEWS_INDEXES_BETWEEN_DATES_FROM_FEED_ID(
 		"SELECT ni_key, feed_key, created_at FROM news_indexes n WHERE n.feed_key = ? AND created_at >= ? AND created_at <= ?",
 		new SQL_ValType[] {LONG, DATE, DATE},
       SQL_StmtType.QUERY,
 		null,
 		new GetNewsIndexResultProcessor(),
 		false
 	),
 	CAT_NEWSITEM_PRESENT(
 		"SELECT c_key FROM cat_news WHERE c_key = ? AND n_key = ? AND ni_key = ?",
 		new SQL_ValType[] {LONG, LONG, LONG},
       SQL_StmtType.QUERY,
 		null,
 		new AbstractResultProcessor() {
 			public Object processResultSet(ResultSet rs) throws java.sql.SQLException { return new Boolean(true); }
 		},
 		true
 	),
 /**
 	GET_NEWS_FROM_CAT(
 		"SELECT n.n_key, n.primary_ni_key, n.url_root, n.url_tail, n.title, n.description, n.author, ni.created_at, ni.feed_key" +
 		   " FROM  news_items n, news_indexes ni, cat_news cn" +
 		   " WHERE (cn.c_key = ?) AND (cn.n_key = n.n_key) AND (cn.ni_key = ni.ni_key) " +
 		   " ORDER BY cn.date_stamp DESC, cn.n_key DESC LIMIT ?, ?",
 		new SQL_ValType[] {LONG, INT, INT},
       SQL_StmtType.QUERY,
 		null,
 		new GetNewsItemResultProcessor(),
 		false
 	),
 	GET_NEWS_KEYS_FROM_ISSUE(
 		"SELECT n_key FROM cat_news cn, categories c WHERE c.t_key = ? AND cn.c_key = c.c_key ORDER by date_stamp DESC, n_key DESC LIMIT ?, ?",
 		new SQL_ValType[] {LONG, INT, INT},
       SQL_StmtType.QUERY,
 		null,
 		new GetLongResultProcessor(),
 		false
 	),
 	GET_NEWS_KEYS_FROM_ISSUE_BETWEEN_DATES(
 		"SELECT n_key FROM cat_news cn, categories c WHERE c.t_key = ? AND cn.c_key = c.c_key AND date_stamp >= ? AND date_stamp <= ? ORDER by date_stamp DESC, n_key DESC LIMIT ?, ?",
 		new SQL_ValType[] {LONG, DATE, DATE, INT, INT},
       SQL_StmtType.QUERY,
 		null,
 		new GetLongResultProcessor(),
 		false
 	),
 **/
 	GET_LEAF_CAT_KEYS_FOR_NEWSITEM(
 		"SELECT cn.c_key FROM cat_news cn, categories c WHERE n_key = ? AND cn.c_key = c.c_key AND c.valid = true",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.QUERY,
 		null,
 		new GetLongResultProcessor(),
 		false
 	),
 	GET_ALL_CAT_KEYS_FOR_NEWSITEM(
 		"SELECT distinct(c2.c_key) FROM cat_news cn, categories c, categories c2 WHERE n_key = ? AND cn.c_key = c.c_key AND c.valid = true AND c2.t_key=c.t_key AND c2.lft <= c.lft AND c2.rgt >= c.rgt",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.QUERY,
 		null,
 		new GetLongResultProcessor(),
 		false
 	),
 	GET_CATS_FOR_NEWSITEM(
 		"SELECT c.c_key, c.name, c.cat_id, c.parent_cat, c.f_key, c.u_key, c.t_key, c.num_articles, c.last_update, c.num_new_articles, c.taxonomy_path FROM cat_news cn, categories c WHERE n_key = ? AND cn.c_key = c.c_key AND c.valid = true",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.QUERY,
 		null,
 		new GetCategoryResultProcessor(true, false, false),
 		false
 	),
 	GET_FILTER_TERMS(
 		"SELECT rt_key, term_type, arg1_key, arg2_key FROM filter_rule_terms WHERE f_key = ? ",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.QUERY,
 		null,
 		new AbstractResultProcessor() {
 			public Object processResultSet(ResultSet rs) throws java.sql.SQLException { return new Object[]{rs.getLong(1), rs.getInt(2), rs.getLong(3), rs.getLong(4)}; }
 		},
 		false
 	),
 	GET_ALL_FILTER_KEYS_FOR_USER(
 		"SELECT f_key FROM filters WHERE u_key = ? ",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.QUERY,
 		null,
 		new GetLongResultProcessor(),
 		false
 	),
 	GET_CATCOUNT_FOR_NEWSITEM(
 		"SELECT COUNT(n_key) FROM cat_news WHERE n_key = ?",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.QUERY,
 		null,
 		new GetIntResultProcessor(),
 		true
 	),
 	GET_NEWS_FROM_NEWSINDEX(
 		"SELECT n.n_key, n.primary_ni_key, n.url_root, n.url_tail, n.title, n.description, n.author, ni.created_at, ni.feed_key" +
 		   " FROM  news_items n, news_indexes ni, news_collections nc" +
 		   " WHERE (nc.ni_key = ? AND nc.n_key = n.n_key AND ni.ni_key = nc.ni_key)",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.QUERY,
 		null,
 		new GetNewsItemResultProcessor(),
 		false
 	),
 	GET_ALL_FEEDS_FOR_NEWS_ITEM(
 		"SELECT feed_key FROM news_collections where n_key = ?",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.QUERY,
 		null,
 		new GetLongResultProcessor(),
 		false
 	),
 	GET_DOWNLOADED_NEWS_FOR_FEED(
 		"SELECT n.n_key, n.primary_ni_key, n.url_root, n.url_tail, n.title, n.description, n.author, ni.created_at, ni.feed_key" +
 		   " FROM news_items n, news_indexes ni, downloaded_news dn" +
 		   " WHERE (dn.feed_key = ?) AND (dn.n_key = n.n_key) AND (n.primary_ni_key = ni.ni_key)",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.QUERY,
 		null,
 		new GetNewsItemResultProcessor(),
 		false
 	),
 	GET_DOWNLOADED_NEWS_KEYS_FOR_FEED(
 		"SELECT n_key FROM downloaded_news dn WHERE (dn.feed_key = ?)",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.QUERY,
 		null,
 		new GetLongResultProcessor(),
 		false
 	),
 	GET_FEED(
 		"SELECT feed_key, feed_tag, feed_name, url, cacheable, show_cache_links, num_fetches, num_failures, use_ignore_comments_heuristic FROM feeds WHERE feed_key = ?",
       new SQL_ValType[] {LONG},
 		SQL_StmtType.QUERY,
 		null,
 		new GetFeedResultProcessor(),
 		true
 	),
    GET_FEED_FROM_URL(
 		"SELECT feed_key, feed_tag, feed_name, url, cacheable, show_cache_links, num_fetches, num_failures, use_ignore_comments_heuristic FROM feeds WHERE url = ?",
       new SQL_ValType[] {STRING},
 		SQL_StmtType.QUERY,
 		null,
 		new GetFeedResultProcessor(),
 		true
 	),
 	GET_FEED_FROM_TAG(
 		"SELECT feed_key, feed_tag, feed_name, url, cacheable, show_cache_links, num_fetches, num_failures, use_ignore_comments_heuristic FROM feeds WHERE feed_tag = ?",
       new SQL_ValType[] {STRING},
 		SQL_StmtType.QUERY,
 		null,
 		new GetFeedResultProcessor(),
 		true
 	),
 	GET_ALL_FEEDS(
 		"SELECT feed_key, feed_tag, feed_name, url, cacheable, show_cache_links, num_fetches, num_failures, use_ignore_comments_heuristic FROM feeds",
       new SQL_ValType[] {},
 		SQL_StmtType.QUERY,
 		null,
 		new GetFeedResultProcessor(),
 		false
 	),
 	GET_SOURCE(
 		"SELECT src_key, u_key, feed_key, src_name, src_tag, cacheable, show_cache_links FROM sources WHERE src_key = ?",
       new SQL_ValType[] {LONG},
 		SQL_StmtType.QUERY,
 		null,
 		new GetSourceResultProcessor(),
 		true
 	),
 	GET_TOPIC_SOURCE(
 		"SELECT s.src_key, s.u_key, s.feed_key, s.src_name, s.src_tag, s.cacheable, s.show_cache_links FROM topic_sources t, sources s WHERE t.t_key = ? and t.src_key = s.src_key and s.src_tag = ?",
       new SQL_ValType[] {LONG, STRING},
 		SQL_StmtType.QUERY,
 		null,
 		new GetSourceResultProcessor(),
 		true
 	),
 	GET_USER_SOURCE(
 		"SELECT src_key, u_key, feed_key, src_name, src_tag, cacheable, show_cache_links FROM sources WHERE u_key = ? AND src_tag = ?",
       new SQL_ValType[] {LONG, STRING},
 		SQL_StmtType.QUERY,
 		null,
 		new GetSourceResultProcessor(),
 		true
 	),
 	GET_USER_SOURCE_KEY(
 		"SELECT src_key FROM sources WHERE u_key = ? AND feed_key = ? AND src_tag = ?",
       new SQL_ValType[] {LONG, LONG, STRING},
 		SQL_StmtType.QUERY,
 		null,
 		new GetLongResultProcessor(),
 		true
 	),
    GET_UNIQUE_FEED_TAG(
 		"SELECT feed_tag FROM feeds WHERE url = ?",
       new SQL_ValType[] {STRING},
 		SQL_StmtType.QUERY,
 		null,
 		new GetStringResultProcessor(),
 		true
 	),
    GET_USER_FROM_UID(
       "SELECT u_key,uid,password,name,email,validated FROM users WHERE uid = ?",	// simpler to select all fields rather than ignoring a single field
       new SQL_ValType[] {STRING},
 		SQL_StmtType.QUERY,
 		null,
       new GetUserResultProcessor(),
 		true
    ),
    GET_USER(
       "SELECT u_key,uid,password,name,email,validated FROM users WHERE u_key = ?",	// simpler to select all fields rather than ignoring a single field
       new SQL_ValType[] {LONG},
 		SQL_StmtType.QUERY,
 		null,
       new GetUserResultProcessor(),
 		true
    ),
    GET_ALL_USERS(
       "SELECT u_key,uid,password,name,email,validated FROM users ORDER BY uid",
       new SQL_ValType[] {},
 		SQL_StmtType.QUERY,
 		null,
       new GetUserResultProcessor(),
 		false
    ),
 	GET_ISSUE_INFO(
       "SELECT t_key, num_articles, last_update FROM topics WHERE name = ? AND u_key = ?",
       new SQL_ValType[] {STRING, LONG},
 		SQL_StmtType.QUERY,
 		null,
 		new AbstractResultProcessor() {
 			public Object processResultSet(ResultSet rs) throws java.sql.SQLException { return new Triple(rs.getLong(1), rs.getInt(2), rs.getTimestamp(3)); }
 		},
 		true
 	),
 	GET_CAT_INFO(
 		"SELECT c_key, num_articles, last_update FROM categories WHERE t_key = ? AND cat_id = ?",
       new SQL_ValType[] {LONG, INT},
 		SQL_StmtType.QUERY,
 		null,
 		new AbstractResultProcessor() {
 			public Object processResultSet(ResultSet rs) throws java.sql.SQLException { return new Triple(rs.getLong(1), rs.getInt(2), rs.getTimestamp(3)); }
 		},
 		true
 	),
    GET_ISSUE(
       "SELECT t_key,u_key,name,num_articles,last_update,validated,frozen,private,taxonomy_path,num_new_articles FROM topics WHERE t_key = ?",
       new SQL_ValType[] {LONG},
 		SQL_StmtType.QUERY,
 		null,
 		new GetIssueResultProcessor(),
 		true
    ),
    GET_ISSUE_BY_USER_KEY(
       "SELECT t_key,u_key,name,num_articles,last_update,validated,frozen,private,taxonomy_path,num_new_articles FROM topics WHERE u_key = ? AND name = ?",
       new SQL_ValType[] {LONG, STRING},
 		SQL_StmtType.QUERY,
 		null,
 		new GetIssueResultProcessor(),
 		true
    ),
    GET_ALL_ISSUES_BY_USER_KEY(
       "SELECT t_key,u_key,name,num_articles,last_update,validated,frozen,private,taxonomy_path,num_new_articles FROM topics WHERE u_key = ? ORDER BY lower(name)",
       new SQL_ValType[] {LONG},
 		SQL_StmtType.QUERY,
 		null,
 		new GetIssueResultProcessor(),
 		false
    ),
 	GET_LEAF_CAT_KEYS_FOR_ISSUE(
 	   "SELECT c.c_key FROM categories c WHERE c.t_key = ? AND c.valid = TRUE AND c.rgt = c.lft + 1",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.QUERY,
 		null,
 		new GetLongResultProcessor(),
 		false
 	),
 	GET_CAT_KEYS_FOR_ISSUE(
 		"SELECT c_key FROM categories WHERE t_key = ? AND valid = true",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.QUERY,
 		null,
 		new GetLongResultProcessor(),
 		false
 	),
 	GET_CATS_FOR_ISSUE(
 		"SELECT c_key, name, cat_id, parent_cat, f_key, u_key, t_key, num_articles, last_update, num_new_articles, taxonomy_path FROM categories WHERE t_key = ? AND valid = true",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.QUERY,
 		null,
 		new GetCategoryResultProcessor(true, false, true),
 		false
 	),
    GET_ALL_VALIDATED_ISSUE_KEYS(
       "SELECT t_key FROM topics where validated = true ORDER BY lower(name)",
       new SQL_ValType[] {},
 		SQL_StmtType.QUERY,
 		null,
 		new GetLongResultProcessor(),
 		false
    ),
    GET_ALL_ISSUE_KEYS(
       "SELECT t_key FROM topics ORDER BY lower(name)",
       new SQL_ValType[] {},
 		SQL_StmtType.QUERY,
 		null,
 		new GetLongResultProcessor(),
 		false
    ),
 	GET_IMPORTING_USERS(
 		"SELECT importing_user_key FROM import_dependencies WHERE from_user_key = ?",
       new SQL_ValType[] {LONG},
 		SQL_StmtType.QUERY,
 		null,
 		new GetLongResultProcessor(),
 		false
 	),
 	GET_EXPORTING_USERS(
 		"SELECT from_user_key FROM import_dependencies WHERE importing_user_key = ?",
       new SQL_ValType[] {LONG},
 		SQL_StmtType.QUERY,
 		null,
 		new GetLongResultProcessor(),
 		false
 	),
 	GET_COLLECTION(
 		"SELECT coll_key,file_key,coll_name,coll_type FROM user_collections WHERE uid = ? AND coll_name = ? AND coll_type = ?",
       new SQL_ValType[] {STRING, STRING, STRING},
 		SQL_StmtType.QUERY,
 		null,
 		new GetCollectionResultProcessor(),
 		true
 	),
 	GET_COLLECTION_BY_KEY(
 		"SELECT coll_key,file_key,coll_name,coll_type FROM user_collections WHERE coll_key = ?",
       new SQL_ValType[] {LONG},
 		SQL_StmtType.QUERY,
 		null,
 		new GetCollectionResultProcessor(),
 		true
 	),
 	GET_COLLECTION_KEY(
 		"SELECT coll_key FROM user_collections WHERE u_key = ? AND coll_name = ? AND coll_type = ?",
       new SQL_ValType[] {LONG, STRING, STRING},
 		SQL_StmtType.QUERY,
 		null,
 		new GetLongResultProcessor(),
 		true
 	),
 	GET_ALL_COLLECTIONS_OF_TYPE(
 		"SELECT coll_key,file_key,coll_name,coll_type FROM user_collections WHERE coll_type = ?",
       new SQL_ValType[] {STRING},
 		SQL_StmtType.QUERY,
 		null,
 		new GetCollectionResultProcessor(),
 		false
 	),
 	GET_ALL_COLLECTIONS_OF_TYPE_FOR_USER(
 		"SELECT coll_key,file_key,coll_name,coll_type FROM user_collections WHERE coll_type = ? AND uid = ?",
       new SQL_ValType[] {STRING, STRING},
 		SQL_StmtType.QUERY,
 		null,
 		new GetCollectionResultProcessor(),
 		false
 	),
 	GET_COLLECTION_FOR_CONCEPT(
	   "SELECT c.coll_key,c.file_key,c.coll_name,c.coll_type from user_collections c, collection_entries ce WHERE c.coll_type = 'CPT' AND c.coll_key = ce.coll_key AND ce.entry_key = ?",
       new SQL_ValType[] {LONG},
 		SQL_StmtType.QUERY,
 		null,
 		new GetCollectionResultProcessor(),
		true
 	),
    GET_USER_FILE(
       "SELECT file_key, u_key, file_name FROM user_files WHERE file_key = ?",
       new SQL_ValType[] {LONG},
 		SQL_StmtType.QUERY,
 		null,
 		new GetUserFileResultProcessor(),
 		true
    ),
    GET_ALL_FILES_BY_USER_KEY(
       "SELECT file_key, u_key, file_name FROM user_files WHERE u_key = ? ORDER BY file_name",
       new SQL_ValType[] {LONG},
 		SQL_StmtType.QUERY,
 		null,
 		new GetUserFileResultProcessor(),
 		false
    ),
 	GET_ALL_SOURCES_FROM_USER_COLLECTION(
 		"SELECT s.src_key, s.u_key, s.feed_key, s.src_name, s.src_tag, s.cacheable, s.show_cache_links FROM sources s, collection_entries ce WHERE ce.coll_key = ? AND ce.entry_key = s.src_key",
 		new SQL_ValType[] {LONG},
 		SQL_StmtType.QUERY,
 		null,
 		new GetSourceResultProcessor(),
 		false
 	),
 	GET_SOURCE_FROM_USER_COLLECTION(
 		"SELECT s.src_key, s.u_key, s.feed_key, s.src_name, s.src_tag, s.cacheable, s.show_cache_links FROM sources s, collection_entries ce WHERE ce.coll_key = ? AND s.src_tag = ? AND ce.entry_key = s.src_key",
 		new SQL_ValType[] {LONG, STRING},
 		SQL_StmtType.QUERY,
 		null,
 		new GetSourceResultProcessor(),
 		true
 	),
 	GET_MONITORED_SOURCE_KEYS_FOR_TOPIC(
 		"SELECT t.src_key FROM topic_sources t WHERE t.t_key = ?",
 		new SQL_ValType[] {LONG},
 		SQL_StmtType.QUERY,
 		null,
 		new GetLongResultProcessor(),
 		false
 	),
 	GET_ALL_MONITORED_SOURCES_FOR_USER(
 		"SELECT s.src_key, s.u_key, s.feed_key, s.src_name, s.src_tag, s.cacheable, s.show_cache_links FROM topics t, topic_sources ts, sources s WHERE t.u_key = ? AND ts.t_key = t.t_key AND s.src_key = ts.src_key GROUP BY s.feed_key ORDER BY lower(s.src_name)",
 		new SQL_ValType[] {LONG},
 		SQL_StmtType.QUERY,
 		null,
 		new GetSourceResultProcessor(),
 		false
 	),
 	GET_TOPIC_SOURCE_ROW(
 	   "SELECT max_ni_key FROM topic_sources WHERE t_key = ? AND feed_key = ? ORDER BY src_key",
 		new SQL_ValType[] {LONG, LONG},
 		SQL_StmtType.QUERY,
 		null,
 		new GetLongResultProcessor(),
 		false
 	),
 	GET_CONCEPT(
 		"SELECT u_key, cpt_key, name, defn, token FROM concepts WHERE cpt_key = ?",
 		new SQL_ValType[] {LONG},
 		SQL_StmtType.QUERY,
 		null,
 		new GetConceptTupleResultProcessor(),
 		true
 	),
 	GET_MATCHING_CONCEPT(
 		"SELECT c.u_key, c.cpt_key, c.name, c.defn, c.token FROM concepts c, user_collections uc, collection_entries ce WHERE uc.u_key = ? AND uc.coll_name = ? AND uc.coll_type = 'CPT' AND uc.coll_key = ce.coll_key AND ce.entry_key = c.cpt_key AND c.name = ?",
 		new SQL_ValType[] {LONG, STRING, STRING},
 		SQL_StmtType.QUERY,
 		null,
 		new GetConceptTupleResultProcessor(),
 		true
 	),
 	GET_CONCEPT_FROM_USER_COLLECTION(
 		"SELECT c.u_key, c.cpt_key, c.name, c.defn, c.token FROM concepts c, collection_entries ce WHERE ce.coll_key = ? AND ce.entry_key = c.cpt_key AND c.name = ?",
 		new SQL_ValType[] {LONG, STRING},
 		SQL_StmtType.QUERY,
 		null,
 		new GetConceptTupleResultProcessor(),
 		true
 	),
 	GET_CONCEPT_KEY_FROM_USER_COLLECTION(
 		"SELECT c.cpt_key FROM concepts c, collection_entries ce WHERE ce.coll_key = ? AND ce.entry_key = c.cpt_key AND c.name = ?",
 		new SQL_ValType[] {LONG, STRING},
 		SQL_StmtType.QUERY,
 		null,
 		new GetLongResultProcessor(),
 		true
 	),
 	GET_ALL_CONCEPTS_FROM_USER_COLLECTION(
 		"SELECT c.u_key, c.cpt_key, c.name, c.defn, c.token FROM concepts c, collection_entries ce WHERE ce.coll_key = ? AND ce.entry_key = c.cpt_key",
 		new SQL_ValType[] {LONG},
 		SQL_StmtType.QUERY,
 		null,
 		new GetConceptResultProcessor(),
 		false
 	),
 	GET_ALL_FILTERS_FROM_USER_COLLECTION(
 		"SELECT f.f_key, f.name, f.rule_string, f.rule_key, f.min_match_score FROM filters f, collection_entries ce WHERE ce.coll_key = ? AND ce.entry_key = f.f_key",
 		new SQL_ValType[] {LONG},
 		SQL_StmtType.QUERY,
 		null,
 		new GetFilterResultProcessor(false),
 		false
 	),
 	GET_FILTER_FROM_USER_COLLECTION(
 		"SELECT f.f_key, f.name, f.rule_string, f.rule_key, f.min_match_score, f.u_key FROM filters f, collection_entries ce WHERE ce.coll_key = ? AND ce.entry_key = f.f_key AND f.name = ?",
 		new SQL_ValType[] {LONG, STRING},
 		SQL_StmtType.QUERY,
 		null,
 		new GetFilterResultProcessor(true),
 		true
 	),
 	GET_FILTER_FOR_CAT(
 		"SELECT f_key, name, rule_string, rule_key, min_match_score FROM filters WHERE f_key = ? ",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.QUERY,
 		null,
 		new GetFilterResultProcessor(false),
 		true
 	),
 	GET_FILTER(
 		"SELECT f_key, name, rule_string, rule_key, min_match_score, u_key FROM filters WHERE f_key = ?",
 		new SQL_ValType[] {LONG},
 		SQL_StmtType.QUERY,
 		null,
 		new GetFilterResultProcessor(true),
 		true
 	),
 	GET_ALL_CATEGORIES_FROM_USER_COLLECTION(
 		"SELECT c.c_key, c.name, c.cat_id, c.parent_cat, c.f_key, c.u_key FROM categories c, collection_entries ce WHERE ce.coll_key = ? AND ce.entry_key = c.c_key",
 		new SQL_ValType[] {LONG},
 		SQL_StmtType.QUERY,
 		null,
 		new GetCategoryResultProcessor(false, true, true),
 		false
 	),
 	GET_CATEGORY_FROM_USER_COLLECTION(
 		"SELECT c.c_key, c.name, c.cat_id, c.parent_cat, c.f_key, c.u_key FROM categories c, collection_entries ce WHERE ce.coll_key = ? AND ce.entry_key = c.c_key AND c.name = ? AND c.parent_cat = ?",
 		new SQL_ValType[] {LONG, STRING, LONG},
 		SQL_StmtType.QUERY,
 		null,
 		new GetCategoryResultProcessor(false, true, false),
 		true
 	),
 	GET_CATEGORY(
 		"SELECT c_key, name, cat_id, parent_cat, f_key, u_key, t_key, num_articles, last_update, num_new_articles, taxonomy_path FROM categories WHERE c_key = ?",
 		new SQL_ValType[] {LONG},
 		SQL_StmtType.QUERY,
 		null,
 		new GetCategoryResultProcessor(true, false, false),
 		true
 	),
 	GET_CATEGORY_FROM_TAXONOMY_PATH(
 		"SELECT c_key, name, cat_id, parent_cat, f_key, u_key, t_key, num_articles, last_update, num_new_articles, taxonomy_path FROM categories WHERE taxonomy_path = ? AND valid = ?",
 		new SQL_ValType[] {STRING, BOOLEAN},
 		SQL_StmtType.QUERY,
 		null,
 		new GetCategoryResultProcessor(true, false, false),
 		true
 	),
 	GET_NESTED_CAT_KEYS(
 		"SELECT c_key FROM categories WHERE parent_cat = ? AND valid = true",
 		new SQL_ValType[] {LONG},
 		SQL_StmtType.QUERY,
 		null,
 		new GetLongResultProcessor(),
 		false
 	),
 	GET_NESTED_CATS(
 		"SELECT c_key, name, cat_id, parent_cat, f_key, u_key, t_key, num_articles, last_update, num_new_articles, taxonomy_path FROM categories WHERE parent_cat = ?",
 		new SQL_ValType[] {LONG},
 		SQL_StmtType.QUERY,
 		null,
 		new GetCategoryResultProcessor(true, false, false),
 		false
 	),
 	GET_NESTED_CATS_FROM_USER_COLLECTION(
 		"SELECT c.c_key, c.name, c.cat_id, c.parent_cat, c.f_key FROM categories c, collection_entries ce WHERE ce.coll_key = ? AND ce.entry_key = c.c_key AND c.parent_cat = ?",
 		new SQL_ValType[] {LONG, LONG},
 		SQL_StmtType.QUERY,
 		null,
 		new GetCategoryResultProcessor(false, true, false),
 		false
 	),
    GET_ALL_PUBLIC_FILES(
       "SELECT uf.file_name, uf.u_key FROM user_files uf, users u WHERE uf.u_key = u.u_key AND u.validated = true ORDER BY uf.u_key",
       new SQL_ValType[] {},
 		SQL_StmtType.QUERY,
 		null,
 		new GetPublicFilesResultProcessor(),
 		false
    ),
 	GET_ALL_ACTIVE_FEEDS(
 	   "SELECT feed_key, feed_tag, feed_name, url, cacheable, show_cache_links, num_fetches, num_failures, use_ignore_comments_heuristic FROM feeds WHERE feed_key IN (SELECT distinct feed_key FROM topic_sources, topics where topics.frozen = 0 and topic_sources.t_key=topics.t_key)",
 		new SQL_ValType[] {},
 		SQL_StmtType.QUERY,
 		null,
 		new GetFeedResultProcessor(),
 		false
 	),
 	GET_TOPICS_MONITORING_FEED(
 		"SELECT DISTINCT(t_key) FROM topic_sources WHERE feed_key = ?",
 		new SQL_ValType[] {LONG},
 		SQL_StmtType.QUERY,
 		null,
 		new GetLongResultProcessor(),
 		false
 	),
 		// Prepared Statement Strings for INSERTs 
    INSERT_USER(
 		"INSERT INTO users (uid, password, name, email) VALUES (?,?,?,?)",
 		new SQL_ValType[] {STRING, STRING, STRING, STRING},
       SQL_StmtType.INSERT,
       new SQL_ColumnSize[] {USER_TBL_UID, USER_TBL_PASSWORD, USER_TBL_NAME, USER_TBL_EMAIL},
 		new GetLongResultProcessor(),
 		true
    ),
 	INSERT_TOPIC(
 		"INSERT INTO topics (u_key, name, validated, frozen, private, taxonomy_path) VALUES (?,?,?,?,?,?)",
 		new SQL_ValType[] {LONG, STRING, BOOLEAN, BOOLEAN, BOOLEAN, STRING},
       SQL_StmtType.INSERT,
 		null,
 		new GetLongResultProcessor(),
 		true
 	),
    INSERT_FEED(
 		"INSERT INTO feeds (feed_name, url, num_fetches, num_failures) VALUES (?,?,0,0)",
 		new SQL_ValType[] {STRING, STRING},
       SQL_StmtType.INSERT,
       new SQL_ColumnSize[] {NONE, FEED_TBL_FEEDURL},
 		new GetLongResultProcessor(),
 		true
 	),
 	INSERT_NEWS_INDEX(
 		"INSERT INTO news_indexes (feed_key, created_at) VALUES (?,?)",
       new SQL_ValType[] {LONG, DATE},
 		SQL_StmtType.INSERT,
 		null,
 		new GetLongResultProcessor(),
 		true
 	),
 	INSERT_NEWS_ITEM(
 		"INSERT INTO news_items (primary_ni_key, url_root, url_tail, title, description, author) VALUES (?,?,?,?,?,?)",
       new SQL_ValType[] {LONG, STRING, STRING, STRING, STRING, STRING},
 		SQL_StmtType.INSERT,
 		new SQL_ColumnSize[] {NONE, NEWS_ITEM_TBL_URLROOT, NEWS_ITEM_TBL_URLTAIL, NONE, NONE, NONE},
 		new GetLongResultProcessor(),
 		true
 	),
 	INSERT_INTO_NEWS_COLLECTION(
 		"INSERT IGNORE INTO news_collections (n_key, ni_key, feed_key) VALUES (?, ?, ?)",
 		new SQL_ValType[] {LONG, LONG, LONG},
       SQL_StmtType.INSERT
 	),
 	INSERT_URL_HASH(
 		"INSERT INTO news_item_url_md5_hashes(n_key, url_hash) VALUES(?, md5(?))",
 		new SQL_ValType[] {LONG, STRING},
 		SQL_StmtType.INSERT
 	),
 	INSERT_TITLE_HASH(
 		"INSERT INTO recent_news_title_hashes(n_key, title_hash, story_date) VALUES(?, md5(?), ?)",
 		new SQL_ValType[] {LONG, STRING, DATE},
 		SQL_StmtType.INSERT
 	),
 	INSERT_INTO_RECENT_DOWNLOAD_TABLE(
 		"INSERT IGNORE INTO downloaded_news (feed_key, n_key) VALUES (?, ?)",
 		new SQL_ValType[] {LONG, LONG},
       SQL_StmtType.INSERT
 	),
 	INSERT_CAT(
 		"INSERT INTO categories (name, u_key, t_key, cat_id, parent_cat, f_key, taxonomy_path) VALUES (?,?,?,?,?,?,?)",
       new SQL_ValType[] {STRING, LONG, LONG, INT, LONG, LONG, STRING},
 		SQL_StmtType.INSERT,
       new SQL_ColumnSize[] {CAT_TBL_NAME, NONE, NONE, NONE, NONE, NONE, NONE},
 		new GetLongResultProcessor(),
 		true
 	),
 	INSERT_RULE_TERM(
 		"INSERT INTO filter_rule_terms (f_key, term_type, arg1_key, arg2_key) VALUES (?,?,?,?)",
       new SQL_ValType[] {LONG, INT, LONG,  LONG},
 		SQL_StmtType.INSERT,
       null,
 		new GetLongResultProcessor(),
 		true
 	),
 	INSERT_INTO_CAT_NEWS_TABLE(
 		"INSERT IGNORE INTO cat_news (c_key, n_key, ni_key, date_stamp) VALUES (?,?,?,?)",
 		new SQL_ValType[] {LONG, LONG, LONG, DATE},
       SQL_StmtType.INSERT
 	),
 	INSERT_USER_FILE(
 		"INSERT INTO user_files(u_key, file_name) VALUES (?, ?)",
 		new SQL_ValType[] {LONG, STRING},
       SQL_StmtType.INSERT,
 		null,
 		new GetLongResultProcessor(),
 		true
 	),
 	INSERT_IMPORT_DEPENDENCY(
 	   "INSERT IGNORE INTO import_dependencies(from_user_key, importing_user_key) VALUES (?, ?)",
 		new SQL_ValType[] {LONG, LONG},
       SQL_StmtType.INSERT
 	),
 	INSERT_COLLECTION(
 		"INSERT INTO user_collections (coll_name, coll_type, file_key, u_key, uid) VALUES (?, ?, ?, ?, ?)",
 		new SQL_ValType[] {STRING, STRING, LONG, LONG, STRING},
       SQL_StmtType.INSERT,
 		null,
 		new GetLongResultProcessor(),
 		true
 	),
 	INSERT_ENTRY_INTO_COLLECTION(
 		"INSERT INTO collection_entries (coll_key, entry_key) VALUES (?,?)",
 		new SQL_ValType[] {LONG, LONG},
       SQL_StmtType.INSERT,
 		null,
 		new GetLongResultProcessor(),
 		true
 	),
 	INSERT_CONCEPT(
 		"INSERT INTO concepts(u_key, name, defn, keywords) VALUES (?,?,?,?)",
 		new SQL_ValType[] {LONG, STRING, STRING, STRING},
       SQL_StmtType.INSERT,
 		null,
 		new GetLongResultProcessor(),
 		true
 	),
 	INSERT_FILTER(
 		"INSERT INTO filters (u_key, name, rule_string, min_match_score) VALUES (?,?,?,?)",
 		new SQL_ValType[] {LONG, STRING, STRING, INT},
       SQL_StmtType.INSERT,
 		null,
 		new GetLongResultProcessor(),
 		true
 	),
 	INSERT_USER_SOURCE(
 		"INSERT INTO sources (u_key, feed_key, src_name, src_tag, cacheable, show_cache_links) VALUES (?,?,?,?,?,?)",
 		new SQL_ValType[] {LONG, LONG, STRING, STRING, BOOLEAN, BOOLEAN},
       SQL_StmtType.INSERT,
 		null,
 		new GetLongResultProcessor(),
 		true
 	),
 	INSERT_TOPIC_SOURCE(
 		"INSERT INTO topic_sources (t_key, src_key, feed_key) VALUES (?,?,?)",
 		new SQL_ValType[] {LONG, LONG, LONG},
       SQL_StmtType.INSERT
 	),
 		// Prepared Statement Strings for UPDATEs 
 	RENAME_USER_FILE(
 		"UPDATE user_files SET file_name = ? WHERE file_key = ?",
       new SQL_ValType[] {STRING, LONG},
 		SQL_StmtType.UPDATE
 	),
 	UPDATE_USER(
 		"UPDATE users SET password = ?, name = ?, email = ?, validated = ? WHERE u_key = ?",
       new SQL_ValType[] {STRING, STRING, STRING, BOOLEAN, LONG},
 		SQL_StmtType.UPDATE
 	),
 	UPDATE_LOGIN_DATE(
 		"UPDATE users SET last_login = ? WHERE u_key = ?",
       new SQL_ValType[] {TIMESTAMP, LONG},
 		SQL_StmtType.UPDATE
 	),
 	UPDATE_FEED_CACHEABILITY(
 		"UPDATE feeds SET cacheable = ?, show_cache_links = ? WHERE feed_key = ?",
       new SQL_ValType[] {BOOLEAN, BOOLEAN, LONG}, 
 		SQL_StmtType.UPDATE
 	),
 	SET_FEED_TAG(
 		"UPDATE feeds SET feed_tag = ? WHERE feed_key = ?",
       new SQL_ValType[] {STRING, LONG}, 
 		SQL_StmtType.UPDATE
 	),
 	UPDATE_FEED_STATS(
 		"UPDATE feeds SET num_fetches = ?, num_failures = ? WHERE feed_key = ?",
       new SQL_ValType[] {INT, INT, LONG}, 
 		SQL_StmtType.UPDATE
 	),
    UPDATE_CONCEPT_TOKEN(
       "UPDATE concepts SET token = ? WHERE cpt_key = ?",
 		new SQL_ValType[] {STRING, LONG},
       SQL_StmtType.UPDATE
 	),
 	UPDATE_TOPIC_INFO(
       "UPDATE topics SET validated = ?, frozen = ?, private = ? WHERE t_key = ?",
 		new SQL_ValType[] {BOOLEAN, BOOLEAN, BOOLEAN, LONG},
       SQL_StmtType.UPDATE
 	),
 	UPDATE_ARTCOUNT_FOR_TOPIC(
 		"UPDATE topics SET num_articles = ?, last_update = ?, num_new_articles = ? WHERE t_key = ?",
       new SQL_ValType[] {INT, TIMESTAMP, INT, LONG}, 
 		SQL_StmtType.UPDATE
 	),
 	UPDATE_ART_COUNT_FOR_CAT("UPDATE categories SET categories.num_articles = (SELECT count(*) FROM cat_news WHERE cat_news.c_key = categories.c_key) WHERE categories.c_key = ?",
       new SQL_ValType[] {LONG},
 		SQL_StmtType.UPDATE
 	),
 	UPDATE_ART_COUNTS_FOR_ALL_TOPIC_LEAF_CATS("UPDATE categories SET categories.num_articles = (SELECT count(*) FROM cat_news WHERE cat_news.c_key = categories.c_key) WHERE categories.t_key = ? AND categories.f_key != -1",
       new SQL_ValType[] {LONG},
 		SQL_StmtType.UPDATE
 	),
 /**
 	UPDATE_TOPIC_VALID_STATUS(
       "UPDATE topics SET validated = ? WHERE t_key = ?",
 		new SQL_ValType[] {BOOLEAN, LONG},
       SQL_StmtType.UPDATE
 	),
 **/
 	UPDATE_TOPICS_VALID_STATUS_FOR_USER(
       "UPDATE topics SET validated = ? WHERE u_key = ?",
 		new SQL_ValType[] {BOOLEAN, LONG},
       SQL_StmtType.UPDATE
 	),
 	UPDATE_TOPIC_SOURCE_INFO(
 		"UPDATE topic_sources SET max_ni_key = ? WHERE t_key = ? AND feed_key = ?",
 		new SQL_ValType[] {LONG, LONG, LONG},
       SQL_StmtType.UPDATE
 	),
 	RESET_ALL_TOPIC_SOURCES(
 		"UPDATE topic_sources SET max_ni_key = 0 WHERE t_key = ?",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.UPDATE
 	),
 	UPDATE_LEAF_CAT_NEWS_INFO(
 		"UPDATE categories SET last_update = ?, num_new_articles = ?, num_articles = (select count(*) from cat_news where c_key = categories.c_key) WHERE c_key = ?",
       new SQL_ValType[] {TIMESTAMP, INT, LONG},
 		SQL_StmtType.UPDATE
 	),
 	UPDATE_CAT_NEWS_INFO(
 		"UPDATE categories SET num_articles = ?, last_update = ?, num_new_articles = ? WHERE c_key = ?",
       new SQL_ValType[] {INT, TIMESTAMP, INT, LONG},
 		SQL_StmtType.UPDATE
 	),
 	UPDATE_FILTER(
 		"UPDATE filters SET rule_key = ? WHERE f_key = ?",
       new SQL_ValType[] {LONG, LONG},
 		SQL_StmtType.UPDATE
 	),
    RENAME_CAT(
       "UPDATE categories SET name = ? WHERE c_key = ?",
 		new SQL_ValType[] {STRING, LONG},
       SQL_StmtType.UPDATE,
 		new SQL_ColumnSize[] {CAT_TBL_NAME, NONE},
 		null,
 		true
 	),
    UPDATE_CAT(
       "UPDATE categories SET valid = ?, f_key = ?, name = ?, cat_id = ?, parent_cat = ?, taxonomy_path = ? WHERE c_key = ?",
 		new SQL_ValType[] {BOOLEAN, LONG, STRING, INT, LONG, STRING, LONG},
       SQL_StmtType.UPDATE
 	),
 	SET_NESTED_SET_IDS_FOR_CAT(
       "UPDATE categories SET lft = ?, rgt = ? WHERE c_key = ?",
 		new SQL_ValType[] {INT, INT, LONG},
       SQL_StmtType.UPDATE
 	),
 	UPDATE_CATS_FOR_TOPIC(
       "UPDATE categories SET valid = ?, f_key = -1 WHERE t_key = ?",
 		new SQL_ValType[] {BOOLEAN, LONG},
       SQL_StmtType.UPDATE
 	),
 	UPDATE_CATS_FOR_USER(
       "UPDATE categories SET valid = ?, f_key = -1 WHERE u_key = ?",
 		new SQL_ValType[] {BOOLEAN, LONG},
       SQL_StmtType.UPDATE
 	),
 	UPDATE_SHARED_NEWS_ITEM_ENTRIES(
       "UPDATE IGNORE news_collections SET n_key = ? WHERE n_key = ?",
 		new SQL_ValType[] {LONG, LONG},
       SQL_StmtType.UPDATE
 	),
 	UPDATE_CAT_NEWS(
 		"UPDATE IGNORE cat_news SET n_key = ? WHERE n_key = ?",
 		new SQL_ValType[] {LONG, LONG},
       SQL_StmtType.UPDATE
 	),
 		// Prepared Statement Strings for DELETEs 
 	CLEAR_CAT_NEWS(
 		"DELETE FROM cat_news WHERE c_key = ?",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.DELETE
 	),
 	CLEAR_DOWNLOADED_NEWS_FOR_FEED(
 		"DELETE FROM downloaded_news WHERE feed_key = ?",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.DELETE
 	),
 	CLEAR_DOWNLOADED_NEWS_TABLE(
 		"TRUNCATE downloaded_news",
 		new SQL_ValType[] {},
       SQL_StmtType.DELETE
 	),
 	DELETE_NEWS_FROM_CAT(
 		"DELETE FROM cat_news WHERE c_key = ? AND n_key = ?",
 		new SQL_ValType[] {LONG, LONG},
       SQL_StmtType.DELETE
 	),
 	DELETE_CLASSIFIED_NEWSITEM(
 		"DELETE FROM cat_news WHERE n_key = ?",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.DELETE
 	),
 	DELETE_5_NEWS_ITEMS_FROM_CAT(
 		"DELETE FROM cat_news WHERE c_key = ? AND n_key IN (?, ?, ?, ?, ?)",
 		new SQL_ValType[] {LONG, LONG, LONG, LONG, LONG, LONG},
 		SQL_StmtType.DELETE
 	),
 	DELETE_NEWS_ITEM(
 	   "DELETE FROM news_items WHERE n_key = ?",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.DELETE
 	),
 	DELETE_URL_HASH_ENTRY(
 	   "DELETE FROM news_item_url_md5_hashes WHERE n_key = ?",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.DELETE
 	),
 	DELETE_TITLE_HASH_ENTRY(
 	   "DELETE FROM recent_news_title_hashes WHERE n_key = ?",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.DELETE
 	),
 	DELETE_SHARED_NEWS_ITEM_ENTRIES(
 	   "DELETE FROM news_collections WHERE n_key = ?",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.DELETE
 	),
 	DELETE_USER_FILE(
 		"DELETE FROM user_files WHERE file_key = ?",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.DELETE
 	),
 	DELETE_IMPORT_DEPENDENCIES_FOR_USER(
 		"DELETE FROM import_dependencies WHERE importing_user_key = ?",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.DELETE
 	),
 	DELETE_COLLECTION(
 		"DELETE FROM user_collections WHERE coll_key = ?",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.DELETE
 	),
 	DELETE_ALL_COLLECTIONS_FOR_USER(
 		"DELETE FROM user_collections WHERE u_key = ?",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.DELETE
 	),
 	DELETE_ALL_COLLECTION_ENTRIES(
 		"DELETE FROM collection_entries WHERE coll_key = ?",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.DELETE
 	),
 	DELETE_ALL_COLLECTION_ENTRIES_FOR_USER(
 		"DELETE FROM collection_entries WHERE coll_key IN (SELECT coll_key FROM user_collections WHERE u_key = ?)",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.DELETE
 	),
 	DELETE_ENTRY_FROM_COLLECTION(
 		"DELETE FROM collection_entries WHERE coll_key = ? AND entry_key = ?",
 		new SQL_ValType[] {LONG, LONG},
       SQL_StmtType.DELETE
 	),
 	DELETE_SOURCE_BY_TAG(
 		"DELETE FROM sources WHERE u_key = ? AND coll_key = ? AND src_tag = ?",
 		new SQL_ValType[] {LONG, LONG, STRING},
       SQL_StmtType.DELETE
 	),
 	DELETE_SOURCE_BY_ID(
 		"DELETE FROM sources WHERE src_key = ?",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.DELETE
 	),
 	DELETE_ALL_SOURCES_FOR_USER(
 		"DELETE FROM sources WHERE u_key = ?",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.DELETE
 	),
 	DELETE_CATEGORY(
 		"DELETE FROM categories WHERE c_key = ?",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.DELETE
 	),
 	DELETE_FILTER(
 		"DELETE FROM filters WHERE f_key = ?",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.DELETE
 	),
 	DELETE_ALL_FILTERS_FOR_USER(
 		"DELETE FROM filters WHERE u_key = ?",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.DELETE
 	),
 	DELETE_ALL_FILTER_TERMS_FOR_USER(
 		"DELETE FROM filter_rule_terms WHERE f_key IN (SELECT f_key FROM filters WHERE u_key = ?)",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.DELETE
 	),
 	DELETE_FILTER_TERMS(
 		"DELETE FROM filter_rule_terms WHERE f_key = ?",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.DELETE
 	),
 	DELETE_CONCEPT_BY_ID(
 		"DELETE FROM concepts WHERE cpt_key = ?",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.DELETE
 	),
 	DELETE_ALL_CONCEPTS_FOR_USER(
 		"DELETE FROM concepts WHERE u_key = ?",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.DELETE
 	),
 	DELETE_CONCEPT_BY_NAME(
 		"DELETE FROM concepts WHERE u_key = ? AND coll_key = ? AND name = ?",
 		new SQL_ValType[] {LONG, LONG, STRING},
       SQL_StmtType.DELETE
 	),
 	DELETE_ALL_TOPIC_SOURCES_FOR_USER(
 		"DELETE FROM topic_sources WHERE t_key IN (SELECT t_key FROM topics WHERE u_key = ?)",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.DELETE
 	),
 	DELETE_FROM_TOPIC_SOURCE_TABLE(
 		"DELETE FROM topic_sources WHERE t_key = ?",
 		new SQL_ValType[] {LONG},
       SQL_StmtType.DELETE
 	);
 
    static Log          _log;
    static DB_Interface _db;
 
    public static void init(Log l, DB_Interface db)
    {
       _log = l;
 		_db = db;
    }
 
    public final String           _stmtString;
    public final SQL_ValType[]    _argTypes;
    public final SQL_StmtType     _stmtType;
    public final SQL_ColumnSize[] _colSizes;
 	public final ResultProcessor  _rp;
 	public final boolean          _singleRowOutput;
 
    SQL_Stmt(String stmt, SQL_ValType[] aTypes, SQL_StmtType type, SQL_ColumnSize[] colSizes, ResultProcessor rp, boolean singleRow)
    {
       _stmtString = stmt;
       _argTypes   = aTypes;
       _stmtType   = type;
       _colSizes   = colSizes;
 		_rp         = rp;
 		_singleRowOutput = singleRow;
    }
 
 	SQL_Stmt(String stmt, SQL_ValType[] aTypes, SQL_StmtType type)
 	{
       _stmtString = stmt;
       _argTypes   = aTypes;
       _stmtType   = type;
       _colSizes   = null;
 		_rp         = null;
 		_singleRowOutput = true;
 	}
 
    /**
     * This method executes a prepared sql statement using arguments passed in
     * and pushes the result set through a result processor, if any.  The result
     * processor is just a cumbersome way of passing in a closure since Java
     * does not support closures yet.
     *
     * @param args   Argument array for this sql statement
 	 * @returns the result of executing the query, if any 
     */
    public Object execute(Object[] args)
    {
 		return SQL_StmtExecutor.execute(_stmtString, _stmtType, args, _argTypes, _colSizes, _rp, _singleRowOutput);
    }
 
 	public Object get(Long key)
 	{
 		return SQL_StmtExecutor.execute(_stmtString, _stmtType, new Object[]{key}, _argTypes, _colSizes, _rp, _singleRowOutput);
 	}
 
 	public Object delete(Long key)
 	{
 		return SQL_StmtExecutor.execute(_stmtString, _stmtType, new Object[]{key}, _argTypes, _colSizes, _rp, _singleRowOutput);
 	}
 }
