 package nz.ac.victoria.ecs.kpsmart.state.entities.state;
 
 import java.io.Serializable;
 
 import javax.persistence.Embeddable;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 
 @Entity
 @Table(uniqueConstraints=@UniqueConstraint(columnNames={"name"}))
 public final class Location extends StorageEntity implements Serializable {
 	@GeneratedValue(strategy=GenerationType.AUTO)
 	private long id;
 	
 	private String name;
 	
 	@Id
 	private LocationPK primaryKey = new LocationPK();
 	
 	@Enumerated(EnumType.STRING)
	private Bool international;
 	
 	@Embeddable
 	public static final class LocationPK implements Serializable {
 		private double latitude;
 		
 		private double longitude;
 	}
 
 	public long getId() {
 		return id;
 	}
 
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public double getLatitude() {
 		return getPrimaryKey().latitude;
 	}
 
 	public void setLatitude(double latitude) {
 		this.getPrimaryKey().latitude = latitude;
 	}
 
 	public double getLongitude() {
 		return getPrimaryKey().longitude;
 	}
 
 	public void setLongitude(double longitude) {
 		this.getPrimaryKey().longitude = longitude;
 	}
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	@Override
 	public String toString() {
 		return "Location [id=" + id + ", name=" + name + ", latitude="
 				+ getPrimaryKey().latitude + ", longitude=" + getPrimaryKey().longitude + ", international="
 				+ international.name() + "]";
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + (int) (id ^ (id >>> 32));
 		result = prime * result
 				+ ((international == null) ? 0 : international.hashCode());
 		long temp;
 		temp = Double.doubleToLongBits(getPrimaryKey().latitude);
 		result = prime * result + (int) (temp ^ (temp >>> 32));
 		temp = Double.doubleToLongBits(getPrimaryKey().longitude);
 		result = prime * result + (int) (temp ^ (temp >>> 32));
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Location other = (Location) obj;
 		if (id != other.id)
 			return false;
 		if (international != other.international)
 			return false;
 		if (Double.doubleToLongBits(getPrimaryKey().latitude) != Double
 				.doubleToLongBits(other.getPrimaryKey().latitude))
 			return false;
 		if (Double.doubleToLongBits(getPrimaryKey().longitude) != Double
 				.doubleToLongBits(other.getPrimaryKey().longitude))
 			return false;
 		if (name == null) {
 			if (other.name != null)
 				return false;
 		} else if (!name.equals(other.name))
 			return false;
 		return true;
 	}
 
 	public boolean isInternational() {
 		return international == Bool.True;
 	}
 
 	public void setInternational(boolean international) {
 		this.international = international ? Bool.True : Bool.False;
 	}
 
 	public LocationPK getPrimaryKey() {
 		return primaryKey;
 	}
 
 	public void setPrimaryKey(LocationPK primaryKey) {
 		this.primaryKey = primaryKey;
 	}
 }
