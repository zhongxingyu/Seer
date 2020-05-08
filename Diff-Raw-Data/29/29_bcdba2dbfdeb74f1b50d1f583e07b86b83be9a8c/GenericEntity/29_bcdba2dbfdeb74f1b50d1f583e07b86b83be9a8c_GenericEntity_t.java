 package uk.frequency.glance.server.model;
 
 import java.util.Date;
 
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.MappedSuperclass;
 import javax.persistence.PrePersist;
 import javax.persistence.PreUpdate;
 
 @MappedSuperclass
 public abstract class GenericEntity {
 
 	@Id
 	@GeneratedValue(strategy=GenerationType.TABLE) //TODO create different generators for each hierarchy
	protected Long id;
 	
 //	@Column(insertable=false, updatable=false, nullable=false)
 	protected Date creationTime;
 	
 //	@Column(insertable=false, updatable=false, nullable=false)
 	protected Date updateTime;
 	
 //	@Column(insertable=false, updatable=false, nullable=false)
	protected Boolean deleted;
 
 	
 	@PrePersist
 	public void prePersist(){
 		updateTime = creationTime = new Date(); //TODO use hibernate "Entity Manager" to make this work
 	}
 	
 	@PreUpdate
 	public void preUpdate(){
 		updateTime = new Date();  //TODO use hibernate "Entity Manager" to make this work
 	}
 	
	public Long getId() {
 		return id;
 	}
 
	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public Date getCreationTime() {
 		return creationTime;
 	}
 
 	public void setCreationTime(Date creationTime) {
 		this.creationTime = creationTime;
 	}
 
 	public Date getUpdateTime() {
 		return updateTime;
 	}
 
 	public void setUpdateTime(Date updateTime) {
 		this.updateTime = updateTime;
 	}
 
 	public boolean isDeleted() {
 		return deleted;
 	}
 
 	public void setDeleted(boolean deleted) {
 		this.deleted = deleted;
 	}
 	
 	@Override
 	public String toString() {
 		return this.getClass().getSimpleName() 
 				+ ": " + id;
 	}
 	
 }
