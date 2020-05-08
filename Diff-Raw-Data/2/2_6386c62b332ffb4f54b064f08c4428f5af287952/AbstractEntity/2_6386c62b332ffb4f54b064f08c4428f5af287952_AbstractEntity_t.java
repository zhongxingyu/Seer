 package de.thomas_letsch.model;
 
 import java.io.Serializable;
 
 import javax.persistence.Column;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.MappedSuperclass;
 import javax.persistence.Version;
 
 /**
  * Class implementing the basic properties of all entities.
  */
 @MappedSuperclass
 public abstract class AbstractEntity implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id = null;
 
 	@Version
 	@Column(nullable = false)
 	private Long version;
 
 	public final Long getId() {
 		return id;
 	}
 
 	public final Long getVersion() {
 		return version;
 	}
 }
