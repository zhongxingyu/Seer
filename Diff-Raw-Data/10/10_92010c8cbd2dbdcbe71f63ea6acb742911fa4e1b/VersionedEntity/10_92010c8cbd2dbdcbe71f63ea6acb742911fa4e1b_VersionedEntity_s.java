 package topshelf.utils.persist;
 
 import java.util.Date;
 
 import javax.persistence.MappedSuperclass;
 import javax.persistence.PrePersist;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.Version;
import javax.validation.constraints.NotNull;
 
 @MappedSuperclass
 public abstract class VersionedEntity<PK, T extends EntityHelper<PK,T>> extends EntityHelper<PK,T> {
 
 	@PrePersist
 	void pre() {
 		if (null == created)
 			created = new Date();
 	}
 
 	@Version
 	protected int version;
 	
	@NotNull
 	@Temporal(TemporalType.TIMESTAMP)
 	private Date created;
 	
 	public int getVersion() {
 		return version;
 	}
 	
 	public void setVersion(int version) {
 		this.version = version;
 	}
 	
 	public Date getCreated() {
 		return created;
 	}
 	
 	public void setCreated(Date created) {
 		this.created = created;
 	}
 }
