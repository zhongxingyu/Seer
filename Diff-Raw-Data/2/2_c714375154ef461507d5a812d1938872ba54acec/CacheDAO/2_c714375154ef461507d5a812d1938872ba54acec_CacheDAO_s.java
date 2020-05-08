 package com.zeyomir.ocfun.dao;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.util.Log;
 import com.zeyomir.ocfun.R;
 import com.zeyomir.ocfun.model.Cache;
 
 public class CacheDAO {
 	public static final String[] from = {DbAdapter.KEY_CACHES_TYPE,
 			DbAdapter.KEY_CACHES_NAME, DbAdapter.KEY_CACHES_COORDS,
 			DbAdapter.KEY_CACHES_IS_FOUND};
 	public static final int[] to = {R.id.list_type_ico, R.id.list_cache_name,
 			R.id.list_distance, R.id.list_found};
 	public static final String searchableColumn = DbAdapter.KEY_CACHES_NAME;
 
 	public static final String idColumn = DbAdapter.KEY_CACHES_ID;
 
 	private DbAdapter db;
 
 	public static long save(Cache c) {
 		DbAdapter db = DbAdapter.open();
 		Log.d("DB", "trying to delete copy...");
 		db.clean(DbAdapter.DATABASE_TABLE_CACHES, "code='" + c.code + "'");
 		long id;
 		if (c.type == InternalResourceMapper.custom.id())
 			id = db.insert(map(c, db.count(DbAdapter.DATABASE_TABLE_CACHES, "code like '" + c.code + "%'")), DbAdapter.DATABASE_TABLE_CACHES);
 		else
 			id = db.insert(map(c), DbAdapter.DATABASE_TABLE_CACHES);
 		DbAdapter.close();
 		return id;
 	}
 
 	private static ContentValues map(Cache c) {
 		ContentValues values = new ContentValues();
 		values.put(DbAdapter.KEY_CACHES_CODE, c.code);
 		values.put(DbAdapter.KEY_CACHES_NAME, c.name);
 		values.put(DbAdapter.KEY_CACHES_ATTRIBUTES, c.attributes);
 		values.put(DbAdapter.KEY_CACHES_DESCRIPTION, c.description);
         values.put(DbAdapter.KEY_CACHES_NOTE, c.notes);
 		values.put(DbAdapter.KEY_CACHES_DIFFICULTY, "" + c.difficulty);
 		values.put(DbAdapter.KEY_CACHES_HINT, c.hint);
 		values.put(DbAdapter.KEY_CACHES_IS_FOUND, c.isFound ? 1 : 0);
 		values.put(DbAdapter.KEY_CACHES_LAST_FOUND, c.lastFoundOn);
 		values.put(DbAdapter.KEY_CACHES_COORDS, c.coords);
 		values.put(DbAdapter.KEY_CACHES_OWNER, c.owner);
 		values.put(DbAdapter.KEY_CACHES_REQUIRE_PASSWORD, c.requiresPassword ? 1 : 0);
 		values.put(DbAdapter.KEY_CACHES_SIZE, "" + c.size);
 		values.put(DbAdapter.KEY_CACHES_TERRAIN, "" + c.terrain);
 		values.put(DbAdapter.KEY_CACHES_TYPE, "" + c.type);
 		return values;
 	}
 
 	private static ContentValues map(Cache c, int numOfCustomCaches){
 		ContentValues values = map(c);
 		values.put(DbAdapter.KEY_CACHES_CODE, c.code + numOfCustomCaches);
 		return values;
 	}
 
 	private static Cache map(Cursor c) {
 		long id = c.getLong(c.getColumnIndex(DbAdapter.KEY_CACHES_ID));
 		String code = c.getString(c.getColumnIndex(DbAdapter.KEY_CACHES_CODE));
 		String name = c.getString(c.getColumnIndex(DbAdapter.KEY_CACHES_NAME));
 		String coords = c.getString(c
 				.getColumnIndex(DbAdapter.KEY_CACHES_COORDS));
 		int type = c.getInt(c.getColumnIndex(DbAdapter.KEY_CACHES_TYPE));
 		String owner = c
 				.getString(c.getColumnIndex(DbAdapter.KEY_CACHES_OWNER));
 		double size = c.getDouble(c.getColumnIndex(DbAdapter.KEY_CACHES_SIZE));
 		double difficulity = c.getDouble(c
 				.getColumnIndex(DbAdapter.KEY_CACHES_DIFFICULTY));
 		double terrain = c.getDouble(c
 				.getColumnIndex(DbAdapter.KEY_CACHES_TERRAIN));
 		boolean requiresPassword =
                     c.getInt(c.getColumnIndex(DbAdapter.KEY_CACHES_REQUIRE_PASSWORD))==1;
 		String description = c.getString(c
 				.getColumnIndex(DbAdapter.KEY_CACHES_DESCRIPTION));
         String notes = c
                 .getString(c.getColumnIndex(DbAdapter.KEY_CACHES_NOTE));
         if (notes == null)
             notes = "";
 		String attributes = c.getString(c
 				.getColumnIndex(DbAdapter.KEY_CACHES_ATTRIBUTES));
 		String hint = c.getString(c.getColumnIndex(DbAdapter.KEY_CACHES_HINT));
 		String lastFoundOn = c.getString(c
 				.getColumnIndex(DbAdapter.KEY_CACHES_LAST_FOUND));
 		boolean isFound =
                 c.getInt(c.getColumnIndex(DbAdapter.KEY_CACHES_IS_FOUND))==1;
 
 		return new Cache(id, code, name, coords, type, owner, size,
 				difficulity, terrain, requiresPassword, description, notes,
 				attributes, hint, lastFoundOn, isFound);
 	}
 
 	public void open() {
 		this.db = DbAdapter.open();
 	}
 
 	public void close() {
 		DbAdapter.close();
 	}
 
 	public Cursor list(String query) {
 		Cursor c = db.fetch("select "
 				+ DbAdapter.KEY_CACHES_ID
 				+ ", "
 				+ DbAdapter.KEY_CACHES_TYPE
 				+ ", "
 				+ DbAdapter.KEY_CACHES_NAME
 				+ ", "
 				+ DbAdapter.KEY_CACHES_COORDS
 				+ ", "
 				+ DbAdapter.KEY_CACHES_IS_FOUND
 				+ " from "
 				+ DbAdapter.DATABASE_TABLE_CACHES
 				/*+ (query.equals("") ? query : (" where "
 						+ DbAdapter.KEY_CACHES_CODE + " like %" + query
 						+ "% or " + DbAdapter.KEY_CACHES_NAME + " like %"
 						+ query + "%" + " COLLATE NOCASE")*/
 				+ " ORDER BY " + DbAdapter.KEY_CACHES_NAME);
 		return c;
 	}
     public Cursor listWithoutFound(String query) {
         Cursor c = db.fetch("select "
                 + DbAdapter.KEY_CACHES_ID
                 + ", "
                 + DbAdapter.KEY_CACHES_TYPE
                 + ", "
                 + DbAdapter.KEY_CACHES_NAME
                 + ", "
                 + DbAdapter.KEY_CACHES_COORDS
                 + ", "
                 + DbAdapter.KEY_CACHES_IS_FOUND
                 + " from "
                 + DbAdapter.DATABASE_TABLE_CACHES
                + " where " + DbAdapter.KEY_CACHES_IS_FOUND + "<>0"
 				/*+ (query.equals("") ? query : (" where "
 						+ DbAdapter.KEY_CACHES_CODE + " like %" + query
 						+ "% or " + DbAdapter.KEY_CACHES_NAME + " like %"
 						+ query + "%" + " COLLATE NOCASE")*/
                 + " ORDER BY " + DbAdapter.KEY_CACHES_NAME);
         return c;
     }
 
 	public static Cache get(long id) {
 		DbAdapter db = DbAdapter.open();
 		Cursor c = db.fetch("select * from " + DbAdapter.DATABASE_TABLE_CACHES
 				+ " where " + DbAdapter.KEY_CACHES_ID + "=" + id);
 		Log.i("CacheDAO", "pobrano kesza o id = " + id);
 		DbAdapter.close();
 		if (c.getCount() > 0)
 			return map(c);
 		return null;
 	}
 
 	public void clean() {
 		db.clean(DbAdapter.DATABASE_TABLE_CACHES, null);
 	}
 
 	public static void delete(long id) {
 		DbAdapter db = DbAdapter.open();
 		db.clean(DbAdapter.DATABASE_TABLE_CACHES, "_id=" + id);
 		DbAdapter.close();
 	}
 
     public static void update(Cache updatedCache) {
         DbAdapter db = DbAdapter.open();
         db.update(map(updatedCache), DbAdapter.DATABASE_TABLE_CACHES, ""+updatedCache.id);
     }
 }
