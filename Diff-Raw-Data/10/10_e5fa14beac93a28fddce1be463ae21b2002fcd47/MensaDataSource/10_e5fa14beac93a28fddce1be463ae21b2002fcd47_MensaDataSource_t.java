 package com.ese2013.mub.util.database;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 
 import com.ese2013.mub.model.DailyMenuplan;
 import com.ese2013.mub.model.Mensa;
 import com.ese2013.mub.model.Menu;
 import com.ese2013.mub.model.WeeklyMenuplan;
 import com.ese2013.mub.util.DataManager;
 import com.ese2013.mub.util.database.tables.FavoritesTable;
 import com.ese2013.mub.util.database.tables.MensasTable;
 import com.ese2013.mub.util.database.tables.MenusMensasTable;
 import com.ese2013.mub.util.database.tables.MenusTable;
 
 public class MensaDataSource {
 	private SQLiteDatabase database;
 	private SqlDatabaseHelper dbHelper;
 	private static SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMAN);
 
 	public MensaDataSource(Context context) {
 		dbHelper = new SqlDatabaseHelper(context);
 	}
 
 	public void open() throws SQLException {
 		database = dbHelper.getWritableDatabase();
 	}
 
 	public void close() {
 		dbHelper.close();
 	}
 
 	public List<Mensa> loadMensaList() {
 		List<Mensa> mensas = new ArrayList<Mensa>();
 		Cursor c = database.rawQuery("SELECT * FROM " + MensasTable.TABLE_MENSAS, null);
 		final int POS_ID = c.getColumnIndex(MensasTable.COL_ID);
 		final int POS_NAME = c.getColumnIndex(MensasTable.COL_NAME);
 		final int POS_STREET = c.getColumnIndex(MensasTable.COL_STREET);
 		final int POS_ZIP = c.getColumnIndex(MensasTable.COL_ZIP);
 		final int POS_LON = c.getColumnIndex(MensasTable.COL_LON);
 		final int POS_LAT = c.getColumnIndex(MensasTable.COL_LAT);
 		final int POS_TIMESTAMP = c.getColumnIndex(MensasTable.COL_TIMESTAMP);
 		c.moveToFirst();
 		do {
 			Mensa.MensaBuilder builder = new Mensa.MensaBuilder();
 			int mensaId = c.getInt(POS_ID);
 			builder.setId(mensaId);
 			builder.setName(c.getString(POS_NAME));
 			builder.setStreet(c.getString(POS_STREET));
 			builder.setZip(c.getString(POS_ZIP));
 			builder.setLongitude(c.getDouble(POS_LON));
 			builder.setLatitude(c.getDouble(POS_LAT));
 			builder.setIsFavorite(DataManager.getSingleton().isInFavorites(mensaId));
 			builder.setTimestamp(c.getInt(POS_TIMESTAMP));
 			mensas.add(builder.build());
 		} while (c.moveToNext());
 		c.close();
 		return mensas;
 	}
 
 	public WeeklyMenuplan loadMenuplan(int mensaId) {
 		String query = "select * from " + MenusTable.TABLE_MENUS + " inner join "
 				+ MenusMensasTable.TABLE_MENUS_MENSAS + " on " + MenusTable.TABLE_MENUS + "."
 				+ MenusTable.COL_HASH + " = " + MenusMensasTable.TABLE_MENUS_MENSAS + "."
 				+ MenusTable.COL_HASH + " where " + MenusMensasTable.TABLE_MENUS_MENSAS + "."
 				+ MensasTable.COL_ID + " = " + mensaId + " ;";
 		Cursor c = database.rawQuery(query, null);
 		final int POS_TITLE = c.getColumnIndex(MenusTable.COL_TITLE);
 		final int POS_DESC = c.getColumnIndex(MenusTable.COL_DESC);
 		final int POS_DATE = c.getColumnIndex(MenusTable.COL_DATE);
 		final int POS_HASH = c.getColumnIndex(MenusTable.COL_HASH);
 		WeeklyMenuplan p = new WeeklyMenuplan();
 		c.moveToFirst();
 		do {
 			try {
 				Menu.MenuBuilder builder = new Menu.MenuBuilder();
 				builder.setTitle(c.getString(POS_TITLE));
 				builder.setDescription(c.getString(POS_DESC));
 				builder.setDate(fm.parse(c.getString(POS_DATE)));
 				builder.setHash(c.getInt(POS_HASH));
 				p.addMenu(builder.build());
 			} catch (ParseException e) {
 				// If this happens, the db violated it's contract of properly
 				// storing the given data.
 				throw new AssertionError("Database did not save properly");
 			}
 
 		} while (c.moveToNext());
 		return p;
 	}
 
 	public void storeMensaList(List<Mensa> mensas) {
 		for (Mensa m : mensas)
 			storeMensa(m);
 	}
 
 	private void storeMensa(Mensa m) {
 		ContentValues values = new ContentValues();
 		values.put(MensasTable.COL_ID, m.getId());
 		values.put(MensasTable.COL_NAME, m.getName());
 		values.put(MensasTable.COL_STREET, m.getStreet());
 		values.put(MensasTable.COL_ZIP, m.getZip());
 		values.put(MensasTable.COL_LON, m.getLongitude());
 		values.put(MensasTable.COL_LAT, m.getLatitude());
 		values.put(MensasTable.COL_TIMESTAMP, m.getTimestamp());
 		database.replace(MensasTable.TABLE_MENSAS, null, values);
 	}
 
 	public void storeWeeklyMenuplan(Mensa mensa) {
 		WeeklyMenuplan plan = mensa.getMenuplan();
 		for (DailyMenuplan d : plan)
 			for (Menu m : d.getMenus())
 				storeMenu(m, mensa);
 	}
 
 	private void storeMenu(Menu m, Mensa mensa) {
 		ContentValues values = new ContentValues();
 		values.put(MenusTable.COL_HASH, m.getHash());
 		values.put(MenusTable.COL_TITLE, m.getTitle());
 		values.put(MenusTable.COL_DESC, m.getDescription());
 		values.put(MenusTable.COL_DATE, fm.format(m.getDate()));
 		database.replace(MenusTable.TABLE_MENUS, null, values);
 
 		ContentValues values2 = new ContentValues();
 		values2.put(MenusTable.COL_HASH, m.getHash());
 		values2.put(MensasTable.COL_ID, mensa.getId());
 		database.replace(MenusMensasTable.TABLE_MENUS_MENSAS, null, values2);
 	}
 
 	public boolean isInFavorites(int mensaId) {
 		Cursor c = database.rawQuery("select * from " + FavoritesTable.TABLE_FAV_MENSAS + " where "
 				+ MensasTable.COL_ID + "=" + mensaId, null);
 		return c.getCount() != 0;
 	}
 
 	public void storeFavorites(List<Mensa> mensas) {
 		database.delete(FavoritesTable.TABLE_FAV_MENSAS, null, null);
 		for (Mensa m : mensas) {
 			if (m.isFavorite()) {
 				ContentValues values = new ContentValues();
 				values.put(MensasTable.COL_ID, m.getId());
 				database.insert(FavoritesTable.TABLE_FAV_MENSAS, null, values);
 			}
 		}
 	}
 
 	public int getMensaTimestamp(int mensaId) {
 		Cursor c = database.rawQuery("select " + MensasTable.COL_TIMESTAMP + " from " + MensasTable.TABLE_MENSAS + " where "
 				+ MensasTable.COL_ID + "=" + mensaId, null);
 		c.moveToFirst();
 		return c.getInt(0);
 	}
 
 	public void deleteMenus() {
 		database.delete(MenusTable.TABLE_MENUS, null, null);
		database.delete(MenusMensasTable.TABLE_MENUS_MENSAS, null, null);
 	}
 }
