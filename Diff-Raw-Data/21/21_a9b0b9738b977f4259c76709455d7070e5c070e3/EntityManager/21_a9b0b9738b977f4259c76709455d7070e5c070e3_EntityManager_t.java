 
 package edu.common.dynamicextensions.entitymanager;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.ArrayList;
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
 import edu.common.dynamicextensions.domain.DomainObjectFactory;
 import edu.common.dynamicextensions.domain.DynamicExtensionBaseDomainObject;
 import edu.common.dynamicextensions.domain.Entity;
 import edu.common.dynamicextensions.domain.EntityGroup;
 import edu.common.dynamicextensions.domain.FileAttributeRecordValue;
 import edu.common.dynamicextensions.domain.FileAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.databaseproperties.ColumnProperties;
 import edu.common.dynamicextensions.domain.databaseproperties.ConstraintProperties;
 import edu.common.dynamicextensions.domain.databaseproperties.TableProperties;
 import edu.common.dynamicextensions.domain.userinterface.Container;
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AbstractMetadataInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationDisplayAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.RoleInterface;
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
 			EntityManagerExceptionConstantsInterface
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
 	public void setInstance(EntityManagerInterface entityManagerInterface)
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
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#persistEntity(edu.common.dynamicextensions.domaininterface.EntityInterface)
 	 */
 	public EntityInterface persistEntity(EntityInterface entityInterface)
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
 			//Calling the method which actually calls the insert/update method on dao. Hibernatedao is passed to this
 			//method and transaction is handled in the calling method.
 			entityInterface = saveOrUpdateEntity(entityInterface, hibernateDAO, stack,
 					isEntitySaved);
 			//Committing the changes done in the hibernate session to the database.
 			hibernateDAO.commit();
 		}
 		catch (Exception e)
 		{
 			//			Queries for data table creation and modification are fired in the method saveOrUpdateEntity. So if there
 			//is any exception while storing the metadata , we need to roll back the queries that were fired. So
 			//calling the following method to do that.
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
 	public EntityInterface persistEntityMetadata(EntityInterface entityInterface,
 			boolean isDataTablePresent) throws DynamicExtensionsSystemException,
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
 		return persistEntity(entityInterface);
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
 		entityGroup = saveOrUpdateEntityGroup(entityGroupInterface, isEntityGroupNew);
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
 		entityGroup = saveOrUpdateEntityGroup(entityGroupInterface, isEntityGroupNew);
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
 			Long recordId) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		AttributeRecord collectionAttributeRecord = getAttributeRecord(entityId, attributeId,
 				recordId, null);
 		Collection<CollectionAttributeRecordValue> recordValueCollection = collectionAttributeRecord
 				.getValueCollection();
 
 		List<String> valueList = new ArrayList<String>();
 		for (CollectionAttributeRecordValue recordValue : recordValueCollection)
 		{
 			valueList.add(recordValue.getValue());
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
 			Long recordId) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		AttributeRecord record = getAttributeRecord(entityId, attributeId, recordId, null);
 		return record.getFileRecord();
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
 			HibernateDAO hibernateDao) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 
 		Map substitutionParameterMap = new HashMap();
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
 		AttributeRecord collectionAttributeRecord = (AttributeRecord) recordCollection.iterator()
 				.next();
 		return collectionAttributeRecord;
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
 		EntityInterface entityInterface = null;
 		if (entityName == null || entityName.equals(""))
 		{
 			return entityInterface;
 		}
 		//Getting the instance of the default biz logic on which retrieve method is later called.
 		DefaultBizLogic defaultBizLogic = BizLogicFactory.getDefaultBizLogic();
 		List entityInterfaceList = new ArrayList();
 		try
 		{
 			//the following method gives the object , the class name of which is passed as the first parameter.
 			// The criteria for the object is given in the second and third parameter. The second parameter is the 
 			// field of the object that needs to be compared with the values that is given as the third parameter.
 			entityInterfaceList = defaultBizLogic.retrieve(Entity.class.getName(), "name",
 					entityName);
 		}
 		catch (DAOException e)
 		{
 			throw new DynamicExtensionsSystemException(e.getMessage(), e);
 		}
 
 		if (entityInterfaceList != null && entityInterfaceList.size() > 0)
 		{
 			entityInterface = (EntityInterface) entityInterfaceList.get(0);
 		}
 
 		return entityInterface;
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
 	 * @throws DynamicExtensionsSystemException 
 	 * @throws DynamicExtensionsApplicationException 
 	 * @throws UserNotAuthorizedException 
 	 * @throws DAOException 
 	 */
 	private void postSaveProcessEntity(Entity entity, HibernateDAO hibernateDAO,
 			Stack rollbackQueryStack) throws DynamicExtensionsApplicationException,
 			DynamicExtensionsSystemException, DAOException, UserNotAuthorizedException
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
 					if (targetEntity.getId() == null)
 					{
 						boolean isEntitySaved = false;
 						((Entity) targetEntity).setDataTableState(entity.getDataTableState());
 						targetEntity = saveOrUpdateEntity(targetEntity, hibernateDAO,
 								rollbackQueryStack, isEntitySaved);
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
 				constraintPropertiesSysGen = systemGeneratedAssociation.getConstraintProperties();
 			}
 			constraintPropertiesSysGen.setName(association.getConstraintProperties().getName());
 			//Swapping the source and target keys.
 			constraintPropertiesSysGen.setSourceEntityKey(association.getConstraintProperties()
 					.getTargetEntityKey());
 			constraintPropertiesSysGen.setTargetEntityKey(association.getConstraintProperties()
 					.getSourceEntityKey());
 			//Populating the sys. generated association.
 			systemGeneratedAssociation.setName(association.getName());
 			systemGeneratedAssociation.setDescription(association.getDescription());
 			systemGeneratedAssociation.setTargetEntity(association.getEntity());
 			systemGeneratedAssociation.setEntity(association.getTargetEntity());
 			//Swapping the source and target roles.
 			systemGeneratedAssociation.setSourceRole(association.getTargetRole());
 			systemGeneratedAssociation.setTargetRole(association.getSourceRole());
 			systemGeneratedAssociation.setAssociationDirection(AssociationDirection.BI_DIRECTIONAL);
 			systemGeneratedAssociation.setIsSystemGenerated(true);
 			systemGeneratedAssociation.setConstraintProperties(constraintPropertiesSysGen);
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
 				association.getTargetEntity().removeAbstractAttribute(systemGeneratedAssociation);
 				isTargetEntityChanged = true;
 			}
 		}
 
 		if (isTargetEntityChanged)
 		{
 			//Saving the modified target entity.
 			hibernateDAO.update(association.getTargetEntity(), null, false, false, false);
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
 						+ name);
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
 	 *  Returns all entities in the whole system for a given type of the object
 	 * @return
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
 
 	/**
 	 * Returns a collection of entity objects given the entity description
 	 * @param entityDescription
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public Collection getEntityByDescription(String entityDescription)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		return null;
 	}
 
 	/**
 	 * Returns a collection of Entity objects given the attribute description
 	 * @param attributeDescription
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public Collection getEntitiesByAttributeDescription(String attributeDescription)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		return null;
 	}
 
 	/**
 	 * Returns a collection of entity objects given the entity concept name.
 	 * @param entityConceptName
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public Collection getEntitiesByConceptName(String entityConceptName)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		return null;
 	}
 
 	/**
 	 * Returns a collection of entities given attribute concept code.
 	 * @param attributeConceptCode
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public Collection getEntitiesByAttributeConceptCode(String attributeConceptCode)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		return null;
 	}
 
 	/**
 	 * Returns a collection of entities given the attribute concept name
 	 * @param attributeConceptname
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public Collection getEntitiesByAttributeConceptName(String attributeConceptName)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		return null;
 	}
 
 	/**
 	 * Returns a collection of entity objects given the entity object with specific criteria. 
 	 * @param entityInterface
 	 * @return
 	 */
 	public Collection findEntity(EntityInterface entityInterface)
 	{
 		return null;
 	}
 
 	/**
 	 * This method is used to save the container into the database.
 	 * @param containerInterface container to save
 	 * @return ContainerInterface container Interface that is saved.
 	 * @throws DynamicExtensionsSystemException Thrown if for any reason operation can not be completed.
 	 * @throws DynamicExtensionsApplicationException Thrown if the entity name already exists.
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	public ContainerInterface persistContainer(ContainerInterface containerInterface)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
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
 		boolean isentitySaved = true;
 		if (entity != null && entity.getId() == null)
 		{
 			isentitySaved = false;
 		}
 		boolean isContainerSaved = true;
 		if (container.getId() == null)
 		{
 			isContainerSaved = false;
 		}
 		try
 		{
 			hibernateDAO.openSession(null);
 			if (entity != null)
 			{
 				// saves the entity into database. It populates rollbackQueryStack with the 
 				// queries that restores the database state to the state before calling this method
 				// in case of exception. 
 				saveOrUpdateEntity(entity, hibernateDAO, rollbackQueryStack, isentitySaved);
 
 			}
 
 			preSaveProcessContainer(container); //preprocess
 			if (isContainerSaved)
 			{
 				hibernateDAO.update(container, null, false, false, false);
 			}
 			else
 			{
 				hibernateDAO.insert(container, null, false, false);
 			}
 			hibernateDAO.commit();
 
 		}
 		catch (DAOException e)
 		{
 			//In case of exception execute roll back queries to restore the database state.
 			rollbackQueries(rollbackQueryStack, entity, e, hibernateDAO);
 			throw new DynamicExtensionsSystemException(
 					"Exception occured while opening a session to save the container.");
 		}
 		catch (UserNotAuthorizedException e)
 		{
 			rollbackQueries(rollbackQueryStack, entity, e, hibernateDAO);
 		}
 		finally
 		{
 			try
 			{
 				hibernateDAO.closeSession();
 			}
 			catch (DAOException e)
 			{
 				rollbackQueries(rollbackQueryStack, entity, e, hibernateDAO);
 			}
 		}
 		return container;
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
 	 * This method processes entity beofre saving it to databse.
 	 * <li> It validates entity for duplicate name of entity,attributes and association
 	 * <li> It sets created and updated date-time.
 	 * 
 	 * @param entity entity
 	 */
 	private void preSaveProcessEntity(EntityInterface entity)
 			throws DynamicExtensionsApplicationException
 	{
 		validateEntityForSaving(entity);// chk if entity is vlaid or not.
 
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
 			HibernateDAO hibernateDAO) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException, HibernateException, SQLException, DAOException,
 			UserNotAuthorizedException
 	{
 		if (entity == null || dataValue == null || dataValue.isEmpty())
 		{
 			throw new DynamicExtensionsSystemException("Input to insert data is null");
 		}
 
 		StringBuffer columnNameString = new StringBuffer("IDENTIFIER ");
 		Long identifier = entityManagerUtil
 				.getNextIdentifier(entity.getTableProperties().getName());
 		StringBuffer columnValuesString = new StringBuffer(identifier.toString());
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
 					columnNameString.append(" , ");
 					columnValuesString.append(" , ");
 					String dbColumnName = primitiveAttribute.getColumnProperties().getName();
 
 					columnNameString.append(dbColumnName);
 					value = EntityManagerUtil.getFormattedValue(attribute, value);
 					columnValuesString.append(value);
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
 						Long recordIdForContainedEntity = insertDataForSingleEntity(association
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
 	public Long insertData(EntityInterface entity, Map dataValue)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 
 		Long recordId = null;
 		HibernateDAO hibernateDAO = null;
 		try
 		{
 			DAOFactory factory = DAOFactory.getInstance();
 			hibernateDAO = (HibernateDAO) factory.getDAO(Constants.HIBERNATE_DAO);;
 			hibernateDAO.openSession(null);
 
 			recordId = insertDataForSingleEntity(entity, dataValue, hibernateDAO);
 
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
 
 		return recordId;
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
 			throw new DynamicExtensionsSystemException("Input to edit data is null");
 		}
 
 		StringBuffer updateColumnString = new StringBuffer();
 		String tableName = entity.getTableProperties().getName();
 
 		List<AttributeRecord> collectionRecords = new ArrayList<AttributeRecord>();
 		List<AttributeRecord> deleteCollectionRecords = new ArrayList<AttributeRecord>();
 		List<AttributeRecord> fileRecords = new ArrayList<AttributeRecord>();
 
 		Set uiColumnSet = dataValue.keySet();
 		Iterator uiColumnSetIter = uiColumnSet.iterator();
 		List associationRemoveDataQueryList = new ArrayList();
 		List associationInsertDataQueryList = new ArrayList();
 		while (uiColumnSetIter.hasNext())
 		{
 			AbstractAttribute attribute = (AbstractAttribute) uiColumnSetIter.next();
 			Object value = dataValue.get(attribute);
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
 					fileRecord.getFileRecord().copyValues(fileRecordValue);
 					fileRecords.add(fileRecord);
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
 					value = EntityManagerUtil.getFormattedValue(attribute, value);
 					updateColumnString.append(value);
 				}
 			}
 			else
 			{
 				AssociationInterface association = (AssociationInterface) attribute;
 				List<Long> recordIdList = new ArrayList<Long>();
 
 				// for association need to remove previously associated target reocrd first.
 				String removeQuery = queryBuilder.getAssociationRemoveDataQuery(
 						((Association) attribute), recordId);
 
 				if (association.getSourceRole().getAssociationsType().equals(
 						AssociationType.CONTAINTMENT))
 				{
 
 					entityManagerUtil.executeDML(removeQuery);
 
 					List<Map> listOfMapsForContainedEntity = (List<Map>) value;
 					for (Map valueMapForContainedEntity : listOfMapsForContainedEntity)
 					{
 						Long reocordId = insertDataForSingleEntity(association.getTargetEntity(),
 								valueMapForContainedEntity, hibernateDAO);
 						recordIdList.add(recordId);
 					}
 
 				}
 				else
 				{
 					recordIdList = (List<Long>) value;
 				}
 
 				if (removeQuery != null && removeQuery.trim().length() != 0)
 				{
 					associationRemoveDataQueryList.add(removeQuery);
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
 
 		if (updateColumnString.length() != 0)
 		{
 
 			Connection conn = DBUtil.getConnection();
 			for (String queryString : editDataQueryList)
 			{
 				logDebug("editData", "Query is: " + queryString.toString());
 				PreparedStatement statement = conn.prepareStatement(queryString);
 				statement.executeUpdate();
 			}
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
 			logDebug("editData", "updating filereocrd for multi select: "
 					+ fileRecord.getFileRecord().getFileName());
 			hibernateDAO.update(fileRecord, null, false, false, false);
 		}
 
 		return true;
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#editData(edu.common.dynamicextensions.domaininterface.EntityInterface, java.util.Map, java.lang.Long)
 	 */
 	public boolean editData(EntityInterface entity, Map dataValue, Long recordId)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 
 		boolean isSuccess;
 		HibernateDAO hibernateDAO = null;
 		try
 		{
 
 			DAOFactory factory = DAOFactory.getInstance();
 			hibernateDAO = (HibernateDAO) factory.getDAO(Constants.HIBERNATE_DAO);
 
 			hibernateDAO.openSession(null);
 
 			isSuccess = editDataForSingleEntity(entity, dataValue, recordId, hibernateDAO);
 
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
 
 		return true;
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
 
 	/**
 	 * This method is used by create as well as edit entity methods. This method holds all the common part 
 	 * related to saving the entity into the database and also handling the exceptions .
 	 * @param entityInterface Entity to be stored in the database.
 	 * @param isNew flag for whether it is a save or update.
 	 * @param hibernateDAO 
 	 * @param isNewFlag 
 	 * @return Entity . Stored instance of the entity.
 	 * @throws DynamicExtensionsApplicationException System exception in case of any fatal errors.
 	 * @throws DynamicExtensionsSystemException Thrown in case of duplicate name or authentication failure.
 	 */
 	private EntityInterface saveOrUpdateEntity(EntityInterface entityInterface,
 			HibernateDAO hibernateDAO, Stack rollbackQueryStack, boolean isEntitySaved)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		logDebug("saveOrUpdateEntity", "Entering method");
 
 		Entity entity = (Entity) entityInterface;//(Entity) DynamicExtensionsUtility.cloneObject(entityInterface);
 		List reverseQueryList = new LinkedList();
 		List queryList = null;
 
 		checkForDuplicateEntityName(entity);
 		Entity databaseCopy = null;
 		try
 		{
 			if (!isEntitySaved)
 			{
 				preSaveProcessEntity(entity);
 				hibernateDAO.insert(entity, null, false, false);
 			}
 			else
 			{
 				databaseCopy = (Entity) DBUtil.loadCleanObj(Entity.class, entity.getId());
 				hibernateDAO.update(entity, null, false, false, false);
 
 			}
 			postSaveProcessEntity(entity, hibernateDAO, rollbackQueryStack);
 			hibernateDAO.update(entity, null, false, false, false);
 
 			if (entity.getDataTableState() == DATA_TABLE_STATE_CREATED)
 			{
 				if (!isEntitySaved)
 				{
 					queryList = queryBuilder.getCreateEntityQueryList(entity, reverseQueryList,
 							hibernateDAO, rollbackQueryStack);
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
 	public Map<AbstractAttributeInterface, Object> getRecordById(EntityInterface entity,
 			Long recordId) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		Map recordValues = new HashMap();
 
 		if (entity == null || entity.getId() == null || recordId == null)
 		{
 			throw new DynamicExtensionsSystemException("Invalid Input");
 		}
 
 		Collection attributesCollection = entity.getAttributeCollection();
 		List<AttributeInterface> collectionAttributes = new ArrayList<AttributeInterface>();
 		List<AttributeInterface> fileAttributes = new ArrayList<AttributeInterface>();
 
 		String tableName = entity.getTableProperties().getName();
 		List<String> selectColumnNameList = new ArrayList<String>();
 		String[] whereColumnName = new String[]{IDENTIFIER};
 		String[] whereColumnCondition = new String[]{"="};
 		Object[] whereColumnValue = new Object[]{recordId};
 
 		Iterator attriIterator = attributesCollection.iterator();
 		Map columnNameMap = new HashMap();
 		int index = 0;
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
 			else
 			{
 				//for the other attributes, create select query.
 				String dbColumnName = attribute.getColumnProperties().getName();
 				//String uiColumnName = attribute.getName();
 				selectColumnNameList.add(dbColumnName);
 				//selectColumnName[index] = dbColumnName;
 				columnNameMap.put(dbColumnName, attribute);
 				index++;
 			}
 		}
 
 		//get association values. 
 		recordValues.putAll(queryBuilder.getAssociationGetRecordQueryList(entity, recordId));
 
 		String[] selectColumnName = new String[selectColumnNameList.size()];
 		for (int i = 0; i < selectColumnNameList.size(); i++)
 		{
 			selectColumnName[i] = selectColumnNameList.get(i);
 		}
 
 		JDBCDAO jdbcDao = null;
 		try
 		{
 			jdbcDao = (JDBCDAO) DAOFactory.getInstance().getDAO(Constants.JDBC_DAO);
 			jdbcDao.openSession(null);
 
 			List result = jdbcDao.retrieve(tableName, selectColumnName, whereColumnName,
 					whereColumnCondition, whereColumnValue, null);
 			List innerList = null;
 
 			if (result != null && result.size() != 0)
 			{
 				innerList = (List) result.get(0);
 			}
 			if (innerList != null && !selectColumnNameList.isEmpty() && selectColumnName.length > 0)
 			{
 
 				for (int i = 0; i < innerList.size(); i++)
 				{
 					String value = (String) innerList.get(i);
 					String dbColumnName = selectColumnName[i];
 					Attribute attribute = (Attribute) columnNameMap.get(dbColumnName);
 					//put the value for other attributes
 					recordValues.put(attribute, value);
 				}
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
 		return recordValues;
 	}
 
 	/**
 	 * processes entity group before saving.
 	 * @param entity entity
 	 * @throws DynamicExtensionsApplicationException 
 	 */
 	private void preSaveProcessEntityGroup(EntityGroupInterface entityGroup)
 			throws DynamicExtensionsApplicationException
 	{
 		validateName(entityGroup.getName());
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
 			boolean isNew) throws DynamicExtensionsApplicationException,
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
 			checkForDuplicateEntityGroupName(entityGroup);
 
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
 				for (EntityInterface entity : entityCollection)
 				{
 					entityInterface = entity;
 					boolean isEntitySaved = false;
 					if (entityInterface.getId() != null)
 					{
 						isEntitySaved = true;
 					}
 					saveOrUpdateEntity(entityInterface, hibernateDAO, stack, isEntitySaved);
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
 	 */
 	private void checkForDuplicateEntityGroupName(EntityGroup entityGroup)
 			throws DynamicExtensionsApplicationException
 	{
 		// TODO Auto-generated method stub
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
 	private Collection executeHQL(String queryName, Map substitutionParameterMap)
 			throws DynamicExtensionsSystemException
 	{
 		Collection entityCollection = new HashSet();
 		HibernateDAO hibernateDAO = (HibernateDAO) DAOFactory.getInstance().getDAO(
 				Constants.HIBERNATE_DAO);
 		try
 		{
 			hibernateDAO.openSession(null);
 			Query query = substitutionParameterForQuery(queryName, substitutionParameterMap);
 			entityCollection = query.list();
 			hibernateDAO.commit();
 		}
 		catch (DAOException e)
 		{
 			try
 			{
 				hibernateDAO.rollback();
 				throw new DynamicExtensionsSystemException("Exception occured while executing hqk",
 						e);
 
 			}
 			catch (DAOException e1)
 			{
 				throw new DynamicExtensionsSystemException("Error while rolling back the session",
 						e1);
 			}
 
 		}
 
 		catch (HibernateException e)
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
 				throw new DynamicExtensionsSystemException("Error while closing the session", e);
 			}
 		}
 		return entityCollection;
 	}
 
 	/**
 	 * This method substitues the parameters from substitutionParameterMap into the input query. 
 	 * @param substitutionParameterMap
 	 * @throws HibernateException 
 	 */
 	private Query substitutionParameterForQuery(String queryName, Map substitutionParameterMap)
 			throws HibernateException
 	{
 		Session session = DBUtil.currentSession();
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
 		Collection attributeCollection = entity.getAttributeCollection();
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
 					// remove AttributeRecord objects for multi select and file type attributes
 					if (attribute.getIsCollection()
 							|| attribute.getAttributeTypeInformation() instanceof FileAttributeTypeInformation)
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
 					String associationRemoveQuery = QueryBuilderFactory.getQueryBuilder()
 							.getAssociationRemoveDataQuery(association, recordId);
 					associationRemoveQueryList.add(associationRemoveQuery);
 				}
 			}
 			Connection conn = DBUtil.getConnection();
 			StringBuffer query = new StringBuffer();
 			query.append(DELETE_KEYWORD + WHITESPACE + entity.getTableProperties().getName()
 					+ WHITESPACE + WHERE_KEYWORD + WHITESPACE + IDENTIFIER + WHITESPACE + EQUAL
 					+ WHITESPACE + recordId.toString());
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
 			Query query = substitutionParameterForQuery(queryName, substitutionParameterMap);
 			entityCollection = query.list();
 		}
 		catch (HibernateException e)
 		{
 			throw new DynamicExtensionsSystemException(e.getMessage(), e, DYEXTN_S_001);
 		}
 
 		return entityCollection;
 
 	}
 
 	/**
 	 * validate the entity for
 	 * 1. Name - should not contain any special characters, should not be empty,null
 	 * 2. Description - should be less than 1000 characters.
 	 * 
 	 * @param entity
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private void validateEntityForSaving(EntityInterface entity)
 			throws DynamicExtensionsApplicationException
 	{
 
 		validateName(entity.getName());
 		Collection<AbstractAttributeInterface> collection = entity.getAbstractAttributeCollection();
 		if (collection != null && !collection.isEmpty())
 		{
 			Iterator iterator = collection.iterator();
 			while (iterator.hasNext())
 			{
 				AbstractMetadataInterface abstractMetadataInterface = (AbstractMetadataInterface) iterator
 						.next();
 				validateName(abstractMetadataInterface.getName());
 			}
 		}
 
 		if (entity.getDescription() != null && entity.getDescription().length() > 1000)
 		{
 			throw new DynamicExtensionsApplicationException("Entity description size exceeded ",
 					null, DYEXTN_A_004);
 		}
 		Collection<String> nameCollection = new HashSet<String>();
 		for (AbstractAttributeInterface attribute : collection)
 		{
 			if (!nameCollection.contains(attribute.getName()))
 			{
 				nameCollection.add(attribute.getName());
 			}
 			else
 			{
 				throw new DynamicExtensionsApplicationException(
 						"Attribute names should be unique for the entity ", null, DYEXTN_A_006,
 						attribute.getName());
 			}
 		}
 		return;
 	}
 
 	/**
 	 * @param name
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private void validateName(String name) throws DynamicExtensionsApplicationException
 	{
 		/**
 		 * Constant representing valid names 
 		 */
 		final String VALIDCHARSREGEX = "[^\\\\/:*?\"<>&;|']*";
 
 		if (name == null || name.trim().length() == 0 || !name.matches(VALIDCHARSREGEX))
 		{
 			throw new DynamicExtensionsApplicationException("Object name invalid", null,
 					DYEXTN_A_003);
 		}
 		if (name.trim().length() > 40)
 		{
 			throw new DynamicExtensionsApplicationException("Object name exceeds maximum limit",
 					null, DYEXTN_A_007);
 		}
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
 		Map substitutionParameterMap = new HashMap();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", entityGroupIdentifier));
 		return executeHQL("getAllContainersByEntityGroupId", substitutionParameterMap);
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
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getRecordsForAssociationControl(edu.common.dynamicextensions.domaininterface.userinterface.AssociationControlInterface)
 	 */
 	public Map<Long, List<String>> getRecordsForAssociationControl(
 			AssociationControlInterface associationControl) throws DynamicExtensionsSystemException
 	{
 		Map<Long, List<String>> outputMap = new HashMap<Long, List<String>>();
 
 		Collection associationAttributesCollection = associationControl
 				.getAssociationDisplayAttributeCollection();
 
 		List associationAttributesList = new ArrayList(associationAttributesCollection);
 		Collections.sort(associationAttributesList);
 		String[] selectColumnName = new String[associationAttributesList.size() + 1];
 
 		Iterator attributeIterator = associationAttributesCollection.iterator();
 		AssociationDisplayAttributeInterface displayAttribute = null;
 		selectColumnName[0] = IDENTIFIER;
 		int index = 1;
 		while (attributeIterator.hasNext())
 		{
 			displayAttribute = (AssociationDisplayAttributeInterface) attributeIterator.next();
 			selectColumnName[index++] = displayAttribute.getAttribute().getColumnProperties()
 					.getName();
 		}
 
 		String tableName = displayAttribute.getAttribute().getEntity().getTableProperties()
 				.getName();
 
 		JDBCDAO jdbcDao = null;
 		try
 		{
 			jdbcDao = (JDBCDAO) DAOFactory.getInstance().getDAO(Constants.JDBC_DAO);
 			jdbcDao.openSession(null);
 
 			List result = jdbcDao.retrieve(tableName, selectColumnName);
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
 
 		Collection groupBeansCollection = executeHQL("getAllGroupBeans", new HashMap());
 		Iterator groupBeansIterator = groupBeansCollection.iterator();
 		Object[] objectArray;
 		AssociationTreeObject associationTreeObject;
 
 		while (groupBeansIterator.hasNext())
 		{
 			objectArray = (Object[]) groupBeansIterator.next();
 			associationTreeObject = processGroupBean(objectArray);
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
 	private AssociationTreeObject processGroupBean(Object[] objectArray)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		AssociationTreeObject associationTreeObjectForGroup = new AssociationTreeObject(
 				(Long) objectArray[0], (String) objectArray[1]);
 
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
 			processForChildContainer(associationTreeObjectForContainer);
 			associationTreeObjectForGroup
 					.addAssociationTreeObject(associationTreeObjectForContainer);
 
 		}
 
 		return associationTreeObjectForGroup;
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
 			result = jdbcDao.retrieve(tableName, selectColumnName);
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
 
 }
