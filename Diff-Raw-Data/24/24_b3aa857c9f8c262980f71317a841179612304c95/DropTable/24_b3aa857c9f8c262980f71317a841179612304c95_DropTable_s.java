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
     }
 
     public void tearDown(SQLiteDatabase db){
     }
 }
