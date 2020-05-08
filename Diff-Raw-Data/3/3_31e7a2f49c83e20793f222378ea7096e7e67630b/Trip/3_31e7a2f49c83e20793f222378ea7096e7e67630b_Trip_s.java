 package models;
 
 
 import org.joda.time.DateTime;
 import play.data.format.Formats;
 import play.data.validation.Constraints;
 import play.db.ebean.*;
 import javax.persistence.*;
 import java.util.*;
 import play.data.validation.Constraints.*;
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: Stijn
  * Date: 30-3-13
  * Time: 16:35
  * To change this template use File | Settings | File Templates.
  */
 @Entity
 public class Trip extends Model{
 
 
     @GeneratedValue
     @Id
     public Integer tid;
 
     public static Finder<Integer, Trip> find = new Finder(Integer.class, Trip.class);
 
     public User tripper;
     public int tripperId;
 
 
     public ArrayList<Integer> withBuddy;
 
     public String comments;
 
     @Required
     public Drug drug;
     public int drugId;
 
     @Formats.DateTime(pattern="dd/MM/yyyy-hh:mm:ss")
     public DateTime dfrom;
 
     @Formats.DateTime(pattern="dd/MM/yyyy-hh:mm:ss")
     public DateTime dtill;
 
     @Constraints.Min(1)
     public int number;
 
     public Measure measure;
     public int measureId;
 
     public Trip(User tripper, Drug drug, DateTime from, DateTime till, int number, Measure measure, String comments) {
         this.tripper = tripper;
         this.tripperId = tripper.uid;
         this.drug = drug;
         this.drugId = drug.did;
         this.dfrom = from;
         this.dtill = till;
         this.number = number;
         this.measure = measure;
         this.measureId = measure.mid;
         this.comments = comments;
         withBuddy = new ArrayList<>();
     }
 
     public Trip(User tripper, Drug drug) {
         this.tripper = tripper;
         this.tripperId = tripper.uid;
         this.drug = drug;
         this.dfrom = new DateTime();
         this.dtill = new DateTime();
         this.number = 1;
         this.measure = Measure.findName("unit");
         this.comments = "";
         this.withBuddy = new ArrayList<>();
     }
 
     public void addBuddy(User user) {
         if(!withBuddy.contains(user.uid)){
             withBuddy.add(user.uid);
         }
         this.save();
     }
 
     public static void create(Trip trip) {
         trip.save();
     }
 
     public static void delete(int id) {
         find.ref(id).delete();
     }
 
     public static List<Trip> all(){
         return find.all();
     }
 
     public static Trip findById(int id){
         return find.where().eq("tid", id).findUnique();
     }
 
     public static List<Drug> findDrugsUsed(User u){
         List<Trip> trips = find.where().eq("tripperId", u.uid).findList();
         List<Drug> result = new ArrayList<>();
         for(Trip trip : trips) {
             Drug d = Drug.findById(trip.drugId);
             if(!result.contains(d)){
                 result.add(d);
             }
         }
         return result;
     }
 }
