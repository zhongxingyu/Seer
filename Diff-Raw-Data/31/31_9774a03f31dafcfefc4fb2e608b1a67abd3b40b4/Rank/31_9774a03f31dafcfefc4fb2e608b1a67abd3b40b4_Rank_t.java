 package models;
 
 import play.db.ebean.Model;
import play.db.ebean.Model.Finder; // Import Finder as sometimes Play! shows compilation error "not found: type Finder"
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Model class that maps to DB table rank.
  * Contains a method to get ranks.
  *
  * Date: 18/10/13
  * Time: 14:20
  *
  * @author      Sav Balac
  * @version     %I%, %G%
  * @since       1.0
  */
 @Entity
 public class Rank extends Model {
 
     // Instance variables
     @Id public Long                 id;
     public String                   name;
 
 
     /**
      * Generic query helper for entity Rank.
      */
     public static Finder<Long, Rank> find = new Finder<Long, Rank>(Long.class, Rank.class);
 
 
     /**
      * Returns a map of all ranks typically used in a select.
      * @return Map<String,String>
      */
     public static Map<String,String> options() {
         LinkedHashMap<String,String> options = new LinkedHashMap<String,String>();
         List<Rank> ranks = Rank.find.where().orderBy("name").findList();
         for(Rank rank : ranks) {
             options.put(rank.id.toString(), rank.name);
         }
         return options;
     }
 
 
 }
