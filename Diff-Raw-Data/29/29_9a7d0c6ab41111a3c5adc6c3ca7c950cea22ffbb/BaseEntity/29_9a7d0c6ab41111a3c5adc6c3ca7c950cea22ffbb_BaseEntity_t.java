 package cz.cvut.fel.bupro.model;
 
 import java.io.Serializable;
 
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.MappedSuperclass;
 
 @MappedSuperclass
 public abstract class BaseEntity implements Serializable {
 	private static final long serialVersionUID = -4070328341863824859L;
 
 	@Id
 	@GeneratedValue
 	private Long id;
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
		if (this == obj) {
 			return true;
		}
		if (obj == null) {
 			return false;
		}
		if (!(obj instanceof BaseEntity)) {
 			return false;
		}
 		BaseEntity other = (BaseEntity) obj;
		if (getId() == null) {
			if (other.getId() != null) {
 				return false;
			}
		} else if (!getId().equals(other.getId())) {
 			return false;
		}
 		return true;
 	}
 
 	@Override
 	public String toString() {
		return getClass().getName() + " [id=" + getId() + ", hash=" + hashCode() + "]";
 	}
 
 }
