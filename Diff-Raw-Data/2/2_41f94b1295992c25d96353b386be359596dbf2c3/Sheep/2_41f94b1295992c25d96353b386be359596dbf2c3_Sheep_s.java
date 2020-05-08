 package models;
 
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
import play.api.data.validation.Constraint;
 import play.db.ebean.Model;
 
 @Entity
 @Table(name = "Sheep")
 public class Sheep extends Model {
 
 	@Id
 	public long id;
 	public long producerId;
 	public long sheepId;
 	@Required
 	public long rfid;
 	
 	public String name;
 	public long timeOfBirth;
 	public double birthWeight;
 	public String notes;
 	public boolean attacked;
 	public long timeAdded;
 
 	public static Model.Finder<Long, Sheep> find = new Model.Finder<Long, Sheep>(Long.class, Sheep.class);
 	public static Model.Finder<String, Sheep> findByName = new Model.Finder<String, Sheep>(String.class, Sheep.class);
 	
 	public static Sheep create(long id, long rfid, long producerId) {
 		return new Sheep();
 	}
 
 	public static List<Sheep> findAll() {
 		return find.all();
 	}
 
 	public static List<Sheep> findByName(String name) {
 		return findByName.where().eq("name", name).findList();
 	}
 
 }
