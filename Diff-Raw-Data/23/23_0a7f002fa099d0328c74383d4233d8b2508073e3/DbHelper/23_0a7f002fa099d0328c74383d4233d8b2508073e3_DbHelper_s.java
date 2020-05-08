 package az.his.android.persist;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 public class DbHelper extends SQLiteOpenHelper {
     public static final int VER = 1;
     private static final String FILENAME = "AzHis.db";
 
     public DbHelper(Context context) {
         super(context, FILENAME, null, VER);
     }
 
     @Override
     public void onCreate(SQLiteDatabase db) {
         StringBuilder sql = new StringBuilder();
         sql.append("CREATE TABLE ")
                 .append(CategoryColumns.TABLE_NAME)
                 .append(" (")
                 .append(CategoryColumns._ID)
                 .append(" INTEGER PRIMARY KEY, ")
                 .append(CategoryColumns.NAME)
                 .append(" TEXT, ")
                 .append(CategoryColumns.FOREIGN_ID)
                 .append(" INTEGER)");
 
         db.execSQL(sql.toString());
 
         sql = new StringBuilder();
         sql.append("CREATE TABLE ")
                 .append(TransactColumns.TABLE_NAME)
                 .append(" (")
                 .append(TransactColumns._ID)
                 .append(" INTEGER PRIMARY KEY, ")
                 .append(TransactColumns.AMOUNT)
                 .append(" INTEGER, ")
                 .append(TransactColumns.CAT_ID)
                 .append(" INTEGER, ")
                 .append(TransactColumns.STAMP)
                 .append(" INTEGER)");
 
         db.execSQL(sql.toString());
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
     }
 
     public List<Category> getCats() {
         Cursor cursor = getCatsCursor();
 
         List<Category> ret = new ArrayList<Category>();
         while (cursor.moveToNext()) {
             Category category = new Category();
             category.setId(cursor.getInt(0));
             category.setName(cursor.getString(1));
             category.setForeignId(cursor.getInt(2));
             ret.add(category);
         }
 
         cursor.close();
         return ret;
     }
 
     public Cursor getCatsCursor() {
         SQLiteDatabase db = getReadableDatabase();
 
         String[] proj = new String[]{
                 CategoryColumns.NAME,
                 CategoryColumns._ID,
                 CategoryColumns.FOREIGN_ID
         };
 
         Cursor cursor = db.query(
                 CategoryColumns.TABLE_NAME,
                 proj,
                 null, null, null, null,
                 CategoryColumns.NAME
         );
 
 //        db.close();
 
         return cursor;
     }
 
     public void replaceCats(Map<Integer, String> cats) {
         SQLiteDatabase db = getWritableDatabase();
         db.beginTransaction();
        db.delete(CategoryColumns.TABLE_NAME, "1=1", new String[]{});

        for (Integer catId : cats.keySet()) {
            ContentValues values = new ContentValues();
            values.put(CategoryColumns.NAME, cats.get(catId));
            values.put(CategoryColumns.FOREIGN_ID, catId);
            db.insert(CategoryColumns.TABLE_NAME, null, values);
         }

        db.endTransaction();
         db.close();
     }
 
     public void addTransaction(Integer amount, Integer catId) {
         SQLiteDatabase db = getWritableDatabase();
 
         ContentValues values = new ContentValues();
         values.put(TransactColumns.AMOUNT, amount);
         values.put(TransactColumns.CAT_ID, catId);
         values.put(TransactColumns.STAMP, (new Date()).getTime());
         db.insert(CategoryColumns.TABLE_NAME, null, values);
 
         db.close();
     }
 
     public long getTransactionNum() {
         SQLiteDatabase db = getReadableDatabase();
         long num = db.compileStatement("SELECT COUNT(*) FROM " + TransactColumns.TABLE_NAME).simpleQueryForLong();
         db.close();
 
         return num;
     }
 }
