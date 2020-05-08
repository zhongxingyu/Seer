 package ee.itcollege.borderproject.common;
 
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.MappedSuperclass;
 import javax.persistence.PrePersist;
 import javax.persistence.PreRemove;
 import javax.persistence.PreUpdate;
 import javax.persistence.TableGenerator;
 import javax.validation.constraints.NotNull;
 
 import org.springframework.security.core.context.SecurityContextHolder;
 
 @MappedSuperclass
 public abstract class BaseEntity {
 	
 	@Id
 	@GeneratedValue(strategy = GenerationType.TABLE, 
 					generator =  "generatorName")
 	@TableGenerator(table= "SEQUENCE", 
 					initialValue = 1, 
 					allocationSize = 1, 
 					name = "generatorName",
 					pkColumnName = "SEQ_NAME",
 					valueColumnName = "SEQ_COUNT")
 	private int id;
 	
 	@Column(name = "avaja")
 	private String creator;
 	
 	@Column(name = "avatud")
 	private Date created;
 	
 	@Column(name = "muutja")
 	private String modifier;
 	
 	@Column(name = "muudetud")
 	private Date modified;
 	
 	@Column(name = "sulgeja")
 	private String remover;
 	
 	@Column(name = "suletud")
 	private Date removed;
 	
 	private int version;
 	
 	@Column(name = "kommentaar")
 	private String comment;
 	
 	public String getComment() {
 		return comment;
 	}
 
 	public int getVersion() {
 		return version;
 	}
 
 	public void setComment(String comment) {
 		this.comment = comment;
 	}
 
 	public void setVersion(int version) {
 		this.version = version;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;  
 	}
 	
 	public String getCreator() {
 		return creator;
 	}
 
 	public Date getCreated() {
 		return created;
 	}
 
 	public String getModifier() {
 		return modifier;
 	}
 
 	public Date getModified() {
 		return modified;
 	}
 
 	public String getRemover() {
 		return remover;
 	}
 
 	public Date getRemoved() {
 		return removed;
 	}
 
 	public void setCreator(String creator) {
 		this.creator = creator;
 	}
 
 	public void setCreated(Date created) {
 		this.created = created;
 	}
 
 	public void setModifier(String modifier) {
 		this.modifier = modifier;
 	}
 
 	public void setModified(Date modified) {
 		this.modified = modified;
 	}
 
 	public void setRemover(String remover) {
 		this.remover = remover;
 	}
 
 	public void setRemoved(Date removed) {
 		this.removed = removed;
 	}
 	
 	@PrePersist
     public void recordCreated() {
 		setCreator(getInvokerUserName());
         setCreated( new Date() );
     }
 
     @PreUpdate
     public void recordModified() {
     	setModifier(getInvokerUserName());
         setModified( new Date() );
     }
 
     // Because removing is prohibited, then we cannot set the remover here.
     // The remover is set in borderproject.common.CrudDaoJpa.java delete() method
     @PreRemove
     public void preventRemove() {
         throw new SecurityException("Removing is prohibited!");
     }
     
     private String getInvokerUserName() {
     	 if (SecurityContextHolder.getContext().getAuthentication() == null) 
     		 return "system";
     	   	 
     	 return SecurityContextHolder.getContext().getAuthentication().getName();
     }
     
 }
