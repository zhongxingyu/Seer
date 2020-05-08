 //used a tutorial from vogella.com
 package edu.westmont.course;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.location.Location;
 import android.util.Log;
 
 import com.google.android.gms.maps.model.LatLng;
 
 public class PositionsDataSource {
 	
   public String run = "default";
 
   // Database fields
   private SQLiteDatabase database;
   private MySQLiteHelper dbHelper;
   private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
       MySQLiteHelper.COLUMN_LATITUDE, MySQLiteHelper.COLUMN_LONGITUDE,
       MySQLiteHelper.COLUMN_HEIGHT,MySQLiteHelper.COLUMN_TIME};
 
   public PositionsDataSource(Context context) {
     dbHelper = new MySQLiteHelper(context);
   }
 
   public void open() throws SQLException {
     database = dbHelper.getWritableDatabase();
   }
 
   public void close() {
     dbHelper.close();
   }
 
   public Position createPosition(Location loc) {
     ContentValues values = new ContentValues();
     values.put(MySQLiteHelper.COLUMN_LATITUDE, loc.getLatitude());
     values.put(MySQLiteHelper.COLUMN_LONGITUDE, loc.getLongitude());
     values.put(MySQLiteHelper.COLUMN_HEIGHT, loc.getAltitude());
     values.put(MySQLiteHelper.COLUMN_TIME, loc.getTime());
     long insertId = database.insert(run, null,
         values);
     Cursor cursor = database.query(run,
         allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
         null, null, null);
     cursor.moveToFirst();
     Position newPosition = cursorToPosition(cursor);
     cursor.close();
     return newPosition;
   }
 
   //TODO fix this to work with multiple tables.
   public void deletePosition(Position position) {
     long id = position.getId();
     System.out.println("Position deleted with id: " + id);
     database.delete(MySQLiteHelper.TABLE_POSITIONS, MySQLiteHelper.COLUMN_ID
         + " = " + id, null);
   }
   
   public void setRunName(String runName){
 	  run = sanitizeInput(runName);
   }
   
   //set the run name prior to calling this method.
   public void makeRun(){
 	  if(!containsTable(run)) dbHelper.createTable(database,run);
   }
   
   public boolean containsTable(String tablename){
 	  Cursor cursor = dbHelper.showAllTables(database);
 	    if (cursor.moveToFirst()){
 	    	do{
 	    		if(cursor.getString(0).equals(tablename)) return true;
 	    	}while (cursor.moveToNext());
 	    }
 	    return false;
   }
   
   //logs all table names.
   public void displayAllTables(){
 	  Cursor cursor = dbHelper.showAllTables(database);
 	  Log.w(MySQLiteHelper.class.getName(),"the current tables are: ");
 	    if (cursor.moveToFirst()){
 	    	do{
 	    		Log.w(MySQLiteHelper.class.getName(),cursor.getString(0));
 	    	}while (cursor.moveToNext());
 	    }
   }
 
   //gets all positions from the current runs table
   public List<Position> getAllPositions() {
     List<Position> positions = new ArrayList<Position>();
 
     Cursor cursor = database.query(run,
         allColumns, null, null, null, null, null);
 
     cursor.moveToFirst();
     while (!cursor.isAfterLast()) {
       Position position = cursorToPosition(cursor);
       positions.add(position);
       cursor.moveToNext();
     }
     // make sure to close the cursor
     cursor.close();
     Log.w("dataSource","finished with getting all positions");
     return positions;
   }
 
   private Position cursorToPosition(Cursor cursor) {
     Position position = new Position("database");
     position.setId(cursor.getLong(0));
     Log.w("PositionsDataSouce","The id is: " + position.getId());
     position.setLatitude(cursor.getDouble(1));
     position.setLongitude(cursor.getDouble(2));
     position.setAltitude(cursor.getDouble(3));
     position.setTime(cursor.getLong(4));
     position.setAccuracy(99); //arbitrary number for accuracy. I think I can get away with not storing accuracy.
     return position;
   }
   
   private String sanitizeInput(String runName){
 	  runName = runName.trim();
 	  //replaces anything that is not a letter or a number with an underscore.
 	  runName = runName.replaceAll("[^[a-zA-Z_0-9]]", "_");
 	  return runName;
   }
 } 
