 package inc.meh.DBHelper;
 
 import android.content.Context; 
 import android.database.Cursor; 
 import android.database.sqlite.SQLiteDatabase; 
 import android.database.sqlite.SQLiteOpenHelper; 
 import android.database.sqlite.SQLiteStatement; 
 import android.util.Log;  
 import java.util.ArrayList; 
 import java.util.Date;
 import java.util.List;   
 
 
 public class DBHelper {      
 	private static final String DATABASE_NAME = "trips.db";    
 	private static final int DATABASE_VERSION = 11;    
 	private static final String TABLE_NAME = "coordinates";      
 	private Context context;    
 	private SQLiteDatabase db;      
 	private SQLiteStatement insertStmt;    
 	private static final String INSERT = "insert into " + TABLE_NAME + " (tripid,insertype,lat,lon,dist2Prev,cumDist,created_date) values (?,?,?,?,?,?,?)";      
 	public DBHelper(Context context) {       
 		this.context = context;       
 		OpenHelper openHelper = new OpenHelper(this.context);       
 		this.db = openHelper.getWritableDatabase();       
 		this.insertStmt = this.db.compileStatement(INSERT);   
 		}     
 	
 	public long insert(int tripid, String insertype,Double lat, Double lon, Double dist2PrevCoord, Double cumDist) {  
 		this.insertStmt.bindDouble(1, tripid);
 		this.insertStmt.bindString(2, insertype);
 		this.insertStmt.bindDouble(3,lat);
 		this.insertStmt.bindDouble(4,lon);
 		this.insertStmt.bindDouble(5,dist2PrevCoord);
 		this.insertStmt.bindDouble(6,cumDist);
 
 		//insert date as GMT time (to avoid conflicts when changing time-zones
 		Date today = new Date();
 		this.insertStmt.bindString(7, today.toGMTString());
 
 		return this.insertStmt.executeInsert();    
 		}      
 	
 	public void deleteAll() {       
 			this.db.delete(TABLE_NAME, null, null);    
 			}      
 	
 /*	public String SelectRow(String str) {
 		String strRow = "";
 		
 		Cursor cursor = this.db.query(TABLE_NAME, new String[] {"insertype"}, "insertype like '" + str + "%'",null,null,null,"id desc");
 		
 		if (cursor.moveToFirst()) {
 			strRow = cursor.getString(0);
 		}
 		
 		cursor.close();
         return strRow; 
     }
 */	
 	public List<String> getTripInfo(String groupstring){
 		List<String> lTripInfo = new ArrayList<String>();
		Cursor cursor = this.db.query(TABLE_NAME, new String[] { groupstring, "max(created_date)", "max(cumDist)"},null, null, groupstring, null, groupstring +" asc");
 		
 		if (cursor.moveToFirst()) {
			do {
 				lTripInfo.add(Double.toString(cursor.getDouble(0)));
 				lTripInfo.add(cursor.getString(1));
 				lTripInfo.add(Double.toString(cursor.getDouble(2)));
 				}
			while (cursor.moveToNext());
		}
		
 		if (cursor != null && !cursor.isClosed()) {
 			cursor.close();    
 			}   
		
 		return lTripInfo;  
 	}
 	
 	public int getTripId()	{	
 		List<String> list = selectOneRow("");
 		
 		if (list.isEmpty()) {
 			// this will be the first trip
 			return 1;
 		}
 		else {
 			// this will be a subsequent trip of id 1 greater then the previous id
 			return (int) Double.parseDouble(list.get(5)) + 1;
 		}
 		
 		
 	}
 	
 	public List<String> selectOneRow(String whereClause) {      
 		List<String> list = new ArrayList<String>();
 		Cursor cursor = this.db.query(TABLE_NAME, new String[] { "insertype","lat","lon","dist2Prev","cumDist","tripid"}, "insertype like '" + whereClause + "%'",null, null, null, "coordinateid desc","1");
 		if (cursor.moveToFirst()) {
 				list.add(cursor.getString(0));
 				list.add(Double.toString(cursor.getDouble(1)));
 				list.add(Double.toString(cursor.getDouble(2)));
 				list.add(Double.toString(cursor.getDouble(3)));
 				list.add(Double.toString(cursor.getDouble(4)));
 				list.add(Double.toString(cursor.getDouble(5)));
 			}
 		if (cursor != null && !cursor.isClosed()) {
 			cursor.close();    
 			}   
 		return list;  
 	}   
 	
 	public List<String> selectAll(String sortstring) {      
 		List<String> list = new ArrayList<String>();
 		Cursor cursor = this.db.query(TABLE_NAME, new String[] { "insertype", "lat", "lon","dist2Prev","cumDist","created_date","tripid",sortstring},null, null, null, null, sortstring);
 		if (cursor.moveToFirst()) {
 			do {
 				list.add(cursor.getString(0));
 				list.add(Double.toString(cursor.getDouble(1)));
 				list.add(Double.toString(cursor.getDouble(2)));
 				list.add(Double.toString(cursor.getDouble(3)));
 				list.add(Double.toString(cursor.getDouble(4)));
 				list.add(cursor.getString(5));
 				list.add(Double.toString(cursor.getDouble(6)));
 				}
 			while (cursor.moveToNext());
 			}
 		if (cursor != null && !cursor.isClosed()) {
 			cursor.close();    
 			}   
 		return list;  
 		}     
 	
 	public List<String> exportAll(String sortstring) {      
 		List<String> list = new ArrayList<String>();
 		Cursor cursor = this.db.query(TABLE_NAME, new String[] { "created_date","cumDist",sortstring},null, null, null, null, sortstring);
 		if (cursor.moveToFirst()) {
 			do {
 				list.add(cursor.getString(0));
 				list.add(Double.toString(cursor.getDouble(1)));
 				}
 			while (cursor.moveToNext());
 			}
 		if (cursor != null && !cursor.isClosed()) {
 			cursor.close();    
 			}   
 		return list;  
 		}     
 	
 	
 		private static class OpenHelper extends SQLiteOpenHelper {  
 			OpenHelper(Context context) {         
 				super(context, DATABASE_NAME, null, DATABASE_VERSION); 
 				}         
 			
 			@Override      
 			public void onCreate(SQLiteDatabase db) {         
 				db.execSQL("CREATE TABLE coordinates (coordinateid INTEGER PRIMARY KEY AUTOINCREMENT, tripid integer, insertype TEXT, lat real, lon real, dist2Prev real, cumDist real, created_date date default CURRENT_DATE)");      
 				//db.execSQL("CREATE TABLE " + "trip (tripid INTEGER PRIMARY KEY AUTOINCREMENT, catid integer, name text, description text, distance real, duration text, vehicleid integer, starttime text, stoptime text)");
 				//db.execSQL("CREATE TABLE " + "category (catid INTEGER PRIMARY KEY AUTOINCREMENT, name text, description text, isdeductible boolean)");
 				//db.execSQL("CREATE TABLE " + "route (routeid INTEGER PRIMARY KEY AUTOINCREMENT, tripid integer)");
 				}         
 			
 			@Override     
 			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 				Log.w("Example", "Upgrading database, this will drop tables and recreate.");   
 				db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);          onCreate(db);       
 				}   
 			}
 		}
