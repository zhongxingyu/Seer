 package com.gardin.piazza.domain.users;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 /**
  * Represents a User's address. Has 3 non-null attributes:
  * street, ZIP code and city.
  * 
  * @author Pierre Gardin
  */
 @Entity
 @Table(name = "address")
 public class Address {
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private int id;
 
     @Column(name = "building", nullable = true)
     private String building;
 
     @Column(name = "city", nullable = false)
     private String city;
 
     @Column(name = "floor", nullable = true)
     private int floor;
 
     /**
      * Can contain letters, e.g. "33bis" in France.
      */
     @Column(name = "street_number", nullable = true)
     private String streetNumber;
 
     @Column(name = "street", nullable = false)
     private String street;
 
     /**
      * In some countries, the ZIP code contains letters.
      */
     @Column(name = "zip_code", nullable = false)
     private String zipCode;
 
     public String getBuilding() {
         return building;
     }
 
     public String getCity() {
         return city;
     }
 
     public int getFloor() {
         return floor;
     }
 
     public int getId() {
         return id;
     }
 
     public final String getStreet() {
         return street;
     }
 
     public String getStreetNumber() {
         return streetNumber;
     }
 
     public final String getZipCode() {
         return zipCode;
     }
 
     public void setBuilding(String building) {
         this.building = building;
     }
 
     public void setCity(String city) {
         this.city = city;
     }
 
     public void setFloor(int floor) {
         this.floor = floor;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
    public void setNumber(String number) {
        this.streetNumber = number;
    }

     public final void setStreet(String street) {
         this.street = street;
     }
 
     public final void setZipCode(String zipCode) {
         this.zipCode = zipCode;
     }
 }
