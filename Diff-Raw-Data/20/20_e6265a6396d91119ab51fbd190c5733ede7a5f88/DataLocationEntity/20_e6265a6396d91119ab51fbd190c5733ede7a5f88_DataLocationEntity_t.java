 package org.chai.kevin.location;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 
 @Entity(name="DataLocationEntity")
 @Table(name="dhsst_entity_data")
 @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
 public class DataLocationEntity extends CalculationEntity {
 
 	private LocationEntity location;
 	private DataEntityType type;
 	
 	@ManyToOne(targetEntity=LocationEntity.class)
 	public LocationEntity getLocation() {
 		return location;
 	}
 	
 	public void setLocation(LocationEntity location) {
 		this.location = location;
 	}
 	
 	@ManyToOne(targetEntity=DataEntityType.class)
 	public DataEntityType getType() {
 		return type;
 	}
 	
 	public void setType(DataEntityType type) {
 		this.type = type;
 	}
 	
 	@Override
 	@Transient
 	public boolean collectsData() {
 		return true;
 	}
 
 	@Override
 	@Transient
 	public List<DataLocationEntity> getDataEntities() {
 		List<DataLocationEntity> result = new ArrayList<DataLocationEntity>();
 		result.add(this);
 		return result;
 	}
 
 	@Override
 	@Transient
 	public List<LocationEntity> getChildren() {
 		return new ArrayList<LocationEntity>();
 	}
 	
 	@Override
 	public List<DataLocationEntity> getDataEntities(Set<LocationLevel> skipLevels, Set<DataEntityType> types) {
		List<DataLocationEntity> result = new ArrayList<DataLocationEntity>();
		if (types == null || types.contains(type)) result.add(this);
		return result;
 	}
 
 	@Override
 	public List<LocationEntity> getChildren(Set<LocationLevel> skipLevels) {
 		return getChildren();
 	}
 
 	@Override
 	public String toString() {
 		return "DataLocationEntity [type=" + type + ", getNames()="
 				+ getNames() + ", getCode()=" + getCode() + "]";
 	}
 
 	@Override
 	@Transient
 	public LocationEntity getParent() {
 		return location;
 	}
 
 }
