 package net.novacodex.hibernate.search.spatial;
 
 import org.hibernate.search.annotations.Field;
 import org.hibernate.search.annotations.FieldBridge;
 import org.hibernate.search.annotations.Index;
 import org.hibernate.search.annotations.Indexed;
 import org.hibernate.search.annotations.NumericField;
 import org.hibernate.search.annotations.Store;
 
 import javax.persistence.Embedded;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 
import org.h2.command.ddl.Analyze;

 @Entity
 @Indexed
 public class POI {
 	@Id
 	Integer id;
 
 	@Field(store = Store.YES)
 	String name;
 
	@Field(store = Store.YES)
	String type;

 	@Field(store = Store.YES, index = Index.UN_TOKENIZED)
 	@NumericField
 	double latitude;
 	@Field(store = Store.YES, index = Index.UN_TOKENIZED)
 	@NumericField
 	double longitude;
 
	@Field(store = Store.YES, index = Index.UN_TOKENIZED)
 	@FieldBridge(impl = SpatialFieldBridge.class)
 	@Embedded
 	public SpatialIndexable getLocation() {
 		return new SpatialIndexable() {
 			@Override
 			public double getLatitude() {
 				return latitude;
 			}
 
 			@Override
 			public double getLongitude() {
 				return longitude;
 			}
 		};
 	}
 
	public POI(Integer id, String name, double latitude, double longitude, String type) {
 		this.id = id;
 		this.name = name;
 		this.latitude = latitude;
 		this.longitude = longitude;
		this.type= type;
 	}
 
 	public POI() {
 	}
 
 	public Integer getId() {
 		return id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public double getLatitude() {
 		return latitude;
 	}
 
 	public double getLongitude() {
 		return longitude;
 	}

	public String getType() {
		return type;
	}

 }
