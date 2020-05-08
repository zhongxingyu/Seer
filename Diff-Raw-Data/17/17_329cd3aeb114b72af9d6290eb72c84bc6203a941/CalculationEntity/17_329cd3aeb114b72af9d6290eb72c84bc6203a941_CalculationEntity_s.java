 package org.chai.kevin.location;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.AttributeOverride;
 import javax.persistence.AttributeOverrides;
 import javax.persistence.Basic;
 import javax.persistence.Column;
 import javax.persistence.Embedded;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.chai.kevin.Translation;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 
 @Entity(name="CalculationEntity")
 @Table(name="dhsst_entity_calculation")
 @Inheritance(strategy=InheritanceType.JOINED)
 @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
 public abstract class CalculationEntity {
 
 	private Long id;
 	private Translation names = new Translation();
 	private String code;
 	private String coordinates;
 	
 	@Id
 	@GeneratedValue
 	public Long getId() {
 		return id;
 	}
 	
 	public void setId(Long id) {
 		this.id = id;
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
 	
 	@Basic
 	public String getCode() {
 		return code;
 	}
 	
 	public void setCode(String code) {
 		this.code = code;
 	}
 
 	@Basic
 	public String getCoordinates() {
 		return coordinates;
 	}
 	
 	public void setCoordinates(String coordinates) {
 		this.coordinates = coordinates;
 	}
 	
 	@Transient
 	public abstract LocationEntity getParent();		
 	
 	protected boolean collectLocations(List<LocationEntity> locations, List<DataLocationEntity> dataLocations, Set<DataEntityType> dataEntityTypes, Set<LocationLevel> skips) {
 		boolean result = false;
 		for (LocationEntity child : getChildren(skips)) {
 			result = result | child.collectLocations(locations, dataLocations, dataEntityTypes, skips);
 		}
 	
		if (!result) {
			List<DataLocationEntity> dataEntities = getDataEntities(skips, dataEntityTypes);
			if (!dataEntities.isEmpty()) {
				result = true;
				if (dataLocations != null) dataLocations.addAll(dataEntities);
			}
 		}
 		
 		if (result && locations != null) locations.add((LocationEntity) this);
 		return result;
 	}
 	
 	public List<DataLocationEntity> collectDataLocationEntities(Set<DataEntityType> dataEntityTypes, Set<LocationLevel> skips) {
 		List<DataLocationEntity> dataLocations = new ArrayList<DataLocationEntity>();
 		collectLocations(null, dataLocations, dataEntityTypes, skips);
 		return dataLocations;
 	}
 	
 	@Transient
 	public abstract List<DataLocationEntity> getDataEntities();
 	
 	@Transient
 	public abstract List<DataLocationEntity> getDataEntities(Set<LocationLevel> skipLevels, Set<DataEntityType> types);		
 	
 	@Transient
 	public abstract List<LocationEntity> getChildren();
 	
 	@Transient
 	public abstract List<LocationEntity> getChildren(Set<LocationLevel> skipLevels);
 	
 	@Transient
 	public abstract boolean collectsData();
 	
 	public String toJson() {
 		// TODO 
 		return "";
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
 		if (!(obj instanceof CalculationEntity))
 			return false;
 		CalculationEntity other = (CalculationEntity) obj;
 		if (code == null) {
 			if (other.code != null)
 				return false;
 		} else if (!code.equals(other.code))
 			return false;
 		return true;
 	}
 	
 }
