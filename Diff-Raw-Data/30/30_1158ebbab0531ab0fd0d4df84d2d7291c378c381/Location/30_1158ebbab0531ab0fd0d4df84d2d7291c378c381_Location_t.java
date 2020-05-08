 package edu.thangiah.entity;
 
 import java.io.Serializable;
import java.util.Set;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 import org.hibernate.validator.NotEmpty;
 import org.hibernate.validator.Valid;
 
 /**
  * Location model. 
 * @author pbair, Kelly Smith
  */
 @Entity
 @Table(name="locations")
 public class Location implements Serializable {
     public static final String WAREHOUSE_TYPE = "warehouse";
     public static final String DEPOT_TYPE = "depot";
 
     private static final long serialVersionUID = 1L;
     protected Long id = null;
     private String name = null;
     private String streetAddress1 = null;
     private String streetAddress2 = null;
     private String city = null;
     private String state = null;
     private String zip = null;
     private String roadName = null;
     private Integer roadNumber = null;
     private String latitude = null;
     private String longitude = null;
     private String locationType = null;
     private Contractor contractor = null;
    private Set<Vehicle> vehicles = null;
 
     public Location() {
         super();
     }
 
     public Location(Long id) {
         this.id = id;
     }
 
     public Location(String locationType) {
         this.locationType = locationType;
     }
 
     public Location(Long id, String name, String streetAddress1, String streetAddress2, String city, String state, String zip, String roadName,
            Integer roadNumber, String latitude, String longitude, String locationType, Contractor contractor, Set<Vehicle> vehicles) {
         this.id = id;
         this.name = name;
         this.streetAddress1 = streetAddress1;
         this.streetAddress2 = streetAddress2;
         this.city = city;
         this.state = state;
         this.zip = zip;
         this.roadName = roadName;
         this.roadNumber = roadNumber;
         this.latitude = latitude;
         this.longitude = longitude;
         this.locationType = locationType;
         this.contractor = contractor;
        this.vehicles = vehicles;
     }
 
     @Column(name="name")
     @NotEmpty
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     @Column(name="street_address_1")
     @NotEmpty
     public String getStreetAddress1() {
         return streetAddress1;
     }
 
     public void setStreetAddress1(String streetAddress1) {
         this.streetAddress1 = streetAddress1;
     }
 
     @Column(name="street_address_2")
     public String getStreetAddress2() {
         return streetAddress2;
     }
 
     public void setStreetAddress2(String streetAddress2) {
         this.streetAddress2 = streetAddress2;
     }
 
     @Column(name="city")
     @NotEmpty
     public String getCity() {
         return city;
     }
 
     public void setCity(String city) {
         this.city = city;
     }
 
     @Column(name = "state")
     @NotEmpty
     public String getState() {
         return state;
     }
 
     public void setState(String state) {
         this.state = state;
     }
 
     @Column(name="zip")
     @NotEmpty
     public String getZip() {
         return zip;
     }
 
     public void setZip(String zip) {
         this.zip = zip;
     }
 
     @Column(name="road_name")
     public String getRoadName() {
         return roadName;
     }
 
     public void setRoadName(String roadName) {
         this.roadName = roadName;
     }
 
     @Column(name="road_number")
     public Integer getRoadNumber() {
         return roadNumber;
     }
 
     public void setRoadNumber(Integer roadNumber) {
         this.roadNumber = roadNumber;
     }
 
     @Column(name="latitude")
     public String getLatitude() {
         return latitude;
     }
 
     public void setLatitude(String latitude) {
         this.latitude = latitude;
     }
 
     @Column(name="longitude")
     public String getLongitude() {
         return longitude;
     }
 
     public void setLongitude(String longitude) {
         this.longitude = longitude;
     }
 
     @Column(name="location_type")
     @NotEmpty
     public String getLocationType() {
         return locationType;
     }
 
     public void setLocationType(String locationType) {
         this.locationType= locationType;
     }
 
     @ManyToOne
     @JoinColumn(name="contractor_id", nullable = false)
     @Valid
     public Contractor getContractor() {
         return contractor;
     }
 
     public void setContractor(Contractor contractor) {
         this.contractor = contractor;
     }
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
    @OneToMany(mappedBy="location")
     @Valid
     public Set<Vehicle> getVehicles() {
         return vehicles;
     }
 
     public void setVehicles(Set<Vehicle> vehicles) {
         this.vehicles = vehicles;
    }
 }
