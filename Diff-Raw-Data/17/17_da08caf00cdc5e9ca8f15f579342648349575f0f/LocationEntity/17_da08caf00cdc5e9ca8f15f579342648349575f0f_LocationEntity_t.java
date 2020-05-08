 package org.chai.kevin.location;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 
 @Entity(name="LocationEntity")
 @Table(name="dhsst_entity_location")
 @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
 public class LocationEntity extends CalculationEntity {
 
 	private LocationEntity parent;
 	private List<LocationEntity> children = new ArrayList<LocationEntity>();
 	private List<DataLocationEntity> dataLocationEntities = new ArrayList<DataLocationEntity>();
 	private LocationLevel level;
 
 	@ManyToOne(targetEntity=LocationEntity.class)
 	public LocationEntity getParent() {
 		return parent;
 	}
 	
 	public void setParent(LocationEntity parent) {
 		this.parent = parent;
 	}
 	
 	@OneToMany(targetEntity=LocationEntity.class, mappedBy="parent")
 	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
 	public List<LocationEntity> getChildren() {
 		return children;
 	}
 	
 	public void setChildren(List<LocationEntity> children) {
 		this.children = children;
 	}
 
 	@ManyToOne(targetEntity=LocationLevel.class)
 	public LocationLevel getLevel() {
 		return level;
 	}
 	
 	public void setLevel(LocationLevel level) {
 		this.level = level;
 	}
 	
 	@Override
 	@OneToMany(targetEntity=DataLocationEntity.class, mappedBy="location")
 	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
 	public List<DataLocationEntity> getDataEntities() {
 		return dataLocationEntities;
 	}
 	
 	public void setDataEntities(List<DataLocationEntity> dataLocationEntities) {
 		this.dataLocationEntities = dataLocationEntities;
 	}
 
 	@Override
 	public List<DataLocationEntity> getDataEntities(Set<LocationLevel>  skipLevels, Set<DataEntityType> types) {
 		List<DataLocationEntity> result = new ArrayList<DataLocationEntity>();
 		
 		List<DataLocationEntity> dataEntities = getDataEntities();
 		for (DataLocationEntity dataEntity : dataEntities) {
 			if (types == null || types.contains(dataEntity.getType())) 
 				result.add(dataEntity);
 		}		
 		
 		for (LocationEntity child : children) {
			if (skipLevels != null && skipLevels.contains(child.getLevel())) {
 				result.addAll(child.getDataEntities(skipLevels, types));
 			}
 		}
 		
 		return result;				
 	}
 
 	public List<LocationEntity> collectTreeWithDataEntities(Set<DataEntityType> dataEntityTypes, Set<LocationLevel> skips) {
 		List<LocationEntity> locations = new ArrayList<LocationEntity>();
 		collectLocations(locations, null, dataEntityTypes, skips);
 		return locations;
 	}
 	
 	@Override
 	public List<LocationEntity> getChildren(Set<LocationLevel> skipLevels) {
 		List<LocationEntity> result = new ArrayList<LocationEntity>();
 		for (LocationEntity child : children) {
 			if (skipLevels != null && skipLevels.contains(child.getLevel())) {
 				result.addAll(child.getChildren(skipLevels));
 			}
 			else result.add(child);
 		}
 		return result;
 	}
 	
 	@Transient
 	public List<CalculationEntity> getChildrenEntities() {
 		List<CalculationEntity> result = new ArrayList<CalculationEntity>();
 		result.addAll(getDataEntities());
 		result.addAll(getChildren());
 		return result;
 	}
 	
 	@Override
 	public boolean collectsData() {
 		return false;
 	}
 
 	@Override
 	public String toString() {
 		return "LocationEntity [getCode()=" + getCode() + "]";
 	}
 
 }
