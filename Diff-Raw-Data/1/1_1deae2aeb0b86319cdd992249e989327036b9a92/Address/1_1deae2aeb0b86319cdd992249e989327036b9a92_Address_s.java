 package com.epam.employees.model;
 
 import com.epam.employees.constants.DBConstants;
 import java.util.Set;
 import javax.persistence.*;
 import org.hibernate.annotations.BatchSize;
 
 /**
  *
  * @author Owl
  */
 @Entity
 @BatchSize(size = 100)
 @Table(name = DBConstants.ADDRESS_TABLE)
 public class Address extends PersistentEntity {
 
    ываыва
     private static final long serialVersionUID = 1L;
     private String street;
     private String building;
     private String room;
     private City city;
     private Set<Company> companies;
 
     public void setStreet(String street) {
         this.street = street;
     }
 
     @Column(name = DBConstants.ADDRESS_STREET)
     public String getStreet() {
         return street;
     }
 
     public void setBuilding(String building) {
         this.building = building;
     }
 
     @Column(name = DBConstants.ADDRESS_BUILDING)
     public String getBuilding() {
         return building;
     }
 
     public void setRoom(String room) {
         this.room = room;
     }
 
     @Column(name = DBConstants.ADDRESS_ROOM)
     public String getRoom() {
         return room;
     }
 
     public void setCity(City city) {
         this.city = city;
     }
 
     @ManyToOne(cascade = CascadeType.ALL)
     @JoinColumn(name = DBConstants.CITY_ID)
     public City getCity() {
         return city;
     }
 
     public void setCompanies(Set<Company> companies) {
         this.companies = companies;
     }
 
     @OneToMany
     public Set<Company> getCompanies() {
         return this.companies;
     }
 }
