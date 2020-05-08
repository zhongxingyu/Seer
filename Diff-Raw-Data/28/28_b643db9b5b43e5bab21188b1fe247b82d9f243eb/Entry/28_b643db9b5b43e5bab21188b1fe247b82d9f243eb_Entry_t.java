 package models;
 
 import java.util.List;
 
 import siena.*;
 
 public class Entry extends Model {
 	
 	@Id
     public Long id;
 	
	public String youtubeId;
	public String title;
     public Float latitude;
     public Float longitude;
     
 	@Override
 	public String toString() {
		return this.title;
 	}
 
 	public static Query<Entry> all() {
 		return Model.all(Entry.class);
 	}
 
 }
