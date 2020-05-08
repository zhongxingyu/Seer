 package com.xmedic.troll.service.db;
 
 import java.util.List;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 
 import com.xmedic.troll.service.TrollService;
 import com.xmedic.troll.service.model.City;
 
 public class TrollServiceSqlLite implements TrollService {
 
 	private SQLiteDatabase db;
 	 
 	 @SuppressWarnings("unused")
 	private Context context;
 	
 
 	 public TrollServiceSqlLite(Context context) {
 		 SqlLiteOpenHelper helper = new SqlLiteOpenHelper(context);
 		 this.db = helper.getReadableDatabase();
 		 this.context = context;
 	 }
 
 
 	public List<City> getCities() {
 		String query = "SELECT c.* FROM city c WHERE c.country = ?";
 		Cursor cursor = db.rawQuery(query, new String[] {"LT"} );
 		return DataTransfomer.toList(cursor, DoCity.instance);
     }
 }
