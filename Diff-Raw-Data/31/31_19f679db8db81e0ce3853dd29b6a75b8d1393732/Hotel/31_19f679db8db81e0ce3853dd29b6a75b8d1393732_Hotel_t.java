 package models;
 
 import play.db.ebean.Model;
 
 import javax.persistence.*;
 
 import static play.data.validation.Constraints.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 @Entity
 @Table(name = "HOTEL")
 public class Hotel extends Model {
 
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     public Long id;
 
     @Required
     public String name;
 
     @Required
     public String address;
 
     @OneToMany
     public List<Apartment> apartments = new ArrayList<Apartment>();
 
 
 
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
 
     public List<Apartment> getApartments() {
         return apartments;
     }
 
     public void setApartments(List<Apartment> apartments) {
         this.apartments.clear();
         this.apartments.addAll(apartments);
     }
 
 
 
 
     public static Finder<Long,Hotel> find = new Finder<Long, Hotel>(Long.class, Hotel.class);
 
     public static List<Hotel> all() {
         return find.all();
     }
 
     public static Hotel get(Long id) {
         return find.byId(id);
     }
 
     public static void save(Hotel hotel) {
         hotel.save();
     }
 
     public static void delete(Long id) {
         find.ref(id).delete();
     }
 
 }
