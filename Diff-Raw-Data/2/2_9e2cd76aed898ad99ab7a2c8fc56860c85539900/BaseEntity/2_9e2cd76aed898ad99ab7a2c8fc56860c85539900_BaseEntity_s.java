 package org.springframework.samples.petclinic;
 
 import com.gigaspaces.annotation.pojo.SpaceExclude;
 import com.gigaspaces.annotation.pojo.SpaceId;
 import com.gigaspaces.annotation.pojo.SpaceRouting;
 
 import javax.persistence.Id;
 import javax.persistence.MappedSuperclass;
 import javax.persistence.Transient;
 import java.io.Serializable;
 
 /**
  * Simple JavaBean domain object with an id property.
  * Used as a base class for objects needing this property.
  *
  *
  */
 @MappedSuperclass
 public class BaseEntity implements Serializable{
 
 	private Integer id;
 	
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
     @Id
     @SpaceId(autoGenerate = false)
     @SpaceRouting
 	public Integer getId() {
 		return id;
 	}
 
     @SpaceExclude
     @Transient
 	public boolean isNewEntity() {
 		return (getId() == null);
 	}
 
 
     public boolean equals(Object o) {
         if (this == o) return true;
        if (!(o.getClass().equals(this.getClass()))) return false;
         BaseEntity that = (BaseEntity) o;
         if (id != null ? !id.equals(that.id) : that.id != null) return false;
         return true;
     }
 
     public int hashCode() {
         return (id != null ? id.hashCode() : 0);
     }
 }
