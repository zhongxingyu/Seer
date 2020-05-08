 package model;
 
 import java.io.Serializable;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Table;
 import org.hibernate.annotations.GenericGenerator;
 
 @Entity
@Table( name = "LOCATION" )
 public class Location implements Serializable{
 	
 
 	private static final long serialVersionUID = -6691679149775091891L;
 	
 	private long locationId; //auto incremented key
 	private String name;
 	private String address;
 	private int maxCapacity;
 	private String contactName;
 	private String phone1;
 	private String phone2;
 	
 		
 	Location() {} //not public on purpose
 
 	public Location(String name, String address, int maxCapacity, 
 			String contactName, String phone1, String phone2) {
 		this.name = name;
 		this.address = address;
 		this.maxCapacity = maxCapacity;
 		this.contactName = contactName;
 		this.phone1 = phone1;
 		this.phone2 = phone2;
 	}
 
 	@Id
 	@GeneratedValue(generator="increment")
 	@GenericGenerator(name="increment", strategy = "increment")
 	public long getLocationId() {
 		return locationId;
 	}
 	public void setLocationId(long locationId) {
 		this.locationId = locationId;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public String getAddress() {
 		return address;
 	}
 	public void setAddress(String address) {
 		this.address = address;
 	}
 	public int getMaxCapacity() {
 		return maxCapacity;
 	}
 	public void setMaxCapacity(int maxCapacity) {
 		this.maxCapacity = maxCapacity;
 	}
 
 	public String getContactName() {
 		return contactName;
 	}
 
 	public void setContactName(String contactName) {
 		this.contactName = contactName;
 	}
 
 	public String getPhone1() {
 		return phone1;
 	}
 
 	public void setPhone1(String phone1) {
 		this.phone1 = phone1;
 	}
 
 	public String getPhone2() {
 		return phone2;
 	}
 
 	public void setPhone2(String phone2) {
 		this.phone2 = phone2;
 	}
 	
 	
 }
