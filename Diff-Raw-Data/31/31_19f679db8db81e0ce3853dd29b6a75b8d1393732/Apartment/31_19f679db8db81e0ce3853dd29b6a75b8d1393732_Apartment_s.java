 package models;
 
 import play.data.validation.Constraints;
 import play.db.ebean.Model;
 
 import javax.persistence.*;
 import java.util.List;
 
 import static play.data.validation.Constraints.Required;
 
 @Entity
 @Table(name = "APARTMENT")
 public class Apartment extends Model {
 
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     public Long id;
 
     @ManyToOne
     @JoinColumn(name="HOTEL_ID", nullable=false)
     public Hotel hotel;
 
    @ManyToOne
    @JoinColumn(name="APARTMENT_TYPE_ID", nullable=false)
     public ApartmentType apartmentType;
 
     @Required
     public String title;
 
     @Required
     public Integer capacity;
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public Hotel getHotel() {
         return hotel;
     }
 
     public void setHotel(Hotel hotel) {
         this.hotel = hotel;
     }
 
     public ApartmentType getApartmentType() {
         return apartmentType;
     }
 
     public void setApartmentType(ApartmentType apartmentType) {
         this.apartmentType = apartmentType;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public Integer getCapacity() {
         return capacity;
     }
 
     public void setCapacity(Integer capacity) {
         this.capacity = capacity;
     }
 
 
 
 
     public static Finder<Long,ApartmentType> find = new Finder<Long, ApartmentType>(Long.class, ApartmentType.class);
 
     public static List<ApartmentType> all() {
         return find.all();
     }
 
     public static ApartmentType get(Long id) {
         return find.byId(id);
     }
 
     public static void save(ApartmentType hotel) {
         hotel.save();
     }
 
     public static void delete(Long id) {
         find.ref(id).delete();
     }
 
 }
