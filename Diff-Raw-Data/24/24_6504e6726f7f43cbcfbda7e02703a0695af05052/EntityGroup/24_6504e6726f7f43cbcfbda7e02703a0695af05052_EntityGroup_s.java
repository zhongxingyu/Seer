 
 package edu.common.dynamicextensions.domain;
 
 import java.util.Collection;
 import java.util.HashSet;
 
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 
 /**
  * This Class represents a Group of Entities.
  * @version 1.0
  * @created 28-Sep-2006 12:20:08 PM
  * @hibernate.joined-subclass table="DYEXTN_ENTITY_GROUP"
  * @hibernate.joined-subclass-key column="IDENTIFIER"
  */
 public class EntityGroup extends AbstractMetadata implements EntityGroupInterface
 {
 
 	/**
 	 * Serial Version Unique Identifier
 	 */
 	private static final long serialVersionUID = 1234567890L;
 
 	/**
 	 * Short name of the Entity group.
 	 */
 	protected String shortName;
 
 	/**
 	 * Long  name of the Entity group.
 	 */
 	protected String longName;
 
 	/**
 	 * The Version of the Entity group.
 	 */
 	protected String version;
 
 	/**
 	 * isSystemGenerated.
 	 */
	protected Boolean isSystemGenerated = new Boolean(true);
 
 	/**
 	 * Collection of Entity in this Entity group.
 	 */
 	protected Collection<EntityInterface> entityCollection = new HashSet<EntityInterface>();
 
 	/**
 	 * isCurrent.
 	 */
 	boolean isCurrent = false;
 
 	/**
 	 * mainContainerCollection.
 	 */
 	protected Collection<ContainerInterface> mainContainerCollection = new HashSet<ContainerInterface>();
 
 	/**
 	 * Empty Constructor
 	 */
 	public EntityGroup()
 	{
 	}
 
     //column="ENTITY_ID"
 
 	/**
 	 * This method returns the Collection of the Entities in the group.
 	 * @hibernate.set name="entityCollection" table="DYEXTN_ENTITY"
      * cascade="save-update" inverse="false" lazy="false"
 	 * @hibernate.collection-key column="ENTITY_GROUP_ID"
 	 * @hibernate.cache  usage="read-write"
 	 * @hibernate.collection-one-to-many class="edu.common.dynamicextensions.domain.Entity"
 	 * @return Returns the Collection of the Entities in the group.
 	 */
 	public Collection<EntityInterface> getEntityCollection()
 	{
 		return entityCollection;
 	}
 
 	/**
 	 * This method sets the entityCollection to the given Collection of the Entities.
 	 * @param entityCollection The entityCollection to set.
 	 */
 	public void setEntityCollection(Collection<EntityInterface> entityCollection)
 	{
 		this.entityCollection = entityCollection;
 	}
 
 	/**
 	 * This method returns the long name of the Entity group.
 	 * @hibernate.property name="longName" type="string" column="LONG_NAME"
 	 * @return the long name of the Entity group..
 	 */
 	public String getLongName()
 	{
 		return longName;
 	}
 
 	/**
 	 * This method sets the long name of the Entity group to the given name
 	 * @param longName the name to be set.
 	 */
 	public void setLongName(String longName)
 	{
 		this.longName = longName;
 	}
 
 	/**
 	 * This method returns the short name of the Entity group.
 	 * @hibernate.property name="shortName" type="string" column="SHORT_NAME"
 	 * @return the short name of the Entity group.
 	 */
 	public String getShortName()
 	{
 		return shortName;
 	}
 
 	/**
 	 * This method sets the short name of the Entity group to the given name
 	 * @param longName the name to be set.
 	 */
 	public void setShortName(String shortName)
 	{
 		this.shortName = shortName;
 	}
 
 	/**
 	 * This method returns the version of the Entity group.
 	 * @hibernate.property name="version" type="string" column="VERSION"
 	 * @return the version of the Entity group.
 	 */
 	public String getVersion()
 	{
 		return version;
 	}
 
 	/**
 	 * This method sets the version of the Entity group to the given version
 	 * @param version the version to be set.
 	 */
 	public void setVersion(String version)
 	{
 		this.version = version;
 	}
 
 
 
      /**
      *
      */
     public void addEntity(EntityInterface entityInterface) {
         if (this.entityCollection == null) {
             entityCollection = new HashSet<EntityInterface>();
         }
         entityCollection.add(entityInterface);
 
     }
 
     /**
      *
      */
     public void removeEntity(EntityInterface entityInterface) {
 
     	if (entityCollection.contains(entityInterface))
 		{
 			entityCollection.remove(entityInterface);
         }
     }
 
 
 
 
 
 
     /**
 	 * This method returns the Collection of AbstractAttribute.
 	 * @hibernate.set name="mainContainerCollection" table="DYEXTN_CONTAINER"
 	 * cascade="save-update" inverse="false" lazy="false"
 	 * @hibernate.collection-key column="ENTITY_GROUP_ID"
 	 * @hibernate.cache  usage="read-write"
 	 * @hibernate.collection-one-to-many class="edu.common.dynamicextensions.domain.userinterface.Container"
 	 * @return the Collection of main containers of this entity group.
 	 */
 	public Collection<ContainerInterface> getMainContainerCollection()
 	{
 		return mainContainerCollection;
 	}
 
 
 	/**
 	 * @param mainContainerCollection The mainContainerCollection to set.
 	 */
 	public void setMainContainerCollection(Collection<ContainerInterface> mainContainerCollection)
 	{
 		this.mainContainerCollection = mainContainerCollection;
 	}
 
 	/**
 	 * @return Returns the isCurrent.
 	 */
 	public boolean isCurrent()
 	{
 		return isCurrent;
 	}
 
 
 	/**
 	 * @param isCurrent The isCurrent to set.
 	 */
 	public void setCurrent(boolean isCurrent)
 	{
 		this.isCurrent = isCurrent;
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.domaininterface.EntityGroupInterface#addMainContainer(edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface)
 	 */
 	public void addMainContainer(ContainerInterface containerInterface)
 	{
 		this.mainContainerCollection.add(containerInterface);
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.domaininterface.EntityGroupInterface#removeMainContainer(edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface)
 	 */
 	public void removeMainContainer(ContainerInterface containerInterface)
 	{
 		this.mainContainerCollection.remove(containerInterface);
 	}
 
 
 	/**
 	 * This method returns whether the Attribute is a Collection or not.
 	 * @hibernate.property name="isSystemGenerated" type="boolean" column="IS_SYSTEM_GENERATED"
 	 * @return Returns the isSystemGenerated.
 	 */
 	public Boolean getIsSystemGenerated()
 	{
 		return isSystemGenerated;
 	}
 
 
 	/**
 	 * @param isSystemGenerated
 	 */
 	public void setIsSystemGenerated(Boolean isSystemGenerated)
 	{
 		this.isSystemGenerated = isSystemGenerated;
 	}
 
 	public EntityInterface getEntityByName(String entityName)
 	{
 		for(EntityInterface entity : entityCollection)
 		{
 			if(entity.getName().equalsIgnoreCase(entityName))
 			{
				return entity;
 			}
 		}
		return null;
 	}
 
 }
