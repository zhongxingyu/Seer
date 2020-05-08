 package tengwa.djvu;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 
 public class RecentDbAdapter {
     public static final String TABLE_NAME = RecentDbHelper.TABLE_NAME;
 	public static final String KEY_ROWID = RecentDbHelper.KEY_ROWID;
     public static final String KEY_NAME = RecentDbHelper.KEY_NAME;
     public static final String KEY_PATH = RecentDbHelper.KEY_PATH;
     public static final String KEY_PAGE = RecentDbHelper.KEY_PAGE;
     public static final String KEY_DATE = RecentDbHelper.KEY_DATE;
    public static final String SELECT_ALL = "SELECT * FROM " + TABLE_NAME;
 	private Context mContext;
 	private SQLiteDatabase mDatabase;
 	private RecentDbHelper mDbHelper;
 
 	public RecentDbAdapter(Context context) {
 		this.mContext = context;
 	}
 
 	public RecentDbAdapter open() throws SQLException {
 		mDbHelper = new RecentDbHelper(mContext);
 		mDatabase = mDbHelper.getWritableDatabase();
 		return this;
 	}
 
 	public void close() {
 		mDbHelper.close();
 	}
 
 	public long create(String name, String path, int page) {
        Cursor c = mDatabase.rawQuery(SELECT_ALL, null);
        int pathColumn = c.getColumnIndex(KEY_PATH);
        if (c.moveToFirst()){
            do {
                String p = c.getString(pathColumn);
                if (p.equals(path)) {
                    long id = c.getLong(c.getColumnIndex(KEY_ROWID));
                    update(id, page);
                    c.close();
                    return id;
                }
            } while (c.moveToNext());
        }
        c.close();

         ContentValues values = createContentValues(name, path, page);
 		return mDatabase.insert(TABLE_NAME, null, values);
 	}
 
 	public boolean update(long rowId, int page){
 		ContentValues updateValues = createContentValues(page);
 		return mDatabase.update(TABLE_NAME, updateValues, KEY_ROWID + "=" + rowId, null) > 0;
 	}
 
 	public boolean delete(long rowId) {
 		return mDatabase.delete(TABLE_NAME, KEY_ROWID + "=" + rowId, null) > 0;
 	}
 
 	public Cursor fetchAll() {
         //TODO: order by date descending
 		return mDatabase.query(TABLE_NAME, new String[] { KEY_ROWID, KEY_NAME, KEY_PATH, KEY_PAGE,
                 KEY_DATE }, null, null, null, null, null);
 	}
 
 	public Cursor fetch(long rowId) throws SQLException {
 		Cursor mCursor = mDatabase.query(true, TABLE_NAME, new String[]
                 {KEY_ROWID, KEY_NAME, KEY_PATH, KEY_PAGE, KEY_DATE },
 				KEY_ROWID + "=" + rowId, null, null, null, null, null);
 		if (mCursor != null) {
 			mCursor.moveToFirst();
 		}
 		return mCursor;
 	}
 
 	private ContentValues createContentValues(String name, String path, int page){
 		ContentValues values = createContentValues(page);
         values.put(KEY_NAME, name);
 		values.put(KEY_PATH, path);
 		return values;
 	}
 
     private ContentValues createContentValues(int page){
 		ContentValues values = new ContentValues();
         values.put(KEY_PAGE, page);
         values.put(KEY_DATE, System.currentTimeMillis());
 		return values;
 	}

    private ContentValues createContentValues(long id, String name, String path, int page){
        ContentValues values = createContentValues(name, path, page);
        values.put(KEY_ROWID, id);
		return values;
    }
 }
