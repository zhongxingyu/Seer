 package il.ac.huji.app4beer.DAL;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class SqlLiteHelper extends SQLiteOpenHelper {
 
 	public SqlLiteHelper(Context context) {
	    super(context, "app4beer_db", null, 15);
 	}
 	
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 	    db.execSQL("create table events ( " +
 		  	      "_id integer primary key autoincrement, " +
 		  	      "date integer, " +
 		  	      "name string, " +
 		  	      "owner integer, " +
 		  	      "location string, " +
 		  	      "description string);");
 	    
 	    db.execSQL("create table groups ( " +
 		  	      "_id integer primary key autoincrement, " +
 		  	      "name string unique);");
 	   
 	    db.execSQL("create table contacts ( " +
 		  	      "_id integer primary key autoincrement, " +
 		  	      "name string unique, phone string);");
 	    
 	    db.execSQL("create table participants ( " +
 		  	      "contactid integer, " +
 		  	      "attending integer, " +
 		  	      "source integer, " +
 		  	      "eventid integer);");
 	    
 	    db.execSQL("create table eventgroups (" +
 		  	      "eventid integer, groupid integer);");
 	    
 	    db.execSQL("create table members ( " +
 		  	      "contactid integer, groupid integer);");
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
 		String tables[] = {"events", "groups", "contacts", "participants", "eventgroups", "members"};
 		for (int i=0;i<tables.length;i++) {
 			db.execSQL("drop table IF EXISTS "+tables[i]+";");
 		}
 		onCreate(db);
 	}
 
 }
