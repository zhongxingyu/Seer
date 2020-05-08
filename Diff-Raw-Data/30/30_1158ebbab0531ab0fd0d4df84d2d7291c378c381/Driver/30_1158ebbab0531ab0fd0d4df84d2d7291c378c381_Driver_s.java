 package edu.thangiah.entity;
 
 import java.io.Serializable;
 import java.sql.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 
 import org.hibernate.validator.NotNull;
 import org.hibernate.validator.Valid;
 
 /**
  * Driver Model.
  * @author pbair
  */
 @Entity
 @Table(name="drivers")
 public class Driver extends AbstractEntity implements Serializable {
     private static final long serialVersionUID = 1L;
     
     protected Long id = null;
     private Contact contact = null;
     private Set<Vehicle> vehicles = new HashSet<Vehicle>(0);
     private String licenseNumber = null;
     private Date licenseExpiration = null;
     private String licenseClass = null;
     private Contractor contractor = null;
 
     public Driver() {
         super();
     }
 
     public Driver(Long id) {
     	this.id = id;
     }
 
     public Driver(Long id, Contact contact, Set<Vehicle> vehicles, String licenseNumber, Date licenseExpiration, String licenseClass,
             Contractor contractor) {
         this.id = id;
         this.contact = contact;
         this.vehicles = vehicles;
         this.licenseNumber = licenseNumber;
         this.licenseExpiration = licenseExpiration;
         this.licenseClass = licenseClass;
         this.contractor = contractor;
     }
 
     @OneToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
     @JoinColumn(name="contact_id")
     @Valid
     @NotNull
     public Contact getContact() {
         return contact;
     }
 
     public void setContact(Contact contact) {
         this.contact = contact;
     }
 
     @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch=FetchType.EAGER, mappedBy="drivers")
     @Valid
     public Set<Vehicle> getVehicles() {
         return vehicles;
     }
 
     public void setVehicles(Set<Vehicle> vehicles) {
         this.vehicles = vehicles;
     }
 
     @Column(name="license_number")
     public String getLicenseNumber() {
         return licenseNumber;
     }
 
     public void setLicenseNumber(String licenseNumber) {
         this.licenseNumber = licenseNumber;
     }
 
     @Column(name="license_expiration")
     public Date getLicenseExpiration() {
         return licenseExpiration;
     }
 
     public void setLicenseExpiration(Date licenseExpiration) {
         this.licenseExpiration = licenseExpiration;
     }
 
     @Column(name="license_class")
     public String getLicenseClass() {
         return licenseClass;
     }
 
     public void setLicenseClass(String licenseClass) {
         this.licenseClass = licenseClass;
     }
 
     @ManyToOne
     @JoinColumn(name="contractor_id")
     @Valid
     public Contractor getContractor() {
         return contractor;
     }
 
     public void setContractor(Contractor contractor) {
         this.contractor = contractor;
     }
 
 	@Override
 	public String getViewLink() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 }
