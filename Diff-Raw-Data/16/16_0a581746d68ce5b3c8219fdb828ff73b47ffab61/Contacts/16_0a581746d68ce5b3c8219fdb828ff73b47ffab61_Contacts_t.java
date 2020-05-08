 package ch.ffhs.esa.lifeguard.domain;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 import ch.ffhs.esa.lifeguard.persistence.TableGateway;
 
 /**
  * @author Juerg Gutknecht <juerg.gutknecht@students.ffhs.ch>
  *
  */
 public class Contacts
 	extends TableGateway
 	implements ContactsInterface {
 
 	
 	
 	/*//////////////////////////////////////////////////////////////////////////
 	 * CLASS CONSTANTS
 	 */
 	
 	private static final String COLUMN_NAME = "name";
 	private static final String COLUMN_PHONE = "phone";
 	private static final String COLUMN_POSITION = "position";
 	
 	private static final int COLUMN_INDEX_NAME = 1;
 	private static final int COLUMN_INDEX_PHONE = 2;
 	private static final int COLUMN_INDEX_POSITION = 3;
 	
 	
 	/*//////////////////////////////////////////////////////////////////////////
 	 * INITIALIZATION
 	 */
 	
 	public Contacts(SQLiteOpenHelper helper) {
 		super(helper, "contacts");
 	}
 	
 	
 	/*//////////////////////////////////////////////////////////////////////////
 	 * TABLE CREATION AND UPDATING
 	 */
 	
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		Log.d(Contacts.class.toString(), "onCreate...");
 		try {
 			String query = "CREATE TABLE " + this.getTable()
 				     + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT"
 				     + ", " + COLUMN_NAME + " TEXT NOT NULL"
 				     + ", " + COLUMN_PHONE + " TEXT NOT NULL"
 				     + ", " + COLUMN_POSITION + " UNSIGNED INTEGER NOT NULL DEFAULT 0"
 				     + ");";
 			Log.d(Contacts.class.toString(), "Creating table...");
 			Log.d(Contacts.class.toString(), query);
 			db.execSQL(query);
 			Log.d(Contacts.class.toString(), "Table created.");
 			
 			Log.d(Contacts.class.toString(), "Inserting test data...");
 			db.execSQL("INSERT INTO " + this.getTable() + " (name, phone, position) VALUES ('Jane Doe', '0123456789', 1);");
 			db.execSQL("INSERT INTO " + this.getTable() + " (name, phone, position) VALUES ('John Doe', '0234567890', 2);");
 			Log.d(Contacts.class.toString(), "Test data inserted.");
 		} catch (SQLiteException e) {
 			Log.d(Contacts.class.toString(), e.getMessage());
 		}
 	};
 	
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		Log.d(Contacts.class.toString(), "onUpdate...");
 		Log.d(Contacts.class.toString(), "Dropping table if exists...");
 		db.execSQL("DROP TABLE IF EXISTS " + this.getTable());
 		Log.d(Contacts.class.toString(), "Table dropped.");
 		this.onCreate(db);
 	}
 
 	
 	/*//////////////////////////////////////////////////////////////////////////
 	 * CRUD OPERATIONS
 	 */
 	
 	@Override
 	public long persist(ContactInterface object) {
 		Log.d(Contacts.class.getName(), "Persisting " + object);
 		ContentValues values = new ContentValues();
 		values.put(COLUMN_NAME, object.getName());
 		values.put(COLUMN_PHONE, object.getPhone());
 		values.put(COLUMN_POSITION, object.getPosition());
 		
 		long id = -1L;
 		
 		try {
 			if (object.getId() > 0) {
 				this.getWritableDatabase().update(
 					this.getTable(),
 					values,
 					COLUMN_ID + " = ?",
 					new String[] {String.valueOf(object.getId())}
 				);
 				id = object.getId();
 			} else {
 				int pos = this.getMaxPosition() + 1;
 				values.put(COLUMN_POSITION, pos);
 				id = this.getWritableDatabase().insert(this.getTable(), null, values);
				object.setId(id);
				object.setPosition(pos);
 			}
 		} catch (SQLiteException e) {
 			Log.e(Contacts.class.getName(), e.getMessage());
 		} finally {
 			this.closeDatabase();
 		}
 
 		return id;
 	}
 
 	@Override
 	public void delete(ContactInterface object) {
 		this.getWritableDatabase().delete(
 			this.getTable(),
 			"_id = ?",
 			new String[] {String.valueOf(object.getId())}
 		);
 		this.closeDatabase();
 	}
 
 	@Override
 	public List<ContactInterface> getAll() {
 		Cursor cursor = null;
 		try {
 			cursor = this.getReadableDatabase().rawQuery(
 				"SELECT * FROM " + this.getTable() + " WHERE ? ORDER BY " + COLUMN_POSITION,
 				new String[] {"1"}
 			);
 		} catch (IllegalStateException e) {
 			Log.e(Contacts.class.toString(), e.getMessage());
 		} catch (SQLiteException e) {
 			Log.e(Contacts.class.toString(), e.getMessage());
 		}
 		
 		List<ContactInterface> contacts = new ArrayList<ContactInterface>();
 		
 		if (null != cursor && cursor.moveToFirst()) {
 			while (!cursor.isAfterLast()) {
 				contacts.add(this.createContact(cursor));
 				cursor.moveToNext();
 			}
 		}
 		
 		return contacts;
 	}
 
 	@Override
 	public ContactInterface findById(long id) {
 		Cursor cursor = this.getReadableDatabase().rawQuery(
 			"SELECT * FROM "
 		    + this.getTable()
 		    + " WHERE " + COLUMN_ID + " = ? LIMIT 0, 1",
 			new String[] {String.valueOf(id)}
 		);
 		
 		ContactInterface contact;
 		
 		if (null != cursor && cursor.moveToFirst()) {
 			contact = this.createContact(cursor);
 		} else {
 			contact = new Contact();
 		}
 		this.closeDatabase();
 		return contact;
 	}
 	
 	
 	/*//////////////////////////////////////////////////////////////////////////
 	 * PROTECTED OPERATIONS
 	 */
 
 	protected ContactInterface createContact(Cursor cursor) {
 		ContactInterface contact = new Contact();
 		
 		contact.setId(cursor.getLong(COLUMN_INDEX_ID));
 		contact.setName(cursor.getString(COLUMN_INDEX_NAME))
 			   .setPhone(cursor.getString(COLUMN_INDEX_PHONE))
 			   .setPosition(cursor.getInt(COLUMN_INDEX_POSITION));
 		
 		return contact;
 	}
 	
 	protected int getMaxPosition() {
 		String query = "SELECT MAX(" + COLUMN_POSITION + ") FROM " + this.getTable();
 		Cursor c = this.getReadableDatabase().rawQuery(query, null);
 		
 		int pos = -1;
 		if (null != c && c.moveToFirst()) {
 			pos = c.getInt(0);
 		}
 		
 		return pos;
 	}
 }
