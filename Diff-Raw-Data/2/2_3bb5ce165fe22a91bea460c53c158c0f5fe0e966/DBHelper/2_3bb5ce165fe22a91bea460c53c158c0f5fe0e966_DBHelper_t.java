 /*
  * Copyright (C) 2008 Google Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package edu.pitt.cs1635group3;
 
 import java.util.ArrayList;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 /**
  * Simple notes database access helper class. Defines the basic CRUD operations
  * for the notepad example, and gives the ability to list all notes as well as
  * retrieve or modify a specific note.
  * 
  * This has been improved from the first version of this tutorial through the
  * addition of better error handling and also using returning a Cursor instead
  * of using a collection of inner classes (which is less scalable and not
  * recommended).
  */
 public class DBHelper {
 
 	public static final String ITEM_TABLE = "item";
 	public static final String LIST_TABLE = "list";
 	public static final String USER_TABLE = "user";
 
 	public static final String KEY_ITEM_ID = "id";
 	public static final String KEY_PARENT_ID = "parent_id";
 	public static final String KEY_ITEM_NAME = "name";
 	public static final String KEY_ADDER_ID = "adder_id";
 	public static final String KEY_ADD_DATE = "add_date";
 	public static final String KEY_QUANTITY = "quantity";
 	public static final String KEY_ASSIGNED_TO = "assignee_id";
 	public static final String KEY_ASSIGNER_ID = "assigner_id";
 	public static final String KEY_NOTES = "notes";
 	public static final String KEY_COMPLETED = "completed";
 	public static final String KEY_COMPLETION_DATE = "completion_date";
 	public static final String KEY_ITEM_PREV = "prev_id";
 	public static final String KEY_ITEM_NEXT = "next_id";
 	public static final String KEY_ITEM_SELECTED = "selected"; // ONLY FOR THE
 																// ANDROID APP
 																// (wont come
 																// from server.
 																// will
 																// initialize to
 																// false!!)
 
 	public static final String KEY_LIST_ID = "id";
 	public static final String KEY_LIST_NAME = "name";
 	public static final String KEY_CREATOR_ID = "creator_id";
 	public static final String KEY_CREATION_DATE = "creation_date";
 	public static final String KEY_NOTE = "notes";
 
 	public static final String KEY_MAP_ITEM_LIST_ID = "id";
 	public static final String KEY_MAP_LIST_USER_ID = "id";
 	public static final String KEY_MAP_ITEM_ID = "item_id";
 	public static final String KEY_MAP_LIST_ID = "list_id";
 	public static final String KEY_MAP_USER_ID = "user_id";
 
 	public static final String KEY_USER_ID = "id";
 	public static final String KEY_USER_FIRST = "first";
 	public static final String KEY_USER_LAST = "last";
 
 	private static final String TAG = "SociaList: DbAdapter";
 	private DatabaseHelper mDbHelper;
 	private SQLiteDatabase db;
 
 	/**
 	 * Database creation sql statement
 	 */
 	private static final String ITEM_CREATE = "create table item (id integer primary key autoincrement, "
 			+ "parent_id integer, "
 			+ "name text not null, "
 			+ "adder_id integer, "
 			+ "add_date date DEFAULT CURRENT_DATE, "
 			+ "quantity integer DEFAULT '1', "
 			+ "assignee_id integer, "
 			+ "assigner_id integer, "
 			+ "notes text, completed integer, "
 			+ "completion_date text, "
 			+ "prev_id integer, "
 			+ "next_id integer, "
 			+ "selected integer, " 
 			+ "UNIQUE (id) ON CONFLICT IGNORE)";
 	
 	/*If the default value of the column is a constant NULL, text, 
 	 * blob or signed-number value, then that value is used directly in the new row.
 
 If the default value of a column is an expression in parentheses, 
 then the expression is evaluated once for each row inserted and 
 the results used in the new row.
 
 If the default value of a column is CURRENT_TIME, CURRENT_DATE or
  CURRENT_TIMESTAMP, then the value used in the new row is a text
   representation of the current UTC date and/or time. For CURRENT_TIME,
    the format of the value is "HH:MM:SS". For CURRENT_DATE, "YYYY-MM-DD". 
    The format for CURRENT_TIMESTAMP is "YYYY-MM-DD HH:MM:SS".
 */
 
 	private static final String LIST_CREATE = "create table list (id integer primary key autoincrement, "
 			+ "name text not null, creator_id integer not null, "
			+ "creation_date text DEFAULT CURRENT_DATE, UNIQUE(id) ON CONFLICT IGNORE)";
 
 	private static final String MAP_LIST_USER_CREATE = "create table map_list_user (id integer primary key autoincrement, "
 			+ "list_id integer not null, user_id integer not null)";
 
 	private static final String USER_CREATE = "create table user (id integer primary key autoincrement, "
 			+ "first text not null, last text not null)";
 
 	private static final String DATABASE_NAME = "socialist_db";
 	private static final int DATABASE_VERSION = 1;
 
 	private final Context mCtx;
 
 	/**
 	 * Constructor - takes the context to allow the database to be
 	 * opened/created
 	 * 
 	 * @param ctx
 	 *            the Context within which to work
 	 */
 	public DBHelper(Context ctx) {
 		this.mCtx = ctx;
 	}
 
 	public boolean abandonShip() {
 		return db.delete("list", null, null) > 0;
 	}
 
 	private static class DatabaseHelper extends SQLiteOpenHelper {
 
 		public DatabaseHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			//db.delete("list", null, null);
 			//db.delete("item", null, null);
 			//db.delete("user", null, null);
 			db.execSQL(ITEM_CREATE);
 			db.execSQL(LIST_CREATE);
 			db.execSQL(MAP_LIST_USER_CREATE);
 			db.execSQL(USER_CREATE);
 			
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
 					+ newVersion + ", which will destroy all old data");
 			onCreate(db);
 		}
 	} // end DatabaseHelper inner class
 
 	public DBHelper open() throws SQLException {
 
 		mDbHelper = new DatabaseHelper(mCtx);
 		db = mDbHelper.getWritableDatabase();
 		return this;
 	}
 
 	public void close() {
 		mDbHelper.close();
 	}
 
 	/*
 	 * USER METHODS
 	 */
 
 	public long insertUser(User u) {
 		ContentValues initialValues = new ContentValues();
 		initialValues.put(KEY_USER_ID, u.getID());
 		initialValues.put(KEY_USER_FIRST, u.getFirstName());
 		initialValues.put(KEY_USER_LAST, u.getLastName());
 		return db.insert(USER_TABLE, null, initialValues);
 	}
 
 	public CharSequence[] getUsersForDialog() { // TODO - make this take ListID
 												// as a param
 
 		CharSequence[] users;
 
 		String myQuery = "SELECT * FROM user"; // TODO - more detailed query
 												// involving list &
 												// map_list_user
 		Cursor c = db.rawQuery(myQuery, null);
 
 		if (c != null)
 			c.moveToFirst();
 		users = new CharSequence[c.getCount()]; // allow for number of users
 												// returned
 
 		c.moveToFirst();
 		int i = 0;
 		while (!c.isAfterLast()) {
 			users[i] = (CharSequence) c.getString(1) + " " + c.getString(2);
 			c.moveToNext();
 			i++;
 		}
 
 		return users;
 	}
 
 	public ArrayList<User> getAllUsers() {
 
 		ArrayList<User> users = null;
 		String myQuery = "SELECT * FROM user";// WHERE parent_id = " + ID;
 		Cursor c = db.rawQuery(myQuery, null);
 
 		if (c != null) {
 			users = new ArrayList<User>(c.getCount());
 			c.moveToFirst();
 
 			while (!c.isAfterLast()) {
 				User u = new User(c.getInt(0), c.getString(1), c.getString(2));
 				users.add(u);
 				c.moveToNext();
 			}
 
 		}
 		return users;
 
 	}
 
 	public String getUserNameByID(int row) {
 		String myQuery = "SELECT * FROM user WHERE id = " + row;
 		Cursor c = db.rawQuery(myQuery, null);
 
 		if (c != null)
 			c.moveToFirst();
 
 		String name = c.getString(1) + " " + c.getString(2);
 		c.close();
 		return name;
 
 	}
 
 	public User getUserByID(int row) {
 		// Log.i("DB USER", "Querying for user ID = " + row);
 		String myQuery = "SELECT * FROM user WHERE id = " + row;
 		Cursor c = db.rawQuery(myQuery, null);
 
 		if (c != null)
 			c.moveToFirst();
 
 		User u = cursorToUser(c);
 		c.close();
 		return u;
 	}
 
 	public int getUserByName(String user) {
 
 		String[] pieces = user.split(" ");
 		String fname = pieces[0];
 		String lname = pieces[1];
 
 		String myQuery = "SELECT * FROM user WHERE first = ? AND last = ?";
 		Cursor c = db.rawQuery(myQuery, new String[] { fname, lname });
 
 		if (c != null)
 			c.moveToFirst();
 
 		int userID = c.getInt(0);
 		c.close();
 		return userID;
 	}
 
 	private User cursorToUser(Cursor c) {
 		// Log.d("DB", "c.getCount() is " +c.getCount());
 		User u = new User(c.getInt(0), c.getString(1), c.getString(2));
 		c.close();
 		return u;
 	}
 
 	/*
 	 * LIST METHODS
 	 */
 	
 	public boolean deleteList(CustomList list) {
 
 		return db.delete(LIST_TABLE, KEY_LIST_ID + "=?",
 				new String[] { String.valueOf(list.getID()) }) > 0;
 	}
 	
 	public boolean deleteListByID(int listID) {
 
 		return db.delete(LIST_TABLE, KEY_LIST_ID + "=?", 
 				new String[] { String.valueOf(listID) }) > 0;
 	}
 
 	public long insertList(CustomList list) {
 		Log.i("INSERTING LIST", "Here" + list.getName());
 		ContentValues initialValues = new ContentValues();
 		initialValues.put(KEY_LIST_ID, list.getID());
 		initialValues.put(KEY_LIST_NAME, list.getName());
 		initialValues.put(KEY_CREATOR_ID, list.getCreator());
 		initialValues.put(KEY_CREATION_DATE, list.getCreationDate());
 		return db.insert(LIST_TABLE, null, initialValues);
 	}
 
 	public ArrayList<CustomList> getAllLists() {
 
 		ArrayList<CustomList> lists = null;
 		String myQuery = "SELECT * FROM list";// WHERE parent_id = " + ID;
 		Cursor c = db.rawQuery(myQuery, null);
 
 		if (c != null) {
 			lists = new ArrayList<CustomList>(c.getCount());
 			c.moveToFirst();
 
 			while (!c.isAfterLast()) {
 				CustomList l = new CustomList(c.getInt(0), c.getString(1));
 				l.setCreator(c.getInt(2));
 				l.setCreationDate(c.getString(3));
 				lists.add(l);
 				c.moveToNext();
 			}
 		}
 		return lists;
 
 	}
 
 	public CustomList getListByID(int i) {
 
 		String myQuery = "SELECT * FROM list WHERE id = " + i;
 		Cursor c = db.rawQuery(myQuery, null);
 		if (c != null) {
 			c.moveToFirst();
 		}
 
 		CustomList myList = new CustomList(c.getInt(0), c.getString(1));
 		myList.setCreator(c.getInt(2));
 		myList.setCreationDate(c.getString(3));
 		return myList;
 	}
 
 	public ArrayList<Item> getItemsForListByID(int ID) {
 
 		ArrayList<Item> items = null;
 		String myQuery = "SELECT * FROM item WHERE parent_id = " + ID;
 		Cursor c = db.rawQuery(myQuery, null);
 
 		if (c != null) {
 			items = new ArrayList<Item>(c.getCount());
 			c.moveToFirst();
 
 			while (!c.isAfterLast()) {
 				Item i = cursorToItem(c);
 				items.add(i);
 				c.moveToNext();
 			}
 		}
 		c.close();
 		return items;
 
 	}
 
 	/*
 	 * ITEM METHODS
 	 */
 
 	public void insertItem(Item i) {
 		ContentValues initialValues = new ContentValues();
 
 		int isCompleted = 0;
 		if (i.isCompleted())
 			isCompleted = 1;
 
 		initialValues.put(KEY_ITEM_ID, i.getID());
 		initialValues.put(KEY_PARENT_ID, i.getParentID());
 		initialValues.put(KEY_ITEM_NAME, i.getName());
 		initialValues.put(KEY_ADDER_ID, i.getCreator());
 		initialValues.put(KEY_ADD_DATE, i.getCreationDate());
 		initialValues.put(KEY_QUANTITY, i.getQuantity());
 		initialValues.put(KEY_ASSIGNED_TO, i.getAssignee());
 		initialValues.put(KEY_ASSIGNER_ID, i.getAssigner());
 		initialValues.put(KEY_NOTES, i.getNotes());
 		initialValues.put(KEY_COMPLETED, isCompleted);
 		initialValues.put(KEY_COMPLETION_DATE, i.getCompletionDate());
 		initialValues.put(KEY_ITEM_PREV, i.getPrev());
 		initialValues.put(KEY_ITEM_NEXT, i.getNext());
 		initialValues.put(KEY_ITEM_SELECTED, 0); // On insertion, no item will
 													// ever be selected.
 													// (Right?) - Kim
 
 		Log.e("INSERTED ITEM", "Item " +i.getName()+ ", "+ db.insert(ITEM_TABLE, null, initialValues));
 	}
 
 	public boolean deleteItem(Item i) {
 		// Note - whenever this is called, be sure to update the encompassing list structure, since items are doubly-linked.
 
 		return db.delete(ITEM_TABLE, KEY_ITEM_ID + "=?",
 				new String[] { String.valueOf(i.getID()) }) > 0;
 	}
 
 	public Item getItem(int row) {
 		String myQuery = "SELECT * FROM item WHERE id = " + row;
 		Cursor c = db.rawQuery(myQuery, null);
 
 		if (c != null) {
 			c.moveToFirst();
 		}
 
 		Item i = cursorToItem(c);
 		c.close();
 		return i;
 	}
 
 	private Item cursorToItem(Cursor c) {
 		//Log.d("DB", "number of fields is " + c.getCount()+ ". Name is " +c.getString(2)+ " and ID is " +c.getInt(0));
 		Item i = new Item();
 		i.setID(c.getInt(0));
 		i.setName(c.getString(2));
 		i.setParent(c.getInt(1));
 		i.setCreator(c.getInt(3));
 		i.setCreationDate(c.getString(4));
 		i.setQuantity(c.getInt(5));
 		i.assignTo(c.getInt(6));
 		i.setAssigner(c.getInt(7));
 		i.setNotes(c.getString(8));
 		i.setCompleted(c.getInt(9));
 		i.setCompletionDate(c.getString(10));
 		i.setPrev(c.getInt(11));
 		i.setNext(c.getInt(12));
 		i.setSelected(c.getInt(13)); // when we're pulling items from the db,
 										// this is when we check if it's
 										// selected.
 
 		return i;
 	}
 
 	public boolean updateItem(Item i) {
 		ContentValues args = new ContentValues();
 
 		int isCompleted = 0, isSelected = 0;
 		if (i.isCompleted())
 			isCompleted = 1;
 		if (i.isSelected())
 			isSelected = 1;
 
 		args.put(KEY_ITEM_ID, i.getID());
 		args.put(KEY_PARENT_ID, i.getParentID());
 		args.put(KEY_ITEM_NAME, i.getName());
 		args.put(KEY_ADDER_ID, i.getCreator());
 		args.put(KEY_ADD_DATE, i.getCreationDate());
 		args.put(KEY_QUANTITY, i.getQuantity());
 		args.put(KEY_ASSIGNED_TO, i.getAssignee());
 		args.put(KEY_ASSIGNER_ID, i.getAssigner());
 		args.put(KEY_NOTES, i.getNotes());
 		args.put(KEY_COMPLETED, isCompleted);
 		args.put(KEY_COMPLETION_DATE, i.getCompletionDate());
 		args.put(KEY_ITEM_PREV, i.getPrev());
 		args.put(KEY_ITEM_NEXT, i.getNext());
 		args.put(KEY_ITEM_SELECTED, isSelected);
 
 		Log.d("SUCCESS:UPDATE ITEM", "Item " + i.getName() + " assigned to "
 				+ i.getAssignee() + " and isSelected " + i.isSelected());
 		return db.update(ITEM_TABLE, args, KEY_ITEM_ID + "=?",
 				new String[] { String.valueOf(i.getID()) }) > 0;
 
 	}
 }
