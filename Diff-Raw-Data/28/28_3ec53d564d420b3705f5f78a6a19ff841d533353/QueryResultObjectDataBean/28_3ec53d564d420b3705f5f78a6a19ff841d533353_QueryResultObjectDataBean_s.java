 /**
  * 
  */
 
 package edu.wustl.common.beans;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.wustl.common.querysuite.security.PrivilegeType;
 
 /**
  * @author supriya_dankh
  * A class that will have information related CSM for every entity in query.Such as Entity name ,if this entity is 
  * main entity(for which CSM data is inserted in CSM tables) if not a main entity ,then which is main entity etc..   
  */
 public class QueryResultObjectDataBean
 {
 
 	//Particular entity from query for which QueryResultObjectDataBean has to be set.
 	private EntityInterface entity;
 
 	//Main entity of entity.
 	private EntityInterface mainEntity;
 
 	//Index of Identifier column of main entity.
 	private int mainEntityIdentifierColumnId = -1;
 
 	//Index of Identifier column of entity.
 	private int entityId;
 
 	//Privilege Type of entity (class level/object level/No privilege)
 	private PrivilegeType privilegeType;
 
 	//If this entity has associated Identified data or not.
 	private boolean hasAssociatedIdentifiedData;
 
 	//List of index  of identified data columns.
	private Vector<Integer> IdentifiedDataColumnIds = new Vector<Integer>();
 
 	//List of index  of object data columns.
	private Vector<Integer> objectColumnIds = new Vector<Integer>();
 
 	//Map of index of identifier columns of all main entities that are present in query.
 	private Map<EntityInterface, Integer> entityIdIndexMap = new HashMap<EntityInterface, Integer>();
 
 	//A boolean variable that will state if read denied could be possible on a object or not.
 	private boolean isReadDeniedObject = false;
 
 	private String csmEntityName;
 
 	// A boolean variable that will state if the entity contains any attribute of data type : file.
 	private boolean isClobeType;
 
 	// this map will have the key as the index of the file type attribute and value as its metadata
 	private Map<Integer, ? extends Object> fileTypeAtrributeIndexMetadataMap = new HashMap<Integer, Object>();
 	/*
 	 * 
 	 */
 	private List tqColumnMetadataList = new ArrayList();
 	
 	/**
 	 * Returns entity.
 	 * @return EntityInterface.
 	 */
 	public EntityInterface getEntity()
 	{
 		return entity;
 	}
 
 	/**
 	 * Sets entity.
 	 * @param entity
 	 */
 	public void setEntity(EntityInterface entity)
 	{
 		this.entity = entity;
 		this.csmEntityName = entity.getName();
 	}
 
 	/**
 	 * Get main entity of this particular entity.
 	 * @return EntityInterface.
 	 */
 	public EntityInterface getMainEntity()
 	{
 		return mainEntity;
 	}
 
 	/**
 	 * Sets main entity of this particular entity.
 	 * @param mainEntity
 	 */
 	public void setMainEntity(EntityInterface mainEntity)
 	{
 		this.mainEntity = mainEntity;
 		this.csmEntityName = mainEntity.getName();
 	}
 
 	/**
 	 * Get if this particular entity is main entity or not.
 	 * @return isMainEntity.
 	 */
 	public boolean isMainEntity()
 	{
 		return mainEntity == null;
 	}
 
 	/**
 	 * Get id of main entity.
 	 * @return main entity id.
 	 */
 	public int getMainEntityIdentifierColumnId()
 	{
 		return mainEntityIdentifierColumnId;
 	}
 
 	/**
 	 * Set id of main entity.
 	 * @param mainEntityIdentifierColumnId
 	 */
 	public void setMainEntityIdentifierColumnId(int mainEntityIdentifierColumnId)
 	{
 		this.mainEntityIdentifierColumnId = mainEntityIdentifierColumnId;
 	}
 
 	/**
 	 * Get id of entity.
 	 * @return
 	 */
 	public int getEntityId()
 	{
 		return entityId;
 	}
 
 	/**
 	 * Set id of entity
 	 * @param entityId
 	 */
 	public void setEntityId(int entityId)
 	{
 		this.entityId = entityId;
 	}
 
 	/**
 	 * Set if this entity has associated identified data or not.
 	 * @return hasAssociatedIdentifiedData.
 	 */
 	public boolean isHasAssociatedIdentifiedData()
 	{
 		return hasAssociatedIdentifiedData;
 	}
 
 	/**
 	 * Get if this entity has associated identified data or not.
 	 * @param hasAssociatedIdentifiedData
 	 */
 	public void setHasAssociatedIdentifiedData(boolean hasAssociatedIdentifiedData)
 	{
 		this.hasAssociatedIdentifiedData = hasAssociatedIdentifiedData;
 	}
 
 	/**
 	 * Set List of index of identified data columns.
 	 * @return IdentifiedDataColumnIds.
 	 */
	public Vector<Integer> getIdentifiedDataColumnIds()
 	{
 		return IdentifiedDataColumnIds;
 	}
 
 	/**
 	 * Get List of index of identified data columns.
 	 * @param identifiedDataColumnIds
 	 */
	public void setIdentifiedDataColumnIds(Vector<Integer> identifiedDataColumnIds)
 	{
 		IdentifiedDataColumnIds = identifiedDataColumnIds;
 	}
 
 	/**
 	 * Set List of index  of object data columns.
 	 * @return objectColumnIds.
 	 */
	public Vector<Integer> getObjectColumnIds()
 	{
 		return objectColumnIds;
 	}
 
 	/**
 	 * Get List of index  of object data columns.
 	 * @param objectColumnIds
 	 */
	public void setObjectColumnIds(Vector<Integer> objectColumnIds)
 	{
 		this.objectColumnIds = objectColumnIds;
 	}
 
 	/**
 	 * Get privilege type.
 	 * @return privilegeType.
 	 */
 	public PrivilegeType getPrivilegeType()
 	{
 		return privilegeType;
 	}
 
 	/**
 	 * Set privilege type.
 	 * @param privilegeType
 	 */
 	public void setPrivilegeType(PrivilegeType privilegeType)
 	{
 		this.privilegeType = privilegeType;
 	}
 
 	public Map<EntityInterface, Integer> getEntityIdIndexMap()
 	{
 		return entityIdIndexMap;
 	}
 
 	public void setEntityIdIndexMap(Map<EntityInterface, Integer> entityIdIndexMap)
 	{
 		this.entityIdIndexMap = entityIdIndexMap;
 	}
 
 	public boolean isReadDeniedObject()
 	{
 		return isReadDeniedObject;
 	}
 
 	public void setReadDeniedObject(boolean isReadDeniedObject)
 	{
 		this.isReadDeniedObject = isReadDeniedObject;
 	}
 
 	public String getCsmEntityName()
 	{
 		return csmEntityName;
 	}
 
 	public void setCsmEntityName(String csmEntityName)
 	{
 		this.csmEntityName = csmEntityName;
 	}
 
 	/**
	 * returns true if atleast one attribute is of 'file' type
 	 * @return value of isClobeType
 	 */
 	public boolean isClobeType()
 	{
 		return isClobeType;
 	}
 
 	/**
	 * sets clob type true if atleast one attribute is of 'file' type
	 * @param isClobeType - true if atleast one attribute is of 'file' type
 	 */
 	public void setClobeType(boolean isClobeType)
 	{
 		this.isClobeType = isClobeType;
 	}
 
 	/**
 	 * 
 	 * @return map will have the key as the index of the 'file' 
	 * type attribute and value as its metadata
 	 */
 	public Map<Integer, ? extends Object> getFileTypeAtrributeIndexMetadataMap()
 	{
 		return fileTypeAtrributeIndexMetadataMap;
 	}
 
 	/**
 	 * sets the map
 	 * @param fileTypeAtrributeIndexMetadataMap
 	 */
 	public void setFileTypeAtrributeIndexMetadataMap(
 			Map<Integer, ? extends Object> fileTypeAtrributeIndexMetadataMap)
 	{
 		this.fileTypeAtrributeIndexMetadataMap = fileTypeAtrributeIndexMetadataMap;
 	}
 	public List getTqColumnMetadataList() {
 		return tqColumnMetadataList;
 	}
 
 	public void setTqColumnMetadataList(List tqColumnMetadataList) {
 		this.tqColumnMetadataList = tqColumnMetadataList;
 	}
 }
