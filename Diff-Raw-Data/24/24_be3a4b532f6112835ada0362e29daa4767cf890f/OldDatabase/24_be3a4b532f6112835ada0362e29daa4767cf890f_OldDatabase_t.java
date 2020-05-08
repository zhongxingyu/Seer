 package jp.gr.java_conf.neko_daisuki.simplemediascanner;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.provider.BaseColumns;
 
 class OldDatabase {
 
     private static class DatabaseHelper extends SQLiteOpenHelper {
 
         public interface Columns extends BaseColumns {
 
             public static final String PATH = "path";
         }
 
         public static final String TABLE_NAME = "directories";
 
         private static final String DATABASE_NAME = "directories.db";
         private static final int DATABASE_VERSION = 1;
 
         public DatabaseHelper(Context ctx) {
             super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
         }
 
         @Override
         public void onCreate(SQLiteDatabase db) {
         }
 
         @Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         }
     }
 
     public static void importOldDatabase(Context context) {
         Database database = new Database();
         database.initializeEmptyDatabase();
 
         List<String> directories = queryDirectories(context);
         for (String path: directories) {
             database.addTask(path);
         }
         Util.writeDatabase(context, database);
     }
 
     private static List<String> queryDirectories(Context context) {
         List<String> directories = new LinkedList<String>();
 
         SQLiteOpenHelper database = new DatabaseHelper(context);
         try {
             SQLiteDatabase db = database.getReadableDatabase();
            Cursor cursor;
            try {
                cursor = db.query(
                        DatabaseHelper.TABLE_NAME,
                        new String[] { DatabaseHelper.Columns.PATH },
                        null,   // selection
                        null,   // selection args
                        null,   // group by
                        null,   // having
                        null);  // order by
            }
            catch (SQLiteException e) {
                return directories;
            }
             try {
                 while (cursor.moveToNext()) {
                     directories.add(cursor.getString(0));
                 }
             }
             finally {
                 cursor.close();
             }
         }
         finally {
             database.close();
         }
 
         return directories;
     }
 }
