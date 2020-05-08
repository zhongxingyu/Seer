 
 package edu.common.dynamicextensions.entitymanager;
 
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 import java.util.Map.Entry;
 
 import org.hibernate.HibernateException;
 
 import edu.common.dynamicextensions.dao.impl.DynamicExtensionDAO;
 import edu.common.dynamicextensions.domain.BaseAbstractAttribute;
 import edu.common.dynamicextensions.domain.BooleanAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.CategoryAttribute;
 import edu.common.dynamicextensions.domain.CategoryEntity;
 import edu.common.dynamicextensions.domain.DateAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.DomainObjectFactory;
 import edu.common.dynamicextensions.domain.PathAssociationRelationInterface;
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AbstractMetadataInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.BaseAbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryAssociationInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryEntityInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryInterface;
 import edu.common.dynamicextensions.domaininterface.DynamicExtensionBaseDomainObjectInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.PathInterface;
 import edu.common.dynamicextensions.domaininterface.PermissibleValueInterface;
 import edu.common.dynamicextensions.domaininterface.SemanticPropertyInterface;
 import edu.common.dynamicextensions.domaininterface.UserDefinedDEInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.common.dynamicextensions.util.global.DEConstants;
 import edu.wustl.dao.HibernateDAO;
 import edu.wustl.dao.JDBCDAO;
 import edu.wustl.dao.daofactory.DAOConfigFactory;
 import edu.wustl.dao.exception.DAOException;
 
 /**
  * @author rajesh_patil
  * @author mandar_shidhore
  * @author kunal_kamble
  */
 public class CategoryManager extends AbstractMetadataManager implements CategoryManagerInterface
 {
 
 	/**
 	 * Static instance of the CategoryManager.
 	 */
 	private static CategoryManagerInterface categoryManager = null;
 
 	/**
 	 * Static instance of the queryBuilder.
 	 */
 	private static DynamicExtensionBaseQueryBuilder queryBuilder = null;
 
 	/**
 	 * Instance of entity manager utility class
 	 */
 	EntityManagerUtil entityManagerUtil = new EntityManagerUtil();
 
 	/**
 	 * Empty Constructor.
 	 */
 	protected CategoryManager()
 	{
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * Returns the instance of the Entity Manager.
 	 * @return entityManager singleton instance of the Entity Manager.
 	 */
 	public static synchronized CategoryManagerInterface getInstance()
 	{
 		if (categoryManager == null)
 		{
 			categoryManager = new CategoryManager();
 			DynamicExtensionsUtility.initialiseApplicationVariables();
 			queryBuilder = QueryBuilderFactory.getQueryBuilder();
 		}
 
 		return categoryManager;
 	}
 
 	/**
 	 * LogFatalError.
 	 */
 	protected void logFatalError(Exception e, AbstractMetadataInterface abstractMetadata)
 	{
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * Method to persist a category.
 	 * @param categry interface for Category
 	 * @throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	 */
 	public CategoryInterface persistCategory(CategoryInterface categry)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		CategoryInterface category = (CategoryInterface) persistDynamicExtensionObject(categry);
 
 		return category;
 	}
 
 	/**
 	 * Method to persist category meta-data.
 	 * @param category interface for Category
 	 * @throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	 */
 	public CategoryInterface persistCategoryMetadata(CategoryInterface category)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		CategoryInterface savedCategory = (CategoryInterface) persistDynamicExtensionObjectMetdata(category);
 
 		return savedCategory;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.AbstractMetadataManager#preProcess(edu.common.dynamicextensions.domaininterface.DynamicExtensionBaseDomainObjectInterface, java.util.List, edu.wustl.common.dao.HibernateDAO, java.util.List)
 	 */
 	protected void preProcess(DynamicExtensionBaseDomainObjectInterface dyExtBsDmnObj,
 			List<String> revQueries, List<String> queries) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		CategoryInterface category = (CategoryInterface) dyExtBsDmnObj;
 
 		getDynamicQueryList(category, revQueries, queries);
 	}
 
 	/**
 	 * This method gets a list of dynamic tables creation queries.
 	 * @param category
 	 * @param revQueries
 	 * @param queries
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	protected List<String> getDynamicQueryList(CategoryInterface category, List<String> revQueries,
 			List<String> queries) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		List<CategoryEntityInterface> catEntities = new ArrayList<CategoryEntityInterface>();
 
 		// Use HashMap instead of List to ensure entity list contains unique entity only.
 		HashMap<String, CategoryEntityInterface> catEntObjects = new HashMap<String, CategoryEntityInterface>();
 		DynamicExtensionsUtility.getUnsavedCategoryEntityList(category.getRootCategoryElement(),
 				catEntObjects);
 		Iterator<String> keySetIter = catEntObjects.keySet().iterator();
 		while (keySetIter.hasNext())
 		{
 			catEntities.add(catEntObjects.get(keySetIter.next()));
 		}
 
 		for (CategoryEntityInterface categoryEntity : catEntities)
 		{
 			List<String> createQueries = queryBuilder.getCreateCategoryQueryList(categoryEntity,
 					revQueries);
 			if (createQueries != null && !createQueries.isEmpty())
 			{
 				queries.addAll(createQueries);
 			}
 		}
 
 		for (CategoryEntityInterface categoryEntity : catEntities)
 		{
 			List<String> createQueries = queryBuilder.getUpdateCategoryEntityQueryList(
 					categoryEntity, revQueries);
 			if (createQueries != null && !createQueries.isEmpty())
 			{
 				queries.addAll(createQueries);
 			}
 		}
 
 		// Use HashMap instead of List to ensure entity list contains unique entity only.
 		List<CategoryEntityInterface> savedCatEntities = new ArrayList<CategoryEntityInterface>();
 		catEntObjects = new HashMap<String, CategoryEntityInterface>();
 		DynamicExtensionsUtility.getSavedCategoryEntityList(category.getRootCategoryElement(),
 				catEntObjects);
 		keySetIter = catEntObjects.keySet().iterator();
 		while (keySetIter.hasNext())
 		{
 			savedCatEntities.add(catEntObjects.get(keySetIter.next()));
 		}
 		try
 		{
 			for (CategoryEntityInterface categoryEntity : savedCatEntities)
 			{				
 				CategoryEntity dbaseCopy = (CategoryEntity) DynamicExtensionsUtility.getCleanObject(CategoryEntity.class.getCanonicalName(),categoryEntity.getId());
 	
 				// Only for category entity for which table is getting created.
 				if (dbaseCopy.isCreateTable())
 				{
 					List<String> updateQueries = queryBuilder.getUpdateEntityQueryList(
 							(CategoryEntity) categoryEntity, dbaseCopy, revQueries);
 	
 					if (updateQueries != null && !updateQueries.isEmpty())
 					{
 						queries.addAll(updateQueries);
 					}
 				}
 			}
 		}
 		catch(DAOException exception)
 		{
 			throw new DynamicExtensionsSystemException("Not able to retrieve Object.",exception);
 		}		
 		return queries;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.AbstractMetadataManager#postProcess(java.util.List, java.util.List, java.util.Stack)
 	 */
 	protected void postProcess(List<String> queries, List<String> revQueries,
 			Stack<String> rlbkQryStack) throws DynamicExtensionsSystemException
 	{
 		queryBuilder.executeQueries(queries, revQueries, rlbkQryStack);
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.CategoryManagerInterface#insertData(edu.common.dynamicextensions.domaininterface.CategoryInterface, java.util.Map)
 	 */
 	public Long insertData(CategoryInterface category,
 			Map<BaseAbstractAttributeInterface, Object> dataValue, Long... userId)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		List<Map<BaseAbstractAttributeInterface, Object>> dataValMaps = new ArrayList<Map<BaseAbstractAttributeInterface, Object>>();
 		dataValMaps.add(dataValue);
 		Long id = ((userId != null || userId.length > 0) ? userId[0] : null);
 		List<Long> recordIds = insertData(category, dataValMaps, id);
 
 		return recordIds.get(0);
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.CategoryManagerInterface#insertData(edu.common.dynamicextensions.domaininterface.CategoryInterface, java.util.List)
 	 */
 	public List<Long> insertData(CategoryInterface category,
 			List<Map<BaseAbstractAttributeInterface, Object>> dataValMaps, Long... userId)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		List<Long> recordIds = new ArrayList<Long>();
 
 		JDBCDAO jdbcDao = null;
 		try
 		{
 			jdbcDao= DynamicExtensionsUtility.getJDBCDAO();
 			Long id = ((userId != null || userId.length > 0) ? userId[0] : null);
 
 			for (Map<BaseAbstractAttributeInterface, ?> dataValMap : dataValMaps)
 			{
 				Long recordId = insertDataForHierarchy(category.getRootCategoryElement(),
 						dataValMap, jdbcDao, id);
 				recordIds.add(recordId);
 			}
 			jdbcDao.commit();
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			throw (DynamicExtensionsApplicationException) handleRollback(e,
 					"Error while inserting data", jdbcDao, false);
 		}
 		catch (DAOException e)
 		{
 			throw (DynamicExtensionsSystemException) handleRollback(e,
 					"Error while inserting data", jdbcDao, true);
 		}
 		catch (HibernateException e)
 		{
 			throw (DynamicExtensionsSystemException) handleRollback(e,
 					"Error while inserting data", jdbcDao, true);
 		}
 		catch (SQLException e)
 		{
 			throw (DynamicExtensionsSystemException) handleRollback(e,
 					"Error while inserting data", jdbcDao, true);
 		}
 		catch (ParseException e)
 		{
 			throw (DynamicExtensionsSystemException) handleRollback(e,
 					"Error while inserting data", jdbcDao, true);
 		}
 		catch (IOException e)
 		{
 			throw (DynamicExtensionsSystemException) handleRollback(e,
 					"Error while inserting data", jdbcDao, true);
 		}
 		finally
 		{
 			try
 			{
 				DynamicExtensionsUtility.closeJDBCDAO(jdbcDao);
 			}
 			catch (DAOException e)
 			{
 				throw (DynamicExtensionsSystemException) handleRollback(e, "Error while closing",
 						jdbcDao, true);
 			}
 		}
 
 		return recordIds;
 	}
 
 	/**
 	 * This method inserts the data for hierarchy of category entities one by one.
 	 * @param catEntity
 	 * @param dataValue
 	 * @param jdbcDAO
 	 * @param userId
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws HibernateException
 	 * @throws SQLException
 	 * @throws DAOException
 	 * @throws UserNotAuthorizedException
 	 * @throws ParseException
 	 * @throws IOException
 	 */
 	private Long insertDataForHierarchy(CategoryEntityInterface catEntity,
 			Map<BaseAbstractAttributeInterface, ?> dataValue, JDBCDAO jdbcDao, Long... userId)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException,
 			HibernateException, SQLException, DAOException,
 			ParseException, IOException
 	{
 		List<CategoryEntityInterface> catEntities = getParentEntityList(catEntity);
 		Map<CategoryEntityInterface, Map<BaseAbstractAttributeInterface, Object>> entityValMap = initialiseEntityValueMap(dataValue);
 		Long parentRecId = null;
 		Long parntCatRecId = null;
 		Long id = ((userId != null || userId.length > 0) ? userId[0] : null);
 
 		for (CategoryEntityInterface categoryEntity : catEntities)
 		{
 			Map<BaseAbstractAttributeInterface, Object> valueMap = entityValMap.get(categoryEntity);
 
 			// If parent category entity table not created, then add its attribute map to value map.
 			CategoryEntity parntCatEntity = (CategoryEntity) categoryEntity
 					.getParentCategoryEntity();
 			while (parntCatEntity != null && !parntCatEntity.isCreateTable())
 			{
 				Map<BaseAbstractAttributeInterface, Object> innerValMap = entityValMap
 						.get(parntCatEntity);
 
 				if (innerValMap != null)
 				{
 					valueMap.putAll(innerValMap);
 				}
 
 				parntCatEntity = (CategoryEntity) parntCatEntity.getParentCategoryEntity();
 			}
 
 			parentRecId = insertDataForSingleCategoryEntity(categoryEntity, valueMap, jdbcDao,
 					parentRecId, id);
 			parntCatRecId = getRootCategoryRecordId(categoryEntity, parentRecId);
 		}
 
 		return parntCatRecId;
 	}
 
 	/**
 	 * @param catEntity
 	 * @param parentRecId
 	 * @return
 	 * @throws SQLException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private Long getRootCategoryRecordId(CategoryEntityInterface catEntity, Long parentRecId)
 			throws SQLException, DynamicExtensionsSystemException
 	{
 		CategoryInterface category = catEntity.getCategory();
 		CategoryEntityInterface rootCatEntity = category.getRootCategoryElement();
 
 		StringBuffer query = new StringBuffer();
 		query.append(SELECT_KEYWORD + WHITESPACE + IDENTIFIER + WHITESPACE + FROM_KEYWORD
 				+ WHITESPACE + rootCatEntity.getTableProperties().getName() + WHITESPACE
 				+ WHERE_KEYWORD + WHITESPACE + RECORD_ID + EQUAL + parentRecId);
 
 		Long rootCERecId = null;
 
 		List<Long> results = getResultIDList(query.toString(), IDENTIFIER);
 		if (!results.isEmpty())
 		{
 			rootCERecId = (Long) results.get(0);
 		}
 
 		return rootCERecId;
 	}
 
 	/**
 	 * This method inserts data for a single category entity.
 	 * @param catEntity
 	 * @param dataValue
 	 * @param jdbcDAO
 	 * @param parentRecId
 	 * @param userId
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws SQLException
 	 * @throws DAOException
 	 * @throws UserNotAuthorizedException
 	 * @throws ParseException
 	 * @throws IOException
 	 */
 	private Long insertDataForSingleCategoryEntity(CategoryEntityInterface catEntity,
 			Map<BaseAbstractAttributeInterface, Object> dataValue,JDBCDAO jdbcDao,
 			Long parentRecId, Long... userId) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException, SQLException, DAOException,
 			ParseException, IOException
 	{
 		Long identifier = null;
 
 		Map<String, Long> keyMap = new HashMap<String, Long>();
 		Map<String, Long> fullKeyMap = new HashMap<String, Long>();
 		Map<String, List<Long>> records = new HashMap<String, List<Long>>();
 		Long id = ((userId != null || userId.length > 0) ? userId[0] : null);
 		CategoryInterface category = catEntity.getCategory();
 
 		if (catEntity == null)
 		{
 			throw new DynamicExtensionsSystemException("Input to insert data is null");
 		}
 
 		CategoryEntityInterface rootCatEntity = category.getRootCategoryElement();
 		String rootCatEntName = DynamicExtensionsUtility.getCategoryEntityName(rootCatEntity
 				.getName());
 
 		// If root category entity does not have any attribute, or all its category attributes
 		// are related attributes, then explicitly insert identifier into entity table and
 		// insert this identifier as record identifier in category entity table.
 		if (rootCatEntity.getCategoryAttributeCollection().size() == 0
 				|| isAllRelatedCategoryAttributesCollection(rootCatEntity))
 		{
 			// Insert blank record in all parent entity tables of root category entity so use insertDataForHeirarchy and add all keys to keymap, recordmap, fullkeymap.
 			Map<AbstractAttributeInterface, Object> attributes = new HashMap<AbstractAttributeInterface, Object>();
 			EntityManagerInterface entityManager = EntityManager.getInstance();
 			Long entityId = entityManager.insertDataForHeirarchy(catEntity.getEntity(), attributes,
 					jdbcDao, id);
 			keyMap.put(rootCatEntName, entityId);
 			fullKeyMap.put(rootCatEntName, entityId);
 
 			List<Long> identifiers = new ArrayList<Long>();
 			identifiers.add(entityId);
 			records.put(rootCatEntName, identifiers);
 
 			while (catEntity.getParentCategoryEntity() != null)
 			{
 				String parentCatEntName = DynamicExtensionsUtility.getCategoryEntityName(catEntity
 						.getParentCategoryEntity().getName());
 				keyMap.put(parentCatEntName, entityId);
 				fullKeyMap.put(parentCatEntName, entityId);
 
 				List<Long> recordIds = records.get(parentCatEntName);
 				if (recordIds == null)
 				{
 					recordIds = new ArrayList<Long>();
 				}
 
 				recordIds.add(entityId);
 				records.put(parentCatEntName, recordIds);
 				catEntity = catEntity.getParentCategoryEntity();
 			}
 
 			Long catEntId = entityManagerUtil.getNextIdentifier(rootCatEntity.getTableProperties()
 					.getName());
 
 			// Query to insert record into category entity table.
 			String insertQuery = "INSERT INTO " + rootCatEntity.getTableProperties().getName()
 					+ " (IDENTIFIER, ACTIVITY_STATUS, " + RECORD_ID + ") VALUES (" + catEntId
 					+ ", 'ACTIVE', " + entityId + ")";
 
 			executeUpdateQuery(insertQuery, id, jdbcDao);
 			logDebug("insertData", "categoryEntityTableInsertQuery is : " + insertQuery);
 		}
 
 		boolean areMultplRecrds = false;
 		boolean isNoCatAttrPrsnt = false;
 
 		String entityFKColName = null;
 		String catEntFKColName = null;
 		Long srcCatEntityId = null;
 		Long srcEntityId = null;
 
 		// Separate out category attribute and category association. The map coming from UI can contain both in any order, 
 		// but we require to insert record for category attribute first for root category entity.
 		Map<CategoryAttributeInterface, Object> catAttributes = new HashMap<CategoryAttributeInterface, Object>();
 		Map<CategoryAssociationInterface, Object> catAssociations = new HashMap<CategoryAssociationInterface, Object>();
 
 		Set<BaseAbstractAttributeInterface> keySet = dataValue.keySet();
 		Iterator<BaseAbstractAttributeInterface> iter = keySet.iterator();
 		while (iter.hasNext())
 		{
 			Object obj = iter.next();
 			if (obj instanceof CategoryAttributeInterface)
 			{
 				catAttributes.put((CategoryAttributeInterface) obj, dataValue.get(obj));
 			}
 			else
 			{
 				catAssociations.put((CategoryAssociationInterface) obj, dataValue.get(obj));
 			}
 		}
 
 		// Insert records for category attributes.
 		insertRecordsForCategoryEntityTree(entityFKColName, catEntFKColName, srcCatEntityId,
 				srcEntityId, catEntity, catAttributes, keyMap, fullKeyMap, records,
 				areMultplRecrds, isNoCatAttrPrsnt, jdbcDao, id);
 
 		// Insert records for category associations.
 		insertRecordsForCategoryEntityTree(entityFKColName, catEntFKColName, srcCatEntityId,
 				srcEntityId, catEntity, catAssociations, keyMap, fullKeyMap, records,
 				areMultplRecrds, isNoCatAttrPrsnt, jdbcDao, id);
 
 		Long rootCERecId = getRootCategoryEntityRecordId(category.getRootCategoryElement(),
 				(Long) fullKeyMap.get(rootCatEntName));
 
 		insertRecordsForRelatedAttributes(rootCERecId, category.getRootCategoryElement(), records,
 				jdbcDao, id);
 
 		if (parentRecId != null)
 		{
 			identifier = parentRecId;
 		}
 		else
 		{
 			identifier = (Long) keyMap.get(rootCatEntName);
 		}
 
 		return identifier;
 	}
 
 	/**
 	 * This method checks whether category attributes in a category entity's 
 	 * parent are related attributes or not. It true, it inserts records for the same first.
 	 * @param rootCatEntity
 	 * @param records
 	 * @param jdbcDAO
 	 * @param userId
 	 * @throws SQLException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private void insertAllParentRelatedCategoryAttributesCollection(
 			CategoryEntityInterface rootCatEntity, Map<String, List<Long>> records,
 			JDBCDAO jdbcDao, Long userId) throws SQLException, DynamicExtensionsSystemException
 	{
 		// Get all attributes including parent's attributes.
 		Collection<CategoryAttributeInterface> catAttributes = rootCatEntity
 				.getAllCategoryAttributes();
 		for (CategoryAttributeInterface catAttribute : catAttributes)
 		{
 			if (catAttribute.getIsRelatedAttribute() != null
 					&& catAttribute.getIsRelatedAttribute())
 			{
 				// In case of related attributes, check if it is parent's attribute.
 				if (!catAttribute.getAbstractAttribute().getEntity().equals(
 						rootCatEntity.getEntity()))
 				{
 					StringBuffer columnNames = new StringBuffer();
 					StringBuffer columnValues = new StringBuffer();
 					StringBuffer columnNamesValues = new StringBuffer();
 
 					AttributeInterface attribute = catAttribute.getAbstractAttribute().getEntity()
 							.getAttributeByName(catAttribute.getAbstractAttribute().getName());
 
 					// Fetch column names and column values for related category attributes.
 					populateColumnNamesAndValues(attribute, catAttribute, columnNames,
 							columnValues, columnNamesValues);
 
 					String entTableName = attribute.getEntity().getTableProperties().getName();
 					List<Long> recordIds = records.get(DynamicExtensionsUtility
 							.getCategoryEntityName(rootCatEntity.getName()));
 					if (recordIds != null && columnNamesValues.length() > 0)
 					{
 						for (Long identifer : recordIds)
 						{
 							// Update entity query.
 							String query = "UPDATE " + entTableName + " SET " + columnNamesValues
 									+ " WHERE IDENTIFIER = " + identifer;
 							executeUpdateQuery(query, userId, jdbcDao);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * This method checks if all category attributes in a category entity are related attributes.
 	 * @param catEntity
 	 * @return true if all category attributes are related attributes, false otherwise
 	 */
 	private boolean isAllRelatedCategoryAttributesCollection(CategoryEntityInterface catEntity)
 	{
 		Collection<CategoryAttributeInterface> catAttributes = catEntity.getAllCategoryAttributes();
 		boolean flag = true;
 		for (CategoryAttributeInterface catAttribute : catAttributes)
 		{
 			if (catAttribute.getIsRelatedAttribute() == null
 					|| catAttribute.getIsRelatedAttribute() == false)
 			{
 				flag = false;
 				break;
 			}
 		}
 
 		return flag;
 	}
 
 	/**
 	 * This method checks whether all attributes are invisible type related attributes 
 	 * or visible type related attributes or all are normal attribute.
 	 * @param catEntity
 	 * @return
 	 */
 	private boolean isAllRelatedInvisibleCategoryAttributesCollection(
 			CategoryEntityInterface catEntity)
 	{
 		Collection<CategoryAttributeInterface> catAttributes = catEntity.getAllCategoryAttributes();
 
 		if (catAttributes != null && catAttributes.isEmpty())
 		{
 			return false;
 		}
 
 		for (CategoryAttributeInterface catAttribute : catAttributes)
 		{
 			if (catAttribute.getIsRelatedAttribute() == null
 					|| catAttribute.getIsRelatedAttribute() == false)
 			{
 				return false;
 			}
 			else if (catAttribute.getIsRelatedAttribute() == true
 					&& (catAttribute.getIsVisible() != null && catAttribute.getIsVisible() == true))
 			{
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * Insert records for related attributes in each category entity.
 	 * @param rootRecordId
 	 * @param rootCatEntity
 	 * @param records
 	 * @param jdbcDAO
 	 * @param identifier
 	 * @throws SQLException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private void insertRecordsForRelatedAttributes(Long rootRecordId,
 			CategoryEntityInterface rootCatEntity, Map<String, List<Long>> records,
 			JDBCDAO jdbcDao, Long identifier) throws SQLException, DynamicExtensionsSystemException
 	{
 		CategoryInterface category = rootCatEntity.getCategory();
 
 		// Get a collection of category entities having a collection of related attributes.
 		Collection<CategoryEntityInterface> catEntWithRA = category
 				.getRelatedAttributeCategoryEntityCollection();
 
 		// Call this method for root category entity's parent's related attributes.
 		insertAllParentRelatedCategoryAttributesCollection(rootCatEntity, records, jdbcDao,
 				identifier);
 
 		for (CategoryEntityInterface categoryEntity : catEntWithRA)
 		{
 			StringBuffer columnNames = new StringBuffer();
 			StringBuffer columnValues = new StringBuffer();
 			StringBuffer colNamesValues = new StringBuffer();
 
 			// Fetch column names and column values for related category attributes.
 			getColumnNamesAndValuesForRelatedCategoryAttributes(categoryEntity, columnNames,
 					columnValues, colNamesValues, records, jdbcDao, identifier);
 
 			CategoryAssociationInterface catAssociation = getCategoryAssociationWithRootCategoryEntity(
 					rootCatEntity, categoryEntity);
 
 			if (catAssociation == null)
 			{
 				// Pass the category entity, which is parent category entity of  
 				// root category entity so we have to insert into parent entity table.
 				insertRelatedAttributeRecordsForRootCategoryEntity(categoryEntity, colNamesValues,
 						records, jdbcDao, identifier);
 			}
 			else
 			{
 				insertRelatedAttributeRecordsForCategoryEntity(categoryEntity, catAssociation,
 						columnNames, columnValues, colNamesValues, rootRecordId, records, jdbcDao,
 						identifier);
 			}
 		}
 	}
 
 	/**
 	 * This method clubs column names and column values for related category attributes.
 	 * @param catEntity
 	 * @param columnNames
 	 * @param columnValues
 	 * @param colNamesValues
 	 * @throws DynamicExtensionsSystemException
 	 * @throws SQLException 
 	 */
 	private void getColumnNamesAndValuesForRelatedCategoryAttributes(
 			CategoryEntityInterface catEntity, StringBuffer columnNames, StringBuffer columnValues,
 			StringBuffer colNamesValues, Map<String, List<Long>> records, JDBCDAO jdbcDao,
 			Long userId) throws DynamicExtensionsSystemException, SQLException
 	{
 		Collection<CategoryAttributeInterface> catAttributes = new HashSet<CategoryAttributeInterface>();
 		catAttributes = catEntity.getCategoryAttributeCollection();
 		for (CategoryAttributeInterface catAttribute : catAttributes)
 		{
 			if (catAttribute.getIsRelatedAttribute() != null
 					&& catAttribute.getIsRelatedAttribute() == true)
 			{
 				AttributeInterface attribute = catAttribute.getAbstractAttribute().getEntity()
 						.getAttributeByName(catAttribute.getAbstractAttribute().getName());
 
 				if (catAttribute.getAbstractAttribute() instanceof AssociationInterface)
 				{
 					populateAndInsertRecordForRelatedMultiSelectCategoryAttribute(catAttribute,
 							attribute, records, userId, jdbcDao);
 				}
 				else
 				{
 					populateColumnNamesAndValues(attribute, catAttribute, columnNames,
 							columnValues, colNamesValues);
 				}
 			}
 		}
 	}
 
 	/**
 	 * This method populates and inserts records for related multiselect category attribute.
 	 * @param catAttribute
 	 * @param attribute
 	 * @param records
 	 * @param userId
 	 * @param jdbcDAO
 	 * @throws DynamicExtensionsSystemException
 	 * @throws SQLException
 	 */
 	private void populateAndInsertRecordForRelatedMultiSelectCategoryAttribute(
 			CategoryAttributeInterface catAttribute, AttributeInterface attribute,
 			Map<String, List<Long>> records, Long userId, JDBCDAO jdbcDao)
 			throws DynamicExtensionsSystemException, SQLException
 	{
 		AssociationInterface association = (AssociationInterface) catAttribute
 				.getAbstractAttribute();
 		EntityInterface targetEntity = association.getTargetEntity();
 		String tgtEntTblName = targetEntity.getTableProperties().getName();
 		List<Long> ids = records.get(catAttribute.getCategoryEntity().getName());
 		for (Long id : ids)
 		{
 			Long identifier = entityManagerUtil.getNextIdentifier(tgtEntTblName);
 
 			// Query to insert record into entity table of multiselect attribute.
 			String insertQuery = "INSERT INTO " + tgtEntTblName + " (IDENTIFIER, ACTIVITY_STATUS, "
 					+ attribute.getColumnProperties().getName() + ") VALUES (" + identifier
 					+ ", 'ACTIVE', " + "'" + catAttribute.getDefaultValue() + "'" + ")";
 
 			executeUpdateQuery(insertQuery, userId, jdbcDao);
 
 			// Query to update record in entity table of multiselect attribute.
 			String updateQuery = "UPDATE "
 					+ tgtEntTblName
 					+ " SET "
 					+ association.getConstraintProperties().getTgtEntityConstraintKeyProperties()
 							.getTgtForiegnKeyColumnProperties().getName() + " = " + id
 					+ " WHERE IDENTIFIER = " + identifier;
 
 			executeUpdateQuery(updateQuery, userId, jdbcDao);
 		}
 	}
 
 	/**
 	 * @param attribute
 	 * @param catAttribute
 	 * @param columnNames
 	 * @param columnValues
 	 * @param colNamesValues
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private void populateColumnNamesAndValues(AttributeInterface attribute,
 			CategoryAttributeInterface catAttribute, StringBuffer columnNames,
 			StringBuffer columnValues, StringBuffer colNamesValues)
 			throws DynamicExtensionsSystemException
 	{
 		String columnName = attribute.getColumnProperties().getName();
 		AttributeTypeInformationInterface attrTypeInfo = attribute.getAttributeTypeInformation();
 
 		// If attribute type information is date type whose default value is not getting set 
 		// as if it is read only then skip it, do not set in query.
 		if (!(attrTypeInfo instanceof DateAttributeTypeInformation && catAttribute
 				.getDefaultValue() == null))
 		{
 			if (columnNames.toString().length() > 0)
 			{
 				columnNames.append(", ");
 				columnValues.append(", ");
 				colNamesValues.append(", ");
 			}
 
 			String defaultValue = catAttribute.getDefaultValue();
 
 			if (attrTypeInfo instanceof BooleanAttributeTypeInformation)
 			{
 				defaultValue = DynamicExtensionsUtility
 						.getValueForCheckBox(DynamicExtensionsUtility
 								.isCheckBoxChecked(defaultValue));
 			}
 
 			// Replace any single and double quotes value with a proper escape character.
 			defaultValue = DynamicExtensionsUtility.getEscapedStringValue(defaultValue);
 			columnNames.append(columnName);
 			String appName=DynamicExtensionDAO.getInstance().getAppName();
 			String dbType=DAOConfigFactory.getInstance().getDAOFactory(appName).getDataBaseType();
 			if (dbType.equals(DEConstants.ORACLE_DATABASE)
 					&& attrTypeInfo instanceof DateAttributeTypeInformation)
 			{
 
 				columnValues.append(DynamicExtensionsUtility
 						.getDefaultDateForRelatedCategoryAttribute(attribute, catAttribute
 								.getDefaultValue()));
 				colNamesValues.append(columnName);
 				colNamesValues.append(" = ");
 				colNamesValues.append(DynamicExtensionsUtility
 						.getDefaultDateForRelatedCategoryAttribute(attribute, catAttribute
 								.getDefaultValue()));
 			}
 			else
 			{
 				columnValues.append("'" + defaultValue + "'");
 				colNamesValues.append(columnName);
 				colNamesValues.append(" = '");
 				colNamesValues.append(defaultValue + "'");
 			}
 		}
 	}
 
 	/**
 	 * This method returns a category association between root category entity 
 	 * and category entity passed to this method.
 	 * @param rootCatEntity
 	 * @param catEntity
 	 * @return category association between root category entity and category entity passed
 	 */
 	private CategoryAssociationInterface getCategoryAssociationWithRootCategoryEntity(
 			CategoryEntityInterface rootCatEntity, CategoryEntityInterface catEntity)
 	{
 		Collection<CategoryAssociationInterface> catAssociations = rootCatEntity
 				.getCategoryAssociationCollection();
 
 		for (CategoryAssociationInterface catAssociation : catAssociations)
 		{
 			if (catAssociation.getTargetCategoryEntity() != null
 					&& catAssociation.getTargetCategoryEntity().equals(catEntity))
 			{
 				return catAssociation;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * This method inserts records for related category attributes of root category entity.
 	 * @param rootCatEntity
 	 * @param colNamesValues
 	 * @param records
 	 * @param jdbcDAO
 	 * @param userId
 	 * @throws SQLException
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	private void insertRelatedAttributeRecordsForRootCategoryEntity(
 			CategoryEntityInterface rootCatEntity, StringBuffer colNamesValues,
 			Map<String, List<Long>> records, JDBCDAO jdbcDao, Long userId) throws DynamicExtensionsSystemException
 	{
 		String entTableName = rootCatEntity.getEntity().getTableProperties().getName();
 		List<Long> recordIds = records.get(DynamicExtensionsUtility
 				.getCategoryEntityName(rootCatEntity.getName()));
 		if (recordIds != null && colNamesValues.length() > 0)
 		{
 			for (Long identifer : recordIds)
 			{
 				// Update entity query.
 				String query = "UPDATE " + entTableName + " SET " + colNamesValues
 						+ " WHERE IDENTIFIER = " + identifer;
 				executeUpdateQuery(query, userId, jdbcDao);
 			}
 		}
 	}
 
 	/**
 	 * This method inserts records for related category attributes of category entities 
 	 * other than root category entity.
 	 * @param catEntity
 	 * @param catAssociation
 	 * @param columnNames
 	 * @param columnValues
 	 * @param colNamesValues
 	 * @param rootRecId
 	 * @param records
 	 * @param jdbcDAO
 	 * @param userId
 	 * @throws SQLException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private void insertRelatedAttributeRecordsForCategoryEntity(CategoryEntityInterface catEntity,
 			CategoryAssociationInterface catAssociation, StringBuffer columnNames,
 			StringBuffer columnValues, StringBuffer colNamesValues, Long rootRecId,
 			Map<String, List<Long>> records, JDBCDAO jdbcDao, Long userId) throws SQLException,
 			DynamicExtensionsSystemException
 	{
 		String catEntTblName = catEntity.getTableProperties().getName();
 		String entTblName = catEntity.getEntity().getTableProperties().getName();
 		String catEntFK = catAssociation.getConstraintProperties()
 				.getTgtEntityConstraintKeyProperties().getTgtForiegnKeyColumnProperties().getName();
 
 		List<Long> srcEntityId = records.get(DynamicExtensionsUtility
 				.getCategoryEntityName(catAssociation.getCategoryEntity().getName()));
 		String catEntityName = DynamicExtensionsUtility.getCategoryEntityName(catEntity.getName());
 		if (records.get(catEntityName) != null)
 		{
 			if (colNamesValues.length() > 0)
 			{
 				List<Long> recordIds = records.get(catEntityName);
 				for (Long id : recordIds)
 				{
 					String updateEntQuery = "UPDATE " + entTblName + " SET " + colNamesValues
 							+ " WHERE IDENTIFIER = " + id;
 					executeUpdateQuery(updateEntQuery, userId, jdbcDao);
 
 					String selectQuery = "SELECT IDENTIFIER FROM " + catEntTblName + " WHERE "
 							+ RECORD_ID + " = " + id;
 
 					List<Long> resultIds = getResultIDList(selectQuery, "IDENTIFIER");
 					if (resultIds.isEmpty())
 					{
 						Long catEntId = entityManagerUtil.getNextIdentifier(catEntTblName);
 
 						// Insert query for category entity table.
 						String insertQuery = "INSERT INTO " + catEntTblName
 								+ " (IDENTIFIER, ACTIVITY_STATUS, " + RECORD_ID + ", " + catEntFK
 								+ ") VALUES (" + catEntId + ", 'ACTIVE', " + id + ", " + rootRecId
 								+ ")";
 						executeUpdateQuery(insertQuery, userId, jdbcDao);
 					}
 				}
 			}
 		}
 		// If all attributes are invisible type related attributes, then only insert explicitly, 
 		// in all other cases the category entity name must available in map of records.  
 		else if (records.get(catEntityName) == null
 				&& isAllRelatedInvisibleCategoryAttributesCollection(catEntity))
 		{
 			PathInterface path = catEntity.getPath();
 
 			Collection<PathAssociationRelationInterface> pathAssoRel = path
 					.getSortedPathAssociationRelationCollection();
 			for (PathAssociationRelationInterface par : pathAssoRel)
 			{
 				AssociationInterface association = par.getAssociation();
 
 				if (association.getTargetEntity().getId() != catEntity.getEntity().getId())
 				{
 					if (records.get(association.getTargetEntity().getName() + "["
 							+ par.getTargetInstanceId() + "]") == null)
 					{
 						srcEntityId = new ArrayList<Long>();
 						for (Long sourceId : srcEntityId)
 						{
 							Long entityId = entityManagerUtil.getNextIdentifier(association
 									.getTargetEntity().getTableProperties().getName());
 
 							String insertQuery = "INSERT INTO "
 									+ association.getTargetEntity().getTableProperties().getName()
 									+ "(IDENTIFIER, ACTIVITY_STATUS, "
 									+ association.getConstraintProperties()
 											.getTgtEntityConstraintKeyProperties()
 											.getTgtForiegnKeyColumnProperties().getName()
 									+ ") VALUES (" + entityId + ", 'ACTIVE'," + sourceId + ")";
 							executeUpdateQuery(insertQuery, userId, jdbcDao);
 
 							srcEntityId.add(entityId);
 						}
 
 						records.put(association.getTargetEntity().getName() + "["
 								+ par.getTargetInstanceId() + "]", srcEntityId);
 					}
 					else
 					{
 						srcEntityId = records.get(association.getTargetEntity().getName() + "["
 								+ par.getTargetInstanceId() + "]");
 					}
 				}
 				else
 				{
 					for (Long sourceId : srcEntityId)
 					{
 						Long entityId = entityManagerUtil.getNextIdentifier(entTblName);
 
 						// Insert query for entity table.
 						String insQryForEnt = "INSERT INTO "
 								+ entTblName
 								+ " (IDENTIFIER, ACTIVITY_STATUS, "
 								+ columnNames
 								+ ", "
 								+ association.getConstraintProperties()
 										.getTgtEntityConstraintKeyProperties()
 										.getTgtForiegnKeyColumnProperties().getName()
 								+ ") VALUES (" + entityId + ", " + "'ACTIVE', " + columnValues
 								+ ", " + sourceId + ")";
 						executeUpdateQuery(insQryForEnt, userId, jdbcDao);
 
 						Long catEntId = entityManagerUtil.getNextIdentifier(catEntTblName);
 
 						// Insert query for category entity table.
 						String insQryForCatEnt = "INSERT INTO " + catEntTblName
 								+ " (IDENTIFIER, ACTIVITY_STATUS, " + RECORD_ID + ", " + catEntFK
 								+ ") VALUES (" + catEntId + ", 'ACTIVE', " + entityId + ", "
 								+ rootRecId + ")";
 						executeUpdateQuery(insQryForCatEnt, userId, jdbcDao);
 					}
 				}
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.CategoryManagerInterface#editData(edu.common.dynamicextensions.domaininterface.CategoryEntityInterface, java.util.Map, java.lang.Long)
 	 */
 	public boolean editData(CategoryEntityInterface rootCatEntity,
 			Map<BaseAbstractAttributeInterface, Object> dataValue, Long recordId, Long... userId)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException,
 			SQLException
 	{
 		CategoryInterface category = rootCatEntity.getCategory();
 
 		Long entityRecId = getRootCategoryEntityRecordId(rootCatEntity, recordId);
 		Long id = ((userId != null || userId.length > 0) ? userId[0] : null);
 		List<Long> entityRecIds = new ArrayList<Long>();
 		entityRecIds.add(entityRecId);
 
 		Map<AbstractAttributeInterface, Object> rootCERecords = new HashMap<AbstractAttributeInterface, Object>();
 		populateRootEntityRecordMap(rootCatEntity, rootCERecords, dataValue);
 
 		JDBCDAO jdbcDao = null;		
 
 		Boolean isEdited = false;
 
 		// Roll back queries for category entity records.
 		Stack<String> rlbkQryStack = new Stack<String>();
 
 		try
 		{
 			jdbcDao= DynamicExtensionsUtility.getJDBCDAO();
 
 			// Clear all records from entity table.
 			EntityManagerInterface entityManager = EntityManager.getInstance();
 			isEdited = entityManager.editDataForHeirarchy(rootCatEntity.getEntity(), rootCERecords,
 					entityRecId, jdbcDao, id);
 
 			// Clear all records from category entity table.
 			clearCategoryEntityData(rootCatEntity, recordId, rlbkQryStack, id, jdbcDao);
 
 			if (isEdited)
 			{
 				Map<String, Long> keyMap = new HashMap<String, Long>();
 				Map<String, Long> fullKeyMap = new HashMap<String, Long>();
 				Map<String, List<Long>> recordsMap = new HashMap<String, List<Long>>();
 				String catEntityName = DynamicExtensionsUtility.getCategoryEntityName(rootCatEntity
 						.getName());
 				keyMap.put(catEntityName, entityRecId);
 				fullKeyMap.put(catEntityName, entityRecId);
 				List<Long> identifiers = new ArrayList<Long>();
 				identifiers.add(entityRecId);
 				recordsMap.put(catEntityName, identifiers);
 
 				CategoryEntityInterface catEntity = rootCatEntity;
 
 				// Add parent's record id also as parent entity tables are edited 
 				// in editDataForHeirarchy method.
 				while (catEntity.getParentCategoryEntity() != null)
 				{
 					String parentCateEntName = DynamicExtensionsUtility
 							.getCategoryEntityName(catEntity.getParentCategoryEntity().getName());
 					keyMap.put(parentCateEntName, entityRecId);
 					fullKeyMap.put(parentCateEntName, entityRecId);
 
 					List<Long> recordIds = recordsMap.get(parentCateEntName);
 					if (recordIds == null)
 					{
 						recordIds = new ArrayList<Long>();
 					}
 					recordIds.add(entityRecId);
 					recordsMap.put(parentCateEntName, recordIds);
 
 					catEntity = catEntity.getParentCategoryEntity();
 				}
 
 				for (CategoryAttributeInterface catAttribute : rootCatEntity
 						.getAllCategoryAttributes())
 				{
 					dataValue.remove(catAttribute);
 				}
 
 				boolean areMultplRecrds = false;
 				boolean isNoCatAttrPrsnt = false;
 
 				String entityFKColName = null;
 				String catEntFKColName = null;
 				Long srcCatEntityId = null;
 				Long srcEntityId = null;
 
 				insertRecordsForCategoryEntityTree(entityFKColName, catEntFKColName,
 						srcCatEntityId, srcEntityId, rootCatEntity, dataValue, keyMap, fullKeyMap,
 						recordsMap, areMultplRecrds, isNoCatAttrPrsnt, jdbcDao, id);
 
 				Long rootCatEntId = getRootCategoryEntityRecordId(
 						category.getRootCategoryElement(), (Long) fullKeyMap.get(catEntityName));
 
 				insertRecordsForRelatedAttributes(rootCatEntId, category.getRootCategoryElement(),
 						recordsMap, jdbcDao, id);
 				jdbcDao.commit();
 			}
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			throw (DynamicExtensionsApplicationException) handleRollback(e,
 					"Error while inserting data", jdbcDao, false);
 		}
 		catch (DAOException e)
 		{
 			throw (DynamicExtensionsSystemException) handleRollback(e,
 					"Error while inserting data", jdbcDao, true);
 		}
 		catch (HibernateException e)
 		{
 			throw (DynamicExtensionsSystemException) handleRollback(e,
 					"Error while inserting data", jdbcDao, true);
 		}
 		catch (ParseException e)
 		{
 			throw (DynamicExtensionsSystemException) handleRollback(e,
 					"Error while inserting data", jdbcDao, true);
 		}
 		catch (IOException e)
 		{
 			throw (DynamicExtensionsSystemException) handleRollback(e,
 					"Error while inserting data", jdbcDao, true);
 		}
 		finally
 		{
 			try
 			{
 				DynamicExtensionsUtility.closeJDBCDAO(jdbcDao);
 			}
 			catch (DAOException e)
 			{
 				throw (DynamicExtensionsSystemException) handleRollback(e, "Error while closing",
 						jdbcDao, true);
 			}
 		}
 
 		return isEdited;
 	}
 
 	/**
 	 * This helper method recursively inserts records for a single category entity
 	 * and all its category associations i.e. in turn for a whole category entity tree.
 	 * @param entityFKColName
 	 * @param catEntFKColName
 	 * @param srcCatEntityId
 	 * @param srcEntityId
 	 * @param categoryEnt
 	 * @param dataValue
 	 * @param keyMap
 	 * @param fullKeyMap
 	 * @param areMultplRecrds
 	 * @param isNoCatAttrPrsnt
 	 * @param jdbcDAO
 	 * @throws DynamicExtensionsSystemException
 	 * @throws SQLException
 	 * @throws HibernateException
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws UserNotAuthorizedException
 	 * @throws DAOException
 	 * @throws IOException
 	 * @throws ParseException
 	 */
 	private void insertRecordsForCategoryEntityTree(String entityFKColName, String catEntFKColName,
 			Long srcCatEntityId, Long srcEntityId, CategoryEntityInterface categoryEnt,
 			Map dataValue, Map<String, Long> keyMap, Map<String, Long> fullKeyMap,
 			Map<String, List<Long>> records, boolean areMultplRecrds, boolean isNoCatAttrPrsnt,
 			JDBCDAO jdbcDao, Long userId) throws DynamicExtensionsSystemException, SQLException,
 			HibernateException, DynamicExtensionsApplicationException,
 			DAOException, ParseException, IOException
 	{
 		Object value = null;
 
 		// Variable to check if record has been inserted for category entity.
 		boolean isCatEntRecIns = false;
 		Map<AbstractAttributeInterface, Object> attributes = null;
 
 		Set uiColumnSet = dataValue.keySet();
 		Iterator uiColumnSetIter = uiColumnSet.iterator();
 
 		while (uiColumnSetIter.hasNext())
 		{
 			BaseAbstractAttribute attribute = (BaseAbstractAttribute) uiColumnSetIter.next();
 			value = dataValue.get(attribute);
 
 			if (value == null)
 			{
 				continue;
 			}
 
 			if (attribute instanceof CategoryAttributeInterface && !isCatEntRecIns)
 			{
 				String catEntTblName = categoryEnt.getTableProperties().getName();
 
 				Long entityId = null;
 				EntityManagerInterface entityManager = EntityManager.getInstance();
 
 				if (isNoCatAttrPrsnt)
 				{
 					attributes = null;
 				}
 				else
 				{
 					attributes = createAttributeMap(dataValue);
 				}
 				String catEntName = DynamicExtensionsUtility
 						.getCategoryEntityName(((CategoryAttribute) attribute).getCategoryEntity()
 								.getName());
 				if (keyMap.get(catEntName) != null && !areMultplRecrds)
 				{
 					entityId = (Long) keyMap.get(catEntName);
 
 					// Edit data for entity hierarchy.
 					entityManager.editDataForHeirarchy(categoryEnt.getEntity(), attributes,
 							entityId, jdbcDao, userId);
 				}
 				else
 				{
 					entityId = entityManager.insertDataForHeirarchy(categoryEnt.getEntity(),
 							attributes, jdbcDao, userId);
 				}
 
 				Long catEntId = null;
 
 				// Check whether table is created for category entity.
 				if (((CategoryEntity) categoryEnt).isCreateTable())
 				{
 					catEntId = entityManagerUtil.getNextIdentifier(categoryEnt.getTableProperties()
 							.getName());
 
 					// Insert query for category entity table.
 					String insertQuery = "INSERT INTO " + catEntTblName
 							+ " (IDENTIFIER, ACTIVITY_STATUS, " + RECORD_ID + ") VALUES ("
 							+ catEntId + ", 'ACTIVE', " + entityId + ")";
 					executeUpdateQuery(insertQuery, userId, jdbcDao);
 				}
 
 				if (catEntFKColName != null && entityFKColName != null)
 				{
 					if (((CategoryEntity) categoryEnt).isCreateTable())
 					{
 						// Update query for category entity table.
 						String updateQuery = "UPDATE " + catEntTblName + " SET " + catEntFKColName
 								+ " = " + srcCatEntityId + " WHERE IDENTIFIER = " + catEntId;
 						executeUpdateQuery(updateQuery, userId, jdbcDao);
 					}
 
 					// Update query for entity table.
 					String updateEntQuery = "UPDATE "
 							+ ((CategoryAttribute) attribute).getCategoryEntity().getEntity()
 									.getTableProperties().getName() + " SET " + entityFKColName
 							+ " = " + srcEntityId + " WHERE IDENTIFIER = " + entityId;
 					executeUpdateQuery(updateEntQuery, userId, jdbcDao);
 				}
 
 				CategoryEntityInterface catEntity = categoryEnt;
 				String categoryEntName = DynamicExtensionsUtility.getCategoryEntityName(catEntity
 						.getName());
 				keyMap.put(categoryEntName, entityId);
 				fullKeyMap.put(categoryEntName, entityId);
 
 				List<Long> recordIds = records.get(categoryEntName);
 				if (recordIds == null)
 				{
 					recordIds = new ArrayList<Long>();
 				}
 				recordIds.add(entityId);
 
 				records.put(categoryEntName, recordIds);
 
 				while (catEntity.getParentCategoryEntity() != null)
 				{
 					String parentCategoryEntName = DynamicExtensionsUtility
 							.getCategoryEntityName(catEntity.getParentCategoryEntity().getName());
 					keyMap.put(parentCategoryEntName, entityId);
 					fullKeyMap.put(parentCategoryEntName, entityId);
 
 					List<Long> recIds = records.get(parentCategoryEntName);
 					if (recIds == null)
 					{
 						recIds = new ArrayList<Long>();
 					}
 					recIds.add(entityId);
 					records.put(parentCategoryEntName, recIds);
 
 					catEntity = catEntity.getParentCategoryEntity();
 				}
 
 				isCatEntRecIns = true;
 			}
 			else if (attribute instanceof CategoryAssociationInterface)
 			{
 				CategoryAssociationInterface catAssociation = (CategoryAssociationInterface) attribute;
 
 				PathInterface path = catAssociation.getTargetCategoryEntity().getPath();
 				Collection<PathAssociationRelationInterface> pathAssoRel = path
 						.getSortedPathAssociationRelationCollection();
 
 				Long sourceEntityId = (Long) fullKeyMap.get(DynamicExtensionsUtility
 						.getCategoryEntityName(catAssociation.getCategoryEntity().getName()));
 
 				// Foreign key column name.
				String fKeyColName = new String();
 
 				String selectQuery = "SELECT IDENTIFIER FROM "
 						+ catAssociation.getCategoryEntity().getTableProperties().getName()
 						+ " WHERE " + RECORD_ID + " = " + sourceEntityId;
 
 				List<Long> identifiers = getResultIDList(selectQuery, "IDENTIFIER");
 
 				Long resultId = null;
 				if (identifiers != null && !identifiers.isEmpty())
 				{
 					resultId = identifiers.get(0);
 				}
 
 				Long srcCategoryEntId = resultId;
 
 				String catEntFKClmnName = catAssociation.getConstraintProperties()
 						.getTgtEntityConstraintKeyProperties().getTgtForiegnKeyColumnProperties()
 						.getName();
 
 				EntityInterface entity = catAssociation.getTargetCategoryEntity().getEntity();
 
 				for (PathAssociationRelationInterface par : pathAssoRel)
 				{
 					AssociationInterface association = par.getAssociation();
 
 					fKeyColName = association.getConstraintProperties()
 							.getTgtEntityConstraintKeyProperties()
 							.getTgtForiegnKeyColumnProperties().getName();
 
 					if (association.getTargetEntity().getId() != entity.getId())
 					{
 						if (fullKeyMap.get(association.getTargetEntity().getName() + "["
 								+ par.getTargetInstanceId() + "]") == null)
 						{
 							Long entityId = entityManagerUtil.getNextIdentifier(association
 									.getTargetEntity().getTableProperties().getName());
 							String insertQuery = "INSERT INTO "
 									+ association.getTargetEntity().getTableProperties().getName()
 									+ "(IDENTIFIER, ACTIVITY_STATUS, "
 									+ association.getConstraintProperties()
 											.getTgtEntityConstraintKeyProperties()
 											.getTgtForiegnKeyColumnProperties().getName()
 									+ ") VALUES (" + entityId + ", 'ACTIVE'," + sourceEntityId
 									+ ")";
 							executeUpdateQuery(insertQuery, userId, jdbcDao);
 
 							sourceEntityId = entityId;
 
 							fullKeyMap.put(association.getTargetEntity().getName() + "["
 									+ par.getTargetInstanceId() + "]", sourceEntityId);
 							keyMap.put(association.getTargetEntity().getName() + "["
 									+ par.getTargetInstanceId() + "]", sourceEntityId);
 
 							List<Long> recIds = records.get(association.getTargetEntity().getName()
 									+ "[" + par.getTargetInstanceId() + "]");
 							if (recIds == null)
 							{
 								recIds = new ArrayList<Long>();
 							}
 							recIds.add(entityId);
 							records.put(association.getTargetEntity().getName() + "["
 									+ par.getTargetInstanceId() + "]", recIds);
 						}
 						else
 						{
 							sourceEntityId = (Long) fullKeyMap.get(association.getTargetEntity()
 									.getName()
 									+ "[" + par.getTargetInstanceId() + "]");
 						}
 					}
 					else
 					{
 						sourceEntityId = (Long) fullKeyMap.get(association.getEntity().getName()
 								+ "[" + par.getSourceInstanceId() + "]");
 					}
 				}
 
 				List<Map<BaseAbstractAttributeInterface, Object>> mapsOfCntaindEnt = (List) value;
 
 				Map<CategoryAttributeInterface, Object> catAttributes = new HashMap<CategoryAttributeInterface, Object>();
 				Map<CategoryAssociationInterface, Object> catAssociations = new HashMap<CategoryAssociationInterface, Object>();
 
 				for (Map<BaseAbstractAttributeInterface, Object> valueMap : mapsOfCntaindEnt)
 				{
 					Set<BaseAbstractAttributeInterface> keySet = valueMap.keySet();
 					Iterator<BaseAbstractAttributeInterface> iter = keySet.iterator();
 					while (iter.hasNext())
 					{
 						Object obj = iter.next();
 						if (obj instanceof CategoryAttributeInterface)
 						{
 							catAttributes.put((CategoryAttributeInterface) obj, valueMap.get(obj));
 						}
 						else
 						{
 							catAssociations.put((CategoryAssociationInterface) obj, valueMap
 									.get(obj));
 						}
 					}
 
 					if (mapsOfCntaindEnt.size() > 1)
 					{
 						areMultplRecrds = true;
 					}
 					else
 					{
 						areMultplRecrds = false;
 					}
 
 					// Insert data for category attributes.
 					if (catAttributes.isEmpty())
 					{
 						isNoCatAttrPrsnt = true;
 						CategoryAttributeInterface dummyCatAttr = DomainObjectFactory.getInstance()
 								.createCategoryAttribute();
 						dummyCatAttr.setCategoryEntity(catAssociation.getTargetCategoryEntity());
 						catAttributes.put(dummyCatAttr, "");
 
 						insertRecordsForCategoryEntityTree(fKeyColName, catEntFKClmnName,
 								srcCategoryEntId, sourceEntityId, catAssociation
 										.getTargetCategoryEntity(), catAttributes, keyMap,
 								fullKeyMap, records, areMultplRecrds, isNoCatAttrPrsnt, jdbcDao,
 								userId);
 
 						isNoCatAttrPrsnt = false;
 					}
 					else
 					{
 						insertRecordsForCategoryEntityTree(fKeyColName, catEntFKClmnName,
 								srcCategoryEntId, sourceEntityId, catAssociation
 										.getTargetCategoryEntity(), catAttributes, keyMap,
 								fullKeyMap, records, areMultplRecrds, isNoCatAttrPrsnt, jdbcDao,
 								userId);
 					}
 					catAttributes.clear();
 
 					// Insert data for category associations.
 					insertRecordsForCategoryEntityTree(fKeyColName, catEntFKClmnName,
 							srcCategoryEntId, sourceEntityId, catAssociation
 									.getTargetCategoryEntity(), catAssociations, keyMap,
 							fullKeyMap, records, areMultplRecrds, isNoCatAttrPrsnt, jdbcDao, userId);
 					catAssociations.clear();
 
 					fullKeyMap.putAll(keyMap);
 					keyMap.remove(DynamicExtensionsUtility.getCategoryEntityName(catAssociation
 							.getTargetCategoryEntity().getName()));
 				}
 			}
 		}
 	}
 
 	/**
 	 * This method returns the category data value map for the given root category entity.
 	 * @param rootCatEntity
 	 * @param recordId
 	 * @return map of category entity data
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws SQLException
 	 */
 	public Map<BaseAbstractAttributeInterface, Object> getRecordById(
 			CategoryEntityInterface rootCatEntity, Long recordId)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException,
 			SQLException
 	{
 		Map<BaseAbstractAttributeInterface, Object> dataValue = new HashMap<BaseAbstractAttributeInterface, Object>();
 
 		HibernateDAO hibernateDAO = null;
 
 		try
 		{
 			hibernateDAO =DynamicExtensionsUtility.getHibernateDAO();
 			retrieveRecords(rootCatEntity, dataValue, recordId);
 		}
 		catch (DAOException e)
 		{
 			throw (DynamicExtensionsSystemException) handleRollback(e,
 					"Error while retrieving data", hibernateDAO, true);
 		}
 		finally
 		{
 			try
 			{
 				DynamicExtensionsUtility.closeHibernateDAO(hibernateDAO);
 			}
 			catch (DAOException e)
 			{
 				throw (DynamicExtensionsSystemException) handleRollback(e, "Error while closing",
 						hibernateDAO, true);
 			}
 		}
 
 		Map<BaseAbstractAttributeInterface, Object> curatedRecords = new HashMap<BaseAbstractAttributeInterface, Object>();
 		curateMapForRelatedAttributes(curatedRecords, dataValue);
 
 		dataValue = curatedRecords;
 
 		return dataValue;
 	}
 
 	/**
 	 * This method removes related invisible category attributes from the map.
 	 * @param curatedRecords
 	 * @param dataValue
 	 */
 	private void curateMapForRelatedAttributes(
 			Map<BaseAbstractAttributeInterface, Object> curatedRecords,
 			Map<BaseAbstractAttributeInterface, Object> dataValue)
 	{
 		Iterator<BaseAbstractAttributeInterface> iter = dataValue.keySet().iterator();
 		while (iter.hasNext())
 		{
 			Object obj = iter.next();
 
 			if (obj instanceof CategoryAttributeInterface)
 			{
 				CategoryAttributeInterface catAttr = (CategoryAttributeInterface) obj;
 
 				if (catAttr.getIsRelatedAttribute() != null && catAttr.getIsRelatedAttribute()
 						&& catAttr.getIsVisible() != null && catAttr.getIsVisible())
 				{
 					curatedRecords.put((BaseAbstractAttributeInterface) obj, dataValue.get(obj));
 				}
 				else if (catAttr.getIsRelatedAttribute() != null
 						&& !catAttr.getIsRelatedAttribute())
 				{
 					curatedRecords.put((BaseAbstractAttributeInterface) obj, dataValue.get(obj));
 				}
 			}
 			else
 			{
 				CategoryAssociationInterface catAssociation = (CategoryAssociationInterface) obj;
 
 				List<Map<BaseAbstractAttributeInterface, Object>> mapsOfCntdRec = (List) dataValue
 						.get(catAssociation);
 				List<Map<BaseAbstractAttributeInterface, Object>> innerRecList = new ArrayList<Map<BaseAbstractAttributeInterface, Object>>();
 
 				for (Map<BaseAbstractAttributeInterface, Object> map : mapsOfCntdRec)
 				{
 					Map<BaseAbstractAttributeInterface, Object> innerRecords = new HashMap<BaseAbstractAttributeInterface, Object>();
 					curateMapForRelatedAttributes(innerRecords, map);
 					innerRecList.add(innerRecords);
 				}
 
 				curatedRecords.put(catAssociation, innerRecList);
 			}
 		}
 	}
 
 	/**
 	 * @param catEntity
 	 * @param dataValue
 	 * @param rootCatEntRecId
 	 * @throws SQLException
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private void retrieveRecords(CategoryEntityInterface catEntity,
 			Map<BaseAbstractAttributeInterface, Object> dataValue, long rootCatEntRecId)
 			throws SQLException, DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		Long recordId = null;
 		String catEntTblName = "";
 		String selRecIdQuery = "";
 
 		boolean isRecordIdNull = false;
 
 		// Only the root category entity has the category object set in it.
 		if (catEntity.getCategory() != null)
 		{
 			catEntTblName = catEntity.getTableProperties().getName();
 
 			selRecIdQuery = "SELECT " + RECORD_ID + " FROM " + catEntTblName
 					+ " WHERE IDENTIFIER = " + rootCatEntRecId;
 
 			List<Long> identifiers = getResultIDList(selRecIdQuery, RECORD_ID);
 
 			if (identifiers != null && !identifiers.isEmpty())
 			{
 				recordId = identifiers.get(0);
 			}
 		}
 
 		// If entity model is different than category model then entity data is inserted
 		// according to entity model and category data is inserted according to category model.
 		// In this case recordId can be NULL in category entity table.
 		if (recordId == null)
 		{
 			isRecordIdNull = true;
 			recordId = rootCatEntRecId;
 		}
 
 		Map<AbstractAttributeInterface, Object> entityRecords = new HashMap<AbstractAttributeInterface, Object>();
 		entityRecords.putAll(EntityManager.getInstance().getEntityRecordById(catEntity.getEntity(),
 				recordId));
 
 		// If root category entity has parent entity, then get data from parent entity table
 		// with same record id.
 		CategoryEntityInterface parentCatEnt = catEntity.getParentCategoryEntity();
 		while (parentCatEnt != null)
 		{
 			Map<AbstractAttributeInterface, Object> innerValues = EntityManager.getInstance()
 					.getEntityRecordById(parentCatEnt.getEntity(), recordId);
 			if (innerValues != null)
 			{
 				entityRecords.putAll(innerValues);
 			}
 
 			parentCatEnt = parentCatEnt.getParentCategoryEntity();
 		}
 
 		if (!isAllRelatedInvisibleCategoryAttributesCollection(catEntity))
 		{
 			for (CategoryAttributeInterface catAttribute : catEntity.getAllCategoryAttributes())
 			{
 				dataValue.put(catAttribute, entityRecords.get(catAttribute.getAbstractAttribute()));
 			}
 		}
 
 		Collection<CategoryAssociationInterface> catAssociations = new ArrayList<CategoryAssociationInterface>(
 				catEntity.getCategoryAssociationCollection());
 		for (CategoryAssociationInterface catAssociation : catAssociations)
 		{
 			CategoryEntityInterface targetCatEnt = catAssociation.getTargetCategoryEntity();
 			if (targetCatEnt != null
 					&& !isAllRelatedInvisibleCategoryAttributesCollection(targetCatEnt)
 					&& (((CategoryEntity) targetCatEnt).isCreateTable()))
 			{
 				catEntTblName = targetCatEnt.getTableProperties().getName();
 
 				if (isRecordIdNull)
 				{
 					String selectQuery = "SELECT IDENTIFIER FROM "
 							+ catAssociation.getCategoryEntity().getTableProperties().getName()
 							+ " WHERE " + RECORD_ID + " = " + recordId;
 
 					List<Long> identifiers = getResultIDList(selectQuery, "IDENTIFIER");
 
 					if (identifiers != null && !identifiers.isEmpty())
 					{
 						rootCatEntRecId = identifiers.get(0);
 					}
 				}
 
 				selRecIdQuery = "SELECT "
 						+ RECORD_ID
 						+ " FROM "
 						+ catEntTblName
 						+ " WHERE "
 						+ catAssociation.getConstraintProperties()
 								.getTgtEntityConstraintKeyProperties()
 								.getTgtForiegnKeyColumnProperties().getName() + " = "
 						+ rootCatEntRecId;
 
 				List<Map<BaseAbstractAttributeInterface, Object>> innerRecords = new ArrayList<Map<BaseAbstractAttributeInterface, Object>>();
 				dataValue.put(catAssociation, innerRecords);
 
 				List<Long> recordIds = getResultIDList(selRecIdQuery, RECORD_ID);
 				for (Long recId : recordIds)
 				{
 					Map<BaseAbstractAttributeInterface, Object> innerRecord = new HashMap<BaseAbstractAttributeInterface, Object>();
 					innerRecords.add(innerRecord);
 
 					retrieveRecords(targetCatEnt, innerRecord, recId);
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param entity
 	 * @param dataValue
 	 * @return
 	 */
 	private Map<CategoryEntityInterface, Map<BaseAbstractAttributeInterface, Object>> initialiseEntityValueMap(
 			Map<BaseAbstractAttributeInterface, ?> dataValue)
 	{
 		Map<CategoryEntityInterface, Map<BaseAbstractAttributeInterface, Object>> catEntRecords = new HashMap<CategoryEntityInterface, Map<BaseAbstractAttributeInterface, Object>>();
 
 		for (BaseAbstractAttributeInterface baseAbstrAttr : dataValue.keySet())
 		{
 			CategoryEntityInterface categoryEntity = null;
 			if (baseAbstrAttr instanceof CategoryAttributeInterface)
 			{
 				categoryEntity = ((CategoryAttributeInterface) baseAbstrAttr).getCategoryEntity();
 			}
 			else
 			{
 				categoryEntity = ((CategoryAssociationInterface) baseAbstrAttr).getCategoryEntity();
 			}
 			Object value = dataValue.get(baseAbstrAttr);
 
 			Map<BaseAbstractAttributeInterface, Object> entDataValues = (Map) catEntRecords
 					.get(categoryEntity);
 			if (entDataValues == null)
 			{
 				entDataValues = new HashMap<BaseAbstractAttributeInterface, Object>();
 				catEntRecords.put(categoryEntity, entDataValues);
 			}
 			entDataValues.put(baseAbstrAttr, value);
 		}
 
 		return catEntRecords;
 	}
 
 	/**
 	 * @param catEntity
 	 * @return
 	 */
 	private List<CategoryEntityInterface> getParentEntityList(CategoryEntityInterface catEntity)
 	{
 		// As here the parent category entity whose table is not created is blocked so it is not added in list.
 		List<CategoryEntityInterface> catEntities = new ArrayList<CategoryEntityInterface>();
 		catEntities.add(catEntity);
 
 		// Bug # 10265 - modified as per code review comment.
 		// Reviewer name - Rajesh Patil
 		CategoryEntityInterface categoryEnt = catEntity.getParentCategoryEntity();
 		while (categoryEnt != null && ((CategoryEntity) categoryEnt).isCreateTable())
 		{
 			catEntities.add(0, categoryEnt);
 			categoryEnt = categoryEnt.getParentCategoryEntity();
 		}
 
 		return catEntities;
 	}
 
 	/**
 	 * This method populates record map for entity which belongs to root category entity.
 	 * @param rootCatEntity
 	 * @param rootEntRecords
 	 * @param attributeValues
 	 */
 	private void populateRootEntityRecordMap(CategoryEntityInterface rootCatEntity,
 			Map<AbstractAttributeInterface, Object> rootEntRecords,
 			Map<BaseAbstractAttributeInterface, Object> attributeValues)
 	{
 		// Set of category data map entries.
 		Set<Entry<BaseAbstractAttributeInterface, Object>> dataMapEntry = attributeValues
 				.entrySet();
 
 		AbstractAttributeInterface abstrAttribute = null;
 		Object entityValue = null;
 		CategoryAttributeInterface catAttribute;
 		BaseAbstractAttributeInterface baseAbstrAttr;
 		Object categoryValue;
 
 		for (Entry<BaseAbstractAttributeInterface, Object> entry : dataMapEntry)
 		{
 			baseAbstrAttr = entry.getKey();
 			categoryValue = entry.getValue();
 			if (baseAbstrAttr instanceof CategoryAttributeInterface)
 			{
 				catAttribute = (CategoryAttributeInterface) baseAbstrAttr;
 				abstrAttribute = catAttribute.getAbstractAttribute();
 				entityValue = categoryValue;
 
 				// Add root cat entity and its parent category entity's attribute.
 				for (CategoryAttributeInterface rootCECatAttr : rootCatEntity
 						.getAllCategoryAttributes())
 				{
 					if ((catAttribute == rootCECatAttr)
 							&& (catAttribute.getIsRelatedAttribute() == null || catAttribute
 									.getIsRelatedAttribute() == false))
 					{
 						rootEntRecords.put(abstrAttribute, entityValue);
 					}
 				}
 			}
 		}
 
 		for (CategoryAssociationInterface catAssociation : rootCatEntity
 				.getCategoryAssociationCollection())
 		{
 			// Add all root category entity's associations.
 			for (AssociationInterface association : catAssociation.getCategoryEntity().getEntity()
 					.getAssociationCollection())
 			{
 				if (!rootEntRecords.containsKey(association))
 				{
 					rootEntRecords.put(association, new ArrayList());
 				}
 			}
 			// Also add any associations which are related to parent entity's associations
 			// i.e. associations between root category entity's parent entity and another class 
 			// whose category association is created.
 			EntityInterface entity = catAssociation.getCategoryEntity().getEntity();
 			while (entity.getParentEntity() != null)
 			{
 				for (AssociationInterface association : entity.getParentEntity()
 						.getAssociationCollection())
 				{
 					if (association.getTargetEntity() == catAssociation.getTargetCategoryEntity()
 							.getEntity())
 					{
 						if (!rootEntRecords.containsKey(association))
 						{
 							rootEntRecords.put(association, new ArrayList());
 						}
 					}
 				}
 
 				entity = entity.getParentEntity();
 			}
 		}
 	}
 
 	/**
 	 * @param categoryEnt
 	 * @param recordId
 	 * @param rlbkQryStack
 	 * @param userId
 	 * @param jdbcDAO
 	 * @throws SQLException
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	private void clearCategoryEntityData(CategoryEntityInterface categoryEnt, Long recordId,
 			Stack<String> rlbkQryStack, Long userId, JDBCDAO jdbcDao) throws DynamicExtensionsSystemException
 	{
 		CategoryEntityInterface catEntity = categoryEnt;
 
 		for (CategoryAssociationInterface catAssociation : catEntity
 				.getCategoryAssociationCollection())
 		{
 			if (catAssociation.getTargetCategoryEntity().getChildCategories().size() != 0)
 			{
 				String selectQuery = "SELECT IDENTIFIER FROM "
 						+ catAssociation.getTargetCategoryEntity().getTableProperties().getName()
 						+ " WHERE "
 						+ catAssociation.getConstraintProperties()
 								.getTgtEntityConstraintKeyProperties()
 								.getTgtForiegnKeyColumnProperties().getName() + " = " + recordId;
 
 				List<Long> recordIds = getResultIDList(selectQuery, "IDENTIFIER");
 				for (Long recId : recordIds)
 				{
 					clearCategoryEntityData(catAssociation.getTargetCategoryEntity(), recId,
 							rlbkQryStack, userId, jdbcDao);
 
 					String deleteQuery = "DELETE FROM "
 							+ catAssociation.getTargetCategoryEntity().getTableProperties()
 									.getName() + " WHERE IDENTIFIER = " + recId;
 					executeUpdateQuery(deleteQuery, userId, jdbcDao);
 					rlbkQryStack.push(deleteQuery);
 				}
 			}
 			else
 			{
 				String deleteQuery = "DELETE FROM "
 						+ catAssociation.getTargetCategoryEntity().getTableProperties().getName()
 						+ " WHERE "
 						+ catAssociation.getConstraintProperties()
 								.getTgtEntityConstraintKeyProperties()
 								.getTgtForiegnKeyColumnProperties().getName() + " = " + recordId;
 				executeUpdateQuery(deleteQuery, userId, jdbcDao);
 				rlbkQryStack.push(deleteQuery);
 			}
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected DynamicExtensionBaseQueryBuilder getQueryBuilderInstance()
 	{
 		return queryBuilder;
 	}
 
 	/**
 	 * @param rootCatEntity
 	 * @param recordId
 	 * @return the record id of the hook entity
 	 * @throws SQLException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private Long getRootCategoryEntityRecordId(CategoryEntityInterface rootCatEntity, Long recordId)
 			throws SQLException, DynamicExtensionsSystemException
 	{
 		StringBuffer query = new StringBuffer();
 		query.append(SELECT_KEYWORD + WHITESPACE + RECORD_ID + WHITESPACE + FROM_KEYWORD
 				+ WHITESPACE + rootCatEntity.getTableProperties().getName() + WHITESPACE
 				+ WHERE_KEYWORD + WHITESPACE + IDENTIFIER + EQUAL + recordId);
 
 		Long rootCatEntRecId = null;
 
 		List<Long> results = getResultIDList(query.toString(), RECORD_ID);
 		if (!results.isEmpty())
 		{
 			rootCatEntRecId = (Long) results.get(0);
 		}
 
 		return rootCatEntRecId;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.CategoryManagerInterface#isPermissibleValuesSubsetValid(edu.common.dynamicextensions.domaininterface.AttributeInterface, java.util.List)
 	 */
 	public boolean isPermissibleValuesSubsetValid(UserDefinedDEInterface userDefinedDE,
 			Map<String, Collection<SemanticPropertyInterface>> desiredPVs)
 	{
 		boolean arePVsValid = true;
 
 		if (userDefinedDE != null)
 		{
 			List<Object> attributePVs = new ArrayList<Object>();
 
 			for (PermissibleValueInterface pv : userDefinedDE.getPermissibleValueCollection())
 			{
 				attributePVs.add(pv.getValueAsObject());
 			}
 
 			boolean allDoubleValues = false;
 			Iterator<PermissibleValueInterface> itrPV = userDefinedDE
 					.getPermissibleValueCollection().iterator();
 			while (itrPV.hasNext())
 			{
 				if (itrPV.next() instanceof edu.common.dynamicextensions.domain.DoubleValue)
 				{
 					allDoubleValues = true;
 				}
 				else
 				{
 					allDoubleValues = false;
 				}
 			}
 
 			boolean allFloatValues = false;
 			Iterator<PermissibleValueInterface> itrPVFloat = userDefinedDE
 					.getPermissibleValueCollection().iterator();
 			while (itrPVFloat.hasNext())
 			{
 				if (itrPVFloat.next() instanceof edu.common.dynamicextensions.domain.FloatValue)
 				{
 					allFloatValues = true;
 				}
 				else
 				{
 					allFloatValues = false;
 				}
 			}
 
 			boolean allIntegerValues = false;
 			Iterator<PermissibleValueInterface> itrPVInteger = userDefinedDE
 					.getPermissibleValueCollection().iterator();
 			while (itrPVInteger.hasNext())
 			{
 				if (itrPVInteger.next() instanceof edu.common.dynamicextensions.domain.IntegerValue)
 				{
 					allIntegerValues = true;
 				}
 				else
 				{
 					allIntegerValues = false;
 				}
 			}
 
 			boolean allShortValues = false;
 			Iterator<PermissibleValueInterface> itrPVShort = userDefinedDE
 					.getPermissibleValueCollection().iterator();
 			while (itrPVShort.hasNext())
 			{
 				if (itrPVShort.next() instanceof edu.common.dynamicextensions.domain.ShortValue)
 				{
 					allShortValues = true;
 				}
 				else
 				{
 					allShortValues = false;
 				}
 			}
 
 			boolean allLongValues = false;
 			Iterator<PermissibleValueInterface> itrPVLong = userDefinedDE
 					.getPermissibleValueCollection().iterator();
 			while (itrPVLong.hasNext())
 			{
 				if (itrPVLong.next() instanceof edu.common.dynamicextensions.domain.LongValue)
 				{
 					allLongValues = true;
 				}
 				else
 				{
 					allLongValues = false;
 				}
 			}
 
 			if (allFloatValues && desiredPVs != null)
 			{
 				Set<String> permissibleValues = desiredPVs.keySet();
 				for (String value : permissibleValues)
 				{
 					if (!attributePVs.contains(Float.parseFloat(value)))
 					{
 						arePVsValid = false;
 					}
 				}
 			}
 			else if (allDoubleValues && desiredPVs != null)
 			{
 				Set<String> permissibleValues = desiredPVs.keySet();
 				for (String value : permissibleValues)
 				{
 					if (!attributePVs.contains(Double.parseDouble(value)))
 					{
 						arePVsValid = false;
 					}
 				}
 			}
 			else if (allIntegerValues && desiredPVs != null)
 			{
 				Set<String> permissibleValues = desiredPVs.keySet();
 				for (String value : permissibleValues)
 				{
 					if (!attributePVs.contains(Integer.parseInt(value)))
 					{
 						arePVsValid = false;
 					}
 				}
 			}
 			else if (allShortValues && desiredPVs != null)
 			{
 				Set<String> permissibleValues = desiredPVs.keySet();
 				for (String value : permissibleValues)
 				{
 					if (!attributePVs.contains(Short.parseShort(value)))
 					{
 						arePVsValid = false;
 					}
 				}
 			}
 			else if (allLongValues && desiredPVs != null)
 			{
 				Set<String> permissibleValues = desiredPVs.keySet();
 				for (String value : permissibleValues)
 				{
 					if (!attributePVs.contains(Long.parseLong(value)))
 					{
 						arePVsValid = false;
 					}
 				}
 			}
 			else if (desiredPVs != null)
 			{
 				Set<String> permissibleValues = desiredPVs.keySet();
 				for (String value : permissibleValues)
 				{
 					if (!attributePVs.contains(value))
 					{
 						arePVsValid = false;
 					}
 				}
 			}
 		}
 
 		return arePVsValid;
 	}
 
 	/**
 	 * This method executes a SQL query and returns a list of identifier, record identifier
 	 * depending upon column name passed.
 	 * @param query
 	 * @param columnName
 	 * @return a list of identifier, record identifier depending upon column name passed.
 	 * @throws SQLException
 	 */
 	private List<Long> getResultIDList(String query, String columnName) throws DynamicExtensionsSystemException
 	{
 		List<Long> recordIds = new ArrayList<Long>();
 		ResultSet resultSet = null;
 		Object value = null;
 		JDBCDAO jdbcDAO=null;
 
 		try
 		{
 			jdbcDAO= DynamicExtensionsUtility.getJDBCDAO();
 			resultSet = jdbcDAO.getQueryResultSet(query);
 
 			while (resultSet.next())
 			{
 				value = resultSet.getObject(columnName);
 				recordIds.add(new Long(value.toString()));
 			}
 		}
 		catch (DAOException e)
 		{
 			throw new DynamicExtensionsSystemException("Error executing query ",e);
 		}
 		catch (NumberFormatException e)
 		{
 			throw new DynamicExtensionsSystemException("Error executing query ",e);
 		}
 		catch (SQLException e)
 		{
 			throw new DynamicExtensionsSystemException("Error executing query ",e);
 		}
 		finally
 		{
 			try
 			{
 				DynamicExtensionsUtility.closeJDBCDAO(jdbcDAO);
 			}
 			catch (DAOException e)
 			{
 				throw new DynamicExtensionsSystemException("Error executing query ",e);
 			}
 		}
 
 		return recordIds;
 	}
 
 	/**
 	 * getEntityRecordIdByRootCategoryEntityRecordId.
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	public Long getEntityRecordIdByRootCategoryEntityRecordId(Long rootCategoryEntityRecordId,
 			String rootCategoryTableName) throws DynamicExtensionsSystemException
 	{
 		String query = "select record_Id from " + rootCategoryTableName + " where IDENTIFIER ="
 				+ rootCategoryEntityRecordId;
 		return getEntityRecordId(query);
 	}
 
 	/**
 	 * getEntityRecordIdByRootCategoryEntityRecordId.
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	public Long getRootCategoryEntityRecordIdByEntityRecordId(Long rootCategoryEntityRecordId,
 			String rootCategoryTableName) throws DynamicExtensionsSystemException
 	{
 		String query = "select IDENTIFIER from " + rootCategoryTableName + " where record_Id ="
 				+ rootCategoryEntityRecordId;
 		return getEntityRecordId(query);
 	}
 
 	/**
 	 * 
 	 * @param query
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private Long getEntityRecordId(String query) throws DynamicExtensionsSystemException
 	{
 		ResultSet resultSet = null;
 		Long entityRecordId = null;
 		JDBCDAO jdbcDAO=null;
 		try
 		{
 			jdbcDAO= DynamicExtensionsUtility.getJDBCDAO();
 			resultSet = jdbcDAO.getQueryResultSet(query);
 			resultSet.next();
 			entityRecordId = resultSet.getLong(1);
 		}
 		catch (DAOException e)
 		{
 			throw new DynamicExtensionsSystemException("Exception in query execution", e);
 		}
 		catch (SQLException e)
 		{
 			throw new DynamicExtensionsSystemException("Exception in query execution", e);
 		}
 		finally
 		{
 			try
 			{
 				DynamicExtensionsUtility.closeJDBCDAO(jdbcDAO);
 			}
 			catch (DAOException e)
 			{
 				throw new DynamicExtensionsSystemException("Exception in query execution", e);
 			}
 		}
 		return entityRecordId;
 	}
 
 	/**
 	 * This method executes a SQL query.
 	 * @param query
 	 * @throws SQLException
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	private void executeUpdateQuery(String query, Long userId,JDBCDAO jdbcDao) throws DynamicExtensionsSystemException
 	{
 		try
 		{
 			jdbcDao.executeUpdate(query);
 		}
 		catch (DAOException e)
 		{
 			throw new DynamicExtensionsSystemException(e.getMessage(),e);
 		}
 	}
 
 	/**
 	 *
 	 * @param dataValue
 	 * @return
 	 */
 	private Map<AbstractAttributeInterface, Object> createAttributeMap(
 			Map<BaseAbstractAttributeInterface, Object> dataValue)
 	{
 		Map<AbstractAttributeInterface, Object> attributes = new HashMap<AbstractAttributeInterface, Object>();
 
 		Iterator<BaseAbstractAttributeInterface> attrIter = dataValue.keySet().iterator();
 		while (attrIter.hasNext())
 		{
 			Object obj = attrIter.next();
 			if (obj instanceof CategoryAttributeInterface)
 			{
 				attributes.put(((CategoryAttributeInterface) obj).getAbstractAttribute(), dataValue
 						.get(obj));
 			}
 		}
 
 		return attributes;
 	}
 
 }
