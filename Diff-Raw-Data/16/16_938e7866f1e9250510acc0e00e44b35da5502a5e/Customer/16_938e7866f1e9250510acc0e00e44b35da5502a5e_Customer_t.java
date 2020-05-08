 package sse.model;
 
 import javax.persistence.*;
 
 import java.io.Serializable;
 import java.util.List;
 
 /**
  * 
  * @author cog
  * @author Andrea Fueresz
  * 
  */
 @NamedQueries({@NamedQuery(name="filterCustomers", query="SELECT c FROM Customer c WHERE c.surname = ?")})
 @Entity
 @Table(name = "customer")
 public class Customer implements Serializable {
 	private static final long serialVersionUID = -5936444317786302460L;
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private Long id;
     private String name;
     private String surname;
 	private String billingAdress;
     private String zip;
     private String city;
 	private String company;
 	private String note;
 	private Double discount;
 	private String phone;
 	private String eMail;
 	private String web;
 	private String fax;
 
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "customer")
 	private List<Reservation> reservations;
 
 	public String getBillingAdress() {
 		return billingAdress;
 	}
 
 	public void setBillingAdress(String billingAdress) {
 		this.billingAdress = billingAdress;
 	}
 
 	public String getCompany() {
 		return company;
 	}
 
 	public void setCompany(String company) {
 		this.company = company;
 	}
 
 	public String getNote() {
 		return note;
 	}
 
 	public void setNote(String note) {
 		this.note = note;
 	}
 
 	public Double getDiscount() {
 		return discount;
 	}
 
 	public void setDiscount(Double discount) {
 		this.discount = discount;
 	}
 
 	public String getPhone() {
 		return phone;
 	}
 
 	public void setPhone(String phone) {
 		this.phone = phone;
 	}
 
 	public String geteMail() {
 		return eMail;
 	}
 
 	public void seteMail(String eMail) {
 		this.eMail = eMail;
 	}
 
 	public String getWeb() {
 		return web;
 	}
 
 	public void setWeb(String web) {
 		this.web = web;
 	}
 
 	public String getFax() {
 		return fax;
 	}
 
 	public void setFax(String fax) {
 		this.fax = fax;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public List<Reservation> getReservations() {
 		return reservations;
 	}
 
 	public void setReservations(List<Reservation> reservations) {
 		this.reservations = reservations;
 	}
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getSurname() {
         return surname;
     }
 
     public void setSurname(String surname) {
         this.surname = surname;
     }
 
     public String getZip() {
         return zip;
     }
 
     public void setZip(String zip) {
         this.zip = zip;
     }
 
     public String getCity() {
         return city;
     }
 
     public void setCity(String city) {
         this.city = city;
     }
 
     @Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
 		Customer other = (Customer) obj;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		return true;
 	}
 	
 	public String toString() {
 		return this.getId()+", "+this.getSurname()+", "+this.getName()+", "+this.getBillingAdress();
 	}
 
 }
