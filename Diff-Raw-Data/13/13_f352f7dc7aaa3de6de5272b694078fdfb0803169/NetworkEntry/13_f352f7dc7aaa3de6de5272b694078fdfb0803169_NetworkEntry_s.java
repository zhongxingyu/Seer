 package eu.justas.geoip.model;
 
 import java.io.Serializable;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.Transient;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 /**
  *
  * @author justas
  */
 @Entity(name = "network")
 @XmlRootElement
 public class NetworkEntry implements Serializable {
 
     private static final long serialVersionUID = 1L;
     @Id
     @XmlTransient
     private Long startIp;
     
     @XmlTransient
     private Long endIp;
 
     @ManyToOne
    @Column(name = "locationId")
     private Location location;
     
     @Column(name = "locationId")
     private Long locationId;
     
     @Transient
     private String startIpString;
 
     @Transient
     private String endIpString;
 
     public String getEndIpString() {
         return endIpString;
     }
 
     public void setEndIpString(String endIpString) {
         this.endIpString = endIpString;
     }
 
     public String getStartIpString() {
         return startIpString;
     }
 
     public void setStartIpString(String startIpString) {
         this.startIpString = startIpString;
     }
 
     public Long getLocationId() {
         return locationId;
     }
 
     public void setLocationId(Long locationId) {
         this.locationId = locationId;
     }
 
     public NetworkEntry(Long startIp, Long endIp, Location location) {
         this.startIp = startIp;
         this.location = location;
         this.endIp = endIp;
     }
 
     public Location getLocation() {
         return location;
     }
 
     public void setLocation(Location location) {
         this.location = location;
     }
 
     public NetworkEntry() {
     }
 
     public Long getStartIp() {
         return startIp;
     }
 
     public void setStartIp(Long startIp) {
         this.startIp = startIp;
     }
 
     public Long getEndIp() {
         return endIp;
     }
 
     public void setEndIp(Long endIp) {
         this.endIp = endIp;
     }
 
     @Override
     public int hashCode() {
         int hash = 5;
         hash = 61 * hash + (this.startIp != null ? this.startIp.hashCode() : 0);
         hash = 61 * hash + (this.locationId != null ? this.locationId.hashCode() : 0);
         hash = 61 * hash + (this.endIp != null ? this.endIp.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final NetworkEntry other = (NetworkEntry) obj;
         if (this.startIp != other.startIp && (this.startIp == null || !this.startIp.equals(other.startIp))) {
             return false;
         }
         if (this.locationId != other.locationId && (this.locationId == null || !this.locationId.equals(other.locationId))) {
             return false;
         }
         if (this.endIp != other.endIp && (this.endIp == null || !this.endIp.equals(other.endIp))) {
             return false;
         }
         return true;
     }
 }
