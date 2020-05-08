 
 package edu.common.dynamicextensions.entitymanager;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 
 import net.sf.hibernate.HibernateException;
 import net.sf.hibernate.Query;
 import net.sf.hibernate.Session;
 import edu.common.dynamicextensions.bizlogic.BizLogicFactory;
 import edu.common.dynamicextensions.domain.AbstractAttribute;
 import edu.common.dynamicextensions.domain.Association;
 import edu.common.dynamicextensions.domain.Attribute;
 import edu.common.dynamicextensions.domain.AttributeRecord;
 import edu.common.dynamicextensions.domain.CollectionAttributeRecordValue;
 import edu.common.dynamicextensions.domain.DateAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.DomainObjectFactory;
 import edu.common.dynamicextensions.domain.DynamicExtensionBaseDomainObject;
 import edu.common.dynamicextensions.domain.Entity;
 import edu.common.dynamicextensions.domain.EntityGroup;
 import edu.common.dynamicextensions.domain.FileAttributeRecordValue;
 import edu.common.dynamicextensions.domain.FileAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.ObjectAttributeRecordValue;
 import edu.common.dynamicextensions.domain.ObjectAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.TaggedValue;
 import edu.common.dynamicextensions.domain.databaseproperties.ColumnProperties;
 import edu.common.dynamicextensions.domain.databaseproperties.ConstraintProperties;
 import edu.common.dynamicextensions.domain.databaseproperties.TableProperties;
 import edu.common.dynamicextensions.domain.userinterface.Container;
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationDisplayAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.DynamicExtensionBaseDomainObjectInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.ObjectAttributeRecordValueInterface;
 import edu.common.dynamicextensions.domaininterface.RoleInterface;
 import edu.common.dynamicextensions.domaininterface.TaggedValueInterface;
 import edu.common.dynamicextensions.domaininterface.databaseproperties.ColumnPropertiesInterface;
 import edu.common.dynamicextensions.domaininterface.databaseproperties.ConstraintPropertiesInterface;
 import edu.common.dynamicextensions.domaininterface.databaseproperties.TablePropertiesInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.AssociationControlInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainmentAssociationControlInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ControlInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.util.AssociationTreeObject;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.common.dynamicextensions.util.global.Constants;
 import edu.common.dynamicextensions.util.global.Constants.AssociationDirection;
 import edu.common.dynamicextensions.util.global.Constants.AssociationType;
 import edu.common.dynamicextensions.util.global.Constants.Cardinality;
 import edu.wustl.common.beans.NameValueBean;
 import edu.wustl.common.beans.SessionDataBean;
 import edu.wustl.common.bizlogic.AbstractBizLogic;
 import edu.wustl.common.bizlogic.DefaultBizLogic;
 import edu.wustl.common.dao.AbstractDAO;
 import edu.wustl.common.dao.DAOFactory;
 import edu.wustl.common.dao.HibernateDAO;
 import edu.wustl.common.dao.JDBCDAO;
 import edu.wustl.common.security.exceptions.UserNotAuthorizedException;
 import edu.wustl.common.util.dbManager.DAOException;
 import edu.wustl.common.util.dbManager.DBUtil;
 import edu.wustl.common.util.logger.Logger;
 
 /**
  * This is a singleton class that manages operations related to dynamic entity creation,attributes creation,
  * adding data into those entities and retrieving data out of them.
  *
  *  In order to mock  EntityManager class we need to create a a mock class which extends EntityManager class.
  * i.e.We create a class named as EntityManagerMock which will extend from EntityManager.EntityManagerMock
  * class will override the unimplemented methods from EntityManager.Entity manager is having a method
  * as setInstance.The application which is using this mock will place the instance of mock class in
  * EntityManager class using setInstancxe method on startup.
  *
  */
 /**
  * @author Geetika Bangard
  * @author Vishvesh Mulay
  * @author Rahul Ner
  */
 public class EntityManager
 		implements
 			EntityManagerInterface,
 			EntityManagerConstantsInterface,
 			EntityManagerExceptionConstantsInterface,
 			DynamicExtensionsQueryBuilderConstantsInterface
 {
 
 	/**
 	 * Static instance of the entity manager.
 	 */
 	private static EntityManagerInterface entityManagerInterface = null;
 
 	/**
 	 * Instance of database specific query builder.
 	 */
 	private static DynamicExtensionBaseQueryBuilder queryBuilder = null;
 
 	/**
 	 * Instance of entity manager util class
 	 */
 	EntityManagerUtil entityManagerUtil = new EntityManagerUtil();
 
 	/**
 	 * Empty Constructor.
 	 */
 	protected EntityManager()
 	{
 	}
 
 	/**
 	 * Returns the instance of the Entity Manager.
 	 * @return entityManager singleton instance of the Entity Manager.
 	 */
 	public static synchronized EntityManagerInterface getInstance()
 	{
 		if (entityManagerInterface == null)
 		{
 			entityManagerInterface = new EntityManager();
 			DynamicExtensionsUtility.initialiseApplicationVariables();
 			queryBuilder = QueryBuilderFactory.getQueryBuilder();
 		}
 
 		return entityManagerInterface;
 	}
 
 	/**
 	 * Mock entity manager can be placed in the entity manager using this method.
 	 * @param entityManager
 	 */
 	public static void setInstance(EntityManagerInterface entityManagerInterface)
 	{
 		EntityManager.entityManagerInterface = entityManagerInterface;
 
 	}
 
 	/**
 	 * This method is used to log the messages in a uniform manner. The method takes the string method name and
 	 * string message. Using these parameters the method formats the message and logs it.
 	 * @param methodName Name of the method for which the message needs to be logged.
 	 * @param message The message that needs to be logged.
 	 */
 	private void logDebug(String methodName, String message)
 	{
 		Logger.out.debug("[EntityManager.]" + methodName + "()--" + message);
 	}
 
 	public EntityInterface persistEntity(EntityInterface entityInterface)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		return persistEntity(entityInterface, true);
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#persistEntity(edu.common.dynamicextensions.domaininterface.EntityInterface)
 	 */
 	public EntityInterface persistEntity(EntityInterface entityInterface, boolean addIdAttribute)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		logDebug("persistEntity", "entering the method");
 		Entity entity = (Entity) entityInterface;
 		boolean isEntitySaved = true;
 		//Depending on the presence of Id field , the method that is to be invoked (insert/update), is decided.
 		if (entity.getId() == null)
 		{
 			isEntitySaved = false;
 		}
 
 		HibernateDAO hibernateDAO = (HibernateDAO) DAOFactory.getInstance().getDAO(
 				Constants.HIBERNATE_DAO);
 		Stack stack = new Stack();
 
 		try
 		{
 
 			hibernateDAO.openSession(null);
 			//Calling the method which actually calls the insert/update method on dao.
 			//Hibernatedao is passed to this method and transaction is handled in the calling method.
 			saveEntityGroup(entityInterface, hibernateDAO);
 			List<EntityInterface> processedEntityList = new ArrayList<EntityInterface>();
 
 			entityInterface = saveOrUpdateEntity(entityInterface, hibernateDAO, stack,
 					isEntitySaved, processedEntityList, addIdAttribute, false,true);
 
 			//Committing the changes done in the hibernate session to the database.
 			hibernateDAO.commit();
 		}
 		catch (Exception e)
 		{
 			//Queries for data table creation and modification are fired in the method saveOrUpdateEntity.
 			//So if there is any exception while storing the metadata ,
 			//we need to roll back the queries that were fired. So calling the following method to do that.
 			rollbackQueries(stack, entity, e, hibernateDAO);
 
 			if (e instanceof DynamicExtensionsApplicationException)
 			{
 				throw (DynamicExtensionsApplicationException) e;
 			}
 			else
 			{
 				throw new DynamicExtensionsSystemException(e.getMessage(), e);
 			}
 
 		}
 		finally
 		{
 			try
 			{
 				postSaveOrUpdateEntity(entityInterface);
 				//In any case , after all the operations , hibernate session needs to be closed. So this call has
 				// been added in the finally clause.
 				hibernateDAO.closeSession();
 			}
 			catch (Exception e)
 			{
 				//Queries for data table creation and modification are fired in the method saveOrUpdateEntity. So if there
 				//is any exception while storing the metadata , we need to roll back the queries that were fired. So
 				//calling the following method to do that.
 				rollbackQueries(stack, entity, e, hibernateDAO);
 			}
 		}
 		logDebug("persistEntity", "exiting the method");
 		return entityInterface;
 	}
 
 	/**
 	 * This method removes the flag of processed from the entity once the entities have been saved.
 	 * @param entityInterface
 	 */
 	private void postSaveOrUpdateEntity(EntityInterface entityInterface)
 	{
 		if (entityInterface == null)
 		{
 			return;
 		}
 		Set<EntityInterface> entitySet = new HashSet<EntityInterface>();
 
 		entitySet.add(entityInterface);
 
 		DynamicExtensionsUtility.getAssociatedEntities(entityInterface, entitySet);
 		EntityInterface tempEntity = entityInterface.getParentEntity();
 		while (tempEntity != null)
 		{
 			entitySet.add(tempEntity);
 			tempEntity = tempEntity.getParentEntity();
 		}
 		for (EntityInterface entity : entitySet)
 		{
 			((Entity) entity).setProcessed(false);
 		}
 
 	}
 
 	/**This method saves the correct unsaved entity group from the list of entity groups present in the entity.
 	 * @param entityInterface
 	 * @param hibernateDAO
 	 * @return
 	 * @throws DAOException
 	 * @throws UserNotAuthorizedException
 	 */
 	private EntityGroupInterface saveEntityGroup(EntityInterface entityInterface,
 			HibernateDAO hibernateDAO) throws DAOException, UserNotAuthorizedException
 	{
 		Set<EntityInterface> processedEntities = new HashSet<EntityInterface>();
 		Set<EntityGroupInterface> processedEntityGroups = new HashSet<EntityGroupInterface>();
 		EntityManagerUtil.getAllEntityGroups(entityInterface, processedEntities,
 				processedEntityGroups);
 		EntityGroupInterface entityGroup = null;
 		for (EntityGroupInterface tempEntityGroup : processedEntityGroups)
 		{
 			if (tempEntityGroup.getId() == null)
 			{
 
 				hibernateDAO.insert(tempEntityGroup, null, false, false);
 			}
 			if (((EntityGroup) tempEntityGroup).isCurrent())
 			{
 				entityGroup = tempEntityGroup;
 			}
 		}
 		return entityGroup;
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#persistEntityMetadata(edu.common.dynamicextensions.domaininterface.EntityInterface)
 	 */
 	public EntityInterface persistEntityMetadata(EntityInterface entityInterface,
 			boolean isDataTablePresent,boolean copyDataTableState) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		if (isDataTablePresent)
 		{
 			((Entity) entityInterface).setDataTableState(DATA_TABLE_STATE_ALREADY_PRESENT);
 		}
 		else
 		{
 			((Entity) entityInterface).setDataTableState(DATA_TABLE_STATE_NOT_CREATED);
 		}
 		Entity entity = (Entity) entityInterface;
 		boolean isEntitySaved = true;
 		//Depending on the presence of Id field , the method that is to be invoked (insert/update), is decided.
 		if (entity.getId() == null)
 		{
 			isEntitySaved = false;
 		}
 
 		HibernateDAO hibernateDAO = (HibernateDAO) DAOFactory.getInstance().getDAO(
 				Constants.HIBERNATE_DAO);
 		Stack stack = new Stack();
 
 		try
 		{
 
 			hibernateDAO.openSession(null);
 			//Calling the method which actually calls the insert/update method on dao.
 			//Hibernatedao is passed to this method and transaction is handled in the calling method.
 			saveEntityGroup(entityInterface, hibernateDAO);
 			List<EntityInterface> processedEntityList = new ArrayList<EntityInterface>();
 
 			entityInterface = saveOrUpdateEntityMetadata(entityInterface, hibernateDAO, stack,
 					isEntitySaved, processedEntityList, copyDataTableState);
 
 			//Committing the changes done in the hibernate session to the database.
 			hibernateDAO.commit();
 		}
 		catch (Exception e)
 		{
 			//Queries for data table creation and modification are fired in the method saveOrUpdateEntity.
 			//So if there is any exception while storing the metadata ,
 			//we need to roll back the queries that were fired. So calling the following method to do that.
 			rollbackQueries(stack, entity, e, hibernateDAO);
 
 			if (e instanceof DynamicExtensionsApplicationException)
 			{
 				throw (DynamicExtensionsApplicationException) e;
 			}
 			else
 			{
 				throw new DynamicExtensionsSystemException(e.getMessage(), e);
 			}
 
 		}
 		finally
 		{
 			try
 			{
 				postSaveOrUpdateEntity(entityInterface);
 				//In any case , after all the operations , hibernate session needs to be closed. So this call has
 				// been added in the finally clause.
 				hibernateDAO.closeSession();
 			}
 			catch (DAOException e)
 			{
 				//Queries for data table creation and modification are fired in the method saveOrUpdateEntity. So if there
 				//is any exception while storing the metadata , we need to roll back the queries that were fired. So
 				//calling the following method to do that.
 				rollbackQueries(stack, entity, e, hibernateDAO);
 			}
 		}
 		logDebug("persistEntity", "exiting the method");
 		return entityInterface;
 	}
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#persistEntityMetadata(edu.common.dynamicextensions.domaininterface.EntityInterface)
 	 */
 	public EntityInterface persistEntityMetadataForAnnotation(EntityInterface entityInterface,
 			boolean isDataTablePresent,boolean copyDataTableState,AssociationInterface association) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		if (isDataTablePresent)
 		{
 			((Entity) entityInterface).setDataTableState(DATA_TABLE_STATE_ALREADY_PRESENT);
 		}
 		else
 		{
 			((Entity) entityInterface).setDataTableState(DATA_TABLE_STATE_NOT_CREATED);
 		}
 		Entity entity = (Entity) entityInterface;
 		boolean isEntitySaved = true;
 		//Depending on the presence of Id field , the method that is to be invoked (insert/update), is decided.
 		if (entity.getId() == null)
 		{
 			isEntitySaved = false;
 		}
 
 		HibernateDAO hibernateDAO = (HibernateDAO) DAOFactory.getInstance().getDAO(
 				Constants.HIBERNATE_DAO);
 		Stack stack = new Stack();
 
 		try
 		{
 
 			hibernateDAO.openSession(null);
 			//Calling the method which actually calls the insert/update method on dao.
 			//Hibernatedao is passed to this method and transaction is handled in the calling method.
 			saveEntityGroup(entityInterface, hibernateDAO);
 			List<EntityInterface> processedEntityList = new ArrayList<EntityInterface>();
 
 			entityInterface = saveOrUpdateEntityMetadataForSingleAnnotation(entityInterface, hibernateDAO, stack,
 					isEntitySaved, processedEntityList, copyDataTableState,association);
 
 			//Committing the changes done in the hibernate session to the database.
 			hibernateDAO.commit();
 		}
 		catch (Exception e)
 		{
 			//Queries for data table creation and modification are fired in the method saveOrUpdateEntity.
 			//So if there is any exception while storing the metadata ,
 			//we need to roll back the queries that were fired. So calling the following method to do that.
 			rollbackQueries(stack, entity, e, hibernateDAO);
 
 			if (e instanceof DynamicExtensionsApplicationException)
 			{
 				throw (DynamicExtensionsApplicationException) e;
 			}
 			else
 			{
 				throw new DynamicExtensionsSystemException(e.getMessage(), e);
 			}
 
 		}
 		finally
 		{
 			try
 			{
 				postSaveOrUpdateEntity(entityInterface);
 				//In any case , after all the operations , hibernate session needs to be closed. So this call has
 				// been added in the finally clause.
 				hibernateDAO.closeSession();
 			}
 			catch (DAOException e)
 			{
 				//Queries for data table creation and modification are fired in the method saveOrUpdateEntity. So if there
 				//is any exception while storing the metadata , we need to roll back the queries that were fired. So
 				//calling the following method to do that.
 				rollbackQueries(stack, entity, e, hibernateDAO);
 			}
 		}
 		logDebug("persistEntity", "exiting the method");
 		return entityInterface;
 	}
 	/**
 	 * This method creates an entity group.The entities in the group are also saved.
 	 * @param entityGroupInterface entity group to be saved.
 	 * @return entityGroupInterface Saved  entity group.
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public EntityGroupInterface persistEntityGroup(EntityGroupInterface entityGroupInterface)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		logDebug("createEntityGroup", "Entering method");
 		EntityGroup entityGroup = (EntityGroup) entityGroupInterface;
 		//Calling the following method to process the entity group before saving.
 		//This includes setting the created date and updated date etc.
 		preSaveProcessEntityGroup(entityGroup);
 		//Following method actually calls the dao's insert or update method.
 		boolean isEntityGroupNew = true;
 		if (entityGroupInterface.getId() != null)
 		{
 			isEntityGroupNew = false;
 		}
 		boolean isOnlyMetadata = false;
 		entityGroup = saveOrUpdateEntityGroup(entityGroupInterface, isEntityGroupNew,
 				isOnlyMetadata,true);
 		logDebug("createEntity", "Exiting method");
 		return entityGroupInterface;
 	}
 	/**
 	 * This method creates an entity group.The entities in the group are also saved.
 	 * @param entityGroupInterface entity group to be saved.
 	 * @return entityGroupInterface Saved  entity group.
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public EntityGroupInterface persistEntityGroupWithAllContainers(EntityGroupInterface entityGroupInterface,Collection<ContainerInterface> containerColl)
 	throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		logDebug("createEntityGroup", "Entering method");
 		EntityGroup entityGroup = (EntityGroup) entityGroupInterface;
 		//Calling the following method to process the entity group before saving.
 		//This includes setting the created date and updated date etc.
 		//preSaveProcessEntityGroup(entityGroup);
 		DynamicExtensionsUtility.validateName(entityGroup.getName());
 		if (entityGroup.getId() != null)
 		{
 			entityGroup.setLastUpdated(new Date());
 		}
 		else
 		{
 			entityGroup.setCreatedDate(new Date());
 			entityGroup.setLastUpdated(entityGroup.getCreatedDate());
 		}
 		//Following method actually calls the dao's insert or update method.
 		boolean isEntityGroupNew = true;
 		if (entityGroupInterface.getId() != null)
 		{
 			isEntityGroupNew = false;
 		}
 		boolean isOnlyMetadata = false;
 		entityGroup = saveOrUpdateEntityGroupAndContainers(entityGroupInterface, isEntityGroupNew,
 				isOnlyMetadata,containerColl,true);
 		logDebug("createEntity", "Exiting method");
 		return entityGroupInterface;
 	}
 
 	/**
 	 * This method persists an entity group and the associated entities without creating the data table
 	 * for the entities.
 	 * @param entityGroupInterface entity group to be saved.
 	 * @return entityGroupInterface Saved  entity group.
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public EntityGroupInterface persistEntityGroupMetadata(EntityGroupInterface entityGroupInterface)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		logDebug("createEntityGroup", "Entering method");
 		EntityGroup entityGroup = (EntityGroup) entityGroupInterface;
 		//Calling the following method to process the entity group before saving.
 		//This includes setting the created date and updated date etc.
 		preSaveProcessEntityGroup(entityGroup);
 		//Following method actually calls the dao's insert or update method.
 		boolean isEntityGroupNew = true;
 		if (entityGroupInterface.getId() != null)
 		{
 			isEntityGroupNew = false;
 		}
 		Collection<EntityInterface> entityCollection = entityGroup.getEntityCollection();
 		if (entityCollection != null && !entityCollection.isEmpty())
 		{
 			for (EntityInterface entityInterface : entityCollection)
 			{
 				if (entityInterface.getId() == null)
 				{
 					((Entity) entityInterface).setDataTableState(DATA_TABLE_STATE_NOT_CREATED);
 				}
 			}
 		}
 		boolean isOnlyMetadata = true;
 		entityGroup = saveOrUpdateEntityGroup(entityGroupInterface, isEntityGroupNew,
 				isOnlyMetadata,true);
 		logDebug("createEntity", "Exiting method");
 		return entityGroupInterface;
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getEntityGroupByShortName(java.lang.String)
 	 */
 	public EntityGroupInterface getEntityGroupByShortName(String entityGroupShortName)
 			throws DynamicExtensionsSystemException
 	{
 		EntityGroupInterface entityGroupInterface = null;
 		Collection entityGroupCollection = new HashSet();
 		if (entityGroupShortName == null || entityGroupShortName.equals(""))
 		{
 			return entityGroupInterface;
 		}
 		//Getting the instance of the default biz logic class which has the method that returns the particular object
 		//depending on the value of a particular column of the associated table.
 		DefaultBizLogic defaultBizLogic = BizLogicFactory.getDefaultBizLogic();
 
 		try
 		{
 			//Calling retrieve method to  get the entity group object based on the given value of short name.
 			//Passed parameters are the class name of the entity group class, the name of the hibernate object member variable
 			// and the value of that member variable.
 			entityGroupCollection = defaultBizLogic.retrieve(EntityGroup.class.getName(),
 					"shortName", entityGroupShortName);
 			if (entityGroupCollection != null && entityGroupCollection.size() > 0)
 			{
 				entityGroupInterface = (EntityGroupInterface) entityGroupCollection.iterator()
 						.next();
 			}
 		}
 		catch (DAOException e)
 		{
 			throw new DynamicExtensionsSystemException(e.getMessage(), e);
 		}
 		return entityGroupInterface;
 
 	}
 
 	/** The actual values of the multi select attribute are not stored in the entity's data table because there can
 	 * be more than one values associated with the particular multiselect attribute. so for this reason, these values
 	 * are stored in a different table. CollectionAttributeRecordValues is the hibernate object that maps to that table.
 	 * This method is used to get the list of all the CollectionAttributeRecordValues object for the given combination
 	 * of the entity, attribute and the particular record of the entity. CollectionAttributeRecordValues object
 	 * holds the values of any "multiselect" attributes or file attributes.
 	 * @param entityId
 	 * @param attributeId
 	 * @param recordId
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private List<String> getCollectionAttributeRecordValues(Long entityId, Long attributeId,
 			Long recordId) throws DynamicExtensionsSystemException
 
 	{
 		List<String> valueList = null;
 		AttributeRecord collectionAttributeRecord = getAttributeRecord(entityId, attributeId,
 				recordId, null);
 		if (collectionAttributeRecord != null)
 		{
 			Collection<CollectionAttributeRecordValue> recordValueCollection = collectionAttributeRecord
 					.getValueCollection();
 
 			valueList = new ArrayList<String>();
 			for (CollectionAttributeRecordValue recordValue : recordValueCollection)
 			{
 				valueList.add(recordValue.getValue());
 			}
 		}
 		return valueList;
 	}
 
 	/** This method is used to get the actual file contents for the file attribute for given record of the
 	 * given entity. Actual file contents are not stored in the entity's data table but are stored in a different
 	 * table. FileAttributeRecordValue is the hibernate object that maps to that table. So the file contents are
 	 * returned in the form of FileAttributeRecordValue object.
 	 * returns file record value
 	 * @param entityId
 	 * @param attributeId
 	 * @param recordId
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private FileAttributeRecordValue getFileAttributeRecordValue(Long entityId, Long attributeId,
 			Long recordId) throws DynamicExtensionsSystemException
 
 	{
 		AttributeRecord record = getAttributeRecord(entityId, attributeId, recordId, null);
 		FileAttributeRecordValue fileAttributeRecordValue = null;
 		if (record != null)
 		{
 			fileAttributeRecordValue = record.getFileRecord();
 		}
 		return fileAttributeRecordValue;
 	}
 
 	private ObjectAttributeRecordValueInterface getObjectAttributeRecordValue(Long entityId,
 			Long attributeId, Long recordId) throws DynamicExtensionsSystemException
 
 	{
 		AttributeRecord record = getAttributeRecord(entityId, attributeId, recordId, null);
 		ObjectAttributeRecordValue objectAttributeRecordValue = null;
 		if (record != null)
 		{
 			objectAttributeRecordValue = record.getObjectRecord();
 		}
 		return objectAttributeRecordValue;
 	}
 
 	/** The actual values of the multi select attribute are not stored in the entity's data table because there can
 	 * be more than one values associated with the particular multiselect attribute. so for this reason, these values
 	 * are stored in a different table. AttributeRecord is the hibernate object that maps to that table.
 	 * So this method is used to get the AttributeRecord for the given combination of entity attribute and the particular
 	 * record of the entity.
 	 * @param entityId
 	 * @param attributeId
 	 * @param recordId
 	 * @return
 	 */
 	private AttributeRecord getAttributeRecord(Long entityId, Long attributeId, Long recordId,
 			HibernateDAO hibernateDao) throws DynamicExtensionsSystemException
 
 	{
 
 		Map substitutionParameterMap = new HashMap();
 		AttributeRecord collectionAttributeRecord = null;
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", entityId));
 		substitutionParameterMap.put("1", new HQLPlaceHolderObject("long", attributeId));
 		substitutionParameterMap.put("2", new HQLPlaceHolderObject("long", recordId));
 		Collection recordCollection = null;
 		if (hibernateDao == null)
 		{
 			//Required HQL is stored in the hbm file. The following method takes the name of the query and
 			// the actual values for the placeholders as the parameters.
 			recordCollection = executeHQL("getCollectionAttributeRecord", substitutionParameterMap);
 		}
 		else
 		{
 			//Required HQL is stored in the hbm file. The following method takes the name of the query and
 			// the actual values for the placeholders as the parameters.
 			recordCollection = executeHQL(hibernateDao, "getCollectionAttributeRecord",
 					substitutionParameterMap);
 		}
 		if (recordCollection != null && !recordCollection.isEmpty())
 		{
 			collectionAttributeRecord = (AttributeRecord) recordCollection.iterator().next();
 		}
 		return collectionAttributeRecord;
 	}
 	/** The actual values of the multi select attribute are not stored in the entity's data table because there can
 	 * be more than one values associated with the particular multiselect attribute. so for this reason, these values
 	 * are stored in a different table. AttributeRecord is the hibernate object that maps to that table.
 	 * So this method is used to get the AttributeRecord for the given combination of entity attribute and the particular
 	 * record of the entity.
 	 * @param entityId
 	 * @param attributeId
 	 * @return
 	 */
 	public Collection<Integer> getAttributeRecordsCount(Long entityId, Long attributeId) throws DynamicExtensionsSystemException
 
 	{
 		Map substitutionParameterMap = new HashMap();
 		AttributeRecord collectionAttributeRecord = null;
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", entityId));
 		substitutionParameterMap.put("1", new HQLPlaceHolderObject("long", attributeId));
 
 		//Required HQL is stored in the hbm file. The following method takes the name of the query and
 		// the actual values for the placeholders as the parameters.
 		Collection recordCollection = executeHQLWithCleanSession("getAttributeRecords",
 				substitutionParameterMap);
 
 		return recordCollection;
 	}
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAssociations(java.lang.Long, java.lang.Long)
 	 */
 	public Collection<AssociationInterface> getAssociations(Long sourceEntityId, Long targetEntityId)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Map substitutionParameterMap = new HashMap();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", sourceEntityId));
 		substitutionParameterMap.put("1", new HQLPlaceHolderObject("long", targetEntityId));
 		//Following method is called to execute the stored HQL , the name of which is given as the first parameter.
 		//The second parameter is the map which contains the actual values that are replaced for the placeholders.
 		Collection associationCollection = executeHQL("getAssociations", substitutionParameterMap);
 		return associationCollection;
 	}
 
 	/**
 	 * This method returns the container interface given the entity identifier.
 	 * @param EntityInterface
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public ContainerInterface getContainerByEntityIdentifier(Long entityIdentifier)
 			throws DynamicExtensionsSystemException
 	{
 		ContainerInterface containerInterface = null;
 		Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", entityIdentifier));
 		Collection containerCollection = executeHQL("getContainerOfEntity",
 				substitutionParameterMap);
 		if (containerCollection != null && containerCollection.size() > 0)
 		{
 			containerInterface = (ContainerInterface) containerCollection.iterator().next();
 		}
 
 		return containerInterface;
 
 	}
 
 	/**
 	 * This method returns the control given the attribute identifier
 	 * @param controlIdentifier
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public ControlInterface getControlByAbstractAttributeIdentifier(Long abstractAttributeIdentifier)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		ControlInterface controlInterface = null;
 		Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("long",
 				abstractAttributeIdentifier));
 		Collection controlCollection = executeHQL("getControlOfAbstractAttribute",
 				substitutionParameterMap);
 		if (controlCollection != null && controlCollection.size() > 0)
 		{
 			controlInterface = (ControlInterface) controlCollection.iterator().next();
 		}
 
 		return controlInterface;
 	}
 
 	/**
 	 * This method returns the EntityInterface given the entity name.
 	 * @param entityGroupShortName
 	 * @return
 	 */
 	public EntityInterface getEntityByName(String entityName)
 			throws DynamicExtensionsSystemException
 	{
 		EntityInterface entityInterface = (EntityInterface) getObjectByName(Entity.class.getName(),
 				entityName);
 		return entityInterface;
 	}
 
 	/**
 	 * This method returns the EntityInterface given the entity name.
 	 * @param entityGroupShortName
 	 * @return
 	 */
 	public EntityGroupInterface getEntityGroupByName(String entityGroupName)
 			throws DynamicExtensionsSystemException
 	{
 		EntityGroupInterface entityGroupInterface = (EntityGroupInterface) getObjectByName(
 				EntityGroup.class.getName(), entityGroupName);
 		return entityGroupInterface;
 	}
 
 	/**
 	 * This method returns the object given the class name and object name.
 	 * @param className class name
 	 * @param objectName objectName
 	 * @return DynamicExtensionBaseDomainObjectInterface Base DE interface
 	 */
 	private DynamicExtensionBaseDomainObjectInterface getObjectByName(String className,
 			String objectName) throws DynamicExtensionsSystemException
 	{
 		DynamicExtensionBaseDomainObjectInterface object = null;
 		if (objectName == null || objectName.equals(""))
 		{
 			return object;
 		}
 		//Getting the instance of the default biz logic on which retrieve method is later called.
 		DefaultBizLogic defaultBizLogic = BizLogicFactory.getDefaultBizLogic();
 		List objectList = new ArrayList();
 		try
 		{
 			//the following method gives the object , the class name of which is passed as the first parameter.
 			// The criteria for the object is given in the second and third parameter. The second parameter is the
 			// field of the object that needs to be compared with the values that is given as the third parameter.
 			objectList = defaultBizLogic.retrieve(className, "name", objectName);
 		}
 		catch (DAOException e)
 		{
 			throw new DynamicExtensionsSystemException(e.getMessage(), e);
 		}
 
 		if (objectList != null && objectList.size() > 0)
 		{
 			object = (DynamicExtensionBaseDomainObjectInterface) objectList.get(0);
 		}
 
 		return object;
 	}
 
 	/**
 	 * Returns an attribute given the entity name and attribute name.
 	 * @param entityName name of the entity.
 	 * @param attributeName name of the attribute.
 	 * @return AttributeInterface attribute interface
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public AttributeInterface getAttribute(String entityName, String attributeName)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		AttributeInterface attributeInterface = null;
 		AbstractAttributeInterface abstractAttributeInterface;
 		String name;
 		if (entityName == null || entityName.equals("") || attributeName == null
 				|| attributeName.equals(""))
 		{
 			return attributeInterface;
 		}
 		//First the entity object is fetched for the name that is passed.Then the entity's attribute collection is
 		//scanned to select the required attribute.
 		EntityInterface entityInterface = getEntityByName(entityName);
 		if (entityInterface != null)
 		{
 			Collection abstractAttributeCollection = entityInterface
 					.getAbstractAttributeCollection();
 			if (abstractAttributeCollection != null)
 			{
 				Iterator abstractAttributeIterator = abstractAttributeCollection.iterator();
 
 				while (abstractAttributeIterator.hasNext())
 				{
 					abstractAttributeInterface = (AbstractAttributeInterface) abstractAttributeIterator
 							.next();
 					if (abstractAttributeInterface instanceof AttributeInterface)
 					{
 						attributeInterface = (AttributeInterface) abstractAttributeInterface;
 						name = attributeInterface.getName();
 						if (name != null && name.equals(attributeName))
 						{
 							return attributeInterface;
 						}
 					}
 				}
 
 			}
 
 		}
 		return attributeInterface;
 	}
 
 	/**
 	 * Returns an association object given the entity name and source role name.
 	 * @param entityName
 	 * @param sourceRoleName
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 
 	public Collection<AssociationInterface> getAssociation(String entityName, String sourceRoleName)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Map substitutionParameterMap = new HashMap();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("string", entityName));
 		substitutionParameterMap.put("1", new HQLPlaceHolderObject("string", sourceRoleName));
 		//Following method is called to execute the stored HQL , the name of which is given as the first parameter.
 		//The second parameter is the map which contains the actual values that are replaced for the placeholders.
 
 		Collection<AssociationInterface> associationCollection = executeHQL("getAssociation",
 				substitutionParameterMap);
 
 		return associationCollection;
 	}
 
 	/**
 	 * This method returns the collection of entities given the concept code for the entity.
 	 * @param entityConceptCode concept code for the entity
 	 * @return entityCollection a collection of entities.
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public Collection<EntityInterface> getEntitiesByConceptCode(String entityConceptCode)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Map substitutionParameterMap = new HashMap();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("string", entityConceptCode));
 		//Following method is called to execute the stored HQL , the name of which is given as the first parameter.
 		//The second parameter is the map which contains the actual values that are replaced for the placeholders.
 		Collection entityCollection = executeHQL("getEntitiesByConceptCode",
 				substitutionParameterMap);
 		return entityCollection;
 	}
 
 	/**
 	 * Returns all entities in the whole system
 	 * @return Collection Entity Collection
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public Collection<EntityInterface> getAllEntities() throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		//CAlling generic method to return all stored instances of the object, the class name of which is passed as
 		//the parameter.
 		return getAllObjects(EntityInterface.class.getName());
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getEntityByIdentifier(java.lang.String)
 	 */
 	public EntityInterface getEntityByIdentifier(String identifier)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		//		CAlling generic method to return all stored instances of the object, the identifier of which is passed as
 		//the parameter.
 		return (EntityInterface) getObjectByIdentifier(EntityInterface.class.getName(), identifier);
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getEntityByIdentifier(java.lang.Long)
 	 */
 	public EntityInterface getEntityByIdentifier(Long id) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		//		CAlling generic method to return all stored instances of the object, the identifier of which is passed as
 		//the parameter.
 		return (EntityInterface) getObjectByIdentifier(EntityInterface.class.getName(), id
 				.toString());
 	}
 
 	/**
 	 * This method populates the TableProperties object in entity which holds the unique tablename for the entity.
 	 * This table name is generated using the unique identifier that is generated after saving the object.
 	 * The format for generating this table/column name is "DE_<E/AT/AS>_<UNIQUE IDENTIFIER>"
 	 * So we need this method to generate the table name for the entity then create the corresponding
 	 * tableProperties object and then update the entity object with this newly added tableProperties object.
 	 * Similarly we add ColumnProperties object for each of the attribute and also the ConstraintProperties object
 	 * for each of the associations.
 	 * @param entity Entity object on which to process the post save operations.
 	 * @param rollbackQueryStack
 	 * @param hibernateDAO
 	 * @param processedEntityList
 	 * @param addIdAttribute
 	 * @param copyDataTableState - Bug#5196, 5097 - Copying the dataTableState for entity to the associated target entity
 	 * based on this boolean value. Default value is 'true'. While adding association with static catissue entity,
 	 * value is 'false'.
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws UserNotAuthorizedException
 	 * @throws DAOException
 	 * @throws HibernateException
 	 */
 	private void postSaveProcessEntityForSingleAnnotation(Entity entity, HibernateDAO hibernateDAO,
 			Stack rollbackQueryStack, List<EntityInterface> processedEntityList,
 			boolean addIdAttribute, boolean isEntityFromXMI,boolean copyDataTableState,AssociationInterface annotationAssociation)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException,
 			DAOException, UserNotAuthorizedException, HibernateException
 	{
 		if (entity.getTableProperties() == null)
 		{
 			TableProperties tableProperties = new TableProperties();
 			String tableName = TABLE_NAME_PREFIX + UNDERSCORE + entity.getId();
 			tableProperties.setName(tableName);
 			entity.setTableProperties(tableProperties);
 		}
 		Collection attributeCollection = entity.getAbstractAttributeCollection();
 		entity.setLastUpdated(new Date());
 		if (attributeCollection != null && !attributeCollection.isEmpty())
 		{
 			Collection tempAttributeCollection = new HashSet(attributeCollection);
 			Iterator iterator = tempAttributeCollection.iterator();
 			while (iterator.hasNext())
 			{
 				AbstractAttribute attribute = (AbstractAttribute) iterator.next();
 				if (attribute instanceof Attribute
 						&& ((Attribute) attribute).getColumnProperties() == null)
 				{
 					ColumnProperties colProperties = new ColumnProperties();
 					String colName = COLUMN_NAME_PREFIX + UNDERSCORE + attribute.getId();
 					colProperties.setName(colName);
 					((Attribute) attribute).setColumnProperties(colProperties);
 				}
 				else if (attribute instanceof AssociationInterface)
 				{
 					Association association = (Association) attribute;
 					if (annotationAssociation != null && association.equals(annotationAssociation))
 					{
 						ConstraintPropertiesInterface constraintProperties = association
 								.getConstraintProperties();
 						EntityInterface targetEntity = association.getTargetEntity();
 						boolean isEntitySaved = false;
 						if (!association.getIsSystemGenerated())
 						{
 							if (targetEntity.getId() != null)
 							{
 								isEntitySaved = true;
 							}
 							if(copyDataTableState)
 							{
 								((Entity) targetEntity).setDataTableState(entity.getDataTableState());
 							}
 							if (entity.getDataTableState() == DATA_TABLE_STATE_CREATED)
 							{
 								targetEntity = saveOrUpdateEntity(targetEntity, hibernateDAO,
 										rollbackQueryStack, isEntitySaved, processedEntityList,
 										addIdAttribute, isEntityFromXMI,copyDataTableState);
 							}
 							else
 							{
 								targetEntity = saveOrUpdateEntityMetadata(targetEntity, hibernateDAO,
 										rollbackQueryStack, isEntitySaved, processedEntityList,copyDataTableState);
 							}
 
 						}
 						//Calling the particular method that populates the constraint properties for the association.
 						populateConstraintProperties(association);
 						//Calling the method which creates or removes the system generated association depending on
 						//the passed association.
 						populateSystemGeneratedAssociation(association, hibernateDAO);
 					}
 				}
 			}
 		}
 	}
 	/**
 	 * This method populates the TableProperties object in entity which holds the unique tablename for the entity.
 	 * This table name is generated using the unique identifier that is generated after saving the object.
 	 * The format for generating this table/column name is "DE_<E/AT/AS>_<UNIQUE IDENTIFIER>"
 	 * So we need this method to generate the table name for the entity then create the corresponding
 	 * tableProperties object and then update the entity object with this newly added tableProperties object.
 	 * Similarly we add ColumnProperties object for each of the attribute and also the ConstraintProperties object
 	 * for each of the associations.
 	 * @param entity Entity object on which to process the post save operations.
 	 * @param rollbackQueryStack
 	 * @param hibernateDAO
 	 * @param processedEntityList
 	 * @param addIdAttribute
 	 * @param copyDataTableState - Bug#5196, 5097 - Copying the dataTableState for entity to the associated target entity
 	 * based on this boolean value. Default value is 'true'. While adding association with static catissue entity,
 	 * value is 'false'.
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws UserNotAuthorizedException
 	 * @throws DAOException
 	 * @throws HibernateException
 	 */
 	private void postSaveProcessEntity(Entity entity, HibernateDAO hibernateDAO,
 			Stack rollbackQueryStack, List<EntityInterface> processedEntityList,
 			boolean addIdAttribute, boolean isEntityFromXMI,boolean copyDataTableState)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException,
 			DAOException, UserNotAuthorizedException, HibernateException
 	{
 		if (entity.getTableProperties() == null)
 		{
 			TableProperties tableProperties = new TableProperties();
 			String tableName = TABLE_NAME_PREFIX + UNDERSCORE + entity.getId();
 			tableProperties.setName(tableName);
 			entity.setTableProperties(tableProperties);
 		}
 		Collection attributeCollection = entity.getAbstractAttributeCollection();
 		entity.setLastUpdated(new Date());
 		if (attributeCollection != null && !attributeCollection.isEmpty())
 		{
 			Collection tempAttributeCollection = new HashSet(attributeCollection);
 			Iterator iterator = tempAttributeCollection.iterator();
 			while (iterator.hasNext())
 			{
 				AbstractAttribute attribute = (AbstractAttribute) iterator.next();
 				if (attribute instanceof Attribute
 						&& ((Attribute) attribute).getColumnProperties() == null)
 				{
 					ColumnProperties colProperties = new ColumnProperties();
 					String colName = COLUMN_NAME_PREFIX + UNDERSCORE + attribute.getId();
 					colProperties.setName(colName);
 					((Attribute) attribute).setColumnProperties(colProperties);
 				}
 				else if (attribute instanceof AssociationInterface)
 				{
 					Association association = (Association) attribute;
 
 					ConstraintPropertiesInterface constraintProperties = association
 							.getConstraintProperties();
 					EntityInterface targetEntity = association.getTargetEntity();
 					boolean isEntitySaved = false;
 					if (!association.getIsSystemGenerated())
 					{
 						if (targetEntity.getId() != null)
 						{
 							isEntitySaved = true;
 						}
 						if (copyDataTableState)
 						{
 							((Entity) targetEntity).setDataTableState(entity.getDataTableState());
 						}
 						if (entity.getDataTableState() == DATA_TABLE_STATE_CREATED)
 						{
 							targetEntity = saveOrUpdateEntity(targetEntity, hibernateDAO,
 									rollbackQueryStack, isEntitySaved, processedEntityList,
 									addIdAttribute, isEntityFromXMI, copyDataTableState);
 						}
 						else
 						{
 							targetEntity = saveOrUpdateEntityMetadata(targetEntity, hibernateDAO,
 									rollbackQueryStack, isEntitySaved, processedEntityList,
 									copyDataTableState);
 						}
 
 					}
 					//Calling the particular method that populates the constraint properties for the association.
 					populateConstraintProperties(association);
 					//Calling the method which creates or removes the system generated association depending on
 					//the passed association.
 					populateSystemGeneratedAssociation(association, hibernateDAO);
 				}
 			}
 		}
 	}
 	/**This method is used for following purposes.
 	 * 1. The method creates a system generated association in case when the association is bidirectional.
 	 * Bi directional association is supposed to be a part of the target entity's attributes.
 	 * So we create a replica of the original association (which we call as system generated association)
 	 * and this association is added to the target entity's attribute collection.
 	 * 2. The method also removes the system generated association from the target entity
 	 * when the association direction of the assciation is changed from "bi-directional" to "SRC-Destination".
 	 * In this case we no longer need the system generated association.
 	 * So if the sys. generated association is present , it is removed.
 	 * @param association
 	 * @param hibernateDAO
 	 * @throws UserNotAuthorizedException
 	 * @throws DAOException
 	 */
 	private void populateSystemGeneratedAssociation(Association association,
 			HibernateDAO hibernateDAO) throws DAOException, UserNotAuthorizedException
 	{
 		//Getting the sys.generated association for the given original association.
 		if (association.getIsSystemGenerated())
 		{
 			return;
 		}
 		else
 		{
 			Association systemGeneratedAssociation = getSystemGeneratedAssociation(association);
 			boolean isTargetEntityChanged = false;
 			if (association.getAssociationDirection() == AssociationDirection.BI_DIRECTIONAL)
 			{
 				ConstraintPropertiesInterface constraintPropertiesSysGen = new ConstraintProperties();
 				if (systemGeneratedAssociation == null)
 				{
 					systemGeneratedAssociation = new Association();
 				}
 				else
 				{
 					constraintPropertiesSysGen = systemGeneratedAssociation
 							.getConstraintProperties();
 				}
 				constraintPropertiesSysGen.setName(association.getConstraintProperties().getName());
 				//Swapping the source and target keys.
 				constraintPropertiesSysGen.setSourceEntityKey(association.getConstraintProperties()
 						.getTargetEntityKey());
 				constraintPropertiesSysGen.setTargetEntityKey(association.getConstraintProperties()
 						.getSourceEntityKey());
 				//Populating the sys. generated association.
 				//systemGeneratedAssociation.setName(association.getName());
 
 				//For XMI import, we can get self referencing bi directional associations. Hence creating unique name for the sys generated association.
 				systemGeneratedAssociation.setName(association.getName()
 						+ association.getEntity().getId());
 
 				systemGeneratedAssociation.setDescription(association.getDescription());
 				systemGeneratedAssociation.setTargetEntity(association.getEntity());
 				systemGeneratedAssociation.setEntity(association.getTargetEntity());
 				//Swapping the source and target roles.
 				systemGeneratedAssociation.setSourceRole(association.getTargetRole());
 				systemGeneratedAssociation.setTargetRole(association.getSourceRole());
 				systemGeneratedAssociation
 						.setAssociationDirection(AssociationDirection.BI_DIRECTIONAL);
 				systemGeneratedAssociation.setIsSystemGenerated(true);
 				systemGeneratedAssociation.setConstraintProperties(constraintPropertiesSysGen);
 
 				for (TaggedValueInterface taggedValue : association.getTaggedValueCollection())
 				{
 					systemGeneratedAssociation.addTaggedValue(((TaggedValue) taggedValue).clone());
 				}
 
 				//Adding the sys.generated association to the target entity.
 				association.getTargetEntity().addAbstractAttribute(systemGeneratedAssociation);
 				isTargetEntityChanged = true;
 			}
 			else
 			{
 				//Removing the not required sys. generated association because the direction has been changed
 				//from "bi directional" to "src-destination".
 				if (systemGeneratedAssociation != null)
 				{
 					association.getTargetEntity().removeAbstractAttribute(
 							systemGeneratedAssociation);
 					isTargetEntityChanged = true;
 				}
 			}
 
 			if (isTargetEntityChanged)
 			{
 				//Saving the modified target entity.
 				try
 				{
 					DBUtil.currentSession().saveOrUpdateCopy(association.getTargetEntity());
 				}
 				catch (Exception e)
 				{
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				//hibernateDAO.update(association.getTargetEntity(), null, false, false, false);
 			}
 		}
 	}
 
 	/**This method is used to get the system generated association given the original association.
 	 * System generated association is searched based on the following criteria
 	 * 1. The flag "isSystemGenerated" should be set.
 	 * 2. The source and target roles are swapped. So original association's source role should be the target role
 	 * of the sys.generated association and vice versa.
 	 * @param association
 	 * @return
 	 */
 	private Association getSystemGeneratedAssociation(Association association)
 	{
 		EntityInterface targetEnetity = association.getTargetEntity();
 		Collection associationCollection = targetEnetity.getAssociationCollection();
 		if (associationCollection != null && !associationCollection.isEmpty())
 		{
 			Iterator associationIterator = associationCollection.iterator();
 			while (associationIterator.hasNext())
 			{
 				Association associationInterface = (Association) associationIterator.next();
 				if (associationInterface.getIsSystemGenerated()
 						&& associationInterface.getSourceRole().equals(association.getTargetRole())
 						&& associationInterface.getTargetRole().equals(association.getSourceRole()))
 				{
 					return associationInterface;
 				}
 			}
 		}
 		return null;
 	}
 
 	/**This method populates the constraint properties for the given association. Creation/population of
 	 * constraint properties depend on Cardinalities of the source and target roles.
 	 * Folliowing are the possible cases.
 	 * 1. Many to many. --> source key , target key and middle table name are created and populated.
 	 * 2.Many to one --> Only source key is created and populated as the extra column gets added to the source entity.
 	 * 3. One to one or one to many --> In either case, only target key is populated because one extra column gets
 	 * added to the target entity.
 	 * Naming conventions for the source, target keys and the middle table are
 	 * Source key --> DE_E_S_[Source entity identifier]_[Association_identifier]_IDENTIFIER
 	 * Target key --> DE_E_T_[target entity identifier]_[Association_identifier_IDENTIFIER
 	 * Middle table name --> DE_E_[Source entity identifier]_[target entity identifier]_[Association_identifier]
 	 * @param association
 	 */
 	private void populateConstraintProperties(Association association)
 	{
 
 		ConstraintPropertiesInterface constraintProperties = association.getConstraintProperties();
 		if (constraintProperties == null)
 		{
 			constraintProperties = DomainObjectFactory.getInstance().createConstraintProperties();
 		}
 		EntityInterface sourceEntity = association.getEntity();
 		EntityInterface targetEntity = association.getTargetEntity();
 		if (((Entity) sourceEntity).getDataTableState() == DATA_TABLE_STATE_ALREADY_PRESENT)
 		{
 			return;
 		}
 		RoleInterface sourceRole = association.getSourceRole();
 		RoleInterface targetRole = association.getTargetRole();
 		Cardinality sourceMaxCardinality = sourceRole.getMaximumCardinality();
 		Cardinality targetMaxCardinality = targetRole.getMaximumCardinality();
 		if (sourceMaxCardinality == Cardinality.MANY && targetMaxCardinality == Cardinality.MANY)
 		{
 			constraintProperties.setSourceEntityKey(ASSOCIATION_COLUMN_PREFIX + UNDERSCORE + "S"
 					+ UNDERSCORE + sourceEntity.getId() + UNDERSCORE + association.getId()
 					+ UNDERSCORE + IDENTIFIER);
 			constraintProperties.setTargetEntityKey(ASSOCIATION_COLUMN_PREFIX + UNDERSCORE + "T"
 					+ UNDERSCORE + targetEntity.getId() + UNDERSCORE + association.getId()
 					+ UNDERSCORE + IDENTIFIER);
 			constraintProperties.setName(ASSOCIATION_NAME_PREFIX + UNDERSCORE
 					+ sourceEntity.getId() + UNDERSCORE + targetEntity.getId() + UNDERSCORE
 					+ +association.getId());
 		}
 		else if (sourceMaxCardinality == Cardinality.MANY
 				&& targetMaxCardinality == Cardinality.ONE)
 		{
 			constraintProperties.setSourceEntityKey(ASSOCIATION_COLUMN_PREFIX + UNDERSCORE
 					+ targetEntity.getId() + UNDERSCORE + association.getId() + UNDERSCORE
 					+ IDENTIFIER);
 			constraintProperties.setTargetEntityKey(null);
 			constraintProperties.setName(sourceEntity.getTableProperties().getName());
 		}
 		else
 		{
 			constraintProperties.setTargetEntityKey(ASSOCIATION_COLUMN_PREFIX + UNDERSCORE
 					+ sourceEntity.getId() + UNDERSCORE + association.getId() + UNDERSCORE
 					+ IDENTIFIER);
 			constraintProperties.setSourceEntityKey(null);
 			constraintProperties.setName(targetEntity.getTableProperties().getName());
 		}
 		association.setConstraintProperties(constraintProperties);
 
 	}
 
 	/**
 	 * This method checks if the entity can be created with the given name or not. This method will check for the duplicate name
 	 * as per the following rule
 	 * <br>The entities which belong to the same entity group can not share same name.
 	 * @param entity Entity whose name's uniqueness is to be checked.
 	 * @throws DynamicExtensionsApplicationException This will basically act as a duplicate name  exception.
 	 */
 	private void checkForDuplicateEntityName(Entity entity)
 			throws DynamicExtensionsApplicationException
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * This method is called when there any exception occurs while generating the data table queries for the entity. Valid scenario is
 	 * that if we need to fire Q1 Q2 and Q3 in order to create the data tables and Q1 Q2 get fired successfully and exception occurs
 	 * while executing query Q3 then this method receives the query list which holds the set of queries which negate the effect of
 	 * the queries which were generated successfully so that the metadata information and database are in synchronisation.
 	 * @param reverseQueryList Stack that maintains the queries to execute.
 	 * @param conn
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private void rollbackQueries(Stack reverseQueryList, Entity entity, Exception e, AbstractDAO dao)
 			throws DynamicExtensionsSystemException
 	{
 		String message = "";
 		try
 		{
 			dao.rollback();
 		}
 		catch (DAOException e2)
 		{
 			logDebug("rollbackQueries", DynamicExtensionsUtility.getStackTrace(e));
 			DynamicExtensionsSystemException ex = new DynamicExtensionsSystemException(message, e);
 			ex.setErrorCode(DYEXTN_S_000);
 			throw ex;
 
 		}
 
 		if (reverseQueryList != null && !reverseQueryList.isEmpty())
 		{
 
 			Connection conn;
 			try
 			{
 				conn = DBUtil.getConnection();
 				while (!reverseQueryList.empty())
 				{
 					String query = (String) reverseQueryList.pop();
 					PreparedStatement statement = null;
 					statement = conn.prepareStatement(query);
 					statement.executeUpdate();
 				}
 
 			}
 			catch (HibernateException e1)
 			{
 				message = e1.getMessage();
 
 			}
 			catch (SQLException exc)
 			{
 				message = exc.getMessage();
 				LogFatalError(exc, entity);
 			}
 			finally
 			{
 				logDebug("rollbackQueries", DynamicExtensionsUtility.getStackTrace(e));
 				DynamicExtensionsSystemException ex = new DynamicExtensionsSystemException(message,
 						e);
 				ex.setErrorCode(DYEXTN_S_000);
 				throw ex;
 			}
 
 		}
 
 	}
 
 	/**
 	 * this method is called when exception occurs while executing the rollback queries or reverse queries. When this method is called , it
 	 * signifies that the database state and the metadata state for the entity are not in synchronisation and administrator needs some
 	 * database correction.
 	 * @param e The exception that took place.
 	 * @param entity Entity for which data tables are out of sync.
 	 */
 	private void LogFatalError(Exception e, Entity entity)
 	{
 		String table = "";
 		String name = "";
 		if (entity != null)
 		{
 			entity.getTableProperties().getName();
 			name = entity.getName();
 		}
 		Logger.out
 				.error("***Fatal Error.. Incosistent data table and metadata information for the entity -"
 						+ name + "***");
 		Logger.out.error("Please check the table -" + table);
 		Logger.out.error("The cause of the exception is - " + e.getMessage());
 		Logger.out.error("The detailed log is : ");
 		e.printStackTrace();
 
 	}
 
 	/**
 	 * Returns a collection of entities having attribute with the given name
 	 * @param attributeName
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public Collection getEntitiesByAttributeName(String attributeName)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		return null;
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAllContainers()
 	 */
 	public Collection<ContainerInterface> getAllContainers()
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		//CAlling generic method to return all stored instances of the object, the class name of which is passed as
 		//the parameter.
 		return getAllObjects(ContainerInterface.class.getName());
 	}
 
 	/**
 	 * This method returns object for a given class name and identifer
 	 * @param objectName  name of the class of the object
 	 * @param identifier identifier of the object
 	 * @return  obejct
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private DynamicExtensionBaseDomainObject getObjectByIdentifier(String objectName,
 			String identifier) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		AbstractBizLogic bizLogic = BizLogicFactory.getDefaultBizLogic();
 		DynamicExtensionBaseDomainObject object;
 		try
 		{
 			List objectList = bizLogic.retrieve(objectName, Constants.ID, identifier);
 
 			if (objectList == null || objectList.size() == 0)
 			{
 				Logger.out.debug("Required Obejct not found: Object Name*" + objectName
 						+ "*   identifier  *" + identifier + "*");
 				System.out.println("Required Obejct not found: Object Name*" + objectName
 						+ "*   identifier  *" + identifier + "*");
 				throw new DynamicExtensionsApplicationException("OBJECT_NOT_FOUND");
 			}
 
 			object = (DynamicExtensionBaseDomainObject) objectList.get(0);
 		}
 		catch (DAOException e)
 		{
 			throw new DynamicExtensionsSystemException(e.getMessage(), e);
 		}
 		return object;
 	}
 
 	/**
 	 *  Returns all instances in the whole system for a given type of the object
 	 * @return Collection of instances of given class
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private Collection getAllObjects(String objectName) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		AbstractBizLogic bizLogic = BizLogicFactory.getDefaultBizLogic();
 		Collection objectList = new HashSet();
 
 		try
 		{
 			objectList = bizLogic.retrieve(objectName);
 			if (objectList == null)
 			{
 				objectList = new HashSet();
 			}
 		}
 		catch (DAOException e)
 		{
 			throw new DynamicExtensionsSystemException(e.getMessage(), e);
 		}
 		return objectList;
 	}
 
 	public ContainerInterface persistContainer(ContainerInterface containerInterface)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 
 		return persistContainer(containerInterface,true);
 	}
 
 
 
 //	/**
 //	 * This method is used to save the container into the database.
 //	 * @param containerInterface container to save
 //	 * @return ContainerInterface container Interface that is saved.
 //	 * @throws DynamicExtensionsSystemException Thrown if for any reason operation can not be completed.
 //	 * @throws DynamicExtensionsApplicationException Thrown if the entity name already exists.
 //	 * @throws DynamicExtensionsSystemException
 //	 */
 //	public ContainerInterface persistContainer(ContainerInterface containerInterface,
 //			List<ArrayList> processedContainerListEntityList,boolean addIdAttribute)
 //			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 //	{
 //		HibernateDAO hibernateDAO = (HibernateDAO) DAOFactory.getInstance().getDAO(
 //				Constants.HIBERNATE_DAO);
 //		ContainerInterface container = null;
 //		try
 //		{
 //
 //			hibernateDAO.closeSession();
 //			hibernateDAO.openSession(null);
 //
 //			List<ContainerInterface> processedContainerList = processedContainerListEntityList
 //					.get(0);
 //			List<EntityInterface> processedEntityList = processedContainerListEntityList.get(1);
 //
 ////			container = persistContainer(containerInterface, addIdAttribute, hibernateDAO,
 ////					processedEntityList, processedContainerList);
 //			hibernateDAO.commit();
 //
 //		}
 //		catch (DAOException e)
 //		{
 //			//rollbackQueries(rollbackQueryStack, entity, e, hibernateDAO);
 //			throw new DynamicExtensionsSystemException(
 //					"DAOException occured while opening a session to save the container.", e);
 //		}
 //		finally
 //		{
 //			try
 //			{
 //				hibernateDAO.closeSession();
 //			}
 //			catch (Exception e)
 //			{
 //				//	rollbackQueries(rollbackQueryStack, entity, e, hibernateDAO);
 //				throw new DynamicExtensionsSystemException(
 //						"DAOException occured while closing a session to save the container.", e);
 //			}
 //		}
 //		return container;
 //
 //	}
 
 	/**
 	 * @param containerInterface
 	 * @param addIdAttribute
 	 * @param hibernateDAO
 	 * @param processedEntityList
 	 * @param processedContainerList
 	 * @param dataBaseCopy
 	 * @return
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private ContainerInterface persistContainer(ContainerInterface containerInterface,
 			boolean addIdAttribute, HibernateDAO hibernateDAO,
 			/*List<EntityInterface> processedEntityList,*/
 			List<ContainerInterface> processedContainerList, EntityGroupInterface currentEntityGroup)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		Container container = (Container) containerInterface;
 
 		boolean isContainerProcessed = isContainerProcessed(processedContainerList,
 				containerInterface);
 		if (isContainerProcessed)
 		{
 			return container;
 		}
 		else
 		{
 			processedContainerList.add(container);
 		}
 
 		Stack rollbackQueryStack = new Stack();
 		if (container == null)
 		{
 			throw new DynamicExtensionsSystemException("Container passed is null");
 		}
 
 		Entity entity = (Entity) container.getEntity();
 		Session session = null;
 		try
 		{
 			session = DBUtil.currentSession();
 			if (container.getBaseContainer() != null)
 			{
 				if (container.getBaseContainer().getId() == null)
 				{
 					persistContainer(container.getBaseContainer(), addIdAttribute,
 							hibernateDAO,/* processedEntityList, */processedContainerList,currentEntityGroup);
 				}
 			}
 
 			if (entity != null)
 			{
 				saveContainerForContainmentAssociation(container, addIdAttribute, hibernateDAO,
 						 processedContainerList, currentEntityGroup);
 			}
 
 			preSaveProcessContainer(container); //preprocess
 
 			session.saveOrUpdateCopy(container);
 
 			if (currentEntityGroup != null)
 			{
 				currentEntityGroup.addMainContainer(container);
 				session.saveOrUpdateCopy(currentEntityGroup);
 			}
 		}
 		catch (HibernateException e)
 		{
 			//In case of exception execute roll back queries to restore the database state.
 			rollbackQueries(rollbackQueryStack, entity, e, hibernateDAO);
 			throw new DynamicExtensionsSystemException(
 					"Exception occured while opening a session to save the container.", e);
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			rollbackQueries(rollbackQueryStack, entity, e, hibernateDAO);
 			e.printStackTrace();
 			throw e;
 		}
 		return container;
 	}
 
 	/**
 	 * @param processedContainerList
 	 * @param container
 	 * @return
 	 */
 	private boolean isContainerProcessed(List<ContainerInterface> processedContainerList,
 			ContainerInterface container)
 	{
 		for (ContainerInterface containerFormList : processedContainerList)
 		{
 			if (containerFormList.getCaption().equalsIgnoreCase(container.getCaption()))
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * @param processedEntityList
 	 * @param entity
 	 * @return
 	 */
 	private boolean isEntityProcessed(List<EntityInterface> processedEntityList,
 			EntityInterface entity)
 	{
 		for (EntityInterface entityFromList : processedEntityList)
 		{
 			if (entityFromList.getName().equalsIgnoreCase(entity.getName()))
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * This method is used to save the container into the database.
 	 * @param containerInterface container to save
 	 * @return ContainerInterface container Interface that is saved.
 	 * @throws DynamicExtensionsSystemException Thrown if for any reason operation can not be completed.
 	 * @throws DynamicExtensionsApplicationException Thrown if the entity name already exists.
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public ContainerInterface persistContainer(ContainerInterface containerInterface,
 			boolean addIdAttribute) throws DynamicExtensionsApplicationException,
 			DynamicExtensionsSystemException
 	{
 		Container container = (Container) containerInterface;
 		Stack rollbackQueryStack = new Stack();
 		if (container == null)
 		{
 			throw new DynamicExtensionsSystemException("Container passed is null");
 		}
 
 		Entity entity = (Entity) container.getEntity();
 		HibernateDAO hibernateDAO = (HibernateDAO) DAOFactory.getInstance().getDAO(
 				Constants.HIBERNATE_DAO);
 		Session session = null;
 		boolean isentitySaved = true;
 		if (entity != null && entity.getId() == null)
 		{
 			isentitySaved = false;
 		}
 
 		try
 		{
 
 			hibernateDAO.closeSession();
 			hibernateDAO.openSession(null);
 			session = DBUtil.currentSession();
 			EntityGroupInterface currentEntityGroup = null;
 
 			if (entity != null)
 			{
 				//saveEntityGroup first
 				currentEntityGroup = saveEntityGroup(entity, hibernateDAO);
 				// saves the entity into database. It populates rollbackQueryStack with the
 				// queries that restores the database state to the state before calling this method
 				// in case of exception.
 				List<EntityInterface> processedEntityList = new ArrayList<EntityInterface>();
 				saveOrUpdateEntity(entity, hibernateDAO, rollbackQueryStack, isentitySaved,
 						processedEntityList, addIdAttribute, false, true);
 				saveChildContainers(container, session);
 			}
 
 			preSaveProcessContainer(container); //preprocess
 
 			session.saveOrUpdateCopy(container);
 
 			if (currentEntityGroup != null)
 			{
 				currentEntityGroup.addMainContainer(container);
 				session.saveOrUpdateCopy(currentEntityGroup);
 			}
 
 			hibernateDAO.commit();
 
 		}
 		catch (HibernateException e)
 		{
 
 			//In case of exception execute roll back queries to restore the database state.
 			rollbackQueries(rollbackQueryStack, entity, e, hibernateDAO);
 			throw new DynamicExtensionsSystemException(
 					"Exception occured while opening a session to save the container.", e);
 		}
 		catch (DAOException e)
 		{
 			rollbackQueries(rollbackQueryStack, entity, e, hibernateDAO);
 			throw new DynamicExtensionsSystemException(
 					"DAOException occured while opening a session to save the container.", e);
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			rollbackQueries(rollbackQueryStack, entity, e, hibernateDAO);
 			e.printStackTrace();
 			throw e;
 		}
 		catch (UserNotAuthorizedException e)
 		{
 			rollbackQueries(rollbackQueryStack, entity, e, hibernateDAO);
 			e.printStackTrace();
 			throw new DynamicExtensionsSystemException(
 					"DAOException occured while opening a session to save the container.", e);
 		}
 		finally
 		{
 			try
 			{
 				postSaveOrUpdateEntity(entity);
 				//				session.close();
 				//				DBUtil.closeSession();
 				hibernateDAO.closeSession();
 			}
 			catch (Exception e)
 			{
 				rollbackQueries(rollbackQueryStack, entity, e, hibernateDAO);
 			}
 		}
 		return container;
 	}
 	/**
 	 * @param container
 	 * @param session
 	 * @param addIdAttribute
 	 * @param hibernateDAO
 	 * @param processedEntityList
 	 * @param processedContainerList
 	 * @throws HibernateException
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private void saveContainerForContainmentAssociation(ContainerInterface container,
 			boolean addIdAttribute, HibernateDAO hibernateDAO,
 			List<ContainerInterface> processedContainerList, EntityGroupInterface currentEntityGroup) throws HibernateException,
 			DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		for (ControlInterface control : container.getControlCollection())
 		{
 			if (control instanceof ContainmentAssociationControlInterface)
 			{
 				ContainmentAssociationControlInterface associationControl = (ContainmentAssociationControlInterface) control;
 				persistContainer(associationControl.getContainer(), addIdAttribute,
 						hibernateDAO,  processedContainerList,currentEntityGroup);
 			}
 		}
 	}
 	/**
 	 * @param container
 	 * @param session
 	 * @throws HibernateException
 	 */
 	private void saveChildContainers(ContainerInterface container, Session session) throws HibernateException,
 			DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		if (container != null)
 		{
 		for (ControlInterface control : container.getControlCollection())
 		{
 			if (control instanceof ContainmentAssociationControlInterface)
 			{
 				ContainmentAssociationControlInterface associationControl = (ContainmentAssociationControlInterface) control;
 
 				session.saveOrUpdateCopy(associationControl.getContainer());
 
 				saveChildContainers(associationControl.getContainer(), session);
 
 			}
 		}
 		}
 	}
 
 	/**
 	 * This method preprocesses container to validate it.
 	 * @param container container
 	 */
 	private void preSaveProcessContainer(Container container)
 			throws DynamicExtensionsApplicationException
 	{
 		if (container.getEntity() != null)
 		{
 			preSaveProcessEntity(container.getEntity());
 		}
 	}
 
 	/**
 	 * This method processes entity before saving it to databse.
 	 * <li> It validates entity for duplicate name of entity,attributes and association
 	 * <li> It sets created and updated date-time.
 	 *
 	 * @param entity entity
 	 */
 	private void preSaveProcessEntity(EntityInterface entity)
 			throws DynamicExtensionsApplicationException
 	{
 		DynamicExtensionsUtility.validateEntityForSaving(entity);// chk if entity is valid or not.
 
 		correctCardinalities(entity); // correct the cardinality if max cardinality  < min cardinality
 
 		if (entity.getId() != null)
 		{
 			entity.setLastUpdated(new Date());
 		}
 		else
 		{
 			entity.setCreatedDate(new Date());
 			entity.setLastUpdated(entity.getCreatedDate());
 		}
 	}
 
 	/**
 	 * This method corrects cardinalities such that max cardinality  < minimum cardinality ,otherwise it throws exception
 	 * @param entity
 	 */
 	private void correctCardinalities(EntityInterface entity)
 			throws DynamicExtensionsApplicationException
 	{
 		Collection associationCollection = entity.getAssociationCollection();
 		if (associationCollection != null && !associationCollection.isEmpty())
 		{
 			Iterator iterator = associationCollection.iterator();
 			while (iterator.hasNext())
 			{
 				Association association = (Association) iterator.next();
 				swapCardinality(association.getSourceRole());
 				swapCardinality(association.getTargetRole());
 
 			}
 		}
 	}
 
 	/**
 	 * @param role
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private void swapCardinality(RoleInterface role) throws DynamicExtensionsApplicationException
 	{
 		// make Min cardinality < Max cardinality
 		if (role.getMinimumCardinality().equals(Cardinality.MANY)
 				|| role.getMaximumCardinality().equals(Cardinality.ZERO))
 		{
 			Cardinality e = role.getMinimumCardinality();
 			role.setMinimumCardinality(role.getMaximumCardinality());
 			role.setMaximumCardinality(e);
 		}
 
 		if (role.getMaximumCardinality().equals(Cardinality.ZERO))
 		{
 			throw new DynamicExtensionsApplicationException("Cardinality constraint violated",
 					null, DYEXTN_A_005);
 		}
 	}
 
 	/**
 	 * @param entity
 	 * @param dataValue
 	 * @param hibernateDAO
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws HibernateException
 	 * @throws SQLException
 	 * @throws DAOException
 	 * @throws UserNotAuthorizedException
 	 */
 	private Long insertDataForSingleEntity(EntityInterface entity, Map dataValue,
 			HibernateDAO hibernateDAO, Long parentRecordId)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException,
 			HibernateException, SQLException, DAOException, UserNotAuthorizedException
 	{
 		if (entity == null)
 		{
 			throw new DynamicExtensionsSystemException("Input to insert data is null");
 		}
 
 		// if empty, insert row with only identifer column value.
 		if (dataValue == null)
 		{
 			dataValue = new HashMap();
 		}
 
 		StringBuffer columnNameString = new StringBuffer("IDENTIFIER ");
 		Long identifier = null;
 		if (parentRecordId != null)
 		{
 			identifier = parentRecordId;
 		}
 		else
 		{
 			identifier = entityManagerUtil.getNextIdentifier(entity.getTableProperties().getName());
 		}
 		StringBuffer columnValuesString = new StringBuffer(identifier.toString());
 		columnNameString.append(" , " + Constants.ACTIVITY_STATUS_COLUMN);
 		columnValuesString.append(" , '" + Constants.ACTIVITY_STATUS_ACTIVE + "' ");
 
 		String tableName = entity.getTableProperties().getName();
 
 		List<AttributeRecord> attributeRecords = new ArrayList<AttributeRecord>();
 
 		Set uiColumnSet = dataValue.keySet();
 		Iterator uiColumnSetIter = uiColumnSet.iterator();
 		List<String> queryList = new ArrayList<String>();
 		Object value = null;
 		while (uiColumnSetIter.hasNext())
 		{
 			AbstractAttribute attribute = (AbstractAttribute) uiColumnSetIter.next();
 			value = dataValue.get(attribute);
 
 			if (value == null)
 			{
 				continue;
 			}
 
 			if (attribute instanceof AttributeInterface)
 			{
 				AttributeInterface primitiveAttribute = (AttributeInterface) attribute;
 
 				// populate FileAttributeRecordValue HO
 				if (primitiveAttribute.getAttributeTypeInformation() instanceof FileAttributeTypeInformation)
 				{
 					AttributeRecord fileRecord = populateFileAttributeRecord(null, entity,
 							primitiveAttribute, identifier, (FileAttributeRecordValue) value);
 					attributeRecords.add(fileRecord);
 					continue;
 				}
 
 				// populate ObjectAttributeRecordValue HO
 				if (primitiveAttribute.getAttributeTypeInformation() instanceof ObjectAttributeTypeInformation)
 				{
 					AttributeRecord objectRecord = populateObjectAttributeRecord(null, entity,
 							primitiveAttribute, identifier, (ObjectAttributeRecordValue) value);
 					attributeRecords.add(objectRecord);
 					continue;
 				}
 
 				//	 For collection type attribute, populate CollectionAttributeRecordValue HO
 				if (primitiveAttribute.getIsCollection())
 				{
 					AttributeRecord collectionRecord = populateCollectionAttributeRecord(null,
 							entity, primitiveAttribute, identifier, (List<String>) value);
 					attributeRecords.add(collectionRecord);
 				}
 				else
 				// for other attribute, append to query
 				{
 					String strValue = queryBuilder.getFormattedValue(attribute, value);
 
 					if (strValue != null && !strValue.equalsIgnoreCase(""))
 					{
 						columnNameString.append(" , ");
 						columnValuesString.append(" , ");
 						String dbColumnName = primitiveAttribute.getColumnProperties().getName();
 						columnNameString.append(dbColumnName);
 						columnValuesString.append(strValue);
 					}
 				}
 			}
 			else
 			{
 				//In case of association separate queries need to fire depending on the cardinalities
 				AssociationInterface association = (AssociationInterface) attribute;
 				List<Long> recordIdList = null;
 
 				if (association.getSourceRole().getAssociationsType().equals(
 						AssociationType.CONTAINTMENT))
 				{
 					List<Map> listOfMapsForContainedEntity = (List) value;
 					recordIdList = new ArrayList<Long>();
 
 					//Map valueMapForContainedEntity = (Map) value;
 					for (Map valueMapForContainedEntity : listOfMapsForContainedEntity)
 					{
 						//						Long recordIdForContainedEntity = insertDataForSingleEntity(association
 						//								.getTargetEntity(), valueMapForContainedEntity, hibernateDAO, null);
 
 						Long recordIdForContainedEntity = insertDataForHeirarchy(association
 								.getTargetEntity(), valueMapForContainedEntity, hibernateDAO);
 						recordIdList.add(recordIdForContainedEntity);
 					}
 
 				}
 				else
 				{
 					recordIdList = (List<Long>) value;
 				}
 
 				queryList.addAll(queryBuilder.getAssociationInsertDataQuery(association,
 						recordIdList, identifier));
 
 			}
 		}
 
 		//query for other attributes.
 		StringBuffer query = new StringBuffer("INSERT INTO " + tableName + " ( ");
 		query.append(columnNameString);
 		query.append(" ) VALUES (");
 		query.append(columnValuesString);
 		query.append(" ) ");
 		queryList.add(0, query.toString());
 
 		logDebug("insertData", "Query is: " + query.toString());
 
 		Connection conn = DBUtil.getConnection();
 
 		for (String queryString : queryList)
 		{
 			logDebug("insertData", "Query for insert data is : " + queryString);
 			PreparedStatement statement = conn.prepareStatement(queryString);
 			statement.executeUpdate();
 		}
 
 		for (AttributeRecord collectionAttributeRecord : attributeRecords)
 		{
 			hibernateDAO.insert(collectionAttributeRecord, null, false, false);
 		}
 
 		return identifier;
 
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#insertData(edu.common.dynamicextensions.domaininterface.EntityInterface, java.util.Map)
 	 */
 	public Long insertData(EntityInterface entity, Map<AbstractAttributeInterface, ?> dataValue)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		List<Map<AbstractAttributeInterface, ?>> dataValueMapList = new ArrayList<Map<AbstractAttributeInterface, ?>>();
 		dataValueMapList.add(dataValue);
 		List<Long> recordIdList = insertData(entity, dataValueMapList);
 		return recordIdList.get(0);
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#insertData(edu.common.dynamicextensions.domaininterface.EntityInterface, java.util.Map)
 	 */
 	public List<Long> insertData(EntityInterface entity,
 			List<Map<AbstractAttributeInterface, ?>> dataValueMapList)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 
 		List<Long> recordIdList = new ArrayList<Long>();
 		HibernateDAO hibernateDAO = null;
 		try
 		{
 			DAOFactory factory = DAOFactory.getInstance();
 			hibernateDAO = (HibernateDAO) factory.getDAO(Constants.HIBERNATE_DAO);
 			hibernateDAO.openSession(null);
 
 			for (Map<AbstractAttributeInterface, ?> dataValue : dataValueMapList)
 			{
 				Long recordId = insertDataForHeirarchy(entity, dataValue, hibernateDAO);
 				recordIdList.add(recordId);
 			}
 
 			hibernateDAO.commit();
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			throw (DynamicExtensionsApplicationException) handleRollback(e,
 					"Error while inserting data", hibernateDAO, false);
 		}
 		catch (Exception e)
 		{
 			throw (DynamicExtensionsSystemException) handleRollback(e,
 					"Error while inserting data", hibernateDAO, true);
 		}
 		finally
 		{
 			try
 			{
 				hibernateDAO.closeSession();
 			}
 			catch (DAOException e)
 			{
 				throw (DynamicExtensionsSystemException) handleRollback(e, "Error while closing",
 						hibernateDAO, true);
 			}
 		}
 
 		return recordIdList;
 	}
 
 	/**
 	 * @param entity
 	 * @param dataValue
 	 * @param hibernateDAO
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws HibernateException
 	 * @throws SQLException
 	 * @throws DAOException
 	 * @throws UserNotAuthorizedException
 	 */
 	private Long insertDataForHeirarchy(EntityInterface entity,
 			Map<AbstractAttributeInterface, ?> dataValue, HibernateDAO hibernateDAO)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException,
 			HibernateException, SQLException, DAOException, UserNotAuthorizedException
 	{
 		List<EntityInterface> entityList = getParentEntityList(entity);
 		Map<EntityInterface, Map> entityValueMap = initialiseEntityValueMap(entity, dataValue);
 		Long parentRecordId = null;
 		for (EntityInterface entityInterface : entityList)
 		{
 			Map valueMap = entityValueMap.get(entityInterface);
 			parentRecordId = insertDataForSingleEntity(entityInterface, valueMap, hibernateDAO,
 					parentRecordId);
 		}
 
 		return parentRecordId;
 	}
 
 	/**
 	 * @param entity
 	 * @param dataValue
 	 * @return
 	 */
 	private Map<EntityInterface, Map> initialiseEntityValueMap(EntityInterface entity,
 			Map<AbstractAttributeInterface, ?> dataValue)
 	{
 		Map<EntityInterface, Map> entityMap = new HashMap<EntityInterface, Map>();
 
 		for (AbstractAttributeInterface abstractAttributeInterface : dataValue.keySet())
 		{
 			EntityInterface attributeEntity = abstractAttributeInterface.getEntity();
 			Object value = dataValue.get(abstractAttributeInterface);
 
 			Map<AbstractAttributeInterface, Object> entityDataValueMap = (Map) entityMap
 					.get(attributeEntity);
 			if (entityDataValueMap == null)
 			{
 				entityDataValueMap = new HashMap<AbstractAttributeInterface, Object>();
 				entityMap.put(attributeEntity, entityDataValueMap);
 			}
 			entityDataValueMap.put(abstractAttributeInterface, value);
 		}
 		return entityMap;
 	}
 
 	/**
 	 * @param entity
 	 * @return
 	 */
 	private List<EntityInterface> getParentEntityList(EntityInterface entity)
 	{
 		List<EntityInterface> entityList = new ArrayList<EntityInterface>();
 		entityList.add(entity);
 		while (entity.getParentEntity() != null)
 		{
 			entityList.add(0, entity.getParentEntity());
 			entity = entity.getParentEntity();
 		}
 		return entityList;
 	}
 
 	/**
 	 * @param e
 	 * @param string
 	 * @param hibernateDAO
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private Exception handleRollback(Exception e, String exceptionMessage, AbstractDAO dao,
 			boolean isExceptionToBeWrapped)
 	{
 		try
 		{
 			dao.rollback();
 		}
 		catch (DAOException e1)
 		{
 			return new DynamicExtensionsSystemException("error while rollback", e);
 		}
 
 		if (isExceptionToBeWrapped)
 		{
 			return new DynamicExtensionsSystemException(exceptionMessage, e);
 		}
 		else
 		{
 			return e;
 		}
 	}
 
 	/**
 	 * @param entity
 	 * @param dataValue
 	 * @param recordId
 	 * @return
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private boolean editDataForSingleEntity(EntityInterface entity, Map dataValue, Long recordId,
 			HibernateDAO hibernateDAO) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException, HibernateException, SQLException, DAOException,
 			UserNotAuthorizedException
 	{
 
 		if (entity == null || dataValue == null || dataValue.isEmpty())
 		{
 			return true;
 		}
 		StringBuffer updateColumnString = new StringBuffer();
 		String tableName = entity.getTableProperties().getName();
 		List<AttributeRecord> collectionRecords = new ArrayList<AttributeRecord>();
 		List<AttributeRecord> deleteCollectionRecords = new ArrayList<AttributeRecord>();
 		List<AttributeRecord> fileRecords = new ArrayList<AttributeRecord>();
 		List<AttributeRecord> objectRecords = new ArrayList<AttributeRecord>();
 
 		Set uiColumnSet = dataValue.keySet();
 		Iterator uiColumnSetIter = uiColumnSet.iterator();
 		List associationRemoveDataQueryList = new ArrayList();
 		List associationInsertDataQueryList = new ArrayList();
 		while (uiColumnSetIter.hasNext())
 		{
 			AbstractAttribute attribute = (AbstractAttribute) uiColumnSetIter.next();
 			Object value = dataValue.get(attribute);
 			if (value == null)
 			{
 				continue;
 			}
 			if (attribute instanceof AttributeInterface)
 			{
 				AttributeInterface primitiveAttribute = (AttributeInterface) attribute;
 
 				if (primitiveAttribute.getIsCollection())
 				{
 					// get previous values for multi select attributes
 					AttributeRecord collectionRecord = getAttributeRecord(entity.getId(),
 							primitiveAttribute.getId(), recordId, hibernateDAO);
 					List<String> listOfValues = (List<String>) value;
 
 					if (!listOfValues.isEmpty())
 					{ //if some values are provided,set these values clearing previous ones.
 						collectionRecord = populateCollectionAttributeRecord(collectionRecord,
 								entity, primitiveAttribute, recordId, (List<String>) value);
 						collectionRecords.add(collectionRecord);
 					}
 
 					if (collectionRecord != null && listOfValues.isEmpty())
 					{
 						//if updated value is empty list, then delete previously saved value if any.
 						deleteCollectionRecords.add(collectionRecord);
 					}
 
 				}
 				else if (primitiveAttribute.getAttributeTypeInformation() instanceof FileAttributeTypeInformation)
 				{
 					//For file type attribute,FileAttributeRecordValue needs to be updated for that record.
 
 					FileAttributeRecordValue fileRecordValue = (FileAttributeRecordValue) value;
 					AttributeRecord fileRecord = getAttributeRecord(entity.getId(),
 							primitiveAttribute.getId(), recordId, hibernateDAO);
 					if (fileRecord != null)
 					{
 						fileRecord.getFileRecord().copyValues(fileRecordValue);
 					}
 					else
 					{
 						fileRecord = populateFileAttributeRecord(null, entity,primitiveAttribute, recordId, (FileAttributeRecordValue) value);
 					}
 
 			//		fileRecord.getFileRecord().copyValues(fileRecordValue);
 					fileRecords.add(fileRecord);
 				}
 				else if (primitiveAttribute.getAttributeTypeInformation() instanceof ObjectAttributeTypeInformation)
 				{
 					//For object type attribute,ObjectAttributeRecordValue needs to be updated for that record.
 
 					ObjectAttributeRecordValue objectRecordValue = (ObjectAttributeRecordValue) value;
 					AttributeRecord objectRecord = getAttributeRecord(entity.getId(),
 							primitiveAttribute.getId(), recordId, hibernateDAO);
 					objectRecord.getObjectRecord().copyValues(objectRecordValue);
 					objectRecords.add(objectRecord);
 				}
 				else
 				{
 					//for other attributes, create the udpate query.
 					String dbColumnName = primitiveAttribute.getColumnProperties().getName();
 
 					if (updateColumnString.length() != 0)
 					{
 						updateColumnString.append(WHITESPACE + COMMA + WHITESPACE);
 					}
 
 					updateColumnString.append(dbColumnName);
 					updateColumnString.append(WHITESPACE + EQUAL + WHITESPACE);
 					value = queryBuilder.getFormattedValue(attribute, value);
 					updateColumnString.append(value);
 				}
 			}
 			else
 			{
 				AssociationInterface association = (AssociationInterface) attribute;
 				List<Long> recordIdList = new ArrayList<Long>();
 
 				if (association.getSourceRole().getAssociationsType().equals(
 						AssociationType.CONTAINTMENT))
 				{
 					List<String> removeContainmentRecordQuery = new ArrayList<String>();
 					recordIdList.add(recordId);
 
 					queryBuilder.getContenmentAssociationRemoveDataQueryList(
 							((Association) attribute), recordIdList, removeContainmentRecordQuery,
 							false);
 
 					entityManagerUtil.executeDML(removeContainmentRecordQuery);
 
 					List<Map> listOfMapsForContainedEntity = (List<Map>) value;
 					recordIdList.clear();
 					for (Map valueMapForContainedEntity : listOfMapsForContainedEntity)
 					{
 						//Long childRecordId = insertDataForSingleEntity(association
 						//.getTargetEntity(), valueMapForContainedEntity, hibernateDAO, null);
 						Long childRecordId = insertDataForHeirarchy(association.getTargetEntity(),
 								valueMapForContainedEntity, hibernateDAO);
 						recordIdList.add(childRecordId);
 					}
 
 				}
 				else
 				{
 					// for association need to remove previously associated target reocrd first.
 					String removeQuery = queryBuilder.getAssociationRemoveDataQuery(
 							((Association) attribute), recordId);
 
 					if (removeQuery != null && removeQuery.trim().length() != 0)
 					{
 						associationRemoveDataQueryList.add(removeQuery);
 					}
 
 					recordIdList = (List<Long>) value;
 				}
 
 				//then add new associated target records.
 				List insertQuery = queryBuilder.getAssociationInsertDataQuery(
 						((Association) attribute), recordIdList, recordId);
 				if (insertQuery != null && insertQuery.size() != 0)
 				{
 					associationInsertDataQueryList.addAll(insertQuery);
 				}
 
 			}
 		}
 
 		List<String> editDataQueryList = new ArrayList<String>();
 		editDataQueryList.addAll(associationRemoveDataQueryList);
 		editDataQueryList.addAll(associationInsertDataQueryList);
 
 		if (updateColumnString.length() != 0)
 		{
 			StringBuffer query = new StringBuffer("UPDATE " + tableName + " SET ");
 			query.append(updateColumnString);
 			query.append(" where ");
 			query.append(IDENTIFIER);
 			query.append(WHITESPACE + EQUAL + WHITESPACE);
 			query.append(recordId);
 			editDataQueryList.add(query.toString());
 		}
 
 		Connection conn = DBUtil.getConnection();
 		for (String queryString : editDataQueryList)
 		{
 			logDebug("editData", "Query is: " + queryString.toString());
 			PreparedStatement statement = conn.prepareStatement(queryString);
 			statement.executeUpdate();
 		}
 
 		for (AttributeRecord collectionAttributeRecord : collectionRecords)
 		{
 			logDebug("editData", "updating multi select: "
 					+ collectionAttributeRecord.getValueCollection());
 			hibernateDAO.update(collectionAttributeRecord, null, false, false, false);
 		}
 
 		for (AttributeRecord collectionAttributeRecord : deleteCollectionRecords)
 		{
 			logDebug("editData", "deleting multi select: "
 					+ collectionAttributeRecord.getValueCollection());
 			hibernateDAO.update(collectionAttributeRecord, null, false, false, false);
 		}
 
 		for (AttributeRecord fileRecord : fileRecords)
 		{
 			logDebug("editData", "updating filereocrd : "
 					+ fileRecord.getFileRecord().getFileName());
 			if(fileRecord.getId() != null)
 			{
 				hibernateDAO.update(fileRecord, null, false, false, false);
 			}
 			else
 			{
 				hibernateDAO.insert(fileRecord, null, false, false);
 			}
 		}
 
 		for (AttributeRecord objectRecord : objectRecords)
 		{
 			logDebug("editData", "updating object : "
 					+ objectRecord.getObjectRecord().getClassName());
 			hibernateDAO.update(objectRecord, null, false, false, false);
 		}
 
 		return true;
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#editData(edu.common.dynamicextensions.domaininterface.EntityInterface, java.util.Map, java.lang.Long)
 	 */
 	public boolean editData(EntityInterface entity, Map<AbstractAttributeInterface, ?> dataValue,
 			Long recordId) throws DynamicExtensionsApplicationException,
 			DynamicExtensionsSystemException
 	{
 
 		boolean isSuccess = false;
 
 		HibernateDAO hibernateDAO = null;
 		try
 		{
 
 			DAOFactory factory = DAOFactory.getInstance();
 			hibernateDAO = (HibernateDAO) factory.getDAO(Constants.HIBERNATE_DAO);
 
 			hibernateDAO.openSession(null);
 			List<EntityInterface> entityList = getParentEntityList(entity);
 			Map<EntityInterface, Map> entityValueMap = initialiseEntityValueMap(entity, dataValue);
 			for (EntityInterface entityInterface : entityList)
 			{
 				Map valueMap = entityValueMap.get(entityInterface);
 				isSuccess = editDataForSingleEntity(entityInterface, valueMap, recordId,
 						hibernateDAO);
 			}
 			hibernateDAO.commit();
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			throw (DynamicExtensionsApplicationException) handleRollback(e,
 					"Error while inserting data", hibernateDAO, false);
 		}
 		catch (Exception e)
 		{
 			throw (DynamicExtensionsSystemException) handleRollback(e, "Error while updating",
 					hibernateDAO, true);
 		}
 		finally
 		{
 			try
 			{
 				hibernateDAO.closeSession();
 			}
 			catch (DAOException e)
 			{
 				throw (DynamicExtensionsSystemException) handleRollback(e, "Error while closing",
 						hibernateDAO, true);
 			}
 
 		}
 
 		return isSuccess;
 	}
 
 	/**
 	 * This method returns a list of AttributeRecord that for a particular multiselect attribute of
 	 * the entity.
 	 * @param collectionRecord
 	 *
 	 * @param entity entity for which data has been entered.
 	 * @param primitiveAttribute attribute for which data has been entered.
 	 * @param identifier id of the record
 	 * @param values List of values for this multiselect attribute
 	 * @return  list of AttributeRecord
 	 */
 	private AttributeRecord populateCollectionAttributeRecord(AttributeRecord collectionRecord,
 			EntityInterface entity, AttributeInterface primitiveAttribute, Long identifier,
 			List<String> values)
 	{
 		if (collectionRecord == null)
 		{
 			collectionRecord = new AttributeRecord();
 			collectionRecord.setValueCollection(new HashSet<CollectionAttributeRecordValue>());
 		}
 		else
 		{
 			collectionRecord.getValueCollection().clear();
 		}
 		Collection<CollectionAttributeRecordValue> valueCollection = collectionRecord
 				.getValueCollection();
 
 		collectionRecord.setEntity(entity);
 		collectionRecord.setAttribute(primitiveAttribute);
 		collectionRecord.setRecordId(identifier);
 		for (String value : values)
 		{
 			CollectionAttributeRecordValue collectionAttributeRecordValue = new CollectionAttributeRecordValue();
 			collectionAttributeRecordValue.setValue(value);
 			valueCollection.add(collectionAttributeRecordValue);
 		}
 
 		return collectionRecord;
 	}
 
 	/**
 	 * Populates AttributeRecord object for given entity and record id
 	 *
 	 * @param fileRecord if null creates a new AttributeRecord objec t, otheerwise updates the existing one
 	 * @param entity for which this AttributeRecord object belongs
 	 * @param primitiveAttribute  for which this AttributeRecord object belongs
 	 * @param identifier for which this AttributeRecord object belongs
 	 * @param value the new values for the file type attribute
 	 * @return
 	 */
 	private AttributeRecord populateFileAttributeRecord(AttributeRecord fileRecord,
 			EntityInterface entity, AttributeInterface primitiveAttribute, Long identifier,
 			FileAttributeRecordValue value)
 	{
 		if (fileRecord == null)
 		{
 			fileRecord = new AttributeRecord();
 		}
 		FileAttributeRecordValue fileRecordValue = (FileAttributeRecordValue) value;
 
 		fileRecord.setFileRecord(fileRecordValue);
 		fileRecord.setEntity(entity);
 		fileRecord.setAttribute(primitiveAttribute);
 		fileRecord.setRecordId(identifier);
 
 		return fileRecord;
 	}
 
 	private AttributeRecord populateObjectAttributeRecord(AttributeRecord objectRecord,
 			EntityInterface entity, AttributeInterface primitiveAttribute, Long identifier,
 			ObjectAttributeRecordValue value)
 	{
 		if (objectRecord == null)
 		{
 			objectRecord = new AttributeRecord();
 		}
 		ObjectAttributeRecordValue objectRecordValue = (ObjectAttributeRecordValue) value;
 		objectRecord.setObjectRecord(objectRecordValue);
 		objectRecord.setEntity(entity);
 		objectRecord.setAttribute(primitiveAttribute);
 		objectRecord.setRecordId(identifier);
 
 		return objectRecord;
 	}
 
 	/**
 	 * This method is used by create as well as edit entity methods. This method holds all the common part
 	 * related to saving the entity into the database and also handling the exceptions .
 	 * @param entityInterface Entity to be stored in the database.
 	 * @param isNew flag for whether it is a save or update.
 	 * @param hibernateDAO
 	 * @param processedEntityList
 	 * @param addIdAttribute
 	 * @param isNewFlag
 	 * @return Entity . Stored instance of the entity.
 	 * @throws DynamicExtensionsApplicationException System exception in case of any fatal errors.
 	 * @throws DynamicExtensionsSystemException Thrown in case of duplicate name or authentication failure.
 	 * @throws DAOException
 	 * @throws HibernateException
 	 */
 	private EntityInterface saveOrUpdateEntity(EntityInterface entityInterface,
 			HibernateDAO hibernateDAO, Stack rollbackQueryStack, boolean isEntitySaved,
 			List<EntityInterface> processedEntityList, boolean addIdAttribute,
 			boolean isEntityFromXMI,boolean copyDataTableState) throws DynamicExtensionsApplicationException,
 			DynamicExtensionsSystemException, DAOException, HibernateException
 	{
 		logDebug("saveOrUpdateEntity", "Entering method");
 
 		Entity entity = (Entity) entityInterface;
 
 		if (isEntityFromXMI)
 		{
 			boolean isEntityProcessed = isEntityProcessed(processedEntityList, entity);
 			if (isEntityProcessed)
 			{
 				return entity;
 			}
 			else
 			{
 				processedEntityList.add(entity);
 			}
 		}
 		else
 		{
 			if (processedEntityList.contains(entity))
 			{
 				return entity;
 			}
 			else
 			{
 				processedEntityList.add(entity);
 			}
 		}
 
 		//		if (entity.getParentEntity() != null && entity.getParentEntity().getId() == null)
 		//		{
 		//			throw new DynamicExtensionsApplicationException("Unsaved Parent not allowed", null,
 		//					DYEXTN_A_011);
 		//		}
 		List reverseQueryList = new LinkedList();
 		List queryList = null;
 
 		checkForDuplicateEntityName(entity);
 		Entity databaseCopy = null;
 
 		try
 		{
 			Session session = DBUtil.currentSession();
 			if (!isEntitySaved)
 			{
 				if (addIdAttribute)
 				{
 					addIdAttribute(entity);
 				}
 				preSaveProcessEntity(entity);
 			}
 			else
 			{
 				databaseCopy = (Entity) DBUtil.loadCleanObj(Entity.class, entity.getId());
 				if (queryBuilder.isParentChanged(entity, databaseCopy))
 				{
 					checkParentChangeAllowed(entity);
 				}
 			}
 
 			//			if (entity.getParentEntity() != null)
 			//			{
 			//				saveOrUpdateEntity(entity.getParentEntity(), hibernateDAO, rollbackQueryStack,
 			//						true, processedEntityList, addIdAttribute);
 			//			}
 
 			if (entity.getParentEntity() != null)
 			{
 				boolean isParentEntitySaved = false;
 				if (entity.getParentEntity().getId() != null)
 				{
 					isParentEntitySaved = true;
 				}
 
 				saveOrUpdateEntity(entity.getParentEntity(), hibernateDAO, rollbackQueryStack,
 						isParentEntitySaved, processedEntityList, addIdAttribute, isEntityFromXMI,copyDataTableState);
 
 			}
 			//Fixed bug 5619
 			if(entity.getId() != null)
 			{
 				session.update(entity);
 			}
 			else
 			{
 				session.save(entity);
 			}
 			//entity = (Entity) session.saveOrUpdateCopy(entity);
 
 			postSaveProcessEntity(entity, hibernateDAO, rollbackQueryStack, processedEntityList,
 					addIdAttribute, isEntityFromXMI, copyDataTableState);
 
 			entity = (Entity) session.saveOrUpdateCopy(entity);
 
 			if (entity.getDataTableState() == DATA_TABLE_STATE_CREATED)
 			{
 				if (!isEntitySaved)
 				{
 					queryList = queryBuilder.getCreateEntityQueryList(entity, reverseQueryList,
 							hibernateDAO, rollbackQueryStack, addIdAttribute);
 				}
 				else
 				{
 					queryList = queryBuilder.getUpdateEntityQueryList(entity,
 							(Entity) databaseCopy, reverseQueryList);
 				}
 
 				queryBuilder.executeQueries(queryList, reverseQueryList, rollbackQueryStack);
 			}
 		}
 		catch (UserNotAuthorizedException e)
 		{
 			throw new DynamicExtensionsApplicationException(
 					"User is not authorised to perform this action", e, DYEXTN_A_002);
 		}
 
 		return entity;
 	}
 
 	/**
 	 * This method adds a system generated attribute to the entity.
 	 * @param entity
 	 */
 	private void addIdAttribute(EntityInterface entity)
 	{
 		DomainObjectFactory domainObjectFactory = DomainObjectFactory.getInstance();
 		AttributeInterface idAttribute = domainObjectFactory.createLongAttribute();
 		idAttribute.setName(ID_ATTRIBUTE_NAME);
 		idAttribute.setIsPrimaryKey(new Boolean(true));
 		idAttribute.setIsNullable(new Boolean(false));
 		ColumnPropertiesInterface column = domainObjectFactory.createColumnProperties();
 		column.setName(IDENTIFIER);
 		idAttribute.setColumnProperties(column);
 		entity.addAttribute(idAttribute);
 		idAttribute.setEntity(entity);
 	}
 
 	/**
 	 * This method is used by create as well as edit entity methods. This method holds all the common part
 	 * related to saving the entity into the database and also handling the exceptions .
 	 * @param entityInterface Entity to be stored in the database.
 	 * @param isNew flag for whether it is a save or update.
 	 * @param hibernateDAO
 	 * @param processedEntityList
 	 * @param isNewFlag
 	 * @return Entity . Stored instance of the entity.
 	 * @throws DynamicExtensionsApplicationException System exception in case of any fatal errors.
 	 * @throws DynamicExtensionsSystemException Thrown in case of duplicate name or authentication failure.
 	 * @throws DAOException
 	 * @throws HibernateException
 	 */
 	private EntityInterface saveOrUpdateEntityMetadata(EntityInterface entityInterface,
 			HibernateDAO hibernateDAO, Stack rollbackQueryStack, boolean isEntitySaved,
 			List<EntityInterface> processedEntityList, boolean copyDataTableState)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException,
 			DAOException, HibernateException
 	{
 		logDebug("saveOrUpdateEntity", "Entering method");
 
 		Entity entity = (Entity) entityInterface;
 		if (processedEntityList.contains(entity))
 		{
 			return entity;
 		}
 		else
 		{
 			processedEntityList.add(entity);
 		}
 		checkForDuplicateEntityName(entity);
 		//Entity databaseCopy = null;
 		try
 		{
 			Session session = DBUtil.currentSession();
 			Long start = null, end = null;
 			if (!isEntitySaved)
 			{
 				preSaveProcessEntity(entity);
 				EntityInterface parentEntity = entity.getParentEntity();
 				if (parentEntity != null)
 				{
 					boolean isParentEntitySaved = false;
 					if (parentEntity.getId() != null)
 					{
 						isParentEntitySaved = true;
 					}
 					saveOrUpdateEntityMetadata(parentEntity, hibernateDAO, rollbackQueryStack,
 							isParentEntitySaved, processedEntityList, copyDataTableState);
 				}
 				hibernateDAO.insert(entity, null, false, false);
 			}
 			else
 			{
 				//databaseCopy = (Entity) DBUtil.loadCleanObj(Entity.class, entity.getId());
 				Long databaseParentId = getOriginalParentId(entity.getId());
 				if (entity.getDataTableState() == DATA_TABLE_STATE_CREATED
 						&& queryBuilder.isParentChanged(entity, databaseParentId))
 				{
 					checkParentChangeAllowed(entity);
 				}
 				if (entity.getParentEntity() != null)
 				{
 					saveOrUpdateEntityMetadata(entity.getParentEntity(), hibernateDAO,
 							rollbackQueryStack, true, processedEntityList, copyDataTableState);
 				}
 				hibernateDAO.update(entity, null, false, false, false);
 			}
 
 			//entity = (Entity) session.saveOrUpdateCopy(entity);
 			//since only metadata is saved
 			postSaveProcessEntity(entity, hibernateDAO, rollbackQueryStack, processedEntityList,
 					false, false, copyDataTableState);
 			//entity = (Entity) session.saveOrUpdateCopy(entity);
 			hibernateDAO.update(entity, null, false, false, false);
 		}
 		catch (UserNotAuthorizedException e)
 		{
 			logDebug("saveOrUpdateEntity", DynamicExtensionsUtility.getStackTrace(e));
 			throw new DynamicExtensionsApplicationException(
 					"User is not authorised to perform this action", e, DYEXTN_A_002);
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			logDebug("saveOrUpdateEntity", DynamicExtensionsUtility.getStackTrace(e));
 			throw e;
 		}
 		catch (Exception e)
 		{
 			logDebug("saveOrUpdateEntity", DynamicExtensionsUtility.getStackTrace(e));
 			throw new DynamicExtensionsSystemException(e.getMessage(), e, DYEXTN_S_001);
 		}
 
 		logDebug("saveOrUpdateEntity", "Exiting Method");
 
 		return entity;//(Entity) getEntityByIdentifier(entity.getId().toString());
 	}
 	/**
 	 * This method is used by create as well as edit entity methods. This method holds all the common part
 	 * related to saving the entity into the database and also handling the exceptions .
 	 * @param entityInterface Entity to be stored in the database.
 	 * @param isNew flag for whether it is a save or update.
 	 * @param hibernateDAO
 	 * @param processedEntityList
 	 * @param isNewFlag
 	 * @return Entity . Stored instance of the entity.
 	 * @throws DynamicExtensionsApplicationException System exception in case of any fatal errors.
 	 * @throws DynamicExtensionsSystemException Thrown in case of duplicate name or authentication failure.
 	 * @throws DAOException
 	 * @throws HibernateException
 	 */
 	private EntityInterface saveOrUpdateEntityMetadataForSingleAnnotation(EntityInterface entityInterface,
 			HibernateDAO hibernateDAO, Stack rollbackQueryStack, boolean isEntitySaved,
 			List<EntityInterface> processedEntityList, boolean copyDataTableState,AssociationInterface association)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException,
 			DAOException, HibernateException
 	{
 		logDebug("saveOrUpdateEntity", "Entering method");
 
 		Entity entity = (Entity) entityInterface;
 		if (processedEntityList.contains(entity))
 		{
 			return entity;
 		}
 		else
 		{
 			processedEntityList.add(entity);
 		}
 		checkForDuplicateEntityName(entity);
 		//Entity databaseCopy = null;
 		try
 		{
 			Session session = DBUtil.currentSession();
 			Long start = null, end = null;
 			if (!isEntitySaved)
 			{
 				preSaveProcessEntity(entity);
 				EntityInterface parentEntity = entity.getParentEntity();
 				if (parentEntity != null)
 				{
 					boolean isParentEntitySaved = false;
 					if (parentEntity.getId() != null)
 					{
 						isParentEntitySaved = true;
 					}
 					saveOrUpdateEntityMetadata(parentEntity, hibernateDAO, rollbackQueryStack,
 							isParentEntitySaved, processedEntityList, copyDataTableState);
 				}
 				hibernateDAO.insert(entity, null, false, false);
 			}
 			else
 			{
 				//databaseCopy = (Entity) DBUtil.loadCleanObj(Entity.class, entity.getId());
 				Long databaseParentId = getOriginalParentId(entity.getId());
 				if (entity.getDataTableState() == DATA_TABLE_STATE_CREATED
 						&& queryBuilder.isParentChanged(entity, databaseParentId))
 				{
 					checkParentChangeAllowed(entity);
 				}
 				if (entity.getParentEntity() != null)
 				{
 					saveOrUpdateEntityMetadata(entity.getParentEntity(), hibernateDAO,
 							rollbackQueryStack, true, processedEntityList, copyDataTableState);
 				}
 				hibernateDAO.update(entity, null, false, false, false);
 			}
 
 			//entity = (Entity) session.saveOrUpdateCopy(entity);
 			//since only metadata is saved
 			postSaveProcessEntityForSingleAnnotation(entity, hibernateDAO, rollbackQueryStack, processedEntityList,
 					false, false, copyDataTableState,association);
 			//entity = (Entity) session.saveOrUpdateCopy(entity);
 			hibernateDAO.update(entity, null, false, false, false);
 		}
 		catch (UserNotAuthorizedException e)
 		{
 			logDebug("saveOrUpdateEntity", DynamicExtensionsUtility.getStackTrace(e));
 			throw new DynamicExtensionsApplicationException(
 					"User is not authorised to perform this action", e, DYEXTN_A_002);
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			logDebug("saveOrUpdateEntity", DynamicExtensionsUtility.getStackTrace(e));
 			throw e;
 		}
 		catch (Exception e)
 		{
 			logDebug("saveOrUpdateEntity", DynamicExtensionsUtility.getStackTrace(e));
 			throw new DynamicExtensionsSystemException(e.getMessage(), e, DYEXTN_S_001);
 		}
 
 		logDebug("saveOrUpdateEntity", "Exiting Method");
 
 		return entity;//(Entity) getEntityByIdentifier(entity.getId().toString());
 	}
 	/**
 	 * @param id
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private Long getOriginalParentId(Long id) throws DynamicExtensionsSystemException
 	{
 		StringBuffer query = new StringBuffer();
 		Long parentId = null;
 		query.append(SELECT_KEYWORD).append(WHITESPACE).append("parent_entity_id").append(
 				WHITESPACE).append(FROM_KEYWORD).append(WHITESPACE).append("DYEXTN_ENTITY").append(
 				WHITESPACE).append(WHERE_KEYWORD).append(IDENTIFIER).append(EQUAL).append(
 				id.toString());
 		Object[] obj = queryBuilder.executeDMLQuery(query.toString());
 		if (obj != null)
 		{
 			parentId = (Long) obj[0];
 		}
 		return parentId;
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
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getRecordById(edu.common.dynamicextensions.domaininterface.EntityInterface, java.lang.Long)
 	 * Value in the map depends on the type of the attribute as explaned below.<br>
 	 * Map
 	 *    key    - Attribute Name
 	 *    Value  - List<String> --           multiselect attribute.
 	 *             FileAttributeRecordValue  File attribute.
 	 *             List<Long>                Association
 	 *                  if One-One   |____   List will contain only 1 record id that is of target entity's record
 	 *                     Many-One  |
 	 *                  otherwise it will contains one or more reocrd ids.
 	 *
 	 *             String                    Other attribute type.
 	 */
 	private Map<AbstractAttributeInterface, Object> getEntityRecordById(EntityInterface entity,
 			Long recordId) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		Map<AbstractAttributeInterface, Object> recordValues = new HashMap<AbstractAttributeInterface, Object>();
 
 		Collection attributesCollection = entity.getAttributeCollection();
 		attributesCollection = entityManagerUtil.filterSystemAttributes(attributesCollection);
 		List<AttributeInterface> collectionAttributes = new ArrayList<AttributeInterface>();
 		List<AttributeInterface> fileAttributes = new ArrayList<AttributeInterface>();
 		List<AttributeInterface> objectAttributes = new ArrayList<AttributeInterface>();
 
 		String tableName = entity.getTableProperties().getName();
 		List<String> selectColumnNameList = new ArrayList<String>();
 
 		Iterator attriIterator = attributesCollection.iterator();
 		Map columnNameMap = new HashMap();
 		while (attriIterator.hasNext())
 		{
 			AttributeInterface attribute = (AttributeInterface) attriIterator.next();
 
 			if (attribute.getIsCollection())
 			{ // need to fetch AttributeRecord object for the multi select type attribute.
 				collectionAttributes.add(attribute);
 			}
 			else if (attribute.getAttributeTypeInformation() instanceof FileAttributeTypeInformation)
 			{
 				// need to fetch AttributeRecord object for the File type attribute.
 				fileAttributes.add(attribute);
 			}
 			else if (attribute.getAttributeTypeInformation() instanceof ObjectAttributeTypeInformation)
 			{
 				// need to fetch AttributeRecord object for the File type attribute.
 				objectAttributes.add(attribute);
 			}
 			else
 			{
 				//for the other attributes, create select query.
 
 				String dbColumnName = attribute.getColumnProperties().getName();
 				selectColumnNameList.add(dbColumnName);
 				columnNameMap.put(dbColumnName, attribute);
 			}
 		}
 
 		//get association values.
 		recordValues.putAll(queryBuilder.getAssociationGetRecordQueryList(entity, recordId));
 
 		try
 		{
 
 			if (!selectColumnNameList.isEmpty())
 			{
 
 				StringBuffer query = new StringBuffer();
 				query.append(SELECT_KEYWORD).append(WHITESPACE);
 
 				for (int i = 0; i < selectColumnNameList.size(); i++)
 				{
 					if (i != 0)
 					{
 						query.append(" , ");
 					}
 					query.append(selectColumnNameList.get(i));
 				}
 
 				query.append(WHITESPACE).append(FROM_KEYWORD).append(WHITESPACE).append(tableName)
 						.append(WHITESPACE).append(WHERE_KEYWORD).append(WHITESPACE).append(
 								IDENTIFIER).append(EQUAL).append(recordId);
 				/*get values for simple attributes*/
 
 				recordValues.putAll(getAttributeValues(selectColumnNameList, query.toString(),
 						columnNameMap));
 			}
 
 			/*
 			 * process any multi select attributes
 			 */
 			for (AttributeInterface attribute : collectionAttributes)
 			{
 				List<String> valueList = getCollectionAttributeRecordValues(entity.getId(),
 						attribute.getId(), recordId);
 				//put the value multi select attributes
 				recordValues.put(attribute, valueList);
 			}
 			/*
 			 * process any file type attributes
 			 */
 			for (AttributeInterface attribute : fileAttributes)
 			{
 				FileAttributeRecordValue fileRecordValue = getFileAttributeRecordValue(entity
 						.getId(), attribute.getId(), recordId);
 				//put the value file attributes
 				recordValues.put(attribute, fileRecordValue);
 			}
 
 			/*
 			 * process any file type attributes
 			 */
 			for (AttributeInterface attribute : objectAttributes)
 			{
 				ObjectAttributeRecordValueInterface objectRecordValue = getObjectAttributeRecordValue(
 						entity.getId(), attribute.getId(), recordId);
 				//put the value file attributes
 				recordValues.put(attribute, objectRecordValue);
 			}
 
 		}
 		catch (SQLException e)
 		{
 			throw new DynamicExtensionsSystemException("Error while retrieving the data", e);
 		}
 
 		return recordValues;
 	}
 
 	/**
 	 * The method returns the entity records for the given entity, attribute and records.
 	 * @param entity Entity whose records are to be shown
 	 * @param abstractAttributeCollection The set of attributes for which values are to be shown
 	 * @param recordIds Record ids whose values are to be shown
 	 * @return EntityRecordResultInterface Object containing the result.
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public EntityRecordResultInterface getEntityRecords(EntityInterface entity,
 			List<? extends AbstractAttributeInterface> abstractAttributeCollection,
 			List<Long> recordIds) throws DynamicExtensionsSystemException
 	{
 		if (abstractAttributeCollection == null || abstractAttributeCollection.isEmpty())
 		{
 			return null;
 		}
 		//		List<AbstractAttributeInterface> tempAbstractAttributeCollection = new ArrayList(
 		//				abstractAttributeCollection);
 		//		tempAbstractAttributeCollection = EntityManagerUtil
 		//				.filterSystemAttributes(tempAbstractAttributeCollection);
 
 		//Initialising entityRecord and entityRecordMetadata
 
 		EntityRecordResultInterface entityRecordResult = new EntityRecordResult();
 		EntityRecordMetadata recordMetadata = new EntityRecordMetadata();
 		recordMetadata.setAttributeList(abstractAttributeCollection);
 		entityRecordResult.setEntityRecordMetadata(recordMetadata);
 
 		recordMetadata.setAttributeList(abstractAttributeCollection);
 
 		//Filtering abstract attributes into attribtute and association
 		List<AssociationInterface> associationCollection = new ArrayList<AssociationInterface>();
 		List<AttributeInterface> attributesCollection = new ArrayList<AttributeInterface>();
 		filterAttributes(abstractAttributeCollection, attributesCollection, associationCollection);
 		//		attributesCollection = EntityManagerUtil.filterSystemAttributes(attributesCollection);
 
 		//Initialising collection for file attributes and collection attributes.
 		List<AttributeInterface> collectionAttributes = new ArrayList<AttributeInterface>();
 		List<AttributeInterface> fileAttributes = new ArrayList<AttributeInterface>();
 		List<AttributeInterface> objectAttributes = new ArrayList<AttributeInterface>();
 
 		String tableName = entity.getTableProperties().getName();
 		List<String> selectColumnNameList = new ArrayList<String>();
 
 		Iterator attriIterator = attributesCollection.iterator();
 		Map columnNameMap = new HashMap();
 
 		while (attriIterator.hasNext())
 		{
 			AttributeInterface attribute = (AttributeInterface) attriIterator.next();
 			//Filtering attributes into primitive attributes and collection attributes and file attributes
 			if (attribute.getIsCollection())
 			{ // need to fetch AttributeRecord object for the multi select type attribute.
 				collectionAttributes.add(attribute);
 			}
 			else if (attribute.getAttributeTypeInformation() instanceof FileAttributeTypeInformation)
 			{
 				// need to fetch AttributeRecord object for the File type attribute.
 				fileAttributes.add(attribute);
 			}
 			else if (attribute.getAttributeTypeInformation() instanceof ObjectAttributeTypeInformation)
 			{
 				// need to fetch AttributeRecord object for the File type attribute.
 				objectAttributes.add(attribute);
 			}
 			else
 			{
 				//for the other attributes, create select query.
 				String dbColumnName = attribute.getColumnProperties().getName();
 				selectColumnNameList.add(dbColumnName);
 				columnNameMap.put(dbColumnName, attribute);
 			}
 		}
 		try
 		{
 			//Processing primitive attributes
 			StringBuffer query = new StringBuffer();
 			query.append(SELECT_KEYWORD).append(IDENTIFIER).append(WHITESPACE);
 
 			for (int i = 0; i < selectColumnNameList.size(); i++)
 			{
 				query.append(" , ");
 				query.append(selectColumnNameList.get(i));
 			}
 
 			query.append(WHITESPACE).append(FROM_KEYWORD).append(tableName);
 
 			if (recordIds != null && !recordIds.isEmpty())
 			{
 				query.append(WHERE_KEYWORD).append(IDENTIFIER).append(IN_KEYWORD).append(
 						EntityManagerUtil.getListToString(recordIds));
 			}
 
 			/*get values for simple attributes*/
 
 			List<EntityRecordInterface> entityRecordList = getEntityRecordList(
 					selectColumnNameList, query.toString(), columnNameMap, recordMetadata);
 
 			entityRecordResult.setEntityRecordList(entityRecordList);
 			/*
 			 * process any multi select attributes
 			 */
 
 			for (AttributeInterface attribute : collectionAttributes)
 			{
 				for (EntityRecordInterface entityRecord : entityRecordList)
 				{
 					Long recordId = entityRecord.getRecordId();
 					List<String> valueList = getCollectionAttributeRecordValues(entity.getId(),
 							attribute.getId(), recordId);
 					int index = abstractAttributeCollection.indexOf(attribute);
 					entityRecord.getRecordValueList().set(index, valueList);
 				}
 
 			}
 			for (AttributeInterface attribute : fileAttributes)
 			{
 
 				for (EntityRecordInterface entityRecord : entityRecordList)
 				{
 					Long recordId = entityRecord.getRecordId();
 					FileAttributeRecordValue fileRecordValue = getFileAttributeRecordValue(entity
 							.getId(), attribute.getId(), recordId);
 					int index = abstractAttributeCollection.indexOf(attribute);
 					entityRecord.getRecordValueList().set(index, fileRecordValue);
 				}
 
 			}
 
 			for (AttributeInterface attribute : objectAttributes)
 			{
 
 				for (EntityRecordInterface entityRecord : entityRecordList)
 				{
 					Long recordId = entityRecord.getRecordId();
 					ObjectAttributeRecordValueInterface objectRecordValue = getObjectAttributeRecordValue(
 							entity.getId(), attribute.getId(), recordId);
 					int index = abstractAttributeCollection.indexOf(attribute);
 					entityRecord.getRecordValueList().set(index, objectRecordValue);
 				}
 
 			}
 
 			for (EntityRecordInterface entityRecord : entityRecordList)
 			{
 				Long recordId = entityRecord.getRecordId();
 				queryBuilder.putAssociationValues(associationCollection, entityRecordResult,
 						entityRecord, recordId);
 			}
 
 		}
 		catch (SQLException e)
 		{
 			throw new DynamicExtensionsSystemException("Error while retrieving the data", e);
 		}
 
 		return entityRecordResult;
 	}
 
 	/**
 	 * filers abstractAttributes into attributes and associations
 	 * @param abstractAttributeCollection
 	 * @param attributesCollection
 	 * @param associationCollection
 	 */
 	private void filterAttributes(
 			List<? extends AbstractAttributeInterface> abstractAttributeCollection,
 			Collection<AttributeInterface> attributesCollection,
 			Collection<AssociationInterface> associationCollection)
 	{
 		for (AbstractAttributeInterface abstractAttributeInterface : abstractAttributeCollection)
 		{
 			if (abstractAttributeInterface instanceof AssociationInterface)
 			{
 				associationCollection.add((AssociationInterface) abstractAttributeInterface);
 			}
 			else
 			{
 				attributesCollection.add((AttributeInterface) abstractAttributeInterface);
 			}
 		}
 
 	}
 
 	/**
 	 * @param selectColumnNameList
 	 * @param query
 	 * @param columnNameMap
 	 * @param multipleRows
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws SQLException
 	 */
 	private Map<AbstractAttributeInterface, Object> getAttributeValues(
 			List<String> selectColumnNameList, String query, Map columnNameMap)
 			throws DynamicExtensionsSystemException, SQLException
 	{
 		Map<AbstractAttributeInterface, Object> recordValues = new HashMap<AbstractAttributeInterface, Object>();
 		ResultSet resultSet = entityManagerUtil.executeQuery(query);
 
 		if (resultSet.next())
 		{
 			for (int i = 0; i < selectColumnNameList.size(); i++)
 			{
 
 				String dbColumnName = selectColumnNameList.get(i);
 				String value = getValueFromResultSet(resultSet, columnNameMap, dbColumnName, i);
 				Attribute attribute = (Attribute) columnNameMap.get(dbColumnName);
 				recordValues.put(attribute, value);
 			}
 		}
 		resultSet.close();
 		return recordValues;
 	}
 
 	/**
 	 * @param selectColumnNameList
 	 * @param query
 	 * @param columnNameMap
 	 * @param recordMetadata
 	 * @param multipleRows
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws SQLException
 	 */
 	private List<EntityRecordInterface> getEntityRecordList(List<String> selectColumnNameList,
 			String query, Map columnNameMap, EntityRecordMetadata recordMetadata)
 			throws DynamicExtensionsSystemException, SQLException
 	{
 		ResultSet resultSet = entityManagerUtil.executeQuery(query);
 		List<EntityRecordInterface> entityRecordList = new ArrayList<EntityRecordInterface>();
 
 		while (resultSet.next())
 		{
 			EntityRecordInterface entityRecord = new EntityRecord();
 			Long id = resultSet.getLong(1);
 			entityRecord.setRecordId(id);
 			Object[] values = new Object[recordMetadata.getAttributeList().size()];
 			for (int i = 1; i <= selectColumnNameList.size(); i++)
 			{
 				String dbColumnName = selectColumnNameList.get(i - 1);
 				String value = getValueFromResultSet(resultSet, columnNameMap, dbColumnName, i);
 				AttributeInterface attribute = (AttributeInterface) columnNameMap.get(dbColumnName);
 				int indexOfAttribute = recordMetadata.getAttributeList().indexOf(attribute);
 				values[indexOfAttribute] = value;
 			}
 			entityRecord.setRecordValueList(Arrays.asList(values));
 			entityRecordList.add(entityRecord);
 		}
 		resultSet.close();
 		return entityRecordList;
 	}
 
 	private String getValueFromResultSet(ResultSet resultSet, Map columnNameMap,
 			String dbColumnName, int index) throws SQLException
 	{
 		Attribute attribute = (Attribute) columnNameMap.get(dbColumnName);
 
 		Object valueObj = resultSet.getObject(index + 1);
 		String value = "";
 
 		if (valueObj != null)
 		{
 			if (valueObj instanceof java.util.Date)
 			{
 
 				DateAttributeTypeInformation dateAttributeTypeInf = (DateAttributeTypeInformation) attribute
 						.getAttributeTypeInformation();
 
 				String format = dateAttributeTypeInf.getFormat();
 				if (format == null)
 				{
 					format = Constants.DATE_ONLY_FORMAT;
 				}
 
 				valueObj = resultSet.getTimestamp(index + 1);
 
 				SimpleDateFormat formatter = new SimpleDateFormat(format);
 				value = formatter.format((java.util.Date) valueObj);
 			}
 			else
 			{
 				value = valueObj.toString();
 			}
 		}
 		return value;
 	}
 
 	/**
 	 * This method retrives the data for given entity for given record. It also returns the values of
 	 * any inherited attributes.
 	 *
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getRecordById(edu.common.dynamicextensions.domaininterface.EntityInterface, java.lang.Long)
 	 */
 	public Map<AbstractAttributeInterface, Object> getRecordById(EntityInterface entity,
 			Long recordId) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 
 		if (entity == null || entity.getId() == null || recordId == null)
 		{
 			throw new DynamicExtensionsSystemException("Invalid Input");
 		}
 
 		Map<AbstractAttributeInterface, Object> recordValues = new HashMap<AbstractAttributeInterface, Object>();
 
 		do
 		{
 			Map<AbstractAttributeInterface, Object> recordValuesForSingleEntity = getEntityRecordById(
 					entity, recordId);
 			recordValues.putAll(recordValuesForSingleEntity);
 			entity = entity.getParentEntity();
 		}
 		while (entity != null);
 
 		return recordValues;
 	}
 
 	/**
 	 *
 	 * @param attributeInterface
 	 * @param recordId
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public FileAttributeRecordValue getFileAttributeRecordValueByRecordId(
 			AttributeInterface attribute, Long recordId) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		EntityInterface entity = attribute.getEntity();
 		FileAttributeRecordValue fileRecordValue = getFileAttributeRecordValue(entity.getId(),
 				attribute.getId(), recordId);
 		return fileRecordValue;
 	}
 
 	/**
 	 * processes entity group before saving.
 	 * @param entity entity
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private void preSaveProcessEntityGroup(EntityGroupInterface entityGroup)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 
 		DynamicExtensionsUtility.validateName(entityGroup.getName());
 		checkForDuplicateEntityGroupName(entityGroup);
 		if (entityGroup.getId() != null)
 		{
 			entityGroup.setLastUpdated(new Date());
 		}
 		else
 		{
 			entityGroup.setCreatedDate(new Date());
 			entityGroup.setLastUpdated(entityGroup.getCreatedDate());
 		}
 	}
 
 	/**
 	 * This method is used by create as well as edit entity group. This method holds all the common part
 	 * related to saving the entity group into the database and also handling the exceptions .
 	 * @param entityGroupInterface EntityGroupInterface  to be stored in the database.
 	 * @param isNew flag for whether it is a save or update.
 	 * @return EntityGroup . Stored instance of the entity group.
 	 * @throws DynamicExtensionsApplicationException System exception in case of any fatal errors.
 	 * @throws DynamicExtensionsSystemException Thrown in case of duplicate name or authentication failure.
 	 */
 	private EntityGroup saveOrUpdateEntityGroup(EntityGroupInterface entityGroupInterface,
 			boolean isNew, boolean isOnlyMetadata, boolean copyDataTableState) throws DynamicExtensionsApplicationException,
 			DynamicExtensionsSystemException
 	{
 		logDebug("saveOrUpdateEntityGroup", "Entering method");
 		EntityGroup entityGroup = (EntityGroup) entityGroupInterface;
 		HibernateDAO hibernateDAO = (HibernateDAO) DAOFactory.getInstance().getDAO(
 				Constants.HIBERNATE_DAO);
 		Stack stack = new Stack();
 		EntityInterface entityInterface = null;
 		try
 		{
 
 			if (isNew)
 			{
 				hibernateDAO.openSession(null);
 				hibernateDAO.insert(entityGroup, null, false, false);
 			}
 			else
 			{
 				Long id = entityGroup.getId();
 				hibernateDAO.openSession(null);
 				hibernateDAO.update(entityGroup, null, false, false, false);
 			}
 			Collection<EntityInterface> entityCollection = entityGroup.getEntityCollection();
 			if (entityCollection != null && !entityCollection.isEmpty())
 			{
 				List<EntityInterface> processedEntityList = new ArrayList<EntityInterface>();
 				for (EntityInterface entity : entityCollection)
 				{
 					entityInterface = entity;
 					boolean isEntitySaved = false;
 					if (entityInterface.getId() != null)
 					{
 						isEntitySaved = true;
 					}
 					if (isOnlyMetadata)
 					{
 						saveOrUpdateEntityMetadata(entityInterface, hibernateDAO, stack,
 								isEntitySaved, processedEntityList, copyDataTableState);
 					}
 					else
 					{
 						saveOrUpdateEntity(entityInterface, hibernateDAO, stack, isEntitySaved,
 								processedEntityList, false, false, copyDataTableState);
 					}
 				}
 			}
 			hibernateDAO.commit();
 		}
 		catch (Exception e)
 		{
 			//			Queries for data table creation and modification are fired in the method saveOrUpdateEntity. So if there
 			//is any exception while storing the metadata , we need to roll back the queries that were fired. So
 			//calling the following method to do that.
 			rollbackQueries(stack, (Entity) entityInterface, e, hibernateDAO);
 			if (e instanceof DynamicExtensionsApplicationException)
 			{
 				throw (DynamicExtensionsApplicationException) e;
 			}
 			else
 			{
 				throw new DynamicExtensionsSystemException(e.getMessage(), e);
 			}
 
 		}
 		finally
 		{
 			try
 			{
 				postSaveOrUpdateEntity(entityInterface);
 				hibernateDAO.closeSession();
 
 			}
 			catch (DAOException e)
 			{
 				throw new DynamicExtensionsSystemException(
 						"Exception occured while closing the session", e, DYEXTN_S_001);
 			}
 
 		}
 		logDebug("saveOrUpdateEntity", "Exiting Method");
 		return entityGroup;
 	}
 	/**
 	 * This method is used by create as well as edit entity group. This method holds all the common part
 	 * related to saving the entity group into the database and also handling the exceptions .
 	 * @param entityGroupInterface EntityGroupInterface  to be stored in the database.
 	 * @param isNew flag for whether it is a save or update.
 	 * @return EntityGroup . Stored instance of the entity group.
 	 * @throws DynamicExtensionsApplicationException System exception in case of any fatal errors.
 	 * @throws DynamicExtensionsSystemException Thrown in case of duplicate name or authentication failure.
 	 */
 	private EntityGroup saveOrUpdateEntityGroupAndContainers(EntityGroupInterface entityGroupInterface,
 			boolean isNew, boolean isOnlyMetadata, Collection<ContainerInterface> containerColl,boolean copyDataTableState) throws DynamicExtensionsApplicationException,
 			DynamicExtensionsSystemException
 	{
 		logDebug("saveOrUpdateEntityGroup", "Entering method");
 		EntityGroup entityGroup = (EntityGroup) entityGroupInterface;
 		HibernateDAO hibernateDAO = (HibernateDAO) DAOFactory.getInstance().getDAO(
 				Constants.HIBERNATE_DAO);
 		Stack stack = new Stack();
 		EntityInterface entityInterface = null;
 		try
 		{
 			if (isNew)
 			{
 				hibernateDAO.openSession(null);
 				hibernateDAO.insert(entityGroup, null, false, false);
 			}
 			else
 			{
 				hibernateDAO.openSession(null);
 				hibernateDAO.update(entityGroup, null, false, false, false);
 			}
 			Collection<EntityInterface> entityCollection = entityGroup.getEntityCollection();
 			if (entityCollection != null && !entityCollection.isEmpty())
 			{
 				List<EntityInterface> processedEntityList = new ArrayList<EntityInterface>();
 				for (EntityInterface entity : entityCollection)
 				{
 					entityInterface = entity;
 					boolean isEntitySaved = false;
 					if (entityInterface.getId() != null)
 					{
 						isEntitySaved = true;
 					}
 					if (isOnlyMetadata)
 					{
 						saveOrUpdateEntityMetadata(entityInterface, hibernateDAO, stack,
 								isEntitySaved, processedEntityList, copyDataTableState);
 					}
 					else
 					{
 						saveOrUpdateEntity(entityInterface, hibernateDAO, stack, isEntitySaved,
								processedEntityList, false, false, copyDataTableState);
 					}
 				}
 			}
 			//For containers
 			if(containerColl != null && !containerColl.isEmpty())
 			{
 				List<ContainerInterface> processedContainerList = new ArrayList<ContainerInterface>();
 				for (ContainerInterface container : containerColl)
 				{
 					persistContainer(container,	false, hibernateDAO, processedContainerList,entityGroupInterface);
 				}
 			}
 
 			hibernateDAO.commit();
 		}
 		catch (Exception e)
 		{
 			//			Queries for data table creation and modification are fired in the method saveOrUpdateEntity. So if there
 			//is any exception while storing the metadata , we need to roll back the queries that were fired. So
 			//calling the following method to do that.
 			rollbackQueries(stack, (Entity) entityInterface, e, hibernateDAO);
 			if (e instanceof DynamicExtensionsApplicationException)
 			{
 				throw (DynamicExtensionsApplicationException) e;
 			}
 			else
 			{
 				throw new DynamicExtensionsSystemException(e.getMessage(), e);
 			}
 		}
 		finally
 		{
 			try
 			{
 				postSaveOrUpdateEntity(entityInterface);
 				hibernateDAO.closeSession();
 			}
 			catch (DAOException e)
 			{
 				throw new DynamicExtensionsSystemException(
 						"Exception occured while closing the session", e, DYEXTN_S_001);
 			}
 		}
 		logDebug("saveOrUpdateEntity", "Exiting Method");
 		return entityGroup;
 	}
 
 	/**
 	 * This method checks if the entity group can be created with the given name or not.
 	 * This method will check for the duplicate name as per the following rule
 	 * @param entityGroup Entity Group whose name's uniqueness is to be checked.
 	 * @throws DynamicExtensionsApplicationException This will basically act as a duplicate name exception.
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public void checkForDuplicateEntityGroupName(EntityGroupInterface entityGroup)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		//Map substitutionParameterMap = new HashMap();
 		//substitutionParameterMap.put("0", new HQLPlaceHolderObject("string", entityGroup.getName()));
 		//Collection collection = executeHQL("checkDuplicateGroupName", substitutionParameterMap);
 
 		Connection conn;
 		try
 		{
 			JDBCDAO dao = (JDBCDAO) DAOFactory.getInstance().getDAO(Constants.JDBC_DAO);
 			dao.openSession(null);
 
 			String query = "select count(*) from dyextn_abstract_metadata d , dyextn_entity_group e where d.identifier = e.identifier and d.name = '"
 					+ entityGroup.getName() + "'";
 			List result = dao.executeQuery(query, new SessionDataBean(), false, null);
 
 			if (result != null && !result.isEmpty())
 			{
 				List count = (List) result.get(0);
 				if (count != null && !count.isEmpty())
 				{
 					int numberOfOccurence = new Integer((String) count.get(0)).intValue();
 					if (numberOfOccurence > 0)
 					{
 						throw new DynamicExtensionsApplicationException(
 								"Duplicate Entity Group name", null, DYEXTN_A_015);
 					}
 				}
 			}
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			// TODO Auto-generated catch block
 			throw e;
 		}
 		catch (Exception e)
 		{
 			// TODO Auto-generated catch block
 			throw new DynamicExtensionsSystemException("Error while checking duplicate count", e);
 		}
 
 		/*if (collection != null && !collection.isEmpty()) {
 		 Integer count = (Integer) collection.iterator().next();
 		 if (count > 0)
 		 {
 		 throw new DynamicExtensionsApplicationException("Duplicate Entity Group name",null, DYEXTN_A_015);
 		 }
 		 }*/
 	}
 
 	/**
 	 *  This method executes the HQL query given the query name and query parameters.
 	 *  The queries are specified in the EntityManagerHQL.hbm.xml file.For each query a name is given.
 	 *  Each query is replaced with parameters before execution.The parametrs are given by each calling method.
 	 * @param entityConceptCode
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private Collection executeHQL(String queryName,
 			Map<String, HQLPlaceHolderObject> substitutionParameterMap)
 			throws DynamicExtensionsSystemException
 	{
 		Collection entityCollection = new HashSet();
 		HibernateDAO hibernateDAO = (HibernateDAO) DAOFactory.getInstance().getDAO(
 				Constants.HIBERNATE_DAO);
 		try
 		{
 			hibernateDAO.openSession(null);
 			Session session = DBUtil.currentSession();
 			Query query = substitutionParameterForQuery(session,queryName, substitutionParameterMap);
 			entityCollection = query.list();
 			//	hibernateDAO.commit();
 		}
 		catch (Exception e)
 		{
 			throw new DynamicExtensionsSystemException("Error while rolling back the session", e);
 		}
 
 
 		finally
 		{
 			try
 			{
 				hibernateDAO.closeSession();
 
 			}
 			catch (DAOException e)
 			{
 				throw new DynamicExtensionsSystemException(
 						"Exception occured while closing the session", e, DYEXTN_S_001);
 			}
 
 		}
 		return entityCollection;
 	}
 	/**
 	 *  This method executes the HQL query given the query name and query parameters.
 	 *  The queries are specified in the EntityManagerHQL.hbm.xml file.For each query a name is given.
 	 *  Each query is replaced with parameters before execution.The parametrs are given by each calling method.
 	 * @param entityConceptCode
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private Collection executeHQLWithCleanSession(String queryName,
 			Map<String, HQLPlaceHolderObject> substitutionParameterMap)
 			throws DynamicExtensionsSystemException
 	{
 		Collection entityCollection = new HashSet();
 		Session	session = null;
 		try
 		{
 			session = DBUtil.getCleanSession();
 			Query query = substitutionParameterForQuery(session,queryName, substitutionParameterMap);
 			entityCollection = query.list();
 
 		}
 		catch (Exception e)
 		{
 			throw new DynamicExtensionsSystemException("Error while rolling back the session", e);
 		}
 		finally
 		{
 			try
 			{
 				session.close();
 			}
 			catch (HibernateException e)
 			{
 				throw new DynamicExtensionsSystemException(
 						"Exception occured while closing the session", e, DYEXTN_S_001);
 			}
 
 		}
 		return entityCollection;
 	}
 	/**
 	 * This method substitues the parameters from substitutionParameterMap into the input query.
 	 * @param substitutionParameterMap
 	 * @throws HibernateException
 	 */
 	private Query substitutionParameterForQuery(Session session,String queryName, Map substitutionParameterMap)
 			throws HibernateException
 	{
 		Query q = session.getNamedQuery(queryName);
 		for (int counter = 0; counter < substitutionParameterMap.size(); counter++)
 		{
 			HQLPlaceHolderObject hPlaceHolderObject = (HQLPlaceHolderObject) substitutionParameterMap
 					.get(counter + "");
 			String objectType = hPlaceHolderObject.getType();
 			if (objectType.equals("string"))
 			{
 				q.setString(counter, hPlaceHolderObject.getValue() + "");
 			}
 			else if (objectType.equals("integer"))
 			{
 				q.setInteger(counter, Integer.parseInt(hPlaceHolderObject.getValue() + ""));
 			}
 			else if (objectType.equals("long"))
 			{
 				q.setLong(counter, Long.parseLong(hPlaceHolderObject.getValue() + ""));
 			}
 			else if (objectType.equals("boolean"))
 			{
 				q.setBoolean(counter, Boolean.parseBoolean(hPlaceHolderObject.getValue() + ""));
 			}
 		}
 		return q;
 
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#deleteRecord(edu.common.dynamicextensions.domaininterface.EntityInterface, java.lang.Long)
 	 */
 	public boolean deleteRecord(EntityInterface entity, Long recordId)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		boolean isRecordDeleted = false;
 
 		queryBuilder.validateForDeleteRecord(entity, recordId, null);
 
 		Collection attributeCollection = entity.getAttributeCollection();
 		//attributeCollection = entityManagerUtil.filterSystemAttributes(attributeCollection);
 		Collection associationCollection = entity.getAssociationCollection();
 		HibernateDAO hibernateDAO = null;
 		DAOFactory factory = DAOFactory.getInstance();
 		hibernateDAO = (HibernateDAO) factory.getDAO(Constants.HIBERNATE_DAO);
 		List associationRemoveQueryList = new ArrayList();
 
 		try
 		{
 
 			hibernateDAO.openSession(null);
 			if (attributeCollection != null && !attributeCollection.isEmpty())
 			{
 				Iterator iterator = attributeCollection.iterator();
 				while (iterator.hasNext())
 				{
 					AttributeInterface attribute = (AttributeInterface) iterator.next();
 					AttributeTypeInformationInterface typeInfo = attribute
 							.getAttributeTypeInformation();
 					// remove AttributeRecord objects for multi select and file type attributes
 					if (attribute.getIsCollection()
 							|| typeInfo instanceof FileAttributeTypeInformation
 							|| typeInfo instanceof ObjectAttributeTypeInformation)
 					{
 						AttributeRecord collectionAttributeRecord = getAttributeRecord(entity
 								.getId(), attribute.getId(), recordId, hibernateDAO);
 						hibernateDAO.delete(collectionAttributeRecord);
 					}
 				}
 			}
 			if (associationCollection != null && !associationCollection.isEmpty())
 			{
 				Iterator iterator = associationCollection.iterator();
 				while (iterator.hasNext())
 				{
 					Association association = (Association) iterator.next();
 
 					if (association.getSourceRole().getAssociationsType().equals(
 							AssociationType.CONTAINTMENT))
 					{
 						List<Long> recordIdList = new ArrayList<Long>();
 						recordIdList.add(recordId);
 						QueryBuilderFactory.getQueryBuilder()
 								.getContenmentAssociationRemoveDataQueryList(association,
 										recordIdList, associationRemoveQueryList, true);
 
 					}
 					//					else
 					//					{
 					//						String associationRemoveQuery = QueryBuilderFactory.getQueryBuilder()
 					//								.getAssociationRemoveDataQuery(association, recordId);
 					//
 					//						associationRemoveQueryList.add(associationRemoveQuery);
 					//
 					//					}
 
 				}
 			}
 			Connection conn = DBUtil.getConnection();
 			StringBuffer query = new StringBuffer();
 			//			query.append(DELETE_KEYWORD + WHITESPACE + entity.getTableProperties().getName()
 			//					+ WHITESPACE + WHERE_KEYWORD + WHITESPACE + IDENTIFIER + WHITESPACE + EQUAL
 			//					+ WHITESPACE + recordId.toString());
 
 			query.append(UPDATE_KEYWORD + WHITESPACE + entity.getTableProperties().getName());
 			query.append(SET_KEYWORD + Constants.ACTIVITY_STATUS_COLUMN + EQUAL + " '"
 					+ Constants.ACTIVITY_STATUS_DISABLED + "' ");
 			query.append(WHERE_KEYWORD + WHITESPACE + IDENTIFIER + WHITESPACE + EQUAL + WHITESPACE
 					+ recordId.toString());
 
 			List<String> deleteRecordQueryList = new ArrayList<String>(associationRemoveQueryList);
 			deleteRecordQueryList.add(0, query.toString());
 			for (String queryString : deleteRecordQueryList)
 			{
 				logDebug("deleteRecord", "QUERY for delete record is : " + queryString.toString());
 				if (queryString != null && queryString.trim().length() != 0)
 				{
 					PreparedStatement statement = conn.prepareStatement(queryString.toString());
 					statement.executeUpdate();
 				}
 			}
 			hibernateDAO.commit();
 			isRecordDeleted = true;
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			try
 			{
 				hibernateDAO.rollback();
 			}
 			catch (DAOException e1)
 			{
 				throw new DynamicExtensionsSystemException(e.getMessage(), e, DYEXTN_S_001);
 			}
 
 			throw e;
 		}
 		catch (Exception e)
 		{
 			try
 			{
 				hibernateDAO.rollback();
 			}
 			catch (DAOException e1)
 			{
 				throw new DynamicExtensionsSystemException(e.getMessage(), e, DYEXTN_S_001);
 			}
 			throw new DynamicExtensionsSystemException(e.getMessage(), e, DYEXTN_S_001);
 		}
 		finally
 		{
 			try
 			{
 				hibernateDAO.closeSession();
 			}
 			catch (DAOException e1)
 			{
 				throw new DynamicExtensionsSystemException(e1.getMessage(), e1, DYEXTN_S_001);
 			}
 		}
 
 		return isRecordDeleted;
 	}
 
 	/**
 	 *
 	 * @param hibernateDAO
 	 * @param queryName
 	 * @param substitutionParameterMap
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private Collection executeHQL(HibernateDAO hibernateDAO, String queryName,
 			Map substitutionParameterMap) throws DynamicExtensionsSystemException
 	{
 		Collection entityCollection = new HashSet();
 
 		try
 		{
 			Session session = DBUtil.currentSession();
 			Query query = substitutionParameterForQuery(session,queryName, substitutionParameterMap);
 			entityCollection = query.list();
 		}
 		catch (HibernateException e)
 		{
 			throw new DynamicExtensionsSystemException(e.getMessage(), e, DYEXTN_S_001);
 		}
 		return entityCollection;
 
 	}
 
 	/**
 	 * Returns all entitiy groups in the whole system
 	 * @return Collection Entity group Collection
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public Collection<EntityGroupInterface> getAllEntitiyGroups()
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		return getAllObjects(EntityGroupInterface.class.getName());
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAllContainersByEntityGroupId(java.lang.Long)
 	 */
 	public Collection<ContainerInterface> getAllContainersByEntityGroupId(Long entityGroupIdentifier)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 
 	{
 		Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", entityGroupIdentifier));
 		return executeHQL("getAllContainersByEntityGroupId", substitutionParameterMap);
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getMainContainer(java.lang.Long)
 	 */
 	public Collection<NameValueBean> getMainContainer(Long entityGroupIdentifier)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", entityGroupIdentifier));
 		return executeHQL("getMainContainers", substitutionParameterMap);
 	}
 
 	/**
 	 * Returns all entitiy groups in the whole system
 	 * @return Collection Entity group Beans Collection
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public Collection<NameValueBean> getAllEntityGroupBeans()
 			throws DynamicExtensionsSystemException
 	{
 
 		Collection<NameValueBean> entityGroupBeansCollection = new ArrayList<NameValueBean>();
 		Collection groupBeansCollection = executeHQL("getAllGroupBeans", new HashMap());
 		Iterator groupBeansIterator = groupBeansCollection.iterator();
 		Object[] objectArray;
 
 		while (groupBeansIterator.hasNext())
 		{
 			objectArray = (Object[]) groupBeansIterator.next();
 			NameValueBean entityGroupNameValue = new NameValueBean();
 			entityGroupNameValue.setName(objectArray[0]);
 			entityGroupNameValue.setValue(objectArray[1]);
 			entityGroupBeansCollection.add(entityGroupNameValue);
 		}
 
 		return entityGroupBeansCollection;
 	}
 
 	/**
 	 * This method returns container interface given the container identifier
 	 * @param identifier
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public ContainerInterface getContainerByIdentifier(String identifier)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		return (ContainerInterface) getObjectByIdentifier(ContainerInterface.class.getName(),
 				identifier);
 	}
 
 	/**
 	 * This method retreives records by executing query of the form
 	 *
 	 select childTable.identifier, childTable.attribute1, parentTable.attribute5
 	 from childTable join parentTable
 	 on childTable.identifier = parentTable.identifier
 	 where childTable.activity_status = "active"
 
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getRecordsForAssociationControl(edu.common.dynamicextensions.domaininterface.userinterface.AssociationControlInterface)
 	 */
 	public Map<Long, List<String>> getRecordsForAssociationControl(
 			AssociationControlInterface associationControl) throws DynamicExtensionsSystemException
 	{
 		Map<Long, List<String>> outputMap = new HashMap<Long, List<String>>();
 		List<String> tableNames = new ArrayList<String>();
 		String tableName;
 		String columnName;
 		String selectClause = SELECT_KEYWORD;
 		String fromClause = FROM_KEYWORD;
 		String onClause = ON_KEYWORD;
 
 		Collection associationAttributesCollection = associationControl
 				.getAssociationDisplayAttributeCollection();
 
 		List associationAttributesList = new ArrayList(associationAttributesCollection);
 		Collections.sort(associationAttributesList);
 
 		Iterator attributeIterator = associationAttributesCollection.iterator();
 		AssociationDisplayAttributeInterface displayAttribute = null;
 
 		while (attributeIterator.hasNext())
 		{
 			displayAttribute = (AssociationDisplayAttributeInterface) attributeIterator.next();
 			columnName = displayAttribute.getAttribute().getColumnProperties().getName();
 			tableName = displayAttribute.getAttribute().getEntity().getTableProperties().getName();
 
 			if (tableNames.size() == 0)
 			{
 				selectClause = selectClause + tableName + "." + IDENTIFIER;
 				fromClause = fromClause + tableName;
 				onClause = onClause + tableName + "." + IDENTIFIER;
 				tableNames.add(tableName);
 			}
 			else
 			{
 				if (tableNames.indexOf(tableName) == -1)
 				{
 					tableNames.add(tableName);
 					fromClause = fromClause + JOIN_KEYWORD + tableName;
 					onClause = onClause + EQUAL + tableName + "." + IDENTIFIER;
 				}
 			}
 			selectClause = selectClause + " , " + columnName;
 		}
 
 		StringBuffer query = new StringBuffer();
 		query.append(selectClause);
 		query.append(fromClause);
 		if (tableNames.size() > 1)
 		{
 			query.append(onClause);
 		}
 		query.append(WHERE_KEYWORD + queryBuilder.getRemoveDisbledRecordsQuery(tableNames.get(0)));
 
 		JDBCDAO jdbcDao = null;
 		try
 		{
 			jdbcDao = (JDBCDAO) DAOFactory.getInstance().getDAO(Constants.JDBC_DAO);
 			jdbcDao.openSession(null);
 
 			List result = jdbcDao
 					.executeQuery(query.toString(), new SessionDataBean(), false, null);
 			if (result != null)
 			{
 				for (int i = 0; i < result.size(); i++)
 				{
 					List innerList = (List) result.get(i);
 					Long recordId = Long.parseLong((String) innerList.get(0));
 					innerList.remove(0);
 					outputMap.put(recordId, innerList);
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			throw new DynamicExtensionsSystemException("Error while retrieving the data", e);
 		}
 		finally
 		{
 			try
 			{
 				jdbcDao.closeSession();
 			}
 			catch (DAOException e)
 			{
 				throw new DynamicExtensionsSystemException("Error while retrieving the data", e);
 			}
 		}
 
 		return outputMap;
 	}
 
 	/**
 	 *
 	 * @param entityGroupInterface
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public Collection<AssociationTreeObject> getAssociationTree()
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Collection associationTreeObjectCollection = new HashSet();
 
 		Collection groupBeansCollection = getAllEntityGroupBeans();
 		Iterator groupBeansIterator = groupBeansCollection.iterator();
 		AssociationTreeObject associationTreeObject;
 
 		while (groupBeansIterator.hasNext())
 		{
 			associationTreeObject = processGroupBean((NameValueBean) groupBeansIterator.next());
 			associationTreeObjectCollection.add(associationTreeObject);
 		}
 
 		return associationTreeObjectCollection;
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
 		AssociationTreeObject associationTreeObjectForGroup = new AssociationTreeObject(new Long(
 				groupBean.getValue()), groupBean.getName());
 
 		Map substitutionParameterMap = new HashMap();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("long",
 				associationTreeObjectForGroup.getId()));
 
 		Collection containersBeansCollection = executeHQL("getAllContainersBeansByEntityGroupId",
 				substitutionParameterMap);
 
 		Iterator containerBeansIterator = containersBeansCollection.iterator();
 		Object[] objectArrayForContainerBeans;
 		AssociationTreeObject associationTreeObjectForContainer;
 
 		while (containerBeansIterator.hasNext())
 		{
 			objectArrayForContainerBeans = (Object[]) containerBeansIterator.next();
 			associationTreeObjectForContainer = new AssociationTreeObject(
 					(Long) objectArrayForContainerBeans[0],
 					(String) objectArrayForContainerBeans[1]);
 			//processForChildContainer(associationTreeObjectForContainer);
 			associationTreeObjectForGroup
 					.addAssociationTreeObject(associationTreeObjectForContainer);
 
 		}
 
 		return associationTreeObjectForGroup;
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAllContainerBeans()
 	 */
 	public List<NameValueBean> getAllContainerBeans() throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
 		Collection containersBeansCollection = executeHQL("getAllContainerBeans",
 				substitutionParameterMap);
 		Iterator containerBeansIterator = containersBeansCollection.iterator();
 		Object[] objectArrayForContainerBeans;
 		List<NameValueBean> list = new ArrayList<NameValueBean>();
 		while (containerBeansIterator.hasNext())
 		{
 			objectArrayForContainerBeans = (Object[]) containerBeansIterator.next();
 			list.add(new NameValueBean((String) objectArrayForContainerBeans[1],
 					(Long) objectArrayForContainerBeans[0]));
 		}
 		return list;
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAllContainerBeans()
 	 */
 	public List<ContainerInformationObject> getAllContainerInformationObjects()
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
 		Collection containerInformationObjectCollection = executeHQL(
 				"getAllContainerInformationObjects", substitutionParameterMap);
 		Iterator containerInformationObjectIterator = containerInformationObjectCollection
 				.iterator();
 		Object[] objectArrayForContainerInformationObject;
 		List<ContainerInformationObject> list = new ArrayList<ContainerInformationObject>();
 		while (containerInformationObjectIterator.hasNext())
 		{
 			objectArrayForContainerInformationObject = (Object[]) containerInformationObjectIterator
 					.next();
 			list.add(new ContainerInformationObject(
 					(String) objectArrayForContainerInformationObject[1],
 					((Long) objectArrayForContainerInformationObject[0]).toString(),
 					(String) objectArrayForContainerInformationObject[2]));
 		}
 		return list;
 	}
 
 	/**
 	 *
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public Map<String, String> getAllContainerBeansMap() throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
 		Collection containersBeansCollection = executeHQL("getAllContainerBeans",
 				substitutionParameterMap);
 		Iterator containerBeansIterator = containersBeansCollection.iterator();
 		Object[] objectArrayForContainerBeans;
 		//List<NameValueBean> list = new ArrayList<NameValueBean>();
 		Map<String, String> containerBeansMap = new HashMap<String, String>();
 		String containerId;
 		String containerCaption;
 		while (containerBeansIterator.hasNext())
 		{
 			objectArrayForContainerBeans = (Object[]) containerBeansIterator.next();
 			containerCaption = (String) objectArrayForContainerBeans[1];
 			containerId = ((Long) objectArrayForContainerBeans[0]).toString();
 			//			list.add(new NameValueBean((String) objectArrayForContainerBeans[1],
 			//					(Long) objectArrayForContainerBeans[0]));
 			containerBeansMap.put(containerId, containerCaption);
 		}
 		return containerBeansMap;
 	}
 
 	/**
 	 *
 	 * @param objectArrayForContainerBeans
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private AssociationTreeObject processForChildContainer(
 			AssociationTreeObject associationTreeObjectForContainer)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 
 		ContainerInterface containerInterface = getContainerByIdentifier(associationTreeObjectForContainer
 				.getId().toString());
 		Collection<ControlInterface> controlsCollection = containerInterface.getControlCollection();
 
 		for (ControlInterface control : controlsCollection)
 		{
 			if (control instanceof ContainmentAssociationControlInterface)
 			{
 				ContainerInterface container = ((ContainmentAssociationControlInterface) control)
 						.getContainer();
 				AssociationTreeObject associationTreeObject = new AssociationTreeObject(container
 						.getId(), container.getCaption());
 				processForChildContainer(associationTreeObject);
 				associationTreeObjectForContainer.addAssociationTreeObject(associationTreeObject);
 			}
 		}
 
 		return associationTreeObjectForContainer;
 
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAllRecords(edu.common.dynamicextensions.domaininterface.EntityInterface)
 	 */
 	public List<EntityRecord> getAllRecords(EntityInterface entity)
 			throws DynamicExtensionsSystemException
 	{
 		List<EntityRecord> recordList = new ArrayList<EntityRecord>();
 		JDBCDAO jdbcDao = null;
 		List<List> result;
 		try
 		{
 			jdbcDao = (JDBCDAO) DAOFactory.getInstance().getDAO(Constants.JDBC_DAO);
 			jdbcDao.openSession(null);
 			TablePropertiesInterface tablePropertiesInterface = entity.getTableProperties();
 			String tableName = tablePropertiesInterface.getName();
 			String[] selectColumnName = {IDENTIFIER};
 			String[] whereColumnName = {Constants.ACTIVITY_STATUS_COLUMN};
 			String[] whereColumnCondition = {EQUAL};
 			Object[] whereColumnValue = {"'" + Constants.ACTIVITY_STATUS_ACTIVE + "'"};
 			result = jdbcDao.retrieve(tableName, selectColumnName, whereColumnName,
 					whereColumnCondition, whereColumnValue, null);
 			recordList = getRecordList(result);
 
 		}
 		catch (DAOException e)
 		{
 			throw new DynamicExtensionsSystemException("Error while retrieving the data", e);
 		}
 		finally
 		{
 			try
 			{
 				jdbcDao.closeSession();
 			}
 			catch (DAOException e)
 			{
 				throw new DynamicExtensionsSystemException("Error while retrieving the data", e);
 			}
 		}
 		return recordList;
 	}
 
 	/**
 	 *
 	 * @param result
 	 * @return
 	 */
 	private List<EntityRecord> getRecordList(List<List> result)
 	{
 		List<EntityRecord> recordList = new ArrayList<EntityRecord>();
 		EntityRecord entityRecord;
 		String id;
 		for (List innnerList : result)
 		{
 			if (innnerList != null && !innnerList.isEmpty())
 			{
 				id = (String) innnerList.get(0);
 				if (id != null)
 				{
 					entityRecord = new EntityRecord(new Long(id));
 					recordList.add(entityRecord);
 				}
 			}
 		}
 		return recordList;
 	}
 
 	/**
 	 * @throws DynamicExtensionsSystemException
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getChildrenEntities(edu.common.dynamicextensions.domaininterface.EntityInterface)
 	 */
 	public Collection<EntityInterface> getChildrenEntities(EntityInterface entity)
 			throws DynamicExtensionsSystemException
 	{
 		Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", entity.getId()));
 
 		return executeHQL("getChildrenEntities", substitutionParameterMap);
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAssociationByIdentifier(java.lang.Long)
 	 */
 	public AssociationInterface getAssociationByIdentifier(Long associationId)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", associationId));
 		Collection assocationCollection = executeHQL("getAssociationByIdentifier",
 				substitutionParameterMap);
 		if (assocationCollection.isEmpty())
 		{
 			throw new DynamicExtensionsApplicationException(
 					"Object Not Found : id" + associationId, null, DYEXTN_A_008);
 		}
 		return (AssociationInterface) assocationCollection.iterator().next();
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAssociationsForTargetEntity(edu.common.dynamicextensions.domaininterface.EntityInterface)
 	 */
 	public Collection<AssociationInterface> getIncomingAssociations(EntityInterface entity)
 			throws DynamicExtensionsSystemException
 	{
 		Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", entity.getId()));
 		Collection<AssociationInterface> assocationCollection = executeHQL(null,
 				"getAssociationsForTargetEntity", substitutionParameterMap);
 		return assocationCollection;
 	}
 
 	/**
 	 * @param containerId
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public String getContainerCaption(Long containerId) throws DynamicExtensionsSystemException
 	{
 		Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", containerId));
 		Collection containerCaption = executeHQL("getContainerCaption", substitutionParameterMap);
 		return containerCaption.iterator().next().toString();
 
 	}
 
 	public void deleteRecords(Long containerId, List<Long> recordIdList)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		ContainerInterface container = DynamicExtensionsUtility
 				.getContainerByIdentifier(containerId.toString());
 
 		EntityInterface entityInterface = container.getEntity();
 		for (Long recordId : recordIdList)
 		{
 			deleteRecord(entityInterface, recordId);
 		}
 
 	}
 
 	/** (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#addAssociationColumn(edu.common.dynamicextensions.domaininterface.AssociationInterface)
 	 */
 	public void addAssociationColumn(AssociationInterface association)
 			throws DynamicExtensionsSystemException
 	{
 		List list = new ArrayList();
 		String query;
 		Stack stack = new Stack();
 		try
 		{
 			query = queryBuilder.getQueryPartForAssociation(association, list, true);
 
 			List queryList = new ArrayList();
 			queryList.add(query);
 			stack = queryBuilder.executeQueries(queryList, list, stack);
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			if (!stack.isEmpty())
 			{
 				rollbackQueries(stack, (Entity) association.getEntity(), e, DAOFactory
 						.getInstance().getDAO(Constants.HIBERNATE_DAO));
 			}
 		}
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#associateEntityRecords(edu.common.dynamicextensions.domaininterface.AssociationInterface, java.lang.Long, java.lang.Long)
 	 */
 	public void associateEntityRecords(AssociationInterface associationInterface,
 			Long sourceEntityRecordId, Long TargetEntityRecordId)
 			throws DynamicExtensionsSystemException
 	{
 		queryBuilder.associateRecords(associationInterface, sourceEntityRecordId,
 				TargetEntityRecordId);
 	}
 
 	/**
 	 * @param containerId
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public Long getEntityIdByContainerId(Long containerId) throws DynamicExtensionsSystemException
 	{
 		Map substitutionParameterMap = new HashMap();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", containerId));
 		Collection recordCollection = null;
 		//Required HQL is stored in the hbm file. The following method takes the name of the query and
 		// the actual values for the placeholders as the parameters.
 		recordCollection = executeHQL("getEntityIdForContainerId", substitutionParameterMap);
 		if (recordCollection != null && !recordCollection.isEmpty())
 		{
 			return (Long) recordCollection.iterator().next();
 		}
 		return null;
 	}
 
 	/**
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public Map<Long, Date> getEntityCreatedDateByContainerId()
 			throws DynamicExtensionsSystemException
 	{
 		Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
 		Map<Long, Date> map = new HashMap<Long, Date>();
 		Collection containersBeansCollection;
 		containersBeansCollection = executeHQL("getAllEntityCreatedDateByContainerId",
 				substitutionParameterMap);
 
 		if (containersBeansCollection != null && !containersBeansCollection.isEmpty())
 		{
 			Iterator iter = containersBeansCollection.iterator();
 
 			while (iter.hasNext())
 			{
 				Object[] objectArray = (Object[]) iter.next();
 				map.put((Long) objectArray[0], (Date) objectArray[1]);
 			}
 		}
 		return map;
 
 	}
     /**
     *
     * @param isAbstarct
     * @param entityIdentifier
     * @return
     * @throws DynamicExtensionsSystemException
     */
 	public Long checkContainerForAbstractEntity(Long entityIdentifier, boolean isAbstarct)
 	throws DynamicExtensionsSystemException
 	{
 		Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("long",entityIdentifier));
 		substitutionParameterMap.put("1", new HQLPlaceHolderObject("boolean",isAbstarct));
 
 		Collection containerCollection = executeHQL("checkContainerForAbstractEntity",substitutionParameterMap);
 
 		Long contId = null;
 
 		if (containerCollection != null && containerCollection.size() > 0)
 		{
 			contId = (Long) containerCollection.iterator().next();
 
 		}
 		return contId;
 	}
 	public  Long getEntityId(String entityName) throws DynamicExtensionsSystemException 
     {
 		ResultSet rsltSet = null;
     	String entityTableName = "dyextn_abstract_metadata";
     	String NAME = "name";
     	StringBuffer query = new StringBuffer();
     	query.append(SELECT_KEYWORD + WHITESPACE + IDENTIFIER);
         query.append(WHITESPACE + FROM_KEYWORD + WHITESPACE + entityTableName + WHITESPACE);
         query.append(WHERE_KEYWORD  + WHITESPACE + NAME +  WHITESPACE + EQUAL + "'"+entityName+"'");
         System.out.println("Query = "  +query.toString());
         try {
             rsltSet = EntityManagerUtil.executeQuery(query.toString());
             rsltSet.next();
             Long identifier = rsltSet.getLong(IDENTIFIER);
             return identifier;
         }
         catch(Exception e)
         {
         	e.printStackTrace();
         }
         finally
 		{
 			if (rsltSet != null)
 			{
 				try
 				{
 					rsltSet.close();
 				}
 				catch (SQLException e)
 				{
 					throw new DynamicExtensionsSystemException(e.getMessage(), e);
 				}
 			}
 		}
         return null;
     }
 	
 	/**
 	 * Get the container Id for the specified entity Id
 	 * This method fires direct JDBC SQL queries without using hibernate for performance purposes  
 	 * @param entityId : Id for the entity whose container id is to be fetched
 	 * @return : container Id for specified entity
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public  Long getContainerIdForEntity(Long entityId) throws DynamicExtensionsSystemException
 	{
 		ResultSet rsltSet = null;
 		String tableName = "dyextn_container";
     	String ENTITY_ID_FIELD_NAME = "ENTITY_ID";
     	StringBuffer query = new StringBuffer();
     	query.append(SELECT_KEYWORD + WHITESPACE + IDENTIFIER);
         query.append(WHITESPACE + FROM_KEYWORD + WHITESPACE + tableName + WHITESPACE);
         query.append(WHERE_KEYWORD  + WHITESPACE + ENTITY_ID_FIELD_NAME +  WHITESPACE + EQUAL + "'"+entityId+"'");
         System.out.println("Query = "  +query.toString());
         try {
             rsltSet = EntityManagerUtil.executeQuery(query.toString());
             if(rsltSet!=null)
             {
             	rsltSet.next();
 	            Long identifier = rsltSet.getLong(IDENTIFIER);
 	            return identifier;
             }
         }
         catch(Exception e)
         {
         	e.printStackTrace();
         }
         finally
 		{
 			if (rsltSet != null)
 			{
 				try
 				{
 
 					rsltSet.close();
 
 				}
 				catch (SQLException e)
 				{
 					throw new DynamicExtensionsSystemException(e.getMessage(), e);
 				}
 			}
 		}
         return null;
 	}
 	/**
 	 * Get next identifier for an entity from entity table when a record is to be inserted to the entity table. 
 	 * @param entityName :  Name of the entity
 	 * @return :  Next identifier that can be assigned to a entity record
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public  Long getNextIdentifierForEntity(String entityName) throws DynamicExtensionsSystemException
 	{
 		ResultSet rsltSet = null;
 		String tableName = "dyextn_database_properties";
     	String NAME = "NAME";
     	StringBuffer query = new StringBuffer();
     	query.append(SELECT_KEYWORD + WHITESPACE + NAME);
         query.append(WHITESPACE + FROM_KEYWORD + WHITESPACE + tableName + WHITESPACE);
         query.append(WHERE_KEYWORD  + WHITESPACE +  IDENTIFIER +  WHITESPACE + EQUAL );
         query.append(OPENING_BRACKET);
             query.append(SELECT_KEYWORD + WHITESPACE + IDENTIFIER);
 	        query.append(WHITESPACE + FROM_KEYWORD + WHITESPACE + "dyextn_table_properties" + WHITESPACE);
 	        query.append(WHERE_KEYWORD  + WHITESPACE +  "ENTITY_ID" +  WHITESPACE + EQUAL );
 	        query.append(OPENING_BRACKET);
 	            query.append(SELECT_KEYWORD + WHITESPACE + IDENTIFIER);
 		        query.append(WHITESPACE + FROM_KEYWORD + WHITESPACE + "dyextn_abstract_metadata" + WHITESPACE);
 		        query.append(WHERE_KEYWORD  + WHITESPACE +  "NAME" +  WHITESPACE + EQUAL +"'"+ entityName+"'");
 		    query.append(CLOSING_BRACKET);
         query.append(CLOSING_BRACKET);
         System.out.println("Query = "  +query.toString());
         try {
              rsltSet = EntityManagerUtil.executeQuery(query.toString());
             if(rsltSet!=null)
             {
             	rsltSet.next();
 	            String entityTableName = rsltSet.getString(NAME);
 	            if(entityTableName!=null) 
 	            {
 	            	EntityManagerUtil entityManagerUtil = new EntityManagerUtil();
 	            	return entityManagerUtil.getNextIdentifier(entityTableName);
 	            }
 	        }
         }
         catch(Exception e)
         {
         	e.printStackTrace();
         }
         finally
 		{
 			if (rsltSet != null)
 			{
 				try
 				{
 
 					rsltSet.close();
 
 				}
 				catch (SQLException e)
 				{
 					throw new DynamicExtensionsSystemException(e.getMessage(), e);
 				}
 			}
 		}
         return null;
 	}
 
 }
