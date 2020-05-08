 package eu.monniot.memoArcher;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.database.Cursor;
 
 import com.activeandroid.Model;
 import com.activeandroid.annotation.Column;
 import com.activeandroid.annotation.Table;
 
 
 @Table(name = "bows")
 public class Bow extends Model {
 	
 	private List<Landmark> mLandmarks;
 
 	@Column(name = "name")
 	public String name;
 	
 	@Column(name = "mark_unit")
 	public String markUnit;
 	
 	@Column(name = "distance_unit")
 	public String distanceUnit;
 	
 	public Bow() {
 		super();
 		mLandmarks = new ArrayList<Landmark>();
 	}
 	
 	public Bow(String name, String markUnit, String distanceUnit) {
 		super();
 		
 		this.name = name;
 		this.markUnit = markUnit;
 		this.distanceUnit = distanceUnit;
 	}
 
 	public List<Landmark> landmarks() {
		refreshLandmarks();
 		return mLandmarks;
 	}
 	
 	public int landmarksCount() {
		if(mLandmarks.size() == 0)
			refreshLandmarks();
		
 		return mLandmarks.size();
 	}
 	
 	public Landmark landmark(int id) {
 		
 		for(Landmark lm : mLandmarks) {
 			if( lm.getId().longValue() == id)
 				return lm;
 		}
 		
 		return null;
 	}
 	
	public void refreshLandmarks() {
		mLandmarks = getMany(Landmark.class, "bow");
	}
	
 	public static List<Bow> all() {
 		return all(Bow.class);
 	}
 	
 	public static Bow loadFromCursor(Cursor cursor){
 		Bow b = new Bow();
 		b.loadFromCursor(Bow.class, cursor);
 		return b;
 	}
 	
 	@Override
 	public String toString() {
 		return name;
 	}
 }
