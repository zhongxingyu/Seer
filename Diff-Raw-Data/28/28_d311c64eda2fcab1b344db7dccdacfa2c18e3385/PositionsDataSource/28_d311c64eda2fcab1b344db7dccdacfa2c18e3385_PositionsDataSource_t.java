 //used a tutorial from vogella.com
 package edu.westmont.course;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.location.Location;
 import android.util.Log;
 
 public class PositionsDataSource {
 
 	public String run = "default";
 
 	// Database fields
 	private SQLiteDatabase database;
 	private MySQLiteHelper dbHelper;
 	private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
 			MySQLiteHelper.COLUMN_LATITUDE, MySQLiteHelper.COLUMN_LONGITUDE,
 			MySQLiteHelper.COLUMN_HEIGHT,MySQLiteHelper.COLUMN_TIME,MySQLiteHelper.COLUMN_SPEED};
 
 	public PositionsDataSource(Context context) {
 		dbHelper = new MySQLiteHelper(context);
 	}
 
 	public void open() throws SQLException {
 		database = dbHelper.getWritableDatabase();
 	}
 
 	public void close() {
 		dbHelper.close();
 	}
 
 	//TODO fix this to work with multiple tables.
 	public void deletePosition(Position position) {
 		long id = position.getId();
 		System.out.println("Position deleted with id: " + id);
 		database.delete(MySQLiteHelper.TABLE_POSITIONS, MySQLiteHelper.COLUMN_ID
 				+ " = " + id, null);
 	}
 
 	public void setRunName(String runName){
 		run = runName;
 
 		Log.w("setRunName","The run Name is: "+runName);
 
 		ContentValues values = new ContentValues();
 		values.put(MySQLiteHelper.COLUMN_RUN_NAME, runName);
 		//only starts a new run if the previous was not marked as finished.
 		if (rowIsInTable(MySQLiteHelper.COLUMN_RUN,MySQLiteHelper.TABLE_STATS,getLastID(runName))){
 			Log.w("setRunName","Making a new Table: " + runName);
 			database.insert(MySQLiteHelper.TABLE_RUNS,null,values);
 		}
 		else Log.w("setRunName", "Table "+runName+" has an unfinished run, continuing that run.");
 
 		Log.w("SetRunName","id is: " + getLastID(runName));
 	}
 
 	public void makeRun(){
 		if(!containsTable(run)) dbHelper.createTable(database,run);
 	}
 
 	private boolean rowIsInTable(String column,String table,int item){
 		Cursor cursor = database.query(table, null, column + "=" + item, null, null, null, null);
 		if (cursor.getCount()>0) return true;
 		else return false;
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
 
 
 
 	public void createPosition(Location loc,String runName) {
 		int id = getLastID(runName);
 
 
 		ContentValues values = new ContentValues();
 		values.put(MySQLiteHelper.COLUMN_ID,id);
 		values.put(MySQLiteHelper.COLUMN_LATITUDE, loc.getLatitude());
 		values.put(MySQLiteHelper.COLUMN_LONGITUDE, loc.getLongitude());
 		values.put(MySQLiteHelper.COLUMN_HEIGHT, loc.getAltitude());
 		values.put(MySQLiteHelper.COLUMN_TIME, loc.getTime());
 		values.put(MySQLiteHelper.COLUMN_SPEED, loc.getSpeed());
 		database.insert(MySQLiteHelper.TABLE_POSITIONS, null,values);
 	}
 
 	private Position cursorToPosition(Cursor cursor) {
 		Position position = new Position("database");
 		position.setId(cursor.getLong(0));
 
 		position.setLatitude(cursor.getDouble(1));
 		position.setLongitude(cursor.getDouble(2));
 		position.setAltitude(cursor.getDouble(3));
 		position.setTime(cursor.getLong(4));
 		position.setSpeed(cursor.getFloat(5));
 		position.setAccuracy(99); //arbitrary number for accuracy. I think I can get away with not storing accuracy.
 		return position;
 	}
 
 
 
 	public void deleteAllRunEntries(String runName){
 		int[] runIDs = getIDs(runName);
 		int i;
 
 		for (i=0;i<runIDs.length;i++){
 			//delete from positions
 			Log.w("deleteRun",
 					"# of rows deleted: " + database.delete(MySQLiteHelper.TABLE_POSITIONS, MySQLiteHelper.COLUMN_ID + "=" + runIDs[i], null));
 			//delete from runs
 			database.delete(MySQLiteHelper.TABLE_RUNS, MySQLiteHelper.COLUMN_RUN_ID + "=" + runIDs[i], null);
 			//delete from stats
 			database.delete(MySQLiteHelper.TABLE_STATS, MySQLiteHelper.COLUMN_RUN + "=" + runIDs[i], null);
 		}
 	}
 
 	//total time in Seconds
 	public Long totalTime(String runName){
 		int id = getLastID(runName);
 		Long time = (long) 0;
 		String[] column = {MySQLiteHelper.COLUMN_TIME};
 		Cursor cursor = database.query(MySQLiteHelper.TABLE_POSITIONS, column, MySQLiteHelper.COLUMN_ID + "=" + id,
 				null, null, null, null);
 
 		if (cursor.getCount() != 0){
 			cursor.moveToFirst();
 			time = cursor.getLong(0);
 			cursor.moveToLast();
 			time = cursor.getLong(0) - time;
 			time = time / 1000; //convert to seconds.
 		}
 		cursor.close();
 		return time;
 	}
 
 	public double highest(String runName,String column){
 		String[] columns = {column, MySQLiteHelper.COLUMN_DATE};
 		double spdOrAlt = 0;
 		//Log.w("highest","runName is: " + runName);
 		Cursor cursor = getAllFromStatistics(getIDs(runName),columns);
 		cursor.moveToLast();
 		if (column.equals(MySQLiteHelper.COLUMN_HIGHEST_ALTITUDE)) spdOrAlt = cursor.getDouble(3);
 		if (column.equals(MySQLiteHelper.COLUMN_HIGHEST_SPEED)) spdOrAlt = cursor.getDouble(1);
 		cursor.close();
 		Log.w("PositionsDataSource", "highest Speed is: " + spdOrAlt);
 		return spdOrAlt;
 	}
 
 	private Cursor getAllFromStatistics(int[] ids,String[] columns){
 		String IDs = intArrayToString(ids);
 		String selection = stringArrayToString(columns);
 		Log.w("getAllFromStatistics","IDs are: " + IDs);
 		Log.w("getAllFromStatistics","selections are: " + selection);
 		String query = "SELECT "+ "*" +" FROM " + MySQLiteHelper.TABLE_STATS + " WHERE "+ MySQLiteHelper.COLUMN_RUN +" IN " + IDs;
 		return database.rawQuery(query,null);
 	}
 	//I don't know where to put these next two methods. Also there is probably some way to combine the two, but I don't know
 	//how to handle the conflicting data types.
 	public static String stringArrayToString(String[] array){
 		int i;
 		String string = "(";
 		for (i=0;i<array.length;i++){
 			string += array[i] + "";
 			if (i<array.length - 1) string += ",";
 		}
 		string += ")";
 		return string;
 	}
 	public static String intArrayToString(int[] array){
 		int i;
 		String string = "(";
 		for (i=0;i<array.length;i++){
 			string += array[i] + "";
 			if (i<array.length - 1) string += ",";
 		}
 		string += ")";
 		return string;
 	}
 
 	public double totalDistance(String runName){
 		List<Position> positions = getCurrentRun(runName);
 		Position position;
 		double totalDistance = 0;
 		int i;
 		for (i=0;i<positions.size()-1;i++){
 			position = positions.get(i);
 			totalDistance += position.distanceTo(positions.get(i+1));
 		}
 		return totalDistance;
 	}
 
 	public double averageSpeed(String runName){
 		return totalDistance(runName) / totalTime(runName);
 	}
 
 	public Point[] timeVsNumber(String runName){
 		Point[] points;
 		int i;
 		String[] column = {MySQLiteHelper.COLUMN_BEST_TIME};
 		Cursor cursor = getAllFromStatistics(getIDs(runName), column);
 		cursor.moveToFirst();
 		Log.w("PositionsDataSource","cursor count is: " + cursor.getCount());
 		points = new Point[cursor.getCount()];
 		for (i=0;i<cursor.getCount();i++){
 			points[i] = new Point(i,cursor.getLong(2) / 1000);
 			cursor.moveToNext();
 		}
 		cursor.close();
 		return points;
 	}
 
 	private int getTableSize(String table){
 		Cursor cursor = database.query(table, null, null, null, null, null, null);
 		Log.w("getTableSize","table size of " + table + "is: " + cursor.getCount());
 		return cursor.getCount();
 	}
 
 	private int[] getIDs(String runName){
 		Cursor cursor = database.query(MySQLiteHelper.TABLE_RUNS, null,
 				MySQLiteHelper.COLUMN_RUN_NAME + "=" + "'"+runName+"'", null, null, null, null);
 		Log.w("getIDs", "cursor count is: " + cursor.getCount());
 		int[] ids = new int[cursor.getCount()];
 		int i;
 		cursor.moveToFirst();
 		Log.w("getIDs", "The IDs are: ");
 		for (i=0;i<cursor.getCount();i++){
 			ids[i] = cursor.getInt(1);
 			Log.w("getIDs", "" + ids[i]);
 			cursor.moveToNext();
 		}
 		cursor.close();
 		Log.w("getIDs","IDs are: " + intArrayToString(ids));
 		return ids;
 	}
 
 	//returns the highest id with the same run name.
 	private int getLastID(String runName){
 		String[] columns = {MySQLiteHelper.COLUMN_RUN_NAME,MySQLiteHelper.COLUMN_RUN_ID};
 		Cursor cursor = database.query(MySQLiteHelper.TABLE_RUNS, columns, MySQLiteHelper.COLUMN_RUN_NAME + "=" + "'"+runName+"'", null, null, null, null);
 		//Log.w("get Last ID","Pulling from: " + cursor.getString(0) + " The ID is: " + cursor.getInt(1));
 		int id = 0;
 		if (cursor.moveToLast()) {id = cursor.getInt(1);Log.w("getLastID","last ID is: "+id);}
 		cursor.close();
 		return id;
 	}
 
 	public LinkedList<String> getAllRuns(){
 		LinkedList<String> runs = new LinkedList<String>();
 		String[] columns = {MySQLiteHelper.COLUMN_RUN_NAME};
 		Cursor cursor = database.query(MySQLiteHelper.TABLE_RUNS, columns, 
 				null, null, null, null, null, null);
 		cursor.moveToFirst();
 		while(!cursor.isAfterLast()){
 			if (!runs.contains(cursor.getString(0))) runs.add(cursor.getString(0));
 			cursor.moveToNext();
 		}
 		return runs;
 	}
 
 	public List<Position> getBestRun(String runName,String column){
 		String[] columns = {MySQLiteHelper.COLUMN_RUN,column};
 		Cursor cursor = getAllFromStatistics(getIDs(runName),columns);
 		long bestTime = -1;
 		int theID = -1;
 		cursor.moveToFirst();
 		while (!cursor.isAfterLast()){
 			if (bestTime > cursor.getLong(1) || bestTime < 0){
 				bestTime = cursor.getLong(1);
 				theID = cursor.getInt(0);
 			}
 			cursor.moveToNext();
 		}
 		return getAllPositions(runName,theID);
 	}
 
 	public void done(String runName){
 		//add best time, speed, and altitude to statistics table
 		addDataToStatistics(runName);
 		close(); //Maybe?
 	}
 	//=======
 
 
 
 
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
 
 	//gets all positions from the current runs table. Use an id of 0 or less to get the current run.
 	public List<Position> getAllPositions(String runName,int id) {
 		List<Position> positions = new ArrayList<Position>();
 		if (id < 0) id = getLastID(runName);
 
 		Cursor cursor = database.query(MySQLiteHelper.TABLE_POSITIONS,
 				allColumns, MySQLiteHelper.COLUMN_ID + "=" + id, null, null, null, null);
 
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
 
 	public List<Position> getCurrentRun(String runName){
 		int id = getLastID(runName);
 		return getAllPositions(runName,id);
 	}
 
 	//adds the data from the current run to the Stats table
 	private void addDataToStatistics(String runName){
 		int id = getLastID(runName);
 		long time = 0, date = 0;
 		double altitude = 0;
 		float speed = 0;
 		ContentValues values = new ContentValues();
 		Cursor cursor = database.query(MySQLiteHelper.TABLE_POSITIONS, allColumns, MySQLiteHelper.COLUMN_ID + "=" + id,
 				null, null, null, null);
 
 		cursor.moveToFirst();
 		while (!cursor.isAfterLast()) {
 			if (cursor.isFirst()) time = cursor.getLong(4);
 			if (altitude < cursor.getDouble(3)) altitude = cursor.getDouble(3);
 			if (speed < cursor.getFloat(5)) speed = cursor.getFloat(5);
 			if (cursor.isLast()) {time = cursor.getLong(4) - time; date = cursor.getLong(4);}
 			cursor.moveToNext();
 		}
 		cursor.close();
 
 		values.put(MySQLiteHelper.COLUMN_RUN, getLastID(runName));
 		values.put(MySQLiteHelper.COLUMN_HIGHEST_SPEED, speed);
 		values.put(MySQLiteHelper.COLUMN_BEST_TIME, time);
 		values.put(MySQLiteHelper.COLUMN_HIGHEST_ALTITUDE, altitude);
 		values.put(MySQLiteHelper.COLUMN_DATE, date);
 		database.insert(MySQLiteHelper.TABLE_STATS, null, values);
 		Log.w("PositionsDataSource","highest speed: "+ speed + ". time: " + time / 1000 + " seconds. highest altitude: " + altitude);
 	}
 } 
