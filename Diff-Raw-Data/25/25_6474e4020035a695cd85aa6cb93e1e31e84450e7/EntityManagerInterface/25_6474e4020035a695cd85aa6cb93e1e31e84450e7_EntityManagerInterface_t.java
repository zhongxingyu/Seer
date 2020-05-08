 
 package edu.common.dynamicextensions.entitymanager;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import edu.common.dynamicextensions.domain.FileAttributeRecordValue;
 import edu.common.dynamicextensions.domain.integration.EntityMapCondition;
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AbstractEntityInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.AssociationControlInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ControlInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.util.AssociationTreeObject;
 import edu.wustl.common.beans.NameValueBean;
 import edu.wustl.dao.HibernateDAO;
 import edu.wustl.dao.JDBCDAO;
 import edu.wustl.dao.exception.DAOException;
 
 /**
  *
  * @author mandar_shidhore
  *
  */
 public interface EntityManagerInterface
 {
 
 	/**
 	 * Persists the entity to the database. Also creates the dynamic tables and associations
 	 * between those tables using the meta data information in the entity object.
 	 * @param entity
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	EntityInterface persistEntity(EntityInterface entity) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException;
 
 	/**
 	 * This method is used to save the meta data information  
 	 * of the given entity without creating its data table.
 	 * @param entityInterface entity to be persisted
 	 */
 	EntityInterface persistEntityMetadata(EntityInterface entity)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException;
 
 	/**
 	 * Returns a collection of association objects given the source entity id and
 	 * target entity id.
 	 * @param sourceEntityId
 	 * @param targetEntityId
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	Collection<AssociationInterface> getAssociations(Long sourceEntityId, Long targetEntityId)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException;
 
 	/**
	 * Returns a collection of association objects given the source entity id and
	 * target entity id.
	 * @param srcEntityId
	 * @param tgtEntityId
	 * @param hibernatedao
	 * @return
	 * @throws DynamicExtensionsSystemException
	 * @throws DynamicExtensionsApplicationException
	 */
	public Collection<AssociationInterface> getAssociations(Long srcEntityId, Long tgtEntityId,
			HibernateDAO hibernatedao) throws DynamicExtensionsSystemException,
			DynamicExtensionsApplicationException;

	/**
 	 * Returns a collection of association ids given the source entity id and
 	 * target entity id.
 	 * @param sourceEntityId
 	 * @param targetEntityId
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	Collection<Long> getAssociationIds(Long sourceEntityId, Long targetEntityId)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException;
 
 	/**
 	 * Returns an entity object given the entity name;
 	 * @param entityName
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	EntityInterface getEntityByName(String entityName) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException;
 
 	/**
	 * @param entityName
	 * @param hibernateDAO
	 * @return
	 * @throws DynamicExtensionsSystemException
	 * @throws DynamicExtensionsApplicationException
	 */
	EntityInterface getEntityByName(String entityName, HibernateDAO hibernateDAO)
			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException;

	/**
 	 * Returns an association object given the entity name and source role name.
 	 * @param entityName
 	 * @param srcRoleName
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	Collection<AssociationInterface> getAssociation(String entityName, String srcRoleName)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException;
 
 	/**
 	 *
 	 * @param sourceEntityName
 	 * @param sourceRoleName
 	 * @param targetEntityName
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	AssociationInterface getAssociation(String sourceEntityName, String sourceRoleName,
 			String targetEntityName) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException;
 
 	/**
 	 * Returns an association object given the association name.
 	 * @param associationName
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	AssociationInterface getAssociationByName(String assoName)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException;
 
 	/**
 	 * Returns a collection of entities given the entity concept code.
 	 * @param entityConceptCode
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	Collection<EntityInterface> getEntitiesByConceptCode(String entityConceptCode)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException;
 
 	/**
 	 * Returns all entities in the whole system
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	Collection<EntityInterface> getAllEntities() throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException;
 
 	/**
 	 * Returns a single  entity for given identifier
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	EntityInterface getEntityByIdentifier(String identifier)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException;
 
 	/**
 	 * Returns the entity based on the id passed.
 	 * @param identifier
 	 * @return
 	 * @throws DynamicExtensionsSystemException thrown in case of fatal system exceptions
 	 * @throws DynamicExtensionsApplicationException thrown in case application specific errors.
 	 */
 	EntityInterface getEntityByIdentifier(Long identifier) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException;
 
 	/**
 	 * Returns all Containers in the whole system
 	 * @return Collection of ContainerInterface
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	Collection<ContainerInterface> getAllContainers() throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException;
 
 	/**
 	 * Retrieve all containers from a particular group.
 	 * @param entityGroupIdentifier
 	 * @return collection of containers.
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	Collection<ContainerInterface> getAllContainersByEntityGroupId(Long entityGroupIdentifier)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException;
 
 	/**
 	 * This method inserts one record for the entity.
 	 */
 	Long insertData(EntityInterface entity, Map<AbstractAttributeInterface, Object> dataValue,
 			Long... userId) throws DynamicExtensionsApplicationException,
 			DynamicExtensionsSystemException;
 
 	/**
 	 * This method inserts multiple records for the entity. This is a single transaction, so either all records are inserted or nothing
 	 * is persisted.
 	 * It returns the record id in the same sequence as that of input maps in dataValueMapList.
 	 */
 	List<Long> insertData(EntityInterface entity,
 			List<Map<AbstractAttributeInterface, Object>> dataValueMapList, Long... userId)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException;
 
 	/**
 	 * This method updates the existing record for the given entity.
 	 * @param entity       Entity for which record needs to be updated
 	 * @param dataValue    map that contains  name of the attribute whose value is changed and its new value
 	 *                     If it is multiselect attribute then value should be List<string>
 	 * @param recordId     Id of the record
 	 * @return true if success
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	boolean editData(EntityInterface entity, Map<AbstractAttributeInterface, ?> dataValue,
 			Long recordId, Long... userId) throws DynamicExtensionsApplicationException,
 			DynamicExtensionsSystemException;
 
 	/**
 	 * The method returns the entity records for the given entity, attribute and records.
 	 * @param entity
 	 * @param abstrAttributes
 	 * @param recordIds
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	EntityRecordResultInterface getEntityRecords(EntityInterface entity,
 			List<? extends AbstractAttributeInterface> abstrAttributes, List<Long> recordIds)
 			throws DynamicExtensionsSystemException;
 
 	/**
 	 * Returns a particular record for the given recordId of the given entityId
 	 * @param entityId
 	 * @param recordId
 	 * @return Map key - attribute name
 	 *             value - attribute value
 	 */
 	Map<AbstractAttributeInterface, Object> getRecordById(EntityInterface entity, Long recordId)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException;
 
 	/**
 	 * This method deletes a particular record for an entity.
 	 * @param entity Entity for which record needs to be deleted
 	 * @param recordId Id of the record
 	 * @return success failure flag. true if successful
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	boolean deleteRecord(EntityInterface entity, Long recordId)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException;
 
 	/**
 	 * Method deletes the passed records of the passed container.
 	 * @param containerId
 	 * @param recordIdList
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	void deleteRecords(Long containerId, List<Long> recordIdList)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException;
 
 	/**
 	 * This method returns the all the record for the given control of the
 	 * association.
 	 *
 	 * @return map
 	 *    key   recordId
 	 *    value List<String> list of column values
 	 */
 	Map<Long, List<String>> getRecordsForAssociationControl(
 			AssociationControlInterface associationControl) throws DynamicExtensionsSystemException;
 
 	/**
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	List<NameValueBean> getAllContainerBeans() throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException;
 
 	/**
 	 *
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	List<ContainerInformationObject> getAllContainerInformationObjects()
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException;
 
 	/**
 	 *
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	Map<String, String> getAllContainerBeansMap() throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException;
 
 	/**
 	 * returns all the records for a given entity
 	 */
 	List<EntityRecord> getAllRecords(AbstractEntityInterface entity)
 			throws DynamicExtensionsSystemException;
 
 	/**
 	 * This method returns all the children entities of givens entity.
 	 * @return Collection of EntityInterface
 	 * @throws DynamicExtensionsSystemException
 	 */
 	Collection<EntityInterface> getChildrenEntities(EntityInterface entity)
 			throws DynamicExtensionsSystemException;
 
 	/**
 	 * @param associationId
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	AssociationInterface getAssociationByIdentifier(Long associationId)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException;
 
 	/**
 	 * This method returns collection of the association for a given target entity.
 	 * @param entity
 	 * @return
 	 */
 	Collection<AssociationInterface> getIncomingAssociations(EntityInterface entity)
 			throws DynamicExtensionsSystemException;
 
 	/**
 	 * @param containerId
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	String getContainerCaption(Long containerId) throws DynamicExtensionsSystemException;
 
 	/**
 	 * @param association
 	 * @throws DynamicExtensionsSystemException
 	 */
 	void addAssociationColumn(AssociationInterface association)
 			throws DynamicExtensionsSystemException;
 
 	/**
 	 * @param associationInterface
 	 * @param sourceEntityRecordId
 	 * @param TargetEntityRecordId
 	 * @throws DynamicExtensionsSystemException
 	 */
 	void associateEntityRecords(AssociationInterface associationInterface,
 			Long sourceEntityRecordId, Long TargetEntityRecordId)
 			throws DynamicExtensionsSystemException;
 
 	/**
 	 * @param containerId
 	 * @throws DynamicExtensionsSystemException
 	 */
 	Long getEntityIdByContainerId(Long containerId) throws DynamicExtensionsSystemException;
 
 	Map<Long, Date> getEntityCreatedDateByContainerId() throws DynamicExtensionsSystemException;
 
 	/**
 	 *
 	 * @param isAbstarct
 	 * @param entityIdentifier
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	Long checkContainerForAbstractEntity(Long entityIdentifier, boolean isAbstarct)
 			throws DynamicExtensionsSystemException;
 
 	/**
 	 * @param entityIdentifier
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	Long checkContainerForAbstractCategoryEntity(Long entityIdentifier)
 			throws DynamicExtensionsSystemException;
 
 	/**
 	 * Get entity id for entity. This method fires direct SQL query and does not use hibernate for
 	 * performance reasons.
 	 *
 	 * @param entityName : Name of the entity for which Id is to be fetched
 	 * @return :  Id of the specified entity
 	 * @throws DynamicExtensionsSystemException
 	 */
 	Long getEntityId(String entityName) throws DynamicExtensionsSystemException;
 
 	/**
 	 * Get the container Id for the specified entity Id
 	 * This method fires direct JDBC SQL queries without using hibernate for performance purposes
 	 * @param entityId : Id for the entity whose container id is to be fetched
 	 * @return : container Id for specified entity
 	 * @throws DynamicExtensionsSystemException
 	 */
 	Long getContainerIdForEntity(Long entityId) throws DynamicExtensionsSystemException;
 
 	/**
 	 * Get next identifier for an entity from entity table when a record is to be inserted to the entity table.
 	 * @param entityName :  Name of the entity
 	 * @return :  Next identifier that can be assigned to a entity record
 	 * @throws DynamicExtensionsSystemException
 	 */
 	Long getNextIdentifierForEntity(String entityName) throws DynamicExtensionsSystemException;
 
 	/**
 	 * Returns an attribute given the entity name and attribute name.
 	 * @param entityName name of the entity.
 	 * @param attributeName name of the attribute.
 	 * @return AttributeInterface attribute interface
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	AttributeInterface getAttribute(String entityName, String attributeName)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException;
 
 	/**
 	 * Returns all entitiy groups in the whole system
 	 * @return Collection Entity group Beans Collection
 	 * @throws DynamicExtensionsSystemException
 	 */
 	Collection<NameValueBean> getAllEntityGroupBeans() throws DynamicExtensionsSystemException;
 
 	/**
 	 * validateEntityGroup.
 	 * @throws DynamicExtensionsSystemException
 	 */
 	boolean validateEntity(EntityInterface entity) throws DynamicExtensionsApplicationException,
 			DynamicExtensionsSystemException;
 
 	/**
 	 *
 	 * @param entityId
 	 * @param attributeId
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	Collection<Integer> getAttributeRecordsCount(Long entityId, Long attributeId)
 			throws DynamicExtensionsSystemException;
 
 	/**
 	 * @param entityId
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	ContainerInterface getContainerByEntityIdentifier(Long entityId)
 			throws DynamicExtensionsSystemException;
 
 	/**
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	Collection<AssociationTreeObject> getAssociationTree(Long entityGroupId)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException;
 
 	/**
 	 * @param attribute
 	 * @param recordId
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DAOException 
 	 * @throws SQLException 
 	 * @throws IOException 
 	 */
 	FileAttributeRecordValue getFileAttributeRecordValueByRecordId(AttributeInterface attribute,
 			Long recordId) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException, DAOException, SQLException, IOException;
 
 	Long isCategory(Long containerId) throws DynamicExtensionsSystemException;
 
 	/**
 	 *
 	 * @param hookEntityId
 	 * @return the container Id of the DE entities that are associated with given static hook entity
 	 */
 	Collection<ContainerInterface> getDynamicEntitiesContainerIdFromHookEntity(Long hookEntityId)
 			throws DynamicExtensionsSystemException;
 
 	/**
 	 *
 	 * @param hookEntityId
 	 * @return  the container Id of the DE entities/categories that are associated with given static hook entity
 	 * @throws DynamicExtensionsSystemException
 	 */
 	Collection<ContainerInterface> getCategoriesContainerIdFromHookEntity(Long hookEntityId)
 			throws DynamicExtensionsSystemException;
 
 	/**
 	 *
 	 * @param hookEntityRecId
 	 * @param containerId
 	 * @return the category or form record id based on the containerId and hookentityRecId
 	 */
 	String getDynamicTableName(Long containerId) throws DynamicExtensionsSystemException;
 
 	/**
 	 *
 	 * @param categoryContainerId
 	 * @param staticRecId
 	 * @return the record id of the category depending on hook entity record id.
 	 */
 	String getColumnNameForAssociation(Long hookEntityId, Long containerId)
 			throws DynamicExtensionsSystemException;
 
 	Long getCategoryRootContainerId(Long containerId) throws DynamicExtensionsSystemException;
 
 	/**
 	 * @param entityGroupIdentifier
 	 * @return
 	 */
 	Collection<NameValueBean> getMainContainer(Long entityGroupIdentifier)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException;
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#persistEntityMetadata(edu.common.dynamicextensions.domaininterface.EntityInterface)
 	 */
 	EntityInterface persistEntityMetadataForAnnotation(EntityInterface entityInterface,
 			boolean isDataTablePresent, boolean copyDataTableState, AssociationInterface association)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException;
 
 	/**
 	 * @param entityInterface
 	 * @param isDataTablePresent
 	 * @param copyDataTableState
 	 * @param association
 	 * @param hibernatedao
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	EntityInterface persistEntityMetadataForAnnotation(EntityInterface entityInterface,
 			boolean isDataTablePresent, boolean copyDataTableState,
 			AssociationInterface association, HibernateDAO hibernatedao)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException;
 
 	/**
 	 * @see edu.common.dynamicextensions.entitymanager.EntityManagerInterface#getAssociationsForTargetEntity(edu.common.dynamicextensions.domaininterface.EntityInterface)
 	 */
 	Collection<Long> getIncomingAssociationIds(EntityInterface entity)
 			throws DynamicExtensionsSystemException;
 
 	/**
 	 * @param entityGroupName
 	 * @return EntityGroupInterface EntityGroupInterface
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	EntityGroupInterface getEntityGroupByName(String entityGroupName)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException;
 
 	/**
 	 * Returns all entityGroups in the system.
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	Collection<EntityGroupInterface> getAllEntitiyGroups() throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException;
 
 	/**
 	 * This method returns the control given the attribute identifier
 	 * @param controlIdentifier
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	ControlInterface getControlByAbstractAttributeIdentifier(Long abstractAttributeIdentifier)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException;
 
 	/**
 	 * @param entityGroupId
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	List<NameValueBean> getAllContainerBeansByEntityGroupId(Long entityGroupId)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException;
 
 	Map<AbstractAttributeInterface, Object> getEntityRecordById(EntityInterface entity,
 			Long recordId, JDBCDAO... dao) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException;
 
 	/**
 	 * @param entity
 	 * @param dataValue
 	 * @param jdbcDao
 	 * @param parentRecordId
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	Long insertDataForSingleEntity(EntityInterface entity, Map<?, ?> dataValue, JDBCDAO jdbcDao,
 			Long parentRecordId, Long... userId) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException;
 
 	/**
 	 * @param entity
 	 * @param dataValue
 	 * @param recordId
 	 * @param jdbcDAO
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	boolean editDataForSingleEntity(EntityInterface entity, Map<?, ?> dataValue, Long recordId,
 			JDBCDAO jdbcDao, Long... userId) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException;
 
 	/**
 	 * @param entity
 	 * @param dataValue
 	 * @param jdbcDAO
 	 * @param identifier
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	Long insertDataForHeirarchy(EntityInterface entity,
 			Map<AbstractAttributeInterface, ?> dataValue, JDBCDAO jdbcDao, Long... identifier)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException;
 
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
 	boolean editDataForHeirarchy(EntityInterface entity,
 			Map<AbstractAttributeInterface, ?> dataValue, Long recordId, JDBCDAO jdbcDao,
 			Long... userId) throws DynamicExtensionsApplicationException,
 			DynamicExtensionsSystemException;
 
 	/**
 	 * This method updates attribute type info object
 	 * @param attrTypeInfo
 	 * @return AttributeTypeInformationInterface
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	AttributeTypeInformationInterface updateAttributeTypeInfo(
 			AttributeTypeInformationInterface attrTypeInfo)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException;
 
 	/**
 	 * This method retrieves entity group id given the entity group name by executing hql query
 	 * @param entityGroupName
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	Long getEntityGroupId(String entityGroupName) throws DynamicExtensionsSystemException;
 
 	/**
 	 * This method retrieves the entity id for the given entity group and the given entity name
 	 * @param entityName
 	 * @param entityGroupId
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	Long getEntityId(String entityName, Long entityGroupId) throws DynamicExtensionsSystemException;
 
 	/**
 	 * This method retrieves the attribute id for the entity and the given attribute name.
 	 * @param attributeName
 	 * @param entityId
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	Long getAttributeId(String attributeName, Long entityId)
 			throws DynamicExtensionsSystemException;
 
 	/**
 	 * This method retrives the AttributeTypeInformation object given the attribute id.
 	 * @param attributeId
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	AttributeTypeInformationInterface getAttributeTypeInformation(Long attributeId)
 			throws DynamicExtensionsSystemException;
 
 	/**
 	 * @param containerCaption
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	Long getContainerIdByCaption(String containerCaption) throws DynamicExtensionsSystemException;
 
 	/**
 	 * @param containerId
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	String getCategoryCaption(Long categoryId) throws DynamicExtensionsSystemException;
 
 	/**
 	 * @param attributeId
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	Long getAssociationAttributeId(Long attributeId) throws DynamicExtensionsSystemException;
 
 	/**
 	 * @param categoryEntityId
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	Long getEntityIdByCategorEntityId(Long categoryEntityId)
 			throws DynamicExtensionsSystemException;
 
 	/**
 	 * getAllEntityIdsForEntityGroup.
 	 * @param entityGroupId
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	Collection<Long> getAllEntityIdsForEntityGroup(Long entityGroupId)
 			throws DynamicExtensionsSystemException;
 
 	/**
 	 * getRootCategoryEntityIdByCategoryName.
 	 * @param categoryName
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	Long getRootCategoryEntityIdByCategoryName(String categoryName)
 			throws DynamicExtensionsSystemException;
 
 	/**
 	 * @param entityId
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	Long getContainerIdFromEntityId(Long entityId) throws DynamicExtensionsSystemException;
 
 	/**
 	 * @param entityId
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	String getContainerCaptionFromEntityId(Long entityId) throws DynamicExtensionsSystemException;
 
 	/**
 	 * @return SystemGenerated EntityGroup beans
 	 * @throws DynamicExtensionsSystemException
 	 */
 	Collection<NameValueBean> getAllSystemGenEntityGroupBeans()
 			throws DynamicExtensionsSystemException;
 
 	/**
 	 * @param staticRecordId
 	 * @return collection of EntityMapConditions for a staticRecordId
 	 * @throws DynamicExtensionsSystemException
 	 */
 	Collection<EntityMapCondition> getAllConditionsByStaticRecordId(Long staticRecordId)
 			throws DynamicExtensionsSystemException;
 
 	/**
 	 * @param entityName to get entityGroup
 	 * @return entityGroupName of a particular entity
 	 * @throws DynamicExtensionsSystemException
 	 */
 	String getEntityGroupNameByEntityName(String entityName, Long containerId)
 			throws DynamicExtensionsSystemException;
 
 	/**
 	 * This method return all the records based on entity
 	 * @param entity Identifier of entity
 	 * @param recordId recordId of entity
 	 * @return recordValues for the entity
 	 * @throws DynamicExtensionsSystemException fails to get entity record
 	 * @throws DynamicExtensionsApplicationException fails to get entity record
 	 */
 	Map<AbstractAttributeInterface, Object> getRecordForSingleEntity(EntityInterface entity,
 			Long recordId) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException;
 
 	/**
 	 * @param pathAssociationRelationId  identifier of pathAssociationRelationId
 	 * @return association Id based on pathAssociationRelationId
 	 * @throws DynamicExtensionsSystemException
 	 */
 	Long getAssociationIdFrmPathAssoRelationId(Long pathAssociationRelationId)
 			throws DynamicExtensionsSystemException;
 
 	/**
 	 * @param pathId identifier of Path
 	 * @return collection of PathAssociationRelationIds
 	 * @throws DynamicExtensionsSystemException
 	 */
 	Collection<Long> getPathAssociationRelationIds(Long pathId)
 			throws DynamicExtensionsSystemException;
 
 	/**
 	 * @param associationId  identifier of association
 	 * @return source entity name
 	 * @throws DynamicExtensionsSystemException
 	 */
 	String getSrcEntityNameFromAssociationId(Long associationId)
 			throws DynamicExtensionsSystemException;
 
 	/**
 	 * @param associationId  identifier of association
 	 * @return target entity name
 	 * @throws DynamicExtensionsSystemException
 	 */
 	String getTgtEntityNameFromAssociationId(Long associationId)
 			throws DynamicExtensionsSystemException;
 
 	/**
 	 * @param associationRelationId  PathAssociationRelationId
 	 * @return instanceId of the source categoryEntity
 	 * @throws DynamicExtensionsSystemException
 	 */
 	Long getSrcInstanceIdFromAssociationRelationId(Long associationRelationId)
 			throws DynamicExtensionsSystemException;
 
 	/**
 	 * @param associationRelationId  PathAssociationRelationId
 	 * @return instanceId of the target categoryEntity
 	 * @throws DynamicExtensionsSystemException
 	 */
 	Long getTgtInstanceIdFromAssociationRelationId(Long associationRelationId)
 			throws DynamicExtensionsSystemException;
 
 	/**
 	 * @return categoryEntityId collection
 	 * @throws DynamicExtensionsSystemException
 	 */
 	Collection<Long> getAllCategoryEntityId() throws DynamicExtensionsSystemException;
 
 	/**
 	 * @param categoryId identifier of category entity
 	 * @return name of category
 	 * @throws DynamicExtensionsSystemException
 	 */
 	String getCategoryEntityNameByCategoryEntityId(Long categoryId)
 			throws DynamicExtensionsSystemException;
 
 }
