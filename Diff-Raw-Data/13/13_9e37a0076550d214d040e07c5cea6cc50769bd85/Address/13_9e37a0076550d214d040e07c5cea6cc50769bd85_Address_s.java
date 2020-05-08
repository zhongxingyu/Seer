 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package at.ac.tuwien.infosys.aic.model;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  *
  * @author smolle
  */
 @XmlRootElement
 @XmlAccessorType(XmlAccessType.FIELD) //all Fields ill be serialized
 public class Address {
 
     @XmlAttribute
     private String id;
     private String street;
     private String city;
     private int house = 0;
     private int door = 0;
     private String zipCode;
     private boolean isShipping = false;
     private boolean isBilling = false;
     private boolean isOther = false;
 
     public Address() {
     }
 
     public String getCity() {
         return city;
     }
 
     public void setCity(String city) {
         this.city = city;
     }
 
     public int getDoor() {
         return door;
     }
 
     public void setDoor(int door) {
         this.door = door;
     }
 
     public int getHouse() {
         return house;
     }
 
     public void setHouse(int house) {
         this.house = house;
     }
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public boolean isIsShipping() {
        return isBilling;
     }
 
     public void setIsShipping(boolean isShipping) {
         this.isShipping = isShipping;
     }
 
     public boolean isIsBilling() {
         return isBilling;
     }
 
     public void setIsBilling(boolean isBilling) {
         this.isBilling = isBilling;
     }
 
     public boolean isIsOther() {
         return isOther;
     }
 
     public void setIsOther(boolean isOther) {
         this.isOther = isOther;
     }
 
     public String getStreet() {
         return street;
     }
 
     public void setStreet(String street) {
         this.street = street;
     }
 
     public String getZipCode() {
         return zipCode;
     }
 
     public void setZipCode(String zipCode) {
         this.zipCode = zipCode;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Address other = (Address) obj;
         if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
             return false;
         }
         if ((this.street == null) ? (other.street != null) : !this.street.equals(other.street)) {
             return false;
         }
         if ((this.city == null) ? (other.city != null) : !this.city.equals(other.city)) {
             return false;
         }
         if (this.house != other.house) {
             return false;
         }
         if (this.door != other.door) {
             return false;
         }
         if ((this.zipCode == null) ? (other.zipCode != null) : !this.zipCode.equals(other.zipCode)) {
             return false;
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 29 * hash + (this.id != null ? this.id.hashCode() : 0);
         hash = 29 * hash + (this.street != null ? this.street.hashCode() : 0);
         hash = 29 * hash + (this.city != null ? this.city.hashCode() : 0);
         hash = 29 * hash + this.house;
         hash = 29 * hash + this.door;
         hash = 29 * hash + (this.zipCode != null ? this.zipCode.hashCode() : 0);
         return hash;
     }
 
     @Override
     public String toString() {
         StringBuffer stringbuffer = new StringBuffer();
         stringbuffer.append(street);
         stringbuffer.append(" ");
         stringbuffer.append(house);
         if (door != 0) {
             stringbuffer.append("/");
             stringbuffer.append(door);
 
         }
         stringbuffer.append(", ");
         stringbuffer.append(zipCode);
         stringbuffer.append(" ");
         stringbuffer.append(city);
         return stringbuffer.toString();
     }
 }
