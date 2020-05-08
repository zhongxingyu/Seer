 
 package edu.common.dynamicextensions.entitymanager;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.sql.Blob;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.text.ParseException;
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
 
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 import edu.common.dynamicextensions.bizlogic.BizLogicFactory;
 import edu.common.dynamicextensions.domain.AbstractAttribute;
 import edu.common.dynamicextensions.domain.Association;
 import edu.common.dynamicextensions.domain.Attribute;
 import edu.common.dynamicextensions.domain.AttributeRecord;
 import edu.common.dynamicextensions.domain.CollectionAttributeRecordValue;
 import edu.common.dynamicextensions.domain.DateAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.DomainObjectFactory;
 import edu.common.dynamicextensions.domain.Entity;
 import edu.common.dynamicextensions.domain.EntityGroup;
 import edu.common.dynamicextensions.domain.FileAttributeRecordValue;
 import edu.common.dynamicextensions.domain.FileAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.ObjectAttributeRecordValue;
 import edu.common.dynamicextensions.domain.ObjectAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.userinterface.SelectControl;
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AbstractMetadataInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationDisplayAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.DynamicExtensionBaseDomainObjectInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.ObjectAttributeRecordValueInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.AssociationControlInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ControlInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.util.AssociationTreeObject;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.common.dynamicextensions.util.global.Constants;
 import edu.common.dynamicextensions.util.global.Variables;
 import edu.common.dynamicextensions.util.global.Constants.AssociationType;
 import edu.wustl.common.beans.NameValueBean;
 import edu.wustl.common.beans.SessionDataBean;
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
  *
  * @author mandar_shidhore
  *
  */
 public class EntityManager extends AbstractMetadataManager implements EntityManagerInterface
 {
 
 	/**
 	 * Static instance of the entity manager.
 	 */
 	private static EntityManagerInterface entityManager = null;
 
 	/**
 	 * Static instance of the queryBuilder.
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
 		if (entityManager == null)
 		{
 			entityManager = new EntityManager();
 			DynamicExtensionsUtility.initialiseApplicationVariables();
 			queryBuilder = QueryBuilderFactory.getQueryBuilder();
 		}
 
 		return entityManager;
 	}
 
 	/**
 	 * Mock entity manager can be placed in the entity manager using this method.
 	 * @param entityManager
 	 */
 	public static void setInstance(EntityManagerInterface entityManagerInterface)
 	{
 		EntityManager.entityManager = entityManagerInterface;
 
 	}
 
 	/**
 	 * Saves the entity into the database.Also prepares the dynamic tables and associations
 	 * between those tables using the metadata information in the entity object.
 	 * EntityInterface can be obtained from DomainObjectFactory.
 	 * @param entityInterface
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public EntityInterface persistEntity(EntityInterface entity) throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		List reverseQueryList = new LinkedList();
 		List queryList = new ArrayList();
 		Stack rollbackQueryStack = new Stack();
 		HibernateDAO hibernateDAO = (HibernateDAO) DAOFactory.getInstance().getDAO(Constants.HIBERNATE_DAO);
 		try
 		{
 			hibernateDAO.openSession(null);
 
 			//preProcess(entity, queryList, hibernateDAO, reverseQueryList);
 			preProcess(entity, reverseQueryList, hibernateDAO, queryList);
 
 			if (entity.getId() == null)
 			{
 				hibernateDAO.insert(entity, null, false, false);
 			}
 			else
 			{
 				hibernateDAO.update(entity, null, false, false, false);
 			}
 
 			postProcess(queryList, reverseQueryList, rollbackQueryStack, hibernateDAO);
 
 			hibernateDAO.commit();
 			//			Update the dynamic extension cache for all containers within entitygroup
 			EntityGroupInterface entityGroupInterface = entity.getEntityGroup();
 			DynamicExtensionsUtility.updateDynamicExtensionsCache(entityGroupInterface.getId());
 		}
 		catch (DAOException e)
 		{
 			rollbackQueries(rollbackQueryStack, entity, e, hibernateDAO);
 			throw new DynamicExtensionsSystemException("DAOException occured while opening a session to save the container.", e);
 		}
 		catch (UserNotAuthorizedException e)
 		{
 			rollbackQueries(rollbackQueryStack, entity, e, hibernateDAO);
 			e.printStackTrace();
 			throw new DynamicExtensionsSystemException("DAOException occured while opening a session to save the container.", e);
 		}
 		finally
 		{
 			try
 			{
 				hibernateDAO.closeSession();
 			}
 			catch (DAOException e)
 			{
 				throw new DynamicExtensionsSystemException("DAOException occured while opening a session to save the container.", e);
 			}
 		}
 		return entity;
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#persistEntityMetadata(edu.common.dynamicextensions.domaininterface.EntityInterface)
 	 */
 	public EntityInterface persistEntityMetadata(EntityInterface entity) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		Stack rollbackQueryStack = new Stack();
 		HibernateDAO hibernateDAO = (HibernateDAO) DAOFactory.getInstance().getDAO(Constants.HIBERNATE_DAO);
 		try
 		{
 			hibernateDAO.openSession(null);
 
 			if (entity.getId() == null)
 			{
 				hibernateDAO.insert(entity, null, false, false);
 			}
 			else
 			{
 				hibernateDAO.update(entity, null, false, false, false);
 			}
 
 			hibernateDAO.commit();
 			//Update the dynamic extension cache for all containers within entitygroup
 			EntityGroupInterface entityGroupInterface = entity.getEntityGroup();
 			DynamicExtensionsUtility.updateDynamicExtensionsCache(entityGroupInterface.getId());
 		}
 		catch (DAOException e)
 		{
 			rollbackQueries(rollbackQueryStack, entity, e, hibernateDAO);
 			throw new DynamicExtensionsSystemException("DAOException occured while opening a session to save the container.", e);
 		}
 		catch (UserNotAuthorizedException e)
 		{
 			rollbackQueries(rollbackQueryStack, entity, e, hibernateDAO);
 			throw new DynamicExtensionsSystemException("DAOException occured while opening a session to save the container.", e);
 		}
 		finally
 		{
 			try
 			{
 				hibernateDAO.closeSession();
 			}
 			catch (DAOException e)
 			{
 				throw new DynamicExtensionsSystemException("DAOException occured while opening a session to save the container.", e);
 			}
 		}
 		return entity;
 	}
 
 	/**
 	 * This method creates dynamic table queries for the entities within a group.
 	 * @param group EntityGroup
 	 * @param reverseQueryList List of queries to be executed in case any problem occurs at DB level.
 	 * @param hibernateDAO
 	 * @param queryList List of queries to be executed to created dynamicn tables.
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	protected void preProcess(DynamicExtensionBaseDomainObjectInterface dynamicExtensionBaseDomainObject, List reverseQueryList,
 			HibernateDAO hibernateDAO, List queryList) throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		EntityInterface entityObject = (EntityInterface) dynamicExtensionBaseDomainObject;
 		createDynamicQueries(entityObject, reverseQueryList, hibernateDAO, queryList);
 	}
 
 	/**
 	 * @param entity
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private void checkParentChangeAllowed(Entity entity) throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		String tableName = entity.getTableProperties().getName();
 		if (queryBuilder.isDataPresent(tableName))
 		{
 			throw new DynamicExtensionsApplicationException("Can not change the data type of the attribute", null, DYEXTN_A_010);
 		}
 	}
 
 	/**
 	 * This method executes dynamic table queries created for all the entities within a group.
 	 * @param queryList List of queries to be executed to created dynamicn tables.
 	 * @param reverseQueryList List of queries to be executed in case any problem occurs at DB level.
 	 * @param rollbackQueryStack Stack to undo any changes done beforehand at DB level.
 	 * @throws DynamicExtensionsSystemException
 	 */
 	protected void postProcess(List queryList, List reverseQueryList, Stack rollbackQueryStack, HibernateDAO hibernateDAO)
 			throws DynamicExtensionsSystemException
 	{
 		queryBuilder.executeQueries(queryList, reverseQueryList, rollbackQueryStack, hibernateDAO);
 	}
 
 	/**
 	 * createDynamicQueries.
 	 * @param entityInterface
 	 * @param reverseQueryList
 	 * @param hibernateDAO
 	 * @param queryList
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private List createDynamicQueries(EntityInterface entityInterface, List reverseQueryList, HibernateDAO hibernateDAO, List queryList)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		return getDynamicQueryList(entityInterface.getEntityGroup(), reverseQueryList, hibernateDAO, queryList);
 	}
 
 	/**
 	 * This method is called when there any exception occurs while generating the data table queries
 	 * for the entity. Valid scenario is that if we need to fire Q1 Q2 and Q3 in order to create the
 	 * data tables and Q1 Q2 get fired successfully and exception occurs while executing query Q3 then
 	 * this method receives the query list which holds the set of queries which negate the effect of the
 	 * queries which were generated successfully so that the metadata information and database are in
 	 * synchronisation.
 	 * @param reverseQueryList List of queries to be executed in case any problem occurs at DB level.
 	 * @param entity Entity
 	 * @param e Exception encountered
 	 * @param dao AbstractDAO
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private void rollbackQueries(Stack reverseQueryStack, EntityInterface entity, Exception e, AbstractDAO dao)
 			throws DynamicExtensionsSystemException
 	{
 		String message = "";
 		/*try
 		 {*/
 		dao.rollback();
 		/*}
 		 catch (DAOException e2)
 		 {
 		 logDebug("rollbackQueries", DynamicExtensionsUtility.getStackTrace(e));
 		 DynamicExtensionsSystemException ex = new DynamicExtensionsSystemException(message, e);
 		 ex.setErrorCode(DYEXTN_S_000);
 		 throw ex;
 		 }*/
 
 		if (reverseQueryStack != null && !reverseQueryStack.isEmpty())
 		{
 			Connection conn;
 			try
 			{
 				conn = DBUtil.getConnection();
 				while (!reverseQueryStack.empty())
 				{
 					String query = (String) reverseQueryStack.pop();
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
 				DynamicExtensionsSystemException ex = new DynamicExtensionsSystemException(message, e);
 				ex.setErrorCode(DYEXTN_S_000);
 				throw ex;
 			}
 		}
 	}
 
 	/**
 	 * This method is called when exception occurs while executing the rollback queries
 	 * or reverse queries. When this method is called , it signifies that the database state
 	 * and the metadata state for the entity are not in synchronisation and administrator
 	 * needs some database correction.
 	 * @param e The exception that took place.
 	 * @param entity Entity for which data tables are out of sync.
 	 */
 	protected void LogFatalError(Exception e, AbstractMetadataInterface abstractMetadata)
 	{
 		String table = "";
 		String name = "";
 		if (abstractMetadata != null)
 		{
 			EntityInterface entity = (EntityInterface) abstractMetadata;
 			entity.getTableProperties().getName();
 			name = entity.getName();
 		}
 		Logger.out.error("***Fatal Error.. Incosistent data table and metadata information for the entity -" + name + "***");
 		Logger.out.error("Please check the table -" + table);
 		Logger.out.error("The cause of the exception is - " + e.getMessage());
 		Logger.out.error("The detailed log is : ");
 		e.printStackTrace();
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAssociations(java.lang.Long, java.lang.Long)
 	 */
 	public Collection<AssociationInterface> getAssociations(Long sourceEntityId, Long targetEntityId) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
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
 	 * This method returns the EntityInterface given the entity name.
 	 * @param entityGroupShortName
 	 * @return
 	 */
 	public EntityInterface getEntityByName(String entityName) throws DynamicExtensionsSystemException
 	{
 		EntityInterface entityInterface = (EntityInterface) getObjectByName(Entity.class.getName(), entityName);
 		return entityInterface;
 	}
 
 	/**
 	 * Returns an association object given the entity name and source role name.
 	 * @param entityName
 	 * @param sourceRoleName
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 
 	public Collection<AssociationInterface> getAssociation(String entityName, String sourceRoleName) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		Map substitutionParameterMap = new HashMap();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("string", entityName));
 		substitutionParameterMap.put("1", new HQLPlaceHolderObject("string", sourceRoleName));
 		//Following method is called to execute the stored HQL , the name of which is given as the first parameter.
 		//The second parameter is the map which contains the actual values that are replaced for the placeholders.
 
 		Collection<AssociationInterface> associationCollection = executeHQL("getAssociation", substitutionParameterMap);
 
 		return associationCollection;
 	}
 
 	/**
 	 * Returns an association object given the entity name and source role name.
 	 * @param entityName
 	 * @param associationName
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 
 	public AssociationInterface getAssociationByName(String asociationName) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		Map substitutionParameterMap = new HashMap();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("string", asociationName));
 
 		//Following method is called to execute the stored HQL , the name of which is given as the first parameter.
 		//The second parameter is the map which contains the actual values that are replaced for the placeholders.
 
 		Collection<AssociationInterface> associationCollection = executeHQL("getAssociationByName", substitutionParameterMap);
 		AssociationInterface associationInterface = null;
 		if (associationCollection != null && associationCollection.size() != 0)
 		{
 			associationInterface = associationCollection.iterator().next();
 		}
 		return associationInterface;
 
 	}
 
 	public AssociationInterface getAssociation(String sourceEntityName, String asociationName, String targetEntityName)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Map substitutionParameterMap = new HashMap();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("string", sourceEntityName));
 		substitutionParameterMap.put("1", new HQLPlaceHolderObject("string", asociationName));
 		substitutionParameterMap.put("2", new HQLPlaceHolderObject("string", targetEntityName));
 
 		//Following method is called to execute the stored HQL , the name of which is given as the first parameter.
 		//The second parameter is the map which contains the actual values that are replaced for the placeholders.
 
 		Collection<AssociationInterface> associationCollection = executeHQL("getAssociationBySourceTargetEntity", substitutionParameterMap);
 		AssociationInterface associationInterface = null;
 		if (associationCollection != null && associationCollection.size() != 0)
 		{
 			associationInterface = associationCollection.iterator().next();
 		}
 		return associationInterface;
 
 	}
 
 	/**
 	 * This method returns the collection of entities given the concept code for the entity.
 	 * @param entityConceptCode concept code for the entity
 	 * @return entityCollection a collection of entities.
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public Collection<EntityInterface> getEntitiesByConceptCode(String entityConceptCode) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		Map substitutionParameterMap = new HashMap();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("string", entityConceptCode));
 		//Following method is called to execute the stored HQL , the name of which is given as the first parameter.
 		//The second parameter is the map which contains the actual values that are replaced for the placeholders.
 		Collection entityCollection = executeHQL("getEntitiesByConceptCode", substitutionParameterMap);
 		return entityCollection;
 	}
 
 	/**
 	 * Returns all entities in the whole system
 	 * @return Collection Entity Collection
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public Collection<EntityInterface> getAllEntities() throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		//CAlling generic method to return all stored instances of the object, the class name of which is passed as
 		//the parameter.
 		return getAllObjects(EntityInterface.class.getName());
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getEntityByIdentifier(java.lang.Long)
 	 */
 	public EntityInterface getEntityByIdentifier(Long id) throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		//      CAlling generic method to return all stored instances of the object, the identifier of which is passed as
 		//the parameter.
 		return (EntityInterface) getObjectByIdentifier(EntityInterface.class.getName(), id.toString());
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getEntityByIdentifier(java.lang.String)
 	 */
 	public EntityInterface getEntityByIdentifier(String identifier) throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		//      CAlling generic method to return all stored instances of the object, the identifier of which is passed as
 		//the parameter.
 		return (EntityInterface) getObjectByIdentifier(EntityInterface.class.getName(), identifier);
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAllContainers()
 	 */
 	public Collection<ContainerInterface> getAllContainers() throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		//CAlling generic method to return all stored instances of the object, the class name of which is passed as
 		//the parameter.
 		return getAllObjects(ContainerInterface.class.getName());
 	}
 
 	public Collection<ContainerInterface> getAllContainersByEntityGroupId(Long entityGroupIdentifier) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 
 	{
 		Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", entityGroupIdentifier));
 		return executeHQL("getAllContainersByEntityGroupId", substitutionParameterMap);
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#insertData(edu.common.dynamicextensions.domaininterface.EntityInterface, java.util.Map)
 	 */
 	public Long insertData(EntityInterface entity, Map<AbstractAttributeInterface, Object> dataValue, Long... userId)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		List<Map<AbstractAttributeInterface, Object>> dataValueMapList = new ArrayList<Map<AbstractAttributeInterface, Object>>();
 		dataValueMapList.add(dataValue);
 		Long uId = ((userId != null && userId.length != 0) ? userId[0] : null);
 
 		List<Long> recordIdList = insertData(entity, dataValueMapList, uId);
 		return recordIdList.get(0);
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#insertData(edu.common.dynamicextensions.domaininterface.EntityInterface, java.util.Map)
 	 */
 	public List<Long> insertData(EntityInterface entity, List<Map<AbstractAttributeInterface, Object>> dataValueMapList, Long... userId)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 
 		List<Long> recordIdList = new ArrayList<Long>();
 		Long uId = ((userId != null && userId.length != 0) ? userId[0] : null);
 
 		JDBCDAO jdbcDao = null;
 		try
 		{
 			DAOFactory factory = DAOFactory.getInstance();
 			jdbcDao = (JDBCDAO) factory.getDAO(Constants.JDBC_DAO);
 			jdbcDao.openSession(null);
 
 			for (Map<AbstractAttributeInterface, ?> dataValue : dataValueMapList)
 			{
 				Long recordId = insertDataForHeirarchy(entity, dataValue, jdbcDao, uId);
 				recordIdList.add(recordId);
 			}
 
 			jdbcDao.commit();
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			throw (DynamicExtensionsApplicationException) handleRollback(e, "Error while inserting data", jdbcDao, false);
 		}
 		catch (Exception e)
 		{
 			throw (DynamicExtensionsSystemException) handleRollback(e, "Error while inserting data", jdbcDao, true);
 		}
 		finally
 		{
 			try
 			{
 				jdbcDao.closeSession();
 			}
 			catch (DAOException e)
 			{
 				throw (DynamicExtensionsSystemException) handleRollback(e, "Error while closing", jdbcDao, true);
 			}
 		}
 
 		return recordIdList;
 	}
 
 	/**
 	 * @param entity
 	 * @param dataValue
 	 * @param jdbcDao
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws HibernateException
 	 * @throws SQLException
 	 * @throws DAOException
 	 * @throws UserNotAuthorizedException
 	 * @throws IOException
 	 * @throws ParseException
 	 */
 	public Long insertDataForHeirarchy(EntityInterface entity, Map<AbstractAttributeInterface, ?> dataValue, JDBCDAO jdbcDao, Long... id)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		List<EntityInterface> entityList = getParentEntityList(entity);
 		Long uId = ((id != null || id.length > 0) ? id[0] : null);
 		Map<EntityInterface, Map> entityValueMap = initialiseEntityValueMap(entity, dataValue);
 		Long parentRecordId = null;
 		for (EntityInterface entityInterface : entityList)
 		{
 			Map valueMap = entityValueMap.get(entityInterface);
 			parentRecordId = insertDataForSingleEntity(entityInterface, valueMap, jdbcDao, parentRecordId, uId);
 		}
 
 		return parentRecordId;
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
 	 * @param entity
 	 * @param dataValue
 	 * @return
 	 */
 	private Map<EntityInterface, Map> initialiseEntityValueMap(EntityInterface entity, Map<AbstractAttributeInterface, ?> dataValue)
 	{
 		Map<EntityInterface, Map> entityMap = new HashMap<EntityInterface, Map>();
 		//Ensuring Null check in case of Category Inheritance
 		if (dataValue == null)
 		{
 			dataValue = new HashMap();
 		}
 		for (AbstractAttributeInterface abstractAttributeInterface : dataValue.keySet())
 		{
 			EntityInterface attributeEntity = abstractAttributeInterface.getEntity();
 			Object value = dataValue.get(abstractAttributeInterface);
 
 			Map<AbstractAttributeInterface, Object> entityDataValueMap = (Map) entityMap.get(attributeEntity);
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
 	 * @param dataValue
 	 * @param jdbcDao
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws HibernateException
 	 * @throws SQLException
 	 * @throws DAOException
 	 * @throws UserNotAuthorizedException
 	 * @throws IOException
 	 * @throws ParseException
 	 */
 	public Long insertDataForSingleEntity(EntityInterface entity, Map dataValue, JDBCDAO jdbcDao, Long parentRecordId, Long... id)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Long uId = ((id != null && id.length != 0) ? id[0] : null);
 		if (entity == null)
 		{
 			throw new DynamicExtensionsSystemException("Input to insert data is null");
 		}
 
 		// if empty, insert row with only identifer column value.
 		if (dataValue == null)
 		{
 			dataValue = new HashMap();
 		}
 
 		List<Object> columnValues = new ArrayList<Object>();
 		List<String> columnNames = new ArrayList<String>();
 
 		Long identifier = null;
 		if (parentRecordId != null)
 		{
 			identifier = parentRecordId;
 		}
 		else
 		{
 			identifier = entityManagerUtil.getNextIdentifier(entity.getTableProperties().getName());
 		}
 
 		StringBuffer queryValuesString = new StringBuffer();
 		StringBuffer queryString = new StringBuffer(INSERT_INTO_KEYWORD);
 		queryString.append(entity.getTableProperties().getName() + WHITESPACE);
 
 		queryString.append(OPENING_BRACKET);
 		queryString.append("IDENTIFIER,");
 		queryString.append(Constants.ACTIVITY_STATUS_COLUMN);
 
 		columnNames.add("IDENTIFIER ");
 		columnValues.add(identifier);
 		queryValuesString.append(VALUES_KEYWORD);
 		queryValuesString.append(OPENING_BRACKET);
 		queryValuesString.append(identifier);
 		queryValuesString.append(COMMA);
 
 		columnNames.add(Constants.ACTIVITY_STATUS_COLUMN);
 		columnValues.add(Constants.ACTIVITY_STATUS_ACTIVE);
 		queryValuesString.append(Constants.ACTIVITY_STATUS_ACTIVE);
 		queryValuesString.append(COMMA);
 
 		String tableName = entity.getTableProperties().getName();
 
 		Set uiColumnSet = dataValue.keySet();
 		Iterator uiColumnSetIter = uiColumnSet.iterator();
 		List<String> queryList = new ArrayList<String>();
 		Object value = null;
 
 		DynamicExtensionBaseQueryBuilder baseQueryBuilder = new DynamicExtensionBaseQueryBuilder();
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
 				updateColumnNamesAndColumnValues(attribute, value, columnNames, columnValues);
 				if (((AttributeInterface) attribute).getAttributeTypeInformation() instanceof FileAttributeTypeInformation)
 				{
 					queryString.append(COMMA);
 					queryValuesString.append(COMMA);
 					queryString.append(attribute.getName() + UNDERSCORE + FILE_NAME);
 					queryValuesString.append(((FileAttributeRecordValue) value).getFileName());
 					queryValuesString.append(COMMA);
 					queryString.append(COMMA);
 					queryString.append(attribute.getName() + UNDERSCORE + CONTENT_TYPE);
 					queryValuesString.append(((FileAttributeRecordValue) value).getContentType());
 					queryString.append(COMMA);
 					queryValuesString.append(((FileAttributeRecordValue) value).getFileContent());
 				}
 				else
 				{
 					queryValuesString.append(COMMA);
 					queryValuesString.append(baseQueryBuilder.getFormattedValue(attribute, value));
 					queryString.append(COMMA);
 					queryString.append(((AttributeInterface) attribute).getColumnProperties().getName());
 				}
 			}
 			else
 			{
 				//In case of association separate queries need to fire depending on the cardinalities
 				AssociationInterface association = (AssociationInterface) attribute;
 				List<Long> recordIdList = null;
 
 				if (association.getSourceRole().getAssociationsType().equals(AssociationType.CONTAINTMENT)
 						|| (association.getSourceRole().getAssociationsType().equals(AssociationType.ASSOCIATION) && association.getIsCollection()))
 				{
 					List<Map> listOfMapsForContainedEntity = (List) value;
 					recordIdList = new ArrayList<Long>();
 
 					//Map valueMapForContainedEntity = (Map) value;
 					for (Map valueMapForContainedEntity : listOfMapsForContainedEntity)
 					{
 						//                      Long recordIdForContainedEntity = insertDataForSingleEntity(association
 						//                              .getTargetEntity(), valueMapForContainedEntity, hibernateDAO, null);
 
 						Long recordIdForContainedEntity = insertDataForHeirarchy(association.getTargetEntity(), valueMapForContainedEntity, jdbcDao,
 								uId);
 						recordIdList.add(recordIdForContainedEntity);
 					}
 
 				}
 				else
 				{
 					recordIdList = (List<Long>) value;
 				}
 
 				queryList.addAll(queryBuilder.getAssociationInsertDataQuery(association, recordIdList, identifier));
 
 			}
 		}
 
 		try
 		{
 			jdbcDao.insert(tableName, columnValues, columnNames);
 
 			queryString.append(CLOSING_BRACKET);
 			queryValuesString.append(CLOSING_BRACKET);
 			queryString.append(queryValuesString);
 
 			jdbcDao.insert(DomainObjectFactory.getInstance().createDESQLAudit(uId, queryString.toString()), null, false, false);
 
 			Connection conn = DBUtil.getConnection();
 
 			for (String string : queryList)
 			{
 				logDebug("insertData", "Query for insert data is : " + queryString);
 				PreparedStatement statement = conn.prepareStatement(string);
 				statement.executeUpdate();
 				jdbcDao.insert(DomainObjectFactory.getInstance().createDESQLAudit(uId, queryString.toString()), null, false, false);
 			}
 		}
 		catch (SQLException e)
 		{
 			throw new DynamicExtensionsApplicationException("Exception in query execution", e);
 		}
 		catch (DAOException e)
 		{
 			throw new DynamicExtensionsApplicationException("Exception in query execution", e);
 		}
 		catch (UserNotAuthorizedException e)
 		{
 			throw new DynamicExtensionsApplicationException("Exception in query execution", e);
 		}
 
 		return identifier;
 
 	}
 
 	/**
 	 * @param attribute
 	 * @param value
 	 * @param columnNames
 	 * @param columnValues
 	 * @throws DynamicExtensionsApplicationException 
 
 	 */
 	private void updateColumnNamesAndColumnValues(AbstractAttribute attribute, Object value, List<String> columnNames, List<Object> columnValues)
 			throws DynamicExtensionsApplicationException
 	{
 		AttributeInterface primitiveAttribute = (AttributeInterface) attribute;
 
 		// populate FileAttributeRecordValue HO
 		if (primitiveAttribute.getAttributeTypeInformation() instanceof FileAttributeTypeInformation)
 		{
 			if (!(value instanceof String))
 			{
 				populateFileAttribute(columnNames, columnValues, (FileAttributeRecordValue) value, primitiveAttribute);
 			}
 		}
 		else
 		{
 
 			try
 			{
 				columnNames.add(primitiveAttribute.getColumnProperties().getName());
 				if (primitiveAttribute.getAttributeTypeInformation() instanceof DateAttributeTypeInformation)
 				{
 					if (value != null && value.toString().length() != 0)
 					{
 						String dateFormat = ((DateAttributeTypeInformation) primitiveAttribute.getAttributeTypeInformation()).getFormat();
 						value = new Timestamp(new SimpleDateFormat(dateFormat).parse(value.toString()).getTime());
 					}
 					else
 					{
 						value = "";
 					}
 				}
 				else if (primitiveAttribute.getAttributeTypeInformation() instanceof ObjectAttributeTypeInformation)
 				{
 					ByteArrayOutputStream bStream = new ByteArrayOutputStream();
 					ObjectOutputStream oStream = new ObjectOutputStream(bStream);
 					oStream.writeObject(value);
 
 					value = bStream.toByteArray();
 				}
 				columnValues.add(value);
 			}
 			catch (ParseException e)
 			{
 				throw new DynamicExtensionsApplicationException("Exception in parsing date for attribute " + attribute.getName(), e);
 			}
 			catch (IOException e)
 			{
 				throw new DynamicExtensionsApplicationException("Exception while creating blob for object type attribute " + attribute.getName(), e);
 			}
 		}
 	}
 
 	/**
 	 * This method adds the extra columns information
 	 * that needs to be maintained while adding the file data
 	 * @param columnNames list of column names
 	 * @param columnValues list of column values
 	 * @param value file attribute value
 	 * @param attribute
 	 */
 	private void populateFileAttribute(List<String> columnNames, List<Object> columnValues, FileAttributeRecordValue value,
 			AttributeInterface attribute)
 	{
 		columnNames.add(attribute.getName() + UNDERSCORE + FILE_NAME);
 		columnValues.add(value.getFileName());
 
 		columnNames.add(attribute.getName() + UNDERSCORE + CONTENT_TYPE);
 		columnValues.add(value.getContentType());
 
 		columnNames.add(attribute.getColumnProperties().getName());
 		columnValues.add(value.getFileContent());
 
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
 	private AttributeRecord populateCollectionAttributeRecord(AttributeRecord collectionRecord, EntityInterface entity,
 			AttributeInterface primitiveAttribute, Long identifier, List<String> values)
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
 		Collection<CollectionAttributeRecordValue> valueCollection = collectionRecord.getValueCollection();
 
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
 
 	private AttributeRecord populateObjectAttributeRecord(AttributeRecord objectRecord, EntityInterface entity,
 			AttributeInterface primitiveAttribute, Long identifier, ObjectAttributeRecordValue value)
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
 	 * Populates AttributeRecord object for given entity and record id
 	 *
 	 * @param fileRecord if null creates a new AttributeRecord objec t, otheerwise updates the existing one
 	 * @param entity for which this AttributeRecord object belongs
 	 * @param primitiveAttribute  for which this AttributeRecord object belongs
 	 * @param identifier for which this AttributeRecord object belongs
 	 * @param value the new values for the file type attribute
 	 * @return
 	 */
 	private AttributeRecord populateFileAttributeRecord(AttributeRecord fileRecord, EntityInterface entity, AttributeInterface primitiveAttribute,
 			Long identifier, FileAttributeRecordValue value)
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
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#editData(edu.common.dynamicextensions.domaininterface.EntityInterface, java.util.Map, java.lang.Long)
 	 */
 	public boolean editData(EntityInterface entity, Map<AbstractAttributeInterface, ?> dataValue, Long recordId, Long... userId)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 
 		boolean isSuccess = false;
 		Long uId = ((userId != null && userId.length != 0) ? userId[0] : null);
 
 		JDBCDAO jdbcDAO = null;
 		try
 		{
 
 			DAOFactory factory = DAOFactory.getInstance();
 			jdbcDAO = (JDBCDAO) factory.getDAO(Constants.JDBC_DAO);
 
 			jdbcDAO.openSession(null);
 			List<EntityInterface> entityList = getParentEntityList(entity);
 			Map<EntityInterface, Map> entityValueMap = initialiseEntityValueMap(entity, dataValue);
 			for (EntityInterface entityInterface : entityList)
 			{
 				Map valueMap = entityValueMap.get(entityInterface);
 				isSuccess = editDataForSingleEntity(entityInterface, valueMap, recordId, jdbcDAO, uId);
 			}
 
 			jdbcDAO.commit();
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			throw (DynamicExtensionsApplicationException) handleRollback(e, "Error while inserting data", jdbcDAO, false);
 		}
 		catch (Exception e)
 		{
 			throw (DynamicExtensionsSystemException) handleRollback(e, "Error while updating", jdbcDAO, true);
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
 
 		return isSuccess;
 	}
 
 	/**
 	 * @param entity
 	 * @param dataValue
 	 * @param recordId
 	 * @param jdbcDAO
 	 * @param userId
 	 * @return
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public boolean editDataForHeirarchy(EntityInterface entity, Map<AbstractAttributeInterface, ?> dataValue, Long recordId, JDBCDAO jdbcDAO,
 			Long... userId) throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		boolean isSuccess = false;
 		Long uId = ((userId != null && userId.length != 0) ? userId[0] : null);
 		List<EntityInterface> entityList = getParentEntityList(entity);
 		Map<EntityInterface, Map> entityValueMap = initialiseEntityValueMap(entity, dataValue);
 		try
 		{
 			for (EntityInterface entityInterface : entityList)
 			{
 				Map valueMap = entityValueMap.get(entityInterface);
 				isSuccess = editDataForSingleEntity(entityInterface, valueMap, recordId, jdbcDAO, uId);
 			}
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			throw (DynamicExtensionsApplicationException) handleRollback(e, "Error while inserting data", jdbcDAO, false);
 		}
 		catch (Exception e)
 		{
 			throw (DynamicExtensionsSystemException) handleRollback(e, "Error while updating", jdbcDAO, true);
 		}
 		return isSuccess;
 	}
 
 	/**
 	 * @param entity
 	 * @param dataValue
 	 * @param recordId
 	 * @return
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public boolean editDataForSingleEntity(EntityInterface entity, Map dataValue, Long recordId, JDBCDAO jdbcDAO, Long... userId)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Long uId = ((userId != null && userId.length != 0) ? userId[0] : null);
 
 		if (entity == null || dataValue == null || dataValue.isEmpty())
 		{
 			return true;
 		}
 		List<String> columnNames = new ArrayList<String>();
 		List<Object> columnValues = new ArrayList<Object>();
 
 		String tableName = entity.getTableProperties().getName();
 
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
 				updateColumnNamesAndColumnValues(attribute, value, columnNames, columnValues);
 			}
 			else
 			{
 				AssociationInterface association = (AssociationInterface) attribute;
 				List<Long> recordIdList = new ArrayList<Long>();
 
 				if (association.getSourceRole().getAssociationsType().equals(AssociationType.CONTAINTMENT)
 						|| (association.getSourceRole().getAssociationsType().equals(AssociationType.ASSOCIATION) && association.getIsCollection()))
 				{
 					List<String> removeContainmentRecordQuery = new ArrayList<String>();
 					recordIdList.add(recordId);
 
 					queryBuilder.getContenmentAssociationRemoveDataQueryList(((Association) attribute), recordIdList, removeContainmentRecordQuery,
 							false);
 
					entityManagerUtil.executeDML(removeContainmentRecordQuery);
 
 					List<Map> listOfMapsForContainedEntity = (List<Map>) value;
 					recordIdList.clear();
 					for (Map valueMapForContainedEntity : listOfMapsForContainedEntity)
 					{
 						//Long childRecordId = insertDataForSingleEntity(association
 						//.getTargetEntity(), valueMapForContainedEntity, hibernateDAO, null);
 						Long childRecordId = insertDataForHeirarchy(association.getTargetEntity(), valueMapForContainedEntity, jdbcDAO, uId);
 						recordIdList.add(childRecordId);
 					}
 
 				}
 				else
 				{
 					// for association need to remove previously associated target reocrd first.
 					String removeQuery = queryBuilder.getAssociationRemoveDataQuery(((Association) attribute), recordId);
 
 					if (removeQuery != null && removeQuery.trim().length() != 0)
 					{
 						associationRemoveDataQueryList.add(removeQuery);
 					}
 
 					recordIdList = (List<Long>) value;
 				}
 
 				//then add new associated target records.
 				List insertQuery = queryBuilder.getAssociationInsertDataQuery(((Association) attribute), recordIdList, recordId);
 				if (insertQuery != null && insertQuery.size() != 0)
 				{
 					associationInsertDataQueryList.addAll(insertQuery);
 				}
 
 			}
 		}
 
 		List<String> editDataQueryList = new ArrayList<String>();
 		editDataQueryList.addAll(associationRemoveDataQueryList);
 		editDataQueryList.addAll(associationInsertDataQueryList);
 
 		// Shift the below code into the jdbcdao 
 		Connection conn = jdbcDAO.getConnection();
 
 		try
 		{
 			jdbcDAO.setAutoCommit(false);
 			if (columnNames.size() != 0)
 			{
 				StringBuffer query = new StringBuffer("UPDATE " + tableName + " SET ");
 				StringBuffer auditQuery = new StringBuffer("UPDATE " + tableName + " SET ");
 				Iterator<String> iterator = columnNames.iterator();
 				String columnName = null;
 				while (iterator.hasNext())
 				{
 					columnName = iterator.next();
 					query.append(columnName + EQUAL + "?");
 					auditQuery.append(columnName + EQUAL + "?");
 					if (iterator.hasNext())
 					{
 						query.append(COMMA + WHITESPACE);
 						auditQuery.append(COMMA + WHITESPACE);
 
 					}
 
 				}
 				query.append(WHERE_KEYWORD);
 				query.append(IDENTIFIER);
 				query.append(WHITESPACE + EQUAL + WHITESPACE);
 				query.append(recordId);
 
 				PreparedStatement preparedStatement;
 
 				preparedStatement = conn.prepareStatement(query.toString());
 
 				int i = 1;
 				String value = null;
 				for (Object columnValue : columnValues)
 				{
 					preparedStatement.setObject(i++, columnValue);
 					auditQuery.replace(auditQuery.indexOf("?"), auditQuery.indexOf("?") + 1, columnValue == null ? "" : columnValue.toString());
 				}
 				preparedStatement.setMaxFieldSize(1);
 				preparedStatement.executeUpdate();
 				jdbcDAO.insert(DomainObjectFactory.getInstance().createDESQLAudit(uId, auditQuery.toString()), null, false, false);
 			}
 
 			for (String queryString : editDataQueryList)
 			{
 				logDebug("editData", "Query is: " + queryString.toString());
 				PreparedStatement statement = conn.prepareStatement(queryString);
 				statement.executeUpdate();
 			}
 		}
 		catch (SQLException e)
 		{
 			throw new DynamicExtensionsApplicationException("Exception in editing data", e);
 		}
 		catch (DAOException e)
 		{
 			throw new DynamicExtensionsApplicationException("Exception in editing data", e);
 		}
 		catch (UserNotAuthorizedException e)
 		{
 			throw new DynamicExtensionsApplicationException("Exception in editing data", e);
 		}
 		return true;
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
 	public Map<AbstractAttributeInterface, Object> getEntityRecordById(EntityInterface entity, Long recordId)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Map<AbstractAttributeInterface, Object> recordValues = new HashMap<AbstractAttributeInterface, Object>();
 
 		Collection attributesCollection = entity.getAttributeCollection();
 		attributesCollection = entityManagerUtil.filterSystemAttributes(attributesCollection);
 		String tableName = entity.getTableProperties().getName();
 		List<String> selectColumnNameList = new ArrayList<String>();
 
 		Iterator attriIterator = attributesCollection.iterator();
 		Map columnNameMap = new HashMap();
 		while (attriIterator.hasNext())
 		{
 			AttributeInterface attribute = (AttributeInterface) attriIterator.next();
 			String dbColumnName = null;
 			//for the other attributes, create select query.
 			if (attribute.getAttributeTypeInformation() instanceof FileAttributeTypeInformation)
 			{
 				dbColumnName = attribute.getName() + UNDERSCORE + FILE_NAME;
 			}
 			else
 			{
 				dbColumnName = attribute.getColumnProperties().getName();
 			}
 			selectColumnNameList.add(dbColumnName);
 			columnNameMap.put(dbColumnName, attribute);
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
 
 				query.append(WHITESPACE).append(FROM_KEYWORD).append(WHITESPACE).append(tableName).append(WHITESPACE).append(WHERE_KEYWORD).append(
 						WHITESPACE).append(IDENTIFIER).append(EQUAL).append(recordId);
 				/*get values for simple attributes*/
 
 				recordValues.putAll(getAttributeValues(selectColumnNameList, query.toString(), columnNameMap));
 			}
 		}
 		catch (SQLException e)
 		{
 			throw new DynamicExtensionsSystemException("Error while retrieving the data", e);
 		}
 
 		return recordValues;
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
 	private List<String> getCollectionAttributeRecordValues(Long entityId, Long attributeId, Long recordId) throws DynamicExtensionsSystemException
 
 	{
 		List<String> valueList = null;
 		AttributeRecord collectionAttributeRecord = getAttributeRecord(entityId, attributeId, recordId, null);
 		if (collectionAttributeRecord != null)
 		{
 			Collection<CollectionAttributeRecordValue> recordValueCollection = collectionAttributeRecord.getValueCollection();
 
 			valueList = new ArrayList<String>();
 			for (CollectionAttributeRecordValue recordValue : recordValueCollection)
 			{
 				valueList.add(recordValue.getValue());
 			}
 		}
 		return valueList;
 	}
 
 	/**
 	 * @param entityId
 	 * @param attributeId
 	 * @param recordId
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private ObjectAttributeRecordValueInterface getObjectAttributeRecordValue(Long entityId, Long attributeId, Long recordId)
 			throws DynamicExtensionsSystemException
 
 	{
 		AttributeRecord record = getAttributeRecord(entityId, attributeId, recordId, null);
 		ObjectAttributeRecordValue objectAttributeRecordValue = null;
 		if (record != null)
 		{
 			objectAttributeRecordValue = record.getObjectRecord();
 		}
 		return objectAttributeRecordValue;
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
 	private FileAttributeRecordValue getFileAttributeRecordValue(Long entityId, Long attributeId, Long recordId)
 			throws DynamicExtensionsSystemException
 
 	{
 		AttributeRecord record = getAttributeRecord(entityId, attributeId, recordId, null);
 		FileAttributeRecordValue fileAttributeRecordValue = null;
 		if (record != null)
 		{
 			fileAttributeRecordValue = record.getFileRecord();
 		}
 		return fileAttributeRecordValue;
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
 	private AttributeRecord getAttributeRecord(Long entityId, Long attributeId, Long recordId, HibernateDAO hibernateDao)
 			throws DynamicExtensionsSystemException
 
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
 			recordCollection = executeHQL(hibernateDao, "getCollectionAttributeRecord", substitutionParameterMap);
 		}
 		if (recordCollection != null && !recordCollection.isEmpty())
 		{
 			collectionAttributeRecord = (AttributeRecord) recordCollection.iterator().next();
 		}
 		return collectionAttributeRecord;
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
 	private Map<AbstractAttributeInterface, Object> getAttributeValues(List<String> selectColumnNameList, String query, Map columnNameMap)
 			throws DynamicExtensionsSystemException, SQLException
 	{
 		Map<AbstractAttributeInterface, Object> recordValues = new HashMap<AbstractAttributeInterface, Object>();
 
 		Statement statement = null;
 		ResultSet resultSet = null;
 		try
 		{
 			Connection conn = DBUtil.getConnection();
 			statement = conn.createStatement();
 			resultSet = statement.executeQuery(query);
 
 			if (resultSet.next())
 			{
 				for (int i = 0; i < selectColumnNameList.size(); i++)
 				{
 					String dbColumnName = selectColumnNameList.get(i);
 					Object value = getValueFromResultSet(resultSet, columnNameMap, dbColumnName, i);
 					Attribute attribute = (Attribute) columnNameMap.get(dbColumnName);
 					recordValues.put(attribute, value);
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			throw new SQLException(e.getMessage());
 		}
 		finally
 		{
 			resultSet.close();
 			statement.close();
 		}
 
 		return recordValues;
 	}
 
 	private Object getValueFromResultSet(ResultSet resultSet, Map columnNameMap, String dbColumnName, int index) throws SQLException, IOException,
 			ClassNotFoundException
 	{
 		Attribute attribute = (Attribute) columnNameMap.get(dbColumnName);
 
 		Object valueObj = resultSet.getObject(index + 1);
 		Object value = "";
 
 		if (valueObj != null)
 		{
 			if (valueObj instanceof java.util.Date)
 			{
 
 				DateAttributeTypeInformation dateAttributeTypeInf = (DateAttributeTypeInformation) attribute.getAttributeTypeInformation();
 
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
 				if (attribute.getAttributeTypeInformation() instanceof ObjectAttributeTypeInformation)
 				{
 					if (Variables.databaseName.equals(Constants.ORACLE_DATABASE))
 					{
 						Blob blob = (Blob) valueObj;
 						value = new ObjectInputStream(blob.getBinaryStream()).readObject();
 					}
 					if (Variables.databaseName.equals(Constants.MYSQL_DATABASE))
 					{
 						ByteArrayInputStream bais = new ByteArrayInputStream((byte[]) valueObj);
 						value = new ObjectInputStream(bais).readObject();
 					}
 
 				}
 				else
 				{
 					value = valueObj;
 				}
 			}
 		}
 
 		//All objects on the UI are handled as String, so objects string value needs to be 
 		//stored in the map 
 		if (!(((AttributeInterface) attribute).getAttributeTypeInformation() instanceof FileAttributeTypeInformation)
 				&& !(((AttributeInterface) attribute).getAttributeTypeInformation() instanceof ObjectAttributeTypeInformation))
 		{
 			value = value.toString();
 		}
 		return value;
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
 			List<? extends AbstractAttributeInterface> abstractAttributeCollection, List<Long> recordIds) throws DynamicExtensionsSystemException
 	{
 		if (abstractAttributeCollection == null || abstractAttributeCollection.isEmpty())
 		{
 			return null;
 		}
 		//      List<AbstractAttributeInterface> tempAbstractAttributeCollection = new ArrayList(
 		//              abstractAttributeCollection);
 		//      tempAbstractAttributeCollection = EntityManagerUtil
 		//              .filterSystemAttributes(tempAbstractAttributeCollection);
 
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
 		//      attributesCollection = EntityManagerUtil.filterSystemAttributes(attributesCollection);
 
 		String tableName = entity.getTableProperties().getName();
 		List<String> selectColumnNameList = new ArrayList<String>();
 
 		Iterator attriIterator = attributesCollection.iterator();
 		Map columnNameMap = new HashMap();
 
 		while (attriIterator.hasNext())
 		{
 			AttributeInterface attribute = (AttributeInterface) attriIterator.next();
 			//for the other attributes, create select query.
 			String dbColumnName = attribute.getColumnProperties().getName();
 			selectColumnNameList.add(dbColumnName);
 			columnNameMap.put(dbColumnName, attribute);
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
 				query.append(WHERE_KEYWORD).append(IDENTIFIER).append(IN_KEYWORD).append(EntityManagerUtil.getListToString(recordIds));
 			}
 
 			/*get values for simple attributes*/
 
 			List<EntityRecordInterface> entityRecordList = getEntityRecordList(selectColumnNameList, query.toString(), columnNameMap, recordMetadata);
 
 			entityRecordResult.setEntityRecordList(entityRecordList);
 			for (EntityRecordInterface entityRecord : entityRecordList)
 			{
 				Long recordId = entityRecord.getRecordId();
 				queryBuilder.putAssociationValues(associationCollection, entityRecordResult, entityRecord, recordId);
 			}
 
 		}
 		catch (SQLException e)
 		{
 			throw new DynamicExtensionsSystemException("Error while retrieving the data", e);
 		}
 		catch (IOException e)
 		{
 			throw new DynamicExtensionsSystemException("Error while retrieving the data", e);
 		}
 		catch (ClassNotFoundException e)
 		{
 			throw new DynamicExtensionsSystemException("Error while retrieving the data", e);
 		}
 
 		return entityRecordResult;
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
 	 * @throws ClassNotFoundException
 	 * @throws IOException
 	 */
 	private List<EntityRecordInterface> getEntityRecordList(List<String> selectColumnNameList, String query, Map columnNameMap,
 			EntityRecordMetadata recordMetadata) throws DynamicExtensionsSystemException, SQLException, IOException, ClassNotFoundException
 	{
 		List<EntityRecordInterface> entityRecordList = new ArrayList<EntityRecordInterface>();
 		ResultSet resultSet = null;
 		try
 		{
 			resultSet = entityManagerUtil.executeQuery(query);
 			while (resultSet.next())
 			{
 				EntityRecordInterface entityRecord = new EntityRecord();
 				Long id = resultSet.getLong(1);
 				entityRecord.setRecordId(id);
 				Object[] values = new Object[recordMetadata.getAttributeList().size()];
 				for (int i = 1; i <= selectColumnNameList.size(); i++)
 				{
 					String dbColumnName = selectColumnNameList.get(i - 1);
 					Object value = getValueFromResultSet(resultSet, columnNameMap, dbColumnName, i);
 					AttributeInterface attribute = (AttributeInterface) columnNameMap.get(dbColumnName);
 					int indexOfAttribute = recordMetadata.getAttributeList().indexOf(attribute);
 					values[indexOfAttribute] = value;
 				}
 				entityRecord.setRecordValueList(Arrays.asList(values));
 				entityRecordList.add(entityRecord);
 			}
 		}
 		finally
 		{
 			if (resultSet != null)
 			{
 				try
 				{
 					resultSet.close();
 				}
 				catch (SQLException e)
 				{
 					throw new DynamicExtensionsSystemException(e.getMessage(), e);
 				}
 			}
 		}
 		return entityRecordList;
 	}
 
 	/**
 	 * filers abstractAttributes into attributes and associations
 	 * @param abstractAttributeCollection
 	 * @param attributesCollection
 	 * @param associationCollection
 	 */
 	private void filterAttributes(List<? extends AbstractAttributeInterface> abstractAttributeCollection,
 			Collection<AttributeInterface> attributesCollection, Collection<AssociationInterface> associationCollection)
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
 	 * This method retrives the data for given entity for given record. It also returns the values of
 	 * any inherited attributes.
 	 *
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getRecordById(edu.common.dynamicextensions.domaininterface.EntityInterface, java.lang.Long)
 	 */
 	public Map<AbstractAttributeInterface, Object> getRecordById(EntityInterface entity, Long recordId) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		Map<AbstractAttributeInterface, Object> recordValues = new HashMap<AbstractAttributeInterface, Object>();
 		HibernateDAO hibernateDAO = null;
 		try
 		{
 			DAOFactory factory = DAOFactory.getInstance();
 			hibernateDAO = (HibernateDAO) factory.getDAO(Constants.HIBERNATE_DAO);
 			hibernateDAO.openSession(null);
 			if (entity == null || entity.getId() == null || recordId == null)
 			{
 				throw new DynamicExtensionsSystemException("Invalid Input");
 			}
 			do
 			{
 				Map<AbstractAttributeInterface, Object> recordValuesForSingleEntity = getEntityRecordById(entity, recordId);
 				recordValues.putAll(recordValuesForSingleEntity);
 				entity = entity.getParentEntity();
 			}
 			while (entity != null);
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			throw (DynamicExtensionsApplicationException) handleRollback(e, "Error while retriving data", hibernateDAO, false);
 		}
 		catch (Exception e)
 		{
 			throw (DynamicExtensionsSystemException) handleRollback(e, "Error while retriving data", hibernateDAO, true);
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
 		return recordValues;
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#deleteRecord(edu.common.dynamicextensions.domaininterface.EntityInterface, java.lang.Long)
 	 */
 	public boolean deleteRecord(EntityInterface entity, Long recordId) throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
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
 					AttributeTypeInformationInterface typeInfo = attribute.getAttributeTypeInformation();
 					// remove AttributeRecord objects for multi select and file type attributes
 					if (typeInfo instanceof FileAttributeTypeInformation || typeInfo instanceof ObjectAttributeTypeInformation)
 					{
 						AttributeRecord collectionAttributeRecord = getAttributeRecord(entity.getId(), attribute.getId(), recordId, hibernateDAO);
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
 
 					if (association.getSourceRole().getAssociationsType().equals(AssociationType.CONTAINTMENT))
 					{
 						List<Long> recordIdList = new ArrayList<Long>();
 						recordIdList.add(recordId);
 						QueryBuilderFactory.getQueryBuilder().getContenmentAssociationRemoveDataQueryList(association, recordIdList,
 								associationRemoveQueryList, true);
 
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
 			query.append(SET_KEYWORD + Constants.ACTIVITY_STATUS_COLUMN + EQUAL + " '" + Constants.ACTIVITY_STATUS_DISABLED + "' ");
 			query.append(WHERE_KEYWORD + WHITESPACE + IDENTIFIER + WHITESPACE + EQUAL + WHITESPACE + recordId.toString());
 
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
 			/*try
 			 {*/
 			hibernateDAO.rollback();
 			/*}
 			 catch (DAOException e1)
 			 {
 			 throw new DynamicExtensionsSystemException(e.getMessage(), e, DYEXTN_S_001);
 			 }*/
 
 			throw e;
 		}
 		catch (Exception e)
 		{
 			/*try
 			 {*/
 			hibernateDAO.rollback();
 			/*}
 			 catch (DAOException e1)
 			 {
 			 throw new DynamicExtensionsSystemException(e.getMessage(), e, DYEXTN_S_001);
 			 }*/
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
 
 	public void deleteRecords(Long containerId, List<Long> recordIdList) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		ContainerInterface container = DynamicExtensionsUtility.getContainerByIdentifier(containerId.toString());
 
 		EntityInterface entityInterface = (EntityInterface) container.getAbstractEntity();
 		for (Long recordId : recordIdList)
 		{
 			deleteRecord(entityInterface, recordId);
 		}
 
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
 	public Map<Long, List<String>> getRecordsForAssociationControl(AssociationControlInterface associationControl)
 			throws DynamicExtensionsSystemException
 	{
 		Map<Long, List<String>> outputMap = new HashMap<Long, List<String>>();
 		List<String> tableNames = new ArrayList<String>();
 		String tableName;
 		String targetEntityTable = "";
 		String columnName;
 		String onClause = ON_KEYWORD;
 
 		int counter = 0;
 		boolean containsMultipleAttributes = false;
 
 		Collection associationAttributesCollection = associationControl.getAssociationDisplayAttributeCollection();
 
 		if (associationControl instanceof SelectControl)
 			targetEntityTable = ((AssociationInterface) ((SelectControl) associationControl).getBaseAbstractAttribute()).getTargetEntity()
 					.getTableProperties().getName();
 
 		String selectClause = SELECT_KEYWORD + targetEntityTable + "." + IDENTIFIER;
 		String fromClause = FROM_KEYWORD + targetEntityTable + ", ";
 		String whereClause = WHERE_KEYWORD;
 		String multipleColumnsClause = SELECT_KEYWORD + targetEntityTable + "." + IDENTIFIER + ", ";
 
 		List associationAttributesList = new ArrayList(associationAttributesCollection);
 		Collections.sort(associationAttributesList);
 
 		Iterator attributeIterator = associationAttributesCollection.iterator();
 		AssociationDisplayAttributeInterface displayAttribute = null;
 
 		while (attributeIterator.hasNext())
 		{
 			displayAttribute = (AssociationDisplayAttributeInterface) attributeIterator.next();
 			columnName = displayAttribute.getAttribute().getColumnProperties().getName();
 			tableName = displayAttribute.getAttribute().getEntity().getTableProperties().getName();
 
 			if (associationControl instanceof SelectControl
 					&& ((AssociationInterface) ((SelectControl) associationControl).getBaseAbstractAttribute()).getTargetEntity().getParentEntity() != null)
 			{
 				selectClause = selectClause + ", " + tableName + "." + columnName;
 
 				if (!(fromClause.contains(tableName)))
 					fromClause = fromClause + tableName + ", ";
 
 				if (counter == 0 && associationAttributesCollection.size() > 1)
 				{
 					whereClause = whereClause + tableName + ".ACTIVITY_STATUS <> 'Disabled' AND ";
 					whereClause = whereClause + tableName + "." + IDENTIFIER + " = ";
 				}
 				else if (counter > 0 && associationAttributesCollection.size() > 1)
 				{
 					whereClause = whereClause + tableName + "." + IDENTIFIER + " AND " + tableName + ".ACTIVITY_STATUS <> 'Disabled' AND "
 							+ tableName + "." + IDENTIFIER + " = ";
 				}
 				else if (associationAttributesCollection.size() == 1)
 				{
 					if (!(fromClause.contains(targetEntityTable)))
 						fromClause = fromClause + targetEntityTable + ", ";
 
 					whereClause = whereClause + targetEntityTable + ".ACTIVITY_STATUS <> 'Disabled' AND ";
 					whereClause = whereClause + tableName + "." + IDENTIFIER + " = " + targetEntityTable + "." + IDENTIFIER + " AND "
 							+ targetEntityTable + "." + IDENTIFIER + " = ";
 				}
 
 				counter++;
 
 				tableNames.add(tableName);
 			}
 			else
 			{
 				containsMultipleAttributes = true;
 				multipleColumnsClause += columnName + ", ";
 				tableNames.add(tableName);
 			}
 
 			if (tableNames.size() == 0 && !(associationControl instanceof SelectControl))
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
 		}
 
 		if (!containsMultipleAttributes)
 		{
 			int lastIndexOfAND = whereClause.lastIndexOf("AND");
 			whereClause = whereClause.substring(0, lastIndexOfAND);
 			fromClause = fromClause.substring(0, fromClause.length() - 2);
 		}
 
 		if (((AssociationInterface) ((SelectControl) associationControl).getBaseAbstractAttribute()).getTargetEntity().getParentEntity() == null)
 			multipleColumnsClause = multipleColumnsClause.substring(0, multipleColumnsClause.length() - 2) + FROM_KEYWORD + targetEntityTable;
 
 		StringBuffer query = new StringBuffer();
 
 		if (!containsMultipleAttributes)
 		{
 			query.append(selectClause + fromClause + whereClause);
 		}
 		else
 		{
 			query.append(multipleColumnsClause);
 			query.append(WHERE_KEYWORD + queryBuilder.getRemoveDisbledRecordsQuery(tableNames.get(0)));
 		}
 
 		JDBCDAO jdbcDao = null;
 		try
 		{
 			jdbcDao = (JDBCDAO) DAOFactory.getInstance().getDAO(Constants.JDBC_DAO);
 			jdbcDao.openSession(null);
 			List result = new ArrayList();
 			result = jdbcDao.executeQuery(query.toString(), new SessionDataBean(), false, null);
 
 			if (result != null)
 			{
 				if (!containsMultipleAttributes)
 				{
 					for (int i = 0; i < result.size(); i++)
 					{
 						List innerList = (List) result.get(i);
 						Long recordId = Long.parseLong((String) innerList.get(0));
 						innerList.remove(0);
 						outputMap.put(recordId, innerList);
 					}
 				}
 				else
 				{
 					for (int i = 0; i < result.size(); i++)
 					{
 						List innerList = (List) result.get(i);
 						Long recordId = Long.parseLong((String) innerList.get(0));
 
 						if (outputMap.containsKey(recordId))
 						{
 							List<String> tempStringList = new ArrayList<String>();
 
 							String existingString = outputMap.get(recordId).toString().replace("[", " ");
 							existingString = existingString.replace("]", " ");
 
 							tempStringList.add(existingString.trim() + associationControl.getSeparator() + (String) innerList.get(1));
 							outputMap.put(recordId, tempStringList);
 						}
 						else
 						{
 							innerList.remove(0);
 							outputMap.put(recordId, innerList);
 						}
 					}
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
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAllContainerBeans()
 	 */
 	public List<NameValueBean> getAllContainerBeans() throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
 		Collection containersBeansCollection = executeHQL("getAllContainerBeans", substitutionParameterMap);
 		Iterator containerBeansIterator = containersBeansCollection.iterator();
 		Object[] objectArrayForContainerBeans;
 		List<NameValueBean> list = new ArrayList<NameValueBean>();
 		while (containerBeansIterator.hasNext())
 		{
 			objectArrayForContainerBeans = (Object[]) containerBeansIterator.next();
 			//In case of category creation form caption is optional.
 			if ((String) objectArrayForContainerBeans[1] != null)
 				list.add(new NameValueBean((String) objectArrayForContainerBeans[1], (Long) objectArrayForContainerBeans[0]));
 		}
 		return list;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAllContainerBeansByEntityGroupId(java.lang.Long)
 	 */
 	public List<NameValueBean> getAllContainerBeansByEntityGroupId(Long entityGroupId) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", entityGroupId));
 		Collection containersBeansCollection = executeHQL("getAllContainersBeansByEntityGroupId", substitutionParameterMap);
 		Iterator containerBeansIterator = containersBeansCollection.iterator();
 		Object[] objectArrayForContainerBeans;
 		List<NameValueBean> list = new ArrayList<NameValueBean>();
 		while (containerBeansIterator.hasNext())
 		{
 			objectArrayForContainerBeans = (Object[]) containerBeansIterator.next();
 			//In case of category creation form caption is optional.
 			if ((String) objectArrayForContainerBeans[1] != null)
 				list.add(new NameValueBean((String) objectArrayForContainerBeans[1], (Long) objectArrayForContainerBeans[0]));
 		}
 		return list;
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAllContainerBeans()
 	 */
 	public List<ContainerInformationObject> getAllContainerInformationObjects() throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
 		Collection containerInformationObjectCollection = executeHQL("getAllContainerInformationObjects", substitutionParameterMap);
 		Iterator containerInformationObjectIterator = containerInformationObjectCollection.iterator();
 		Object[] objectArrayForContainerInformationObject;
 		List<ContainerInformationObject> list = new ArrayList<ContainerInformationObject>();
 		while (containerInformationObjectIterator.hasNext())
 		{
 			objectArrayForContainerInformationObject = (Object[]) containerInformationObjectIterator.next();
 			list.add(new ContainerInformationObject((String) objectArrayForContainerInformationObject[1],
 					((Long) objectArrayForContainerInformationObject[0]).toString(), (String) objectArrayForContainerInformationObject[2]));
 		}
 		return list;
 	}
 
 	/**
 	 *
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public Map<String, String> getAllContainerBeansMap() throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
 		Collection containersBeansCollection = executeHQL("getAllContainerBeans", substitutionParameterMap);
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
 			//          list.add(new NameValueBean((String) objectArrayForContainerBeans[1],
 			//                  (Long) objectArrayForContainerBeans[0]));
 			containerBeansMap.put(containerId, containerCaption);
 		}
 		return containerBeansMap;
 	}
 
 	/**
 	 * @throws DynamicExtensionsSystemException
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getChildrenEntities(edu.common.dynamicextensions.domaininterface.EntityInterface)
 	 */
 	public Collection<EntityInterface> getChildrenEntities(EntityInterface entity) throws DynamicExtensionsSystemException
 	{
 		Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", entity.getId()));
 
 		return executeHQL("getChildrenEntities", substitutionParameterMap);
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAssociationByIdentifier(java.lang.Long)
 	 */
 	public AssociationInterface getAssociationByIdentifier(Long associationId) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", associationId));
 		Collection assocationCollection = executeHQL("getAssociationByIdentifier", substitutionParameterMap);
 		if (assocationCollection.isEmpty())
 		{
 			throw new DynamicExtensionsApplicationException("Object Not Found : id" + associationId, null, DYEXTN_A_008);
 		}
 		return (AssociationInterface) assocationCollection.iterator().next();
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAssociationsForTargetEntity(edu.common.dynamicextensions.domaininterface.EntityInterface)
 	 */
 	public Collection<AssociationInterface> getIncomingAssociations(EntityInterface entity) throws DynamicExtensionsSystemException
 	{
 		Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", entity.getId()));
 		Collection<AssociationInterface> assocationCollection = executeHQL(null, "getAssociationsForTargetEntity", substitutionParameterMap);
 		return assocationCollection;
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAssociationsForTargetEntity(edu.common.dynamicextensions.domaininterface.EntityInterface)
 	 */
 	public Collection<Long> getIncomingAssociationIds(EntityInterface entity) throws DynamicExtensionsSystemException
 	{
 		Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", entity.getId()));
 		Collection<Long> assocationCollection = executeHQL(null, "getAssociationIdsForTargetEntity", substitutionParameterMap);
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
 
 	/** (non-Javadoc)
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#addAssociationColumn(edu.common.dynamicextensions.domaininterface.AssociationInterface)
 	 */
 	public void addAssociationColumn(AssociationInterface association) throws DynamicExtensionsSystemException
 	{
 		List list = new ArrayList();
 		String query;
 		Stack stack = new Stack();
 		try
 		{
 			query = queryBuilder.getQueryPartForAssociation(association, list, true);
 
 			List queryList = new ArrayList();
 			queryList.add(query);
 			stack = queryBuilder.executeQueries(queryList, list, stack, null);
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			if (!stack.isEmpty())
 			{
 				rollbackQueries(stack, (Entity) association.getEntity(), e, DAOFactory.getInstance().getDAO(Constants.HIBERNATE_DAO));
 			}
 		}
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#associateEntityRecords(edu.common.dynamicextensions.domaininterface.AssociationInterface, java.lang.Long, java.lang.Long)
 	 */
 	public void associateEntityRecords(AssociationInterface associationInterface, Long sourceEntityRecordId, Long TargetEntityRecordId)
 			throws DynamicExtensionsSystemException
 	{
 		queryBuilder.associateRecords(associationInterface, sourceEntityRecordId, TargetEntityRecordId);
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
 	public Map<Long, Date> getEntityCreatedDateByContainerId() throws DynamicExtensionsSystemException
 	{
 		Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
 		Map<Long, Date> map = new HashMap<Long, Date>();
 		Collection containersBeansCollection;
 		containersBeansCollection = executeHQL("getAllEntityCreatedDateByContainerId", substitutionParameterMap);
 
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
 	public Long checkContainerForAbstractEntity(Long entityIdentifier, boolean isAbstarct) throws DynamicExtensionsSystemException
 	{
 		Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", entityIdentifier));
 		substitutionParameterMap.put("1", new HQLPlaceHolderObject("boolean", isAbstarct));
 
 		Collection containerCollection = executeHQL("checkContainerForAbstractEntity", substitutionParameterMap);
 
 		Long contId = null;
 
 		if (containerCollection != null && containerCollection.size() > 0)
 		{
 			contId = (Long) containerCollection.iterator().next();
 
 		}
 		return contId;
 	}
 
 	/**
 	 *
 	 *
 	 * @param entityIdentifier
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public Long checkContainerForAbstractCategoryEntity(Long entityIdentifier) throws DynamicExtensionsSystemException
 	{
 		Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", entityIdentifier));
 
 		Collection containerCollection = executeHQL("checkContainerForAbstractCategoryEntity", substitutionParameterMap);
 
 		Long contId = null;
 
 		if (containerCollection != null && containerCollection.size() > 0)
 		{
 			contId = (Long) containerCollection.iterator().next();
 
 		}
 		return contId;
 	}
 
 	public Long getEntityId(String entityName) throws DynamicExtensionsSystemException
 	{
 		ResultSet resultSet = null;
 		String entityTableName = "dyextn_abstract_metadata";
 		String NAME = "name";
 		StringBuffer query = new StringBuffer();
 		query.append(SELECT_KEYWORD + WHITESPACE + IDENTIFIER);
 		query.append(WHITESPACE + FROM_KEYWORD + WHITESPACE + entityTableName + WHITESPACE);
 		query.append(WHERE_KEYWORD + WHITESPACE + NAME + WHITESPACE + EQUAL + "'" + entityName + "'");
 		System.out.println("Query = " + query.toString());
 		try
 		{
 			resultSet = EntityManagerUtil.executeQuery(query.toString());
 			resultSet.next();
 			Long identifier = resultSet.getLong(IDENTIFIER);
 			return identifier;
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		finally
 		{
 			if (resultSet != null)
 			{
 				try
 				{
 					resultSet.close();
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
 	public Long getContainerIdForEntity(Long entityId) throws DynamicExtensionsSystemException
 	{
 		ResultSet resultSet = null;
 		String tableName = "dyextn_container";
 		String ENTITY_ID_FIELD_NAME = "ENTITY_ID";
 		StringBuffer query = new StringBuffer();
 		query.append(SELECT_KEYWORD + WHITESPACE + IDENTIFIER);
 		query.append(WHITESPACE + FROM_KEYWORD + WHITESPACE + tableName + WHITESPACE);
 		query.append(WHERE_KEYWORD + WHITESPACE + ENTITY_ID_FIELD_NAME + WHITESPACE + EQUAL + "'" + entityId + "'");
 		System.out.println("Query = " + query.toString());
 		try
 		{
 			resultSet = EntityManagerUtil.executeQuery(query.toString());
 			if (resultSet != null)
 			{
 				resultSet.next();
 				Long identifier = resultSet.getLong(IDENTIFIER);
 				return identifier;
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		finally
 		{
 			if (resultSet != null)
 			{
 				try
 				{
 					resultSet.close();
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
 	public Long getNextIdentifierForEntity(String entityName) throws DynamicExtensionsSystemException
 	{
 		ResultSet resultSet = null;
 		String tableName = "dyextn_database_properties";
 		String NAME = "NAME";
 		StringBuffer query = new StringBuffer();
 		query.append(SELECT_KEYWORD + WHITESPACE + NAME);
 		query.append(WHITESPACE + FROM_KEYWORD + WHITESPACE + tableName + WHITESPACE);
 		query.append(WHERE_KEYWORD + WHITESPACE + IDENTIFIER + WHITESPACE + EQUAL);
 		query.append(OPENING_BRACKET);
 		query.append(SELECT_KEYWORD + WHITESPACE + IDENTIFIER);
 		query.append(WHITESPACE + FROM_KEYWORD + WHITESPACE + "dyextn_table_properties" + WHITESPACE);
 		query.append(WHERE_KEYWORD + WHITESPACE + "ENTITY_ID" + WHITESPACE + EQUAL);
 		query.append(OPENING_BRACKET);
 		query.append(SELECT_KEYWORD + WHITESPACE + IDENTIFIER);
 		query.append(WHITESPACE + FROM_KEYWORD + WHITESPACE + "dyextn_abstract_metadata" + WHITESPACE);
 		query.append(WHERE_KEYWORD + WHITESPACE + "NAME" + WHITESPACE + EQUAL + "'" + entityName + "'");
 		query.append(CLOSING_BRACKET);
 		query.append(CLOSING_BRACKET);
 		System.out.println("Query = " + query.toString());
 		try
 		{
 			resultSet = EntityManagerUtil.executeQuery(query.toString());
 			if (resultSet != null)
 			{
 				resultSet.next();
 				String entityTableName = resultSet.getString(NAME);
 				if (entityTableName != null)
 				{
 					EntityManagerUtil entityManagerUtil = new EntityManagerUtil();
 					return entityManagerUtil.getNextIdentifier(entityTableName);
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		finally
 		{
 			if (resultSet != null)
 			{
 				try
 				{
 					resultSet.close();
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
 	 * Returns an attribute given the entity name and attribute name.
 	 * @param entityName name of the entity.
 	 * @param attributeName name of the attribute.
 	 * @return AttributeInterface attribute interface
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public AttributeInterface getAttribute(String entityName, String attributeName) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		AttributeInterface attributeInterface = null;
 		AbstractAttributeInterface abstractAttributeInterface;
 		String name;
 		if (entityName == null || entityName.equals("") || attributeName == null || attributeName.equals(""))
 		{
 			return attributeInterface;
 		}
 
 		EntityInterface entityInterface = getEntityByName(entityName);
 		if (entityInterface != null)
 		{
 			Collection abstractAttributeCollection = entityInterface.getAbstractAttributeCollection();
 			if (abstractAttributeCollection != null)
 			{
 				Iterator abstractAttributeIterator = abstractAttributeCollection.iterator();
 
 				while (abstractAttributeIterator.hasNext())
 				{
 					abstractAttributeInterface = (AbstractAttributeInterface) abstractAttributeIterator.next();
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
 	 * Returns all entitiy groups in the whole system
 	 * @return Collection Entity group Beans Collection
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public Collection<NameValueBean> getAllEntityGroupBeans() throws DynamicExtensionsSystemException
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
 	 * validateEntity.
 	 */
 	public boolean validateEntity(EntityInterface entityInterface) throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		Collection<EntityInterface> entityCollection = entityInterface.getEntityGroup().getEntityCollection();
 		for (EntityInterface entity : entityCollection)
 		{
 			Entity entityObject = (Entity) entityInterface;
 			if (entity.getId() == null)
 			{
 				DynamicExtensionsUtility.validateEntity(entity);
 			}
 			else
 			{
 				Entity databaseCopy = (Entity) DBUtil.loadCleanObj(Entity.class, entity.getId());
 				if (queryBuilder.isParentChanged((Entity) entity, databaseCopy))
 				{
 					checkParentChangeAllowed(entityObject);
 				}
 			}
 		}
 		return true;
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
 		Collection recordCollection = executeHQLWithCleanSession("getAttributeRecords", substitutionParameterMap);
 
 		return recordCollection;
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
 	private Collection executeHQLWithCleanSession(String queryName, Map<String, HQLPlaceHolderObject> substitutionParameterMap)
 			throws DynamicExtensionsSystemException
 	{
 		Collection entityCollection = new HashSet();
 		Session session = null;
 		try
 		{
 			session = DBUtil.getCleanSession();
 			Query query = substitutionParameterForQuery(session, queryName, substitutionParameterMap);
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
 				throw new DynamicExtensionsSystemException("Exception occured while closing the session", e, DYEXTN_S_001);
 			}
 
 		}
 		return entityCollection;
 	}
 
 	/**
 	 * This method substitues the parameters from substitutionParameterMap into the input query.
 	 * @param substitutionParameterMap
 	 * @throws HibernateException
 	 */
 	private Query substitutionParameterForQuery(Session session, String queryName, Map substitutionParameterMap) throws HibernateException
 	{
 		Query q = session.getNamedQuery(queryName);
 		for (int counter = 0; counter < substitutionParameterMap.size(); counter++)
 		{
 			HQLPlaceHolderObject hPlaceHolderObject = (HQLPlaceHolderObject) substitutionParameterMap.get(counter + "");
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
 	 *
 	 */
 	protected DynamicExtensionBaseQueryBuilder getQueryBuilderInstance()
 	{
 		return queryBuilder;
 	}
 
 	/**
 	 * This method returns the container interface given the entity identifier.
 	 * @param EntityInterface
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public ContainerInterface getContainerByEntityIdentifier(Long entityIdentifier) throws DynamicExtensionsSystemException
 	{
 		ContainerInterface containerInterface = null;
 		Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", entityIdentifier));
 		Collection containerCollection = executeHQL("getContainerOfEntity", substitutionParameterMap);
 		if (containerCollection != null && containerCollection.size() > 0)
 		{
 			containerInterface = (ContainerInterface) containerCollection.iterator().next();
 		}
 
 		return containerInterface;
 
 	}
 
 	/**
 	 *
 	 * @param entityGroupInterface
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public Collection<AssociationTreeObject> getAssociationTree() throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
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
 	private AssociationTreeObject processGroupBean(NameValueBean groupBean) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		AssociationTreeObject associationTreeObjectForGroup = new AssociationTreeObject(new Long(groupBean.getValue()), groupBean.getName());
 
 		Map substitutionParameterMap = new HashMap();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", associationTreeObjectForGroup.getId()));
 
 		Collection containersBeansCollection = executeHQL("getAllContainersBeansByEntityGroupId", substitutionParameterMap);
 
 		Iterator containerBeansIterator = containersBeansCollection.iterator();
 		Object[] objectArrayForContainerBeans;
 		AssociationTreeObject associationTreeObjectForContainer;
 
 		while (containerBeansIterator.hasNext())
 		{
 			objectArrayForContainerBeans = (Object[]) containerBeansIterator.next();
 			associationTreeObjectForContainer = new AssociationTreeObject((Long) objectArrayForContainerBeans[0],
 					(String) objectArrayForContainerBeans[1]);
 			//processForChildContainer(associationTreeObjectForContainer);
 			associationTreeObjectForGroup.addAssociationTreeObject(associationTreeObjectForContainer);
 
 		}
 
 		return associationTreeObjectForGroup;
 	}
 
 	/**
 	 *
 	 * @param attributeInterface
 	 * @param recordId
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DAOException
 	 * @throws SQLException
 	 * @throws IOException
 	 */
 	public FileAttributeRecordValue getFileAttributeRecordValueByRecordId(AttributeInterface attribute, Long recordId)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException, DAOException, SQLException, IOException
 	{
 		EntityInterface entity = attribute.getEntity();
 		FileAttributeRecordValue fileRecordValue = new FileAttributeRecordValue();
 		String query = SELECT_KEYWORD + attribute.getName() + UNDERSCORE + FILE_NAME + COMMA + attribute.getName() + UNDERSCORE + CONTENT_TYPE
 				+ COMMA + attribute.getColumnProperties().getName() + FROM_KEYWORD + entity.getTableProperties().getName() + WHITESPACE
 				+ WHERE_KEYWORD + IDENTIFIER + EQUAL + recordId;
 
 		ResultSet resultSet = null;
 		Statement statement = null;
 		Connection connection = null;
 		try
 		{
 			connection = DBUtil.getConnection();
 			statement = connection.createStatement();
 			resultSet = statement.executeQuery(query);
 			while (resultSet.next())
 			{
 				fileRecordValue.setFileName(resultSet.getString(attribute.getName() + UNDERSCORE + FILE_NAME));
 				fileRecordValue.setContentType(resultSet.getString(attribute.getName() + UNDERSCORE + CONTENT_TYPE));
 				Blob blob = resultSet.getBlob(attribute.getColumnProperties().getName());
 				byte[] byteArray = blob.getBytes(1, (int) blob.length());
 
 				fileRecordValue.setFileContent(byteArray);
 			}
 		}
 		finally
 		{
 			resultSet.close();
 			statement.close();
 			DBUtil.closeConnection();
 		}
 
 		//resultSet.close();
 
 		/*	ToDo: Correct this logger statement
 		 * 	logDebug("insertData", "Query is: " + query.toString());
 		 * */
 		return fileRecordValue;
 	}
 
 	public ResultSet executeQuery(String query) throws SQLException
 	{
 		ResultSet resultSet = null;
 		Statement statement = null;
 		Connection connection = null;
 		try
 		{
 			connection = DBUtil.getConnection();
 			statement = connection.createStatement();
 			resultSet = statement.executeQuery(query);
 		}
 		finally
 		{
 			resultSet.close();
 			statement.close();
 			DBUtil.closeConnection();
 		}
 
 		return resultSet;
 	}
 
 	/**
 	 *
 	 * @param hookEntityId
 	 * @return  the container Id of the DE entities/categories that are associated with given static hook entity
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public Collection<ContainerInterface> getCategoriesContainerIdFromHookEntity(Long hookEntityId) throws DynamicExtensionsSystemException
 	{
 		Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", hookEntityId));
 		Collection containerCollection = executeHQL("getCategoryContainerIdFromHookEntiy", substitutionParameterMap);
 		return containerCollection;
 	}
 
 	/**
 	 * This method returns the Table name generated fro this container
 	 */
 	public String getDynamicTableName(Long containerId) throws DynamicExtensionsSystemException
 	{
 		String tableName = "";
 		Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", containerId));
 		Collection containerCollection = executeHQL("getDynamicTableName", substitutionParameterMap);
 
 		if (containerCollection != null && containerCollection.size() > 0)
 		{
 			tableName = (String) containerCollection.iterator().next();
 		}
 		return tableName;
 	}
 
 	/**
 	 *
 	 * @param hookEntityId
 	 * @return the container Id of the DE entities that are associated with given static hook entity
 	 */
 	public Collection<ContainerInterface> getDynamicEntitiesContainerIdFromHookEntity(Long hookEntityId) throws DynamicExtensionsSystemException
 	{
 		Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", hookEntityId));
 		Collection containerCollection = executeHQL("getFormsContainerIdFromHookEntiy", substitutionParameterMap);
 		return containerCollection;
 	}
 
 	/**
 	 *
 	 * @param containerId
 	 * @return whether this entity is simple DE form /category.
 	 */
 	public Long isCategory(Long containerId) throws DynamicExtensionsSystemException
 	{
 		Long containerIdentifier = null;
 		Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", containerId));
 		Collection containerCollection = executeHQL("isCategory", substitutionParameterMap);
 		if (containerCollection != null && containerCollection.size() > 0)
 		{
 			containerIdentifier = (Long) containerCollection.iterator().next();
 		}
 		return containerIdentifier;
 	}
 
 	/**
 	 * This method returns container id for the root entity for the given category conatainer id
 	 */
 	public Long getCategoryRootContainerId(Long containerId) throws DynamicExtensionsSystemException
 	{
 		Long containerIdentifier = null;
 		Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", containerId));
 		Collection containerCollection = executeHQL("getCategoryRootContainerId", substitutionParameterMap);
 		if (containerCollection != null && containerCollection.size() > 0)
 		{
 			containerIdentifier = (Long) containerCollection.iterator().next();
 		}
 		return containerIdentifier;
 	}
 
 	/**
 	 * this method returns the column name for the assocation
 	 */
 	public String getColumnNameForAssociation(Long hookEntityId, Long containerId) throws DynamicExtensionsSystemException
 	{
 		String colName = null;
 		Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", hookEntityId));
 		substitutionParameterMap.put("1", new HQLPlaceHolderObject("long", containerId));
 		Collection containerCollection = executeHQL("getColumnNameForAssociation", substitutionParameterMap);
 
 		if (containerCollection != null && containerCollection.size() > 0)
 		{
 			colName = (String) containerCollection.iterator().next();
 		}
 		return colName;
 	}
 
 	public EntityInterface persistEntityMetadataForAnnotation(EntityInterface entityInterface, boolean isDataTablePresent,
 			boolean copyDataTableState, AssociationInterface association) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		Entity entity = (Entity) entityInterface;
 		if (isDataTablePresent)
 		{
 			((Entity) entityInterface).setDataTableState(DATA_TABLE_STATE_ALREADY_PRESENT);
 		}
 		else
 		{
 			((Entity) entityInterface).setDataTableState(DATA_TABLE_STATE_NOT_CREATED);
 		}
 
 		//      boolean isEntitySaved = true;
 		//      //Depending on the presence of Id field , the method that is to be invoked (insert/update), is decided.
 		//      if (entity.getId() == null)
 		//      {
 		//          isEntitySaved = false;
 		//      }
 
 		HibernateDAO hibernateDAO = (HibernateDAO) DAOFactory.getInstance().getDAO(Constants.HIBERNATE_DAO);
 		//      Stack stack = new Stack();
 
 		try
 		{
 
 			hibernateDAO.openSession(null);
 
 			hibernateDAO.update(entity, null, false, false, false);
 			//Calling the method which actually calls the insert/update method on dao.
 			//Hibernatedao is passed to this method and transaction is handled in the calling method.
 			//          saveEntityGroup(entityInterface, hibernateDAO);
 			//          List<EntityInterface> processedEntityList = new ArrayList<EntityInterface>();
 			//
 			//          entityInterface = saveOrUpdateEntityMetadataForSingleAnnotation(entityInterface, hibernateDAO, stack,
 			//                  isEntitySaved, processedEntityList, copyDataTableState,association);
 
 			//Committing the changes done in the hibernate session to the database.
 			hibernateDAO.commit();
 		}
 		catch (Exception e)
 		{
 			//Queries for data table creation and modification are fired in the method saveOrUpdateEntity.
 			//So if there is any exception while storing the metadata ,
 			//we need to roll back the queries that were fired. So calling the following method to do that.
 			//rollbackQueries(stack, entity, e, hibernateDAO);
 
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
 				//postSaveOrUpdateEntity(entityInterface);
 				// entity.setProcessed(false);
 				//In any case , after all the operations , hibernate session needs to be closed. So this call has
 				// been added in the finally clause.
 				hibernateDAO.closeSession();
 			}
 			catch (DAOException e)
 			{
 				//Queries for data table creation and modification are fired in the method saveOrUpdateEntity. So if there
 				//is any exception while storing the metadata , we need to roll back the queries that were fired. So
 				//calling the following method to do that.
 				//rollbackQueries(stack, entity, e, hibernateDAO);
 			}
 		}
 		logDebug("persistEntity", "exiting the method");
 		return entityInterface;
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getMainContainer(java.lang.Long)
 	 */
 	public Collection<NameValueBean> getMainContainer(Long entityGroupIdentifier) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", entityGroupIdentifier));
 		return executeHQL("getMainContainers", substitutionParameterMap);
 	}
 
 	/**
 	 * This method returns the EntityInterface given the entity name.
 	 * @param entityGroupShortName
 	 * @return
 	 */
 	public EntityGroupInterface getEntityGroupByName(String entityGroupName) throws DynamicExtensionsSystemException
 	{
 		EntityGroupInterface entityGroupInterface = (EntityGroupInterface) getObjectByName(EntityGroup.class.getName(), entityGroupName);
 		return entityGroupInterface;
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getEntityGroupByShortName(java.lang.String)
 	 */
 	public EntityGroupInterface getEntityGroupByShortName(String entityGroupShortName) throws DynamicExtensionsSystemException
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
 			entityGroupCollection = defaultBizLogic.retrieve(EntityGroup.class.getName(), "shortName", entityGroupShortName);
 			if (entityGroupCollection != null && entityGroupCollection.size() > 0)
 			{
 				entityGroupInterface = (EntityGroupInterface) entityGroupCollection.iterator().next();
 			}
 		}
 		catch (DAOException e)
 		{
 			throw new DynamicExtensionsSystemException(e.getMessage(), e);
 		}
 		return entityGroupInterface;
 
 	}
 
 	/**
 	 * Returns all entitiy groups in the whole system
 	 * @return Collection Entity group Collection
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public Collection<EntityGroupInterface> getAllEntitiyGroups() throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		return getAllObjects(EntityGroupInterface.class.getName());
 	}
 
 	/**
 	 * This method returns the control given the attribute identifier
 	 * @param controlIdentifier
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public ControlInterface getControlByAbstractAttributeIdentifier(Long abstractAttributeIdentifier) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		ControlInterface controlInterface = null;
 		Map<String, HQLPlaceHolderObject> substitutionParameterMap = new HashMap<String, HQLPlaceHolderObject>();
 		substitutionParameterMap.put("0", new HQLPlaceHolderObject("long", abstractAttributeIdentifier));
 		Collection controlCollection = executeHQL("getControlOfAbstractAttribute", substitutionParameterMap);
 		if (controlCollection != null && controlCollection.size() > 0)
 		{
 			controlInterface = (ControlInterface) controlCollection.iterator().next();
 		}
 
 		return controlInterface;
 	}
 
 }
