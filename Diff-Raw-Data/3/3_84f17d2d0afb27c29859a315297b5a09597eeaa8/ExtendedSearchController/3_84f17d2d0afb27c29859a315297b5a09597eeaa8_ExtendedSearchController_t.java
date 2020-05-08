 package org.infoglue.cms.controllers.kernel.impl.simple;
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.exolab.castor.jdo.Database;
 import org.exolab.castor.jdo.OQLQuery;
 import org.exolab.castor.jdo.PersistenceException;
 import org.exolab.castor.jdo.QueryResults;
 import org.infoglue.cms.entities.content.ContentVersion;
 import org.infoglue.cms.entities.content.ContentVersionVO;
 import org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl;
 import org.infoglue.cms.entities.kernel.BaseEntityVO;
 import org.infoglue.cms.entities.management.CategoryVO;
 import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
 import org.infoglue.cms.entities.management.LanguageVO;
 import org.infoglue.cms.exception.SystemException;
 
 /**
  * 
  */
 public class ExtendedSearchController extends BaseController {
 	/**
 	 * The singleton controller.
 	 */
 	private static final ExtendedSearchController instance = new ExtendedSearchController();
 
 	/**
 	 * Singleton class; don't allow instantiation.
 	 */
 	private ExtendedSearchController() 
 	{
 		super();
 	}
 
 	/**
 	 * Returns the singleton controller.
 	 */
 	public static ExtendedSearchController getController()
 	{
 		return instance;
 	}
 
 	/**
 	 * 
 	 */
 	public List search(final ContentTypeDefinitionVO contentTypeDefinitionVO, final LanguageVO languageVO, final Map categories) throws SystemException
 	{
 		return search(contentTypeDefinitionVO, languageVO, categories, Collections.EMPTY_LIST, null);
 	}
 
 	/**
 	 * 
 	 */
 	public List search(final ContentTypeDefinitionVO contentTypeDefinitionVO, final LanguageVO languageVO, final Map categories, final Database db) throws SystemException
 	{
 		final List contentTypeDefintionVOs = new ArrayList();
 		contentTypeDefintionVOs.add(contentTypeDefinitionVO);
 		return search(contentTypeDefintionVOs, languageVO, categories, Collections.EMPTY_LIST, null, db);
 	}
 	
 	/**
 	 * 
 	 */
 	public List search(final List contentTypeDefinitionVOs, final LanguageVO languageVO, final Map categories) throws SystemException
 	{
 		return search(contentTypeDefinitionVOs, languageVO, categories, Collections.EMPTY_LIST, null);
 	}
 
 	/**
 	 * 
 	 */
 	public List search(final List contentTypeDefinitionVOs, final LanguageVO languageVO, final Map categories, final Database db) throws SystemException
 	{
 		return search(contentTypeDefinitionVOs, languageVO, categories, Collections.EMPTY_LIST, null, db);
 	}
 	
 	/**
 	 * 
 	 */
 	public List search(final ContentTypeDefinitionVO contentTypeDefinitionVO, final LanguageVO languageVO, final Map categories, final List xmlAttributes, final String freetext) throws SystemException
 	{
 		final List contentTypeDefintionVOs = new ArrayList();
 		contentTypeDefintionVOs.add(contentTypeDefinitionVO);
 		return search(contentTypeDefintionVOs, languageVO, categories, xmlAttributes, freetext);
 	}
 	
 	/**
 	 * 
 	 */
 	public List search(final List contentTypeDefinitionVOs, final LanguageVO languageVO, final Map categories, final List xmlAttributes, final String freetext) throws SystemException
 	{
 		final Database db = beginTransaction();
 		try
 		{
 			final List result = search(contentTypeDefinitionVOs, languageVO, categories, xmlAttributes, freetext, db);
 			commitTransaction(db);
 			return result;
 		}
 		catch (Exception e)
 		{
 			rollbackTransaction(db);
 			throw new SystemException(e.getMessage());
 		}
 	}
 	
 	/**
 	 * 
 	 */
 	public List search(final List contentTypeDefinitionVOs, final LanguageVO languageVO, final Map categories, final List xmlAttributes, final String freetext, final Database db) throws SystemException
 	{
 		try 
 		{
 			final SqlBuilder sqlBuilder = new SqlBuilder(contentTypeDefinitionVOs, languageVO, categories, xmlAttributes, freetext);
 			final OQLQuery oql = db.getOQLQuery(sqlBuilder.getSQL());
 			for(Iterator i=sqlBuilder.getBindings().iterator(); i.hasNext(); )
 				oql.bind(i.next());
 			return createResults(oql.execute(Database.ReadOnly));
 		} 
 		catch(Exception e)
 		{
 			throw new SystemException(e.getMessage());
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private List createResults(final QueryResults qr) throws PersistenceException, SystemException {
 		final List results = new ArrayList();
 		while(qr.hasMore()) {
 			final ContentVersion contentVersion = (ContentVersion) qr.next();
 			results.add(contentVersion.getValueObject());
 		}
 		return results;
 	}
 	
 	/**
 	 * Unused. 
 	 */
 	public BaseEntityVO getNewVO() { return null; }
 }
 
 /**
  * 
  */
 class SqlBuilder {
 	//
 	private static final String SELECT_KEYWORD                = "SELECT";
 	private static final String FROM_KEYWORD                  = "FROM";
 	private static final String WHERE_KEYWORD                 = "WHERE";
 	
 	private static final String SPACE                         = " ";
 	private static final String COMMA                         = ",";
 	private static final String AND                           = "AND";
 	private static final String OR                            = "OR";
 	
 	//
 	private static final String CATEGORY_ALIAS_PREFIX         = "cat";
 	private static final String CONTENT_ALIAS                 = "c";
 	private static final String CONTENT_CATEGORY_ALIAS_PREFIX = "ccat";
 	private static final String CONTENT_VERSION_ALIAS         = "cv";
 
 	//
 	private static final String CATEGORY_TABLE                = "cmcategory";
 	private static final String CONTENT_CATEGORY_TABLE        = "cmcontentcategory";
 	private static final String CONTENT_TABLE                 = "cmCont"; // TODO: TEMPORARY FIX!!! "cmcontent";
 	private static final String CONTENT_VERSION_TABLE         = "cmContVer"; // TODO: TEMPORARY FIX!!! "cmcontentversion";
 	
 	//
 	private static final String CV_ACTIVE_CLAUSE              = CONTENT_VERSION_ALIAS + ".isActive=1";
 	private static final String CV_LANGUAGE_CLAUSE            = CONTENT_VERSION_ALIAS + ".languageId={0}";
 	private static final String CV_STATE_CLAUSE               = CONTENT_VERSION_ALIAS + ".stateId=" + ContentVersionVO.PUBLISHED_STATE;
 	private static final String CV_CONTENT_JOIN               = CONTENT_ALIAS + ".ContId=" + CONTENT_VERSION_ALIAS + ".ContId"; // TODO: TEMPORARY FIX!!! CONTENT_ALIAS + ".contentId=" + CONTENT_VERSION_ALIAS + ".contentId";
 	private static final String CV_LATEST_VERSION_CLAUSE      = CONTENT_VERSION_ALIAS + ".ContVerId in (select max(ContVerId) from " + CONTENT_VERSION_TABLE + " cv2 where cv2.ContId=" + CONTENT_VERSION_ALIAS + ".ContId)"; //  TODO: TEMPORARY FIX!!! CONTENT_VERSION_ALIAS + ".contentVersionId in (select max(contentVersionId) from " + CONTENT_VERSION_TABLE + " cv2 where cv2.contentId=" + CONTENT_VERSION_ALIAS + ".contentId)";
 	
 	//
 	private static final String C_CONTENT_TYPE_CLAUSE         = CONTENT_ALIAS + ".contentTypeDefId={0}"; // TODO: TEMPORARY FIX!!! CONTENT_ALIAS + ".contentTypeDefinitionId={0}";
 	
 	//
 	private static final String FREETEXT_EXPRESSION           = CONTENT_VERSION_ALIAS + ".VerValue like {0}"; // TODO: TEMPORARY FIX!!! CONTENT_VERSION_ALIAS + ".versionValue like {0}";
 	private static final String FREETEXT_EXPRESSION_VARIABLE  = "%<{0}><![CDATA[%{1}%]]></{0}>%";
 	
 	//
	private static final String CATEGORY_CLAUSE               = "(" + CATEGORY_ALIAS_PREFIX + "{0}.active=1 " + AND + SPACE + CATEGORY_ALIAS_PREFIX + "{0}.categoryId={1} " + AND + SPACE + CONTENT_CATEGORY_ALIAS_PREFIX + "{0}.attributeName={2} " + AND + SPACE + CONTENT_CATEGORY_ALIAS_PREFIX + "{0}.categoryId = " + CATEGORY_ALIAS_PREFIX + "{0}.categoryId  " + AND + SPACE + CONTENT_CATEGORY_ALIAS_PREFIX + "{0}.ContVerId=" + CONTENT_VERSION_ALIAS + ".ContVerId)";
	//private static final String CATEGORY_CLAUSE               = "(" + CATEGORY_ALIAS_PREFIX + "{0}.active=1 " + AND + SPACE + CATEGORY_ALIAS_PREFIX + "{0}.categoryId={1} " + AND + SPACE + CONTENT_CATEGORY_ALIAS_PREFIX + "{0}.attributeName={2} " + AND + SPACE + CONTENT_CATEGORY_ALIAS_PREFIX + "{0}.categoryId = " + CATEGORY_ALIAS_PREFIX + "{0}.categoryId  " + AND + SPACE + CONTENT_CATEGORY_ALIAS_PREFIX + "{0}.contentVersionId=" + CONTENT_VERSION_ALIAS + ".contentVersionId)";
 	
 	private final List contentTypeDefinitionVOs;
 	private final LanguageVO languageVO;
 	private final Map categories; // <ContentCategory.attributeName> -> <Category>
 	private final List xmlAttributes;
 	private final String freetext;
 
 	//
 	private final Map uniqueCategoryTableKeys = new HashMap();
 	
 	//
 	private String sql;
 	private List bindings;
 	private int bindingsCounter;
 	
 	
 	/**
 	 *
 	 */
 	public SqlBuilder(final List contentTypeDefinitionVOs, final LanguageVO languageVO, final Map categories, final List xmlAttributes, final String freetext) {
 		super();
 		this.languageVO               = languageVO;
 		this.contentTypeDefinitionVOs = contentTypeDefinitionVOs;
 		this.categories               = categories;
 		this.xmlAttributes            = xmlAttributes;
 		this.freetext                 = freetext;
 		
 		initializeUniqueCategoryTableKeys();
 		
 		this.bindings = new ArrayList();
 		bindingsCounter = 1;
 		
 		this.sql = generate();
 	}
 
 	/**
 	 * 
 	 */
 	public String getSQL() {
 		return sql;
 	}
 	
 	/**
 	 * 
 	 */
 	public List getBindings() {
 		return bindings;
 	}
 	
 	/**
 	 * 
 	 */
 	private void initializeUniqueCategoryTableKeys() {
 		int uniqueKey=0;
 		for(final Iterator names=categories.keySet().iterator(); names.hasNext(); ++uniqueKey)
 			uniqueCategoryTableKeys.put(names.next(), new Integer(uniqueKey));
 	}
 	
 	/**
 	 * 
 	 */
 	private String getUniqueCategoryTableKey(String attributeName) { return 
 		uniqueCategoryTableKeys.get(attributeName).toString(); 
 	}
 	
 	/**
 	 * 
 	 */
 	private String generate() {
 		return "CALL SQL" + SPACE + generateSelectClause() + SPACE + generateFromClause() + SPACE + generateWhereClause() + " AS " + ContentVersionImpl.class.getName();
 	}
 	
 	/**
 	 * 
 	 */
 	private String generateSelectClause() {
 		return 	SELECT_KEYWORD + SPACE + 
 		CONTENT_VERSION_ALIAS + ".ContVerId" +
 		//CONTENT_VERSION_ALIAS + ".contentVersionId" +
 		COMMA + CONTENT_VERSION_ALIAS + ".stateId" +
 		COMMA + CONTENT_VERSION_ALIAS + ".modifiedDateTime" +
 		COMMA + CONTENT_VERSION_ALIAS + ".VerComment" +
 		//COMMA + CONTENT_VERSION_ALIAS + ".versionComment" +
 		COMMA + CONTENT_VERSION_ALIAS + ".isCheckedOut" +
 		COMMA + CONTENT_VERSION_ALIAS + ".isActive" +
 		COMMA + CONTENT_VERSION_ALIAS + ".contentId" +
 		COMMA + CONTENT_VERSION_ALIAS + ".languageId" +
 		COMMA + CONTENT_VERSION_ALIAS + ".versionModifier" +
 		COMMA + CONTENT_VERSION_ALIAS + ".contentVersionId" +
 		//COMMA + CONTENT_VERSION_ALIAS + ".contentVersionId" +
 		//COMMA + CONTENT_VERSION_ALIAS + ".versionValue";
 		COMMA + CONTENT_VERSION_ALIAS + ".VerValue";
 	}
 
 	/**
 	 * 
 	 */
 	private String generateFromClause() {
 		final List tables = new ArrayList();
 		tables.add(CONTENT_TABLE + SPACE + CONTENT_ALIAS);
 		tables.add(CONTENT_VERSION_TABLE + SPACE + CONTENT_VERSION_ALIAS);
 		tables.addAll(getCategoryTables());
 		
 		return FROM_KEYWORD + SPACE + joinCollection(tables, COMMA);
 	}
 
 	/**
 	 * 
 	 */
 	private String generateWhereClause() {
 		final List clauses = new ArrayList();
 		clauses.addAll(getContentWhereClauses());
 		clauses.add(getContentVersionWhereClauses());
 		if(doFreetextSearch())
 			clauses.add(getFreetextWhereClause());
 		clauses.addAll(getCategoriesWhereClauses());
 		return WHERE_KEYWORD + SPACE + joinCollection(clauses, SPACE + AND + SPACE);
 	}
 
 	/**
 	 * 
 	 */
 	private boolean doFreetextSearch() {
 		return freetext != null && freetext.length() > 0 && !xmlAttributes.isEmpty();
 	}
 	
 	/**
 	 * 
 	 */
 	private List getContentWhereClauses() {
 		final List clauses = new ArrayList();
 
 		clauses.add(CV_ACTIVE_CLAUSE);
 		clauses.add(CV_STATE_CLAUSE);
 		clauses.add(CV_LATEST_VERSION_CLAUSE);
 		clauses.add(CV_CONTENT_JOIN);
 		
 		clauses.add(MessageFormat.format(CV_LANGUAGE_CLAUSE, new Object[] { getBindingVariable() }));
 		bindings.add(languageVO.getId());
 		
 		return clauses;
 	}
 	
 	/**
 	 * 
 	 */
 	private String getContentVersionWhereClauses() {
 		final List expressions = new ArrayList();
 		for(final Iterator i=contentTypeDefinitionVOs.iterator(); i.hasNext(); ) {
 			final ContentTypeDefinitionVO contentTypeDefinitionVO = (ContentTypeDefinitionVO) i.next();
 			expressions.add(MessageFormat.format(C_CONTENT_TYPE_CLAUSE, new Object[] { getBindingVariable() }));
 			bindings.add(contentTypeDefinitionVO.getId());
 		}
 
 		return "(" + joinCollection(expressions, SPACE + OR + SPACE) + ")";
 	}
 
 	/**
 	 * 
 	 */
 	private List getCategoriesWhereClauses() {
 		final List clauses = new ArrayList();
 		for(Iterator names=categories.keySet().iterator(); names.hasNext(); )
 			clauses.add(getCategoriesWhereClause((String) names.next()));
 		return clauses;
 	}
 
 	/**
 	 * 
 	 */
 	private String getCategoriesWhereClause(final String attributeName) {
 		final CategoryVO categoryVO = (CategoryVO) categories.get(attributeName);
 		final String alias          = getUniqueCategoryTableKey(attributeName);
 		bindings.add(categoryVO.getId());
 		bindings.add(attributeName);
 		return MessageFormat.format(CATEGORY_CLAUSE, new Object[] { alias, getBindingVariable(), getBindingVariable() });
 	}
 	
 	/**
 	 * 
 	 */
 	private String getFreetextWhereClause() {
 		final List expressions = new ArrayList();
 		for(final Iterator i=xmlAttributes.iterator(); i.hasNext(); ) {
 			final String xmlAttribute = (String) i.next();
 			final String freeTextExpression = MessageFormat.format(FREETEXT_EXPRESSION, new Object[] { getBindingVariable() }); 
 			final String freeTextVariable   = MessageFormat.format(FREETEXT_EXPRESSION_VARIABLE, new Object[] { xmlAttribute, freetext }); 
 			
 			bindings.add(freeTextVariable);
 			expressions.add(freeTextExpression);
 		}
 		return "(" + joinCollection(expressions, SPACE + OR + SPACE) + ")";
 	}
 	
 	/**
 	 * 
 	 */
 	private List getCategoryTables() {
 		final List tables = new ArrayList();
 		for(Iterator i=categories.keySet().iterator(); i.hasNext(); ) {
 			final String attributeName  = (String) i.next();
 			final String uniqueKey      = getUniqueCategoryTableKey(attributeName);
 			tables.add(CATEGORY_TABLE + SPACE + CATEGORY_ALIAS_PREFIX + uniqueKey);
 			tables.add(CONTENT_CATEGORY_TABLE + SPACE + CONTENT_CATEGORY_ALIAS_PREFIX + uniqueKey);
 		}
 		return tables;
 	}
 	
 	/**
 	 * 
 	 */
 	private String joinCollection(final Collection collection, final String delimiter) {
 		final StringBuffer sb = new StringBuffer();
 		for(Iterator i=collection.iterator(); i.hasNext(); ) {
 			String element = (String) i.next();
 			sb.append(element + (i.hasNext() ? delimiter : ""));
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * 
 	 */
 	private String getBindingVariable() {
 		return "$" + bindingsCounter++;
 	}
 }
