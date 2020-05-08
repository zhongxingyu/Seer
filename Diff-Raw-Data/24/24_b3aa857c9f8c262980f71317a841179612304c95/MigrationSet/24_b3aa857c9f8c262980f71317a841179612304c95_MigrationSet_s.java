 package ca.ottawaandroid.velvet.migrations;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 
 public class MigrationSet {
     private static final String DATABASE_VERSION_SQL = "CREATE TABLE IF NOT EXISTS DATABASE_VERSION (ID INTEGER PRIMARY KEY, CURRENT_VERSION INTEGER)";
     private static final String GET_VERSION_SQL = "SELECT CURRENT_VERSION FROM DATABASE_VERSION LIMIT 1";
     private static final String DATABASE_VERSION = "DATABASE_VERSION", ID = "ID", VERSION = "CURRENT_VERSION";
     
     private int mVersion = 0;
     private LinkedHashMap<Integer, ArrayList<Migration>> mVerMigrations;
 
     public MigrationSet(){
 	mVerMigrations = new LinkedHashMap<Integer, ArrayList<Migration>>();
     }
 
     public void version(int v){
 	if( !mVerMigrations.containsKey(v) && v == (mVersion + 1)){
 	    ++mVersion;
 	    mVerMigrations.put(v, new ArrayList<Migration>());
 	}
     }
 
     public void add(Migration migration){
 	mVerMigrations.get(mVersion).add(migration);
     }
 
     public void add(int version, Migration migration){
 	version(version);
 	add(migration);
     }
 
     public void apply(SQLiteDatabase db){
 	apply(db, getVersion(db), mVersion);
     }
 
     public void apply(SQLiteDatabase db, int startingFrom, int finishingAt){
 	int currentMigration = startingFrom;
 	for(Integer mVer : mVerMigrations.keySet()){
 	    if(mVer > finishingAt){
 		break;
 	    }
 	    if(mVer <= currentMigration) {
 		continue;
 	    }
 	    migrate(mVerMigrations.get(mVer), db);
 	    updateVersionTable(db, mVer.intValue());
 	}
     }
 
     void migrate(ArrayList<Migration> ms, SQLiteDatabase db){
 	for(Migration m : ms){
 	    m.setUp(db);
 	}
     }
 
     void rollback(ArrayList<Migration> ms){
     }
 
     private void updateVersionTable(SQLiteDatabase db, int nextVersion){
 	db.execSQL(DATABASE_VERSION_SQL);
	ContentValues cv = new ContentValues();
	cv.put(ID, 1);
	cv.put(VERSION, nextVersion);
	db.replace(DATABASE_VERSION, null, cv);
     }
 
     private int getVersion(SQLiteDatabase db){
 	int version;
 	Cursor c = null;
 	try {
 	    c = db.rawQuery(GET_VERSION_SQL, null);
 	    c.moveToFirst();
 	    version = c.getInt(c.getColumnIndex(VERSION));
 	} catch(SQLiteException e) {
 	    version = 0;
 	} finally {
 	    if( c != null ){
 		c.close();
 	    }
 	}
 	return version;
     }
 }
