 package no.ntnu.stud.fallprevention.connectivity;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import no.ntnu.stud.fallprevention.R;
 import no.ntnu.stud.fallprevention.datastructures.Contact;
 import no.ntnu.stud.fallprevention.datastructures.Event;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.DatabaseUtils;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.net.Uri;
 import android.provider.ContactsContract;
 import android.util.Log;
 
 /**
  * Database helper is an object that builds and maintains the database. It also
  * works as an interface to the database.
  * 
  * @author Elias, Johannes
  * 
  */
 
 public class DatabaseHelper extends SQLiteOpenHelper {
 
 	public static final int DATABASE_VERSION = 22;
 	public static final String DATABASE_NAME = "FallPrevention.db";
 
 	public static final String COMMA = ", ";
 	public static final String START_PAR = " (";
 	public static final String END_PAR = ") ";
 	public static final String DOT = ".";
 	public static final String EQUAL = "=";
 
 	private Context context;
 
 	public DatabaseHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		this.context = context;
 	}
 
 	/**
 	 * Builds a table which is used to store the information. Is called
 	 * automatically by the system when a DataBaseHelper object is created, but
 	 * no database exists.
 	 * 
 	 * @param db
 	 *            - Provided by the system
 	 */
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		// Fill the database with some random entries, and of course build the
 		// tables
 		final String CREATE_TABLE_1 = "CREATE TABLE "
 				+ DatabaseContract.EventType.TABLE_NAME + START_PAR
 				+ DatabaseContract.EventType.COLUMN_NAME_ID
 				+ " INTEGER PRIMARY KEY,"
 				+ DatabaseContract.EventType.COLUMN_NAME_TITLE + COMMA
 				+ DatabaseContract.EventType.COLUMN_NAME_DESCRIPTION + COMMA
 				+ DatabaseContract.EventType.COLUMN_NAME_ICON + END_PAR;
 		final String CREATE_TABLE_2 = "CREATE TABLE "
 				+ DatabaseContract.Event.TABLE_NAME + START_PAR
 				+ DatabaseContract.Event.COLUMN_NAME_ID
 				+ " INTEGER PRIMARY KEY,"
 				+ DatabaseContract.Event.COLUMN_NAME_TYPEID + END_PAR;
 		final String CREATE_TABLE_3 = "CREATE TABLE "
 				+ DatabaseContract.Contact.TABLE_NAME + START_PAR
 				+ DatabaseContract.Contact.COLUMN_NAME_ID
 				+ " INTEGER PRIMARY KEY,"
 				+ DatabaseContract.Contact.COLUMN_NAME_NAME + COMMA
 				+ DatabaseContract.Contact.COLUMN_NAME_PHONE + END_PAR;
 		final String CREATE_TABLE_4 = "CREATE TABLE "
 				+ DatabaseContract.AlarmTypes.TABLE_NAME + START_PAR
 				+ DatabaseContract.AlarmTypes.COLUMN_NAME_ID
 				+ " INTEGER PRIMARY KEY,"
 				+ DatabaseContract.AlarmTypes.COLUMN_NAME_DESCRIPTION + END_PAR;
 		final String FILL_INFO_1 = "INSERT INTO EventType (TypeID, Description, Headline, Icon) VALUES (0, 'Event_1', 'Event_1', \'halo\')";
		final String FILL_INFO_2 = "INSERT INTO EventType (TypeID, Description, Headline, Icon) VALUES (1, 'Event_2', 'Event_2', \'sleep\')";
 		final String FILL_INFO_3 = "INSERT INTO Event (ID, TypeID) VALUES (0, 0)";
 		final String FILL_INFO_4 = "INSERT INTO Event (ID, TypeID) VALUES (1, 1)";
 		final String FILL_INFO_5 = "INSERT INTO Event (ID, TypeID) VALUES (2, 1)";
 		final String FILL_INFO_6 = "INSERT INTO Contact (PersonID, Name, PhoneNumber) VALUES (0, 'Dat-Danny Pham', 47823094)";
 		final String FILL_INFO_7 = "INSERT INTO Contact (PersonID, Name, PhoneNumber) VALUES (1, 'Fyllip Larzzon', 2356094)";
 		final String FILL_INFO_8 = "INSERT INTO AlarmTypes (AlarmID, Description) VALUES (0, 'SMS if risk is high')";
 		final String FILL_INFO_9 = "INSERT INTO AlarmTypes (AlarmID, Description) VALUES (1, 'SMS if sudden spike')";
 		final String FILL_INFO_10 = "INSERT INTO AlarmTypes (AlarmID, Description) VALUES (2, 'SMS if gradual improvement')";
 		final String FILL_INFO_11 = "INSERT INTO AlarmTypes (AlarmID, Description) VALUES (3, 'SMS if fall')";
 		final String FILL_INFO_12 = "INSERT INTO EventType (TypeID, Description, Headline, Icon) VALUES (2, 'Event_3', 'Event_3', \'sleep\')";
 		final String FILL_INFO_13 = "INSERT INTO EventType (TypeID, Description, Headline, Icon) VALUES (3,'Event_4','Event_4',\'halo\')";
 		db.execSQL(CREATE_TABLE_1);
 		db.execSQL(CREATE_TABLE_2);
 		db.execSQL(CREATE_TABLE_3);
 		db.execSQL(CREATE_TABLE_4);
 		db.execSQL(FILL_INFO_1);
 		db.execSQL(FILL_INFO_2);
 		db.execSQL(FILL_INFO_3);
 		db.execSQL(FILL_INFO_4);
 		db.execSQL(FILL_INFO_5);
 		db.execSQL(FILL_INFO_6);
 		db.execSQL(FILL_INFO_7);
 		db.execSQL(FILL_INFO_8);
 		db.execSQL(FILL_INFO_9);
 		db.execSQL(FILL_INFO_10);
 		db.execSQL(FILL_INFO_11);
 		db.execSQL(FILL_INFO_12);
 		db.execSQL(FILL_INFO_13);
 	}
 
 	/**
 	 * Upgrades the database version by clearing the content and building new
 	 * tables. Is called automatically by the system if one changes
 	 * DATABASE_VERSION.
 	 * 
 	 * @param: db - The database to upgrade
 	 * @param: oldVersion - The version number of the database before the
 	 *         upgrade
 	 * @param: newVersion - The version number of the database after the upgrade
 	 */
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		// Clears the database on an upgrade, and reset it
 		reset(db);
 		try {
 			db.execSQL("DROP TABLE " + DatabaseContract.EventType.TABLE_NAME);
 		} catch (SQLiteException e) {
 			// Do nothing...
 		}
 		try {
 			db.execSQL("DROP TABLE " + DatabaseContract.Event.TABLE_NAME);
 		} catch (SQLiteException e) {
 			// Do nothing...
 		}
 		try {
 			db.execSQL("DROP TABLE " + DatabaseContract.Contact.TABLE_NAME);
 		} catch (SQLiteException e) {
 			// Do nothing...
 		}
 		try {
 			db.execSQL("DROP TABLE " + DatabaseContract.AlarmTypes.TABLE_NAME);
 		} catch (SQLiteException e) {
 			// Do nothing...
 		}
 		onCreate(db);
 	}
 
 	/**
 	 * Downgrade the database version and clear its content. Called
 	 * automatically by the system if the DATABASE_VERSION constant is changed
 	 * to a lower number.
 	 * 
 	 * @param: db - The database to downgrade
 	 * @param: oldVersion - The version number of the database before the
 	 *         downgrade
 	 * @param: newVersion - The version number of the database after the
 	 *         downgrade
 	 */
 	@Override
 	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		// Does the same as if the database has been upgraded.
 		onUpgrade(db, oldVersion, newVersion);
 	}
 
 	/**
 	 * Fetches a list of (Event.ID, EventType.Description, EventType.Icon)
 	 * tuples from the database.
 	 * 
 	 * @return The list
 	 */
 	public List<Event> dbGetEventList() {
 		List<Event> events = new ArrayList<Event>();
 		SQLiteDatabase db = getReadableDatabase();
 
 		Cursor c = db.rawQuery("SELECT "
 				+ DatabaseContract.EventType.COLUMN_NAME_TITLE + COMMA
 				+ DatabaseContract.Event.COLUMN_NAME_ID + COMMA
 				+ DatabaseContract.EventType.COLUMN_NAME_ICON + " FROM "
 				+ DatabaseContract.Event.TABLE_NAME + " INNER JOIN "
 				+ DatabaseContract.EventType.TABLE_NAME + " ON "
 				+ DatabaseContract.Event.TABLE_NAME + DOT
 				+ DatabaseContract.Event.COLUMN_NAME_TYPEID + EQUAL
 				+ DatabaseContract.EventType.TABLE_NAME + DOT
 				+ DatabaseContract.EventType.COLUMN_NAME_ID, null);
 
 		// Iterate over the data fetched
 		c.moveToFirst();
 		for (int i = 0; i < c.getCount(); i++) {
 			c.moveToPosition(i);
 			Event e = new Event(getLocalEventTitle(c.getString(0)),
 					Integer.parseInt(c.getString(1)), c.getString(2));
 			events.add(e);
 		}
 
 		// Close database connection
 		c.close();
 		db.close();
 
 		return events;
 	}
 
 	/**
 	 * A help for localizing messages from the database. Translates abstract
 	 * event title codes to strings found in the localized string.xml files.
 	 * 
 	 * @param origTitle
 	 *            - The event title code found in the database.
 	 * @return The localized event title.
 	 */
 	public String getLocalEventTitle(String origTitle) {
 		String mReturner = "";
 		if (origTitle.equalsIgnoreCase("event_1")) {
 			mReturner = context.getString(R.string.event_list_goodJob_title);
 		} else if (origTitle.equalsIgnoreCase("event_2")) {
 			mReturner = context.getString(R.string.event_list_badJob_title);
 		} else if (origTitle.equalsIgnoreCase("event_3")) {
 			mReturner = context.getString(R.string.event_list_noChange_title);
 		} else {
 			mReturner = origTitle;
 		}
 		return mReturner;
 	}
 
 	/**
 	 * A help for localizing messages from the database. Translates abstract
 	 * event description codes to strings found in the localized string.xml
 	 * files.
 	 * 
 	 * @param origDesc
 	 *            - The event description code found in the database.
 	 * @return The localized event description.
 	 */
 	public String getLocalEventDescription(String origDesc) {
 		String mReturner = "";
 		if (origDesc.equalsIgnoreCase("event_1")) {
 			mReturner = context.getString(R.string.event_list_goodJob_desc);
 		} else if (origDesc.equalsIgnoreCase("event_2")) {
 			mReturner = context.getString(R.string.event_list_badJob_desc);
 		} else if (origDesc.equalsIgnoreCase("event_3")) {
 			mReturner = context.getString(R.string.event_list_noChange_desc);
 		} else {
 			mReturner = origDesc;
 		}
 		return mReturner;
 	}
 
 	/**
 	 * Stores a new event of the appropriate type to the database.
 	 */
 	public void dbAddEvent(int eventType) {
 		SQLiteDatabase db = getWritableDatabase();
 		ContentValues values = new ContentValues();
 		values.put(DatabaseContract.Event.COLUMN_NAME_TYPEID, eventType);
 		db.insert(DatabaseContract.Event.TABLE_NAME, null, values);
 		db.close();
 	}
 
 	/**
 	 * Deletes the event with the given ID from the database.
 	 * 
 	 * @return True if an event was deleted, false if no event with the given id
 	 *         exists or an error occurred.
 	 */
 	public boolean dbDeleteEvent(int id) {
 		int rowsAffected = 0;
 		try {
 			SQLiteDatabase db = getWritableDatabase();
 			rowsAffected = db.delete(DatabaseContract.Event.TABLE_NAME,
 					DatabaseContract.Event.COLUMN_NAME_ID + " = ?",
 					new String[] { String.valueOf(id) });
 			db.close();
 		} catch (SQLiteException e) {
 			return false;
 		}
 		return (rowsAffected > 0);
 	}
 
 	/**
 	 * Fetches information about a single event from the database.
 	 * 
 	 * @param id
 	 *            - the id of the event in the database
 	 * 
 	 * @return A map where the column names are keys
 	 */
 	public Map<String, String> dbGetEventInfo(int id) {
 		Map<String, String> stringMap = new HashMap<String, String>();
 
 		SQLiteDatabase db = getReadableDatabase();
 
 		Cursor c = db.rawQuery(
 				"SELECT " + DatabaseContract.EventType.COLUMN_NAME_DESCRIPTION
 						+ ", " + DatabaseContract.EventType.COLUMN_NAME_TITLE
 						+ " FROM " + DatabaseContract.Event.TABLE_NAME
 						+ " INNER JOIN "
 						+ DatabaseContract.EventType.TABLE_NAME + " ON "
 						+ DatabaseContract.Event.TABLE_NAME + "."
 						+ DatabaseContract.Event.COLUMN_NAME_TYPEID + "="
 						+ DatabaseContract.EventType.TABLE_NAME + "."
 						+ DatabaseContract.EventType.COLUMN_NAME_ID
 						+ " WHERE ID=" + id, null);
 
 		// Assumes there is only one reply from the database, as ID is primary
 		// key of events.
 		c.moveToFirst();
 
 		// Put all the columns into the map, so as to transfer all the
 		// information found by the search
 		Log.v("DatabaseHelper", DatabaseUtils.dumpCursorToString(c));
 		for (int i = 0; i < c.getColumnCount(); i++) {
 			if (c.getColumnName(i).equalsIgnoreCase("description")) {
 				stringMap.put(c.getColumnName(i),
 						getLocalEventDescription(c.getString(i)));
 			} else {
 				stringMap.put(c.getColumnName(i),
 						getLocalEventTitle(c.getString(i)));
 			}
 		}
 
 		c.close();
 		db.close();
 
 		return stringMap;
 	}
 
 	/**
 	 * Checks if there are any events in the database.
 	 * 
 	 * @returns A boolean stating whether there are any events in the database
 	 */
 	public boolean dbHaveEvents() {
 		SQLiteDatabase db = getReadableDatabase();
 		Cursor c = db.rawQuery("SELECT * FROM "
 				+ DatabaseContract.Event.TABLE_NAME, null);
 
 		boolean haveEvents = (c.getCount() > 0);
 
 		c.close();
 		db.close();
 
 		return haveEvents;
 	}
 
 	/**
 	 * Gets a list of the contacts stored in the application.
 	 * 
 	 * @return A list of contact objects.
 	 */
 	public List<Contact> dbGetContactList() {
 		List<Contact> contacts = new ArrayList<Contact>();
 
 		SQLiteDatabase db = getReadableDatabase();
 		Cursor cursor = db.rawQuery("SELECT "
 				+ DatabaseContract.Contact.COLUMN_NAME_ID + COMMA
 				+ DatabaseContract.Contact.COLUMN_NAME_NAME + COMMA
 				+ DatabaseContract.Contact.COLUMN_NAME_PHONE + " FROM "
 				+ DatabaseContract.Contact.TABLE_NAME, null);
 
 		for (int i = 0; i < cursor.getCount(); i++) {
 			cursor.moveToPosition(i);
 			Contact contact = new Contact(cursor.getString(1), cursor.getInt(0));
 			try {
 				contact.setPhoneNumber(cursor.getString(2));
 			} catch (NumberFormatException nfe) {
 				contact.setPhoneNumber("");
 			}
 			contacts.add(contact);
 		}
 
 		cursor.close();
 		db.close();
 
 		return contacts;
 	}
 
 	/**
 	 * Gets the list of alarm types that are used in the application.
 	 * 
 	 * @return A list of the names of the alarm types that are used.
 	 */
 	public List<String> dbGetAlarmTypes() {
 		List<String> alarm = new ArrayList<String>();
 
 		SQLiteDatabase db = getReadableDatabase();
 		Cursor cursor = db.rawQuery("SELECT "
 				+ DatabaseContract.AlarmTypes.COLUMN_NAME_DESCRIPTION
 				+ " FROM " + DatabaseContract.AlarmTypes.TABLE_NAME, null);
 
 		for (int i = 0; i < cursor.getCount(); i++) {
 			cursor.moveToPosition(i);
 			alarm.add(cursor.getString(0));
 		}
 
 		cursor.close();
 		db.close();
 
 		return alarm;
 	}
 
 	/**
 	 * Add a contact to the application's local list of contacts from the
 	 * phone's global contact list.
 	 * 
 	 * @param id
 	 *            - the id of the contact in the phone's contact list.
 	 * @return The contact's name if successful, None otherwise.
 	 */
 	public String dbAddContact(String id) {
 		// Look up the name in the contacts table
 		Uri uri = ContactsContract.Contacts.CONTENT_URI;
 		String[] projection = new String[] { ContactsContract.Contacts.DISPLAY_NAME };
 		String selection = ContactsContract.Contacts._ID + " = " + id;
 		String[] selectionArgs = null;
 		String orderBy = null;
 
 		Cursor cursor = context.getContentResolver().query(uri, projection,
 				selection, selectionArgs, orderBy);
 
 		cursor.moveToFirst();
 		String name = cursor.getString(0);
 		cursor.close();
 
 		// Now make another query to get the phone number of the contact
 		uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
 		projection = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER };
 		selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
 		selectionArgs = new String[] { id };
 		cursor = context.getContentResolver().query(uri, projection, selection,
 				selectionArgs, orderBy);
 
 		cursor.moveToFirst();
 		String pNumber = cursor.getString(0);
 		cursor.close();
 
 		// Then store information to database
 		SQLiteDatabase db = getWritableDatabase();
 		ContentValues values = new ContentValues();
 		values.put(DatabaseContract.Contact.COLUMN_NAME_NAME, name);
 		values.put(DatabaseContract.Contact.COLUMN_NAME_PHONE, pNumber);
 
 		long newRowId = db.insert(DatabaseContract.Contact.TABLE_NAME, null,
 				values);
 		db.close();
 
 		// Return the name of the contact if sucessful, null if contact not
 		// saved
 		return (newRowId > -1) ? name : null;
 	}
 
 	/**
 	 * Add a contact to the application's local list of contacts. Stores the
 	 * name and phone number of the contact.
 	 * 
 	 * @param contact
 	 *            - a contact object that contains the name and phone number.
 	 */
 	public void dbAddContact(Contact contact) {
 		SQLiteDatabase db = getReadableDatabase();
 
 		ContentValues values = new ContentValues();
 		values.put(DatabaseContract.Contact.COLUMN_NAME_NAME, contact.getName());
 		values.put(DatabaseContract.Contact.COLUMN_NAME_PHONE,
 				contact.getPhoneNumber());
 
 		db.insert(DatabaseContract.Contact.TABLE_NAME, null, values);
 		db.close();
 	}
 
 	/**
 	 * Gets a contact from the local application database, found by id.
 	 * 
 	 * @param id
 	 *            - the id of the contact
 	 * @return a contact object containing the name, phone number and id of the
 	 *         contact.
 	 */
 	public Contact dbGetContact(int id) {
 		SQLiteDatabase db = getReadableDatabase();
 		String table = DatabaseContract.Contact.TABLE_NAME;
 		String[] projection = new String[] {
 				DatabaseContract.Contact.COLUMN_NAME_NAME,
 				DatabaseContract.Contact.COLUMN_NAME_PHONE };
 		String selection = DatabaseContract.Contact.COLUMN_NAME_ID + " = " + id;
 		String[] selectionArgs = null;
 		String orderBy = null;
 		Cursor cursor = db.query(table, projection, selection, selectionArgs,
 				null, null, orderBy);
 		cursor.moveToFirst();
 		Contact contact = new Contact(cursor.getString(0), id);
 		contact.setPhoneNumber(cursor.getString(1));
 		cursor.close();
 		db.close();
 		return contact;
 	}
 
 	/**
 	 * Synchronizes the information stored in the database with the contact
 	 * object.
 	 * 
 	 * @param contact
 	 *            - the contact object to synchronize to.
 	 * @return a boolean that is true if the update was successfull.
 	 */
 	public boolean dbUpdateContact(Contact contact) {
 		SQLiteDatabase db = getReadableDatabase();
 
 		ContentValues values = new ContentValues();
 		values.put(DatabaseContract.Contact.COLUMN_NAME_NAME, contact.getName());
 		values.put(DatabaseContract.Contact.COLUMN_NAME_PHONE,
 				contact.getPhoneNumber());
 
 		String selection = DatabaseContract.Contact.COLUMN_NAME_ID + " = "
 				+ contact.getId();
 		String[] selectionArgs = null;
 
 		boolean updated = (db.update(DatabaseContract.Contact.TABLE_NAME,
 				values, selection, selectionArgs) > 0);
 		db.close();
 
 		return updated;
 	}
 
 	/**
 	 * Clear all data from the database.
 	 */
 	public void dbClearAllData() {
 		SQLiteDatabase db = getWritableDatabase();
 		reset(db);
 	}
 
 	/**
 	 * Deletes contact from the database, using the id stored in the contact
 	 * object.
 	 * 
 	 * @param contact
 	 *            - the object representing the contact that should be deleted.
 	 */
 	public void dbDeleteContact(Contact contact) {
 		SQLiteDatabase db = getWritableDatabase();
 		String table = DatabaseContract.Contact.TABLE_NAME;
 		String whereClause = DatabaseContract.Contact.COLUMN_NAME_ID + " = "
 				+ contact.getId();
 		String[] whereArgs = null;
 		db.delete(table, whereClause, whereArgs);
 		db.close();
 	}
 
 	/**
 	 * Resets the database - clears all the tables, and creates tables according
 	 * to the current specifiactions.
 	 * 
 	 * @param db
 	 *            - The database object to clear.
 	 */
 	public void reset(SQLiteDatabase db) {
 		try {
 			db.execSQL("DROP TABLE " + DatabaseContract.EventType.TABLE_NAME);
 		} catch (SQLiteException e) {
 			// Do nothing...
 		}
 		try {
 			db.execSQL("DROP TABLE " + DatabaseContract.Event.TABLE_NAME);
 		} catch (SQLiteException e) {
 			// Do nothing...
 		}
 		try {
 			db.execSQL("DROP TABLE " + DatabaseContract.Contact.TABLE_NAME);
 		} catch (SQLiteException e) {
 			// Do nothing...
 		}
 		try {
 			db.execSQL("DROP TABLE " + DatabaseContract.AlarmTypes.TABLE_NAME);
 		} catch (SQLiteException e) {
 			// Do nothing...
 		}
 		onCreate(db);
 	}
 }
