 
 package edu.wustl.cab2b.server.datalist;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import edu.common.dynamicextensions.domain.DomainObjectFactory;
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.RoleInterface;
 import edu.common.dynamicextensions.entitymanager.EntityManager;
 import edu.common.dynamicextensions.entitymanager.EntityManagerConstantsInterface;
 import edu.common.dynamicextensions.entitymanager.EntityManagerInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.util.global.Constants.AssociationDirection;
 import edu.common.dynamicextensions.util.global.Constants.AssociationType;
 import edu.common.dynamicextensions.util.global.Constants.Cardinality;
 import edu.wustl.cab2b.common.datalist.DataList;
 import edu.wustl.cab2b.common.datalist.DataListBusinessInterface;
 import edu.wustl.cab2b.common.datalist.IDataRow;
 import edu.wustl.cab2b.common.domain.DataListMetadata;
 import edu.wustl.cab2b.server.util.DynamicExtensionUtility;
 import edu.wustl.common.bizlogic.DefaultBizLogic;
 import edu.wustl.common.dao.AbstractDAO;
 import edu.wustl.common.dao.DAO;
 import edu.wustl.common.dao.DAOFactory;
 import edu.wustl.common.exception.BizLogicException;
 import edu.wustl.common.security.exceptions.UserNotAuthorizedException;
 import edu.wustl.common.util.dbManager.DAOException;
 import edu.wustl.common.util.global.Constants;
 import edu.wustl.common.util.logger.Logger;
 
 /**
  * Class which performs several <i>Data List</i> operations like 
  * save, retreive etc.
  * @author chetan_bh
  */
 public class DataListOperations extends DefaultBizLogic
 {
 
 	/**
 	 * Hibernate DAO Type to use.
 	 */
 	private static final int DAO_TYPE = Constants.HIBERNATE_DAO;
 
 	/**
 	 * DE's Entity manager instance.
 	 */
 	EntityManagerInterface entityManager = EntityManager.getInstance();
 
 	/**
 	 * DE's DomainObjectFactory instance.
 	 */
 	DomainObjectFactory domainObjectFactory = DomainObjectFactory.getInstance();
 
 	/**
 	 * DataList entity group. 
 	 */
 	EntityGroupInterface dataListEntityGroup = DynamicExtensionUtility
 			.getEntityGroupByName(edu.wustl.cab2b.common.util.Constants.DATALIST_ENTITY_GROUP_NAME);
 
 	/**
 	 * Map to maintain list of new entities created so that 
 	 * we can reuse the existing entiy later.
 	 * 
 	 * New entities will contain new attributes, corresponding to 
 	 * old entitiy's old attributes. 
 	 */
 	Map<String, EntityInterface> mapOfNewEntities = new HashMap<String, EntityInterface>();
 
 	/**
 	 * Map to maintain list of new Associations created between
 	 * new entities to reuse later. 
 	 */
 	Map<String, AssociationInterface> mapOfNewAssociations = new HashMap<String, AssociationInterface>();
 
 	/**
 	 * This is a Map of attribute->value and association->list_of_containments.
 	 * This map complies to the requiremnt of 2nd parameter in 
 	 * {@link EntityManager#insertData(EntityInterface, Map)}.
 	 */
 	Map<AbstractAttributeInterface, Object> dataListAttributesMap;
 
 	/**
 	 * Entity instance for data list as an entity.
 	 */
 	EntityInterface dataListEntity;
 
 	public List<DataListMetadata> retrieveAllDataListMetadata()
 			throws DAOException, ClassNotFoundException
 	{
 		List<DataListMetadata> allDataList = null;
 
 		String hql = "from DataListMetadata";
 		DAO dao = DAOFactory.getInstance().getDAO(DAO_TYPE);
 		((AbstractDAO) dao).openSession(null);
 
 		allDataList = (List<DataListMetadata>) dao.executeQuery(hql, null, false, null);
 		Logger.out.info("allDataListMetadata.size() ########### " + allDataList.size());
 		return allDataList;
 	}
 
 	public DataList retrieveDataList(Long dataListId)
 	{
 		DataList dataList = new DataList();
 		//TODO Yet to implement this functionality.
 		return dataList;
 	}
 
 	/**
 	 * Constructs the recursive map {@link DataListOperations#dataListAttributesMap}} recursively 
 	 * and persists it to the database using Dynamic Extension APIs.
 	 *  
 	 * @param dataListToSave
 	 * @return idetifier of the new root level entity(new DataList entity) created.
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 * @throws UserNotAuthorizedException 
 	 * @throws BizLogicException 
 	 * @throws DAOException 
 	 */
 	public Long save(DataList dataListToSave) throws DynamicExtensionsApplicationException,
 			DynamicExtensionsSystemException, BizLogicException, UserNotAuthorizedException
 	{
 		Logger.out.info("DataListOperations :: save() " + dataListToSave);
 		List<IDataRow> dataRows = dataListToSave.getDataList();
 
 		/* There will always be one root node is the datalist tree. */
 		IDataRow rootDataRow = dataRows.get(0);
 
 		createMapForDataListEntity(dataListToSave.getDataListAnnotation());
 
 		for (IDataRow firstLevelTypeDataRow : rootDataRow.getChildren())
 		{
 			AssociationInterface newAssociation = null;
 
 			if (firstLevelTypeDataRow.isData() == false)
 			{
 				List<IDataRow> valueDataRowsForThisType = firstLevelTypeDataRow.getChildren();
 				for (IDataRow firstLevelDataRow : valueDataRowsForThisType)
 				{
 					EntityInterface newCurrentEntity = mapOfNewEntities.get(firstLevelDataRow
 							.getEntityInterface().getName()
 							+ "_" + 2);
 					if (newCurrentEntity == null)
 					{
 						newCurrentEntity = getNewEntity(firstLevelDataRow.getEntityInterface());
 						mapOfNewEntities.put(firstLevelDataRow.getEntityInterface().getName() + "_"
 								+ 2, newCurrentEntity);
 					}
 
 					newAssociation = mapOfNewAssociations.get(dataListEntity.getName() + "_"
 							+ firstLevelDataRow.getEntityInterface().getName());
 
 					if (newAssociation == null)
 					{
 						newAssociation = createNewOneToManyAsso(dataListEntity, newCurrentEntity);
 						mapOfNewAssociations.put(dataListEntity.getName() + "_"
 								+ firstLevelDataRow.getEntityInterface().getName(), newAssociation);
 						dataListAttributesMap.put(newAssociation, new ArrayList());
 						dataListEntity.addAssociation(newAssociation);
 					}
 					List<Map> listOfSubMaps = (List<Map>) dataListAttributesMap.get(newAssociation);
 
 					Map<AbstractAttributeInterface, Object> mapForFirstLevelDataRow = new HashMap<AbstractAttributeInterface, Object>();
 					constructDataRowMap(firstLevelDataRow, mapForFirstLevelDataRow,
 							newCurrentEntity, 3);
 					//Logger.out.info("InsertThis ####### " + mapForFirstLevelDataRow);
 
 					listOfSubMaps.add(mapForFirstLevelDataRow);
 				}
 			}
 			else
 			{
 				EntityInterface newCurrentEntity = mapOfNewEntities.get(firstLevelTypeDataRow
 						.getEntityInterface().getName()
 						+ "_" + 2);
 				if (newCurrentEntity == null)
 				{
 					newCurrentEntity = getNewEntity(firstLevelTypeDataRow.getEntityInterface());
 					mapOfNewEntities.put(firstLevelTypeDataRow.getEntityInterface().getName() + "_"
 							+ 2, newCurrentEntity);
 				}
 
 				newAssociation = mapOfNewAssociations.get(dataListEntity.getName() + "_"
 						+ firstLevelTypeDataRow.getEntityInterface().getName());
 				if (newAssociation == null)
 				{
 					newAssociation = createNewOneToManyAsso(dataListEntity, newCurrentEntity);
 					mapOfNewAssociations.put(dataListEntity.getName() + "_"
 							+ firstLevelTypeDataRow.getEntityInterface().getName(), newAssociation);
 					dataListAttributesMap.put(newAssociation, new ArrayList());
 					dataListEntity.addAssociation(newAssociation);
 				}
 				List<Map> listOfSubMaps = (List<Map>) dataListAttributesMap.get(newAssociation);
 
 				Map<AbstractAttributeInterface, Object> mapForFirstLevelDataRow = new HashMap<AbstractAttributeInterface, Object>();
 				constructDataRowMap(firstLevelTypeDataRow, mapForFirstLevelDataRow,
 						newCurrentEntity, 3);
 
 				listOfSubMaps.add(mapForFirstLevelDataRow);
 			}
 		}
 
 		Long entityId = entityManager.persistEntity(dataListEntity).getId();
 
 		Long recordId = entityManager.insertData(dataListEntity, dataListAttributesMap);
 
 		Logger.out.info("this is final schema for DataList " + dataListEntity);
 		Logger.out.info("DataList saved successfully !!!! (entityId, recordId) :: " + entityId
 				+ ", " + recordId);
 
 		DataListMetadata dataListMetadata = dataListToSave.getDataListAnnotation();
 		dataListMetadata.setEntityId(entityId);
 		
 		Long dataListId = saveDataListMetadata(dataListMetadata);
 
 		return dataListId;
 	}
 
 	/**
 	 * Creates and returns a new one to many association between source target entities.
 	 * @param srcEntity source entity of the new association
 	 * @param tarEntity target enetiyt of the new association
 	 * @return new association
 	 */
 	private AssociationInterface createNewOneToManyAsso(EntityInterface srcEntity,
 			EntityInterface tarEntity)
 	{
 		AssociationInterface returner = null;
 
 		returner = domainObjectFactory.createAssociation();
 		String associationName = "AssociationName_" + srcEntity.getAssociationCollection().size()
 				+ 1;
 		returner.setName(associationName);
 		returner.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 		returner.setEntity(srcEntity);
 		returner.setTargetEntity(tarEntity);
 		returner.setSourceRole(getNewRole(AssociationType.CONTAINTMENT, "source_role_"
 				+ associationName, Cardinality.ONE, Cardinality.ONE));
 		returner.setTargetRole(getNewRole(AssociationType.CONTAINTMENT, "target_role_"
 				+ associationName, Cardinality.ZERO, Cardinality.MANY));
 
 		return returner;
 	}
 
 	/**
 	 * Creates and returns new Role for an association.
 	 * @param associationType associationType
 	 * @param name name
 	 * @param minCard  minCard
 	 * @param maxCard maxCard
 	 * @return  RoleInterface
 	 */
 	private RoleInterface getNewRole(AssociationType associationType, String name,
 			Cardinality minCard, Cardinality maxCard)
 	{
 		RoleInterface role = domainObjectFactory.createRole();
 		role.setAssociationsType(associationType);
 		role.setName(name);
 		role.setMinimumCardinality(minCard);
 		role.setMaximumCardinality(maxCard);
 		return role;
 	}
 
 	/**
 	 * 
 	 * @param dataListMetadata
 	 */
 	private void createMapForDataListEntity(DataListMetadata dataListMetadata)
 	{
 		dataListAttributesMap = new HashMap<AbstractAttributeInterface, Object>();
 
 		dataListEntity = domainObjectFactory.createEntity();
 
 		dataListEntity.setName("DataList_" + System.currentTimeMillis());
 
 		dataListEntity.addEntityGroupInterface(dataListEntityGroup);
 
 		dataListEntityGroup.addEntity(dataListEntity);
 
 		AttributeInterface dataListName = domainObjectFactory.createStringAttribute();
 		dataListName.setName("name");
 
 		AttributeInterface dataListDesc = domainObjectFactory.createStringAttribute();
 		dataListDesc.setName("description");
 
 		AttributeInterface dataListDateCreated = domainObjectFactory.createDateAttribute();
 		dataListDateCreated.setName("dateCreatedOn");
 
 		AttributeInterface dataListDateLastModified = domainObjectFactory.createDateAttribute();
 		dataListDateLastModified.setName("dataLastModifiedOn");
 
 		dataListEntity.addAttribute(dataListName);
 		dataListEntity.addAttribute(dataListDesc);
 		dataListEntity.addAttribute(dataListDateCreated);
 		dataListEntity.addAttribute(dataListDateLastModified);
 
 		dataListAttributesMap.put(dataListName, dataListMetadata.getName());
 		dataListAttributesMap.put(dataListDesc, dataListMetadata.getDescription());
 		dataListAttributesMap.put(dataListDateCreated, dataListMetadata.getCreatedOn());
 		dataListAttributesMap.put(dataListDateLastModified, dataListMetadata.getLastUpdatedOn());
 
 		mapOfNewEntities.put(dataListEntity.getName() + "_1", dataListEntity); // Tree level one.		
 	}
 
 	/**
 	 * A recursive function to construct the recursive map of attribute->value 
 	 * and association->list(map).
 	 * 
 	 * @param dataRowToSave
 	 * @param mapToConstruct
 	 * @param newEntity
 	 * @param treeLevel
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private void constructDataRowMap(IDataRow dataRowToSave,
 			Map<AbstractAttributeInterface, Object> mapToConstruct, EntityInterface newEntity,
 			int treeLevel) throws DynamicExtensionsApplicationException,
 			DynamicExtensionsSystemException
 	{
 		Object[] values = dataRowToSave.getRow();
 
		// if a DataRow has values, it means it is a record.
 		if (values != null)
 		{
 			/* Find an existing entity in the newEntitiesMap for with same name and same tree level */
 
 			List<AttributeInterface> newAttributes = getOrderedAttributes(dataRowToSave
 					.getAttributes(), newEntity.getAttributeCollection());
 
 			for (int i = 0; i < newAttributes.size(); i++)
 			{
 				AttributeInterface attribute = newAttributes.get(i);
 				Object value = values[i];
 				mapToConstruct.put(attribute, value);
 			}
 
 			List<IDataRow> children = dataRowToSave.getChildren();
			
 			if (children != null && children.size() > 0)
 			{
 				treeLevel++;
 				for (IDataRow dataRow : children)
				{					
					if(dataRow.getEntityInterface() == null) // or if row values == null
 					{
 						for(IDataRow subDR : dataRow.getChildren())
 						{
 							EntityInterface newChildEntity = mapOfNewEntities.get(subDR.getEntityInterface().getName()+"_"+treeLevel);
 							if(newChildEntity == null)
 							{
 								newChildEntity = getNewEntity(subDR.getEntityInterface());
 								mapOfNewEntities.put(subDR.getEntityInterface().getName()+"_"+treeLevel, newChildEntity);
 							}
 							
 							AssociationInterface deAsso = mapOfNewAssociations.get(newEntity.getName()+"_"+newChildEntity.getName());
 							
 							if(deAsso == null)
 							{
 								deAsso = createNewOneToManyAsso(newEntity, newChildEntity);
 								mapOfNewAssociations.put(newEntity.getName()+"_"+newChildEntity.getName(), deAsso);
 								mapToConstruct.put(deAsso, new ArrayList<Map>());
 								newEntity.addAssociation(deAsso);
 							}
 							List<Map> listOfMapToAppend = (List<Map>) mapToConstruct.get(deAsso);
 							/* This if block is needed because same association may be used in g1-(p1,p2)  and also in g2-(p3,p4) 
 							 * In this case we have to add the DE association and new arraylist to the new map to construct for 
 							 * second association  */
 							if(listOfMapToAppend == null)
 							{
 								listOfMapToAppend = new ArrayList<Map>();
 								mapToConstruct.put(deAsso, listOfMapToAppend);
 							}
 							Map<AbstractAttributeInterface, Object> mapToAppend = new HashMap<AbstractAttributeInterface, Object>();
 							
 							constructDataRowMap(subDR, mapToAppend, newChildEntity, treeLevel+1);
 							
 							listOfMapToAppend.add(mapToAppend);
 							
 						}
 					}
 					else{
 						EntityInterface newChildEntity = mapOfNewEntities.get(dataRow
 								.getEntityInterface().getName()
 								+ "_" + treeLevel);
 						if (newChildEntity == null)
 						{
 							newChildEntity = getNewEntity(dataRow.getEntityInterface());
 							mapOfNewEntities.put(dataRow.getEntityInterface().getName() + "_"
 									+ treeLevel, newChildEntity);
 						}
 
 						AssociationInterface deAsso = mapOfNewAssociations.get(newEntity.getName()
 								+ "_" + newChildEntity.getName());
 						if (deAsso == null)
 						{	
 							deAsso = createNewOneToManyAsso(newEntity, newChildEntity);
 							mapOfNewAssociations.put(newEntity.getName() + "_"
 									+ newChildEntity.getName(), deAsso);
 							mapToConstruct.put(deAsso, new ArrayList<Map>());
 							newEntity.addAssociation(deAsso);
 						}
 						List<Map> listOfMapToAppend = (List<Map>) mapToConstruct.get(deAsso);
						if(listOfMapToAppend == null)
						{
							listOfMapToAppend = new ArrayList<Map>();
							mapToConstruct.put(deAsso, listOfMapToAppend);
						}
						
 						Map<AbstractAttributeInterface, Object> mapToAppend = new HashMap<AbstractAttributeInterface, Object>();
 
 						constructDataRowMap(dataRow, mapToAppend, newChildEntity, treeLevel);
 
 						listOfMapToAppend.add(mapToAppend);
 					}
 				}
 			}
 		}
 		else
 		{
 			List<IDataRow> children = dataRowToSave.getChildren();
 			if (children != null)
 			{
 				for (IDataRow dataRow : children)
 				{
 					constructDataRowMap(dataRow, mapToConstruct, newEntity, treeLevel);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Returns the ordered attributes of unordered list in a new list.
 	 * @param orderedOldAttribs
 	 * @param unOrderedNewAttribs
 	 * @return
 	 */
 	private List<AttributeInterface> getOrderedAttributes(
 			List<AttributeInterface> orderedOldAttribs,
 			Collection<AttributeInterface> unOrderedNewAttribs)
 	{
 		List<AttributeInterface> returner = new ArrayList<AttributeInterface>();
 		for (AttributeInterface attribute : orderedOldAttribs)
 		{
 			AttributeInterface foundAttrib = findAttributeByName(unOrderedNewAttribs, attribute
 					.getName());
 			returner.add(foundAttrib);
 		}
 		return returner;
 	}
 
 	/**
 	 * Finds and returns an atttribute by name in the list of attributes.
 	 * @param attribs
 	 * @param attribName
 	 * @return
 	 */
 	private AttributeInterface findAttributeByName(Collection<AttributeInterface> attribs,
 			String attribName)
 	{
 		AttributeInterface returner = null;
 		for (AttributeInterface attrib : attribs)
 		{
 			if (attrib.getName().startsWith(attribName))
 			{
 				returner = attrib;
 				break;
 			}
 		}
 		return returner;
 	}
 
 	/**
 	 * Creates and returns a new attribute given old attribute.
 	 * @param oldAttribute
 	 * @return
 	 */
 	private AttributeInterface getNewAttribute(AttributeInterface oldAttribute)
 	{
 		//Logger.out.info("DataListSessionBean :: getNewAttribute");
 		AttributeInterface newAttribute = null;
 		String oldAttributeType = oldAttribute.getDataType();
 		if (oldAttributeType.equals(EntityManagerConstantsInterface.STRING_ATTRIBUTE_TYPE))
 			newAttribute = domainObjectFactory.createStringAttribute();
 		else if (oldAttributeType.equals(EntityManagerConstantsInterface.INTEGER_ATTRIBUTE_TYPE))
 			newAttribute = domainObjectFactory.createIntegerAttribute();
 		else if (oldAttributeType.equals(EntityManagerConstantsInterface.DATE_ATTRIBUTE_TYPE))
 			newAttribute = domainObjectFactory.createDateAttribute();
 		else if (oldAttributeType.equals(EntityManagerConstantsInterface.DOUBLE_ATTRIBUTE_TYPE))
 			newAttribute = domainObjectFactory.createDoubleAttribute();
 		else if (oldAttributeType.equals(EntityManagerConstantsInterface.FILE_ATTRIBUTE_TYPE))
 			newAttribute = domainObjectFactory.createFileAttribute();
 		else if (oldAttributeType.equals(EntityManagerConstantsInterface.SHORT_ATTRIBUTE_TYPE))
 			newAttribute = domainObjectFactory.createShortAttribute();
 		else if (oldAttributeType.equals(EntityManagerConstantsInterface.LONG_ATTRIBUTE_TYPE))
 			newAttribute = domainObjectFactory.createLongAttribute();
 		else if (oldAttributeType.equals(EntityManagerConstantsInterface.BOOLEAN_ATTRIBUTE_TYPE))
 			newAttribute = domainObjectFactory.createBooleanAttribute();
		else if (oldAttributeType.equals(EntityManagerConstantsInterface.FLOAT_ATTRIBUTE_TYPE))
			newAttribute = domainObjectFactory.createFloatAttribute();
		else if (oldAttributeType.equals(EntityManagerConstantsInterface.DATE_TIME_ATTRIBUTE_TYPE))
			newAttribute = domainObjectFactory.createDateAttribute();
 		else
 		{
 			Logger.out.debug("Extra attribute not mapped");
 		}
 		return newAttribute;
 	}
 
 	/**
 	 * Creates and returns a new entity given an old entity.
 	 * @param oldEntity
 	 * @return
 	 */
 	private EntityInterface getNewEntity(EntityInterface oldEntity)
 	{
		//Logger.out.info("oldEntity #### "+oldEntity.getName());
		//Logger.out.info("oldEntity attribs "+oldEntity.getAttributeCollection());
 		EntityInterface newEntity = domainObjectFactory.createEntity();
 		newEntity.addEntityGroupInterface(dataListEntityGroup);
 		dataListEntityGroup.addEntity(newEntity);
 
 		newEntity.setName(oldEntity.getName());
 
 		Collection<AttributeInterface> oldAttribs = oldEntity.getAttributeCollection();
 		for (AttributeInterface oldAttrib : oldAttribs)
 		{
			//Logger.out.info("oldAttri #### "+oldAttrib);
 			AttributeInterface newAttrib = getNewAttribute(oldAttrib);
 			newAttrib.setName(oldAttrib.getName());
 			newEntity.addAttribute(newAttrib);
 		}
 
 		return newEntity;
 	}
 
 	/**
 	 * @throws UserNotAuthorizedException 
 	 * @throws BizLogicException 
 	 * @see DataListBusinessInterface#saveDataListMetadata(DataListMetadata)
 	 */
 	public Long saveDataListMetadata(DataListMetadata datalistMetadata) throws 
 			BizLogicException, UserNotAuthorizedException
 	{
 		insert(datalistMetadata, DAO_TYPE);
 		Logger.out.info("########### saved matadata successfully ");
 		return datalistMetadata.getId();
 	}
 
 }
