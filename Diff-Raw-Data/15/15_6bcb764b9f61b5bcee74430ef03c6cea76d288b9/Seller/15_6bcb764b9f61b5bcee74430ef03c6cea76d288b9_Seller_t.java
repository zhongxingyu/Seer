 package net.kerflyn.broceliand.model;
 
import net.kerflyn.broceliand.model.charge.ShippingChargeStrategy;

 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.OneToOne;
 
 @Entity
 public class Seller {
 
     @Id
     @GeneratedValue
     private Long id;
 
     private String name;
 
     private String address;
 
     private String city;
 
     private String country;
 
     @OneToOne
    private ShippingChargeStrategy shippingChargeStrategy;
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
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
 
     public String getCity() {
         return city;
     }
 
     public void setCity(String city) {
         this.city = city;
     }
 
     public String getCountry() {
         return country;
     }
 
     public void setCountry(String country) {
         this.country = country;
     }
 
    public ShippingChargeStrategy getShippingChargeStrategy() {
        return shippingChargeStrategy;
     }
 
    public void setShippingChargeStrategy(ShippingChargeStrategy shippingChargeStrategy) {
        this.shippingChargeStrategy = shippingChargeStrategy;
     }
 }
