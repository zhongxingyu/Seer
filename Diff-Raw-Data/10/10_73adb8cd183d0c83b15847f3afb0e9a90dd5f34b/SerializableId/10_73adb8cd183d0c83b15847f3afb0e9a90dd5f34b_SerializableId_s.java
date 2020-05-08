 package net.sf.gilead.core.serialization;
 
 import java.io.Serializable;
 
 /**
  * Id / Class structure for proxy information collection handling
  * @author bruno.marchesson
  *
  */
 public class SerializableId implements Serializable
 {
 	//----
 	// Attributes
 	//----
 	/**
 	 * Serialization ID
 	 */
 	private static final long serialVersionUID = -4365286012503534L;
 	
 	/**
 	 * The underlying id
 	 */
 	protected Serializable id;
 	
 	/**
 	 * Hash code for non persistent and transient values
 	 */
 	protected Integer hashCode;
 	
 	/**
 	 * The associated entity name
 	 */
 	protected String entityName;
 
 	//----
 	// Properties
 	//----
 	/**
 	 * @return the id
 	 */
 	public Serializable getId() {
 		return id;
 	}
 
 	/**
 	 * @param id the id to set
 	 */
 	public void setId(Serializable id) {
 		this.id = id;
 	}
 
 	/**
 	 * @return the hashCode
 	 */
 	public Integer getHashCode() {
 		return hashCode;
 	}
 
 	/**
 	 * @param hashCode the hashCode to set
 	 */
 	public void setHashCode(Integer hashCode) {
 		this.hashCode = hashCode;
 	}
 
 	/**
 	 * @return the entity Name
 	 */
 	public String getEntityName() {
 		return entityName;
 	}
 
 	/**
 	 * @param entityName the entity Name to set
 	 */
 	public void setEntityName(String entityName) {
 		this.entityName = entityName;
 	}
 	
 	//-------------------------------------------------------------------------
 	//
 	// Public interface
 	//
 	//-------------------------------------------------------------------------
 	@Override
 	public boolean equals(Object other)
 	{
 		if ((other == null) ||
 			(other instanceof SerializableId == false))
 		{
 			return false;
 		}
 		
		if (entityName.equals(((SerializableId)other).entityName) == false)
 		{
 			return false;
 		}
 		if (id == null)
 		{
 			return (((SerializableId)other).hashCode == hashCode);
 		}
 		else 
 		{
 			return id.equals(((SerializableId)other).id);
 		}
 	}
 }
