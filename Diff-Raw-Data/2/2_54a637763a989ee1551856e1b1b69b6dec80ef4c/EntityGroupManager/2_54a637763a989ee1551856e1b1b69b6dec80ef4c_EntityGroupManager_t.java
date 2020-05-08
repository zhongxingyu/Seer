 
 package edu.common.dynamicextensions.entitymanager;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 
 import edu.common.dynamicextensions.bizlogic.BizLogicFactory;
 import edu.common.dynamicextensions.domain.DomainObjectFactory;
 import edu.common.dynamicextensions.domain.Entity;
 import edu.common.dynamicextensions.domain.EntityGroup;
 import edu.common.dynamicextensions.domaininterface.AbstractMetadataInterface;
 import edu.common.dynamicextensions.domaininterface.DynamicExtensionBaseDomainObjectInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.TaggedValueInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.util.AssociationTreeObject;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.wustl.common.beans.NameValueBean;
 import edu.wustl.common.bizlogic.DefaultBizLogic;
 import edu.wustl.common.exception.BizLogicException;
 import edu.wustl.common.util.logger.Logger;
 import edu.wustl.dao.JDBCDAO;
 import edu.wustl.dao.exception.DAOException;
 import edu.wustl.dao.query.generator.DBTypes;
 import edu.wustl.dao.util.NamedQueryParam;
 
 /**
  *
  * @author rajesh_patil
  *
  */
 public class EntityGroupManager extends AbstractMetadataManager
 		implements
 			EntityGroupManagerInterface,
 			EntityGroupManagerConstantsInterface
 {
 
 	private static EntityGroupManagerInterface entGrpManager = null;
 
 	/**
 	 * Static instance of the queryBuilder.
 	 */
 	private static DynamicExtensionBaseQueryBuilder queryBuilder = null;
 
 	/**
 	 * Empty Constructor.
 	 */
 	protected EntityGroupManager()
 	{
		super();
 	}
 
 	/**
 	 * Returns the instance of the Entity Group Manager.
 	 * @return entityManager singleton instance of the Entity Manager.
 	 */
 	public static synchronized EntityGroupManagerInterface getInstance()
 	{
 		if (entGrpManager == null)
 		{
 			entGrpManager = new EntityGroupManager();
 			DynamicExtensionsUtility.initialiseApplicationVariables();
 			queryBuilder = QueryBuilderFactory.getQueryBuilder();
 		}
 
 		return entGrpManager;
 	}
 
 	/**
 	 *
 	 */
 	protected DynamicExtensionBaseQueryBuilder getQueryBuilderInstance()
 	{
 		return queryBuilder;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityGroupManagerInterface#persistEntityGroup(edu.common.dynamicextensions.domaininterface.EntityGroupInterface)
 	 */
 	public EntityGroupInterface persistEntityGroup(EntityGroupInterface group)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		EntityGroupInterface entityGroup = (EntityGroupInterface) persistDynamicExtensionObject(group);
 
 		// Update the dynamic extension cache for all containers within entity group.
 		DynamicExtensionsUtility.updateDynamicExtensionsCache(entityGroup.getId());
 
 		return entityGroup;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityGroupManagerInterface#persistEntityGroupMetadata(edu.common.dynamicextensions.domaininterface.EntityGroupInterface)
 	 */
 	public EntityGroupInterface persistEntityGroupMetadata(EntityGroupInterface entityGroup)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		addTaggedValue(entityGroup);
 		EntityGroupInterface entGroup = (EntityGroupInterface) persistDynamicExtensionObjectMetdata(entityGroup);
 
 		// Update the dynamic extension cache for all containers within entity group.
 		DynamicExtensionsUtility.updateDynamicExtensionsCache(entGroup.getId());
 
 		return entGroup;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.AbstractMetadataManager#preProcess(edu.common.dynamicextensions.domaininterface.DynamicExtensionBaseDomainObjectInterface, java.util.List, java.util.List)
 	 */
 	protected void preProcess(DynamicExtensionBaseDomainObjectInterface dyExtBsDmnObj,
 			List<String> revQueries, List<String> queries) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		EntityGroupInterface entityGroup = (EntityGroupInterface) dyExtBsDmnObj;
 		getDynamicQueryList(addTaggedValue(entityGroup), revQueries, queries);
 	}
 
 	/**
 	 * This method adds caB2BEntityGroup tagged value to the entity group
 	 * @param entityGroup
 	 * @return
 	 */
 	private EntityGroupInterface addTaggedValue(EntityGroupInterface entityGroup)
 	{
 		addTaggedValue(entityGroup, CAB2B_ENTITY_GROUP, CAB2B_ENTITY_GROUP);
 		addTaggedValue(entityGroup, PACKAGE_NAME, entityGroup.getName());
 		addTaggedValue(entityGroup, METADATA_ENTITY_GROUP, METADATA_ENTITY_GROUP);
 
 		return entityGroup;
 	}
 
 	/**
 	 * This method adds caB2BEntityGroup tagged value to the entity group.
 	 * @param entityGroup
 	 * @param key
 	 * @param value
 	 * @return
 	 */
 	private EntityGroupInterface addTaggedValue(EntityGroupInterface entityGroup, String key,
 			String value)
 	{
 		Collection<TaggedValueInterface> taggedValues = entityGroup.getTaggedValueCollection();
 		boolean isTgdValAdded = false;
 
 		for (TaggedValueInterface taggedValue : taggedValues)
 		{
 			if (taggedValue.getKey().equalsIgnoreCase(key))
 			{
 				isTgdValAdded = true;
 			}
 		}
 
 		if (!isTgdValAdded)
 		{
 			TaggedValueInterface taggedValue = DomainObjectFactory.getInstance()
 					.createTaggedValue();
 			taggedValue.setKey(key);
 			taggedValue.setValue(value);
 			entityGroup.addTaggedValue(taggedValue);
 		}
 
 		return entityGroup;
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
 	 * @see edu.common.dynamicextensions.entitymanager.AbstractMetadataManager#logFatalError(java.lang.Exception, edu.common.dynamicextensions.domaininterface.AbstractMetadataInterface)
 	 */
 	protected void logFatalError(Exception exception, AbstractMetadataInterface abstrMetadata)
 	{
 		String name = "";
 		if (abstrMetadata != null)
 		{
 			EntityGroupInterface entityGroup = (EntityGroupInterface) abstrMetadata;
 			name = entityGroup.getName();
 		}
 
 		Logger.out
 				.error("***Fatal Error.. Inconsistent data table and metadata information for the entity -"
 						+ name + "***");
 		Logger.out.error("The cause of the exception is - " + exception.getMessage());
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityGroupManagerInterface#getEntityGroupByShortName(java.lang.String)
 	 */
 	public EntityGroupInterface getEntityGroupByShortName(String shortName)
 			throws DynamicExtensionsSystemException
 	{
 		EntityGroupInterface entityGroup = null;
 		Collection entityGroups = new HashSet();
 
 		if (shortName == null || shortName.equals(""))
 		{
 			return entityGroup;
 		}
 
 		// Get the instance of the default biz logic class which has the method 
 		// that returns the particular object depending on the value of a particular 
 		// column of the associated table.
 		DefaultBizLogic defBizLogic = BizLogicFactory.getDefaultBizLogic();
 
 		try
 		{
 			// Call retrieve method to get the entity group object based on the given value of short name.
 			entityGroups = defBizLogic
 					.retrieve(EntityGroup.class.getName(), "shortName", shortName);
 			if (entityGroups != null && !entityGroups.isEmpty())
 			{
 				entityGroup = (EntityGroupInterface) entityGroups.iterator().next();
 			}
 		}
 		catch (BizLogicException e)
 		{
 			throw new DynamicExtensionsSystemException(e.getMessage(), e);
 		}
 
 		return entityGroup;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityGroupManagerInterface#getEntityGroupByName(java.lang.String)
 	 */
 	public EntityGroupInterface getEntityGroupByName(String name)
 			throws DynamicExtensionsSystemException
 	{
 		EntityGroupInterface entityGroup = (EntityGroupInterface) getObjectByName(EntityGroup.class
 				.getName(), name);
 
 		return entityGroup;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityGroupManagerInterface#getMainContainer(java.lang.Long)
 	 */
 	public Collection<NameValueBean> getMainContainer(Long identifier)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, identifier));
 
 		return executeHQL("getMainContainers", substParams);
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityGroupManagerInterface#getAllEntityGroupBeans()
 	 */
 	public Collection<NameValueBean> getAllEntityGroupBeans()
 			throws DynamicExtensionsSystemException
 	{
 		Collection<NameValueBean> entGroupBeans = new ArrayList<NameValueBean>();
 		Object[] objectArray;
 
 		Collection groupBeans = executeHQL("getAllGroupBeans", new HashMap());
 		Iterator grpBeansIter = groupBeans.iterator();
 		while (grpBeansIter.hasNext())
 		{
 			objectArray = (Object[]) grpBeansIter.next();
 
 			NameValueBean nameValueBean = new NameValueBean();
 			nameValueBean.setName(objectArray[0]);
 			nameValueBean.setValue(objectArray[1]);
 
 			entGroupBeans.add(nameValueBean);
 		}
 
 		return entGroupBeans;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityGroupManagerInterface#getAssociationTree()
 	 */
 	public Collection<AssociationTreeObject> getAssociationTree()
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Collection<AssociationTreeObject> assTreeObjects = new HashSet<AssociationTreeObject>();
 
 		AssociationTreeObject assoTreeObject;
 
 		Collection<NameValueBean> groupBeans = getAllEntityGroupBeans();
 		Iterator<NameValueBean> grpBeansIter = groupBeans.iterator();
 		while (grpBeansIter.hasNext())
 		{
 			assoTreeObject = processGroupBean(grpBeansIter.next());
 			assTreeObjects.add(assoTreeObject);
 		}
 
 		return assTreeObjects;
 	}
 
 	/**
 	 *
 	 * @param objectArray
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private AssociationTreeObject processGroupBean(NameValueBean groupBean)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		AssociationTreeObject assoTreeObject = new AssociationTreeObject(Long.valueOf(groupBean
 				.getValue()), groupBean.getName());
 
 		Map<String, NamedQueryParam> substParams = new HashMap<String, NamedQueryParam>();
 		substParams.put("0", new NamedQueryParam(DBTypes.LONG, assoTreeObject.getId()));
 
 		Object[] contBeans;
 		AssociationTreeObject contAssoTreeObj;
 
 		Collection containerBeans = executeHQL("getAllContainersBeansByEntityGroupId", substParams);
 		Iterator contBeansIter = containerBeans.iterator();
 		while (contBeansIter.hasNext())
 		{
 			contBeans = (Object[]) contBeansIter.next();
 			contAssoTreeObj = new AssociationTreeObject((Long) contBeans[0], (String) contBeans[1]);
 
 			assoTreeObject.addAssociationTreeObject(contAssoTreeObj);
 		}
 
 		return assoTreeObject;
 	}
 
 	/**
 	 * @param entity
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private void checkParentChangeAllowed(Entity entity) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		String tableName = entity.getTableProperties().getName();
 		if (queryBuilder.isDataPresent(tableName))
 		{
 			throw new DynamicExtensionsApplicationException(
 					"Can not change the data type of the attribute", null, DYEXTN_A_010);
 		}
 	}
 
 	/**
 	 * validateEntityGroup.
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public boolean validateEntityGroup(EntityGroupInterface entityGroup)
 	throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		try
 		{
 			EntityGroup dbaseCopy = (EntityGroup)DynamicExtensionsUtility.getCleanObject(EntityGroup.class.getCanonicalName(), entityGroup.getId());
 			Collection<EntityInterface> entities = entityGroup.getEntityCollection();
 			for (EntityInterface entObject : entities)
 			{
 				Entity entity = (Entity) entObject;
 				if (entity.getId() == null)
 				{
 					DynamicExtensionsUtility.validateEntity(entity);
 				}
 				else
 				{
 					EntityInterface dbaseCpy = getEntityFromGroup(dbaseCopy, entity.getId());
 					if (EntityManagerUtil.isParentChanged((Entity) entity, (Entity) dbaseCpy))
 					{
 						checkParentChangeAllowed(entity);
 					}
 				}
 			}
 		}
 		catch(DAOException exception)
 		{
 			throw new DynamicExtensionsSystemException(exception.getMessage(),exception);
 		}
 		return true;
 	}
 
 	/**
 	 * getEntityFromGroup.
 	 * @param entityGroup
 	 * @param entityId
 	 * @return
 	 */
 	private EntityInterface getEntityFromGroup(EntityGroupInterface entityGroup, Long entityId)
 	{
 		Collection<EntityInterface> entities = entityGroup.getEntityCollection();
 		for (EntityInterface entity : entities)
 		{
 			if (entity.getId() != null && entity.getId().equals(entityId))
 			{
 				return entity;
 			}
 		}
 
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityGroupManagerInterface#checkForDuplicateEntityGroupName(edu.common.dynamicextensions.domaininterface.EntityGroupInterface)
 	 */
 	public void checkForDuplicateEntityGroupName(EntityGroupInterface entityGroup)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		JDBCDAO jdbcDao = null;
 		try
 		{
 			jdbcDao = DynamicExtensionsUtility.getJDBCDAO();
 			String query = "select count(*) from dyextn_abstract_metadata d , dyextn_entity_group e where d.identifier = e.identifier and d.name = '"
 					+ entityGroup.getName() + "'";
 			List result = jdbcDao.executeQuery(query);
 
 			if (result != null && !result.isEmpty())
 			{
 				List count = (List) result.get(0);
 				if (count != null && !count.isEmpty())
 				{
 					int noOfOccurances = Integer.valueOf((String) count.get(0)).intValue();
 					if (noOfOccurances > 0)
 					{
 						throw new DynamicExtensionsApplicationException(
 								"Duplicate Entity Group name", null, DYEXTN_A_015);
 					}
 				}
 			}
 		}
 		catch (DAOException e)
 		{
 			throw new DynamicExtensionsSystemException("Error while checking for duplicate group",
 					e);
 		}		
 		finally
 		{
 			try
 			{
 				DynamicExtensionsUtility.closeJDBCDAO(jdbcDao);
 			}
 			catch (DAOException e)
 			{
 				throw new DynamicExtensionsSystemException("DAOException", e);
 			}
 		}
 	}
 
 }
