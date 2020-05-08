 package no.ntnu.stud.fallprevention;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.provider.ContactsContract;
 import android.net.Uri;
 
 /**
  * Database helper is an object that builds and maintains the database. It also
  * works as an interface to the database.
  * 
  * @author elias
  *
  */
 
 public class DatabaseHelper extends SQLiteOpenHelper {
 
 	public static final int DATABASE_VERSION = 21;
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
 	
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		// Fill the database with some random entries, and of course build the tables
 		final String CREATE_TABLE_1 = 
 				"CREATE TABLE " + DatabaseContract.EventType.TABLE_NAME + START_PAR +
 				DatabaseContract.EventType.COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
 				DatabaseContract.EventType.COLUMN_NAME_TITLE + COMMA +
 				DatabaseContract.EventType.COLUMN_NAME_DESCRIPTION + COMMA +
 				DatabaseContract.EventType.COLUMN_NAME_ICON + END_PAR;
 		final String CREATE_TABLE_2 = 
 				"CREATE TABLE " + DatabaseContract.Event.TABLE_NAME + START_PAR +
 				DatabaseContract.Event.COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
 				DatabaseContract.Event.COLUMN_NAME_TYPEID + END_PAR;
 		final String CREATE_TABLE_3 = 
 				"CREATE TABLE " + DatabaseContract.Contact.TABLE_NAME + START_PAR +
 				DatabaseContract.Contact.COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
 				DatabaseContract.Contact.COLUMN_NAME_NAME + COMMA +
 				DatabaseContract.Contact.COLUMN_NAME_PHONE + END_PAR;
 		final String CREATE_TABLE_4 = 
 				"CREATE TABLE " + DatabaseContract.AlarmTypes.TABLE_NAME + START_PAR +
 				DatabaseContract.AlarmTypes.COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
 				DatabaseContract.AlarmTypes.COLUMN_NAME_DESCRIPTION + END_PAR;
 		final String FILL_INFO_1 = 
 				"INSERT INTO EventType (TypeID, Description, Headline, Icon) VALUES (0, \'You should really keep working out and not be a lazy bastard!\', \'You are lazy!\', \'halo\')";
 		final String FILL_INFO_2 = 
 				"INSERT INTO EventType (TypeID, Description, Headline, Icon) VALUES (1, \'You are really cool, beacause you spend a lot of time clicking through our program!\', \'You are awesome!\', \'sleep\')";
 		final String FILL_INFO_3 = 
 				"INSERT INTO Event (ID, TypeID) VALUES (0, 0)";
 		final String FILL_INFO_4 = 
 				"INSERT INTO Event (ID, TypeID) VALUES (1, 1)";
 		final String FILL_INFO_5 = 
 				"INSERT INTO Event (ID, TypeID) VALUES (2, 1)";
 		final String FILL_INFO_6 = 
 				"INSERT INTO Contact (PersonID, Name, PhoneNumber) VALUES (0, 'Dat-danny Pham', 47823094)";
 		final String FILL_INFO_7 = 
 				"INSERT INTO Contact (PersonID, Name, PhoneNumber) VALUES (1, 'Fyllip Larzzon', 2356094)";
 		final String FILL_INFO_8 =
 				"INSERT INTO AlarmTypes (AlarmID, Description) VALUES (0, 'SMS if risk is high')";
 		final String FILL_INFO_9 =
 				"INSERT INTO AlarmTypes (AlarmID, Description) VALUES (1, 'SMS if sudden spike')";
 		final String FILL_INFO_10 =
 				"INSERT INTO AlarmTypes (AlarmID, Description) VALUES (2, 'SMS if gradual improvement')";
 		final String FILL_INFO_11 =
 				"INSERT INTO AlarmTypes (AlarmID, Description) VALUES (3, 'SMS if fall')";
 		
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
		
		db.close();
 	}
 	
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		// Clears the database on an upgrade, and reset it
 		reset(db);
 		db.execSQL("DROP TABLE " + DatabaseContract.EventType.TABLE_NAME);
 		db.execSQL("DROP TABLE " + DatabaseContract.Event.TABLE_NAME);
 		db.execSQL("DROP TABLE " + DatabaseContract.Contact.TABLE_NAME);
 		db.execSQL("DROP TABLE " + DatabaseContract.AlarmTypes.TABLE_NAME);
 		onCreate(db);
 	}
 	
 	@Override
 	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		// Does the same as if the database has been upgraded.
 		onUpgrade(db, oldVersion, newVersion);
 	}
 
 	/**
 	 * Fetches a list of (Event.ID, EventType.Description, EventType.Icon)
 	 * tuples from the database.   
 	 * 
 	 * @return 
 	 */
 	public List<Event> dbGetEventList() {
 		List<Event> events = new ArrayList<Event>();
 		SQLiteDatabase db = getReadableDatabase();
 		
 		Cursor c = db.rawQuery("SELECT " + 
 				DatabaseContract.EventType.COLUMN_NAME_TITLE + COMMA +
 				DatabaseContract.Event.COLUMN_NAME_ID + COMMA +
 				DatabaseContract.EventType.COLUMN_NAME_ICON + 
 				" FROM " + DatabaseContract.Event.TABLE_NAME + " INNER JOIN " +
 				DatabaseContract.EventType.TABLE_NAME + " ON " + 
 				DatabaseContract.Event.TABLE_NAME + DOT + DatabaseContract.Event.COLUMN_NAME_TYPEID + EQUAL + 
 				DatabaseContract.EventType.TABLE_NAME + DOT + DatabaseContract.EventType.COLUMN_NAME_ID, null);
 
 		// Iterate over the data fetched
 		for (int i = 0; i < c.getCount(); i++) {
 			c.moveToPosition(i);
 			Event e = new Event( c.getString(0), Integer.parseInt(c.getString(1)), c.getString(2) );
 			events.add(e);
 		}
 		
 		// Close database connection
 		c.close();
 		db.close();
 		
 		return events;
 	}
 	
 	/**
 	 * Deletes the event with the given ID from the database.
 	 * 
 	 * @return True if an event was deleted, false if no event with the given id exists or an error occured.
 	 */
 	public boolean dbDeleteEvent(int id) {
 		int rowsAffected = 0;
 		try {
 			SQLiteDatabase db = getWritableDatabase();
 			rowsAffected = db.delete(DatabaseContract.Event.TABLE_NAME, DatabaseContract.Event.COLUMN_NAME_ID + " = ?", new String[] {String.valueOf(id)});
 			db.close();
 		} catch (SQLiteException e) {
 			return false;
 		}
 		return (rowsAffected > 0);
 	}
 	
 	/**
 	 * Fetches information about a single event from the database. 
 	 * 
 	 * @param ID of the event in the database
 	 * 
 	 * @return A map where the column names are keys
 	 */
 	public Map<String, String> dbGetEventInfo(int id) {
 		Map<String, String> stringMap = new HashMap<String, String>();
 		
 		SQLiteDatabase db = getReadableDatabase();
 		
 		Cursor c = db.rawQuery("SELECT " + DatabaseContract.EventType.COLUMN_NAME_DESCRIPTION + ", " +
 				DatabaseContract.EventType.COLUMN_NAME_TITLE + " FROM " +
 				DatabaseContract.Event.TABLE_NAME + " INNER JOIN " +
 				DatabaseContract.EventType.TABLE_NAME + " ON " +
 				DatabaseContract.Event.TABLE_NAME + "." + DatabaseContract.Event.COLUMN_NAME_TYPEID +
 				"=" + DatabaseContract.EventType.TABLE_NAME + "." + DatabaseContract.EventType.COLUMN_NAME_ID +
 				" WHERE ID=" + id, null);
 		
 		// Assumes there is only one reply from the database, as ID is primary key of events.
 		c.moveToFirst();
 		
 		// Put all the columns into the map, so as to transfer all the information found by the search
 		for (int i = 0; i<c.getColumnCount(); i++) {
 			stringMap.put(c.getColumnName(i), c.getString(i));
 		}
 		
 		c.close();
 		db.close();
 		
 		return stringMap;
 	}
 	
 	/**
 	 * Checks if there are any events to read
 	 * 
 	 * @returns True if there are any events in the database
 	 */
 	public boolean dbHaveEvents() {
 		SQLiteDatabase db = getReadableDatabase();
 		Cursor c = db.rawQuery("SELECT * FROM " + DatabaseContract.Event.TABLE_NAME, null);
 		
 		boolean haveEvents = (c.getCount() > 0);
 		
 		c.close();
 		db.close();
 		
 		return haveEvents;
 	}
 	
 	/**
 	 * 
 	 * 
 	 * @return
 	 */
 	public RiskStatus dbGetStatus() {
 		double random = new Random().nextDouble();
 		RiskStatus returner = RiskStatus.VERY_GOOD_JOB;
 		if (random < 0.20) {
 			returner = RiskStatus.BAD_JOB;
 		}
 		else if (random < 0.4) {
 			returner = RiskStatus.NOT_SO_OK_JOB;
 		}
 		else if (random < 0.6) {
 			returner = RiskStatus.OK_JOB;
 		}
 		else if (random < 0.8) {
 			returner = RiskStatus.GOOD_JOB;
 		}
 		return returner;
 	}
 	
 	public List<Double> dbGetRiskHistory(int length) {
 		List<Double> riskHistory = new ArrayList<Double>();
 		Random r = new Random();
 		for (int i = 0; i < length; i++) {
 			riskHistory.add(r.nextDouble()*10);
 		}
 		return riskHistory;
 	}
 	
 	public List<Contact> dbGetContactList() {
 		List<Contact> contacts = new ArrayList<Contact>();
 		
 		SQLiteDatabase db = getReadableDatabase();
 		Cursor cursor = db.rawQuery("SELECT " + DatabaseContract.Contact.COLUMN_NAME_ID + COMMA +
 				DatabaseContract.Contact.COLUMN_NAME_NAME + COMMA +
 				DatabaseContract.Contact.COLUMN_NAME_PHONE + " FROM " + 
 				DatabaseContract.Contact.TABLE_NAME, null);
 		
 		for (int i = 0; i<cursor.getCount(); i++) {
 			cursor.moveToPosition(i);
 			Contact contact = new Contact();
 			contact.setId(cursor.getInt(0));
 			contact.setName(cursor.getString(1));
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
 	
 	public List<String> dbGetAlarmTypes() {
 		List<String> alarm = new ArrayList<String>();
 		
 		SQLiteDatabase db = getReadableDatabase();
 		Cursor cursor = db.rawQuery("SELECT " + DatabaseContract.AlarmTypes.COLUMN_NAME_DESCRIPTION + 
 				" FROM " + DatabaseContract.AlarmTypes.TABLE_NAME, null);
 		
 		for (int i = 0; i<cursor.getCount(); i++) {
 			cursor.moveToPosition(i);
 			alarm.add(cursor.getString(0));
 		}
 		
 		cursor.close();
 		db.close();
 		
 		return alarm;
 	}
 	/**
 	 * 
 	 * @param id
 	 * @return The contact's name
 	 */
 	public String dbAddContact(String id) {		
 		// Look up the name in the contacts table
 		Uri uri = ContactsContract.Contacts.CONTENT_URI;
 		String[] projection = new String[] {
 				ContactsContract.Contacts.DISPLAY_NAME
 		};
 		String selection = ContactsContract.Contacts._ID + " = " + id;
 		String[] selectionArgs = null;
 		String orderBy = null;
 		
 		Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, orderBy);
 		
 		cursor.moveToFirst();
 		String name = cursor.getString(0);
 		cursor.close();
 		
 		// Now make another query to get the phone number of the contact
 		uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
 		projection = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER };
 		selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
 		selectionArgs = new String[]{ id };
 		cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, orderBy);
 		
 		cursor.moveToFirst();
 		String pNumber = cursor.getString(0);
 		cursor.close();
 		
 		// Then store information to database
 		SQLiteDatabase db = getWritableDatabase();
 		ContentValues values = new ContentValues();
 		values.put(DatabaseContract.Contact.COLUMN_NAME_NAME, name);
 		values.put(DatabaseContract.Contact.COLUMN_NAME_PHONE, pNumber);
 		
 		long newRowId = db.insert(DatabaseContract.Contact.TABLE_NAME, null, values);
 		db.close();
 	
 		// Return the name of the contact if sucessful, null if contact not saved
 		return (newRowId > -1) ? name : null;
 	}
 	
 	public Contact dbGetContact(int id) {
 		SQLiteDatabase db = getReadableDatabase();
 		String table = DatabaseContract.Contact.TABLE_NAME;
 		String[] projection = new String[] { 
 			DatabaseContract.Contact.COLUMN_NAME_NAME,
 			DatabaseContract.Contact.COLUMN_NAME_PHONE
 		};
 		String selection = DatabaseContract.Contact.COLUMN_NAME_ID + " = " + id;
 		String[] selectionArgs = null;
 		String orderBy = null;
 		Cursor cursor = db.query(table, projection, selection, selectionArgs, null, null, orderBy);
 		cursor.moveToFirst();
 		Contact contact = new Contact();
 		contact.setId(id);
 		contact.setName(cursor.getString(0));
 		contact.setPhoneNumber(cursor.getString(1));
 		cursor.close();
 		db.close();
 		return contact;
 	}
 	
 	public boolean dbUpdateContact(Contact contact) {
 		SQLiteDatabase db = getReadableDatabase();
 		
 		ContentValues values = new ContentValues();
 		values.put(DatabaseContract.Contact.COLUMN_NAME_NAME, contact.getName());
 		values.put(DatabaseContract.Contact.COLUMN_NAME_PHONE, contact.getPhoneNumber());
 		
 		String selection = DatabaseContract.Contact.COLUMN_NAME_ID + " = " + contact.getId();
 		String[] selectionArgs = null;
 
 		boolean updated = (db.update(DatabaseContract.Contact.TABLE_NAME, values, selection, selectionArgs) > 0);
 		db.close();
 		
 		return updated;
 	}
 	
 	public void dbClearAllData() {
 		SQLiteDatabase db = getWritableDatabase();
 		reset(db);
 	}
 	
 	public void reset(SQLiteDatabase db) {
 		db.execSQL("DROP TABLE " + DatabaseContract.EventType.TABLE_NAME);
 		db.execSQL("DROP TABLE " + DatabaseContract.Event.TABLE_NAME);
 		db.execSQL("DROP TABLE " + DatabaseContract.Contact.TABLE_NAME);
 		db.execSQL("DROP TABLE " + DatabaseContract.AlarmTypes.TABLE_NAME);
 		onCreate(db);
 	}
 }
