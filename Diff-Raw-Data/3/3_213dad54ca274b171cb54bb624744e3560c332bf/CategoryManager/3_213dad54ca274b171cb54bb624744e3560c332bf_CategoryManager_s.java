 
 package edu.common.dynamicextensions.entitymanager;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
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
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.PathInterface;
 import edu.common.dynamicextensions.domaininterface.PermissibleValueInterface;
 import edu.common.dynamicextensions.domaininterface.SemanticPropertyInterface;
 import edu.common.dynamicextensions.domaininterface.UserDefinedDEInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.common.dynamicextensions.util.global.Constants;
 import edu.common.dynamicextensions.util.global.Variables;
 import edu.wustl.common.dao.DAOFactory;
 import edu.wustl.common.dao.HibernateDAO;
 import edu.wustl.common.dao.JDBCDAO;
 import edu.wustl.common.security.exceptions.UserNotAuthorizedException;
 import edu.wustl.common.util.dbManager.DAOException;
 import edu.wustl.common.util.dbManager.DBUtil;
 
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
 	protected void LogFatalError(Exception e, AbstractMetadataInterface abstractMetadata)
 	{
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * Method to persist a category.
 	 * @param categoryInterface interface for Category
 	 * @throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	 */
 	public CategoryInterface persistCategory(CategoryInterface categoryInterface) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		CategoryInterface category = (CategoryInterface) persistDynamicExtensionObject(categoryInterface);
 
 		// Update the dynamic extension cache for all containers within entity group
 //		CategoryEntityInterface catEntityInterface = category.getRootCategoryElement();
 //		EntityGroupInterface entityGroupInterface = catEntityInterface.getEntity().getEntityGroup();
 //		DynamicExtensionsUtility.updateDynamicExtensionsCache(entityGroupInterface.getId());
 		return category;
 	}
 
 	/**
 	 * Method to persist category meta-data.
 	 * @param categoryInterface interface for Category
 	 * @throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	 */
 	public CategoryInterface persistCategoryMetadata(CategoryInterface categoryInterface) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		CategoryInterface category = (CategoryInterface) persistDynamicExtensionObjectMetdata(categoryInterface);
 
 		// Update the dynamic extension cache for all containers within entity group
 //		CategoryEntityInterface catEntityInterface = category.getRootCategoryElement();
 //		EntityGroupInterface entityGroupInterface = catEntityInterface.getEntity().getEntityGroup();
 //		DynamicExtensionsUtility.updateDynamicExtensionsCache(entityGroupInterface.getId());
 		return category;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.AbstractMetadataManager#preProcess(edu.common.dynamicextensions.domaininterface.DynamicExtensionBaseDomainObjectInterface, java.util.List, edu.wustl.common.dao.HibernateDAO, java.util.List)
 	 */
 	protected void preProcess(DynamicExtensionBaseDomainObjectInterface dynamicExtensionBaseDomainObject, List<String> reverseQueryList,
 			HibernateDAO hibernateDAO, List<String> queryList) throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		CategoryInterface category = (CategoryInterface) dynamicExtensionBaseDomainObject;
 
 		getDynamicQueryList(category, reverseQueryList, hibernateDAO, queryList);
 	}
 
 	/**
 	 * This method gets a list of dynamic tables creation queries.
 	 * @param category
 	 * @param reverseQueryList
 	 * @param hibernateDAO
 	 * @param queryList
 	 * @return list of dynamic tables creation queries
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	protected List<String> getDynamicQueryList(CategoryInterface category, List<String> reverseQueryList, HibernateDAO hibernateDAO,
 			List<String> queryList) throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		List<CategoryEntityInterface> categoryEntityList = new ArrayList<CategoryEntityInterface>();
 		// Use HashMap instead of List to ensure entity list contains unique entity only.
 		HashMap<String, CategoryEntityInterface> objCategoryMap = new HashMap<String, CategoryEntityInterface>();
 		DynamicExtensionsUtility.getUnsavedCategoryEntityList(category.getRootCategoryElement(), objCategoryMap);
 		Iterator keyIterator = objCategoryMap.keySet().iterator();
 		while (keyIterator.hasNext())
 		{
 			categoryEntityList.add(objCategoryMap.get(keyIterator.next()));
 		}
 
 		for (CategoryEntityInterface categoryEntityInterface : categoryEntityList)
 		{
 			List<String> createQueryList = queryBuilder.getCreateCategoryQueryList(categoryEntityInterface, reverseQueryList, hibernateDAO);
 			if (createQueryList != null && !createQueryList.isEmpty())
 			{
 				queryList.addAll(createQueryList);
 			}
 		}
 		for (CategoryEntityInterface categoryEntityInterface : categoryEntityList)
 		{
 			List<String> createQueryList = queryBuilder.getUpdateCategoryEntityQueryList(categoryEntityInterface, reverseQueryList, hibernateDAO);
 			if (createQueryList != null && !createQueryList.isEmpty())
 			{
 				queryList.addAll(createQueryList);
 			}
 		}
 		//Use HashMap instead of List to ensure entitylist contains unique entity only
 		List<CategoryEntityInterface> savedCategoryEntityList = new ArrayList<CategoryEntityInterface>();
 		objCategoryMap = new HashMap<String, CategoryEntityInterface>();
 		DynamicExtensionsUtility.getSavedCategoryEntityList(category.getRootCategoryElement(), objCategoryMap);
 		keyIterator = objCategoryMap.keySet().iterator();
 		while (keyIterator.hasNext())
 		{
 			savedCategoryEntityList.add(objCategoryMap.get(keyIterator.next()));
 		}
 		for (CategoryEntityInterface savedCategoryEntity : savedCategoryEntityList)
 		{
 			CategoryEntity databaseCopy = (CategoryEntity) DBUtil.loadCleanObj(CategoryEntity.class, savedCategoryEntity.getId());
 			//Only for category entity for which table is getting created
 			if (databaseCopy.isCreateTable())
 			{
 				List<String> updateQueryList = queryBuilder.getUpdateEntityQueryList((CategoryEntity) savedCategoryEntity, databaseCopy,
 						reverseQueryList);
 
 				if (updateQueryList != null && !updateQueryList.isEmpty())
 				{
 					queryList.addAll(updateQueryList);
 				}
 			}
 		}
 
 		return queryList;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.AbstractMetadataManager#postProcess(java.util.List, java.util.List, java.util.Stack)
 	 */
 	protected void postProcess(List<String> queryList, List<String> reverseQueryList, Stack rollbackQueryStack, HibernateDAO hibernateDAO)
 			throws DynamicExtensionsSystemException
 	{
 		queryBuilder.executeQueries(queryList, reverseQueryList, rollbackQueryStack, hibernateDAO);
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.CategoryManagerInterface#insertData(edu.common.dynamicextensions.domaininterface.CategoryInterface, java.util.Map)
 	 */
 	public Long insertData(CategoryInterface category, Map<BaseAbstractAttributeInterface, Object> dataValue, Long... userId)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		List<Map<BaseAbstractAttributeInterface, Object>> dataValueMapList = new ArrayList<Map<BaseAbstractAttributeInterface, Object>>();
 		dataValueMapList.add(dataValue);
 		Long id = ((userId != null || userId.length > 0) ? userId[0] : null);
 		List<Long> recordIdList = insertData(category, dataValueMapList, id);
 		return recordIdList.get(0);
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.CategoryManagerInterface#insertData(edu.common.dynamicextensions.domaininterface.CategoryInterface, java.util.List)
 	 */
 	public List<Long> insertData(CategoryInterface category, List<Map<BaseAbstractAttributeInterface, Object>> categoryDataValueMapList,
 			Long... userId) throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		List<Long> recordIdList = new ArrayList<Long>();
 
 		JDBCDAO jdbcDAO = null;
 		try
 		{
 			DAOFactory factory = DAOFactory.getInstance();
 			jdbcDAO = (JDBCDAO) factory.getDAO(Constants.JDBC_DAO);
 			jdbcDAO.openSession(null);
 			Long id = ((userId != null || userId.length > 0) ? userId[0] : null);
 
 			for (Map<BaseAbstractAttributeInterface, ?> categoryDataValue : categoryDataValueMapList)
 			{
 				Long recordId = insertDataForHierarchy(category.getRootCategoryElement(), categoryDataValue, jdbcDAO, id);
 				recordIdList.add(recordId);
 			}
 
 			jdbcDAO.commit();
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			throw (DynamicExtensionsApplicationException) handleRollback(e, "Error while inserting data", jdbcDAO, false);
 		}
 		catch (Exception e)
 		{
 			throw (DynamicExtensionsSystemException) handleRollback(e, "Error while inserting data", jdbcDAO, true);
 		}
 		finally
 		{
 			try
 			{
 				jdbcDAO.closeSession();
 			}
 			catch (DAOException e)
 			{
 				throw (DynamicExtensionsSystemException) handleRollback(e, "Error while closing", jdbcDAO, true);
 			}
 		}
 
 		return recordIdList;
 	}
 
 	/**
 	 * This method inserts the data for hierarchy of category entities one by one.
 	 * @param entity
 	 * @param dataValue
 	 * @param jdbcDAO
 	 * @param userId
 	 * @return parent record identifier
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws HibernateException
 	 * @throws SQLException
 	 * @throws DAOException
 	 * @throws UserNotAuthorizedException
 	 * @throws IOException
 	 * @throws ParseException
 	 */
 	private Long insertDataForHierarchy(CategoryEntityInterface categoryEntity, Map<BaseAbstractAttributeInterface, ?> dataValue, JDBCDAO jdbcDAO,
 			Long... userId) throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException, HibernateException, SQLException,
 			DAOException, UserNotAuthorizedException, ParseException, IOException
 	{
 		List<CategoryEntityInterface> categoryEntityList = getParentEntityList(categoryEntity);
 		Map<CategoryEntityInterface, Map> entityValueMap = initialiseEntityValueMap(dataValue);
 		Long parentRecordId = null;
 		Long parentCategoryRecordId = null;
 		Map testdatamap = new HashMap();
 		Long id = ((userId != null || userId.length > 0) ? userId[0] : null);
 		for (CategoryEntityInterface categoryEntityInterface : categoryEntityList)
 		{
 			Map valueMap = entityValueMap.get(categoryEntityInterface);
 			// If parent category entity table not created, then add its attribute map to value map.
 			CategoryEntity objParentCategoryEntity = (CategoryEntity) categoryEntityInterface.getParentCategoryEntity();
 			while (objParentCategoryEntity != null && !objParentCategoryEntity.isCreateTable())
 			{
 				Map innerValueMap = entityValueMap.get(objParentCategoryEntity);
 				if (innerValueMap != null)
 					valueMap.putAll(innerValueMap);
 				objParentCategoryEntity = (CategoryEntity) objParentCategoryEntity.getParentCategoryEntity();
 
 			}
 			parentRecordId = insertDataForSingleCategoryEntity(categoryEntityInterface, valueMap, jdbcDAO, parentRecordId, testdatamap, id);
 			parentCategoryRecordId = getRootCategoryRecordId(categoryEntityInterface, parentRecordId);
 		}
 
 		return parentCategoryRecordId;
 	}
 
 	/**
 	 * @param categoryEntity
 	 * @param parentRecordId
 	 * @return
 	 * @throws SQLException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private Long getRootCategoryRecordId(CategoryEntityInterface categoryEntity, Long parentRecordId) throws SQLException,
 			DynamicExtensionsSystemException
 	{
 		CategoryInterface category = categoryEntity.getCategory();
 		CategoryEntityInterface rootCategoryEntity = category.getRootCategoryElement();
 
 		StringBuffer query = new StringBuffer();
 		query.append(SELECT_KEYWORD + WHITESPACE + IDENTIFIER + WHITESPACE + FROM_KEYWORD + WHITESPACE
 				+ rootCategoryEntity.getTableProperties().getName() + WHITESPACE + WHERE_KEYWORD + WHITESPACE + RECORD_ID + EQUAL + parentRecordId);
 
 		Long rootCategoryRecordId = null;
 
 		List<Long> resultList = getResultIDList(query.toString(), IDENTIFIER);
 		if (resultList.size() > 0)
 		{
 			rootCategoryRecordId = (Long) resultList.get(0);
 		}
 
 		return rootCategoryRecordId;
 	}
 
 	/**
 	 * This method inserts data for a single category entity.
 	 * @param categoryEntity
 	 * @param dataValue
 	 * @param jdbcDAO
 	 * @param parentRecordId
 	 * @param dataMap
 	 * @param userId
 	 * @return parent record identifier
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws HibernateException
 	 * @throws SQLException
 	 * @throws DAOException
 	 * @throws UserNotAuthorizedException
 	 * @throws IOException
 	 * @throws ParseException
 	 */
 	private Long insertDataForSingleCategoryEntity(CategoryEntityInterface categoryEntity, Map dataValue, JDBCDAO jdbcDAO, Long parentRecordId,
 			Map dataMap, Long... userId) throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException, SQLException, DAOException,
 			UserNotAuthorizedException, ParseException, IOException
 	{
 		Long identifier = null;
 
 		Map<String, Long> keyMap = new HashMap<String, Long>();
 		Map<String, Long> fullKeyMap = new HashMap<String, Long>();
 		Map<String, List<Long>> recordsMap = new HashMap<String, List<Long>>();
 		Long id = ((userId != null || userId.length > 0) ? userId[0] : null);
 		CategoryInterface category = categoryEntity.getCategory();
 
 		if (categoryEntity == null)
 		{
 			throw new DynamicExtensionsSystemException("Input to insert data is null");
 		}
 
 		CategoryEntityInterface rootCategoryEntity = category.getRootCategoryElement();
 		String rootCategoryEntityName = rootCategoryEntity.getName();
 
 		// If root category entity does not have any attribute, or all its category attributes
 		// are related attributes, then explicitly insert identifier into entity table and
 		// insert this identifier as record identifier in category entity table.
 		if (rootCategoryEntity.getCategoryAttributeCollection().size() == 0 || isAllRelatedCategoryAttributesCollection(rootCategoryEntity))
 		{
 			// Insert blank record in all parent entity tables of root category entity so use insertDataForHeirarchy and add all keys to keymap, recordmap, fullkeymap.
 			Map<AbstractAttributeInterface, Object> attributeMap = new HashMap<AbstractAttributeInterface, Object>();
 			EntityManagerInterface entityManager = EntityManager.getInstance();
 			Long entityIdentifier = entityManager.insertDataForHeirarchy(categoryEntity.getEntity(), attributeMap, jdbcDAO, id);
 			keyMap.put(rootCategoryEntityName, entityIdentifier);
 			fullKeyMap.put(rootCategoryEntityName, entityIdentifier);
 
 			List<Long> idList = new ArrayList<Long>();
 			idList.add(entityIdentifier);
 			recordsMap.put(rootCategoryEntityName, idList);
 
 			while (categoryEntity.getParentCategoryEntity() != null)
 			{
 				keyMap.put(categoryEntity.getParentCategoryEntity().getName(), entityIdentifier);
 				fullKeyMap.put(categoryEntity.getParentCategoryEntity().getName(), entityIdentifier);
 
 				List<Long> recoIdList = recordsMap.get(categoryEntity.getParentCategoryEntity().getName());
 				if (recoIdList == null)
 				{
 					recoIdList = new ArrayList<Long>();
 				}
 				recoIdList.add(entityIdentifier);
 				recordsMap.put(categoryEntity.getParentCategoryEntity().getName(), recoIdList);
 				categoryEntity = categoryEntity.getParentCategoryEntity();
 
 			}
 			//
 			Long categoryIdentifier = entityManagerUtil.getNextIdentifier(rootCategoryEntity.getTableProperties().getName());
 			String categoryEntityTableInsertQuery = "INSERT INTO " + rootCategoryEntity.getTableProperties().getName()
 					+ " (IDENTIFIER, ACTIVITY_STATUS, " + RECORD_ID + ") VALUES (" + categoryIdentifier + ", 'ACTIVE', " + entityIdentifier + ")";
 
 			executeUpdateQuery(categoryEntityTableInsertQuery, id, jdbcDAO);
 			logDebug("insertData", "categoryEntityTableInsertQuery is : " + categoryEntityTableInsertQuery);
 		}
 
 		boolean isMultipleRecords = false;
 		boolean isNoCategoryAttributePresent = false;
 
 		String entityForeignKeyColumnName = null;
 		String categoryEntityForeignKeyColumnName = null;
 		Long sourceCategoryEntityId = null;
 		Long sourceEntityId = null;
 		//Separate out cat. attribute and cat. association ,as from UI map it can contains both in anyorder but we requires to insert attribute first for rootcatgoryentity
 		Map<CategoryAttributeInterface, Object> categoryAttributeMap = new HashMap<CategoryAttributeInterface, Object>();
 		Map<CategoryAssociationInterface, Object> categoryAssociationMap = new HashMap<CategoryAssociationInterface, Object>();
 
 		Set<BaseAbstractAttributeInterface> keySet = dataValue.keySet();
 		Iterator<BaseAbstractAttributeInterface> iter = keySet.iterator();
 
 		while (iter.hasNext())
 		{
 			Object obj = iter.next();
 			if (obj instanceof CategoryAttributeInterface)
 			{
 				categoryAttributeMap.put((CategoryAttributeInterface) obj, dataValue.get(obj));
 			}
 			else
 			{
 				categoryAssociationMap.put((CategoryAssociationInterface) obj, dataValue.get(obj));
 			}
 		}
 		insertRecordsForCategoryEntityTree(entityForeignKeyColumnName, categoryEntityForeignKeyColumnName, sourceCategoryEntityId, sourceEntityId,
 				categoryEntity, categoryAttributeMap, keyMap, fullKeyMap, recordsMap, isMultipleRecords, isNoCategoryAttributePresent, jdbcDAO, id);
 
 		insertRecordsForCategoryEntityTree(entityForeignKeyColumnName, categoryEntityForeignKeyColumnName, sourceCategoryEntityId, sourceEntityId,
 				categoryEntity, categoryAssociationMap, keyMap, fullKeyMap, recordsMap, isMultipleRecords, isNoCategoryAttributePresent, jdbcDAO, id);
 
 		Long rootCategoryEntityRecordId = getRootCategoryEntityRecordId(category.getRootCategoryElement(), (Long) fullKeyMap.get(rootCategoryEntity
 				.getName()));
 
 		insertRecordsForRelatedAttributes(rootCategoryEntityRecordId, category.getRootCategoryElement(), recordsMap, jdbcDAO, id);
 
 		if (parentRecordId != null)
 		{
 			identifier = parentRecordId;
 		}
 		else
 		{
 			identifier = (Long) keyMap.get(rootCategoryEntity.getName());
 		}
 
 		return identifier;
 	}
 
 	/**
 	 * This method checks whether category attributes in a category entity's Parent are related attributes or not ,if yes then  insert it first.
 	 * @param rootCategoryEntity
 	 * @param recordsMap
 	 * @param jdbcDAO
 	 * @param userId
 	 * @throws SQLException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private void insertAllParentRelatedCategoryAttributesCollection(CategoryEntityInterface rootCategoryEntity, Map<String, List<Long>> recordsMap,
 			JDBCDAO jdbcDAO, Long userId) throws SQLException, DynamicExtensionsSystemException
 	{
 		// Get all attributes including parent's attributes.
 		Collection<CategoryAttributeInterface> categoryAttributes = rootCategoryEntity.getAllCategoryAttributes();
 		for (CategoryAttributeInterface categoryAttribute : categoryAttributes)
 		{
 			if (categoryAttribute.getIsRelatedAttribute() != null && categoryAttribute.getIsRelatedAttribute())
 			{
 				//In case of related attribute.check whether it is parent's attribute.
 				if (!categoryAttribute.getAbstractAttribute().getEntity().equals(rootCategoryEntity.getEntity()))
 				{
 					StringBuffer columnNames = new StringBuffer();
 					StringBuffer columnValues = new StringBuffer();
 					StringBuffer columnNamesValues = new StringBuffer();
 					AttributeInterface attributeInterface = categoryAttribute.getAbstractAttribute().getEntity().getAttributeByName(
 							categoryAttribute.getAbstractAttribute().getName());
 					// Fetch column names and column values for related category attributes.
 					populateColumnNamesAndValues(attributeInterface, categoryAttribute, columnNames, columnValues, columnNamesValues);
 
 					String entityTableName = attributeInterface.getEntity().getTableProperties().getName();
 					List<Long> recordIdList = recordsMap.get(rootCategoryEntity.getName());
 					if (recordIdList != null && columnNamesValues.length() > 0)
 					{
 						for (Long identifer : recordIdList)
 						{
 							String updateEntityQuery = "UPDATE " + entityTableName + " SET " + columnNamesValues + " WHERE IDENTIFIER = " + identifer;
 							executeUpdateQuery(updateEntityQuery, userId, jdbcDAO);
 						}
 					}
 
 				}
 			}
 		}
 	}
 
 	/**
 	 * This method checks if all category attributes in a category entity are related attributes.
 	 * @param categoryEntity
 	 * @return true if all category attributes are related attributes, false otherwise
 	 */
 	private boolean isAllRelatedCategoryAttributesCollection(CategoryEntityInterface categoryEntity)
 	{
 		Collection<CategoryAttributeInterface> categoryAttributes = categoryEntity.getAllCategoryAttributes(); //.getCategoryAttributeCollection();
 
 		for (CategoryAttributeInterface categoryAttribute : categoryAttributes)
 		{
 			if (categoryAttribute.getIsRelatedAttribute() == null || categoryAttribute.getIsRelatedAttribute() == false)
 			{
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * This method is used to check whether all attributes are  invisible type related attribute or any one is related and visible or all are normal attribute
 	 * @param categoryEntity
 	 * @return
 	 */
 	private boolean isAllRelatedInvisibleCategoryAttributesCollection(CategoryEntityInterface categoryEntity)
 	{
 		Collection<CategoryAttributeInterface> categoryAttributes = categoryEntity.getAllCategoryAttributes();
 
 		if (categoryAttributes != null && categoryAttributes.size() == 0)
 		{
 			return false;
 		}
 
 		for (CategoryAttributeInterface categoryAttribute : categoryAttributes)
 		{
 			if (categoryAttribute.getIsRelatedAttribute() == null || categoryAttribute.getIsRelatedAttribute() == false)
 			{
 				return false;
 			}
 			else if (categoryAttribute.getIsRelatedAttribute() == true
 					&& (categoryAttribute.getIsVisible() != null && categoryAttribute.getIsVisible() == true))
 			{
 				return false;
 
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * Insert records for related attributes in each category entity.
 	 * @param rootRecordId
 	 * @param rootCategoryeEntity
 	 * @param recordsMap
 	 * @param jdbcDAO
 	 * @param id
 	 * @throws SQLException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private void insertRecordsForRelatedAttributes(Long rootRecordId, CategoryEntityInterface rootCategoryeEntity,
 			Map<String, List<Long>> recordsMap, JDBCDAO jdbcDAO, Long id) throws SQLException, DynamicExtensionsSystemException
 	{
 		CategoryInterface category = rootCategoryeEntity.getCategory();
 		Collection<CategoryEntityInterface> relatedAttributeCategoryEntities = category.getRelatedAttributeCategoryEntityCollection();
 		//call  this method for rootcategoryentity's parent's  related attribute
 		insertAllParentRelatedCategoryAttributesCollection(rootCategoryeEntity, recordsMap, jdbcDAO, id);
 		for (CategoryEntityInterface categoryEntity : relatedAttributeCategoryEntities)
 		{
 			StringBuffer columnNames = new StringBuffer();
 			StringBuffer columnValues = new StringBuffer();
 			StringBuffer columnNamesValues = new StringBuffer();
 
 			// Fetch column names and column values for related category attributes.
 			getColumnNamesAndValuesForRelatedCategoryAttributes(categoryEntity, columnNames, columnValues, columnNamesValues);
 
 			CategoryAssociationInterface categoryAssociation = getCategoryAssociationWithRootCategoryEntity(rootCategoryeEntity, categoryEntity);
 
 			if (categoryAssociation == null)
 			{
 				//pass the category entity this is  parent category entity of root categoryentity so we have to insertinto parent entity table
 				insertRelatedAttributeRecordsForRootCategoryEntity(categoryEntity, columnNamesValues, recordsMap, jdbcDAO, id);
 			}
 			else
 			{
 				insertRelatedAttributeRecordsForCategoryEntity(categoryEntity, categoryAssociation, columnNames, columnValues, columnNamesValues,
 						rootRecordId, recordsMap, jdbcDAO, id);
 			}
 		}
 	}
 
 	/**
 	 * This method clubs column names and column values for related category attributes.
 	 * @param categoryEntity
 	 * @param columnNames
 	 * @param columnValues
 	 * @param columnNamesValues
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private void getColumnNamesAndValuesForRelatedCategoryAttributes(CategoryEntityInterface categoryEntity, StringBuffer columnNames,
 			StringBuffer columnValues, StringBuffer columnNamesValues) throws DynamicExtensionsSystemException
 	{
 		Collection<CategoryAttributeInterface> categoryAttributes = new HashSet<CategoryAttributeInterface>();
 
 		categoryAttributes = categoryEntity.getCategoryAttributeCollection();
 		for (CategoryAttributeInterface categoryAttribute : categoryAttributes)
 		{
 			if (categoryAttribute.getIsRelatedAttribute() != null && categoryAttribute.getIsRelatedAttribute() == true)
 			{
 				AttributeInterface attributeInterface = categoryAttribute.getAbstractAttribute().getEntity().getAttributeByName(
 						categoryAttribute.getAbstractAttribute().getName());
 				populateColumnNamesAndValues(attributeInterface, categoryAttribute, columnNames, columnValues, columnNamesValues);
 			}
 		}
 	}
 
 	/**
 	 * @param attribute
 	 * @param defaultValue
 	 * @param columnNames
 	 * @param columnValues
 	 * @param columnNamesValues
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private void populateColumnNamesAndValues(AttributeInterface attribute, CategoryAttributeInterface categoryAttribute, StringBuffer columnNames,
 			StringBuffer columnValues, StringBuffer columnNamesValues) throws DynamicExtensionsSystemException
 	{
 		String columnName = attribute.getColumnProperties().getName();
 		AttributeTypeInformationInterface attributeInformation = attribute.getAttributeTypeInformation();
 		// If attribute type information is date type whose default value is not getting set as if it is read only then skip it, do not set in query.
 		if (!(attributeInformation instanceof DateAttributeTypeInformation && categoryAttribute.getDefaultValue() == null))
 		{
 
 			if (columnNames.toString().length() > 0)
 			{
 				columnNames.append(", ");
 				columnValues.append(", ");
 				columnNamesValues.append(", ");
 			}
 
 			String defaultValue = categoryAttribute.getDefaultValue();
 
 			if (attributeInformation instanceof BooleanAttributeTypeInformation)
 			{
 				defaultValue = DynamicExtensionsUtility.getValueForCheckBox(DynamicExtensionsUtility.isCheckBoxChecked(defaultValue));
 			}
 
 			// Replace any single and double quotes value with a proper escape character.
 			defaultValue = DynamicExtensionsUtility.getEscapedStringValue(defaultValue);
 
 			if (Variables.databaseName.equals(Constants.ORACLE_DATABASE) && attributeInformation instanceof DateAttributeTypeInformation)
 			{
 				columnNames.append(columnName);
 				columnValues.append(DynamicExtensionsUtility
 						.getDefaultDateForRelatedCategoryAttribute(attribute, categoryAttribute.getDefaultValue()));
 				columnNamesValues.append(columnName);
 				columnNamesValues.append(" = ");
 				columnNamesValues.append(DynamicExtensionsUtility.getDefaultDateForRelatedCategoryAttribute(attribute, categoryAttribute
 						.getDefaultValue()));
 			}
 			else
 			{
 				columnNames.append(columnName);
 				columnValues.append("'" + defaultValue + "'");
 				columnNamesValues.append(columnName);
 				columnNamesValues.append(" = ");
 				columnNamesValues.append("'" + defaultValue + "'");
 			}
 		}
 	}
 
 	/**
 	 * This method returns a category association between root category entity and category entity passed to this method.
 	 * @param rootCategoryeEntity
 	 * @param categoryEntity
 	 * @return category association between root category entity and category entity passed
 	 */
 	private CategoryAssociationInterface getCategoryAssociationWithRootCategoryEntity(CategoryEntityInterface rootCategoryeEntity,
 			CategoryEntityInterface categoryEntity)
 	{
 		Collection<CategoryAssociationInterface> categoryAssociations = rootCategoryeEntity.getCategoryAssociationCollection();
 
 		for (CategoryAssociationInterface categoryAssociation : categoryAssociations)
 		{
 			if (categoryAssociation.getTargetCategoryEntity() != null && categoryAssociation.getTargetCategoryEntity().equals(categoryEntity))
 			{
 				return categoryAssociation;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * This method inserts records for related category attributes of root category entity.
 	 * @param rootCategoryeEntity
 	 * @param columnNamesValues
 	 * @param recordsMap
 	 * @param jdbcDAO
 	 * @param userId
 	 * @throws SQLException
 	 */
 	private void insertRelatedAttributeRecordsForRootCategoryEntity(CategoryEntityInterface rootCategoryeEntity, StringBuffer columnNamesValues,
 			Map<String, List<Long>> recordsMap, JDBCDAO jdbcDAO, Long userId) throws SQLException
 	{
 		String entityTableName = rootCategoryeEntity.getEntity().getTableProperties().getName();
 		List<Long> recordIdList = recordsMap.get(rootCategoryeEntity.getName());
 		if (recordIdList != null && columnNamesValues.length() > 0)
 		{
 			for (Long identifer : recordIdList)
 			{
 				String updateEntityQuery = "UPDATE " + entityTableName + " SET " + columnNamesValues + " WHERE IDENTIFIER = " + identifer;
 				executeUpdateQuery(updateEntityQuery, userId, jdbcDAO);
 			}
 		}
 	}
 
 	/**
 	 * This method inserts records for related category attributes of category entities other than root category entity.
 	 * @param categoryEntity
 	 * @param categoryAssociation
 	 * @param columnNames
 	 * @param columnValues
 	 * @param relatedAttributeUpdateQuery
 	 * @param rootRecordId
 	 * @param recordsMap
 	 * @param jdbcDAO
 	 * @param userId
 	 * @throws SQLException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private void insertRelatedAttributeRecordsForCategoryEntity(CategoryEntityInterface categoryEntity,
 			CategoryAssociationInterface categoryAssociation, StringBuffer columnNames, StringBuffer columnValues,
 			StringBuffer relatedAttributeUpdateQuery, Long rootRecordId, Map<String, List<Long>> recordsMap, JDBCDAO jdbcDAO, Long userId)
 			throws SQLException, DynamicExtensionsSystemException
 	{
 		String categoryEntityTableName = categoryEntity.getTableProperties().getName();
 		String entityTableName = categoryEntity.getEntity().getTableProperties().getName();
 		String categoryEntityForeignKey = categoryAssociation.getConstraintProperties().getTargetEntityKey();
 
 		List<Long> sourceEntityId = recordsMap.get(categoryAssociation.getCategoryEntity().getName());
 
 		if (recordsMap.get(categoryEntity.getName()) != null)
 		{
 			//insertRelatedAttributesRecordForCategoryEntitiesOnUI(recordsMap, categoryEntity, entityTableName, relatedAttributeUpdateQuery, userId, hibernateDAO, categoryEntityTableName, categoryEntityForeignKey, rootRecordId);
 			if (relatedAttributeUpdateQuery.length() > 0)
 			{
 				List<Long> recordIdList = recordsMap.get(categoryEntity.getName());
 				for (Long id : recordIdList)
 				{
 					String updateEntityQuery = "UPDATE " + entityTableName + " SET " + relatedAttributeUpdateQuery + " WHERE IDENTIFIER = " + id;
 					executeUpdateQuery(updateEntityQuery, userId, jdbcDAO);
 
 					String selectQuery = "SELECT IDENTIFIER FROM " + categoryEntityTableName + " WHERE " + RECORD_ID + " = " + id;
 					List<Long> resultIdList = getResultIDList(selectQuery, "IDENTIFIER");
 
 					if (resultIdList.size() == 0)
 					{
 						Long categoryIdentifier = entityManagerUtil.getNextIdentifier(categoryEntityTableName);
 						String insertCategoryEntityQuery = "INSERT INTO " + categoryEntityTableName + " (IDENTIFIER, ACTIVITY_STATUS, " + RECORD_ID
 								+ ", " + categoryEntityForeignKey + ") VALUES (" + categoryIdentifier + ", 'ACTIVE', " + id + ", " + rootRecordId
 								+ ")";
 						executeUpdateQuery(insertCategoryEntityQuery, userId, jdbcDAO);
 					}
 				}
 			}
 		}
		else
 		{
 			//insertRelatedAttributeRecordsForCategoryEntitiesInPath(recordsMap, categoryEntity, columnNames, columnValues, categoryEntityForeignKey, sourceEntityId, rootRecordId, entityTableName, categoryEntityTableName, userId, hibernateDAO);
 			PathInterface path = categoryEntity.getPath();
 			Collection<PathAssociationRelationInterface> pathAssociationRelations = path.getSortedPathAssociationRelationCollection();
 
 			for (PathAssociationRelationInterface par : pathAssociationRelations)
 			{
 				AssociationInterface association = par.getAssociation();
 
 				if (association.getTargetEntity().getId() != categoryEntity.getEntity().getId())
 				{
 					if (recordsMap.get(association.getTargetEntity().getName() + "[" + par.getTargetInstanceId() + "]") == null)
 					{
 						sourceEntityId = new ArrayList<Long>();
 						for (Long sourceId : sourceEntityId)
 						{
 							Long entityIdentifier = entityManagerUtil.getNextIdentifier(association.getTargetEntity().getTableProperties().getName());
 							String insertQuery = "INSERT INTO " + association.getTargetEntity().getTableProperties().getName()
 									+ "(IDENTIFIER, ACTIVITY_STATUS, " + association.getConstraintProperties().getTargetEntityKey() + ") VALUES ("
 									+ entityIdentifier + ", 'ACTIVE'," + sourceId + ")";
 							executeUpdateQuery(insertQuery, userId, jdbcDAO);
 							sourceEntityId.add(entityIdentifier);
 						}
 
 						recordsMap.put(association.getTargetEntity().getName() + "[" + par.getTargetInstanceId() + "]", sourceEntityId);
 					}
 					else
 					{
 						sourceEntityId = recordsMap.get(association.getTargetEntity().getName() + "[" + par.getTargetInstanceId() + "]");
 					}
 				}
 				else
 				{
 					for (Long sourceId : sourceEntityId)
 					{
 						Long entityIdentifier = entityManagerUtil.getNextIdentifier(entityTableName);
 						String insertEntityQuery = "INSERT INTO " + entityTableName + " (IDENTIFIER, ACTIVITY_STATUS, " + columnNames + ", "
 								+ association.getConstraintProperties().getTargetEntityKey() + ") VALUES (" + entityIdentifier + ", " + "'ACTIVE', "
 								+ columnValues + ", " + sourceId + ")";
 						executeUpdateQuery(insertEntityQuery, userId, jdbcDAO);
 
 						Long categoryIdentifier = entityManagerUtil.getNextIdentifier(categoryEntityTableName);
 						String insertCategoryEntityQuery = "INSERT INTO " + categoryEntityTableName + " (IDENTIFIER, ACTIVITY_STATUS, " + RECORD_ID
 								+ ", " + categoryEntityForeignKey + ") VALUES (" + categoryIdentifier + ", 'ACTIVE', " + entityIdentifier + ", "
 								+ rootRecordId + ")";
 						executeUpdateQuery(insertCategoryEntityQuery, userId, jdbcDAO);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param recordsMap
 	 * @param categoryEntity
 	 * @param columnNames
 	 * @param columnValues
 	 * @param categoryEntityForeignKey
 	 * @param sourceEntityId
 	 * @param rootRecordId
 	 * @param entityTableName
 	 * @param categoryEntityTableName
 	 * @param userId
 	 * @param jdbcDAO
 	 * @throws DynamicExtensionsSystemException
 	 * @throws SQLException
 	 */
 	/*private void insertRelatedAttributeRecordsForCategoryEntitiesInPath(Map<String, List<Long>> recordsMap, CategoryEntityInterface categoryEntity, StringBuffer columnNames, StringBuffer columnValues, String categoryEntityForeignKey, List<Long> sourceEntityId, Long rootRecordId,
 	 String entityTableName, String categoryEntityTableName, Long userId, HibernateDAO hibernateDAO) throws DynamicExtensionsSystemException, SQLException
 	 {
 	 PathInterface path = categoryEntity.getPath();
 	 Collection<PathAssociationRelationInterface> pathAssociationRelations = path.getSortedPathAssociationRelationCollection();
 
 	 for (PathAssociationRelationInterface par: pathAssociationRelations)
 	 {
 	 AssociationInterface association = par.getAssociation();
 
 	 if (association.getTargetEntity().getId() != categoryEntity.getEntity().getId())
 	 {
 	 if (recordsMap.get(association.getTargetEntity().getName()+"["+par.getTargetInstanceId()+"]") == null)
 	 {
 	 sourceEntityId = new ArrayList<Long>();
 	 for (Long sourceId: sourceEntityId)
 	 {
 	 Long entityIdentifier = entityManagerUtil.getNextIdentifier(association.getTargetEntity().getTableProperties().getName());
 	 String insertQuery = "INSERT INTO "+association.getTargetEntity().getTableProperties().getName()+"(IDENTIFIER, ACTIVITY_STATUS, "+association.getConstraintProperties().getTargetEntityKey()+") VALUES ("+entityIdentifier+", 'ACTIVE',"+sourceId+")";
 	 executeUpdateQuery(insertQuery, userId, hibernateDAO);
 	 sourceEntityId.add(entityIdentifier);
 	 }
 
 	 recordsMap.put(association.getTargetEntity().getName()+"["+par.getTargetInstanceId()+"]", sourceEntityId);
 	 }
 	 else
 	 {
 	 sourceEntityId = recordsMap.get(association.getTargetEntity().getName()+"["+par.getTargetInstanceId()+"]");
 	 }
 	 }
 	 else
 	 {
 	 for (Long sourceId: sourceEntityId)
 	 {
 	 Long entityIdentifier = entityManagerUtil.getNextIdentifier(entityTableName);
 	 String insertEntityQuery = "INSERT INTO "+entityTableName+" (IDENTIFIER, ACTIVITY_STATUS, "+columnNames+", "+association.getConstraintProperties().getTargetEntityKey()+") VALUES ("+entityIdentifier+", "+ "'ACTIVE', "+columnValues+", "+sourceId+")";
 	 executeUpdateQuery(insertEntityQuery, userId, hibernateDAO);
 
 	 Long categoryIdentifier = entityManagerUtil.getNextIdentifier(categoryEntityTableName);
 	 String insertCategoryEntityQuery = "INSERT INTO "+categoryEntityTableName+" (IDENTIFIER, ACTIVITY_STATUS, "+RECORD_ID+", "+categoryEntityForeignKey+") VALUES ("+categoryIdentifier+", 'ACTIVE', "+entityIdentifier+", "+rootRecordId+")";
 	 executeUpdateQuery(insertCategoryEntityQuery, userId, hibernateDAO);
 	 }
 	 }
 	 }
 	 }*/
 
 	/**
 	 * @param recordsMap
 	 * @param categoryEntity
 	 * @param entityTableName
 	 * @param relatedAttributeUpdateQuery
 	 * @param userId
 	 * @param jdbcDAO
 	 * @param categoryEntityTableName
 	 * @param categoryEntityForeignKey
 	 * @param rootRecordId
 	 * @throws SQLException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	/*private void insertRelatedAttributesRecordForCategoryEntitiesOnUI(Map<String, List<Long>> recordsMap, CategoryEntityInterface categoryEntity, String entityTableName,
 	 StringBuffer relatedAttributeUpdateQuery, Long userId, HibernateDAO hibernateDAO, String categoryEntityTableName,
 	 String categoryEntityForeignKey, Long rootRecordId) throws SQLException, DynamicExtensionsSystemException
 	 {
 	 List<Long> recordIdList = recordsMap.get(categoryEntity.getName());
 	 for (Long id: recordIdList)
 	 {
 	 String updateEntityQuery = "UPDATE "+entityTableName+" SET "+relatedAttributeUpdateQuery+" WHERE IDENTIFIER = "+id;
 	 executeUpdateQuery(updateEntityQuery, userId, hibernateDAO);
 
 	 String selectQuery = "SELECT IDENTIFIER FROM "+categoryEntityTableName+" WHERE "+RECORD_ID+" = "+id;
 	 List<Long> resultIdList = getResultIDList(selectQuery, "IDENTIFIER");
 
 	 if (resultIdList.size() == 0)
 	 {
 	 Long categoryIdentifier = entityManagerUtil.getNextIdentifier(categoryEntityTableName);
 	 String insertCategoryEntityQuery = "INSERT INTO "+categoryEntityTableName+" (IDENTIFIER, ACTIVITY_STATUS, "+RECORD_ID+", "+categoryEntityForeignKey+") VALUES ("+categoryIdentifier+", 'ACTIVE', "+id+", "+rootRecordId+")";
 	 executeUpdateQuery(insertCategoryEntityQuery, userId, hibernateDAO);
 	 }
 	 }
 	 }*/
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.CategoryManagerInterface#editData(edu.common.dynamicextensions.domaininterface.CategoryEntityInterface, java.util.Map, java.lang.Long)
 	 */
 	public boolean editData(CategoryEntityInterface rootcategoryEntity, Map<BaseAbstractAttributeInterface, Object> attributeValueMap, Long recordId,
 			Long... userId) throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException, SQLException
 	{
 		CategoryInterface category = rootcategoryEntity.getCategory();
 
 		Long entityRecordId = getRootCategoryEntityRecordId(rootcategoryEntity, recordId);
 		Long id = ((userId != null || userId.length > 0) ? userId[0] : null);
 		List<Long> entityRecordIdList = new ArrayList<Long>();
 		entityRecordIdList.add(entityRecordId);
 
 		Map<AbstractAttributeInterface, Object> rootEntityRecordsMap = new HashMap<AbstractAttributeInterface, Object>();
 		populateRootEntityRecordMap(rootcategoryEntity, rootEntityRecordsMap, attributeValueMap);
 
 		JDBCDAO jdbcDAO = null;
 
 		Boolean isEdited = false;
 		Stack<String> categoryEntityReverseQueryStack = new Stack<String>();
 
 		try
 		{
 			DAOFactory factory = DAOFactory.getInstance();
 			jdbcDAO = (JDBCDAO) factory.getDAO(Constants.JDBC_DAO);
 			jdbcDAO.openSession(null);
 
 			// Clear all records from entity table.
 			EntityManagerInterface entityManager = EntityManager.getInstance();
 			isEdited = entityManager.editDataForHeirarchy(rootcategoryEntity.getEntity(), rootEntityRecordsMap, entityRecordId, jdbcDAO, id);
 
 			// Clear all records from category entity table.
 			clearCategoryEntityData(rootcategoryEntity, recordId, categoryEntityReverseQueryStack, id, jdbcDAO);
 
 			if (isEdited)
 			{
 				Map<String, Long> keyMap = new HashMap<String, Long>();
 				Map<String, Long> fullKeyMap = new HashMap<String, Long>();
 				Map<String, List<Long>> recordsMap = new HashMap<String, List<Long>>();
 
 				keyMap.put(rootcategoryEntity.getName(), entityRecordId);
 				fullKeyMap.put(rootcategoryEntity.getName(), entityRecordId);
 				List<Long> idList = new ArrayList<Long>();
 				idList.add(entityRecordId);
 				recordsMap.put(rootcategoryEntity.getName(), idList);
 
 				CategoryEntityInterface catEntity = rootcategoryEntity;
 				//add parent's record id also as parent entiy tables are edited in editDataForHeirarchy
 				while (catEntity.getParentCategoryEntity() != null)
 				{
 					keyMap.put(catEntity.getParentCategoryEntity().getName(), entityRecordId);
 					fullKeyMap.put(catEntity.getParentCategoryEntity().getName(), entityRecordId);
 
 					List<Long> recoIdList = recordsMap.get(catEntity.getParentCategoryEntity().getName());
 					if (recoIdList == null)
 					{
 						recoIdList = new ArrayList<Long>();
 					}
 					recoIdList.add(entityRecordId);
 					recordsMap.put(catEntity.getParentCategoryEntity().getName(), recoIdList);
 					catEntity = catEntity.getParentCategoryEntity();
 
 				}
 
 				for (CategoryAttributeInterface categoryAttribute : rootcategoryEntity.getAllCategoryAttributes())
 				{
 					attributeValueMap.remove(categoryAttribute);
 				}
 
 				boolean isMultipleRecords = false;
 				boolean isNoCategoryAttributePresent = false;
 
 				String entityForeignKeyColumnName = null;
 				String categoryEntityForeignKeyColumnName = null;
 				Long sourceCategoryEntityId = null;
 				Long sourceEntityId = null;
 
 				insertRecordsForCategoryEntityTree(entityForeignKeyColumnName, categoryEntityForeignKeyColumnName, sourceCategoryEntityId,
 						sourceEntityId, rootcategoryEntity, attributeValueMap, keyMap, fullKeyMap, recordsMap, isMultipleRecords,
 						isNoCategoryAttributePresent, jdbcDAO, id);
 
 				Long rootCategoryEntityId = getRootCategoryEntityRecordId(category.getRootCategoryElement(), (Long) fullKeyMap.get(rootcategoryEntity
 						.getName()));
 
 				insertRecordsForRelatedAttributes(rootCategoryEntityId, category.getRootCategoryElement(), recordsMap, jdbcDAO, id);
 
 				jdbcDAO.commit();
 			}
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			//rollbackQueries(categoryEntityReverseQueryStack);
 			throw (DynamicExtensionsApplicationException) handleRollback(e, "Error while inserting data", jdbcDAO, false);
 		}
 		catch (Exception e)
 		{
 			throw (DynamicExtensionsSystemException) handleRollback(e, "Error while inserting data", jdbcDAO, true);
 		}
 		finally
 		{
 			try
 			{
 				jdbcDAO.closeSession();
 			}
 			catch (DAOException e)
 			{
 				throw (DynamicExtensionsSystemException) handleRollback(e, "Error while closing", jdbcDAO, true);
 			}
 		}
 
 		return isEdited;
 	}
 
 	/**
 	 * This helper method recursively inserts records for a single category entity
 	 * and all its category associations i.e. in turn for a whole category entity tree.
 	 * @param entityForeignKeyColumnName
 	 * @param categoryEntityForeignKeyColumnName
 	 * @param sourceCategoryEntityIdentifier
 	 * @param sourceEntityIdentifier
 	 * @param categoryEntity
 	 * @param dataValue
 	 * @param keyMap
 	 * @param fullKeyMap
 	 * @param isMultipleRecords
 	 * @param isNoCategoryAttributePresent
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
 	private void insertRecordsForCategoryEntityTree(String entityForeignKeyColumnName, String categoryEntityForeignKeyColumnName,
 			Long sourceCategoryEntityIdentifier, Long sourceEntityIdentifier, CategoryEntityInterface categoryEntity, Map dataValue,
 			Map<String, Long> keyMap, Map<String, Long> fullKeyMap, Map<String, List<Long>> recordsMap, boolean isMultipleRecords,
 			boolean isNoCategoryAttributePresent, JDBCDAO jdbcDAO, Long userId) throws DynamicExtensionsSystemException, SQLException,
 			HibernateException, DynamicExtensionsApplicationException, UserNotAuthorizedException, DAOException, ParseException, IOException
 	{
 		Object value = null;
 		boolean isCategoryEntityRecordInserted = false;
 		Map<AbstractAttributeInterface, Object> attributeMap = null;
 
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
 
 			if (attribute instanceof CategoryAttributeInterface && !isCategoryEntityRecordInserted)
 			{
 				String categoryEntityTableName = categoryEntity.getTableProperties().getName();//((CategoryAttribute) attribute).getCategoryEntity().getTableProperties().getName();
 
 				Long entityIdentifier = null;
 				EntityManagerInterface entityManager = EntityManager.getInstance();
 
 				if (isNoCategoryAttributePresent)
 				{
 					attributeMap = null;
 				}
 				else
 				{
 					attributeMap = createAttributeMap(dataValue);
 				}
 
 				if (keyMap.get(((CategoryAttribute) attribute).getCategoryEntity().getName()) != null && !isMultipleRecords)
 				{
 					entityIdentifier = (Long) keyMap.get(((CategoryAttribute) attribute).getCategoryEntity().getName());
 					//Edit data for entity heirarchy
 					entityManager.editDataForHeirarchy(categoryEntity.getEntity(), attributeMap, entityIdentifier, jdbcDAO, userId);
 				}
 				else
 				{
 					entityIdentifier = entityManager.insertDataForHeirarchy(categoryEntity.getEntity(), attributeMap, jdbcDAO, userId);
 
 				}
 				Long categoryEntityidentifier = null;
 				//Check whether table is created for categoryentity
 				if (((CategoryEntity) categoryEntity).isCreateTable())
 				{
 					categoryEntityidentifier = entityManagerUtil.getNextIdentifier(categoryEntity.getTableProperties().getName());
 					String insertQueryForCategoryEntity = "INSERT INTO " + categoryEntityTableName + " (IDENTIFIER, ACTIVITY_STATUS, " + RECORD_ID
 							+ ") VALUES (" + categoryEntityidentifier + ", 'ACTIVE', " + entityIdentifier + ")";
 					executeUpdateQuery(insertQueryForCategoryEntity, userId, jdbcDAO);
 				}
 
 				if (categoryEntityForeignKeyColumnName != null && entityForeignKeyColumnName != null)
 				{
 					if (((CategoryEntity) categoryEntity).isCreateTable())
 					{
 						String updateCategoryEntityQuery = "UPDATE " + categoryEntityTableName + " SET " + categoryEntityForeignKeyColumnName + " = "
 								+ sourceCategoryEntityIdentifier + " WHERE IDENTIFIER = " + categoryEntityidentifier;
 						executeUpdateQuery(updateCategoryEntityQuery, userId, jdbcDAO);
 					}
 
 					String updateEntityQuery = "UPDATE "
 							+ ((CategoryAttribute) attribute).getCategoryEntity().getEntity().getTableProperties().getName() + " SET "
 							+ entityForeignKeyColumnName + " = " + sourceEntityIdentifier + " WHERE IDENTIFIER = " + entityIdentifier;
 					executeUpdateQuery(updateEntityQuery, userId, jdbcDAO);
 				}
 
 				CategoryEntityInterface catEntity = categoryEntity;
 				keyMap.put(catEntity.getName(), entityIdentifier);
 				fullKeyMap.put(catEntity.getName(), entityIdentifier);
 
 				List<Long> recIdList = recordsMap.get(catEntity.getName());
 				if (recIdList == null)
 				{
 					recIdList = new ArrayList<Long>();
 				}
 				recIdList.add(entityIdentifier);
 				recordsMap.put(catEntity.getName(), recIdList);
 				while (catEntity.getParentCategoryEntity() != null)
 				{
 					keyMap.put(catEntity.getParentCategoryEntity().getName(), entityIdentifier);
 					fullKeyMap.put(catEntity.getParentCategoryEntity().getName(), entityIdentifier);
 
 					List<Long> recoIdList = recordsMap.get(catEntity.getParentCategoryEntity().getName());
 					if (recoIdList == null)
 					{
 						recoIdList = new ArrayList<Long>();
 					}
 					recoIdList.add(entityIdentifier);
 					recordsMap.put(catEntity.getParentCategoryEntity().getName(), recoIdList);
 					catEntity = catEntity.getParentCategoryEntity();
 
 				}
 
 				isCategoryEntityRecordInserted = true;
 			}
 			else if (attribute instanceof CategoryAssociationInterface)
 			{
 				CategoryAssociationInterface categoryAssociation = (CategoryAssociationInterface) attribute;
 
 				PathInterface path = categoryAssociation.getTargetCategoryEntity().getPath();
 				Collection<PathAssociationRelationInterface> pathAssociationRelations = path.getSortedPathAssociationRelationCollection();
 
 				Long sourceEntityId = (Long) fullKeyMap.get(categoryAssociation.getCategoryEntity().getName());
 				String foreignKeyColumnName = new String();
 
 				String selectQuery = "SELECT IDENTIFIER FROM " + categoryAssociation.getCategoryEntity().getTableProperties().getName() + " WHERE "
 						+ RECORD_ID + " = " + sourceEntityId;
 
 				List<Long> idList = getResultIDList(selectQuery, "IDENTIFIER");
 
 				Long resultId = null;
 				if (idList != null && idList.size() > 0)
 				{
 					resultId = idList.get(0);
 				}
 
 				Long sourceCategoryEntityId = resultId;
 
 				String categoryForeignKeyColName = categoryAssociation.getConstraintProperties().getTargetEntityKey();
 
 				EntityInterface entity = categoryAssociation.getTargetCategoryEntity().getEntity();
 
 				for (PathAssociationRelationInterface par : pathAssociationRelations)
 				{
 					AssociationInterface association = par.getAssociation();
 
 					foreignKeyColumnName = association.getConstraintProperties().getTargetEntityKey();
 
 					if (association.getTargetEntity().getId() != entity.getId())
 					{
 						if (fullKeyMap.get(association.getTargetEntity().getName() + "[" + par.getTargetInstanceId() + "]") == null)
 						{
 							Long entityIdentifier = entityManagerUtil.getNextIdentifier(association.getTargetEntity().getTableProperties().getName());
 							String insertQuery = "INSERT INTO " + association.getTargetEntity().getTableProperties().getName()
 									+ "(IDENTIFIER, ACTIVITY_STATUS, " + association.getConstraintProperties().getTargetEntityKey() + ") VALUES ("
 									+ entityIdentifier + ", 'ACTIVE'," + sourceEntityId + ")";
 							executeUpdateQuery(insertQuery, userId, jdbcDAO);
 
 							sourceEntityId = entityIdentifier;
 
 							fullKeyMap.put(association.getTargetEntity().getName() + "[" + par.getTargetInstanceId() + "]", sourceEntityId);
 							keyMap.put(association.getTargetEntity().getName() + "[" + par.getTargetInstanceId() + "]", sourceEntityId);
 
 							List<Long> recIdList = recordsMap.get(association.getTargetEntity().getName() + "[" + par.getTargetInstanceId() + "]");
 							if (recIdList == null)
 							{
 								recIdList = new ArrayList<Long>();
 							}
 							recIdList.add(entityIdentifier);
 							recordsMap.put(association.getTargetEntity().getName() + "[" + par.getTargetInstanceId() + "]", recIdList);
 						}
 						else
 						{
 							sourceEntityId = (Long) fullKeyMap.get(association.getTargetEntity().getName() + "[" + par.getTargetInstanceId() + "]");
 						}
 					}
 					else
 					{
 						sourceEntityId = (Long) fullKeyMap.get(association.getEntity().getName() + "[" + par.getSourceInstanceId() + "]");
 					}
 				}
 
 				List<Map<BaseAbstractAttributeInterface, Object>> listOfMapsForContainedEntity = (List) value;
 
 				Map<CategoryAttributeInterface, Object> categoryAttributeMap = new HashMap<CategoryAttributeInterface, Object>();
 				Map<CategoryAssociationInterface, Object> categoryAssociationMap = new HashMap<CategoryAssociationInterface, Object>();
 
 				for (Map<BaseAbstractAttributeInterface, Object> valueMapForContainedEntity : listOfMapsForContainedEntity)
 				{
 					Set<BaseAbstractAttributeInterface> keySet = valueMapForContainedEntity.keySet();
 					Iterator<BaseAbstractAttributeInterface> iter = keySet.iterator();
 
 					while (iter.hasNext())
 					{
 						Object obj = iter.next();
 						if (obj instanceof CategoryAttributeInterface)
 						{
 							categoryAttributeMap.put((CategoryAttributeInterface) obj, valueMapForContainedEntity.get(obj));
 						}
 						else
 						{
 							categoryAssociationMap.put((CategoryAssociationInterface) obj, valueMapForContainedEntity.get(obj));
 						}
 					}
 
 					if (listOfMapsForContainedEntity.size() > 1)
 					{
 						isMultipleRecords = true;
 					}
 					else
 					{
 						isMultipleRecords = false;
 					}
 
 					// Insert data for category attributes.
 					if (categoryAttributeMap.size() == 0)
 					{
 						isNoCategoryAttributePresent = true;
 						CategoryAttributeInterface dummyCategoryAttribute = DomainObjectFactory.getInstance().createCategoryAttribute();
 						dummyCategoryAttribute.setCategoryEntity(categoryAssociation.getTargetCategoryEntity());
 						categoryAttributeMap.put(dummyCategoryAttribute, "");
 
 						insertRecordsForCategoryEntityTree(foreignKeyColumnName, categoryForeignKeyColName, sourceCategoryEntityId, sourceEntityId,
 								categoryAssociation.getTargetCategoryEntity(), categoryAttributeMap, keyMap, fullKeyMap, recordsMap,
 								isMultipleRecords, isNoCategoryAttributePresent, jdbcDAO, userId);
 
 						isNoCategoryAttributePresent = false;
 					}
 					else
 					{
 						insertRecordsForCategoryEntityTree(foreignKeyColumnName, categoryForeignKeyColName, sourceCategoryEntityId, sourceEntityId,
 								categoryAssociation.getTargetCategoryEntity(), categoryAttributeMap, keyMap, fullKeyMap, recordsMap,
 								isMultipleRecords, isNoCategoryAttributePresent, jdbcDAO, userId);
 					}
 					categoryAttributeMap.clear();
 
 					// Insert data for category associations.
 					insertRecordsForCategoryEntityTree(foreignKeyColumnName, categoryForeignKeyColName, sourceCategoryEntityId, sourceEntityId,
 							categoryAssociation.getTargetCategoryEntity(), categoryAssociationMap, keyMap, fullKeyMap, recordsMap, isMultipleRecords,
 							isNoCategoryAttributePresent, jdbcDAO, userId);
 					categoryAssociationMap.clear();
 
 					fullKeyMap.putAll(keyMap);
 					keyMap.remove(categoryAssociation.getTargetCategoryEntity().getName());
 				}
 			}
 		}
 	}
 
 	/**
 	 * This method returns the category data value map for the given root category entity.
 	 * @param rootCategoryEntity
 	 * @param recordId
 	 * @return map of category entity data
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws SQLException
 	 */
 	public Map<BaseAbstractAttributeInterface, Object> getRecordById(CategoryEntityInterface rootCategoryEntity, Long recordId)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException, SQLException
 	{
 		Map<BaseAbstractAttributeInterface, Object> categoryDataValueMap = new HashMap<BaseAbstractAttributeInterface, Object>();
 
 		HibernateDAO hibernateDAO = null;
 
 		try
 		{
 			DAOFactory factory = DAOFactory.getInstance();
 			hibernateDAO = (HibernateDAO) factory.getDAO(Constants.HIBERNATE_DAO);
 			hibernateDAO.openSession(null);
 			retrieveRecords(rootCategoryEntity, categoryDataValueMap, recordId);
 		}
 		catch (Exception e)
 		{
 			throw (DynamicExtensionsSystemException) handleRollback(e, "Error while retrieving data", hibernateDAO, true);
 		}
 		finally
 		{
 			try
 			{
 				hibernateDAO.closeSession();
 			}
 			catch (DAOException e)
 			{
 				throw (DynamicExtensionsSystemException) handleRollback(e, "Error while closing", hibernateDAO, true);
 			}
 		}
 
 		return categoryDataValueMap;
 	}
 
 	/**
 	 * @param categoryEntity
 	 * @param categoryDataValueMap
 	 * @param rootCategoryEntityRecordId
 	 * @throws SQLException
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private void retrieveRecords(CategoryEntityInterface categoryEntity, Map<BaseAbstractAttributeInterface, Object> categoryDataValueMap,
 			long rootCategoryEntityRecordId) throws SQLException, DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Long recordId = null;
 		String categoryEntityTableName = "";
 		String selectRecordIdQuery = "";
 
 		boolean isRecordIdNull = false;
 
 		// Only the root category entity has the category object set in it.
 		if (categoryEntity.getCategory() != null)
 		{
 			categoryEntityTableName = categoryEntity.getTableProperties().getName();
 
 			selectRecordIdQuery = "SELECT " + RECORD_ID + " FROM " + categoryEntityTableName + " WHERE IDENTIFIER = " + rootCategoryEntityRecordId;
 
 			List<Long> idList = getResultIDList(selectRecordIdQuery, RECORD_ID);
 
 			if (idList != null && idList.size() > 0)
 			{
 				recordId = idList.get(0);
 			}
 		}
 
 		// If entity model is different than category model then entity data is inserted
 		// according to entity model and category data is inserted according to category model.
 		// In this case recordId can be NULL in category entity table.
 		if (recordId == null)
 		{
 			isRecordIdNull = true;
 			recordId = rootCategoryEntityRecordId;
 		}
 
 		Map<AbstractAttributeInterface, Object> entityRecordsMap = new HashMap<AbstractAttributeInterface, Object>();
 		entityRecordsMap.putAll(EntityManager.getInstance().getEntityRecordById(categoryEntity.getEntity(), recordId));
 
 		//If root catentity has parent entity then get data from parent entitytable  with same record id
 		CategoryEntityInterface objParentCatEntity = categoryEntity.getParentCategoryEntity();
 		while (objParentCatEntity != null)
 		{
 			Map<AbstractAttributeInterface, Object> innerValueMap = EntityManager.getInstance().getEntityRecordById(objParentCatEntity.getEntity(),
 					recordId);
 			if (innerValueMap != null)
 				entityRecordsMap.putAll(innerValueMap);
 			objParentCatEntity = objParentCatEntity.getParentCategoryEntity();
 
 		}
 
 		if (!isAllRelatedInvisibleCategoryAttributesCollection(categoryEntity))
 		{
 			for (CategoryAttributeInterface categoryAttribute : categoryEntity.getAllCategoryAttributes())
 			{
 				categoryDataValueMap.put(categoryAttribute, entityRecordsMap.get(categoryAttribute.getAbstractAttribute()));
 			}
 		}
 
 		Collection<CategoryAssociationInterface> categoryAssociationCollection = new ArrayList<CategoryAssociationInterface>(categoryEntity
 				.getCategoryAssociationCollection());
 
 		for (CategoryAssociationInterface categoryAssociation : categoryAssociationCollection)
 		{
 			CategoryEntityInterface targetCategoryEntity = categoryAssociation.getTargetCategoryEntity();
 			if (targetCategoryEntity != null && !isAllRelatedInvisibleCategoryAttributesCollection(targetCategoryEntity)
 					&& (((CategoryEntity) targetCategoryEntity).isCreateTable()))
 			{
 				categoryEntityTableName = targetCategoryEntity.getTableProperties().getName();
 
 				if (isRecordIdNull)
 				{
 					String selectQuery = "SELECT IDENTIFIER FROM " + categoryAssociation.getCategoryEntity().getTableProperties().getName()
 							+ " WHERE " + RECORD_ID + " = " + recordId;
 
 					List<Long> idList = getResultIDList(selectQuery, "IDENTIFIER");
 
 					if (idList != null && idList.size() > 0)
 					{
 						rootCategoryEntityRecordId = idList.get(0);
 					}
 				}
 
 				selectRecordIdQuery = "SELECT " + RECORD_ID + " FROM " + categoryEntityTableName + " WHERE "
 						+ categoryAssociation.getConstraintProperties().getTargetEntityKey() + " = " + rootCategoryEntityRecordId;
 
 				List<Map<BaseAbstractAttributeInterface, Object>> innerList = new ArrayList<Map<BaseAbstractAttributeInterface, Object>>();
 				categoryDataValueMap.put(categoryAssociation, innerList);
 
 				List<Long> recordIdList = getResultIDList(selectRecordIdQuery, RECORD_ID);
 
 				for (Long recId : recordIdList)
 				{
 					Map<BaseAbstractAttributeInterface, Object> innerMap = new HashMap<BaseAbstractAttributeInterface, Object>();
 					innerList.add(innerMap);
 
 					retrieveRecords(targetCategoryEntity, innerMap, recId);
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param entity
 	 * @param dataValue
 	 * @return
 	 */
 	private Map<CategoryEntityInterface, Map> initialiseEntityValueMap(Map<BaseAbstractAttributeInterface, ?> dataValue)
 	{
 		Map<CategoryEntityInterface, Map> categoryEntityMap = new HashMap<CategoryEntityInterface, Map>();
 
 		for (BaseAbstractAttributeInterface baseAbstractAttributeInterface : dataValue.keySet())
 		{
 			CategoryEntityInterface attributeCategoryEntity = null;
 			if (baseAbstractAttributeInterface instanceof CategoryAttributeInterface)
 			{
 				attributeCategoryEntity = ((CategoryAttributeInterface) baseAbstractAttributeInterface).getCategoryEntity();
 			}
 			else
 			{
 				attributeCategoryEntity = ((CategoryAssociationInterface) baseAbstractAttributeInterface).getCategoryEntity();
 			}
 			Object value = dataValue.get(baseAbstractAttributeInterface);
 
 			Map<BaseAbstractAttributeInterface, Object> entityDataValueMap = (Map) categoryEntityMap.get(attributeCategoryEntity);
 			if (entityDataValueMap == null)
 			{
 				entityDataValueMap = new HashMap<BaseAbstractAttributeInterface, Object>();
 				categoryEntityMap.put(attributeCategoryEntity, entityDataValueMap);
 			}
 			entityDataValueMap.put(baseAbstractAttributeInterface, value);
 		}
 
 		return categoryEntityMap;
 	}
 
 	/**
 	 * @param categoryEntity
 	 * @return
 	 */
 	private List<CategoryEntityInterface> getParentEntityList(CategoryEntityInterface categoryEntity)
 	{
 		//As here the parent category entity whose table is not created  is blocked so its not added in list
 		List<CategoryEntityInterface> categoryEntityList = new ArrayList<CategoryEntityInterface>();
 		categoryEntityList.add(categoryEntity);
 		//bug # 10265 -modified as per code review comment.
 		//reviewer name - Rajesh Patil
 		CategoryEntityInterface objCategoryEntity = categoryEntity.getParentCategoryEntity();
 		while (objCategoryEntity != null && ((CategoryEntity) objCategoryEntity).isCreateTable())
 		{
 			categoryEntityList.add(0, objCategoryEntity);
 			objCategoryEntity = objCategoryEntity.getParentCategoryEntity();
 
 		}
 		return categoryEntityList;
 	}
 
 	/**
 	 * This method populates record map for entity which belongs to root category entity.
 	 * @param rootCategoryEntity
 	 * @param rootEntityRecordsMap
 	 * @param attributeValueMap
 	 */
 	private void populateRootEntityRecordMap(CategoryEntityInterface rootCategoryEntity,
 			Map<AbstractAttributeInterface, Object> rootEntityRecordsMap, Map<BaseAbstractAttributeInterface, Object> attributeValueMap)
 	{
 		Set<Entry<BaseAbstractAttributeInterface, Object>> categoryDataMapEntries = attributeValueMap.entrySet();
 
 		AbstractAttributeInterface abstractAttribute = null;
 		Object entityValue = null;
 		CategoryAttributeInterface categoryAttribute;
 
 		BaseAbstractAttributeInterface baseAbstractAttribute;
 
 		Object categoryValue;
 		for (Entry<BaseAbstractAttributeInterface, Object> entry : categoryDataMapEntries)
 		{
 			baseAbstractAttribute = entry.getKey();
 			categoryValue = entry.getValue();
 			if (baseAbstractAttribute instanceof CategoryAttributeInterface)
 			{
 				categoryAttribute = (CategoryAttributeInterface) baseAbstractAttribute;
 				abstractAttribute = categoryAttribute.getAbstractAttribute();
 				entityValue = categoryValue;
 
 				//add root cat entity and its parent category entity's attribute
 				for (CategoryAttributeInterface rootcategoryAttribute : rootCategoryEntity.getAllCategoryAttributes())
 				{
 					if ((categoryAttribute == rootcategoryAttribute)
 							&& (categoryAttribute.getIsRelatedAttribute() == null || categoryAttribute.getIsRelatedAttribute() == false))
 					{
 						rootEntityRecordsMap.put(abstractAttribute, entityValue);
 					}
 
 				}
 			}
 		}
 
 		for (CategoryAssociationInterface categoryAssociation : rootCategoryEntity.getCategoryAssociationCollection())
 		{
 			//add all root catentity's association
 			for (AssociationInterface association : categoryAssociation.getCategoryEntity().getEntity().getAssociationCollection())
 			{
 				if (!rootEntityRecordsMap.containsKey(association))
 				{
 					rootEntityRecordsMap.put(association, new ArrayList());
 				}
 			}
 			//also add any association which are related to parent entity's association--association between rootcategoryentity's parententity and another class whose category association is created
 			EntityInterface entity = categoryAssociation.getCategoryEntity().getEntity();
 			while (entity.getParentEntity() != null)
 			{
 				for (AssociationInterface association : entity.getParentEntity().getAssociationCollection())
 				{
 					if (association.getTargetEntity() == categoryAssociation.getTargetCategoryEntity().getEntity())
 					{
 						if (!rootEntityRecordsMap.containsKey(association))
 						{
 							rootEntityRecordsMap.put(association, new ArrayList());
 						}
 					}
 				}
 				entity = entity.getParentEntity();
 			}
 		}
 	}
 
 	/**
 	 * @param categoryEntity
 	 * @param recordId
 	 * @param jdbcDAO
 	 * @throws SQLException
 	 */
 	private void clearCategoryEntityData(CategoryEntityInterface categoryEntity, Long recordId, Stack<String> categoryEntityReverseQueryStack,
 			Long userId, JDBCDAO jdbcDAO) throws SQLException
 	{
 		CategoryEntityInterface catEntity = categoryEntity;
 
 		for (CategoryAssociationInterface categoryAssociation : catEntity.getCategoryAssociationCollection())
 		{
 			if (categoryAssociation.getTargetCategoryEntity().getChildCategories().size() != 0)
 			{
 				String selectQuery = "SELECT IDENTIFIER FROM " + categoryAssociation.getTargetCategoryEntity().getTableProperties().getName()
 						+ " WHERE " + categoryAssociation.getConstraintProperties().getTargetEntityKey() + " = " + recordId;
 
 				List<Long> recordIdList = getResultIDList(selectQuery, "IDENTIFIER");
 				for (Long recId : recordIdList)
 				{
 					clearCategoryEntityData(categoryAssociation.getTargetCategoryEntity(), recId, categoryEntityReverseQueryStack, userId, jdbcDAO);
 					String deleteQuery = "DELETE FROM " + categoryAssociation.getTargetCategoryEntity().getTableProperties().getName()
 							+ " WHERE IDENTIFIER = " + recId;
 					executeUpdateQuery(deleteQuery, userId, jdbcDAO);
 					categoryEntityReverseQueryStack.push(deleteQuery);
 				}
 			}
 			else
 			{
 				String deleteQuery = "DELETE FROM " + categoryAssociation.getTargetCategoryEntity().getTableProperties().getName() + " WHERE "
 						+ categoryAssociation.getConstraintProperties().getTargetEntityKey() + " = " + recordId;
 				executeUpdateQuery(deleteQuery, userId, jdbcDAO);
 				categoryEntityReverseQueryStack.push(deleteQuery);
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
 	 * @param rootCategoryEntityInterface
 	 * @param recordId
 	 * @return the record id of the hook entity
 	 * @throws SQLException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private Long getRootCategoryEntityRecordId(CategoryEntityInterface rootCategoryEntityInterface, Long recordId) throws SQLException,
 			DynamicExtensionsSystemException
 	{
 		StringBuffer query = new StringBuffer();
 		query.append(SELECT_KEYWORD + WHITESPACE + RECORD_ID + WHITESPACE + FROM_KEYWORD + WHITESPACE
 				+ rootCategoryEntityInterface.getTableProperties().getName() + WHITESPACE + WHERE_KEYWORD + WHITESPACE + IDENTIFIER + EQUAL
 				+ recordId);
 
 		Long rootEntityRecordId = null;
 
 		List<Long> resultList = getResultIDList(query.toString(), RECORD_ID);
 		if (resultList.size() > 0)
 		{
 			rootEntityRecordId = (Long) resultList.get(0);
 		}
 		return rootEntityRecordId;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.CategoryManagerInterface#isPermissibleValuesSubsetValid(edu.common.dynamicextensions.domaininterface.AttributeInterface, java.util.List)
 	 */
 	public boolean isPermissibleValuesSubsetValid(UserDefinedDEInterface userDefinedDE,
 			Map<String, Collection<SemanticPropertyInterface>> desiredPermissibleValues)
 	{
 		boolean arePermissibleValuesCorrect = true;
 
 		if (userDefinedDE != null)
 		{
 			List<Object> attributePermissibleValues = new ArrayList<Object>();
 
 			for (PermissibleValueInterface pv : userDefinedDE.getPermissibleValueCollection())
 			{
 				attributePermissibleValues.add(pv.getValueAsObject());
 			}
 
 			boolean allDoubleValues = false;
 			Iterator<PermissibleValueInterface> itrPV = userDefinedDE.getPermissibleValueCollection().iterator();
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
 			Iterator<PermissibleValueInterface> itrPVFloat = userDefinedDE.getPermissibleValueCollection().iterator();
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
 			Iterator<PermissibleValueInterface> itrPVInteger = userDefinedDE.getPermissibleValueCollection().iterator();
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
 			Iterator<PermissibleValueInterface> itrPVShort = userDefinedDE.getPermissibleValueCollection().iterator();
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
 			Iterator<PermissibleValueInterface> itrPVLong = userDefinedDE.getPermissibleValueCollection().iterator();
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
 
 			if (allFloatValues && desiredPermissibleValues != null)
 			{
 				Set<String> permissibleValueString = desiredPermissibleValues.keySet();
 				for (String s : permissibleValueString)
 				{
 					if (!attributePermissibleValues.contains(Float.parseFloat(s)))
 					{
 						arePermissibleValuesCorrect = false;
 					}
 				}
 			}
 			else if (allDoubleValues && desiredPermissibleValues != null)
 			{
 				Set<String> permissibleValueString = desiredPermissibleValues.keySet();
 				for (String s : permissibleValueString)
 				{
 					if (!attributePermissibleValues.contains(Double.parseDouble(s)))
 					{
 						arePermissibleValuesCorrect = false;
 					}
 				}
 			}
 			else if (allIntegerValues && desiredPermissibleValues != null)
 			{
 				Set<String> permissibleValueString = desiredPermissibleValues.keySet();
 				for (String s : permissibleValueString)
 				{
 					if (!attributePermissibleValues.contains(Integer.parseInt(s)))
 					{
 						arePermissibleValuesCorrect = false;
 					}
 				}
 			}
 			else if (allShortValues && desiredPermissibleValues != null)
 			{
 				Set<String> permissibleValueString = desiredPermissibleValues.keySet();
 				for (String s : permissibleValueString)
 				{
 					if (!attributePermissibleValues.contains(Short.parseShort(s)))
 					{
 						arePermissibleValuesCorrect = false;
 					}
 				}
 			}
 			else if (allLongValues && desiredPermissibleValues != null)
 			{
 				Set<String> permissibleValueString = desiredPermissibleValues.keySet();
 				for (String s : permissibleValueString)
 				{
 					if (!attributePermissibleValues.contains(Long.parseLong(s)))
 					{
 						arePermissibleValuesCorrect = false;
 					}
 				}
 			}
 			else if (desiredPermissibleValues != null)
 			{
 				Set<String> permissibleValueString = desiredPermissibleValues.keySet();
 				for (String s : permissibleValueString)
 				{
 					if (!attributePermissibleValues.contains(s))
 					{
 						arePermissibleValuesCorrect = false;
 					}
 				}
 			}
 		}
 
 		return arePermissibleValuesCorrect;
 	}
 
 	/**
 	 * This method executes a SQL query and returns a list of identifier, record identifier
 	 * depending upon column name passed.
 	 * @param query
 	 * @param columnName
 	 * @return a list of identifier, record identifier depending upon column name passed.
 	 * @throws SQLException
 	 */
 	private List<Long> getResultIDList(String query, String columnName) throws SQLException
 	{
 		List<Long> recordIdList = new ArrayList<Long>();
 		Connection conn = null;
 		Statement statement = null;
 		ResultSet resultSet = null;
 		Object value = null;
 
 		try
 		{
 			conn = DBUtil.getConnection();
 			statement = conn.createStatement();
 			resultSet = statement.executeQuery(query);
 
 			while (resultSet.next())
 			{
 				value = resultSet.getObject(columnName);
 				recordIdList.add(new Long(value.toString()));
 			}
 		}
 		catch (Exception e)
 		{
 			throw new SQLException(e.getMessage());
 		}
 		finally
 		{
 			try
 			{
 				resultSet.close();
 				statement.close();
 			}
 			catch (SQLException e)
 			{
 				e.printStackTrace();
 				throw new SQLException(e.getMessage());
 			}
 		}
 
 		return recordIdList;
 	}
 
 	/**
 	 * This method executes a SQL query.
 	 * @param query
 	 * @throws SQLException
 	 */
 	private void executeUpdateQuery(String query, Long userId, JDBCDAO jdbcDAO) throws SQLException
 	{
 		Connection conn = null;
 		Statement statement = null;
 
 		try
 		{
 			jdbcDAO.insert(DomainObjectFactory.getInstance().createDESQLAudit(userId, query), null, false, false);;
 
 			conn = DBUtil.getConnection();
 			statement = conn.createStatement();
 			statement.executeUpdate(query);
 		}
 		catch (Exception e)
 		{
 			throw new SQLException(e.getMessage());
 		}
 		finally
 		{
 			try
 			{
 				statement.close();
 			}
 			catch (SQLException e)
 			{
 				e.printStackTrace();
 				throw new SQLException(e.getMessage());
 			}
 		}
 	}
 
 	/**
 	 *
 	 * @param dataValueMap
 	 * @return
 	 */
 	private Map<AbstractAttributeInterface, Object> createAttributeMap(Map<BaseAbstractAttributeInterface, Object> dataValueMap)
 	{
 		Map<AbstractAttributeInterface, Object> attributeMap = new HashMap<AbstractAttributeInterface, Object>();
 
 		Iterator<BaseAbstractAttributeInterface> attributeIterator = dataValueMap.keySet().iterator();
 
 		while (attributeIterator.hasNext())
 		{
 			Object obj = attributeIterator.next();
 			if (obj instanceof CategoryAttributeInterface)
 			{
 				attributeMap.put(((CategoryAttributeInterface) obj).getAbstractAttribute(), dataValueMap.get(obj));
 			}
 		}
 
 		return attributeMap;
 	}
 
 	//	private void rollbackQueries(Stack<String> reverseQueryStack) throws DynamicExtensionsSystemException
 	//	{
 	//		if (reverseQueryStack != null && !reverseQueryStack.isEmpty())
 	//		{
 	//			try
 	//			{
 	//				while (!reverseQueryStack.empty())
 	//				{
 	//					String query = (String) reverseQueryStack.pop();
 	//					executeUpdateQuery(query);
 	//				}
 	//			}
 	//			catch (SQLException e)
 	//			{
 	//				throw new DynamicExtensionsSystemException(e.getMessage());
 	//			}
 	//		}
 	//	}
 
 }
