 package com.xmedic.troll.service.db;
 
 import java.util.List;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 
 import com.xmedic.troll.service.TrollService;
 import com.xmedic.troll.service.db.DataTransfomer.DoCity;
 import com.xmedic.troll.service.db.DataTransfomer.DoLevel;
 import com.xmedic.troll.service.model.City;
 import com.xmedic.troll.service.model.Level;
 
 public class TrollServiceSqlLite implements TrollService {
 
 	private SQLiteDatabase db;
 	 
 	@SuppressWarnings("unused")
 	private Context context;
 	
 
 	 public TrollServiceSqlLite(Context context) {
 		 SqlLiteOpenHelper helper = new SqlLiteOpenHelper(context);
 		 this.db = helper.getReadableDatabase();
 		 this.context = context;
 	 }
 
 	public List<Level> getLevels() {
         Cursor cursor = db.rawQuery(
         		"SELECT id, name, description, startCityId, goalCityId FROM level", 
         		new String[] {});
         List<Level> levels = DataTransfomer.toList(cursor, DoLevel.instance);        
         return levels;
 	}
 
 	public Level getLevel(String levelId) {
         Cursor cursor = db.rawQuery(
         		"SELECT id, name, description, startCityId, goalCityId " +
         		"FROM level WHERE id = ?", 
         		new String[] {levelId});
         return DataTransfomer.to(cursor, DoLevel.instance);
 	}
 
 	public City getCity(String cityId) {
         Cursor cursor = db.rawQuery(
         		"SELECT id, name, country, latitude, longitude, population, x, y " +
         		"FROM city WHERE id = ?", 
         		new String[] {cityId});
         return DataTransfomer.to(cursor, DoCity.instance);
 	}
 
 	public List<City> getNearbyCities(String cityId) {
         Cursor cursor = db.rawQuery(
         		"SELECT c.id, c.name, c.country, c.latitude, c.longitude, c.population, c.x, c.y " +
        				"FROM city c INNER JOIN road r ON c.id = r.toCityId " +
         				"WHERE r.fromCityId = ?", 
         		new String[] {cityId});
         return DataTransfomer.toList(cursor, DoCity.instance);
 	}
 }
