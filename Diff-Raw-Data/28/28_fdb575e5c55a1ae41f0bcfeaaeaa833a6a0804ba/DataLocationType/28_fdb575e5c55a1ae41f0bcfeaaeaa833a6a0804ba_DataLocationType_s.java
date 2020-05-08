 package org.chai.kevin.location;
 
 import javax.persistence.AttributeOverride;
 import javax.persistence.AttributeOverrides;
 import javax.persistence.Basic;
 import javax.persistence.Column;
 import javax.persistence.Embedded;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 import org.chai.kevin.Translation;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 
 @Entity(name="DataLocationType")
 @Table(name="dhsst_location_data_location_type")
 @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
 public class DataLocationType {
 
 	private Long id;
 	private String code;
 	private Translation names = new Translation();
 	
 	@Id
 	@GeneratedValue
 	public Long getId() {
 		return id;
 	}
 	
 	public void setId(Long id) {
 		this.id = id;
 	}
 	
 	@Basic
 	public String getCode() {
 		return code;
 	}
 
 	public void setCode(String code) {
 		this.code = code;
 	}
 	
 	@Embedded
 	@AttributeOverrides({
 		@AttributeOverride(name="jsonValue", column=@Column(name="names", nullable=false))
 	})
 	public Translation getNames() {
 		return names;
 	}
 	
 	public void setNames(Translation names) {
 		this.names = names;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((code == null) ? 0 : code.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (!(obj instanceof DataLocationType))
 			return false;
 		DataLocationType other = (DataLocationType) obj;
 		if (code == null) {
 			if (other.code != null)
 				return false;
 		} else if (!code.equals(other.code))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
		return "DataLocationType [getId()=" + getId() + ", getCode()=" + getCode() + "]";
 	}
 	
 }
