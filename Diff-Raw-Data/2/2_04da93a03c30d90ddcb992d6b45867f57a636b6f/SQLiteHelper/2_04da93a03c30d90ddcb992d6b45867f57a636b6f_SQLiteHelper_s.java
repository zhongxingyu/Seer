 package com.teamluper.luper;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 import java.io.InputStream;
 
 public class SQLiteHelper extends SQLiteOpenHelper {
   private static final String DATABASE_NAME = "luperlocal.db";
  private static final int DATABASE_VERSION = 10;
 
   private Context context;
 
   public SQLiteHelper(Context c) {
     super(c, DATABASE_NAME, null, DATABASE_VERSION);
     context = c;
   }
 
   @Override
   public void onCreate(SQLiteDatabase database) {
     InputStream in = context.getResources().openRawResource(R.raw.create_sqlite_tables);
     java.util.Scanner s = new java.util.Scanner(in).useDelimiter(";");
     while(s.hasNext()) {
       database.execSQL(s.next());
     }
   }
 
   @Override
   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
     Log.w(SQLiteHelper.class.getName(),
         "Upgrading database from version " + oldVersion + " to "
             + newVersion + ", which will destroy all old data");
     onCreate(db);
     // TODO if the version changes after 1.0, we'll need to use some
     // ALTER TABLE queries for upgrading without deleting anything!
   }
 
   public int getVersion() {
     return DATABASE_VERSION;
   }
 }
