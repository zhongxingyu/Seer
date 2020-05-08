 package nz.gen.wellington.guardian.android.usersettings;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import nz.gen.wellington.guardian.android.model.Section;
 import nz.gen.wellington.guardian.android.model.Tag;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteStatement;
 import android.util.Log;
 
 // TODO null safe all db opens.
 public class SqlLiteFavouritesDAO {
 	
 	private static final String TAG = "SqlLiteFavouritesDAO";
 	
 	private static final String DATABASE_NAME = "guardian-lite.db";
 	private static final int DATABASE_VERSION = 1;
 	private static final String TAG_TABLE = "favourites";
 
 	private static final String INSERT = "insert into " + TAG_TABLE + "(type, apiid, name, sectionid) values (?, ?, ?, ?)";
 
 	OpenHelper openHelper;
 
 	public SqlLiteFavouritesDAO(Context context) {
 		openHelper = new OpenHelper(context);
 	}
 	
	public boolean hasFavourites() {	// TODO count query rather than select all
 		SQLiteDatabase db = openHelper.getReadableDatabase();		
 		Cursor cursor = db.query(TAG_TABLE, new String[] { "type", "apiid", "name","sectionid" }, null, null, null, null, "name asc");		
 		int total = cursor.getCount();
 		closeCursor(cursor);
 		db.close();
 		return total > 0;	
 	}
 	
	public long insert(String type, String apiid, String name, String sectionid) {
 		SQLiteDatabase db = openHelper.getWritableDatabase();
 		SQLiteStatement insertStmt = db.compileStatement(INSERT);
 		insertStmt.bindString(1, type);
 		insertStmt.bindString(2, apiid);
 		insertStmt.bindString(3, name);
 		insertStmt.bindString(4, sectionid);
 		long result = insertStmt.executeInsert();
 		db.close();
 		return result;
 	}
 	
 		
	public void deleteAll() {
 		SQLiteDatabase db = openHelper.getWritableDatabase();
 		db.delete(TAG_TABLE, null, null);
 		db.close();
 	}
 	
 	
	public boolean isFavourite(Tag tag) {
 		SQLiteDatabase db = openHelper.getReadableDatabase();
 		Cursor cursor = db.query(TAG_TABLE, new String[] { "apiid" }, " type = 'tag' and apiid = ? ", new String[] { tag.getId() }, null, null, "name asc");
 		final boolean isFavourite = cursor.getCount() > 0;
 		closeCursor(cursor);
 		final boolean result = isFavourite;	
 		db.close();
 		return result;
 	}
 	
 
	public boolean isFavourite(Section section) {
 		SQLiteDatabase db = openHelper.getReadableDatabase();
 		Cursor cursor = db.query(TAG_TABLE, new String[] { "apiid" }, " type = 'section' and apiid = ? ", new String[] { section.getId() }, null, null, "name asc");
 		final boolean isFavourite = cursor.getCount() > 0;
 		closeCursor(cursor);		
 		db.close();
 		return isFavourite;	
 	}
 	
 	
	public void removeSection(Section section) {
 		SQLiteDatabase db = openHelper.getWritableDatabase();
 		db.delete(TAG_TABLE, " apiid = ? ", new String[] { section.getId() });
 		db.close();
 	}
 	
	public void removeTag(Tag tag) {
 		SQLiteDatabase db = openHelper.getWritableDatabase();
 		db.delete(TAG_TABLE, " apiid = ? ", new String[] { tag.getId() });
 		db.close();
 	}
 	
 	
	public List<Tag> getFavouriteTags(Map<String, Section> sectionsMap) {
 		List<Tag> favouriteTags = new ArrayList<Tag>();
 		SQLiteDatabase db = openHelper.getReadableDatabase();
 		if (db != null && db.isOpen()) {
 			Cursor cursor = db.query(TAG_TABLE, new String[] { "type", "apiid", "name","sectionid" }, null, null, null, null, "name asc");		
 			if (cursor.moveToFirst()) {
 				do {
 					final String type = cursor.getString(0);
 					final String id = cursor.getString(1);
 					final String name = cursor.getString(2);
 					final String sectionId = cursor.getString(3);
 					if (type.equals("tag")) {
 						favouriteTags.add(new Tag(name, id, sectionsMap.get(sectionId)));
 					}
 					
 				} while (cursor.moveToNext());
 			}
 			closeCursor(cursor);
 			db.close();
 		} else {
 			Log.i(TAG, "Could not open readable database connection");
 		}
 		return favouriteTags;
 	}
 
 
	public List<Section> getFavouriteSections(Map<String, Section> sectionsMap) {
 		SQLiteDatabase db = openHelper.getReadableDatabase();
 		Cursor cursor = db.query(TAG_TABLE, new String[] { "type", "apiid", "name","sectionid" }, null, null, null, null, "name asc");
 		
 		List<Section> favouriteSections = new ArrayList<Section>();
 		if (cursor.moveToFirst()) {
 			do {
 				final String type = cursor.getString(0);
 				final String sectionId = cursor.getString(3);
 				if (type.equals("section")) {
 					Section section = sectionsMap.get(sectionId);
 					if (section != null) {
 						favouriteSections.add(section);
 					}
 				}
 				
 			} while (cursor.moveToNext());
 		}
 		closeCursor(cursor);
 		db.close();
 		return favouriteSections;
 	}
 	
 	
	public boolean addTag(Tag keyword) {
 		SQLiteDatabase db = openHelper.getWritableDatabase();
 		boolean result = false;
 		if (haveRoom(db)) {
 			this.insert("tag", keyword.getId(), keyword.getName(), (keyword.getSection() != null) ? keyword.getSection().getId(): "global");
 			result = true;
 		}
 		db.close();
 		return result;		
 	}
 	
 	
	public boolean addSection(Section section) {
 		SQLiteDatabase db = openHelper.getReadableDatabase();
 		boolean result = false;
 		if (this.haveRoom(db)) {
 			this.insert("section", section.getId(), section.getName(), section.getId());
 			result = true;
 		}
 		db.close();
 		return result;
 	}
 	
 	
 	private boolean haveRoom(SQLiteDatabase db) {
 		Cursor cursor = db.query(TAG_TABLE, new String[] { "type", "apiid", "name","sectionid" }, null, null, null, null, "name asc");		
 		int total = cursor.getCount();
 		closeCursor(cursor);
 		return total < 20;	
 	}
 	
 	private void closeCursor(Cursor cursor) {
 		if (cursor != null && !cursor.isClosed()) {
 			cursor.close();
 		}
 	}	
 		
 	private static class OpenHelper extends SQLiteOpenHelper {
 
 		OpenHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 			
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db.execSQL("CREATE TABLE " + TAG_TABLE + "(id INTEGER PRIMARY KEY, type, apiid, name, sectionid TEXT)");
 		}
 		
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			db.execSQL("DROP TABLE IF EXISTS " + TAG_TABLE);
 			onCreate(db);
 		}
 	}
 	
 }
