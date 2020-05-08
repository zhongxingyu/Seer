 package com.davidwilemski.umichdining;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DatabaseModel extends SQLiteOpenHelper {
 	static final String dbName = "umichdining";
 	static final String tbName = "menus";
 	static final String id = "id";
 	static final String location = "location";
 	static final String date = "date";
 	static final String meal = "meal";
 	static final String menu = "menu";
 	
 	public DatabaseModel(Context context) {
 		super(context, dbName, null, 1); // Version number is the last one
 	}
 	
     @Override
     public void onCreate(SQLiteDatabase db) {
         db.execSQL("CREATE TABLE " + tbName + " (" +
         		id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
         		location + " TEXT, " +
         		date + " TEXT, " +
         		meal + " TEXT, " +
         		menu + " TEXT)");
         
     }
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public void insertRecord(String newlocation, String newdate, String newmeal, String newmenu) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		// Check to make sure that the information isn't in the db already
 		// If it is, we update.
 		Cursor c = this.getLocationMeal(newlocation, newdate, newmeal);
 		if(!c.moveToFirst()) {
 			ContentValues cv = new ContentValues();
 			cv.put(location, newlocation);
 			cv.put(date, newdate);
 			cv.put(meal, newmeal);
 			cv.put(menu, newmenu);
 			db.insert(tbName, null, cv);
 		} else {
 			String changeID = c.getString(c.getColumnIndex(id));
 			ContentValues cv = new ContentValues();
 			cv.put(menu, newmenu);
 			db.update(tbName, cv, id + "=?" , new String[]{changeID});
 		}
 		c.close();
 		db.close();
 	}
 	
 	public Cursor getLocationMeal(String currLocation, String currDate, String currMeal) {
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cur = db.query(tbName, new String[]{menu, id, meal}, location + "=? AND " + date + "=? AND " + meal + "=?", new String[]{currLocation, currDate, currMeal}, null, null, null);
 		//db.close();
 		return cur;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void fetchData(Context context) {
 		try {
 			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 		    Date date = new Date();
 			String urlLocation = "http://roadrunner.davidwilemski.com/UMichDining/index.php/menu/getAllMenus/" + dateFormat.format(date);
 			String response = "";
 			String line = "";
 			URL url = new URL(urlLocation);
 			BufferedReader input = new BufferedReader(new InputStreamReader(url.openStream()));
 		    //System.out.println(dateFormat.format(date));
 			while((line = input.readLine()) != null) {
 				response += line;
 			}
 			try{
 				JSONObject jO = new JSONObject(response);
 				//System.out.println(jArray.get(0).toString());
 				Iterator it = jO.keys();
 				while(it.hasNext()) {
 					String item = (String) it.next();
 					JSONObject place = jO.optJSONObject(item);
 					//System.out.println(item);
 					if(place.has("breakfast"))
 						this.insertRecord(item, dateFormat.format(date), "breakfast", place.getString("breakfast").toString());
 					//System.out.println(place.getString("breakfast").toString());
 					if(place.has("lunch"))
 						this.insertRecord(item, dateFormat.format(date), "lunch", place.getString("lunch").toString());
 					if(place.has("dinner"))
 						this.insertRecord(item, dateFormat.format(date), "dinner", place.getString("dinner").toString());
 				}
 				//JSONObject place = new JSONObject(jO.getString("Bursley"));
 			} catch (JSONException e) {
 				System.out.println(e.getMessage());
 			}
 		} catch (Exception e) {
 			// TODO Error message
 			//Toast.makeText(this, "Something went wrong Fetching the Data.", Toast.LENGTH_SHORT).show();
 		}
 	}
 }
