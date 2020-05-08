 package topshelf.utils.persist;
 
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.MappedSuperclass;
 
 /**
  * Use this as an Entity's baseclass to property expose the
  * InstanceRequest CRUD methods found in EntityHelper up on the
  * GWT RequestFactory level.  Otherwise, the simpler BasicEntity
  * will do just fine and not require the generic type of itself
  * defined, the type returned by persist() and merge().
  * 
  * @author bloo
  *
  * @param <T>
  */
 @MappedSuperclass
 public class BasicCrudEntity<T extends VersionedEntity<Long,T>> extends VersionedEntity<Long,T> {
 
 	@Id
 	@GeneratedValue(strategy=GenerationType.IDENTITY)
 	protected Long id;
 
 	public Long getId() {
 		return id;
 	}
 	
 	public void setId(Long id) {
 		this.id = id;
 	}
 	
 	@Override
 	public int hashCode() {
		return null == id ? null : getClass().hashCode() ^ id.hashCode();
 	}
 	
 	@Override
 	public boolean equals(Object that) {
 		if (null == that) return false;
 		return hashCode() == that.hashCode();
 	}
 }
