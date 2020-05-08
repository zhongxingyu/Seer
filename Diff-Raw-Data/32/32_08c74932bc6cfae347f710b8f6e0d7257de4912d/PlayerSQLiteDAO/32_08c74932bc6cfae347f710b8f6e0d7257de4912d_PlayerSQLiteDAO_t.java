 package de.cbrn35.apmemory.lobby;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import de.cbrn35.apmemory.Player;
 import de.cbrn35.apmemory.Stats;
 
 public class PlayerSQLiteDAO implements PlayerDAO {
 	
 	private MySQLiteOpenHelper dbOpenHelper;
 	private Context ctx;
 	
 	public PlayerSQLiteDAO(Context ctx) {
 		this.ctx = ctx;
 		dbOpenHelper = new MySQLiteOpenHelper(ctx);
 	}
 	
 	
 	public Player getPlayer() {
 		Player result = null;
 		SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
 		String[] columns = {
 			PlayerDB.ID,
 			PlayerDB.USERNAME,
 			PlayerDB.EMAIL,
 			PlayerDB.LASTACTIVITY,
 			PlayerDB.SIGNUPTIMESTAMP,
 			PlayerDB.CURRENTSCORE,
 			PlayerDB.CURRENTGAMEID,
 			PlayerDB.WON,
 			PlayerDB.LOST,
 			PlayerDB.PICKSUCCESS,
 			PlayerDB.PICKFAIL
 		};
 		Cursor c = db.query(PlayerDB.TABLE_NAME, columns, null, null, null, null, null);
 		
 		while(c.moveToNext()) {
 			result = new Player(
 					c.getInt(0),
 					c.getString(1),
 					c.getString(2),
 					c.getString(3),
 					c.getString(4),
 					c.getInt(5),
 					c.getInt(6),
 					new Stats(
 							c.getInt(7),
 							c.getInt(8),
 							c.getInt(9),
 							c.getInt(10)
 					)
 			);
 		}
		c.close();
 		db.close();
 		return result;
 	}
 
 	
 	public long persist(Player player) {
 		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
 		db.delete(PlayerDB.TABLE_NAME, null, null);
 		ContentValues cv = PlayerDB.createContentValues(player);
 		long id = db.insert(PlayerDB.TABLE_NAME, null, cv);
 		db.close();
 		return id;
 	}
 
 	
 	public void update(Player player) {
 		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
 		ContentValues cv = PlayerDB.createContentValues(player);
 		String[] whereArgs = { ""+player.id };
 		db.update(PlayerDB.TABLE_NAME, cv, PlayerDB.ID + "=?", whereArgs);
 		db.close();
 	}
 
 	
 	public void delete(Player player) {
 		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
 		String[] whereArgs = { ""+player.id };
 		db.delete(PlayerDB.TABLE_NAME, PlayerDB.ID + "=?", whereArgs);
 		db.close();
 	}
 
 }
