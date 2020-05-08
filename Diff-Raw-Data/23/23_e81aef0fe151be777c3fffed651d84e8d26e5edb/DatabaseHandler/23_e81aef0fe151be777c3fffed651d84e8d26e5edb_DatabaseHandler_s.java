 package se.chalmers.project14.database;
 
 /*
  * Copyright (c) 2012 ICRL
  * See the file license.txt for copying permission.
  */
 
 import java.util.ArrayList;
 import java.util.List;
 
 import se.chalmers.project14.model.Coordinates;
 import se.chalmers.project14.model.Door;
 import se.chalmers.project14.model.House;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 /**
  * A class representing the database, It creates three database tables which all
  * are connected with an id, which mean that if you have an id you can get all
  * information that existing the database. DatabaseHandler has methods so we can
  * add and get values from our database
  * 
  * @author tomassellden
  * 
  */
 public class DatabaseHandler extends SQLiteOpenHelper {
 
 	private static final int DATABASE_VERSION = 8;
 	private static final String DATABASE_NAME = "cordinatesManager";
 	private static final String TABLE_COORDINATE = "CoordinateTable";
 	private static final String TABLE_HOUSE = "HouseTable";
 	private static final String TABLE_DOORCOORDINATES = "DoorTable";
 
 	private static final String KEY_ID = "_id";
 	private static final String KEY_LECTUREROOM = "_lectureRoom";
 	private static final String KEY_BUILDING = "_building";
 	private static final String KEY_COORDINATES = "_houseCoordinates";
 	private static final String KEY_FLOOR = "_floor";
 	private static final String KEY_DOOR = "_doorCoordinates";
 
 	private String CREATE_HOUSE_TABLE = "CREATE TABLE IF NOT EXISTS "
 			+ TABLE_HOUSE + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
 			+ KEY_LECTUREROOM + " TEXT," + KEY_FLOOR + " TEXT" + ")";
 
 	private String CREATE_COORDINATES_TABLE = "CREATE TABLE IF NOT EXISTS "
 			+ TABLE_COORDINATE + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
 			+ KEY_COORDINATES + " TEXT" + ")";
 
 	private String CREATE_DOOR_TABLE = "CREATE TABLE IF NOT EXISTS "
 			+ TABLE_DOORCOORDINATES + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
 			+ KEY_BUILDING + " TEXT," + KEY_DOOR + " TEXT" + ")";
 
 	public DatabaseHandler(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 
 	}
 
 	/**
 	 * Creates the database. Its creating three table, one containing hte
 	 * lecture room, one contains information about the coordinates and one
 	 * contains information about the doorcoordinates and which floor the
 	 * lecture room can be found,
 	 */
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL(CREATE_HOUSE_TABLE);
 		db.execSQL(CREATE_COORDINATES_TABLE);
 		db.execSQL(CREATE_DOOR_TABLE);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_COORDINATE);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_HOUSE);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOORCOORDINATES);
 		onCreate(db);
 
 	}
 
 	/**
 	 * Adding house/lecture room to the database table "TABLE_HOUSE"
 	 * 
 	 * @param house
 	 *            a house object containing information about the lecture name
 	 * @param coordinates
 	 *            a coordinate object containing information about the lectures
 	 *            coordinates
 	 * @param door
 	 *            a door object containing information about the which door and
 	 *            which floor the lecture room can be found
 	 */
 	public void addCthHouse(House house, Coordinates coordinates, Door door) {
 		SQLiteDatabase sqdb = this.getWritableDatabase();
 		ContentValues contentValues = new ContentValues();
 		int tableKey = house.getId();
 		contentValues.put(KEY_ID, tableKey);
 		contentValues.put(KEY_FLOOR, house.getFloor());
 		contentValues.put(KEY_LECTUREROOM, house.getLectureRoom());
 		sqdb.insertWithOnConflict(TABLE_HOUSE, null, contentValues,
 				SQLiteDatabase.CONFLICT_IGNORE);
 		sqdb.close();
 		addCthCoordinates(tableKey, coordinates, door);
 	}
 
 	/**
 	 * Adding coordinates to the database table "TABLE_COORDINATE"
 	 * 
 	 * @param tableKey
 	 *            the id the coordinates and are connected to
 	 * @param coordinates
 	 *            a coordinate object containing information about the lectures
 	 *            coordinates
 	 * @param door
 	 *            a door object containing information about the which door and
 	 *            which floor the lecture room can be found
 	 */
 	public void addCthCoordinates(int tableKey, Coordinates coordinates,
 			Door door) {
 		SQLiteDatabase sqdb = this.getWritableDatabase();
 		ContentValues contentValues = new ContentValues();
 		contentValues.put(KEY_ID, tableKey);
 		contentValues.put(KEY_COORDINATES, coordinates.getCoordinates());
 		sqdb.insertWithOnConflict(TABLE_COORDINATE, null, contentValues,
 				SQLiteDatabase.CONFLICT_IGNORE);
 		sqdb.close();
 		addCthDoors(tableKey, door);
 
 	}
 
 	/**
 	 * Adding door cordinates and which floor lecture room can be found to the
 	 * database table "TABLE_DOORCOORDINATES"
 	 * 
 	 * @param tableKey
 	 *            the id the coordinates and are connected to
 	 * @param door
 	 *            a door object containing information about the which door and
 	 *            which floor the lecture room can be found
 	 */
 	public void addCthDoors(int tableKey, Door door) {
 		SQLiteDatabase sqdb = this.getWritableDatabase();
 		ContentValues contentValues = new ContentValues();
 		contentValues.put(KEY_ID, tableKey);
 		contentValues.put(KEY_BUILDING, door.getBuilding());
 		contentValues.put(KEY_DOOR, door.getDoorCoordinates());
 		sqdb.insertWithOnConflict(TABLE_DOORCOORDINATES, null, contentValues,
 				SQLiteDatabase.CONFLICT_IGNORE);
 		sqdb.close();
 	}
 
 	/**
 	 * Getter for the house name connected to the id
 	 * 
 	 * @param id
 	 *            an int, the id which all information about one building is
 	 *            connected to
 	 * @return a House object containing the house/lecture room the id is
 	 *         connected to
 	 */
 	public House getHouse(int id) {
 		SQLiteDatabase sqdb = this.getReadableDatabase();
 
 		Cursor cursor = sqdb.query(TABLE_HOUSE, new String[] { KEY_ID,
 				KEY_LECTUREROOM, KEY_FLOOR }, KEY_ID + "=?",
 				new String[] { String.valueOf(id) }, null, null, null, null);
 		if (cursor != null) {
 			cursor.moveToFirst();
 		}
 		House house = new House(cursor.getString(1));
 		return house;
 	}
 
 	/**
 	 * Getter for the coordinates connected to the id
 	 * 
 	 * @param id
 	 *            an int, the id which all information about one building is
 	 *            connected to
 	 * @return a Coordinate object, containing the coordinates the id is
 	 *         connected to
 	 * 
 	 */
 	public Coordinates getCoordinates(int id) {
 		SQLiteDatabase sqdb = this.getReadableDatabase();
 		Cursor cursor = sqdb.query(TABLE_COORDINATE, new String[] { KEY_ID,
 				KEY_COORDINATES }, KEY_ID + "=?",
 				new String[] { String.valueOf(id) }, null, null, null, null);
 		if (cursor != null) {
 			cursor.moveToFirst();
 		}
 		Coordinates coordinates = new Coordinates(cursor.getString(1));
 		return coordinates;
 	}
 
 	/**
 	 * Getter for the doorcoordinates and which floor the lecture room/house can
 	 * be found connected to the id
 	 * 
 	 * @param id
 	 *            an int, the id which all information about one building is
 	 *            connected to
 	 * @return a Door object, containing the lecture room/house coordinates and
 	 *         the floor the id is connected to
 	 */
 	public Door getDoorCoordinates(int id) {
 		SQLiteDatabase sqdb = this.getReadableDatabase();
 		Cursor cursor = sqdb.query(TABLE_DOORCOORDINATES, new String[] {
 				KEY_ID, KEY_BUILDING, KEY_DOOR }, KEY_ID + "=?",
 				new String[] { String.valueOf(id) }, null, null, null, null);
 		if (cursor != null) {
 			cursor.moveToFirst();
 		}
 		Door door = new Door(cursor.getString(1), cursor.getString(2));
 		return door;
 	}
 
 	/**
 	 * Getter for all floors/lecture rooms that exists in the database table
 	 * TABLE_HOUSE
 	 * 
 	 * @return an ArrayList containing all house/lecture room that exists in the
 	 *         database table TABLE_HOUSE
 	 */
 	public List<House> getAllLectureRoom() {
 		List<House> houseList = new ArrayList<House>();
 		String allHouse = "SELECT * FROM " + TABLE_HOUSE;
 		SQLiteDatabase sqdb = this.getWritableDatabase();
 		Cursor cursor = sqdb.rawQuery(allHouse, null);
 
 		cursor.moveToFirst();
 		while (!cursor.isAfterLast()) {
 			House house = new House();
 			house.setId(Integer.parseInt(cursor.getString(0)));
 			house.setLectureRoom(cursor.getString(1));
 			house.SetFloor(cursor.getString(2));
 			houseList.add(house);
 			cursor.moveToNext();
 		}
 
 		sqdb.close();
 		cursor.close();
 		return houseList;
 	}
 
 	/**
 	 * Getter for all houses/doorcoordinates that exists in the database table
 	 * TABLE_DOORCOORDINATES
 	 * 
 	 * @return An arrayList containing all house/doorcoordinates that exists in
 	 *         the database table TABLE_DOORCOORDINATES
 	 */
 	public List<Door> getAllDoorsAndBuildings() {
 		List<Door> doorAndBuildingList = new ArrayList<Door>();
 		String alldoors = "SELECT * FROM " + TABLE_DOORCOORDINATES;
 		SQLiteDatabase sqdb = this.getWritableDatabase();
 		Cursor cursor = sqdb.rawQuery(alldoors, null);
 
 		cursor.moveToFirst();
 		while (!cursor.isAfterLast()) {
 			Door door = new Door();
 			door.setDoor(cursor.getString(1));
 			door.setBuilding(cursor.getString(2));
 			doorAndBuildingList.add(door);
 			cursor.moveToNext();
 		}
		Door door = new Door();
		door.setListDoor(doorAndBuildingList);
 		sqdb.close();
 		cursor.close();
 		return doorAndBuildingList;
 	}
 }
