 package edu.berkeley.cs160.smartnature;
 
 import java.util.ArrayList;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.graphics.Rect;
 
 public class DatabaseHelper {
 	private static final String DATABASE_NAME = "gardengnome.db";
 	private static final int DATABASE_VERSION = 2;
 	
 	private static final String CREATE = "CREATE TABLE ";
 	private static final String DROP = "DROP TABLE IF EXISTS ";
 	
 	private static final String GARDEN = "garden";
 	private static final String PLOT = "plot";
 	private static final String PLANT = "plant";
 	private static final String ENTRY = "entry";
 	private static final String PHOTO = "photo";
 	
 	private SQLiteDatabase db;
 	
 	public DatabaseHelper(Context context) {
 		OpenHelper openHelper = new OpenHelper(context);
 		db = openHelper.getWritableDatabase();
 	}
 	
 	public int insertGarden(Garden garden) {
 		int id = (int) db.insert(GARDEN, null, garden.getContentValues());
 		garden.setId(id);
 		return id;
 	}
 	
 	public int insertPlot(Garden garden, Plot plot) {
 		ContentValues values = plot.getContentValues();
 		values.put("garden_id", garden.getId());
 		int id = (int) db.insert(PLOT, null, values);
 		plot.setId(id);
 		return id;
 	}
 	
 	public int insertPlant(Plot plot, Plant plant) {
 		ContentValues values = plant.getContentValues();
 		values.put("plot_id", plot.getId());
 		int id = (int) db.insert(PLANT, null, values);
 		plant.setId(id);
 		return id;
 	}
 	
 	public int insertEntry(Plant plant, Entry entry) {
 		ContentValues values = entry.getContentValues();
 		values.put("plant_id", plant.getId());
 		int id = (int) db.insert(ENTRY, null, values);
 		entry.setId(id);
 		return id;
 	}
 	
 	public int insertPhoto(Garden garden, Photo photo) {
 		ContentValues values = photo.getContentValues();
 		values.put("garden_id", garden.getId());
 		int id = (int) db.insert(PHOTO, null, values);
 		photo.setId(id);
 		return id;
 	}
 	
	/*
 	public Garden selectGarden(int garden_id) {
 		String[] selectionArgs = { Integer.toString(garden_id) };
 		Cursor cursor = db.query(GARDEN, null, "_id=?", selectionArgs, null, null, null);
 		if (!cursor.moveToFirst())
 			System.out.println("tried to select nonexistent garden");
 		
 		Garden garden = new Garden(getString(cursor, "name"));
 		garden.setId(getInt(cursor, "_id"));
 		garden.setServerId(getInt(cursor, "server_id"));
 		garden.setRawBounds(Helper.toRectF(getString(cursor, "bounds")));
 		garden.setCity(getString(cursor, "city"));
 		garden.setState(getString(cursor, "state"));
 		if (getInt(cursor, "is_public") == 1)
 			garden.setPublic(true);
 		
 		if (!cursor.isClosed())
 			cursor.close();
 		return garden;
 	}
	*/
 	
 	public ArrayList<Garden> selectGardens() {
 		Cursor cursor = db.query(GARDEN, null, null, null, null, null, null);
 		ArrayList<Garden> gardens = new ArrayList<Garden>();
 		if (cursor.moveToFirst()) {
 			do {
 				Garden garden = new Garden(getString(cursor, "name"));
 				garden.setId(getInt(cursor, "_id"));
 				garden.setServerId(getInt(cursor, "server_id"));
 				garden.setRawBounds(Helper.toRectF(getString(cursor, "bounds")));
 				garden.setCity(getString(cursor, "city"));
 				garden.setState(getString(cursor, "state"));
 				if (getInt(cursor, "is_public") == 1)
 					garden.setPublic(true);
 				gardens.add(garden);
 			} while (cursor.moveToNext());
 		}
 		
 		if (!cursor.isClosed())
 			cursor.close();
 		return gardens;
 	}
 	
 	public ArrayList<Photo> selectPhotos(Garden garden) {
 		String[] selectionArgs = { Integer.toString(garden.getId()) };
 		Cursor cursor = db.query(PHOTO, null, "garden_id=?", selectionArgs, null, null, null);
 		ArrayList<Photo> photos = new ArrayList<Photo>();
 		if (cursor.moveToFirst()) {
 			do {
 				Photo photo = new Photo(getString(cursor, "uri"));
 				photo.setId(getInt(cursor, "_id"));
 				photo.setServerId(getInt(cursor, "server_id"));
 				photos.add(photo);
 			} while (cursor.moveToNext());
 		}
 		
 		if (!cursor.isClosed())
 			cursor.close();
 		return photos;
 	}
 	
 	public ArrayList<Plot> selectPlots(Garden garden) {
 		String[] selectionArgs = { Integer.toString(garden.getId()) };
 		Cursor cursor = db.query(PLOT, null, "garden_id=?", selectionArgs, null, null, null);
 		ArrayList<Plot> plots = new ArrayList<Plot>();
 		if (cursor.moveToFirst()) {
 			do {
 				Plot plot;
 				String name = getString(cursor, "name");
 				Rect bounds = Rect.unflattenFromString(getString(cursor, "bounds"));
 				int shape = getInt(cursor, "shape");
 				if (shape == Plot.POLY)
 					plot = new Plot(name, bounds, Helper.toFloatArray(getString(cursor, "points")));
 				else
 					plot = new Plot(name, bounds, shape);
 				
 				plot.setId(getInt(cursor, "_id"));
 				plot.setServerId(getInt(cursor, "server_id"));
 				plot.setAngle(getFloat(cursor, "angle"));
 				plot.setColor(getInt(cursor, "color"));
 				plots.add(plot);
 			} while (cursor.moveToNext());
 		}
 		
 		if (!cursor.isClosed())
 			cursor.close();
 		return plots;
 	}
 	
 	public ArrayList<Plant> selectPlants(Plot plot) {
 		String[] selectionArgs = { Integer.toString(plot.getId()) };
 		Cursor cursor = db.query(PLANT, null, "plot_id=?", selectionArgs, null, null, null);
 		ArrayList<Plant> plants = new ArrayList<Plant>();
 		if (cursor.moveToFirst()) {
 			do {
 				Plant plant = new Plant(getString(cursor, "name"));
 				plant.setId(getInt(cursor, "_id"));
 				plant.setServerId(getInt(cursor, "server_id"));
 				plants.add(plant);
 			} while (cursor.moveToNext());
 		}
 		
 		if (!cursor.isClosed())
 			cursor.close();
 		return plants;
 	}
 	
 	public ArrayList<Entry> selectEntries(Plant plant) {
 		String[] selectionArgs = { Integer.toString(plant.getId()) };
 		Cursor cursor = db.query(ENTRY, null, "plant_id=?", selectionArgs, null, null, null);
 		ArrayList<Entry> entries = new ArrayList<Entry>();
 		if (cursor.moveToFirst()) {
 			do {
 				String body = getString(cursor, "name");
 				long date = Long.parseLong(getString(cursor, "date"));
 				Entry entry = new Entry(body, date);
 				entry.setId(getInt(cursor, "_id"));
 				entry.setServerId(getInt(cursor, "server_id"));
 				entries.add(entry);
 			} while (cursor.moveToNext());
 		}
 		
 		if (!cursor.isClosed())
 			cursor.close();
 		return entries;
 	}
 	
 	public int updateGarden(Garden garden) {
 		String[] whereArgs = { Integer.toString(garden.getId()) };
 		return db.update(GARDEN, garden.getContentValues(), "_id=?", whereArgs);
 	}
 	
 	public int updatePhoto(Photo photo) {
 		String[] whereArgs = { Integer.toString(photo.getId()) };
 		return db.update(PHOTO, photo.getContentValues(), "_id=?", whereArgs);
 	}
 	
 	public int updatePlot(Plot plot) {
 		String[] whereArgs = { Integer.toString(plot.getId()) };
 		return db.update(PLOT, plot.getContentValues(), "_id=?", whereArgs);
 	}
 	
 	public int updatePlant(Plant plant) {
 		String[] whereArgs = { Integer.toString(plant.getId()) };
 		return db.update(PLANT, plant.getContentValues(), "_id=?", whereArgs);
 	}
 	
 	public int updateEntry(Entry entry) {
 		String[] whereArgs = { Integer.toString(entry.getId()) };
 		return db.update(ENTRY, entry.getContentValues(), "_id=?", whereArgs);
 	}
 	
 	public int deleteGarden(Garden garden) {
 		String[] whereArgs = { Integer.toString(garden.getId()) };
 		return db.delete(GARDEN, "_id=?", whereArgs);
 	}
 	
 	public int deletePlot(Plot plot) {
 		String[] whereArgs = { Integer.toString(plot.getId()) };
 		return db.delete(PLOT, "_id=?", whereArgs);
 	}
 	
 	public int deletePlant(Plant plant) {
 		String[] whereArgs = { Integer.toString(plant.getId()) };
 		return db.delete(PLANT, "_id=?", whereArgs);
 	}
 	
 	public int deleteEntry(Entry entry) {
 		String[] whereArgs = { Integer.toString(entry.getId()) };
 		return db.delete(ENTRY, "_id=?", whereArgs);
 	}
 	
 	private class OpenHelper extends SQLiteOpenHelper {
 		OpenHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
		
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			String SCAFFOLD = " (_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, server_id INTEGER, name TEXT, ";
 			db.execSQL(CREATE + GARDEN + SCAFFOLD + "bounds TEXT, city TEXT, state TEXT, is_public INTEGER)");
 			db.execSQL(CREATE + PLOT   + SCAFFOLD + "garden_id INTEGER, shape INTEGER, color INTEGER, angle REAL, bounds TEXT, points TEXT)");
 			db.execSQL(CREATE + PLANT  + SCAFFOLD + "plot_id INTEGER)");
 			db.execSQL(CREATE + ENTRY  + SCAFFOLD + "plant_id INTEGER, date TEXT)");
 			db.execSQL(CREATE + PHOTO  + SCAFFOLD + "garden_id INTEGER, uri TEXT)");
 		}
		
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			db.execSQL(DROP + "garden");
 			db.execSQL(DROP + "plot");
 			db.execSQL(DROP + "plant");
 			db.execSQL(DROP + "entry");
 			db.execSQL(DROP + "photo");
 			onCreate(db);
 		}
 	}
 	
 	private float getFloat(Cursor cursor, String column) {
 		return cursor.getFloat(cursor.getColumnIndex(column));
 	}
 	private int getInt(Cursor cursor, String column) {
 		return cursor.getInt(cursor.getColumnIndex(column));
 	}
 	private String getString(Cursor cursor, String column) {
 		return cursor.getString(cursor.getColumnIndex(column));
 	}
 	
 }
