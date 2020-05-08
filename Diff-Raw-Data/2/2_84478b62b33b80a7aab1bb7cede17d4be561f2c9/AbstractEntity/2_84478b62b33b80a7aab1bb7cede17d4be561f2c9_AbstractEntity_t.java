 package com.mjeanroy.springhub.models.entities;
 
 import javax.persistence.*;
 
 /**
  * Entity using default configuration :
  * <ul>
  * <li>Primary key is a Long.</li>
  * <li>Identifier is stored in a column named 'ID'</li>
  * <li>Id is generated using {@linkplain javax.persistence.GenerationType#IDENTITY}</li>
  * </ul>
  */
 @MappedSuperclass
 public abstract class AbstractEntity extends AbstractGenericEntity {
 
 	/** Id of entity */
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false, precision = 22, scale = 0)
 	protected Long id;
 
 	public AbstractEntity() {
 		super();
 	}
 
 	@Override
 	public Long entityId() {
 		return getId();
 	}
 
 	@Override
 	public Long modelId() {
 		return getId();
 	}
 
 	/**
 	 * Get {@link #id}
 	 *
 	 * @return {@link #id}
 	 */
 	public Long getId() {
 		return id;
 	}
 
 	/**
 	 * Set {@link #id}
 	 *
 	 * @param id
 	 */
 	public void setId(Long id) {
 		this.id = id;
 	}
 }
