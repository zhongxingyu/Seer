 package models;
 
 import java.util.List;
 
 import siena.*;
 
 public class Entry extends Model {
 	
 	@Id
     public Long id;
 	
	public String url;
     public Float latitude;
     public Float longitude;
     
	public Entry(String url, Float latitude, Float longitude) {
		super();
		this.url = url;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
 	@Override
 	public String toString() {
		return this.url;
	}

	public static List<Entry> queryAll() {
		return Model.all(Entry.class).fetch();
 	}
 
 	public static Query<Entry> all() {
 		return Model.all(Entry.class);
 	}
 
 }
