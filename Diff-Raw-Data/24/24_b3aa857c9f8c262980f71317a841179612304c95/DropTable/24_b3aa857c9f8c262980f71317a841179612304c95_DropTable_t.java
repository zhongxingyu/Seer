 package ca.ottawaandroid.velvet.migrations;
 
 import android.util.Log;
 import android.database.sqlite.SQLiteDatabase;
 
 public class DropTable implements Migration {
     public static final String DROP_TABLE_SQL = "DROP TABLE %s";
 
     private String mTbl;
 
     public DropTable(String tbl){
 	mTbl = tbl;
     }
 
     public void setUp(SQLiteDatabase db){
	String sql = String.format(DROP_TABLE_SQL, mTbl);
	Log.d("Migration->DropTable#setUp", sql);
	db.execSQL(sql);
     }
 
     public void tearDown(SQLiteDatabase db){
     }
 }
