 package org.infoglue.cms.controllers.kernel.impl.simple;
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.exolab.castor.jdo.Database;
 import org.exolab.castor.jdo.OQLQuery;
 import org.exolab.castor.jdo.PersistenceException;
 import org.exolab.castor.jdo.QueryResults;
 import org.infoglue.cms.entities.content.Content;
 import org.infoglue.cms.entities.content.ContentVersion;
 import org.infoglue.cms.entities.content.impl.simple.ContentImpl;
 import org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl;
 import org.infoglue.cms.entities.content.impl.simple.SmallContentImpl;
 import org.infoglue.cms.entities.kernel.BaseEntityVO;
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
 	public Set search(final Integer stateId, final ContentTypeDefinitionVO contentTypeDefinitionVO, final LanguageVO languageVO, final CategoryConditions categories) throws SystemException
 	{
 		return search(stateId, contentTypeDefinitionVO, languageVO, categories, Collections.EMPTY_LIST, null);
 	}
 
 	/**
 	 * 
 	 */
 	public Set search(final Integer stateId, final ContentTypeDefinitionVO contentTypeDefinitionVO, final LanguageVO languageVO, final CategoryConditions categories, final Database db) throws SystemException
 	{
 		final List contentTypeDefintionVOs = new ArrayList();
 		contentTypeDefintionVOs.add(contentTypeDefinitionVO);
 		return search(stateId, contentTypeDefintionVOs, languageVO, categories, Collections.EMPTY_LIST, null, db);
 	}
 	
 	/**
 	 * 
 	 */
 	public Set search(final Integer stateId, final List contentTypeDefinitionVOs, final LanguageVO languageVO, final CategoryConditions categories) throws SystemException
 	{
 		return search(stateId, contentTypeDefinitionVOs, languageVO, categories, Collections.EMPTY_LIST, null);
 	}
 
 	/**
 	 * 
 	 */
 	public Set search(final Integer stateId, final List contentTypeDefinitionVOs, final LanguageVO languageVO, final CategoryConditions categories, final Database db) throws SystemException
 	{
 		return search(stateId, contentTypeDefinitionVOs, languageVO, categories, Collections.EMPTY_LIST, null, db);
 	}
 	
 	/**
 	 * 
 	 */
 	public Set search(final Integer stateId, final ContentTypeDefinitionVO contentTypeDefinitionVO, final LanguageVO languageVO, final CategoryConditions categories, final List xmlAttributes, final String freetext) throws SystemException
 	{
 		final List contentTypeDefintionVOs = new ArrayList();
 		contentTypeDefintionVOs.add(contentTypeDefinitionVO);
 		return search(stateId, contentTypeDefintionVOs, languageVO, categories, xmlAttributes, freetext);
 	}
 	
 	/**
 	 * 
 	 */
 	public Set search(final Integer stateId, final List contentTypeDefinitionVOs, final LanguageVO languageVO, final CategoryConditions categories, final List xmlAttributes, final String freetext) throws SystemException
 	{
 		final Database db = beginTransaction();
 		try
 		{
 			final Set result = search(stateId, contentTypeDefinitionVOs, languageVO, categories, xmlAttributes, freetext, db);
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
 	public Set search(final Integer stateId, final List contentTypeDefinitionVOs, final LanguageVO languageVO, final CategoryConditions categories, final List xmlAttributes, final String freetext, final Database db) throws SystemException
 	{
 		try 
 		{
 			final SqlBuilder sqlBuilder = new SqlBuilder(stateId, contentTypeDefinitionVOs, languageVO, categories, xmlAttributes, freetext);
 			final OQLQuery oql = db.getOQLQuery(sqlBuilder.getSQL());
 			for(Iterator i=sqlBuilder.getBindings().iterator(); i.hasNext(); )
 				oql.bind(i.next());
 			return createResults(oql.execute(Database.ReadOnly));
 		} 
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			throw new SystemException(e.getMessage());
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private Set createResults(final QueryResults qr) throws PersistenceException, SystemException {
 		final Set results = new HashSet();
 		while(qr.hasMore())
 			results.add(qr.next());
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
 	private static final String CONTENT_ALIAS                 = "c";
 	private static final String CONTENT_VERSION_ALIAS         = "cv";
 
 	//
 	private static final String CONTENT_TABLE                 = "cmCont";
 	//private static final String CONTENT_TABLE                 = "cmcontent";
 	private static final String CONTENT_VERSION_TABLE         = "cmContVer";
 	//private static final String CONTENT_VERSION_TABLE         = "cmcontentversion";
 	
 	//
 	private static final String CV_ACTIVE_CLAUSE              = CONTENT_VERSION_ALIAS + ".isActive=1";
 	private static final String CV_LANGUAGE_CLAUSE            = CONTENT_VERSION_ALIAS + ".languageId={0}";
 	//private static final String CV_STATE_CLAUSE               = CONTENT_VERSION_ALIAS + ".stateId={0}";
 	private static final String CV_STATE_CLAUSE               = CONTENT_VERSION_ALIAS + ".stateId>={0}";
 	private static final String CV_CONTENT_JOIN               = CONTENT_ALIAS + ".ContId=" + CONTENT_VERSION_ALIAS + ".ContId";
 	//private static final String CV_CONTENT_JOIN               = CONTENT_ALIAS + ".contentId=" + CONTENT_VERSION_ALIAS + ".contentId";
 	private static final String CV_LATEST_VERSION_CLAUSE      = CONTENT_VERSION_ALIAS + ".ContVerId in (select max(ContVerId) from " + CONTENT_VERSION_TABLE + " cv2 where cv2.ContId=" + CONTENT_VERSION_ALIAS + ".ContId)";
 	//private static final String CV_LATEST_VERSION_CLAUSE      = CONTENT_VERSION_ALIAS + ".contentVersionId in (select max(contentVersionId) from " + CONTENT_VERSION_TABLE + " cv2 where cv2.contentId=" + CONTENT_VERSION_ALIAS + ".contentId)";
 	
 	//
 	private static final String C_CONTENT_TYPE_CLAUSE         = CONTENT_ALIAS + ".contentTypeDefId={0}";
 	//private static final String C_CONTENT_TYPE_CLAUSE         = CONTENT_ALIAS + ".contentTypeDefinitionId={0}";
 	
 	//
 	private static final String FREETEXT_EXPRESSION           = CONTENT_VERSION_ALIAS + ".VerValue like {0}";
 	//private static final String FREETEXT_EXPRESSION           = CONTENT_VERSION_ALIAS + ".versionValue like {0}";
 	private static final String FREETEXT_EXPRESSION_VARIABLE  = "%<{0}><![CDATA[%{1}%]]></{0}>%";
 	
 	private final List contentTypeDefinitionVOs;
 	private final LanguageVO languageVO;
 	private final CategoryConditions categories;
 	private final List xmlAttributes;
 	private final String freetext;
 	private Integer stateId;
 
 	//
 	private final Map uniqueCategoryTableKeys = new HashMap();
 	
 	//
 	private String sql;
 	private List bindings;
 	private int bindingsCounter;
 	
 	
 	/**
 	 *
 	 */
 	public SqlBuilder(final Integer stateId, final List contentTypeDefinitionVOs, final LanguageVO languageVO, final CategoryConditions categories, final List xmlAttributes, final String freetext) {
 		super();
 		this.stateId                  = stateId;
 		this.languageVO               = languageVO;
 		this.contentTypeDefinitionVOs = contentTypeDefinitionVOs;
 		this.categories               = categories;
 		this.xmlAttributes            = xmlAttributes;
 		this.freetext                 = freetext;
 		this.bindings                 = new ArrayList();
 		
 		this.sql = generate();
 		System.out.println("this.stateId=" + this.stateId);
 		System.out.println("======================================================================");
 		System.out.println("#" + sql + "#");
 		System.out.println("======================================================================");
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
 	private String generate() {
		return "CALL SQL" + SPACE + generateSelectClause() + SPACE + generateFromClause() + SPACE + generateWhereClause() + " AS " + ContentImpl.class.getName();
 	}
 	
 	/**
 	 * 
 	 */
 	private String generateSelectClause() {
 		return 	SELECT_KEYWORD + SPACE + 
 		CONTENT_ALIAS + ".ContId" +
 		COMMA + CONTENT_ALIAS + ".name" +
 		COMMA + CONTENT_ALIAS + ".publishDateTime" +
 		COMMA + CONTENT_ALIAS + ".expireDateTime" +
 		COMMA + CONTENT_ALIAS + ".isBranch" +
 		COMMA + CONTENT_ALIAS + ".isProtected" +
 		COMMA + CONTENT_ALIAS + ".contentTypeDefId" +
 		COMMA + CONTENT_ALIAS + ".parentContId" +
 		COMMA + CONTENT_ALIAS + ".repositoryId" +
 		COMMA + CONTENT_ALIAS + ".parentContId" +
 		COMMA + CONTENT_ALIAS + ".ContId" +
 		COMMA + CONTENT_ALIAS + ".creator";
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
 		return freetext != null && freetext.length() > 0 && xmlAttributes != null && !xmlAttributes.isEmpty();
 	}
 	
 	/**
 	 * 
 	 */
 	private List getContentWhereClauses() {
 		final List clauses = new ArrayList();
 
 		clauses.add(CV_ACTIVE_CLAUSE);
 		clauses.add(CV_LATEST_VERSION_CLAUSE);
 		clauses.add(CV_CONTENT_JOIN);
 		clauses.add(MessageFormat.format(CV_STATE_CLAUSE, new Object[] { getBindingVariable() }));
 		bindings.add(stateId);
 
 		if(languageVO != null) {
 			clauses.add(MessageFormat.format(CV_LANGUAGE_CLAUSE, new Object[] { getBindingVariable() }));
 			bindings.add(languageVO.getId());
 		}
 		
 		return clauses;
 	}
 	
 	/**
 	 * 
 	 */
 	private String getContentVersionWhereClauses() {
 		final List expressions = new ArrayList();
 		if(contentTypeDefinitionVOs != null)
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
 		if(categories != null && categories.hasCondition())
 			clauses.add(categories.getWhereClauseOQL(bindings));
 		return clauses;
 	}
 
 	/**
 	 * 
 	 */
 	private String getFreetextWhereClause() {
 		final List expressions = new ArrayList();
 		if(xmlAttributes != null)
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
 		if(categories != null)
 			tables.addAll(categories.getFromClauseTables());
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
 		return "$" + (bindings.size() + 1);
 	}
 }
